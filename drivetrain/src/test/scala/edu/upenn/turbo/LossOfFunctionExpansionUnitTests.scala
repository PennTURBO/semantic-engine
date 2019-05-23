package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID
import scala.collection.mutable.ArrayBuffer

class LossOfFunctionExpansionUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false

    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    RunDrivetrainProcess.setInstantiation("http://www.itmat.upenn.edu/biobank/test_instantiation_1")
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphLoadData(false)
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        testRepoManager = graphDBMaterials.getTestRepoManager()
        testRepository = graphDBMaterials.getTestRepository()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
  
    test("single allele expansion")
    {
        val insert: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:part1 a obo:NCBITaxon_9606 .
                  pmbb:part1 obo:RO_0000056 pmbb:bbenc1 .
                  pmbb:bbenc1 a turbo:TURBO_0000527 .
                  pmbb:shortcutBbEnc1 a turbo:TURBO_0010169 .
                  pmbb:shortcutBbEnc1 turbo:TURBO_0010113 pmbb:bbenc1 .
              }
              graph pmbb:Shortcuts_LofShortcuts
              {
                  pmbb:allele1 a turbo:TURBO_0010144 .
                  pmbb:allele1 turbo:TURBO_0007605 "ERLEC1(ENSG00000068912)"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0007602 "UPENN_UPENN10000047_someDigits"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0007607 turbo:TURBO_0000590 .
                  pmbb:allele1 turbo:TURBO_0010095 "2"^^xsd:int .
                  pmbb:allele1 turbo:TURBO_0010142 pmbb:shortcutBbEnc1 .
                  pmbb:allele1 turbo:TURBO_0007608 "lof_data_from_tests"^^xsd:String .
                  pmbb:allele1 obo:IAO_0000142 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> .
                  pmbb:allele1 turbo:TURBO_0007609 turbo:TURBO_0000451 .
                  pmbb:allele1 turbo:TURBO_0010015 "ERLEC1"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0010016 "ENSG00000068912"^^xsd:String .
                  
                  pmbb:shortcutBbEnc1 a turbo:TURBO_0010169 .
              }
          }
          
          """
        
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010180")
        
        val output: String = """
          
          ASK
          {
              Graph pmbb:expanded
              {
                  ?allele a obo:OBI_0001352 .
                  
                  ?allele obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?allele .
                  ?genomeCridSymb obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?genomeCridSymb .
                  ?dataset obo:BFO_0000051 ?genomeRegDen .
                  ?genomeRegDen obo:BFO_0000050 ?dataset .
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title "lof_data_from_tests"^^xsd:String .
                  ?instantiation a turbo:TURBO_0000522 .
                  ?instantiation obo:OBI_0000293 ?dataset .
                  
                  ?allele obo:IAO_0000142 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> .
                  ?allele turbo:TURBO_0010016 "ERLEC1"^^xsd:String .
                  ?allele turbo:TURBO_0010015 "ENSG00000068912"^^xsd:String .
                  ?allele obo:OBI_0001938 turbo:TURBO_0000590 .
                  ?allele turbo:TURBO_0010095 "2"^^xsd:int .
                  
                  ?formProcess a obo:OBI_0200000 .
                  ?formProcess obo:OBI_0000299 ?allele .
                  ?formProcess obo:OBI_0000293 ?sequenceData .
                  ?sequenceData a obo:OBI_0001573 .
                  
                  ?allele obo:IAO_0000136 ?DNA .
                  ?DNA a obo:OBI_0001868 .
                  ?DNA obo:OGG_0000000014 pmbb:part1 .
                  
                  ?DNA obo:OBI_0000643 ?DNAextract .
                  ?DNAextract a obo:OBI_0001051 .
                  ?DNAextractionProcess a obo:OBI_0000257 .
                  ?DNAextractionProcess obo:OBI_0000299 ?DNAextract .
                  ?DNAextractionProcess obo:OBI_0000293 ?specimen .
                  
                  ?exomeSequenceProcess a obo:OBI_0002118 .
                  ?exomeSequenceProcess obo:OBI_0000293 ?DNAextract .
                  ?exomeSequenceProcess obo:OBI_0000299 ?sequenceData .
                  
                  ?specimen a obo:OBI_0001479 .
                  ?collectionProcess a obo:OBI_0600005 .
                  ?collectionProcess obo:OBI_0000299 ?specimen .
                  ?collectionProcess obo:BFO_0000050 pmbb:bbenc1 .
                  pmbb:bbenc1 obo:BFO_0000051 ?collectionProcess .
                  ?collectionProcess obo:OBI_0000293 pmbb:part1 .
                  
                  ?genomeCrid a turbo:TURBO_0000566 .
                  ?genomeCrid obo:IAO_0000219 ?specimen .
                  ?genomeCrid obo:BFO_0000051 ?genomeCridSymb .
                  ?genomeCridSymb obo:BFO_0000050 ?genomeCrid .
                  ?genomeCridSymb turbo:TURBO_0010094 "UPENN_UPENN10000047_someDigits"^^xsd:String .
                  ?genomeCridSymb a turbo:TURBO_0000568 .
                  
                  ?genomeRegDen obo:BFO_0000050 ?genomeCrid .
                  ?genomeRegDen a turbo:TURBO_0000567 .
                  ?genomeRegDen obo:IAO_0000219 ?genomeRegURI .
                  ?genomeCrid obo:BFO_0000051 ?genomeRegDen .
              
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, output).get should be (true)
        
        val countTrips: String = 
        """
        Select * Where
        {
            Graph pmbb:expanded
            {
                ?s ?p ?o .
            }
        }
        """
        
        val tripsResult: ArrayBuffer[String] = update.querySparqlAndUnpackTuple(testCxn, countTrips, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/IAO_0000142",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0010016",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000643",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OGG_0000000014", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/RO_0000056",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010015"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, tripsResult.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        tripsResult.size should be (54)
    }
}