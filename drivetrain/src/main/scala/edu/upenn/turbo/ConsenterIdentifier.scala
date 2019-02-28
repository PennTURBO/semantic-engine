package edu.upenn.turbo

class ConsenterIdentifier extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = ConsenterIdentifier.pattern
    val baseVariableName = ConsenterIdentifier.baseVariableName
    val typeURI = ConsenterIdentifier.typeURI
    val variablesToSelect = ConsenterIdentifier.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object ConsenterIdentifier extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): ConsenterIdentifier =
    {
        new ConsenterIdentifier(optional)
    }
    
    val baseVariableName = "consenterCrid"
    val consenterBvn = Consenter.baseVariableName
    
    val valuesKey = "partSymbolValue"
    val registryKey = "partReg"
    val cridKey = "consenterCrid"
    
    val consenterSymbol = "partSymbol"
    val consenterRegistry = "partRegDen"
    
    val dataset = "consenterDataset"
    val datasetTitle = "datasetTitle"
   
    val instantiation = "instantiation"

    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000503 .
          ?$baseVariableName obo:IAO_0000219 ?$consenterBvn .
          ?$baseVariableName obo:BFO_0000051 ?$consenterSymbol .
          ?$consenterSymbol obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?$consenterRegistry .
          ?$consenterRegistry obo:BFO_0000050 ?$baseVariableName .
          ?$consenterSymbol a turbo:TURBO_0000504 .
          ?$consenterSymbol turbo:TURBO_0006510 ?$valuesKey .
          ?$consenterRegistry a turbo:TURBO_0000505 .
          ?$consenterRegistry obo:IAO_0000219 ?$registryKey .
          ?$registryKey a turbo:TURBO_0000506 .
          
          ?$dataset a obo:IAO_0000100 .
          ?$dataset dc11:title ?$datasetTitle .
          
          ?$consenterRegistry obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$consenterRegistry .
          ?$consenterSymbol obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$consenterSymbol .
          
          ?$instantiation a turbo:TURBO_0000522 .
          ?$instantiation obo:OBI_0000293 ?$dataset .
          
      """
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000503"""
    
    val variablesToSelect = Array(consenterBvn, valuesKey, registryKey)
}