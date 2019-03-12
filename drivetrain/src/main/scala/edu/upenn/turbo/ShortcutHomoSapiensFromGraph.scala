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
    val typeURI = "http://haydensgraph.org/shortcut_obo_NCBITaxon_9606"
    val pattern = buildPattern(typeURI)  
    
    def buildPattern(typeURI: String): String =
    {
        var pattern = ""
        val res = getGraphData(typeURI).toSet.toList
        for (a <- res)
        {
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
          println(a(0) + " " + a(1) + " " + a(2) + " " + a(3) + " " + a(4))
        }
        pattern
    }
    
    def getGraphData(typeURI: String, traversedNodes: HashSet[String] = new HashSet[String]): ArrayBuffer[ArrayBuffer[Value]] =
    {
        println("building pattern for uri " + typeURI)
        println("traversed nodes: ")
        for (a <- traversedNodes)
        {
          println(a)
        }
        if (traversedNodes.contains(typeURI)) ArrayBuffer(ArrayBuffer())
        else
        {
            println("did not contain")
            val query: String = s"""
            
            Select ?subjValue ?predValue ?objValue ?required ?typeURI Where
            {
              Values ?typeURI {<$typeURI>}
              {
                ?graphConnection a graph:ObjectConnectionFromShortcuts .
                ?graphConnection graph:subject ?typeURI .
                ?graphConnection graph:predicate ?predValue .
                ?graphConnection graph:object ?objValue .
                ?graphConnection graph:required ?required .
              }
              UNION
              {
                ?graphConnection a graph:ObjectConnectionFromShortcuts .
                ?graphConnection graph:subject ?subjValue .
                ?graphConnection graph:predicate ?predValue .
                ?graphConnection graph:object ?typeURI .
                ?graphConnection graph:required ?required .
              }
            }
            
            """
            val newSet = traversedNodes + typeURI
            var patternList = update.querySparqlAndUnpackTuple(sparqlPrefixes + query, Array("subjValue", "predValue", "objValue", "typeURI", "required"))
            for (a <- patternList)
            {
                if (a(0) != null) for (b <- getGraphData(a(0).toString, newSet)) if (b.size > 0) patternList += b
                if (a(2) != null) for (c <- getGraphData(a(2).toString, newSet)) if (c.size > 0) patternList += c
            }
            patternList
        }
    }
}