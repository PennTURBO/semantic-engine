package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import org.eclipse.rdf4j.model.Literal
import java.io.File

class SnapshotTest extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

var testsToRun: Array[File] = null
var allTests: Array[File] = null
var testSearchString: Option[String] = None
  
    override def run(testName: Option[String], args: org.scalatest.Args): Status =
    {
        if (args.configMap.contains("findTest")) testSearchString = Option(args.configMap.getRequired[String]("findTest"))
        findTestsFromSearchString(testSearchString)
        val superRes = super.run(testName, args)
        superRes
    }

    before
    {
        helper.deleteAllTriplesInDatabase(cxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    def findTestsFromSearchString(searchStringOptional: Option[String])
    {
        if (!searchStringOptional.isDefined) 
        {
            logger.info("No specific test search given; running all Snapshot tests")
            testsToRun = allTests
        }
        else
        {
            val searchString: String = searchStringOptional.get
            logger.info("Searching for Snapshot tests containing string " + searchString)
            val regexSearch: scala.util.matching.Regex = searchString.toLowerCase().r
            var testsToRunBuffer = new ArrayBuffer[File]
            for (singleTest <- allTests) if (regexSearch.findFirstIn(singleTest.toString.toLowerCase()) != None) testsToRunBuffer += singleTest
            testsToRun = testsToRunBuffer.toArray
        }
        logger.info("The following tests will be run (list size:" + testsToRun.size+")")
        testsToRun.foreach{ test => logger.info(test.toString)}
        println()
    }
    
    def getAllTests()
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(false)
        cxn = graphDBMaterials.getConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
        RunDrivetrainProcess.setInputNamedGraphsCache(false)
        
        OntologyLoader.addOntologyFromUrl(gmCxn)
        
        val directory = new File("src//test//scala//edu//upenn//turbo//AutoGenTests")
        allTests = directory.listFiles.filter(_.isFile).toArray
    }
    
    getAllTests()
    var prevInstructionSet = ""
    allTests.foreach{ testName =>
      
        val fileMap = io.Source
          .fromFile(testName)
          .mkString
          .split("(?=\\n\\S+\\s*->)")
          .map(_.trim.split("\\s*->\\s*"))
          .map(arr => (arr(0)->arr(1)))
          .toMap     

        //for ((k,v) <- fileMap) println("key: " + k + " value: " + v)
        
        test(s"all fields test on $testName")
        {
            assume(testsToRun.contains(testName))
            RunDrivetrainProcess.setGlobalUUID(fileMap("UUIDKey"))
            
            if (fileMap("instructionSetFile") != prevInstructionSet) 
            {
                DrivetrainDriver.updateModel(gmCxn, fileMap("instructionSetFile")+".ttl")
                OntologyLoader.addOntologyFromUrl(gmCxn)
                prevInstructionSet = fileMap("instructionSetFile")
            }
            
            val inputTriples = fileMap("allInputTriples")
            update.updateSparql(cxn, s"INSERT DATA { $inputTriples \n}")
            RunDrivetrainProcess.runProcess(fileMap("updateSpecificationURI"))
            val outputTriples = fileMap("allOutputTriples").split("\n")
            
            val getAllTriples: String = "SELECT * WHERE {GRAPH <"+fileMap("outputNamedGraph")+"> {?s ?p ?o .}}"
            val result = update.querySparqlAndUnpackTuple(cxn, getAllTriples, Array("s", "p", "o"))
            
            var resultsArray = new ArrayBuffer[String]
            for (index <- 0 to result.size-1) 
            {
                if (!result(index)(2).isInstanceOf[Literal]) resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
                else resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
            }
            helper.checkStringArraysForEquivalency(outputTriples, resultsArray.toArray)("equivalent").asInstanceOf[String] should be ("true")

        }
        test(s"minimum fields test on $testName")
        {
            assume(testsToRun.contains(testName))
            RunDrivetrainProcess.setGlobalUUID(fileMap("UUIDKey"))
            
            if (fileMap("instructionSetFile") != prevInstructionSet) 
            {
                DrivetrainDriver.updateModel(gmCxn, fileMap("instructionSetFile")+".ttl")
                OntologyLoader.addOntologyFromUrl(gmCxn)
                prevInstructionSet = fileMap("instructionSetFile")
            }
            
            val inputTriples = fileMap("minimumInputTriples")
            update.updateSparql(cxn, s"INSERT DATA { $inputTriples \n}")
            RunDrivetrainProcess.runProcess(fileMap("updateSpecificationURI"))
            val outputTriples = fileMap("minimumOutputTriples").split("\n")
            
            val getAllTriples: String = "SELECT * WHERE {GRAPH <"+fileMap("outputNamedGraph")+"> {?s ?p ?o .}}"
            val result = update.querySparqlAndUnpackTuple(cxn, getAllTriples, Array("s", "p", "o"))
            
            var resultsArray = new ArrayBuffer[String]
            for (index <- 0 to result.size-1) 
            {
                if (!result(index)(2).isInstanceOf[Literal]) resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
                else resultsArray += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
            }
            helper.checkStringArraysForEquivalency(outputTriples, resultsArray.toArray)("equivalent").asInstanceOf[String] should be ("true")
        }
    }
}