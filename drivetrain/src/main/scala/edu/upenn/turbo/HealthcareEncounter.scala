package edu.upenn.turbo

object HealthcareEncounter extends ExpandedGraphObjectSingleton
{
    baseVariableName = "healthcareEncounter"
    val encounterDate = "healthcareEncounterDate"
    
    val shortcutName = "shortcutHealthcareEncounterName"
    
    val encounterStart = "healthcareEncounterStart"
    val dateOfHealthcareEncounterStringValue = "healthcareEncounterDateStringValue"
    val dateOfHealthcareEncounterDateValue = "healthcareEncounterDateDateValue"
    
    val dataset = "dateDataset"
    
    pattern = s"""
  	
  		?$baseVariableName a obo:OGMS_0000097 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		?$encounterStart a turbo:TURBO_0000511 .
  		?$encounterStart obo:RO_0002223 ?$baseVariableName .
  		            
  		?$encounterDate a turbo:TURBO_0000512 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .

      """

    optionalPattern = s"""
      
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfHealthcareEncounterStringValue .
  		?$encounterDate turbo:TURBO_0006511 ?$dateOfHealthcareEncounterDateValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

        """

    optionalLinks = Map(
        "BMI" -> HealthcareEncounterBMI, 
        "Height" -> HealthcareEncounterHeight, 
        "Weight" -> HealthcareEncounterWeight, 
        "Diagnosis" -> Diagnosis,
        "Prescription" -> Prescription
    )

    mandatoryLinks = Map(
        "Identifier" -> HealthcareEncounterIdentifier
    )
    
    namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    variablesToSelect = Array(baseVariableName)
}