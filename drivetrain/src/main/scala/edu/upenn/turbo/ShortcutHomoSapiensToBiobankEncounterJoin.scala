package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHomoSapiensToBiobankEncounterJoin extends ShortcutGraphObjectInstance
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
    
    val pattern = ShortcutHomoSapiensToBiobankEncounterJoin.pattern
    val baseVariableName = ShortcutHomoSapiensToBiobankEncounterJoin.baseVariableName
    val typeURI = ShortcutHomoSapiensToBiobankEncounterJoin.typeURI
    val expansionRules = ShortcutHomoSapiensToBiobankEncounterJoin.expansionRules
    val variablesToSelect = ShortcutHomoSapiensToBiobankEncounterJoin.variablesToSelect
    
    val whereTypesForExpansion = ShortcutHomoSapiensToBiobankEncounterJoin.whereTypesForExpansion
    val insertTypesForExpansion: Array[GraphObjectSingleton] = ShortcutHomoSapiensToBiobankEncounterJoin.insertTypesForExpansion
    val optionalWhereTypesForExpansion = ShortcutHomoSapiensToBiobankEncounterJoin.optionalWhereTypesForExpansion
}

object ShortcutHomoSapiensToBiobankEncounterJoin extends ShortcutGraphObjectSingleton
{    
    def create(instantiation: String, namedGraph: String, globalUUID: String, optional: Boolean = false): ShortcutHomoSapiensToBiobankEncounterJoin =
    {
        new ShortcutHomoSapiensToBiobankEncounterJoin(instantiation, namedGraph, globalUUID, optional)
    }
    
    val typeURI = ""
    
    val baseVariableName = "shortcutHomoSapiensToBiobankEncounterJoin"
    
    val homoSapiensName = HomoSapiens.baseVariableName
    val encounterName = BiobankEncounter.baseVariableName
  
    val pattern = s"""
      
      ?$homoSapiensName graphBuilder:linksToEncounter ?$encounterName .
      
    """
    
    val variablesToSelect = Array(homoSapiensName, encounterName)
    
    val expansionRules: Array[ExpansionRule] = Array(
        
             ExpansionFromShortcutValue.create(HomoSapiensToBiobankEncounterJoin.baseVariableName, 
                                               homoSapiensName,
                                               SingletonMD5OfOriginal),
             
             ExpansionOfIntermediateNode.create(HomoSapiensToBiobankEncounterJoin.homoSapiensHeight, 
                                               SingletonMD5OfOriginalWithDependent, 
                                               BiobankEncounterHeight.baseVariableName,
                                               homoSapiensName),
                                               
             ExpansionOfIntermediateNode.create(HomoSapiensToBiobankEncounterJoin.homoSapiensWeight, 
                                                SingletonMD5OfOriginalWithDependent, 
                                                BiobankEncounterWeight.baseVariableName,
                                                homoSapiensName),
                                                
             ExpansionOfIntermediateNode.create(HomoSapiensToBiobankEncounterJoin.homoSapiensAdipose, 
                                               SingletonMD5OfOriginalWithDependent, 
                                               BiobankEncounterBMI.baseVariableName,
                                               homoSapiensName)
    )
    
    val whereTypesForExpansion: Array[GraphObjectSingleton] = Array(HomoSapiens, HomoSapiensIdentifier, BiobankEncounter, BiobankEncounterIdentifier, this)
    val optionalWhereTypesForExpansion: Array[GraphObjectSingleton] = Array(BiobankEncounterBMI, BiobankEncounterHeight, BiobankEncounterWeight, 
                                                                                     BiobankEncounterDate)
    val insertTypesForExpansion: Array[GraphObjectSingleton] = Array(HomoSapiensToBiobankEncounterJoin)
}