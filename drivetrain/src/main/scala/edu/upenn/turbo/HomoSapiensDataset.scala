package edu.upenn.turbo

class HomoSapiensDataset extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiensDataset.pattern
    val baseVariableName = HomoSapiensDataset.baseVariableName
    val typeURI = HomoSapiensDataset.typeURI
    val variablesToSelect = HomoSapiensDataset.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HomoSapiensDataset extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiensDataset =
    {
        new HomoSapiensDataset(optional)
    }
    
    val typeURI = """http://purl.obolibrary.org/obo/IAO_0000100"""
    
    val baseVariableName = "homoSapiensDataset"
    val datasetTitle = "homoSapiensDatasetTitle"
    
    val homoSapiensIdentifierBvn = HomoSapiensIdentifier.baseVariableName
    val homoSapiensRegistry = HomoSapiensRegistry.baseVariableName
    val homoSapiensSymbol = HomoSapiensSymbol.baseVariableName
    val genderIdentiyDatum = GenderIdentityDatum.baseVariableName
    val raceIdentityDatum = RaceIdentityDatum.baseVariableName
    val dateOfbirthDatum = DateOfBirthDatum.baseVariableName
    
    val instantiation = "instantiation"

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$baseVariableName dc11:title ?$datasetTitle .
          
          ?$instantiation a turbo:TURBO_0000522 .
          ?$instantiation obo:OBI_0000293 ?$baseVariableName .
          
          ?$homoSapiensRegistry obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$homoSapiensRegistry .
          
          ?$homoSapiensSymbol obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$homoSapiensSymbol .
          
          ?$genderIdentiyDatum obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$genderIdentiyDatum .
          
          ?$raceIdentityDatum obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$raceIdentityDatum .
          
          ?$dateOfbirthDatum obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$dateOfbirthDatum .
          
      """
    
    val variablesToSelect = Array(baseVariableName, instantiation, datasetTitle)
}