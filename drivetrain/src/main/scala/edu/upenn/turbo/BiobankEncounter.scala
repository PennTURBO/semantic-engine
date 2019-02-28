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
    override val optionalPattern = BiobankEncounter.optionalPattern
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounter extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounter =
    {
        new BiobankEncounter(optional)
    }
    
    val baseVariableName = "biobankEncounter"
    
    val encounterDate = "biobankEncounterDate"
    val shortcutName = "shortcutBiobankEncounterName"
    val encounterStart = "biobankEncounterStart"
    
    val dateOfBiobankEncounterStringValue = "biobankEncounterDateStringValue"
    val dateOfBiobankEncounterDateValue = "biobankEncounterDateDateValue"
    
    val dataset = "dateDataset"

    val pattern = s"""
              		
      ?$baseVariableName a turbo:TURBO_0000527 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		?$encounterStart a turbo:TURBO_0000531 .
  		?$encounterStart obo:RO_0002223 ?$baseVariableName .
  		?$encounterDate a turbo:TURBO_0000532 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .

      """

    override val optionalPattern = s"""
      
      ?$encounterDate turbo:TURBO_0006511 ?$dateOfBiobankEncounterDateValue .
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfBiobankEncounterStringValue .
      ?$encounterDate obo:BFO_0000050 ?$dataset .
      ?$dataset obo:BFO_0000051 ?$encounterDate .

        """

    override val optionalLinks = Map(
        "BMI" -> BiobankEncounterBMI, "Height" -> BiobankEncounterHeight, "Weight" -> BiobankEncounterWeight
    )

    override val mandatoryLinks = Map(
        "Identifier" -> BiobankEncounterIdentifier
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000527"
    
    val variablesToSelect = Array(baseVariableName)
}