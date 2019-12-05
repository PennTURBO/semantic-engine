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
        def inputTriplesGraph = "http://www.itmat.upenn.edu/biobank/inputTriplesTestGraph"
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
        
        def testFileName = helper.getPostfixfromURI(process) + "AutomaticSnapshotTest"
        
        writeTestFile(testFileName, requiredInputTriples, outputPredsMax, minimumInputTriples, outputPredsMin)
    }
    
    def writeTestFile(testFileName: String, requiredInputTriples: String, outputPredsMax: ArrayBuffer[String], minimumInputTriples: String, outputPredsMin: ArrayBuffer[String])
    {
        val pw = new PrintWriter(testFileName)
        
        
        
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