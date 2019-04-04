package edu.upenn.turbo

class HealthcareEncounterRegistry extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterRegistry.pattern
    val baseVariableName = HealthcareEncounterRegistry.baseVariableName
    val typeURI = HealthcareEncounterRegistry.typeURI
    val variablesToSelect = HealthcareEncounterRegistry.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterRegistry extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounterRegistry =
    {
        new HealthcareEncounterRegistry(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000510"""
    
    val baseVariableName = "healthcareEncounterRegistry"
    val healthcareEncounterIdentifier = HealthcareEncounterIdentifier.baseVariableName
    
    val valuesKey = "healthcareEncounterRegistryValue"

    val pattern = s"""
          
      ?$healthcareEncounterIdentifier obo:BFO_0000051 ?$baseVariableName .
  		?$baseVariableName obo:BFO_0000050 ?$healthcareEncounterIdentifier .
  		?$baseVariableName a <$typeURI> .
  		?$baseVariableName obo:IAO_0000219 ?$valuesKey .
  		?$valuesKey a turbo:TURBO_0000513  .
          
      """
  
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(valuesKey)
}