package edu.upenn.turbo

class HealthcareEncounterIdentifier (healthcareEncounter:HealthcareEncounter) extends ExpandedGraphObject
{
    val baseVariableName = "healthcareEncounterCrid"
    val healthcareEncounterBvn = healthcareEncounter.baseVariableName
    val valuesKey = "healthcareEncounterIdValue"
    val registryKey = "healthcareEncounterRegistry"

    val pattern = s"""
          
      ?$baseVariableName a turbo:TURBO_0000508 .
  		?$baseVariableName obo:IAO_0000219 ?$healthcareEncounterBvn .
  		?$baseVariableName obo:BFO_0000051 ?healthcareEncounterSymbol .
  		?$baseVariableName obo:BFO_0000051 ?healthcareEncounterRegistryDenoter .
  		
  		?healthcareEncounterSymbol obo:BFO_0000050 ?$baseVariableName .
  		?healthcareEncounterSymbol a turbo:TURBO_0000509 .
  		?healthcareEncounterSymbol turbo:TURBO_0006510 ?$valuesKey .
  		
  		?healthcareEncounterRegistryDenoter obo:BFO_0000050 ?$baseVariableName .
  		?healthcareEncounterRegistryDenoter a turbo:TURBO_0000510 .
  		?healthcareEncounterRegistryDenoter obo:IAO_0000219 ?$registryKey .
  		?$registryKey a turbo:TURBO_0000513  .
  		
  		?instantiation a turbo:TURBO_0000522 .
      ?instantiation obo:OBI_0000293 ?dataset .
      ?dataset a obo:IAO_0000100 .
  		?dataset dc11:title ?datasetTitle .
  		
  		?dataset obo:BFO_0000051 ?healthcareEncounterSymbol .
  		?healthcareEncounterSymbol obo:BFO_0000050 ?dataset .
  		?dataset obo:BFO_0000051 ?healthcareEncounterRegistryDenoter .
      ?healthcareEncounterRegistryDenoter obo:BFO_0000050 ?dataset .
          
      """
    val optionalPattern = """"""
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
    
    val connections = Map(
      "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000508"""
    
    val variablesToSelect = Array(healthcareEncounterBvn, valuesKey, registryKey)
}