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
    def runAllExpansionProcesses(cxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI())
    {
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
        
        val biobankEntityLinkingExpansion = buildConsenterToBiobankEncounterLinkingExpansionQuery(globalUUID)
        val healthcareEntityLinkingExpansion = buildConsenterToHealthcareEncounterLinkingExpansionQuery(globalUUID)
        update.updateSparql(cxn, biobankEntityLinkingExpansion)
        update.updateSparql(cxn, healthcareEntityLinkingExpansion)
    }
    
    def buildParticipantExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val consenter = new Consenter()
        val shortcutConsenter = new ShortcutConsenter(instantiation, namedGraph, consenter)
        
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
  
        val encounter = new BiobankEncounter()
        val consenter = new Consenter()
        val join = new ParticipantToEncounterJoin(consenter, encounter)
        val shortcutJoin = new ShortcutParticipantToEncounterJoin(join, consenter, encounter)
        
        val shortcutEncounter = new ShortcutBiobankEncounter(instantiation, namedGraph, encounter, shortcutJoin)
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(shortcutEncounter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(shortcutEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(encounter, shortcutJoin))
        queryBuilder.buildInsertQuery()
    }
    
    def buildHealthcareEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
  
        val encounter = new HealthcareEncounter()
        val consenter = new Consenter()
        val join = new ParticipantToEncounterJoin(consenter, encounter)
        val shortcutJoin = new ShortcutParticipantToEncounterJoin(join, consenter, encounter)
        
        val shortcutEncounter = new ShortcutHealthcareEncounter(instantiation, namedGraph, encounter, shortcutJoin)
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(shortcutEncounter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(shortcutEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(encounter))
        queryBuilder.buildInsertQuery()
    }
    
    def buildConsenterToBiobankEncounterLinkingExpansionQuery(globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        
        val consenter = new Consenter()
        val encounter = new BiobankEncounter()
        
        val join = new ParticipantToEncounterJoin(consenter, encounter)
        val shortcutJoin = new ShortcutParticipantToEncounterJoin(join, consenter, encounter)
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
             Map(shortcutJoin -> true, consenter -> true, encounter -> true), Map(), Map())
             
        queryBuilder.bindBuilder(Array(shortcutJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(join))
        queryBuilder.buildInsertQuery()
    }
    
    def buildConsenterToHealthcareEncounterLinkingExpansionQuery(globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        
        val consenter = new Consenter()
        val encounter = new HealthcareEncounter()
        
        val join = new ParticipantToEncounterJoin(consenter, encounter)
        val shortcutJoin = new ShortcutParticipantToEncounterJoin(join, consenter, encounter)
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
             Map(shortcutJoin -> true, consenter -> true, encounter -> true), Map(), Map())
             
        queryBuilder.bindBuilder(Array(shortcutJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(join))
        queryBuilder.buildInsertQuery()
    }
}