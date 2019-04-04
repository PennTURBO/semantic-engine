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

object DateOfBirthDatum extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): DateOfBirthDatum =
    {
        new DateOfBirthDatum(optional)
    }
    
    val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004950"
    
    val baseVariableName = "dobDatum"
    val birth = "dob"
    val dateOfBirthString = "dobValue"
    val dateOfBirthDate = "dobDate"
    
    val homoSapiens = HomoSapiens.baseVariableName
    
    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
          ?$baseVariableName turbo:TURBO_0006510 ?$dateOfBirthString .
          ?$baseVariableName turbo:TURBO_0006511 ?$dateOfBirthDate .
          
          ?$baseVariableName obo:IAO_0000136 ?$birth .
          ?$homoSapiens turbo:TURBO_0000303 ?$birth .
          ?$birth a obo:UBERON_0035946 .
          
      """
    
    val variablesToSelect = Array(dateOfBirthString, dateOfBirthDate)
}