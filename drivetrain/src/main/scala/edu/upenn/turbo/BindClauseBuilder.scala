package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class BindClauseBuilder extends ProjectwideGlobals
{
    var clause = ""
    
    var localUUID: String = ""
    // holds the final set of bind rules to be added to the query, one rule per entry
    var bindRules = new HashSet[String]
    // map of output nodes to input nodes that are that ensure correct cardinality enforcement
    var cardinalityMap = new HashMap[String, String]
    var process: String = ""
    var singletonElements = new HashSet[GraphPatternElement]
    var superSingletonElements = new HashSet[GraphPatternElement]
    var standardElementsToBind = new HashSet[GraphPatternElement]
    var boundInBindClause = new HashSet[GraphPatternElement]
    
    /*
     * Builds the bind clause and returns a list of variables that have been bound
     */
    def buildBindClause(process: String, localUUID: String, inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe]): HashSet[GraphPatternElement] =
    {   
        this.process = process
        this.localUUID = localUUID
        
        buildCardinalityMap(inputs, outputs)
        
        val boundSingletonVariables = buildSingletonBindClauses()
        val boundBaseGroupVariables = buildBaseGroupBindClauses()
        
        for (a <- bindRules) clause += a
        
        boundInBindClause
    }
    
    def buildCardinalityMap(inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe])
    {
        // first check if there are only 1-1 connections in the input, if so you can choose any element to use as the enforcer
        var inputHasLevelChange = false
        for (recipe <- inputs)
        {
            if (recipe.isInstanceOf[InstToInstConnRecipe] && (recipe.cardinality == oneToManyMultiplicity || recipe.cardinality == manyToOneMultiplicity))
            {
                inputHasLevelChange = true
            }
        }
        for (recipe <- outputs)
        {
            if (!recipe.subject.existsInInput.get) 
            {
                if (recipe.subject.isInstanceOf[Instance])
                {
                    if (recipe.subject.asInstanceOf[Instance].isSuperSingleton.get) superSingletonElements += recipe.subject
                    else if (recipe.subject.asInstanceOf[Instance].isSingleton.get) singletonElements += recipe.subject
                    else 
                    {
                        standardElementsToBind += recipe.subject
                        if (recipe.subject.createdWithRule == None || (recipe.subject.createdWithRule != None && recipe.subject.createdWithRule.get.contains("multiplicityEnforcer")))
                        {
                            assignCardinalityToInstance(recipe.subject.asInstanceOf[Instance], inputs, outputs, inputHasLevelChange)
                        }
                    } 
                }
                else if (recipe.subject.isInstanceOf[Term] && recipe.subject.createdWithRule != None)
                {
                    standardElementsToBind += recipe.subject
                }
            }
            if (!recipe.crObject.existsInInput.get)
            {
                if (recipe.crObject.isInstanceOf[Instance])
                {
                    if (recipe.crObject.asInstanceOf[Instance].isSuperSingleton.get) superSingletonElements += recipe.crObject
                    else if (recipe.crObject.asInstanceOf[Instance].isSingleton.get) singletonElements += recipe.crObject
                    else 
                    {
                        standardElementsToBind += recipe.crObject  
                        if (recipe.crObject.createdWithRule == None || (recipe.crObject.createdWithRule != None && recipe.crObject.createdWithRule.get.contains("multiplicityEnforcer")))
                        {
                            assignCardinalityToInstance(recipe.crObject.asInstanceOf[Instance], inputs, outputs, inputHasLevelChange)                            
                        }
                    } 
                }
                else if (recipe.crObject.isInstanceOf[Term] && recipe.crObject.createdWithRule != None)
                {
                    standardElementsToBind += recipe.crObject
                }
            }
        }
    }
    
    def assignCardinalityToInstance(instance: Instance, inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe], inputHasLevelChange: Boolean)
    {
        if (!cardinalityMap.contains(instance.value))
        {
            var defaultMethodSuccess = false
            var directMethodSuccess = false
            if (!inputHasLevelChange) defaultMethodSuccess = findEnforcerWithDefaultMethod(instance, inputs)
            if (!defaultMethodSuccess) directMethodSuccess = findEnforcerWithDirectCardinalityMethod(instance)
            if (!defaultMethodSuccess && !directMethodSuccess) 
            {
               val unassignedElement = instance.value
               throw new RuntimeException(s"Could not assign cardinality enforcer for element $unassignedElement")
            }
        }
    }
    
    def findEnforcerWithDefaultMethod(instance: Instance, inputs: HashSet[ConnectionRecipe]): Boolean =
    {
        var foundEnforcer: Boolean = false
        val inputsIterator = inputs.toBuffer.sortWith(_.name < _.name).toIterator
        var enforcer: String = ""
        while (enforcer == "" && inputsIterator.hasNext)
        {
            val nextInput = inputsIterator.next
            if (!nextInput.isOptional.get && nextInput.optionalGroup == None)
            {
                if (nextInput.subject.isInstanceOf[Instance]) enforcer = nextInput.subject.value
                else if (nextInput.crObject.isInstanceOf[Instance]) enforcer = nextInput.crObject.value
            }
            if (enforcer != "") 
            {
                cardinalityMap += instance.value -> enforcer
                foundEnforcer = true
            }
        }
        foundEnforcer
    }

    def findEnforcerWithDirectCardinalityMethod(instance: Instance): Boolean =
    {
        var foundEnforcer: Boolean = false
        var enforcer: String = ""
        // sorting by connection recipe ensures that we will always choose the same enforcer, if there are multiple that are valid
        val connectionIterator = instance.oneToOneConnections.toBuffer.sortWith(_.value < _.value).toIterator
        while (enforcer == "" && connectionIterator.hasNext)
        {
            val connectedElement = connectionIterator.next
            if (connectedElement.existsInInput.get && connectedElement.isInstanceOf[Instance])
            {
                var connectionRequired = false
                for (recipe <- connectedElement.referencedByRecipes)
                {
                    if (!recipe.isOptional.get && recipe.optionalGroup == None) connectionRequired = true
                }
                if (connectionRequired) enforcer = connectedElement.value
            }
        }
        if (enforcer != "") 
        {
            cardinalityMap += instance.value -> enforcer
            foundEnforcer = true
        }
        foundEnforcer
    }
    
    def buildSingletonBindClauses()
    {
        for (singleton <- singletonElements)
        {
            if (!boundInBindClause.contains(singleton))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton.value)
                if (singleton.createdWithRule == None)
                {
                    bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\",\"${process}")))) AS ${singletonAsVar})\n""" 
                }
                else addCustomBindRule(singleton)
                boundInBindClause += singleton 
            }
        }
        for (superSingleton <- superSingletonElements)
        {
            if (!boundInBindClause.contains(superSingleton))
            {
                val superSingletonAsVar = helper.convertTypeToSparqlVariable(superSingleton.value)
                if (superSingleton.createdWithRule == None)
                {
                    bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${superSingletonAsVar}\",\"${localUUID}\")))) AS ${superSingletonAsVar})\n"""
                }
                else addCustomBindRule(superSingleton)
                boundInBindClause += superSingleton  
            }
        }
    }
    
    def buildBaseGroupBindClauses()
    {
        for (element <- standardElementsToBind)
        {
            if (!boundInBindClause.contains(element))
            {
                if (element.createdWithRule == None)
                {
                    if (element.dependentOn != None) addDependentBindRule(element)
                    else addStandardBindRule(element)    
                }
                else addCustomBindRule(element)
            }
        } 
    }
    
    def addCustomBindRule(element: GraphPatternElement)
    {
        val elementName = element.value
        val assigneeAsVar = helper.convertTypeToSparqlVariable(elementName)
        var thisCustomRule = helper.removeQuotesFromString(element.createdWithRule.get.split("\\^")(0))+"\n"
        if (thisCustomRule.contains("dependent"))
        {
            assert(element.dependentOn != None, s"Element $elementName has no dependent, but dependent is requested in custom rule $thisCustomRule")
            thisCustomRule = thisCustomRule.replaceAll("\\$\\{dependent\\}", helper.convertTypeToSparqlVariable(element.dependentOn.get.value))
        }
        if (thisCustomRule.contains("multiplicityEnforcer"))
        {
            assert(cardinalityMap.contains(elementName), s"Element $elementName has no cardinality enforcer, but an enforcer is requested in custom rule $thisCustomRule")
            thisCustomRule = thisCustomRule.replaceAll("\\$\\{multiplicityEnforcer\\}", helper.convertTypeToSparqlVariable(cardinalityMap(elementName)))
        }
        thisCustomRule = thisCustomRule.replaceAll("\\$\\{replacement\\}", assigneeAsVar)
        thisCustomRule = thisCustomRule.replaceAll("\\$\\{localUUID\\}", localUUID)
        thisCustomRule = thisCustomRule.replaceAll("\\$\\{defaultPrefix\\}", defaultPrefix)
        // these assertions may not be valid if a user decides to create a prefix or term with one of these words in it
        assert (!thisCustomRule.contains("replacement"), s"No replacement for custom rule was identified, but custom rule requires a replacement. Rule string: $thisCustomRule")
        assert (!thisCustomRule.contains("dependent"), s"No dependent for custom rule was identified, but custom rule requires a dependent. Rule string: $thisCustomRule")
        assert (!thisCustomRule.contains("localUUID"))
        assert (!thisCustomRule.contains("multiplicityEnforcer"), s"No cardinality enforcer for custom rule was identified, but custom rule requires an enforcer. Rule string: $thisCustomRule")
        assert (!thisCustomRule.contains("defaultPrefix"))
        bindRules += thisCustomRule
    }
    
    def addStandardBindRule(element: GraphPatternElement)
    {   
        var newNodeAsVar = helper.convertTypeToSparqlVariable(element.value)
        assert(cardinalityMap.contains(element.value), s"No cardinality enforcer found for new element $newNodeAsVar")
        var multiplicityEnforcerAsVar = helper.convertTypeToSparqlVariable(cardinalityMap(element.value))
        bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))) AS ${newNodeAsVar})\n"""
    }
    
    def addDependentBindRule(element: GraphPatternElement)
    {
        var newNodeAsVar = helper.convertTypeToSparqlVariable(element.value)
        assert(cardinalityMap.contains(element.value), s"No cardinality enforcer found for new element $newNodeAsVar")
        var multiplicityEnforcerAsVar = helper.convertTypeToSparqlVariable(cardinalityMap(element.value))
        var dependeeAsVar = helper.convertTypeToSparqlVariable(element.dependentOn.get.value)
        bindRules += s"""BIND(IF (BOUND(${dependeeAsVar}), uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))), ?unbound) AS ${newNodeAsVar})\n"""   
    }
}