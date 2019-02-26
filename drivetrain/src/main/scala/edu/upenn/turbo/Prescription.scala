package edu.upenn.turbo

object Prescription extends GraphObject
{
    baseVariableName = "prescription"
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "medId"
    
    val prescriptionCrid = "medCrid"
    val medicationOrderName = "orderNameString"
    val medicationSymbol = "medSymb"
    val medicationSymbolValue = "medId"
    val mappedMedicationTerm = "medMapped"
    
    val dataset = "dataset"
    
    pattern = s"""
      
        ?$encounterVariableName obo:RO_0002234 ?$baseVariableName .
        ?$baseVariableName a obo:PDRO_0000001 .
      	?$baseVariableName turbo:TURBO_0006512 ?$medicationOrderName .
      	?$baseVariableName turbo:TURBO_0000307 ?$mappedMedicationTerm .
      	 
  	    ?$prescriptionCrid obo:IAO_0000219 ?$baseVariableName .
  	    ?$prescriptionCrid a turbo:TURBO_0000561 .
  	    ?$prescriptionCrid obo:BFO_0000051 ?$medicationSymbol .
  	    
  	    ?$medicationSymbol obo:BFO_0000050 ?$prescriptionCrid .
  	    ?$medicationSymbol a turbo:TURBO_0000562 .
  	    ?$medicationSymbol turbo:TURBO_0006510 ?$medicationSymbolValue .
  	    
  	    ?$dataset obo:BFO_0000051 ?$medicationSymbol .
    		?$medicationSymbol obo:BFO_0000050 ?$dataset .
    		?$dataset obo:BFO_0000051 ?$baseVariableName .
    		?$baseVariableName obo:BFO_0000050 ?$dataset .
    		
      """
      
      namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      typeURI = "http://purl.obolibrary.org/obo/PDRO_0000001"
      
      variablesToSelect = Array(encounterVariableName, valuesKey, "medId")
}