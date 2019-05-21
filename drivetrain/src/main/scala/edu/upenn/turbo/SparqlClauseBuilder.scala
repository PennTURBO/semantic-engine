package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

abstract class SparqlClauseBuilder extends ProjectwideGlobals
{
    var clause: String = ""
}

class WhereClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, Value]], defaultInputGraph: String): HashSet[String] =
    {
        var varsForProcessInput = new HashSet[String]
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- inputs)
        {
            for (key <- requiredInputKeysList) assert (rowResult.contains(key))
            
            var graphForThisRow: String = defaultInputGraph
            var optionalGroupForThisRow: String = null
            var subjectAType = false
            var objectAType = false
            
            var required = true
            if (rowResult(requiredBool).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
            
            if (rowResult(graphOfCreatingProcess) != null) graphForThisRow = rowResult(graphOfCreatingProcess).toString
            if (rowResult(sparqlOptionalGroup) != null) optionalGroupForThisRow = rowResult(sparqlOptionalGroup).toString
            if (rowResult(subjectType) != null) 
            {
                subjectAType = true
                varsForProcessInput += rowResult(sparqlSubject).toString
            }
            if (rowResult(objectType) != null) 
            {
                objectAType = true
                varsForProcessInput += rowResult(sparqlObject).toString
            }
            if (required && optionalGroupForThisRow == null)
            {
                val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString,
                                                     subjectAType, objectAType)
                triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
            }
            else if (optionalGroupForThisRow != null)
            {
                val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString,
                                          subjectAType, objectAType)
                triplesGroup.addToOptionalGroup(newTriple, graphForThisRow, optionalGroupForThisRow, required)
            }
            else
            {
                val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString,
                                          subjectAType, objectAType)
                triplesGroup.addOptionalTripleToRequiredGroup(newTriple, graphForThisRow)
            }
        }
        clause = triplesGroup.buildWhereClauseFromTriplesGroup()
        varsForProcessInput
    }
}

class InsertClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, Value]], process: String, varsForProcessInput: HashSet[String], usedVariables: HashSet[String])
    {
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- outputs)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key))
            assert (!rowResult.contains(sparqlOptionalGroup))
            helper.validateURI(processNamedGraph)
            
            var subjectAType = false
            var objectAType = false
            if (rowResult(subjectType) != null) subjectAType = true
            if (rowResult(objectType) != null) objectAType = true
            var objectIsLiteral = false
            if (rowResult(connectionRecipeType).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") objectIsLiteral = true
            val graph = rowResult(graphFromSparql).toString
            
            val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString, 
                                      subjectAType, objectAType)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            val subjectProcessTriple = new Triple(process, "turbo:createdTriplesAbout", rowResult(sparqlSubject).toString, false, false)
            triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            if (!objectIsLiteral && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:createdTriplesAbout", rowResult(sparqlObject).toString, false, false)
                triplesGroup.addRequiredTripleToRequiredGroup(objectProcessTriple, processNamedGraph)
            }
        }
        for (uri <- varsForProcessInput)
        {
            val processInputTriple = new Triple(process, "obo:OBI_0000293", uri, false, false)
            triplesGroup.addRequiredTripleToRequiredGroup(processInputTriple, processNamedGraph)
        }
        clause = triplesGroup.buildInsertClauseFromTriplesGroup(usedVariables)
    }
}

class InsertDataClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def buildInsertDataClauseFromTriplesList(triplesList: ArrayBuffer[Triple], graph: String)
    {
        val triplesGroup = new TriplesGroupBuilder()
        for (triple <- triplesList) triplesGroup.addRequiredTripleToRequiredGroup(triple, graph)
        clause = triplesGroup.buildInsertDataClauseFromTriplesGroup()
    }
}