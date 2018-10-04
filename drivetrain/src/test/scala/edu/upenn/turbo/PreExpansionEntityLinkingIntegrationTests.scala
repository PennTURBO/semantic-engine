package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class PreExpansionEntityLinkingIntegrationTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    val entlink: EntityLinker = new EntityLinker()
    val ontLoad: OntologyLoader = new OntologyLoader()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
        //load TURBO ontology manually
        ontLoad.addOntologyFromUrl(cxn)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    /**
     * This test is designed to test the capacity of Drivetrain to handle entity linking pre-expansion. We will insert two healthcare encounters and 
     * two biobank encounters in shortcut form, with shortcut links to consenter literal values and consenter registries. One of the two of each pair
     * of encounters will have an associated shortcut consenter which matches the ID/registry combo and therefore should be linked. We will check that 
     * the link is created after running the code blocks contained within "expansion" and "entity linking". We will also ensure that proper join data
     * is created in named graph pmbb:entityLinkData for the encounters which do not match one of the consenters. To complete the test, we will insert
     * consenters to match the previously unmatched encounters in order to make sure that the automatically created join data is properly read. 
     * 
     * Run in SBT using "test-only edu.upenn.turbo.PreExpansionEntityLinkingIntegrationTests"
     */
    
    test("test pre-expansion entity linking")
    {
        val insert: String = """
          Insert Data
          {
              Graph pmbb:Shortcuts_hcEncShortcuts
              {
                  pmbb:hcEnc1 a obo:OGMS_0000097 ;
                      turbo:TURBO_0010000 '1' ;
                      turbo:ScHcEnc2UnexpandedConsenter "http://www.itmat.upenn.edu/biobank/cons1"^^xsd:anyURI ;
                      turbo:TURBO_0010002 "http://www.itmat.upenn.edu/biobank/consReg1"^^xsd:anyURI ;
                      turbo:TURBO_0000643 'hc_enc_dataset' ;
                      turbo:TURBO_0000648 '3' ;
                      turbo:TURBO_0000650 "http://www.itmat.upenn.edu/biobank/hcEncReg1"^^xsd:anyURI .
                  pmbb:hcEnc2 a obo:OGMS_0000097 ;
                      turbo:TURBO_0010000 '2' ;
                      turbo:ScHcEnc2UnexpandedConsenter "http://www.itmat.upenn.edu/biobank/cons2"^^xsd:anyURI ;
                      turbo:TURBO_0010002 "http://www.itmat.upenn.edu/biobank/consReg1"^^xsd:anyURI ;
                      turbo:TURBO_0000643 'hc_enc_dataset' ;
                      turbo:TURBO_0000648 '4' ;
                      turbo:TURBO_0000650 "http://www.itmat.upenn.edu/biobank/hcEncReg1"^^xsd:anyURI .
              }
              Graph pmbb:Shortcuts_bbEncShortcuts
              {
                  pmbb:bbEnc1 a turbo:TURBO_0000527 ;
                      turbo:TURBO_0010010 '1' ;
                      turbo:ScBbEnc2UnexpandedConsenter "http://www.itmat.upenn.edu/biobank/cons1"^^xsd:anyURI ;
                      turbo:TURBO_0010012 "http://www.itmat.upenn.edu/biobank/consReg1"^^xsd:anyURI ;
                      turbo:TURBO_0000623 'bb_enc_dataset' ;
                      turbo:TURBO_0000628 '5';
                      turbo:TURBO_0000630 "http://www.itmat.upenn.edu/biobank/bbEncReg1"^^xsd:anyURI .
                  pmbb:bbEnc2 a turbo:TURBO_0000527 ;
                      turbo:TURBO_0010010 '2' ;
                      turbo:ScBbEnc2UnexpandedConsenter "http://www.itmat.upenn.edu/biobank/cons2"^^xsd:anyURI ;
                      turbo:TURBO_0010012 "http://www.itmat.upenn.edu/biobank/consReg1"^^xsd:anyURI ;
                      turbo:TURBO_0000623 'bb_enc_dataset' ;
                      turbo:TURBO_0000628 '6';
                      turbo:TURBO_0000630 "http://www.itmat.upenn.edu/biobank/bbEncReg1"^^xsd:anyURI .
              }
              Graph pmbb:Shortcuts_bbConsShortcuts
              {
                  pmbb:cons1 a turbo:TURBO_0000502 ;
                      turbo:TURBO_0000610 "http://www.itmat.upenn.edu/biobank/consReg1"^^xsd:anyURI ;
                      turbo:TURBO_0000608 '1' ;
                      turbo:TURBO_0000603 'bb_cons_dataset' .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        runDrivetrainTestStack()
        
        val ask1: String = """
          Ask
          {
              Graph pmbb:expanded
              {
                  ?cons1 a turbo:TURBO_0000502 .
                  ?consCrid1 a turbo:TURBO_0000503 .
                  ?consCrid1 obo:IAO_0000219 ?cons1 .
                  ?consCrid1 obo:BFO_0000051 ?consRegDen1 .
                  ?consCrid1 obo:BFO_0000051 ?consSymb1 .
                  ?consSymb1 a turbo:TURBO_0000504 .
                  ?consRegDen1 a turbo:TURBO_0000505 .
                  ?consSymb1 turbo:TURBO_0006510 '1'^^xsd:string .
                  ?consRegDen1 obo:IAO_0000219 pmbb:consReg1 .
                  pmbb:consReg1 a turbo:TURBO_0000506 .
                  ?consDataset a obo:IAO_0000100 .
                  ?consDataset obo:BFO_0000051 ?consSymb1 .
                  ?consDataset obo:BFO_0000051 ?consRegDen1 .
                  ?consDataset dc11:title 'bb_cons_dataset' .
                  ?cons1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  ?cons1 obo:RO_0000056 ?hcEnc3 .
                  ?cons1 obo:RO_0000056 ?bbEnc5 .
                  
                  ?hcEnc3 a obo:OGMS_0000097 .
                  ?hcEncCrid3 a turbo:TURBO_0000508 .
                  ?hcEncCrid3 obo:IAO_0000219 ?hcEnc3 .
                  ?hcEncCrid3 obo:BFO_0000051 ?hcEncRegDen3 .
                  ?hcEncCrid3 obo:BFO_0000051 ?hcEncSymb3 .
                  ?hcEncSymb3 a turbo:TURBO_0000509 .
                  ?hcEncRegDen3 a turbo:TURBO_0000510 .
                  ?hcEncSymb3 turbo:TURBO_0006510 '3'^^xsd:string .
                  ?hcEncRegDen3 obo:IAO_0000219 pmbb:hcEncReg1 .
                  pmbb:hcEncReg1 a turbo:TURBO_0000513 .
                  ?hcEncDataset a obo:IAO_0000100 .
                  ?hcEncDataset obo:BFO_0000051 ?hcEncSymb3 .
                  ?hcEncDataset obo:BFO_0000051 ?hcEncRegDen3 .
                  ?hcEncDataset dc11:title 'hc_enc_dataset' .
                  ?hcEnc3 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  ?bbEnc5 a turbo:TURBO_0000527 .
                  ?bbEncCrid5 a turbo:TURBO_0000533 .
                  ?bbEncCrid5 obo:IAO_0000219 ?bbEnc5 .
                  ?bbEncCrid5 obo:BFO_0000051 ?bbEncRegDen5 .
                  ?bbEncCrid5 obo:BFO_0000051 ?bbEncSymb5 .
                  ?bbEncSymb5 a turbo:TURBO_0000534 .
                  ?bbEncRegDen5 a turbo:TURBO_0000535 .
                  ?bbEncSymb5 turbo:TURBO_0006510 '5'^^xsd:string .
                  ?bbEncRegDen5 obo:IAO_0000219 pmbb:bbEncReg1 .
                  pmbb:bbEncReg1 a turbo:TURBO_0000543 .
                  ?bbEncDataset a obo:IAO_0000100 .
                  ?bbEncDataset obo:BFO_0000051 ?bbEncSymb5 .
                  ?bbEncDataset obo:BFO_0000051 ?bbEncRegDen5 .
                  ?bbEncDataset dc11:title 'bb_enc_dataset' .
                  ?bbEnc5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              }
          }
          """
        
        val ask2: String = """
          Ask
          {
              Graph pmbb:entityLinkData
              {
                  ?entLinkHcCrid a turbo:TURBO_0000508 .
                  ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcSymb .
                  ?entLinkHcSymb turbo:TURBO_0006510 '4'^^xsd:string .
                  ?entLinkHcSymb a turbo:TURBO_0000509 .
                  ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcRegDen .
                  ?entLinkHcRegDen a turbo:TURBO_0000510 .
                  ?entLinkHcRegDen obo:IAO_0000219 pmbb:hcEncReg1 .
                  pmbb:hcEncReg1 a turbo:TURBO_0000513 .
                  
                  ?entLinkPartCrid1 turbo:TURBO_0000302 ?entLinkHcCrid .
                  
                  ?entLinkPartCrid1 a turbo:TURBO_0000503 .
                  ?entLinkPartCrid1 obo:BFO_0000051 ?entLinkPartSymb1 .
                  ?entLinkPartSymb1 a turbo:TURBO_0000504 .
                  ?entLinkPartSymb1 turbo:TURBO_0006510 '2'^^xsd:string .
                  ?entLinkPartCrid1 obo:BFO_0000051 ?entLinkPartRegDen1 .
                  ?entLinkPartRegDen1 a turbo:TURBO_0000505 .
                  ?entLinkPartRegDen1 obo:IAO_0000219 pmbb:consReg1 .
                  pmbb:consReg1 a turbo:TURBO_0000506 .
                  
                  ?entLinkBbCrid a turbo:TURBO_0000533 .
                  ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbSymb .
                  ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbRegDen .
                  ?entLinkBbSymb turbo:TURBO_0006510 '6'^^xsd:string .
                  ?entLinkBbSymb a turbo:TURBO_0000534 .
                  ?entLinkBbRegDen a turbo:TURBO_0000535 .
                  ?entLinkBbRegDen obo:IAO_0000219 pmbb:bbEncReg1 .
                  pmbb:bbEncReg1 a turbo:TURBO_0000543 .
                  
                  ?entLinkPartCrid2 turbo:TURBO_0000302 ?entLinkBbCrid .
                  
                  ?entLinkPartCrid2 a turbo:TURBO_0000503 .
                  ?entLinkPartCrid2 obo:BFO_0000051 ?entLinkPartSymb2 .
                  ?entLinkPartSymb2 a turbo:TURBO_0000504 .
                  ?entLinkPartSymb2 turbo:TURBO_0006510 '2'^^xsd:string .
                  ?entLinkPartCrid2 obo:BFO_0000051 ?entLinkPartRegDen2 .
                  ?entLinkPartRegDen2 a turbo:TURBO_0000505 .
                  ?entLinkPartRegDen2 obo:IAO_0000219 pmbb:consReg1 .
              }
          }
          """
        
        val countLinks: String = """
          Select (count (?links) as ?linksCount) Where
          {
              ?links obo:RO_0000056 ?o .
          }
          """
        
        val countEntLink = """
        Select (count (?s) as ?scount)
        Where
        {
            Graph pmbb:entityLinkData
            {
                ?s ?p ?o .
            }
        }
        """
            
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countLinks, "linksCount")(0).split("\"")(1).toInt should be (2)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countEntLink, "scount")(0).split("\"")(1).toInt should be (33)
        
        val insert2: String = """
          Insert Data
          {
              Graph pmbb:Shortcuts_bbConsShortcuts
              {
                  pmbb:cons2 a turbo:TURBO_0000502 ;
                      turbo:TURBO_0000610 "http://www.itmat.upenn.edu/biobank/consReg1"^^xsd:anyURI ;
                      turbo:TURBO_0000608 '2' ;
                      turbo:TURBO_0000603 'bb_cons_dataset' .
              }
          }
          """
        
        update.updateSparql(cxn, sparqlPrefixes + insert2)
        runDrivetrainTestStack()
        
        val ask3: String = """
          Ask
          {
              Graph pmbb:expanded
              {
                  ?cons2 a turbo:TURBO_0000502 .
                  ?consCrid2 a turbo:TURBO_0000503 .
                  ?consCrid2 obo:IAO_0000219 ?cons2 .
                  ?consCrid2 obo:BFO_0000051 ?consRegDen2 .
                  ?consCrid2 obo:BFO_0000051 ?consSymb2 .
                  ?consSymb2 a turbo:TURBO_0000504 .
                  ?consRegDen2 a turbo:TURBO_0000505 .
                  ?consSymb2 turbo:TURBO_0006510 '2'^^xsd:string .
                  ?consRegDen2 obo:IAO_0000219 pmbb:consReg1 .
                  pmbb:consReg1 a turbo:TURBO_0000506 .
                  ?consDataset a obo:IAO_0000100 .
                  ?consDataset obo:BFO_0000051 ?consSymb2 .
                  ?consDataset obo:BFO_0000051 ?consRegDen2 .
                  ?consDataset dc11:title 'bb_cons_dataset' .
                  ?cons2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  ?cons2 obo:RO_0000056 ?hcEnc4 .
                  ?cons2 obo:RO_0000056 ?bbEnc6 .
                  
                  ?hcEnc4 a obo:OGMS_0000097 .
                  ?hcEncCrid4 a turbo:TURBO_0000508 .
                  ?hcEncCrid4 obo:IAO_0000219 ?hcEnc4 .
                  ?hcEncCrid4 obo:BFO_0000051 ?hcEncRegDen4 .
                  ?hcEncCrid4 obo:BFO_0000051 ?hcEncSymb4 .
                  ?hcEncSymb4 a turbo:TURBO_0000509 .
                  ?hcEncRegDen4 a turbo:TURBO_0000510 .
                  ?hcEncSymb4 turbo:TURBO_0006510 '4'^^xsd:string .
                  ?hcEncRegDen4 obo:IAO_0000219 pmbb:hcEncReg1 .
                  pmbb:hcEncReg1 a turbo:TURBO_0000513 .
                  ?hcEncDataset a obo:IAO_0000100 .
                  ?hcEncDataset obo:BFO_0000051 ?hcEncSymb4 .
                  ?hcEncDataset obo:BFO_0000051 ?hcEncRegDen4 .
                  ?hcEncDataset dc11:title 'hc_enc_dataset' .
                  ?hcEnc4 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  ?bbEnc6 a turbo:TURBO_0000527 .
                  ?bbEncCrid6 a turbo:TURBO_0000533 .
                  ?bbEncCrid6 obo:IAO_0000219 ?bbEnc6 .
                  ?bbEncCrid6 obo:BFO_0000051 ?bbEncRegDen6 .
                  ?bbEncCrid6 obo:BFO_0000051 ?bbEncSymb6 .
                  ?bbEncSymb6 a turbo:TURBO_0000534 .
                  ?bbEncRegDen6 a turbo:TURBO_0000535 .
                  ?bbEncSymb6 turbo:TURBO_0006510 '6'^^xsd:string .
                  ?bbEncRegDen6 obo:IAO_0000219 pmbb:bbEncReg1 .
                  pmbb:bbEncReg1 a turbo:TURBO_0000543 .
                  ?bbEncDataset a obo:IAO_0000100 .
                  ?bbEncDataset obo:BFO_0000051 ?bbEncSymb6 .
                  ?bbEncDataset obo:BFO_0000051 ?bbEncRegDen6 .
                  ?bbEncDataset dc11:title 'bb_enc_dataset' .
                  ?bbEnc6 turbo:TURBO_0006500 'true'^^xsd:boolean .
              }
          }
          """
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (true)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countLinks, "linksCount")(0).split("\"")(1).toInt should be (4)
    }
    
   def runDrivetrainTestStack()
    {
        DrivetrainDriver.main(Array("expand", "--skipchecks"))
        DrivetrainDriver.main(Array("reftrack")) 
        DrivetrainDriver.main(Array("entlink", "false")) 
    }
}