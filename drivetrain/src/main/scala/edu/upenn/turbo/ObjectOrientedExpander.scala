package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class ObjectOrientedExpander extends ProjectwideGlobals with SparqlBatch
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
        
        val biobankEntityLinkingExpansion = batchFunction(
            cxn, 
            buildHomoSapiensToBiobankEncounterLinkingExpansionQuery, 
            HomoSapiens,
            Array(instantiation, globalUUID)
            )
            
        val healthcareEntityLinkingExpansion = buildHomoSapiensToHealthcareEncounterLinkingExpansionQuery(instantiation, globalUUID)
        update.updateSparql(cxn, healthcareEntityLinkingExpansion)
        println(healthcareEntityLinkingExpansion)
    }
    
    def buildParticipantExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val homoSapiens = HomoSapiens.create(false)
        val shortcutHomoSapiens = ShortcutHomoSapiens.create(instantiation, namedGraph, false)
        val buildList = Array(shortcutHomoSapiens)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutHomoSapiens), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(homoSapiens))
        
        queryBuilder.buildInsertQuery()
    }
    
    def buildBiobankEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val biobankEncounter = BiobankEncounter.create(false)
        val shortcutBiobankEncounter = ShortcutBiobankEncounter.create(instantiation, namedGraph, false)
        val shortcutHomoSapiensToBiobankEncounterJoin = ShortcutHomoSapiensToBiobankEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val buildList = Array(shortcutBiobankEncounter)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutBiobankEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(biobankEncounter, shortcutHomoSapiensToBiobankEncounterJoin))
        
        queryBuilder.buildInsertQuery()
    }
    
    def buildHealthcareEncounterExpansionQuery(instantiation: String, globalUUID: String, namedGraph: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val healthcareEncounter = HealthcareEncounter.create(false)
        val shortcutHealthcareEncounter = ShortcutHealthcareEncounter.create(instantiation, namedGraph, false)
        val shortcutHomoSapiensToHealthcareEncounterJoin = ShortcutHomoSapiensToHealthcareEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val buildList = Array(shortcutHealthcareEncounter)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(shortcutHealthcareEncounter), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(healthcareEncounter, shortcutHomoSapiensToHealthcareEncounterJoin))
        
        queryBuilder.buildInsertQuery()
    }
    
    def buildHomoSapiensToBiobankEncounterLinkingExpansionQuery(instantiation: String, globalUUID: String, batchValues: Map[String, Array[String]] = null): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val shortcutHomoSapiensToBiobankEncounterJoin = ShortcutHomoSapiensToBiobankEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val homoSapiens = HomoSapiens.create(false)
        val biobankEncounter = BiobankEncounter.create(false)
        val homoSapiensToBiobankEncounterJoin = HomoSapiensToBiobankEncounterJoin.create(false)
        val biobankEncounterHeight = BiobankEncounterHeight.create(true)
        val biobankEncounterWeight = BiobankEncounterWeight.create(true)
        val biobankEncounterBMI = BiobankEncounterBMI.create(true)
        val biobankEncounterDate = BiobankEncounterDate.create(true)
        
        val buildList = Array(shortcutHomoSapiensToBiobankEncounterJoin, homoSapiens, biobankEncounter, 
                              biobankEncounterHeight, biobankEncounterWeight, biobankEncounterBMI,
                              biobankEncounterDate)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList, batchValues)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
      
        queryBuilder.bindBuilder(Array(shortcutHomoSapiensToBiobankEncounterJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(homoSapiensToBiobankEncounterJoin))
        queryBuilder.buildInsertQuery()
    }
    
    def buildHomoSapiensToHealthcareEncounterLinkingExpansionQuery(instantiation: String, globalUUID: String): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        val shortcutHomoSapiensToHealthcareEncounterJoin = ShortcutHomoSapiensToHealthcareEncounterJoin.create(instantiation, entityLinkingStagingGraph, false)
        val homoSapiens = HomoSapiens.create(false)
        val healthcareEncounter = HealthcareEncounter.create(false)
        val homoSapiensToHealthcareEncounterJoin = HomoSapiensToHealthcareEncounterJoin.create(false)
        val healthcareEncounterHeight = HealthcareEncounterHeight.create(true)
        val healthcareEncounterWeight = HealthcareEncounterWeight.create(true)
        val healthcareEncounterBMI = HealthcareEncounterBMI.create(true)
        val healthcareEncounterDate = HealthcareEncounterDate.create(true)
        
        val buildList = Array(shortcutHomoSapiensToHealthcareEncounterJoin, homoSapiens, healthcareEncounter, 
                              healthcareEncounterHeight, healthcareEncounterWeight, healthcareEncounterBMI,
                              healthcareEncounterDate)
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(buildList)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
      
        queryBuilder.bindBuilder(Array(shortcutHomoSapiensToHealthcareEncounterJoin), randomUUID, globalUUID)
        queryBuilder.insertBuilder(Array(homoSapiensToHealthcareEncounterJoin))
        queryBuilder.buildInsertQuery()
    }
}