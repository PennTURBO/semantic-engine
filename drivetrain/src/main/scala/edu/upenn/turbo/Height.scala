package edu.upenn.turbo

class Height (encounter:Encounter) extends ExpandedGraphObject
{
    val baseVariableName = "heightAssay"
    val encounterVariableName = encounter.baseVariableName
    val valuesKey = "heightValue"
    
    val pattern = s"""
      
        ?heightValSpec a obo:OBI_0001931 ;
             obo:IAO_0000039 obo:UO_0000015 ;
             obo:OBI_0002135 ?$valuesKey .
        		               
  	    ?$baseVariableName a turbo:TURBO_0001511 ;
  	         obo:OBI_0000299 ?heightDatum .

      	?heightDatum a obo:IAO_0000408 ;
      	     obo:OBI_0001938 ?heightValSpec .
      	     
      	?$encounterVariableName obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$encounterVariableName .
        
        ?dataset obo:BFO_0000051 ?heightDatum .
        ?heightDatum obo:BFO_0000050 ?dataset .
        ?dataset a obo:IAO_0000100 .
    		
      """
      val optionalPattern = """"""
      val optionalLinks = new Array[ExpandedGraphObject](0)
      val mandatoryLinks = new Array[ExpandedGraphObject](0)
      
      val connections = Map(
          "" -> ""
      )
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}