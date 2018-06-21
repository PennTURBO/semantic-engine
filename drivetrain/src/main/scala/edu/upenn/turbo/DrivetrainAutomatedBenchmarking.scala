package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer
import java.io.PrintWriter
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.model.Value

/**
 * This class contains the Benchmarking script, which runs the Drivetrain full stack and records the time each segment takes to complete, as well as graph-based
 * statistics. Output can be found in the benchmarking folder. Can be called through the main class in DrivetrainDriver by issuing the command "benchmark." Full
 * documentation can be found in the "Usage" section of the Drivetrain documentation.
 */
class DrivetrainAutomatedBenchmarking extends ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB
    val sparqlChecks: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
    val expand: Expander = new Expander()
    val reftrack: ReferentTracker = new ReferentTracker()
    val join: EntityLinker = new EntityLinker()
    val dobConc: BirthdateConclusionator = new BirthdateConclusionator()
    val biosexConc: BiosexConclusionator = new BiosexConclusionator()
    val bmiConc: BMIConclusionator = new BMIConclusionator()
    val diagmap: DiagnosisMapper = new DiagnosisMapper()
    val medmap: MedicationMapper = new MedicationMapper()
    val encReftrack: EncounterReferentTracker = new EncounterReferentTracker()
    val partReftrack: ParticipantReferentTracker = new ParticipantReferentTracker()
    
    var conclusionationNamedGraph: IRI = null
    var tripCountInfo: String = ""
    var instantiation: IRI = null
    
    /**T
     * his holds the running time of any operations external to the full stack such as sparql queries to determine the number of nodes at a given stage.
     * It is subtracted from the total time at the end
     */
    var subtractFromTotal: Double = 0
  
    def runBenchmarking(args: Array[String]): Unit =
    {
        logger.info("Beginning benchmarking sequence")
        logger.info("Output available in turbo/benchmarking")
        val currTime: String = helper.getCurrentTimestamp()
        val csvFile: File = new File("..//benchmarking//benchmark_" + currTime + "//drivetrain_benchmark_" + currTime + ".csv")
        //make new folder for new benchmarking info
        csvFile.getParentFile.mkdirs()
        val writeCSV: PrintWriter = new PrintWriter(csvFile)
        val writeTXT: PrintWriter = new PrintWriter(new File
            ("..//benchmarking//benchmark_" + currTime + "//benchmark_info_" + currTime + ".txt"))
        
        //write info from properties file to text file in same folder as CSV benchmark info
        addBenchmarkInfoToTextFile(writeTXT, currTime)
        
        writeCSV.println("stage, seconds")
        writeCSV.println()
        
        var cxn: RepositoryConnection = null
        var repoManager: RemoteRepositoryManager = null
        var repository: Repository = null
          
        try
        {
            //get connection
            val startGetConnection = System.nanoTime()
            val graphDBMaterials: TurboGraphConnection = connect.initializeGraph(true)
            cxn = graphDBMaterials.getConnection()
            repoManager = graphDBMaterials.getRepoManager()
            repository = graphDBMaterials.getRepository()
            val connectionEstablished = System.nanoTime()
            writeCSV.println("Connect to Graph DB," + ((connectionEstablished - startGetConnection)/1000000000.0).toString)
            
            //empty repository before running drivetrain
            helper.deleteAllTriplesInDatabase(cxn)
            
            //run the full stack - write time measurements in real time
            logger.info("Starting timed drivetrain run")
            runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Drivetrain Time", benchmarkFullStack, true)
        }
        finally
        {
            writeTXT.close()
            writeCSV.close()  
            connect.closeGraphConnection(cxn, repoManager, repository, false) 
        }
    }
    
    def benchmarkFullStack(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {   
        countTriplesInDatabase(cxn, "Startup")
        
        //pre-expansion
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Pre-Expansion", benchmarkPreExpansion)
        
        countTriplesInDatabase(cxn, "Pre-Expansion")
        
        //expansion
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Expansion", benchmarkExpansion)
        
        countTriplesInDatabase(cxn, "Expansion")
        
        //post-expansion
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Post-Expansion", benchmarkPostExpansion)
        
        countTriplesInDatabase(cxn, "Post-expansion")
        
        //get info on number of pre-referent-tracking encounter/consenter nodes, print to writeTXT
        countDistinctOfType(cxn, writeTXT, Array("http://transformunify.org/ontologies/TURBO_0000502", 
            "http://transformunify.org/ontologies/TURBO_0000527", "http://purl.obolibrary.org/obo/OGMS_0000097", "http://purl.obolibrary.org/obo/PDRO_0000024",
            "http://purl.obolibrary.org/obo/OGMS_0000073"), Array("biobank consenter", "biobank encounter", "healthcare encounter", "drug prescription", 
            "diagnosis"), "pre-referent tracking")
        
        //referent tracking
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Referent Tracking", benchmarkReferentTracking)
        
        countTriplesInDatabase(cxn, "Referent Tracking")
        
        //get info on number of post-referent-tracking encounter/consenter nodes, print to writeTXT
        countDistinctOfType(cxn, writeTXT, Array("http://transformunify.org/ontologies/TURBO_0000502", 
            "http://transformunify.org/ontologies/TURBO_0000527", "http://purl.obolibrary.org/obo/OGMS_0000097", "http://purl.obolibrary.org/obo/PDRO_0000024",
            "http://purl.obolibrary.org/obo/OGMS_0000073"), Array("biobank consenter", "biobank encounter", "healthcare encounter", "drug prescription", 
            "diagnosis"), "post-referent tracking")
        
        //entity linking
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Entity Linking", benchmarkEntityLinking)
        
        countTriplesInDatabase(cxn, "Entity Linking")
        
        getLOFInfo(cxn, writeTXT)
        
        //conclusionating
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Conclusionating", benchmarkConclusionating)
        
        countTriplesInDatabase(cxn, "Conclusionating")
        
        //post-conclusionating
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Post-Conclusionating", benchmarkPostConclusionating)
        
        countTriplesInDatabase(cxn, "Post-Conclusionating")
        
        //diagnosis mapping
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Diagnosis Mapping", benchmarkDiagnosisMapping)
        
        countTriplesInDatabase(cxn, "Diagnosis Mapping")
        
        //medication mapping
        runBenchmarkingStage(cxn, writeCSV, writeTXT, "Total Medication Mapping", benchmarkMedicationMapping)
        
        countTriplesInDatabase(cxn, "Medication Mapping")
        
        //get info on number of diag codes/diagnosis registry/distinct medications
        getDiagAndMedInfo(cxn, writeTXT, "post med mapping")
        
        val start = System.nanoTime()
        writeTXT.println()
        writeTXT.println("TripleCount at each stage")
        writeTXT.println(tripCountInfo)
        val stop = System.nanoTime()
        subtractFromTotal += (stop - start)/1000000000.0
        logger.info("SUBTRACT FROM TOTAL VALUE: " + subtractFromTotal)
    }
    
    def benchmarkPreExpansion(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        //dataload - source files, TURBO ontology
        val startDataload = System.nanoTime()
        connect.loadDataFromPropertiesFile(cxn)
        val dataLoaded = System.nanoTime()
        
        writeCSV.println("Load Data (source files + turbo ontology)," + ((dataLoaded - startDataload)/1000000000.0).toString)
        
        //pre-expansion checks
        val startPreExpChecks = System.nanoTime()
        sparqlChecks.preExpansionChecks(cxn)
        val endPreExpChecks = System.nanoTime()
        
        writeCSV.println("Pre-Expansion Checks," + ((endPreExpChecks - startPreExpChecks)/1000000000.0).toString)
        
        //apply symmetrical properties
        val startAppSymmProps = System.nanoTime()
        helper.applySymmetricalProperties(cxn)
        val endAppSymmProps = System.nanoTime()
        
        writeCSV.println("Apply Symmetrical Properties," + ((endAppSymmProps - startAppSymmProps)/1000000000.0).toString)
    }
    
    def benchmarkExpansion(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        instantiation = helper.genPmbbIRI(cxn)
        
        //get list of shortcut named graphs
        val getGraphsListStart = System.nanoTime()
        val graphsList: String = helper.generateShortcutNamedGraphsString(cxn)
        val getGraphsListStop = System.nanoTime()
        
        writeCSV.println("Get List of Shortcut Graphs," + ((getGraphsListStop - getGraphsListStart)/1000000000.0).toString)
        
        //expand healthcare encounters
        val startExpHcEncs = System.nanoTime()
        expand.expandHealthcareEncounterShortcuts(cxn, instantiation, graphsList)
        val stopExpHcEncs = System.nanoTime()
        
        writeCSV.println("Expand Healthcare Encounters," + ((stopExpHcEncs - startExpHcEncs)/1000000000.0).toString)
        
        //expand biobank encounters
        val startExpBbEncs = System.nanoTime()
        expand.expandBiobankEncounterShortcuts(cxn, instantiation, graphsList)
        val stopExpBbEncs = System.nanoTime()
        
        writeCSV.println("Expand Biobank Encounters," + ((stopExpBbEncs - startExpBbEncs)/1000000000.0).toString)
        
        //expand biobank consenters
        val startExpBbCons = System.nanoTime()
        expand.participantExpansion(cxn, instantiation, graphsList)
        val stopExpBbCons = System.nanoTime()
        
        writeCSV.println("Expand Biobank Consenters," + ((stopExpBbCons - startExpBbCons)/1000000000.0).toString)
        
        //expand biobank encounter - biobank consenter joins
        val startExpBbConsToBbEncs = System.nanoTime()
        expand.biobankEncounterParticipantJoinExpansion(cxn, instantiation, graphsList)
        val stopExpBbConsToBbEncs = System.nanoTime()
        
        writeCSV.println("Expand Bb Enc to Bb Cons Joins," + ((stopExpBbConsToBbEncs - startExpBbConsToBbEncs)/1000000000.0).toString)
        
        //expand healthcare encounter - biobank consenter joins
        val startExpBbConsToHcEncs = System.nanoTime()
        expand.healthcareEncounterParticipantJoinExpansion(cxn, instantiation, graphsList)
        val stopExpBbConsToHcEncs = System.nanoTime()
        
        writeCSV.println("Expand Hc Enc to Bb Cons Joins," + ((stopExpBbConsToHcEncs - startExpBbConsToHcEncs)/1000000000.0).toString)
    }
    
    def benchmarkPostExpansion(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        //clear shortcut named graph
        val startClrScNg = System.nanoTime()
        helper.clearShortcutNamedGraphs(cxn)
        val stopClrScNg = System.nanoTime()
        
        writeCSV.println("Clear Shortcut Named Graph," + ((stopClrScNg - startClrScNg)/1000000000.0).toString)
        
        //post-expansion checks
        val startPostExpChecks = System.nanoTime()
        sparqlChecks.postExpansionChecks(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "post-expansion")
        val stopPostExpChecks = System.nanoTime()
        
        writeCSV.println("Post-Expansion Checks," + ((stopPostExpChecks - startPostExpChecks)/1000000000.0).toString)
        
        //move triples to expanded graph
        val startMvTriplesToExpNg = System.nanoTime()
        helper.moveDataFromOneNamedGraphToAnother(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "http://www.itmat.upenn.edu/biobank/expanded")
        val stopMvTriplesToExpNg = System.nanoTime()
        
        writeCSV.println("Move Triples to Expanded Graph," + ((stopMvTriplesToExpNg - startMvTriplesToExpNg)/1000000000.0).toString)
        
        //clear post-expansion named graph
        val startClrPostExpNg = System.nanoTime()
        helper.clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck")
        val stopClrPostExpNg = System.nanoTime()
        
        writeCSV.println("Clear Post-Expansion Named Graph," + ((stopClrPostExpNg - startClrPostExpNg)/1000000000.0).toString)
        
        //add string labels to ontology
        val startAddStrLbls = System.nanoTime()
        helper.addStringLabelsToOntology(cxn)
        val stopAddStrLbls = System.nanoTime()
        
        writeCSV.println("Add String Labels to Ontology," + ((stopAddStrLbls - startAddStrLbls)/1000000000.0).toString)
    }
    
    def benchmarkReferentTracking(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    { 
        //reftrack encounters
        val startEncReftrack = System.nanoTime()
        encReftrack.reftrackEncounters(cxn)
        helper.completeReftrackProcess(cxn)
        val stopEncReftrack = System.nanoTime()
        
        writeCSV.println("Reftrack Encounters (biobank and healthcare)," + ((stopEncReftrack - startEncReftrack)/1000000000.0).toString)
        
        //reftrack primary encounter dependents
        val startEncPrimDepsReftrack = System.nanoTime()
        encReftrack.reftrackPrimaryBiobankEncounterDependents(cxn)
        encReftrack.reftrackPrimaryHealthcareEncounterDependents(cxn)
        helper.completeReftrackProcess(cxn)
        val stopEncPrimDepsReftrack = System.nanoTime()
        
        writeCSV.println("Reftrack Primary Encounter Dependents (biobank and healthcare)," + ((stopEncPrimDepsReftrack - startEncPrimDepsReftrack)/1000000000.0).toString)
        
        //reftrack secondary encounter dependents
        val startEncSecondDepsReftrack = System.nanoTime()
        encReftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        encReftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        helper.completeReftrackProcess(cxn)
        val stopEncSecondDepsReftrack = System.nanoTime()
        
        writeCSV.println("Reftrack Secondary Encounter Dependents (biobank and healthcare)," + ((stopEncSecondDepsReftrack - startEncSecondDepsReftrack)/1000000000.0).toString)
        
        //reftrack tertiary encounter dependents
        val startEncTertDepsReftrack = System.nanoTime()
        encReftrack.reftrackBiobankBMI(cxn)
        encReftrack.reftrackHealthcareBMI(cxn)
        helper.completeReftrackProcess(cxn)
        encReftrack.reftrackBMIValSpecs(cxn)
        helper.completeReftrackProcess(cxn)
        val stopEncTertDepsReftrack = System.nanoTime()
        
        writeCSV.println("Reftrack Tertiary Encounter Dependents (biobank and healthcare)," + ((stopEncTertDepsReftrack - startEncTertDepsReftrack)/1000000000.0).toString)
        
        //reftrack biobank consenters
        val startBbConsReftrack = System.nanoTime()
        partReftrack.reftrackParticipants(cxn)
        helper.completeReftrackProcess(cxn)
        val stopBbConsReftrack = System.nanoTime()
        
        writeCSV.println("Reftrack Biobank Consenters," + ((stopBbConsReftrack - startBbConsReftrack)/1000000000.0).toString)
        
        //reftrack biobank consenter dependents
        val startBbConsDepsReftrack = System.nanoTime()
        partReftrack.reftrackParticipantDependents(cxn)
        helper.completeReftrackProcess(cxn)
        partReftrack.reftrackSecondaryParticipantDependents(cxn)
        helper.completeReftrackProcess(cxn)
        val stopBbConsDepsReftrack = System.nanoTime()
        
        writeCSV.println("Reftrack Biobank Consenter Dependents," + ((stopBbConsDepsReftrack - startBbConsDepsReftrack)/1000000000.0).toString)
    }
    
    def benchmarkEntityLinking(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        //link healthcare encounters to biobank consenters
        val startJoinHcEncsToBbCons = System.nanoTime()
        val consResult = join.getConsenterInfo(cxn)
        join.joinParticipantsAndHealthcareEncounters(cxn, consResult)
        val stopJoinHcEncsToBbCons = System.nanoTime()
        
        writeCSV.println("Link Hc Encs to Bb Cons," + ((stopJoinHcEncsToBbCons - startJoinHcEncsToBbCons)/1000000000.0).toString)
        
        //link biobank encounters to biobank consenters
        val startJoinBbEncsToBbCons = System.nanoTime()
        join.joinParticipantsAndBiobankEncounters(cxn, consResult)
        val stopJoinBbEncsToBbCons = System.nanoTime()
        
        writeCSV.println("Link Bb Encs to Bb Cons," + ((stopJoinBbEncsToBbCons - startJoinBbEncsToBbCons)/1000000000.0).toString)
        
        //connect BMI to adipose
        val startConnectBmiToAdi = System.nanoTime()
        join.connectBMIToAdipose(cxn)
        val stopConnectBmiToAdi = System.nanoTime()
        
        writeCSV.println("Connect BMI To Adipose," + ((stopConnectBmiToAdi - startConnectBmiToAdi)/1000000000.0).toString)
        
        //load LOF data
        val loadLOFstart = System.nanoTime()
        connect.loadDataFromPropertiesFile(cxn, inputLOFFiles, "LOFShortcuts", false)
        val loadLOFstop = System.nanoTime()
        
        writeCSV.println("Load LOF files," + ((loadLOFstop - loadLOFstart)/1000000000.0).toString)
        
        //get list of LOF graphs
        val getGraphListStart = System.nanoTime()
        val lofGraphs: ArrayBuffer[String] = helper.generateShortcutNamedGraphsList(cxn, "http://www.itmat.upenn.edu/biobank/LOFShortcuts")
        val getGraphListStop = System.nanoTime()
        
        writeCSV.println("Get list of LOF shortcut graphs," + ((getGraphListStop - getGraphListStart)/1000000000.0).toString)
        
        //connect LOF to BB Encs
        val startConnectLOF = System.nanoTime()
        join.connectLossOfFunctionToBiobankEncounters(cxn, lofGraphs)
        val stopConnectLOF = System.nanoTime()
        
        writeCSV.println("Connect LOF to BB Encs," + ((stopConnectLOF - startConnectLOF)/1000000000.0).toString)
        
        //expand loss of function
        val startLOFexpand = System.nanoTime()
        expand.expandLossOfFunctionShortcuts(cxn, instantiation, lofGraphs)
        val stopLOFexpand = System.nanoTime()
        
        writeCSV.println("Expand Loss of Function Data," + ((stopLOFexpand - startLOFexpand)/1000000000.0).toString)
        
        //add error logging for unexpanded alleles
        val startErrLog = System.nanoTime()
        expand.createErrorTriplesForUnexpandedAlleles(cxn, lofGraphs)
        val stopErrLog = System.nanoTime()
        
        writeCSV.println("Write Error Log for Unexpanded Alleles," + ((stopErrLog - startErrLog)/1000000000.0).toString)
    }
    
    def benchmarkConclusionating(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        logger.info("starting conclusionating")
        val f: ValueFactory = cxn.getValueFactory()
        conclusionationNamedGraph = f.createIRI("http://www.itmat.upenn.edu/biobank/Conclusionations" + helper.getCurrentTimestamp(""))
        val masterConclusionation: IRI = helper.genPmbbIRI(cxn)
        val masterPlanspec: IRI = helper.genPmbbIRI(cxn)
        val masterPlan: IRI = helper.genPmbbIRI(cxn)
        
        //conclusionate birth
        val startDobConc = System.nanoTime()
        dobConc.conclusionateBirthdate(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        val stopDobConc = System.nanoTime()
        
        writeCSV.println("Conclusionate Date of Birth," + ((stopDobConc - startDobConc)/1000000000.0).toString)
        
        //conclusionate biosex
        val startBiosexConc = System.nanoTime()
        biosexConc.conclusionateBiosex(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        val stopBiosexConc = System.nanoTime()
        
        writeCSV.println("Conclusionate Biosex," + ((stopBiosexConc - startBiosexConc)/1000000000.0).toString)
        
        //conclusionate BMI
        val startBmiConc = System.nanoTime()
        bmiConc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        val stopBmiConc = System.nanoTime()
        
        writeCSV.println("Conclusionate BMI," + ((stopBmiConc - startBmiConc)/1000000000.0).toString)
        logger.info("finished conclusionating")
    }
    
    def benchmarkPostConclusionating(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        //apply inverses
        val startAppInverses = System.nanoTime()
        //helper.applyInverses(cxn, conclusionationNamedGraph)
        val stopAppInverses = System.nanoTime()
        
        //writeCSV.println("Apply Inverses," + ((stopAppInverses - startAppInverses)/1000000000.0).toString)
        writeCSV.println("Apply Inverses,SKIPPED")
        
        //apply symmetrical properties
        val startAppSymmProps = System.nanoTime()
        helper.applySymmetricalProperties(cxn)
        val stopAppSymmProps = System.nanoTime()
        
        writeCSV.println("Apply Symmetrical Properties," + ((stopAppSymmProps - startAppSymmProps)/1000000000.0).toString)
        
        //apply labels
        if (applyLabels == "true")
        {
            val startApplyLabels = System.nanoTime()
            helper.addLabelsToEverything(cxn, "http://www.itmat.upenn.edu/biobank/expanded")
            helper.addLabelsToEverything(cxn, "http://www.itmat.upenn.edu/biobank/entityLinkData")
            val stopApplyLabels = System.nanoTime()
            
            writeCSV.println("Apply Labels," + ((stopApplyLabels - startApplyLabels)/1000000000.0).toString)
        }
        else writeCSV.println("Apply Labels,SKIPPED")
          
        //post-conclusionation checks
        val startPostConcChecks = System.nanoTime()
        sparqlChecks.postExpansionChecks(cxn, "http://www.itmat.upenn.edu/biobank/expanded", "post-conclusion")
        sparqlChecks.postExpansionChecks(cxn, conclusionationNamedGraph.toString, "post-conclusion")
        val stopPostConcChecks = System.nanoTime()
        
        writeCSV.println("Post-Conclusionation Checks," + ((stopPostConcChecks - startPostConcChecks)/1000000000.0).toString)
    }
    
    def benchmarkDiagnosisMapping(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        //load disease ontologies - mondo, ICD9, ICD10
        if (loadDiseaseOntologies == "true")
        {
            val startLoadDiseaseOntologies =  System.nanoTime()
            diagmap.addDiseaseOntologies(cxn)
            val stopLoadDiseaseOntologies =  System.nanoTime()
            
            writeCSV.println("Load Disease Ontologies (mondo + ICD9 + ICD10)," + ((stopLoadDiseaseOntologies - startLoadDiseaseOntologies)/1000000000.0).toString)
        }
        else writeCSV.println("Load Disease Ontologies,SKIPPED")
        
        //diagnosis mapping
        val startDiagMap = System.nanoTime()
        diagmap.performDiagnosisMapping(cxn)
        val stopDiagMap = System.nanoTime()
        
        writeCSV.println("Diagnosis Mapping," + ((stopDiagMap - startDiagMap)/1000000000.0).toString)
    }
    
    def benchmarkMedicationMapping(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter)
    {
        if (loadDrugOntologies == "true")
        {
            val startLoadMedOntologies = System.nanoTime()
            medmap.addDrugOntologies(cxn)
            val stopLoadMedOntologies = System.nanoTime()
            
            writeCSV.println("Load Drug Ontologies," + ((stopLoadMedOntologies - startLoadMedOntologies)/1000000000.0).toString)
        }
        else writeCSV.println("Load Drug Ontologies,SKIPPED")
        
        val startRunMedMap = System.nanoTime()
        val success: Boolean = medmap.runMedicationMapping(cxn)
        val stopRunMedMap = System.nanoTime()
        
        if (success) writeCSV.println("Medication Mapping," + ((stopRunMedMap - startRunMedMap)/1000000000.0).toString)
        else writeCSV.println("Medication Mapping, SKIPPED")
    }
    
    def addBenchmarkInfoToTextFile(writeTXT: PrintWriter, currTime: String)
    {
        writeTXT.println("This file contains information about the Drivetrain Automated Benchmarking run at " + currTime)
        writeTXT.println()
        writeTXT.println("serviceURL: " + serviceURL)
        writeTXT.println("inputFiles: " + inputFiles)
        writeTXT.println("turboOntology: " + ontologyURL)
        writeTXT.println("repository: " + namespace)
    }
    
    def countDistinctOfType(cxn: RepositoryConnection, writeTXT: PrintWriter, typeList: Array[String], labelList: Array[String], stage: String)
    {
        val start = System.nanoTime()
        if (typeList.size != labelList.size) logger.info("cannot log benchmarking info; error in array input sizes")
        else
        {
            writeTXT.println()
            writeTXT.println("Count instances for " + stage + " stage ")
            for (index <- 0 to typeList.size - 1)
            {
                writeTXT.println("Counting instances of " + typeList(index))
                val query: String = """
                  select (count (distinct ?type) as ?typecount) where
                  {
                      graph ?g
                      {
                          ?type a <"""+ typeList(index) +""">
                      }
                      filter (?g != pmbb:ontology)
                  }
                  """
                 //logger.info("parsing number: " + helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "typecount")(0))
                 writeTXT.println(labelList(index) + ": " + helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "typecount")(0).toString.split("\"")(1).toInt)
            }
        }
        val stop = System.nanoTime()
        subtractFromTotal += (stop - start)/1000000000.0
        logger.info("SUBTRACT FROM TOTAL VALUE: " + subtractFromTotal)
    }
    
    def countTriplesInDatabase(cxn: RepositoryConnection, stage: String)
    {
        val start = System.nanoTime()
        
        val query: String = """
          select (count (?s) as ?tripcount) where
          {
              ?s ?p ?o .
          }
          """
         //logger.info("parsing number: " + helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "tripcount")(0))
         tripCountInfo += "TriplesCount " + stage + ": " + helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, 
                 "tripcount")(0).split("\"")(1).toInt + System.lineSeparator()
        
        val stop = System.nanoTime()
        subtractFromTotal += (stop - start)/1000000000.0
        logger.info("SUBTRACT FROM TOTAL VALUE: " + subtractFromTotal)
    }
    
    def getDiagAndMedInfo(cxn: RepositoryConnection, writeTXT: PrintWriter, stage: String)
    {
        val start = System.nanoTime()
        
        val getDistinctDiagCodes: String = """
          SELECT (count(distinct ?diagCodeLiteral) as ?diagCodeCount) WHERE
          {
              ?diagcode a turbo:TURBO_0000554 .
              ?diagcode turbo:TURBO_0006510 ?diagCodeLiteral .
          }
          """
        
        val getDiagRegistryCount: String = """
          SELECT (count(?ICD9) as ?ICD9Count) (count(?ICD10) as ?ICD10Count)  WHERE
          {
              ?diagCodeRegID a turbo:TURBO_0000555 .
        		  ?diagCodeRegID obo:IAO_0000219 ?diagCodeRegURI .
              ?diagCodeRegURI a turbo:TURBO_0000556 .
              BIND ( IF (?diagCodeRegURI = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, ?diagCodeRegURI, ?unbound) AS ?ICD10)
              BIND ( IF (?diagCodeRegURI = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, ?diagCodeRegURI, ?unbound) AS ?ICD9)
          }
          """
        
        val getDistinctMedCount: String = """
          SELECT (count (distinct ?drugURI) as ?medCount) WHERE
          {
              ?drugPrescript a obo:PDRO_0000024 .
        	    ?drugPrescript obo:IAO_0000142 ?drugURI .
          }
          """
        
        writeTXT.println()
        writeTXT.println("Distinct Diagnosis Codes: " + helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDistinctDiagCodes, "diagCodeCount")(0).split("\"")(1).toInt)
        val diagRegResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDiagRegistryCount, Array("ICD9Count", "ICD10Count"))
        writeTXT.println("ICD9 Codes: " + diagRegResults(0)(0).toString.split("\"")(1).toInt)
        writeTXT.println("ICD10 Codes: " + diagRegResults(0)(1).toString.split("\"")(1).toInt)
        writeTXT.println("Distinct Medication Codes: " + helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDistinctMedCount, "medCount")(0).split("\"")(1).toInt)
        
        val stop = System.nanoTime()
        subtractFromTotal += (stop - start)/1000000000.0
        logger.info("SUBTRACT FROM TOTAL VALUE: " + subtractFromTotal)
    }
    
    def getLOFInfo(cxn: RepositoryConnection, writeTXT: PrintWriter)
    {
        val start = System.nanoTime()
        
        val getExpandedLOFInfo: String = """
          SELECT (count (distinct ?allele) as ?alleleCount) WHERE
          {
              graph pmbb:expanded
              {
              	?allele a obo:OBI_0001352 .
              }
          }
          """
        
        val getUnexpandedLOFInfo: String = """
          SELECT (count (distinct ?allele) as ?alleleCount) WHERE
          {
              graph ?g 
              {
              	?allele a obo:OBI_0001352 .
              }
              Filter (?g != pmbb:expanded)
          }
          """
        
        val expandedCount: Int = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getExpandedLOFInfo, "alleleCount")(0).split("\"")(1).toInt
        val unexpandedCount: Int = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getUnexpandedLOFInfo, "alleleCount")(0).split("\"")(1).toInt
        
        writeTXT.println()
        writeTXT.println("Total rows of LOF data loaded: " + (expandedCount + unexpandedCount))
        writeTXT.println("Total rows of LOF data expanded: " + expandedCount)
        writeTXT.println("Total rows of LOF data unexpanded: " + unexpandedCount)
        
        val stop = System.nanoTime()
        
        subtractFromTotal += (stop - start)/1000000000.0
        logger.info("SUBTRACT FROM TOTAL VALUE: " + subtractFromTotal)
    }
    
    def runBenchmarkingStage(cxn: RepositoryConnection, writeCSV: PrintWriter, writeTXT: PrintWriter, stage: String, 
        drivetrainFunction: (RepositoryConnection, PrintWriter, PrintWriter) => Unit, subtract: Boolean = false)
    {
        val startTime = System.nanoTime()
        drivetrainFunction(cxn, writeCSV, writeTXT)
        val stopTime = System.nanoTime()
        
        var totalTime = (stopTime - startTime)/1000000000.0
        if (subtract) 
        {
            logger.info("Total time for entire run: " + totalTime)
            logger.info("Time for non-drivetrain functions: " + subtractFromTotal)
            logger.info("Time for drivetrain functions: " + (totalTime - subtractFromTotal))
            totalTime = totalTime - subtractFromTotal
        }
        
        writeCSV.println(stage + "," + totalTime.toString)
        writeCSV.println()
    }
}