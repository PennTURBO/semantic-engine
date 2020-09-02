package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.FileReader

object DrivetrainDriver {
  
  val logger = LoggerFactory.getLogger(getClass)
  
  def main(args: Array[String]): Unit =
  {
      val globalUUID = UUID.randomUUID().toString().replaceAll("-", "")
      var graphDBMaterials: TurboGraphConnection = null
      if (args.size == 0) logger.info("At least one command line argument required to run the drivetrain application.")
      else
      {
          assert("main" == System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"main\"; check your build.sbt file")
          if (args(0) == "buildTest") buildAutomatedTest(args)
          else if (args(0) == "debug") runDebugMode(args)
          else
          {
              try
              {   
                  graphDBMaterials = ConnectToGraphDB.initializeGraph()
              
                  Globals.gmCxn = graphDBMaterials.getGmConnection()
                  Globals.gmRepoManager = graphDBMaterials.getGmRepoManager()
                  Globals.gmRepository = graphDBMaterials.getGmRepository() 
              
                  Globals.cxn = graphDBMaterials.getConnection()
                  Globals.repoManager = graphDBMaterials.getRepoManager()
                  Globals.repository = graphDBMaterials.getRepository()
                  
                  val graphModelValidator = new GraphModelValidator()
                  
                  if (Globals.cxn == null || Globals.gmCxn == null) logger.info("There was a problem initializing the graph. Please check your properties file for errors.")
                  // utility method that loads a RDF file into the production repository - format is hardcoded as the 3rd argument
                  else if (args(0) == "loadRepoFromFile") Utilities.loadDataFromFile(Globals.cxn, args(1), RDFFormat.RDFXML)
                  // utility method that loads triples from a URI into the production repository - format is hardcoded as the 4th argument
                  else if (args(0) == "loadRepoFromUrl") OntologyLoader.addOntologyFromUrl(Globals.cxn, args(1), args(1), RDFFormat.RDFXML)
                  // loads application ontology specified in properties file into production repository
                  else if (args(0) == "loadOntologyToProductionRepo") OntologyLoader.addOntologyFromUrl(Globals.cxn)
                  // loads application ontology specified in properties file into model repository
                  else if (args(0) == "loadOntologyToModelRepo") OntologyLoader.addOntologyFromUrl(Globals.cxn)
                  else if (args(0) == "updateModel") updateModel(graphDBMaterials)
                  // this means run all update specifications in order, not run all possible commands
                  else if (args(0) == "allUpdates")
                  {
                      updateModel(graphDBMaterials)
                      clearProductionNamedGraphs()
                      runAllDrivetrainProcesses(globalUUID)
                  }
                  // prints out the query for a given update spec...does not run anything against the DB
                  else if (args(0) == "printQueryForUpdate")
                  {
                      if (args.size < 2) logger.info("Must provide a process URI after printQuery declaration")
                      else 
                      {
                          updateModel(graphDBMaterials)
                          RunDrivetrainProcess.setGlobalUUID(globalUUID)
                          graphModelValidator.checkAcornFilesForMissingTypes()
                          if (Globals.validateAgainstOntology) graphModelValidator.validateGraphSpecificationAgainstOntology()
                          val query = RunDrivetrainProcess.createPatternMatchQuery(args(1))
                          if (query != null)
                          {
                              logger.info("Here is the SPARQL statement for the process you requested.")
                              println(query.getQuery()) 
                          }
                      }
                  }
                  else if (args(0) == "singleUpdate")
                  {
                      if (args.size < 2) logger.info("Must provide a process URI after run singleUpdate declaration")
                      else
                      {   
                          updateModel(graphDBMaterials)
                          clearProductionNamedGraphs()

                          val processAsURI = Utilities.getProcessNameAsUri(args(1))
                          graphModelValidator.validateProcessSpecification(processAsURI)
                          
                          logger.info("Note that running individual Drivetrain Updates is recommended for testing only. To run the full stack, use 'run allUpdates'")
                          RunDrivetrainProcess.setGlobalUUID(globalUUID)
                          graphModelValidator.checkAcornFilesForMissingTypes()
                          if (Globals.validateAgainstOntology) graphModelValidator.validateGraphSpecificationAgainstOntology()
                          val thisProcess = 
                          RunDrivetrainProcess.runProcess(processAsURI)    
                      }
                  }
                  else if (args(0) == "validateModel")
                  {
                      updateModel(graphDBMaterials)
                      
                      graphModelValidator.checkAcornFilesForMissingTypes()
                      
                      val processes = Utilities.getAllProcessesInOrder(Globals.gmCxn)
                      graphModelValidator.validateProcessesAgainstGraphSpecification(processes)
                      
                      val modelReader = new GraphModelReader()
                      
                      for (process <- processes)
                      {
                          graphModelValidator.validateConnectionRecipesInProcess(process)
                          graphModelValidator.validateConnectionRecipeTypeDeclarations(process)  
                          
                          val inputs = modelReader.getInputs(process)
                          val outputs = modelReader.getOutputs(process)
                          val removals = modelReader.getRemovals(process)
                          val modelInterpreter = new GraphModelInterpreter()
                          
                          val (inputRecipeList, outputRecipeList, removalsRecipeList) = modelInterpreter.handleAcornData(inputs, outputs, removals)
                          graphModelValidator.validateAcornResults(inputRecipeList, outputRecipeList)
                      }
                      
                      graphModelValidator.validateGraphSpecificationAgainstOntology()
                      logger.info("Validation checks passed")
                  }
                  else logger.info("Unrecognized command line argument " + args(0))
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
  
  def updateModel(graphConnect: TurboGraphConnection, instructionSetFile: String = Globals.instructionSetFile, graphSpecFile: String = Globals.graphSpecificationFile, acornOntology: String = Globals.acornOntologyFile)
  {
      try
      {
          // update data model and ontology upon establishing connection
          DrivetrainDriver.updateModel(graphConnect.getGmConnection(), instructionSetFile, graphSpecFile, acornOntology)
          OntologyLoader.addOntologyFromUrl(graphConnect.getGmConnection())
      }
      catch
      {
          case e: RuntimeException => ConnectToGraphDB.closeGraphConnection(graphConnect, false)
      }
  }
  
  def updateModel(gmCxn: RepositoryConnection, instructionSetFile: String, graphSpecFile: String, acornOntology: String)
  {
      logger.info("Updating transformation instructions using file " + instructionSetFile)
      val graph = Globals.defaultPrefix + "instructionSet"
      Utilities.deleteAllTriplesInDatabase(gmCxn)
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
                      // prefixes stored but not added to graph
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
      //logger.info(query)
      SparqlUpdater.updateSparql(gmCxn, query)
      
      logger.info("Updating graph specification using file " + graphSpecFile)
      val graphSpecGraph = Globals.defaultPrefix + "graphSpecification"
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
                      // prefixes stored but not added to graph
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
      //logger.info(query)
      SparqlUpdater.updateSparql(gmCxn, query)
      
      logger.info("Updating Acorn ontology using file " + acornOntology)
      val acornOntologyGraph = Globals.defaultPrefix + "acornOntology"
      query = s"INSERT DATA { Graph <$acornOntologyGraph> {"
      val acornBr = io.Source.fromFile(s"ontologies//"+Globals.acornOntologyFile)
      for (line <- acornBr.getLines())
      {
          if (line.size > 0)
          {
              if (line.charAt(0) != '#')
              {
                  if (line.charAt(0) != '@') query += line+"\n"
                  else
                  {
                      // prefixes stored but not added to graph
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
      //logger.info(query)
      SparqlUpdater.updateSparql(gmCxn, query)
      
      br.close()
  }
  
  def runAllDrivetrainProcesses(globalUUID: String)
  {
      //load the TURBO ontology
      OntologyLoader.addOntologyFromUrl(Globals.cxn)
      RunDrivetrainProcess.runAllDrivetrainProcesses(globalUUID)
      if (Globals.reinferRepo)
      {
          logger.info("setting reasoning to rdf plus")
          ReasoningManager.setReasoningToRdfPlus(Globals.cxn)
          ReasoningManager.setReasoningToNone(Globals.cxn) 
      }
      if (Globals.loadAdditionalOntologies)
      {
          logger.info("loading extra ontologies")
          OntologyLoader.loadRelevantOntologies(Globals.cxn)
      }
  }
  
  def clearProductionNamedGraphs()
  {
      if (Globals.clearGraphsAtStart)
      {
          logger.info("Clearing production named graphs...")
          Utilities.clearNamedGraph(Globals.cxn, Globals.processNamedGraph)
          Utilities.clearNamedGraph(Globals.cxn, Globals.expandedNamedGraph)
      }
  }
  
  def buildAutomatedTest(args: Array[String])
  {
      // get connection to test repo
      val graphDbTestConnectionDetails = ConnectToGraphDB.getTestRepositoryConnection()
      Globals.cxn = graphDbTestConnectionDetails.getConnection()
      Globals.gmCxn = graphDbTestConnectionDetails.getGmConnection()
      val graphModelValidator = new GraphModelValidator()
      
      try
      {
          RunDrivetrainProcess.setMultithreading(false)
          
          var buildArray = new ArrayBuffer[String]
          if (!(args.size > 1)) 
          {
              logger.info(s"No URI found as argument, all update specifications in instruction set " + Globals.instructionSetFile + " will be processed.")
              buildArray = Utilities.getAllProcessInInstructionSet(Globals.gmCxn)
          }
          else buildArray = ArrayBuffer(args(1))
          val testBuilder = new TestBuilder()
          
          for (process <- buildArray)
          {
              Utilities.deleteAllTriplesInDatabase(Globals.cxn)
              val processAsURI = Utilities.getProcessNameAsUri(process)
              graphModelValidator.validateProcessSpecification(processAsURI)
              logger.info(s"Building test for process $processAsURI")
              testBuilder.buildTest(Globals.cxn, Globals.gmCxn, processAsURI)  
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
      Globals.cxn = graphDbTestConnectionDetails.getConnection()
      Globals.gmCxn = graphDbTestConnectionDetails.getGmConnection()
      
      try
      {
          RunDrivetrainProcess.setMultithreading(false)
          
          var buildArray = new ArrayBuffer[String]
          val nonProcessArgs = Array("debug", "--min")
          if (nonProcessArgs.contains(args(args.size-1)))
          {
              logger.info(s"No URI found as argument, all update specifications in instruction set " +Globals.instructionSetFile + " will be processed.")
              buildArray = Utilities.getAllProcessesInOrder(Globals.gmCxn)
          }
          else buildArray = ArrayBuffer(args(args.size-1))
          val testBuilder = new TestBuilder()
          
          Utilities.deleteAllTriplesInDatabase(Globals.cxn)
          if (args.size > 1 && args(1) == "--min") testBuilder.postMinTripleOutput(Globals.cxn, Globals.gmCxn, buildArray)
          else testBuilder.postMaxTripleOutput(Globals.cxn, Globals.gmCxn, buildArray)
          logger.info("Your requested output data is available in the testing repository.")
      }
      finally
      {
          ConnectToGraphDB.closeGraphConnection(graphDbTestConnectionDetails)
      }
   }
}