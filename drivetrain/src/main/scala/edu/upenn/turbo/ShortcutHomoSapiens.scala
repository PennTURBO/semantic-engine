package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHomoSapiens extends ShortcutGraphObjectInstance
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
    
    val pattern = ShortcutHomoSapiens.pattern
    val baseVariableName = ShortcutHomoSapiens.baseVariableName
    val typeURI = ShortcutHomoSapiens.typeURI
    val expansionRules = ShortcutHomoSapiens.expansionRules
    val variablesToSelect = ShortcutHomoSapiens.variablesToSelect
    
    val whereTypesForExpansion = ShortcutHomoSapiens.whereTypesForExpansion
    val insertTypesForExpansion: Array[GraphObjectSingleton] = ShortcutHomoSapiens.insertTypesForExpansion
    val optionalWhereTypesForExpansion = ShortcutHomoSapiens.optionalWhereTypesForExpansion
}

object ShortcutHomoSapiens extends ShortcutGraphObjectSingleton
{    
    def create(instantiation: String, namedGraph: String, globalUUID: String, optional: Boolean = false): ShortcutHomoSapiens =
    {
        new ShortcutHomoSapiens(instantiation, namedGraph, globalUUID, optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/NCBITaxon_9606"
    
    val instantiationKey = "instantiation"
    val baseVariableName = "shortcutPart"
    
    val shortcutName = "shortcutPart"
    val valuesKey = "homoSapiensSymbolValue"
    val registryKey = "homoSapiensRegistryString"
    
    val cridKey = "shortcutCrid"
    val datasetTitle = "shortcutHomoSapiensDatasetTitle"
    
    val dateOfBirthString = "dateOfBirthStringValue"
    val dateOfBirthDate = "dateOfBirthDateValue"
    
    val genderIdentityValue = "genderIdentityDatumValue"
    val genderIdentityType = "genderIdentityTypeString"
    
    val raceIdentityValue = "raceIdentityDatumValue"
    val raceIdentityType = "raceIdentityTypeString"
    
    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$cridKey a turbo:TURBO_0000503 .
          ?$cridKey obo:IAO_0000219 ?$baseVariableName .
          ?$cridKey turbo:TURBO_0003603 ?$datasetTitle .
          ?$cridKey turbo:TURBO_0003610 ?$registryKey .
          ?$cridKey turbo:TURBO_0003608 ?$valuesKey .

          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000604  ?$dateOfBirthString .
          }
          OPTIONAL
          {
           ?$baseVariableName turbo:TURBO_0000605   ?$dateOfBirthDate .
          }
          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000606  ?$genderIdentityValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000607   ?$genderIdentityType .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000614 ?$raceIdentityType .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000615 ?$raceIdentityValue .
          }
      """
    
    val variablesToSelect = Array(baseVariableName, registryKey, valuesKey)
    
    val expansionRules = Array(
        
        ExpansionFromShortcutValue.create(RaceIdentityDatum.raceIdentityValue, raceIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(DateOfBirthDatum.dateOfBirthString, dateOfBirthString, BindAs),
        ExpansionFromShortcutValue.create(DateOfBirthDatum.dateOfBirthDate, dateOfBirthDate, BindAs),
        ExpansionFromShortcutValue.create(HomoSapiensSymbol.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(HomoSapiensRegistry.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(GenderIdentityDatum.genderIdentityValue, genderIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(GenderIdentityDatum.genderIdentityValue, genderIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(RaceIdentityDatum.raceIdentityType, raceIdentityType, StringToURI),
        ExpansionFromShortcutValue.create(HomoSapiens.shortcutName, shortcutName, URIToString),
        ExpansionFromShortcutValue.create(HomoSapiensDataset.instantiation, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(HomoSapiensDataset.datasetTitle, datasetTitle, BindAs),
        
        ExpansionOfIntermediateNode.create(GenderIdentityDatum.biosex, BindIfBoundMD5LocalRandom, genderIdentityValue),
        ExpansionOfIntermediateNode.create(DateOfBirthDatum.birth, BindIfBoundMD5LocalRandom, dateOfBirthString),
        ExpansionOfIntermediateNode.create(HomoSapiens.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(HomoSapiensDataset.baseVariableName, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(HomoSapiensIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(HomoSapiensSymbol.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(HomoSapiensRegistry.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(GenderIdentityDatum.baseVariableName, BindIfBoundMD5LocalRandom, genderIdentityValue),
        ExpansionOfIntermediateNode.create(RaceIdentityDatum.baseVariableName, BindIfBoundMD5LocalRandom, raceIdentityType),
        ExpansionOfIntermediateNode.create(RaceIdentityDatum.raceIdentificationProcess, BindIfBoundMD5LocalRandom, raceIdentityType),
        ExpansionOfIntermediateNode.create(DateOfBirthDatum.baseVariableName, BindIfBoundMD5LocalRandom, dateOfBirthString),
        ExpansionOfIntermediateNode.create(GenderIdentityDatum.genderIdentityType, BiologicalSexIRI, genderIdentityType)
        )
        
    val whereTypesForExpansion: Array[GraphObjectSingleton] = Array(this)
    val insertTypesForExpansion: Array[GraphObjectSingleton] = Array(HomoSapiens, HomoSapiensIdentifier, GenderIdentityDatum, RaceIdentityDatum, DateOfBirthDatum,
                                                                     HomoSapiensSymbol, HomoSapiensRegistry, HomoSapiensDataset)
                                                                     val optionalWhereTypesForExpansion: Array[GraphObjectSingleton] = Array()
}