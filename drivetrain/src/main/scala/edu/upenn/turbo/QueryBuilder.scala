package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import scala.collection.mutable.HashMap

class QueryBuilder extends Query with IRIConstructionRules
{
    def whereBuilder(buildList: Map[GraphObject, Boolean], connectionList: Map[GraphObject, GraphObject], limit: Integer = null)
    {
        var whereBlocks = new HashMap[GraphObject, String]
        var connectionStrings = ""
        for ((k,v) <- buildList)
        {
            var entry: String = ""
            entry += "GRAPH <" + k.namedGraph + "> {"
            if (!v) entry += "OPTIONAL {"
            entry += k.pattern
            whereBlocks += k -> entry
        }
        for ((k,v) <- connectionList) 
        {
            val connectionPredicate = k.connections(v.typeURI)
            val entry = "?" + k.baseVariableName + " <" + connectionPredicate + "> ?" + v.baseVariableName + " ."
            if (!buildList(k)) whereBlocks(k) += entry
            else if (!buildList(v)) whereBlocks(v) += entry
            else connectionStrings += entry + "\n"
        }
        for ((k,v) <- whereBlocks)
        {
            whereClause += v + "}\n"
            if (!buildList(k)) whereClause += "}\n"
        } 
        whereClause += connectionStrings
        if (limit != null) whereClause += "LIMIT " + limit.toString
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

    def bindBuilder(buildTypes: Array[ShortcutGraphObject], localUUID: String, globalUUID: String)
    {
        for (a <- buildTypes)
        {
            val dependencyConversions = a.expandedVariableShortcutDependencies
            val bindAsConversions = a.expandedVariableShortcutBindings
            for ((rule,list) <- a.variableExpansions)
            {
                for (element <- list)
                {
                    var thisBind = rule.replaceAll("replacement",element)
                                        .replaceAll("localUUID",localUUID)
                                        .replaceAll("globalUUID",globalUUID)
                                        .replaceAll("mainExpansionTypeVariableName",a.baseVariableName)
                    if (thisBind.contains("dependent")) thisBind = thisBind.replaceAll("dependent",dependencyConversions(element))
                    if (thisBind.contains("original")) thisBind = thisBind.replaceAll("original",bindAsConversions(element))
                    thisBind = thisBind.replaceAll("\\?instantiationUUID","\""+a.instantiation+"\"")
                    bindClause += thisBind+"\n"
                }
            }
        }
    }

    def insertBuilder(buildTypes: Array[ExpandedGraphObject])
    {
        for (a <- buildTypes)
        {
            addInsertClauseToString(a)

            for (nodeType <- a.optionalLinks)
            {
                addInsertClauseToString(nodeType)
            }
            for (nodeType <- a.mandatoryLinks)
            {
                addInsertClauseToString(nodeType)
            }
        }
        insertClause += "}"
    }

    def addInsertClauseToString(buildType: ExpandedGraphObject)
    {
        insertClause += " GRAPH <" + buildType.namedGraph + "> {"
        insertClause += buildType.pattern
        insertClause += buildType.optionalPattern
        insertClause += "}"
    }

    def deleteBuilder(){}
}