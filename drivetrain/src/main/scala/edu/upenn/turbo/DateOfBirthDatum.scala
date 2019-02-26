package edu.upenn.turbo

object DateOfBirthDatum extends GraphObject
{
    baseVariableName = "dob"
    val birthBvn = Consenter.birthVariableName
    val dateOfBirthString = "dobValue"
    val dateOfBirthDate = "dobDate"
    
    val dataset = "dateDataset"
    
    pattern = s"""
          
          ?$baseVariableName a <http://www.ebi.ac.uk/efo/EFO_0004950> .
          ?$baseVariableName turbo:TURBO_0006510 ?$dateOfBirthString .
          ?$baseVariableName turbo:TURBO_0006511 ?$dateOfBirthDate .
          ?$baseVariableName obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$baseVariableName .
          ?$baseVariableName obo:IAO_0000136 ?$birthBvn .
          ?$birthBvn a obo:UBERON_0035946 .
          ?$dataset a obo:IAO_0000100 .
          
      """
      
    namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    typeURI = "http://www.ebi.ac.uk/efo/EFO_0004950"
    
    variablesToSelect = Array(birthBvn, dateOfBirthString, dateOfBirthDate)
}