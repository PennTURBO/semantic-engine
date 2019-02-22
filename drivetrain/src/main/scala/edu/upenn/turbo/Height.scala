package edu.upenn.turbo

class Height (encounter:Encounter) extends GraphObject
{
    val baseVariableName = "heightAssay"
    val encounterVariableName = encounter.baseVariableName
    val valuesKey = "heightValue"
    val datumKey = "heightDatum"
    
    val heightValue = "heightValue"
    
    val valueSpecification = "heightValSpec"
    
    val dataset = "dataset"
    
    val pattern = s"""
      
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
    
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      override val typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}