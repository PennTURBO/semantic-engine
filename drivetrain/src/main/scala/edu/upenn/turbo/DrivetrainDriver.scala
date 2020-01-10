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
          try
          {
              graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData()
              
              cxn = graphDBMaterials.getConnection()
              repoManager = graphDBMaterials.getRepoManager()
              repository = graphDBMaterials.getRepository()
              
              testCxn = graphDBMaterials.getTestConnection()
              testRepoManager = graphDBMaterials.getTestRepoManager()
              testRepository = graphDBMaterials.getTestRepository()
              
              gmCxn = graphDBMaterials.getGmConnection()
              gmRepoManager = graphDBMaterials.getGmRepoManager()
              gmRepository = graphDBMaterials.getGmRepository() 
              
              val instantiationURI = defaultPrefix + UUID.randomUUID().toString().replaceAll("-", "")
              
              if (cxn == null || gmCxn == null) logger.info("There was a problem initializing the graph. Please check your properties file for errors.")
              else if (args(0) == "loadRepoFromFile") helper.loadDataFromFile(cxn, args(1), RDFFormat.NQUADS)
              else if (args(0) == "loadRepoFromUrl") OntologyLoader.addOntologyFromUrl(cxn, args(1), Map(args(2) -> RDFFormat.RDFXML))
              else if (args(0) == "loadTestTurboOntology") OntologyLoader.addOntologyFromUrl(testCxn)
              else if (args(0) == "updateModelOntology") OntologyLoader.addOntologyFromUrl(gmCxn)
              else if (args(0) == "updateModel") updateModel(gmCxn)
              else if (args(0) == "buildTest") buildAutomatedTest(gmCxn, testCxn, args)
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
                          logger.info(query.getQuery()) 
                      }
                  }
              }
              else
              {
                  RunDrivetrainProcess.setGraphModelConnection(gmCxn)
                  RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
                  GraphModelValidator.validateProcessSpecification(helper.getProcessNameAsUri(args(0)))
                  
                  //load the TURBO ontology
                  OntologyLoader.addOntologyFromUrl(cxn)
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
  
  def updateModel(gmCxn: RepositoryConnection, instructionSetFile: String = instructionSetFile, graphSpecFile: String = graphSpecificationFile, acornOntology: String = acornOntologyFile)
  {
      logger.info("Updating graph model using file " + instructionSetFile)
      val graph = s"$defaultPrefix" + "instructionSet"
      helper.deleteAllTriplesInDatabase(gmCxn)
      var query = s"INSERT DATA { Graph <$graph> {"
      var prefixes = ""
      val br = io.Source.fromFile(s"ontologies//$instructionSetFile")
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
      val graphSpecBr = io.Source.fromFile(s"ontologies//$graphSpecFile")
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
  
  def buildAutomatedTest(gmCxn: RepositoryConnection, testCxn: RepositoryConnection, args: Array[String])
  {
      assert (args.size > 1, "No process specified for Automated Test Builder; please specify URI")
      RunDrivetrainProcess.setGraphModelConnection(gmCxn)
      RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
      val process = helper.getProcessNameAsUri(args(1))
      GraphModelValidator.validateProcessSpecification(process)
      def testBuilder = new TestBuilder()
      testBuilder.buildTest(testCxn, gmCxn, process)
  }
}