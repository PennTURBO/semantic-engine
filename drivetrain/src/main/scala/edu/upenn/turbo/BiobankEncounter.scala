package edu.upenn.turbo

class BiobankEncounter extends Encounter
{
    val baseVariableName = "biobankEncounter"
    val encounterDateVariableName = "biobankEncounterDate"
    val linkedConsenterSymbol = "consenterSymbol"
    val linkedConsenterRegistry = "consenterRegistry"

    val pattern = s"""
              		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?shortcutBiobankEncounterName .
  		
  		?biobankEncounterStart a turbo:TURBO_0000531 .
  		?biobankEncounterStart obo:RO_0002223 ?$baseVariableName .
  		?$encounterDateVariableName a turbo:TURBO_0000532 .
  		?$encounterDateVariableName obo:IAO_0000136 ?biobankEncounterStart .
  		
  		?$baseVariableName <http://graphBuilder.org/linksToConsenterWithSymbol> ?$linkedConsenterSymbol .
  		?$baseVariableName <http://graphBuilder.org/linksToConsenterWithSymbol> ?$linkedConsenterRegistry .

      """

    val optionalPattern = s"""
      
      ?$encounterDateVariableName turbo:TURBO_0006511 ?biobankEncounterDateDateValue .
      ?$encounterDateVariableName turbo:TURBO_0006512 ?biobankEncounterDateStringValue .
      ?$encounterDateVariableName obo:BFO_0000050 ?dateDataset .
      ?dateDataset obo:BFO_0000051 ?$encounterDateVariableName .

        """

    val optionalLinks = Map(
        "BMI" -> new BMI(this), "Height" -> new Height(this), "Weight" -> new Weight(this)
    )

    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map(
        "Identifier" -> new BiobankEncounterIdentifier(this)
    )
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName)
}