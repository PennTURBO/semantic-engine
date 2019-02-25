package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutParticipantToEncounterJoin(expandedJoin: ParticipantToEncounterJoin, consenter: Consenter, encounter: Encounter) extends ShortcutGraphObject
{
    val baseVariableName = "shortcutParticipantToEncounterJoin"
    
    val consenterName = consenter.baseVariableName
    val encounterName = encounter.baseVariableName
  
    val pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_entityLinkingShortcuts"
    
    val variablesToSelect = Array(consenterName, encounterName)
    
    override val expansionRules: Array[ExpansionRule] = Array(
        
             ExpansionOfIntermediateNode.create(expandedJoin.baseVariableName, RandomUUID)  
             
    )
}