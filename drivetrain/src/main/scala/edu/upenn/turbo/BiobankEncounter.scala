package edu.upenn.turbo

class BiobankEncounter extends Encounter
{
    val baseVariableName = "biobankEncounter"
    
    val encounterDate = "biobankEncounterDate"
    val shortcutName = "shortcutBiobankEncounterName"
    val encounterStart = "biobankEncounterStart"
    
    val dateOfBiobankEncounterStringValue = "biobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "biobankEncounterDateDateValue"
    
    val dataset = "dateDataset"

    val pattern = s"""
              		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		?$encounterStart a turbo:TURBO_0000531 .
  		?$encounterStart obo:RO_0002223 ?$baseVariableName .
  		?$encounterDate a turbo:TURBO_0000532 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .

      """

    override val optionalPattern = s"""
      
      ?$encounterDate turbo:TURBO_0006511 ?$dateOfBiobankEncounterDateValue .
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfBiobankEncounterStringValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

        """

    override val optionalLinks = Map(
        "BMI" -> new BMI(this), "Height" -> new Height(this), "Weight" -> new Weight(this)
    )

    override val mandatoryLinks: Map[String, GraphObject] = Map(
        "Identifier" -> new BiobankEncounterIdentifier(this)
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    override val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName)
}