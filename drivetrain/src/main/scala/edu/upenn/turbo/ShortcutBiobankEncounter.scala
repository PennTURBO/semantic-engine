package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutBiobankEncounter(newInstantiation: String, newNamedGraph: String) extends ShortcutGraphObject
{

    val instantiation = newInstantiation
    val baseVariableName = "shortcutBiobankEncounter"
    val valuesKey = "ShortcutBiobankEncounterIdValue"
    val registryKey = "biobankEncounterRegistryString"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000527 ;
              turbo:TURBO_0000623   ?shortcutDatasetTitle;
              turbo:TURBO_0000628   ?$valuesKey ;
              turbo:TURBO_0000630   ?$registryKey .

          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000635 ?ShortcutBmiValue .
          }
          OPTIONAL
          {
          ?$baseVariableName turbo:TURBO_0000627 ?ShortcutWeightValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000626 ?ShortcutHeightValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000624 ?ShortcutBiobankEncounterDateStringValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000625 ?ShortcutBiobankEncounterDateDateValue .
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
                                          "dateDataset" -> "ShortcutBiobankEncounterDateStringValue", 
                                          "BMI" -> "ShortcutBmiValue",
                                          "BMIvalspec" -> "BMI",
                                          "heightValSpec" -> "ShortcutHeightValue",
                                          "heightAssay" -> "ShortcutHeightValue",
                                          "heightDatum" -> "ShortcutHeightValue",
                                          "weightValSpec" -> "ShortcutWeightValue",
                                          "weightAssay" -> "ShortcutWeightValue",
                                          "weightDatum" -> "ShortcutWeightValue"
                                        )

    val expandedVariableShortcutBindings = Map(
                                          "biobankEncounterRegistry" -> registryKey, 
                                          "shortcutBiobankEncounterName" -> baseVariableName,
                                          "instantiation" -> "instantiationUUID",
                                          "bmiValue" -> "ShortcutBmiValue",
                                          "heightValue" -> "ShortcutHeightValue",
                                          "weightValue" -> "ShortcutWeightValue",
                                          "biobankEncounterDateStringValue" -> "ShortcutBiobankEncounterDateStringValue",
                                          "biobankEncounterDateDateValue" -> "ShortcutBiobankEncounterDateDateValue",
                                          "biobankEncounterIdValue" -> valuesKey,
                                          "dsTitle" -> "shortcutDatasetTitle"
                                        )
}