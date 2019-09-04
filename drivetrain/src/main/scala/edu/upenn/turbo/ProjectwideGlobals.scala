package edu.upenn.turbo

import org.slf4j.LoggerFactory
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

trait ProjectwideGlobals extends Enumeration
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
     val bioportalApiKey = helper.retrievePropertyFromFile("bioportalApiKey")
     val reinferRepo = getBooleanProperty("reinferRepo")
     val loadAdditionalOntologies = getBooleanProperty("loadAdditionalOntologies")
     val graphModelFile = helper.retrievePropertyFromFile("graphModelFile")
     
     def getBooleanProperty(property: String): Boolean =
     {
        def boolAsString = helper.retrievePropertyFromFile(property)
        if (boolAsString == "true") return true
        else return false
     }
     
     val replacementString = "[replaceMe]"
     
     //define enums for SPARQL variables used in the retrieval methods
      val SUBJECT = Value("SUBJECT")
      val PREDICATE = Value("PREDICATE")
      val OBJECT = Value("OBJECT")
      val SUBJECTTYPE = Value("SUBJECTTYPE")
      val OBJECTTYPE = Value("OBJECTTYPE")
      val GRAPH = Value("GRAPH")
      val OPTIONALGROUP = Value("OPTIONALGROUP")
      val EXPANDEDENTITY = Value("EXPANDEDENTITY")
      val SHORTCUTENTITY = Value("SHORTCUTENTITY")
      val CONNECTIONRECIPETYPE = Value("CONNECTIONRECIPETYPE")
      val GRAPHOFCREATINGPROCESS = Value("GRAPHOFCREATINGPROCESS")
      val MINUSGROUP = Value("MINUSGROUP")
      val GRAPHOFORIGIN = Value("GRAPHOFORIGIN")
      val INPUTTYPE = Value("INPUTTYPE")
      val SUBJECTCONTEXT = Value("SUBJECTCONTEXT")
      val OBJECTCONTEXT = Value("OBJECTCONTEXT")
      val CONTEXT = Value("CONTEXT")
      val MULTIPLICITY = Value("MULTIPLICITY")
      val OBJECTRULE = Value("OBJECTRULE")
      val SUBJECTRULE = Value("SUBJECTRULE")
      val SUBJECTADESCRIBER = Value("SUBJECTADESCRIBER")
      val OBJECTADESCRIBER = Value("OBJECTADESCRIBER")
      val SUBJECTDEPENDEE = Value("SUBJECTDEPENDEE")
      val OBJECTDEPENDEE = Value("OBJECTDEPENDEE")
      val OBJECTMULTIOBJECTDESCRIBER = Value("OBJECTMULTIOBJECTDESCRIBER")
      
      val requiredInputKeysList = ArrayBuffer(SUBJECT, PREDICATE, OBJECT, SUBJECTTYPE, OBJECTTYPE, GRAPH, 
                                              OPTIONALGROUP, CONNECTIONRECIPETYPE, GRAPHOFCREATINGPROCESS,
                                              MINUSGROUP, GRAPHOFORIGIN, INPUTTYPE, MULTIPLICITY, OBJECTMULTIOBJECTDESCRIBER)
                                              
      val requiredOutputKeysList = ArrayBuffer(SUBJECT, PREDICATE, OBJECT, SUBJECTTYPE, OBJECTTYPE, GRAPH, CONNECTIONRECIPETYPE,
                                              SUBJECTCONTEXT, OBJECTCONTEXT, MULTIPLICITY, OBJECTRULE, SUBJECTRULE, OBJECTADESCRIBER,
                                              SUBJECTADESCRIBER, SUBJECTDEPENDEE, OBJECTDEPENDEE)
      
      // define enums used as keys for process meta info hashmap
      val PROCESS, DATE, OUTPUTNAMEDGRAPH, METAQUERY, PROCESSRUNTIME, TRIPLESADDED, REPLACEMENTSTRING, INPUTNAMEDGRAPHS = Value

}