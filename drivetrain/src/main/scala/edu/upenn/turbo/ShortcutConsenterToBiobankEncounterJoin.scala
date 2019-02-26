package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

object ShortcutConsenterToBiobankEncounterJoin extends ShortcutGraphObject
{
    baseVariableName = "shortcutConsenterToBiobankEncounterJoin"
    
    val consenterName = Consenter.baseVariableName
    val encounterName = BiobankEncounter.baseVariableName
  
    pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    namedGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_entityLinkingShortcuts"
    
    variablesToSelect = Array(consenterName, encounterName)
    
    expansionRules = Array(
        
             ExpansionOfIntermediateNode.create(ConsenterToBiobankEncounterJoin.baseVariableName, RandomUUID)  
             
    )
}