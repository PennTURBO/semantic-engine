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
              
              val instantiationURI = "http://www.itmat.upenn.edu/biobank/" + UUID.randomUUID().toString().replaceAll("-", "")
              
              if (cxn == null || gmCxn == null) logger.info("There was a problem initializing the graph. Please check your properties file for errors.")
              else if (args(0) == "loadRepoFromFile") helper.loadDataFromFile(cxn, args(1), RDFFormat.NQUADS)
              else if (args(0) == "loadRepoFromUrl") OntologyLoader.addOntologyFromUrl(cxn, args(1), Map(args(2) -> RDFFormat.RDFXML))
              else if (args(0) == "loadTestTurboOntology") OntologyLoader.addOntologyFromUrl(testCxn)
              else if (args(0) == "updateModelOntology") OntologyLoader.addOntologyFromUrl(gmCxn)
              else if (args(0) == "updateModel") updateModel(gmCxn)
              else if (args(0) == "all") runAllDrivetrainProcesses(cxn, gmCxn, globalUUID)
              else if (args(0) == "printQuery")
              {
                  if (args.size < 2) logger.info("Must provide a process URI after printQuery declaration")
                  else 
                  {
                      RunDrivetrainProcess.setInstantiation(instantiationURI)
                      RunDrivetrainProcess.setGlobalUUID(globalUUID)
                      RunDrivetrainProcess.setGraphModelConnection(gmCxn)
                      RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
                      val query = RunDrivetrainProcess.createPatternMatchQuery(args(1))
                      if (query != null)
                      {
                          logger.info("Here is the SPARQL statement for the process you requested.")
                          logger.info(query.getQuery()) 
                      }
                  }
              }
              else if (args(0).startsWith("http://transformunify.org/ontologies/"))
              {
                  //load the TURBO ontology
                  OntologyLoader.addOntologyFromUrl(cxn)
                  
                  logger.info("Note that running individual Drivetrain processes is recommended for testing only. To run the full stack, use 'run all'")
                  RunDrivetrainProcess.setInstantiation(instantiationURI)
                  RunDrivetrainProcess.setGlobalUUID(globalUUID)
                  RunDrivetrainProcess.setGraphModelConnection(gmCxn)
                  RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
                  RunDrivetrainProcess.runProcess(args(0))
              }
              else logger.info("Ontology loaded in production repo, no further action taken.")
          }
          catch
          {
              case e: RuntimeException => 
                logger.info("exception thrown:" + e.printStackTrace())
                if (cxn != null) ConnectToGraphDB.closeGraphConnection(graphDBMaterials, false)
          }
          finally 
          {
              if (cxn != null) ConnectToGraphDB.closeGraphConnection(graphDBMaterials, false)
          }
      }
  }
  
  def updateModel(gmCxn: RepositoryConnection)
  {
      logger.info("Updating graph model...")
      val graph = "http://www.itmat.upenn.edu/biobank/dataModel"
      helper.clearNamedGraph(gmCxn, graph)
      var query = s"INSERT DATA { Graph <$graph> {"
      var prefixes = ""
      val br = io.Source.fromFile("ontologies//turbo_dataModel_file.ttl")
      for (line <- br.getLines())
      {
          if (line.size > 0)
          {
              if (line.charAt(0) != '#') 
              {
                  if (line.charAt(0) != '@') query += line 
                  else
                  {
                      var formattedPrefix = line.substring(1, line.size-1)
                      prefixes += formattedPrefix+"\n"
                  }
              }
          }
      }
      query += "}}"
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
}