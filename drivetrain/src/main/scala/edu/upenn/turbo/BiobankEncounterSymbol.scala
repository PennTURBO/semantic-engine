package edu.upenn.turbo

class BiobankEncounterSymbol extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterSymbol.pattern
    val baseVariableName = BiobankEncounterSymbol.baseVariableName
    val typeURI = BiobankEncounterSymbol.typeURI
    val variablesToSelect = BiobankEncounterSymbol.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterSymbol extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterSymbol =
    {
        new BiobankEncounterSymbol(optional)
    }
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000534"
    
    val baseVariableName = "biobankEncounterSymbol"
    val biobankEncounterIdentifier = BiobankEncounterIdentifier.baseVariableName
    val shortcutName = "shortcutBiobankEncounterSymbol"
    val valuesKey = "biobankEncounterSymbolValue"

    val pattern = s"""
              		
      ?$biobankEncounterIdentifier obo:BFO_0000051 ?$baseVariableName .
      ?$baseVariableName obo:BFO_0000050 ?$biobankEncounterIdentifier .
      ?$baseVariableName a <$typeURI> . 
      ?$baseVariableName turbo:TURBO_0006510 ?$valuesKey .

      """
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(valuesKey)
}