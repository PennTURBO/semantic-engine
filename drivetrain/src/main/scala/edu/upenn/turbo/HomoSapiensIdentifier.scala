package edu.upenn.turbo

class HomoSapiensIdentifier extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiensIdentifier.pattern
    val baseVariableName = HomoSapiensIdentifier.baseVariableName
    val typeURI = HomoSapiensIdentifier.typeURI
    val variablesToSelect = HomoSapiensIdentifier.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HomoSapiensIdentifier extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): HomoSapiensIdentifier =
    {
        new HomoSapiensIdentifier(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000503"""
    
    val baseVariableName = "homoSapiensCrid"
    val homoSapiensBvn = HomoSapiens.baseVariableName
    
    val valuesKey = "partSymbolValue"
    val registryKey = "partReg"
    val cridKey = "homoSapiensCrid"
    
    val homoSapiensSymbol = "partSymbol"
    val homoSapiensRegistry = "partRegDen"
    
    val dataset = HomoSapiens.dataset
    val datasetTitle = "homoSapiensIdentifierDatasetTitle"
   
    val instantiation = "instantiation"

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$baseVariableName obo:IAO_0000219 ?$homoSapiensBvn .
          ?$baseVariableName obo:BFO_0000051 ?$homoSapiensSymbol .
          ?$homoSapiensSymbol obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$homoSapiensRegistry .
          ?$homoSapiensRegistry obo:BFO_0000050 ?$baseVariableName .
          ?$homoSapiensSymbol a turbo:TURBO_0000504 .
          ?$homoSapiensSymbol turbo:TURBO_0006510 ?$valuesKey .
          ?$homoSapiensRegistry a turbo:TURBO_0000505 .
          ?$homoSapiensRegistry obo:IAO_0000219 ?$registryKey .
          ?$registryKey a turbo:TURBO_0000506 .
          
          ?$dataset a obo:IAO_0000100 .
          ?$dataset dc11:title ?$datasetTitle .
          
          ?$homoSapiensRegistry obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$homoSapiensRegistry .
          ?$homoSapiensSymbol obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$homoSapiensSymbol .
          
          ?$instantiation a turbo:TURBO_0000522 .
          ?$instantiation obo:OBI_0000293 ?$dataset .
          
      """
    
    val variablesToSelect = Array(homoSapiensBvn, valuesKey, registryKey)
}