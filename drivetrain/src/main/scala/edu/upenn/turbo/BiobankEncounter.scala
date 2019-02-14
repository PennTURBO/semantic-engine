package edu.upenn.turbo

class BiobankEncounter extends Encounter
{
    val baseVariableName = "biobankEncounter"
    val encounterDateVariableName = "biobankEncounterDate"

    val pattern = s"""
              		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?shortcutBiobankEncounterName .
  		
  		?biobankEncounterStart a turbo:TURBO_0000531 .
  		?biobankEncounterStart obo:RO_0002223 ?$baseVariableName .
  		?$encounterDateVariableName a turbo:TURBO_0000532 .
  		?$encounterDateVariableName obo:IAO_0000136 ?biobankEncounterStart .

      """

    val optionalPattern = s"""
      
      ?$encounterDateVariableName turbo:TURBO_0006511 ?biobankEncounterDateDateValue .
      ?$encounterDateVariableName turbo:TURBO_0006512 ?biobankEncounterDateStringValue .
      ?$encounterDateVariableName obo:BFO_0000050 ?dateDataset .
      ?dateDataset obo:BFO_0000051 ?$encounterDateVariableName .

        """

    val optionalLinks = Array(
        new BMI(this), new Height(this), new Weight(this)
    )

    val mandatoryLinks: Array[ExpandedGraphObject] = Array(
        new BiobankEncounterIdentifier(this)
    )
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName)
}