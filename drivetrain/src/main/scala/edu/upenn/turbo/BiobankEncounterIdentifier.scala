package edu.upenn.turbo

class BiobankEncounterIdentifier (biobankEncounter:BiobankEncounter) extends GraphObject
{
    val baseVariableName = "biobankEncounterCrid"
    val biobankEncounterBvn = biobankEncounter.baseVariableName
    
    val valuesKey = "biobankEncounterIdValue"
    val registryKey = "biobankEncounterRegistry"
    
    val encounterSymbol = "biobankEncounterSymbol"
    val encounterRegistryDenoter = "biobankEncounterRegDen"
    
    val dataset = "dataset"
    val datasetTitle = "datasetTitle"
    
    val instantiationKey = "instantiation"

    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000533 .
      		?$baseVariableName obo:IAO_0000219 ?$biobankEncounterBvn .
      		?$baseVariableName obo:BFO_0000051 ?$encounterSymbol .
      		?$baseVariableName obo:BFO_0000051 ?$encounterRegistryDenoter  .
          ?$encounterSymbol obo:BFO_0000050 ?$baseVariableName .
          ?$encounterRegistryDenoter  obo:BFO_0000050 ?$baseVariableName .
      		?$encounterSymbol a turbo:TURBO_0000534 . 
      		?$encounterSymbol turbo:TURBO_0006510 ?$valuesKey .
      		?$encounterRegistryDenoter  a turbo:TURBO_0000535 .
      		?$encounterRegistryDenoter  obo:IAO_0000219 ?$registryKey .
      		?$registryKey a turbo:TURBO_0000543 .
      		
      		?$dataset a obo:IAO_0000100 .
          ?$dataset dc11:title ?$datasetTitle .
          ?$encounterRegistryDenoter  obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$encounterRegistryDenoter  .
          ?$encounterSymbol obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$encounterSymbol .
          
          ?$instantiationKey a turbo:TURBO_0000522 .
          ?$instantiationKey obo:OBI_0000293 ?$dataset .
          
      """
    
    
    

    
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    override val typeURI = """http://transformunify.org/ontologies/TURBO_0000533"""
    
    val variablesToSelect = Array(biobankEncounterBvn, valuesKey, registryKey)
}