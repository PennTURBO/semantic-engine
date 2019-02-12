package edu.upenn.turbo

class BiobankEncounter extends Encounter
{
    val baseVariableName = "biobankEncounter"
    val encounterDateVariableName = "biobankEncounterDate"

    val pattern = s"""
          
      ?instantiation a turbo:TURBO_0000522 .
  		?instantiation obo:OBI_0000293 ?dataset .
  		?dataset a obo:IAO_0000100 .
  		?dataset dc11:title ?dsTitle .
        		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?shortcutbiobankEncounterName .
  		
  		?biobankEncounterStart a turbo:TURBO_0000531 .
  		?biobankEncounterStart obo:RO_0002223 ?$baseVariableName .
  		?$encounterDateVariableName a turbo:TURBO_0000532 .
  		?$encounterDateVariableName obo:IAO_0000136 ?biobankEncounterStart .
      """

    val optionalPatterns = Array(
        new BMI(this), new Height(this), new Weight(this)
    )

    val mandatoryPatterns: Array[ExpandedGraphObject] = Array(
        new BiobankEncounterIdentifier(this)
    )
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/expanded"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName)
}