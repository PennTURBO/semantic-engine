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
    
    val optionalPattern = """"""
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_entityLinkingShortcuts"
    
    val typeURI = ""
    val variablesToSelect = Array(consenterName, encounterName)
    
    val connections = Map(
        "" -> ""
    )
    
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
    
    val variableExpansions = LinkedHashMap(
                                RandomUUID -> Array(expandedJoin.baseVariableName)
                             )
    
    val expandedVariableShortcutDependencies: Map[String, String] = Map()
    val expandedVariableShortcutBindings: Map[String, String] = Map()
    
    val appendToBind: String = ""
    val instantiation: String = ""
}