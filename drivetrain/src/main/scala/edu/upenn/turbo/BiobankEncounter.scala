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
    override val optionalLinks = BiobankEncounter.optionalLinks
    override val mandatoryLinks = BiobankEncounter.mandatoryLinks
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounter extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): BiobankEncounter =
    {
        new BiobankEncounter(optional)
    }
    
    val baseVariableName = "biobankEncounter"
    
    val shortcutName = "shortcutBiobankEncounterName"
    
    val dataset = "biobankEncounterDataset"

    val pattern = s"""
              		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .

      """

    override val optionalLinks = Map(
        "BMI" -> BiobankEncounterBMI, "Height" -> BiobankEncounterHeight, "Weight" -> BiobankEncounterWeight, "Date" -> BiobankEncounterDate
    )

    override val mandatoryLinks = Map(
        "Identifier" -> BiobankEncounterIdentifier
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName)
}