package edu.upenn.turbo

class Consenter extends ExpandedGraphObject
{
    val baseVariableName = "part"
    val birthVariableName = "birth"
    
    val pattern = s"""

          ?instantiation a turbo:TURBO_0000522 .
          ?instantiation obo:OBI_0000293 ?dataset .
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?$baseVariableName obo:RO_0000086 ?biosex .
          ?biosex a obo:PATO_0000047 .
          ?$baseVariableName turbo:TURBO_0000303 ?birth .
          ?birth a obo:UBERON_0035946 .
          ?$baseVariableName obo:RO_0000086 ?height .
          ?height a obo:PATO_0000119 .
          ?$baseVariableName obo:RO_0000086 ?weight .
          ?weight a obo:PATO_0000128 .
          ?$baseVariableName obo:BFO_0000051 ?adipose .
          ?adipose obo:BFO_0000050 ?$baseVariableName .
          ?adipose a obo:UBERON_0001013 .
          
          ?partCrid a turbo:TURBO_0000503 .
          ?partCrid obo:IAO_0000219 ?$baseVariableName .
          ?partCrid obo:BFO_0000051 ?partSymbol .
          ?partSymbol obo:BFO_0000050 ?partCrid .
          ?partCrid obo:BFO_0000051 ?partRegDen .
          ?partRegDen obo:BFO_0000050 ?partCrid .
          ?partSymbol a turbo:TURBO_0000504 .
          ?partSymbol turbo:TURBO_0006510 ?partSymbolValue .
          ?partRegDen a turbo:TURBO_0000505 .
          ?partRegDen obo:IAO_0000219 ?partReg .
          ?partReg a turbo:TURBO_0000506 .
          
          ?$baseVariableName turbo:TURBO_0006601 ?shortcutPartName .

          ?dataset a obo:IAO_0000100 .
          ?dataset dc11:title ?datasetTitle .
          
          ?partRegDen obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?partRegDen .
          ?partSymbol obo:BFO_0000050 ?dataset .
          ?dataset obo:BFO_0000051 ?partSymbol .
          
      """

    val optionalPatterns = Array(
        new GenderIdentityDatum(this), new RaceIdentityDatum(this), new DateOfBirthDatum(this)
    )
    
    val connections = Map(
        "http://transformunify.org/ontologies/OGMS_0000097" -> "http://purl.obolibrary.org/obo/RO_0000056",
        "http://transformunify.org/ontologies/TURBO_0000527" -> "http://purl.obolibrary.org/obo/RO_0000056"
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array("part", "partSymb", "partReg")

    def getValuesKey(): String = "partSymb"
    def getRegistryKey(): String = "partReg"
}