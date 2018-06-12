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
              Graph pmbb:LOFShortcuts
              {
                  pmbb:allele1 a obo:OBI_0001352 .
                  pmbb:allele1 turbo:TURBO_0007601 "B" .
                  pmbb:allele1 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele1 turbo:TURBO_0007604 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele1 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele1 turbo:TURBO_0007605 "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                  pmbb:allele1 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  pmbb:allele1 turbo:TURBO_0007606 "1"^^<http://www.w3.org/2001/XMLSchema#integer> .
                  pmbb:allele1 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele1 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  
                  pmbb:allele2 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele2 turbo:TURBO_0007601 "B" .
                  pmbb:allele2 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000590"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele2 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  pmbb:allele2 turbo:TURBO_0007604 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele2 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele2 turbo:TURBO_0007606 "2"^^<http://www.w3.org/2001/XMLSchema#integer> .
                  pmbb:allele2 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele2 a obo:OBI_0001352 .
                  pmbb:allele2 turbo:TURBO_0007605 "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                  
                  pmbb:allele3 turbo:TURBO_0007605 "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                  pmbb:allele3 turbo:TURBO_0007606 "1"^^<http://www.w3.org/2001/XMLSchema#integer> .
                  pmbb:allele3 turbo:TURBO_0007601 "B" .
                  pmbb:allele3 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele3 a obo:OBI_0001352 .
                  pmbb:allele3 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele3 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele3 turbo:TURBO_0007604 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele3 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele3 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
              }}"""
        helper.updateSparql(cxn, sparqlPrefixes + insert)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("3 LOF alleles connect to a bb enc with consenter")
    {
              val insert: String = """
              Insert Data {
              Graph pmbb:expanded
              {
                  pmbb:bbSymb1 turbo:TURBO_0006510 "B" ;
          		               a turbo:TURBO_0000534 .
              		pmbb:bbEncCrid1 a turbo:TURBO_0000533 ;
              		           obo:BFO_0000051 pmbb:bbSymb1 ;
              		           obo:IAO_0000219 pmbb:bbEnc1 ;
              		           obo:BFO_0000051 pmbb:bbEncRegDen1 .
              		pmbb:bbEncRegDen1 a turbo:TURBO_0000535 ;
              		             obo:IAO_0000219 turbo:TURBO_0000420 .
              		turbo:TURBO_0000420 a turbo:TURBO_0000543 .
              		pmbb:bbEnc1  a  turbo:TURBO_0000527 ;
              		       turbo:TURBO_0006500 'true'^^xsd:boolean .
          		    pmbb:consenter1 obo:RO_0000056 pmbb:bbEnc1 .
          		    pmbb:consenter1 a turbo:TURBO_0000502 .
          		    pmbb:consenter1 turbo:TURBO_0006500 'true'^^xsd:boolean .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.connectLossOfFunctionToBiobankEncounters(cxn)
        
        val ask: String = """
          ASK
          {
              pmbb:allele1 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
              pmbb:allele2 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
              pmbb:allele3 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
          }
          """
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("3 LOF alleles connect to a bb enc without consenter")
    {
              val insert: String = """
              Insert Data {
              Graph pmbb:expanded
              {
                  pmbb:bbSymb1 turbo:TURBO_0006510 "B" ;
          		               a turbo:TURBO_0000534 .
              		pmbb:bbEncCrid1 a turbo:TURBO_0000533 ;
              		           obo:BFO_0000051 pmbb:bbSymb1 ;
              		           obo:IAO_0000219 pmbb:bbEnc1 ;
              		           obo:BFO_0000051 pmbb:bbEncRegDen1 .
              		pmbb:bbEncRegDen1 a turbo:TURBO_0000535 ;
              		             obo:IAO_0000219 turbo:TURBO_0000420 .
              		turbo:TURBO_0000420 a turbo:TURBO_0000543 .
              		pmbb:bbEnc1  a  turbo:TURBO_0000527 ;
              		       turbo:TURBO_0006500 'true'^^xsd:boolean .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.connectLossOfFunctionToBiobankEncounters(cxn)
        
        val ask1: String = """
          ASK
          {
              pmbb:allele1 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
          }
          """
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (false)
        
        val ask2: String = """
          ASK
          {
              pmbb:allele2 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
          }
          """
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
              
        val ask3: String = """
          ASK
          {
              pmbb:allele3 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
          }
          """
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (false)
    }
}