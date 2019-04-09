package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class EntityLinkingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val clearDatabaseAfterRun: Boolean = false
    val ooe = new ObjectOrientedExpander
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    DrivetrainProcessFromGraphModel.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    DrivetrainProcessFromGraphModel.setInstantiation("http://www.itmat.upenn.edu/biobank/test_instantiation_1")
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearDatabaseAfterRun)
    }
   
    /*test("biobank encounter expansion with entity linking - all fields")
    {
        val query = """INSERT DATA { 
          GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000635 "18.8252626423"^^xsd:float ;
          turbo:TURBO_0000624 "15/Jan/2017" ;
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000627 "61.2244897959"^^xsd:float ;
          turbo:TURBO_0000626 "180.34"^^xsd:float ;
          turbo:TURBO_0000625 "2017-01-15"^^xsd:date ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI ;
          turbo:TURBO_0010012 "http://transformunify.org/ontologies/UPHS"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010010 "4" ;
          turbo:ScBbEnc2UnexpandedHomoSapiens "http://transformunify.org/ontologies/UPHS/4" .
          }
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
          <http://transformunify.org/ontologies/UPHS/4> a obo:NCBITaxon_9606 .
          pmbb:crid1 obo:IAO_0000219 <http://transformunify.org/ontologies/UPHS/4> ;
          a turbo:TURBO_0000503 ;
          turbo:TURBO_0010084 "part_expand" ;
          turbo:TURBO_0010079 "4" ;
          turbo:TURBO_0010082 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        
        update.updateSparql(cxn, sparqlPrefixes + query)
        ooe.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        val check: String = """
          ASK
          {
              ?homoSapiens obo:RO_0000056 ?biobankEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?biobankEncounter .
          		?biobankEncounterCrid turbo:TURBO_0000302 ?homoSapiensCrid .
          		?weightDatum obo:IAO_0000136 ?homoSapiens.
          		?heightDatum obo:IAO_0000136 ?homoSapiens.
          		?weightDatum obo:IAO_0000221 ?homoSapiensWeight .
          		?heightDatum obo:IAO_0000221 ?homoSapiensHeight .
          		?weightAssay obo:OBI_0000293 ?homoSapiens.
          		?weightAssay obo:OBI_0000293 ?homoSapiens.
          		
          		?homoSapiens a obo:NCBITaxon_9606 .
          		?homoSapiens obo:RO_0000086 ?homoSapiensWeight .
          		?homoSapiensWeight a obo:PATO_0000128 .
          		?homoSapiens obo:RO_0000086 ?homoSapiensHeight .
          		?homoSapiensHeight a obo:PATO_0000119 .
          		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
          		?homoSapiensCrid a turbo:TURBO_0000503 .
          		
          		?biobankEncounter a turbo:TURBO_0000527 .
          		?biobankEncounterCrid obo:IAO_0000219 ?biobankEncounter .
          		?biobankEncounterCrid a turbo:TURBO_0000533 .
          		?weightAssay obo:BFO_0000050 ?biobankEncounter .
          		?weightAssay a obo:OBI_0000445 .
          		?heightAssay obo:BFO_0000050 ?biobankEncounter .
          		?heightAssay a turbo:TURBO_0001511 .
          		?heightDatum a obo:IAO_0000408 .
          		?heightAssay obo:OBI_0000299 ?heightDatum .
          		?weightDatum a obo:IAO_0000414 .
          		?weightAssay obo:OBI_0000299 ?weightDatum .
          		
          		?homoSapiens obo:BFO_0000051 ?adipose .
              ?adipose obo:BFO_0000050 ?homoSapiens .
              ?adipose a obo:UBERON_0001013 .
              ?adipose obo:IAO_0000136 ?BMI .
              ?BMI a efo:EFO_0004340 .
              ?BMI obo:IAO_0000581 ?encounterDate .
              ?encounterStart a turbo:TURBO_0000531 .
          		?encounterStart obo:RO_0002223 ?healthcareEncounter .          
          		?encounterDate a turbo:TURBO_0000532 .
          		?encounterDate obo:IAO_0000136 ?encounterStart .
          }
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + check).get should be (true)
    }*/
    
    /*test("biobank encounter expansion with entity linking - minimum fields")
    {
        val query = """INSERT DATA { 
          GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI ;
          turbo:TURBO_0010012 "http://transformunify.org/ontologies/UPHS"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010010 "4" ;
          turbo:ScBbEnc2UnexpandedHomoSapiens "http://transformunify.org/ontologies/UPHS/4" .
          }
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
          <http://transformunify.org/ontologies/UPHS/4> a obo:NCBITaxon_9606 .
          pmbb:crid1 obo:IAO_0000219 <http://transformunify.org/ontologies/UPHS/4> ;
          a turbo:TURBO_0000503 ;
          turbo:TURBO_0010084 "part_expand" ;
          turbo:TURBO_0010079 "4" ;
          turbo:TURBO_0010082 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        
        update.updateSparql(cxn, sparqlPrefixes + query)
        ooe.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        val check: String = """
          ASK
          {
              ?homoSapiens obo:RO_0000056 ?biobankEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?biobankEncounter .
          		?biobankEncounterCrid turbo:TURBO_0000302 ?homoSapiensCrid .
          		
          		?homoSapiens a obo:NCBITaxon_9606 .
          		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
          		?homoSapiensCrid a turbo:TURBO_0000503 .
          		
          		?biobankEncounter a turbo:TURBO_0000527 .
          		?biobankEncounterCrid obo:IAO_0000219 ?biobankEncounter .
          		?biobankEncounterCrid a turbo:TURBO_0000533 .
          }
          """
        
        val noHeightWeightAdiposeBmiOrDate: String = """
              ASK {
              values ?heightOrWeight {
                obo:PATO_0000119 
                obo:PATO_0000128 
                obo:OBI_0000445 
                turbo:TURBO_0001511 
                obo:IAO_0000408 
                obo:IAO_0000414
                obo:UBERON_0001013
                efo:EFO_0004340
                turbo:TURBO_0000511
                turbo:TURBO_0000512
                }
              ?s a ?heightOrWeight . }
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + check).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + noHeightWeightAdiposeBmiOrDate).get should be (false)
    }*/
    
    test ("healthcare encounter entity linking - all fields")
    {
      // these triples were generated from the output of the first healthcare encounter expansion test and the first homo sapiens expansion unit test on 4/9/19
      val insert = """
            INSERT DATA
            {
            Graph pmbb:expanded {
                # healthcare encounter triples start here
                <http://transformunify.org/ontologies/diagnosis1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_OGMS_0000073> .
                <http://transformunify.org/ontologies/prescription1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_PDRO_0000001> .
                <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_OGMS_0000097> .
                <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_NCBITaxon_9606> .
                <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000508> .
                <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000097> .
                <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.ebi.ac.uk/efo/EFO_0004340> .
                <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073> .
                <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000512> .
                <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000510> .
                <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000509> .
                <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000408> .
                <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000414> .
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
                <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/LNC/8302-2> .
                <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/LNC/29463-7> .
                <http://www.itmat.upenn.edu/biobank/f7ac10d8b83634c03102e5f8c2ef2bb8f1a3cf119eab301ba11c93590c697e87> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> .
                <http://www.itmat.upenn.edu/biobank/de72182e4f83bb1f02d8a2c4234272bb358ab845286f73201e42a352a9789ade> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0000440> .
                <http://www.itmat.upenn.edu/biobank/fa576d3b9d946db990f08d9367e1247ba691d0fdf4b10068d84427b524fd6aae> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> .
                <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://purl.obolibrary.org/obo/IAO_0000581> <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> .
                <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/3f46602e1d017e31a31d658ce6a368ff7ce0c95f28ce54b2305ffbcc269eb074> .
                <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> .
                <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> .
                <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> .
                <http://www.itmat.upenn.edu/biobank/1df62fd1b3118238280eeecbb1218cec0c3e7447322384f35386e47f9cb66cdd> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://purl.obolibrary.org/obo/RO_0002234> <http://transformunify.org/ontologies/diagnosis1> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://purl.obolibrary.org/obo/RO_0002234> <http://transformunify.org/ontologies/prescription1> .
                <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/RO_0002234> <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> .
                <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> <http://purl.obolibrary.org/obo/RO_0002234> <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000643> "enc_expand.csv" .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000644> "15/Jan/2017" .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000645> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000646> "177.8"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000647> "83.0082554658"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000648> "20" .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000655> "26.2577659792"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0004601> "401.9" .
                <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0004602> "ICD-9" .
                <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0004603> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
                <http://transformunify.org/ontologies/prescription1> <http://transformunify.org/ontologies/TURBO_0005601> "3" .
                <http://transformunify.org/ontologies/prescription1> <http://transformunify.org/ontologies/TURBO_0005611> "holistic soil from the ganges" .
                <http://transformunify.org/ontologies/prescription1> <http://transformunify.org/ontologies/TURBO_0005612> <http://transformunify.org/ontologies/someDrug> .
                <http://www.itmat.upenn.edu/biobank/fce7085bf1b83fb3b93f899b51d4be1345680b0711cc16b8a3ea58a74033bf44> <http://transformunify.org/ontologies/TURBO_0006510> "20" .
                <http://www.itmat.upenn.edu/biobank/44d4598fe897f4cbd1cfea023223300218415b47cb016f0ebb548c20e9911de4> <http://transformunify.org/ontologies/TURBO_0006510> "3" .
                <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://transformunify.org/ontologies/TURBO_0006511> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0006512> "401.9" .
                <http://www.itmat.upenn.edu/biobank/60071a3d5e5376521c3a2d29284177073ffa909f0b1e52a3b7cf8103ad46c9de> <http://transformunify.org/ontologies/TURBO_0006512> "15/Jan/2017" .
                <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> <http://transformunify.org/ontologies/TURBO_0006512> "holistic soil from the ganges" .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0000703> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0000306> <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
                <http://www.itmat.upenn.edu/biobank/875fc9f72f1dec3f42c4f0d7481aa793019e2999650c7d778cd52adb92b3746c> <http://transformunify.org/ontologies/TURBO_0000307> <http://transformunify.org/ontologies/someDrug> .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0006515> "ICD-9" .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010002> <http://www.itmat.upenn.edu/biobank/part1> .
                <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean> .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean> .
                <http://transformunify.org/ontologies/diagnosis1> <http://transformunify.org/ontologies/TURBO_0010014> "1"^^<http://www.w3.org/2001/XMLSchema#Integer> .
                <http://www.itmat.upenn.edu/biobank/fb2d542f8c40f9cfe47da7b8b41b023e0317c5db958748c3820921487ce57f5e> <http://transformunify.org/ontologies/TURBO_0010014> "1"^^<http://www.w3.org/2001/XMLSchema#Integer> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/c705c96ae7d783fe1df73258008081314b0bef58d2131cb422cde4c2ee9377b3> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010110> <http://transformunify.org/ontologies/TURBO_0000440> .
                <http://www.itmat.upenn.edu/biobank/18e9dc1aee9d7a306f4ae1075f109e82218aa606c391b9ae073bc15a2b2f2b0e> <http://transformunify.org/ontologies/TURBO_0010094> "26.2577659792"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://www.itmat.upenn.edu/biobank/51517ee9477e67cf4f34c3fb8e007d99532c0e78c3ac32c904aa9bb20de8d61a> <http://transformunify.org/ontologies/TURBO_0010094> "177.8"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://www.itmat.upenn.edu/biobank/bbde93384a4ae0247f0df4c1722840e34c40c91078e0ed4894292a99f79c57a6> <http://transformunify.org/ontologies/TURBO_0010094> "83.0082554658"^^<http://www.w3.org/2001/XMLSchema#float> .
                
                # homo sapiens triples start here
                <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
                <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_NCBITaxon_9606> .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_turbo_TURBO_0000503> .
                <http://www.itmat.upenn.edu/biobank/5101e324d4fe0cb3262fe7c62d44db731137e5d958e1d97a3a0614459cbe605d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
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
                <http://www.itmat.upenn.edu/biobank/98bf372edccf5fa4e39a303137617beeecb20d86e1fde340ec340747920c0c8d> <http://transformunify.org/ontologies/TURBO_0006510> "4" .
                <http://www.itmat.upenn.edu/biobank/9bf9ca30cf4611505dd489a4288be250d2b89fa522f5d002589290addc4e2ddb> <http://transformunify.org/ontologies/TURBO_0006510> "F" .
                <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://transformunify.org/ontologies/TURBO_0006510> "04/May/1969" .
                <http://www.itmat.upenn.edu/biobank/9bf2513f5f84359779c474b09b2ec3e983b7a0ba4e3449a26ad18d55a4c4255b> <http://transformunify.org/ontologies/TURBO_0006511> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://www.itmat.upenn.edu/biobank/d23422d82901be7aecf9252d8121b4b96958d4edad381af1f98662be616c9d7b> <http://transformunify.org/ontologies/TURBO_0006512> "asian" .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/2c1e667e85a0404bccb6d90feac4e23503413ffaa64813361dff86ead3573af9> .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://transformunify.org/ontologies/TURBO_0010082> <http://transformunify.org/ontologies/TURBO_0000410> .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010089> <http://purl.obolibrary.org/obo/OMRSE_00000138> .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010090> <http://purl.obolibrary.org/obo/OMRSE_00000181> .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://transformunify.org/ontologies/TURBO_0010079> "4" .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://transformunify.org/ontologies/TURBO_0010084> "part_expand" .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010085> "04/May/1969" .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010086> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010098> "F" .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010100> "asian" .
              }
            }
        """
      update.updateSparql(cxn, sparqlPrefixes + insert)
      println(DrivetrainProcessFromGraphModel.runProcess(cxn, gmCxn, "http://transformunify.org/ontologies/healthcareEncounterLinkingProcess"))
      
        val check: String = """
          ASK
          {
          graph pmbb:expanded {
              ?homoSapiens obo:RO_0000056 ?healthcareEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?healthcareEncounter .
          		?healthcareEncounterCrid turbo:TURBO_0000302 ?homoSapiensCrid .
          		?homoSapiensCrid turbo:TURBO_0000302 ?healthcareEncounterCrid .
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

          		?heightDatum a obo:IAO_0000408 .
          		?weightDatum a obo:IAO_0000414 .
          		
          		?homoSapiens obo:BFO_0000051 ?adipose .
              ?adipose obo:BFO_0000050 ?homoSapiens .
              ?adipose a obo:UBERON_0001013 .
              ?BMI obo:IAO_0000136 ?adipose .
              ?BMI a efo:EFO_0004340 .
              ?BMI obo:IAO_0000581 ?encounterDate .
              ?encounterStart a turbo:TURBO_0000511 .
          		?encounterStart obo:RO_0002223 ?healthcareEncounter .          
          		?encounterDate a turbo:TURBO_0000512 .
          		?encounterDate obo:IAO_0000136 ?encounterStart .
          }}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + check).get should be (true)
    }
    
    test("healthcare encounter entity linking - minimum fields")
    {
              // these triples were generated from the output of the second healthcare encounter expansion test and the second homo sapiens expansion unit test on 4/9/19
      val insert = """
            INSERT DATA
            {
            Graph pmbb:expanded {
                # healthcare encounter triples start here
                <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_OGMS_0000097> .
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
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000643> "enc_expand.csv" .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0000648> "20" .
                <http://www.itmat.upenn.edu/biobank/adee56d0206c36f67682eaff401093c5cf1f91259f9339fd273b902a0393ac11> <http://transformunify.org/ontologies/TURBO_0006510> "20" .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/20b777012bab4374cbb3649f419024ae0c672e888b4346f19c11fac58611b1af> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> <http://transformunify.org/ontologies/TURBO_0010110> <http://transformunify.org/ontologies/TURBO_0000440> .
                <http://www.itmat.upenn.edu/biobank/hcenc1> turbo:TURBO_0010002 <http://www.itmat.upenn.edu/biobank/part1> .

                
                # homo sapiens triples start here
                <http://www.itmat.upenn.edu/biobank/test_instantiation_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000522> .
                <http://www.itmat.upenn.edu/biobank/part1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_obo_NCBITaxon_9606> .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/shortcut_turbo_TURBO_0000503> .
                <http://www.itmat.upenn.edu/biobank/c23be6c01fdd2f8733635beef06207397ffe895b57663b4005610e0b42428625> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
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
                <http://www.itmat.upenn.edu/biobank/d991df175ba7e344e1590ccc389439f68beca50d4da128241865eba371813568> <http://transformunify.org/ontologies/TURBO_0006510> "4" .
                <http://www.itmat.upenn.edu/biobank/part1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/52ae0822b37763ef4964e36a337909f88cd334b34c3cebfc4c45d39ecf5851e5> .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://transformunify.org/ontologies/TURBO_0010082> <http://transformunify.org/ontologies/TURBO_0000410> .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://transformunify.org/ontologies/TURBO_0010079> "4" .
                <http://www.itmat.upenn.edu/biobank/crid1> <http://transformunify.org/ontologies/TURBO_0010084> "part_expand" .

              }
            }
        """
      update.updateSparql(cxn, sparqlPrefixes + insert)
      println(DrivetrainProcessFromGraphModel.runProcess(cxn, gmCxn, "http://transformunify.org/ontologies/healthcareEncounterLinkingProcess"))
        
        val check: String = """
          ASK
          {
              ?homoSapiens obo:RO_0000056 ?healthcareEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?healthcareEncounter .
          		?healthcareEncounterCrid turbo:TURBO_0000302 ?homoSapiensCrid .
          		?homoSapiensCrid turbo:TURBO_0000302 ?healthcareEncounterCrid .
          		
          		?homoSapiens a obo:NCBITaxon_9606 .
          		?homoSapiensCrid obo:IAO_0000219 ?homoSapiens .
          		?homoSapiensCrid a turbo:TURBO_0000503 .
          		
          		?healthcareEncounter a obo:OGMS_0000097 .
          		?healthcareEncounterCrid obo:IAO_0000219 ?healthcareEncounter .
          		?healthcareEncounterCrid a turbo:TURBO_0000508 .
          		
          }
          """
        
        val noHeightWeightAdiposeBmiOrDate: String = """
              ASK {
              values ?notexists {
                obo:PATO_0000119 
                obo:PATO_0000128 
                obo:OBI_0000445 
                turbo:TURBO_0001511 
                obo:IAO_0000408 
                obo:IAO_0000414
                obo:UBERON_0001013
                efo:EFO_0004340
                turbo:TURBO_0000511
                turbo:TURBO_0000512
                }
              ?s a ?notexists . }
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + check).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + noHeightWeightAdiposeBmiOrDate).get should be (false)
    }
}