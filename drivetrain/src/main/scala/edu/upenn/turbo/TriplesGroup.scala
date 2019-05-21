package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

class TriplesGroupBuilder extends Enumeration with ProjectwideGlobals
{
    val requiredGroup = new TriplesGroup()
    val optionalGroups = new ArrayBuffer[TriplesGroup]
    
    val allGraphsUsed = new HashSet[String]
    val typesUsed = new HashSet[String]
    
    var variablesUsed: HashSet[String] = null
    
    val INSERT, WHERE, INSERT_DATA = Value
    
    def addRequiredTripleToRequiredGroup(triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        if (requiredGroup.requiredInGroup.contains(graph))
        {
            for (reqTriple <- requiredGroup.requiredInGroup(graph))
            {
                if (triple.isSameAs(reqTriple)) okToAdd = false
            }
            if (okToAdd) requiredGroup.requiredInGroup(graph) += triple
        }
        else
        {
            requiredGroup.requiredInGroup += graph -> ArrayBuffer(triple)
            allGraphsUsed += graph
        }
    }
    
    def addOptionalTripleToRequiredGroup(triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        if (requiredGroup.optionalInGroup.contains(graph))
        {
            for (reqTriple <- requiredGroup.optionalInGroup(graph))
            {
                if (triple.isSameAs(reqTriple)) okToAdd = false
            }
            if (okToAdd) requiredGroup.optionalInGroup(graph) += triple
        }
        else
        {
            requiredGroup.optionalInGroup += graph -> ArrayBuffer(triple)
            allGraphsUsed += graph
        }
    }
    
    def addRequiredTripleToOptionalGroup(optionalGroup: TriplesGroup, triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        if (optionalGroup.requiredInGroup.contains(graph))
        {
            for (reqTriple <- optionalGroup.requiredInGroup(graph))
            {
                if (triple.isSameAs(reqTriple)) okToAdd = false
            }
            if (okToAdd) optionalGroup.requiredInGroup(graph) += triple
        }
        else
        {
            optionalGroup.requiredInGroup += graph -> ArrayBuffer(triple)
            allGraphsUsed += graph
        }
    }
    
    def addOptionalTripleToOptionalGroup(optionalGroup: TriplesGroup, triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        if (optionalGroup.optionalInGroup.contains(graph))
        {
            for (reqTriple <- optionalGroup.optionalInGroup(graph))
            {
                if (triple.isSameAs(reqTriple)) okToAdd = false
            }
            if (okToAdd) optionalGroup.optionalInGroup(graph) += triple
        }
        else
        {
            optionalGroup.optionalInGroup += graph -> ArrayBuffer(triple)
            allGraphsUsed += graph
        }
    }
    
    def addToOptionalGroup(triple: Triple, graph: String, groupName: String, required: Boolean)
    {
        var groupToAdd: TriplesGroup = null
        for (group <- optionalGroups)
        {
            if (group.groupName == groupName)
            {
                groupToAdd = group
            }
        }
        if (groupToAdd == null)
        {
           groupToAdd = new TriplesGroup()
           groupToAdd.groupName = groupName
           optionalGroups += groupToAdd
        }
        
        if (required) addRequiredTripleToOptionalGroup(groupToAdd, triple, graph)
        else addOptionalTripleToOptionalGroup(groupToAdd, triple, graph)
    }
    
    def buildInsertClauseFromTriplesGroup(variablesUsed: HashSet[String]): String =
    {
        this.variablesUsed = variablesUsed
        buildClauseFromTriplesGroup(INSERT)
    }
    
    def buildWhereClauseFromTriplesGroup(): String =
    {
        buildClauseFromTriplesGroup(WHERE)
    }
    
    def buildInsertDataClauseFromTriplesGroup(): String =
    {
        buildClauseFromTriplesGroup(INSERT_DATA)
    }
    
    def buildClauseFromTriplesGroup(clauseType: Value): String =
    {
        var clause = ""
        for (graph <- allGraphsUsed)
        {
            clause += s"GRAPH <$graph> {\n"
            clause += addTriplesToClause(clauseType, requiredGroup, graph, false)
            for (optionalGroup <- optionalGroups)
            {
                logger.info("adding group: " + optionalGroup.groupName)
                clause += addTriplesToClause(clauseType, optionalGroup, graph, true)
            }
            clause += "}\n"
        }
        clause
    }
    
    def addTriplesToClause(clauseType: Value, group: TriplesGroup, graph: String, optionalGroup: Boolean): String =
    {
        assert(clauseType == WHERE || clauseType == INSERT || clauseType == INSERT_DATA)
        var clause = ""
        if (optionalGroup) clause += "OPTIONAL {\n"
        if (group.requiredInGroup.contains(graph))
        {
            for (triple <- group.requiredInGroup(graph))
            {
               clause += makeTriple(clauseType, triple)
               clause += addTypeTriples(triple)
            }
        }
        if (group.optionalInGroup.contains(graph))
        {
            for (triple <- group.optionalInGroup(graph))
            {
                val tripleString = makeTriple(clauseType, triple)
                assert(tripleString != null && tripleString != "")
                clause += s"OPTIONAL {\n $tripleString "
                clause += addTypeTriples(triple)
                clause += "}\n"
            }   
        }
        if (optionalGroup) clause += "}\n"
        clause
    }
    
    def makeTriple(clauseType: Value, triple: Triple): String =
    {
        var clause = ""
        if (clauseType == WHERE) clause += triple.makeTripleWithVariables()
        else if (clauseType == INSERT)
        {
            assert(variablesUsed != null && variablesUsed.size != 0)
            clause += triple.makeTripleWithVariablesIfPreexisting(variablesUsed)
        }
        else if (clauseType == INSERT_DATA) clause += triple.makeTriple()
        clause
    }
    
    def addTypeTriples(triple: Triple): String =
    {
       var clause = ""
       if (triple.subjectAType && !typesUsed.contains(triple.tripleSubject)) 
       {
           clause += triple.makeSubjectTypeTriple()
           typesUsed += triple.tripleSubject
       }
       if (triple.objectAType && !typesUsed.contains(triple.tripleObject)) 
       {
           clause += triple.makeObjectTypeTriple()
           typesUsed += triple.tripleObject
       }
       clause
    }
}

class TriplesGroup extends ProjectwideGlobals
{
    var requiredInGroup: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
    var optionalInGroup: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
    
    var groupName: String = null
}