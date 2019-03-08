package edu.upenn.turbo

class HealthcareEncounterIdentifier extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterIdentifier.pattern
    val baseVariableName = HealthcareEncounterIdentifier.baseVariableName
    val typeURI = HealthcareEncounterIdentifier.typeURI
    val variablesToSelect = HealthcareEncounterIdentifier.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterIdentifier extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounterIdentifier =
    {
        new HealthcareEncounterIdentifier(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000508"""
    
    val baseVariableName = "healthcareEncounterCrid"
    val healthcareEncounterBvn = HealthcareEncounter.baseVariableName

    val pattern = s"""
          
      ?$baseVariableName a <$typeURI> .
  		?$baseVariableName obo:IAO_0000219 ?$healthcareEncounterBvn .
          
      """
  
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(healthcareEncounterBvn)
}

