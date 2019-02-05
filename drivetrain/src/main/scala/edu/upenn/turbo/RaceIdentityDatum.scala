package edu.upenn.turbo

class RaceIdentityDatum(consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "rid"
    val consenterBvn = consenter.baseVariableName

    val pattern = s"""
          
          ?rip a obo:OMRSE_00000099 .
          ?rip obo:OBI_0000299 ?$baseVariableName .
          ?$baseVariableName turbo:TURBO_0006512 ?ridValue .
          ?$baseVariableName a ?ridType .
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
    
    val typeURI = """values ?ridType {}"""
    
    val variablesToSelect = Array("part", "ridType", "ridValue")

    def getValuesKey(): String = "ridValue"
}