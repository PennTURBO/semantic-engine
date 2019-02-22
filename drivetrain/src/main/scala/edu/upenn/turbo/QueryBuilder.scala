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
    def whereBuilder(buildList: Map[GraphObject, Boolean], connectionList: Map[GraphObject, GraphObject], valuesList: Map[String, Array[String]], limit: Integer = null)
    {
        var whereBlocks = new HashMap[GraphObject, String]
        var connectionStrings = ""
        for ((k,v) <- valuesList)
        {
            whereClause += "Values ?" + k + "{"
            for (value <- v) whereClause += " " + value
            whereClause += "}\n"
        }
        for ((k,v) <- buildList)
        {
            var entry: String = ""
            entry += "GRAPH <" + k.namedGraph + "> {"
            if (!v) entry += "OPTIONAL {"
            entry += k.pattern
            
            for ((key, value) <- k.mandatoryLinks)
            {
               entry += value.pattern
            }
                
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
    
    def selectBuilder(buildTypes: Array[GraphObject])
    {      
        for (item <- buildTypes)
        {
            for (variable <- item.variablesToSelect)
            {
                selectClause += "?" + variable + " "
            }
        }
    }

    def bindBuilder(buildTypes: Array[ShortcutGraphObject], localUUID: String, globalUUID: String)
    {
        for (a <- buildTypes)
        {
            val expansionRules = a.expansionRules
            for (expansionRule <- expansionRules)
            {
                val rule = expansionRule.rule
                val shortcutVariable = expansionRule.shortcutVariableName
                val expandedVariable = expansionRule.expandedVariableName
                val dependent = expansionRule.dependent
                
                var thisBind = rule.replaceAll("replacement",expandedVariable)
                                    .replaceAll("localUUID",localUUID)
                                    .replaceAll("globalUUID",globalUUID)
                                    .replaceAll("mainExpansionTypeVariableName",a.baseVariableName)
                if (thisBind.contains("dependent")) thisBind = thisBind.replaceAll("dependent",dependent)
                if (thisBind.contains("original")) thisBind = thisBind.replaceAll("original",shortcutVariable)
                thisBind = thisBind.replaceAll("instantiationPlaceholder","\""+a.instantiation+"\"")
                bindClause += thisBind+"\n"
            }
            bindClause += a.appendToBind+"\n"
        }
    }

    def insertBuilder(buildTypes: Array[GraphObject])
    {
        for (a <- buildTypes)
        {
            addInsertClauseToString(a)

            for ((k,nodeType) <- a.optionalLinks)
            {
                addInsertClauseToString(nodeType)
            }
            for ((k,nodeType) <- a.mandatoryLinks)
            {
                addInsertClauseToString(nodeType)
            }
        }
        insertClause += "}"
    }

    def addInsertClauseToString(buildType: GraphObject)
    {
        insertClause += " GRAPH <" + buildType.namedGraph + "> {"
        insertClause += buildType.pattern
        insertClause += buildType.optionalPattern
        insertClause += "}"
    }

    def deleteBuilder(){}
    
    def filterBuilder(equivalencyList: (Map[String, String]))
    {
        for ((k,v) <- equivalencyList)
        {
            whereClause = whereClause.replaceAll("\\?" + k + " ", "\\?" + v + " ")
        }
    }
}