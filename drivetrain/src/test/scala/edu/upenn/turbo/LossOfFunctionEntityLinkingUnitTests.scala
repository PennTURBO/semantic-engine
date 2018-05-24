package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class LossOfFunctionEntityLinkingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val entLink: EntityLinker = new EntityLinker
    
    val dnaToConsenter: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:DNA1 obo:BFO_0000050 pmbb:consenter1 .
                  pmbb:consenter1 obo:BFO_0000051 pmbb:DNA1 .
              }
          }
          """
        val collToConsenter: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:collectionProcess1 obo:OBI_0000293 pmbb:consenter1 .
                  pmbb:consenter1 obo:OBI_0000299 pmbb:collectionProcess1 .
              }
          }
          """
        val bbSymbToLofDataset: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:bbSymb1 obo:BFO_0000050 pmbb:dataset1 .
                  pmbb:dataset1 obo:BFO_0000051 pmbb:bbSymb1 .
              }
          }
          """
        
        val bbEncToColl: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:bbenc1 obo:BFO_0000051 pmbb:collectionProcess1 .
                  pmbb:collectionProcess1 obo:BFO_0000050 pmbb:bbenc1 .
              }
          }
          """
        
        val graphBuilder: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:allele1 graphBuilder:willBeLinkedWith pmbb:bbenc1 .
              }
          }
          """
        
        val shortcutRelation1: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:allele1 turbo:TURBO_0007601 ?someVar .
              }
          }
          """
        
        val shortcutRelation2: String = """
          ASK
          {
              Graph pmbb:expanded
              {
                  pmbb:allele1 turbo:TURBO_0007609 ?someVar .
              }
          }
          """
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  # participant with lit value '1' and reg value 'registry1'
                  
                  pmbb:consenter1 a turbo:TURBO_0000502 .
                  pmbb:consenter1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:consenterCrid1 a turbo:TURBO_0000503 .
                  pmbb:consenterCrid1 obo:IAO_0000219 pmbb:consenter1 .
                  pmbb:consenterCrid1 obo:BFO_0000051 pmbb:consenterRegDen1 .
                  pmbb:consenterCrid1 obo:BFO_0000051 pmbb:consenterSymb1 .
                  pmbb:consenterSymb1 obo:BFO_0000050 pmbb:consenterCrid1 .
                  pmbb:consenterSymb1 turbo:TURBO_0006510 '1' .
                  pmbb:consenterSymb1 rdf:type turbo:TURBO_0000504 .
                  pmbb:consenterRegDen1 obo:BFO_0000050 pmbb:consenterCrid1 .
                  pmbb:consenterRegDen1 turbo:TURBO_0006510 'registry1' .
                  pmbb:consenterRegDen1 rdf:type turbo:TURBO_0000505 .
                  pmbb:consenterRegDen1 obo:IAO_0000219 pmbb:registry1 .
                  pmbb:registry1 rdf:type turbo:TURBO_0000506 .
                  pmbb:consenter1 obo:RO_0000086 pmbb:height1 .
          		    pmbb:consenter1 obo:RO_0000086 pmbb:weight1 .
          		    pmbb:height1 rdf:type obo:PATO_0000119 .
          		    pmbb:weight1 rdf:type obo:PATO_0000128 .
                  
                  # bb enc with lit value '2' and reg value 'registry2' 
                  
                  pmbb:bbenc1 a turbo:TURBO_0000527 .
                  pmbb:bbenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbCrid1 a turbo:TURBO_0000533 .
              		pmbb:bbCrid1 obo:IAO_0000219 pmbb:bbenc1 .
              		pmbb:bbCrid1 obo:BFO_0000051 pmbb:bbSymb1 .
              		pmbb:bbCrid1 obo:BFO_0000051 pmbb:bbRegDen1 .
               		pmbb:bbSymb1 obo:BFO_0000050 pmbb:bbCrid1 .
               		pmbb:bbSymb1 turbo:TURBO_0006510 '2' .
               		pmbb:bbSymb1 a turbo:TURBO_0000534 .
               		pmbb:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
              		pmbb:bbRegDen1 obo:BFO_0000050 pmbb:bbCrid1 .
              		pmbb:bbRegDen1 turbo:TURBO_0006510 'registry2' .
              		pmbb:bbRegDen1 a turbo:TURBO_0000535 .
              		pmbb:bbRegDen1 obo:IAO_0000219 pmbb:registry2 .
              		pmbb:registry2 a turbo:TURBO_0000543 .
              		
              		# some unattached LOF data, omitting connection info for bb encs (this will be added in individual tests)
              		
              		pmbb:allele1 a obo:OBI_0001352 .
              		pmbb:allele1 obo:BFO_0000050 pmbb:dataset1 .
              		pmbb:dataset1 obo:BFO_0000051 pmbb:allele1 .
              		pmbb:dataset1 a obo:IAO_0000100 .
              		pmbb:allele1 obo:IAO_0000136 pmbb:DNA1 .
              		pmbb:DNA1 a obo:CHEBI_16991 .
              		pmbb:DNA1 obo:BFO_0000050 pmbb:specimen1 .
              		pmbb:specimen1 a obo:OBI_0001479 .
              		pmbb:collectionProcess1 obo:OBI_0000299 pmbb:specimen1 .
              		pmbb:collectionProcess1 a obo:OBI_0600005 .
              }
          }
          """
          helper.updateSparql(cxn, sparqlPrefixes + insert)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("connect LOF to an existent consenter and encounter")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:consenter1 obo:RO_0000056 pmbb:bbenc1 .
                  pmbb:allele1 turbo:TURBO_0007601 '2' .
                  pmbb:allele1 turbo:TURBO_0007609 pmbb:registry2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.connectLossOfFunctionToBiobankEncounters(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + dnaToConsenter).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + collToConsenter).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbSymbToLofDataset).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbEncToColl).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + graphBuilder).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation1).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation2).get should be (false)
    }
    
    test("encounter requested is not present")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:allele1 turbo:TURBO_0007601 '5' .
                  pmbb:allele1 turbo:TURBO_0007609 pmbb:registry5 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.connectLossOfFunctionToBiobankEncounters(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + dnaToConsenter).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + collToConsenter).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbSymbToLofDataset).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbEncToColl).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + graphBuilder).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation1).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation2).get should be (true)
    }
    
    test("encounter is present but not attached to a consenter")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:allele1 turbo:TURBO_0007601 '2' .
                  pmbb:allele1 turbo:TURBO_0007609 pmbb:registry2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.connectLossOfFunctionToBiobankEncounters(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + dnaToConsenter).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + collToConsenter).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbSymbToLofDataset).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbEncToColl).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + graphBuilder).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation1).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation2).get should be (false)
    }
    
    test("consenter added later")
    {
        val insLink: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:bbSymb1 obo:BFO_0000050 pmbb:dataset1 .
                  pmbb:dataset1 obo:BFO_0000051 pmbb:bbSymb1 .
                  pmbb:collectionProcess1 obo:BFO_0000050 pmbb:bbenc1 .
                  pmbb:bbenc1 obo:BFO_0000051 pmbb:collectionProcess1 .
              }
              Graph pmbb:entityLinkData
              {
                  pmbb:entLinkBbCrid a turbo:TURBO_0000533 .
                  pmbb:entLinkBbCrid obo:BFO_0000051 pmbb:entLinkBbSymb .
                  pmbb:entLinkBbCrid obo:BFO_0000051 pmbb:entLinkBbRegDen .
                  pmbb:entLinkBbSymb turbo:TURBO_0006510 '2' .
                  pmbb:entLinkBbSymb a turbo:TURBO_0000534 .
                  pmbb:entLinkBbRegDen a turbo:TURBO_0000535 .
                  pmbb:entLinkBbRegDen obo:IAO_0000219 pmbb:registry2 .
                  pmbb:registry2 a turbo:TURBO_0000543 .
                  
                  pmbb:entLinkPartCrid turbo:TURBO_0000302 pmbb:entLinkBbCrid .
                  
                  pmbb:entLinkPartCrid a turbo:TURBO_0000503 .
                  pmbb:entLinkPartCrid obo:BFO_0000051 pmbb:entLinkPartSymb .
                  pmbb:entLinkPartSymb a turbo:TURBO_0000504 .
                  pmbb:entLinkPartSymb turbo:TURBO_0006510 '1' .
                  pmbb:entLinkPartCrid obo:BFO_0000051 pmbb:entLinkPartRegDen .
                  pmbb:entLinkPartRegDen a turbo:TURBO_0000505 .
                  pmbb:entLinkPartRegDen obo:IAO_0000219 pmbb:registry1 .
                  pmbb:registry1 a turbo:TURBO_0000506 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insLink)
        entLink.joinParticipantsAndBiobankEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + dnaToConsenter).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + collToConsenter).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbSymbToLofDataset).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + bbEncToColl).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + graphBuilder).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation1).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + shortcutRelation2).get should be (false)
    }
}