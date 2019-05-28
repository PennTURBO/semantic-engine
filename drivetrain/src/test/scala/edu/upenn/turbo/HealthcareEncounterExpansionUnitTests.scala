package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID

class HealthcareEncounterExpansionUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
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
    
    val healthcareEncounterMinimum: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?encounter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OGMS_0000097 .
        		?encounterCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000508 .
        		?encounterCrid obo:IAO_0000219 ?encounter .
        		
        		?encounterCrid obo:BFO_0000051 ?encounterSymb .
        		?encounterCrid obo:BFO_0000051 ?encounterRegDen .
        		?encounterSymb obo:BFO_0000050 ?encounterCrid .
        		?encounterRegDen obo:BFO_0000050 ?encounterCrid .
        		
        		?encounterSymb a turbo:TURBO_0000509 .
        		?encounterRegDen a turbo:TURBO_0000510 .
        	
       }}
      """
    
    val healthcareSymbolAndRegistry: String = """
      ASK {GRAPH pmbb:expanded {
          ?encounter a obo:OGMS_0000097 .
          ?encounterCrid a turbo:TURBO_0000508 .
          ?encounterCrid obo:IAO_0000219 ?encounter .
          ?encounterCrid obo:BFO_0000051 ?encsymb .
      		?encounterCrid obo:BFO_0000051 ?encregden .
      		?encsymb a turbo:TURBO_0000509 .
      		?encregden a turbo:TURBO_0000510 .
      		?encsymb turbo:TURBO_0010094 '20' .
      		?encregden obo:IAO_0000219 turbo:TURBO_0000440 .
      		?encSymb obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?encSymb .
      		?encregden obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?encregden .
      		?dataset a obo:IAO_0000100 .
      }}
      """
   
    val healthcareDiagnosis: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
          
                ?dataset a obo:IAO_0000100 .
                ?encounter a obo:OGMS_0000097 .
            		?encounter obo:OBI_0000299 ?diagnosis .
            		?diagnosis a obo:OGMS_0000073 .
            		?diagnosis turbo:TURBO_0010094 "401.9" .
            		?diagnosis turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
            		?diagnosis turbo:TURBO_0006515 "ICD-9" .
            		?diagnosis obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
            		?diagnosis obo:BFO_0000050 ?dataset .
            		?dataset obo:BFO_0000051 ?diagnosis .
            		
            		?diagnosis turbo:TURBO_0010013 "true"^^xsd:Boolean .
            		?diagnosis turbo:TURBO_0010014 "1"^^xsd:Integer .
        	}}
          """
    
    val healthcareMedications: String = """
      ask {graph pmbb:expanded {
          ?dataset a obo:IAO_0000100 .
          ?encounter a obo:OGMS_0000097 .
          ?encounter obo:OBI_0000299 ?drugPrescript .
      		?drugPrescript a obo:PDRO_0000001 .
      		?drugPrescript turbo:TURBO_0010094 "holistic soil from the ganges" .
      		?medCrid obo:IAO_0000219 ?drugPrescript .
      		?medCrid a turbo:TURBO_0000561 .
      		?medCrid obo:BFO_0000051 ?medCridSymbol .
      		?medCridSymbol a turbo:TURBO_0000562 .
      		?medCridSymbol turbo:TURBO_0010094 "3" .
      		?drugPrescript turbo:TURBO_0000307 turbo:someDrug .
      		
      		?drugPrescript obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?drugPrescript .
      		?medCridSymbol obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?medCridSymbol .
      		}}
      """
    
    val healthcareHeightWeightAndBMI: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
          
                ?encounter obo:OBI_0000299 ?BMI .
                ?encounter turbo:TURBO_0010139 ?heightDatum .
                ?encounter turbo:TURBO_0010139 ?weightDatum .
                
                ?encounter a obo:OGMS_0000097 .
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:BFO_0000050 ?dataset ;
            		  turbo:TURBO_0010094 "26.2577659792"^^xsd:float .
            		?dataset a obo:IAO_0000100 .
            		
          	    ?heightDatum rdf:type turbo:TURBO_0010138 ;
          	                 obo:IAO_0000039 obo:UO_0000015 ;
          	                 turbo:TURBO_0010094 "177.8"^^xsd:float ;
          	                 obo:BFO_0000050 ?dataset .
          	    
          	    ?weightDatum rdf:type obo:OBI_0001929 ;
          	                 obo:BFO_0000050 ?dataset ;
          	                 obo:IAO_0000039 obo:UO_0000009 ;
          	                 turbo:TURBO_0010094 "83.0082554658"^^xsd:float  .
          	                   
          	    ?dataset obo:BFO_0000051 ?BMI .
          		  ?dataset obo:BFO_0000051 ?weightDatum .
          		  ?dataset obo:BFO_0000051 ?heightDatum .
        	}}
        	"""
    
    val healthcareEncounterDate: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encDate turbo:TURBO_0010095 "15/Jan/2017" .
        		?encDate turbo:TURBO_0010096 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?dataset obo:BFO_0000051 ?encDate .
        		?encDate obo:BFO_0000050 ?dataset .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?dataset a obo:IAO_0000100 .
        		?encounter a obo:OGMS_0000097 .
           }}"""
    
    val processMeta: String = """
        ASK 
        { 
          Graph pmbb:processes
          {
              ?processBoundary obo:RO_0002223 ontologies:TURBO_0010179 .
              ?processBoundary a obo:BFO_0000035 .
              ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
              ?timeMeasDatum a obo:IAO_0000416 .
              ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
              
              ontologies:TURBO_0010179 
                  turbo:TURBO_0010106 ?someQuery ;
                  turbo:TURBO_0010107 ?someRuntime ;
                  turbo:TURBO_0010108 ?someNumberOfTriples;
                  turbo:TURBO_0010186 pmbb:expanded ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_healthcareEncounterShortcuts ;
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
    
    test("hc encounter with all fields")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          
          <http://www.itmat.upenn.edu/biobank/hcenc1>
            turbo:TURBO_0000655 "26.2577659792"^^xsd:float ;
            turbo:TURBO_0000644 "15/Jan/2017" ;
            turbo:TURBO_0000648 "20" ;
            turbo:TURBO_0000647 "83.0082554658"^^xsd:float ;
            turbo:TURBO_0000646 "177.8"^^xsd:float ;
            turbo:TURBO_0000645 "2017-01-15"^^xsd:date ;
            a turbo:TURBO_0010158 ;
            turbo:TURBO_0010110 turbo:TURBO_0000440 ;
            turbo:TURBO_0000643 "enc_expand.csv" ;
            turbo:TURBO_0010131 "http://www.itmat.upenn.edu/biobank/part1"^^xsd:anyURI ;
          
          obo:OBI_0000299 pmbb:diagnosis1 .
          pmbb:diagnosis1 a turbo:TURBO_0010160 ;
            turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
            turbo:TURBO_0004602 "ICD-9" ;
            turbo:TURBO_0004601 "401.9" ;
            turbo:TURBO_0010013 "true"^^xsd:Boolean ;
            turbo:TURBO_0010014 "1"^^xsd:Integer .
          
          pmbb:hcenc1 obo:OBI_0000299 pmbb:prescription1 .
          pmbb:prescription1 a turbo:TURBO_0010159 ;
            turbo:TURBO_0005601 "3" ;
            turbo:TURBO_0005611 "holistic soil from the ganges" ;
            turbo:TURBO_0005612 turbo:someDrug .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareMedications).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/OBI_0000299", 
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/IAO_0000142",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://transformunify.org/ontologies/TURBO_0000703",
            "http://purl.obolibrary.org/obo/RO_0002223", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000039", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/IAO_0000039", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010131", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010013",
            "http://transformunify.org/ontologies/TURBO_0000307", "http://transformunify.org/ontologies/TURBO_0010014",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000581"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (73)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010179
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagnosis1 ;
                  obo:OBI_0000293 pmbb:prescription1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000440 ;
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD9CM/401.9> ;
                  ontologies:TURBO_0010184 turbo:someDrug ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000511 ;
                  ontologies:TURBO_0010184 ?TURBO_0000512 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0000510 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000511 a turbo:TURBO_0000511 .
                ?TURBO_0000512 a turbo:TURBO_0000512 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0000510 a turbo:TURBO_0000510 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("hc encounter with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareMedications).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (21)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010179
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000440 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0000510 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0000510 a turbo:TURBO_0000510 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("hc encounter without ID")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareMedications).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (false)
        update.querySparqlBoolean(testCxn, anyProcess).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
   
    test("hc encounter without registry")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0000648 "20" .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareMedications).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (false)
        update.querySparqlBoolean(testCxn, anyProcess).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("hc encounter without dataset")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareMedications).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (false)
        update.querySparqlBoolean(testCxn, anyProcess).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("hc encounter with text but not xsd values and only diag code without reg info")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000655 "26.2577659792"^^xsd:float ;
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000644 "15/Jan/2017" ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0000647 "83.0082554658"^^xsd:float ;
          turbo:TURBO_0000646 "177.8"^^xsd:float ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> ;
          a turbo:TURBO_0010158 ;
          obo:OBI_0000299 pmbb:diagnosis1 .
          pmbb:diagnosis1 a turbo:TURBO_0010160 ;
              turbo:TURBO_0004602 "ICD-9" .
              
          pmbb:hcenc1 obo:OBI_0000299 pmbb:prescription1 .
          pmbb:prescription1 a turbo:TURBO_0010159 ;
          turbo:TURBO_0005601 "3" .
          
          }}
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        val diagnosisNoXsd: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a obo:OGMS_0000097 .
                ?dataset a obo:IAO_0000100 .
        		?encounter obo:OBI_0000299 ?diagnosis.
        		?diagnosis a obo:OGMS_0000073 .
        		?diagnosis turbo:TURBO_0006515 "ICD-9" .
        		?diagnosis obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?diagnosis .
        	}}
          """
        
        val dateNoXsd: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encDate turbo:TURBO_0010095 "15/Jan/2017" .
        		# ?encDate turbo:TURBO_0010096 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?dataset obo:BFO_0000051 ?encDate .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?dataset a obo:IAO_0000100 .
        		?encounter a obo:OGMS_0000097 .
           }}"""
        
        val healthcareMedicationsMinimum: String = """
        ask {graph pmbb:expanded {
            ?dataset a obo:IAO_0000100 .
            ?encounter a obo:OGMS_0000097 .
            ?encounter obo:OBI_0000299 ?drugPrescript .
        		?drugPrescript a obo:PDRO_0000001 .
  
        		?medCrid obo:IAO_0000219 ?drugPrescript .
        		?medCrid a turbo:TURBO_0000561 .
        		?medCrid obo:BFO_0000051 ?medCridSymbol .
        		?medCridSymbol a turbo:TURBO_0000562 .
        		?medCridSymbol turbo:TURBO_0010094 "3" .
        		
        		?drugPrescript obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?drugPrescript .
        		?medCridSymbol obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?medCridSymbol .
        		}}
        """
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (false)
        update.querySparqlBoolean(testCxn, healthcareMedicationsMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (false)
        update.querySparqlBoolean(testCxn, diagnosisNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, dateNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/OBI_0000299", 
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000581", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://purl.obolibrary.org/obo/BFO_0000050"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (63)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010179
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagnosis1 ;
                  obo:OBI_0000293 pmbb:prescription1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000440 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000511 ;
                  ontologies:TURBO_0010184 ?TURBO_0000512 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0000510 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000511 a turbo:TURBO_0000511 .
                ?TURBO_0000512 a turbo:TURBO_0000512 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0000510 a turbo:TURBO_0000510 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("ensure diagnosis info stays together with duplicate hc enc URI")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> ;
          obo:OBI_0000299 pmbb:diagnosis1 .
          pmbb:diagnosis1 a turbo:TURBO_0010160 ;
          turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
          turbo:TURBO_0004602 "ICD-9" ;
          turbo:TURBO_0004601 "401.9" .
          
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> ;
          obo:OBI_0000299 pmbb:diagnosis2 .
          pmbb:diagnosis2 a turbo:TURBO_0010160 ;
          turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> ;
          turbo:TURBO_0004602 "ICD-10" ;
          turbo:TURBO_0004601 "177.8" . 
          }}"""
        
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        val checkDiag: String = """
          Ask
          {
              Graph pmbb:expanded
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?diagnosis1 .
                  ?diagnosis1 a obo:OGMS_0000073 .
                  ?diagnosis2 a obo:OGMS_0000073 .
                  
                  ?diagnosis1 turbo:TURBO_0006515 "ICD-9" .
                  ?diagnosis1 turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
                  ?diagnosis1 turbo:TURBO_0010094 "401.9" .
                  ?diagnosis1 obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
                  
                  ?diagnosis2 turbo:TURBO_0006515 "ICD-10" .
                  ?diagnosis2 turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
                  ?diagnosis2 turbo:TURBO_0010094 "177.8" .
                  ?diagnosis2 obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD10CM/177.8> .
                  
                  Filter (?diagnosis1 != ?diagnosis2) 
              }
          }
          """
        
        val countDiag: String = """
          Select (count (distinct ?diagnosis) as ?diagnosisCount)
          {
              Graph pmbb:expanded
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?diagnosis .
                  ?diagnosis a obo:OGMS_0000073 .
              }
          }
          """
         update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
         update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (true)
         update.querySparqlBoolean(testCxn, checkDiag).get should be (true)
         update.querySparqlAndUnpackTuple(testCxn, countDiag, "diagnosisCount")(0) should startWith ("\"2")
         update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (true)
         update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010179
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagnosis1 ;
                  obo:OBI_0000293 pmbb:diagnosis2 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000440 ;
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD9CM/401.9> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD10CM/177.8> ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0000510 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073_1 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073_2 ;

                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0000510 a turbo:TURBO_0000510 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?OGMS_0000073_1 a obo:OGMS_0000073 .
                ?OGMS_0000073_2 a obo:OGMS_0000073 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("ensure medication info stays together with duplicate hc enc URI")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 turbo:TURBO_0000440 ;
          obo:OBI_0000299 pmbb:prescription1 .
          pmbb:prescription1 a turbo:TURBO_0010159 ;
          turbo:TURBO_0005601 "3" ;
          turbo:TURBO_0005611 "holistic soil from the ganges" .
          
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 turbo:TURBO_0000440 ;
          obo:OBI_0000299 pmbb:prescription2 .
          pmbb:prescription2 a turbo:TURBO_0010159 ;
          turbo:TURBO_0005601 "4" ;
          turbo:TURBO_0005611 "medicinal purple kush" . 
          }}"""
        
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        val checkDiag: String = """
          Ask
          {
              Graph pmbb:expanded
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?prescription1 .
                  ?prescription1 a obo:PDRO_0000001 .
                  ?prescription1 turbo:TURBO_0010094 "holistic soil from the ganges" .
                  ?medCrid1 obo:IAO_0000219 ?prescription1 .
                  ?medCrid1 a turbo:TURBO_0000561 .
                  ?enc obo:OBI_0000299 ?prescription2 .
                  ?prescription2 a obo:PDRO_0000001 .
                  ?prescription2 turbo:TURBO_0010094 "medicinal purple kush" .
                  ?medCrid2 obo:IAO_0000219 ?prescription2 .
                  ?medCrid2 a turbo:TURBO_0000561 .
                  
                  ?medCrid1 obo:BFO_0000051 ?medCridSymbol1 .
                  ?medCridSymbol1 a turbo:TURBO_0000562 .
                  ?medCridSymbol1 turbo:TURBO_0010094 "3" .
                  
                  ?medCrid2 obo:BFO_0000051 ?medCridSymbol2 .
                  ?medCridSymbol2 a turbo:TURBO_0000562 .
                  ?medCridSymbol2 turbo:TURBO_0010094 "4" .
                  
                  Filter (?medCrid1 != ?medCrid2) 
                  Filter (?prescription1 != ?prescription2) 
              }
          }
          """
        
        val countDiag: String = """
          Select (count (distinct ?prescription) as ?prescriptCount) (count (distinct ?medCrid) as ?medCridCount)
          {
              Graph pmbb:expanded
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?prescription .
                  ?prescription a obo:PDRO_0000001 .
                  ?medCrid obo:IAO_0000219 ?prescription .
              }
          }
          """
         update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
         update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (true)
         update.querySparqlBoolean(testCxn, checkDiag).get should be (true)
         update.querySparqlAndUnpackTuple(testCxn, countDiag, "prescriptCount")(0) should startWith ("\"2")
         update.querySparqlAndUnpackTuple(testCxn, countDiag, "medCridCount")(0) should startWith ("\"2")
         update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (true)
         update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val processInputsOutputs: String = """
        ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010179
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:prescription1 ;
                  obo:OBI_0000293 pmbb:prescription2 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000440 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0000510 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001_1 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561_2 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0000510 a turbo:TURBO_0000510 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?PDRO_0000001_1 a obo:PDRO_0000001 .
                ?PDRO_0000001_2 a obo:PDRO_0000001 .
                ?TURBO_0000562_1 a turbo:TURBO_0000562 .
                ?TURBO_0000562_2 a turbo:TURBO_0000562 .
                ?TURBO_0000561_1 a turbo:TURBO_0000561 .
                ?TURBO_0000561_2 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("expand hc encs over multiple named graphs")
    {
        val insert1: String = """
          INSERT DATA
          {
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'identifierAndRegistry.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts1
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'diagnosis.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> ;
                      obo:OBI_0000299 pmbb:diagCridSC .
                  pmbb:diagCridSC a turbo:TURBO_0010160 ;
                      turbo:TURBO_0004602 'ICD-9' ;
                      turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                      turbo:TURBO_0004601 '401.9' ;
                      turbo:TURBO_0010013 "true"^^xsd:Boolean ;
                      turbo:TURBO_0010014 "1"^^xsd:Integer .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts2
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'meds.csv' ;
                      obo:OBI_0000299 pmbb:prescription ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> .
                      pmbb:prescription a turbo:TURBO_0010159 ;
                      turbo:TURBO_0005611 "holistic soil from the ganges" ;
                      turbo:TURBO_0005612 turbo:someDrug ;
                      turbo:TURBO_0005601 "3" .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts3
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'bmiAndHeightWeight.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> ;
                      turbo:TURBO_0000646 '177.8'^^xsd:float ;
                      turbo:TURBO_0000647 '83.0082554658'^^xsd:float ;
                      turbo:TURBO_0000655 '26.2577659792'^^xsd:float .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts4
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'date.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000440> ;
                      turbo:TURBO_0000644 '15/Jan/2017' ;
                      turbo:TURBO_0000645 '2017-01-15'^^xsd:date .
              }
              Graph pmbb:Shortcuts_heressomeotherstuff
              {
                  pmbb:person a obo:NCBITaxon_9606 .
              }
          }
          """
        update.updateSparql(testCxn, insert1)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
        
        val datasetCheck1: String = """
          ASK
          {
              GRAPH pmbb:expanded
              {
                  ?encounter a obo:OGMS_0000097 .
                  ?encounterCrid a turbo:TURBO_0000508 .
                  ?encounterCrid obo:IAO_0000219 ?encounter .
                  ?encounterCrid obo:BFO_0000051 ?encsymb .
              		?encounterCrid obo:BFO_0000051 ?encregden .
              		?encsymb a turbo:TURBO_0000509 .
              		?encregden a turbo:TURBO_0000510 .
              		
              		?encSymb obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?encSymb .
              		?encregden obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?encregden .
              		
              		?dataset a obo:IAO_0000100 .
              		?dataset dc11:title 'identifierAndRegistry.csv'^^xsd:string .
              		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
              		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
              }
          }
          """
        
        val datasetCheck2: String = """
          ASK
          {
              GRAPH pmbb:expanded
              {
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title 'diagnosis.csv'^^xsd:string .
                  pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
                  
                  ?encounter a obo:OGMS_0000097 .
              		?encounter obo:OBI_0000299 ?diagnosis .
              		?diagnosis a obo:OGMS_0000073 .
              		?diagnosis turbo:TURBO_0010094 "401.9" .
            		  ?diagnosis turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
            		  ?diagnosis turbo:TURBO_0006515 "ICD-9" .
            		  ?diagnosis obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
              		
              		?dataset obo:BFO_0000051 ?diagnosis .
              		?diagnosis obo:BFO_0000050 ?dataset .
              }
          }
          """
        
        val datasetCheck3: String = """
          ASK
          {
              GRAPH pmbb:expanded
              {
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title 'meds.csv'^^xsd:string .
                  pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
                  
                  ?encounter a obo:OGMS_0000097 .
              		?encounter obo:OBI_0000299 ?drugPrescript .
              		?drugPrescript a obo:PDRO_0000001 .
              		?medCrid obo:IAO_0000219 ?drugPrescript .
              		?medCrid a turbo:TURBO_0000561 .
              		?medCrid obo:BFO_0000051 ?medCridSymbol .
              		?medCridSymbol a turbo:TURBO_0000562 .
              		
              		?drugPrescript obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?drugPrescript .
              		?medCridSymbol obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?medCridSymbol .
              }
          }
          """
        
        val datasetCheck4: String = """
          ASK
          {
              GRAPH pmbb:expanded
              {
                ?dataset a obo:IAO_0000100 .
                ?dataset dc11:title 'bmiAndHeightWeight.csv'^^xsd:string .
                pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
              
                ?encounter a obo:OGMS_0000097 .
                ?encounter obo:OBI_0000299 ?BMI .
                ?encounter turbo:TURBO_0010139 ?heightDatum .
                ?encounter turbo:TURBO_0010139 ?weightDatum .
                
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
            		    turbo:TURBO_0010094 "26.2577659792"^^xsd:float .
            		
        	      ?heightDatum rdf:type turbo:TURBO_0010138 ;
        	                 obo:IAO_0000039 obo:UO_0000015 ;
        	                 turbo:TURBO_0010094 "177.8"^^xsd:float ;
        	                 obo:BFO_0000050 ?dataset .
        	    
        	      ?weightDatum rdf:type obo:OBI_0001929 ;
        	                 obo:BFO_0000050 ?dataset ;
        	                 obo:IAO_0000039 obo:UO_0000009 ;
        	                 turbo:TURBO_0010094 "83.0082554658"^^xsd:float .
          	   
          	    ?BMI obo:BFO_0000050 ?dataset .   
          	    ?weightDatum obo:BFO_0000050 ?dataset .
          	    ?heightDatum obo:BFO_0000050 ?dataset .            
          	    ?dataset obo:BFO_0000051 ?BMI .
          	    
          		  ?dataset obo:BFO_0000051 ?weightDatum .
          		  ?dataset obo:BFO_0000051 ?heightDatum .
              }
          }
          """
        
        val datasetCheck5: String = """
          ASK
          {
              GRAPH pmbb:expanded
              {
                ?dataset a obo:IAO_0000100 .
                ?dataset dc11:title 'date.csv' .
                pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
              
                ?encounter a obo:OGMS_0000097 .
                ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
            		?encDate obo:IAO_0000136 ?encStart .
            		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
            		?encStart obo:RO_0002223 ?encounter .
            		
            		?dataset obo:BFO_0000051 ?encDate .
            		?encDate obo:BFO_0000050 ?dataset .
              }
          }
          """
        
        val thereShouldOnlyBeOneEncounter: String = """
          Select ?enc Where
          {
              Graph pmbb:expanded{
              ?enc a obo:OGMS_0000097 .}
          }
          """
        
        val thereShouldBeFiveDatasets: String = """
          Select ?dataset Where
          {
              Graph pmbb:expanded {
              ?dataset a obo:IAO_0000100 .}
          }
          """
        
        val processMetaMultipleDatasets: String = """
        ASK 
        { 
          Graph pmbb:processes
          {
              ?processBoundary obo:RO_0002223 ontologies:TURBO_0010179 .
              ?processBoundary a obo:BFO_0000035 .
              ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
              ?timeMeasDatum a obo:IAO_0000416 .
              ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
              
              ontologies:TURBO_0010179 
                  turbo:TURBO_0010106 ?someQuery ;
                  turbo:TURBO_0010107 ?someRuntime ;
                  turbo:TURBO_0010108 ?someNumberOfTriples;
                  turbo:TURBO_0010186 pmbb:expanded ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_healthcareEncounterShortcuts ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_healthcareEncounterShortcuts1 ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_healthcareEncounterShortcuts2 ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_healthcareEncounterShortcuts3 ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_healthcareEncounterShortcuts4 ;
          }
        }
        """
        
        update.querySparqlBoolean(testCxn, processMetaMultipleDatasets).get should be (true)
        update.querySparqlAndUnpackTuple(testCxn, thereShouldOnlyBeOneEncounter, "enc").size should be (1)
        update.querySparqlAndUnpackTuple(testCxn, thereShouldBeFiveDatasets, "dataset").size should be (5)
        update.querySparqlBoolean(testCxn, healthcareEncounterMinimum).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareDiagnosis).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareMedications).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterDate).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareSymbolAndRegistry).get should be (true)
        update.querySparqlBoolean(testCxn, datasetCheck1).get should be (true)
        update.querySparqlBoolean(testCxn, datasetCheck2).get should be (true)
        update.querySparqlBoolean(testCxn, datasetCheck3).get should be (true)
        update.querySparqlBoolean(testCxn, datasetCheck4).get should be (true)
        update.querySparqlBoolean(testCxn, datasetCheck5).get should be (true)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010179
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagCridSC ;
                  obo:OBI_0000293 pmbb:prescription ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000440 ;
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD9CM/401.9> ;
                  ontologies:TURBO_0010184 turbo:someDrug ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100_1 ;
                  ontologies:TURBO_0010184 ?IAO_0000100_2 ;
                  ontologies:TURBO_0010184 ?IAO_0000100_3 ;
                  ontologies:TURBO_0010184 ?IAO_0000100_4 ;
                  ontologies:TURBO_0010184 ?IAO_0000100_5 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000511 ;
                  ontologies:TURBO_0010184 ?TURBO_0000512 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0000510 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?IAO_0000100_1 a obo:IAO_0000100 .
                ?IAO_0000100_2 a obo:IAO_0000100 .
                ?IAO_0000100_3 a obo:IAO_0000100 .
                ?IAO_0000100_4 a obo:IAO_0000100 .
                ?IAO_0000100_5 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000511 a turbo:TURBO_0000511 .
                ?TURBO_0000512 a turbo:TURBO_0000512 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0000510 a turbo:TURBO_0000510 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
}