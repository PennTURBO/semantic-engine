package edu.upenn.turbo

class ConsenterIdentifier (consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "consenterCrid"
    val consenterBvn = consenter.baseVariableName
    val valuesKey = "partSymbolValue"
    val registryKey = "partReg"

    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000503 .
          ?$baseVariableName obo:IAO_0000219 ?$consenterBvn .
          ?$baseVariableName obo:BFO_0000051 ?partSymbol .
          ?partSymbol obo:BFO_0000050 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000051 ?partRegDen .
          ?partRegDen obo:BFO_0000050 ?$baseVariableName .
          ?partSymbol a turbo:TURBO_0000504 .
          ?partSymbol turbo:TURBO_0006510 ?$valuesKey .
          ?partRegDen a turbo:TURBO_0000505 .
          ?partRegDen obo:IAO_0000219 ?$registryKey .
          ?$registryKey a turbo:TURBO_0000506 .
          
          ?dataset a obo:IAO_0000100 .
          ?dataset dc11:title ?datasetTitle .
          
          ?partRegDen obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?partRegDen .
          ?partSymbol obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?partSymbol .
          
          ?instantiation a turbo:TURBO_0000522 .
          ?instantiation obo:OBI_0000293 ?dataset .
          
      """
      
    val optionalPatterns = new Array[ExpandedGraphObject](0)
    val mandatoryPatterns = new Array[ExpandedGraphObject](0)

    val connections = Map(
      "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000503"""
    
    val variablesToSelect = Array(consenterBvn, valuesKey, registryKey)
}