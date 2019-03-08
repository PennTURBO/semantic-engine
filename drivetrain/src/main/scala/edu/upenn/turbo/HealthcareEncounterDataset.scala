package edu.upenn.turbo

class HealthcareEncounterDataset extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterDataset.pattern
    val baseVariableName = HealthcareEncounterDataset.baseVariableName
    val typeURI = HealthcareEncounterDataset.typeURI
    val variablesToSelect = HealthcareEncounterDataset.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterDataset extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounterDataset =
    {
        new HealthcareEncounterDataset(optional)
    }
    
    val typeURI = """http://purl.obolibrary.org/obo/IAO_0000100"""
    
    val baseVariableName = "healthcareEncounterDataset"
    val datasetTitle = "healthcareEncounterDatasetTitle"
    val instantiation = "instantiation"
    
    val healthcareEncounterSymbol = HealthcareEncounterSymbol.baseVariableName
    val healthcareEncounterRegistry = HealthcareEncounterRegistry.baseVariableName
    val healthcareEncounterBMI = HealthcareEncounterBMI.baseVariableName
    val healthcareEncounterDate = HealthcareEncounterDate.baseVariableName
    val healthcareEncounterHeight = HealthcareEncounterHeight.datumKey
    val healthcareEncounterWeight = HealthcareEncounterWeight.datumKey

    val pattern = s"""
          
          ?$instantiation a turbo:TURBO_0000522 .
          ?$instantiation obo:OBI_0000293 ?$baseVariableName .
          
          ?$baseVariableName a <$typeURI> .
      		?$baseVariableName dc11:title ?$datasetTitle .
      		
      		?$baseVariableName obo:BFO_0000051 ?$healthcareEncounterSymbol .
      		?$healthcareEncounterSymbol obo:BFO_0000050 ?$baseVariableName .
      		
      		?$baseVariableName obo:BFO_0000051 ?$healthcareEncounterRegistry .
          ?$healthcareEncounterRegistry obo:BFO_0000050 ?$baseVariableName .
          
          ?$baseVariableName obo:BFO_0000051 ?$healthcareEncounterDate .
          ?$healthcareEncounterDate obo:BFO_0000050 ?$baseVariableName .
          
          ?$baseVariableName obo:BFO_0000051 ?$healthcareEncounterHeight .
          ?$healthcareEncounterHeight obo:BFO_0000050 ?$baseVariableName .
          
          ?$baseVariableName obo:BFO_0000051 ?$healthcareEncounterWeight .
          ?$healthcareEncounterWeight obo:BFO_0000050 ?$baseVariableName .
          
          ?$baseVariableName obo:BFO_0000051 ?$healthcareEncounterBMI .
          ?$healthcareEncounterBMI obo:BFO_0000050 ?$baseVariableName .
          
      """
  
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(datasetTitle)
}