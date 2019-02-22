package edu.upenn.turbo

class Consenter extends GraphObject
{
    val baseVariableName = "part"
    val birthVariableName = "birth"
    val heightKey = "height"
    val weightKey = "weight"
    val adiposeKey = "adipose"
    val biosexKey = "biosex"
    val shortcutName = "shortcutPartName"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?$baseVariableName obo:RO_0000086 ?$biosexKey .
          ?$biosexKey a obo:PATO_0000047 .
          ?$baseVariableName turbo:TURBO_0000303 ?$birthVariableName .
          ?$birthVariableName a obo:UBERON_0035946 .
          ?$baseVariableName obo:RO_0000086 ?$heightKey .
          ?$heightKey a obo:PATO_0000119 .
          ?$baseVariableName obo:RO_0000086 ?$weightKey .
          ?$weightKey a obo:PATO_0000128 .
          ?$baseVariableName obo:BFO_0000051 ?$adiposeKey .
          ?$adiposeKey obo:BFO_0000050 ?$baseVariableName .
          ?$adiposeKey a obo:UBERON_0001013 .
          
          ?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
          
      """

    override val mandatoryLinks: Map[String, GraphObject] = Map(
        "Identifier" -> new ConsenterIdentifier(this)
    )

    override val optionalLinks = Map(
        "GenderIdentityDatum" -> new GenderIdentityDatum(this), 
        "RaceIdentityDatum" -> new RaceIdentityDatum(this), 
        "DateOfBirthDatum" -> new DateOfBirthDatum(this)
    )

    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    override val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName)
}