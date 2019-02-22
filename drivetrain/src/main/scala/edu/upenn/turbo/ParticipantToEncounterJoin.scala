package edu.upenn.turbo

class ParticipantToEncounterJoin (consenter: Consenter, encounter: Encounter) extends GraphObject
{
    val baseVariableName = "participantUnderInvestigationRole"
    val consenterName = consenter.baseVariableName
    val encounterName = encounter.baseVariableName
    val biobankEncounterCrid = encounter.mandatoryLinks("Identifier").baseVariableName
    val consenterCrid = consenter.mandatoryLinks("Identifier").baseVariableName
    val consenterHeight = consenter.heightKey
    val consenterWeight = consenter.weightKey
    
    val encounterHeight: Height = encounter.optionalLinks("Height").asInstanceOf[Height]
    val encounterWeight: Weight = encounter.optionalLinks("Weight").asInstanceOf[Weight]
    val heightAssay = encounterHeight.baseVariableName
    val heightDatum = encounterHeight.datumKey
    val weightAssay = encounterWeight.baseVariableName
    val weightDatum = encounterWeight.datumKey
    
    
    val pattern = s"""
      
          ?$consenterName obo:RO_0000056 ?$encounterName .
          
          ?$consenterName obo:RO_0000087 ?$baseVariableName .
          
      		?$baseVariableName a obo:OBI_0000097 .
      		
      		?$baseVariableName obo:BFO_0000054 ?$encounterName .
      		
      		?$biobankEncounterCrid turbo:TURBO_0000302 ?$consenterCrid .
      		
      		?$weightDatum obo:IAO_0000136 ?$consenterName.
      		
      		?$heightDatum obo:IAO_0000136 ?$consenterName.
      		
      		?$weightDatum obo:IAO_0000221 ?$consenterWeight .
      		
      		?$heightDatum obo:IAO_0000221 ?$consenterHeight .
      		
      		?$weightAssay obo:OBI_0000293 ?$consenterName.
      		
      		?$weightDatum obo:OBI_0000293 ?$consenterName.
      
      """
      		
    val namedGraph: String = "http://www.itmat.upenn.edu/biobank/expanded"

    override val typeURI: String = "http://purl.obolibrary.org/obo/OBI_0000097"
    
    val variablesToSelect: Array[String] = Array(consenterName, encounterName)
}