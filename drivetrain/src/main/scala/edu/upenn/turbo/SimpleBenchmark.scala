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
        val preExpansionCount = helper.countTriplesInDatabase(cxn)
        
        val startTotalTimeCount = System.nanoTime()
        
        val instantiation = helper.genPmbbIRI()
        DrivetrainDriver.main(Array("loadTurboOntology"))
        DrivetrainDriver.main(Array("expand", "--instantiation", instantiation))
        
        val postExpansionCount = helper.countTriplesInDatabase(cxn)
        val finishedExpand = System.nanoTime()
        
        val prereftrackCounts: ArrayBuffer[String] = countDistinctOfType(cxn, Array("http://transformunify.org/ontologies/TURBO_0000502", 
            "http://transformunify.org/ontologies/TURBO_0000527", "http://purl.obolibrary.org/obo/OGMS_0000097", "http://purl.obolibrary.org/obo/PDRO_0000001",
            "http://purl.obolibrary.org/obo/OGMS_0000073"), Array("biobank consenter", "biobank encounter", "healthcare encounter", "drug prescription", 
            "diagnosis"), "pre-referent tracking")
        
        DrivetrainDriver.main(Array("reftrack"))
        
        val postReftrackCount = helper.countTriplesInDatabase(cxn)
        val finishedReferentTrack = System.nanoTime()
        
        val postreftrackCounts: ArrayBuffer[String] = countDistinctOfType(cxn, Array("http://transformunify.org/ontologies/TURBO_0000502", 
            "http://transformunify.org/ontologies/TURBO_0000527", "http://purl.obolibrary.org/obo/OGMS_0000097", "http://purl.obolibrary.org/obo/PDRO_0000001",
            "http://purl.obolibrary.org/obo/OGMS_0000073"), Array("biobank consenter", "biobank encounter", "healthcare encounter", "drug prescription", 
            "diagnosis"), "post-referent tracking")
        
        DrivetrainDriver.main(Array("entlink", "true", "--instantiation", instantiation))
        
        val postEntlinkCount = helper.countTriplesInDatabase(cxn)
        val finishedEntlink = System.nanoTime()
        
        DrivetrainDriver.main(Array("conclusionate", ".51", ".51")) 
        
        val postConclusionsCount = helper.countTriplesInDatabase(cxn)
        val endTotalTimeCount = System.nanoTime()
        
        var dsTitles: String = ""
        for (ds <- helper.getDatasetNames(cxn)) dsTitles += ds + " "
        
        val currTime: String = helper.getCurrentTimestamp()
        val filePrefix: String = "..//benchmarking//simpleBenchmark_" + currTime
        val txtFile: File = new File (filePrefix + "//benchmark_info_" + currTime + ".txt")
        //make new folder for new benchmarking info
        txtFile.getParentFile.mkdirs()
        val writeTXT: PrintWriter = new PrintWriter(txtFile)
        
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
        writeTXT.println("Entity Linking running time: " + (finishedEntlink - finishedReferentTrack)/1000000000.0)
        writeTXT.println("Conclusionation running time: " + (endTotalTimeCount - finishedEntlink)/1000000000.0)
        writeTXT.println()
        writeTXT.println("Triple Count at each stage")
        writeTXT.println("Pre-expand count: " + preExpansionCount)
        writeTXT.println("Post-expand count: " + postExpansionCount)
        writeTXT.println("Post-reftrack count: " + postReftrackCount)
        writeTXT.println("Post-conclusion count: " + postConclusionsCount)
        writeTXT.println()
        getLOFInfo(cxn, writeTXT)
        writeTXT.println()
        getDiagAndMedInfo(cxn, writeTXT)
        writeTXT.println()
        for (count <- prereftrackCounts)
        {
            writeTXT.println(count)
        }
        for (count <- postreftrackCounts)
        {
            writeTXT.println(count)
        }
        writeTXT.println()
        writeTXT.close()
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
        
        val expandedCount: Int = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getExpandedLOFInfo, "alleleCount")(0).split("\"")(1).toInt
        val unexpandedCount: Int = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getUnexpandedLOFInfo, "alleleCount")(0).split("\"")(1).toInt
        
        writeTXT.println("Total rows of LOF data loaded: " + (expandedCount + unexpandedCount))
        writeTXT.println("Total rows of LOF data expanded: " + expandedCount)
        writeTXT.println("Total rows of LOF data unexpanded: " + unexpandedCount)
    }
    
    def getDiagAndMedInfo(cxn: RepositoryConnection, writeTXT: PrintWriter)
    {
        val start = System.nanoTime()
        
        val getDistinctDiagCodes: String = """
          SELECT (count(distinct ?diagCodeLiteral) as ?diagCodeCount) WHERE
          {
              ?diagcode a obo:OGMS_0000073 .
              ?diagcode turbo:TURBO_0006512 ?diagCodeLiteral .
          }
          """
        
        val getDiagRegistryCount: String = """
          SELECT (count(?ICD9) as ?ICD9Count) (count(?ICD10) as ?ICD10Count)  WHERE
          {
              ?diag a obo:OGMS_0000073 .
              ?diag turbo:TURBO_0006515 ?diagCodeRegURI .
              BIND ( IF (?diagCodeRegURI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^xsd:anyURI, ?diagCodeRegURI, ?unbound) AS ?ICD10)
              BIND ( IF (?diagCodeRegURI = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890"^^xsd:anyURI, ?diagCodeRegURI, ?unbound) AS ?ICD9)
          }
          """
        
        val getDistinctMedCount: String = """
          SELECT (count (distinct ?drugURI) as ?medCount) WHERE
          {
              ?drugPrescript a obo:PDRO_0000001 .
        	    ?drugPrescript turbo:TURBO_0000307 ?drugURI .
          }
          """
        
        writeTXT.println("Distinct Diagnosis Codes: " + update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDistinctDiagCodes, "diagCodeCount")(0).split("\"")(1).toInt)
        val diagRegResults: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDiagRegistryCount, Array("ICD9Count", "ICD10Count"))
        writeTXT.println("ICD9 Codes: " + diagRegResults(0)(0).toString.split("\"")(1).toInt)
        writeTXT.println("ICD10 Codes: " + diagRegResults(0)(1).toString.split("\"")(1).toInt)
        writeTXT.println("Distinct Medication Codes: " + update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getDistinctMedCount, "medCount")(0).split("\"")(1).toInt)
    }
    
    def countDistinctOfType(cxn: RepositoryConnection, typeList: Array[String], labelList: Array[String], stage: String): ArrayBuffer[String] =
    {
        val counts: ArrayBuffer[String] = new ArrayBuffer[String]()
        if (typeList.size != labelList.size) logger.info("cannot log benchmarking info; error in array input sizes")
        else
        {
            counts += "Count instances for " + stage + " stage "
            for (index <- 0 to typeList.size - 1)
            {
                counts += "Counting instances of " + typeList(index)
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
                 //logger.info("parsing number: " + update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "typecount")(0))
                 counts += labelList(index) + ": " + update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "typecount")(0).toString.split("\"")(1).toInt
            }
        }
        counts
    }
}