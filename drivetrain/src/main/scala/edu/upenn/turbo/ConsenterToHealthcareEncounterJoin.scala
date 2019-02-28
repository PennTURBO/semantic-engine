package edu.upenn.turbo

class ConsenterToHealthcareEncounterJoin extends GraphObjectInstance
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
    }
    
    var optional: Boolean = false
    
    val pattern = ConsenterToHealthcareEncounterJoin.pattern
    val baseVariableName = ConsenterToHealthcareEncounterJoin.baseVariableName
    val typeURI = ConsenterToHealthcareEncounterJoin.typeURI
    val variablesToSelect = ConsenterToHealthcareEncounterJoin.variablesToSelect
    
    var namedGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"
}


object ConsenterToHealthcareEncounterJoin extends ExpandedGraphObjectSingleton
{
    def create(optional: Boolean): ConsenterToHealthcareEncounterJoin =
    {
        new ConsenterToHealthcareEncounterJoin(optional)
    }
    
    val baseVariableName = "participantUnderInvestigationRole"
    val consenterName = Consenter.baseVariableName
    val encounterName = HealthcareEncounter.baseVariableName
    val healthcareEncounterCrid = HealthcareEncounterIdentifier.baseVariableName
    val consenterCrid = ConsenterIdentifier.baseVariableName
    val consenterHeight = Consenter.heightKey
    val consenterWeight = Consenter.weightKey
    
    val heightAssay = HealthcareEncounterHeight.baseVariableName
    val heightDatum = HealthcareEncounterHeight.datumKey
    val weightAssay = HealthcareEncounterWeight.baseVariableName
    val weightDatum = HealthcareEncounterWeight.datumKey
    
    
    val pattern = s"""
      
          ?$consenterName obo:RO_0000056 ?$encounterName .
          
          ?$consenterName obo:RO_0000087 ?$baseVariableName .
          
      		?$baseVariableName a obo:OBI_0000097 .
      		
      		?$baseVariableName obo:BFO_0000054 ?$encounterName .
      		
      		?$healthcareEncounterCrid turbo:TURBO_0000302 ?$consenterCrid .
      		
      		?$weightDatum obo:IAO_0000136 ?$consenterName.
      		
      		?$heightDatum obo:IAO_0000136 ?$consenterName.
      		
      		?$weightDatum obo:IAO_0000221 ?$consenterWeight .
      		
      		?$heightDatum obo:IAO_0000221 ?$consenterHeight .
      		
      		?$weightAssay obo:OBI_0000293 ?$consenterName.
      		
      		?$weightAssay obo:OBI_0000293 ?$consenterName.
      
      """

    val typeURI = "http://purl.obolibrary.org/obo/OBI_0000097"
    
    val variablesToSelect = Array(consenterName, encounterName)
}