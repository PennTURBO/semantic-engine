package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.util.Properties
import java.io.FileInputStream
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.util.Calendar
import java.text.SimpleDateFormat
import java.net.URL
import java.net.ConnectException
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.OpenRDFException
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.query.BooleanQuery
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.Reader
import java.io.BufferedInputStream
import java.io.InputStream
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.Model

/**
 * The TurboMultiuseClass contains methods whose functionality is repeatedly used by some component of the Drivetrain application. A few of the methods
 * in this class may be used by the Drivetrain test suite as well. The functions are here to be used and prevent repetitive development. 
 **/
//change name to something more relevant to the methods inside the class
class TurboMultiuseClass
{
    val sparqlPrefixes = """
			PREFIX  :     <http://transformunify.org/ontologies/>
			PREFIX  dc11: <http://purl.org/dc/elements/1.1/>
			PREFIX  obo:  <http://purl.obolibrary.org/obo/>
			PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
			PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>
			PREFIX  turbo: <http://transformunify.org/ontologies/>
			PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>
			PREFIX  nci:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
			PREFIX graphBuilder: <http://graphBuilder.org/>
			PREFIX pmbb: <http://www.itmat.upenn.edu/biobank/>
			PREFIX sys: <http://www.ontotext.com/owlim/system#>
			"""
    val logger = LoggerFactory.getLogger(getClass)
    val updater: SparqlUpdater = new SparqlUpdater
    
    /**
     * Deletes all triples in the entire database, including all named graphs.
     */
    def deleteAllTriplesInDatabase(cxn: RepositoryConnection)
     {
         val deleteAll: String = "DELETE {?s ?p ?o} WHERE {?s ?p ?o .} "
         val tupleDelete = cxn.prepareUpdate(QueryLanguage.SPARQL, deleteAll)
         tupleDelete.execute()
     }
    
    /**
     * Deletes all triples in a specified named graph.
     */
    def clearNamedGraph(cxn: RepositoryConnection, namedGraph: String)
     {
         //val deleteAll: String = "DELETE { GRAPH <" + namedGraph + "> { ?s ?p ?o }} WHERE {?s ?p ?o .}"
      
         //Clear Graph seems to give better performance than deleting a triple pattern
         val deleteAll: String = "CLEAR GRAPH <" + namedGraph + ">"
         updater.updateSparql(cxn, deleteAll)
     }
    
    /**
     * Parse string URI of input to remove the prefix. It's mostly meant for URI's which are composed of a prefix and then a UUID. If there are '/' characters
     * in the input that are not part of the prefix, this method may not work as expected.
     * 
     * @return a string which contains the input string, minus everything before and including the last '/' character in the input string
     */
    def getPostfixfromURI(someObject: String): String =
    {
        var uuidOfSomeObject: String = ""
        for (i <- 0 to someObject.length-1)
        {
            uuidOfSomeObject += someObject.charAt(i).toString()
            if (someObject.charAt(i) == '/') uuidOfSomeObject = ""
        }
        uuidOfSomeObject
    }
    
    /**
     * Overloaded method to generate a random UUID with the TURBO prefix. Overload accepts RepositoryConnection object as input.
     * 
     * @return an IRI with a new and unique TURBO URI
     */
    def genTurboIRI(cxn: RepositoryConnection): IRI =
    {
        val f: ValueFactory = cxn.getValueFactory()
        val newIRI: String = "http://transformunify.org/ontologies/" + UUID.randomUUID().toString().replaceAll("-", "")
        f.createIRI(newIRI)
    }
    
    /**
     * Overloaded method to generate a random UUID with the TURBO prefix. Overload accepts ValueFactory object as input.
     * 
     * @return an IRI with a new and unique TURBO URI
     */
    def genTurboIRI(f: ValueFactory): IRI =
    {
        val newIRI: String = "http://transformunify.org/ontologies/" + UUID.randomUUID().toString().replaceAll("-", "")
        f.createIRI(newIRI)
    }
    
    /**
     * Overloaded method to generate a random UUID with the PMBB prefix. No database connection necessary because no conversion to IRI is executed.
     * 
     * @return a String with a new and unique PMBB URI
     */
    def genPmbbIRI(): String =
    {
        "http://transformunify.org/ontologies/" + UUID.randomUUID().toString().replaceAll("-", "")
    }
    
    /**
     * Overloaded method to generate a random UUID with the PMBB prefix. Overload accepts RepositoryConnection object as input.
     * 
     * @return an IRI with a new and unique PMBB URI
     */
    def genPmbbIRI(cxn: RepositoryConnection): IRI =
    {
        val f: ValueFactory = cxn.getValueFactory()
        val newIRI: String = "http://www.itmat.upenn.edu/biobank/" + UUID.randomUUID().toString().replaceAll("-", "")
        f.createIRI(newIRI)
    }
    
    /**
     * Overloaded method to generate a random UUID with the PMBB prefix. Overload accepts RepositoryConnection object as input.
     * 
     * @return an IRI with a new and unique PMBB URI
     */
    def genPmbbIRI(f: ValueFactory): IRI =
    {
        val newIRI: String = "http://www.itmat.upenn.edu/biobank/" + UUID.randomUUID().toString().replaceAll("-", "")
        f.createIRI(newIRI)
    }
    
    /**
     * Overloaded method which creates a string representation of the current time in 'yyyyMMdd' format, using a default separator of "".
     * 
     * @return a string representation of the current time with default separator
     */
    def getCurrentTimestamp(): String = 
    {
        val date = Calendar.getInstance()
        var hour: String = date.get(Calendar.HOUR_OF_DAY).toString
        var minute: String = date.get(Calendar.MINUTE).toString
        var second: String = date.get(Calendar.SECOND).toString
        val format = new SimpleDateFormat("yyyyMMdd")
        val thisDate = format.format(Calendar.getInstance().getTime())
        if (hour.length == 1) hour = "0" + hour 
        if (minute.length == 1) minute = "0" + minute 
        if (second.length == 1) second = "0" + second 
        val datetimeStamp: String = thisDate.toString + hour.toString + minute.toString + second.toString
        /*println("PRINTING DATESTAMP")
        println(datetimeStamp)
        println(thisDate)*/
        datetimeStamp
    }
    
    /**
     * Overloaded method which creates a string representation of the current time in 'yyyyMMdd' format, using a custom separator received as input.
     * 
     * @return a string representation of the current time with custom separator
     */
    def getCurrentTimestamp(separator: String): String = 
    {
        val date = Calendar.getInstance()
        var hour: String = date.get(Calendar.HOUR_OF_DAY).toString
        var minute: String = date.get(Calendar.MINUTE).toString
        var second: String = date.get(Calendar.SECOND).toString
        val format = new SimpleDateFormat("yyyy"+separator+"MM"+separator+"dd")
        val thisDate = format.format(Calendar.getInstance().getTime())
        if (hour.length == 1) hour = "0" + hour 
        if (minute.length == 1) minute = "0" + minute 
        if (second.length == 1) second = "0" + second 
        val datetimeStamp: String = thisDate.toString + separator + hour.toString + separator + minute.toString + separator + second.toString
        //println("PRINTING DATESTAMP")
        //println(datetimeStamp)
        datetimeStamp
    }
    
    def removeAngleBracketsFromString(input: String): String =
    {
        var result: String = input
        if (result.length > 0)
        {
            if (result.charAt(0) == '<' && result.charAt(result.length-1) == '>')
            {
                result = result.substring(1, result.length-1)
            }
        }
        result
    }
    
    /**
     * Moves all RDF data in a specified Graph DB repository to another specified Graph DB repository.  This method is somewhat inefficient.
     */
    def moveDataFromOneRepositoryToAnother(from: String, to: RepositoryConnection)
    {
        val query: String = "INSERT DATA {CONSTRUCT ?s ?p ?o FROM <" + from + "> WHERE 	{?s ?p ?o .}}"
        val triplesToMove = to.prepareGraphQuery(QueryLanguage.SPARQL, query)
    }
    
    /**
     * Moves all RDF data in a specified named graph to another specified named graph.
     */
    def moveDataFromOneNamedGraphToAnother (cxn: RepositoryConnection, fromGraph: String, toGraph: String)
    {
        val moveTriples: String = 
            """
               ADD <"""+fromGraph+"""> TO <"""+toGraph+""">
            """
        updater.updateSparql(cxn, sparqlPrefixes + moveTriples)
    }
    
    /**
     * Overloaded method which prints all triples in all named graphs to the console.
     */
    def printAllInDatabase(cxn: RepositoryConnection)
    {
        val queryAll: String = "SELECT ?s ?p ?o WHERE {?s ?p ?o .}"
        val results = updater.querySparqlAndUnpackTuple(cxn, queryAll, Array("s","p","o"))
        logger.info("Number of statements: " + results.size.toString)
        for (result <- results)
        {
            val result0 = "<" + result(0).toString + ">"
            val result1 = "<" + result(1).toString + ">"
            var result2 = result(2).toString
            if (result2.charAt(0) != '"') result2 = "<" + result2 + ">" 
            println(result0 + " " + result1 + " " + result2 + " .")
        }
    }
    
    /**
     * Overloaded method which prints all triples in a single named graph given as input to the console.
     */
    def printAllInNamedGraph(cxn: RepositoryConnection, namedGraph: String)
    {
        val queryAll: String = "SELECT ?s ?p ?o WHERE { GRAPH <" + namedGraph + "> {?s ?p ?o .}}"
        val results = updater.querySparqlAndUnpackTuple(cxn, queryAll, Array("s","p","o"))
        logger.info("Number of statements: " + results.size.toString)
        for (result <- results)
        {
            val result0 = "<" + result(0).toString + ">"
            val result1 = "<" + result(1).toString + ">"
            var result2 = result(2).toString
            if (result2.charAt(0) != '"') result2 = "<" + result2 + ">" 
            println(result0 + " " + result1 + " " + result2 + " .")
        }
    }
    
    /**
     * Receives string variables describing the state of the error and adds them to a hashmap which is processed by 
     * the logErrorMessage method.  This method was specifically designed to work with the DrivetrainSparqlChecks 
     * class to perform data validation on the contents of the triplestore.
     */
    def writeErrorLog (process: String, error: String, variables: String = "")
    {
        val map: HashMap[String, String] = new HashMap[String, String]
        map += "process" -> process
        map += "cause" -> error
        map += "variables" -> variables
        logErrorMessage(map)    
    }
    
    /**
     * Writes an error message to the file specified in the TURBO property 'errorLogFile' based on the information
     * in the hashmap received as input.
     */
    def logErrorMessage(map: HashMap[String, String])
    {
        val file: File = new File(retrievePropertyFromFile("errorLogFile"))
        val fw: FileWriter = new FileWriter(file, true)
        val process: Option[String] = map.get("process")
        val cause: Option[String] = map.get("cause")
        val dataset: Option[String] = map.get("dataset")
        val result: Option[String] = map.get("result")
        val variables: Option[String] = map.get("variables")
        
        var write: String = 
          "\n" +
          "New Error Log " + getCurrentTimestamp() + " \n"
          if (process != None) write += "Occurred during process: " + process.get + " \n"
          if (cause != None) write += "Caused by: " + cause.get + " \n"
          if (variables != None) write += "Variables returned: " + variables.get + " \n"
          if (dataset != None) write += "Error found in dataset: " + dataset.get + " \n"
          if (result != None) write += "Resulting action taken: " + result.get + " \n"
          
          fw.write(write)
          fw.flush()
          fw.close()
    }
    
    /**
     * Launches a boolean query to determine whether there is one or more triples in the named graph received as input.
     * 
     * @return a Boolean true if one or more triples was found, false if otherwise
     */
    def isThereDataInNamedGraph(cxn: RepositoryConnection, namedGraph: IRI): Boolean =
    {
        val sparql: String = "ASK {GRAPH <" + namedGraph + "> {?s ?p ?o .}}"
        updater.querySparqlBoolean(cxn, sparql).get
    }
  
  /**
   * Overloaded method to load triples from a file into the triplestore. Triples are loaded into default named graph.
   */
  def loadDataFromFile(cxn: RepositoryConnection, resource: String, dataFormat: RDFFormat)
     {
         cxn.begin()
         val file: File = new File(resource)
         if (!(file.exists)) logger.info ("Specified file " + resource + " does not exist in the necessary location.")
         val is: InputStream = new FileInputStream(resource)
         val reader: Reader = new InputStreamReader(new BufferedInputStream(is))
         logger.info("adding " + resource)
         try cxn.add(reader, "", dataFormat)
         finally reader.close()
         cxn.commit()
     }
    
  /**
   * Overloaded method to load triples from a file into the triplestore. Triples are loaded into named graph received as input.
   */
    def loadDataFromFile(cxn: RepositoryConnection, resource: String, dataFormat: RDFFormat, namedGraph: String)
     {
         cxn.begin()
         val f: ValueFactory = cxn.getValueFactory()
         val file: File = new File(resource)
         if (!(file.exists)) logger.info ("Specified file " + resource + " does not exist in the necessary location.")
         val is: InputStream = new FileInputStream(resource)
         val reader: Reader = new InputStreamReader(new BufferedInputStream(is))
         logger.info("adding " + resource)
         try cxn.add(reader, "", dataFormat, f.createIRI(namedGraph))
         finally reader.close()
         cxn.commit()
     }
  
  /**
   * Searches through a specified named graph to find symmetrical properties as defined by the TURBO ontology, and adds
   * the corresponding inverse triple to the same named graph as the original triple. Note that default named graph argument 
   * "?g" specifies that all named graphs should be searched for symmetrical properties, with the exception of ontology named 
   * graphs which are FILTERED.
   */
  def applySymmetricalProperties (cxn: RepositoryConnection, namedGraph:String = "?g")
  {
      var namedGraph_1 = ""
      if (namedGraph != "?g") namedGraph_1 = "<" + namedGraph + ">"
      else namedGraph_1 = namedGraph
      
      val insert: String = """
        INSERT {
            graph """+namedGraph_1+""" 
              {
                    ?o ?p ?s .
        }} 
        WHERE 
        {
            graph """+namedGraph_1+""" 
              {
                    ?s ?p ?o . 
              }
        graph pmbb:ontology {
            ?p a <http://www.w3.org/2002/07/owl#SymmetricProperty> .
            }
            FILTER ("""+namedGraph_1+""" != pmbb:ontology)
            FILTER ("""+namedGraph_1+""" != pmbb:ICD9Ontology)
            FILTER ("""+namedGraph_1+""" != pmbb:ICD10Ontology)
            FILTER ("""+namedGraph_1+""" != pmbb:mondoOntology)
        }
        """
            
      updater.updateSparql(cxn, sparqlPrefixes + insert)
  }
  
  /**
   * Searches through all named graphs excluding ontology named graphs for inverse properties, as defined by the TURBO ontology.
   * Inserts the corresponding inverse triple into a special named graphs called pmbb:inverses.
   */
  def applyInverses (cxn: RepositoryConnection, concNamedGraph: IRI)
    {
        //Trying a non-sparql based approach with the goal of improved performance
    
        //First, make a mapping of all the inverse predicates in the graph
    
        logger.info("Compiling list of inverse predicates from ontology...")    
    
        val getInversePreds: String = """
          Select ?p ?inverse Where
          {
              Graph pmbb:ontology
              {
                  ?p owl:inverseOf ?inverse .
              }
          }
          """
        
        val inverseList: ArrayBuffer[ArrayBuffer[Value]] = updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getInversePreds, ArrayBuffer("p", "inverse"))
        
        var inverseMap: HashMap[Value, Value] = new HashMap[Value, Value]
        var inversePredString: String = ""
        
        for (inverse <- inverseList) 
        {
            inverseMap += inverse(0) -> inverse(1)
            inversePredString += "<" + inverse(0).toString + ">"
        }
        
        // Now, pull all relevant triples from the relevant graphs
        
        logger.info("Searching for triples in need of inversing...")
        
        val getAllTriples: String = """
          Select ?s ?p ?o Where
          {
              Values ?g {pmbb:expanded pmbb:entityLinkData <"""+concNamedGraph.toString+""">}
              Values ?p {""" + inversePredString + """}
              Graph ?g
              {
                  ?s ?p ?o .
              }
          }
          """
              
        val triplesList: ArrayBuffer[ArrayBuffer[Value]] = updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getAllTriples, ArrayBuffer("s", "p", "o"))
        
        logger.info("Applying inverses to " + triplesList.size + " triples...")
        
        var model: Model = new LinkedHashModel()
        
        var count: Int = 0
        
        for (triple <- triplesList) 
        {
            model.add(triple(2).asInstanceOf[IRI], inverseMap(triple(1)).asInstanceOf[IRI], triple(0).asInstanceOf[IRI])
            if (model.size() == 100000)
            {
                count += 100000
                cxn.begin()
                cxn.add(model, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/inverses"))
                cxn.commit()
                model = new LinkedHashModel()
                logger.info("Triples committed: " + count)
            }
        }
        
        cxn.begin()
        cxn.add(model, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/inverses"))
        cxn.commit()
        
        logger.info("Finished applying inverses")
    }
  
    /**
     * Automatically deletes all triples from all named graphs which contain shortcut relationships. This is called as part of the post-expansion
     * process.
     */
    def clearShortcutNamedGraphs (cxn: RepositoryConnection)
    {
        val graphs: ArrayBuffer[String] = generateNamedGraphsListFromPrefix(cxn, "http://www.itmat.upenn.edu/biobank/Shortcuts")
        for (graph <- graphs) clearNamedGraph(cxn, graph)
    }
    
    /**
     * This is a strange method which executes a batched Referent Tracking completition phase, which entails moving all properties from a non-reftracked
     * node to a reftracked node. The referent tracker class is responsible for creating pointers from non-reftracked nodes to reftracked nodes but not
     * for migrating the properties or retiring the non-reftracked node. 
     */
    def completeReftrackProcess (cxn: RepositoryConnection, threshold: Integer = 1000)
    {
         logger.info("starting complete reftrack process")
         /**
          * it is required that all nodes to be reftracked be the subject and object of a triple. 
          * As a workaround for some nodes not being the object of a triple, we can insert something temporarily and remove it later.
          * Not ideal, but should work for now.
          */
         addTempSubjectToAllNodesToBeReftracked(cxn)
         val getNodesToBeReftracked: String = """
           select ?node where
           {
               graph pmbb:expanded
               {
                   ?node graphBuilder:willBeCombinedWith ?reftrackedNode .
                   ?node graphBuilder:placeholderDemotionType ?demotionType .
               }
           }
           """       
          val result: ArrayBuffer[String] = updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getNodesToBeReftracked, "node")
          var values: String = ""
          val resultSize = result.size
          logger.info("number of nodes to retire: " + resultSize)
          for (a <- 0 to resultSize - 1)
          {
              values += "<" + result(a) + "> "
              if ((a % threshold == 0 && a != 0) || a == (resultSize - 1))
              {   
                  //logger.info("running to node " + (a - 1))
                  completeProcessForAllValuesInString(cxn, values, "Retiring " + a + " of " + resultSize + " nodes")
                  values = ""
                  //logger.info("back out in for loop")
              }
          }
         cleanupAfterCompletingReftrackProcess(cxn)
         logger.info("finished retiring nodes")
    }
    
    /**
     * This method contains the SPARQL responsible for migrating properties from and retiring non-reftracked nodes. It is called by the method above
     * and executes the migration/retiring process for a set of nodes provided in the "values" string variable.
     */
    def completeProcessForAllValuesInString(cxn: RepositoryConnection, values: String, percentFinished: String)
    {
            val randomUUID: String = UUID.randomUUID().toString().replaceAll("-", "")       
            val completeReftrackProcess: String = """
            DELETE {
              ?originalNode ?predicateForCopy1 ?objectForCopy .
              ?subjectForCopy ?predicateForCopy2 ?originalNode .
            }
            INSERT {
              # """ + percentFinished + """ 
              GRAPH <http://www.itmat.upenn.edu/biobank/expanded>
              {
              ?reftrackedNode ?predicateForCopy1 ?objectForCopy .
              ?reftrackedNode turbo:TURBO_0006602 ?originalNodeString .
              ?subjectForCopy ?predicateForCopy2 ?reftrackedNode .
              ?reftrackedNode turbo:TURBO_0006500 'true'^^xsd:boolean .
              ?newRetiredNode rdf:type ?demotionType .
              ?newRetiredNode turbo:TURBO_0001700 ?reftrackedNode .
              ?newRetiredNode obo:IAO_0000225 obo:IAO_0000226 .
              ?newRetiredNode turbo:TURBO_0006602 ?originalNodeString .
            }}
            WHERE {
              VALUES ?originalNode { """+values+""" }
              ?originalNode graphBuilder:willBeCombinedWith ?reftrackedNode .
              ?originalNode graphBuilder:placeholderDemotionType ?demotionType .
              ?originalNode ?predicateForCopy1 ?objectForCopy .
              ?subjectForCopy ?predicateForCopy2 ?originalNode
              BIND (str(?originalNode) AS ?originalNodeString)
              # 3/20/18 adding md5 to ?newRetiredNode due to complications with "values" batching technique
              BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/",md5(CONCAT("retired node", str(?originalNode),""""+randomUUID+"""")))) AS ?newRetiredNode)
            }
          """
        updater.updateSparql(cxn, sparqlPrefixes + completeReftrackProcess)
        //updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + completeReftrackProcess, ArrayBuffer("originalNode", "reftrackedNode", "demotionType", "predicateForDelete1", 
            //"objectForDelete", "predicateForCopy1", "objectForCopy", "subjectForCopy", "predicateForCopy2", "originalNodeString", "newRetiredNode"))
        //logger.info("received result")
    }
    
    /**
     * This method searches for all nodes which have been assigned referent tracking destinations and fills in a temporary subject with the 
     * graphBuilder prefix. It is necessary to have this temporary subject during the completion of the referent tracking process, because
     * the completeReftrackProcess method expects every node which is to be referent tracked to be the subject and the object of at least one triple.
     */
    def addTempSubjectToAllNodesToBeReftracked(cxn: RepositoryConnection)
    {
        val fixNodesThatAreNotObjects: String = """
           insert 
           {
               graphBuilder:tempSubj graphBuilder:tempPred ?node .
           }
           where
           {
               graph pmbb:expanded
               {
                   ?node graphBuilder:willBeCombinedWith ?reftrackedNode .
                   ?node graphBuilder:placeholderDemotionType ?demotionType .
               }
               minus
               {
                   ?subject ?predicate ?node .
               }
           }
           """
         updater.updateSparql(cxn, sparqlPrefixes + fixNodesThatAreNotObjects)
    }
    
    /**
     * This method performs two cleanup operations on the database. First, temporary subjects and predicates are removed. Second, all
     * pre-referent tracking pointers are removed. After this method runs, no intermediate state triples generated by the referent 
     * tracking process should exist in the graph.
     */
    def cleanupAfterCompletingReftrackProcess(cxn: RepositoryConnection)
    {
        val removeTemporaryPredicates: String = """
           delete 
           {
               graphBuilder:tempSubj graphBuilder:tempPred ?node .
           }
           where
           {
               graphBuilder:tempSubj graphBuilder:tempPred ?node .
           }
           """
         updater.updateSparql(cxn, sparqlPrefixes + removeTemporaryPredicates)
         
         val removeGraphBuilder: String = """
           delete 
           {
               ?s graphBuilder:willBeCombinedWith ?o .
               ?s graphBuilder:placeholderDemotionType ?type .
           }
           where 
           {
               ?s graphBuilder:willBeCombinedWith ?o .
               ?s graphBuilder:placeholderDemotionType ?type .
           }
           """
         updater.updateSparql(cxn, sparqlPrefixes + removeGraphBuilder)
    }
    
    /**
     * This method executes SPARQL to search through a specified named graph for nodes which do not have labels. It then searches the TURBO
     * ontology to find the label of that node's type and concatanates this label with the first 4 digits of the node's UUID postfix to create
     * an instance label, which is applied to the node as a single triple using the "rdfs:label" predicate in the same named graph as the node.
     * Note that labels are not guaranteed to be unique, and it is possible that a node may be assigned multiple labels if its type has multiple
     * labels in the ontology, or if it itself has multiple types.  Also note that default named graph argument "?g" specifies that all named 
     * graphs should be searched for symmetrical properties, with the exception of ontology named graphs which are FILTERED.
     */
    def addLabelsToEverything (cxn: RepositoryConnection, namedGraph: String = "?g")
    {
        var namedGraph_1 = ""
        if (namedGraph != "?g") namedGraph_1 = "<" + namedGraph + ">"
        else namedGraph_1 = namedGraph
        
        val addLabelsToEverything: String = """
          Insert 
          {
              Graph """+namedGraph_1+"""
              {
                  ?node rdfs:label ?label .
              }
          }
          Where
          {
              Graph """+namedGraph_1+"""
              {
                  ?node a ?nodetype .
              }
              Graph pmbb:ontology
              {
                  ?nodetype rdfs:label ?ontologylabel .
              }
              
              Minus
              {
                  ?node rdfs:label ?somelabel .
              }
              FILTER (?nodetype != turbo:TURBO_0000506)
              FILTER (?nodetype != turbo:TURBO_0000513)
              FILTER (?nodetype != turbo:TURBO_0000543)
              FILTER ("""+namedGraph_1+""" != pmbb:ontology)
              FILTER ("""+namedGraph_1+""" != pmbb:ICD9Ontology)
              FILTER ("""+namedGraph_1+""" != pmbb:ICD10Ontology)
              FILTER ("""+namedGraph_1+""" != pmbb:mondoOntology)
              BIND (CONCAT(REPLACE(?ontologylabel, " ", ""), "/", substr(str(?node), 38, 4)) AS ?label)
          }
          """
        
        updater.updateSparql(cxn, sparqlPrefixes + addLabelsToEverything)
    }
    
    /**
     * Reads a Java properties file and searches for a specific property given by the propertyID string variable. Default file input
     * is the TURBO properties file. This is the main method responsible for pulling in property values from the TURBO properties file.
     * 
     * @return a String holding the value of the requested property
     */
    def retrievePropertyFromFile(propertyID: String, file: String = "..//turbo_properties.properties"): String =
     {
         val input: FileInputStream = new FileInputStream(file)
         val props: Properties = new Properties()
         props.load(input)
         input.close()
         props.getProperty(propertyID)
     }
    
    /**
     * Creates an MD5 representation of a given input string. This is guaranteed to be unique to that string.
     * 
     * @return a String holding the MD5 representation
     */
    def md5Hash(text: String) : String = 
    {
        java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
    }
    
    /**
     * This method's creation was necessitated by the fact that some labels in the TURBO ontology are represented in a format other than 
     * rdf:langString. In order to be properly read by the addLabelsToEverything method, labels must be in the rdf:langString format. This 
     * method searches through the TURBO ontology for all non-rdf:langString-formatted labels and adds a string label with the same value 
     * as the original label back into the ontology named graph.
     */
    def addStringLabelsToOntology (cxn: RepositoryConnection)
    {
        val update: String = """
          Insert 
          {
              Graph pmbb:ontology
              {
                  ?class rdfs:label ?stringLabel .
              }
          }
          Where
          {
               Graph pmbb:ontology
               {
                   ?class rdfs:label ?label .
                   FILTER (datatype(?label) = rdf:langString)
               }
               BIND (str(?label) AS ?stringLabel)   
          }
          """
        
        updater.updateSparql(cxn, sparqlPrefixes + update)
    }
    
    /**
     * Generates string of all Shortcut named graphs by issuing retrieving a list of shortcut graphs and transforming it to a string
     * 
     * @return a string representation of all shortcut named graphs for expansion
     */
    def generateShortcutNamedGraphsString(cxn: RepositoryConnection, asFrom: Boolean = false, prefix: String = "http://www.itmat.upenn.edu/biobank/Shortcuts"): String =
    {
        var graphsString = ""
        for (a <- generateNamedGraphsListFromPrefix(cxn, prefix))
        {
            if (!asFrom) graphsString += "<" + a + "> "
            else graphsString += "FROM <" + a + "> "
        }
        graphsString
    }
    
    /**
     * Generates list of all Shortcut named graphs by issuing a Sparql command to retrieve all named graphs which start with "Shortcuts"
     * 
     * @return a list representation of all shortcut named graphs for expansion
     */
    def generateNamedGraphsListFromPrefix(cxn: RepositoryConnection, graphsPrefix: String = "http://www.itmat.upenn.edu/biobank/Shortcuts"): ArrayBuffer[String] =
    {
        val getGraphs: String = """
        select distinct ?g where 
        {
            graph ?g
            {
                ?s ?p ?o .
            }
            filter (strStarts(str(?g), """"+graphsPrefix+""""))
        }"""
        
        updater.querySparqlAndUnpackTuple(cxn, getGraphs, "g")
    }
    
    //These 2 globals are associated with the two methods below
    private var nonMatchesArr1: ArrayBuffer[String] = new ArrayBuffer[String]
    private var nonMatchesArr2: ArrayBuffer[String] = new ArrayBuffer[String]
    
    /**
     * This method was developed for use with the suite of Expansion tests, in order to determine if the predicates created by the Expander 
     * match a set of expected predicates specified in the tests. Sorts input arrays and passes them to findSortedArrayDifferences to be 
     * recursively analyzed. 
     * 
     * @return a Boolean true if sorted arrays are equivalent, false otherwise
     */
    def checkStringArraysForEquivalency(arr1: Array[String], arr2: Array[String]): HashMap[String, Object] =
    {
        logger.info("about to check string arrays for equivalency")
        
        logger.info("size of array 1: " + arr1.size)
        logger.info("size of array 2: " + arr2.size)
        
        //first, sort each array in alphabetical order
        scala.util.Sorting.quickSort(arr1)
        scala.util.Sorting.quickSort(arr2)
        logger.info("finished sorting arrays")
        //search line by line for differences in array
        findSortedArrayDifferences(arr1, 0, arr2, 0)
        
        logger.info("nonMatchesArr1: " + nonMatchesArr1.size)
        for (a <- nonMatchesArr1) logger.info(a)
        logger.info("nonMatchesArr2: " + nonMatchesArr2.size)
        for (a <- nonMatchesArr2) logger.info(a)
        
        var boolToReturn: Boolean = false
        if (nonMatchesArr1.size == 0 && nonMatchesArr2.size == 0) boolToReturn = true
        
        var res1return = nonMatchesArr1
        var res2return = nonMatchesArr2

        nonMatchesArr1 = new ArrayBuffer[String]
        nonMatchesArr2 = new ArrayBuffer[String]
        
        HashMap("results" -> Array(res1return, res2return), "equivalent" -> boolToReturn.toString)
    }
    
    /**
     * This is a recursive method which checks two given arrays of strings against each other and returns the differences. 
     * Keeps track of differences using global variables nonMatchesArr1 and nonMatchesArr2.
     * 
     * Do you even recurse bro? (I hope you do it better than me)
     */
    private def findSortedArrayDifferences(arr1: Array[String], index1: Int, arr2: Array[String], index2: Int)
    {
        if (arr1.size - 1 >= index1 && arr2.size - 1 >= index2)
        {
            val compare: Int = arr1(index1).compareTo(arr2(index2))
            if (compare == 0) findSortedArrayDifferences(arr1, index1+1, arr2, index2+1)
            else if (compare > 0)
            {
                nonMatchesArr2 += arr2(index2)
                findSortedArrayDifferences(arr1, index1, arr2, index2+1)
            }
            else if (compare < 0) 
            {
                nonMatchesArr1 += arr1(index1)
                findSortedArrayDifferences(arr1, index1+1, arr2, index2)
            }
        }
        else
        {
            if (arr1.size -1 < index1)
            {
                for (a <- index2 to arr2.size - 1) nonMatchesArr2 += arr2(a)
            }
            if (arr2.size -1 < index2)
            {
                for (a <- index1 to arr1.size - 1) nonMatchesArr1 += arr1(a)
            }
        }
    }
    
    def convertSparqlResultToStringArray(sparqlResult: ArrayBuffer[ArrayBuffer[Value]]): ArrayBuffer[ArrayBuffer[String]] =
    {
        var arrToReturn: ArrayBuffer[ArrayBuffer[String]] = new ArrayBuffer[ArrayBuffer[String]]
        for (a <- sparqlResult)
        {
            var singleLine: ArrayBuffer[String] = new ArrayBuffer[String]
            for (b <- a) singleLine += b.toString
            arrToReturn += singleLine
        }
        arrToReturn
    }
    
    def removeInferredStatements(cxn: RepositoryConnection)
    {
        var model: Model = new LinkedHashModel()
        val f: ValueFactory = cxn.getValueFactory()
        val select: String =
          """
            Select * FROM <http://www.ontotext.com/implicit> Where {?s ?p ?o .}
          """
        val result: ArrayBuffer[ArrayBuffer[Value]] = updater.querySparqlAndUnpackTuple(cxn, select, Array("s", "p", "o"))
        for (row <- result)
        {
            model.add(row(0).asInstanceOf[IRI], row(1).asInstanceOf[IRI], row(2).asInstanceOf[IRI])
        }
        cxn.remove(model, f.createIRI("http://www.ontotext.com/implicit"))
    }
    
    def countTriplesInDatabase(cxn: RepositoryConnection): Int =
    {        
        val query: String = """
          select (count (?s) as ?tripcount) where
          {
              ?s ?p ?o .
          }
          """
         updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "tripcount")(0).split("\"")(1).toInt
    }
    
    def getDatasetNames(cxn: RepositoryConnection): ArrayBuffer[String] = 
    {
        val query: String = """
          select ?dsTitle where
          {
              ?ds a obo:IAO_0000100 .
              ?ds dc11:title ?dsTitle .
          }
          """
        updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "dsTitle")
    }
    
    def consolidateLOFShortcutGraphs(cxn: RepositoryConnection)
    {
        val lofGraphs: String = generateShortcutNamedGraphsString(cxn, false, "http://www.itmat.upenn.edu/biobank/LOFShortcuts")
        //first count the number of unexpanded rows of LOF data
        val countquery = """
          select (count (?lof) as ?lofcount) where
          {
              ?lof turbo:TURBO_0007603 ?o .   
          }
          """
        val count = updater.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countquery, "lofcount")(0).toString.split("\"")(1).toInt
        //if count < 700,000 safe to consolidate
        if (count < 700000)
        {
            val uuidForNewGraph = UUID.randomUUID().toString().replaceAll("-", "")
            val consolidate = """
              delete
              {
                  	graph ?g
                  	{
                          ?s ?p ?o .
                  	}
              }
              insert
              {
                  graph pmbb:LOFShortcuts_consolidated_"""+uuidForNewGraph+"""
                  {
                      ?s ?p ?o .
                  }
              }
              where
              {
                  values ?g 
                  {
                  """ + lofGraphs + """
                  }
              	graph ?g
              	{
                      ?s ?p ?o .
              	}
              }
              
            """
            updater.updateSparql(cxn, sparqlPrefixes + consolidate)
        }
    }
    
    def getAllValuesOfType(cxn: RepositoryConnection, item: GraphObjectSingleton): ArrayBuffer[String] =
    {
        val baseVar = item.baseVariableName
        val varType = item.typeURI
        
        val query = s"""
          select ?$baseVar where {?$baseVar a <$varType> .}
          """
          
        updater.querySparqlAndUnpackTuple(cxn, query, baseVar)
    }
}