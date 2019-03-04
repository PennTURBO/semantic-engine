package edu.upenn.turbo

class HomoSapiensToBiobankEncounterJoin extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiensToBiobankEncounterJoin.pattern
    val baseVariableName = HomoSapiensToBiobankEncounterJoin.baseVariableName
    val typeURI = HomoSapiensToBiobankEncounterJoin.typeURI
    val variablesToSelect = HomoSapiensToBiobankEncounterJoin.variablesToSelect
    override val optionalPatterns = HomoSapiensToBiobankEncounterJoin.optionalPatterns
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}


object HomoSapiensToBiobankEncounterJoin extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiensToBiobankEncounterJoin =
    {
        new HomoSapiensToBiobankEncounterJoin(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/OBI_0000097"
    
    val baseVariableName = "participantUnderInvestigationRole"
    val homoSapiensName = HomoSapiens.baseVariableName
    val encounterName = BiobankEncounter.baseVariableName
    val biobankEncounterCrid = BiobankEncounterIdentifier.baseVariableName
    val homoSapiensCrid = HomoSapiensIdentifier.baseVariableName
    
    val homoSapiensHeight = HomoSapiens.height
    val homoSapiensWeight = HomoSapiens.weight
    val homoSapiensAdipose = HomoSapiens.adipose
    
    val heightAssay = BiobankEncounterHeight.baseVariableName
    val heightDatum = BiobankEncounterHeight.datumKey
    val weightAssay = BiobankEncounterWeight.baseVariableName
    val weightDatum = BiobankEncounterWeight.datumKey
    val BMI = BiobankEncounterBMI.baseVariableName
    
    
    val pattern = s"""
      
          ?$homoSapiensName obo:RO_0000056 ?$encounterName .
          ?$homoSapiensName obo:RO_0000087 ?$baseVariableName .
      		?$baseVariableName a <$typeURI> .
      		?$baseVariableName obo:BFO_0000054 ?$encounterName .
      		?$biobankEncounterCrid turbo:TURBO_0000302 ?$homoSapiensCrid .
      
      """
          
    override val optionalPatterns: Array[String] = Array(
          s"""
          ?$homoSapiensName obo:BFO_0000051 ?$homoSapiensAdipose .
          ?$homoSapiensAdipose obo:BFO_0000050 ?$homoSapiensName .
          ?$homoSapiensAdipose a obo:UBERON_0001013 .
          ?$homoSapiensAdipose obo:IAO_0000136 ?$BMI .
          """,
          
          s"""
          ?$homoSapiensName obo:RO_0000086 ?$homoSapiensHeight .
          ?$homoSapiensHeight a obo:PATO_0000119 .
          ?$heightDatum obo:IAO_0000136 ?$homoSapiensName.
          ?$heightDatum obo:IAO_0000221 ?$homoSapiensHeight .
          ?$heightAssay obo:OBI_0000293 ?$homoSapiensName.
          """,
          
          s"""
          ?$weightDatum obo:IAO_0000136 ?$homoSapiensName.
      		?$weightDatum obo:IAO_0000221 ?$homoSapiensWeight .
      		?$weightAssay obo:OBI_0000293 ?$homoSapiensName.
      		?$homoSapiensName obo:RO_0000086 ?$homoSapiensWeight .
          ?$homoSapiensWeight a obo:PATO_0000128 .
          """
          
        )
    
    val variablesToSelect = Array(homoSapiensName, encounterName)
}