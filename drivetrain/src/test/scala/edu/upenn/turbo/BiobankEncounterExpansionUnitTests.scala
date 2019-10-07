package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID
import scala.collection.mutable.ArrayBuffer

class BiobankEncounterExpansionUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false

    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
    val instantiationAndDataset: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?instantiation <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
        		?instantiation obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "enc_expand.csv"^^xsd:string .
       }}"""

    val biobankEncounterMinimum: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?encounter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000527 .
        		?encCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000533 .
        		?encCrid obo:IAO_0000219 ?encounter .
        		?encCrid obo:BFO_0000051 ?encsymb .
        		?encCrid obo:BFO_0000051 turbo:TURBO_0000535 .
        		turbo:TURBO_0000535 obo:BFO_0000050 ?encCrid .
        		?encsymb a turbo:TURBO_0000534 . 
        		?encsymb obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?encsymb .
        		?encsymb turbo:TURBO_0010094 'B' .
        		?dataset <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:IAO_0000100 .
       }}
      """
    
    val biobankHeightWeightAndBMI: String = """
        	
        	ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
          
                ?encounter obo:OBI_0000299 ?BMI .
                ?encounter turbo:TURBO_0010139 ?heightDatum .
                ?encounter turbo:TURBO_0010139 ?weightDatum .
                
                ?encounter a turbo:TURBO_0000527 .
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:BFO_0000050 ?dataset ;
            		  turbo:TURBO_0010094 "18.8252626423"^^xsd:float .
            		?dataset a obo:IAO_0000100 .
            		
          	    ?heightDatum rdf:type turbo:TURBO_0010138 ;
          	                 obo:IAO_0000039 obo:UO_0000015 ;
          	                 turbo:TURBO_0010094 "180.34"^^xsd:float  ;
          	                 obo:BFO_0000050 ?dataset .
          	               
          	    
          	    ?weightDatum rdf:type obo:OBI_0001929 ;
          	                 obo:BFO_0000050 ?dataset ;
          	                 obo:IAO_0000039 obo:UO_0000009 ;
          	                 turbo:TURBO_0010094 "61.2244897959"^^xsd:float .
          	                   
          	    ?dataset obo:BFO_0000051 ?BMI .
          		  ?dataset obo:BFO_0000051 ?weightDatum .
          		  ?dataset obo:BFO_0000051 ?heightDatum .
        	}}
          """
    
    val biobankEncounterDate: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?encDate obo:BFO_0000050 ?dataset .
        		?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000532 .
        		?encDate turbo:TURBO_0010095 "15/Jan/2017" .
        		?encDate turbo:TURBO_0010096 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
        		?encStart obo:RO_0002223 ?encounter .
        		?encounter a turbo:TURBO_0000527 .
        		?dataset a obo:IAO_0000100 .
          }}
      """
    
    val processMeta: String = """
          ASK 
          { 
            Graph pmbb:processes
            {
                ?processBoundary obo:RO_0002223 pmbb:BiobankEncounterExpansionProcess .
                ?processBoundary a obo:BFO_0000035 .
                ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
                ?timeMeasDatum a obo:IAO_0000416 .
                ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
                
                pmbb:BiobankEncounterExpansionProcess 
                    turbo:TURBO_0010106 ?someQuery ;
                    turbo:TURBO_0010107 ?someRuntime ;
                    turbo:TURBO_0010108 ?someNumberOfTriples;
                    turbo:TURBO_0010186 pmbb:expanded ;
                    turbo:TURBO_0010187 pmbb:Shortcuts_biobankEncounterShortcuts ;
            }
          }
          """
    
    val anyProcess: String = """
      ASK
      {
          Graph pmbb:processes
          {
              ?s ?p ?o .
          }
      }
      """
    
    val expectedQuery: String = """
      INSERT {
      GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
      ?TURBO_0010138 <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000015> .
      ?TURBO_0010138 rdf:type <http://transformunify.org/ontologies/TURBO_0010138> .
      ?OBI_0001929 <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000009> .
      ?OBI_0001929 rdf:type <http://purl.obolibrary.org/obo/OBI_0001929> .
      ?EFO_0004340 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?EFO_0004340 rdf:type <http://www.ebi.ac.uk/efo/EFO_0004340> .
      ?IAO_0000100 rdf:type <http://purl.obolibrary.org/obo/IAO_0000100> .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?EFO_0004340 .
      ?TURBO_0000522 <http://purl.obolibrary.org/obo/OBI_0000293> ?IAO_0000100 .
      ?TURBO_0000522 rdf:type <http://transformunify.org/ontologies/TURBO_0000522> .
      ?TURBO_0000533 <http://purl.obolibrary.org/obo/IAO_0000219> ?TURBO_0000527 .
      ?TURBO_0000533 rdf:type <http://transformunify.org/ontologies/TURBO_0000533> .
      ?TURBO_0000527 rdf:type <http://transformunify.org/ontologies/TURBO_0000527> .
      ?TURBO_0000527 <http://purl.obolibrary.org/obo/OBI_0000299> ?EFO_0004340 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000534 .
      ?TURBO_0000534 rdf:type <http://transformunify.org/ontologies/TURBO_0000534> .
      ?TURBO_0000533 <http://purl.obolibrary.org/obo/BFO_0000051> ?BiobankEncounterRegistryOfVariousTypes .
      ?TURBO_0000533 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000534 .
      ?BiobankEncounterRegistryOfVariousTypes <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0000533 .
      ?TURBO_0000534 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0000534 <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0000533 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000532 .
      ?TURBO_0000532 rdf:type <http://transformunify.org/ontologies/TURBO_0000532> .
      ?TURBO_0000532 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0000532 <http://purl.obolibrary.org/obo/IAO_0000136> ?TURBO_0000531 .
      ?TURBO_0000531 rdf:type <http://transformunify.org/ontologies/TURBO_0000531> .
      ?TURBO_0000531 <http://purl.obolibrary.org/obo/RO_0002223> ?TURBO_0000527 .
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010113> ?TURBO_0000527 .
      ?TURBO_0010169 rdf:type <http://transformunify.org/ontologies/TURBO_0010169> .
      ?EFO_0004340 <http://purl.obolibrary.org/obo/IAO_0000581> ?TURBO_0000532 .
      ?TURBO_0000527 <http://transformunify.org/ontologies/TURBO_0010139> ?TURBO_0010138 .
      ?TURBO_0000527 <http://transformunify.org/ontologies/TURBO_0010139> ?OBI_0001929 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0010138 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?OBI_0001929 .
      ?TURBO_0010138 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?OBI_0001929 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      ?EFO_0004340 <http://transformunify.org/ontologies/TURBO_0010094> ?bmiDoubleLiteralValue .
      ?IAO_0000100 <http://purl.org/dc/elements/1.1/title> ?datasetTitleStringLiteralValue .
      ?TURBO_0000534 <http://transformunify.org/ontologies/TURBO_0010094> ?biobankEncounterSymbolStringLiteralValue .
      ?TURBO_0000532 <http://transformunify.org/ontologies/TURBO_0010096> ?biobankEncounterDateLiteralValue .
      ?TURBO_0000532 <http://transformunify.org/ontologies/TURBO_0010095> ?biobankEncounterDateStringLiteralValue .
      ?TURBO_0010138 <http://transformunify.org/ontologies/TURBO_0010094> ?lengthMeasurementDoubleLiteralValue .
      ?OBI_0001929 <http://transformunify.org/ontologies/TURBO_0010094> ?massMeasurementDoubleLiteralValue .
      }
      GRAPH <http://www.itmat.upenn.edu/biobank/processes> {
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0010138 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?OBI_0001929 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?EFO_0004340 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?IAO_0000100 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0000522 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0000533 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0000527 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0000534 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?BiobankEncounterRegistryOfVariousTypes .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0000532 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0000531 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0010169 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> turbo:TURBO_0010184 ?TURBO_0010161 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> obo:OBI_0000293 ?TURBO_0010161 .
      <http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess> obo:OBI_0000293 ?TURBO_0010169 .
      }
      }
      WHERE {
      GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
      VALUES ?BiobankEncounterRegistryOfVariousTypes {<http://transformunify.org/ontologies/TURBO_0000535>}
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010286> ?BiobankEncounterRegistryOfVariousTypes .
      ?TURBO_0010169 rdf:type <http://transformunify.org/ontologies/TURBO_0010169> .
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000623> ?datasetTitleStringLiteralValue .
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000628> ?biobankEncounterSymbolStringLiteralValue .
      OPTIONAL {
       ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
       ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      }
      OPTIONAL {
       ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000635> ?bmiDoubleLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000625> ?biobankEncounterDateLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000624> ?biobankEncounterDateStringLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000626> ?lengthMeasurementDoubleLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0000627> ?massMeasurementDoubleLiteralValue .
       }
      }
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000527","localUUID", str(?TURBO_0010169))))) AS ?TURBO_0000527)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000534","localUUID", str(?TURBO_0010169))))) AS ?TURBO_0000534)
      BIND(IF (BOUND(?lengthMeasurementDoubleLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0010138","localUUID", str(?TURBO_0010169))))), ?unbound) AS ?TURBO_0010138)
      BIND(IF (BOUND(?massMeasurementDoubleLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?OBI_0001929","localUUID", str(?TURBO_0010169))))), ?unbound) AS ?OBI_0001929)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000522","localUUID")))) AS ?TURBO_0000522)
      BIND(IF (BOUND(?biobankEncounterDateStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000532","localUUID", str(?TURBO_0010169))))), ?unbound) AS ?TURBO_0000532)
      BIND(IF (BOUND(?biobankEncounterDateStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000531","localUUID", str(?TURBO_0010169))))), ?unbound) AS ?TURBO_0000531)
      BIND(IF (BOUND(?bmiDoubleLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?EFO_0004340","localUUID", str(?TURBO_0010169))))), ?unbound) AS ?EFO_0004340)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000533","localUUID", str(?TURBO_0010169))))) AS ?TURBO_0000533)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT(str(?datasetTitleStringLiteralValue),"localUUID")))) AS ?IAO_0000100)
      }
      """
    
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
    
    test("generated query matched expected query")
    {
        var expectedQueryListBuffer = new ArrayBuffer[String]
        for (a <- expectedQuery.replaceAll(" ","").split("\\n"))
        {
            val replacement = a.substring(0,a.length()-1).replace("localUUID", RunDrivetrainProcess.localUUID)
            expectedQueryListBuffer += replacement
        }
        var expectedQueryList = expectedQueryListBuffer.toArray
        
        val processQueryMap = RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess")
        var thisQuery = processQueryMap("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess").getQuery().replaceAll(" ", "").split("\\n")
        helper.checkStringArraysForEquivalency(thisQuery, expectedQueryList)("equivalent").asInstanceOf[String] should be ("true")
    }
  
    test("bb encounter with all fields")
    {
        logger.info("starting biobank encounter expanison unit tests")
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000635 "18.8252626423"^^xsd:float ;
          turbo:TURBO_0000624 "15/Jan/2017" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000627 "61.2244897959"^^xsd:float ;
          turbo:TURBO_0000626 "180.34"^^xsd:float ;
          turbo:TURBO_0000625 "2017-01-15"^^xsd:date ;
          turbo:TURBO_0000629 "biobank" ;
          turbo:TURBO_0010286 turbo:TURBO_0000535 ;
          turbo:TURBO_0010133 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }}
          """
        update.updateSparql(testCxn, insert)
        
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess")
          
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://transformunify.org/ontologies/TURBO_0010133",
            "http://purl.obolibrary.org/obo/IAO_0000581", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010139",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010139",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050"
            
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (checkPredicates.size)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                pmbb:BiobankEncounterExpansionProcess
                
                  obo:OBI_0000293 pmbb:bbenc1 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000531 ;
                  ontologies:TURBO_0010184 ?TURBO_0000532 ;
                  ontologies:TURBO_0010184 ?TURBO_0000533 ;
                  ontologies:TURBO_0010184 ?TURBO_0000534 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000535 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?TURBO_0000527 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  
                  ontologies:TURBO_0010184 pmbb:bbenc1 ;
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000531 a turbo:TURBO_0000531 .
                ?TURBO_0000532 a turbo:TURBO_0000532 .
                ?TURBO_0000533 a turbo:TURBO_0000533 .
                ?TURBO_0000534 a turbo:TURBO_0000534 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?TURBO_0000527 a turbo:TURBO_0000527 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("bb encounter with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0010286 turbo:TURBO_0000535 .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (checkPredicates.size)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                pmbb:BiobankEncounterExpansionProcess
                
                  obo:OBI_0000293 pmbb:bbenc1 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000533 ;
                  ontologies:TURBO_0010184 ?TURBO_0000534 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000535 ;
                  ontologies:TURBO_0010184 ?TURBO_0000527 ;
                  
                  ontologies:TURBO_0010184 pmbb:bbenc1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000533 a turbo:TURBO_0000533 .
                ?TURBO_0000534 a turbo:TURBO_0000534 .
                ?TURBO_0000527 a turbo:TURBO_0000527 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("bb encounter with text but not xsd values")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000635 "18.8252626423"^^xsd:float ;
          turbo:TURBO_0000624 "15/Jan/2017" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000627 "61.2244897959"^^xsd:float ;
          turbo:TURBO_0000626 "180.34"^^xsd:float ;
          turbo:TURBO_0000629 "biobank" ;
          turbo:TURBO_0010286 turbo:TURBO_0000535 .
          # turbo:TURBO_0000625 "2017-01-15"^^xsd:date .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess")
        
        val dateNoXsd: String = """
          ask {
          GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
        		?encounter a turbo:TURBO_0000527 .
        		?dataset a obo:IAO_0000100 .
        		?encDate a turbo:TURBO_0000532 .
        		?encDate turbo:TURBO_0010095 "15/Jan/2017" .
        		# ?encDate turbo:TURBO_0010096 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
        		?encStart obo:RO_0002223 ?Encounter1 .
        	}}
          """
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (false) 
        update.querySparqlBoolean(testCxn, dateNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/IAO_0000581",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010139",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0010094"   
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (checkPredicates.size)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                pmbb:BiobankEncounterExpansionProcess
                
                  obo:OBI_0000293 pmbb:bbenc1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000535 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000533 ;
                  ontologies:TURBO_0010184 ?TURBO_0000534 ;
                  ontologies:TURBO_0010184 ?TURBO_0000532 ;
                  ontologies:TURBO_0010184 ?TURBO_0000531 ;
                  ontologies:TURBO_0010184 ?TURBO_0000527 ;
                  
                  ontologies:TURBO_0010184 pmbb:bbenc1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000533 a turbo:TURBO_0000533 .
                ?TURBO_0000534 a turbo:TURBO_0000534 .
                ?TURBO_0000531 a turbo:TURBO_0000531 .
                ?TURBO_0000532 a turbo:TURBO_0000532 .
                ?TURBO_0000527 a turbo:TURBO_0000527 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
}