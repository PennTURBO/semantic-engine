package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenterToBiobankEncounterJoin extends ShortcutGraphObjectInstance
{
    def this(instantiation: String, namedGraph: String, optional: Boolean)
    {
        this()
        this.instantiation = instantiation
        this.namedGraph = namedGraph
        this.optional = optional
    }
  
    var instantiation: String = null
    var namedGraph: String = null
    var optional: Boolean = false
    
    val pattern = ShortcutConsenterToBiobankEncounterJoin.pattern
    val baseVariableName = ShortcutConsenterToBiobankEncounterJoin.baseVariableName
    val typeURI = ShortcutConsenterToBiobankEncounterJoin.typeURI
    val expansionRules = ShortcutConsenterToBiobankEncounterJoin.expansionRules
    val variablesToSelect = ShortcutConsenterToBiobankEncounterJoin.variablesToSelect
}

object ShortcutConsenterToBiobankEncounterJoin extends ShortcutGraphObjectSingletonWithCreate
{    
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutConsenterToBiobankEncounterJoin =
    {
        new ShortcutConsenterToBiobankEncounterJoin(instantiation, namedGraph, optional)
    }
    
    val baseVariableName = "shortcutConsenterToBiobankEncounterJoin"
    
    val consenterName = Consenter.baseVariableName
    val encounterName = BiobankEncounter.baseVariableName
  
    val pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    val variablesToSelect = Array(consenterName, encounterName)
    
    val typeURI = "http://graphBuilder.org/linksToEncounter"
    
    val expansionRules: Array[ExpansionRule] = Array(
        
             ExpansionOfIntermediateNode.create(ConsenterToBiobankEncounterJoin.baseVariableName, RandomUUID),  
             
             ExpansionOfIntermediateNode.create(ConsenterToBiobankEncounterJoin.consenterHeight, 
                                               SingletonMD5OfOriginalWithDependent, 
                                               BiobankEncounterHeight.baseVariableName,
                                               consenterName),
                                               
             ExpansionOfIntermediateNode.create(ConsenterToBiobankEncounterJoin.consenterWeight, 
                                                SingletonMD5OfOriginalWithDependent, 
                                                BiobankEncounterWeight.baseVariableName,
                                                consenterName),
                                                
             ExpansionOfIntermediateNode.create(ConsenterToBiobankEncounterJoin.consenterAdipose, 
                                               SingletonMD5OfOriginalWithDependent, 
                                               BiobankEncounterBMI.baseVariableName,
                                               consenterName)
    )
}