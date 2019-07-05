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
    
    val rxNormCleanupProcessMeta: String = """
      ASK 
      { 
        Graph pmbb:processes
        {
            ?processBoundary obo:RO_0002223 ontologies:RxNormUrlCleanupProcess .
            ?processBoundary a obo:BFO_0000035 .
            ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
            ?timeMeasDatum a obo:IAO_0000416 .
            ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
            
            ontologies:RxNormUrlCleanupProcess 
                turbo:TURBO_0010106 ?someQuery ;
                turbo:TURBO_0010107 ?someRuntime ;
                turbo:TURBO_0010108 ?someNumberOfTriples;
                turbo:TURBO_0010186 pmbb:expanded ;
                turbo:TURBO_0010187 pmbb:expanded ;
        }
      }
      """
      
 
    test ("invalid RxNORM URI removal - valid RxNORN URI")
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
      RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/RxNormUrlCleanupProcess")
      
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
        update.querySparqlBoolean(testCxn, rxNormCleanupProcessMeta).get should be (false)
      
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
      RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/RxNormUrlCleanupProcess")
      
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
        update.querySparqlBoolean(testCxn, rxNormCleanupProcessMeta).get should be (true)
      
        val count: String = "SELECT * WHERE {Graph pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (1)
      
        val processInputsOutputs: String = """
          
          ASK
          {
              GRAPH pmbb:processes
              {
                  ontologies:RxNormUrlCleanupProcess
                  
                    obo:OBI_0000293 pmbb:prescription1 ;
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
}
