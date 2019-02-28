package edu.upenn.turbo

class GenderIdentityDatum extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = GenderIdentityDatum.pattern
    val baseVariableName = GenderIdentityDatum.baseVariableName
    val typeURI = GenderIdentityDatum.typeURI
    val variablesToSelect = GenderIdentityDatum.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object GenderIdentityDatum extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): GenderIdentityDatum =
    {
        new GenderIdentityDatum(optional)
    }
    
    val baseVariableName = "gid"
    val consenterBvn = Consenter.baseVariableName
    val genderIdentityValue = "gidValue"
    val genderIdentityType = "gidType"
    
    val dataset = "genderDataset"

    val pattern = s"""
          
          ?$baseVariableName turbo:TURBO_0006510 ?$genderIdentityValue .
          ?$baseVariableName a ?$genderIdentityType .
          ?$baseVariableName obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$consenterBvn .
          ?$consenterBvn a turbo:TURBO_0000502 .
          ?$dataset a obo:IAO_0000100 .
          
      """
  
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = s"""values ?genderIdentityType {
                                      obo:OMRSE_00000133 
                                      obo:OMRSE_00000138 
                                      obo:OMRSE_00000141
                                    }"""
    
    val variablesToSelect = Array(genderIdentityType, genderIdentityValue)
}