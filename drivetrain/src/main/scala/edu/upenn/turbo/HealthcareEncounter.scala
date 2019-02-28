package edu.upenn.turbo

class HealthcareEncounter extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounter.pattern
    val baseVariableName = HealthcareEncounter.baseVariableName
    val typeURI = HealthcareEncounter.typeURI
    val variablesToSelect = HealthcareEncounter.variablesToSelect
    
    override val optionalPattern = HealthcareEncounter.optionalPattern
    override val optionalLinks = HealthcareEncounter.optionalLinks
    override val mandatoryLinks = HealthcareEncounter.mandatoryLinks
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounter extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounter =
    {
        new HealthcareEncounter(optional)
    }
    
    val baseVariableName = "healthcareEncounter"
    val encounterDate = "healthcareEncounterDate"
    
    val shortcutName = "shortcutHealthcareEncounterName"
    
    val encounterStart = "healthcareEncounterStart"
    val dateOfHealthcareEncounterStringValue = "healthcareEncounterDateStringValue"
    val dateOfHealthcareEncounterDateValue = "healthcareEncounterDateDateValue"
    
    val dateDataset = "dateDataset"
    val dataset = "healthcareEncounterDataset"
    
    val pattern = s"""
  	
  		?$baseVariableName a obo:OGMS_0000097 .
  		?$baseVariableName turbo:TURBO_0006601 ?$shortcutName .
  		
  		?$encounterStart a turbo:TURBO_0000511 .
  		?$encounterStart obo:RO_0002223 ?$baseVariableName .
  		            
  		?$encounterDate a turbo:TURBO_0000512 .
  		?$encounterDate obo:IAO_0000136 ?$encounterStart .

      """

    override val optionalPattern = s"""
      
      ?$encounterDate turbo:TURBO_0006512 ?$dateOfHealthcareEncounterStringValue .
  		?$encounterDate turbo:TURBO_0006511 ?$dateOfHealthcareEncounterDateValue .
      ?$encounterDate obo:BFO_0000050 ?$dateDataset .
      ?$dateDataset obo:BFO_0000051 ?$encounterDate .

        """

    override val optionalLinks = Map(
        "BMI" -> HealthcareEncounterBMI, 
        "Height" -> HealthcareEncounterHeight, 
        "Weight" -> HealthcareEncounterWeight, 
        "Diagnosis" -> Diagnosis,
        "Prescription" -> Prescription
    )

    override val mandatoryLinks = Map(
        "Identifier" -> HealthcareEncounterIdentifier
    )
    
    val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000097"
    
    val variablesToSelect = Array(baseVariableName)
}