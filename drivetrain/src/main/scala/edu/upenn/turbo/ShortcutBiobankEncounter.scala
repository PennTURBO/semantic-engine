package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutBiobankEncounter(newInstantiation: String, newNamedGraph: String, 
                              biobankEncounter: BiobankEncounter, join: ShortcutParticipantToEncounterJoin) 
                              extends ShortcutGraphObject
{
    val biobankEncounterIdentifier = biobankEncounter.mandatoryLinks("Identifier").asInstanceOf[BiobankEncounterIdentifier]
    val BMI = biobankEncounter.optionalLinks("BMI").asInstanceOf[BMI]
    val height = biobankEncounter.optionalLinks("Height").asInstanceOf[Height]
    val weight = biobankEncounter.optionalLinks("Weight").asInstanceOf[Weight]

    override val instantiation = newInstantiation
    
    val instantiationKey = "instantiation"
    val baseVariableName = "shortcutBiobankEncounter"
    
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

    val namedGraph = newNamedGraph
    
    override val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
    
    override val expansionRules = Array(
        
        ExpansionFromShortcutValue.create(biobankEncounterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(biobankEncounterIdentifier.instantiationKey, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(biobankEncounter.shortcutName, baseVariableName, URIToString),
        ExpansionFromShortcutValue.create(join.consenterName, consenterURI, MD5GlobalRandomWithOriginal),
        ExpansionFromShortcutValue.create(BMI.bmiValue, bmiValue, BindAs),
        ExpansionFromShortcutValue.create(height.heightValue, heightValue, BindAs),
        ExpansionFromShortcutValue.create(weight.weightValue, weightValue, BindAs),
        ExpansionFromShortcutValue.create(biobankEncounter.dateOfBiobankEncounterStringValue, dateOfBiobankEncounterStringValue, BindAs),
        ExpansionFromShortcutValue.create(biobankEncounter.dateOfBiobankEncounterDateValue, dateOfBiobankEncounterDateValue, BindAs),
        ExpansionFromShortcutValue.create(biobankEncounterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(biobankEncounterIdentifier.datasetTitle, datasetTitle, BindAs),
       
        ExpansionOfIntermediateNode.create(biobankEncounter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(biobankEncounterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(biobankEncounterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(biobankEncounter.encounterDate, RandomUUID),
        ExpansionOfIntermediateNode.create(biobankEncounter.encounterStart, RandomUUID),
        ExpansionOfIntermediateNode.create(biobankEncounterIdentifier.encounterRegistryDenoter, RandomUUID),
        ExpansionOfIntermediateNode.create(biobankEncounterIdentifier.encounterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(BMI.baseVariableName, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(BMI.valueSpecification, BindIfBoundRandomUUID, bmiValue),
        ExpansionOfIntermediateNode.create(height.baseVariableName, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(height.valueSpecification, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(height.datumKey, BindIfBoundRandomUUID, heightValue),
        ExpansionOfIntermediateNode.create(weight.baseVariableName, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(weight.valueSpecification, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(weight.datumKey, BindIfBoundRandomUUID, weightValue),
        ExpansionOfIntermediateNode.create(biobankEncounter.dataset, BindIfBoundDataset, dateOfBiobankEncounterStringValue)
        
    )
}
