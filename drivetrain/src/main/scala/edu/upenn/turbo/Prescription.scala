package edu.upenn.turbo

class Prescription (healthcareEncounter:HealthcareEncounter) extends ExpandedGraphObject
{
    val baseVariableName = "prescription"
    val encounterVariableName = healthcareEncounter.baseVariableName
    val valuesKey = "drugURI"
    
    val pattern = s"""
      
        ?$encounterVariableName obo:RO_0002234 ?$baseVariableName .
        ?$baseVariableName a obo:PDRO_0000001 .
      	?$baseVariableName turbo:TURBO_0006512 ?orderNameString .
      	?$baseVariableName turbo:TURBO_0000307 ?$valuesKey .
      	 
  	    ?medCrid obo:IAO_0000219 ?$baseVariableName .
  	    ?medCrid a turbo:TURBO_0000561 .
  	    ?medCrid obo:BFO_0000051 ?medSymb .
  	    
  	    ?medSymb obo:BFO_0000050 ?medCrid .
  	    ?medSymb a turbo:TURBO_0000562 .
  	    ?medSymb turbo:TURBO_0006510 ?medId .
  	    
  	    ?dataset obo:BFO_0000051 ?medSymb .
    		?medSymb obo:BFO_0000050 ?dataset .
    		?dataset obo:BFO_0000051 ?$baseVariableName .
    		?$baseVariableName obo:BFO_0000050 ?dataset .
    		
      """
      val optionalPattern = """"""
      val optionalLinks: Map[String, ExpandedGraphObject] = Map()
      val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
      
      val connections = Map(
          "" -> ""
      )
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val typeURI = "http://purl.obolibrary.org/obo/PDRO_0000001"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey, "medId")
}