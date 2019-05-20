package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

class TriplesGroupBuilder extends ProjectwideGlobals
{
    val requiredGroup = new RequiredGroup()
    val optionalGroups = new ArrayBuffer[OptionalGroup]
    
    def addRequiredTripleToRequiredGroup(triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        for (reqTriple <- requiredGroup.requiredInGroup(graph))
        {
            if (triple.isSameAs(reqTriple)) okToAdd = false
        }
        if (okToAdd)
        {
            if (requiredGroup.requiredInGroup.contains(graph)) requiredGroup.requiredInGroup(graph) += triple
            else requiredGroup.requiredInGroup += graph -> ArrayBuffer(triple)
        }
    }
    
    def addOptionalTripleToRequiredGroup(triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        for (optTriple <- requiredGroup.optionalInGroup(graph))
        {
            if (triple.isSameAs(optTriple)) okToAdd = false
        }
        if (okToAdd)
        {
            if (requiredGroup.optionalInGroup.contains(graph)) requiredGroup.optionalInGroup(graph) += triple
            else requiredGroup.optionalInGroup += graph -> ArrayBuffer(triple)
        }
    }
    
    def addRequiredTripleToOptionalGroup(optionalGroup: OptionalGroup, triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        for (reqTriple <- optionalGroup.requiredInGroup(graph))
        {
            if (triple.isSameAs(reqTriple)) okToAdd = false
        }
        if (okToAdd)
        {
            if (optionalGroup.requiredInGroup.contains(graph)) optionalGroup.requiredInGroup(graph) += triple
            else optionalGroup.requiredInGroup += graph -> ArrayBuffer(triple)
        }
    }
    
    def addOptionalTripleToOptionalGroup(optionalGroup: OptionalGroup, triple: Triple, graph: String)
    {
        var okToAdd: Boolean = true
        for (optTriple <- optionalGroup.optionalInGroup(graph))
        {
            if (triple.isSameAs(optTriple)) okToAdd = false
        }
        if (okToAdd)
        {
            if (optionalGroup.optionalInGroup.contains(graph)) optionalGroup.optionalInGroup(graph) += triple
            else optionalGroup.optionalInGroup += graph -> ArrayBuffer(triple)
        }
    }
    
    def addToOptionalGroup(triple: Triple, graph: String, groupName: String, required: Boolean)
    {
        var groupToAdd: OptionalGroup = null
        for (group <- optionalGroups)
        {
            if (group.groupName == groupName)
            {
                groupToAdd = group
            }
        }
        if (groupToAdd == null)
        {
           groupToAdd = new OptionalGroup()
           optionalGroups += groupToAdd
        }
        
        if (required) addRequiredTripleToOptionalGroup(groupToAdd, triple, graph)
        else addOptionalTripleToOptionalGroup(groupToAdd, triple, graph)
    }
    
    def buildClauseFromTriplesGroup(): String =
    {
        ""
    }
}

class TriplesGroup extends ProjectwideGlobals
{
    var requiredInGroup: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
    var optionalInGroup: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
}

class RequiredGroup extends TriplesGroup
{
    
}

class OptionalGroup extends TriplesGroup
{
    var groupName: String = null
}