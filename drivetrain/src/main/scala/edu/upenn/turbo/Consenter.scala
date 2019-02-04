package edu.upenn.turbo

class Consenter extends GraphObject
{
    
    val pattern = """
          
          ?part a turbo:TURBO_0000502 .
          ?part obo:RO_0000086 ?biosex .
          ?biosex a obo:PATO_0000047 .
          ?part turbo:TURBO_0000303 ?birth .
          ?birth a obo:UBERON_0035946 .
          ?part obo:RO_0000086 ?height .
          ?height a obo:PATO_0000119 .
          ?part obo:RO_0000086 ?weight .
          ?weight a obo:PATO_0000128 .
          ?part obo:BFO_0000051 ?adipose .
          ?adipose a obo:UBERON_0001013 .
          
          ?dob a efo:EFO_0004950 .
          ?dob obo:IAO_0000136 ?birth .
          
          ?partCrid a turbo:TURBO_0000503 .
          ?partCrid obo:IAO_0000219 ?part .
          ?partCrid obo:BFO_0000051 ?partSymbol .
          ?partCrid obo:BFO_0000051 ?partRegDen .
          ?partSymbol a turbo:TURBO_0000504 .
          ?partSymbol turbo:TURBO_0006510 ?partSymb .
          ?partRegDen a turbo:TURBO_0000505 .
          ?partRegDen obo:IAO_0000219 ?partReg .
          ?partReg a turbo:TURBO_0000506 .
          
          ?part turbo:TURBO_0006601 ?shortcutPartName .
          
      """
    
    val connections = Map(
        "http://transformunify.org/ontologies/OGMS_0000097" -> "http://purl.obolibrary.org/obo/RO_0000056",
        "http://transformunify.org/ontologies/TURBO_0000527" -> "http://purl.obolibrary.org/obo/RO_0000056"
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/expanded"
    
    val baseVariableName = "part"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array("part", "partSymb", "partReg")

    def getValuesKey(): String = "partSymb"
    def getRegistryKey(): String = "partReg"
}