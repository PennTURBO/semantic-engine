package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

object ShortcutConsenterToHealthcareEncounterJoin extends ShortcutGraphObjectSingleton
{
    baseVariableName = "shortcutConsenterToHealthcareEncounterJoin"
    
    val consenterName = Consenter.baseVariableName
    val encounterName = HealthcareEncounter.baseVariableName
  
    pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    variablesToSelect = Array(consenterName, encounterName)
    
    expansionRules = Array(
        
             ExpansionOfIntermediateNode.create(ConsenterToHealthcareEncounterJoin.baseVariableName, RandomUUID)  
             
    )
}