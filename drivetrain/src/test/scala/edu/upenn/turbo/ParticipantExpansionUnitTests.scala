package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

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
    
    val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
    
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
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("participant with all fields")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
              turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000604 "04/May/1969" ;
              turbo:TURBO_0000606 "F" ;
              turbo:TURBO_0000609 'inpatient' ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI ;
              
              # adding race data 7/31/18
              turbo:TURBO_0000614 'http://purl.obolibrary.org/obo/OMRSE_00000181'^^xsd:anyURI ;
              turbo:TURBO_0000615 'asian' .
          }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
        
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
        		
        		?rip a obo:OMRSE_00000099 .
        		?rip obo:OBI_0000299 ?rid .
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0006512 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
          }}
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + extraFields).get should be (true)
        
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
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
            "http://transformunify.org/ontologies/TURBO_0006510", "http://transformunify.org/ontologies/TURBO_0006511",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://transformunify.org/ontologies/TURBO_0006512",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (51)
    }
    
    test("participant with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")        
        
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
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (34) 
    }
    
    test("participant without psc")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "part_expand" ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("participant without dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("participant without registry")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "4" ;
              turbo:TURBO_0000603 "part_expand" .
          }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (false)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "s")
        result.size should be (0)
    }
    
    test("participant with text but not xsd values")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
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
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.participantExpansion(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
        
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
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + dateNoXsd).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + gidNoXsd).get should be (true)
        
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        
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
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (43)
    }
    
    test("expand consenter with multiple identifiers - single dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_participantShortcuts {
            pmbb:part1
            turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
            turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
            a turbo:TURBO_0000502 ;
            turbo:TURBO_0000604 "04/May/1969" ;
            turbo:TURBO_0000606 "F" ;
            turbo:TURBO_0000614 'http://purl.obolibrary.org/obo/OMRSE_00000181'^^xsd:anyURI ;
            turbo:TURBO_0000615 'asian' .
            
            pmbb:shortcutCrid1 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid1 a turbo:TURBO_0000503 .
            pmbb:shortcutCrid2 a turbo:TURBO_0000503 .
            pmbb:shortcutCrid3 a turbo:TURBO_0000503 .
            
            pmbb:shortcutCrid1 turbo:TURBO_0003603 'dataset1' .
            pmbb:shortcutCrid2 turbo:TURBO_0003603 'dataset1' .
            pmbb:shortcutCrid3 turbo:TURBO_0003603 'dataset1' .
            
            pmbb:shortcutCrid1 turbo:TURBO_0003608 'jerry' .
            pmbb:shortcutCrid2 turbo:TURBO_0003608 'kramer' .
            pmbb:shortcutCrid3 turbo:TURBO_0003608 'elaine' .
            
            pmbb:shortcutCrid1 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000402"^^xsd:anyURI .
            pmbb:shortcutCrid2 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000403"^^xsd:anyURI .
            pmbb:shortcutCrid3 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000410"^^xsd:anyURI .

          }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.expandParticipantsMultipleIdentifiers(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
    
        val output: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        	
        		?part a :TURBO_0000502 .
        		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "dataset1" .
        		?consenter turbo:TURBO_0006601 ?previousUriText .
        		
        		?part :TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .
        		?part obo:BFO_0000051 ?adipose .
        		?adipose obo:BFO_0000050 ?part .
        		?adipose a obo:UBERON_0001013 .
        		?part obo:RO_0000086 ?weight .
        		?weight a obo:PATO_0000119 .
        		?part obo:RO_0000086 ?height .
        		?height a obo:PATO_0000128 .

        		?gid :TURBO_0006510 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob :TURBO_0006510 "04/May/1969" .
        		?dob :TURBO_0006511 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .
        		
        		?rip a obo:OMRSE_00000099 .
        		?rip obo:OBI_0000299 ?rid .
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0006512 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0000503 .
        		?patientCrid1 obo:BFO_0000051 ?patientRegDen1 .
        		?patientRegDen1 obo:BFO_0000050 ?patientCrid1 .
        		?patientRegDen1 a turbo:TURBO_0000505 .
        		?patientRegDen1 obo:IAO_0000219 turbo:TURBO_0000402 .
        		turbo:TURBO_0000402 a turbo:TURBO_0000506 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0006510 "jerry"^^xsd:string .
            ?patientRegDen1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen1 .
            ?partSymb1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0000503 .
        		?patientCrid2 obo:BFO_0000051 ?patientRegDen2 .
        		?patientRegDen2 obo:BFO_0000050 ?patientCrid2 .
        		?patientRegDen2 a turbo:TURBO_0000505 .
        		?patientRegDen2 obo:IAO_0000219 turbo:TURBO_0000403 .
        		turbo:TURBO_0000403 a turbo:TURBO_0000506 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0006510 "kramer"^^xsd:string .
            ?patientRegDen2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen2 .
            ?partSymb2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0000503 .
        		?patientCrid3 obo:BFO_0000051 ?patientRegDen3 .
        		?patientRegDen3 obo:BFO_0000050 ?patientCrid3 .
        		?patientRegDen3 a turbo:TURBO_0000505 .
        		?patientRegDen3 obo:IAO_0000219 turbo:TURBO_0000410 .
        		turbo:TURBO_0000410 a turbo:TURBO_0000506 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0006510 "elaine"^^xsd:string .
            ?patientRegDen3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen3 .
            ?partSymb3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + output).get should be (true)
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        result.size should be (80)
    }
    
    test("expand consenter with multiple identifiers - multiple datasets")
    {
        val insert: String = """
          INSERT DATA {
          GRAPH pmbb:Shortcuts_participantShortcuts1 {
            pmbb:part1
            turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
            a turbo:TURBO_0000502 ;
            turbo:TURBO_0000606 "F" .
            pmbb:shortcutCrid1 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid1 a turbo:TURBO_0000503 .
            pmbb:shortcutCrid1 turbo:TURBO_0003603 'dataset1' .
            pmbb:shortcutCrid1 turbo:TURBO_0003608 'jerry' .
            pmbb:shortcutCrid1 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000402"^^xsd:anyURI .  
          }
          
          GRAPH pmbb:Shortcuts_participantShortcuts2 {
            pmbb:part1 a turbo:TURBO_0000502 ;
            turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
            turbo:TURBO_0000604 "04/May/1969" .
            pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid2 a turbo:TURBO_0000503 .
            pmbb:shortcutCrid2 turbo:TURBO_0003603 'dataset2' .
            pmbb:shortcutCrid2 turbo:TURBO_0003608 'kramer' .
            pmbb:shortcutCrid2 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000403"^^xsd:anyURI .
          }
          
          GRAPH pmbb:Shortcuts_participantShortcuts3 {
            pmbb:part1 a turbo:TURBO_0000502 ;
            turbo:TURBO_0000614 'http://purl.obolibrary.org/obo/OMRSE_00000181'^^xsd:anyURI ;
            turbo:TURBO_0000615 'asian' .
            pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid3 a turbo:TURBO_0000503 .
            pmbb:shortcutCrid3 turbo:TURBO_0003603 'dataset3' .
            pmbb:shortcutCrid3 turbo:TURBO_0003608 'elaine' .
            pmbb:shortcutCrid3 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000410"^^xsd:anyURI .
          }
          
          }"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.expandParticipantsMultipleIdentifiers(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), 
            "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts1><http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts2><http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts3>",
            UUID.randomUUID().toString.replaceAll("-", ""), randomUUID)
    
          val output: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        	
        		?part a :TURBO_0000502 .
        		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset1 .
        		?dataset1 a obo:IAO_0000100 .
        		?dataset1 dc11:title "dataset1" .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset2 .
        		?dataset2 a obo:IAO_0000100 .
        		?dataset2 dc11:title "dataset2" .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset3 .
        		?dataset3 a obo:IAO_0000100 .
        		?dataset3 dc11:title "dataset3" .
        		?consenter turbo:TURBO_0006601 ?previousUriText .
        		
        		?part :TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .
        		?part obo:BFO_0000051 ?adipose .
        		?adipose obo:BFO_0000050 ?part .
        		?adipose a obo:UBERON_0001013 .
        		?part obo:RO_0000086 ?weight .
        		?weight a obo:PATO_0000119 .
        		?part obo:RO_0000086 ?height .
        		?height a obo:PATO_0000128 .

        		?gid :TURBO_0006510 "F" .
        		?gid obo:BFO_0000050 ?dataset1 .
        		?dataset1 obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob :TURBO_0006510 "04/May/1969" .
        		?dob :TURBO_0006511 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset2 .
        		?dataset2 obo:BFO_0000051 ?dob .
        		
        		?rip a obo:OMRSE_00000099 .
        		?rip obo:OBI_0000299 ?rid .
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0006512 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset3 .
        		?dataset3 obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0000503 .
        		?patientCrid1 obo:BFO_0000051 ?patientRegDen1 .
        		?patientRegDen1 obo:BFO_0000050 ?patientCrid1 .
        		?patientRegDen1 a turbo:TURBO_0000505 .
        		?patientRegDen1 obo:IAO_0000219 turbo:TURBO_0000402 .
        		turbo:TURBO_0000402 a turbo:TURBO_0000506 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0006510 "jerry"^^xsd:string .
            ?patientRegDen1 obo:BFO_0000050 ?dataset1 .
            ?dataset1 obo:BFO_0000051 ?patientRegDen1 .
            ?partSymb1 obo:BFO_0000050 ?dataset1 .
            ?dataset1 obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0000503 .
        		?patientCrid2 obo:BFO_0000051 ?patientRegDen2 .
        		?patientRegDen2 obo:BFO_0000050 ?patientCrid2 .
        		?patientRegDen2 a turbo:TURBO_0000505 .
        		?patientRegDen2 obo:IAO_0000219 turbo:TURBO_0000403 .
        		turbo:TURBO_0000403 a turbo:TURBO_0000506 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0006510 "kramer"^^xsd:string .
            ?patientRegDen2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?patientRegDen2 .
            ?partSymb2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0000503 .
        		?patientCrid3 obo:BFO_0000051 ?patientRegDen3 .
        		?patientRegDen3 obo:BFO_0000050 ?patientCrid3 .
        		?patientRegDen3 a turbo:TURBO_0000505 .
        		?patientRegDen3 obo:IAO_0000219 turbo:TURBO_0000410 .
        		turbo:TURBO_0000410 a turbo:TURBO_0000506 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0006510 "elaine"^^xsd:string .
            ?patientRegDen3 obo:BFO_0000050 ?dataset3 .
            ?dataset3 obo:BFO_0000051 ?patientRegDen3 .
            ?partSymb3 obo:BFO_0000050 ?dataset3 .
            ?dataset3 obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + output).get should be (true)
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        result.size should be (86)
    }
    
    test("combining deprecated and multi-ID consenter shortcuts - single dataset")
    {
        val insert: String = """
          INSERT DATA {
          GRAPH pmbb:Shortcuts_participantShortcuts
          {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "dataset1" ;
              turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
              turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "jerry" ;
              turbo:TURBO_0000604 "04/May/1969" ;
              turbo:TURBO_0000606 "F" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/TURBO_0000402"^^xsd:anyURI ;
              
              # adding race data 7/31/18
              turbo:TURBO_0000614 'http://purl.obolibrary.org/obo/OMRSE_00000181'^^xsd:anyURI ;
              turbo:TURBO_0000615 'asian' .
              
              pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
              pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
              pmbb:shortcutCrid2 a turbo:TURBO_0000503 .
              pmbb:shortcutCrid3 a turbo:TURBO_0000503 .
              
              pmbb:shortcutCrid2 turbo:TURBO_0003603 'dataset1' .
              pmbb:shortcutCrid3 turbo:TURBO_0003603 'dataset1' .
              
              pmbb:shortcutCrid2 turbo:TURBO_0003608 'kramer' .
              pmbb:shortcutCrid3 turbo:TURBO_0003608 'elaine' .
              
              pmbb:shortcutCrid2 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000403"^^xsd:anyURI .
              pmbb:shortcutCrid3 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000410"^^xsd:anyURI .
          }
          }"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.expandAllParticipants(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), 
            "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts>", randomUUID)
            
        val output: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        	
        		?part a :TURBO_0000502 .
        		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "dataset1" .
        		?consenter turbo:TURBO_0006601 ?previousUriText .
        		
        		?part :TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .
        		?part obo:BFO_0000051 ?adipose .
        		?adipose obo:BFO_0000050 ?part .
        		?adipose a obo:UBERON_0001013 .
        		?part obo:RO_0000086 ?weight .
        		?weight a obo:PATO_0000119 .
        		?part obo:RO_0000086 ?height .
        		?height a obo:PATO_0000128 .

        		?gid :TURBO_0006510 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob :TURBO_0006510 "04/May/1969" .
        		?dob :TURBO_0006511 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .
        		
        		?rip a obo:OMRSE_00000099 .
        		?rip obo:OBI_0000299 ?rid .
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0006512 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0000503 .
        		?patientCrid1 obo:BFO_0000051 ?patientRegDen1 .
        		?patientRegDen1 obo:BFO_0000050 ?patientCrid1 .
        		?patientRegDen1 a turbo:TURBO_0000505 .
        		?patientRegDen1 obo:IAO_0000219 turbo:TURBO_0000402 .
        		turbo:TURBO_0000402 a turbo:TURBO_0000506 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0006510 "jerry"^^xsd:string .
            ?patientRegDen1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen1 .
            ?partSymb1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0000503 .
        		?patientCrid2 obo:BFO_0000051 ?patientRegDen2 .
        		?patientRegDen2 obo:BFO_0000050 ?patientCrid2 .
        		?patientRegDen2 a turbo:TURBO_0000505 .
        		?patientRegDen2 obo:IAO_0000219 turbo:TURBO_0000403 .
        		turbo:TURBO_0000403 a turbo:TURBO_0000506 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0006510 "kramer"^^xsd:string .
            ?patientRegDen2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen2 .
            ?partSymb2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0000503 .
        		?patientCrid3 obo:BFO_0000051 ?patientRegDen3 .
        		?patientRegDen3 obo:BFO_0000050 ?patientCrid3 .
        		?patientRegDen3 a turbo:TURBO_0000505 .
        		?patientRegDen3 obo:IAO_0000219 turbo:TURBO_0000410 .
        		turbo:TURBO_0000410 a turbo:TURBO_0000506 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0006510 "elaine"^^xsd:string .
            ?patientRegDen3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen3 .
            ?partSymb3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + output).get should be (true)
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        result.size should be (80)
    }
    
    test("combining deprecated and multi-ID consenter shortcuts - multiple datasets")
    {
        val insert: String = """
          INSERT DATA {
          GRAPH pmbb:Shortcuts_participantShortcuts1 
          {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0000603 "dataset1" ;
              turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
              turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0000502 ;
              turbo:TURBO_0000608 "jerry" ;
              turbo:TURBO_0000604 "04/May/1969" ;
              turbo:TURBO_0000606 "F" ;
              turbo:TURBO_0000610 "http://transformunify.org/ontologies/TURBO_0000402"^^xsd:anyURI ;
              
              # adding race data 7/31/18
              turbo:TURBO_0000614 'http://purl.obolibrary.org/obo/OMRSE_00000181'^^xsd:anyURI ;
              turbo:TURBO_0000615 'asian' .
          }
          GRAPH pmbb:Shortcuts_participantShortcuts2
          {
              <http://www.itmat.upenn.edu/biobank/part1> a turbo:TURBO_0000502 .
              
              pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
              pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
              pmbb:shortcutCrid2 a turbo:TURBO_0000503 .
              pmbb:shortcutCrid3 a turbo:TURBO_0000503 .
              
              pmbb:shortcutCrid2 turbo:TURBO_0003603 'dataset2' .
              pmbb:shortcutCrid3 turbo:TURBO_0003603 'dataset2' .
              
              pmbb:shortcutCrid2 turbo:TURBO_0003608 'kramer' .
              pmbb:shortcutCrid3 turbo:TURBO_0003608 'elaine' .
              
              pmbb:shortcutCrid2 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000403"^^xsd:anyURI .
              pmbb:shortcutCrid3 turbo:TURBO_0003610 "http://transformunify.org/ontologies/TURBO_0000410"^^xsd:anyURI .
          }
          }"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        expand.expandAllParticipants(cxn, 
            cxn.getValueFactory.createIRI("http://www.itmat.upenn.edu/biobank/test_instantiation_1"), 
            "<http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts1><http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts2>", randomUUID)
    
        val output: String = """
          ASK {GRAPH pmbb:postExpansionCheck {
        	
        		?part a :TURBO_0000502 .
        		pmbb:test_instantiation_1 a turbo:TURBO_0000522 .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset1 .
        		?dataset1 a obo:IAO_0000100 .
        		?dataset1 dc11:title "dataset1" .
        		pmbb:test_instantiation_1 obo:OBI_0000293 ?dataset2 .
        		?dataset2 a obo:IAO_0000100 .
        		?dataset2 dc11:title "dataset2" .
        		?consenter turbo:TURBO_0006601 ?previousUriText .
        		
        		?part :TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .
        		?part obo:BFO_0000051 ?adipose .
        		?adipose obo:BFO_0000050 ?part .
        		?adipose a obo:UBERON_0001013 .
        		?part obo:RO_0000086 ?weight .
        		?weight a obo:PATO_0000119 .
        		?part obo:RO_0000086 ?height .
        		?height a obo:PATO_0000128 .

        		?gid :TURBO_0006510 "F" .
        		?gid obo:BFO_0000050 ?dataset1 .
        		?dataset1 obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob :TURBO_0006510 "04/May/1969" .
        		?dob :TURBO_0006511 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset1 .
        		?dataset1 obo:BFO_0000051 ?dob .
        		
        		?rip a obo:OMRSE_00000099 .
        		?rip obo:OBI_0000299 ?rid .
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0006512 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset1 .
        		?dataset1 obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0000503 .
        		?patientCrid1 obo:BFO_0000051 ?patientRegDen1 .
        		?patientRegDen1 obo:BFO_0000050 ?patientCrid1 .
        		?patientRegDen1 a turbo:TURBO_0000505 .
        		?patientRegDen1 obo:IAO_0000219 turbo:TURBO_0000402 .
        		turbo:TURBO_0000402 a turbo:TURBO_0000506 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0006510 "jerry"^^xsd:string .
            ?patientRegDen1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?patientRegDen1 .
            ?partSymb1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0000503 .
        		?patientCrid2 obo:BFO_0000051 ?patientRegDen2 .
        		?patientRegDen2 obo:BFO_0000050 ?patientCrid2 .
        		?patientRegDen2 a turbo:TURBO_0000505 .
        		?patientRegDen2 obo:IAO_0000219 turbo:TURBO_0000403 .
        		turbo:TURBO_0000403 a turbo:TURBO_0000506 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0006510 "kramer"^^xsd:string .
            ?patientRegDen2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?patientRegDen2 .
            ?partSymb2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0000503 .
        		?patientCrid3 obo:BFO_0000051 ?patientRegDen3 .
        		?patientRegDen3 obo:BFO_0000050 ?patientCrid3 .
        		?patientRegDen3 a turbo:TURBO_0000505 .
        		?patientRegDen3 obo:IAO_0000219 turbo:TURBO_0000410 .
        		turbo:TURBO_0000410 a turbo:TURBO_0000506 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0006510 "elaine"^^xsd:string .
            ?patientRegDen3 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?patientRegDen3 .
            ?partSymb3 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + output).get should be (true)
        val count: String = "SELECT * WHERE {GRAPH pmbb:postExpansionCheck {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, "p")
        result.size should be (83)
    }
}