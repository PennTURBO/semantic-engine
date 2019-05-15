package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

class TriplesGroup extends ProjectwideGlobals
{
    var namedGraph: String = null
    var groupMap: HashMap[String, TriplesGroupList] = new HashMap[String, TriplesGroupList]

    def addTripleFromModelGraphRowResult(rowResult: HashMap[String, Value], useOptionals: Boolean = true)
    {
        val graphForThisRow = rowResult(graph).toString
        
        var subjectTypeTriple: Triple = null
        var objectTypeTriple: Triple = null
        
        if (rowResult(subjectType) != null) subjectTypeTriple = new Triple(rowResult(subject).toString, "rdf:type", rowResult(subject).toString, true)
        if (rowResult(objectType) != null) objectTypeTriple = new Triple(rowResult(objectVar).toString, "rdf:type", rowResult(objectVar).toString, true)
        
        var optionalGroupForThisRow: String = noGroup
        if (rowResult(optionalGroup) != null) optionalGroupForThisRow = rowResult(optionalGroup).toString
        
        var required: Boolean = true
        val requiredForThisRow = rowResult(requiredBool)
        if (requiredForThisRow.toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>" && useOptionals) required = false
        val tripleForThisRow = new Triple(rowResult(subject).toString, rowResult(predicate).toString, rowResult(objectVar).toString, required)
        if (groupMap.contains(graphForThisRow))
        {
            groupMap(graphForThisRow).addTripleToGroupList(optionalGroupForThisRow, tripleForThisRow)
        }
        else 
        {
            groupMap += graphForThisRow -> new TriplesGroupList(optionalGroupForThisRow, ArrayBuffer(tripleForThisRow))
        }
        if (subjectTypeTriple != null) groupMap(graphForThisRow).groupList(optionalGroupForThisRow) += subjectTypeTriple
        if (objectTypeTriple != null) groupMap(graphForThisRow).groupList(optionalGroupForThisRow) += objectTypeTriple
    }
}

class TriplesGroupList
{
    def this(optionalGroup: String, triplesList: ArrayBuffer[Triple])
    {
        this
        groupList += optionalGroup -> triplesList
    }
    
    def addTripleToGroupList(optionalGroup: String, newTriple: Triple)
    {
        if (!groupList.contains(optionalGroup)) groupList += optionalGroup -> ArrayBuffer(newTriple)
        else
        {
            var tripleExists = false
            for (thisTriple <- groupList(optionalGroup)) 
            {
                if (thisTriple.getSubject == newTriple.getSubject
                    && thisTriple.getPredicate == newTriple.getPredicate
                    && thisTriple.getObject == newTriple.getObject) tripleExists = true
            }
            if (!tripleExists) groupList(optionalGroup) += newTriple
        }
    }
    
    var groupList: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]
}