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
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = false
    val entLink = new EntityLinker
    val ooe = new ObjectOrientedExpander
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
    
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
   
    test("biobank encounter expansion with entity linking - all fields")
    {
        
    }
    
    test("biobank encounter expansion with entity linking - minimum fields")
    {
        
    }
    
    test ("healthcare encounter expansion with entity linking - all fields")
    {
        val query = """INSERT DATA { 
          GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a obo:OGMS_0000097 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0000650 "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010002 "http://transformunify.org/ontologies/UPHS"^^<http://www.w3.org/2001/XMLSchema#anyURI> ;
          turbo:TURBO_0010000 "4" ;
          turbo:ScHcEnc2UnexpandedConsenter "http://transformunify.org/ontologies/UPHS/4" ;
          turbo:TURBO_0000646 "12" ;
          turbo:TURBO_0000647 "13" ;
          turbo:TURBO_0000655 "14" ;
          turbo:TURBO_0000644 "01/12/1993" ;
          turbo:TURBO_0000645 "01/12/1993"^^xsd:Date .
          }
          GRAPH pmbb:Shortcuts_consenterShortcuts {
          <http://transformunify.org/ontologies/UPHS/4> a turbo:TURBO_0000502 .
          pmbb:crid1 obo:IAO_0000219 <http://transformunify.org/ontologies/UPHS/4> ;
          a turbo:TURBO_0000503 ;
          turbo:TURBO_0003603 "part_expand" ;
          turbo:TURBO_0003608 "4" ;
          turbo:TURBO_0003610 "http://transformunify.org/ontologies/UPHS"^^xsd:anyURI .
          }}"""
        
        update.updateSparql(cxn, sparqlPrefixes + query)
        ooe.runAllExpansionProcesses(cxn, randomUUID, "http://www.itmat.upenn.edu/biobank/test_instantiation_1")
        
        val check: String = """
          ASK
          {
              ?consenter obo:RO_0000056 ?healthcareEncounter .
              ?consenter obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?healthcareEncounter .
          		?healthcareEncounterCrid turbo:TURBO_0000302 ?consenterCrid .
          		?weightDatum obo:IAO_0000136 ?consenter.
          		?heightDatum obo:IAO_0000136 ?consenter.
          		?weightDatum obo:IAO_0000221 ?consenterWeight .
          		?heightDatum obo:IAO_0000221 ?consenterHeight .
          		?weightAssay obo:OBI_0000293 ?consenter.
          		?weightAssay obo:OBI_0000293 ?consenter.
          		
          		?consenter a turbo:TURBO_0000502 .
          		?consenter obo:RO_0000086 ?consenterWeight .
          		?consenterWeight a obo:PATO_0000128 .
          		?consenter obo:RO_0000086 ?consenterHeight .
          		?consenterHeight a obo:PATO_0000119 .
          		?consenterCrid obo:IAO_0000219 ?consenter .
          		?consenterCrid a turbo:TURBO_0000503 .
          		
          		?healthcareEncounter a obo:OGMS_0000097 .
          		?healthcareEncounterCrid obo:IAO_0000219 ?healthcareEncounter .
          		?healthcareEncounterCrid a turbo:TURBO_0000508 .
          		?weightAssay obo:BFO_0000050 ?encounter .
          		?weightAssay a obo:OBI_0000445 .
          		?heightAssay obo:BFO_0000050 ?encounter .
          		?heightAssay a turbo:TURBO_0001511 .
          		?heightDatum a obo:IAO_0000408 .
          		?heightAssay obo:OBI_0000299 ?heightDatum .
          		?weightDatum a obo:IAO_0000414 .
          		?weightAssay obo:OBI_0000299 ?weightDatum .
          		
          		?consenter obo:BFO_0000051 ?adipose .
              ?adipose obo:BFO_0000050 ?consenter .
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
    
    /*test("healthcare encounter expansion with entity linking - minimum fields")
    {
      
    }*/
}