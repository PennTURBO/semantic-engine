package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

class TriplesGroup extends ProjectwideGlobals
{
    var inputGroupMap: HashMap[String, TriplesGroupList] = new HashMap[String, TriplesGroupList]
    var outputTriplesMap: HashMap[String, ArrayBuffer[Triple]] = new HashMap[String, ArrayBuffer[Triple]]

    def addTripleFromModelGraphOutputRowResult(rowResult: HashMap[String, Value], process: String, varsForProcessInput: ArrayBuffer[String])
    {
        for (key <- requiredOutputKeysList) assert (rowResult.contains(key))
        assert (!rowResult.contains(optionalGroup))
        
        val processGraph = "http://www.itmat.upenn.edu/biobank/processes"
        var objectIsLiteral = false
        if (rowResult(connectionRecipeType).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") objectIsLiteral = true
        
        val outputGraphForThisTriple = rowResult(graphFromSparql).toString
        
        val tripleForThisRow = new Triple(rowResult(subject).toString, rowResult(predicate).toString, rowResult(objectVar).toString)
        addTripleToOutputList(tripleForThisRow, outputGraphForThisTriple)
        val processSubjectTriple = new Triple(process, "turbo:createdTriplesAbout", rowResult(subject).toString)
        addTripleToOutputList(processSubjectTriple, processGraph)
        if (!objectIsLiteral && rowResult(predicate).toString != "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && rowResult(predicate).toString != "rdf:type")
        {
            val processObjectTriple = new Triple(process, "turbo:createdTriplesAbout", rowResult(objectVar).toString)
            addTripleToOutputList(processObjectTriple, processGraph)
        }
        
        if (rowResult(subjectType) != null)
        {
            val subjectTypeTriple = new Triple(rowResult(subject).toString, "rdf:type", rowResult(subject).toString, true)
            addTripleToOutputList(subjectTypeTriple, outputGraphForThisTriple)
        }
        if (rowResult(objectType) != null)
        {
            val objectTypeTriple = new Triple(rowResult(objectVar).toString, "rdf:type", rowResult(objectVar).toString, true)
            addTripleToOutputList(objectTypeTriple, outputGraphForThisTriple)
        }
        
        for (uri <- varsForProcessInput)
        {
            val processInputTriple = new Triple(process, "obo:OBI_0000293", uri)
            addTripleToOutputList(processInputTriple, processGraph)
        }
    }
    
    def addTripleToOutputList(triple: Triple, graph: String)
    {
        var tripleExists = false
        if (!outputTriplesMap.contains(graph)) outputTriplesMap += graph -> ArrayBuffer(triple)
        else
        {
            for (thisTriple <- outputTriplesMap(graph)) 
            {
                if (thisTriple.getSubject == triple.getSubject
                    && thisTriple.getPredicate == triple.getPredicate
                    && thisTriple.getObject == triple.getObject) tripleExists = true
            }
            if (!tripleExists) outputTriplesMap(graph) += triple 
        }
    }
    
    def addTripleFromModelGraphInputRowResult(rowResult: HashMap[String, Value])
    {
        for (key <- requiredInputKeysList) assert (rowResult.contains(key))
        
        var graphForThisRow = defaultGraph
        if (rowResult(graphOfCreatingProcess) != null) graphForThisRow = rowResult(graphOfCreatingProcess).toString
        
        var optionalGroupForThisRow: String = noGroup
        if (rowResult(optionalGroup) != null) optionalGroupForThisRow = rowResult(optionalGroup).toString
        
        var required: Boolean = true
        if (rowResult(requiredBool).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
        val tripleForThisRow = new Triple(rowResult(subject).toString, rowResult(predicate).toString, rowResult(objectVar).toString, required)
        if (inputGroupMap.contains(graphForThisRow))
        {
            inputGroupMap(graphForThisRow).addTripleToGroupList(optionalGroupForThisRow, tripleForThisRow)
        }
        else 
        {
            inputGroupMap += graphForThisRow -> new TriplesGroupList(optionalGroupForThisRow, ArrayBuffer(tripleForThisRow))
        }
        if (rowResult(subjectType) != null) 
        {
            val subjectTypeTriple = new Triple(rowResult(subject).toString, "rdf:type", rowResult(subject).toString, true)
            inputGroupMap(graphForThisRow).addTripleToGroupList(optionalGroupForThisRow, subjectTypeTriple)
        }
        if (rowResult(objectType) != null) 
        {
            val objectTypeTriple = new Triple(rowResult(objectVar).toString, "rdf:type", rowResult(objectVar).toString, true)
            inputGroupMap(graphForThisRow).addTripleToGroupList(optionalGroupForThisRow, objectTypeTriple)
        }
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