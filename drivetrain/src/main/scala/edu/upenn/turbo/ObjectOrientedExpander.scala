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
    val entityLinkingStagingGraph = "http://www.itmat.upenn.edu/biobank/Shortcuts_entityLinkingStaging"
    
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
        
        val biobankEntityLinkingExpansion = buildConsenterToBiobankEncounterLinkingExpansionQuery(instantiation, globalUUID)
        val healthcareEntityLinkingExpansion = buildConsenterToHealthcareEncounterLinkingExpansionQuery(instantiation, globalUUID)
        update.updateSparql(cxn, biobankEntityLinkingExpansion)
        update.updateSparql(cxn, healthcareEntityLinkingExpansion)
        println(healthcareEntityLinkingExpansion)
    }
    
    def buildParticipantExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val consenter = Consenter.create(false)
        val shortcutConsenter = ShortcutConsenter.create(instantiation, namedGraph, false)
        val buildList = Array(shortcutConsenter)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutConsenter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(consenter))
        
        queryBuilder.buildInsertQuery()
    }
    
    def buildBiobankEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val biobankEncounter = BiobankEncounter.create(false)
        val shortcutBiobankEncounter = ShortcutBiobankEncounter.create(instantiation, namedGraph, false)
        val shortcutConsenterToBiobankEncounterJoin = ShortcutConsenterToBiobankEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val buildList = Array(shortcutBiobankEncounter)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutBiobankEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(biobankEncounter, shortcutConsenterToBiobankEncounterJoin))
        
        queryBuilder.buildInsertQuery()
    }
    
    def buildHealthcareEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val healthcareEncounter = HealthcareEncounter.create(false)
        val shortcutHealthcareEncounter = ShortcutHealthcareEncounter.create(instantiation, namedGraph, false)
        val shortcutConsenterToHealthcareEncounterJoin = ShortcutConsenterToHealthcareEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val buildList = Array(shortcutHealthcareEncounter)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutHealthcareEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(healthcareEncounter, shortcutConsenterToHealthcareEncounterJoin))
        
        queryBuilder.buildInsertQuery()
    }
    
    def buildConsenterToBiobankEncounterLinkingExpansionQuery(instantiation: String, globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val shortcutConsenterToBiobankEncounterJoin = ShortcutConsenterToBiobankEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val consenter = Consenter.create(false)
        val biobankEncounter = BiobankEncounter.create(false)
        val consenterToBiobankEncounterJoin = ConsenterToBiobankEncounterJoin.create(false)
        val biobankEncounterHeight = BiobankEncounterHeight.create(true)
        val biobankEncounterWeight = BiobankEncounterWeight.create(true)
        val biobankEncounterBMI = BiobankEncounterBMI.create(true)
        val biobankEncounterDate = BiobankEncounterDate.create(true)
        
        val buildList = Array(shortcutConsenterToBiobankEncounterJoin, consenter, biobankEncounter, 
                              biobankEncounterHeight, biobankEncounterWeight, biobankEncounterBMI,
                              biobankEncounterDate)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
      
        queryBuilder.bindBuilder(Array(shortcutConsenterToBiobankEncounterJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(consenterToBiobankEncounterJoin))
        queryBuilder.buildInsertQuery()
    }
    
    def buildConsenterToHealthcareEncounterLinkingExpansionQuery(instantiation: String, globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val shortcutConsenterToHealthcareEncounterJoin = ShortcutConsenterToHealthcareEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val consenter = Consenter.create(false)
        val healthcareEncounter = HealthcareEncounter.create(false)
        val consenterToHealthcareEncounterJoin = ConsenterToHealthcareEncounterJoin.create(false)
        val healthcareEncounterHeight = HealthcareEncounterHeight.create(true)
        val healthcareEncounterWeight = HealthcareEncounterWeight.create(true)
        val healthcareEncounterBMI = HealthcareEncounterBMI.create(true)
        val healthcareEncounterDate = HealthcareEncounterDate.create(true)
        
        val buildList = Array(shortcutConsenterToHealthcareEncounterJoin, consenter, healthcareEncounter, 
                              healthcareEncounterHeight, healthcareEncounterWeight, healthcareEncounterBMI,
                              healthcareEncounterDate)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
      
        queryBuilder.bindBuilder(Array(shortcutConsenterToHealthcareEncounterJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(consenterToHealthcareEncounterJoin))
        queryBuilder.buildInsertQuery()
    }
}