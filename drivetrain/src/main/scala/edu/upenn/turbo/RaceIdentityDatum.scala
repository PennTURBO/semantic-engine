package edu.upenn.turbo

class RaceIdentityDatum extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = RaceIdentityDatum.pattern
    val baseVariableName = RaceIdentityDatum.baseVariableName
    val typeURI = RaceIdentityDatum.typeURI
    val variablesToSelect = RaceIdentityDatum.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object RaceIdentityDatum extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): RaceIdentityDatum =
    {
        new RaceIdentityDatum(optional)
    }
    
    val typeURI = s"""values ?raceIdentityType {}"""
    
    val baseVariableName = "rid"
    val homoSapiensBvn = HomoSapiens.baseVariableName
    val raceIdentityValue = "ridValue"
    val raceIdentityType = "ridType"
    
    val raceIdentificationProcess = "raceIdentificationProcess"

    val pattern = s"""
          
          ?$raceIdentificationProcess a obo:OMRSE_00000099 .
          ?$raceIdentificationProcess obo:OBI_0000299 ?$baseVariableName .
          ?$baseVariableName turbo:TURBO_0006512 ?$raceIdentityValue .
          ?$baseVariableName a ?$raceIdentityType .
          ?$baseVariableName obo:IAO_0000136 ?$homoSapiensBvn .
          ?$homoSapiensBvn a obo:NCBITaxon_9606 .
          
      """

    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(homoSapiensBvn, raceIdentityType, raceIdentityValue)
}