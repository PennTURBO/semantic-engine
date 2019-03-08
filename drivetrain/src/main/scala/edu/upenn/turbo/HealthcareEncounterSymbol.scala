package edu.upenn.turbo

class HealthcareEncounterSymbol extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterSymbol.pattern
    val baseVariableName = HealthcareEncounterSymbol.baseVariableName
    val typeURI = HealthcareEncounterSymbol.typeURI
    val variablesToSelect = HealthcareEncounterSymbol.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterSymbol extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounterSymbol =
    {
        new HealthcareEncounterSymbol(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000509"""
    
    val baseVariableName = "healthcareEncounterSymbol"
    val healthcareEncounterIdentifier = HealthcareEncounterIdentifier.baseVariableName
    
    val valuesKey = "healthcareEncounterSymbolValue"

    val pattern = s"""
          
      ?$healthcareEncounterIdentifier obo:BFO_0000051 ?$baseVariableName .
  		?$baseVariableName obo:BFO_0000050 ?$healthcareEncounterIdentifier .
  		?$baseVariableName a <$typeURI> .
  		?$baseVariableName turbo:TURBO_0006510 ?$valuesKey .
          
      """
  
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(valuesKey)
}