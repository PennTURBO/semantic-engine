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

object GenderIdentityDatum extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): GenderIdentityDatum =
    {
        new GenderIdentityDatum(optional)
    }
    
    val typeURI = s"""values ?genderIdentityType {
                                      obo:OMRSE_00000133 
                                      obo:OMRSE_00000138 
                                      obo:OMRSE_00000141
                                    }"""
    
    val baseVariableName = "gid"
    val homoSapiensBvn = HomoSapiens.baseVariableName
    val genderIdentityValue = "gidValue"
    val genderIdentityType = "gidType"
    val biosex = "biologicalSex"
    
    val dataset = HomoSapiens.dataset

    val pattern = s"""
          
          ?$baseVariableName turbo:TURBO_0006510 ?$genderIdentityValue .
          ?$baseVariableName a ?$genderIdentityType .
          
          ?$baseVariableName obo:IAO_0000136 ?$homoSapiensBvn .
          ?$homoSapiensBvn a obo:NCBITaxon_9606 .
          
          ?$homoSapiensBvn obo:RO_0000086 ?$biosex .
          ?$biosex a obo:PATO_0000047 .
          
          ?$dataset a obo:IAO_0000100 .
          ?$baseVariableName obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$baseVariableName .
          
      """
  
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(genderIdentityType, genderIdentityValue)
}