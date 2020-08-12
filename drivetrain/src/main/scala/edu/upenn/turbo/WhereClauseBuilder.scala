package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class WhereClauseBuilder(cxn: RepositoryConnection) extends SparqlClauseBuilder with ProjectwideGlobals
{
    this.gmCxn = cxn
    
    val triplesGroup = new TriplesGroupBuilder()
    var varsForProcessInput = new HashSet[Triple]
    var valuesBlock: HashMap[String, String] = new HashMap[String, String]
    var nonDefaultGraphRecipes = new HashSet[ConnectionRecipe]
    var process: String = ""
    
    def setProcess(process: String)
    {
        this.process = process
    }
    
    def addTripleFromRowResult(inputs: HashSet[ConnectionRecipe], defaultInputGraph: String, process: String): HashSet[Triple] =
    {
        assert (process != "")
        setProcess(process)
        for (recipe <- inputs)
        {            
            if (recipe.foundInGraph != None && recipe.foundInGraph != Some(defaultInputGraph)) nonDefaultGraphRecipes += recipe
            else makeNewTripleFromRowResult(recipe, defaultInputGraph)
        }
        for (recipe <- nonDefaultGraphRecipes) makeNewTripleFromRowResult(recipe, defaultInputGraph)
        
        triplesGroup.setValuesBlock(valuesBlock)
        clause = triplesGroup.buildWhereClauseFromTriplesGroup()
        varsForProcessInput
    }
    
    def makeNewTripleFromRowResult(recipe: ConnectionRecipe, defaultInputGraph: String)
    {  
        var subject: String = null
        var predicate: String = recipe.predicate
        var crObject: String = null
        var subjectAnInstance: Boolean = false
        var objectAnInstance: Boolean = false
        var subjectADescriber: Boolean = false
        var objectADescriber: Boolean = false
        var objectALiteralValue: Boolean = false
        var objectADefinedLiteral: Boolean = false
        var suffixOperator: String = null
        var graphForThisRow: String = defaultInputGraph
        var required: Boolean = true
        
        if (recipe.predicateSuffixOperator != None) suffixOperator = recipe.predicateSuffixOperator.get
        if (recipe.isOptional != None) required = !recipe.isOptional.get
        
        if (recipe.foundInGraph != None) graphForThisRow = recipe.foundInGraph.get
        
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
            if (typedRecipe.crObject.asInstanceOf[Term].isResourceList.get) 
            {
                objectADescriber = true
                if (typedRecipe.crObject.asInstanceOf[Term].ranges != None) valuesBlock += typedRecipe.crObject.value -> getRangesAsString(typedRecipe.crObject.value, typedRecipe.crObject.asInstanceOf[Term].ranges.get)
            }
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
            if (typedRecipe.subject.asInstanceOf[Term].isResourceList.get) 
            {
               subjectADescriber = true
               if (typedRecipe.subject.asInstanceOf[Term].ranges != None) valuesBlock += typedRecipe.subject.value -> getRangesAsString(typedRecipe.subject.value, typedRecipe.subject.asInstanceOf[Term].ranges.get)
            }
        }
        if (recipe.isInstanceOf[TermToTermConnRecipe])
        {
            var typedRecipe = recipe.asInstanceOf[TermToTermConnRecipe]
            subject = typedRecipe.subject.value
            crObject = typedRecipe.crObject.value
            if (typedRecipe.crObject.asInstanceOf[Term].isResourceList.get) 
            {
                objectADescriber = true
                if (typedRecipe.crObject.asInstanceOf[Term].ranges != None) valuesBlock += typedRecipe.crObject.value -> getRangesAsString(typedRecipe.crObject.value, typedRecipe.crObject.asInstanceOf[Term].ranges.get)
            }
            if (typedRecipe.subject.asInstanceOf[Term].isResourceList.get) 
            {
               subjectADescriber = true
               if (typedRecipe.subject.asInstanceOf[Term].ranges != None) valuesBlock += typedRecipe.subject.value -> getRangesAsString(typedRecipe.subject.value, typedRecipe.subject.asInstanceOf[Term].ranges.get)
            }
        }
        if (recipe.isInstanceOf[TermToInstConnRecipe])
        {
            var typedRecipe = recipe.asInstanceOf[TermToInstConnRecipe]
            subject = typedRecipe.subject.value
            crObject = typedRecipe.crObject.value
            objectAnInstance = true
            if (typedRecipe.subject.asInstanceOf[Term].isResourceList.get) 
            {
               subjectADescriber = true
               if (typedRecipe.subject.asInstanceOf[Term].ranges != None) valuesBlock += typedRecipe.subject.value -> getRangesAsString(typedRecipe.subject.value, typedRecipe.subject.asInstanceOf[Term].ranges.get)
            }
        }
        
        val newTriple = new Triple(subject, predicate, crObject, subjectAnInstance, objectAnInstance, subjectADescriber, objectADescriber, objectALiteralValue, suffixOperator)
        varsForProcessInput += new Triple(process, "obo:OBI_0000293", subject, false, false, false, (subjectADescriber || subjectAnInstance))
        if (!objectALiteralValue && !objectADefinedLiteral)
        {
            varsForProcessInput += new Triple(process, "obo:OBI_0000293", crObject, false, false, false, (objectADescriber || objectAnInstance))
        }
        graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(graphForThisRow)
        addNewTripleToGroup(newTriple, recipe.minusGroup, recipe.optionalGroup, required, graphForThisRow)
    }
    
    def addNewTripleToGroup(newTriple: Triple, minusGroupForThisRow: Option[String], optionalGroupForThisRow: Option[String], required: Boolean, graphForThisRow: String)
    {
        if (minusGroupForThisRow != None)
        {
            triplesGroup.addTripleToMinusGroup(newTriple, graphForThisRow, minusGroupForThisRow.get)
        }
        else if (required && optionalGroupForThisRow == null)
        {
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
        }
        else if (optionalGroupForThisRow != None)
        {
            triplesGroup.addToOptionalGroup(newTriple, graphForThisRow, optionalGroupForThisRow.get, required)
        }
        else
        {
            triplesGroup.addOptionalTripleToRequiredGroup(newTriple, graphForThisRow)
        }
    }
    
    def getRangesAsString(describer: String, rangeList: ArrayBuffer[String]): String =
    {
        val describerAsVar = helper.convertTypeToSparqlVariable(describer, true)
        var res = s"VALUES $describerAsVar {"
        for (item <- rangeList) res += "<" + item + ">"
        res + "}"
    }
}