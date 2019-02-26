package edu.upenn.turbo

object BiobankEncounterBMI extends GraphObject
{
    baseVariableName = "BiobankEncounterBMI"
    val encounterDate = BiobankEncounter.encounterDate
    val encounterVariableName = BiobankEncounter.baseVariableName
    val valuesKey = "BiobankEncounterBmiValue"
    
    val bmiValue = valuesKey
    
    val valueSpecification = "BiobankEncounterBmiValSpec"
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