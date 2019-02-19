package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutBiobankEncounter(newInstantiation: String, newNamedGraph: String, biobankEncounter: BiobankEncounter) extends ShortcutGraphObject
{
    val biobankEncounterIdentifier = biobankEncounter.mandatoryLinks("Identifier").asInstanceOf[BiobankEncounterIdentifier]
    val BMI = biobankEncounter.optionalLinks("BMI").asInstanceOf[BMI]
    val height = biobankEncounter.optionalLinks("Height").asInstanceOf[Height]
    val weight = biobankEncounter.optionalLinks("Weight").asInstanceOf[Weight]

    val instantiation = newInstantiation
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
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0010010 ?$consenterSymbol .
          }
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array(biobankEncounterIdentifier.registryKey),
                              InstantiationStringToURI -> Array(biobankEncounterIdentifier.instantiationKey),
                              URIToString -> Array(biobankEncounter.shortcutName),
                              MD5GlobalRandom -> Array(biobankEncounter.baseVariableName),
                              DatasetIRI -> Array(biobankEncounterIdentifier.dataset),
                              RandomUUID -> Array(biobankEncounterIdentifier.baseVariableName, biobankEncounter.encounterDate,
                                                  biobankEncounter.encounterStart, biobankEncounterIdentifier.encounterRegistryDenoter, 
                                                  biobankEncounterIdentifier.encounterSymbol),
                              BindIfBoundRandomUUID -> Array(BMI.baseVariableName, BMI.valueSpecification, height.valueSpecification, 
                                                            height.baseVariableName, height.datumKey, weight.valueSpecification, 
                                                            weight.baseVariableName, weight.datumKey),
                              BindAs -> Array(BMI.bmiValue, height.heightValue, weight.weightValue, biobankEncounter.dateOfBiobankEncounterStringValue,
                                              biobankEncounterIdentifier.valuesKey, biobankEncounterIdentifier.datasetTitle, 
                                              biobankEncounter.dateOfBiobankEncounterDateValue),
                              BindIfBoundDataset -> Array(biobankEncounter.dataset)
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          biobankEncounter.dataset -> dateOfBiobankEncounterStringValue, 
                                          BMI.baseVariableName -> bmiValue,
                                          BMI.valueSpecification -> bmiValue,
                                          height.valueSpecification -> heightValue,
                                          height.baseVariableName -> heightValue,
                                          height.datumKey -> heightValue,
                                          weight.valueSpecification -> weightValue,
                                          weight.baseVariableName -> weightValue,
                                          weight.datumKey -> weightValue
                                        )

    val expandedVariableShortcutBindings = Map(
                                          biobankEncounterIdentifier.registryKey -> registryKey, 
                                          biobankEncounter.shortcutName -> baseVariableName,
                                          biobankEncounterIdentifier.instantiationKey -> instantiationKey,
                                          BMI.bmiValue -> bmiValue,
                                          height.heightValue -> heightValue,
                                          weight.weightValue -> weightValue,
                                          biobankEncounter.dateOfBiobankEncounterStringValue -> dateOfBiobankEncounterStringValue,
                                          biobankEncounter.dateOfBiobankEncounterDateValue -> dateOfBiobankEncounterDateValue,
                                          biobankEncounterIdentifier.valuesKey -> valuesKey,
                                          biobankEncounterIdentifier.datasetTitle -> datasetTitle
                                        )
    val appendToBind = """"""
    
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
}
