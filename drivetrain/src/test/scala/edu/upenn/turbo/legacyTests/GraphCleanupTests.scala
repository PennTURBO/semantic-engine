package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID


class GraphCleanupUnitTests extends FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    var graphDBMaterials: TurboGraphConnection = null
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
    val expectedQuery: String = s"""
      DELETE {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
      }
      }
      INSERT {
      GRAPH <${Globals.processNamedGraph}> {
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010161 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010169 .
      }
      }
      WHERE {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
      ?TURBO_0010169 rdf:type <http://transformunify.org/ontologies/TURBO_0010169> .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      }
       }
    """
    
    override def beforeAll()
    {
        assert("test" === System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"test\"; check your build.sbt file")
        
        graphDBMaterials = ConnectToGraphDB.initializeGraph()
        DrivetrainDriver.updateModel(graphDBMaterials, "testing_instruction_set.tis", "testing_graph_specification.gs")
        Globals.cxn = graphDBMaterials.getConnection()
        Globals.gmCxn = graphDBMaterials.getGmConnection()
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    before
    {
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
    }
  
    test("generated biobank encounter cleanup query matched expected query")
    {
        Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess", expectedQuery) should be (true) 
    }
    
    test ("remove SC bb enc to SC person link from expanded graph")
    {
      val insert = s"""
            INSERT DATA
            {
            Graph <${Globals.expandedNamedGraph}> {
                pmbb:scBbEnc1 turbo:TURBO_0010133 pmbb:scPerson1 .
                pmbb:scBbEnc1 a turbo:TURBO_0010169 .
                pmbb:scPerson1 a turbo:TURBO_0010161 .
              }
            }
        """
      SparqlUpdater.updateSparql(Globals.cxn, insert)
      RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess", Globals.dataValidationMode, false)
      
        val check1: String = s"""
          ASK
          {
          Graph <${Globals.expandedNamedGraph}> {
                pmbb:scBbEnc1 turbo:TURBO_0010133 pmbb:scPerson1 .
              }
          }
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, check1).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, Utilities.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/ShortcutBiobankEncounterToShortcutPersonCleanupProcess")).get should be (true)
      
        val count: String = s"SELECT * WHERE {Graph <${Globals.expandedNamedGraph}> {?s ?p ?o .}}"
        val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, count, "p")
        result.size should be (2)
      
        val processInputsOutputs: String = s"""
          
          ASK
          {
              GRAPH <${Globals.processNamedGraph}>
              {
                  ?process a turbo:TURBO_0010347 ;
                  
                    obo:OBI_0000293 pmbb:scBbEnc1 ;
                    obo:OBI_0000293 pmbb:scPerson1 ;
              }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, processInputsOutputs).get should be (true)
    }
}