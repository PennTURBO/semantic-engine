package edu.upenn.turbo

class DateOfBirthDatum(consenter:Consenter) extends ExpandedGraphObject
{
    val baseVariableName = "dob"
    val birthBvn = consenter.birthVariableName
    
    val pattern = s"""
          
          ?$baseVariableName a <http://www.ebi.ac.uk/efo/EFO_0004950> .
          ?$baseVariableName :TURBO_0006510 ?dobValue .
          ?$baseVariableName :TURBO_0006511 ?dobDate .
          ?$baseVariableName obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$birthBvn .
          ?$birthBvn a obo:UBERON_0035946 .
          
      """

    val optionalPatterns = new Array[ExpandedGraphObject](0)
    
    val connections = Map(
        "" -> ""
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004950"
    
    val variablesToSelect = Array("birth", "dobString", "dobDate")

    def getValuesKey(): String = "dobString"
}