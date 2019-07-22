package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID


class HealthcareEncounterEntityLinkingUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers with EntityLinkingUnitTestFields
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
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
 
    test ("healthcare encounter entity linking - all fields")
    {
        // these triples were generated from the output of the first healthcare encounter expansion test and the first homo sapiens expansion unit test on 4/9/19
        val insert = s"""
              INSERT DATA
              {
              Graph pmbb:expanded {
                  $healthcareEncounterTriplesAllFields
                  $homoSapiensTriplesAllFields
                }
              }
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010183")
          
        update.querySparqlBoolean(testCxn, healthcareEncounterAllFieldsOutput).get should be (true)
        update.querySparqlBoolean(testCxn, processMetaHealthcare).get should be (true)
      
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
                    obo:OBI_0000293 ?HTN_00000000 ;
                    obo:OBI_0000293 ?HTN_00000001 ;
                    obo:OBI_0000293 ?VSO_0000006 ;
                    
                    ontologies:TURBO_0010184 ?OGMS_0000097 ;
                    ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                    ontologies:TURBO_0010184 ?EFO_0004340 ;
                    ontologies:TURBO_0010184 ?TURBO_0010138 ;
                    ontologies:TURBO_0010184 ?OBI_0001929 ;
                    ontologies:TURBO_0010184 ?PATO_0000119 ;
                    ontologies:TURBO_0010184 ?OBI_0000093 ;
                    ontologies:TURBO_0010184 ?PATO_0000128 ;
                    ontologies:TURBO_0010184 ?HTN_00000000 ;
                    ontologies:TURBO_0010184 ?HTN_00000001 ;
                    ontologies:TURBO_0010184 ?VSO_0000004 .
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
                  ?HTN_00000000 a obo:HTN_00000000 .
                  ?HTN_00000001 a obo:HTN_00000001 .
                  ?VSO_0000004 a obo:VSO_0000004 .
                  ?VSO_0000006 a obo:VSO_0000006 .
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
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://purl.obolibrary.org/obo/IAO_0000221",
            "http://purl.obolibrary.org/obo/IAO_0000221","http://purl.obolibrary.org/obo/RO_0000052",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        homoSapiensAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
        healthcareEncounterAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
        
        helper.checkStringArraysForEquivalency(expectedPredicates.toArray, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        result.size should be (expectedPredicates.size)
    }
    
    test("healthcare encounter entity linking - minimum fields")
    {
      // these triples were generated from the output of the second healthcare encounter expansion test and the second homo sapiens expansion unit test on 4/9/19
      val insert = s"""
            INSERT DATA
            {
            Graph pmbb:expanded {
                $healthcareEncounterTriplesMinimumFields
                $homoSapiensTriplesMinimumFields
              }
            }
        """
      update.updateSparql(testCxn, insert)
      RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010183")
        
        val check: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  ?homoSapiens obo:RO_0000056 ?healthcareEncounter .
                  ?homoSapiens obo:RO_0000087 ?puirole .
              		?puirole a obo:OBI_0000093 .
              		?puirole obo:BFO_0000054 ?healthcareEncounter .
              		
              		?homoSapiens a obo:NCBITaxon_9606 .
              		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
              		?homoSapiensCrid a turbo:TURBO_0010092 .
              		
              		?healthcareEncounter a obo:OGMS_0000097 .
              		?healthcareEncounterCrid obo:IAO_0000219 ?healthcareEncounter .
              		?healthcareEncounterCrid a turbo:TURBO_0000508 .
              }
          }
          """
        
        val noMeasurementsOrDate: String = """
              ASK {
              values ?notexists {
                obo:PATO_0000119 
                obo:PATO_0000128 
                obo:OBI_0000445 
                turbo:TURBO_0001511 
                turbo:TURBO_0010138 
                obo:OBI_0001929
                efo:EFO_0004340
                turbo:TURBO_0000511
                turbo:TURBO_0000512
                turbo:VSO_0000004
                }
              ?s a ?notexists . }
          """
        
        update.querySparqlBoolean(testCxn, check).get should be (true)
        update.querySparqlBoolean(testCxn, noMeasurementsOrDate).get should be (false)
        update.querySparqlBoolean(testCxn, processMetaHealthcare).get should be (true)
      
        val processInputsOutputs: String = """
          
          ASK
          {
              GRAPH pmbb:processes
              {
                  ontologies:TURBO_0010183
                  
                    obo:OBI_0000293 pmbb:hcenc1 ;
                    obo:OBI_0000293 pmbb:part1 ;
                    obo:OBI_0000293 ?OGMS_0000097 ;
                    obo:OBI_0000293 ?NCBITaxon_9606 ;
                    
                    ontologies:TURBO_0010184 ?OGMS_0000097 ;
                    ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                    ontologies:TURBO_0010184 ?OBI_0000093 ;
              }
              GRAPH pmbb:expanded
              {
                  ?OGMS_0000097 a obo:OGMS_0000097 .
                  ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                  ?OBI_0000093 a obo:OBI_0000093 .
              }
          }
          
          """
        
          update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
      
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
      
        var expectedPredicates: ArrayBuffer[String] = ArrayBuffer(
            "http://purl.obolibrary.org/obo/RO_0000056", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000054", "http://purl.obolibrary.org/obo/RO_0000087"
        )
        homoSapiensMinimumFieldsExpectedPredicates.foreach(expectedPredicates += _)
        healthcareEncounterMinimumFieldsExpectedPredicates.foreach(expectedPredicates += _)
        
        helper.checkStringArraysForEquivalency(expectedPredicates.toArray, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        result.size should be (expectedPredicates.size)
    }
}
    
    class BiobankEncounterEntityLinkingUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers with EntityLinkingUnitTestFields
    {
        val clearTestingRepositoryAfterRun: Boolean = false
        
        var conclusionationNamedGraph: IRI = null
        var masterConclusionation: IRI = null
        var masterPlanspec: IRI = null
        var masterPlan: IRI = null
        
        RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
        
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
        
        test("biobank encounter entity linking - all fields")
        {
            // these triples were generated from the output of the first biobank encounter expansion test and the first homo sapiens expansion unit test on 4/10/19
            val insert = s"""
                  INSERT DATA
                  {
                  Graph pmbb:expanded {
                      $biobankEncounterTriplesAllFields
                      $homoSapiensTriplesAllFields
                    }
                  }
              """
            update.updateSparql(testCxn, insert)
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010182")
            
             val check: String = """
              ASK
              {
              graph pmbb:expanded {
                  ?homoSapiens obo:RO_0000056 ?biobankEncounter .
                  ?homoSapiens obo:RO_0000087 ?puirole .
              		?puirole a obo:OBI_0000097 .
              		?puirole obo:BFO_0000054 ?biobankEncounter .

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
              		?homoSapiensCrid a turbo:TURBO_0010092 .
              		
              		?biobankEncounter a turbo:TURBO_0000527 .
              		?biobankEncounterCrid obo:IAO_0000219 ?biobankEncounter .
              		?biobankEncounterCrid a turbo:TURBO_0000533 .
    
              		?heightDatum a turbo:TURBO_0010138 .
              		?weightDatum a obo:OBI_0001929 .
              		
                  ?BMI obo:IAO_0000136 ?homoSapiens .
                  ?BMI a efo:EFO_0004340 .
                  ?BMI obo:IAO_0000581 ?encounterDate .
                  ?encounterStart a turbo:TURBO_0000531 .
              		?encounterStart obo:RO_0002223 ?biobankEncounter .          
              		?encounterDate a turbo:TURBO_0000532 .
              		?encounterDate obo:IAO_0000136 ?encounterStart .
              }}
              """
            
        update.querySparqlBoolean(testCxn, check).get should be (true)
        update.querySparqlBoolean(testCxn, processMetaBiobank).get should be (true)
            
        val processInputsOutputs: String = """
          
          ASK
          {
              GRAPH pmbb:processes
              {
                  ontologies:TURBO_0010182
                  
                    obo:OBI_0000293 pmbb:bbenc1 ;
                    obo:OBI_0000293 pmbb:part1 ;
                    obo:OBI_0000293 ?TURBO_0000527 ;
                    obo:OBI_0000293 ?EFO_0004340 ;
                    obo:OBI_0000293 ?TURBO_0010138 ;
                    obo:OBI_0000293 ?OBI_0001929 ;
                    obo:OBI_0000293 ?NCBITaxon_9606 ;
                    
                    ontologies:TURBO_0010184 ?TURBO_0000527 ;
                    ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                    ontologies:TURBO_0010184 ?EFO_0004340 ;
                    ontologies:TURBO_0010184 ?TURBO_0010138 ;
                    ontologies:TURBO_0010184 ?OBI_0001929 ;
                    ontologies:TURBO_0010184 ?PATO_0000119 ;
                    ontologies:TURBO_0010184 ?OBI_0000097 ;
                    ontologies:TURBO_0010184 ?PATO_0000128 ;
              }
              GRAPH pmbb:expanded
              {
                  ?TURBO_0000527 a turbo:TURBO_0000527 .
                  ?EFO_0004340 a efo:EFO_0004340 .
                  ?TURBO_0010138 a turbo:TURBO_0010138 .
                  ?OBI_0001929 a obo:OBI_0001929 .
                  ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                  ?PATO_0000119 a obo:PATO_0000119 .
                  ?OBI_0000097 a obo:OBI_0000097 .
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
          homoSapiensAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
          biobankEncounterAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
          
          helper.checkStringArraysForEquivalency(expectedPredicates.toArray, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
          result.size should be (expectedPredicates.size)
      }
    
      test("biobank encounter entity linking - minimum fields")
      {
          // these triples were generated from the output of the second biobank encounter expansion test and the first homo sapiens expansion unit test on 4/10/19
          val insert = s"""
                INSERT DATA
                {
                Graph pmbb:expanded {
                    $biobankEncounterTriplesMinimumFields
                    $homoSapiensTriplesMinimumFields
                  }
                }
            """
          update.updateSparql(testCxn, insert)
          RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010182")
          
          val check: String = """
            ASK
            {
                graph pmbb:expanded
                {
                    ?homoSapiens obo:RO_0000056 ?biobankEncounter .
                    ?homoSapiens obo:RO_0000087 ?puirole .
                		?puirole a obo:OBI_0000097 .
                		?puirole obo:BFO_0000054 ?biobankEncounter .
                		
                		?homoSapiens a obo:NCBITaxon_9606 .
                		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
                		?homoSapiensCrid a turbo:TURBO_0010092 .
                		
                		?biobankEncounter a turbo:TURBO_0000527 .
                		?biobankEncounterCrid obo:IAO_0000219 ?biobankEncounter .
                		?biobankEncounterCrid a turbo:TURBO_0000533 .
                }
            }
            """
          
          val noHeightWeightBmiOrDate: String = """
                ASK {
                values ?heightOrWeight {
                  obo:PATO_0000119 
                  obo:PATO_0000128 
                  obo:OBI_0000445 
                  turbo:TURBO_0001511 
                  turbo:TURBO_0010138 
                  obo:OBI_0001929
                  efo:EFO_0004340
                  turbo:TURBO_0000531
                  turbo:TURBO_0000532
                  }
                ?s a ?heightOrWeight . }
            """
          
          update.querySparqlBoolean(testCxn, check).get should be (true)
          update.querySparqlBoolean(testCxn, noHeightWeightBmiOrDate).get should be (false)
          update.querySparqlBoolean(testCxn, processMetaBiobank).get should be (true)
          
          val processInputsOutputs: String = """
          
          ASK
          {
              GRAPH pmbb:processes
              {
                  ontologies:TURBO_0010182
                  
                    obo:OBI_0000293 pmbb:bbenc1 ;
                    obo:OBI_0000293 pmbb:part1 ;
                    obo:OBI_0000293 ?TURBO_0000527 ;
                    obo:OBI_0000293 ?NCBITaxon_9606 ;
                    
                    ontologies:TURBO_0010184 ?TURBO_0000527 ;
                    ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                    ontologies:TURBO_0010184 ?OBI_0000097 ;
              }
              GRAPH pmbb:expanded
              {
                  ?TURBO_0000527 a turbo:TURBO_0000527 .
                  ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                  ?OBI_0000097 a obo:OBI_0000097 .
              }
          }
          
          """
        
          update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
          
          val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
          val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
          var expectedPredicates: ArrayBuffer[String] = ArrayBuffer(
            "http://purl.obolibrary.org/obo/RO_0000056", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000054", "http://purl.obolibrary.org/obo/RO_0000087"
          )
          homoSapiensMinimumFieldsExpectedPredicates.foreach(expectedPredicates += _)
          biobankEncounterMinimumFieldsExpectedPredicates.foreach(expectedPredicates += _)
          
          helper.checkStringArraysForEquivalency(expectedPredicates.toArray, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
          result.size should be (expectedPredicates.size)
      }
}
    
  class EntityLinkingIntegrationTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers with EntityLinkingUnitTestFields
  {
      val clearTestingRepositoryAfterRun: Boolean = false
      
      var conclusionationNamedGraph: IRI = null
      var masterConclusionation: IRI = null
      var masterPlanspec: IRI = null
      var masterPlan: IRI = null
      
      RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
      
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
  
      test("biobank encounter and healthcare encounter link to homo sapiens - all fields")
      {
          val insert = s"""
                INSERT DATA
                {
                Graph pmbb:expanded {
                    $healthcareEncounterTriplesAllFields
                    $biobankEncounterTriplesAllFields
                    $homoSapiensTriplesAllFields
                  }
                }
            """
          update.updateSparql(testCxn, insert)
          RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010182")
          RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/TURBO_0010183")
          
        update.querySparqlBoolean(testCxn, biobankEncounterAllFieldsOutput).get should be (true)
        update.querySparqlBoolean(testCxn, healthcareEncounterAllFieldsOutput).get should be (true)
          
        val twoLinks = """
        select (count (?encounter) as ?encountercount) where
        {
            ?homosapiens obo:RO_0000056 ?encounter .
        }
        """
        
        val onlyOnePUIRole = """
          select (count (?role) as ?rolecount) where
          {
              ?homosapiens a obo:NCBITaxon_9606 .
              ?homosapiens obo:RO_0000087 ?role .
              ?role a obo:OBI_0000097 .
          }
          """
        
        val onlyOnePatientRole = """
          select (count (?role) as ?rolecount) where
          {
              ?homosapiens a obo:NCBITaxon_9606 .
              ?homosapiens obo:RO_0000087 ?role .
              ?role a obo:OBI_0000093 .
          }
          """
        
        val onlyOneHeight = """
          select (count (?height) as ?heightcount) where
          {
              ?homosapiens a obo:NCBITaxon_9606 .
              ?homosapiens obo:RO_0000086 ?height .
              ?height a obo:PATO_0000119 .
          }
          """
        
        val onlyOneWeight = """
          select (count (?weight) as ?weightcount) where
          {
              ?homosapiens a obo:NCBITaxon_9606 .
              ?homosapiens obo:RO_0000086 ?weight .
              ?weight a obo:PATO_0000128 .
          }
          """
        
        update.querySparqlAndUnpackTuple(testCxn, onlyOnePUIRole, "rolecount")(0).split("\"")(1) should be ("1")
        update.querySparqlAndUnpackTuple(testCxn, onlyOnePatientRole, "rolecount")(0).split("\"")(1) should be ("1")
        update.querySparqlAndUnpackTuple(testCxn, onlyOneHeight, "heightcount")(0).split("\"")(1) should be ("1")
        update.querySparqlAndUnpackTuple(testCxn, onlyOneWeight, "weightcount")(0).split("\"")(1) should be ("1")
        update.querySparqlAndUnpackTuple(testCxn, twoLinks, "encountercount")(0).split("\"")(1) should be ("2")
        update.querySparqlBoolean(testCxn, processMetaBiobank).get should be (true)
        update.querySparqlBoolean(testCxn, processMetaHealthcare).get should be (true)
          
        val count: String = "SELECT * WHERE {GRAPH pmbb:expanded {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        var expectedPredicates: ArrayBuffer[String] = ArrayBuffer(
            "http://purl.obolibrary.org/obo/RO_0000056", "http://purl.obolibrary.org/obo/RO_0000086",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000221", "http://purl.obolibrary.org/obo/BFO_0000054",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/RO_0000087",
            "http://purl.obolibrary.org/obo/IAO_0000221", "http://purl.obolibrary.org/obo/RO_0000087",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0000056",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000221", "http://purl.obolibrary.org/obo/BFO_0000054",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/IAO_0000221",
            "http://purl.obolibrary.org/obo/IAO_0000221", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000221","http://purl.obolibrary.org/obo/RO_0000052"
        )
        homoSapiensAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
        biobankEncounterAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
        healthcareEncounterAllFieldsExpectedPredicates.foreach(expectedPredicates += _)
        
        helper.checkStringArraysForEquivalency(expectedPredicates.toArray, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        result.size should be (expectedPredicates.size)
    }
}
  
trait EntityLinkingUnitTestFields
{
     val homoSapiensTriplesAllFields = """
      
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
      <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161> .
      <http://www.itmat.upenn.edu/biobank/crid1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010168> .
      <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010092> .
      <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000504> .
      <http://www.itmat.upenn.edu/biobank/7c68a8a36f1a25a297cc895cac38bfe4fb6570462f1fc07b13bc888e6cedcfa0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000505> .
      <http://www.itmat.upenn.edu/biobank/9bf9ca30cf4611505dd489a4288be250d2b89fa522f5d002589290addc4e2ddb> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OMRSE_00000138> .
      <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
      <http://www.itmat.upenn.edu/biobank/3bc5df8b984bcd68c5f97235bccb0cdd2a3327746032538ff45acf0d9816bac6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PATO_0000047> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
      <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OMRSE_00000181> .
      <http://www.itmat.upenn.edu/biobank/574d5db4ff45a0ae90e4b258da5bbdea7c5c1f25c8bb3ed5ff6c781e22c85f61> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OMRSE_00000099> .
      <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.ebi.ac.uk/efo/EFO_0004950> .
      <http://www.itmat.upenn.edu/biobank/d8f6fe431ccb98b259f34f0608c2ff5f5755e149531a5cac6959de05f8e8829c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/UBERON_0035946> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://purl.org/dc/elements/1.1/title> "part_expand" .
      <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> .
      <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> .
      <http://www.itmat.upenn.edu/biobank/7c68a8a36f1a25a297cc895cac38bfe4fb6570462f1fc07b13bc888e6cedcfa0> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> .
      <http://www.itmat.upenn.edu/biobank/7c68a8a36f1a25a297cc895cac38bfe4fb6570462f1fc07b13bc888e6cedcfa0> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> .
      <http://www.itmat.upenn.edu/biobank/9bf9ca30cf4611505dd489a4288be250d2b89fa522f5d002589290addc4e2ddb> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> .
      <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> .
      <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> .
      <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> .
      <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/7c68a8a36f1a25a297cc895cac38bfe4fb6570462f1fc07b13bc888e6cedcfa0> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/7c68a8a36f1a25a297cc895cac38bfe4fb6570462f1fc07b13bc888e6cedcfa0> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/9bf9ca30cf4611505dd489a4288be250d2b89fa522f5d002589290addc4e2ddb> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> .
      <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> .
      <http://www.itmat.upenn.edu/biobank/9bf9ca30cf4611505dd489a4288be250d2b89fa522f5d002589290addc4e2ddb> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> .
      <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> .
      <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/d8f6fe431ccb98b259f34f0608c2ff5f5755e149531a5cac6959de05f8e8829c> .
      <http://www.itmat.upenn.edu/biobank/crid1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/part1> .
      <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> .
      <http://www.itmat.upenn.edu/biobank/7c68a8a36f1a25a297cc895cac38bfe4fb6570462f1fc07b13bc888e6cedcfa0> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0000410> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/a6e75672d6cf70e372db48f538ec07a5e56d943a3c98b8f5da4b4c9d146808b0> .
      <http://www.itmat.upenn.edu/biobank/574d5db4ff45a0ae90e4b258da5bbdea7c5c1f25c8bb3ed5ff6c781e22c85f61> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> .
      <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> <http://purl.obolibrary.org/obo/RO_0000086> <http://www.itmat.upenn.edu/biobank/3bc5df8b984bcd68c5f97235bccb0cdd2a3327746032538ff45acf0d9816bac6> .
      <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> <http://transformunify.org/ontologies/TURBO_0000303> <http://www.itmat.upenn.edu/biobank/d8f6fe431ccb98b259f34f0608c2ff5f5755e149531a5cac6959de05f8e8829c> .
      <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> <http://transformunify.org/ontologies/TURBO_0010094> "4" .
      <http://www.itmat.upenn.edu/biobank/9bf9ca30cf4611505dd489a4288be250d2b89fa522f5d002589290addc4e2ddb> <http://transformunify.org/ontologies/TURBO_0010094> "F" .
      <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://transformunify.org/ontologies/TURBO_0010094> "04/May/1969" .
      <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://transformunify.org/ontologies/TURBO_0010096> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
      <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> <http://transformunify.org/ontologies/TURBO_0010095> "asian" .
      <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> .
                      
    """
    
    val homoSapiensTriplesMinimumFields = """
      
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
      <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161> .
      <http://www.itmat.upenn.edu/biobank/crid1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010168> .
      <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010092> .
      <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000504> .
      <http://www.itmat.upenn.edu/biobank/259d0feee9e4784e50007726fbf2049eea067363b2feac34a9458b57f0aa5842> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000505> .
      <http://www.itmat.upenn.edu/biobank/52ae0822b37763ef4964e36a337909f88cd334b34c3cebfc4c45d39ecf5851e5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
      <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
      <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> <http://purl.org/dc/elements/1.1/title> "part_expand" .
      <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> .
      <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> .
      <http://www.itmat.upenn.edu/biobank/259d0feee9e4784e50007726fbf2049eea067363b2feac34a9458b57f0aa5842> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> .
      <http://www.itmat.upenn.edu/biobank/259d0feee9e4784e50007726fbf2049eea067363b2feac34a9458b57f0aa5842> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> .
      <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> .
      <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/259d0feee9e4784e50007726fbf2049eea067363b2feac34a9458b57f0aa5842> .
      <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> .
      <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/259d0feee9e4784e50007726fbf2049eea067363b2feac34a9458b57f0aa5842> .
      <http://www.itmat.upenn.edu/biobank/crid1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/part1> .
      <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/52ae0822b37763ef4964e36a337909f88cd334b34c3cebfc4c45d39ecf5851e5> .
      <http://www.itmat.upenn.edu/biobank/259d0feee9e4784e50007726fbf2049eea067363b2feac34a9458b57f0aa5842> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0000410> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/9d4616b210e7928cc9656994e7411bc99b6a880eff944c6ceb1f3d87ca40b59a> .
      <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> <http://transformunify.org/ontologies/TURBO_0010094> "4" .
      <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/52ae0822b37763ef4964e36a337909f88cd334b34c3cebfc4c45d39ecf5851e5> .
      
      """
    
    val biobankEncounterTriplesAllFields = """
      
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
      <http://www.itmat.upenn.edu/biobank/bbenc1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010169> .
      <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161> .
      <http://www.itmat.upenn.edu/biobank/763cd05b4e2c1d595683d68f0c0900a346a50ef9df4a07e44d16329ec54fdbbc> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.ebi.ac.uk/efo/EFO_0004340> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
      <http://www.itmat.upenn.edu/biobank/32e6a74ee52a6512f307522c854a7f4271f354ad0b6c68d651a5a629c1ed6adf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010138> .
      <http://www.itmat.upenn.edu/biobank/b5f78aa8544ab0b5f4bbac314e410f5054aecf5bb9d5c181aeb27793c1f43689> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001929> .
      <http://www.itmat.upenn.edu/biobank/a4210c3066e67816960c5f94dafad5828306cb88cbafedc9b16530e198b33855> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
      <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
      <http://www.itmat.upenn.edu/biobank/4c5ff0452d5b18eabfa0ab9be5755ab67acbf41a56e101797cf8a4336fa2d3bd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000535> .
      <http://www.itmat.upenn.edu/biobank/d9277f89b6cbcd4fa9056e42a8477ea78899545ca1264945d1bd6760d42864ae> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000534> .
      <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000532> .
      <http://www.itmat.upenn.edu/biobank/f64fc6176f93fc774d1eeb60c7fc6670ced53ea7637dc2fe6a3c1d4e2c73fc4d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000531> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.org/dc/elements/1.1/title> "enc_expand.csv" .
      <http://www.itmat.upenn.edu/biobank/763cd05b4e2c1d595683d68f0c0900a346a50ef9df4a07e44d16329ec54fdbbc> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/32e6a74ee52a6512f307522c854a7f4271f354ad0b6c68d651a5a629c1ed6adf> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/b5f78aa8544ab0b5f4bbac314e410f5054aecf5bb9d5c181aeb27793c1f43689> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/4c5ff0452d5b18eabfa0ab9be5755ab67acbf41a56e101797cf8a4336fa2d3bd> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/4c5ff0452d5b18eabfa0ab9be5755ab67acbf41a56e101797cf8a4336fa2d3bd> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a4210c3066e67816960c5f94dafad5828306cb88cbafedc9b16530e198b33855> .
      <http://www.itmat.upenn.edu/biobank/d9277f89b6cbcd4fa9056e42a8477ea78899545ca1264945d1bd6760d42864ae> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/d9277f89b6cbcd4fa9056e42a8477ea78899545ca1264945d1bd6760d42864ae> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a4210c3066e67816960c5f94dafad5828306cb88cbafedc9b16530e198b33855> .
      <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/763cd05b4e2c1d595683d68f0c0900a346a50ef9df4a07e44d16329ec54fdbbc> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/32e6a74ee52a6512f307522c854a7f4271f354ad0b6c68d651a5a629c1ed6adf> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/b5f78aa8544ab0b5f4bbac314e410f5054aecf5bb9d5c181aeb27793c1f43689> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/4c5ff0452d5b18eabfa0ab9be5755ab67acbf41a56e101797cf8a4336fa2d3bd> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/d9277f89b6cbcd4fa9056e42a8477ea78899545ca1264945d1bd6760d42864ae> .
      <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> .
      <http://www.itmat.upenn.edu/biobank/a4210c3066e67816960c5f94dafad5828306cb88cbafedc9b16530e198b33855> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/4c5ff0452d5b18eabfa0ab9be5755ab67acbf41a56e101797cf8a4336fa2d3bd> .
      <http://www.itmat.upenn.edu/biobank/a4210c3066e67816960c5f94dafad5828306cb88cbafedc9b16530e198b33855> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/d9277f89b6cbcd4fa9056e42a8477ea78899545ca1264945d1bd6760d42864ae> .
      <http://www.itmat.upenn.edu/biobank/32e6a74ee52a6512f307522c854a7f4271f354ad0b6c68d651a5a629c1ed6adf> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000015> .
      <http://www.itmat.upenn.edu/biobank/b5f78aa8544ab0b5f4bbac314e410f5054aecf5bb9d5c181aeb27793c1f43689> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000009> .
      <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/f64fc6176f93fc774d1eeb60c7fc6670ced53ea7637dc2fe6a3c1d4e2c73fc4d> .
      <http://www.itmat.upenn.edu/biobank/a4210c3066e67816960c5f94dafad5828306cb88cbafedc9b16530e198b33855> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> .
      <http://www.itmat.upenn.edu/biobank/4c5ff0452d5b18eabfa0ab9be5755ab67acbf41a56e101797cf8a4336fa2d3bd> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/hcEncReg/biobank> .
      <http://www.itmat.upenn.edu/biobank/763cd05b4e2c1d595683d68f0c0900a346a50ef9df4a07e44d16329ec54fdbbc> <http://purl.obolibrary.org/obo/IAO_0000581> <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/cfd4123bfad19f8f967214441051484b9804ec84f5348068eb78dc60ecd75cfc> .
      <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/763cd05b4e2c1d595683d68f0c0900a346a50ef9df4a07e44d16329ec54fdbbc> .
      <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> turbo:TURBO_0010139 <http://www.itmat.upenn.edu/biobank/32e6a74ee52a6512f307522c854a7f4271f354ad0b6c68d651a5a629c1ed6adf> .
      <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> turbo:TURBO_0010139 <http://www.itmat.upenn.edu/biobank/b5f78aa8544ab0b5f4bbac314e410f5054aecf5bb9d5c181aeb27793c1f43689> .
      <http://www.itmat.upenn.edu/biobank/f64fc6176f93fc774d1eeb60c7fc6670ced53ea7637dc2fe6a3c1d4e2c73fc4d> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> .
      <http://www.itmat.upenn.edu/biobank/d9277f89b6cbcd4fa9056e42a8477ea78899545ca1264945d1bd6760d42864ae> <http://transformunify.org/ontologies/TURBO_0010094> "B" .
      <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> <http://transformunify.org/ontologies/TURBO_0010096> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
      <http://www.itmat.upenn.edu/biobank/35e97d81befe28a5861af55cd8f3009f2c3672cb2f7e73bd1a5e92e50216a96a> <http://transformunify.org/ontologies/TURBO_0010095> "15/Jan/2017" .
      <http://www.itmat.upenn.edu/biobank/bbenc1> turbo:TURBO_0010133 <http://www.itmat.upenn.edu/biobank/part1> .
      <http://www.itmat.upenn.edu/biobank/bbenc1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/93ee0d77147c0a3c5b05d81f60ab0158c5bdc8d0df0d1202d5570fb9d07e702b> .
      <http://www.itmat.upenn.edu/biobank/763cd05b4e2c1d595683d68f0c0900a346a50ef9df4a07e44d16329ec54fdbbc> <http://transformunify.org/ontologies/TURBO_0010094> "18.8252626423"^^<http://www.w3.org/2001/XMLSchema#float> .
      <http://www.itmat.upenn.edu/biobank/32e6a74ee52a6512f307522c854a7f4271f354ad0b6c68d651a5a629c1ed6adf> <http://transformunify.org/ontologies/TURBO_0010094> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
      <http://www.itmat.upenn.edu/biobank/b5f78aa8544ab0b5f4bbac314e410f5054aecf5bb9d5c181aeb27793c1f43689> <http://transformunify.org/ontologies/TURBO_0010094> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
      
      """
    
    val healthcareEncounterTriplesAllFields = """
      
      <http://transformunify.org/ontologies/diagnosis1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160> .
      <http://transformunify.org/ontologies/prescription1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010158> .
      <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161> .
      <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000508> .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000097> .
      <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.ebi.ac.uk/efo/EFO_0004340> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073> .
      <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000512> .
      <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000510> .
      <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000509> .
      <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010138> .
      <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001929> .
      <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000001> .
      <http://www.itmat.upenn.edu/biobank/1df62fd1b3118238280eeecbb1218cec0c3e7447322384f35386e47f9cb66cdd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000511> .
      <http://www.itmat.upenn.edu/biobank/fa576d3b9d946db990f08d9367e1247ba691d0fdf4b10068d84427b524fd6aae> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000561> .
      <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000562> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.org/dc/elements/1.1/title> "enc_expand.csv" .
      <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> .
      <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> .
      <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/fa576d3b9d946db990f08d9367e1247ba691d0fdf4b10068d84427b524fd6aae> .
      <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> .
      <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> .
      <http://www.itmat.upenn.edu/biobank/fa576d3b9d946db990f08d9367e1247ba691d0fdf4b10068d84427b524fd6aae> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> .
      <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000015> .
      <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000009> .
      <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/1df62fd1b3118238280eeecbb1218cec0c3e7447322384f35386e47f9cb66cdd> .
      <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> .
      <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0000440> .
      <http://www.itmat.upenn.edu/biobank/fa576d3b9d946db990f08d9367e1247ba691d0fdf4b10068d84427b524fd6aae> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> .
      <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://purl.obolibrary.org/obo/IAO_0000581> <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> turbo:TURBO_0010139 <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> turbo:TURBO_0010139 <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> .
      <http://www.itmat.upenn.edu/biobank/1df62fd1b3118238280eeecbb1218cec0c3e7447322384f35386e47f9cb66cdd> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/diagnosis1> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/prescription1> .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> .
      <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> <http://transformunify.org/ontologies/TURBO_0010094> "20" .
      <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> <http://transformunify.org/ontologies/TURBO_0010094> "3" .
      <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://transformunify.org/ontologies/TURBO_0010096> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0010095> "401.9" .
      <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://transformunify.org/ontologies/TURBO_0010095> "15/Jan/2017" .
      <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> <http://transformunify.org/ontologies/TURBO_0010095> "holistic soil from the ganges" .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0000703> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
      <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> obo:IAO_0000142 <http://transformunify.org/ontologies/someDrug> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0006515> "ICD-9" .
      <http://www.itmat.upenn.edu/biobank/hcenc1> turbo:TURBO_0010131 <http://www.itmat.upenn.edu/biobank/part1> .
      <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean> .
      <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0010014> "1"^^<http://www.w3.org/2001/XMLSchema#Integer> .
      <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0010014> "1"^^<http://www.w3.org/2001/XMLSchema#Integer> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010110> <http://transformunify.org/ontologies/TURBO_0000440> .
      <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://transformunify.org/ontologies/TURBO_0010094> "26.2577659792"^^<http://www.w3.org/2001/XMLSchema#float> .
      <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://transformunify.org/ontologies/TURBO_0010094> "177.8"^^<http://www.w3.org/2001/XMLSchema#float> .
      <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://transformunify.org/ontologies/TURBO_0010094> "83.0082554658"^^<http://www.w3.org/2001/XMLSchema#float> .
      <http://www.itmat.upenn.edu/biobank/eb02a191-24e3-48ea-97fb-3a822a0a35da>
        a obo:VSO_0000006 ;
        obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> ;
        obo:OBI_0000299 <http://www.itmat.upenn.edu/biobank/1eaefdde-6f9c-4c36-bb6b-a70b9a09b263>, <http://www.itmat.upenn.edu/biobank/2ffce06c-5a34-4fa9-ac10-dd3ddc5364d7> .
      <http://www.itmat.upenn.edu/biobank/1eaefdde-6f9c-4c36-bb6b-a70b9a09b263>
        a obo:HTN_00000000 ;
        obo:OBI_0001938 <http://www.itmat.upenn.edu/biobank/263a5315-e0e5-42dc-814a-157ff337a4fa> .
      <http://www.itmat.upenn.edu/biobank/263a5315-e0e5-42dc-814a-157ff337a4fa>
        a turbo:TURBO_0010150 ;
        obo:IAO_0000039 obo:UO_0000272 ;
        turbo:TURBO_0010094 71.7 .
      <http://www.itmat.upenn.edu/biobank/2ffce06c-5a34-4fa9-ac10-dd3ddc5364d7>
        a obo:HTN_00000001 ;
        obo:OBI_0001938 <http://www.itmat.upenn.edu/biobank/7b0cba9c-f81b-4363-83ff-7f4c7e13e3c0> .
      <http://www.itmat.upenn.edu/biobank/7b0cba9c-f81b-4363-83ff-7f4c7e13e3c0>
        a turbo:TURBO_0010149 ;
        obo:IAO_0000039 obo:UO_0000272 ;
        turbo:TURBO_0010094 130.2 .
      <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> obo:BFO_0000051 <http://www.itmat.upenn.edu/biobank/eb02a191-24e3-48ea-97fb-3a822a0a35da> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> obo:BFO_0000051 <http://www.itmat.upenn.edu/biobank/2ffce06c-5a34-4fa9-ac10-dd3ddc5364d7> .
      <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> obo:BFO_0000051 <http://www.itmat.upenn.edu/biobank/1eaefdde-6f9c-4c36-bb6b-a70b9a09b263> .
      <http://www.itmat.upenn.edu/biobank/1eaefdde-6f9c-4c36-bb6b-a70b9a09b263> obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
      <http://www.itmat.upenn.edu/biobank/2ffce06c-5a34-4fa9-ac10-dd3ddc5364d7> obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
        
      """
    
    val healthcareEncounterTriplesMinimumFields = """
      
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010158> .
      <http://www.itmat.upenn.edu/biobank/5ec200820a87fb752fb7d5830e38c94de552a7a4bb733e167c3d17672834d912> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000508> .
      <http://www.itmat.upenn.edu/biobank/20b777012bab4374cbb3649f419024ae0c672e888b4346f19c11fac58611b1af> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000097> .
      <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
      <http://www.itmat.upenn.edu/biobank/a72b1bc74f01bfbab16ff1337637dcafa142942f8d5d5467f70d86829da00ca4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000510> .
      <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000509> .
      <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> <http://purl.org/dc/elements/1.1/title> "enc_expand.csv" .
      <http://www.itmat.upenn.edu/biobank/a72b1bc74f01bfbab16ff1337637dcafa142942f8d5d5467f70d86829da00ca4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/5ec200820a87fb752fb7d5830e38c94de552a7a4bb733e167c3d17672834d912> .
      <http://www.itmat.upenn.edu/biobank/a72b1bc74f01bfbab16ff1337637dcafa142942f8d5d5467f70d86829da00ca4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> .
      <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/5ec200820a87fb752fb7d5830e38c94de552a7a4bb733e167c3d17672834d912> .
      <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> .
      <http://www.itmat.upenn.edu/biobank/5ec200820a87fb752fb7d5830e38c94de552a7a4bb733e167c3d17672834d912> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/a72b1bc74f01bfbab16ff1337637dcafa142942f8d5d5467f70d86829da00ca4> .
      <http://www.itmat.upenn.edu/biobank/5ec200820a87fb752fb7d5830e38c94de552a7a4bb733e167c3d17672834d912> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> .
      <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/a72b1bc74f01bfbab16ff1337637dcafa142942f8d5d5467f70d86829da00ca4> .
      <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> .
      <http://www.itmat.upenn.edu/biobank/5ec200820a87fb752fb7d5830e38c94de552a7a4bb733e167c3d17672834d912> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/20b777012bab4374cbb3649f419024ae0c672e888b4346f19c11fac58611b1af> .
      <http://www.itmat.upenn.edu/biobank/a72b1bc74f01bfbab16ff1337637dcafa142942f8d5d5467f70d86829da00ca4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0000440> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/7e49f5dcf3dd16503b016b3be01d547c224fe311b6dc48d1bd8d87adb35c5c4b> .
      <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> <http://transformunify.org/ontologies/TURBO_0010094> "20" .
      <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/20b777012bab4374cbb3649f419024ae0c672e888b4346f19c11fac58611b1af> .
      <http://www.itmat.upenn.edu/biobank/hcenc1> turbo:TURBO_0010131 <http://www.itmat.upenn.edu/biobank/part1> .
                
      """
    
    val biobankEncounterTriplesMinimumFields = """
      
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
      <http://www.itmat.upenn.edu/biobank/bbenc1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010169> .
      <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
      <http://www.itmat.upenn.edu/biobank/511b3c30755c79b4db82e6f63aa0184e1905245721d6572eab8b12f1c6fe2c08> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
      <http://www.itmat.upenn.edu/biobank/b380eb5afb263581879f61fb67a0dd7cb58cd77a8c4101363c37ac6bb6beeaf7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
      <http://www.itmat.upenn.edu/biobank/cd2e354931b110e1be0be3c2f0d471b61aa14247e16124b04267a9ff0ae22906> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000535> .
      <http://www.itmat.upenn.edu/biobank/14cf4a0334ab31bcdc77147fe7ec611fc2ce0a8c8b4e90a0835c12109130f2c0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000534> .
      <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> <http://purl.org/dc/elements/1.1/title> "enc_expand.csv" .
      <http://www.itmat.upenn.edu/biobank/cd2e354931b110e1be0be3c2f0d471b61aa14247e16124b04267a9ff0ae22906> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> .
      <http://www.itmat.upenn.edu/biobank/cd2e354931b110e1be0be3c2f0d471b61aa14247e16124b04267a9ff0ae22906> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/511b3c30755c79b4db82e6f63aa0184e1905245721d6572eab8b12f1c6fe2c08> .
      <http://www.itmat.upenn.edu/biobank/14cf4a0334ab31bcdc77147fe7ec611fc2ce0a8c8b4e90a0835c12109130f2c0> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> .
      <http://www.itmat.upenn.edu/biobank/14cf4a0334ab31bcdc77147fe7ec611fc2ce0a8c8b4e90a0835c12109130f2c0> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/511b3c30755c79b4db82e6f63aa0184e1905245721d6572eab8b12f1c6fe2c08> .
      <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/cd2e354931b110e1be0be3c2f0d471b61aa14247e16124b04267a9ff0ae22906> .
      <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/14cf4a0334ab31bcdc77147fe7ec611fc2ce0a8c8b4e90a0835c12109130f2c0> .
      <http://www.itmat.upenn.edu/biobank/511b3c30755c79b4db82e6f63aa0184e1905245721d6572eab8b12f1c6fe2c08> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/cd2e354931b110e1be0be3c2f0d471b61aa14247e16124b04267a9ff0ae22906> .
      <http://www.itmat.upenn.edu/biobank/511b3c30755c79b4db82e6f63aa0184e1905245721d6572eab8b12f1c6fe2c08> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/14cf4a0334ab31bcdc77147fe7ec611fc2ce0a8c8b4e90a0835c12109130f2c0> .
      <http://www.itmat.upenn.edu/biobank/511b3c30755c79b4db82e6f63aa0184e1905245721d6572eab8b12f1c6fe2c08> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/b380eb5afb263581879f61fb67a0dd7cb58cd77a8c4101363c37ac6bb6beeaf7> .
      <http://www.itmat.upenn.edu/biobank/cd2e354931b110e1be0be3c2f0d471b61aa14247e16124b04267a9ff0ae22906> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/hcEncReg/biobank> .
      <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/2d2d5cd55e04c15781d74140e8d18f6f9e5abb631e77242d89f375be29f86b0a> .
      <http://www.itmat.upenn.edu/biobank/14cf4a0334ab31bcdc77147fe7ec611fc2ce0a8c8b4e90a0835c12109130f2c0> <http://transformunify.org/ontologies/TURBO_0010094> "B" .
      <http://www.itmat.upenn.edu/biobank/bbenc1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/b380eb5afb263581879f61fb67a0dd7cb58cd77a8c4101363c37ac6bb6beeaf7> .
      <http://www.itmat.upenn.edu/biobank/bbenc1> turbo:TURBO_0010133 pmbb:part1 .
      
      """
    
    val healthcareEncounterAllFieldsExpectedPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000581",
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
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/IAO_0000142",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://transformunify.org/ontologies/TURBO_0000703",
            "http://purl.obolibrary.org/obo/RO_0002223", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
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
            "http://purl.obolibrary.org/obo/IAO_0000142", "http://transformunify.org/ontologies/TURBO_0010014",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://transformunify.org/ontologies/TURBO_0010013",
            "http://transformunify.org/ontologies/TURBO_0010014", "http://transformunify.org/ontologies/TURBO_0010110",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/OBI_0001938", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039","http://purl.obolibrary.org/obo/IAO_0000039",
            "http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000051"
        )
        
    val healthcareEncounterMinimumFieldsExpectedPredicates = Array (
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
            "http://transformunify.org/ontologies/TURBO_0010113", "http://transformunify.org/ontologies/TURBO_0010131"
        )
        
    val biobankEncounterAllFieldsExpectedPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
             "http://purl.obolibrary.org/obo/OBI_0000299", "http://transformunify.org/ontologies/TURBO_0010133",
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
        
    val biobankEncounterMinimumFieldsExpectedPredicates = Array (
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
             "http://transformunify.org/ontologies/TURBO_0010113", "http://transformunify.org/ontologies/TURBO_0010133"
        )
        
    val homoSapiensAllFieldsExpectedPredicates = Array (
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
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://purl.obolibrary.org/obo/OBI_0000299"
        )
        
    val homoSapiensMinimumFieldsExpectedPredicates = Array (
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
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219"
        )
        
    val healthcareEncounterAllFieldsOutput: String = """
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
          		?homoSapiensBloodPressure a obo:VSO_0000004 .
          		?homoSapiensBloodPressure obo:RO_0000052 ?homoSapiens .
          		
          		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
          		?homoSapiensCrid a turbo:TURBO_0010092 .
          		
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
          		
          		?systolicBloodPressureDatum a obo:HTN_00000001 .
          		?diastolicBloodPressureDatum a obo:HTN_00000000 .
          		?systolicBloodPressureDatum obo:IAO_0000221 ?homoSapiensBloodPressure .
          		?diastolicBloodPressureDatum obo:IAO_0000221 ?homoSapiensBloodPressure .
          }}
          """
    
    val biobankEncounterAllFieldsOutput: String = """
            ASK
            {
            graph pmbb:expanded {
                ?homoSapiens obo:RO_0000056 ?biobankEncounter .
                ?homoSapiens obo:RO_0000087 ?puirole .
            		?puirole a obo:OBI_0000097 .
            		?puirole obo:BFO_0000054 ?biobankEncounter .

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
            		?homoSapiensCrid a turbo:TURBO_0010092 .
            		
            		?biobankEncounter a turbo:TURBO_0000527 .
            		?biobankEncounterCrid obo:IAO_0000219 ?biobankEncounter .
            		?biobankEncounterCrid a turbo:TURBO_0000533 .
  
            		?heightDatum a turbo:TURBO_0010138 .
            		?weightDatum a obo:OBI_0001929 .
            		
                ?BMI obo:IAO_0000136 ?homoSapiens .
                ?BMI a efo:EFO_0004340 .
                ?BMI obo:IAO_0000581 ?encounterDate .
                ?encounterStart a turbo:TURBO_0000531 .
            		?encounterStart obo:RO_0002223 ?biobankEncounter .          
            		?encounterDate a turbo:TURBO_0000532 .
            		?encounterDate obo:IAO_0000136 ?encounterStart .
            }}
            """
    
    val processMetaBiobank: String = """
        ASK 
        { 
          Graph pmbb:processes
          {
              ?processBoundary obo:RO_0002223 ontologies:TURBO_0010182 .
              ?processBoundary a obo:BFO_0000035 .
              ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
              ?timeMeasDatum a obo:IAO_0000416 .
              ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
              
              ontologies:TURBO_0010182 
                  turbo:TURBO_0010106 ?someQuery ;
                  turbo:TURBO_0010107 ?someRuntime ;
                  turbo:TURBO_0010108 ?someNumberOfTriples;
                  turbo:TURBO_0010186 pmbb:expanded ;
                  turbo:TURBO_0010187 pmbb:expanded ;
          }
        }
        """
        
        val processMetaHealthcare: String = """
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

}
    
