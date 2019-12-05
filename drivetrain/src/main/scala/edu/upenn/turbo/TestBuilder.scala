package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import java.io.PrintWriter

class TestBuilder extends ProjectwideGlobals
{
    def buildTest(cxn: RepositoryConnection, process: String)
    {
        def inputTriplesGraph = "http://www.itmat.upenn.edu/biobank/inputTriplesTestGraph"
        
        val requiredInputTriples = generateInputTriplesWithAllRequirements(process, inputTriplesGraph)
        val minimumInputTriples = generateInputTriplesWithMinimumRequirements(process, inputTriplesGraph)
        
        update.updateSparql(cxn, requiredInputTriples)
        val queryResultMax = RunDrivetrainProcess.runProcess(process)
        val outputNamedGraph = queryResultMax(process).defaultOutputGraph
        val outputPredsMax = getOutputPredicates(outputNamedGraph)
        val outputProcessMax = getOutputProcessInfo(processNamedGraph)
        
        helper.deleteAllTriplesInDatabase(cxn)
        
        update.updateSparql(cxn, minimumInputTriples)
        val queryResultMin = RunDrivetrainProcess.runProcess(process)
        val outputPredsMin = getOutputPredicates(outputNamedGraph)
        val outputProcessMin = getOutputProcessInfo(processNamedGraph)
        
        helper.deleteAllTriplesInDatabase(cxn)
        
        def testFileName = helper.getPostfixfromURI(process) + "AutomaticSnapshotTest"
        
        writeTestFile(testFileName, requiredInputTriples, outputPredsMax, outputProcessMax, minimumInputTriples, outputPredsMin, outputProcessMin)
    }
    
    def writeTestFile(testFileName: String, requiredInputTriples: String, outputPredsMax: ArrayBuffer[String], outputProcessMax: ArrayBuffer[String], minimumInputTriples: String, outputPredsMin: ArrayBuffer[String], outputProcessMin: ArrayBuffer[String])
    {
        val pw = new PrintWriter(testFileName)
        
        
        
        pw.close()
    }
    
    def generateInputTriplesWithAllRequirements(process: String, inputTriplesGraph: String): String =
    {
        ""
    }
    
    def generateInputTriplesWithMinimumRequirements(process: String, inputTriplesGraph: String): String =
    {
        ""
    }
    
    def getOutputPredicates(outputNamedGraph: String): ArrayBuffer[String] =
    {
        null
    }
    
    def getOutputProcessInfo(processNamedGraph: String): ArrayBuffer[String] =
    {
        null
    }
}