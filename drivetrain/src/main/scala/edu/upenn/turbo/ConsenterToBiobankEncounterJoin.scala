package edu.upenn.turbo

object ConsenterToBiobankEncounterJoin extends ExpandedGraphObjectSingleton
{
    baseVariableName = "participantUnderInvestigationRole"
    val consenterName = Consenter.baseVariableName
    val encounterName = BiobankEncounter.baseVariableName
    val biobankEncounterCrid = BiobankEncounterIdentifier.baseVariableName
    val consenterCrid = ConsenterIdentifier.baseVariableName
    val consenterHeight = Consenter.heightKey
    val consenterWeight = Consenter.weightKey
    
    val heightAssay = BiobankEncounterHeight.baseVariableName
    val heightDatum = BiobankEncounterHeight.datumKey
    val weightAssay = BiobankEncounterWeight.baseVariableName
    val weightDatum = BiobankEncounterWeight.datumKey
    
    
    pattern = s"""
      
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
      		
    namedGraph = "http://www.itmat.upenn.edu/biobank/expanded"

    typeURI = "http://purl.obolibrary.org/obo/OBI_0000097"
    
    variablesToSelect = Array(consenterName, encounterName)
}