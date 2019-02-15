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
          			    ?shortcutDiagnosis turbo:TURBO_0004603 ?shortcutDiagnosisCodeRegistryURI .
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
          			?$baseVariableName turbo:TURBO_0000645 ?shortcutHealthcareEncounterDateDateValue .
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
          		OPTIONAL
              {
                ?$baseVariableName turbo:TURBO_0010002 ?shortcutConsenterRegistryString .
              }
              OPTIONAL
              {
                ?$baseVariableName turbo:TURBO_0010000 ?shortcutConsenterSymbol .
              }
            
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array("instantiation", "healthcareEncounterRegistry", "drugURI", "diagnosisRegistry",
                                                  "consenterRegistry"),
                              URIToString -> Array("shortcutHealthcareEncounterName"),
                              MD5GlobalRandom -> Array("healthcareEncounter"),
                              DatasetIRI -> Array("dataset"),
                              RandomUUID -> Array("healthcareEncounterCrid", "healthcareEncounterDate", "healthcareEncounterStart",
                                                  "healthcareEncounterRegistryDenoter", "healthcareEncounterSymbol"),
                              BindIfBoundMD5LocalRandomWithDependent -> Array("diagnosis", "heightValSpec", "heightAssay", "heightDatum",
                                                                              "weightValSpec", "weightAssay", "weightDatum", "BMI", "BMIvalspec",
                                                                              "prescription", "medCrid", "medSymb"),
                              BindAs -> Array("bmiValue", "heightValue", "weightValue", "healthcareEncounterDateStringValue",
                                              "healthcareEncounterIdValue", "datasetTitle", "healthcareEncounterDateDateValue", 
                                              "orderNameString", "medId", "diagCodeRegTextVal", "primaryDiag", "diagnosisCodeValue",
                                              "diagSequence", "consenterSymbol"),
                              BindIfBoundDataset -> Array("dateDataset")
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          "dateDataset" -> "shortcutHealthcareEncounterDateStringValue", 
                                          "BMI" -> "shortcutBmiValue",
                                          "BMIvalspec" -> "shortcutBmiValue",
                                          "heightValSpec" -> "shortcutHeightValue",
                                          "heightAssay" -> "shortcutHeightValue",
                                          "heightDatum" -> "shortcutHeightValue",
                                          "weightValSpec" -> "shortcutWeightValue",
                                          "weightAssay" -> "shortcutWeightValue",
                                          "weightDatum" -> "shortcutWeightValue",
                                          "diagnosis" -> "shortcutDiagnosis",
                                          "prescription" -> "shortcutPrescription",
                                          "medCrid" -> "prescription",
                                          "medSymb" -> "prescription"
                                        )

    val expandedVariableShortcutBindings = Map(
                                          "healthcareEncounterRegistry" -> registryKey, 
                                          "shortcutHealthcareEncounterName" -> baseVariableName,
                                          "instantiation" -> "instantiationUUID",
                                          "bmiValue" -> "shortcutBmiValue",
                                          "heightValue" -> "shortcutHeightValue",
                                          "weightValue" -> "shortcutWeightValue",
                                          "healthcareEncounterDateStringValue" -> "shortcutHealthcareEncounterDateStringValue",
                                          "healthcareEncounterDateDateValue" -> "shortcutHealthcareEncounterDateDateValue",
                                          "healthcareEncounterIdValue" -> valuesKey,
                                          "datasetTitle" -> "shortcutDatasetTitle",
                                          "drugURI" -> "shortcutMedMapping",
                                          "diagnosisRegistry" -> "shortcutDiagnosisCodeRegistryURI",
                                          "orderNameString" -> "shortcutMedOrderName",
                                          "medId" -> "shortcutMedId",
                                          "diagCodeRegTextVal" -> "shortcutDiagnosisCodeRegistryString",
                                          "primaryDiag" -> "shortcutPrimaryDiagnosisBoolean",
                                          "diagnosisCodeValue" -> "shortcutDiagnosisCode",
                                          "diagSequence" -> "shortcutDiagnosisSequence",
                                          "consenterRegistry" -> "shortcutConsenterRegistryString",
                                          "consenterSymbol" -> "shortcutConsenterSymbol"
                                        )
                                        
    val appendToBind = """
        BIND(IF (?diagnosisRegistry = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, uri(concat("http://purl.bioontology.org/ontology/ICD9CM/", ?shortcutDiagnosisCode)), ?unbound) AS ?icd9term)
        BIND(IF (?diagnosisRegistry = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, uri(concat("http://purl.bioontology.org/ontology/ICD10CM/", ?shortcutDiagnosisCode)), ?unbound) AS ?icd10term)
        BIND(IF (bound(?icd9term) && !bound(?icd10term),?icd9term,?unbound) as ?concatIcdTerm)
        BIND(IF (bound(?icd10term) && !bound(?icd9term),?icd10term,?concatIcdTerm) as ?concatIcdTerm)
      """
    
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
}