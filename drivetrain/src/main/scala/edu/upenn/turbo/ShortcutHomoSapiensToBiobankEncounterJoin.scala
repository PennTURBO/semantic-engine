package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHomoSapiensToBiobankEncounterJoin extends ShortcutGraphObjectInstance
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
    
    val pattern = ShortcutHomoSapiensToBiobankEncounterJoin.pattern
    val baseVariableName = ShortcutHomoSapiensToBiobankEncounterJoin.baseVariableName
    val typeURI = ShortcutHomoSapiensToBiobankEncounterJoin.typeURI
    val expansionRules = ShortcutHomoSapiensToBiobankEncounterJoin.expansionRules
    val variablesToSelect = ShortcutHomoSapiensToBiobankEncounterJoin.variablesToSelect
}

object ShortcutHomoSapiensToBiobankEncounterJoin extends ShortcutGraphObjectSingletonWithCreate
{    
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutHomoSapiensToBiobankEncounterJoin =
    {
        new ShortcutHomoSapiensToBiobankEncounterJoin(instantiation, namedGraph, optional)
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
}