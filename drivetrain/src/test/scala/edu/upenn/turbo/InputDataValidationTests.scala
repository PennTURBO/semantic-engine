package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import java.io._


class InputDataValidationTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = true
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData()
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
        
        val addProperHomoSapiens = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1> a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 .
          }}
          """
        
        val addProperHealthcareEncounter = """
        INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
            pmbb:hcenc1
            turbo:TURBO_0000643 "enc_expand.csv" ;
            a turbo:TURBO_0010158 ;
            turbo:TURBO_0000648 "20" ;
            turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> .
        }}
        """
        
        update.updateSparql(testCxn, addProperHomoSapiens)
        update.updateSparql(testCxn, addProperHealthcareEncounter)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    /*test("participant without psc")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part2>
              a turbo:TURBO_0010161 .
              pmbb:crid2 obo:IAO_0000219 pmbb:part2 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 .
          }}"""
        update.updateSparql(testCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to an instance of type http://transformunify.org/ontologies/homoSapiensSymbolStringLiteralValue in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("participant without dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part2>
              a turbo:TURBO_0010161 .
              pmbb:crid2 obo:IAO_0000219 pmbb:part2 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 .
          }}"""
        update.updateSparql(testCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to an instance of type http://transformunify.org/ontologies/datasetTitleStringLiteralValue in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("participant without registry")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part2>
              a turbo:TURBO_0010161 .
              pmbb:crid2 obo:IAO_0000219 pmbb:part2 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010084 "part_expand" .
          }}"""
        update.updateSparql(testCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to an instance of type http://transformunify.org/ontologies/HomoSapiensRegistryOfVariousTypes in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("tumor without person")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/tumor1>
              a turbo:TURBO_0010191 .
              pmbb:tumor1 ontologies:TURBO_0010277 pmbb:someTumorRegistry ;
                  ontologies:TURBO_0010194 'tumorId' .
                  
          }}"""
        update.updateSparql(testCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to an instance of type http://transformunify.org/ontologies/TURBO_0010161 in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("tumor without registry")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/tumor1>
              a turbo:TURBO_0010191 .
              pmbb:tumor1 obo:IAO_0000219 pmbb:part1 ;
                  ontologies:TURBO_0010194 'tumorId' .
                  
          }}"""
        update.updateSparql(testCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to an instance of type http://transformunify.org/ontologies/TumorRegistryDenoterOfVariousTypes in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }
    
    test("tumor without symbol")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/tumor1>
              a turbo:TURBO_0010191 .
              pmbb:tumor1 obo:IAO_0000219 pmbb:part1 ;
                  ontologies:TURBO_0010277 pmbb:someTumorRegistry .
                  
          }}"""
        update.updateSparql(testCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010176")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to an instance of type http://transformunify.org/ontologies/tumorSymbolStringLiteralValue in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }*/
    
    /*test("hc enc")
    {
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010179")
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to an instance of type http://transformunify.org/ontologies/tumorSymbolStringLiteralValue in graph http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
        }
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "s")
        result.size should be (0)
    }*/
}