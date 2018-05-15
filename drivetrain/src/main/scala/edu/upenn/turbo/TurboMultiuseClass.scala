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
    
    /**
     * Overloaded method which is Drivetrain's main point of access to Graph DB for SPARQL queries. Used for when only one variable is requested
     * to be returned in the result set.
     * 
     * @return ArrayBuffer[String] representing the results of the query
     */
      def querySparqlAndUnpackTuple(cxn: RepositoryConnection, query: String, variable: String): ArrayBuffer[String] =
      {
          val result: Option[TupleQueryResult] = querySparql(cxn, query)
          val unpackedResult: ArrayBuffer[String] = unpackTuple(result.get, variable)
          //close tupleQueryResult to free resources
          result.get.close()
          unpackedResult
      }
      
    /**
     * Overloaded method which is Drivetrain's main point of access to Graph DB for SPARQL queries. Used for when multiple variables are requested
     * to be returned in the result set. Overloaded to accept Array[String] as list of variables.
     * 
     * @return ArrayBuffer[ArrayBuffer[Value]] representing the results of the query 
     */
      def querySparqlAndUnpackTuple(cxn: RepositoryConnection, query: String, variable: Array[String]): ArrayBuffer[ArrayBuffer[Value]] =
      {
          val result: Option[TupleQueryResult] = querySparql(cxn, query)
          val unpackedResult: ArrayBuffer[ArrayBuffer[Value]] = unpackTuple(result.get, variable)
          //close tupleQueryResult to free resources
          result.get.close()
          unpackedResult
      }
      
      /**
     * Overloaded method which is Drivetrain's main point of access to Graph DB for SPARQL queries. Used for when multiple variables are requested
     * to be returned in the result set. Overloaded to accept ArrayBuffer[String] as list of variables.
     * 
     * @return ArrayBuffer[ArrayBuffer[Value]] representing the results of the query 
     */
      def querySparqlAndUnpackTuple(cxn: RepositoryConnection, query: String, variable: ArrayBuffer[String]): ArrayBuffer[ArrayBuffer[Value]] =
      {
          val result: Option[TupleQueryResult] = querySparql(cxn, query)
          val unpackedResult: ArrayBuffer[ArrayBuffer[Value]] = unpackTuple(result.get, variable)
          //close tupleQueryResult to free resources
          result.get.close()
          unpackedResult
      }
      
      /**
       * QuerySparql() is a generic SPARQL query method which uses the RepositoryConnection object
       * to connect with a Graph database. It can accept any String that uses proper SPARQL syntax.
       * 
       * @return a TupleQueryResult object with the results of the SPARQL query.
       */
      def querySparql(cxn: RepositoryConnection, query: String): Option[TupleQueryResult] =
      {
          var result: Option[TupleQueryResult] = None : Option[TupleQueryResult]
          try 
          {
              //send input String to Blazegraph SPARQL engine via the RepositoryConnection object
              val tupleQuery: TupleQuery = cxn.prepareTupleQuery(QueryLanguage.SPARQL, query)
              //convert tupleQuery into TupleQueryResult using built-in evaluate() function
              result = Some(tupleQuery.evaluate())
              result
          }
          catch
          {
              case e: OpenRDFException => logger.warn(e.toString)
              None
          }
      }
      /**
       * QuerySparqlBoolean() method to handle ASK queries which should return a Boolean rather than a resultset.
       * 
       * @return a Option[Boolean] object with the results of the SPARQL query. If query is invalid, None is returned.
       */
      def querySparqlBoolean(cxn: RepositoryConnection, query: String): Option[Boolean] =
      {  
          try
          {
              val boolQueryResult: BooleanQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, query)
              Some(boolQueryResult.evaluate())
          }
          catch
          {
              case e: OpenRDFException => logger.warn(e.toString)
              None
          }
      }
      
      /**
     * This method accepts a TupleQueryResult and converts it into a list. Each element in the list represents
     * a value of the SPARQL variable for one of the results. 
     * 
     * @return A list of strings which represent the value of the given variable for one of the results
     */
    def unpackTuple(resultTuple: TupleQueryResult, variableToUnpack: String): ArrayBuffer[String] =
    {
        //Create empty list to be populated and returned
        val resultList: ArrayBuffer[String] = new ArrayBuffer[String]
        //For each result, convert it to a String and add it to the list
        while (resultTuple.hasNext())
        {
            val bindingset: BindingSet = resultTuple.next()
            var result: String = removeQuotesFromString(bindingset.getValue(variableToUnpack).toString)
            resultList += result
        }
        //Return list of results as Strings
        resultList
    }
    /**
     * Overloaded UnpackTuple method to accept multiple variables. Variable input given as Array[String]
     * 
     * @return a list of lists of strings which represent the values of the given variable for one of the results
     */
    def unpackTuple(resultTuple: TupleQueryResult, variables: Array[String]): ArrayBuffer[ArrayBuffer[Value]] =
    {
        //Create empty list of lists to be populated and returned
        val resultList: ArrayBuffer[ArrayBuffer[Value]] = new ArrayBuffer[ArrayBuffer[Value]]
        //For each result, add it to the list. Note that this method does not convert to Strings but leaves results as Values
        while (resultTuple.hasNext())
        {
            val oneResult: ArrayBuffer[Value] = new ArrayBuffer[Value]
            val bindingset: BindingSet = resultTuple.next()
            for (a <- 0 to variables.size - 1)
            {
                val result: Value = bindingset.getValue(variables(a))
                oneResult += result
            }
            resultList += oneResult
        }
        //Return list of lists of Value objects
        resultList
    }
    
    /**
     * Overloaded UnpackTuple method to accept multiple variables. Variable input given as ArrayBuffer[String]
     * 
     * @return a list of lists of strings which represent the values of the given variable for one of the results
     */
    def unpackTuple(resultTuple: TupleQueryResult, variables: ArrayBuffer[String]): ArrayBuffer[ArrayBuffer[Value]] =
    {
        //Create empty list of lists to be populated and returned
        val resultList: ArrayBuffer[ArrayBuffer[Value]] = new ArrayBuffer[ArrayBuffer[Value]]
        //For each result, add it to the list. Note that this method does not convert to Strings but leaves results as Values
        while (resultTuple.hasNext())
        {
            val oneResult: ArrayBuffer[Value] = new ArrayBuffer[Value]
            val bindingset: BindingSet = resultTuple.next()
            for (a <- 0 to variables.size - 1)
            {
                val result: Value = bindingset.getValue(variables(a))
                oneResult += result
            }
            resultList += oneResult
        }
        //Return list of lists of Value objects
        resultList
    }
    
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
         updateSparql(cxn, deleteAll)
     }
    
    /**
     * The main point of access for Drivetrain's SPARQL-based updates. Updates are submitted to the method as a SPARQL String.
     * Invalid SPARQL inputs will spawn an exception thrown by an RDF4J method.
     */
    def updateSparql(cxn: RepositoryConnection, update: String)
     {
         //logger.info("inside update sparql")
         cxn.begin()
         //logger.info("finished cxn begin")
         val tupleUpdate = cxn.prepareUpdate(QueryLanguage.SPARQL, update)
         //logger.info("finished prepare update")
         tupleUpdate.execute()
         //logger.info("finished execute")
         cxn.commit()
         //logger.info("changes committed")
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
    
    /**
     * Returns input string if no quotes, otherwise removes quotes and returns input string without quotes. Only leading and trailing quotes
     * are removed. Non-leading or trailing quotes in string will not be affected. Both leading and trailing quotation marks must be present
     * on the string in order to trigger removal; just one or the other present will lead to the method returning the original input string with
     * all original quotation marks.
     * 
     * @return a String containing the input string, minus leading and trailing quotation marks if they existed.
     */
    def removeQuotesFromString(input: String): String =
    {
        var result: String = input
        if (result.length > 0)
        {
            if (result.charAt(0) == '"' && result.charAt(result.length-1) == '"')
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
        updateSparql(cxn, sparqlPrefixes + moveTriples)
    }
    
    /**
     * Overloaded method which prints all triples in all named graphs to the console.
     */
    def printAllInDatabase(cxn: RepositoryConnection)
    {
        val queryAll: String = "SELECT ?s ?p ?o WHERE {?s ?p ?o .}"
        val results = querySparqlAndUnpackTuple(cxn, queryAll, Array("s","p","o"))
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
        val results = querySparqlAndUnpackTuple(cxn, queryAll, Array("s","p","o"))
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
    def writeErrorLog (process: String, error: String, variables: String)
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
        querySparqlBoolean(cxn, sparql).get
    }

  /**
   * Adds an RDF.XML formatted set of triples (usually an ontology) received from a given URL to the specified named graph.
   */
  def addOntologyFromUrl(cxn: RepositoryConnection, 
      ontology: String = "https://turbo-prd-app01.pmacs.upenn.edu/ontology/turbo_merged.owl", 
      namedGraph: String = "http://www.itmat.upenn.edu/biobank/ontology") 
  {
      try
      {
          val f = cxn.getValueFactory
          val OntoUrl = new URL(ontology)
          val OntoGraphName = f.createIRI(namedGraph);
      
          val OntoBase = "http://transformunify.org/ontologies/"
       
          cxn.begin()
          cxn.add(OntoUrl, OntoBase, RDFFormat.RDFXML, OntoGraphName)
          cxn.commit()
      }
      catch
      {
          case f: ConnectException => logger.info("The ontology was not loaded. Please ensure that your URL in the properties file is correct and that the server is online.")
          throw new RuntimeException ("A connection to the ontology could not be established.")
          case e: RuntimeException => logger.info("The ontology was not loaded. Please ensure that your URL in the properties file is correct and that the server is online.")
          throw new RuntimeException ("The ontology could not be accessed at the specified URL.")
      }
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
            
      updateSparql(cxn, sparqlPrefixes + insert)
  }
  
  /**
   * Searches through all named graphs excluding ontology named graphs for inverse properties, as defined by the TURBO ontology.
   * Inserts the corresponding inverse triple into a special named graphs called pmbb:inverses.
   */
  def applyInverses (cxn: RepositoryConnection)
    {
        val check1: String = """
        insert 
        {
       	    graph <http://www.itmat.upenn.edu/biobank/inverses> {
       	    ?o ?inverse ?s .
       	 }
        }
        where {
            graph ?g {
                ?s ?p ?o .
        	}
        	# this minus is to make sure that we do not re-add an inverse that already exists in a named graph other than pmbb:inverses
        	minus
        	{
        	    ?o ?inverse ?s .
        	}
    		graph pmbb:ontology
    		{
                ?p owl:inverseOf ?inverse .
    		}
        	FILTER (?g != pmbb:inverses)
        	FILTER (?g != pmbb:ontology)
    		  FILTER (?p != obo:IAO_0000136)
    		  FILTER (?g != pmbb:ICD9Ontology)
          FILTER (?g != pmbb:ICD10Ontology)
          FILTER (?g != pmbb:mondoOntology)
        }
        """
        updateSparql(cxn, sparqlPrefixes + check1)
    }
  
    /**
     * Automatically deletes all triples from all named graphs which contain shortcut relationships. This is called as part of the post-expansion
     * process.
     */
    def clearShortcutNamedGraphs (cxn: RepositoryConnection)
    {
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/participantShortcuts")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts1")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts2")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts3")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts4")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/biobankEncounterShortcuts")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/healthcareJoinShortcuts")
        clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/biobankJoinShortcuts")
    }
    
    /**
     * This is a strange method which executes a batched Referent Tracking completition phase, which entails moving all properties from a non-reftracked
     * node to a reftracked node. The referent tracker class is responsible for creating pointers from non-reftracked nodes to reftracked nodes but not
     * for migrating the properties or retiring the non-reftracked node. 
     */
    def completeReftrackProcess (cxn: RepositoryConnection)
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
          val result: ArrayBuffer[String] = querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getNodesToBeReftracked, "node")
          var values: String = ""
          val resultSize = result.size
          val threshold: Integer = 1000
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
        updateSparql(cxn, sparqlPrefixes + completeReftrackProcess)
        //querySparqlAndUnpackTuple(cxn, sparqlPrefixes + completeReftrackProcess, ArrayBuffer("originalNode", "reftrackedNode", "demotionType", "predicateForDelete1", 
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
         updateSparql(cxn, sparqlPrefixes + fixNodesThatAreNotObjects)
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
         updateSparql(cxn, sparqlPrefixes + removeTemporaryPredicates)
         
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
         updateSparql(cxn, sparqlPrefixes + removeGraphBuilder)
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
        
        updateSparql(cxn, sparqlPrefixes + addLabelsToEverything)
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
        
        updateSparql(cxn, sparqlPrefixes + update)
    }
    
    /**
     * This is an attempt to programatically change the reasoning level of a repository. It has an optional boolean parameter
     * to reinfer the repository. This functionality is not fully supported by Ontotext Graph DB and this method should be used
     * with care.
     */
    def changeReasoningLevelAndReinferRepository(cxn: RepositoryConnection, newLevel: String, reinfer: Boolean)
    {
        if (newLevel != "empty" && newLevel != "owl-horst-optimized") logger.info("Reasoning level " + newLevel + " is not supported.")
        else 
        {
            logger.info("Attempting to change reasoning level to " + newLevel)
            val addRuleset: String = """ INSERT DATA {_:b sys:addRuleset """"+newLevel+"""" } """
            val setDefaultRuleset: String = """ INSERT DATA {_:b sys:defaultRuleset """"+newLevel+"""" } """    
            val reinferRepo: String = """ INSERT DATA {[] <http://www.ontotext.com/owlim/system#reinfer> []} """
            updateSparql(cxn, sparqlPrefixes + addRuleset)
            updateSparql(cxn, sparqlPrefixes + setDefaultRuleset)
            
            if (reinfer)
            {
                logger.info("Reinferring...")
                updateSparql(cxn, sparqlPrefixes + addRuleset) 
            }
            logger.info("Done.")
        }
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
    def checkStringArraysForEquivalency(arr1: Array[String], arr2: Array[String]): Boolean =
    {
        logger.info("about to check string arrays for equivalency")
        
        logger.info("size of array 1: " + arr1.size)
        logger.info("size of array 2: " + arr2.size)
        
        //first, sort each array in alphabetical order
        scala.util.Sorting.quickSort(arr1)
        scala.util.Sorting.quickSort(arr2)
        
        //search line by line for differences in array
        findSortedArrayDifferences(arr1, 0, arr2, 0)
        
        logger.info("nonMatchesArr1") 
        for (a <- nonMatchesArr1) logger.info(a)
        logger.info("nonMatchesArr2")
        for (a <- nonMatchesArr2) logger.info(a)
        
        var boolToReturn: Boolean = false
        if (nonMatchesArr1.size == 0 && nonMatchesArr2.size == 0) boolToReturn = true

        nonMatchesArr1 = new ArrayBuffer[String]
        nonMatchesArr2 = new ArrayBuffer[String]
        
        boolToReturn
    }
    
    /**
     * This is a recursive method which checks two given arrays of strings against each other and returns the differences. 
     * Keeps track of differences using global variables nonMatchesArr1 and nonMatchesArr2.
     * 
     * Do you even recurse bro? (I hope you do it better than me)
     */
    def findSortedArrayDifferences(arr1: Array[String], index1: Int, arr2: Array[String], index2: Int)
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
}