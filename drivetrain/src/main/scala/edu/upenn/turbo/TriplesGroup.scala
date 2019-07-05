package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

class TriplesGroupBuilder extends ProjectwideGlobals
{
    val INSERT, WHERE, INSERT_DATA, DELETE = Value
    val MINUS, OPTIONAL, REQUIRED = Value
    
    val requiredGroup = new TriplesGroup(REQUIRED)
    val optionalGroups = new ArrayBuffer[TriplesGroup]
    val minusGroups = new ArrayBuffer[TriplesGroup]
    
    val allGraphsUsed = new HashSet[String]
    val typesUsed = new HashSet[String]
    
    var variablesUsed: HashSet[String] = null
    
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

    def addTripleToMinusGroup(triple: Triple, graph: String, groupName: String)
    {
        var minusGroup: TriplesGroup = null
        for (group <- minusGroups)
        {
            if (group.groupName == groupName)
            {
                minusGroup = group
            }
        }
        if (minusGroup == null)
        {
           minusGroup = new TriplesGroup(MINUS)
           minusGroup.groupName = groupName
           minusGroups += minusGroup
        }
        var okToAdd: Boolean = true
        if (minusGroup.requiredInGroup.contains(graph))
        {
            for (reqTriple <- minusGroup.requiredInGroup(graph))
            {
                if (triple.isSameAs(reqTriple)) okToAdd = false
            }
            if (okToAdd) minusGroup.requiredInGroup(graph) += triple
        }
        else
        {
            minusGroup.requiredInGroup += graph -> ArrayBuffer(triple)
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
           groupToAdd = new TriplesGroup(OPTIONAL)
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

    def buildDeleteClauseFromTriplesGroup(): String =
    {
        buildClauseFromTriplesGroup(DELETE)
    }
    
    def buildClauseFromTriplesGroup(clauseType: Value): String =
    {
        var clause = ""
        for (graph <- allGraphsUsed)
        {
            var useGraphForRequired = false
            if (requiredGroup.requiredInGroup.contains(graph)) useGraphForRequired = true
            if (!useGraphForRequired)
            {
              for (optionalGroup <- optionalGroups)
              {
                  if (optionalGroup.requiredInGroup.contains(graph) || optionalGroup.optionalInGroup.contains(graph)) useGraphForRequired = true
              }
            }
            if (useGraphForRequired)
            {
                clause += s"GRAPH <$graph> {\n"
                clause += addTriplesToClause(clauseType, requiredGroup, graph)
                for (optionalGroup <- optionalGroups)
                {
                    clause += addTriplesToClause(clauseType, optionalGroup, graph)
                }
                clause += "}\n"   
            }
        }
        if (minusGroups.size != 0) clause += "MINUS {\n"
        for (graph <- allGraphsUsed)
        {
            var useGraphForMinus = false
            for (minusGroup <- minusGroups)
            {
                if (minusGroup.requiredInGroup.contains(graph) || minusGroup.optionalInGroup.contains(graph)) useGraphForMinus = true
            }
            if (useGraphForMinus)
            {
                clause += s"GRAPH <$graph> {\n"
                for (minusGroup <- minusGroups)
                {
                    clause += addTriplesToClause(clauseType, minusGroup, graph)
                }
                clause += "}\n"
            }
        }
        if (minusGroups.size != 0) clause += "}\n"
        clause
    }
    
    def addTriplesToClause(clauseType: Value, group: TriplesGroup, graph: String): String =
    {
        assert(clauseType == WHERE || clauseType == INSERT || clauseType == INSERT_DATA || clauseType == DELETE)
        assert(group.groupType == OPTIONAL || group.groupType == REQUIRED || group.groupType == MINUS)
        var clause = ""
        if (group.requiredInGroup.contains(graph) || group.optionalInGroup.contains(graph))
        {
            if (group.groupType == OPTIONAL) 
            {
                if (clauseType != WHERE) throw new RuntimeException("Optional triples not allowed outside of WHERE clause")
                clause += "OPTIONAL {\n"
            }
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
            if (group.groupType == OPTIONAL) clause += "}\n"   
        }
        clause
    }
    
    def makeTriple(clauseType: Value, triple: Triple): String =
    {
        var clause = ""
        if (clauseType == WHERE || clauseType == DELETE) clause += triple.makeTripleWithVariables()
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
    def this(groupType: Object)
    { 
        this()
        this.groupType = groupType.asInstanceOf[Value]
    }
    
    var groupName: String = null
    var groupType: Value = null

    var requiredInGroup: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
    var optionalInGroup: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
}