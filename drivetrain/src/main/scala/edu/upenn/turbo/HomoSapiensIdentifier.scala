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

object HomoSapiensIdentifier extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiensIdentifier =
    {
        new HomoSapiensIdentifier(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000503"""
    
    val baseVariableName = "homoSapiensCrid"
    val homoSapiensBvn = HomoSapiens.baseVariableName

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$baseVariableName obo:IAO_0000219 ?$homoSapiensBvn .
          
      """
    
    val variablesToSelect = Array(baseVariableName)
}