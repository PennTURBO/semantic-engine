package edu.upenn.turbo

object Consenter extends GraphObject
{
    baseVariableName = "part"
    val birthVariableName = "birth"
    val heightKey = "height"
    val weightKey = "weight"
    val adiposeKey = "adipose"
    val biosexKey = "biosex"
    val shortcutName = "shortcutPartName"
    
    pattern = s"""
          
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

    mandatoryLinks = Map(
        "Identifier" -> ConsenterIdentifier
    )

    optionalLinks = Map(
        "GenderIdentityDatum" -> GenderIdentityDatum, 
        "RaceIdentityDatum" -> RaceIdentityDatum, 
        "DateOfBirthDatum" -> DateOfBirthDatum
    )

    namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    variablesToSelect = Array(baseVariableName)
}