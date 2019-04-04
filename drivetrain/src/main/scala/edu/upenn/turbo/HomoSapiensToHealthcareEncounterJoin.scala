package edu.upenn.turbo

class HomoSapiensToHealthcareEncounterJoin extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HomoSapiensToHealthcareEncounterJoin.pattern
    val baseVariableName = HomoSapiensToHealthcareEncounterJoin.baseVariableName
    val typeURI = HomoSapiensToHealthcareEncounterJoin.typeURI
    val variablesToSelect = HomoSapiensToHealthcareEncounterJoin.variablesToSelect
    override val optionalPatterns = HomoSapiensToHealthcareEncounterJoin.optionalPatterns
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}


object HomoSapiensToHealthcareEncounterJoin extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HomoSapiensToHealthcareEncounterJoin =
    {
        new HomoSapiensToHealthcareEncounterJoin(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/OBI_0000097"
    
    val baseVariableName = "participantUnderInvestigationRole"
    val homoSapiensName = HomoSapiens.baseVariableName
    val encounterName = HealthcareEncounter.baseVariableName
    val healthcareEncounterCrid = HealthcareEncounterIdentifier.baseVariableName
    val homoSapiensCrid = HomoSapiensIdentifier.baseVariableName
    
    val homoSapiensHeight = HomoSapiens.height
    val homoSapiensWeight = HomoSapiens.weight
    val homoSapiensAdipose = HomoSapiens.adipose
    
    val heightAssay = HealthcareEncounterHeight.baseVariableName
    val heightDatum = HealthcareEncounterHeight.datumKey
    val weightAssay = HealthcareEncounterWeight.baseVariableName
    val weightDatum = HealthcareEncounterWeight.datumKey
    val BMI = HealthcareEncounterBMI.baseVariableName
    
    
    val pattern = s"""
      
          ?$homoSapiensName obo:RO_0000056 ?$encounterName .
          ?$homoSapiensName obo:RO_0000087 ?$baseVariableName .
      		?$baseVariableName a <$typeURI> .
      		?$baseVariableName obo:BFO_0000054 ?$encounterName .
      		?$healthcareEncounterCrid turbo:TURBO_0000302 ?$homoSapiensCrid .
 
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