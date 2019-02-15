package edu.upenn.turbo

class Consenter extends ExpandedGraphObject
{
    val baseVariableName = "part"
    val birthVariableName = "birth"
    val heightKey = "height"
    val weightKey = "weight"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?$baseVariableName obo:RO_0000086 ?biosex .
          ?biosex a obo:PATO_0000047 .
          ?$baseVariableName turbo:TURBO_0000303 ?$birthVariableName .
          ?$birthVariableName a obo:UBERON_0035946 .
          ?$baseVariableName obo:RO_0000086 ?$heightKey .
          ?$heightKey a obo:PATO_0000119 .
          ?$baseVariableName obo:RO_0000086 ?$weightKey .
          ?$weightKey a obo:PATO_0000128 .
          ?$baseVariableName obo:BFO_0000051 ?adipose .
          ?adipose obo:BFO_0000050 ?$baseVariableName .
          ?adipose a obo:UBERON_0001013 .
          
          ?$baseVariableName turbo:TURBO_0006601 ?shortcutPartName .
          
      """

    val optionalPattern = """"""
          
    val mandatoryLinks: Map[String, ExpandedGraphObject] = Map(
        "Identifier" -> new ConsenterIdentifier(this)
    )

    val optionalLinks = Map(
        "GenderIdentityDatum" -> new GenderIdentityDatum(this), 
        "RaceIdentityDatum" -> new RaceIdentityDatum(this), 
        "DateOfBirthDatum" -> new DateOfBirthDatum(this)
    )
    
    val connections = Map(
        "http://transformunify.org/ontologies/OGMS_0000097" -> "http://purl.obolibrary.org/obo/RO_0000056",
        "http://transformunify.org/ontologies/TURBO_0000527" -> "http://purl.obolibrary.org/obo/RO_0000056"
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName)
}