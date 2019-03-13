package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.HashSet
import java.util.UUID
import org.eclipse.rdf4j.model.Value

object ShortcutHomoSapiensFromGraph extends ProjectwideGlobals
{
    //val typeURI = "http://haydensgraph.org/shortcut_obo_NCBITaxon_9606"
    val typeURI = "http://purl.obolibrary.org/obo/NCBITaxon_9606"
    val pattern = buildPattern(typeURI)  
    
    def buildPattern(typeURI: String): String =
    {
        var pattern = ""
        val res = getGraphData(Array(typeURI))
        for (a <- res)
        {
            //for (b <- a) print(b)
            //println()
            var bool = true
            var subject = ""
            
            if (a(0) == null) subject = a(3).toString
            else subject = a(0).toString
            
            val predicate = a(1).toString
            var objectVal = ""
            
            if (a(2) == null) objectVal = a(3).toString
            else objectVal = a(2).toString
            
            if (a(4) == "false") bool = false
            if (!bool) pattern += "OPTIONAL {\n"
            pattern += "<" + subject + "> <" + predicate + "> <" + objectVal + ">"
            if (!bool) pattern += "}"
            pattern += "\n"
          //println(a(0) + " " + a(1) + " " + a(2) + " " + a(3) + " " + a(4))
        }
        pattern
    }
    
    def getGraphData(typeURI: Array[String], traversedNodes: HashSet[String] = new HashSet[String]): ArrayBuffer[ArrayBuffer[Value]] =
    {
        println()
        for (a <- traversedNodes) println(a)
        println()
        var nextResults = new ArrayBuffer[String]
        var newSet = traversedNodes
        var resultsToReturn = new ArrayBuffer[ArrayBuffer[Value]]
        for (a <- typeURI)
        {
            //println("did not contain")
            val query: String = s"""
            
            Select ?subjValue ?predValue ?objValue ?required ?typeURI Where
            {
              Values ?typeURI {<$a>}
              {
                ?graphConnection a graph:ConnectionFromShortcutExpansion .
                ?graphConnection graph:subject ?typeURI .
                ?graphConnection graph:predicate ?predValue .
                ?graphConnection graph:object ?objValue .
                ?graphConnection graph:required ?required .
              }
              UNION
              {
                ?graphConnection a graph:ConnectionFromShortcutExpansion .
                ?graphConnection graph:subject ?subjValue .
                ?graphConnection graph:predicate ?predValue .
                ?graphConnection graph:object ?typeURI .
                ?graphConnection graph:required ?required .
              }
            }
            
            """
            newSet = newSet + a
            
            var patternList = update.querySparqlAndUnpackTuple(sparqlPrefixes + query, Array("subjValue", "predValue", "objValue", "typeURI", "required"))
            println()
            println("searched on type: " + a)
            for (a <- patternList)
            {
                var bool = false
                if (a(0) != null && !traversedNodes.contains(a(0).toString))
                {
                    nextResults += a(0).toString
                    println(a(0))
                    bool = true
                }
                else if (a(2) != null && !traversedNodes.contains(a(2).toString)) 
                {
                  nextResults += a(2).toString
                  println(a(2))
                  bool = true
                }
                println("adding results to return: " + a(0) + " " + a(1) + " " + a(2) + " " + a(3))
                if (bool) resultsToReturn += a
            }
        }
        if (nextResults.size > 0) for (a <- getGraphData(nextResults.toArray, newSet)) resultsToReturn += a
        resultsToReturn
    }
}