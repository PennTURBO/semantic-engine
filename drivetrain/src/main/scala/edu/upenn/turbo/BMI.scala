package edu.upenn.turbo

class BMI (encounter:Encounter) extends ExpandedGraphObject
{
    val baseVariableName = "BMI"
    val encounterDate = encounter.encounterDate
    val encounterVariableName = encounter.baseVariableName
    val valuesKey = "bmiValue"
    
    val bmiValue = "bmiValue"
    
    val valueSpecification = "BmiValSpec"
    val dataset = "dataset"
    
    val pattern = s"""
      
        ?$baseVariableName a efo:EFO_0004340 .
    		?$baseVariableName obo:OBI_0001938 ?$valueSpecification .
    		?$valueSpecification a obo:OBI_0001933 .
    		?$valueSpecification obo:OBI_0002135 ?$valuesKey .
    		?$baseVariableName obo:IAO_0000581 ?$encounterDate .
    		
        ?$encounterVariableName obo:OBI_0000299 ?$baseVariableName .
    		?$dataset obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$dataset .
        ?$dataset a obo:IAO_0000100 .
    		
      """

    val optionalPattern = """"""
  
      val optionalLinks: Map[String, ExpandedGraphObject] = Map()
      val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
      
      val connections = Map(
          "" -> ""
      )
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004340"
      
      val variablesToSelect = Array(encounterDate, valuesKey)
}