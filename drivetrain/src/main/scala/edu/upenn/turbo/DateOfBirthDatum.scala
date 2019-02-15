package edu.upenn.turbo

class DateOfBirthDatum(consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "dob"
    val birthBvn = consenter.birthVariableName
    val valuesKey = "dobValue"
    val dateKey = "dobDate"
    
    val pattern = s"""
          
          ?$baseVariableName a <http://www.ebi.ac.uk/efo/EFO_0004950> .
          ?$baseVariableName turbo:TURBO_0006510 ?$valuesKey .
          ?$baseVariableName turbo:TURBO_0006511 ?$dateKey .
          ?$baseVariableName obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$birthBvn .
          ?$birthBvn a obo:UBERON_0035946 .
          ?dataset a obo:IAO_0000100 .
          
      """

    val optionalPattern = """"""
    val optionalLinks: Map[String, ExpandedGraphObject] = Map()
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004950"
    
    val variablesToSelect = Array(birthBvn, valuesKey, dateKey)
}