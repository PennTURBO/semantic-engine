package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

abstract class TriplesGroup extends ProjectwideGlobals
{
    var clause: String = ""
}

class WhereClauseTriplesGroup extends TriplesGroup with ProjectwideGlobals
{
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, Value]], defaultInputGraph: String): ArrayBuffer[String] =
    {
        var varsForProcessInput = new ArrayBuffer[String]
        var triplesList = new HashMap[String, ArrayBuffer[Triple]]
        for (rowResult <- inputs)
        {
            for (key <- requiredInputKeysList) assert (rowResult.contains(key))
            
            var graphForThisRow: String = defaultInputGraph
            var optionalGroupForThisRow: String = null
            var subjectAType = false
            var objectAType = false
            var required: Boolean = true
            
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
            if (rowResult(requiredBool).toString == "\"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>") required = false
            
            val tripleForThisRow = new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, rowResult(sparqlObject).toString, 
                                                  subjectAType, objectAType, graphForThisRow, required)
            tripleForThisRow.setOptionalGroup(optionalGroupForThisRow)
            
            if (triplesList.contains(graphForThisRow)) triplesList(graphForThisRow) += tripleForThisRow
            else triplesList += graphForThisRow -> ArrayBuffer(tripleForThisRow)
        }
        buildWhereClauseFromTriplesList(triplesList)
        varsForProcessInput
    }
    
    def buildWhereClauseFromTriplesList(triplesMap: HashMap[String, ArrayBuffer[Triple]])
    {
        var triplesToWrite = new HashMap[String, HashSet[String]]
        
        for ((graph, triplesList) <- triplesMap)
        {
            var requiredStatements = new HashSet[String]
            var optionalGroups = new HashMap[String, ArrayBuffer[String]]
            var optionalStatements = new HashSet[ArrayBuffer[String]]
            
            for (triple <- triplesList)
            {
                val triplesAsStrings = triple.makeTripleWithVariables(true)
                for ((tripleString, typeList) <- triplesAsStrings) 
                {
                    if (triple.required && triple.optionalGroup == null)
                    {
                        requiredStatements += tripleString
                        for (typeTriple <- typeList) requiredStatements += typeTriple
                    }
                    else if (triple.optionalGroup != null) 
                    {
                        var tripleToAdd = tripleString
                        if (!triple.required) tripleToAdd = tripleString
                        if (optionalGroups.contains(triple.optionalGroup)) optionalGroups(triple.optionalGroup) += tripleString
                        else optionalGroups += triple.optionalGroup -> ArrayBuffer(tripleString)
                    }
                    else 
                    {
                        for (typeTriple <- typeList) optionalStatements += tripleString
                        optionalStatements += tripleString
                    }
                }
            }
            
            clause += s"GRAPH <$graph> {\n"
            for (statement <- requiredStatements) clause += statement
            for (statement <- optionalStatements)
            {
                if (!requiredStatements.contains(statement)) clause += s"OPTIONAL { \n $statement } \n"
            }
            for ((group, list) <- optionalGroups)
            {
                clause += "OPTIONAL {\n"
                for (statement <- list)
                {
                    if (!requiredStatements.contains(statement)) clause += statement
                }
                clause += "}\n"
            }
            clause += "}\n"
        }
    }
}

class InsertClauseTriplesGroup extends TriplesGroup with ProjectwideGlobals
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
            triplesList += new Triple(rowResult(sparqlSubject).toString, rowResult(sparqlPredicate).toString, 
                                              rowResult(sparqlObject).toString, subjectAType, objectAType, graph)
            
            triplesList += new Triple(process, "turbo:createdTriplesAbout", rowResult(sparqlSubject).toString, false, false, processNamedGraph)
            if (!objectIsLiteral && rowResult(sparqlPredicate).toString != "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" && rowResult(sparqlPredicate).toString != "rdf:type")
            {
                triplesList += new Triple(process, "turbo:createdTriplesAbout", rowResult(sparqlObject).toString, false, false, processNamedGraph)
            }
            for (uri <- varsForProcessInput) triplesList += new Triple(process, "obo:OBI_0000293", uri, false, false, processNamedGraph)
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
            val triplesAsStrings = triple.makeTripleWithVariablesIfPreexisting(usedVariables, true)
            for ((tripleString, typeTriples) <- triplesAsStrings) 
            {
                if (triplesToWrite.contains(triple.namedGraph))
                {
                    triplesToWrite(triple.namedGraph) += tripleString
                    for (typeTriple <- typeTriples) triplesToWrite(triple.namedGraph) += typeTriple
                }
                else triplesToWrite += triple.namedGraph -> HashSet(tripleString)
            }
        }
        for ((graph,tripleSet) <- triplesToWrite)
        {
            clause += s"GRAPH <$graph> {\n"
            for (triple <- tripleSet) clause += triple
            clause += "}\n"
        }
    }
}

class InsertDataClauseTriplesGroup extends TriplesGroup with ProjectwideGlobals
{
    def buildInsertDataClauseFromTriplesList(triplesList: ArrayBuffer[Triple])
    {
        var triplesToWrite = new HashMap[String, HashSet[String]]
        for (triple <- triplesList)
        {
            val tripleString = triple.makeTriple()
            if (triplesToWrite.contains(triple.namedGraph)) triplesToWrite(triple.namedGraph) += tripleString
            else triplesToWrite += triple.namedGraph -> HashSet(tripleString)
        }
        for ((graph,tripleSet) <- triplesToWrite)
        {
            clause += s"GRAPH <$graph> {\n"
            for (triple <- tripleSet) clause += triple
            clause += "}\n"
        }
    }
}