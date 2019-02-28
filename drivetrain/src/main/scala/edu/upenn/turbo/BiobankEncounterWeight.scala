package edu.upenn.turbo

class BiobankEncounterWeight extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterWeight.pattern
    val baseVariableName = BiobankEncounterWeight.baseVariableName
    val typeURI = BiobankEncounterWeight.typeURI
    val variablesToSelect = BiobankEncounterWeight.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterWeight extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterWeight =
    {
        new BiobankEncounterWeight(optional)
    }
    
    val baseVariableName = "BiobankEncounterWeightAssay"
    val encounterVariableName = BiobankEncounter.baseVariableName
    val valuesKey = "BiobankEncounterWeightValue"
    val datumKey = "BiobankEncounterWeightDatum"
    
    val weightValue = valuesKey
    
    val valueSpecification = "BiobankEncounterWeightValSpec"
    
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
      
      val typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}