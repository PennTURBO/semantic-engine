package edu.upenn.turbo

class GenderIdentityDatum(consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "gid"
    val consenterBvn = consenter.baseVariableName

    val pattern = s"""
          
          ?$baseVariableName :TURBO_0006510 ?gidValue .
          ?$baseVariableName a ?gidType .
          ?$baseVariableName obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$consenterBvn .
          ?$consenterBvn a turbo:TURBO_0000502 .
          
      """

    val optionalPatterns = new Array[ExpandedGraphObject](0)
    
    val connections = Map(
      "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = """values ?gidType {obo:OMRSE_00000133 obo:OMRSE_00000138 obo:OMRSE_00000141}"""
    
    val variablesToSelect = Array("gidType", "gidValue")

    def getValuesKey(): String = "gidValue"
}