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
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils

class SparqlUpdater
{
    val logger = LoggerFactory.getLogger(getClass)
    val cxn = DrivetrainDriver.cxn
    
    val sparqlPrefixes = """
			PREFIX  dc11: <http://purl.org/dc/elements/1.1/>
			PREFIX  obo:  <http://purl.obolibrary.org/obo/>
			PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
			PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>
			PREFIX  turbo: <http://transformunify.org/ontologies/>
			PREFIX  ontologies: <http://transformunify.org/ontologies/>
			PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>
			PREFIX  nci:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
			PREFIX graphBuilder: <http://graphBuilder.org/>
			PREFIX pmbb: <http://www.itmat.upenn.edu/biobank/>
			PREFIX sys: <http://www.ontotext.com/owlim/system#>
			PREFIX efo: <http://www.ebi.ac.uk/efo/>
			PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
			PREFIX ns1: <http://www.geneontology.org/formats/oboInOwl#>
			PREFIX graph: <http://haydensgraph.org/>
			PREFIX j.0: <http://example.com/resource/>
      PREFIX snomed: <http://purl.bioontology.org/ontology/SNOMEDCT/>
      PREFIX oboInOwl: <http://www.geneontology.org/formats/oboInOwl#>
      PREFIX Thesaurus: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
      PREFIX properties: <https://github.com/PennTURBO/Drivetrain/blob/master/turbo_properties.properties/>
      PREFIX drivetrain: <https://github.com/PennTURBO/Drivetrain/>
			"""
    
    
    /**
     * Overloaded method which is Drivetrain's main point of access to Graph DB for SPARQL queries. Used for when only one variable is requested
     * to be returned in the result set.
     * 
     * @return ArrayBuffer[String] representing the results of the query
     */
      def querySparqlAndUnpackTuple(cxn: RepositoryConnection, query: String, variable: String): ArrayBuffer[String] =
      {
          assert (cxn != null, "The RepositoryConnection object was null")
          val result: Option[TupleQueryResult] = querySparql(cxn, sparqlPrefixes + query)
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
          assert (cxn != null, "The RepositoryConnection object was null")
          val result: Option[TupleQueryResult] = querySparql(cxn, sparqlPrefixes + query)
          val unpackedResult: ArrayBuffer[ArrayBuffer[Value]] = unpackTuple(result.get, variable)
          //close tupleQueryResult to free resources
          result.get.close()
          unpackedResult
      }
      
      def querySparqlAndUnpackTuple(query: String, variable: Array[String]): ArrayBuffer[ArrayBuffer[Value]] =
      {
          assert (cxn != null, "The RepositoryConnection object was null")
          val result: Option[TupleQueryResult] = querySparql(cxn, sparqlPrefixes + query)
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
          assert (cxn != null, "The RepositoryConnection object was null")
          val result: Option[TupleQueryResult] = querySparql(cxn, sparqlPrefixes + query)
          val unpackedResult: ArrayBuffer[ArrayBuffer[Value]] = unpackTuple(result.get, variable)
          //close tupleQueryResult to free resources
          result.get.close()
          unpackedResult
      }

      def querySparqlAndUnpackToMap(cxn: RepositoryConnection, query: String): HashMap[String, ArrayBuffer[Value]] =
      {
          assert (cxn != null, "The RepositoryConnection object was null")
          val result: Option[TupleQueryResult] = querySparql(cxn, sparqlPrefixes + query)
          val unpackedResult: HashMap[String, ArrayBuffer[Value]] = unpackTupleToMap(result.get)
          //close tupleQueryResult to free resources
          result.get.close()
          unpackedResult
      }
      
      def querySparqlAndUnpackToListOfMap(cxn: RepositoryConnection, query: String): ArrayBuffer[HashMap[String, Value]] =
      {
          assert (cxn != null, "The RepositoryConnection object was null")
          val result: Option[TupleQueryResult] = querySparql(cxn, sparqlPrefixes + query)
          val unpackedResult: ArrayBuffer[HashMap[String, Value]] = unpackTupleToListOfMaps(result.get)
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
          assert (cxn != null, "The RepositoryConnection object was null")
          try
          {
              val boolQueryResult: BooleanQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlPrefixes + query)
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
            var result: String = ""
            try
            {
                result = removeQuotesFromString(bindingset.getValue(variableToUnpack).toString)
            }
            catch
            {
                case e: NullPointerException => throw new RuntimeException(s"Variable $variableToUnpack requested but not present in query select statement")
            }
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
                var result: Value = null
                val variableToUnpack = variables(a)
                try
                {
                    result = bindingset.getValue(variableToUnpack)
                }
                catch
                {
                    case e: NullPointerException => throw new RuntimeException(s"Variable $variableToUnpack requested but not present in query select statement")
                }
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
        unpackTuple(resultTuple, variables.toArray)
    }

    def unpackTupleToMap(resultTuple: TupleQueryResult): HashMap[String, ArrayBuffer[Value]] =
    {
        //Create empty map of lists to be populated and returned
        var resultList: HashMap[String, ArrayBuffer[Value]] = new HashMap[String, ArrayBuffer[Value]]
        //For each result, add it to the list. Note that this method does not convert to Strings but leaves results as Values
        while (resultTuple.hasNext())
        {
            val oneResult: ArrayBuffer[Value] = new ArrayBuffer[Value]
            val bindingset: BindingSet = resultTuple.next()
            for (a <- bindingset.getBindingNames().toArray)
            {
                val variableName = a.toString
                val result: Value = bindingset.getValue(variableName)
                if (resultList.contains(variableName)) resultList(variableName) += result
                else resultList += variableName -> ArrayBuffer(result)
            }
        }
        //Return map of lists of Value objects
        resultList
    }
    
    def unpackTupleToListOfMaps(resultTuple: TupleQueryResult): ArrayBuffer[HashMap[String, Value]] =
    {
        //Create empty list of maps to be populated and returned
        var resultList: ArrayBuffer[HashMap[String, Value]] = new ArrayBuffer[HashMap[String, Value]]
        //For each result, add it to the list. Note that this method does not convert to Strings but leaves results as Values
        while (resultTuple.hasNext())
        {
            val oneResult: HashMap[String, Value] = new HashMap[String, Value]
            val bindingset: BindingSet = resultTuple.next()
            for (a <- bindingset.getBindingNames().toArray)
            {
                val variableName = a.toString
                val result: Value = bindingset.getValue(variableName)
                oneResult += variableName -> result
            }
            resultList += oneResult
        }
        //Return list of maps of Value objects
        resultList
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
         val tupleUpdate = cxn.prepareUpdate(QueryLanguage.SPARQL, sparqlPrefixes + update)
         //logger.info("finished prepare update")
         tupleUpdate.execute()
         //logger.info("finished execute")
         cxn.commit()
         //logger.info("changes committed") 
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
    
    /*def querySparqlWithHTTP(searchTerm: String)
    {
        val post = new HttpPost("http://data.bioontology.org/search?q="+searchTerm)
        post.setHeader("Authorization", "api-key token="+apiKey)
        val client = HttpClientBuilder.create().build()
        val response = client.execute(post)
        val responseData = response.getEntity
        val responseString: String = EntityUtils.toString(responseData)
        logger.info(responseString)
        response.close()
        client.close()
    }*/
}