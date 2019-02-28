package edu.upenn.turbo

class HealthcareEncounterBMI extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = HealthcareEncounterBMI.pattern
    val baseVariableName = HealthcareEncounterBMI.baseVariableName
    val typeURI = HealthcareEncounterBMI.typeURI
    val variablesToSelect = HealthcareEncounterBMI.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object HealthcareEncounterBMI extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): HealthcareEncounterBMI =
    {
        new HealthcareEncounterBMI(optional)
    }
    
    val baseVariableName = "HealthcareEncounterBMI"
    val encounterDate = HealthcareEncounter.encounterDate
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "HealthcareEncounterBmiValue"
    
    val bmiValue = valuesKey
    
    val valueSpecification = "HealthcareEncounterBmiValSpec"
    val dataset = HealthcareEncounter.dataset
    
    val pattern = s"""
      
        ?$baseVariableName a efo:EFO_0004340 .
    		?$baseVariableName obo:OBI_0001938 ?$valueSpecification .
    		?$valueSpecification a obo:OBI_0001933 .
    		?$valueSpecification obo:OBI_0002135 ?$valuesKey .
    		?$baseVariableName obo:IAO_0000581 ?$encounterDate .
    		
        ?$encounterVariableName obo:OBI_0000299 ?$baseVariableName .
    		?$dataset obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$dataset .
        ?$dataset a obo:IAO_0000100 .
    		
      """
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004340"
      
      val variablesToSelect = Array(encounterDate, valuesKey)
}