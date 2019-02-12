package edu.upenn.turbo

class Consenter extends ExpandedGraphObject
{
    val baseVariableName = "part"
    val birthVariableName = "birth"
    val valuesKey = "partSymb"
    val registryKey = "partReg"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?$baseVariableName obo:RO_0000086 ?biosex .
          ?biosex a obo:PATO_0000047 .
          ?$baseVariableName turbo:TURBO_0000303 ?$birthVariableName .
          ?$birthVariableName a obo:UBERON_0035946 .
          ?$baseVariableName obo:RO_0000086 ?height .
          ?height a obo:PATO_0000119 .
          ?$baseVariableName obo:RO_0000086 ?weight .
          ?weight a obo:PATO_0000128 .
          ?$baseVariableName obo:BFO_0000051 ?adipose .
          ?adipose obo:BFO_0000050 ?$baseVariableName .
          ?adipose a obo:UBERON_0001013 .
          
          ?$baseVariableName turbo:TURBO_0006601 ?shortcutPartName .
          
      """
          
    val mandatoryPatterns: Array[ExpandedGraphObject] = Array(
        new ConsenterIdentifier(this)
    )

    val optionalPatterns = Array(
        new GenderIdentityDatum(this), new RaceIdentityDatum(this), new DateOfBirthDatum(this)
    )
    
    val connections = Map(
        "http://transformunify.org/ontologies/OGMS_0000097" -> "http://purl.obolibrary.org/obo/RO_0000056",
        "http://transformunify.org/ontologies/TURBO_0000527" -> "http://purl.obolibrary.org/obo/RO_0000056"
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName, valuesKey, registryKey)
}