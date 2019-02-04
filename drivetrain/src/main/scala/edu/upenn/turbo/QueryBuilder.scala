package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class QueryBuilder 
{
    def whereBuilder(buildList: Map[GraphObject, Boolean], connectionList: Map[GraphObject, GraphObject]): String =
    {
        var entry: String = ""
        
        for ((key,value) <- buildList)
        {
            entry += "GRAPH <" + key.namedGraph + "> {"
            if (!value) entry += "OPTIONAL {"
            entry += key.pattern
            if (!value) entry += "}"
            if (connectionList.contains(key))
            {
                val connectedElement = connectionList(key)
                var optionalConnection = false
                if (!value || !buildList(connectedElement))
                {
                    entry += "OPTIONAL {"
                    optionalConnection = true
                }
                val connectionPredicate = key.connections(connectedElement.typeURI)
                entry += "?" + key.baseVariableName + " <" + connectionPredicate + "> ?" + connectedElement.baseVariableName + " ."
                if (optionalConnection) entry += "}"
            }
            
            entry += "} \n"
        }
        
        var where: String = "WHERE { " + entry + " }"
        
        where
    }
    
    def selectBuilder(buildTypes: Array[GraphObject]): String =
    {
        var select = "SELECT "
      
        for (item <- buildTypes)
        {
            for (variable <- item.variablesToSelect)
            {
                select += "?" + variable + " "
            }
        }
        select
    }
}