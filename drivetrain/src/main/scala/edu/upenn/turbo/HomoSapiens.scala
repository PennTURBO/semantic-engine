package edu.upenn.turbo

class HomoSapiens extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiens.pattern
    val baseVariableName = HomoSapiens.baseVariableName
    val typeURI = HomoSapiens.typeURI
    val variablesToSelect = HomoSapiens.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HomoSapiens extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiens =
    {
        new HomoSapiens(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/NCBITaxon_9606"
    
    val baseVariableName = "part"
    val shortcutName = "shortcutPartName"
    
    val height = "homoSapiensHeightSingleton"
    val weight = "homoSapiensWeightSingleton"
    val adipose = "homoSapiensAdiposeSingleton"
    
    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .          
          ?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
          
      """
    
    val variablesToSelect = Array(baseVariableName)
}