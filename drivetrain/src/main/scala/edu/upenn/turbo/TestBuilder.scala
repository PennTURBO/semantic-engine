package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.io.PrintWriter
import org.eclipse.rdf4j.model.Value

class TestBuilder extends ProjectwideGlobals
{
    def buildTest(cxn: RepositoryConnection, process: String)
    {
        val testFileName = helper.getPostfixfromURI(process) + "AutomaticSnapshotTest.scala"
        val testFilePath = new File("src//test//scala//edu//upenn//turbo//AutoGenTests//" + testFileName)
        assert(!testFilePath.exists(), s"File $testFileName already exists")

        val inputTriplesGraph = "http://www.itmat.upenn.edu/biobank/inputTriplesTestGraph"
        val inputs = RunDrivetrainProcess.getInputs(process)
        
        val requiredInputTriples = generateInputTriplesWithAllRequirements(process, inputTriplesGraph, inputs)
        val minimumInputTriples = generateInputTriplesWithMinimumRequirements(process, inputTriplesGraph, inputs)
        
        update.updateSparql(cxn, requiredInputTriples)
        val queryResultMax = RunDrivetrainProcess.runProcess(process)
        val outputNamedGraph = queryResultMax(process).defaultOutputGraph
        val outputPredsMax = getOutputPredicates(cxn, outputNamedGraph)
        
        helper.deleteAllTriplesInDatabase(cxn)
        
        update.updateSparql(cxn, minimumInputTriples)
        val queryResultMin = RunDrivetrainProcess.runProcess(process)
        val outputPredsMin = getOutputPredicates(cxn, outputNamedGraph)
        
        helper.deleteAllTriplesInDatabase(cxn)
        
        writeTestFile(testFileName, testFilePath, requiredInputTriples, outputPredsMax, minimumInputTriples, outputPredsMin)
    }
    
    def writeTestFile(testFileName: String, testFilePath: File, requiredInputTriples: String, outputPredsMax: ArrayBuffer[String], minimumInputTriples: String, outputPredsMin: ArrayBuffer[String])
    {
        val packageString = "package edu.upenn.turbo\n\n"

        val imports = """
            import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
            import org.eclipse.rdf4j.repository.Repository
            import org.eclipse.rdf4j.repository.RepositoryConnection
            import org.eclipse.rdf4j.model.IRI
            import org.scalatest.BeforeAndAfter
            import org.scalatest._
            import scala.collection.mutable.ArrayBuffer
            import java.util.UUID\n\n
        """

        val classAssertion = s"class $testFileName extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {\n"
        
        val clearTestingRepo = "val clearTestingRepositoryAfterRun: Boolean = false\n"

        val beforesAndAfters = """
        override def beforeAll()
        {
            graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true)
            testCxn = graphDBMaterials.getTestConnection()
            gmCxn = graphDBMaterials.getGmConnection()
            helper.deleteAllTriplesInDatabase(testCxn)
            
            RunDrivetrainProcess.setGraphModelConnection(gmCxn)
            RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
            RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
            RunDrivetrainProcess.setInputNamedGraphsCache(false)
        }

        override def afterAll()
        {
            ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
        }
        
        before
        {
            helper.deleteAllTriplesInDatabase(testCxn)
        }\n\n"""
    }

        val pw = new PrintWriter(testFilePath)
        var fullTestClassString = packageString + imports + classAssertion + clearTestingRepo + beforesAndAfters + "}"
        pw.close()
    }
    
    def generateInputTriplesWithAllRequirements(process: String, inputTriplesGraph: String, inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]]): String =
    {
        var generatedInputTriples = ""
        for (input <- inputs)
        {
            
        }
      
        s"""
            INSERT {
                Graph <$process> {
                   $generatedInputTriples
            }}
        """
    }
    
    def generateInputTriplesWithMinimumRequirements(process: String, inputTriplesGraph: String, inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]]): String =
    {
        var generatedInputTriples = ""
        for (input <- inputs)
        {
            
        }
      
        s"""
            INSERT {
                Graph <$process> {
                   $generatedInputTriples
            }}
        """
    }
    
    def getOutputPredicates(cxn: RepositoryConnection, outputNamedGraph: String): ArrayBuffer[String] =
    {
        update.querySparqlAndUnpackTuple(cxn, s"Select ?p Where { Graph <$outputNamedGraph> { ?s ?p ?o . }}", "p")
    }
}