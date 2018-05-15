package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class ParticipantExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val expand = new Expander
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    val instantiationAndDataset: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
          
        pmbb:test_instantiation_1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
    		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
    		?dataset a obo:IAO_0000100 .
    		?dataset dc11:title "part_expand" .
       }}"""
    
    val minimumPartRequirements: String = """
      ASK { GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
          
          ?part turbo:TURBO_0006601 "http://www.itmat.upenn.edu/biobank/part1" .
          
          ?part a turbo:TURBO_0000502 .
          ?part obo:RO_0000086 ?biosex .
          ?biosex a obo:PATO_0000047 .
          ?part turbo:TURBO_0000303 ?birth .
          ?birth a obo:UBERON_0035946 .
          ?part obo:RO_0000086 ?height .
          ?height a obo:PATO_0000119 .
          ?part obo:RO_0000086 ?weight .
          ?weight a obo:PATO_0000128 .
          ?part obo:BFO_0000051 ?adipose .
          ?adipose a obo:UBERON_0001013 .
          
          ?dob a efo:EFO_0004950 .
          # ?dob obo:BFO_0000050 ?dataset .
          ?dob obo:IAO_0000136 ?birth .
          
          ?partCrid a turbo:TURBO_0000503 .
          ?partCrid obo:IAO_0000219 ?part .
          ?partCrid obo:BFO_0000051 ?partSymbol .
          ?partCrid obo:BFO_0000051 ?partRegDen .
          ?partSymbol a turbo:TURBO_0000504 .
          ?partSymbol turbo:TURBO_0006510 "4" .
          ?partRegDen a turbo:TURBO_0000505 .
          ?partRegDen obo:IAO_0000219 turbo:UPHS .
          turbo:UPHS a turbo:TURBO_0000506 .
          
          ?partSymbol obo:BFO_0000050 ?dataset .
          ?partRegDen obo:BFO_0000050 ?dataset .
          ?dataset a obo:IAO_0000100 .
          
       }}"""
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
    }
    after
    {
        connect.closeConnectionDeleteTriples(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("participant with all fields")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
              turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000604 "04/May/1969" ;
              turbo:TURBO_0000606 "F" ;
              turbo:TURBO_0000609 'inpatient' ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"))
        
        val extraFields: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        		
        		?dataset a obo:IAO_0000100 .
        		?part rdf:type :TURBO_0000502 .

        		?gid :TURBO_0006510 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?gid rdf:type obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?part :TURBO_0000303 ?birth .
        		?birth rdf:type obo:UBERON_0035946 .
        		?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob :TURBO_0006510 "04/May/1969" .
        		?dob :TURBO_0006511 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		
        		?patientCrid obo:IAO_0000219 ?part .
        		?patientCrid a turbo:TURBO_0000503 .
        		?patientCrid obo:BFO_0000051 ?patientRegDen .
        		?patientRegDen a turbo:TURBO_0000505 .
        		?patientRegDen turbo:TURBO_0006510 'inpatient' .
          }}
          """
        helper.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + extraFields).get should be (true)
        
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/RO_0000086",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006601", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://transformunify.org/ontologies/TURBO_0006511"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray) should be (true)
        
        result.size should be (44)
    }
    
    test("participant with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"))
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")        
        
        //compare expected predicates to received predicates
        //only checking predicates because many of the subjects/objects in expanded triples are unique UUIDs
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/RO_0000086",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006601", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray) should be (true)   
        
        result.size should be (34) 
    }
    
    test("participant without psc")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"))
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("participant without dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"))
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("participant without registry")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000603 "part_expand" .
          }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"))
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("participant with text but not xsd values")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              # turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
              # turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000604 "04/May/1969" ;
              turbo:TURBO_0000606 "F" ;
              turbo:TURBO_0000609 "inpatient" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"))
        
        val dateNoXsd: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        		?part rdf:type :TURBO_0000502 .
        		?part turbo:TURBO_0000303 ?birth .
        		?birth rdf:type obo:UBERON_0035946 .
        		?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob :TURBO_0006510 "04/May/1969" .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		# ?dob :TURBO_0006511 "1969-05-04"^^xsd:date .
          }}
          """
        
        val gidNoXsd: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        		?part rdf:type :TURBO_0000502 .
        		?gid obo:IAO_0000136 ?part .
        		?gid a obo:OMRSE_00000133 .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset a obo:IAO_0000100 .
          }}"""
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + dateNoXsd).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + gidNoXsd).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/RO_0000086",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006601", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://transformunify.org/ontologies/TURBO_0006510"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray) should be (true)
        
        result.size should be (43)
    }
}