package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class InsertClauseBuilder(cxn: RepositoryConnection) extends SparqlClauseBuilder with ProjectwideGlobals
{
    this.gmCxn = cxn
    
    def addTripleFromRowResult(outputs: HashSet[ConnectionRecipe], process: String, varsForProcessInput: HashSet[Triple], usedVariables: HashSet[GraphPatternElement], outputGraph: String)
    {
        val triplesGroup = new TriplesGroupBuilder()
        
        for (recipe <- outputs)
        {
            helper.validateURI(processNamedGraph)
            
            var subjectAnInstance = false
            var objectAnInstance = false            
            var objectADescriber = false
            var subjectADescriber = false
            var objectALiteralValue = false
            var objectADefinedLiteral = false
            var subject: String = null
            var predicate: String = recipe.predicate
            var crObject: String = null
            var graphForThisRow: String = null
            
            if (recipe.isInstanceOf[InstToInstConnRecipe])
            {
                var typedRecipe = recipe.asInstanceOf[InstToInstConnRecipe]   
                subject = typedRecipe.subject.value
                crObject = typedRecipe.crObject.value
                subjectAnInstance = true
                objectAnInstance = true
            }
            if (recipe.isInstanceOf[InstToTermConnRecipe])
            {
                var typedRecipe = recipe.asInstanceOf[InstToTermConnRecipe]
                subject = typedRecipe.subject.value
                crObject = typedRecipe.crObject.value
                subjectAnInstance = true
                if (typedRecipe.crObject.asInstanceOf[Term].isResourceList.get) objectADescriber = true
            }
            if (recipe.isInstanceOf[InstToLitConnRecipe])
            {
                var typedRecipe = recipe.asInstanceOf[InstToLitConnRecipe]
                subject = typedRecipe.subject.value
                crObject = typedRecipe.crObject.value
                objectALiteralValue = true
                subjectAnInstance = true
                if (!typedRecipe.crObject.asInstanceOf[Literal].isResourceList.get) objectADefinedLiteral = true
            }
            if (recipe.isInstanceOf[TermToLitConnRecipe])
            {
                var typedRecipe = recipe.asInstanceOf[TermToLitConnRecipe]
                subject = typedRecipe.subject.value
                crObject = typedRecipe.crObject.value
                objectALiteralValue = true
                if (!typedRecipe.crObject.asInstanceOf[Literal].isResourceList.get) objectADefinedLiteral = true
                if (typedRecipe.subject.asInstanceOf[Term].isResourceList.get) subjectADescriber = true
            }
            if (recipe.isInstanceOf[TermToTermConnRecipe])
            {
                var typedRecipe = recipe.asInstanceOf[TermToTermConnRecipe]
                subject = typedRecipe.subject.value
                crObject = typedRecipe.crObject.value
                if (typedRecipe.crObject.asInstanceOf[Term].isResourceList.get) objectADescriber = true
                if (typedRecipe.subject.asInstanceOf[Term].isResourceList.get) subjectADescriber = true
            }
            if (recipe.isInstanceOf[TermToInstConnRecipe])
            {
                var typedRecipe = recipe.asInstanceOf[TermToInstConnRecipe]
                subject = typedRecipe.subject.value
                crObject = typedRecipe.crObject.value
                objectAnInstance = true
                if (typedRecipe.subject.asInstanceOf[Term].isResourceList.get) subjectADescriber = true
            }
            
            val newTriple = new Triple(subject, predicate, crObject, subjectAnInstance, objectAnInstance, subjectADescriber, objectADescriber, objectALiteralValue)
            var graph = helper.checkAndConvertPropertiesReferenceToNamedGraph(outputGraph)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            
            val subjectProcessTriple = new Triple(process, "turbo:TURBO_0010184", subject, false, false, false, (subjectAnInstance || subjectADescriber))
            triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            if (!objectALiteralValue && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:TURBO_0010184", crObject, false, false, false, (objectAnInstance || objectADescriber))
                triplesGroup.addRequiredTripleToRequiredGroup(objectProcessTriple, processNamedGraph)
            }
        }
        for (processInputTriple <- varsForProcessInput)
        {
            triplesGroup.addRequiredTripleToRequiredGroup(processInputTriple, processNamedGraph)
        }
        clause = triplesGroup.buildInsertClauseFromTriplesGroup()
    }
}