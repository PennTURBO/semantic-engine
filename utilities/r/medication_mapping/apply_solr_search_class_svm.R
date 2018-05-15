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
library(rrdf)

saf.val <- FALSE

# check current working directory

# put all filenames here
order.file <-
  "C:/Users/Mark Miller/Desktop/real_wes/wes_pds_enc__med_order.csv"

med.stand.fn <-
  "C:\\Users\\Mark Miller\\Desktop\\real_wes\\wes_pds__med_standard.csv"

med_map_svm_file <-
  "C:\\Users\\Mark Miller\\Desktop\\current_med_map_svm\\med_map_class_svm_20180503.Rdata"

endpoint <- "http://localhost:7200/repositories/dron_no_ndc"

# do you have solr running with the necessary collection ?!
# url for solr
forward.url <-
  'http://localhost:8983/solr/medcore20180503/select'

# forward.url <-
#   'http://localhost:8983/solr/plainsolrmeds/select'

# forward.url <-
#   'http://localhost:9993/solr/dtmeds/select'

num.hits.desired <- 20

###

load(med_map_svm_file)

# unknown orders that we want to map
med_order <-
  read.csv(order.file,
           stringsAsFactors = FALSE)

# my wrapper for solr_search from the solr library
# handles empty results and ranks scores relative to the results for each individual query term
# applied to each unique med order term
# qf = what field to query
# rf = what fields to return
# num.results already defined above
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

# get a vector of unique and slightly tidied orders
med_order$ORDER_NAME.tidy <- med_order$ORDER_NAME

# vector of unique orders
qs.for.forward <- sort(unique(med_order$ORDER_NAME.tidy))
qs.for.forward <- qs.for.forward[nchar(qs.for.forward) > 0]

device_etc_list_onecol <-
  read.delim(
    "C:/Users/Mark Miller/Desktop/med_map_corpus/device_etc_list_onecol.txt",
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

# some qc
hist(forward.with.dist$score, breaks = 99)

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
select ?super ?suplab where {
values ?s { "

query.suffix <- " }
?s rdfs:label ?o ;
rdfs:subClassOf+ ?super .
?super rdfs:label ?suplab
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
    # current.query <- "AUGMENTIN PO"
    print(current.query)
    match.uris <-
      bestmatches$d.full_s[bestmatches$query == current.query]
    match.uris <-
      paste("<", match.uris, ">", sep = "", collapse = " ")
    match.uris <- paste0(match.uris, collapse = " ")
    my.query <- paste0(query.prefix, match.uris, query.suffix)
    my.result <-
      sparql.remote(endpoint = endpoint,
                    sparql = my.query,
                    jena = TRUE)
    my.result <- as.data.frame(my.result)
    
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
names(multi2parent) <- c("query", "parent.label", "parent.uri")

stillunduped <-
  dupematches[!dupematches$query %in% multi2parent$query,]
print(length(unique(stillunduped$query)))
print(stillunduped)


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

failed.orders <-
  sort(unique(toupper(c(
    failures$query, stillunduped$query
  ))))
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

my.result <-
  sparql.remote(endpoint = endpoint,
                sparql = my.query,
                jena = TRUE)
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
  merge(x = redemption,
        y = rxn.known,
        by.x = "RXNORM",
        by.y = "o")
redeemed.redemption$FULL_NAME <-
  toupper(redeemed.redemption$FULL_NAME)
redeemed.redemption <-
  unique(redeemed.redemption[, c("FULL_NAME", "s")])

redeemed.redemption$s <-
  sub(pattern = "^obo:",
      replacement = "http://purl.obolibrary.org/obo/",
      x = redeemed.redemption$s)


final.1 <- multi2parent[, c("query", "parent.uri")]
final.2 <- singlematches[, c("query", "d.full_s")]
names(final.1) <- names(final.2)
names(redeemed.redemption) <- names(final.2)
final.both <-
  unique(rbind.data.frame(final.1, final.2, redeemed.redemption))

# this adds back in the med orders that couldn't be ,mapped
for.karma <-
  med_order[, c(
    "PK_ORDER_MED_ID",
    "FK_MEDICATION_ID",
    "ORDER_NAME",
    "RAWTOHEX.FK_PATIENT_ENCOUNTER_ID."
  )]
for.karma <-
  merge(
    x = for.karma,
    y = final.both,
    by.x = "ORDER_NAME",
    by.y = "query",
    all.x = TRUE
  )

for.karma$devetc <- !for.karma$ORDER_NAME %in% qs.for.forward

# # total mapping, not unique mapping
# print(sum(!is.na(for.karma$d.full_s)) / (nrow(for.karma)- sum(for.karma$devetc)))
#
# temp <- unique(for.karma$ORDER_NAME[is.na(for.karma$d.full_s)])
# print(1 - (length(temp) / length(unique(
#   for.karma$ORDER_NAME
# ))))

# how many non-dev orders?
nondev.ord.ct <- sum(!for.karma$devetc)

# how many unique non-dev orders?
nondev.ord.unique.ct <-
  length(unique(for.karma$ORDER_NAME[!for.karma$devetc]))

# how many URL hits?
dron.url.hit.ct <- sum(!is.na(for.karma$d.full_s))

# how many unique non-dev orders resulted in a DRON hit?
dron.url.hit.unique.ct <-
  length(unique(for.karma$ORDER_NAME[!is.na(for.karma$d.full_s)]))

#total mapping rate
print(dron.url.hit.ct / nondev.ord.ct)

#unique mapping rate
print(dron.url.hit.unique.ct / nondev.ord.unique.ct)

write.csv(for.karma, "wes_med_map_for_karma.csv")

###

# how useful was the solr score?

fwd4plot <-
  unique(forward.with.dist[, c("dl_s", "query", "d.full_s", "score",  "pred")])

ggplot(fwd4plot, aes(x = score, group = pred, fill = pred)) +
  geom_histogram(position = "identity",
                 alpha = 0.5,
                 binwidth = 0.3) + theme_bw() + xlim(0, 25)

ggplot(fwd4plot, aes(x = score, colour = pred)) +
  geom_density(size = 2) + theme_bw() + xlim(0, 25)


###  look at error from training

###  look at unmapped non devetc

needs.followup <-
  for.karma[(!for.karma$devetc) & is.na(for.karma$d.full_s), ]

followup.terms <- unique(needs.followup$ORDER_NAME)

needs.followup <-
  forward.with.dist[forward.with.dist$query %in% followup.terms , ]

temp <-
  merge(
    x = final.both,
    y = forward.with.dist,
    by.x = c("query", "d.full_s"),
    by.y = c("query", "d.full_s")
  )

write.csv(temp, "wes_accepted_mappings.csv", row.names = FALSE)



write.csv(followup.terms, "wes_unmapped_orders.csv", row.names = FALSE)
