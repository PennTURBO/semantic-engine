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
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData()
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
                  pmbb:allele1 turbo:TURBO_0010285 turbo:TURBO_0000567 .
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
                  
                  turbo:TURBO_0000567 obo:BFO_0000050 ?genomeCrid .
                  ?genomeCrid obo:BFO_0000051 turbo:TURBO_0000567 .
              
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
            "http://purl.obolibrary.org/obo/BFO_0000051",  "http://transformunify.org/ontologies/TURBO_0010015",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000643",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OGG_0000000014", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/RO_0000056"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, tripsResult.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        tripsResult.size should be (checkPredicates.size)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010180 
                
                  obo:OBI_0000293 pmbb:allele1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:bbenc1 ;
                  obo:OBI_0000293 pmbb:shortcutBbEnc1 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000566 ;
                  ontologies:TURBO_0010184 ?OBI_0001868 ;
                  ontologies:TURBO_0010184 ?OBI_0001051 ;
                  ontologies:TURBO_0010184 ?OBI_0200000 ;
                  ontologies:TURBO_0010184 ?TURBO_0000568 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000567 ;
                  ontologies:TURBO_0010184 ?OBI_0002118 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001479 ;
                  ontologies:TURBO_0010184 ?OBI_0000257 ;
                  ontologies:TURBO_0010184 ?OBI_0001352 ;
                  ontologies:TURBO_0010184 ?OBI_0600005 ;
                  ontologies:TURBO_0010184 ?OBI_0001573 ;
                  
                  ontologies:TURBO_0010184 pmbb:allele1 ;
                  ontologies:TURBO_0010184 pmbb:bbenc1 ;
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
                  ontologies:TURBO_0010184 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000590 ;
            }
            Graph pmbb:expanded 
            {
                ?TURBO_0000566 a turbo:TURBO_0000566 .
                ?OBI_0001868 a obo:OBI_0001868 .
                ?OBI_0001051 a obo:OBI_0001051 .
                ?OBI_0200000 a obo:OBI_0200000 .
                ?TURBO_0000568 a turbo:TURBO_0000568 .
                ?OBI_0002118 a obo:OBI_0002118 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001479 a obo:OBI_0001479 .
                ?OBI_0000257 a obo:OBI_0000257 .
                ?OBI_0001352 a obo:OBI_0001352 .
                ?OBI_0600005 a obo:OBI_0600005 .
                ?OBI_0001573 a obo:OBI_0001573 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
        
        val processMeta: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ?processBoundary obo:RO_0002223 ontologies:TURBO_0010180 .
                ?processBoundary a obo:BFO_0000035 .
                ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
                ?timeMeasDatum a obo:IAO_0000416 .
                ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
                
                ontologies:TURBO_0010180 
                    turbo:TURBO_0010106 ?someQuery ;
                    turbo:TURBO_0010107 ?someRuntime ;
                    turbo:TURBO_0010108 ?someNumberOfTriples;
                    turbo:TURBO_0010186 pmbb:expanded ;
                    turbo:TURBO_0010187 pmbb:Shortcuts_LofShortcuts ;
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val countProcessTriples: String = 
        """
        Select * Where
        {
            Graph pmbb:processes
            {
                ?s ?p ?o .
            }
        }
        """
        val processTriplesResult: ArrayBuffer[String] = update.querySparqlAndUnpackTuple(testCxn, countProcessTriples, "p")
        processTriplesResult.size should be (33)
    }

    test("double allele expansion - multiplicity biobank encounters")
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

                  pmbb:part1 obo:RO_0000056 pmbb:bbenc2 .
                  pmbb:bbenc2 a turbo:TURBO_0000527 .
                  pmbb:shortcutBbEnc2 a turbo:TURBO_0010169 .
                  pmbb:shortcutBbEnc2 turbo:TURBO_0010113 pmbb:bbenc2 .
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
                  pmbb:allele1 turbo:TURBO_0010285 turbo:TURBO_0000567 .
                  pmbb:allele1 turbo:TURBO_0010015 "ERLEC1"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0010016 "ENSG00000068912"^^xsd:String .
                  
                  pmbb:shortcutBbEnc1 a turbo:TURBO_0010169 .

                  pmbb:allele2 a turbo:TURBO_0010144 .
                  pmbb:allele2 turbo:TURBO_0007605 "ERLEC1(ENSG00000068913)"^^xsd:String .
                  pmbb:allele2 turbo:TURBO_0007602 "UPENN_UPENN10000048_someDigits"^^xsd:String .
                  pmbb:allele2 turbo:TURBO_0007607 turbo:TURBO_0000590 .
                  pmbb:allele2 turbo:TURBO_0010095 "1"^^xsd:int .
                  pmbb:allele2 turbo:TURBO_0010142 pmbb:shortcutBbEnc2 .
                  pmbb:allele2 turbo:TURBO_0007608 "lof_data_from_tests"^^xsd:String .
                  pmbb:allele2 obo:IAO_0000142 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> .
                  pmbb:allele2 turbo:TURBO_0010285 turbo:TURBO_0000567 .
                  pmbb:allele2 turbo:TURBO_0010015 "ERLEC2"^^xsd:String .
                  pmbb:allele2 turbo:TURBO_0010016 "ENSG00000068913"^^xsd:String .
                  
                  pmbb:shortcutBbEnc2 a turbo:TURBO_0010169 .
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
                  
                  turbo:TURBO_0000567 obo:BFO_0000050 ?genomeCrid .
                  ?genomeCrid obo:BFO_0000051 turbo:TURBO_0000567 .

                  ?allele2 a obo:OBI_0001352 .
                  
                  ?allele2 obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?allele2 .
                  ?genomeCridSymb2 obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?genomeCridSymb2 .
                  
                  ?allele2 obo:IAO_0000142 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> .
                  ?allele2 turbo:TURBO_0010016 "ERLEC2"^^xsd:String .
                  ?allele2 turbo:TURBO_0010015 "ENSG00000068913"^^xsd:String .
                  ?allele2 obo:OBI_0001938 turbo:TURBO_0000590 .
                  ?allele2 turbo:TURBO_0010095 "1"^^xsd:int .
                  
                  ?formProcess2 a obo:OBI_0200000 .
                  ?formProcess2 obo:OBI_0000299 ?allele2 .
                  ?formProcess2 obo:OBI_0000293 ?sequenceData2 .
                  ?sequenceData2 a obo:OBI_0001573 .
                  
                  ?allele2 obo:IAO_0000136 ?DNA2 .
                  ?DNA2 a obo:OBI_0001868 .
                  ?DNA2 obo:OGG_0000000014 pmbb:part1 .
                  
                  ?DNA2 obo:OBI_0000643 ?DNAextract2 .
                  ?DNAextract2 a obo:OBI_0001051 .
                  ?DNAextractionProcess2 a obo:OBI_0000257 .
                  ?DNAextractionProcess2 obo:OBI_0000299 ?DNAextract2 .
                  ?DNAextractionProcess2 obo:OBI_0000293 ?specimen2 .
                  
                  ?exomeSequenceProcess2 a obo:OBI_0002118 .
                  ?exomeSequenceProcess2 obo:OBI_0000293 ?DNAextract2 .
                  ?exomeSequenceProcess2 obo:OBI_0000299 ?sequenceData2 .
                  
                  ?specimen2 a obo:OBI_0001479 .
                  ?collectionProcess2 a obo:OBI_0600005 .
                  ?collectionProcess2 obo:OBI_0000299 ?specimen2 .
                  ?collectionProcess2 obo:BFO_0000050 pmbb:bbenc2 .
                  pmbb:bbenc2 obo:BFO_0000051 ?collectionProcess2 .
                  ?collectionProcess2 obo:OBI_0000293 pmbb:part1 .
                  
                  ?genomeCrid2 a turbo:TURBO_0000566 .
                  ?genomeCrid2 obo:IAO_0000219 ?specimen2 .
                  ?genomeCrid2 obo:BFO_0000051 ?genomeCridSymb2 .
                  ?genomeCridSymb2 obo:BFO_0000050 ?genomeCrid2 .
                  ?genomeCridSymb2 turbo:TURBO_0010094 "UPENN_UPENN10000048_someDigits"^^xsd:String .
                  ?genomeCridSymb2 a turbo:TURBO_0000568 .
                  
                  turbo:TURBO_0000567 obo:BFO_0000050 ?genomeCrid2 .
                  ?genomeCrid2 obo:BFO_0000051 turbo:TURBO_0000567 .

                  filter (?allele != ?allele2)
                  filter (?genomeCridSymb != ?genomeCridSymb2)
                  filter (?genomeCrid != ?genomeCrid2)
                  filter (?collectionProcess != ?collectionProcess2)
                  filter (?specimen != ?specimen2)
                  filter (?exomeSquenceProcess != ?exomeSequenceProcess2)
                  filter (?DNA != ?DNA2)
                  filter (?DNAextract != ?DNAextract2)
                  filter (?DNAextractionProcess != ?DNAextractionProcess2)
                  filter (?formProcess != ?formProcess2)
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
            "http://purl.obolibrary.org/obo/BFO_0000051",  "http://transformunify.org/ontologies/TURBO_0010015",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000643",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OGG_0000000014", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/RO_0000056"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, tripsResult.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        tripsResult.size should be (checkPredicates.size)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010180 
                
                  obo:OBI_0000293 pmbb:allele1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:bbenc1 ;
                  obo:OBI_0000293 pmbb:shortcutBbEnc1 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000566 ;
                  ontologies:TURBO_0010184 ?OBI_0001868 ;
                  ontologies:TURBO_0010184 ?OBI_0001051 ;
                  ontologies:TURBO_0010184 ?OBI_0200000 ;
                  ontologies:TURBO_0010184 ?TURBO_0000568 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000567 ;
                  ontologies:TURBO_0010184 ?OBI_0002118 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001479 ;
                  ontologies:TURBO_0010184 ?OBI_0000257 ;
                  ontologies:TURBO_0010184 ?OBI_0001352 ;
                  ontologies:TURBO_0010184 ?OBI_0600005 ;
                  ontologies:TURBO_0010184 ?OBI_0001573 ;
                  
                  ontologies:TURBO_0010184 pmbb:allele1 ;
                  ontologies:TURBO_0010184 pmbb:bbenc1 ;
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
                  ontologies:TURBO_0010184 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000590 ;
            }
            Graph pmbb:expanded 
            {
                ?TURBO_0000566 a turbo:TURBO_0000566 .
                ?OBI_0001868 a obo:OBI_0001868 .
                ?OBI_0001051 a obo:OBI_0001051 .
                ?OBI_0200000 a obo:OBI_0200000 .
                ?TURBO_0000568 a turbo:TURBO_0000568 .
                ?OBI_0002118 a obo:OBI_0002118 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001479 a obo:OBI_0001479 .
                ?OBI_0000257 a obo:OBI_0000257 .
                ?OBI_0001352 a obo:OBI_0001352 .
                ?OBI_0600005 a obo:OBI_0600005 .
                ?OBI_0001573 a obo:OBI_0001573 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
        
        val processMeta: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ?processBoundary obo:RO_0002223 ontologies:TURBO_0010180 .
                ?processBoundary a obo:BFO_0000035 .
                ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
                ?timeMeasDatum a obo:IAO_0000416 .
                ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
                
                ontologies:TURBO_0010180 
                    turbo:TURBO_0010106 ?someQuery ;
                    turbo:TURBO_0010107 ?someRuntime ;
                    turbo:TURBO_0010108 ?someNumberOfTriples;
                    turbo:TURBO_0010186 pmbb:expanded ;
                    turbo:TURBO_0010187 pmbb:Shortcuts_LofShortcuts ;
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val countProcessTriples: String = 
        """
        Select * Where
        {
            Graph pmbb:processes
            {
                ?s ?p ?o .
            }
        }
        """
        val processTriplesResult: ArrayBuffer[String] = update.querySparqlAndUnpackTuple(testCxn, countProcessTriples, "p")
        processTriplesResult.size should be (33)
    }
}