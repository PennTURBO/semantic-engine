package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class BiobankEncounterEntityLinkingUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers with EntityLinkingUnitTestFields
  {
      val clearTestingRepositoryAfterRun: Boolean = false
      
      var conclusionationNamedGraph: IRI = null
      var masterConclusionation: IRI = null
      var masterPlanspec: IRI = null
      var masterPlan: IRI = null
      
      RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
      
      val expectedQuery: String = s"""
        INSERT {
        GRAPH <$expandedNamedGraph> {
        ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000086> ?PATO_0000119 .
        ?NCBITaxon_9606 rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
        ?PATO_0000119 rdf:type <http://purl.obolibrary.org/obo/PATO_0000119> .
        ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000087> ?OBI_0000097 .
        ?OBI_0000097 rdf:type <http://purl.obolibrary.org/obo/OBI_0000097> .
        ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000086> ?PATO_0000128 .
        ?PATO_0000128 rdf:type <http://purl.obolibrary.org/obo/PATO_0000128> .
        ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000056> ?TURBO_0000527 .
        ?TURBO_0000527 rdf:type <http://transformunify.org/ontologies/TURBO_0000527> .
        ?OBI_0000097 <http://purl.obolibrary.org/obo/BFO_0000054> ?TURBO_0000527 .
        ?TURBO_0010138 <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
        ?TURBO_0010138 rdf:type <http://transformunify.org/ontologies/TURBO_0010138> .
        ?TURBO_0010138 <http://purl.obolibrary.org/obo/IAO_0000221> ?PATO_0000119 .
        ?OBI_0001929 <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
        ?OBI_0001929 rdf:type <http://purl.obolibrary.org/obo/OBI_0001929> .
        ?OBI_0001929 <http://purl.obolibrary.org/obo/IAO_0000221> ?PATO_0000128 .
        ?EFO_0004340 <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
        ?EFO_0004340 rdf:type <http://www.ebi.ac.uk/efo/EFO_0004340> .
        }
        GRAPH <$processNamedGraph> {
        <processURI> turbo:TURBO_0010184 ?NCBITaxon_9606 .
        <processURI> turbo:TURBO_0010184 ?PATO_0000119 .
        <processURI> turbo:TURBO_0010184 ?OBI_0000097 .
        <processURI> turbo:TURBO_0010184 ?PATO_0000128 .
        <processURI> turbo:TURBO_0010184 ?TURBO_0000527 .
        <processURI> turbo:TURBO_0010184 ?TURBO_0010138 .
        <processURI> turbo:TURBO_0010184 ?OBI_0001929 .
        <processURI> turbo:TURBO_0010184 ?EFO_0004340 .
        <processURI> obo:OBI_0000293 ?NCBITaxon_9606 .
        <processURI> obo:OBI_0000293 ?TURBO_0010138 .
        <processURI> obo:OBI_0000293 ?TURBO_0010161 .
        <processURI> obo:OBI_0000293 ?TURBO_0000527 .
        <processURI> obo:OBI_0000293 ?OBI_0001929 .
        <processURI> obo:OBI_0000293 ?EFO_0004340 .
        <processURI> obo:OBI_0000293 ?TURBO_0010169 .
        }
        }
        WHERE {
        GRAPH <$expandedNamedGraph> {
        ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010113> ?NCBITaxon_9606 .
        ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
        ?NCBITaxon_9606 rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
        ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010113> ?TURBO_0000527 .
        ?TURBO_0010169 rdf:type <http://transformunify.org/ontologies/TURBO_0010169> .
        ?TURBO_0000527 rdf:type <http://transformunify.org/ontologies/TURBO_0000527> .
        ?TURBO_0010169 <http://transformunify.org/ontologies/TURBO_0010133> ?TURBO_0010161 .
        OPTIONAL {
         ?TURBO_0000527 <http://purl.obolibrary.org/obo/OBI_0000299> ?EFO_0004340 .
         ?EFO_0004340 rdf:type <http://www.ebi.ac.uk/efo/EFO_0004340> .
        }
        OPTIONAL {
         ?TURBO_0000527 <http://transformunify.org/ontologies/TURBO_0010139> ?TURBO_0010138 .
         ?TURBO_0010138 rdf:type <http://transformunify.org/ontologies/TURBO_0010138> .
        }
        OPTIONAL {
         ?TURBO_0000527 <http://transformunify.org/ontologies/TURBO_0010139> ?OBI_0001929 .
         ?OBI_0001929 rdf:type <http://purl.obolibrary.org/obo/OBI_0001929> .
        }
        }
        BIND(IF (BOUND(?TURBO_0010138), uri(concat("$defaultPrefix",SHA256(CONCAT("?PATO_0000119","localUUID", str(?NCBITaxon_9606))))), ?unbound) AS ?PATO_0000119)
        BIND(IF (BOUND(?OBI_0001929), uri(concat("$defaultPrefix",SHA256(CONCAT("?PATO_0000128","localUUID", str(?NCBITaxon_9606))))), ?unbound) AS ?PATO_0000128)
        BIND(uri(concat("$defaultPrefix",SHA256(CONCAT("?OBI_0000097","localUUID")))) AS ?OBI_0000097)
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
      
      test("generated query matched expected query - biobank")
      {
          helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/BiobankEncounterEntityLinkingProcess", expectedQuery) should be (true) 
      }
      
      test("biobank encounter entity linking - all fields")
      {
          // these triples were generated from the output of the first biobank encounter expansion test and the first homo sapiens expansion unit test on 4/10/19
          val insert = s"""
                INSERT DATA
                {
                Graph <$expandedNamedGraph> {
                    $biobankEncounterTriplesAllFields
                    $homoSapiensTriplesAllFields
                  }
                }
            """
          update.updateSparql(testCxn, insert)
          RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterEntityLinkingProcess", dataValidationMode, false)
          
           val check: String = s"""
            ASK
            {
            graph <$expandedNamedGraph> {
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
          
      val processInputsOutputs: String = s"""
        
        ASK
        {
            GRAPH <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
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
            GRAPH <$expandedNamedGraph>
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
          
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
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
              Graph <$expandedNamedGraph> {
                  $biobankEncounterTriplesMinimumFields
                  $homoSapiensTriplesMinimumFields
                }
              }
          """
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/BiobankEncounterEntityLinkingProcess", dataValidationMode, false)
        
        val check: String = s"""
          ASK
          {
              graph <$expandedNamedGraph>
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
        
        val processInputsOutputs: String = s"""
        
        ASK
        {
            GRAPH <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:bbenc1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 ?TURBO_0000527 ;
                  obo:OBI_0000293 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000527 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  ontologies:TURBO_0010184 ?OBI_0000097 ;
            }
            GRAPH <$expandedNamedGraph>
            {
                ?TURBO_0000527 a turbo:TURBO_0000527 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                ?OBI_0000097 a obo:OBI_0000097 .
            }
        }
        
        """
      
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
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
    
trait EntityLinkingUnitTestFields extends ProjectwideGlobals
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
      
  val biobankEncounterAllFieldsOutput: String = s"""
          ASK
          {
          graph <$expandedNamedGraph> {
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
  
      val processMetaBiobank = helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/BiobankEncounterEntityLinkingProcess")
}
    