package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader
import scala.collection.mutable.HashMap
import java.io.PrintWriter
import org.ddahl.rscala
import scala.collection.mutable.ArrayBuffer
import java.util.Arrays
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory

class MedicationMapper extends ProjectwideGlobals
{   
    val connect: ConnectToGraphDB = new ConnectToGraphDB
    
    def runMedicationMapping(cxn: RepositoryConnection): Boolean =
    {
        val f: ValueFactory = cxn.getValueFactory()
        // First, check if all necessary files are in order. If not, do not run medication mapping
        if (!checkForNecessaryMedmapRequirements())
        {
            logger.info("Not all necessary dependencies are in place to run medication mapping, skipping this function...") 
            false
        }
        else
        {
            //If everything is in order, pull necessary data for med mapping
            var unmappedMeds: ArrayBuffer[ArrayBuffer[Value]] = getAllUnmappedMedsInfo(cxn)
            //Create mapping from order names to prescriptions, if there are elements in the list
            if (unmappedMeds.size == 0)
            {
                logger.info("No unmapped medications were found in the triplestore.")
                false
            }
            else
            {
                var mapOrdersToPrescripts: HashMap[String, ArrayBuffer[Value]] = new HashMap[String, ArrayBuffer[Value]]
                var medList: String = ""
                for (a <- unmappedMeds)
                {
                    val ordName: String = a(1).toString.split("\\^\\^<http://www.w3.org/2001/XMLSchema#string>")(0)
                    logger.info("ordername: " + ordName)
                    val prescript: Value = a(0)
                    medList += ordName
                    if (a(1) != unmappedMeds(unmappedMeds.size - 1)(1)) medList += ", "
                    if (mapOrdersToPrescripts.contains(ordName)) mapOrdersToPrescripts(ordName) += prescript
                    else mapOrdersToPrescripts += ordName -> ArrayBuffer(prescript)
                }
                //make call to R script
                val Rresults: Array[Array[String]] = processMedsWithR(medList)
                logger.info("printing r results")
                for (a <- Rresults)
                {
                  println(a(0) + " " + a(1))
                }
                var model: Model = new LinkedHashModel()
                //match R results with mapping info and add statement to model
                val medPredicate: IRI = f.createIRI("http://purl.obolibrary.org/obo/IAO_0000142")
                for (result <- Rresults)
                {
                    for (uri <- mapOrdersToPrescripts("\"" + result(0) + "\""))
                    {
                        model.add(uri.asInstanceOf[IRI], medPredicate, f.createIRI(helper.removeAngleBracketsFromString(result(1))))
                    }
                }
                cxn.begin()
                cxn.add(model, f.createIRI("http://www.itmat.upenn.edu/biobank/expanded"))
                cxn.commit()
            }
        }
        true
    }
            
    def processMedsWithR(medList: String): Array[Array[String]] =
    {
          logger.info("starting R processing...")
          val R = org.ddahl.rscala.RClient()
          R.evalS2("""
            # check the setwd statement and path portions of load/read statements
  
            library(solr)
            
            # for rbindfill
            library(plyr)
            
            library(stringdist)
            
            #for str_count
            library(stringr)
            
            # for the svm
            library(e1071)
            
            # NEW dependency
            library(SPARQL)
            
            saf.val <- FALSE
            
            # check current working directory
            
            # put all filenames here
            med.stand.fn <- """" + medStandardsFile + """"
            
            med_map_svm_file <- """" + SVMfile + """"
            
            endpoint <- """" + serviceURL + """/repositories/""" + dronRepo + """"
                            
            # do you have solr running with the necessary collection ?!
            # url for solr
            forward.url <- """" + solrURL + """"
            
            num.hits.desired <- 20
            
            ###
            
            load(med_map_svm_file)
            
            # unknown orders that we want to map
            med_order <- c(""" + medList + """)
            
            get.solr.frame <-
            function(search.url,
                     search.term,
                     my.qf,
                     rf,
                     num.results) {
              current.query <- URLencode(search.term, reserved = TRUE)
              
              current.query <- paste0(my.qf, "(", current.query, ")")
              
              temp <- solr_search(
                q = current.query,
                rows = num.results,
                base = search.url,
                fl = rf
              )
              
              scaled.ranks <- 0
              
              if (!is.null(temp)) {
                all.scores <- as.numeric(temp$score)
                raw.ranks <-
                  rank(x = as.numeric(all.scores), ties.method = "max")
                maxrank <- max(raw.ranks)
                inv.ranks <- maxrank - raw.ranks
                scaled.ranks <- inv.ranks / maxrank
                
                # print(temp)
              }
              
              temp$query <- as.character(search.term)
              temp$scaled.rank <- scaled.ranks
              
              # print(temp)
              
              return(as.data.frame(temp))
              
            }
          
          # vector of unique orders
          qs.for.forward <- sort(unique(med_order))
          qs.for.forward <- qs.for.forward[nchar(qs.for.forward) > 0]
          
          device_etc_list_onecol <-
            read.delim(
              "C://Users//Hayden_Freedman//PennTURBO//Drivetrain//utilities//r//medication_mapping//device_etc_list_onecol.txt",
              header = FALSE,
              stringsAsFactors = FALSE,
              sep = "\t",
              quote = ""
            )
          
          device_etc_list_onecol <-
            unique(toupper(sort(device_etc_list_onecol$V1)))
          
          keepflag <- !toupper(qs.for.forward) %in% device_etc_list_onecol
          
          qs.for.forward <- qs.for.forward[keepflag]
          
          # apply anonymous function containing "get.solr.frame" to each member of vector qs.for.forward
          # (as if it were a list)
          # and save the results in LIST forward.results
          forward.results <-
            lapply(qs.for.forward, function(current_order) {
              # print(current_order)
              frtemp <-
                get.solr.frame(
                  forward.url,
                  current_order,
                  # 'dl_t:',
                  'dl:',
                  # "dl_s,d.full_s,brand.extract_s,digflag_s,rxn_s,top.full_s,toplab_s,score",
                  "dl,d.full,brand.extract,digflag,rxn,top.full,toplab,score",
                  num.hits.desired
                )
              
              return(frtemp)
              
            })
          
          # forward.results is a list at this point
          # now row-wise bind them together
          forward.results <- do.call(rbind.fill, forward.results)
          
          names(forward.results) <-
            c(
              "d.full_s",
              "dl_s",
              "rxn_s",
              "top.full_s",
              "toplab_s",
              "digflag_s",
              "brand.extract_s",
              "score",
              "query",
              "scaled.rank"
            )
          
          
          # debugging
          dput(names(med_order))
          
          # c("RAWTOHEX.FK_PATIENT_ENCOUNTER_ID.", "PK_ORDER_MED_ID", "ORDER_NAME",
          #   "FK_MEDICATION_ID", "DOSE", "FREQUENCY_NAME", "QUANTITY", "REFILLS",
          #   "UNIT_OF_MEASURE", "LOCATION_CODE", "LOCATION_DESCRIPTION", "ORDER_DATE",
          #   "ORDER_GROUP", "ORDER_PRIORITY_DESC_NONSTD", "ORDER_PRIORITY_DESCRIPTION",
          #   "ORDER_NAME.tidy")
          
          # "PK_MEDICATION_ID" not available... do we really need it?
          
          # ordering the columns, sorting the rows
          forward.results <-
            forward.results[order(c(forward.results$query, forward.results$dl_s)), c(
              "query",
              "dl_s",
              "d.full_s",
              "rxn_s",
              "brand.extract_s",
              "digflag_s",
              "top.full_s",
              "toplab_s",
              "score",
              "scaled.rank"
            )]
          
          # remove null query
          # shouldn't have any
          forward.results <-
            forward.results[!is.na(forward.results$query), ]
          
          # define the desired string similarity metrics
          # WITHOUT soundex
          # for the following application of stringsim
          flex.char.dist.choices.no_soundex <- c('osa',
                                                 'lv',
                                                 'dl',
                                                 'lcs',
                                                 'qgram',
                                                 'cosine',
                                                 'jaccard',
                                                 'jw')
          
          # inefficient recreation?
          unique.qs <- sort(unique(forward.results$query))
          
          # x will be a list of data structures with multiple string similarity
          # metrics between queries and solr results
          x <- lapply(unique.qs, function(current.q) {
            print(current.q)
            current.matches <-
              sort(unique(forward.results$dl_s[forward.results$query == current.q]))
            current.matches <- setdiff(current.matches, NA)
            
            multi.scores <-
              lapply(flex.char.dist.choices.no_soundex, function(current.meth) {
                # print(current.meth)
                current.scores <-
                  stringsim(
                    tolower(current.q),
                    tolower(current.matches),
                    method = current.meth,
                    nthread = 6
                  )
                # print(length(current.scores))
                if (length(current.scores) > 0) {
                  return(current.scores)
                }
                
              })
            multi.scores <- do.call(cbind.data.frame, multi.scores)
            if (nrow(multi.scores) > 0) {
              names(multi.scores) <- flex.char.dist.choices.no_soundex
              
              multi.scores$query <- current.q
              multi.scores$match <- current.matches
              
              return(multi.scores)
            }
            
          })
          
          # convert list of data frames x into one big data frame
          
          x <- do.call(rbind.data.frame, x)
          
          # merge solr results and string similarity results
          # distance 1/similarity
          forward.with.dist <-
            merge(
              x = forward.results,
              y = x,
              by.x = c("query", "dl_s"),
              by.y = c("query", "match")
            )
          
          # get some more similarity info
          # how about string lengths
          forward.with.dist$q.chars <- nchar(forward.with.dist$query)
          forward.with.dist$match.chars <- nchar(forward.with.dist$dl_s)
          forward.with.dist$chardiff <-
            forward.with.dist$q.chars - forward.with.dist$match.chars
          
          
          forward.with.dist$q.words <-
            str_count(forward.with.dist$query, "\\S+")
          forward.with.dist$match.words <-
            str_count(forward.with.dist$dl_s, "\\S+")
          forward.with.dist$worddiff <-
            forward.with.dist$q.words - forward.with.dist$match.words
          
          
          # convert logicals to numerics for numeric svm
          forward.with.dist$brand.extract_s <-
            as.numeric(as.logical(forward.with.dist$brand.extract_s))
          
          forward.with.dist$digflag_s <-
            as.numeric(as.logical(forward.with.dist$digflag_s))
          
          # explicitly show the order of the top drug categories
          # for consistency with trained model
          # but not in use yet
          forward.with.dist$top.full_s <-
            factor(
              forward.with.dist$top.full_s,
              levels = c(
                "http://purl.obolibrary.org/obo/CHEBI_23367",
                "http://purl.obolibrary.org/obo/CHEBI_60004",
                "http://purl.obolibrary.org/obo/DRON_00000005",
                "http://purl.obolibrary.org/obo/DRON_00000015",
                "http://purl.obolibrary.org/obo/DRON_00000016",
                "http://purl.obolibrary.org/obo/DRON_00000017",
                "http://purl.obolibrary.org/obo/DRON_00000018",
                "http://purl.obolibrary.org/obo/DRON_00000019",
                "http://purl.obolibrary.org/obo/DRON_00000020",
                "http://purl.obolibrary.org/obo/DRON_00000021",
                "http://purl.obolibrary.org/obo/DRON_00000022",
                "http://purl.obolibrary.org/obo/DRON_00000024",
                "http://purl.obolibrary.org/obo/DRON_00000026",
                "http://purl.obolibrary.org/obo/OBI_0000047"
              )
            )
          
          # another metric:  how often does each match appear
          matchfreq <- table(forward.with.dist$dl_s)
          matchfreq <-
            cbind.data.frame(names(matchfreq), as.numeric(matchfreq))
          names(matchfreq) <- c("dl_s", "freq")
          matchfreq$dl_s <- as.character(matchfreq$dl_s)
          
          forward.with.dist <-
            merge(x = forward.with.dist,
                  y = matchfreq,
                  by = "dl_s")
          
          # what are the numeric columns that I want to apply the svm regression (machine learning algorithm to?
          regressors <- c(
            "score",
            "scaled.rank",
            "freq",
            "osa",
            "lv",
            "dl",
            "lcs",
            "qgram",
            "cosine",
            "jaccard",
            "jw",
            "chardiff",
            "worddiff",
            "brand.extract_s",
            "digflag_s",
            "top.full_s"
          )
          
          # temp is the fwd data frame with just the desired numerical columns
          temp <- forward.with.dist[, regressors]
          
          # apply the model_svm, which was loaded at the tip of the script, to the numerical data from the unknowns
          # returns a vector of values, nominally 0 to 1 , 1 means solr match probably perfectly indicates what ingredients were intended
          # by the medication order
          # pred <- predict(model_svm, temp)
          
          # add probs back in
          pred <- predict(class_svm, temp,
                          probability = TRUE)
          
          
          # need to hand off query, match and score
          # Hayden and or Mark need to work on that for rscala
          
          # assumed they have the same index
          forward.with.dist$pred <- pred
          forward.with.dist$pred.conf <- attributes(pred)$probabilities[, 1]
          forward.with.dist$contra.conf <- attributes(pred)$probabilities[, 2]
          
          forward.with.dist$distsum  <-
            rowSums(forward.with.dist[, flex.char.dist.choices.no_soundex])
          
          matches <- forward.with.dist[forward.with.dist$pred == 1 , ]
          
          qs.all <- sort(unique(forward.with.dist$query))
          qs.successes <- sort(unique(matches$query))
          
          qs.failures <- setdiff(qs.all, qs.successes)
          
          # # mapping rate
          # print(1 - length(qs.failures) / length(qs.all))
          
          failures <-
            forward.with.dist[!forward.with.dist$query %in% qs.successes , ]
          
          write.csv(x = matches,
                    file = "map_matches_20180503.csv",
                    row.names = FALSE)
          write.csv(x = failures,
                    file = "map_failures_20180503.csv",
                    row.names = FALSE)
          
          # could do this by pred.conf or by distsum
          # bestmatches <- aggregate(
          #   matches$pred.conf,
          #   by = list(matches$query),
          #   FUN = max,
          #   na.rm = TRUE
          # )
          # names(bestmatches) <- c("query", "pred.conf")
          #
          # bestmatches <-
          #   merge(x = matches,
          #         y = bestmatches,
          #         by = c("query", "pred.conf"))
          
          
          bestmatches <- aggregate(
            matches$distsum,
            by = list(matches$query),
            FUN = max,
            na.rm = TRUE
          )
          names(bestmatches) <- c("query", "distsum")
          
          bestmatches <-
            merge(x = matches,
                  y = bestmatches,
                  by = c("query", "distsum"))
          
          
          
          
          
          # why would the freq for two otherwise identical rows be different !?
          # exceptlabels <- setdiff(names(bestmatches), c("dl_s", "freq"))
          # bestmatches <- bestmatches[, exceptlabels]
          bestmatches <- bestmatches[, c("query", "d.full_s")]
          
          bestmatches <- unique(bestmatches)
          
          besttab <- table(bestmatches$query)
          besttab <- cbind.data.frame(names(besttab), as.numeric(besttab))
          names(besttab) <- c("query", "matchcount")
          
          singlematches <- besttab$query[besttab$matchcount == 1]
          singlematches <-
            bestmatches[bestmatches$query %in% singlematches ,]
          
          dupematches <- besttab$query[besttab$matchcount > 1]
          dupematches <- bestmatches[bestmatches$query %in% dupematches ,]
          
          
          query.prefix <-
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          select ?super (str(?labwithtype) as ?suplab) where {
          values ?s { "
          
          query.suffix <- " }
          ?s rdfs:label ?o ;
          rdfs:subClassOf+ ?super .
          ?super rdfs:label ?labwithtype
          filter(isuri(?super))
          }"
          
          too.general <- c(
            "drug capsule",
            "drug product",
            "drug solution",
            "drug suspension",
            "drug tablet",
            "portion of mixture",
            "portion of solution",
            "portion of suspension",
            "processed material"
          )
          
          multi2parent <-
            lapply(sort(unique(dupematches$query)), function(current.query) {
              # current.query <- "CARDIZEM LA 180 MG OR TAB SR 24HR"
              print(current.query)
              match.uris <-
                bestmatches$d.full_s[bestmatches$query == current.query]
              match.uris <-
                paste("<", match.uris, ">", sep = "", collapse = " ")
              match.uris <- paste0(match.uris, collapse = " ")
              my.query <- paste0(query.prefix, match.uris, query.suffix)
              sparql.res <-
                SPARQL::SPARQL(url = endpoint,
                               query = my.query,
                               ns = c())
              my.result <- sparql.res$results
              my.result <- as.data.frame(my.result)
              print(head(my.result))
              
              supertab <- table(my.result$suplab)
              supertab <-
                cbind.data.frame(names(supertab), as.numeric(supertab))
              names(supertab) <- c("superlab", "hits")
              supertab <- supertab[order(supertab$hits, decreasing = TRUE),]
              supertab <-
                supertab[!supertab$superlab %in% too.general,]
              # could supertab have 0 rows?
              if (nrow(supertab) > 0) {
                if (nrow(supertab) > 1) {
                  supervals <- sort(supertab$hits, decreasing = TRUE)
                  if (supervals[1] > supervals[2]) {
                    toplab <- supertab$superlab[supertab$hits == supervals[1]]
                    topuri <-
                      unique(my.result$super[my.result$suplab == toplab])
                    # there is one best parent
                    return(list(current.query, toplab, topuri))
                  } else {
                    # there are multiple equally good parents
                    # get the one with teh shortest label
                    # what if there are multiple equally short lables
                    # or too general parent got in there somehow
                    temp <- nchar(as.character(supertab$superlab))
                    temp <- cbind.data.frame(supertab, temp)
                    temp <- temp[temp$temp == min(temp$temp),]
                    # print("SHORTEST")
                    # print(temp)
                    # print(supertab[supertab$hits == max(supertab$hits),])
                    topuri <-
                      unique(my.result$super[my.result$suplab == temp$superlab])
                    return(list(current.query, temp$superlab, topuri))
                  }
                } else {
                  if (!supertab$superlab %in% too.general) {
                    # print(supertab)
                    topuri <-
                      unique(my.result$super[my.result$suplab == supertab$superlab])
                    # there is only one acceptible parent
                    return(list(current.query, supertab$superlab, topuri))
                  } else {
                    print(supertab)
                    # the best parent is too general
                    return()
                  }
                }
              } else {
                print(my.result)
                # there are no parents
                return()
              }
            })
          
          multi2parent <- do.call(rbind.data.frame, multi2parent)
          if (nrow(multi2parent) > 0) {names(multi2parent) <- c("query", "parent.label", "parent.uri")}
          
          stillunduped <-
            dupematches[!dupematches$query %in% multi2parent$query,]
          print(length(unique(stillunduped$query)))
          print(stillunduped)
          
          
          
          
          ### what if there were no failures... queries that had no solr matches?
          
          final.1 <- multi2parent[, c("query", "parent.uri")]
          final.2 <- singlematches[, c("query", "d.full_s")]
          names(final.1) <- names(final.2)
          final.both <- rbind.data.frame(final.1, final.2)
          
          failed.orders <-
            sort(unique(toupper(c(
              failures$query, stillunduped$query
            ))))
          
          if (length(failed.orders) > 0) {
            #  there has been no string tidying here, except internal to the stringdist calcs
            #  med_order names looks liek tehre all uppercase
            #  med_standard has mixed case
            
            print(med.stand.fn)
            med_standard <-
              read.csv(med.stand.fn,
                       stringsAsFactors = saf.val)
            nrow(med_standard)
            med_standard <- unique(med_standard)
            nrow(med_standard)
            
            med_standard$fn.uc <- toupper(med_standard$FULL_NAME)
            
            redemption <-
              med_standard[med_standard$fn.uc %in% failed.orders &
                             (!is.na(med_standard$RXNORM)), ]
            redemption.rxnorms <- redemption$RXNORM
            redemption.rxnorms <-
              paste('"',
                    redemption.rxnorms,
                    '"',
                    sep = "",
                    collapse = " ")
            
            query.prefix <-
              "PREFIX obo: <http://purl.obolibrary.org/obo/>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            select distinct * where {
            values ?o { "
            query.suffix <- " }
            ?s obo:DRON_00010000 ?o ; rdfs:subClassOf ?super .
            filter(isuri(?super))
            }"
          
            my.query <- paste0(query.prefix, redemption.rxnorms, query.suffix)
            cat(my.query)
            
            sparql.res <- SPARQL::SPARQL(url = endpoint, query = my.query)
            sparql.res <- sparql.res$results
            my.result <- head(sparql.res)
            my.result <- as.data.frame(my.result, stringsAsFactors = FALSE)
            
            ignore.parent <- unique(my.result[, c("o", "s"), ])
            dupe.check <- table(ignore.parent$o)
            dupe.check <-
              cbind.data.frame(names(dupe.check), as.numeric(dupe.check))
            names(dupe.check) <- c("rxnorm", "count")
            
            rxn.singles <-
              ignore.parent[ignore.parent$o %in% dupe.check$rxnorm[dupe.check$count == 1], ]
            rxn.dupes <-
              my.result[my.result$o %in% dupe.check$rxnorm[dupe.check$count > 1], ]
            rxn.dupes <- unique(rxn.dupes[, c("o", "super")])
            
            lingering.dupe.check <- table(rxn.dupes$o)
            lingering.dupe.check <-
              cbind.data.frame(names(lingering.dupe.check),
                               as.numeric(lingering.dupe.check))
            names(lingering.dupe.check) <- c("rxnorm", "parent.count")
            
            parent.sufficient <-
              rxn.dupes[rxn.dupes$o %in% lingering.dupe.check$rxnorm[lingering.dupe.check$parent.count == 1], ]
            
            rxn.still.vague <-
              rxn.dupes[rxn.dupes$o %in% lingering.dupe.check$rxnorm[lingering.dupe.check$parent.count > 1], ]
            
            print(rxn.still.vague)
            
            names(parent.sufficient) <- names(rxn.singles)
            rxn.known <- rbind.data.frame(rxn.singles, parent.sufficient)
            rxn.known$o <- as.numeric(rxn.known$o)
            
            redeemed.redemption <-
              merge(
                x = redemption,
                y = rxn.known,
                by.x = "RXNORM",
                by.y = "o"
              )
            redeemed.redemption$FULL_NAME <-
              toupper(redeemed.redemption$FULL_NAME)
            redeemed.redemption <-
              unique(redeemed.redemption[, c("FULL_NAME", "s")])
            
            redeemed.redemption$s <-
              sub(pattern = "^obo:",
                  replacement = "http://purl.obolibrary.org/obo/",
                  x = redeemed.redemption$s)
            
            
            names(redeemed.redemption) <- names(final.2)
            final.both <-
              unique(rbind.data.frame(final.both, redeemed.redemption))
            
          }
          
          # hand off final.both to calling Scala, but fix incosistent angle brackets on URIs
          return(as.matrix(final.both))
            """)
    }
    
    def tidyOrderName(str: String): String =
    {
        //logger.info("starting tidying")
        //logger.info("input: " + str)
        // to lower case and remove punctuation
        val removeLeadingPunct: String = str.toLowerCase().replaceAll("^[\\!\\#\\$\\%\\&\\'\\*\\+\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\\\^\\_\\`\\|\\~]+", "")
        //logger.info("mid stage: " + removeLeadingPunct)
        val removeTrailingPunct: String = removeLeadingPunct.replaceAll("[\\!\\#\\$\\%\\&\\'\\*\\+\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\\\^\\_\\`\\|\\~]+$", "")
        val removeQuotes: String = removeTrailingPunct.replaceAll("\"", "")
        //logger.info("output: " + removeTrailingPunct)
        removeQuotes
    }
    
    def checkForNecessaryMedmapRequirements(): Boolean =
    {
        var boolToReturn = true
        
        //check for existence of onecol file  
        if (!(new File("..//utilities//r//medication_mapping//device_etc_list_onecol.txt").exists()))
        {
            logger.info("Did not find onecol file")
            boolToReturn = false
        }
        //check for existence of SVM .rdata file
        if (!(new File(SVMfile).exists())) 
        {
            logger.info("Did not find serialized SVM model")
            boolToReturn = false
        }
        //check for existence of med standards file
        if (!(new File(medStandardsFile).exists()))
        {
            logger.info("Did not find medication Standards file")
            boolToReturn = false
        }
        //check that dron repository exists
        try
        {
            val graphconnect: TurboGraphConnection = connect.initializeGraph(dronRepo)  
            connect.closeGraphConnection(graphconnect)
            //can we check to make sure that certain classes are there? all these classes should be there:
            /*
             * "http://purl.obolibrary.org/obo/CHEBI_23367",
              "http://purl.obolibrary.org/obo/CHEBI_60004",
              "http://purl.obolibrary.org/obo/DRON_00000005",
              "http://purl.obolibrary.org/obo/DRON_00000015",
              "http://purl.obolibrary.org/obo/DRON_00000016",
              "http://purl.obolibrary.org/obo/DRON_00000017",
              "http://purl.obolibrary.org/obo/DRON_00000018",
              "http://purl.obolibrary.org/obo/DRON_00000019",
              "http://purl.obolibrary.org/obo/DRON_00000020",
              "http://purl.obolibrary.org/obo/DRON_00000021",
              "http://purl.obolibrary.org/obo/DRON_00000022",
              "http://purl.obolibrary.org/obo/DRON_00000024",
              "http://purl.obolibrary.org/obo/DRON_00000026",
              "http://purl.obolibrary.org/obo/OBI_0000047"
             */
        }
        catch
        {
            case e: NullPointerException => 
            logger.info("Did not find specified Dron repository")
            boolToReturn = false
        }
        //check that Solr instance is running
        try
        {
            scala.io.Source.fromURL(solrURL).mkString
        }
        catch
        {
            case e: Exception =>
            logger.info("Did not find a running Solr instance at the specified address")
            boolToReturn = false
        }
        
        boolToReturn
    }
    
    def getAllUnmappedMedsInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getInfo: String = 
         """
             Select ?prescript ?ordername Where
             {
                 ?prescript a obo:PDRO_0000024 .
                 ?prescript turbo:TURBO_0006512 ?ordername .
             }         
         """
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getInfo, ArrayBuffer("prescript", "ordername"))
    }
    
    def addDrugOntologies(cxn: RepositoryConnection)
    {
        //load chebi lite
        logger.info("loading chebi-lite...")
        helper.addOntologyFromUrl(cxn, "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl", 
                "http://www.itmat.upenn.edu/biobank/drugOntologies")
        logger.info("chebi-lite loaded.")
        //load dron rxnorm
        logger.info("loading dron-rxnorm...")
        helper.addOntologyFromUrl(cxn, "https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl", 
                "http://www.itmat.upenn.edu/biobank/drugOntologies")
        logger.info("dron-rxnorm loaded.")
        //load dron-chebi
        logger.info("loading dron-chebi...")
        helper.addOntologyFromUrl(cxn, "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl", 
                "http://www.itmat.upenn.edu/biobank/drugOntologies")
        logger.info("dron-chebi loaded.")
        //load dron-hand
        logger.info("loading dron-hand...")
        helper.addOntologyFromUrl(cxn, "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl", 
                "http://www.itmat.upenn.edu/biobank/drugOntologies")
        logger.info("dron-hand loaded.")
        //load dron-upper
        logger.info("loading dron-upper...")
        helper.addOntologyFromUrl(cxn, "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl", 
                "http://www.itmat.upenn.edu/biobank/drugOntologies")
        logger.info("dron-upper loaded.")
        //load dron-ingredient
        logger.info("loading dron-ingredient...")
        helper.addOntologyFromUrl(cxn, "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl", 
                "http://www.itmat.upenn.edu/biobank/drugOntologies")
        logger.info("dron-ingredient loaded.")
    }
}