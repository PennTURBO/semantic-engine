package edu.upenn.turbo

class BiobankEncounter extends ExpandedGraphObject
{
    val baseVariableName = "biobankEncounter"

    val pattern = s"""
          
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?shortcutbiobankEncounterName .
  		?biobankEncounterCrid a turbo:TURBO_0000533 .
  		?biobankEncounterCrid obo:IAO_0000219 ?$baseVariableName .
  		?biobankEncounterCrid obo:BFO_0000051 ?biobankEncounterSymbol .
  		?biobankEncounterCrid obo:BFO_0000051 ?biobankEncounterRegDen .
  		?biobankEncounterSymbol a turbo:TURBO_0000534 . 
  		?biobankEncounterSymbol turbo:TURBO_0006510 ?biobankEncounterSymbolValue .
  		?biobankEncounterRegDen a turbo:TURBO_0000535 .
  		?biobankEncounterRegDen obo:IAO_0000219 ?biobankEncounterRegistry .
  		?biobankEncounterRegistry a turbo:TURBO_0000543 .
  		
  		?biobankEncounterStart a turbo:TURBO_0000531 .
  		?biobankEncounterStart obo:RO_0002223 ?$baseVariableName .
  		?biobankEncounterDate a turbo:TURBO_0000532 .
  		?biobankEncounterDate obo:IAO_0000136 ?biobankEncounterStart .
      """

    val optionalPatterns = new Array[ExpandedGraphObject](0)
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/expanded"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array("biobankEncounter", "biobankEncounterSymbolValue", "biobankEncounterRegistry")

    def getValuesKey(): String = "biobankEncounterSymbolValue"
    def getRegistryKey(): String = "biobankEncounterRegistry"
}