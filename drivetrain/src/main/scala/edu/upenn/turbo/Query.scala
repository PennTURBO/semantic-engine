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
    
    override def getQuery(): String = deleteClause + "\n" + insertClause + "\n" + whereClause + "\n" + bindClause + "\n}"
    
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
        if (whereClause == null || whereClause.size == 0) 
        {
            throw new RuntimeException("Insert clause cannot be built before where clause is built.")
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
        var bindRules = new HashSet[String]
        for (rule <- binds)
        {
            val thatSubject = rule("thatSubject")
            val thisObject = rule("thisObject")
            val thatObject = rule("thatObject")
            val thisSubject = rule("thisSubject")
            
            val thisMultiplicity = rule("multiplicity").toString
            
            val thatObjectAsVar = helper.convertTypeToSparqlVariable(thatObject)
            val thatSubjectAsVar = helper.convertTypeToSparqlVariable(thatSubject)
            val thisObjectAsVar = helper.convertTypeToSparqlVariable(thisObject)
            val thisSubjectAsVar = helper.convertTypeToSparqlVariable(thisSubject)
            
            val expansionRule = rule("someRule").toString
            
            if (thatObject != thisObject)
            {
                if (expansionRule == "http://transformunify.org/ontologies/expansionCreatesObjectOf")
                {
                    if (thisMultiplicity == "http://transformunify.org/ontologies/many-singleton") 
                    {
                      bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                        SHA256(CONCAT(\"${thatObjectAsVar}\",\"${localUUID}\",\"${process}")))) AS ${thatObjectAsVar})\n"""
                    }
                    else
                    {
                        var multiplicityMaker = thisSubjectAsVar
                        if (thisMultiplicity == "http://transformunify.org/ontologies/many-1") multiplicityMaker = thisObjectAsVar
                        bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                          SHA256(CONCAT(\"${thatObjectAsVar}\",\"${localUUID}\", str(${multiplicityMaker}))))) AS ${thatObjectAsVar})\n"""   
                    }
                }
            }
            if (thatSubject != thisSubject)
            {
                if (expansionRule == "http://transformunify.org/ontologies/expansionCreatesSubjectOf")
                {
                    if (thisMultiplicity == "http://transformunify.org/ontologies/singleton-many") 
                    {
                      bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                        SHA256(CONCAT(\"${thatSubjectAsVar}\",\"${localUUID}\",\"${process}")))) AS ${thatSubjectAsVar})\n"""
                    }
                    else
                    {
                        var multiplicityMaker = thisSubjectAsVar
                        if (thisMultiplicity == "http://transformunify.org/ontologies/1-many") multiplicityMaker = thisObjectAsVar
                        bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                          SHA256(CONCAT(\"${thatSubjectAsVar}\",\"${localUUID}\", str(${multiplicityMaker}))))) AS ${thatSubjectAsVar})\n"""   
                    }
                }
            }
        }
        for (a <- bindRules) bindClause += a
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