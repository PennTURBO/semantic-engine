package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class HomoSapiensExpansionUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    RunDrivetrainProcess.setInstantiation("http://www.itmat.upenn.edu/biobank/test_instantiation_1")
    
    val instantiationAndDataset: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
          
        pmbb:test_instantiation_1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
    		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
    		?dataset a obo:IAO_0000100 .
    		?dataset dc11:title "part_expand" .
       }}"""
    
    val minimumPartRequirements: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
          
          ?part a obo:NCBITaxon_9606 .
          
          ?partCrid a turbo:TURBO_0000503 .
          ?partCrid obo:IAO_0000219 ?part .
          ?partCrid obo:BFO_0000051 ?partSymbol .
          ?partCrid obo:BFO_0000051 ?partRegDen .
          ?partSymbol a turbo:TURBO_0000504 .
          ?partSymbol turbo:TURBO_0010094 "4" .
          ?partRegDen a turbo:TURBO_0000505 .
          ?partRegDen obo:IAO_0000219 turbo:TURBO_0000410 .
          
          ?partSymbol obo:BFO_0000050 ?dataset .
          ?partRegDen obo:BFO_0000050 ?dataset .
          ?dataset a obo:IAO_0000100 .
          
       }}"""
    
    val processMeta: String = """
        ASK 
        { 
          Graph pmbb:processes
          {
              ?processBoundary obo:RO_0002223 ontologies:TURBO_0010176 .
              ?processBoundary a obo:BFO_0000035 .
              ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
              ?timeMeasDatum a obo:IAO_0000416 .
              ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
              
              ontologies:TURBO_0010176 
                  turbo:TURBO_0010106 ?someQuery ;
                  turbo:TURBO_0010107 ?someRuntime ;
                  turbo:TURBO_0010108 ?someNumberOfTriples;
                  turbo:TURBO_0010186 pmbb:expanded ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts ;
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
    
    test("participant with all fields")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0010089 <http://purl.obolibrary.org/obo/OMRSE_00000138> ;
              turbo:TURBO_0010086 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0010161 ;
              turbo:TURBO_0010085 "04/May/1969" ;
              turbo:TURBO_0010098 "F" ;
              turbo:TURBO_0000609 'inpatient' ;
              
              # adding race data 7/31/18
              turbo:TURBO_0010090 <http://purl.obolibrary.org/obo/OMRSE_00000181> ;
              turbo:TURBO_0010100 'asian' .
              
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010082 turbo:TURBO_0000410 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010084 "part_expand" .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
        val extraFields: String = """
          ASK {GRAPH pmbb:expanded {
        		
        		?dataset a obo:IAO_0000100 .
        		?part rdf:type obo:NCBITaxon_9606 .

        		?gid turbo:TURBO_0010094 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?gid rdf:type obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?part turbo:TURBO_0000303 ?birth .
        		?birth rdf:type obo:UBERON_0035946 .
        		?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .
        		
        		?patientCrid obo:IAO_0000219 ?part .
        		?patientCrid a turbo:TURBO_0000503 .
        		?patientCrid obo:BFO_0000051 ?patientRegDen .
        		?patientRegDen obo:BFO_0000050 ?patientCrid .
        		?patientRegDen a turbo:TURBO_0000505 .
        		# ?patientRegDen turbo:TURBO_0010094 'inpatient' .
        		
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0010094 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .
            
          }}
          """
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(testCxn, extraFields).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val expectedPredicates = Array (
            
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (41)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010176
                
                  obo:OBI_0000293 pmbb:crid1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000410 ;
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000138 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000181 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?TURBO_0000504 a turbo:TURBO_0000504 .
                ?TURBO_0000503 a turbo:TURBO_0000503 .
                ?TURBO_0000505 a turbo:TURBO_0000505 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000138 a obo:OMRSE_00000138 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000181 a obo:OMRSE_00000181 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("participant with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1> a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010082 turbo:TURBO_0000410 .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")        
        
        //compare expected predicates to received predicates
        //only checking predicates because many of the subjects/objects in expanded triples are unique UUIDs
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (21) 
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010176
                
                  obo:OBI_0000293 pmbb:crid1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000410 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?TURBO_0000504 a turbo:TURBO_0000504 .
                ?TURBO_0000503 a turbo:TURBO_0000503 .
                ?TURBO_0000505 a turbo:TURBO_0000505 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("participant without psc")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010082 turbo:TURBO_0000410 .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (false)
        update.querySparqlBoolean(testCxn, anyProcess).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("participant without dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010082 turbo:TURBO_0000410 .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (false)
        update.querySparqlBoolean(testCxn, anyProcess).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("participant without registry")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010084 "part_expand" .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (false)
        update.querySparqlBoolean(testCxn, processMeta).get should be (false)
        update.querySparqlBoolean(testCxn, anyProcess).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("participant with text but not xsd values")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              
              a turbo:TURBO_0010161 ;
              turbo:TURBO_0010085 "04/May/1969" ;
              turbo:TURBO_0010100 'asian' ;
              turbo:TURBO_0010098 "F" .
             
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0000609 "inpatient" ;
              turbo:TURBO_0010082 turbo:TURBO_0000410 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010079 "4" .
              
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
        val dateNoXsd: String = """
          ASK {GRAPH pmbb:expanded {
        		?part rdf:type obo:NCBITaxon_9606 .
        		?part turbo:TURBO_0000303 ?birth .
        		?birth rdf:type obo:UBERON_0035946 .
        		?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob obo:IAO_0000136 ?birth .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .
        		?dataset a obo:IAO_0000100 .
        		# ?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
          }}
          """
        
        val gidNoXsd: String = """
          ASK {GRAPH pmbb:expanded {
        		?part rdf:type obo:NCBITaxon_9606 .
        		?gid obo:IAO_0000136 ?part .
        		?gid a obo:OMRSE_00000133 .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?dataset a obo:IAO_0000100 .
          }}"""
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(testCxn, dateNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, gidNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010095", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/IAO_0000136"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (40)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010176
                
                  obo:OBI_0000293 pmbb:crid1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000410 ;
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000133 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000098 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?TURBO_0000504 a turbo:TURBO_0000504 .
                ?TURBO_0000503 a turbo:TURBO_0000503 .
                ?TURBO_0000505 a turbo:TURBO_0000505 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000133 a obo:OMRSE_00000133 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000098 a obo:OMRSE_00000098 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("expand homoSapiens with multiple identifiers - single dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
            pmbb:part1
            turbo:TURBO_0010089 <http://purl.obolibrary.org/obo/OMRSE_00000138> ;
            turbo:TURBO_0010086 "1969-05-04"^^xsd:date ;
            a turbo:TURBO_0010161 ;
            turbo:TURBO_0010085 "04/May/1969" ;
            turbo:TURBO_0010098 "F" ;
            turbo:TURBO_0010090 <http://purl.obolibrary.org/obo/OMRSE_00000181> ;
            turbo:TURBO_0010100 'asian' .
            
            pmbb:shortcutCrid1 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid1 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid2 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid3 a turbo:TURBO_0010168 .
            
            pmbb:shortcutCrid1 turbo:TURBO_0010084 'dataset1' .
            pmbb:shortcutCrid2 turbo:TURBO_0010084 'dataset1' .
            pmbb:shortcutCrid3 turbo:TURBO_0010084 'dataset1' .
            
            pmbb:shortcutCrid1 turbo:TURBO_0010079 'jerry' .
            pmbb:shortcutCrid2 turbo:TURBO_0010079 'kramer' .
            pmbb:shortcutCrid3 turbo:TURBO_0010079 'elaine' .
            
            pmbb:shortcutCrid1 turbo:TURBO_0010082 <http://transformunify.org/ontologies/TURBO_0000402> .
            pmbb:shortcutCrid2 turbo:TURBO_0010082 <http://transformunify.org/ontologies/TURBO_0000403> .
            pmbb:shortcutCrid3 turbo:TURBO_0010082 <http://transformunify.org/ontologies/TURBO_0000410> .

          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
    
        val output: String = """
          ASK {GRAPH pmbb:expanded {
        	
        		?part a obo:NCBITaxon_9606 .
        		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "dataset1" .

        		?part turbo:TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .

        		?gid turbo:TURBO_0010094 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .

        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0010094 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0000503 .
        		?patientCrid1 obo:BFO_0000051 ?patientRegDen1 .
        		?patientRegDen1 obo:BFO_0000050 ?patientCrid1 .
        		?patientRegDen1 a turbo:TURBO_0000505 .
        		?patientRegDen1 obo:IAO_0000219 turbo:TURBO_0000402 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0010094 "jerry"^^xsd:string .
            ?patientRegDen1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen1 .
            ?partSymb1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0000503 .
        		?patientCrid2 obo:BFO_0000051 ?patientRegDen2 .
        		?patientRegDen2 obo:BFO_0000050 ?patientCrid2 .
        		?patientRegDen2 a turbo:TURBO_0000505 .
        		?patientRegDen2 obo:IAO_0000219 turbo:TURBO_0000403 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0010094 "kramer"^^xsd:string .
            ?patientRegDen2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen2 .
            ?partSymb2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0000503 .
        		?patientCrid3 obo:BFO_0000051 ?patientRegDen3 .
        		?patientRegDen3 obo:BFO_0000050 ?patientCrid3 .
        		?patientRegDen3 a turbo:TURBO_0000505 .
        		?patientRegDen3 obo:IAO_0000219 turbo:TURBO_0000410 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0010094 "elaine"^^xsd:string .
            ?patientRegDen3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen3 .
            ?partSymb3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        
        val oneConsenter = """
          select (count (?homosapiens) as ?homosapienscount) where
          {
              ?homosapiens a obo:NCBITaxon_9606 .
          }
          """
        
        val threeIdentifiers = """
          select (count (?crid) as ?cridcount) where
          {
              ?crid a turbo:TURBO_0000503 .
          }
          """
        
        val threeSymbols = """
          select (count (?symbol) as ?symbolcount) where
          {
              ?symbol a turbo:TURBO_0000504 .
          }
          """
        
        val threeRegistries = """
          select (count (?registry) as ?registrycount) where
          {
              ?registry a turbo:TURBO_0000505 .
          }
          """
        
        update.querySparqlAndUnpackTuple(testCxn, oneConsenter, "homosapienscount")(0).split("\"")(1) should be ("1")
        update.querySparqlAndUnpackTuple(testCxn, threeIdentifiers, "cridcount")(0).split("\"")(1) should be ("3")
        update.querySparqlAndUnpackTuple(testCxn, threeSymbols, "symbolcount")(0).split("\"")(1) should be ("3")
        update.querySparqlAndUnpackTuple(testCxn, threeRegistries, "registrycount")(0).split("\"")(1) should be ("3")
        
        update.querySparqlBoolean(testCxn, output).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val expectedPredicates = Array (
            
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050"
            
          
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")  
        
        result.size should be (69)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010176
                
                  obo:OBI_0000293 pmbb:shortcutCrid1 ;
                  obo:OBI_0000293 pmbb:shortcutCrid2 ;
                  obo:OBI_0000293 pmbb:shortcutCrid3 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000402 ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000403 ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000410 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000504_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_3 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505_3 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503_3 ;
                  
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000138 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000181 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000138 a obo:OMRSE_00000138 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000181 a obo:OMRSE_00000181 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                
                ?TURBO_0000504_1 a turbo:TURBO_0000504 .
                ?TURBO_0000503_1 a turbo:TURBO_0000503 .
                ?TURBO_0000505_1 a turbo:TURBO_0000505 .
                ?TURBO_0000504_2 a turbo:TURBO_0000504 .
                ?TURBO_0000503_2 a turbo:TURBO_0000503 .
                ?TURBO_0000505_2 a turbo:TURBO_0000505 .
                ?TURBO_0000504_3 a turbo:TURBO_0000504 .
                ?TURBO_0000503_3 a turbo:TURBO_0000503 .
                ?TURBO_0000505_3 a turbo:TURBO_0000505 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("expand homoSapiens with multiple identifiers - multiple datasets")
    {
        val insert: String = """
          INSERT DATA {
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts1 {
            pmbb:part1
            turbo:TURBO_0010089 <http://purl.obolibrary.org/obo/OMRSE_00000138> ;
            a turbo:TURBO_0010161 ;
            turbo:TURBO_0010098 "F" .
            pmbb:shortcutCrid1 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid1 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid1 turbo:TURBO_0010084 'dataset1' .
            pmbb:shortcutCrid1 turbo:TURBO_0010079 'jerry' .
            pmbb:shortcutCrid1 turbo:TURBO_0010082 <http://transformunify.org/ontologies/TURBO_0000402> .  
          }
          
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts2 {
            pmbb:part1 a turbo:TURBO_0010161 ;
            turbo:TURBO_0010086 "1969-05-04"^^xsd:date ;
            turbo:TURBO_0010085 "04/May/1969" .
            pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid2 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid2 turbo:TURBO_0010084 'dataset2' .
            pmbb:shortcutCrid2 turbo:TURBO_0010079 'kramer' .
            pmbb:shortcutCrid2 turbo:TURBO_0010082 <http://transformunify.org/ontologies/TURBO_0000403> .
          }
          
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts3 {
            pmbb:part1 a turbo:TURBO_0010161 ;
            turbo:TURBO_0010090 <http://purl.obolibrary.org/obo/OMRSE_00000181> ;
            turbo:TURBO_0010100 'asian' .
            pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid3 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid3 turbo:TURBO_0010084 'dataset3' .
            pmbb:shortcutCrid3 turbo:TURBO_0010079 'elaine' .
            pmbb:shortcutCrid3 turbo:TURBO_0010082 <http://transformunify.org/ontologies/TURBO_0000410> .
          }
          
          }"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
        
          val output: String = """
          ASK {GRAPH pmbb:expanded {
        	
        		?part a obo:NCBITaxon_9606 .
        		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset1 .
        		?dataset1 a obo:IAO_0000100 .
        		?dataset1 dc11:title "dataset1" .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset2 .
        		?dataset2 a obo:IAO_0000100 .
        		?dataset2 dc11:title "dataset2" .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset3 .
        		?dataset3 a obo:IAO_0000100 .
        		?dataset3 dc11:title "dataset3" .
        		
        		?part turbo:TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .

        		?gid turbo:TURBO_0010094 "F" .
        		?gid obo:BFO_0000050 ?dataset1 .
        		?dataset1 obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset2 .
        		?dataset2 obo:BFO_0000051 ?dob .
        		
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0010094 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset3 .
        		?dataset3 obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0000503 .
        		?patientCrid1 obo:BFO_0000051 ?patientRegDen1 .
        		?patientRegDen1 obo:BFO_0000050 ?patientCrid1 .
        		?patientRegDen1 a turbo:TURBO_0000505 .
        		?patientRegDen1 obo:IAO_0000219 turbo:TURBO_0000402 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0010094 "jerry"^^xsd:string .
            ?patientRegDen1 obo:BFO_0000050 ?dataset1 .
            ?dataset1 obo:BFO_0000051 ?patientRegDen1 .
            ?partSymb1 obo:BFO_0000050 ?dataset1 .
            ?dataset1 obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0000503 .
        		?patientCrid2 obo:BFO_0000051 ?patientRegDen2 .
        		?patientRegDen2 obo:BFO_0000050 ?patientCrid2 .
        		?patientRegDen2 a turbo:TURBO_0000505 .
        		?patientRegDen2 obo:IAO_0000219 turbo:TURBO_0000403 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0010094 "kramer"^^xsd:string .
            ?patientRegDen2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?patientRegDen2 .
            ?partSymb2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0000503 .
        		?patientCrid3 obo:BFO_0000051 ?patientRegDen3 .
        		?patientRegDen3 obo:BFO_0000050 ?patientCrid3 .
        		?patientRegDen3 a turbo:TURBO_0000505 .
        		?patientRegDen3 obo:IAO_0000219 turbo:TURBO_0000410 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0010094 "elaine"^^xsd:string .
            ?patientRegDen3 obo:BFO_0000050 ?dataset3 .
            ?dataset3 obo:BFO_0000051 ?patientRegDen3 .
            ?partSymb3 obo:BFO_0000050 ?dataset3 .
            ?dataset3 obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        
        update.querySparqlBoolean(testCxn, output).get should be (true)
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (75)
        
        val processMetaMultipleDatasets: String = """
        ASK 
        { 
          Graph pmbb:processes
          {
              ?processBoundary obo:RO_0002223 ontologies:TURBO_0010176 .
              ?processBoundary a obo:BFO_0000035 .
              ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
              ?timeMeasDatum a obo:IAO_0000416 .
              ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
              
              ontologies:TURBO_0010176 
                  turbo:TURBO_0010106 ?someQuery ;
                  turbo:TURBO_0010107 ?someRuntime ;
                  turbo:TURBO_0010108 ?someNumberOfTriples;
                  turbo:TURBO_0010186 pmbb:expanded ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts1 ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts2 ;
                  turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts3 ;
          }
        }
        """
        update.querySparqlBoolean(testCxn, processMetaMultipleDatasets).get should be (true)
        
        val processInputsOutputs: String = """
          
          ASK 
          { 
            Graph pmbb:processes
            {
                ontologies:TURBO_0010176
                
                  obo:OBI_0000293 pmbb:shortcutCrid1 ;
                  obo:OBI_0000293 pmbb:shortcutCrid2 ;
                  obo:OBI_0000293 pmbb:shortcutCrid3 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000402 ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000403 ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000410 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000504_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_3 ;
                  ontologies:TURBO_0010184 ?TURBO_0000505_3 ;
                  ontologies:TURBO_0010184 ?TURBO_0000503_3 ;
                  
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000138 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000181 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 pmbb:test_instantiation_1 ;
            }
            Graph pmbb:expanded 
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000138 a obo:OMRSE_00000138 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000181 a obo:OMRSE_00000181 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                
                ?TURBO_0000504_1 a turbo:TURBO_0000504 .
                ?TURBO_0000503_1 a turbo:TURBO_0000503 .
                ?TURBO_0000505_1 a turbo:TURBO_0000505 .
                ?TURBO_0000504_2 a turbo:TURBO_0000504 .
                ?TURBO_0000503_2 a turbo:TURBO_0000503 .
                ?TURBO_0000505_2 a turbo:TURBO_0000505 .
                ?TURBO_0000504_3 a turbo:TURBO_0000504 .
                ?TURBO_0000503_3 a turbo:TURBO_0000503 .
                ?TURBO_0000505_3 a turbo:TURBO_0000505 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
}