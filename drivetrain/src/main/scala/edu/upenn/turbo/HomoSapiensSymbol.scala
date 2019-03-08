package edu.upenn.turbo

class HomoSapiensSymbol extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiensSymbol.pattern
    val baseVariableName = HomoSapiensSymbol.baseVariableName
    val typeURI = HomoSapiensSymbol.typeURI
    val variablesToSelect = HomoSapiensSymbol.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HomoSapiensSymbol extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiensSymbol =
    {
        new HomoSapiensSymbol(optional)
    }
    
    val valuesKey = "partSymbolValue"
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000504"""
    
    val baseVariableName = "homoSapiensSymbol"
    val homoSapiensIdentifierBvn = HomoSapiensIdentifier.baseVariableName
    val homoSapiensSymbolValue = valuesKey

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$homoSapiensIdentifierBvn obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000050 ?$homoSapiensIdentifierBvn .
          ?$baseVariableName turbo:TURBO_0006510 ?$homoSapiensSymbolValue .
          
      """
    
    val variablesToSelect = Array(baseVariableName, valuesKey)
}