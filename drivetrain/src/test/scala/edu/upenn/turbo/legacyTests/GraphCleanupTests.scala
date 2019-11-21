package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID


class GraphCleanupUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
    val expectedQuery: String = s"""
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
    
    override def beforeAll()
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "legacyInstructionSet.ttl", "legacyGraphSpec.ttl")
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    before
    {
        helper.deleteAllTriplesInDatabase(testCxn)
    }
  
    test("generated biobank encounter cleanup query matched expected query")
    {
        helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess", expectedQuery) should be (true) 
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
      RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess", dataValidationMode, false)
      
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
}