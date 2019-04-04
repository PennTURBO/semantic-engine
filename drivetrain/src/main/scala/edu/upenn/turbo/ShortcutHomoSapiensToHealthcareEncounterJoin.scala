package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHomoSapiensToHealthcareEncounterJoin extends ShortcutGraphObjectInstance
{
    def this(instantiation: String, namedGraph: String, globalUUID: String, optional: Boolean)
    {
        this()
        this.instantiation = instantiation
        this.namedGraph = namedGraph
        this.optional = optional
        this.globalUUID = globalUUID
    }
  
    var instantiation: String = null
    var namedGraph: String = null
    var optional: Boolean = false
    var globalUUID: String = null
    
    val pattern = ShortcutHomoSapiensToHealthcareEncounterJoin.pattern
    val baseVariableName = ShortcutHomoSapiensToHealthcareEncounterJoin.baseVariableName
    val typeURI = ShortcutHomoSapiensToHealthcareEncounterJoin.typeURI
    val expansionRules = ShortcutHomoSapiensToHealthcareEncounterJoin.expansionRules
    val variablesToSelect = ShortcutHomoSapiensToHealthcareEncounterJoin.variablesToSelect
    
    val whereTypesForExpansion = ShortcutHomoSapiensToHealthcareEncounterJoin.whereTypesForExpansion
    val insertTypesForExpansion: Array[GraphObjectSingleton] = ShortcutHomoSapiensToHealthcareEncounterJoin.insertTypesForExpansion
    val optionalWhereTypesForExpansion = ShortcutHomoSapiensToHealthcareEncounterJoin.optionalWhereTypesForExpansion
    
}

object ShortcutHomoSapiensToHealthcareEncounterJoin extends ShortcutGraphObjectSingleton
{    
    def create(instantiation: String, namedGraph: String, globalUUID: String, optional: Boolean = false): ShortcutHomoSapiensToHealthcareEncounterJoin =
    {
        new ShortcutHomoSapiensToHealthcareEncounterJoin(instantiation, namedGraph, globalUUID, optional)
    }
    
    val typeURI = ""
    
    val baseVariableName = "shortcutHomoSapiensToHealthcareEncounterJoin"
    
    val homoSapiensName = HomoSapiens.baseVariableName
    val encounterName = HealthcareEncounter.baseVariableName
  
    val pattern = s"""
      
      ?$homoSapiensName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    val variablesToSelect = Array(homoSapiensName, encounterName)
    
    val expansionRules: Array[ExpansionRule] = Array(
        
             ExpansionFromShortcutValue.create(HomoSapiensToHealthcareEncounterJoin.baseVariableName, 
                                               homoSapiensName,
                                               SingletonMD5OfOriginal),  
             
             ExpansionOfIntermediateNode.create(HomoSapiensToHealthcareEncounterJoin.homoSapiensHeight, 
                                               SingletonMD5OfOriginalWithDependent, 
                                               HealthcareEncounterHeight.baseVariableName,
                                               homoSapiensName),
                                               
             ExpansionOfIntermediateNode.create(HomoSapiensToHealthcareEncounterJoin.homoSapiensWeight, 
                                                SingletonMD5OfOriginalWithDependent, 
                                                HealthcareEncounterWeight.baseVariableName,
                                                homoSapiensName),
                                                
             ExpansionOfIntermediateNode.create(HomoSapiensToHealthcareEncounterJoin.homoSapiensAdipose, 
                                                SingletonMD5OfOriginalWithDependent, 
                                                HealthcareEncounterBMI.baseVariableName,
                                                homoSapiensName)
    )
    
    val whereTypesForExpansion: Array[GraphObjectSingleton] = Array(this)
    val optionalWhereTypesForExpansion: Array[GraphObjectSingleton] = Array()
    val insertTypesForExpansion: Array[GraphObjectSingleton] = Array(HomoSapiens, HomoSapiensIdentifier, HealthcareEncounter, HealthcareEncounterIdentifier, 
                                                                     HealthcareEncounterBMI, HealthcareEncounterHeight, HealthcareEncounterWeight, 
                                                                     HealthcareEncounterDate, HomoSapiensToHealthcareEncounterJoin)
}