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

var testSearchString: Option[String] = None
var testsToRun: Array[File] = Array()

    override def run(testName: Option[String], args: org.scalatest.Args): Status =
    {
        logger.info("step 1")
        if (args.configMap.contains("findTest")) testSearchString = Option(args.configMap.getRequired[String]("findTest"))
        logger.info("step 2")
        logger.info("step 3")
        val superRes = super.run(testName, args)
        logger.info("step 4")
        superRes
    }
    
    override def beforeAll()
    {
        logger.info("step 6")
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
        cxn = graphDBMaterials.getConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(cxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
        val UUIDKey = "562783805f5b4da38876b9abfbfa06d7"
        RunDrivetrainProcess.setGlobalUUID(UUIDKey)
        RunDrivetrainProcess.setInputNamedGraphsCache(false)
        
        testsToRun = findTestsFromSearchString(testSearchString)
        logger.info("step 7")
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    def findTestsFromSearchString(searchStringOptional: Option[String]): Array[File] =
    {
        val directory = new File("src//test//scala//edu//upenn//turbo//AutoGenTests")
        val availableFiles = directory.listFiles.filter(_.isFile).toArray
        var testsToRun: Array[File] = null
        if (!searchStringOptional.isDefined) 
        {
            logger.info("No specific test search given; running all Snapshot tests")
            testsToRun = availableFiles
        }
        else 
        {
            val searchString: String = searchStringOptional.get
            logger.info("Searching for Snapshot tests containing string " + searchString)
        }
        logger.info("The following tests will be run")
        testsToRun.foreach{ test => logger.info(test.toString)}
        testsToRun
    }
    
    testsToRun.foreach{ testName =>
        test(s"first test on $testName")
        {
            println(s"running first test on $testName")
        }
        test(s"second test on $testName")
        {
            println(s"second test on $testName")   
        }
    }
}