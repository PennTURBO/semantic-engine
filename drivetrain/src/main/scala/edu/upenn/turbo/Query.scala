package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.eclipse.rdf4j.model.Value

abstract class Query extends ProjectwideGlobals
{
    var query: String = ""
    var defaultOutputGraph: String = null
    
    def runQuery(cxn: RepositoryConnection)
    {
        assert (query != "" && query != null)
        update.updateSparql(cxn, query)
    }
    
    def getQuery(): String = query
}

class PatternMatchQuery extends Query
{
    var bindClause: String = ""
    var whereClause: String = ""
    var insertClause: String = ""
    var deleteClause: String = ""
    
    var defaultInputGraph: String = null
    var defaultRemovalsGraph: String = null
    
    var process: String = null

    var whereClauseBuilder: WhereClauseBuilder = new WhereClauseBuilder()
    var insertClauseBuilder: InsertClauseBuilder = new InsertClauseBuilder()
    var deleteClauseBuilder: DeleteClauseBuilder = new DeleteClauseBuilder()
    
    var varsForProcessInput = new HashSet[String]
    
    /* Contains set of of variables used in bind and where clauses, so the insert clause knows that these have already been defined. Any URI present in the 
     in the insert clause will not be converted to a variable unless it is included in this list. */
    var usedVariables: HashSet[String] = new HashSet[String]
    
    override def runQuery(cxn: RepositoryConnection)
    {
        query = getQuery()
        assert (query != "" && query != null)
        update.updateSparql(cxn, query)
    }
    
    override def getQuery(): String = insertClause + "\n" + deleteClause + "\n" + whereClause + "\n" + bindClause + "\n}"
    
    def setInputGraph(inputGraph: String)
    {
        helper.validateURI(inputGraph)
        this.defaultInputGraph = inputGraph
    }
    
    def setOutputGraph(outputGraph: String)
    {
        helper.validateURI(outputGraph)
        this.defaultOutputGraph = outputGraph
    }
    
    def setRemovalsGraph(removalsGraph: String)
    {
        helper.validateURI(removalsGraph)
        this.defaultRemovalsGraph = removalsGraph
    }
    
    def setProcess(process: String)
    {
        assert (process.contains(':'))
        this.process = process
    }
    
    def createInsertClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        assert (insertClause == "")
        if (bindClause == "" || bindClause == null || whereClause == null || whereClause.size == 0) 
        {
            throw new RuntimeException("Insert clause cannot be built before bind clause and insert clause are built.")
        }
        insertClauseBuilder.addTripleFromRowResult(outputs, process, varsForProcessInput, usedVariables)
        assert (insertClauseBuilder.clause != null && insertClauseBuilder.clause != "")
        assert (insertClauseBuilder.clause.contains("GRAPH"))
        val innerClause = insertClauseBuilder.clause
        insertClause += s"INSERT { \n $innerClause \n}"
    }

    def createDeleteClause(removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        assert (deleteClause == "")
        assert (defaultRemovalsGraph != null && defaultRemovalsGraph != "")
        
        deleteClauseBuilder.addTripleFromRowResult(removals, defaultRemovalsGraph)
        assert (deleteClauseBuilder.clause != null && deleteClauseBuilder.clause != "")
        assert (deleteClauseBuilder.clause.contains("GRAPH"))
        val innerClause = deleteClauseBuilder.clause
        deleteClause += s"DELETE { \n $innerClause \n}"
    }
    
    def createWhereClause(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        assert (whereClause == "")
        assert (defaultInputGraph != null && defaultInputGraph != "")
        
        varsForProcessInput = whereClauseBuilder.addTripleFromRowResult(inputs, defaultInputGraph)
        for (row <- inputs) 
        {
            usedVariables += row(OBJECT.toString).toString
            usedVariables += row(SUBJECT.toString).toString
        }
        assert (whereClauseBuilder.clause != null && whereClauseBuilder.clause != "")
        assert (whereClauseBuilder.clause.contains("GRAPH"))
        val innerClause = whereClauseBuilder.clause
        whereClause += s"WHERE { \n $innerClause "
    }
    
    def createBindClause(binds: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], localUUID: String)
    {
        assert (bindClause == "")
        var varList = new ArrayBuffer[Value]
        for (rule <- binds)
        {
            var sparqlBind = rule(SPARQLSTRING.toString).toString.replaceAll("\\$\\{replacement\\}", 
                            helper.convertTypeToSparqlVariable(rule(EXPANDEDENTITY.toString)))
                                         .replaceAll("\\$\\{localUUID\\}", localUUID)
                                         .replaceAll("\\$\\{globalUUID\\}", RunDrivetrainProcess.globalUUID)
                                         .replaceAll("\\$\\{mainExpansionTypeVariableName\\}", helper.convertTypeToSparqlVariable(rule(BASETYPE.toString)))
                                         .replaceAll("\\$\\{instantiationPlaceholder\\}", "\"" + RunDrivetrainProcess.instantiation + "\"")
            if (sparqlBind.contains("${dependent}")) sparqlBind = sparqlBind.replaceAll("\\$\\{dependent\\}",
                helper.convertTypeToSparqlVariable(rule(DEPENDEE.toString)))
            if (sparqlBind.contains("${original}")) sparqlBind = sparqlBind.replaceAll("\\$\\{original\\}",
                helper.convertTypeToSparqlVariable(rule(SHORTCUTENTITY.toString)))
            if (sparqlBind.contains("${singletonType}")) sparqlBind = sparqlBind.replaceAll("\\$\\{singletonType\\}",
                rule(DEPENDEE.toString).toString)
            
            bindClause += sparqlBind.substring(1).split("\"\\^")(0) + "\n"
            
            // add all variables used in bind clause to list of used variables
            if (rule(EXPANDEDENTITY.toString) != null) usedVariables += rule(EXPANDEDENTITY.toString).toString
            if (rule(SHORTCUTENTITY.toString) != null) usedVariables += rule(SHORTCUTENTITY.toString).toString
            if (rule(DEPENDEE.toString) != null) usedVariables += rule(DEPENDEE.toString).toString
            if (rule(BASETYPE.toString) != null) usedVariables += rule(BASETYPE.toString).toString
        }
    }
}

class DataQuery extends Query
{
    val dataInsertTriplesGroup = new InsertDataClauseBuilder()
    def createInsertDataClause(triples: ArrayBuffer[Triple], graph: String)
    {
        
        query += s"INSERT DATA {\n"
        dataInsertTriplesGroup.buildInsertDataClauseFromTriplesList(triples, graph)
        assert (dataInsertTriplesGroup.clause != null && dataInsertTriplesGroup.clause != "")
        assert (dataInsertTriplesGroup.clause.contains("GRAPH"))
        query += dataInsertTriplesGroup.clause
        query += "}"
    }
}