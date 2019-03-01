package edu.upenn.turbo

class BiobankEncounterDate extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterDate.pattern
    val baseVariableName = BiobankEncounterDate.baseVariableName
    val typeURI = BiobankEncounterDate.typeURI
    val variablesToSelect = BiobankEncounterDate.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterDate extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): BiobankEncounterDate =
    {
        new BiobankEncounterDate(optional)
    }
    
    val baseVariableName = "biobankEncounterDate"
    
    val encounterDate = baseVariableName
    val encounterStart = "biobankEncounterStart"
    
    val dateOfBiobankEncounterStringValue = "biobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "biobankEncounterDateDateValue"
    
    val biobankEncounter = BiobankEncounter.baseVariableName
    
    val dataset = BiobankEncounter.dataset

    val pattern = s"""
              		
      ?$biobankEncounter a turbo:TURBO_0000527 .
  		
  		?$encounterStart a turbo:TURBO_0000531 .
  		?$encounterStart obo:RO_0002223 ?$biobankEncounter .
  		?$encounterDate a turbo:TURBO_0000532 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .
  		
  		?$encounterDate turbo:TURBO_0006511 ?$dateOfBiobankEncounterDateValue .
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfBiobankEncounterStringValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

      """

    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(dateOfBiobankEncounterStringValue, dateOfBiobankEncounterDateValue)
}