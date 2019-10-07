package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import java.util.LinkedHashSet
import java.util.LinkedHashMap

class TriplesGroupBuilder extends ProjectwideGlobals
{
    val INSERT, WHERE, INSERT_DATA, DELETE = Value
    val MINUS, OPTIONAL, REQUIRED = Value
    
    val requiredGroup = new TriplesGroup(REQUIRED)
    val optionalGroups = new ArrayBuffer[TriplesGroup]
    val minusGroups = new ArrayBuffer[TriplesGroup]
    
    val allGraphsUsed = new LinkedHashSet[String]
    val typesUsed = new HashSet[String]
    
    var variablesUsed: HashSet[String] = null
    var valuesBlock = new HashMap[String, String]
    
    def setValuesBlock(valuesBlock: HashMap[String, String])
    {
        this.valuesBlock = valuesBlock
    }
    
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
            allGraphsUsed.add(graph)
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
            allGraphsUsed.add(graph)
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
            allGraphsUsed.add(graph)
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
            allGraphsUsed.add(graph)
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
            allGraphsUsed.add(graph)
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
        var graphsAndTriples: HashMap[String, LinkedHashMap[String, Boolean]] = new HashMap[String, LinkedHashMap[String, Boolean]]
        val graphsIterator = allGraphsUsed.iterator()
        while (graphsIterator.hasNext())
        {
            val graph = graphsIterator.next()
            var useGraphForRequired = false
            if (requiredGroup.requiredInGroup.contains(graph) || requiredGroup.optionalInGroup.contains(graph)) useGraphForRequired = true
            if (!useGraphForRequired)
            {
              for (optionalGroup <- optionalGroups)
              {
                  if (optionalGroup.requiredInGroup.contains(graph) || optionalGroup.optionalInGroup.contains(graph)) useGraphForRequired = true
              }
            }
            if (useGraphForRequired)
            {
                if (graphsAndTriples.contains(graph)) graphsAndTriples(graph).put(addTriplesToClause(clauseType, requiredGroup, graph), false)
                else 
                {
                    val newLinkedMap = new LinkedHashMap[String, Boolean]
                    newLinkedMap.put(addTriplesToClause(clauseType, requiredGroup, graph), false)
                    graphsAndTriples += graph -> newLinkedMap
                }
                for (optionalGroup <- optionalGroups)
                {
                    if (graphsAndTriples.contains(graph)) graphsAndTriples(graph).put(addTriplesToClause(clauseType, optionalGroup, graph), false)
                    else 
                    {
                        val newLinkedMap = new LinkedHashMap[String, Boolean]
                        newLinkedMap.put(addTriplesToClause(clauseType, optionalGroup, graph), false)
                        graphsAndTriples += graph -> newLinkedMap
                    }
                }
            }
            var useGraphForMinus = false
            for (minusGroup <- minusGroups)
            {
                if (minusGroup.requiredInGroup.contains(graph) || minusGroup.optionalInGroup.contains(graph)) 
                {
                    assert (clauseType.toString == "WHERE", "Minus triples not allowed outside of WHERE clause")
                    useGraphForMinus = true
                }
            }
            if (useGraphForMinus)
            {
                for (minusGroup <- minusGroups)
                {
                    // this code makes the assumption that only one minus group will be present per graph. This isn't a sustainable assumption but works for now.
                    if (graphsAndTriples.contains(graph)) graphsAndTriples(graph).put("MINUS {\n" + addTriplesToClause(clauseType, minusGroup, graph) + "}\n", false)
                    else 
                    {
                        val newLinkedMap = new LinkedHashMap[String, Boolean]
                        newLinkedMap.put(addTriplesToClause(clauseType, minusGroup, graph), false)
                        graphsAndTriples += graph -> newLinkedMap
                    }
                }
            }
        }
        createClauseFromGraphsAndTriplesGroup(graphsAndTriples)
    }
    
    def createClauseFromGraphsAndTriplesGroup(graphsAndTriples: HashMap[String, LinkedHashMap[String, Boolean]]): String =
    {
        // this code makes the assumption that only one minus group will be present per graph. This isn't a sustainable assumption but works for now.
        var clause = ""
        for ((graph,triplesGroups) <- graphsAndTriples)
        {
            var intermediateClause = ""
            var useMinus = false
            val triplesGroupsIterator = triplesGroups.entrySet().iterator()
            while (triplesGroupsIterator.hasNext())
            {
                val iteratorItem = triplesGroupsIterator.next()
                val group = iteratorItem.getKey()
                val minus = iteratorItem.getValue()
                if (minus) useMinus = true
                intermediateClause += group
            }
            if (useMinus) clause += "MINUS {\n"
            clause += s"GRAPH <$graph> {\n"
            clause += intermediateClause
            clause += "}\n"
            if (useMinus) clause += "}\n"
        }
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
                assert (clauseType.toString == "WHERE", "Optional triples not allowed outside of WHERE clause")
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
            clause += triple.makeTripleWithVariablesExcludeList(variablesUsed)
        }
        else if (clauseType == INSERT_DATA) clause += triple.makeTriple()
        
        if (clauseType == WHERE)
        {
            val subjWithoutBrackets = helper.removeAngleBracketsFromString(triple.tripleSubject)
            val objWithoutBrackets = helper.removeAngleBracketsFromString(triple.tripleObject)
            
            if (valuesBlock.contains(subjWithoutBrackets))
            {
                clause += valuesBlock(subjWithoutBrackets) + "\n"
                valuesBlock.remove(subjWithoutBrackets)
            }
            if (valuesBlock.contains(objWithoutBrackets))
            {
                clause += valuesBlock(objWithoutBrackets) + "\n"
                valuesBlock.remove(objWithoutBrackets)
            }   
        }
        clause
    }
    
    def addTypeTriples(triple: Triple): String =
    {
       var clause = ""
       if (triple.subjectAType && !typesUsed.contains(triple.tripleSubject + triple.subjectContext)) 
       {
           clause += triple.makeSubjectTypeTriple()
           typesUsed += triple.tripleSubject + triple.subjectContext
       }
       if (triple.objectAType && !typesUsed.contains(triple.tripleObject + triple.objectContext)) 
       {
           clause += triple.makeObjectTypeTriple()
           typesUsed += triple.tripleObject + triple.objectContext
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