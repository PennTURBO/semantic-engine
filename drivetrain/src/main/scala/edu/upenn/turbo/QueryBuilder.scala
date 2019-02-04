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
    def whereBuilder(nodeType: GraphObject, notOptional: Boolean): String =
    {
        var entry: String = ""
        
        entry += "GRAPH <" + nodeType.namedGraph + "> {"
        if (!notOptional) entry += "OPTIONAL {"
        entry += nodeType.pattern
        entry
    }
    
    def selectBuilder(buildTypes: Array[GraphObject]): String =
    {
        var select = ""
      
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