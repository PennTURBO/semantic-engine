package edu.upenn.turbo

class BiobankEncounterIdentifier extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterIdentifier.pattern
    val baseVariableName = BiobankEncounterIdentifier.baseVariableName
    val typeURI = BiobankEncounterIdentifier.typeURI
    val variablesToSelect = BiobankEncounterIdentifier.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterIdentifier extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): BiobankEncounterIdentifier =
    {
        new BiobankEncounterIdentifier(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000533"""
    
    val baseVariableName = "biobankEncounterCrid"
    val biobankEncounterBvn = BiobankEncounter.baseVariableName
    
    val valuesKey = "biobankEncounterIdValue"
    val registryKey = "biobankEncounterRegistry"
    
    val encounterSymbol = "biobankEncounterSymbol"
    val encounterRegistryDenoter = "biobankEncounterRegDen"
    
    val dataset = BiobankEncounter.dataset
    val datasetTitle = "biobankEncounterDatasetTitle"
    
    val instantiationKey = "instantiation"

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
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
    
    val variablesToSelect = Array(biobankEncounterBvn, valuesKey, registryKey)
}