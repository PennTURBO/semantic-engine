package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

import java.io.BufferedReader
import java.io.FileReader

object DrivetrainDriver extends ProjectwideGlobals {
  
  def main(args: Array[String]): Unit =
  {
      val globalUUID = UUID.randomUUID().toString().replaceAll("-", "")
      if (args.size == 0) logger.info("At least one command line argument required to run the drivetrain application.")
      //else if (args(0) == "benchmark") benchmark.runBenchmarking(args, globalUUID)
      else
      {
          assert("main" == System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"main\"; check your build.sbt file")
          if (args(0) == "buildTest") buildAutomatedTest(args)
          else if (args(0) == "debug") runDebugMode(args)
          else
          {
              try
              {   
                  graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData()
              
                  gmCxn = graphDBMaterials.getGmConnection()
                  gmRepoManager = graphDBMaterials.getGmRepoManager()
                  gmRepository = graphDBMaterials.getGmRepository() 
              
                  cxn = graphDBMaterials.getConnection()
                  repoManager = graphDBMaterials.getRepoManager()
                  repository = graphDBMaterials.getRepository()
                  
                  val instantiationURI = defaultPrefix + UUID.randomUUID().toString().replaceAll("-", "")
                  
                  if (cxn == null || gmCxn == null) logger.info("There was a problem initializing the graph. Please check your properties file for errors.")
                  else if (args(0) == "loadRepoFromFile") helper.loadDataFromFile(cxn, args(1), RDFFormat.RDFXML)
                  else if (args(0) == "loadRepoFromUrl") OntologyLoader.addOntologyFromUrl(cxn, args(1), Map(args(2) -> RDFFormat.RDFXML))
                  else if (args(0) == "loadTestTurboOntology") OntologyLoader.addOntologyFromUrl(cxn)
                  else if (args(0) == "updateModelOntology") OntologyLoader.addOntologyFromUrl(gmCxn)
                  else if (args(0) == "updateModel") logger.info("model updated")
                  else if (args(0) == "all")
                  {
                      clearProductionNamedGraphs(cxn)
                      runAllDrivetrainProcesses(cxn, gmCxn, globalUUID)
                  }
                  else if (args(0) == "printQuery")
                  {
                      if (args.size < 2) logger.info("Must provide a process URI after printQuery declaration")
                      else 
                      {
                          RunDrivetrainProcess.setGlobalUUID(globalUUID)
                          RunDrivetrainProcess.setGraphModelConnection(gmCxn)
                          RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
                          GraphModelValidator.checkAcornFilesForMissingTypes()
                          if (validateAgainstOntology) GraphModelValidator.validateGraphSpecificationAgainstOntology()
                          val query = RunDrivetrainProcess.createPatternMatchQuery(args(1))
                          if (query != null)
                          {
                              logger.info("Here is the SPARQL statement for the process you requested.")
                              println(query.getQuery()) 
                          }
                      }
                  }
                  else
                  {
                      RunDrivetrainProcess.setGraphModelConnection(gmCxn)
                      RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
                      GraphModelValidator.validateProcessSpecification(helper.getProcessNameAsUri(args(0)))
                      
                      //load the TURBO ontology
                      //OntologyLoader.addOntologyFromUrl(cxn)
                      clearProductionNamedGraphs(cxn)
                      
                      logger.info("Note that running individual Drivetrain processes is recommended for testing only. To run the full stack, use 'run all'")
                      RunDrivetrainProcess.setGlobalUUID(globalUUID)
                      GraphModelValidator.checkAcornFilesForMissingTypes()
                      if (validateAgainstOntology) GraphModelValidator.validateGraphSpecificationAgainstOntology()
                      val thisProcess = helper.getProcessNameAsUri(args(0))
                      RunDrivetrainProcess.runProcess(thisProcess)   
                  }
              }
              catch
              {
                  case e: RuntimeException => 
                    logger.info("exception thrown:" + e.printStackTrace())
                    ConnectToGraphDB.closeGraphConnection(graphDBMaterials, false)
              }
              finally 
              {
                  ConnectToGraphDB.closeGraphConnection(graphDBMaterials, false)
              }
          }
      }
  }
  
  def updateModel(gmCxn: RepositoryConnection, instructionSetFile: String = instructionSetFile, graphSpecFile: String = graphSpecificationFile, acornOntology: String = acornOntologyFile)
  {
      logger.info("Updating graph model using file " + instructionSetFile)
      val graph = s"$defaultPrefix" + "instructionSet"
      helper.deleteAllTriplesInDatabase(gmCxn)
      var query = s"INSERT DATA { Graph <$graph> {"
      var prefixes = ""
      var br: scala.io.BufferedSource = null
      if (instructionSetFile == "testing_instruction_set.tis") br = io.Source.fromFile(s"src//test//scala//edu//upenn//turbo//config_for_testing//testing_instruction_set.tis")
      else br = io.Source.fromFile(s"config//transformation_instruction_sets//$instructionSetFile")
      for (line <- br.getLines())
      {
          if (line.size > 0)
          {
              if (line.charAt(0) != '#')
              {
                  if (line.charAt(0) != '@') query += line+"\n"
                  else
                  {
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
      //logger.info(query)
      update.updateSparql(gmCxn, query)
      
      logger.info("Updating graph specification using file " + graphSpecFile)
      val graphSpecGraph = s"$defaultPrefix" + "graphSpecification"
      query = s"INSERT DATA { Graph <$graphSpecGraph> {"
      var graphSpecBr: scala.io.BufferedSource = null
      if (graphSpecFile == "testing_graph_specification.gs") graphSpecBr = io.Source.fromFile(s"src//test//scala//edu//upenn//turbo//config_for_testing//testing_graph_specification.gs")
      else graphSpecBr = io.Source.fromFile(s"config//graph_specifications//$graphSpecFile")
      for (line <- graphSpecBr.getLines())
      {
          if (line.size > 0)
          {
              if (line.charAt(0) != '#')
              {
                  if (line.charAt(0) != '@') query += line+"\n"
                  else
                  {
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
      //logger.info(query)
      update.updateSparql(gmCxn, query)
      
      logger.info("Updating Acorn ontology using file " + acornOntologyFile)
      val acornOntologyGraph = s"$defaultPrefix" + "acornOntology"
      query = s"INSERT DATA { Graph <$acornOntologyGraph> {"
      val acornBr = io.Source.fromFile(s"ontologies//$acornOntologyFile")
      for (line <- acornBr.getLines())
      {
          if (line.size > 0)
          {
              if (line.charAt(0) != '#')
              {
                  if (line.charAt(0) != '@') query += line+"\n"
                  else
                  {
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
      //logger.info(query)
      update.updateSparql(gmCxn, query)
      
      br.close()
  }
  
  def runAllDrivetrainProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String)
  {
      //load the TURBO ontology
      OntologyLoader.addOntologyFromUrl(cxn)
      RunDrivetrainProcess.runAllDrivetrainProcesses(cxn, gmCxn, globalUUID)
      if (reinferRepo)
      {
          logger.info("setting reasoning to rdf plus")
          ReasoningManager.setReasoningToRdfPlus(cxn)
          ReasoningManager.setReasoningToNone(cxn) 
      }
      if (loadAdditionalOntologies)
      {
          logger.info("loading extra ontologies")
          OntologyLoader.loadRelevantOntologies(cxn)
      }
  }
  
  def clearProductionNamedGraphs(cxn: RepositoryConnection)
  {
      if (clearGraphsAtStart)
      {
          logger.info("Clearing production named graphs...")
          helper.clearNamedGraph(cxn, processNamedGraph)
          helper.clearNamedGraph(cxn, expandedNamedGraph)
      }
  }
  
  def buildAutomatedTest(args: Array[String])
  {
      // get connection to test repo
      val graphDbTestConnectionDetails = ConnectToGraphDB.getTestRepositoryConnection()
      val testCxn = graphDbTestConnectionDetails.getConnection()
      val gmCxn = graphDbTestConnectionDetails.getGmConnection()
      
      try
      {
          RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
          RunDrivetrainProcess.setGraphModelConnection(gmCxn)
          RunDrivetrainProcess.setMultithreading(false)
          
          var buildArray = new ArrayBuffer[String]
          if (!(args.size > 1)) 
          {
              logger.info(s"No URI found as argument, all update specifications in instruction set $instructionSetFile will be processed.")
              buildArray = helper.getAllProcessInInstructionSet(gmCxn)
          }
          else buildArray = ArrayBuffer(args(1))
          val testBuilder = new TestBuilder()
          
          for (process <- buildArray)
          {
              helper.deleteAllTriplesInDatabase(testCxn)
              val processAsURI = helper.getProcessNameAsUri(process)
              GraphModelValidator.validateProcessSpecification(processAsURI)
              logger.info(s"Building test for process $processAsURI")
              testBuilder.buildTest(testCxn, gmCxn, processAsURI)  
          } 
      }
      finally
      {
          ConnectToGraphDB.closeGraphConnection(graphDbTestConnectionDetails)
      }
   }
  
  def runDebugMode(args: Array[String])
  {
      // get connection to test repo
      val graphDbTestConnectionDetails = ConnectToGraphDB.getTestRepositoryConnection()
      val testCxn = graphDbTestConnectionDetails.getConnection()
      val gmCxn = graphDbTestConnectionDetails.getGmConnection()
      
      try
      {
          RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
          RunDrivetrainProcess.setGraphModelConnection(gmCxn)
          RunDrivetrainProcess.setMultithreading(false)
          
          var buildArray = new ArrayBuffer[String]
          val nonProcessArgs = Array("debug", "--min")
          if (nonProcessArgs.contains(args(args.size-1)))
          {
              logger.info(s"No URI found as argument, all update specifications in instruction set $instructionSetFile will be processed.")
              buildArray = helper.getAllProcessesInOrder(gmCxn)
          }
          else buildArray = ArrayBuffer(args(args.size-1))
          val testBuilder = new TestBuilder()
          
          helper.deleteAllTriplesInDatabase(testCxn)
          if (args.size > 1 && args(1) == "--min") testBuilder.postMinTripleOutput(testCxn, gmCxn, buildArray)
          else testBuilder.postMaxTripleOutput(testCxn, gmCxn, buildArray)
          logger.info("Your requested output data is available in the testing repository.")
      }
      finally
      {
          ConnectToGraphDB.closeGraphConnection(graphDbTestConnectionDetails)
      }
   }
}