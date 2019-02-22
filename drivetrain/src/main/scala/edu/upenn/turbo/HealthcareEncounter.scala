package edu.upenn.turbo

class HealthcareEncounter extends Encounter
{
    val baseVariableName = "healthcareEncounter"
    val encounterDate = "healthcareEncounterDate"
    
    val shortcutName = "shortcutHealthcareEncounterName"
    
    val encounterStart = "healthcareEncounterStart"
    val dateOfHealthcareEncounterStringValue = "healthcareEncounterDateStringValue"
    val dateOfHealthcareEncounterDateValue = "healthcareEncounterDateDateValue"
    
    val dataset = "dateDataset"
    
    val pattern = s"""
  	
  		?$baseVariableName a obo:OGMS_0000097 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		?$encounterStart a turbo:TURBO_0000511 .
  		?$encounterStart obo:RO_0002223 ?$baseVariableName .
  		            
  		?$encounterDate a turbo:TURBO_0000512 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .

      """

    override val optionalPattern = s"""
      
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfHealthcareEncounterStringValue .
  		?$encounterDate turbo:TURBO_0006511 ?$dateOfHealthcareEncounterDateValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

        """

    override val optionalLinks = Map(
        "BMI" -> new BMI(this), 
        "Height" -> new Height(this), 
        "Weight" -> new Weight(this), 
        "Diagnosis" -> new Diagnosis(this),
        "Prescription" -> new Prescription(this)
    )

    override val mandatoryLinks: Map[String, GraphObject] = Map(
        "Identifier" -> new HealthcareEncounterIdentifier(this)
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    override val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName)
}