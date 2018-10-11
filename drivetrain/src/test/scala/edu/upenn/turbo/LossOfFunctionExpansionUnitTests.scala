package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class LossOfFunctionExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val expand: Expander = new Expander
    
    val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
    
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
                  pmbb:allele1 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele1 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele1 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele1 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  pmbb:allele1 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele1 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  
                  # pmbb:allele4 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele4 turbo:TURBO_0007601 "B" .
                  pmbb:allele4 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele4 a obo:OBI_0001352 .
                  pmbb:allele4 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele4 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele4 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele4 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele4 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele5 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  # pmbb:allele5 turbo:TURBO_0007601 "B" .
                  pmbb:allele5 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele5 a obo:OBI_0001352 .
                  pmbb:allele5 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele5 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele5 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele5 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele5 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele6 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele6 turbo:TURBO_0007601 "B" .
                  # pmbb:allele6 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele6 a obo:OBI_0001352 .
                  pmbb:allele6 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele6 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele6 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele6 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele6 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele7 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele7 turbo:TURBO_0007601 "B" .
                  pmbb:allele7 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # pmbb:allele7 a obo:OBI_0001352 .
                  pmbb:allele7 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele7 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele7 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele7 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele7 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele8 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele8 turbo:TURBO_0007601 "B" .
                  pmbb:allele8 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele8 a obo:OBI_0001352 .
                  # pmbb:allele8 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele8 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele8 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele8 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele8 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele9 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele9 turbo:TURBO_0007601 "B" .
                  pmbb:allele9 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele9 a obo:OBI_0001352 .
                  pmbb:allele9 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # pmbb:allele9 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele9 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele9 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele9 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele10 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele10 turbo:TURBO_0007601 "B" .
                  pmbb:allele10 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele10 a obo:OBI_0001352 .
                  pmbb:allele10 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele10 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele10 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  # pmbb:allele10 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  pmbb:allele10 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele11 turbo:TURBO_0007605 "gene:TSPAN6(ENSG00000000003);zygosity:1" .
                  pmbb:allele11 turbo:TURBO_0007601 "B" .
                  pmbb:allele11 turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele11 a obo:OBI_0001352 .
                  pmbb:allele11 turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele11 turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele11 turbo:TURBO_0007610 "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                  pmbb:allele11 turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                  # pmbb:allele11 turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                  
                  pmbb:allele1 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele4 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele5 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele6 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele7 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele8 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele9 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele10 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
                  pmbb:allele11 graphBuilder:willBeLinkedWith pmbb:bbEnc1 .
              }
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
          }"""
        
        update.updateSparql(cxn, sparqlPrefixes + insert)
    }
    
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    /**
   * These tests should test 
   * 1. allele expansion method
   * 2. unexpanded allele error logging method
   */
    test("expand 1 row LOF properly formatted, disregard 8 rows improperly formatted")
    {
        expand.expandLossOfFunctionShortcuts(cxn, 
            "http://www.itmat.upenn.edu/biobank/test_instantiation_1", 
            ArrayBuffer("http://www.itmat.upenn.edu/biobank/LOFShortcuts"), randomUUID)  
        
        val countTrips: String = 
        """
        Select * Where
        {
            Graph pmbb:expanded
            {
                ?s ?p ?o .
            }
        }
        """
        
        val tripsResult: ArrayBuffer[String] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countTrips, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://transformunify.org/ontologies/TURBO_0000305",
            "http://transformunify.org/ontologies/TURBO_0006512", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://transformunify.org/ontologies/TURBO_0006510",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000643",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OGG_0000000014", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0006510", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0006500", "http://purl.obolibrary.org/obo/RO_0000056",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0006500"
        )
        
        helper.checkStringArraysForEquivalency(checkPredicates, tripsResult.toArray) should be (true)
        
        tripsResult.size should be (60)
        
        val restNotExpanded: String = 
        """
        Select (Count (?allele) AS ?expandedCount) Where
        {
            # unexpanded alleles should not be "about" a DNA
            ?allele obo:IAO_0000136 ?DNA .
            ?DNA a obo:OBI_0001868 .
        }
        """
        
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + restNotExpanded, "expandedCount")(0) should be ("\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>")
        
        val checkDelete: String = 
        """
        Ask
        {
            pmbb:allele1 ?p ?o .
        }
        """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + checkDelete).get should be (false)
    }
    
    test("test data format error logging")
    {
        /**
         * As of 6/22/18, data format error logging method takes all unexpanded alleles and considers them data format error case.
         */
        expand.createErrorTriplesForUnexpandedAlleles(cxn, ArrayBuffer("http://www.itmat.upenn.edu/biobank/LOFShortcuts"))
        
        val ask: String =
        """
        Ask
        {
            Graph pmbb:errorLogging
            {
                pmbb:allele1 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele4 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele5 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele6 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele7 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele8 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele9 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele10 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                pmbb:allele11 graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
            }
        }  
        """
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
}