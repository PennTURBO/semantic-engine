package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenter(newInstantiation: String, newNamedGraph: String, consenter:Consenter) extends ShortcutGraphObject
{
    val consenterIdentifier = consenter.mandatoryLinks("Identifier").asInstanceOf[ConsenterIdentifier]
    val dateOfBirthDatum = consenter.optionalLinks("DateOfBirthDatum").asInstanceOf[DateOfBirthDatum]
    val genderIdentityDatum = consenter.optionalLinks("GenderIdentityDatum").asInstanceOf[GenderIdentityDatum]
    val raceIdentityDatum = consenter.optionalLinks("RaceIdentityDatum").asInstanceOf[RaceIdentityDatum]

    override val instantiation = newInstantiation
    
    val instantiationKey = "instantiation"
    val baseVariableName = "shortcutPart"
    
    val shortcutName = "shortcutPart"
    val valuesKey = "consenterSymbolValue"
    val registryKey = "consenterRegistryString"
    
    val cridKey = "shortcutCrid"
    val datasetTitle = "shortcutDatasetTitle"
    
    val dateOfBirthString = "dateOfBirthStringValue"
    val dateOfBirthDate = "dateOfBirthDateValue"
    
    val genderIdentityValue = "genderIdentityDatumValue"
    val genderIdentityType = "genderIdentityTypeString"
    
    val raceIdentityValue = "raceIdentityDatumValue"
    val raceIdentityType = "raceIdentityTypeString"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
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
    
    val namedGraph = newNamedGraph
    
    override val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName, registryKey, valuesKey)
    
    override val expansionRules = Array(
        
        ExpansionFromShortcutValue.create(raceIdentityDatum.raceIdentityValue, raceIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(dateOfBirthDatum.dateOfBirthString, dateOfBirthString, BindAs),
        ExpansionFromShortcutValue.create(dateOfBirthDatum.dateOfBirthDate, dateOfBirthDate, BindAs),
        ExpansionFromShortcutValue.create(consenterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(consenterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(genderIdentityDatum.genderIdentityValue, genderIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(genderIdentityDatum.genderIdentityValue, genderIdentityValue, BindAs),
        ExpansionFromShortcutValue.create(raceIdentityDatum.raceIdentityType, raceIdentityType, StringToURI),
        ExpansionFromShortcutValue.create(consenter.shortcutName, shortcutName, URIToString),
        ExpansionFromShortcutValue.create(consenterIdentifier.instantiation, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(consenterIdentifier.datasetTitle, datasetTitle, BindAs),
        
        ExpansionOfIntermediateNode.create(consenter.biosexKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(consenter.birthVariableName, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(consenter.heightKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(consenter.weightKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(consenter.adiposeKey, MD5LocalRandom),
        ExpansionOfIntermediateNode.create(consenter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(consenterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(consenterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(consenterIdentifier.consenterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(consenterIdentifier.consenterRegistry, RandomUUID),
        ExpansionOfIntermediateNode.create(genderIdentityDatum.baseVariableName, BindIfBoundMD5LocalRandom, genderIdentityValue),
        ExpansionOfIntermediateNode.create(raceIdentityDatum.baseVariableName, BindIfBoundMD5LocalRandom, raceIdentityType),
        ExpansionOfIntermediateNode.create(raceIdentityDatum.raceIdentificationProcess, BindIfBoundMD5LocalRandom, raceIdentityType),
        ExpansionOfIntermediateNode.create(dateOfBirthDatum.baseVariableName, BindIfBoundMD5LocalRandom, dateOfBirthString),
        ExpansionOfIntermediateNode.create(genderIdentityDatum.genderIdentityType, BiologicalSexIRI, genderIdentityType),
        ExpansionOfIntermediateNode.create(dateOfBirthDatum.dataset, BindIfBoundDataset, dateOfBirthString),
        ExpansionOfIntermediateNode.create(raceIdentityDatum.dataset, BindIfBoundDataset, raceIdentityType),
        ExpansionOfIntermediateNode.create(genderIdentityDatum.dataset, BindIfBoundDataset, genderIdentityValue)
        
        )
}