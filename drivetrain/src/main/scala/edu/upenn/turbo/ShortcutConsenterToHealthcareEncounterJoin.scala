package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

object ShortcutConsenterToHealthcareEncounterJoin extends ShortcutGraphObject
{
    baseVariableName = "shortcutConsenterToHealthcareEncounterJoin"
    
    val consenterName = Consenter.baseVariableName
    val encounterName = HealthcareEncounter.baseVariableName
  
    pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    namedGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_entityLinkingShortcuts"
    
    variablesToSelect = Array(consenterName, encounterName)
    
    expansionRules = Array(
        
             ExpansionOfIntermediateNode.create(ConsenterToHealthcareEncounterJoin.baseVariableName, RandomUUID)  
             
    )
}