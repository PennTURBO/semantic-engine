package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class ObjectOrientedExpander extends ProjectwideGlobals
{
    def runAllExpansionProcesses(cxn: RepositoryConnection, globalUUID: String)
    {
        val instantiation = helper.genPmbbIRI()
        val namedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn)
        
        for (namedGraph <- namedGraphsList)
        {
          val partipantExpansion = buildParticipantExpansionQuery(instantiation, globalUUID, namedGraph)
          val biobankEncounterExpansion = buildBiobankEncounterExpansionQuery(instantiation, globalUUID, namedGraph)
          val healthcareEncounterExpansion = buildHealthcareEncounterExpansionQuery(instantiation, globalUUID, namedGraph)
          
          update.updateSparql(cxn, partipantExpansion)
          update.updateSparql(cxn, biobankEncounterExpansion)
          update.updateSparql(cxn, healthcareEncounterExpansion)
        }
        
        val participantToBiobankEncounterLinking = buildParticipantToBiobankEncounterLinkingQuery()
        val participantToHealthcareEncounterLinking = buildParticipantToHealthcareEncounterLinkingQuery()
        
        update.updateSparql(cxn, participantToBiobankEncounterLinking)
        update.updateSparql(cxn, participantToHealthcareEncounterLinking)
    }
    
    def buildParticipantExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val shortcutConsenter = new ShortcutConsenter(instantiation, namedGraph)
        val consenter = new Consenter()
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(shortcutConsenter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(shortcutConsenter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(consenter))
        queryBuilder.buildInsertQuery()
    }
    
    def buildBiobankEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
  
        val shortcutEncounter = new ShortcutBiobankEncounter(instantiation, namedGraph)
        val encounter = new BiobankEncounter()
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(shortcutEncounter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(shortcutEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(encounter))
        queryBuilder.buildInsertQuery()
    }
    
    def buildHealthcareEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
  
        val shortcutEncounter = new ShortcutHealthcareEncounter(instantiation, namedGraph)
        val encounter = new HealthcareEncounter()
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(shortcutEncounter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(shortcutEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(encounter))
        queryBuilder.buildInsertQuery()
    }
    
    def buildParticipantToBiobankEncounterLinkingQuery(): String =
    {
        val consenter = new Consenter()
        val encounter = new BiobankEncounter()
        val height = encounter.optionalLinks("Height")
        val weight = encounter.optionalLinks("Weight")
        val consenterIdentifier: ConsenterIdentifier = consenter.mandatoryLinks("Identifier").asInstanceOf[ConsenterIdentifier]
        
        val join = new ParticipantToEncounterJoin(consenter, encounter)
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(Map(consenter -> true, encounter -> true, 
            height -> false, weight -> false), Map(), Map())
            
        queryBuilder.filterBuilder(Map(consenterIdentifier.valuesKey -> encounter.linkedConsenterSymbol,
                                  consenterIdentifier.registryKey -> encounter.linkedConsenterRegistry))
                                  
        queryBuilder.insertBuilder(Array(join))
        
        val finalQuery = queryBuilder.buildInsertQuery()
        finalQuery
    }
    
    def buildParticipantToHealthcareEncounterLinkingQuery(): String =
    {
        val consenter = new Consenter()
        val encounter = new HealthcareEncounter()
        val height = encounter.optionalLinks("Height")
        val weight = encounter.optionalLinks("Weight")
        val consenterIdentifier: ConsenterIdentifier = consenter.mandatoryLinks("Identifier").asInstanceOf[ConsenterIdentifier]
        
        val join = new ParticipantToEncounterJoin(consenter, encounter)
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(Map(consenter -> true, encounter -> true, 
            height -> false, weight -> false), Map(), Map())
            
        queryBuilder.filterBuilder(Map(consenterIdentifier.valuesKey -> encounter.linkedConsenterSymbol,
                                  consenterIdentifier.registryKey -> encounter.linkedConsenterRegistry))
                                  
        queryBuilder.insertBuilder(Array(join))
        
        val finalQuery = queryBuilder.buildInsertQuery()
        finalQuery
    }
}