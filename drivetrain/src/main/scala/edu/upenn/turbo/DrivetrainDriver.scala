package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

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
  val ontLoad: OntologyLoader = new OntologyLoader()
  val reasoner: ReasoningManager = new ReasoningManager()
  val simpleBenchmark: SimpleBenchmark = new SimpleBenchmark()
  val graphOps: DrivetrainGraphOperations = new DrivetrainGraphOperations()
  
  //globally available Conclusionation Named Graph IRI
  var concNamedGraph: Option[IRI] = None : Option[IRI]
  var instantiation: Option[String] = None : Option[String]
  
  /**
   * DrivetrainDriver (ne Presentation Driver) contains the main method for calling Drivetrain functions. Main can be called using "all .51 .51" to run full Drivetrain stack, 
   * or run piecewise using individual commands. Full documentation can be found in the "Usage" section of the Drivetrain documentation. Also has capacity to run benchmarking
   * script if "benchmark" is received as an argument.
   */
  
  def main(args: Array[String]): Unit =
  {
      val globalUUID = UUID.randomUUID().toString().replaceAll("-", "")
      if (args.size == 0) logger.info("At least one command line argument required to run the drivetrain application.")
      else if (args(0) == "benchmark") benchmark.runBenchmarking(args, globalUUID)
      else
      {
          if (args(0) != "all") logger.info("Note that running Drivetrain with any command other than 'all' is supported for testing but should not be executed in production.")
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
                      //connect.loadDataFromPropertiesFile(cxn)
                      var postexpandProceed: Boolean = true
                      if (args.size > 3)
                      {
                          if (args(3) == "--skipchecks") postexpandProceed = runExpansion(cxn, globalUUID, false)
                          else postexpandProceed = runExpansion(cxn, globalUUID)
                      }
                      else postexpandProceed = runExpansion(cxn, globalUUID)
                      if (postexpandProceed)
                      {
                          runReferentTracking(cxn)
                          var loadLOF = true
                          if (loadLOFdata == "false") loadLOF = false 
                          runEntityLinking(cxn, globalUUID, loadLOF, instantiation.get)
                          val concProceed = runConclusionating(cxn, thresholds.get(0), thresholds.get(1))
                          if (concProceed) 
                          {
                              runInferenceWithAddedOntologies(cxn)
                              //runDiagnosisMapping(cxn)
                              //runMedicationMapping(cxn)
                          }
                      }
                  }
              }
              else if (args(0) == "dataload") connect.loadDataFromPropertiesFile(cxn)
              else if (args(0) == "expand")
              {
                  var runChecks: Boolean = true
                  var instantiation: String = ""
                  if (args.contains("--skipchecks")) runChecks = false
                  if (args.contains("--instantiation")) instantiation = args(args.indexOf("--instantiation")+1)
                  runExpansion(cxn, globalUUID, runChecks, instantiation)
              }
              else if (args(0) == "reftrack") runReferentTracking(cxn)
              else if (args(0) == "entlink" && args.size > 1) 
              {
                  var loadLOF = false
                  if (args(1) == "true") loadLOF = true
                  var instantiation: String = ""
                  if (args.contains("--instantiation")) instantiation = args(args.indexOf("--instantiation")+1)
                  runEntityLinking(cxn, globalUUID, loadLOF, instantiation)
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
              else if (args(0) == "loadRepoFromFile") helper.loadDataFromFile(cxn, args(1), RDFFormat.NQUADS)
              else if (args(0) == "loadRepoFromUrl") ontLoad.addOntologyFromUrl(cxn, args(1), Map(args(2) -> RDFFormat.RDFXML))
              else if (args(0) == "loadTurboOntology") ontLoad.addOntologyFromUrl(cxn)
              else if (args(0) == "visualize") visualize.createDrivetrainVisualizations(cxn)
              else if (args(0) == "clearInferred") helper.removeInferredStatements(cxn)
              else if (args(0) == "validateRepository") validateDataInRepository(cxn)
              else if (args(0) == "simpleBenchmark") simpleBenchmark.runSimpleBenchmark(cxn)
              else if (args(0) == "validateShortcuts") logger.info("shortcuts valid: " + sparqlChecks.preExpansionChecks(cxn))
              else if (args(0) == "buildQuery") buildQuery(cxn, globalUUID)
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
  
  def runExpansion(cxn: RepositoryConnection, globalUUID: String, runChecks: Boolean = true, instantiationInput: String = ""): Boolean =
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
          join.runPreExpansionLinking(cxn, globalUUID)
          if (instantiationInput == "") instantiation = Some(expand.expandAllShortcutEntities(cxn, globalUUID))
          else instantiation = Some(expand.expandAllShortcutEntities(cxn, globalUUID, instantiationInput))
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
  
  def runEntityLinking(cxn: RepositoryConnection, globalUUID: String, loadLOFData: Boolean, instantiation: String)
  {
      logger.info("starting entity linking")
      join.joinParticipantsAndEncounters(cxn)
      logger.info("loading LOF data")
      if (loadLOFData) connect.loadDataFromPropertiesFile(cxn, inputLOFFiles, "LOFShortcuts", false)
      val lofGraphs: ArrayBuffer[String] = helper.generateNamedGraphsListFromPrefix(cxn, "http://www.itmat.upenn.edu/biobank/LOFShortcuts")
      logger.info("connecting biobank encounters to LOF data")
      join.connectLossOfFunctionToBiobankEncounters(cxn, lofGraphs)
      logger.info("expanding LOF data")
      if (instantiation == "") expand.expandLossOfFunctionShortcuts(cxn, helper.genPmbbIRI(), lofGraphs, globalUUID)
      else expand.expandLossOfFunctionShortcuts(cxn, instantiation, lofGraphs, globalUUID)
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
      helper.consolidateLOFShortcutGraphs(cxn)
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
              if (!sparqlChecks.postExpansionChecks(cxn, concNamedGraphs(a), "post-conclusion")) concCheck = false
              a = a + 1
          }
          if (!concCheck) logger.info("Checks failed in a conclusions graph!")
      }
      if (expandedGraphCheck && concCheck) true
      else false
  }
  
  def runDiagnosisMapping(cxn: RepositoryConnection)
  {
      logger.info("diagnosis mapping currently deprecated")
      //diagmap.performDiagnosisMapping(cxn)
  }
  
  def runMedicationMapping(cxn: RepositoryConnection)
  {
      //medmap.runMedicationMapping(cxn)
  }
  
  def runInferenceWithAddedOntologies(cxn: RepositoryConnection)
  {
      reasoner.changeReasoningLevel(cxn, "rdfsplus-optimized")
      reasoner.reinferRepository(cxn)
      reasoner.changeReasoningLevel(cxn, "empty")
      reasoner.reinferRepository(cxn)
      
      ontLoad.addDrugOntologies(cxn)
      ontLoad.addDiseaseOntologies(cxn)
      ontLoad.addGeneOntologies(cxn)
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
  
  def buildQuery(cxn: RepositoryConnection, globalUUID: String)
  {
      val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
      val shortcutConsenter = new ShortcutConsenter()
      val consenter = new Consenter()
      
      val query = new Query()
      
      query.buildWhereClause(
          Map(shortcutConsenter -> true),
          Map())
          
      query.buildBindClause(Array(shortcutConsenter), randomUUID, globalUUID)

      val finalQuery = query.buildSelectQuery()
      println(finalQuery)
      println()
      
      //val queryResults = update.querySparqlAndUnpackToMap(cxn, sparqlPrefixes + finalQuery)

  }
}