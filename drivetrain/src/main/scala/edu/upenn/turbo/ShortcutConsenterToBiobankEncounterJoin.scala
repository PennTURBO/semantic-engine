package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

object ShortcutConsenterToBiobankEncounterJoin extends ShortcutGraphObjectSingleton
{
    baseVariableName = "shortcutConsenterToBiobankEncounterJoin"
    
    val consenterName = Consenter.baseVariableName
    val encounterName = BiobankEncounter.baseVariableName
  
    pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    variablesToSelect = Array(consenterName, encounterName)
    
    expansionRules = Array(
        
             ExpansionOfIntermediateNode.create(ConsenterToBiobankEncounterJoin.baseVariableName, RandomUUID)  
             
    )
}