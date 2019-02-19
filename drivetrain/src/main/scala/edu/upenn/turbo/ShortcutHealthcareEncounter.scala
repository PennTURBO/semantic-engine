package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHealthcareEncounter(newInstantiation: String, newNamedGraph: String, healthcareEncounter: HealthcareEncounter) extends ShortcutGraphObject
{
    val healthcareEncounterIdentifier = healthcareEncounter.mandatoryLinks("Identifier").asInstanceOf[HealthcareEncounterIdentifier]
    val BMI = healthcareEncounter.optionalLinks("BMI").asInstanceOf[BMI]
    val height = healthcareEncounter.optionalLinks("Height").asInstanceOf[Height]
    val weight = healthcareEncounter.optionalLinks("Weight").asInstanceOf[Weight]
    val diagnosisInstance = healthcareEncounter.optionalLinks("Diagnosis").asInstanceOf[Diagnosis]
    val prescriptionInstance = healthcareEncounter.optionalLinks("Prescription").asInstanceOf[Prescription]

    val instantiation = newInstantiation
    val baseVariableName = "shortcutHealthcareEncounter"
    val valuesKey = "shortcutHealthcareEncounterIdValue"
    val registryKey = "shortcutHealthcareEncounterRegistryString"
    
    val diagnosis = "shortcutDiagnosis"
    val diagnosisCodeRegistryString = "shortcutDiagnosisCodeRegistryString"
    val diagnosisCodeRegistryURI = "shortcutDiagnosisCodeRegistryURI"
    val diagnosisCode = "shortcutDiagnosisCode"
    val primaryDiagnosis = "shortcutPrimaryDiagnosisBoolean"
    val diagnosisSequence = "shortcutDiagnosisSequence"
    
    val prescription = "shortcutPrescription"
    val medicationSymbolValue = "shortcutMedId"
    val medicationOrderName = "shortcutMedOrderName"
    val mappedMedicationTerm = "shortcutMedMap"
    
    val dateOfHealthcareEncounterStringValue = "healthcareEncounterDateString"
    val dateOfHealthcareEncounterDateValue = "healthcareEncounterFormattedDate"
    
    val heightValue = "healthcareEncounterHeight"
    val weightValue = "healthcareEncounterWeight"
    val bmiValue = "healthcareEncounterBmi"
    
    val consenterRegistry = "healthcareEncounterConsenterRegistry"
    val consenterSymbol = "healthcareEncounterConsenterSymbol"
    
    val datasetTitle = "datasetTitle"
    
    val pattern = s"""
          
        ?$baseVariableName a obo:OGMS_0000097 ;
          		turbo:TURBO_0000643  ?$datasetTitle ;
        			turbo:TURBO_0000648  ?$valuesKey ;
          		turbo:TURBO_0000650 ?$registryKey .
          		
          		optional 
          		{
          		
          		    ?$baseVariableName obo:RO_0002234 ?$diagnosis .
          		    ?$diagnosis a obo:OGMS_0000073 .
          		    ?$diagnosis turbo:TURBO_0004602  ?$diagnosisCodeRegistryString .
          		    
          		    optional 
          		    {
          			    ?$diagnosis turbo:TURBO_0004603 ?$diagnosisCodeRegistryURI .
              	  }
              		optional 
              		{
              		   ?$diagnosis turbo:TURBO_0004601 ?$diagnosisCode .
              		}
              		optional
              		{
              		    ?$diagnosis turbo:TURBO_0010013 ?$primaryDiagnosis .
              		}
              		optional
              		{
              		    ?$diagnosis turbo:TURBO_0010014 ?$diagnosisSequence .
              		}
          		}
          		
          		optional 
          		{
          		    ?$baseVariableName obo:RO_0002234 ?$prescription .
          		    ?$prescription a obo:PDRO_0000001 .
          		    ?$prescription turbo:TURBO_0005601  ?medicationSymbolValue .
          		    
          		    optional
          		    {
          		        ?$prescription turbo:TURBO_0005611  ?$medicationOrderName .
          		    }
          		    optional
          		    {
          		        ?$prescription turbo:TURBO_0005612 ?$mappedMedicationTerm .
          		    }
          		}
          		
          		optional 
          		{
          			?$baseVariableName turbo:TURBO_0000644 ?$dateOfHealthcareEncounterStringValue .
          	  }
          		optional 
          		{
          			?$baseVariableName turbo:TURBO_0000645 ?$dateOfHealthcareEncounterStringValue .
          		}
          		optional 
          		{
          		    ?$baseVariableName turbo:TURBO_0000646 ?$heightValue .
          		}
          		optional 
              {
          		    ?$baseVariableName turbo:TURBO_0000647 ?$weightValue .
          		}
          		optional
          		{
          		    ?$baseVariableName turbo:TURBO_0000655 ?$bmiValue .
          		}
          		OPTIONAL
              {
                ?$baseVariableName turbo:TURBO_0010002 ?$consenterRegistry .
              }
              OPTIONAL
              {
                ?$baseVariableName turbo:TURBO_0010000 ?$consenterSymbol .
              }
            
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array(healthcareEncounterIdentifier.registryKey, prescriptionInstance.mappedMedicationTerm, 
                                                   diagnosisInstance.registryKey),
                              InstantiationStringToURI -> Array("instantiationKey"),
                              URIToString -> Array(healthcareEncounter.shortcutName),
                              MD5GlobalRandom -> Array(healthcareEncounter.baseVariableName),
                              DatasetIRI -> Array(healthcareEncounterIdentifier.dataset),
                              RandomUUID -> Array(healthcareEncounterIdentifier.baseVariableName, healthcareEncounter.encounterDate, 
                                                  healthcareEncounter.encounterStart, healthcareEncounterIdentifier.encounterRegistryDenoter, 
                                                  healthcareEncounterIdentifier.encounterSymbol),
                              BindIfBoundMD5LocalRandomWithDependent -> Array(diagnosisInstance.baseVariableName, height.valueSpecification, 
                                                                              height.baseVariableName, height.datumKey, weight.valueSpecification,
                                                                              weight.baseVariableName, weight.datumKey, BMI.baseVariableName, 
                                                                              BMI.valueSpecification, prescriptionInstance.baseVariableName,
                                                                              prescriptionInstance.prescriptionCrid, prescriptionInstance.medicationSymbol),
                              BindAs -> Array(BMI.valuesKey, height.valuesKey, weight.valuesKey, healthcareEncounter.dateOfHealthcareEncounterStringValue,
                                              healthcareEncounterIdentifier.valuesKey, healthcareEncounter.dateOfHealthcareEncounterDateValue, 
                                              prescriptionInstance.medicationOrderName, diagnosisInstance.registryKey, diagnosisInstance.primaryDiagnosis,  
                                              diagnosisInstance.valuesKey, diagnosisInstance.diagnosisSequence, healthcareEncounterIdentifier.valuesKey),
                              BindIfBoundDataset -> Array(healthcareEncounter.dataset)
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          healthcareEncounter.dataset -> dateOfHealthcareEncounterStringValue, 
                                          BMI.baseVariableName -> bmiValue,
                                          BMI.valueSpecification -> bmiValue,
                                          height.valueSpecification -> heightValue,
                                          height.baseVariableName -> heightValue,
                                          height.datumKey -> heightValue,
                                          weight.valueSpecification -> weightValue,
                                          weight.baseVariableName -> weightValue,
                                          weight.datumKey -> weightValue,
                                          diagnosisInstance.baseVariableName -> diagnosis,
                                          prescriptionInstance.baseVariableName -> prescription,
                                          prescriptionInstance.prescriptionCrid -> prescription,
                                          prescriptionInstance.medicationSymbol -> prescription
                                        )

    val expandedVariableShortcutBindings = Map(
                                          healthcareEncounterIdentifier.registryKey -> registryKey, 
                                          healthcareEncounter.shortcutName -> baseVariableName,
                                          healthcareEncounterIdentifier.instantiationKey -> instantiation,
                                          BMI.bmiValue -> bmiValue,
                                          height.heightValue -> heightValue,
                                          weight.weightValue -> weightValue,
                                          healthcareEncounter.dateOfHealthcareEncounterStringValue -> dateOfHealthcareEncounterStringValue,
                                          healthcareEncounter.dateOfHealthcareEncounterDateValue -> dateOfHealthcareEncounterDateValue,
                                          healthcareEncounterIdentifier.valuesKey -> valuesKey,
                                          healthcareEncounterIdentifier.datasetTitle -> datasetTitle,
                                          prescriptionInstance.mappedMedicationTerm -> mappedMedicationTerm,
                                          diagnosisInstance.diagnosisCodeRegistryString -> diagnosisCodeRegistryString,
                                          prescriptionInstance.medicationOrderName -> medicationOrderName,
                                          prescriptionInstance.medicationSymbolValue -> medicationSymbolValue,
                                          diagnosisInstance.primaryDiagnosis -> primaryDiagnosis,
                                          diagnosisInstance.diagnosisCode -> diagnosisCode,
                                          diagnosisInstance.diagnosisSequence -> diagnosisSequence,
                                          diagnosisInstance.registryKey -> registryKey
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