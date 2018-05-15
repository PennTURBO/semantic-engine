package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

/**
 * This class contains methods which validate and commit error logging from the data integrity checks. 
 */
class DataIntegrityCheckOperations extends ProjectwideGlobals
{
    /**
     * Receives a string representation of a SPARQL query and a list of strings which are the variables defined within that query. Runs query to see if errors exist
     * within the dataset. If errors are found, they are logged by helper method.
     * 
     * @return a Boolean, true if no error was found, false if error was found
     */
    def runSparqlCheck (cxn: RepositoryConnection, check: String, variables: ArrayBuffer[String], stage: String, explanation: String, ruleNumber: Int = 1): Boolean =
    {
        //logger.info("checking for result")
        val result: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check, variables)
        //logger.info("obtained result from check query")
        if (checkResultForError(result, ruleNumber)) 
        {
            var varResults = ""
            if (result.size == 0) helper.writeErrorLog(stage, explanation, "no variable data found in resultset")
            else
            {
                for (a <- result(0)) 
                {
                    if (a == null) varResults += "empty,"
                    else varResults += a.toString + ","
                }
                helper.writeErrorLog(stage, explanation, varResults)
            }
            false
        }
        else true
    }
    
    /**
     * Receives result from a SPARQL query in the form of a list of lists of values, as well as a rule number. Applies specific, hard-coded rule 
     * which applies to the specified rule number to the SPARQL results to determine if an error exists.
     * 
     * @return a Boolean, true if an error is found, false if no error was found 
     */
    def checkResultForError(result: ArrayBuffer[ArrayBuffer[Value]], ruleNumber: Int): Boolean =
    {
         if (ruleNumber == 1)
         {
             //this rule enforces that no results are present - this is the default rule used if none provided
             if (result.size > 0 && result(0)(0) != null) true
             else false
         }
         else if (ruleNumber == 2)
         {
             //this rule enforces that there is exactly one result
             if (result.size == 1) false
             else true
         }
         else false
    }
}