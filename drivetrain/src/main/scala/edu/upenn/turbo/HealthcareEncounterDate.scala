package edu.upenn.turbo

class HealthcareEncounterDate extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterDate.pattern
    val baseVariableName = HealthcareEncounterDate.baseVariableName
    val typeURI = HealthcareEncounterDate.typeURI
    val variablesToSelect = HealthcareEncounterDate.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterDate extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): HealthcareEncounterDate =
    {
        new HealthcareEncounterDate(optional)
    }
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000512"
    
    val baseVariableName = "healthcareEncounterDate"
    val encounterDate = baseVariableName
    
    val encounterStart = "healthcareEncounterStart"
    val dateOfHealthcareEncounterStringValue = "healthcareEncounterDateStringValue"
    val dateOfHealthcareEncounterDateValue = "healthcareEncounterDateDateValue"
    
    val dataset = HealthcareEncounter.dataset
    val healthcareEncounter = HealthcareEncounter.baseVariableName
    
    val pattern = s"""
  	
  		?$healthcareEncounter a obo:OGMS_0000097 .
  		
  		?$encounterStart a turbo:TURBO_0000511 .
  		?$encounterStart obo:RO_0002223 ?$healthcareEncounter .
  		            
  		?$encounterDate a <$typeURI> .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .
  		
  		?$encounterDate turbo:TURBO_0006512 ?$dateOfHealthcareEncounterStringValue .
  		?$encounterDate turbo:TURBO_0006511 ?$dateOfHealthcareEncounterDateValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

      """
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(dateOfHealthcareEncounterStringValue, dateOfHealthcareEncounterDateValue)
}