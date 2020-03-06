package edu.upenn.turbo

import org.slf4j.LoggerFactory
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
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
     val productionServiceURL = helper.retrieveUriPropertyFromFile("productionServiceURL")
     val productionUsername = helper.retrievePropertyFromFile("productionUsername")
     val productionPassword = helper.retrievePropertyFromFile("productionPassword")
     val productionRepository = helper.retrievePropertyFromFile("productionRepository")
     
     val testingServiceURL = helper.retrieveUriPropertyFromFile("testingServiceURL")
     val testingUsername = helper.retrievePropertyFromFile("testingUsername")
     val testingPassword = helper.retrievePropertyFromFile("testingPassword")
     val testingRepository = helper.retrievePropertyFromFile("testingRepository")
     
     val modelServiceURL = helper.retrieveUriPropertyFromFile("modelServiceURL")
     val modelUsername = helper.retrievePropertyFromFile("modelUsername")
     val modelPassword = helper.retrievePropertyFromFile("modelPassword")
     val modelRepository = helper.retrievePropertyFromFile("modelRepository")
     
     val ontologyURL = helper.retrieveUriPropertyFromFile("ontologyURL")
     val processNamedGraph = helper.retrieveUriPropertyFromFile("processNamedGraph").replace("\"","")
     val bioportalApiKey = helper.retrievePropertyFromFile("bioportalApiKey")
     val reinferRepo = getBooleanProperty("reinferRepo")
     val loadAdditionalOntologies = getBooleanProperty("loadAdditionalOntologies")
     val instructionSetFile = helper.retrievePropertyFromFile("instructionSetFile")
     val graphSpecificationFile = helper.retrievePropertyFromFile("graphSpecificationFile")
     val dataValidationMode = helper.retrievePropertyFromFile("dataValidationMode").toLowerCase()
     val defaultPrefix = helper.retrieveUriPropertyFromFile("defaultPrefix")
     val expandedNamedGraph = helper.retrieveUriPropertyFromFile("expandedNamedGraph").replace("\"","")
     val clearGraphsAtStart = getBooleanProperty("clearGraphsAtStart")
     val acornOntologyFile = helper.retrievePropertyFromFile("acornOntologyFile")
     val validateAgainstOntology = getBooleanProperty("validateAgainstOntology")
     val useMultipleThreads = getBooleanProperty("useMultipleThreads")
     
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
      val CONNECTIONNAME = Value("CONNECTIONNAME")
      val REQUIREMENT = Value("REQUIREMENT")
      val OBJECTALITERAL = Value("OBJECTALITERAL")
      val SUFFIXOPERATOR = Value("SUFFIXOPERATOR")
      val OBJECTALITERALVALUE = Value("OBJECTALITERALVALUE")
      val SUBJECTANINSTANCE = Value("SUBJECTANINSTANCE")
      val OBJECTANINSTANCE = Value("OBJECTANINSTANCE")
      val GRAPHLITERALTYPE = Value("GRAPHLITERALTYPE")
      
      val requiredInputKeysList = ArrayBuffer(SUBJECT, PREDICATE, OBJECT, GRAPH, SUBJECTANINSTANCE, OBJECTANINSTANCE,
                                              OPTIONALGROUP, CONNECTIONRECIPETYPE, GRAPHOFCREATINGPROCESS,
                                              MINUSGROUP, GRAPHOFORIGIN, INPUTTYPE, MULTIPLICITY, OBJECTADESCRIBER,
                                              CONNECTIONNAME, SUBJECTADESCRIBER, REQUIREMENT, OBJECTALITERALVALUE, SUFFIXOPERATOR,
                                              SUBJECTCONTEXT, OBJECTCONTEXT, OBJECTALITERAL, GRAPHLITERALTYPE)
                                              
      val requiredOutputKeysList = ArrayBuffer(SUBJECT, PREDICATE, OBJECT, GRAPH, CONNECTIONRECIPETYPE, SUBJECTANINSTANCE, OBJECTANINSTANCE,
                                              SUBJECTCONTEXT, OBJECTCONTEXT, MULTIPLICITY, OBJECTRULE, SUBJECTRULE, OBJECTADESCRIBER,
                                              SUBJECTADESCRIBER, SUBJECTDEPENDEE, OBJECTDEPENDEE, CONNECTIONNAME, OBJECTALITERALVALUE,
                                              OBJECTALITERAL)
      
      // define enums used as keys for process meta info hashmap
      val PROCESS, DATE, OUTPUTNAMEDGRAPH, METAQUERY, PROCESSRUNTIME, TRIPLESADDED, REPLACEMENTSTRING, INPUTNAMEDGRAPHS, PROCESSSPECIFICATION = Value

}