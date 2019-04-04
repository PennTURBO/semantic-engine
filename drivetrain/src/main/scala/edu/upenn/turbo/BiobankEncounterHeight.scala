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
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0001511"
    
    val baseVariableName = "BiobankEncounterHeightAssay"
    val encounterVariableName = BiobankEncounter.baseVariableName
    val valuesKey = "BiobankEncounterHeightValue"
    val datumKey = "BiobankEncounterHeightDatum"
    
    val heightValue = valuesKey
    
    val valueSpecification = "BiobankEncounterHeightValSpec"
    
    val pattern = s"""
      
        ?$valueSpecification a obo:OBI_0001931 ;
             obo:IAO_0000039 obo:UO_0000015 ;
             obo:OBI_0002135 ?$valuesKey .
        		               
  	    ?$baseVariableName a <$typeURI> ;
  	         obo:OBI_0000299 ?$datumKey .

      	?$datumKey a obo:IAO_0000408 ;
      	     obo:OBI_0001938 ?$valueSpecification .
      	     
      	?$encounterVariableName obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$encounterVariableName .
    		
      """
    
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey)
}