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
     val testingRepository = helper.retrievePropertyFromFile("testingRepository")
     val modelRepository = helper.retrievePropertyFromFile("modelRepository")
     val processNamedGraph = helper.retrievePropertyFromFile("processNamedGraph").replace("\"","")
     
     val replacementString = "[replaceMe]"
     
     //define the SPARQL variables used in the retrieval methods
      val sparqlSubject = "subject"
      val sparqlPredicate = "predicate"
      val sparqlObject = "object"
      val subjectType = "subjectType"
      val objectType = "objectType"
      val graphFromSparql = "graph"
      val requiredBool = "required"
      val sparqlOptionalGroup = "optionalGroup"
      val expandedEntity = "expandedEntity"
      val sparqlString = "sparqlString"
      val dependee = "dependee"
      val baseType = "baseType"
      val shortcutEntity = "shortcutEntity"
      val connectionRecipeType = "connectionRecipeType"
      val graphOfCreatingProcess = "graphOfCreatingProcess"
      
      val requiredInputKeysList = ArrayBuffer(sparqlSubject, sparqlPredicate, sparqlObject, subjectType, objectType, graphFromSparql, requiredBool,
                                              sparqlOptionalGroup, connectionRecipeType, baseType, graphOfCreatingProcess)
                                              
      val requiredOutputKeysList = ArrayBuffer(sparqlSubject, sparqlPredicate, sparqlObject, subjectType, objectType, graphFromSparql, connectionRecipeType,
                                              baseType)
      
      val processVar = "process"
      val date = "date"
      val outputNamedGraphVal = "outputNamedGraph"
      val metaQuery = "query"
      val processRuntime = "processRuntime"
}