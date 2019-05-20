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
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, Value]], defaultInputGraph: String): ArrayBuffer[String] =
    {
        var varsForProcessInput = new ArrayBuffer[String]
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
                varsForProcessInput += helper.convertTypeToSparqlVariable(rowResult(sparqlSubject))
            }
            if (rowResult(objectType) != null) 
            {
                objectAType = true
                varsForProcessInput += helper.convertTypeToSparqlVariable(rowResult(sparqlObject))
            }
            if (required && optionalGroupForThisRow == null)
            {
                val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString)
                triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
            }
            else if (optionalGroupForThisRow != null)
            {
                val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString)
                triplesGroup.addToOptionalGroup(newTriple, graphForThisRow, optionalGroupForThisRow, required)
            }
            else
            {
                val newTriple = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString)
                triplesGroup.addOptionalTripleToRequiredGroup(newTriple, graphForThisRow)
            }
        }
        clause = triplesGroup.buildClauseFromTriplesGroup()
        varsForProcessInput
    }
}

/*class InsertClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    var usedVariables: HashSet[String] = null
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, Value]], process: String, varsForProcessInput: ArrayBuffer[String], usedVariables: HashSet[String])
    {
        this.usedVariables = usedVariables
        var triplesList = new ArrayBuffer[Triple]
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
            triplesList += new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString)
            triplesList += new Triple(process, "turbo:createdTriplesAbout", rowResult(sparqlSubject).toString)
            if (!objectIsLiteral && rowResult(sparqlPredicate).toString != "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && rowResult(sparqlPredicate).toString != "rdf:type")
            {
                triplesList += new Triple(process, "turbo:createdTriplesAbout", rowResult(sparqlObject).toString)
            }
            for (uri <- varsForProcessInput) triplesList += new Triple(process, "obo:OBI_0000293", uri)
        }
        
        buildInsertClauseFromTriplesList(triplesList)
    }
    
    def buildInsertClauseFromTriplesList(triplesList: ArrayBuffer[Triple])
    {
        assert (usedVariables != null)
        assert (usedVariables.size != 0)
        var triplesToWrite = new HashMap[String, HashSet[String]]
        for (triple <- triplesList)
        {
            val triplesAsStrings = triple.makeTripleWithVariablesIfPreexisting(usedVariables)

        }
        for ((graph,tripleSet) <- triplesToWrite)
        {
            clause += s"GRAPH <$graph> {\n"
            for (triple <- tripleSet) clause += triple
            clause += "}\n"
        }
    }
}

class InsertDataClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def buildInsertDataClauseFromTriplesList(triplesList: ArrayBuffer[Triple])
    {
        var triplesToWrite = new HashMap[String, HashSet[String]]
        for (triple <- triplesList)
        {
            val tripleString = triple.makeTriple()
        }
        for ((graph,tripleSet) <- triplesToWrite)
        {
            clause += s"GRAPH <$graph> {\n"
            for (triple <- tripleSet) clause += triple
            clause += "}\n"
        }
    }
}*/