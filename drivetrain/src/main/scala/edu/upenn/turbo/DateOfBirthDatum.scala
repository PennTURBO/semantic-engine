package edu.upenn.turbo

class DateOfBirthDatum extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = DateOfBirthDatum.pattern
    val baseVariableName = DateOfBirthDatum.baseVariableName
    val typeURI = DateOfBirthDatum.typeURI
    val variablesToSelect = DateOfBirthDatum.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object DateOfBirthDatum extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): DateOfBirthDatum =
    {
        new DateOfBirthDatum(optional)
    }
    
    val baseVariableName = "dobDatum"
    val birth = "dob"
    val dateOfBirthString = "dobValue"
    val dateOfBirthDate = "dobDate"
    
    val consenter = Consenter.baseVariableName
    
    val dataset = Consenter.dataset
    
    val pattern = s"""
          
          ?$baseVariableName a <http://www.ebi.ac.uk/efo/EFO_0004950> .
          ?$baseVariableName turbo:TURBO_0006510 ?$dateOfBirthString .
          ?$baseVariableName turbo:TURBO_0006511 ?$dateOfBirthDate .
          
          ?$baseVariableName obo:IAO_0000136 ?$birth .
          ?$consenter turbo:TURBO_0000303 ?$birth .
          ?$birth a obo:UBERON_0035946 .
          
          ?$dataset a obo:IAO_0000100 .
          ?$baseVariableName obo:BFO_0000050 ?$dataset .
          ?$dataset obo:BFO_0000051 ?$baseVariableName .
          
      """
    
    val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004950"
    
    val variablesToSelect = Array(dateOfBirthString, dateOfBirthDate)
}