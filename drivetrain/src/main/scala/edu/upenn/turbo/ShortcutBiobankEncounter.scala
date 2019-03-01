package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutBiobankEncounter extends ShortcutGraphObjectInstance
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
    
    val pattern = ShortcutBiobankEncounter.pattern
    val baseVariableName = ShortcutBiobankEncounter.baseVariableName
    val typeURI = ShortcutBiobankEncounter.typeURI
    val expansionRules = ShortcutBiobankEncounter.expansionRules
    val variablesToSelect = ShortcutBiobankEncounter.variablesToSelect
}

object ShortcutBiobankEncounter extends ShortcutGraphObjectSingletonWithCreate
{    
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutBiobankEncounter =
    {
        new ShortcutBiobankEncounter(instantiation, namedGraph, optional)
    }
    
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
    
    val consenterRegistry = "shortcutConsenterRegistryStringForBiobankEncounter"
    val consenterSymbol = "shortcutConsenterSymbolValueForBiobankEncounter"
    val consenterURI = "shortcutConsenterURI"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000527 ;
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
            ?$baseVariableName turbo:TURBO_0010012 ?$consenterRegistry .
            ?$baseVariableName turbo:TURBO_0010010 ?$consenterSymbol .
            ?$baseVariableName turbo:ScBbEnc2UnexpandedConsenter ?$consenterURI .
          }
      """
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
    
    val expansionRules = Array(
        
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.instantiationKey, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(BiobankEncounter.shortcutName, baseVariableName, URIToString),
        ExpansionFromShortcutValue.create(ConsenterToBiobankEncounterJoin.consenterName, consenterURI, MD5GlobalRandomWithOriginal),
        ExpansionFromShortcutValue.create(BiobankEncounterBMI.bmiValue, bmiValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterHeight.heightValue, heightValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterWeight.weightValue, weightValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterDate.dateOfBiobankEncounterStringValue, dateOfBiobankEncounterStringValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterDate.dateOfBiobankEncounterDateValue, dateOfBiobankEncounterDateValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.datasetTitle, datasetTitle, BindAs),
       
        ExpansionOfIntermediateNode.create(BiobankEncounter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterDate.encounterDate, BindIfBoundRandomUUID, dateOfBiobankEncounterStringValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterDate.encounterStart, BindIfBoundRandomUUID, dateOfBiobankEncounterStringValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.encounterRegistryDenoter, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.encounterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterBMI.baseVariableName, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterBMI.valueSpecification, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.baseVariableName, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.valueSpecification, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.datumKey, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.baseVariableName, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.valueSpecification, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.datumKey, BindIfBoundRandomUUID, weightValue)
    )
}
