package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.eclipse.rdf4j.model.Value

abstract class Query extends ProjectwideGlobals
{
    var query: String = ""
    var outputGraph: String = null
    
    def runQuery(cxn: RepositoryConnection)
    {
        assert (query != "" && query != null)
        update.updateSparql(cxn, query)
    }
    
    def setOutputGraph(graph: String)
    {
        assert (graph.contains(':'))
        this.outputGraph = outputGraph
    }
}

class PatternMatchQuery extends Query
{
    var bindClause: String = ""
    var whereClause: String = ""
    var insertClause: String = ""
    
    var inputGraph: String = null
    var process: String = null
    
    /* Contains set of of variables used in bind and where clauses, so the insert clause knows that these have already been defined. Any URI present in the 
     in the insert clause will not be converted to a variable unless it is included in this list. */
    var usedVariables: HashSet[String] = new HashSet[String]
    
    def getQuery(): String = query
    
    def setInputGraph(graph: String)
    {
        assert (graph.contains(':'))
        this.inputGraph = inputGraph
    }
    
    def setProcess(process: String)
    {
        assert (process.contains(':'))
        this.process = process
    }
    
    def createInsertClause(outputs: ArrayBuffer[HashMap[String, Value]])
    {
        assert (insertClause == "")
        val insertClauseTriplesGroup: TriplesGroup = new TriplesGroup()
        if (bindClause == "" || bindClause == null || whereClause == null || whereClause.size == 0) 
        {
            throw new RuntimeException("Insert clause cannot be built before bind clause and insert clause are built.")
        }
        {
            for (row <- outputs) insertClauseTriplesGroup.addTripleFromModelGraphRowResult(row, false)
            
        }
    }
    
    def createWhereClause(inputs: ArrayBuffer[HashMap[String, Value]])
    {
        assert (whereClause == "")
        var innerClause: String = ""
        
        val whereClauseTriplesGroup: TriplesGroup = new TriplesGroup()
        for (row <- inputs) whereClauseTriplesGroup.addTripleFromModelGraphRowResult(row)
        for ((graph, optionalGroupList) <- whereClauseTriplesGroup.groupMap)
        {
            var graphString = s"GRAPH $graph { \n $replacementString \n}\n"
            var triplesString = ""
            for ((groupName, triples) <- optionalGroupList.groupList)
            {
                if (groupName != noGroup) triplesString += "OPTIONAL {\n"
                for (triple <- triples) triplesString += triple.makeTripleWithVariables()
                if (groupName != noGroup) triplesString += "}\n"
            }
            innerClause += graphString.replaceAll(replacementString, triplesString)
        }
        whereClause += s"WHERE { \n $innerClause }"
    }
    
    def createBindClause(binds: ArrayBuffer[HashMap[String, Value]], localUUID: String)
    {
        assert (bindClause == "")
        var varList = new ArrayBuffer[Value]
        for (rule <- binds)
        {
            var sparqlBind = rule(sparqlString).toString.replaceAll("\\$\\{replacement\\}", 
                            helper.convertTypeToSparqlVariable(rule(expandedEntity)))
                                         .replaceAll("\\$\\{localUUID\\}", localUUID)
                                         .replaceAll("\\$\\{globalUUID\\}", RunDrivetrainProcess.globalUUID)
                                         .replaceAll("\\$\\{mainExpansionTypeVariableName\\}", helper.convertTypeToSparqlVariable(rule(baseType)))
                                         .replaceAll("\\$\\{instantiationPlaceholder\\}", "\"" + RunDrivetrainProcess.instantiation + "\"")
            if (sparqlBind.contains("${dependent}")) sparqlBind = sparqlBind.replaceAll("\\$\\{dependent\\}",
                helper.convertTypeToSparqlVariable(rule(dependee)))
            if (sparqlBind.contains("${original}")) sparqlBind = sparqlBind.replaceAll("\\$\\{original\\}",
                helper.convertTypeToSparqlVariable(rule(shortcutEntity)))
            if (sparqlBind.contains("${singletonType}")) sparqlBind = sparqlBind.replaceAll("\\$\\{singletonType\\}",
                rule(dependee).toString)
            
            bindClause += sparqlBind.substring(1).split("\"\\^")(0) + "\n"
            
            // add all variables used in bind clause to list of used variables
            usedVariables += rule(expandedEntity).toString
            usedVariables += rule(shortcutEntity).toString
            usedVariables += rule(dependee).toString
            usedVariables += rule(baseType).toString
        }
    }
}

class DataQuery extends Query
{
    def createInsertDataClause(triples: ArrayBuffer[Triple])
    {
        assert (outputGraph != null)
        query += s"INSERT DATA {\n Graph $outputGraph {\n"
        for (triple <- triples) query += triple.makeTriple()
        query += "}}"
    }
}