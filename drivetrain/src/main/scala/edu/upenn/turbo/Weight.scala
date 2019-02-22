package edu.upenn.turbo

class Weight (encounter:Encounter) extends GraphObject
{
    val baseVariableName = "weightAssay"
    val encounterVariableName = encounter.baseVariableName
    val valuesKey = "weightValue"
    val datumKey = "weightDatum"
    
    val weightValue = "weightValue"
    
    val valueSpecification = "weightValSpec"
    
    val dataset = "dataset"
    
    val pattern = s"""
      
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
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      override val typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}