package edu.upenn.turbo

object HealthcareEncounterBMI extends GraphObject
{
    baseVariableName = "HealthcareEncounterBMI"
    val encounterDate = HealthcareEncounter.encounterDate
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "HealthcareEncounterBmiValue"
    
    val bmiValue = valuesKey
    
    val valueSpecification = "HealthcareEncounterBmiValSpec"
    val dataset = "dataset"
    
    pattern = s"""
      
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
      
      namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      typeURI = "http://www.ebi.ac.uk/efo/EFO_0004340"
      
      variablesToSelect = Array(encounterDate, valuesKey)
}