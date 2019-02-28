package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenterToHealthcareEncounterJoin extends ShortcutGraphObjectInstance
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
    
    val pattern = ShortcutConsenterToHealthcareEncounterJoin.pattern
    val baseVariableName = ShortcutConsenterToHealthcareEncounterJoin.baseVariableName
    val typeURI = ShortcutConsenterToHealthcareEncounterJoin.typeURI
    val expansionRules = ShortcutConsenterToHealthcareEncounterJoin.expansionRules
    val variablesToSelect = ShortcutConsenterToHealthcareEncounterJoin.variablesToSelect
}

object ShortcutConsenterToHealthcareEncounterJoin extends ShortcutGraphObjectSingletonWithCreate
{    
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutConsenterToHealthcareEncounterJoin =
    {
        new ShortcutConsenterToHealthcareEncounterJoin(instantiation, namedGraph, optional)
    }
    
    val baseVariableName = "shortcutConsenterToHealthcareEncounterJoin"
    
    val consenterName = Consenter.baseVariableName
    val encounterName = HealthcareEncounter.baseVariableName
  
    val pattern = s"""
      
      ?$consenterName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    val variablesToSelect = Array(consenterName, encounterName)
    
    val typeURI = "http://graphBuilder.org/linksToEncounter"
    
    val expansionRules: Array[ExpansionRule] = Array(
        
             ExpansionOfIntermediateNode.create(ConsenterToHealthcareEncounterJoin.baseVariableName, RandomUUID)  
             
    )
}