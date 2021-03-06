package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.Queue
import org.slf4j.LoggerFactory

object CardinalityCountBuilder
{
  
    val logger = LoggerFactory.getLogger(getClass)
    
    def getInstanceCounts(inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]]): HashMap[String, Integer] =
    {
        var instanceCountMap = new HashMap[String, Integer]
        val instancesWithOnlyTerms = new HashSet[String]
        val connectionsMap = new HashMap[String, HashSet[String]]
        for (input <- inputs)
        {
            var subjectString = input(Globals.SUBJECT.toString).toString
            var objectString = input(Globals.OBJECT.toString).toString
            if (input(Globals.SUBJECTCONTEXT.toString) != null) subjectString += "_"+Utilities.convertTypeToSparqlVariable(input(Globals.SUBJECTCONTEXT.toString).toString).substring(1)
            if (input(Globals.OBJECTCONTEXT.toString) != null) objectString += "_"+Utilities.convertTypeToSparqlVariable(input(Globals.OBJECTCONTEXT.toString).toString).substring(1)
            val subjectExistsInMap: Boolean = instanceCountMap.contains(subjectString)
            val objectExistsInMap: Boolean = instanceCountMap.contains(objectString)
            val multiplicity = input(Globals.MULTIPLICITY.toString).toString
            if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == Globals.instToInstRecipe || input(Globals.CONNECTIONRECIPETYPE.toString).toString == Globals.instToLiteralRecipe)
            {     
                if (!subjectExistsInMap && !objectExistsInMap)
                {
                    if (multiplicity == Globals.oneToOneMultiplicity)
                    {
                        instanceCountMap += subjectString -> 1
                        instanceCountMap += objectString -> 1
                    }
                    else if (multiplicity == Globals.oneToManyMultiplicity)
                    {
                        instanceCountMap += subjectString -> 1
                        instanceCountMap += objectString -> 2
                    }
                    else if (multiplicity == Globals.manyToOneMultiplicity)
                    {
                        instanceCountMap += subjectString -> 2
                        instanceCountMap += objectString -> 1
                    }
                }
                else if (!subjectExistsInMap && objectExistsInMap)
                {
                    if (multiplicity == Globals.oneToOneMultiplicity) instanceCountMap += subjectString -> instanceCountMap(objectString)
                    else if (multiplicity == Globals.oneToManyMultiplicity)
                    {
                        if (instanceCountMap(objectString) > 1) instanceCountMap += subjectString -> instanceCountMap(objectString)/2
                        else
                        {
                            instanceCountMap += subjectString -> 1
                            instanceCountMap(objectString) = 2
                            // recursively iterate through all connections of objectString, multiply by 2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, objectString, 2)
                        }
                    }
                    else if (multiplicity == Globals.manyToOneMultiplicity) instanceCountMap += subjectString -> instanceCountMap(objectString)*2
                }
                else if (subjectExistsInMap && !objectExistsInMap)
                {
                    if (multiplicity == Globals.oneToOneMultiplicity) instanceCountMap += objectString -> instanceCountMap(subjectString)
                    else if (multiplicity == Globals.manyToOneMultiplicity)
                    {
                        if (instanceCountMap(subjectString) > 1) instanceCountMap += objectString -> instanceCountMap(subjectString)/2
                        else
                        {
                            instanceCountMap += objectString -> 1
                            instanceCountMap(subjectString) = 2
                            // recursively iterate through all connections of subjectString, multiply by 2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, subjectString, 2)
                        }
                    }
                    else if (multiplicity == Globals.oneToManyMultiplicity) instanceCountMap += objectString -> instanceCountMap(subjectString)*2
                }
                else if (subjectExistsInMap && objectExistsInMap)
                {
                    if (multiplicity == Globals.oneToOneMultiplicity && (instanceCountMap(subjectString) != instanceCountMap(objectString)))
                    {
                        if (instanceCountMap(subjectString) > instanceCountMap(objectString)) 
                        {
                            instanceCountMap(objectString) = instanceCountMap(subjectString)
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, objectString, instanceCountMap(subjectString)/instanceCountMap(objectString))
                        }
                        else 
                        {
                            instanceCountMap(subjectString) = instanceCountMap(objectString)
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, subjectString, instanceCountMap(objectString)/instanceCountMap(subjectString))
                        }
                    }
                    else if (multiplicity == Globals.manyToOneMultiplicity)
                    {
                        if (instanceCountMap(subjectString) <= instanceCountMap(objectString))
                        {
                            val multiplier = instanceCountMap(objectString)*2/instanceCountMap(subjectString)
                            instanceCountMap(subjectString) = instanceCountMap(objectString)*2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, subjectString, multiplier)
                        }  
                    }
                    else if (multiplicity == Globals.oneToManyMultiplicity)
                    {
                        if (instanceCountMap(objectString) <= instanceCountMap(subjectString))
                        {
                            val multiplier = instanceCountMap(subjectString)*2/instanceCountMap(objectString)
                            instanceCountMap(objectString) = instanceCountMap(subjectString)*2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, objectString, multiplier)
                        }  
                    }
                }
                if (connectionsMap.contains(subjectString)) connectionsMap(subjectString) += objectString
                else connectionsMap += subjectString -> HashSet(objectString)
                if (connectionsMap.contains(objectString)) connectionsMap(objectString) += subjectString
                else connectionsMap += objectString -> HashSet(subjectString)
            }
            else if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe" && !subjectExistsInMap) instancesWithOnlyTerms += subjectString
            else if (input(Globals.CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe" && !objectExistsInMap) instancesWithOnlyTerms += objectString
        }
        for (instance <- instancesWithOnlyTerms)
        {
            if (!instanceCountMap.contains(instance)) instanceCountMap += instance -> 1
        }
        /*logger.info("printing instance count map")
        for ((k,v) <- instanceCountMap)
        {
            logger.info("Key: " + k + " Count: " + v)
        }*/
        instanceCountMap
    }
    
    def updateMultiplicityChangeThroughConnectionsList(instanceCountMap: HashMap[String, Integer], connectionsList: HashMap[String, HashSet[String]], start: String, multiplier: Integer): HashMap[String, Integer] =
    {
        val updateQueue = new Queue[String]
        val alreadyUpdated = HashSet(start)
        for (cxn <- connectionsList(start)) updateQueue += cxn
        while (!updateQueue.isEmpty)
        {
            val firstElement = updateQueue.dequeue
            // If check added on line below, because it is possible instanceCountMap may not contain an element in the connections list
            // This occurs with singletons that are added to the connections list but not processed by the instance counter
            if (instanceCountMap.contains(firstElement)) instanceCountMap(firstElement) = instanceCountMap(firstElement)*multiplier
            alreadyUpdated += firstElement
            for (cxn <- connectionsList(firstElement)) if (!alreadyUpdated.contains(cxn)) updateQueue += cxn
        }
        instanceCountMap
    }
}