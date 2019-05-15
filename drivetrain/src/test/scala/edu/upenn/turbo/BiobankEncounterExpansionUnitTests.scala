package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID

class BiobankEncounterExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val clearTestingRepositoryAfterRun: Boolean = false

    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    RunDrivetrainProcess.setInstantiation("http://www.itmat.upenn.edu/biobank/test_instantiation_1")
    
    val instantiationAndDataset: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            pmbb:test_instantiation_1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "enc_expand.csv"^^xsd:string .
       }}"""

    val biobankEncounterMinimum: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?encounter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000527 .
        		?encCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000533 .
        		?encCrid obo:IAO_0000219 ?encounter .
        		?encCrid obo:BFO_0000051 ?encsymb .
        		?encCrid obo:BFO_0000051 ?encregden .
        		?encsymb a turbo:TURBO_0000534 . 
        		?encsymb obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?encsymb .
        		?encsymb turbo:TURBO_0010094 'B' .
        		?encregden a turbo:TURBO_0000535 .
        		# ?encregden turbo:TURBO_0010094 'biobank' .
        		?encregden obo:IAO_0000219 <http://transformunify.org/hcEncReg/biobank> .
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
          turbo:TURBO_0000630 <http://transformunify.org/hcEncReg/biobank> ;
          turbo:TURBO_0010012 "http://www.itmat.upenn.edu/biobank/part1"^^xsd:anyURI .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (true)
        
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
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
             "http://purl.obolibrary.org/obo/OBI_0000299", "http://transformunify.org/ontologies/TURBO_0010012",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/IAO_0000581",
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
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010113"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (49)
    }
    
    test("bb encounter with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000630 <http://transformunify.org/hcEncReg/biobank> .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
             "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
             "http://transformunify.org/ontologies/TURBO_0010113"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (21)
    }
   
    test("bb encounter without registry")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000623 "enc_expand.csv" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000628 "B" .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (false)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("bb encounter without ID")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000623 "enc_expand.csv" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000630 <http://transformunify.org/hcEncReg/biobank> ;
          turbo:TURBO_0000629 "biobank" .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (false)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("bb encounter without dataset")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000628 "B" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0000630 <http://transformunify.org/hcEncReg/biobank> ;
          turbo:TURBO_0000629 "biobank" .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
            
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterMinimum).get should be (false)
        update.querySparqlBoolean(testCxn, biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
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
          turbo:TURBO_0000630 <http://transformunify.org/hcEncReg/biobank> .
          # turbo:TURBO_0000625 "2017-01-15"^^xsd:date .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/biobankEncounterExpansionProcess")
        
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
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/IAO_0000581",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/IAO_0000219", 
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
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094"   
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (46)
    }
}