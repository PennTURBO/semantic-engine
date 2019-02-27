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
          //val biobankEncounterExpansion = buildBiobankEncounterExpansionQuery(instantiation, globalUUID, namedGraph)
          //val healthcareEncounterExpansion = buildHealthcareEncounterExpansionQuery(instantiation, globalUUID, namedGraph)
          println(partipantExpansion)
          update.updateSparql(cxn, partipantExpansion)
          //update.updateSparql(cxn, biobankEncounterExpansion)
          //update.updateSparql(cxn, healthcareEncounterExpansion)
        }
        
        //val biobankEntityLinkingExpansion = buildConsenterToBiobankEncounterLinkingExpansionQuery(globalUUID)
        //val healthcareEntityLinkingExpansion = buildConsenterToHealthcareEncounterLinkingExpansionQuery(globalUUID)
        //update.updateSparql(cxn, biobankEntityLinkingExpansion)
        //update.updateSparql(cxn, healthcareEntityLinkingExpansion)
    }
    
    def buildParticipantExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        println("values " + instantiation + " : " + namedGraph)
        val consenter = Consenter.create(false)
        val shortcutConsenter = ShortcutConsenter.create(instantiation, namedGraph, false)
        println("values " + shortcutConsenter.instantiation + " : " + namedGraph)
        val buildList = Array(shortcutConsenter)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutConsenter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(consenter))
        
        queryBuilder.buildInsertQuery()
    }
    
    /*def buildBiobankEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(ShortcutBiobankEncounter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(ShortcutBiobankEncounter), randomUUID, globalUUID, instantiation)
        queryBuilder.insertBuilder(Array(BiobankEncounter, ShortcutConsenterToBiobankEncounterJoin))
        queryBuilder.buildInsertQuery()
    }
    
    def buildHealthcareEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
            Map(ShortcutHealthcareEncounter -> true),
            Map(), Map())
            
        queryBuilder.bindBuilder(Array(ShortcutHealthcareEncounter), randomUUID, globalUUID, instantiation)
        queryBuilder.insertBuilder(Array(HealthcareEncounter))
        queryBuilder.buildInsertQuery()
    }
    
    def buildConsenterToBiobankEncounterLinkingExpansionQuery(globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
       
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
             Map(ShortcutConsenterToBiobankEncounterJoin -> true, Consenter -> true, BiobankEncounter -> true), Map(), Map())
             
        queryBuilder.bindBuilder(Array(ShortcutConsenterToBiobankEncounterJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(ConsenterToBiobankEncounterJoin))
        queryBuilder.buildInsertQuery()
    }
    
    def buildConsenterToHealthcareEncounterLinkingExpansionQuery(globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        
        val queryBuilder = new QueryBuilder()
        
        queryBuilder.whereBuilder(
             Map(ShortcutConsenterToHealthcareEncounterJoin -> true, Consenter -> true, HealthcareEncounter -> true), Map(), Map())
             
        queryBuilder.bindBuilder(Array(ShortcutConsenterToHealthcareEncounterJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(ConsenterToHealthcareEncounterJoin))
        queryBuilder.buildInsertQuery()
    }*/
}