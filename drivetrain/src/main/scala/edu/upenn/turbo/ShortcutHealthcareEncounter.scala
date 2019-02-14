package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHealthcareEncounter(newInstantiation: String, newNamedGraph: String) extends ShortcutGraphObject
{

    val instantiation = newInstantiation
    val baseVariableName = "shortcutHealthcareEncounter"
    val valuesKey = "shortcutHealthcareEncounterIdValue"
    val registryKey = "shortcutHealthcareEncounterRegistryString"
    
    val pattern = s"""
          
        ?$baseVariableName a obo:OGMS_0000097 ;
          		turbo:TURBO_0000643  ?shortcutDatasetTitle ;
        			turbo:TURBO_0000648  ?$valuesKey ;
          		turbo:TURBO_0000650 ?$registryKey .
          		
          		optional 
          		{
          		
          		    ?$baseVariableName obo:RO_0002234 ?shortcutDiagnosis .
          		    ?shortcutDiagnosis a obo:OGMS_0000073 .
          		    ?shortcutDiagnosis turbo:TURBO_0004602  ?shortcutDiagnosisCodeRegistryString .
          		    
          		    optional 
          		    {
          			    ?shortcutDiagnosis turbo:TURBO_0004603 ?diagnosisCodeRegistryURIAsString .
              	  }
              		optional 
              		{
              		   ?shortcutDiagnosis turbo:TURBO_0004601 ?shortcutDiagnosisCode .
              		}
              		optional
              		{
              		    ?shortcutDiagnosis turbo:TURBO_0010013 ?shortcutPrimaryDiagnosisBoolean .
              		}
              		optional
              		{
              		    ?shortcutDiagnosis turbo:TURBO_0010014 ?shortcutDiagnosisSequence .
              		}
          		}
          		
          		optional 
          		{
          		    ?$baseVariableName obo:RO_0002234 ?shortcutPrescription .
          		    ?shortcutPrescription a obo:PDRO_0000001 .
          		    ?shortcutPrescription turbo:TURBO_0005601  ?shortcutMedId .
          		    
          		    optional
          		    {
          		        ?shortcutPrescription turbo:TURBO_0005611  ?shortcutMedOrderName .
          		    }
          		    optional
          		    {
          		        ?shortcutPrescription turbo:TURBO_0005612 ?shortcutMedMapping .
          		    }
          		}
          		
          		optional 
          		{
          			?$baseVariableName turbo:TURBO_0000644 ?shortcutHealthcareEncounterDateStringValue .
          	  }
          		optional 
          		{
          			?$baseVariableName turbo:TURBO_0000645 ?shortcutHealthcareEncounterDateDateString .
          		}
          		optional 
          		{
          		    ?$baseVariableName turbo:TURBO_0000646 ?shortcutHeightValue .
          		}
          		optional 
              {
          		    ?$baseVariableName turbo:TURBO_0000647 ?shortcutWeightValue .
          		}
          		optional
          		{
          		    ?$baseVariableName turbo:TURBO_0000655 ?shortcutBmiValue .
          		}
        }
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array("instantiation", "healthcareEncounterRegistry", "drugURI", "diagnosisRegistry"),
                              URIToString -> Array("shortcutHealthcareEncounterName"),
                              MD5GlobalRandom -> Array("healthcareEncounter"),
                              DatasetIRI -> Array("dataset"),
                              RandomUUID -> Array("healthcareEncounterCrid", "healthcareEncounterDate", "healthcareEncounterStart",
                                                  "healthcareEncounterRegDen", "healthcareEncounterSymbol"),
                              BindIfBoundMD5LocalRandomWithDependent -> Array("diagnosis", "heightValSpec", "heightAssay", "heightDatum",
                                                                              "weightValSpec", "weightAssay", "weightDatum", "BMI", "BMIvalspec",
                                                                              "prescription", "medCrid", "medSymb"),
                              BindAs -> Array("bmiValue", "heightValue", "weightValue", "healthcareEncounterDateStringValue",
                                              "healthcareEncounterIdValue", "datasetTitle", "healthcareEncounterDateDateValue"),
                              BindIfBoundDataset -> Array("dateDataset"),
                              BindIfICD9 -> Array("icd9term"),
                              BindIfICD10 -> Array("icd10term"),
                              BindIfBoundAndOtherNotBoundLeaveUnbound -> Array("diagnosisRegistry"),
                              BindIfBoundAndOtherNotBoundKeepOriginal -> Array("diagnosisRegistry")
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          "dateDataset" -> "shortcutHealthcareEncounterDateStringValue", 
                                          "BMI" -> "shortcutBmiValue",
                                          "BMIvalspec" -> "BMI",
                                          "heightValSpec" -> "shortcutHeightValue",
                                          "heightAssay" -> "shortcutHeightValue",
                                          "heightDatum" -> "shortcutHeightValue",
                                          "weightValSpec" -> "shortcutWeightValue",
                                          "weightAssay" -> "shortcutWeightValue",
                                          "weightDatum" -> "shortcutWeightValue",
                                          "diagnosis" -> "shortcutDiagnosis",
                                          "prescription" -> "shortcutPrescription",
                                          "medCrid" -> "prescription",
                                          "medSymb" -> "prescription",
                                          "diagnosisRegistry" -> "shortcutDiagnosisCode"
                                        )

    val expandedVariableShortcutBindings = Map(
                                          "healthcareEncounterRegistry" -> registryKey, 
                                          "shortcutHealthcareEncounterName" -> baseVariableName,
                                          "instantiation" -> "instantiationUUID",
                                          "bmiValue" -> "ShortcutBmiValue",
                                          "heightValue" -> "ShortcutHeightValue",
                                          "weightValue" -> "ShortcutWeightValue",
                                          "healthcareEncounterDateStringValue" -> "shortcutHealthcareEncounterDateStringValue",
                                          "healthcareEncounterDateDateValue" -> "ShortcutHealthcareEncounterDateDateValue",
                                          "healthcareEncounterIdValue" -> valuesKey,
                                          "datasetTitle" -> "shortcutDatasetTitle",
                                          "drugURI" -> "shortcutMedMapping",
                                          "diagnosisRegistry" -> "diagnosisCodeRegistryURIAsString",
                                          "icd9term" -> "diagnosisRegistry",
                                          "icd10term" -> "diagnosisRegistry"
                                        )
}