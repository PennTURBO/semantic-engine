package edu.upenn.turbo

class BiobankEncounterHeight extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterHeight.pattern
    val baseVariableName = BiobankEncounterHeight.baseVariableName
    val typeURI = BiobankEncounterHeight.typeURI
    val variablesToSelect = BiobankEncounterHeight.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterHeight extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterHeight =
    {
        new BiobankEncounterHeight(optional)
    }
    
    val baseVariableName = "BiobankEncounterHeightAssay"
    val encounterVariableName = BiobankEncounter.baseVariableName
    val valuesKey = "BiobankEncounterHeightValue"
    val datumKey = "BiobankEncounterHeightDatum"
    
    val heightValue = valuesKey
    
    val valueSpecification = "BiobankEncounterBmiValSpec"
    
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
      
      val typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}