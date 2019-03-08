package edu.upenn.turbo

class BiobankEncounterBMI extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = BiobankEncounterBMI.pattern
    val baseVariableName = BiobankEncounterBMI.baseVariableName
    val typeURI = BiobankEncounterBMI.typeURI
    val variablesToSelect = BiobankEncounterBMI.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object BiobankEncounterBMI extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): BiobankEncounterBMI =
    {
        new BiobankEncounterBMI(optional)
    }
    
    val typeURI = "http://www.ebi.ac.uk/efo/EFO_0004340"
    
    val baseVariableName = "BiobankEncounterBMI"
    val encounterDate = BiobankEncounterDate.baseVariableName
    val encounterVariableName = BiobankEncounter.baseVariableName
    val valuesKey = "BiobankEncounterBmiValue"
    
    val bmiValue = valuesKey
    
    val valueSpecification = "BiobankEncounterBmiValSpec"
    
    val pattern = s"""
      
        ?$baseVariableName a <$typeURI> .
    		?$baseVariableName obo:OBI_0001938 ?$valueSpecification .
    		?$valueSpecification a obo:OBI_0001933 .
    		?$valueSpecification obo:OBI_0002135 ?$valuesKey .
    		?$baseVariableName obo:IAO_0000581 ?$encounterDate .
    		
        ?$encounterVariableName obo:OBI_0000299 ?$baseVariableName .
    		
      """
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val variablesToSelect = Array(encounterDate, valuesKey)
}