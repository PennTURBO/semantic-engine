package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer

object DrivetrainDriver extends ProjectwideGlobals {
  val connect: ConnectToGraphDB = new ConnectToGraphDB
  val sparqlChecks: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
  val expand: Expander = new Expander()
  val reftrack: ReferentTracker = new ReferentTracker()
  val join: EntityLinker = new EntityLinker()
  val conclusionate: Conclusionator = new Conclusionator()
  val diagmap: DiagnosisMapper = new DiagnosisMapper()
  val i2i2c2c: I2I2C2C = new I2I2C2C()
  val medmap: MedicationMapper = new MedicationMapper()
  val benchmark: DrivetrainAutomatedBenchmarking = new DrivetrainAutomatedBenchmarking()
  val visualize: DrivetrainVisualizations = new DrivetrainVisualizations()
  val ontLoad: OntologyLoader = new OntologyLoader
  val reasoner: ReasoningManager = new ReasoningManager
  //val future: Futures = new Futures()
  
  //globally available Conclusionation Named Graph IRI
  var concNamedGraph: Option[IRI] = None : Option[IRI]
  var instantiation: Option[IRI] = None : Option[IRI]
  
  /**
   * DrivetrainDriver (ne Presentation Driver) contains the main method for calling Drivetrain functions. Main can be called using "all .51 .51" to run full Drivetrain stack, 
   * or run piecewise using individual commands. Full documentation can be found in the "Usage" section of the Drivetrain documentation. Also has capacity to run benchmarking
   * script if "benchmark" is received as an argument.
   */
  
  def main(args: Array[String]): Unit =
  {
      if (args.size == 0) logger.info("At least one command line argument required to run the drivetrain application.")
      else if (args(0) == "benchmark") benchmark.runBenchmarking(args)
      else
      {
          var cxn: RepositoryConnection = null
          var repoManager: RemoteRepositoryManager = null
          var repository: Repository = null
          try
          {
              val graphDBMaterials: TurboGraphConnection = connect.initializeGraph(true)
              cxn = graphDBMaterials.getConnection()
              repoManager = graphDBMaterials.getRepoManager()
              repository = graphDBMaterials.getRepository() 
              if (cxn == null) logger.info("There was a problem initializing the graph. Please check your properties file for errors.")
              else if (args(0) == "all")
              {
                  val thresholds: Option[Array[Double]] = checkConclusionatorArguments(args)
                  if (thresholds != None) 
                  {
                      connect.loadDataFromPropertiesFile(cxn)
                      var postexpandProceed: Boolean = true
                      if (args.size > 3)
                      {
                          if (args(3) == "--skipchecks") postexpandProceed = runExpansion(cxn, false)
                          else postexpandProceed = runExpansion(cxn)
                      }
                      else postexpandProceed = runExpansion(cxn)
                      if (postexpandProceed)
                      {
                          runReferentTracking(cxn)
                          runEntityLinking(cxn, true)
                          val concProceed = runConclusionating(cxn, thresholds.get(0), thresholds.get(1))
                          if (concProceed) 
                          {
                              runDiagnosisMapping(cxn)
                              runMedicationMapping(cxn)
                              runInferenceWithAddedOntologies(cxn)
                          }
                      }
                  }
              }
              else if (args(0) == "dataload") connect.loadDataFromPropertiesFile(cxn)
              else if (args(0) == "expand")
              {
                  if (args.size > 1)
                  {
                      if (args(1) == "--skipchecks") runExpansion(cxn, false)
                      else runExpansion(cxn)
                  }
                  else runExpansion(cxn)
              }
              else if (args(0) == "reftrack") runReferentTracking(cxn)
              else if (args(0) == "entlink" && args.size > 1) 
              {
                  if (args(1) == "true") runEntityLinking(cxn, true)
                  else if (args(1) == "false") runEntityLinking(cxn, false)
                  else logger.info("Second argument for entity linking should be boolean - true to load LOF data, false to not load LOF data")
              }
              else if (args(0) == "conclusionate") 
              {
                  val thresholds: Option[Array[Double]] = checkConclusionatorArguments(args)
                  if (thresholds != None) runConclusionating(cxn, thresholds.get(0), thresholds.get(1))
              }
              else if (args(0) == "diagmap") runDiagnosisMapping(cxn)
              else if (args(0) == "medmap") runMedicationMapping(cxn)
              else if (args(0) == "i2i2c2c") runI2i2c2cMapping(cxn, args)
              else if (args(0) == "reasoner") runInferenceWithAddedOntologies(cxn)
              else if (args(0) == "loadRepo") helper.loadDataFromFile(cxn, args(1), RDFFormat.TURTLE)
              else if (args(0) == "loadTurboOntology") ontLoad.addOntologyFromUrl(cxn)
              else if (args(0) == "visualize") visualize.createDrivetrainVisualizations(cxn)
              else if (args(0) == "clearInferred") helper.removeInferredStatements(cxn)
              else if (args(0) == "validateRepository") validateDataInRepository(cxn)
              else logger.info("Unrecognized command line argument " + args(0) + ", no action taken")
          }
          finally 
          {
              connect.closeGraphConnection(cxn, repoManager, repository, false)
          }
      }
  }
  
  def checkConclusionatorArguments(args: Array[String]): Option[Array[Double]] =
  {
      var thresholds: Option[Array[Double]] = None : Option[Array[Double]]
      thresholds = None
      var biosexThreshold: Option[Double] = None : Option[Double]
      var dobThreshold: Option[Double] = None : Option[Double]
      if (args.size < 3) logger.info("Not enough arguments supplied. Context and biosex, dob conclusionation thresholds required.")
      else
      {
          try
          {
              biosexThreshold = Some(args(1).toDouble)
              dobThreshold = Some(args(2).toDouble)
          }
          catch
          {
              case e: NumberFormatException => logger.info("One or both thresholds supplied as arguments is not parseable.")
          }
          if (biosexThreshold != None && dobThreshold != None) thresholds = Some(Array(biosexThreshold.get, dobThreshold.get))
      }
      thresholds
  }
  
  def runExpansion(cxn: RepositoryConnection, runChecks: Boolean = true): Boolean =
  {
      logger.info("running expansion")
      var postcheckProceed: Boolean = true
      var precheckProceed: Boolean = true
      logger.info("Starting pre expansion checks")
      if (runChecks) precheckProceed = sparqlChecks.preExpansionChecks(cxn)
      logger.info("pre expansion checks passed: " + precheckProceed)
      if (precheckProceed)
      {
          helper.applySymmetricalProperties(cxn)
          instantiation = Some(expand.expandAllShortcutEntities(cxn))
          logger.info("Encounters and participants have been expanded.")
          if (runChecks) postcheckProceed = sparqlChecks.postExpansionChecks(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "post-expansion")
          logger.info("Post expansion checks passed: " + postcheckProceed)
          if (postcheckProceed)
          {
              helper.moveDataFromOneNamedGraphToAnother(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "http://www.itmat.upenn.edu/biobank/expanded")
              logger.info("Moved triples to expanded graph")
              helper.clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck")
              logger.info("post expansion named graph cleared")
              helper.clearShortcutNamedGraphs(cxn)
              logger.info("Shortcut named graph cleared")
              //Is it really necessary to apply inverses here? Maybe just at the end?
              //helper.applyInverses(cxn)
              helper.addStringLabelsToOntology(cxn)
              logger.info("applied inverses and string labels")
              logger.info("New data is ready for Referent Tracking")
          }
          else logger.info("Post-expansion checks failed!!! Do not proceed with Referent Tracking")
      }
      else logger.info("Pre-expansion checks failed!!! Do not proceed with Referent Tracking")
      if (postcheckProceed && precheckProceed) true
      else false
  }
  
  def runReferentTracking(cxn: RepositoryConnection)
  {
      logger.info("running reftracking")
      reftrack.runAllReftrackProcesses(cxn)
      logger.info("All referent tracking complete")
  }
  
  def runEntityLinking(cxn: RepositoryConnection, loadLOFData: Boolean)
  {
      logger.info("starting entity linking")
      join.joinParticipantsAndEncounters(cxn)
      logger.info("loading LOF data")
      if (loadLOFData) connect.loadDataFromPropertiesFile(cxn, inputLOFFiles, "LOFShortcuts", false)
      val lofGraphs: ArrayBuffer[String] = helper.generateNamedGraphsListFromPrefix(cxn, "http://www.itmat.upenn.edu/biobank/LOFShortcuts")
      logger.info("connecting biobank encounters to LOF data")
      join.connectLossOfFunctionToBiobankEncounters(cxn, lofGraphs)
      logger.info("expanding LOF data")
      if (instantiation == None) expand.expandLossOfFunctionShortcuts(cxn, helper.genTurboIRI(cxn), lofGraphs)
      else expand.expandLossOfFunctionShortcuts(cxn, instantiation.get, lofGraphs)
      expand.createErrorTriplesForUnexpandedAlleles(cxn, lofGraphs)
      logger.info("All entity linking complete")
  }
  
  def runConclusionating(cxn: RepositoryConnection, biosexThreshold: Double, dateofbirthThreshold: Double): Boolean =
  {
      concNamedGraph = Some(conclusionate.runConclusionationProcess(cxn, biosexThreshold, dateofbirthThreshold))
      logger.info("Finished conclusionation process")
      logger.info("Applying inverses and symmetrical properties")
      //helper.applyInverses(cxn, concNamedGraph.get)
      logger.info("finished inverses, doing symm props")
      helper.applySymmetricalProperties(cxn)
      if (applyLabels == "true")
      {
          logger.info("applying labels")
          helper.addLabelsToEverything(cxn, "http://www.itmat.upenn.edu/biobank/expanded")
          helper.addLabelsToEverything(cxn, "http://www.itmat.upenn.edu/biobank/entityLinkData")
          helper.addLabelsToEverything(cxn, concNamedGraph.get.toString)
      }
      logger.info("running post-conclusionation checks")
      validateDataInRepository(cxn)
  }
  
  def validateDataInRepository(cxn: RepositoryConnection): Boolean =
  {
      var concCheck: Boolean = true
      val expandedGraphCheck = sparqlChecks.postExpansionChecks(cxn, "http://www.itmat.upenn.edu/biobank/expanded", "post-conclusion")
      if (!expandedGraphCheck) logger.info("Checks failed in expanded graph!")
      else
      {
          val concNamedGraphs: ArrayBuffer[String] = helper.generateNamedGraphsListFromPrefix(cxn, "http://www.itmat.upenn.edu/biobank/Conclusionations")
          var a: Int = 0
          while (concCheck && a < concNamedGraphs.size)
          {
              if (sparqlChecks.postExpansionChecks(cxn, concNamedGraphs(a), "post-conclusion") == false) concCheck = false
          }
          if (!concCheck) logger.info("Checks failed in a conclusions graph!")
      }
      if (expandedGraphCheck && concCheck) true
      else false
  }
  
  def runDiagnosisMapping(cxn: RepositoryConnection)
  {
      if (loadDiseaseOntologies == "true")
      {
          ontLoad.addDiseaseOntologies(cxn)
          diagmap.addDiseaseOntologies(cxn)
      }
      logger.info("diagnosis mapping currently deprecated")
      //diagmap.performDiagnosisMapping(cxn)
  }
  
  def runMedicationMapping(cxn: RepositoryConnection)
  {
      if (loadDrugOntologies == "true") ontLoad.addDrugOntologies(cxn)
      medmap.runMedicationMapping(cxn)
  }
  
  def runInferenceWithAddedOntologies(cxn: RepositoryConnection)
  {
      reasoner.changeReasoningLevel(cxn, "rdfsplus-optimized")
      reasoner.reinferRepository(cxn)
      reasoner.changeReasoningLevel(cxn, "empty")
      reasoner.reinferRepository(cxn)
      
      ontLoad.addMiscOntologies(cxn)
  }
  
  def runI2i2c2cMapping(cxn: RepositoryConnection, args: Array[String])
  {
      i2i2c2c.runAllI2I2C2CQueries(cxn)
      if (args.size > 1)
      {
          if (args(1) == "export")
          {
              i2i2c2c.exportCSVsFromQueries(cxn)   
          }
      }
  }
}