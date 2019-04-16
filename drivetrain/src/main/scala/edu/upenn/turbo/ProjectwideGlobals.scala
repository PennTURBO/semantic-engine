package edu.upenn.turbo

import org.slf4j.LoggerFactory
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

trait ProjectwideGlobals
{
    val helper: TurboMultiuseClass = new TurboMultiuseClass
    val update: SparqlUpdater = new SparqlUpdater
    val logger = LoggerFactory.getLogger(getClass)
    
    var graphDBMaterials: TurboGraphConnection = null
    
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    
    var gmCxn: RepositoryConnection = null
    var gmRepoManager: RemoteRepositoryManager = null
    var gmRepository: Repository = null
    
    var testCxn: RepositoryConnection = null
    var testRepoManager: RemoteRepositoryManager = null
    var testRepository: Repository = null

     //properties from file are global variables
     val serviceURL = helper.retrievePropertyFromFile("serviceURL")
     val ontologyURL = helper.retrievePropertyFromFile("ontologyURL")
     val productionNamespace = helper.retrievePropertyFromFile("namespace")
     val inputFiles = helper.retrievePropertyFromFile("inputFiles")
     val inputFilesNamedGraphs = helper.retrievePropertyFromFile("inputFilesNamedGraphs")
     val inputFilesFormat = helper.retrievePropertyFromFile("inputFilesFormat")
     val importOntologies = helper.retrievePropertyFromFile("importOntologies")
     val errorLogFile = helper.retrievePropertyFromFile("errorLogFile")
     val applyLabels = helper.retrievePropertyFromFile("applyLabels")
     val inputLOFFiles = helper.retrievePropertyFromFile("inputLOFFiles")
     val loadDiseaseOntologies = helper.retrievePropertyFromFile("loadDiseaseOntologies")
     val loadDrugOntologies = helper.retrievePropertyFromFile("loadDrugOntologies")
     val bioportalAPIkey = helper.retrievePropertyFromFile("bioportalAPIKey")
     val medMappingRepo = helper.retrievePropertyFromFile("medMappingRepo")
     val loadLOFdata = helper.retrievePropertyFromFile("loadLOFData")
     val entityLinkingNamedGraph = helper.retrievePropertyFromFile("entityLinkingNamedGraph")
     val testingNamespace = helper.retrievePropertyFromFile("testingNamespace")
     val modelNamespace = helper.retrievePropertyFromFile("modelNamespace")
}