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
import java.util.UUID

class SimpleBenchmark extends ProjectwideGlobals
{
    def runSimpleBenchmark(cxn: RepositoryConnection)
    {
        val currTime: String = helper.getCurrentTimestamp()
        val filePrefix: String = "..//benchmarking//simpleBenchmark_" + currTime
        val txtFile: File = new File (filePrefix + "//benchmark_info_" + currTime + ".txt")
        //make new folder for new benchmarking info
        txtFile.getParentFile.mkdirs()
        val writeTXT: PrintWriter = new PrintWriter(txtFile)
        
        val preExpansionCount = helper.countTriplesInDatabase(cxn)
        
        val startTotalTimeCount = System.nanoTime()
        
        val instantiation = helper.genPmbbIRI()
        DrivetrainDriver.main(Array("loadTurboOntology"))
        DrivetrainDriver.main(Array("expand", "--instantiation", instantiation))
        
        val postExpansionCount = helper.countTriplesInDatabase(cxn)
        val finishedExpand = System.nanoTime()
        
        DrivetrainDriver.main(Array("reftrack"))
        
        val postReftrackCount = helper.countTriplesInDatabase(cxn)
        val finishedReferentTrack = System.nanoTime()
        
        DrivetrainDriver.main(Array("conclusionate", ".51", ".51")) 
        
        val postConclusionsCount = System.nanoTime()
        val endTotalTimeCount = System.nanoTime()
        
        var dsTitles: String = ""
        for (ds <- helper.getDatasetNames(cxn)) dsTitles += ds + " "
        
        writeTXT.println("This file contains information about the Drivetrain Automated Benchmarking run at " + currTime)
        writeTXT.println()
        writeTXT.println("serviceURL: " + serviceURL)
        writeTXT.println("Input files: " + dsTitles)
        writeTXT.println("Turbo Ontology: " + ontologyURL)
        writeTXT.println("Repository : " + namespace)
        writeTXT.println()
        writeTXT.println("Total running time: " + (endTotalTimeCount - startTotalTimeCount)/1000000000.0)
        writeTXT.println("Expand running time: " + (finishedExpand - startTotalTimeCount)/1000000000.0)
        writeTXT.println("Reftrack running time: " + (finishedReferentTrack - finishedExpand)/1000000000.0)
        writeTXT.println("Conclusionation running time: " + (endTotalTimeCount - finishedReferentTrack)/1000000000.0)
        writeTXT.println()
        writeTXT.println("Triple Count at each stage")
        writeTXT.println("Pre-expand count: " + preExpansionCount)
        writeTXT.println("Post-expand count: " + postExpansionCount)
        writeTXT.println("Post-reftrack count: " + postReftrackCount)
        writeTXT.println("Post-conclusion count: " + postConclusionsCount)
        writeTXT.println()
    }
}