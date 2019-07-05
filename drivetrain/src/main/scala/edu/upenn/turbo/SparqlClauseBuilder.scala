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
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultInputGraph: String): HashSet[String] =
    {
        var varsForProcessInput = new HashSet[String]
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- inputs)
        {
            for (key <- requiredInputKeysList) assert (rowResult.contains(key.toString))
            
            var graphForThisRow: String = defaultInputGraph
            var optionalGroupForThisRow: String = null
            var minusGroupForThisRow: String = null
            var subjectAType = false
            var objectAType = false
            
            var required = true
            if (rowResult(INPUTTYPE.toString).toString == "http://transformunify.org/ontologies/optionalInputTo") required = false
            
            if (rowResult(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = rowResult(GRAPHOFCREATINGPROCESS.toString).toString
            if (rowResult(GRAPHOFORIGIN.toString) != null) graphForThisRow = rowResult(GRAPHOFORIGIN.toString).toString
            if (rowResult(OPTIONALGROUP.toString) != null) optionalGroupForThisRow = rowResult(OPTIONALGROUP.toString).toString
            if (rowResult(MINUSGROUP.toString) != null) minusGroupForThisRow = rowResult(MINUSGROUP.toString).toString
            if (rowResult(SUBJECTTYPE.toString) != null) 
            {
                subjectAType = true
                varsForProcessInput += rowResult(SUBJECT.toString).toString
            }
            if (rowResult(OBJECTTYPE.toString) != null) 
            {
                objectAType = true
                varsForProcessInput += rowResult(OBJECT.toString).toString
            }
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString,
                                                     subjectAType, objectAType)
            if (minusGroupForThisRow != null)
            {
                triplesGroup.addTripleToMinusGroup(newTriple, graphForThisRow, minusGroupForThisRow)
            }
            else if (required && optionalGroupForThisRow == null)
            {
                triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
            }
            else if (optionalGroupForThisRow != null)
            {
                triplesGroup.addToOptionalGroup(newTriple, graphForThisRow, optionalGroupForThisRow, required)
            }
            else
            {
                triplesGroup.addOptionalTripleToRequiredGroup(newTriple, graphForThisRow)
            }
        }
        clause = triplesGroup.buildWhereClauseFromTriplesGroup()
        varsForProcessInput
    }
}

class InsertClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], process: String, varsForProcessInput: HashSet[String], usedVariables: HashSet[String])
    {
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- outputs)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key.toString))
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(processNamedGraph)
            
            var subjectAType = false
            var objectAType = false
            if (rowResult(SUBJECTTYPE.toString) != null) subjectAType = true
            if (rowResult(OBJECTTYPE.toString) != null) objectAType = true
            var objectIsLiteral = false
            if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") objectIsLiteral = true
            val graph = rowResult(GRAPH.toString).toString
            
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                      subjectAType, objectAType)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            if (usedVariables.contains(rowResult(SUBJECT.toString).toString))
            {
                val subjectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(SUBJECT.toString).toString, false, false)
                triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            }
            if (!objectIsLiteral && usedVariables.contains(rowResult(OBJECT.toString).toString) && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(OBJECT.toString).toString, false, false)
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

class DeleteClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultRemovalsGraph: String)
    {
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- removals)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key.toString))
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(defaultRemovalsGraph)
        
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                  false, false)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, defaultRemovalsGraph)
            
            clause = triplesGroup.buildDeleteClauseFromTriplesGroup()
        }
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