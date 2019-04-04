package edu.upenn.turbo

class BiobankEncounterDataset extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterDataset.pattern
    val baseVariableName = BiobankEncounterDataset.baseVariableName
    val typeURI = BiobankEncounterDataset.typeURI
    val variablesToSelect = BiobankEncounterDataset.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterDataset extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterDataset =
    {
        new BiobankEncounterDataset(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/IAO_0000100"
    
    val baseVariableName = "biobankEncounterDataset"
    val valuesKey = "biobankEncounterDatasetTitle"
    val datasetTitle = valuesKey
    val instantiation = "instantiation"
    
    val biobankEncounterRegistry = BiobankEncounterRegistry.baseVariableName
    val biobankEncounterSymbol = BiobankEncounterSymbol.baseVariableName
    val biobankEncounterDate = BiobankEncounterDate.baseVariableName
    val biobankEncounterHeight = BiobankEncounterHeight.datumKey
    val biobankEncounterWeight = BiobankEncounterWeight.datumKey
    val biobankEncounterBMI = BiobankEncounterBMI.baseVariableName
    
    val pattern = s"""
      
          ?$instantiation a turbo:TURBO_0000522 .
          ?$instantiation obo:OBI_0000293 ?$baseVariableName .
              		
          ?$baseVariableName a <$typeURI> .
          ?$baseVariableName dc11:title ?$datasetTitle .
          
          ?$biobankEncounterRegistry  obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$biobankEncounterRegistry  .
          
          ?$biobankEncounterSymbol obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$biobankEncounterSymbol .
          
          ?$biobankEncounterDate obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$biobankEncounterDate .
          
          ?$biobankEncounterHeight obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$biobankEncounterHeight .
          
          ?$biobankEncounterWeight obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$biobankEncounterWeight .
          
          ?$biobankEncounterBMI obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$biobankEncounterBMI .

      """
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(datasetTitle)
}