package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class DeleteClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultRemovalsGraph: String)
    {   
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- removals)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key.toString))
            
            var subjectAnInstance = false
            var objectAnInstance = false
            
            var subjectContext: String = ""
            var objectContext: String = ""
            
            var objectADescriber = false
            var subjectADescriber = false
            
            var objectALiteral = false
     
            if (rowResult(OBJECTALITERALVALUE.toString).toString.contains("true")) objectALiteral = true
            val recipeType = rowResult(CONNECTIONRECIPETYPE.toString).toString
            if (recipeType == instToInstRecipe || recipeType == instToTermRecipe || recipeType == instToLiteralRecipe) subjectAnInstance = true
            if (recipeType == instToInstRecipe || recipeType == termToInstRecipe) objectAnInstance = true
            
            if (rowResult(SUBJECTCONTEXT.toString) != null) subjectContext = rowResult(SUBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTCONTEXT.toString) != null) objectContext = rowResult(OBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTADESCRIBER.toString) != null) objectADescriber = true
            if (rowResult(SUBJECTADESCRIBER.toString) != null) subjectADescriber = true
        
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(defaultRemovalsGraph)
        
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                  false, false, true, true, subjectContext, objectContext, objectALiteral)
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(defaultRemovalsGraph)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
            
            clause = triplesGroup.buildDeleteClauseFromTriplesGroup()
        }
    }
}