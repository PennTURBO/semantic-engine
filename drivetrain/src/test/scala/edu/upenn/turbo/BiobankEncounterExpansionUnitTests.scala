package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID

class BiobankEncounterExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val clearDatabaseAfterRun: Boolean = true
    val objectOrientedExpander = new ObjectOrientedExpander
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
    
    val instantiationAndDataset: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
            pmbb:test_instantiation_1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "enc_expand.csv"^^xsd:string .
       }}"""

    val biobankEncounterMinimum: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
            ?encounter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000527 .
        		?encounter turbo:TURBO_0006601 "http://www.itmat.upenn.edu/biobank/bbenc1" .
        		?encCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000533 .
        		?encCrid obo:IAO_0000219 ?encounter .
        		?encCrid obo:BFO_0000051 ?encsymb .
        		?encCrid obo:BFO_0000051 ?encregden .
        		?encsymb a turbo:TURBO_0000534 . 
        		?encsymb obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?encsymb .
        		?encsymb turbo:TURBO_0006510 'B' .
        		?encregden a turbo:TURBO_0000535 .
        		# ?encregden turbo:TURBO_0006510 'biobank' .
        		?encregden obo:IAO_0000219 <http://transformunify.org/hcEncReg/biobank> .
        		<http://transformunify.org/hcEncReg/biobank> a turbo:TURBO_0000543 .
        		?dataset <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:IAO_0000100 .
       }}
      """
    
    val biobankHeightWeightAndBMI: String = """
          ask {
          GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
        		
        		?BMI obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?weightDatum .
        		?dataset obo:BFO_0000051 ?heightDatum .
        		?dataset a obo:IAO_0000100 .
        		
            ?encounter a turbo:TURBO_0000527 .
        		?encounter obo:OBI_0000299 ?BMI .
        		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
        		?BMI obo:OBI_0001938 ?BMIvalspec .
        		?BMIvalspec a obo:OBI_0001933 .
        		?BMIvalspec obo:OBI_0002135 "18.8252626423"^^xsd:float .
        		?BMI obo:IAO_0000581 ?EncDate1 .
        		?BMI obo:BFO_0000050 ?dataset .
        		?heightValSpec rdf:type obo:OBI_0001931 ;
        		               obo:IAO_0000039 obo:UO_0000015 ;
        		               obo:OBI_0002135 "180.34"^^xsd:float  .
      	    ?heightAssay rdf:type turbo:TURBO_0001511 ;
      	                 obo:BFO_0000050 ?encounter ;
      	                 obo:OBI_0000299 ?heightDatum  .
      	    ?heightDatum rdf:type obo:IAO_0000408 ;
      	                 obo:OBI_0001938 ?heightValSpec .
      	    ?weightAssay rdf:type obo:OBI_0000445 ;
      	                 obo:BFO_0000050 ?encounter ;
      	                 obo:OBI_0000299 ?weightDatum  .
      	    ?weightDatum rdf:type obo:IAO_0000414 ;
      	                 obo:OBI_0001938 ?weightValSpec ;
      	                 obo:BFO_0000050 ?dataset .
      	    ?weightValSpec rdf:type obo:OBI_0001931 ;
      	                   obo:IAO_0000039 obo:UO_0000009 ;
      	                   obo:OBI_0002135 "61.2244897959"^^xsd:float .
        	}}
          """
    
    val biobankEncounterDate: String = """
          ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
            ?encDate obo:BFO_0000050 ?dataset .
        		?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000532 .
        		?encDate turbo:TURBO_0006512 "15/Jan/2017" .
        		?encDate turbo:TURBO_0006511 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
        		?encStart obo:RO_0002223 ?encounter .
        		?encounter a turbo:TURBO_0000527 .
        		?dataset a obo:IAO_0000100 .
          }}
      """
     
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
  
    test("bb encounter with all fields")
    {
        logger.info("starting biobank encounter expanison unit tests")
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000635 "18.8252626423"^^xsd:float ;
          turbo:TURBO_0000624 "15/Jan/2017" ;
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000627 "61.2244897959"^^xsd:float ;
          turbo:TURBO_0000626 "180.34"^^xsd:float ;
          turbo:TURBO_0000625 "2017-01-15"^^xsd:date ;
          turbo:TURBO_0000629 "biobank" ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterDate).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006601", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/OBI_0002135", 
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006512",
            "http://transformunify.org/ontologies/TURBO_0006511", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://purl.obolibrary.org/obo/IAO_0000581", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (59)
    }
    
    test("bb encounter with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006601", "http://purl.obolibrary.org/obo/IAO_0000219", 
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://transformunify.org/ontologies/TURBO_0006510", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (21)
    }
   
    test("bb encounter without registry")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000623 "enc_expand.csv" ;
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000628 "B" .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterMinimum).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("bb encounter without ID")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000623 "enc_expand.csv" ;
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI ;
          turbo:TURBO_0000629 "biobank" .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterMinimum).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("bb encounter without dataset")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000628 "B" ;
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI ;
          turbo:TURBO_0000629 "biobank" .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
            
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterMinimum).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankHeightWeightAndBMI).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterDate).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("bb encounter with text but not xsd values")
    {
        val insert: String = """
          INSERT DATA { GRAPH pmbb:Shortcuts_biobankEncounterShortcuts {
          pmbb:bbenc1
          turbo:TURBO_0000635 "18.8252626423"^^xsd:float ;
          turbo:TURBO_0000624 "15/Jan/2017" ;
          a turbo:TURBO_0000527 ;
          turbo:TURBO_0000628 "B" ;
          turbo:TURBO_0000623 "enc_expand.csv" ;
          turbo:TURBO_0000627 "61.2244897959"^^xsd:float ;
          turbo:TURBO_0000626 "180.34"^^xsd:float ;
          turbo:TURBO_0000629 "biobank" ;
          turbo:TURBO_0000630 "http://transformunify.org/hcEncReg/biobank"^^xsd:anyURI .
          # turbo:TURBO_0000625 "2017-01-15"^^xsd:date .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        objectOrientedExpander.runAllExpansionProcesses(cxn, gmCxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        val dateNoXsd: String = """
          ask {
          GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
        		?encounter a turbo:TURBO_0000527 .
        		?dataset a obo:IAO_0000100 .
        		?encDate a turbo:TURBO_0000532 .
        		?encDate turbo:TURBO_0006512 "15/Jan/2017" .
        		# ?encDate turbo:TURBO_0006511 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
        		?encStart obo:RO_0002223 ?Encounter1 .
        	}}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterMinimum).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankHeightWeightAndBMI).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + biobankEncounterDate).get should be (false) 
        update.querySparqlBoolean(cxn, sparqlPrefixes + dateNoXsd).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006601", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/OBI_0002135", 
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006512",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://purl.obolibrary.org/obo/IAO_0000581", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://purl.obolibrary.org/obo/OBI_0002135",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (58)
    }
}