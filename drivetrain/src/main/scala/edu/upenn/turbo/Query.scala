package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.eclipse.rdf4j.model.Value
import java.util.UUID

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

class PatternMatchQuery(cxn: RepositoryConnection) extends Query
{
    this.gmCxn = cxn
    
    var bindClause: String = ""
    var whereClause: String = ""
    var insertClause: String = ""
    var deleteClause: String = ""
    
    var defaultInputGraph: String = null
    var defaultRemovalsGraph: String = null
    
    var processSpecification: String = null
    var process: String = null

    var bindClauseBuilder: BindClauseBuilder = new BindClauseBuilder()
        
    /* Contains set of of variables used in bind and where clauses, so the insert clause knows that these have already been defined. Any URI present in the 
     in the insert clause will not be converted to a variable unless it is included in this list. The boolean value represents whether a URI is qualified
     to be a multiplicity enforcer for the bind clause. To be qualified, it must have a type declared in the Where block and at some point be listed as required
     input to the process. */
    var usedVariables: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    
    override def runQuery(cxn: RepositoryConnection)
    {
        query = getQuery()
        assert (query != "" && query != null)
        val graphUUID = UUID.randomUUID().toString().replaceAll("-", "")
        logger.info(query)
        update.updateSparql(cxn, query)
    }
    
    override def getQuery(): String = deleteClause + "\n" + insertClause + "\n" + whereClause + bindClause + "}"
    
    def setInputGraph(inputGraph: String)
    {
        this.defaultInputGraph = inputGraph
        helper.validateURI(this.defaultInputGraph)
    }
    
    def setOutputGraph(outputGraph: String)
    {
        this.defaultOutputGraph = outputGraph
        helper.validateURI(this.defaultOutputGraph)
    }
    
    def setRemovalsGraph(removalsGraph: String)
    {
        this.defaultRemovalsGraph = removalsGraph
        helper.validateURI(this.defaultRemovalsGraph)
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
    
    def createInsertClause(outputs: HashSet[ConnectionRecipe])
    {
        assert (insertClause == "")
        if (whereClause == null || whereClause.size == 0 || bindClause == null || bindClause.size == 0) 
        {
            throw new RuntimeException("Insert clause cannot be built before where or bind clauses are built.")
        }
        assert (defaultOutputGraph != null && defaultOutputGraph != "")
        val groupBuilder = new InsertClauseBuilder
        insertClause = groupBuilder.buildInsertGroup(outputs, defaultOutputGraph)
    }

    def createDeleteClause(removals: HashSet[ConnectionRecipe])
    {
        assert (deleteClause == "")
        assert (defaultRemovalsGraph != null && defaultRemovalsGraph != "")
        val groupBuilder = new DeleteClauseBuilder
        deleteClause = groupBuilder.buildDeleteGroup(removals, defaultRemovalsGraph)
    }
    
    def createWhereClause(inputs: HashSet[ConnectionRecipe])
    {
        assert (inputs.size != 0)
        assert (whereClause == "")
        assert (defaultInputGraph != null && defaultInputGraph != "", "No default input named graph set")
        val groupBuilder = new WhereClauseBuilder
        whereClause = groupBuilder.buildWhereGroup(inputs, defaultInputGraph)
    }

    def createBindClause(inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe], localUUID: String)
    {
        assert (inputs.size != 0)
        assert (bindClause == "")
        if (whereClause == null || whereClause.size == 0) 
        {
            throw new RuntimeException("Bind clause cannot be built before where clause is built.")
        }
        usedVariables = bindClauseBuilder.buildBindClause(process, localUUID, inputs, outputs)
     
        bindClause = bindClauseBuilder.clause
        // if we attempted to build the bind clause and no binds were necessary, make the clause contain a single empty character so that other clauses can be built
        if (bindClause == "") bindClause += " "
    }
}