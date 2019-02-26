package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

object ShortcutBiobankEncounter extends ShortcutGraphObject
{    
    val instantiationKey = "instantiation"
    baseVariableName = "shortcutBiobankEncounter"
    
    val valuesKey = "shortcutBiobankEncounterIdValue"
    val registryKey = "shortcutBiobankEncounterRegistryString"
    
    val datasetTitle = "shortcutDatasetTitle"
    
    val bmiValue = "shortcutBiobankEncounterBmiValue"
    val weightValue = "shortcutBiobankEncounterWeightValue"
    val heightValue = "shortcutBiobankEncounterHeightValue"
    
    val dateOfBiobankEncounterStringValue = "shortcutBiobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "shortcutBiobankEncounterDateDateValue"
    
    val consenterRegistry = "shortcutConsenterRegistryStringForBiobankEncounter"
    val consenterSymbol = "shortcutConsenterSymbolValueForBiobankEncounter"
    val consenterURI = "shortcutConsenterURI"
    
    pattern = s"""
          
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
    
    typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
    
    expansionRules = Array(
        
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.instantiationKey, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(BiobankEncounter.shortcutName, baseVariableName, URIToString),
        ExpansionFromShortcutValue.create(ConsenterToBiobankEncounterJoin.consenterName, consenterURI, MD5GlobalRandomWithOriginal),
        ExpansionFromShortcutValue.create(BiobankEncounterBMI.bmiValue, bmiValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterHeight.heightValue, heightValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterWeight.weightValue, weightValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounter.dateOfBiobankEncounterStringValue, dateOfBiobankEncounterStringValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounter.dateOfBiobankEncounterDateValue, dateOfBiobankEncounterDateValue, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(BiobankEncounterIdentifier.datasetTitle, datasetTitle, BindAs),
       
        ExpansionOfIntermediateNode.create(BiobankEncounter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounter.encounterDate, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounter.encounterStart, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.encounterRegistryDenoter, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterIdentifier.encounterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(BiobankEncounterBMI.baseVariableName, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterBMI.valueSpecification, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.baseVariableName, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.valueSpecification, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterHeight.datumKey, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.baseVariableName, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.valueSpecification, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounterWeight.datumKey, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(BiobankEncounter.dataset, BindIfBoundDataset, dateOfBiobankEncounterStringValue)
        
    )
    
    namedGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_*"
}
