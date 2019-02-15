package edu.upenn.turbo

class Diagnosis (healthcareEncounter:HealthcareEncounter) extends ExpandedGraphObject
{
    val baseVariableName = "diagnosis"
    val encounterVariableName = healthcareEncounter.baseVariableName
    val valuesKey = "diagnosisCodeValue"
    val registryKey = "diagnosisRegistry"
    
    val pattern = s"""
      
        ?$encounterVariableName obo:RO_0002234 ?$baseVariableName .
        ?$baseVariableName a obo:OGMS_0000073 .
    		?$baseVariableName turbo:TURBO_0000306 ?concatIcdTerm .
    		?$baseVariableName turbo:TURBO_0000703 ?$registryKey .
    		?$baseVariableName turbo:TURBO_0006515 ?diagCodeRegTextVal .
    		?$baseVariableName turbo:TURBO_0006512 ?$valuesKey .
    		?$baseVariableName turbo:TURBO_0010013 ?primaryDiag .
    		?$baseVariableName turbo:TURBO_0010014 ?diagSequence .
    		
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
      
      val typeURI = "http://purl.obolibrary.org/obo/OGMS_0000073"
      
      val variablesToSelect = Array(encounterVariableName, valuesKey, registryKey)
}