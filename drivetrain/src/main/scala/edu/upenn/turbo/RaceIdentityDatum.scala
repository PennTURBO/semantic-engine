package edu.upenn.turbo

class RaceIdentityDatum(consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "rid"
    val consenterBvn = consenter.baseVariableName
    val raceIdentityValue = "ridValue"
    val raceIdentityType = "ridType"
    
    val dataset = "raceDataset"
    val raceIdentificationProcess = "raceIdentificationProcess"

    val pattern = s"""
          
          ?$raceIdentificationProcess a obo:OMRSE_00000099 .
          ?$raceIdentificationProcess obo:OBI_0000299 ?$baseVariableName .
          ?$baseVariableName turbo:TURBO_0006512 ?$raceIdentityValue .
          ?$baseVariableName a ?$raceIdentityType .
          ?$baseVariableName obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$consenterBvn .
          ?$consenterBvn a turbo:TURBO_0000502 .
          ?$dataset a obo:IAO_0000100 .
          
      """
    val optionalPattern = """"""
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()

    val connections = Map(
      "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = s"""values ?raceIdentityType {}"""
    
    val variablesToSelect = Array(consenterBvn, raceIdentityType, raceIdentityValue)
}