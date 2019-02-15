package edu.upenn.turbo

class BiobankEncounterIdentifier (biobankEncounter:BiobankEncounter) extends ExpandedGraphObject
{
    val baseVariableName = "biobankEncounterCrid"
    val biobankEncounterBvn = biobankEncounter.baseVariableName
    val valuesKey = "biobankEncounterIdValue"
    val registryKey = "biobankEncounterRegistry"

    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000533 .
      		?$baseVariableName obo:IAO_0000219 ?$biobankEncounterBvn .
      		?$baseVariableName obo:BFO_0000051 ?biobankEncounterSymbol .
      		?$baseVariableName obo:BFO_0000051 ?biobankEncounterRegDen .
          ?biobankEncounterSymbol obo:BFO_0000050 ?$baseVariableName .
          ?biobankEncounterRegDen obo:BFO_0000050 ?$baseVariableName .
      		?biobankEncounterSymbol a turbo:TURBO_0000534 . 
      		?biobankEncounterSymbol turbo:TURBO_0006510 ?$valuesKey .
      		?biobankEncounterRegDen a turbo:TURBO_0000535 .
      		?biobankEncounterRegDen obo:IAO_0000219 ?$registryKey .
      		?$registryKey a turbo:TURBO_0000543 .
      		
      		?dataset a obo:IAO_0000100 .
          ?dataset dc11:title ?datasetTitle .
          ?biobankEncounterRegDen obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?biobankEncounterRegDen .
          ?biobankEncounterSymbol obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?biobankEncounterSymbol .
          
          ?instantiation a turbo:TURBO_0000522 .
          ?instantiation obo:OBI_0000293 ?dataset .
          
      """
    val optionalPattern = """"""
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()

    val connections = Map(
      "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000533"""
    
    val variablesToSelect = Array(biobankEncounterBvn, valuesKey, registryKey)
}