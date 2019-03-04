package edu.upenn.turbo

class Diagnosis extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = Diagnosis.pattern
    val baseVariableName = Diagnosis.baseVariableName
    val typeURI = Diagnosis.typeURI
    val variablesToSelect = Diagnosis.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}

object Diagnosis extends ExpandedGraphObjectSingletonFromDataset
{
    def create(optional: Boolean): Diagnosis =
    {
        new Diagnosis(optional)
    }
    
    val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000073"

    val baseVariableName = "diagnosis"
    val encounterVariableName = HealthcareEncounter.baseVariableName
    val valuesKey = "diagnosisCodeValue"
    val registryKey = "diagnosisRegistry"
    
    val diagnosisCode = valuesKey
    
    val icdTerm = "concatIcdTerm"
    val diagnosisCodeRegistryString = "diagCodeRegTextVal"
    val primaryDiagnosis = "primaryDiag"
    val diagnosisSequence = "diagSequence"
    
    val dataset = HealthcareEncounter.dataset
    
    val pattern = s"""
      
        ?$encounterVariableName obo:RO_0002234 ?$baseVariableName .
        ?$baseVariableName a <$typeURI> .
    		?$baseVariableName turbo:TURBO_0000306 ?$icdTerm .
    		?$baseVariableName turbo:TURBO_0000703 ?$registryKey .
    		?$baseVariableName turbo:TURBO_0006515 ?$diagnosisCodeRegistryString .
    		?$baseVariableName turbo:TURBO_0006512 ?$valuesKey .
    		?$baseVariableName turbo:TURBO_0010013 ?$primaryDiagnosis .
    		?$baseVariableName turbo:TURBO_0010014 ?$diagnosisSequence .
    		
    		?$dataset obo:BFO_0000051 ?$baseVariableName .
        ?$baseVariableName obo:BFO_0000050 ?$dataset .
    		
      """
      
      val namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey, registryKey)
}