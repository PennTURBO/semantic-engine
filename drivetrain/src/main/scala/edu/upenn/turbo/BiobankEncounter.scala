package edu.upenn.turbo

class BiobankEncounter extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounter.pattern
    val baseVariableName = BiobankEncounter.baseVariableName
    val typeURI = BiobankEncounter.typeURI
    val variablesToSelect = BiobankEncounter.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounter extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounter =
    {
        new BiobankEncounter(optional)
    }
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val baseVariableName = "biobankEncounter"
    
    val shortcutName = "shortcutBiobankEncounterName"

    val pattern = s"""
              		
      ?$baseVariableName a <$typeURI> .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .

      """
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val variablesToSelect = Array(baseVariableName)
}