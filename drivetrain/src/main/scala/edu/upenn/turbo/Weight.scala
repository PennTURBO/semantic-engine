package edu.upenn.turbo

class Weight (encounter:Encounter) extends ExpandedGraphObject
{
    val baseVariableName = "weightAssay"
    val encounterVariableName = encounter.baseVariableName
    val valuesKey = "weightValue"
    
    val pattern = s"""
      
        ?$baseVariableName a obo:OBI_0000445 ;
  	                 obo:OBI_0000299 ?weightDatum .

  	    ?weightDatum a obo:IAO_0000414 ;
  	                 obo:OBI_0001938 ?weightValSpec .

  	    ?weightValSpec a obo:OBI_0001931 ;
  	                   obo:IAO_0000039 obo:UO_0000009 ;
  	                   obo:OBI_0002135 ?$valuesKey .
  	                  
  	    ?$encounterVariableName obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$encounterVariableName .
        
        ?dataset obo:BFO_0000051 ?weightDatum .
        ?weightDatum obo:BFO_0000050 ?dataset .
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