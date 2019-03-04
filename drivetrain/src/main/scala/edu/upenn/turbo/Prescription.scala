package edu.upenn.turbo

class Prescription extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = Prescription.pattern
    val baseVariableName = Prescription.baseVariableName
    val typeURI = Prescription.typeURI
    val variablesToSelect = Prescription.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object Prescription extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): Prescription =
    {
        new Prescription(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/PDRO_0000001"
    
    val baseVariableName = "prescription"
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "medId"
    
    val prescriptionCrid = "medCrid"
    val medicationOrderName = "orderNameString"
    val medicationSymbol = "medSymb"
    val medicationSymbolValue = "medId"
    val mappedMedicationTerm = "medMapped"
    
    val dataset = HealthcareEncounter.dataset
    
    val pattern = s"""
      
        ?$encounterVariableName obo:RO_0002234 ?$baseVariableName .
        ?$baseVariableName a <$typeURI> .
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
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey, "medId")
}