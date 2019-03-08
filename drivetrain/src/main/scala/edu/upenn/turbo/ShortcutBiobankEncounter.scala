package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutBiobankEncounter extends ShortcutGraphObjectInstance
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
    
    val pattern = ShortcutBiobankEncounter.pattern
    val baseVariableName = ShortcutBiobankEncounter.baseVariableName
    val typeURI = ShortcutBiobankEncounter.typeURI
    val expansionRules = ShortcutBiobankEncounter.expansionRules
    val variablesToSelect = ShortcutBiobankEncounter.variablesToSelect
    
    val whereTypesForExpansion = ShortcutBiobankEncounter.whereTypesForExpansion
    val insertTypesForExpansion: Array[GraphObjectSingleton] = ShortcutBiobankEncounter.insertTypesForExpansion
    val optionalWhereTypesForExpansion: Array[GraphObjectSingleton] = ShortcutBiobankEncounter.optionalWhereTypesForExpansion
}

object ShortcutBiobankEncounter extends ShortcutGraphObjectSingleton
{    
    def create(instantiation: String, namedGraph: String, globalUUID: String, optional: Boolean = false): ShortcutBiobankEncounter =
    {
        new ShortcutBiobankEncounter(instantiation, namedGraph, globalUUID, optional)
    }
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val instantiationKey = "instantiation"
    val baseVariableName = "shortcutBiobankEncounter"
    
    val valuesKey = "shortcutBiobankEncounterIdValue"
    val registryKey = "shortcutBiobankEncounterRegistryString"
    
    val datasetTitle = "shortcutBiobankEncounterDatasetTitle"
    
    val bmiValue = "shortcutBiobankEncounterBmiValue"
    val weightValue = "shortcutBiobankEncounterWeightValue"
    val heightValue = "shortcutBiobankEncounterHeightValue"
    
    val dateOfBiobankEncounterStringValue = "shortcutBiobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "shortcutBiobankEncounterDateDateValue"
    
    val homoSapiensRegistry = "shortcutHomoSapiensRegistryStringForBiobankEncounter"
    val homoSapiensSymbol = "shortcutHomoSapiensSymbolValueForBiobankEncounter"
    val homoSapiensURI = "shortcutHomoSapiensURI"
    
    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> ;
              turbo:TURBO_0000623   ?$datasetTitle;
              turbo:TURBO_0000628   ?$valuesKey ;
              turbo:TURBO_0000630   ?$registryKey .

          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000635 ?$bmiValue .
          }
          OPTIONAL
          {
          ?$baseVariableName turbo:TURBO_0000627 ?$weightValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000626 ?$heightValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000624 ?$dateOfBiobankEncounterStringValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000625 ?$dateOfBiobankEncounterDateValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0010012 ?$homoSapiensRegistry .
            ?$baseVariableName turbo:TURBO_0010010 ?$homoSapiensSymbol .
            ?$baseVariableName turbo:ScBbEnc2UnexpandedHomoSapiens ?$homoSapiensURI .
          }
      """
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
    
    val expansionRules = Array(
        
        ExpansionFromShortcutValue.create(BiobankEncounterRegistry.valuesKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.instantiationKey, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(BiobankEncounter.shortcutName, baseVariableName, URIToString),
        ExpansionFromShortcutValue.create(HomoSapiensToBiobankEncounterJoin.homoSapiensName, homoSapiensURI, MD5GlobalRandomWithOriginal),
        ExpansionFromShortcutValue.create(BiobankEncounterBMI.bmiValue, bmiValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterHeight.heightValue, heightValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterWeight.weightValue, weightValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterDate.dateOfBiobankEncounterStringValue, dateOfBiobankEncounterStringValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterDate.dateOfBiobankEncounterDateValue, dateOfBiobankEncounterDateValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterSymbol.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterDataset.datasetTitle, datasetTitle, BindAs),
       
        ExpansionOfIntermediateNode.create(BiobankEncounter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(BiobankEncounterDataset.baseVariableName, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterDate.encounterDate, BindIfBoundRandomUUID, dateOfBiobankEncounterStringValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterDate.encounterStart, BindIfBoundRandomUUID, dateOfBiobankEncounterStringValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterSymbol.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterRegistry.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterBMI.baseVariableName, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterBMI.valueSpecification, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.baseVariableName, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.valueSpecification, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.datumKey, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.baseVariableName, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.valueSpecification, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.datumKey, BindIfBoundRandomUUID, weightValue)
    )
    
    val whereTypesForExpansion: Array[GraphObjectSingleton] = Array(this)
    val insertTypesForExpansion: Array[GraphObjectSingleton] = Array(BiobankEncounter, BiobankEncounterBMI, 
                                                                     BiobankEncounterDate, BiobankEncounterHeight, 
                                                                     BiobankEncounterWeight, BiobankEncounterIdentifier,
                                                                     BiobankEncounterRegistry, BiobankEncounterSymbol,
                                                                     BiobankEncounterDataset, ShortcutHomoSapiensToBiobankEncounterJoin)
    val optionalWhereTypesForExpansion: Array[GraphObjectSingleton] = Array()
}
