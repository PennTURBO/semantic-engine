package edu.upenn.turbo

class BiobankEncounterIdentifier extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterIdentifier.pattern
    val baseVariableName = BiobankEncounterIdentifier.baseVariableName
    val typeURI = BiobankEncounterIdentifier.typeURI
    val variablesToSelect = BiobankEncounterIdentifier.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterIdentifier extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterIdentifier =
    {
        new BiobankEncounterIdentifier(optional)
    }
    
    val typeURI = """http://transformunify.org/ontologies/TURBO_0000533"""
    val baseVariableName = "biobankEncounterIdentifier"
    val biobankEncounterBvn = BiobankEncounter.baseVariableName
    
    val instantiationKey = "instantiation"

    val pattern = s"""
          
          ?$baseVariableName a <$typeURI> .
      		?$baseVariableName obo:IAO_0000219 ?$biobankEncounterBvn .
          
      """
          
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(biobankEncounterBvn)
}