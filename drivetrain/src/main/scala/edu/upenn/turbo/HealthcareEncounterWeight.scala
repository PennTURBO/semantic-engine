package edu.upenn.turbo

object HealthcareEncounterWeight extends ExpandedGraphObjectSingleton
{
    baseVariableName = "HealthcareEncounterWeightAssay"
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "HealthcareEncounterWeightValue"
    val datumKey = "HealthcareEncounterWeightDatum"
    
    val weightValue = valuesKey
    
    val valueSpecification = "HealthcareEncounterWeightValSpec"
    
    val dataset = "dataset"
    
    pattern = s"""
      
        ?$baseVariableName a obo:OBI_0000445 ;
  	                 obo:OBI_0000299 ?$datumKey.

  	    ?$datumKey a obo:IAO_0000414 ;
  	                 obo:OBI_0001938 ?$valueSpecification .

  	    ?$valueSpecification a obo:OBI_0001931 ;
  	                   obo:IAO_0000039 obo:UO_0000009 ;
  	                   obo:OBI_0002135 ?$valuesKey .
  	                  
  	    ?$encounterVariableName obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$encounterVariableName .
        
        ?$dataset obo:BFO_0000051 ?$datumKey.
        ?$datumKey obo:BFO_0000050 ?$dataset .
        ?$dataset a obo:IAO_0000100 .
    		
      """
      
      namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      variablesToSelect = Array(encounterVariableName, valuesKey)
}