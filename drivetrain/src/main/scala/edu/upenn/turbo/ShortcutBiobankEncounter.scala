package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutBiobankEncounter(newInstantiation: String, newNamedGraph: String) extends ShortcutGraphObject
{

    val instantiation = newInstantiation
    val baseVariableName = "shortcutBiobankEncounter"
    val valuesKey = "shortcutBiobankEncounterIdValue"
    val registryKey = "shortcutBiobankEncounterRegistryString"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000527 ;
              turbo:TURBO_0000623   ?shortcutDatasetTitle;
              turbo:TURBO_0000628   ?$valuesKey ;
              turbo:TURBO_0000630   ?$registryKey .

          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000635 ?shortcutBmiValue .
          }
          OPTIONAL
          {
          ?$baseVariableName turbo:TURBO_0000627 ?shortcutWeightValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000626 ?shortcutHeightValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000624 ?shortcutBiobankEncounterDateStringValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000625 ?shortcutBiobankEncounterDateDateValue .
          }
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array("instantiation", "biobankEncounterRegistry"),
                              URIToString -> Array("shortcutBiobankEncounterName"),
                              MD5GlobalRandom -> Array("biobankEncounter"),
                              DatasetIRI -> Array("dataset"),
                              RandomUUID -> Array("biobankEncounterCrid", "biobankEncounterDate", "biobankEncounterStart",
                                                  "biobankEncounterRegDen", "biobankEncounterSymbol"),
                              BindIfBoundRandomUUID -> Array("BMI", "BMIvalspec", "heightValSpec", "heightAssay",
                                                             "heightDatum", "weightValSpec", "weightAssay", "weightDatum"),
                              BindAs -> Array("bmiValue", "heightValue", "weightValue", "biobankEncounterDateStringValue",
                                              "biobankEncounterIdValue", "dsTitle", "biobankEncounterDateDateValue"),
                              BindIfBoundDataset -> Array("dateDataset")
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          "dateDataset" -> "shortcutBiobankEncounterDateStringValue", 
                                          "BMI" -> "shortcutBmiValue",
                                          "BMIvalspec" -> "BMI",
                                          "heightValSpec" -> "shortcutHeightValue",
                                          "heightAssay" -> "shortcutHeightValue",
                                          "heightDatum" -> "shortcutHeightValue",
                                          "weightValSpec" -> "shortcutWeightValue",
                                          "weightAssay" -> "shortcutWeightValue",
                                          "weightDatum" -> "shortcutWeightValue"
                                        )

    val expandedVariableShortcutBindings = Map(
                                          "biobankEncounterRegistry" -> registryKey, 
                                          "shortcutBiobankEncounterName" -> baseVariableName,
                                          "instantiation" -> "instantiationUUID",
                                          "bmiValue" -> "shortcutBmiValue",
                                          "heightValue" -> "shortcutHeightValue",
                                          "weightValue" -> "shortcutWeightValue",
                                          "biobankEncounterDateStringValue" -> "shortcutBiobankEncounterDateStringValue",
                                          "biobankEncounterDateDateValue" -> "shortcutBiobankEncounterDateDateValue",
                                          "biobankEncounterIdValue" -> valuesKey,
                                          "datasetTitle" -> "shortcutDatasetTitle"
                                        )
}