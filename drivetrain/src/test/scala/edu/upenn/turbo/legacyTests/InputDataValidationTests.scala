package edu.upenn.turbo

import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID
import java.io._
import edu.upenn.turbo.ConnectToGraphDB
import edu.upenn.turbo.ProjectwideGlobals
import edu.upenn.turbo.RunDrivetrainProcess

class InputDataValidationTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    
    override def beforeAll()
    {
        assert("test" === System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"test\"; check your build.sbt file")
        
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "testing_instruction_set.tis", "testing_graph_specification.gs")
        cxn = graphDBMaterials.getConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(cxn)
        
        RunDrivetrainProcess.setConnections(gmCxn, cxn)
        RunDrivetrainProcess.setInputNamedGraphsCache(false)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    before
    {
        helper.deleteAllTriplesInDatabase(cxn)
    }
    
    test("participant without psc")
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
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to literal value http://transformunify.org/ontologies/homoSapiensSymbolStringLiteralValue in one of the following graphs:")) 
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
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
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to literal value http://transformunify.org/ontologies/datasetTitleStringLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("participant without dataset title as URI")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part2>
              a turbo:TURBO_0010161 .
              pmbb:crid2 obo:IAO_0000219 pmbb:part2 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 ;
              turbo:TURBO_0010084 turbo:thisShouldBeALiteral .
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to literal value http://transformunify.org/ontologies/datasetTitleStringLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
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
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010282 turbo:notARealRegistry .
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to term http://transformunify.org/ontologies/HomoSapiensRegistryOfVariousTypes in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("participant with invalid registry")
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
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid2 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to term http://transformunify.org/ontologies/HomoSapiensRegistryOfVariousTypes in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("participant without crid")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0010161 .
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/part1 of type http://transformunify.org/ontologies/TURBO_0010161 does not have the required connection to an instance of type http://transformunify.org/ontologies/TURBO_0010168 in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("crid without participant")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/crid1>
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 ;
              turbo:TURBO_0010084 "part_expand" .
              
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/crid1 of type http://transformunify.org/ontologies/TURBO_0010168 does not have the required connection to an instance of type http://transformunify.org/ontologies/TURBO_0010161 in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("tumor without person")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/tumor1>
              a turbo:TURBO_0010191 .
              pmbb:tumor1 ontologies:TURBO_0010277 turbo:TURBO_0010274 ;
                  ontologies:TURBO_0010194 'tumorId' .
                  
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to an instance of type http://transformunify.org/ontologies/TURBO_0010161 in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
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
              pmbb:part1 a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
                a turbo:TURBO_0010168 ;
                turbo:TURBO_0010084 "part_expand" ;
                turbo:TURBO_0010282 turbo:TURBO_0000505 ;
                turbo:TURBO_0010079 "4" .
                  
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to term http://transformunify.org/ontologies/TumorRegistryDenoterOfVariousTypes in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("tumor with invalid registry")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/tumor1>
              a turbo:TURBO_0010191 .
              pmbb:tumor1 obo:IAO_0000219 pmbb:part1 ;
                  ontologies:TURBO_0010194 'tumorId' ;
                  ontologies:TURBO_0010277 pmbb:notARealRegistry .
              pmbb:part1 a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
                a turbo:TURBO_0010168 ;
                turbo:TURBO_0010084 "part_expand" ;
                turbo:TURBO_0010282 turbo:TURBO_0000505 ;
                turbo:TURBO_0010079 "4" .
                  
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to term http://transformunify.org/ontologies/TumorRegistryDenoterOfVariousTypes in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("tumor without symbol")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/tumor1>
              a turbo:TURBO_0010191 .
              pmbb:tumor1 obo:IAO_0000219 pmbb:part1 ;
                  ontologies:TURBO_0010277 turbo:TURBO_0010274 .
              pmbb:part1 a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
                a turbo:TURBO_0010168 ;
                turbo:TURBO_0010084 "part_expand" ;
                turbo:TURBO_0010282 turbo:TURBO_0000505 ;
                turbo:TURBO_0010079 "4" .
                  
          }}"""
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/tumor1 of type http://transformunify.org/ontologies/TURBO_0010191 does not have the required connection to literal value http://transformunify.org/ontologies/tumorSymbolStringLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("hc encounter without ID")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <$expandedNamedGraph>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }
          }
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/hcenc1 of type http://transformunify.org/ontologies/TURBO_0010158 does not have the required connection to literal value http://transformunify.org/ontologies/healthcareEncounterSymbolLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (2)
    }
   
    /*test("hc encounter without registry")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <$expandedNamedGraph>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/hcenc1 of type http://transformunify.org/ontologies/TURBO_0010158 does not have the required connection to term http://transformunify.org/ontologies/HealthcareEncounterRegistryOfVariousTypes in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (2)
    }
    
    test("hc encounter without dataset")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <$expandedNamedGraph>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/hcenc1 of type http://transformunify.org/ontologies/TURBO_0010158 does not have the required connection to literal value http://transformunify.org/ontologies/datasetTitleStringLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph>{?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (2)
    }
    
    test("diagnosis without healthcare encounter")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:diag1
          a turbo:TURBO_0010160 ;
          ontologies:TURBO_0004602 'registry1' .
          }}
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/diag1 of type http://transformunify.org/ontologies/TURBO_0010160 does not have the required connection to an instance of type http://transformunify.org/ontologies/TURBO_0010158 in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
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
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/bbenc1 of type http://transformunify.org/ontologies/TURBO_0010169 does not have the required connection to term http://transformunify.org/ontologies/BiobankEncounterRegistryOfVariousTypes in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("bb encounter without ID")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000623 "enc_expand.csv" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0010286 turbo:TURBO_0000535 ;
          turbo:TURBO_0000629 "biobank" .
          }}
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/bbenc1 of type http://transformunify.org/ontologies/TURBO_0010169 does not have the required connection to literal value http://transformunify.org/ontologies/biobankEncounterSymbolStringLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("bb encounter without dataset")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000628 "B" ;
          a turbo:TURBO_0010169 ;
          turbo:TURBO_0010286 turbo:TURBO_0000535 ;
          turbo:TURBO_0000629 "biobank" .
          }}
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/bbenc1 of type http://transformunify.org/ontologies/TURBO_0010169 does not have the required connection to literal value http://transformunify.org/ontologies/datasetTitleStringLiteralValue in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }
    
    test("hc encounter without person")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          turbo:TURBO_0000643 "enc_expand.csv" .
          }}
          """
        update.updateSparql(cxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", "stop", false)
            assert (1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Input data error: instance http://www.itmat.upenn.edu/biobank/hcenc1 of type http://transformunify.org/ontologies/TURBO_0010158 does not have the required connection to an instance of type http://transformunify.org/ontologies/TURBO_0010161 in one of the following graphs:"))
        }
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, count, "s")
        result.size should be (0)
    }*/
}