package edu.upenn.turbo

class AdiposeToBiobankEncounterBMI extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = AdiposeToBiobankEncounterBMI.pattern
    val baseVariableName = AdiposeToBiobankEncounterBMI.baseVariableName
    val typeURI = AdiposeToBiobankEncounterBMI.typeURI
    val variablesToSelect = AdiposeToBiobankEncounterBMI.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}


object AdiposeToBiobankEncounterBMI extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): AdiposeToBiobankEncounterBMI =
    {
        new AdiposeToBiobankEncounterBMI(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/UBERON_0001013"
    
    val baseVariableName = HomoSapiens.adipose
    val homoSapiensName = HomoSapiens.baseVariableName
    
    val homoSapiensAdipose = AdiposeToBiobankEncounterBMI.baseVariableName
    
    val BMI = BiobankEncounterBMI.baseVariableName
    
    
    val pattern = s"""
      
          ?$homoSapiensName obo:BFO_0000051 ?$homoSapiensAdipose .
          ?$homoSapiensAdipose obo:BFO_0000050 ?$homoSapiensName .
          ?$homoSapiensAdipose a <$typeURI> .
          ?$homoSapiensAdipose obo:IAO_0000136 ?$BMI .
          
          ?$homoSapiensName a <$HomoSapiens.typeURI> .
          ?$BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
      
      """
        
    val variablesToSelect = Array(homoSapiensName, BMI)
}