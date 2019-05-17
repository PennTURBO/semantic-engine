package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

abstract class TriplesGroup extends ProjectwideGlobals
{
    var clause: String = ""
}

class WhereClauseTriplesGroup extends TriplesGroup with ProjectwideGlobals
{
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, Value]])
    {
        for (rowResult <- inputs)
        {
            for (key <- requiredInputKeysList) assert (rowResult.contains(key))
        }
        
        

        if (rowResult(graphOfCreatingProcess) != null) graphForThisRow = rowResult(graphOfCreatingProcess).toString
        
        var optionalGroupForThisRow: String = noGroup
        if (rowResult(optionalGroup) != null) optionalGroupForThisRow = rowResult(optionalGroup).toString
        
        var subjectAType = false
        var objectAType = false
        
        if (rowResult(subjectType) != null) subjectAType = true
        if (rowResult(objectType) != null) objectAType = true
        
        var required: Boolean = true
        if (rowResult(requiredBool).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
        val tripleForThisRow = new Triple(rowResult(subject).toString, rowResult(predicate).toString, rowResult(objectVar).toString, subjectAType, objectAType, required)
        if (inputGroupMap.contains(graphForThisRow))
        {
            inputGroupMap(graphForThisRow).addTripleToGroupList(optionalGroupForThisRow, tripleForThisRow)
        }
        else 
        {
            inputGroupMap += graphForThisRow -> new TriplesGroupList(optionalGroupForThisRow, ArrayBuffer(tripleForThisRow))
        }
    }
}

class InsertClauseTriplesGroup extends TriplesGroup with ProjectwideGlobals
{
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, Value]], process: String, varsForProcessInput: ArrayBuffer[String])
    {
        var triplesList = new ArrayBuffer[Triple]
        for (rowResult <- outputs)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key))
            assert (!rowResult.contains(optionalGroup))
            
            val processGraph = helper.retrievePropertyFromFile("processNamedGraph")
            
            var subjectAType = false
            var objectAType = false
            if (rowResult(subjectType) != null) subjectAType = true
            if (rowResult(objectType) != null) objectAType = true
            
            var objectIsLiteral = false
            if (rowResult(connectionRecipeType).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") objectIsLiteral = true
            
            val graph = rowResult(graphFromSparql).toString
            triplesList += new Triple(rowResult(subject).toString, rowResult(predicate).toString, 
                                              rowResult(objectVar).toString, subjectAType, objectAType, graph)
            
            triplesList += new Triple(process, "turbo:createdTriplesAbout", rowResult(subject).toString, false, false, processGraph)
            if (!objectIsLiteral && rowResult(predicate).toString != "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && rowResult(predicate).toString != "rdf:type")
            {
                triplesList += new Triple(process, "turbo:createdTriplesAbout", rowResult(objectVar).toString, false, false, processGraph)
            }
            for (uri <- varsForProcessInput) triplesList += new Triple(process, "obo:OBI_0000293", uri, false, false, processGraph)
        }
        
        buildInsertClauseFromTriplesList(triplesList)
    }
    
    def buildInsertClauseFromTriplesList(triplesList: ArrayBuffer[Triple])
    {
        
    }
}