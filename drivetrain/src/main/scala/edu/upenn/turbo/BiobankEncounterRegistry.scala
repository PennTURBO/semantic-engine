package edu.upenn.turbo

class BiobankEncounterRegistry extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterRegistry.pattern
    val baseVariableName = BiobankEncounterRegistry.baseVariableName
    val typeURI = BiobankEncounterRegistry.typeURI
    val variablesToSelect = BiobankEncounterRegistry.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterRegistry extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterRegistry =
    {
        new BiobankEncounterRegistry(optional)
    }
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000535"
    
    val baseVariableName = "biobankEncounterRegistry"
    val biobankEncounterIdentifier = BiobankEncounterIdentifier.baseVariableName
    val shortcutName = "shortcutBiobankEncounterRegistry"
    val valuesKey = "biobankEncounterRegistryValue"

    val pattern = s"""
              		
          ?$biobankEncounterIdentifier obo:BFO_0000051 ?$baseVariableName  .
          ?$baseVariableName  obo:BFO_0000050 ?$biobankEncounterIdentifier .
      		?$baseVariableName  a <$typeURI> .
      		?$baseVariableName  obo:IAO_0000219 ?$valuesKey .
      		?$valuesKey a turbo:TURBO_0000543 .

      """
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(valuesKey)
}