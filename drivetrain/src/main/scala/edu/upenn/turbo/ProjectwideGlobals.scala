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
     val productionRepository = helper.retrievePropertyFromFile("productionRepository")
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
     val testingRepository = helper.retrievePropertyFromFile("testingRepository")
     val modelRepository = helper.retrievePropertyFromFile("modelRepository")
     
     val replacementString = "[replaceMe]"
     
     //define the SPARQL variables used in the retrieval methods
      val subject = "subject"
      val predicate = "predicate"
      val objectVar = "object"
      val subjectType = "subjectType"
      val objectType = "objectType"
      val graphFromSparql = "graph"
      val requiredBool = "required"
      val optionalGroup = "optionalGroup"
      val expandedEntity = "expandedEntity"
      val sparqlString = "sparqlString"
      val dependee = "dependee"
      val baseType = "baseType"
      val shortcutEntity = "shortcutEntity"
      val connectionRecipeType = "connectionRecipeType"
      val graphOfCreatingProcess = "graphOfCreatingProcess"
      
      val requiredInputKeysList = ArrayBuffer(subject, predicate, objectVar, subjectType, objectType, graphFromSparql, requiredBool,
                                              optionalGroup, connectionRecipeType, baseType, graphOfCreatingProcess)
                                              
      val requiredOutputKeysList = ArrayBuffer(subject, predicate, objectVar, subjectType, objectType, graphFromSparql, connectionRecipeType,
                                              baseType)
      
      val processVar = "process"
      val date = "date"
      val outputNamedGraphVal = "outputNamedGraph"
      val metaQuery = "query"
      
      val noGroup = "noGroup"
      val defaultGraph = "defaultGraph"
}