package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

object ShortcutHealthcareEncounter extends ShortcutGraphObject
{
    baseVariableName = "shortcutHealthcareEncounter"
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
    
    val instantiationKey = "instantiation"
    
    pattern = s"""
          
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
    
    typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
    
    expansionRules = Array(
        
        ExpansionFromShortcutValue.create(HealthcareEncounterIdentifier.registryKey, registryKey, StringToURI),
        ExpansionFromShortcutValue.create(Prescription.mappedMedicationTerm, mappedMedicationTerm, StringToURI),
        ExpansionFromShortcutValue.create(Diagnosis.registryKey, diagnosisCodeRegistryURI, StringToURI),
        ExpansionFromShortcutValue.create(HealthcareEncounterIdentifier.instantiationKey, instantiationKey, InstantiationStringToURI),
        ExpansionFromShortcutValue.create(HealthcareEncounter.shortcutName, baseVariableName, URIToString),
        ExpansionFromShortcutValue.create(ConsenterToHealthcareEncounterJoin.consenterName, consenterURI, MD5GlobalRandomWithOriginal),
        ExpansionFromShortcutValue.create(HealthcareEncounterBMI.valuesKey, bmiValue, BindAs),
        ExpansionFromShortcutValue.create(HealthcareEncounterWeight.valuesKey, heightValue, BindAs),
        ExpansionFromShortcutValue.create(HealthcareEncounterWeight.valuesKey, weightValue, BindAs),
        ExpansionFromShortcutValue.create(HealthcareEncounter.dateOfHealthcareEncounterStringValue, dateOfHealthcareEncounterStringValue, BindAs),
        ExpansionFromShortcutValue.create(HealthcareEncounter.dateOfHealthcareEncounterDateValue, dateOfHealthcareEncounterDateValue, BindAs),
        ExpansionFromShortcutValue.create(HealthcareEncounterIdentifier.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(Prescription.medicationOrderName, medicationOrderName, BindAs),
        ExpansionFromShortcutValue.create(Prescription.medicationSymbolValue, medicationSymbolValue, BindAs),
        ExpansionFromShortcutValue.create(Diagnosis.primaryDiagnosis, primaryDiagnosis, BindAs),
        ExpansionFromShortcutValue.create(Diagnosis.valuesKey, valuesKey, BindAs),
        ExpansionFromShortcutValue.create(Diagnosis.diagnosisSequence, diagnosisSequence, BindAs),
        ExpansionFromShortcutValue.create(Diagnosis.diagnosisCodeRegistryString, diagnosisCodeRegistryString, BindAs),
        ExpansionFromShortcutValue.create(Diagnosis.diagnosisCode, diagnosisCode, BindAs),
        
        ExpansionOfIntermediateNode.create(HealthcareEncounter.baseVariableName, MD5GlobalRandom),
        ExpansionOfIntermediateNode.create(HealthcareEncounterIdentifier.dataset, MD5GlobalRandomWithDependent, datasetTitle),
        ExpansionOfIntermediateNode.create(HealthcareEncounterIdentifier.baseVariableName, RandomUUID),
        ExpansionOfIntermediateNode.create(HealthcareEncounter.encounterDate, RandomUUID),
        ExpansionOfIntermediateNode.create(HealthcareEncounter.encounterStart, RandomUUID),
        ExpansionOfIntermediateNode.create(HealthcareEncounterIdentifier.encounterRegistryDenoter, RandomUUID),
        ExpansionOfIntermediateNode.create(HealthcareEncounterIdentifier.encounterSymbol, RandomUUID),
        ExpansionOfIntermediateNode.create(Diagnosis.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, diagnosis),
        ExpansionOfIntermediateNode.create(HealthcareEncounterHeight.valueSpecification, BindIfBoundMD5LocalRandomWithDependent, heightValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterHeight.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, heightValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterHeight.datumKey, BindIfBoundMD5LocalRandomWithDependent, heightValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterWeight.valueSpecification, BindIfBoundMD5LocalRandomWithDependent, weightValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterWeight.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, weightValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterWeight.datumKey, BindIfBoundMD5LocalRandomWithDependent, weightValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterBMI.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, bmiValue),
        ExpansionOfIntermediateNode.create(HealthcareEncounterBMI.valueSpecification, BindIfBoundMD5LocalRandomWithDependent, bmiValue),
        ExpansionOfIntermediateNode.create(Prescription.baseVariableName, BindIfBoundMD5LocalRandomWithDependent, prescription),
        ExpansionOfIntermediateNode.create(Prescription.prescriptionCrid, BindIfBoundMD5LocalRandomWithDependent, prescription),
        ExpansionOfIntermediateNode.create(Prescription.medicationSymbol, BindIfBoundMD5LocalRandomWithDependent, prescription),
        ExpansionOfIntermediateNode.create(HealthcareEncounter.dataset, BindIfBoundDataset, dateOfHealthcareEncounterStringValue)
    )
    
    val diagRegKey: String = Diagnosis.registryKey
    val diagIcdTerm: String = Diagnosis.icdTerm
                                        
    appendToBind = s"""
        BIND(IF (?$diagRegKey = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, uri(concat("http://purl.bioontology.org/ontology/ICD9CM/", ?$diagnosisCode)), ?unbound) AS ?icd9term)
        BIND(IF (?$diagRegKey = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, uri(concat("http://purl.bioontology.org/ontology/ICD10CM/", ?$diagnosisCode)), ?unbound) AS ?icd10term)
        BIND(IF (bound(?icd9term) && !bound(?icd10term),?icd9term,?unbound) as ?$diagIcdTerm)
        BIND(IF (bound(?icd10term) && !bound(?icd9term),?icd10term,?concatIcdTerm) as ?$diagIcdTerm)
      """
        
    namedGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_*"
}