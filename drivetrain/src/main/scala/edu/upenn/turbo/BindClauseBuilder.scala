package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class BindClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    // holds the final set of bind rules to be added to the query, one rule per entry
    var bindRules = new HashSet[String]
    // map of output nodes to input nodes that are that ensure correct cardinality enforcement
    var multiplicityMap = new HashMap[String, String]
    var nodesToCreate = new HashSet[String]
    
    var boundFromInput: HashSet[String] = null
    var process: String = ""
    
    /*
     * Builds the bind clause and returns a list of variables that have been bound
     */
    def buildBindClause(process: String, mapConnectionLists: HashMap[String, HashMap[String, HashMap[String, String]]], setConnectionLists: HashMap[String, HashSet[String]], localUUID: String,
                         customRulesList: HashMap[String, org.eclipse.rdf4j.model.Value], dependenciesList: HashMap[String, org.eclipse.rdf4j.model.Value],
                         nodesToCreate: HashSet[String], inputHasLevelChange: Boolean, inputInstanceCountMap: HashMap[String, Integer], 
                         outputInstanceCountMap: HashMap[String, Integer], literalList: HashSet[String], boundInWhereClause: HashSet[String],
                         optionalList: HashSet[String], classResourceLists: HashSet[String]): HashSet[String] =
    {   
        this.process = process
        boundFromInput = boundInWhereClause
        this.nodesToCreate = nodesToCreate
        
        buildMultiplicityMap(inputHasLevelChange, mapConnectionLists, literalList, inputInstanceCountMap, outputInstanceCountMap, optionalList, classResourceLists)
        
        val boundSingletonVariables = buildSingletonBindClauses(localUUID, setConnectionLists, customRulesList)
        val boundBaseGroupVariables = buildBaseGroupBindClauses(localUUID, customRulesList, dependenciesList, literalList, mapConnectionLists("inputManyToOneList"))
        
        for (a <- bindRules) clause += a
        for (a <- boundSingletonVariables) boundFromInput += a
        for (a <- boundBaseGroupVariables) boundFromInput += a
        boundFromInput
    }
    
    def buildMultiplicityMap(inputHasLevelChange: Boolean, mapConnectionLists: HashMap[String, HashMap[String, HashMap[String, String]]],
                            literalList: HashSet[String], inputInstanceCountMap: HashMap[String, Integer], outputInstanceCountMap: HashMap[String, Integer],
                            optionalList: HashSet[String], classResourceLists: HashSet[String])
    {
        /*for ((k,v) <- outputInstanceCountMap) println("Output Instance: " + k + " Count: " + v)
        println()
        for ((k,v) <- inputInstanceCountMap) println("Input Instance: " + k + " Count: " + v)*/
        // everything in this map needs to be assigned a multiplicity in multiplicityMap, unless it is bound from input
        val outputOneToOneConnections = mapConnectionLists("outputOneToOneList")
        val inputOneToOneConnections = mapConnectionLists("inputOneToOneList")
        val inputManyToOneConnections = mapConnectionLists("inputManyToOneList")
        val elementsForCardinalityCountSearch = new HashSet[String]
        for (newElement <- nodesToCreate)
        {
            // first check if there are only 1-1 connections in the input, if so you can choose any element to use as the enforcer
            if (!inputHasLevelChange) findEnforcerWithDefaultCardinalityMethod(newElement, inputOneToOneConnections, literalList, optionalList, classResourceLists)
            else
            {
                // then check if there are any direct 1-1 connections with an input element
                if (!multiplicityMap.contains(newElement)) findEnforcerWithDirectCardinalityMethod(newElement, literalList, outputOneToOneConnections, inputOneToOneConnections, inputManyToOneConnections, optionalList, classResourceLists)
                // make a list for later of output elements that haven't been assigned a cardinality enforcer yet
                if (!multiplicityMap.contains(newElement)) elementsForCardinalityCountSearch += newElement 
            }
        }
        val elementsForLiteralEnforcerSearch = new HashSet[String]
        // if no match found, use the instance count maps to try to infer a cardinality relationship
        for (newElement <- elementsForCardinalityCountSearch)
        {
            findEnforcerWithInstanceCountCardinalityMethod(newElement, literalList, inputInstanceCountMap, outputInstanceCountMap, outputOneToOneConnections, inputOneToOneConnections, optionalList, classResourceLists)
            if (!multiplicityMap.contains(newElement)) elementsForLiteralEnforcerSearch += newElement 
        }
    }
    
    def findEnforcerWithDefaultCardinalityMethod(newElement: String, inputOneToOneConnections: HashMap[String, HashMap[String, String]], literalList: HashSet[String], optionalList: HashSet[String], classResourceLists: HashSet[String])
    {
        var defaultEnforcer: String = null
        val loop = new Breaks
        loop.breakable
        {
            for ((element, connectionList) <- inputOneToOneConnections)
            {
                if (!literalList.contains(element) && !optionalList.contains(element) && !classResourceLists.contains(element))
                {
                    defaultEnforcer = element
                    loop.break
                }
            }
        }
        if (defaultEnforcer != null)
        {
            logger.info(s"The following cardinality enforcement has been assigned via the default method: $defaultEnforcer enforces cardinality on $newElement")
            multiplicityMap += newElement -> defaultEnforcer
        }
    }
    
    def findEnforcerWithDirectCardinalityMethod(newElement: String, literalList: HashSet[String], outputOneToOneConnections: HashMap[String, HashMap[String, String]], inputOneToOneConnections: HashMap[String, HashMap[String, String]], inputManyToOneConnections: HashMap[String, HashMap[String, String]], optionalList: HashSet[String], classResourceLists: HashSet[String])
    {
        val sameCardinalityOutputConnections = outputOneToOneConnections.getOrElse(newElement, null)
        val literalCandidates = new HashSet[String]
        val optionalCandidates = new HashSet[String]
        if (sameCardinalityOutputConnections != null)
        {
            for ((element,cxnName) <- sameCardinalityOutputConnections)
            {
                if (boundFromInput.contains(element)) 
                {
                    if (literalList.contains(element)) literalCandidates += element
                    if (optionalList.contains(element)) optionalCandidates += element
                    if (!literalCandidates.contains(element) && !optionalCandidates.contains(element) && !classResourceLists.contains(element))
                    {
                        if (!multiplicityMap.contains(newElement))
                        {
                            multiplicityMap += newElement -> element
                            logger.info(s"The following cardinality enforcement has been assigned via the direct cardinality method: $element enforces cardinality on $newElement")
                        }
                    }
                }
            } 
            // if no required, non-literal element was found, search among optional and literal connections for one
            if (!multiplicityMap.contains(newElement)) assignCardinalityEnforcerFromAssociatedElement(newElement, inputOneToOneConnections, optionalCandidates, literalList, optionalList, classResourceLists)
            if (!multiplicityMap.contains(newElement)) assignCardinalityEnforcerFromAssociatedElement(newElement, inputOneToOneConnections, literalCandidates, literalList, optionalList, classResourceLists)
            // if we still haven't found anything, we can use an optional candidate if it exists
            if (!multiplicityMap.contains(newElement))
            {
                for (candidate <- optionalCandidates)
                {
                    if (!literalCandidates.contains(candidate) && !classResourceLists.contains(candidate)) 
                    {
                        if (!multiplicityMap.contains(newElement))
                        {
                            multiplicityMap += newElement -> candidate
                            logger.info(s"The following cardinality enforcement has been assigned via the optional direct cardinality method: $candidate enforces cardinality on $newElement")
                        }
                    }
                }
            }
            // if we still haven't found anything, we can use a literal candidate if it exists
            /*if (!multiplicityMap.contains(newElement))
            {
                for (candidate <- literalCandidates)
                {
                    if (!multiplicityMap.contains(newElement))
                    {
                        multiplicityMap += newElement -> candidate
                        logger.info(s"The following cardinality enforcement has been assigned via the literal direct cardinality method: $candidate enforces cardinality on $newElement")
                    }
                }
            }*/
        } 
    }
    
    def assignCardinalityEnforcerFromAssociatedElement(newElement: String, inputOneToOneConnections: HashMap[String, HashMap[String, String]], listToCheck: HashSet[String], literalList: HashSet[String], optionalList: HashSet[String], classResourceLists: HashSet[String])
    {
        for (candidate <- listToCheck)
        {
            if(inputOneToOneConnections.contains(candidate))
            {
                for ((element, name) <- inputOneToOneConnections(candidate))
                {
                    if (!optionalList.contains(element) && !literalList.contains(element) && !classResourceLists.contains(element))
                    {
                        if (!multiplicityMap.contains(newElement))
                        {
                            multiplicityMap += newElement -> element
                            logger.info(s"The following cardinality enforcement has been assigned via the non-candidate bridge method: $element enforces cardinality on $newElement")   
                        }
                    }
                }
            }
        }
    }
    
    def findEnforcerWithInstanceCountCardinalityMethod(newElement: String, literalList: HashSet[String], inputInstanceCountMap: HashMap[String, Integer], outputInstanceCountMap: HashMap[String, Integer], outputOneToOneConnections: HashMap[String, HashMap[String, String]], inputOneToOneConnections: HashMap[String, HashMap[String, String]], optionalList: HashSet[String], classResourceLists: HashSet[String])
    {
        // look for any output element that has a 1-1 cardinality relationship with an input element, to establish a baseline
        for ((outputElement, connectionList) <- outputOneToOneConnections)
        {
            for ((connectedElement, connectionName) <- connectionList)
            {
                if (boundFromInput.contains(connectedElement))
                {
                    val inputInstanceCount = inputInstanceCountMap(connectedElement)
                    val outputInstanceCount = outputInstanceCountMap(outputElement)
                    val newElementInstanceCount = outputInstanceCountMap(newElement)
                    
                    for ((inputElement, inputElementInstanceCount) <- inputInstanceCountMap)
                    {
                        // avoid literals as cardinality enforcers (for now)
                        if (!literalList.contains(inputElement))
                        {
                            // if the difference in instance counts is the same, it means the elements are on the same "cardinality plane"
                            if (inputInstanceCount - outputInstanceCount == inputElementInstanceCount - newElementInstanceCount)
                            {
                                // if we previously find an enforcer for this element, make sure the new one is the same or exists on the same cardinality plane
                                if (multiplicityMap.contains(newElement))
                                {
                                    val currentEnforcer = multiplicityMap(newElement)
                                    if (currentEnforcer != inputElement && !inputOneToOneConnections(inputElement).contains(currentEnforcer)) throw new RuntimeException(s"Multiple possible cardinality enforcers found for $newElement: $currentEnforcer and $inputElement both appear qualified but exist on different cardinality planes")
                                }
                                else if (!classResourceLists.contains(inputElement))
                                {
                                    multiplicityMap += newElement -> inputElement
                                    logger.info(s"The following cardinality enforcement has been assigned via the cardinality level change method: $inputElement enforces cardinality on $newElement")
                                }
                            } 
                        }
                    }
                }
            }
        }
    }
    
    def buildSingletonBindClauses(localUUID: String, setConnectionLists: HashMap[String, HashSet[String]], customRulesList: HashMap[String, org.eclipse.rdf4j.model.Value]): HashSet[String] =
    {
        val outputSingletonClasses = setConnectionLists("outputSingletonList")
        val outputSuperSingletonClasses = setConnectionLists("outputSuperSingletonList")
        
        val boundVariables = new HashSet[String]
        for (singleton <- outputSingletonClasses)
        {
            if (!(boundFromInput.contains(singleton)) && !customRulesList.contains(singleton))
            {
                boundVariables += singleton
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\",\"${process}")))) AS ${singletonAsVar})\n""" 
            }
        }
        for (singleton <- outputSuperSingletonClasses)
        {
            if (!(boundFromInput.contains(singleton)) && !customRulesList.contains(singleton))
            {
                boundVariables += singleton
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\")))) AS ${singletonAsVar})\n"""
            }
        }
        boundVariables
    }
    
    def buildBaseGroupBindClauses(localUUID: String, customRulesList: HashMap[String, org.eclipse.rdf4j.model.Value], dependenciesList: HashMap[String, org.eclipse.rdf4j.model.Value], literalList: HashSet[String], inputManyToOneConnections: HashMap[String, HashMap[String, String]]): HashSet[String] =
    {
        val boundVariables = new HashSet[String]
        for ((assignee, rule) <- customRulesList)
        {
            assert (!literalList.contains(assignee), s"Literal $assignee has not been bound in input")
            
            val assigneeAsVar = helper.convertTypeToSparqlVariable(assignee)
            var dependent: Option[String] = None : Option[String]
            if (dependenciesList.contains(assignee)) dependent = Some(dependenciesList(assignee).toString)
            var customRule = helper.removeQuotesFromString(rule.toString.split("\\^")(0))+"\n"
            if (dependent != None && customRule.contains("dependent"))
            {
                val dependee = helper.convertTypeToSparqlVariable(dependent.get, true)
                customRule = customRule.replaceAll("\\$\\{dependent\\}", dependee)
            }
            var enforcer: String = null
            if (customRule.contains("multiplicityEnforcer"))
            {
                assert(multiplicityMap.contains(assignee), s"A cardinality enforcer is required by the custom rule for $assignee, but no enforcer was found.")
                assert(!literalList.contains(multiplicityMap(assignee)), s"Literals cannot be used as enforcers for custom rules: $customRule")
                enforcer = multiplicityMap(assignee)
                enforcer = helper.convertTypeToSparqlVariable(enforcer)
                customRule = customRule.replaceAll("\\$\\{multiplicityEnforcer\\}", enforcer)
            }
            addCustomBindRule(customRule, assigneeAsVar, localUUID)
            // remove custom rule assignee from multiplicity map so we don't create two rules for the same thing
            multiplicityMap.remove(assignee)
            nodesToCreate.remove(assignee)
            boundVariables += assignee
        }
        for ((assignee, enforcer) <- multiplicityMap)
        {
            assert (!literalList.contains(assignee), s"Literal $assignee has not been bound in input")
            val assigneeAsVar = helper.convertTypeToSparqlVariable(assignee)
            /*if (literalList.contains(enforcer))
            {
                val secondaryEnforcer
                if (dependenciesList.contains(assignee)) 
                {
                   val dependee = helper.convertTypeToSparqlVariable(dependenciesList(assignee), true)
                   addDependentBindRuleWithLiteral(assigneeAsVar, localUUID, enforcer, dependee)
                }
                else addStandardBindRuleWithLiteral(assigneeAsVar, localUUID, enforcer)  
            }
            else
            {*/
                if (dependenciesList.contains(assignee)) 
                {
                   val dependee = helper.convertTypeToSparqlVariable(dependenciesList(assignee), true)
                   addDependentBindRule(assigneeAsVar, localUUID, enforcer, dependee)
                }
                else addStandardBindRule(assigneeAsVar, localUUID, enforcer)     
            //}
            boundVariables += assignee
            nodesToCreate.remove(assignee)
        }
        var nonCreatedElements = ""
        for (node <- nodesToCreate) nonCreatedElements += node + " "
        if (nonCreatedElements != "") throw new RuntimeException(s"The following elements to be created could not be assigned cardinality enforcers: $nonCreatedElements")
        boundVariables
    }
    
    def addCustomBindRule(customRule: String, assigneeAsVar: String, localUUID: String)
    {
        var thisCustomRule = customRule
        thisCustomRule = thisCustomRule.replaceAll("\\$\\{replacement\\}", assigneeAsVar)
        thisCustomRule = thisCustomRule.replaceAll("\\$\\{localUUID\\}", localUUID)
        thisCustomRule = thisCustomRule.replaceAll("\\$\\{defaultPrefix\\}", defaultPrefix)
        // these assertions may not be valid if a user decides to create a prefix or term with one of these words in it
        assert (!thisCustomRule.contains("replacement"), s"No replacement for custom rule was identified, but custom rule requires a replacement. Rule string: $customRule")
        assert (!thisCustomRule.contains("dependent"), s"No dependent for custom rule was identified, but custom rule requires a dependent. Rule string: $customRule")
        assert (!thisCustomRule.contains("localUUID"))
        assert (!thisCustomRule.contains("multiplicityEnforcer"), s"No cardinality enforcer for custom rule was identified, but custom rule requires an enforcer. Rule string: $customRule")
        assert (!thisCustomRule.contains("defaultPrefix"))
        bindRules += thisCustomRule
    }
    
    def addStandardBindRule(newNode: String, localUUID: String, multiplicityEnforcer: String)
    {   
        if (newNode != "")
        {
            var newNodeAsVar = newNode
            var multiplicityEnforcerAsVar = multiplicityEnforcer
            if (!newNode.contains('?'))
            {
                newNodeAsVar = helper.convertTypeToSparqlVariable(newNode)
            }
            if (!multiplicityEnforcer.contains('?'))
            {
                multiplicityEnforcerAsVar = helper.convertTypeToSparqlVariable(multiplicityEnforcer)
            }
    
            bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))) AS ${newNodeAsVar})\n"""
        }
    }
    
    def addStandardBindRuleWithLiteral(newNode: String, localUUID: String, literalEnforcer: String, connectedInstanceNode: String)
    {   
        if (newNode != "")
        {
            var newNodeAsVar = newNode
            var literalEnforcerAsVar = literalEnforcer
            var instanceAsVar = connectedInstanceNode
            if (!newNode.contains('?'))
            {
                newNodeAsVar = helper.convertTypeToSparqlVariable(newNode)
            }
            if (!literalEnforcer.contains('?'))
            {
                literalEnforcerAsVar = helper.convertTypeToSparqlVariable(literalEnforcer)
            }
            if (!connectedInstanceNode.contains('?'))
            {
                instanceAsVar = helper.convertTypeToSparqlVariable(connectedInstanceNode)
            }
    
            bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${literalEnforcerAsVar}), str(${instanceAsVar}))))) AS ${newNodeAsVar})\n"""
        }
    }
    
    def addDependentBindRuleWithLiteral(newNode: String, localUUID: String, literalEnforcer: String, connectedInstanceNode: String, dependee: String)
    {   
        if (newNode != "")
        {
            var newNodeAsVar = newNode
            var literalEnforcerAsVar = literalEnforcer
            var instanceAsVar = connectedInstanceNode
            var dependeeAsVar = dependee
            if (!newNode.contains('?'))
            {
                newNodeAsVar = helper.convertTypeToSparqlVariable(newNode)
            }
            if (!literalEnforcer.contains('?'))
            {
                literalEnforcerAsVar = helper.convertTypeToSparqlVariable(literalEnforcer)
            }
            if (!connectedInstanceNode.contains('?'))
            {
                instanceAsVar = helper.convertTypeToSparqlVariable(connectedInstanceNode)
            }
            if (!dependee.contains('?'))
            {
                dependeeAsVar = helper.convertTypeToSparqlVariable(dependee)
            }
    
            bindRules += s"""BIND(IF (BOUND(${dependeeAsVar}), uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${literalEnforcerAsVar}), str(${instanceAsVar}))))), ?unbound) AS ${newNodeAsVar})\n"""   
        }
    }
    
    def addDependentBindRule(newNode: String, localUUID: String, multiplicityEnforcer: String, dependee: String)
    {
        if (newNode != "")
        {
            var newNodeAsVar = newNode
            var multiplicityEnforcerAsVar = multiplicityEnforcer
            var dependeeAsVar = dependee
            if (!newNode.contains('?'))
            {
                newNodeAsVar = helper.convertTypeToSparqlVariable(newNode)
            }
            if (!multiplicityEnforcer.contains('?'))
            {
                multiplicityEnforcerAsVar = helper.convertTypeToSparqlVariable(multiplicityEnforcer)
            }
            if (!dependee.contains('?'))
            {
                dependeeAsVar = helper.convertTypeToSparqlVariable(dependee)
            }
            
            bindRules += s"""BIND(IF (BOUND(${dependeeAsVar}), uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))), ?unbound) AS ${newNodeAsVar})\n"""   
        }
    }
}