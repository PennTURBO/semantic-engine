package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutHealthcareEncounter(newInstantiation: String, newNamedGraph: String, 
                                  healthcareEncounter: HealthcareEncounter, join: ShortcutParticipantToEncounterJoin) 
                                  extends ShortcutGraphObject
{
    val healthcareEncounterIdentifier = healthcareEncounter.mandatoryLinks("Identifier").asInstanceOf[HealthcareEncounterIdentifier]
    val BMI = healthcareEncounter.optionalLinks("BMI").asInstanceOf[BMI]
    val height = healthcareEncounter.optionalLinks("Height").asInstanceOf[Height]
    val weight = healthcareEncounter.optionalLinks("Weight").asInstanceOf[Weight]
    val diagnosisInstance = healthcareEncounter.optionalLinks("Diagnosis").asInstanceOf[Diagnosis]
    val prescriptionInstance = healthcareEncounter.optionalLinks("Prescription").asInstanceOf[Prescription]

    override val instantiation = newInstantiation

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
    val consenterURI = "shortcutConsenterURI"
    
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
          		    ?$prescription turbo:TURBO_0005601  ?$medicationSymbolValue .
          		    
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
          			?$baseVariableName turbo:TURBO_0000645 ?$dateOfHealthcareEncounterDateValue .
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
                ?$baseVariableName turbo:TURBO_0010000 ?$consenterSymbol .
                ?$baseVariableName turbo:ScHcEnc2UnexpandedConsenter ?$consenterSymbol .
              }
            
      """  
    
    val namedGraph = newNamedGraph
    
    override val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
    
    override val expansionRules = Array(
        
        ExpansionFromShortcutValue.create(healthcareEncounterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(prescriptionInstance.mappedMedicationTerm, mappedMedicationTerm, StringToURI),
        ExpansionFromShortcutValue.create(diagnosisInstance.registryKey, diagnosisCodeRegistryURI, StringToURI),
        ExpansionFromShortcutValue.create(healthcareEncounterIdentifier.instantiationKey, instantiation, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(healthcareEncounter.shortcutName, baseVariableName, URIToString),
        ExpansionFromShortcutValue.create(join.consenterName, consenterURI, MD5GlobalRandomWithOriginal),
        ExpansionFromShortcutValue.create(BMI.valuesKey, bmiValue, BindAs),
        ExpansionFromShortcutValue.create(height.valuesKey, heightValue, BindAs),
        ExpansionFromShortcutValue.create(weight.valuesKey, weightValue, BindAs),
        ExpansionFromShortcutValue.create(healthcareEncounter.dateOfHealthcareEncounterStringValue, dateOfHealthcareEncounterStringValue, BindAs),
        ExpansionFromShortcutValue.create(healthcareEncounter.dateOfHealthcareEncounterDateValue, dateOfHealthcareEncounterDateValue, BindAs),
        ExpansionFromShortcutValue.create(healthcareEncounterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(prescriptionInstance.medicationOrderName, medicationOrderName, BindAs),
        ExpansionFromShortcutValue.create(prescriptionInstance.medicationSymbolValue, medicationSymbolValue, BindAs),
        ExpansionFromShortcutValue.create(diagnosisInstance.primaryDiagnosis, primaryDiagnosis, BindAs),
        ExpansionFromShortcutValue.create(diagnosisInstance.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(diagnosisInstance.diagnosisSequence, diagnosisSequence, BindAs),
        ExpansionFromShortcutValue.create(diagnosisInstance.diagnosisCodeRegistryString, diagnosisCodeRegistryString, BindAs),
        ExpansionFromShortcutValue.create(diagnosisInstance.diagnosisCode, diagnosisCode, BindAs),
        
        ExpansionOfIntermediateNode.create(healthcareEncounter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(healthcareEncounterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(healthcareEncounterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(healthcareEncounter.encounterDate, RandomUUID),
        ExpansionOfIntermediateNode.create(healthcareEncounter.encounterStart, RandomUUID),
        ExpansionOfIntermediateNode.create(healthcareEncounterIdentifier.encounterRegistryDenoter, RandomUUID),
        ExpansionOfIntermediateNode.create(healthcareEncounterIdentifier.encounterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(diagnosisInstance.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, diagnosis),
        ExpansionOfIntermediateNode.create(height.valueSpecification, BindIfBoundMD5LocalRandomWithDependent, heightValue),
        ExpansionOfIntermediateNode.create(height.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, heightValue),
        ExpansionOfIntermediateNode.create(height.datumKey, BindIfBoundMD5LocalRandomWithDependent, heightValue),
        ExpansionOfIntermediateNode.create(weight.valueSpecification, BindIfBoundMD5LocalRandomWithDependent, weightValue),
        ExpansionOfIntermediateNode.create(weight.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, weightValue),
        ExpansionOfIntermediateNode.create(weight.datumKey, BindIfBoundMD5LocalRandomWithDependent, weightValue),
        ExpansionOfIntermediateNode.create(BMI.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, bmiValue),
        ExpansionOfIntermediateNode.create(BMI.valueSpecification, BindIfBoundMD5LocalRandomWithDependent, bmiValue),
        ExpansionOfIntermediateNode.create(prescriptionInstance.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, prescription),
        ExpansionOfIntermediateNode.create(prescriptionInstance.prescriptionCrid, BindIfBoundMD5LocalRandomWithDependent, prescription),
        ExpansionOfIntermediateNode.create(prescriptionInstance.medicationSymbol, BindIfBoundMD5LocalRandomWithDependent, prescription),
        ExpansionOfIntermediateNode.create(healthcareEncounter.dataset, BindIfBoundDataset, dateOfHealthcareEncounterStringValue)
    )

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array(healthcareEncounterIdentifier.registryKey, prescriptionInstance.mappedMedicationTerm, 
                                                   diagnosisInstance.registryKey),
                              InstantiationStringToURI -> Array(healthcareEncounterIdentifier.instantiationKey),
                              URIToString -> Array(healthcareEncounter.shortcutName),
                              MD5GlobalRandom -> Array(healthcareEncounter.baseVariableName),
                              MD5GlobalRandomWithOriginal -> Array(join.consenterName),
                              MD5GlobalRandomWithDependent -> Array(healthcareEncounterIdentifier.dataset),
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
                                              prescriptionInstance.medicationOrderName, diagnosisInstance.primaryDiagnosis, join.encounterName,
                                              diagnosisInstance.valuesKey, diagnosisInstance.diagnosisSequence, healthcareEncounterIdentifier.valuesKey,
                                              diagnosisInstance.diagnosisCodeRegistryString, prescriptionInstance.medicationSymbolValue),
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
                                          prescriptionInstance.medicationSymbol -> prescription,
                                          healthcareEncounterIdentifier.dataset -> datasetTitle
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
                                          diagnosisInstance.registryKey -> diagnosisCodeRegistryURI,
                                          join.encounterName -> healthcareEncounter.baseVariableName,
                                          join.consenterName -> consenterURI
                                        )
                                        
    override val appendToBind = """
        BIND(IF (?diagnosisRegistry = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, uri(concat("http://purl.bioontology.org/ontology/ICD9CM/", ?shortcutDiagnosisCode)), ?unbound) AS ?icd9term)
        BIND(IF (?diagnosisRegistry = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, uri(concat("http://purl.bioontology.org/ontology/ICD10CM/", ?shortcutDiagnosisCode)), ?unbound) AS ?icd10term)
        BIND(IF (bound(?icd9term) && !bound(?icd10term),?icd9term,?unbound) as ?concatIcdTerm)
        BIND(IF (bound(?icd10term) && !bound(?icd9term),?icd10term,?concatIcdTerm) as ?concatIcdTerm)
      """
}