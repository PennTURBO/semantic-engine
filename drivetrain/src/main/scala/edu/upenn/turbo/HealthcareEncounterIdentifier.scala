package edu.upenn.turbo

object HealthcareEncounterIdentifier extends GraphObject
{
    baseVariableName = "healthcareEncounterCrid"
    val healthcareEncounterBvn = HealthcareEncounter.baseVariableName
    val valuesKey = "healthcareEncounterIdValue"
    val registryKey = "healthcareEncounterRegistry"
    
    val instantiationKey = "instantiation"
    
    val encounterSymbol = "healthcareEncounterSymbol"
    val encounterRegistryDenoter = "healthcareEncounterRegDen"
    
    val dataset = "dataset"
    val datasetTitle = "datasetTitle"

    pattern = s"""
          
      ?$baseVariableName a turbo:TURBO_0000508 .
  		?$baseVariableName obo:IAO_0000219 ?$healthcareEncounterBvn .
  		?$baseVariableName obo:BFO_0000051 ?$encounterSymbol .
  		?$baseVariableName obo:BFO_0000051 ?$encounterRegistryDenoter .
  		
  		?$encounterSymbol obo:BFO_0000050 ?$baseVariableName .
  		?$encounterSymbol a turbo:TURBO_0000509 .
  		?$encounterSymbol turbo:TURBO_0006510 ?$valuesKey .
  		
  		?$encounterRegistryDenoter obo:BFO_0000050 ?$baseVariableName .
  		?$encounterRegistryDenoter a turbo:TURBO_0000510 .
  		?$encounterRegistryDenoter obo:IAO_0000219 ?$registryKey .
  		?$registryKey a turbo:TURBO_0000513  .
  		
  		?$instantiationKey a turbo:TURBO_0000522 .
      ?$instantiationKey obo:OBI_0000293 ?$dataset .
      ?$dataset a obo:IAO_0000100 .
  		?$dataset dc11:title ?$datasetTitle .
  		
  		?$dataset obo:BFO_0000051 ?$encounterSymbol .
  		?$encounterSymbol obo:BFO_0000050 ?$dataset .
  		?$dataset obo:BFO_0000051 ?$encounterRegistryDenoter .
      ?$encounterRegistryDenoter obo:BFO_0000050 ?$dataset .
          
      """
  
    namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    typeURI = """http://transformunify.org/ontologies/TURBO_0000508"""
    
    variablesToSelect = Array(healthcareEncounterBvn, valuesKey, registryKey)
}