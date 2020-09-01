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

object Globals extends Enumeration
{
    val logger = LoggerFactory.getLogger(getClass)
        
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    
    var gmCxn: RepositoryConnection = null
    var gmRepoManager: RemoteRepositoryManager = null
    var gmRepository: Repository = null
    
    var prefixMap = new HashMap[String, String]
    val prefixesFromFile = io.Source.fromFile("config/prefixes.txt").getLines
    for (line <- prefixesFromFile)
    {
        assert(!line.contains(" "), "Check prefixes file: entry " + line + " has an illegal space")
        val splitLine = line.split("\\:",2)
        assert(splitLine.size == 2, "Check prefixes file: Unable to process line " + line)
        Utilities.validateURI(splitLine(1))
        assert(!prefixMap.contains(splitLine(0)), "Duplicate prefix entry for " + splitLine(0))
        prefixMap += splitLine(0) -> splitLine(1)
    }

     //properties from file are global variables
     val productionServiceURL = Utilities.retrieveUriPropertyFromFile("productionServiceURL")
     val productionUsername = Utilities.retrievePropertyFromFile("productionUsername")
     val productionPassword = Utilities.retrievePropertyFromFile("productionPassword")
     val productionRepository = Utilities.retrievePropertyFromFile("productionRepository")
     
     val testingServiceURL = Utilities.retrieveUriPropertyFromFile("testingServiceURL")
     val testingUsername = Utilities.retrievePropertyFromFile("testingUsername")
     val testingPassword = Utilities.retrievePropertyFromFile("testingPassword")
     val testingRepository = Utilities.retrievePropertyFromFile("testingRepository")
     
     val modelServiceURL = Utilities.retrieveUriPropertyFromFile("modelServiceURL")
     val modelUsername = Utilities.retrievePropertyFromFile("modelUsername")
     val modelPassword = Utilities.retrievePropertyFromFile("modelPassword")
     val modelRepository = Utilities.retrievePropertyFromFile("modelRepository")
     
     val ontologyURL = Utilities.retrieveUriPropertyFromFile("ontologyURL")
     val processNamedGraph = Utilities.retrieveUriPropertyFromFile("processNamedGraph").replace("\"","")
     val bioportalApiKey = Utilities.retrievePropertyFromFile("bioportalApiKey")
     val reinferRepo = getBooleanProperty("reinferRepo")
     val loadAdditionalOntologies = getBooleanProperty("loadAdditionalOntologies")
     val instructionSetFile = Utilities.retrievePropertyFromFile("instructionSetFile")
     val graphSpecificationFile = Utilities.retrievePropertyFromFile("graphSpecificationFile")
     val dataValidationMode = Utilities.retrievePropertyFromFile("dataValidationMode").toLowerCase()
     val defaultPrefix = Utilities.retrieveUriPropertyFromFile("defaultPrefix")
     val expandedNamedGraph = Utilities.retrieveUriPropertyFromFile("expandedNamedGraph").replace("\"","")
     val clearGraphsAtStart = getBooleanProperty("clearGraphsAtStart")
     val acornOntologyFile = Utilities.retrievePropertyFromFile("acornOntologyFile")
     val validateAgainstOntology = getBooleanProperty("validateAgainstOntology")
     val useMultipleThreads = getBooleanProperty("useMultipleThreads")

     val numberOfThreads = Utilities.retrievePropertyFromFile("numberOfThreads").toInt
     
     
     def getBooleanProperty(property: String): Boolean =
     {
        def boolAsString = Utilities.retrievePropertyFromFile(property)
        if (boolAsString == "true") return true
        else return false
     }
     
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
      val SUFFIXOPERATOR = Value("SUFFIXOPERATOR")
      val OBJECTALITERALVALUE = Value("OBJECTALITERALVALUE")
      val GRAPHLITERALTYPE = Value("GRAPHLITERALTYPE")
      val SUBJECTUNTYPED = Value("SUBJECTUNTYPED")
      val OBJECTUNTYPED = Value("OBJECTUNTYPED")
      
      val requiredInputKeysList = ArrayBuffer(SUBJECT, PREDICATE, OBJECT, GRAPH,
                                              OPTIONALGROUP, CONNECTIONRECIPETYPE, GRAPHOFCREATINGPROCESS,
                                              MINUSGROUP, GRAPHOFORIGIN, INPUTTYPE, MULTIPLICITY, OBJECTADESCRIBER,
                                              CONNECTIONNAME, SUBJECTADESCRIBER, REQUIREMENT, OBJECTALITERALVALUE, SUFFIXOPERATOR,
                                              SUBJECTCONTEXT, OBJECTCONTEXT, GRAPHLITERALTYPE, SUBJECTUNTYPED, OBJECTUNTYPED)
                                              
      val requiredOutputKeysList = ArrayBuffer(SUBJECT, PREDICATE, OBJECT, GRAPH, CONNECTIONRECIPETYPE,
                                              SUBJECTCONTEXT, OBJECTCONTEXT, MULTIPLICITY, OBJECTRULE, SUBJECTRULE, OBJECTADESCRIBER,
                                              SUBJECTADESCRIBER, SUBJECTDEPENDEE, OBJECTDEPENDEE, CONNECTIONNAME, OBJECTALITERALVALUE,
                                              SUBJECTUNTYPED, OBJECTUNTYPED)
      
      // define enums used as keys for process meta info hashmap
      val PROCESS, DATE, OUTPUTNAMEDGRAPH, METAQUERY, PROCESSRUNTIME, TRIPLESADDED, REPLACEMENTSTRING, INPUTNAMEDGRAPHS, PROCESSSPECIFICATION = Value

      val manyToOneMultiplicity = "https://github.com/PennTURBO/Drivetrain/many-1"
      val oneToManyMultiplicity = "https://github.com/PennTURBO/Drivetrain/1-many"
      val oneToOneMultiplicity = "https://github.com/PennTURBO/Drivetrain/1-1"
      
      val instToInstRecipe = "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe"
      val instToTermRecipe = "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe"
      val termToInstRecipe = "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe"
      val instToLiteralRecipe = "https://github.com/PennTURBO/Drivetrain/InstanceToLiteralRecipe"
      val termToLiteralRecipe = "https://github.com/PennTURBO/Drivetrain/TermToLiteralRecipe"
      val termToTermRecipe = "https://github.com/PennTURBO/Drivetrain/TermToTermRecipe"
      
      val subjectSingleton = Array("https://github.com/PennTURBO/Drivetrain/singleton-many", "https://github.com/PennTURBO/Drivetrain/singleton-superSingleton", "https://github.com/PennTURBO/Drivetrain/singleton-singleton")
      val subjectSuperSingleton = Array("https://github.com/PennTURBO/Drivetrain/superSingleton-many", "https://github.com/PennTURBO/Drivetrain/superSingleton-singleton", "https://github.com/PennTURBO/Drivetrain/superSingleton-superSingleton")
      val objectSingleton = Array("https://github.com/PennTURBO/Drivetrain/many-singleton", "https://github.com/PennTURBO/Drivetrain/superSingleton-singleton", "https://github.com/PennTURBO/Drivetrain/singleton-singleton")
      val objectSuperSingleton = Array("https://github.com/PennTURBO/Drivetrain/many-superSingleton", "https://github.com/PennTURBO/Drivetrain/singleton-superSingleton", "https://github.com/PennTURBO/Drivetrain/superSingleton-superSingleton")
}