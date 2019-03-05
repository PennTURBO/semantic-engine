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
    def whereBuilder(args: WhereBuilderQueryArgs)
    {
        var whereBlocks = new HashMap[GraphObjectInstance, String]
        var connectionStrings = ""
        if (args.valuesList != null)
        {
            for ((k,v) <- args.valuesList)
            {
                whereClause += "Values ?" + k + "{"
                for (value <- v) whereClause += " <" + value + "> "
                whereClause += "}\n"
            }
        }
        for (element <- args.buildList)
        {
            val elementOptional = element.optional
            var entry: String = ""
            entry += "GRAPH <" + element.namedGraph + "> {"
            if (elementOptional) entry += "OPTIONAL {"
            entry += element.pattern
            
            for ((key, value) <- element.mandatoryLinks)
            {
               entry += value.pattern
            }
                
            whereBlocks += element -> entry
        }
        /*for ((k,v) <- connectionList) 
        {
            val connectionPredicate = k.connections(v.typeURI)
            val entry = "?" + k.baseVariableName + " <" + connectionPredicate + "> ?" + v.baseVariableName + " ."
            if (!args.buildList(k)) whereBlocks(k) += entry
            else if (!args.buildList(v)) whereBlocks(v) += entry
            else connectionStrings += entry + "\n"
        }*/
        for ((k,v) <- whereBlocks)
        {
            whereClause += v + "}\n"
            if (k.optional) whereClause += "}\n"
        } 
        whereClause += connectionStrings
        if (args.limit != null) whereClause += "LIMIT " + args.limit.toString
    }
    
    def selectBuilder(buildTypes: Array[GraphObjectInstance])
    {      
        for (item <- buildTypes)
        {
            for (variable <- item.variablesToSelect)
            {
                selectClause += "?" + variable + " "
            }
        }
    }

    def bindBuilder(buildTypes: Array[ShortcutGraphObjectInstance], localUUID: String, globalUUID: String)
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

    def insertBuilder(buildTypes: Array[GraphObjectInstance])
    {
        for (a <- buildTypes)
        {
            addInsertClauseToString(a)

            for ((k,nodeType) <- a.optionalLinks)
            {
                val newInstance = nodeType.asInstanceOf[ExpandedGraphObjectSingleton].create(false)
                addInsertClauseToString(newInstance)
            }
            for ((k,nodeType) <- a.mandatoryLinks)
            {
                val newInstance = nodeType.asInstanceOf[ExpandedGraphObjectSingleton].create(false)
                addInsertClauseToString(newInstance)
            }
        }
        insertClause += "}"
    }

    def addInsertClauseToString(buildType: GraphObjectInstance)
    {
        insertClause += " GRAPH <" + buildType.namedGraph + "> {"
        insertClause += buildType.pattern
        for (pattern <- buildType.optionalPatterns) insertClause += pattern
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
    
    def reset()
    {
        insertClause = "INSERT {"
        deleteClause = "DELETE {"
        selectClause = "SELECT "
        whereClause = "WHERE {"
        bindClause = ""
        filterClause = ""
    }
}