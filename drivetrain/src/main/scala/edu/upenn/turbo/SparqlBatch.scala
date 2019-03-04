package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer

trait SparqlBatch extends ProjectwideGlobals
{
    def batchFunction(cxn: RepositoryConnection, 
                      drivetrainFunction: (String, String, Map[String, Array[String]]) => String, 
                      variableToBatch: GraphObjectSingleton, argsList: Array[String], 
                      incrementsOf: Integer = 500)
    {
        println("Beginning batch process for function " + drivetrainFunction + 
            " using variable " + variableToBatch.baseVariableName + " with increments of " 
            + incrementsOf)
        val valuesList = helper.getAllValuesOfType(cxn, variableToBatch)
        println("Total values to process: " + valuesList.size)
        
        var currentValues = new ArrayBuffer[String]
        var count = 0
        var totalCount = 0
        for (value <- valuesList)
        {
            count = count + 1  
            currentValues += value
            if (count % incrementsOf == 0)
            {
                val query = drivetrainFunction(argsList(0), 
                                               argsList(1), 
                                               Map(variableToBatch.baseVariableName -> currentValues.toArray))
                //println("query: " + query)
                update.updateSparql(cxn, query)
                currentValues = new ArrayBuffer[String]
                totalCount += count
                count = 0
                println("Processed " + totalCount + " values.")
            }
        }
        val query = drivetrainFunction(argsList(0), 
                                       argsList(1), 
                                       Map(variableToBatch.baseVariableName -> currentValues.toArray))
        //println("query: " + query)
        update.updateSparql(cxn, query)
    }
}