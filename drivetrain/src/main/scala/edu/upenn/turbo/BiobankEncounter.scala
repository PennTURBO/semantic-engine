package edu.upenn.turbo

object BiobankEncounter extends GraphObject
{
    baseVariableName = "biobankEncounter"
    
    val encounterDate = "biobankEncounterDate"
    val shortcutName = "shortcutBiobankEncounterName"
    val encounterStart = "biobankEncounterStart"
    
    val dateOfBiobankEncounterStringValue = "biobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "biobankEncounterDateDateValue"
    
    val dataset = "dateDataset"

    pattern = s"""
              		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		?$encounterStart a turbo:TURBO_0000531 .
  		?$encounterStart obo:RO_0002223 ?$baseVariableName .
  		?$encounterDate a turbo:TURBO_0000532 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .

      """

    optionalPattern = s"""
      
      ?$encounterDate turbo:TURBO_0006511 ?$dateOfBiobankEncounterDateValue .
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfBiobankEncounterStringValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

        """

    optionalLinks = Map(
        "BMI" -> BiobankEncounterBMI, "Height" -> BiobankEncounterHeight, "Weight" -> BiobankEncounterWeight
    )

    mandatoryLinks = Map(
        "Identifier" -> BiobankEncounterIdentifier
    )
    
    namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    variablesToSelect = Array(baseVariableName)
}