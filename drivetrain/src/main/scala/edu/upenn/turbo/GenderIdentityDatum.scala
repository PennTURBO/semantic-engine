package edu.upenn.turbo

class GenderIdentityDatum(consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "gid"
    val consenterBvn = consenter.baseVariableName
    val valuesKey = "gidValue"
    val typeKey = "gidType"

    val pattern = s"""
          
          ?$baseVariableName turbo:TURBO_0006510 ?$valuesKey .
          ?$baseVariableName a ?$typeKey .
          ?$baseVariableName obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$consenterBvn .
          ?$consenterBvn a turbo:TURBO_0000502 .
          ?dataset a obo:IAO_0000100 .
          
      """

    val optionalPatterns = new Array[ExpandedGraphObject](0)
    val mandatoryPatterns = new Array[ExpandedGraphObject](0)
    
    val connections = Map(
      "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = s"""values ?$typeKey {obo:OMRSE_00000133 obo:OMRSE_00000138 obo:OMRSE_00000141}"""
    
    val variablesToSelect = Array(typeKey, valuesKey)
}