package edu.upenn.turbo

object HealthcareEncounterHeight extends ExpandedGraphObjectSingleton
{
    baseVariableName = "HealthcareEncounterHeightAssay"
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "HealthcareEncounterHeightValue"
    val datumKey = "HealthcareEncounterDatumKey"
    
    val heightValue = valuesKey
    
    val valueSpecification = "HealthcareEncounterHeightValSpec"
    
    val dataset = "dataset"
    
    pattern = s"""
      
        ?$valueSpecification a obo:OBI_0001931 ;
             obo:IAO_0000039 obo:UO_0000015 ;
             obo:OBI_0002135 ?$valuesKey .
        		               
  	    ?$baseVariableName a turbo:TURBO_0001511 ;
  	         obo:OBI_0000299 ?$datumKey .

      	?$datumKey a obo:IAO_0000408 ;
      	     obo:OBI_0001938 ?$valueSpecification .
      	     
      	?$encounterVariableName obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$encounterVariableName .
        
        ?$dataset obo:BFO_0000051 ?$datumKey .
        ?$datumKey obo:BFO_0000050 ?$dataset .
        ?$dataset a obo:IAO_0000100 .
    		
      """
    
      namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      variablesToSelect = Array(encounterVariableName, valuesKey)
}