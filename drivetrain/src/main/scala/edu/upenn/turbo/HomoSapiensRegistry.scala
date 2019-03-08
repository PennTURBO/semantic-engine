package edu.upenn.turbo

class HomoSapiensRegistry extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiensRegistry.pattern
    val baseVariableName = HomoSapiensRegistry.baseVariableName
    val typeURI = HomoSapiensRegistry.typeURI
    val variablesToSelect = HomoSapiensRegistry.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HomoSapiensRegistry extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiensRegistry =
    {
        new HomoSapiensRegistry(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000505"""
    
    val baseVariableName = "homoSapiensRegistry"
    val valuesKey = "homoSapiensRegistryKey"
    val registryKey = valuesKey
    val homoSapiensIdentifierBvn = HomoSapiensIdentifier.baseVariableName

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$homoSapiensIdentifierBvn obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:BFO_0000050 ?$homoSapiensIdentifierBvn .
          ?$baseVariableName obo:IAO_0000219 ?$registryKey .
          ?$registryKey a turbo:TURBO_0000506 .
          
      """
    
    val variablesToSelect = Array(baseVariableName, registryKey)
}