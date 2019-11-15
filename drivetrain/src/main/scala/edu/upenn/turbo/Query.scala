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
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    
    var bindClause: String = ""
    var whereClause: String = ""
    var insertClause: String = ""
    var deleteClause: String = ""
    
    var defaultInputGraph: String = null
    var defaultRemovalsGraph: String = null
    
    var processSpecification: String = null
    var process: String = null

    var whereClauseBuilder: WhereClauseBuilder = new WhereClauseBuilder()
    var insertClauseBuilder: InsertClauseBuilder = new InsertClauseBuilder()
    var deleteClauseBuilder: DeleteClauseBuilder = new DeleteClauseBuilder()
    var bindClauseBuilder: BindClauseBuilder = new BindClauseBuilder()
    
    var varsForProcessInput = new HashSet[String]
    var rawInputData = new ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]
    
    /* Contains set of of variables used in bind and where clauses, so the insert clause knows that these have already been defined. Any URI present in the 
     in the insert clause will not be converted to a variable unless it is included in this list. The boolean value represents whether a URI is qualified
     to be a multiplicity enforcer for the bind clause. To be qualified, it must have a type declared in the Where block and at some point be listed as required
     input to the process. */
    var usedVariables: HashMap[String, Boolean] = new HashMap[String, Boolean]
    
    override def runQuery(cxn: RepositoryConnection)
    {
        query = getQuery()
        assert (query != "" && query != null)
        update.updateSparql(cxn, query)
    }
    
    override def getQuery(): String = deleteClause + "\n" + insertClause + "\n" + whereClause + bindClause + "}"
    
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
    
    def setProcessSpecification(processSpecification: String)
    {
        assert (processSpecification.contains(':'))
        this.processSpecification = processSpecification
    }
    
    def setProcess(process: String)
    {
        assert (process.contains(':'))
        this.process = process
    }
    
    def setInputData(inputData: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        rawInputData = inputData
    }
    
    def createInsertClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        assert (insertClause == "")
        insertClauseBuilder.setGraphModelConnection(gmCxn)
        if (whereClause == null || whereClause.size == 0 || bindClause == null || bindClause.size == 0) 
        {
            throw new RuntimeException("Insert clause cannot be built before where or bind clauses are built.")
        }
        insertClauseBuilder.addTripleFromRowResult(outputs, process, varsForProcessInput, usedVariables)
        assert (insertClauseBuilder.clause != null && insertClauseBuilder.clause != "")
        assert (insertClauseBuilder.clause.contains("GRAPH"))
        val innerClause = insertClauseBuilder.clause
        insertClause += s"INSERT { \n$innerClause}"
    }

    def createDeleteClause(removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        assert (deleteClause == "")
        assert (defaultRemovalsGraph != null && defaultRemovalsGraph != "")
        
        deleteClauseBuilder.addTripleFromRowResult(removals, defaultRemovalsGraph)
        assert (deleteClauseBuilder.clause != null && deleteClauseBuilder.clause != "")
        assert (deleteClauseBuilder.clause.contains("GRAPH"))
        val innerClause = deleteClauseBuilder.clause
        deleteClause += s"DELETE { \n$innerClause}"
    }
    
    def createWhereClause(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        assert (whereClause == "")
        assert (defaultInputGraph != null && defaultInputGraph != "")
        whereClauseBuilder.setGraphModelConnection(gmCxn)
        varsForProcessInput = whereClauseBuilder.addTripleFromRowResult(inputs, defaultInputGraph)
        // this part of the code determines whether a variable is qualified to be a multiplicity enforcer in the bind clause. If set to true then it is qualified.
        for (row <- inputs) 
        {
            var subjectAnInstance = false
            var objectAnInstance = false
            var objectADescriber = false
            var subjectADescriber = false
            var required = true
            if (row(OBJECTANINSTANCE.toString) != null) objectAnInstance = true
            if (row(SUBJECTANINSTANCE.toString) != null) subjectAnInstance = true
            if (row(SUBJECTADESCRIBER.toString) != null) subjectADescriber = true
            if (row(OBJECTADESCRIBER.toString) != null) objectADescriber = true
            
            if (row(INPUTTYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/hasOptionalInput") required = false
            
            var objectResultBool = true
            if (!required || !(objectAnInstance || objectADescriber)) objectResultBool = false
            
            var subjectResultBool = true
            if (!required || !(subjectAnInstance || subjectADescriber)) subjectResultBool = false
            
            var thisObject = row(OBJECT.toString).toString
            var thisSubject = row(SUBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) thisSubject += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) thisObject += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)

            if (objectADescriber || objectAnInstance) if (!usedVariables.contains(thisObject) || !usedVariables(thisObject)) usedVariables += thisObject -> objectResultBool
            if (subjectADescriber || subjectAnInstance) if (!usedVariables.contains(thisSubject) || !usedVariables(thisSubject)) usedVariables += thisSubject -> subjectResultBool
        }
        assert (whereClauseBuilder.clause != null && whereClauseBuilder.clause != "")
        assert (whereClauseBuilder.clause.contains("GRAPH"))
        val innerClause = whereClauseBuilder.clause
        whereClause += s"WHERE { \n$innerClause"
    }

    def createBindClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], localUUID: String)
    {
        assert (bindClause == "")
        if (whereClause == null || whereClause.size == 0) 
        {
            throw new RuntimeException("Bind clause cannot be built before where clause is built.")
        }
        for ((k,v) <- bindClauseBuilder.buildBindClause(outputs, inputs, localUUID, processSpecification, usedVariables))
        {
            if (!usedVariables.contains(k)) usedVariables += k -> v
        }
        bindClause = bindClauseBuilder.clause
        // if we attempted to build the bind clause and no binds were necessary, make the clause contain a single empty character so that other clauses can be built
        if (bindClause == "") bindClause += " "
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