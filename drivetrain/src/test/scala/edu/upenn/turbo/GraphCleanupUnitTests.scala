package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID


class GraphCleanupUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
    val scHcEncToScPersonCleanupExpectedQuery: String = s"""
      DELETE {
      GRAPH <$expandedNamedGraph> {
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010131> ?TURBO_0010161 .
      }
      }
      INSERT {
      GRAPH <$processNamedGraph> {
      <processURI> obo:OBI_0000293 ?TURBO_0010158 .
      <processURI> obo:OBI_0000293 ?TURBO_0010161 .
      }
      }
      WHERE {
      GRAPH <$expandedNamedGraph> {
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010131> ?TURBO_0010161 .
      ?TURBO_0010158 rdf:type <http://transformunify.org/ontologies/TURBO_0010158> .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      }
       }
    """
    
    val scBbEncToScPersonCleanupExpectedQuery: String = s"""
      DELETE {
      GRAPH <$expandedNamedGraph> {
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
      }
      }
      INSERT {
      GRAPH <$processNamedGraph> {
      <processURI> obo:OBI_0000293 ?TURBO_0010161 .
      <processURI> obo:OBI_0000293 ?TURBO_0010169 .
      }
      }
      WHERE {
      GRAPH <$expandedNamedGraph> {
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
      ?TURBO_0010169 rdf:type <http://transformunify.org/ontologies/TURBO_0010169> .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      }
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
    
    /*test ("invalid RxNORM URI removal - valid RxNORN URI")
    {
      val insert = s"""
            INSERT DATA
            {
            Graph pmbb:expanded {
                pmbb:prescription1 obo:IAO_0000142 pmbb:someRxNormDrug .
                pmbb:prescription1 a obo:PDRO_0000001 .
              }
            Graph <http://data.bioontology.org/ontologies/RXNORM/>
            {
                pmbb:someRxNormDrug a owl:Class .
            }
            }
        """
      update.updateSparql(testCxn, insert)
      RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/RxNormUrlCleanupProcess")
      
        val check: String = """
          ASK
          {
          Graph pmbb:expanded {
                pmbb:prescription1 obo:IAO_0000142 pmbb:someRxNormDrug .
                pmbb:prescription1 a obo:PDRO_0000001 .
              }
            Graph <http://data.bioontology.org/ontologies/RXNORM/>
            {
                pmbb:someRxNormDrug a owl:Class .
            }
          }
          """
        
        update.querySparqlBoolean(testCxn, check).get should be (true)
        update.querySparqlBoolean(testCxn, helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/RxNormUrlCleanupProcess")).get should be (false)
      
        val count: String = "SELECT * WHERE {?s ?p ?o .}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (3)
    }
    
    test ("invalid RxNORM URI removal - invalid RxNORN URI")
    {
      val insert = s"""
            INSERT DATA
            {
            Graph pmbb:expanded {
                pmbb:prescription1 obo:IAO_0000142 pmbb:someRxNormDrug .
                pmbb:prescription1 a obo:PDRO_0000001 .
              }
            }
        """
      update.updateSparql(testCxn, insert)
      RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/RxNormUrlCleanupProcess")
      
        val check1: String = """
          ASK
          {
          Graph pmbb:expanded {
                pmbb:prescription1 obo:IAO_0000142 pmbb:someRxNormDrug .
              }
          }
          """
        
        val check2: String = """
          ASK
          {
          Graph pmbb:expanded {
                pmbb:prescription1 a obo:PDRO_0000001 .
              }
          }
          """
        
        update.querySparqlBoolean(testCxn, check1).get should be (false)
        update.querySparqlBoolean(testCxn, check2).get should be (true)
        update.querySparqlBoolean(testCxn, helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/RxNormUrlCleanupProcess")).get should be (true)
      
        val count: String = "SELECT * WHERE {Graph pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (1)
      
        val processInputsOutputs: String = """
          
          ASK
          {
              GRAPH pmbb:processes
              {
                  pmbb:RxNormUrlCleanupProcess
                  
                    obo:OBI_0000293 pmbb:prescription1 ;
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }*/
    
    test("generated healthcare encounter cleanup query matched expected query")
    {
        var expectedQueryListBuffer = new ArrayBuffer[String]
        val processQueryMap = RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutHealthcareEncounterToShortcutPersonCleanupProcess")
        val query = processQueryMap("http://www.itmat.upenn.edu/biobank/ShortcutHealthcareEncounterToShortcutPersonCleanupProcess")
        val queryText = query.getQuery().replaceAll(" ", "").split("\\n")
        val process = query.process
        for (a <- scHcEncToScPersonCleanupExpectedQuery.replaceAll(" ","").split("\\n"))
        {
            val replacement = a.substring(0,a.length()-1).replace("localUUID", RunDrivetrainProcess.localUUID).replace("processURI", process)
            expectedQueryListBuffer += replacement
        }
        var expectedQueryList = expectedQueryListBuffer.toArray
        helper.checkStringArraysForEquivalency(queryText, expectedQueryList)("equivalent").asInstanceOf[String] should be ("true")
    }
    
    test ("remove SC hc enc to SC person link from expanded graph")
    {
      val insert = s"""
            INSERT DATA
            {
            Graph <$expandedNamedGraph> {
                pmbb:scHcEnc1 turbo:TURBO_0010131 pmbb:scPerson1 .
                pmbb:scHcEnc1 a turbo:TURBO_0010158 .
                pmbb:scPerson1 a turbo:TURBO_0010161 .
              }
            }
        """
      update.updateSparql(testCxn, insert)
      RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutHealthcareEncounterToShortcutPersonCleanupProcess")
      
        val check1: String = s"""
          ASK
          {
          Graph <$expandedNamedGraph> {
                pmbb:scHcEnc1 turbo:TURBO_0010131 pmbb:scPerson1 .
              }
          }
          """
        
        update.querySparqlBoolean(testCxn, check1).get should be (false)
        update.querySparqlBoolean(testCxn, helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/ShortcutHealthcareEncounterToShortcutPersonCleanupProcess")).get should be (true)
      
        val count: String = s"SELECT * WHERE {Graph <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (2)
      
        val processInputsOutputs: String = s"""
          
          ASK
          {
              GRAPH <$processNamedGraph>
              {
                  ?process a turbo:TURBO_0010347 ;
                  
                    obo:OBI_0000293 pmbb:scHcEnc1 ;
                    obo:OBI_0000293 pmbb:scPerson1 ;
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("generated biobank encounter cleanup query matched expected query")
    {
        var expectedQueryListBuffer = new ArrayBuffer[String]
        val processQueryMap = RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")
        val query = processQueryMap("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")
        val queryText = query.getQuery().replaceAll(" ", "").split("\\n")
        val process = query.process
        for (a <- scBbEncToScPersonCleanupExpectedQuery.replaceAll(" ","").split("\\n"))
        {
            val replacement = a.substring(0,a.length()-1).replace("localUUID", RunDrivetrainProcess.localUUID).replace("processURI", process)
            expectedQueryListBuffer += replacement
        }
        var expectedQueryList = expectedQueryListBuffer.toArray
        helper.checkStringArraysForEquivalency(queryText, expectedQueryList)("equivalent").asInstanceOf[String] should be ("true")
    }
    
    test ("remove SC bb enc to SC person link from expanded graph")
    {
      val insert = s"""
            INSERT DATA
            {
            Graph <$expandedNamedGraph> {
                pmbb:scBbEnc1 turbo:TURBO_0010133 pmbb:scPerson1 .
                pmbb:scBbEnc1 a turbo:TURBO_0010169 .
                pmbb:scPerson1 a turbo:TURBO_0010161 .
              }
            }
        """
      update.updateSparql(testCxn, insert)
      RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")
      
        val check1: String = s"""
          ASK
          {
          Graph <$expandedNamedGraph> {
                pmbb:scBbEnc1 turbo:TURBO_0010133 pmbb:scPerson1 .
              }
          }
          """
        
        update.querySparqlBoolean(testCxn, check1).get should be (false)
        update.querySparqlBoolean(testCxn, helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")).get should be (true)
      
        val count: String = s"SELECT * WHERE {Graph <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (2)
      
        val processInputsOutputs: String = s"""
          
          ASK
          {
              GRAPH <$processNamedGraph>
              {
                  ?process a turbo:TURBO_0010347 ;
                  
                    obo:OBI_0000293 pmbb:scBbEnc1 ;
                    obo:OBI_0000293 pmbb:scPerson1 ;
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    /*test("shift data models out of expanded graph back to original shortcut graphs")
    {
        val insert = s"""
            INSERT DATA
            {
            
              }
            }
        """
        
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")
      
        val check1: String = """
          ASK
          {
        
              }
          }
          """
        
        update.querySparqlBoolean(testCxn, check1).get should be (false)
        update.querySparqlBoolean(testCxn, helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")).get should be (true)
    }*/
}
