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
    
    val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearDatabaseAfterRun)
    }
   
    test("biobank encounter expansion with entity linking - all fields")
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
    }
    
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
    }
    
    test ("healthcare encounter expansion with entity linking - all fields")
    {
        val query = """INSERT DATA { 
          GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0010110 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010002 "http://transformunify.org/ontologies/UPHS"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010000 "4" ;
          turbo:ScHcEnc2UnexpandedHomoSapiens "http://transformunify.org/ontologies/UPHS/4" ;
          turbo:TURBO_0000646 "12" ;
          turbo:TURBO_0000647 "13" ;
          turbo:TURBO_0000655 "14" ;
          turbo:TURBO_0000644 "01/12/1993" ;
          turbo:TURBO_0000645 "01/12/1993"^^xsd:Date .
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
              ?homoSapiens obo:RO_0000056 ?healthcareEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?healthcareEncounter .
          		?healthcareEncounterCrid turbo:TURBO_0000302 ?homoSapiensCrid .
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
          		
          		?healthcareEncounter a obo:OGMS_0000097 .
          		?healthcareEncounterCrid obo:IAO_0000219 ?healthcareEncounter .
          		?healthcareEncounterCrid a turbo:TURBO_0000508 .
          		?weightAssay obo:BFO_0000050 ?healthcareEncounter .
          		?weightAssay a obo:OBI_0000445 .
          		?heightAssay obo:BFO_0000050 ?healthcareEncounter .
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
              ?encounterStart a turbo:TURBO_0000511 .
          		?encounterStart obo:RO_0002223 ?healthcareEncounter .          
          		?encounterDate a turbo:TURBO_0000512 .
          		?encounterDate obo:IAO_0000136 ?encounterStart .
          }
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + check).get should be (true)
    }
    
    test("healthcare encounter expansion with entity linking - minimum fields")
    {
        val query = """INSERT DATA { 
          GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0010110 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010002 "http://transformunify.org/ontologies/UPHS"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010000 "4" ;
          turbo:ScHcEnc2UnexpandedHomoSapiens "http://transformunify.org/ontologies/UPHS/4" .
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
              ?homoSapiens obo:RO_0000056 ?healthcareEncounter .
              ?homoSapiens obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?healthcareEncounter .
          		?healthcareEncounterCrid turbo:TURBO_0000302 ?homoSapiensCrid .
          		
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
}