package edu.upenn.turbo

class HealthcareEncounter extends Encounter
{
    val baseVariableName = "healthcareEncounter"
    val encounterDateVariableName = "healthcareEncounterDate"

    val pattern = s"""
          
      ?instantiation a turbo:TURBO_0000522 .
  		?instantiation obo:OBI_0000293 ?dataset .
  	
  		?$baseVariableName a obo:OGMS_0000097 .
  		?$baseVariableName turbo:TURBO_0006601 ?shortcutHealthcareEncounterName .
  		
  		?healthcareEncounterStart a turbo:TURBO_0000511 .
  		?healthcareEncounterStart obo:RO_0002223 ?$baseVariableName .
  		            
  		?$encounterDateVariableName a turbo:TURBO_0000512 .
  		?$encounterDateVariableName obo:IAO_0000136 ?healthcareEncounterStart .

      """

    val optionalPattern = s"""
      
      ?$encounterDateVariableName turbo:TURBO_0006512 ?healthcareEncounterDateStringValue .
  		?$encounterDateVariableName turbo:TURBO_0006511 ?healthcareEncounterDateDateValue .
      ?$encounterDateVariableName obo:BFO_0000050 ?dateDataset .
      ?dateDataset obo:BFO_0000051 ?$encounterDateVariableName .

        """

    val optionalLinks = Array(
        new BMI(this), new Height(this), new Weight(this), new Diagnosis(this), new Prescription(this)
    )

    val mandatoryLinks: Array[ExpandedGraphObject] = Array(
        new HealthcareEncounterIdentifier(this)
    )
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName)
}