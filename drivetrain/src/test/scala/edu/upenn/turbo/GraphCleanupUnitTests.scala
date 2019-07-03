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
    
    val processMeta: String = """
    ASK 
    { 
      Graph pmbb:processes
      {
          ?processBoundary obo:RO_0002223 ontologies:TURBO_0010183 .
          ?processBoundary a obo:BFO_0000035 .
          ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
          ?timeMeasDatum a obo:IAO_0000416 .
          ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
          
          ontologies:TURBO_0010183 
              turbo:TURBO_0010106 ?someQuery ;
              turbo:TURBO_0010107 ?someRuntime ;
              turbo:TURBO_0010108 ?someNumberOfTriples;
              turbo:TURBO_0010186 pmbb:expanded ;
              turbo:TURBO_0010187 pmbb:expanded ;
      }
    }
    """
 
    test ("healthcare encounter entity linking - all fields")
    {
      // these triples were generated from the output of the first healthcare encounter expansion test and the first homo sapiens expansion unit test on 4/9/19
      val insert = s"""
            INSERT DATA
            {
            Graph pmbb:expanded {
                pmbb:prescription1 obo:IAO_0000142 pmbb:someRxNormDrug .
                pmbb:prescription1 a obo:PDRO_0000001 .
              }
            #Graph <http://data.bioontology.org/ontologies/RXNORM/>
            #{
            #    pmbb:someRxNormDrug a owl:Class .
            #}
            }
        """
      update.updateSparql(testCxn, insert)
      RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/RxNormUrlCleanupProcess")
      
        val check: String = """
          ASK
          {
          graph pmbb:expanded {
              ?homoSapiens obo:RO_0000056 ?healthcareEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000093 .
          		?puirole obo:BFO_0000054 ?healthcareEncounter .
          		
          		?weightDatum obo:IAO_0000136 ?homoSapiens.
          		?heightDatum obo:IAO_0000136 ?homoSapiens.
          		?weightDatum obo:IAO_0000221 ?homoSapiensWeight .
          		?heightDatum obo:IAO_0000221 ?homoSapiensHeight .
          		
          		?homoSapiens a obo:NCBITaxon_9606 .
          		?homoSapiens obo:RO_0000086 ?homoSapiensWeight .
          		?homoSapiensWeight a obo:PATO_0000128 .
          		?homoSapiens obo:RO_0000086 ?homoSapiensHeight .
          		?homoSapiensHeight a obo:PATO_0000119 .
          		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
          		?homoSapiensCrid a turbo:TURBO_0000503 .
          		
          		?healthcareEncounter a obo:OGMS_0000097 .
          		?healthcareEncounterCrid obo:IAO_0000219 ?healthcareEncounter .
          		?healthcareEncounterCrid a turbo:TURBO_0000508 .

          		?heightDatum a turbo:TURBO_0010138 .
          		?weightDatum a obo:OBI_0001929 .
          		
                ?BMI obo:IAO_0000136 ?homoSapiens .
                ?BMI a efo:EFO_0004340 .
                ?BMI obo:IAO_0000581 ?encounterDate .
                ?encounterStart a turbo:TURBO_0000511 .
          		?encounterStart obo:RO_0002223 ?healthcareEncounter .          
          		?encounterDate a turbo:TURBO_0000512 .
          		?encounterDate obo:IAO_0000136 ?encounterStart .
          }}
          """
        
        update.querySparqlBoolean(testCxn, check).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
      
        val processInputsOutputs: String = """
          
          ASK
          {
              GRAPH pmbb:processes
              {
                  ontologies:TURBO_0010183
                  
                    obo:OBI_0000293 pmbb:hcenc1 ;
                    obo:OBI_0000293 pmbb:part1 ;
                    obo:OBI_0000293 ?OGMS_0000097 ;
                    obo:OBI_0000293 ?EFO_0004340 ;
                    obo:OBI_0000293 ?TURBO_0010138 ;
                    obo:OBI_0000293 ?OBI_0001929 ;
                    obo:OBI_0000293 ?NCBITaxon_9606 ;
                    
                    ontologies:TURBO_0010184 ?OGMS_0000097 ;
                    ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                    ontologies:TURBO_0010184 ?EFO_0004340 ;
                    ontologies:TURBO_0010184 ?TURBO_0010138 ;
                    ontologies:TURBO_0010184 ?OBI_0001929 ;
                    ontologies:TURBO_0010184 ?PATO_0000119 ;
                    ontologies:TURBO_0010184 ?OBI_0000093 ;
                    ontologies:TURBO_0010184 ?PATO_0000128 ;
              }
              GRAPH pmbb:expanded
              {
                  ?OGMS_0000097 a obo:OGMS_0000097 .
                  ?EFO_0004340 a efo:EFO_0004340 .
                  ?TURBO_0010138 a turbo:TURBO_0010138 .
                  ?OBI_0001929 a obo:OBI_0001929 .
                  ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                  ?PATO_0000119 a obo:PATO_0000119 .
                  ?OBI_0000093 a obo:OBI_0000093 .
                  ?PATO_0000128 a obo:PATO_0000128 .
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
      
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
      
        var expectedPredicates: ArrayBuffer[String] = ArrayBuffer(
            "http://purl.obolibrary.org/obo/RO_0000056", "http://purl.obolibrary.org/obo/RO_0000086",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000221", "http://purl.obolibrary.org/obo/BFO_0000054",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000221", "http://purl.obolibrary.org/obo/RO_0000087",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates.toArray, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        result.size should be (expectedPredicates.size)
    }
}
