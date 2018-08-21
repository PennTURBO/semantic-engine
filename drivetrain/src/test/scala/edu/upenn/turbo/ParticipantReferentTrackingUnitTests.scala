package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class ParticipantReferentTrackingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
    val partreftrack = new ParticipantReferentTracker
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
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

    test("sparql participant referent track pre process one non reftracked one reftracked")
    {
        logger.info("starting test sparql participant referent track pre process one non reftracked one reftracked")
        val insertString: String = """
             INSERT DATA {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                turbo:part1 a turbo:TURBO_0000502 .
                turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                turbo:patientCrid1 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCridSymbol turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol a turbo:TURBO_0000504 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientCridSymbol .
                turbo:dataset1 a obo:IAO_0000100 .
                turbo:instantiation1 obo:OBI_0000293 turbo:dataset1 .
                turbo:instantiation1 a turbo:TURBO_0000522 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientRegDenoter1 .
                turbo:patientRegDenoter1 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter1 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID1 a turbo:TURBO_0000506 .
                
                turbo:part2 a turbo:TURBO_0000502 .
                turbo:patientCrid2 obo:IAO_0000219 turbo:part2 .
                turbo:patientCrid2 obo:BFO_0000051 turbo:patientCridSymbol2 .
                turbo:patientCrid2 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol2 turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol2 obo:BFO_0000050 turbo:dataset2 .
                turbo:patientCridSymbol2 a turbo:TURBO_0000504 .
                turbo:dataset2 a obo:IAO_0000100 .
                turbo:instantiation2 obo:OBI_0000293 turbo:dataset2 .
                turbo:instantiation2 a turbo:TURBO_0000522 . 
                turbo:patientCrid2 obo:BFO_0000051 turbo:patientRegDenoter2 .
                turbo:patientRegDenoter2 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter2 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID2 a turbo:TURBO_0000506 .
                
                turbo:part2 turbo:TURBO_0006500 'true'^^xsd:boolean .
             }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackParticipants(cxn)
        
        val check1: String = """
             ASK {
                 turbo:part1 graphBuilder:willBeCombinedWith turbo:part2 .
                 turbo:part1 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                 }
          """
        
        val check2: String = """
             select ?s ?o where {
                 ?s graphBuilder:willBeCombinedWith ?o .
                 }
          """
        //helper.printAllInDatabase(cxn)
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
        val check2results: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check2, Array("s", "o"))
        check2results.size should be (1)
    }
    
    test("sparql participant referent track pre process two non reftracked one reftracked")
    {
        logger.info("starting test: sparql participant referent track pre process two non reftracked one reftracked")
        val insertString: String = """
             INSERT DATA {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                turbo:part1 a turbo:TURBO_0000502 .
                turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                turbo:patientCrid1 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCridSymbol turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol a turbo:TURBO_0000504 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientCridSymbol .
                turbo:dataset1 a obo:IAO_0000100 .
                turbo:instantiation1 obo:OBI_0000293 turbo:dataset1 .
                turbo:instantiation1 a turbo:TURBO_0000522 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientRegDenoter1 .
                turbo:patientRegDenoter1 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter1 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID1 a turbo:TURBO_0000506 .
                
                turbo:part3 a turbo:TURBO_0000502 .
                turbo:patientCrid3 obo:IAO_0000219 turbo:part3 .
                turbo:patientCrid3 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol2 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCridSymbol2 turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol2 a turbo:TURBO_0000504 .
                turbo:patientCrid3 obo:BFO_0000051 turbo:patientCridSymbol2 .
                turbo:patientCrid3 obo:BFO_0000051 turbo:patientRegDenoter2 .
                turbo:patientRegDenoter2 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter2 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID2 a turbo:TURBO_0000506 .
                
                turbo:part2 a turbo:TURBO_0000502 .
                turbo:patientCrid2 obo:IAO_0000219 turbo:part2 .
                turbo:patientCrid2 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol3 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCridSymbol3 turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol3 a turbo:TURBO_0000504 .
                turbo:patientCrid2 obo:BFO_0000051 turbo:patientCridSymbol .
                turbo:dataset2 a obo:IAO_0000100 .
                turbo:instantiation2 obo:OBI_0000293 turbo:dataset2 .
                turbo:instantiation2 a turbo:TURBO_0000522 . 
                turbo:patientCrid2 obo:BFO_0000051 turbo:patientRegDenoter3 .
                turbo:patientRegDenoter3 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter3 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID3 a turbo:TURBO_0000506 .
                
                turbo:part2 turbo:TURBO_0006500 'true'^^xsd:boolean .
             }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackParticipants(cxn)
        
        val check1: String = """
             ASK {
                 turbo:part1 graphBuilder:willBeCombinedWith turbo:part2 .
                 turbo:part1 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                 }
          """
        
        val check2: String = """
             ASK {
                 turbo:part3 graphBuilder:willBeCombinedWith turbo:part2 .
                 turbo:part3 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                 }
          """
        
        val check3: String = """
             select ?s ?o where {
                 ?s graphBuilder:willBeCombinedWith ?o .
                 }
          """
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
        val bool2: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check2).get
        bool2 should be (true)
        val check3results: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check3, Array("s", "o"))
        check3results.size should be (2)
    }
    
    test("sparql participant referent track pre process two non reftracked no reftracked")
    {
        val insertString: String = """
             INSERT DATA {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                turbo:part1 a turbo:TURBO_0000502 .
                turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                turbo:patientCrid1 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCridSymbol turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol a turbo:TURBO_0000504 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientCridSymbol .
                turbo:dataset1 a obo:IAO_0000100 .
                turbo:instantiation1 obo:OBI_0000293 turbo:dataset1 .
                turbo:instantiation1 a turbo:TURBO_0000522 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientRegDenoter1 .
                turbo:patientRegDenoter1 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter1 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID1 a turbo:TURBO_0000506 .
                
                turbo:part3 a turbo:TURBO_0000502 .
                turbo:patientCrid3 obo:IAO_0000219 turbo:part3 .
                turbo:patientCrid3 a turbo:TURBO_0000503 .
                turbo:patientCridSymbol2 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCridSymbol2 turbo:TURBO_0006510 '1' .
                turbo:patientCridSymbol2 a turbo:TURBO_0000504 .
                turbo:patientCrid3 obo:BFO_0000051 turbo:patientCridSymbol2 .
                turbo:patientCrid3 obo:BFO_0000051 turbo:patientRegDenoter2 .
                turbo:patientRegDenoter2 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter2 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID2 a turbo:TURBO_0000506 .
             }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackParticipants(cxn)
        
        val check1: String = """
             ASK {
                 turbo:part1 graphBuilder:willBeCombinedWith ?o .
                 turbo:part3 graphBuilder:willBeCombinedWith ?o .
                 turbo:part1 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                 turbo:part3 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                 FILTER (?o != turbo:part1)
                 FILTER (?o != turbo:part3)
                 }
          """
        
        val check2: String = """
             select ?s ?o where {
                 ?s graphBuilder:willBeCombinedWith ?o .
                 }
          """
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        val check2results: ArrayBuffer[ArrayBuffer[Value]] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + check2, Array("s", "o"))
        bool1 should be (true)
        check2results.size should be (2)
    }
    
    test("independent participant referent tracking via sparql")
    {
        val insertString: String = """
             INSERT DATA {
                 graph pmbb:expanded
                 {
                    turbo:part1 a turbo:TURBO_0000502 .
                    turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                    turbo:patientCrid1 a turbo:TURBO_0000503 .
                    turbo:patientCridSymbol obo:BFO_0000050 turbo:dataset1 .
                    turbo:patientCridSymbol turbo:TURBO_0006510 '1' .
                    turbo:patientCridSymbol a turbo:TURBO_0000504 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:patientCridSymbol .
                    turbo:dataset1 a obo:IAO_0000100 .
                    turbo:instantiation1 obo:OBI_0000293 turbo:dataset1 .
                    turbo:instantiation1 a turbo:TURBO_0000522 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:patientRegDenoter1 .
                    turbo:patientRegDenoter1 a turbo:TURBO_0000505 .
                    turbo:patientRegDenoter1 obo:IAO_0000219 turbo:patientRegID1 .
                    turbo:patientRegID1 a turbo:TURBO_0000506 .
                    turbo:patientRegDenoter obo:BFO_0000050 turbo:dataset1 .
                    turbo:patientCridSymbol obo:BFO_0000050 turbo:dataset1 .
                    turbo:dataset1 obo:BFO_0000051 turbo:patientRegDenoter .
                    turbo:dataset1 obo:BFO_0000051 turbo:patientCridSymbol .
                    
                    turbo:part2 a turbo:TURBO_0000502 .
                    turbo:patientCrid2 obo:IAO_0000219 turbo:part2 .
                    turbo:patientCrid2 a turbo:TURBO_0000503 .
                    turbo:patientCridSymbol2 obo:BFO_0000050 turbo:dataset1 .
                    turbo:patientCridSymbol2 turbo:TURBO_0006510 '1' .
                    turbo:patientCridSymbol2 a turbo:TURBO_0000504 .
                    turbo:patientCrid2 obo:BFO_0000051 turbo:patientCridSymbol2 .
                    turbo:part2 turbo:hasBiosex turbo:male .
                    turbo:patientCrid2 obo:BFO_0000051 turbo:patientRegDenoter2 .
                    turbo:patientRegDenoter2 a turbo:TURBO_0000505 .
                    turbo:patientRegDenoter2 obo:IAO_0000219 turbo:patientRegID1 .
                    turbo:patientRegID2 a turbo:TURBO_0000506 .
                    turbo:patientRegDenoter2 obo:BFO_0000050 turbo:dataset1 .
                    turbo:patientCridSymbol2 obo:BFO_0000050 turbo:dataset1 .
                    turbo:dataset1 obo:BFO_0000051 turbo:patientRegDenoter2 .
                    turbo:dataset1 obo:BFO_0000051 turbo:patientCridSymbol2 .
                    
                    turbo:part1 graphBuilder:willBeCombinedWith turbo:referentTrackedNode .
                    turbo:part2 graphBuilder:willBeCombinedWith turbo:referentTrackedNode .
                    turbo:part1 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                    turbo:part2 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
             }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        helper.completeReftrackProcess(cxn)
        
        val check1: String = """
             ASK {
                 ?part1 turbo:TURBO_0001700 turbo:referentTrackedNode .
                 ?part2 turbo:TURBO_0001700 turbo:referentTrackedNode .
                 ?part1 obo:IAO_0000225 obo:IAO_0000226 .
                 ?part2 obo:IAO_0000225 obo:IAO_0000226 .
                 ?part1 a turbo:TURBO_0000902 .
                 ?part2 a turbo:TURBO_0000902 .
                 
                 FILTER (?part1 != ?part2)
                 }
          """
        
        val check2: String = """
          ASK
          {
               turbo:referentTrackedNode a turbo:TURBO_0000502 .
               turbo:referentTrackedNode turbo:hasBiosex turbo:male .
               turbo:referentTrackedNode turbo:TURBO_0006500 'true'^^xsd:boolean .
          }
          """
        
        val check3: String = """
          ASK 
          {
               ?crid a turbo:TURBO_0000503 .
               ?crid obo:BFO_0000051 ?cridSymbol .
               ?cridSymbol a turbo:TURBO_0000504 .
               ?cridSymbol turbo:TURBO_0006510 '1' .
               ?crid obo:IAO_0000219 turbo:referentTrackedNode .
               ?crid obo:BFO_0000051 ?cridRegDen .
               ?cridRegDen a turbo:TURBO_0000505 .
               ?cridRegDen obo:IAO_0000219 turbo:patientRegID1 .
               turbo:patientRegID1 a turbo:TURBO_0000506 .
          }
          """
        
        val datasetStuff: String = """
               ASK
               {
                 turbo:instantiation1 a turbo:TURBO_0000522 .
                 turbo:instantiation1 obo:OBI_0000293 turbo:dataset1 .
                 turbo:dataset1 a obo:IAO_0000100 .
                 turbo:dataset1 obo:BFO_0000051 ?cridRegDen .
                 turbo:dataset1 obo:BFO_0000051 ?cridSymbol .
                 ?cridRegDen obo:BFO_0000050 turbo:dataset1 .
                 ?cridSymbol obo:BFO_0000050 turbo:dataset1 .
                 ?cridRegDen a turbo:TURBO_0000505 .
                 ?cridSymbol a turbo:TURBO_0000504 .
               }          
        """
        
        //helper.printAllInDatabase(cxn)
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
        
        val bool5: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check2).get
        bool5 should be (true)
        
        val bool6: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check3).get
        bool6 should be (true)
        
        val bool7: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + datasetStuff).get
        bool7 should be (true)
        
        val noMoreGraphBuilder1: String = """
          ASK { ?subject graphBuilder:willBeCombinedWith ?object . }
          """
        val bool2: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + noMoreGraphBuilder1).get
        bool2 should be (false)
        
        val noMoreGraphBuilder2: String = """
          ASK { ?subject graphBuilder:placeholderDemotionType ?object . }
          """
        val bool3: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + noMoreGraphBuilder2).get
        bool3 should be (false)
    }
    
    test("dependent participant referent tracking")
    {
        val insertString: String = """
             INSERT DATA {
                GRAPH pmbb:expanded {
                    turbo:dataset1 a obo:IAO_0000100 .
                    turbo:instantiation1 obo:OBI_0000293 turbo:dataset1 .
                    turbo:instantiation1 a turbo:TURBO_0000522 .
                    turbo:patientCrid1 obo:BFO_0000050 turbo:dataset1 .
                    turbo:patientCrid2 obo:BFO_0000050 turbo:dataset1 .
                    
                    turbo:part1 a turbo:TURBO_0000502 .
                    turbo:part1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    turbo:part1 obo:RO_0000086 turbo:biosex1 .
                    turbo:part1 obo:RO_0000086 turbo:biosex2 .
                    turbo:part1 obo:RO_0000086 turbo:height1 .
                    turbo:part1 obo:RO_0000086 turbo:weight1 .
                    turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                    turbo:part1 obo:RO_0000086 turbo:height2 .
                    turbo:part1 obo:RO_0000086 turbo:weight2 .
                    turbo:part1 obo:BFO_0000051 turbo:adipose2 .
                    turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                    turbo:part1 turbo:TURBO_0000303 turbo:birth2 .
                    
                    turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                    turbo:patientCrid1 a turbo:TURBO_0000503 .
                    turbo:cridSymbol1 a turbo:TURBO_0000504 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol1 .
                    turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:partRegDen1 .
                    turbo:partRegDen1 a turbo:TURBO_0000505 .
                    turbo:partRegDen1 obo:IAO_0000219 turbo:partRegID1 .
                    turbo:partRegID1 a turbo:TURBO_0000506 .
                    
                    turbo:patientCrid2 obo:IAO_0000219 turbo:part1 .
                    turbo:patientCrid2 a turbo:TURBO_0000503 .
                    turbo:cridSymbol2 turbo:TURBO_0006510 '1' . 
                    turbo:cridSymbol2 a turbo:TURBO_0000504 .
                    turbo:patientCrid2 obo:BFO_0000051 turbo:cridSymbol2 .
                    turbo:patientCrid2 obo:BFO_0000051 turbo:partRegDen2 .
                    turbo:partRegDen2 a turbo:TURBO_0000505 .
                    turbo:partRegDen2 obo:IAO_0000219 turbo:partRegID1 .
                    turbo:partRegID1 a turbo:TURBO_0000506 .
                    
                    turbo:biosex1 a obo:PATO_0000047 .
                    turbo:biosex2 a obo:PATO_0000047 .
                    turbo:height1 a obo:PATO_0000119 .
                    turbo:weight1 a obo:PATO_0000128 .
                    turbo:adipose1 a obo:UBERON_0001013 .
                    turbo:height2 a obo:PATO_0000119 .
                    turbo:weight2 a obo:PATO_0000128 .
                    turbo:adipose2 a obo:UBERON_0001013 .
                    turbo:birth1 a obo:UBERON_0035946 .
                    turbo:birth2 a obo:UBERON_0035946 .
                }
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackParticipantDependents(cxn)
        
        val check1: String = """
             ASK {
                   turbo:patientCrid1 graphBuilder:willBeCombinedWith ?newPSC .
                   turbo:patientCrid2 graphBuilder:willBeCombinedWith ?newPSC .
                   turbo:height1 graphBuilder:willBeCombinedWith ?newHeight .
                   turbo:height2 graphBuilder:willBeCombinedWith ?newHeight .
                   turbo:weight1 graphBuilder:willBeCombinedWith ?newWeight .
                   turbo:weight2 graphBuilder:willBeCombinedWith ?newWeight .
                   turbo:adipose1 graphBuilder:willBeCombinedWith ?newAdi .
                   turbo:adipose2 graphBuilder:willBeCombinedWith ?newAdi .
                   turbo:biosex1 graphBuilder:willBeCombinedWith ?newBioSex .
                   turbo:biosex2 graphBuilder:willBeCombinedWith ?newBioSex .
                   
                   turbo:patientCrid1 graphBuilder:placeholderDemotionType turbo:TURBO_0000903  .
                   turbo:height1 graphBuilder:placeholderDemotionType turbo:TURBO_0001905  .
                   turbo:weight1 graphBuilder:placeholderDemotionType turbo:TURBO_0001908  .
                   turbo:adipose1 graphBuilder:placeholderDemotionType turbo:TURBO_0001901  .
                   turbo:biosex1 graphBuilder:placeholderDemotionType turbo:TURBO_0001902  .
                 }
          """
        //helper.printAllInDatabase(cxn)
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
    }
    
    test("secondary dependent participant referent tracking two each non-reftracked")
    {
        val insertString: String = """
             INSERT DATA {
                GRAPH pmbb:expanded {
                    turbo:part1 a turbo:TURBO_0000502 .
                    turbo:part1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                    turbo:patientCrid1 a turbo:TURBO_0000503 .
                    turbo:patientCrid1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    turbo:cridSymbol1 a turbo:TURBO_0000504 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol1 .
                    turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                    turbo:cridSymbol2 turbo:TURBO_0006510 '1' . 
                    turbo:cridSymbol2 a turbo:TURBO_0000504 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol2 .
                    
                    turbo:patientCrid1 obo:BFO_0000051 turbo:regDen1 .
                    turbo:regDen1 a turbo:TURBO_0000505 .
                    turbo:regDen1 obo:IAO_0000219 turbo:regIDURI1 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:regDen2 .
                    turbo:regDen2 a turbo:TURBO_0000505 .
                    turbo:regDen2 obo:IAO_0000219 turbo:regIDURI1 .
                }
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackSecondaryParticipantDependents(cxn)
        
        val check1: String = """
             ASK {
                   turbo:regDen1 graphBuilder:willBeCombinedWith ?newRegDen .
                   turbo:regDen2 graphBuilder:willBeCombinedWith ?newRegDen .
                   turbo:cridSymbol1 graphBuilder:willBeCombinedWith ?newCridSymbol .
                   turbo:cridSymbol2 graphBuilder:willBeCombinedWith ?newCridSymbol .
                   
                   turbo:regDen1 graphBuilder:placeholderDemotionType turbo:TURBO_0000905  .
                   turbo:regDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000905  .
                   turbo:cridSymbol1 graphBuilder:placeholderDemotionType turbo:TURBO_0000904  .
                   turbo:cridSymbol2 graphBuilder:placeholderDemotionType turbo:TURBO_0000904  .
                  
                   FILTER (?newRegDen != turbo:regDen1)
                   FILTER (?newRegDen != turbo:regDen2)
                   FILTER (?newCridSymbol != turbo:cridSymbol1)
                   FILTER (?newCridSymbol != turbo:cridSymbol2)
                 }
          """
        //helper.printAllInDatabase(cxn)
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
    }
    
    test("secondary dependent participant referent tracking two each one of each ref tracked")
    {
        val insertString: String = """
             INSERT DATA {
                GRAPH pmbb:expanded {
                    turbo:part1 a turbo:TURBO_0000502 .
                    turbo:part1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                    turbo:patientCrid1 a turbo:TURBO_0000503 .
                    turbo:patientCrid1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    turbo:cridSymbol1 a turbo:TURBO_0000504 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol1 .
                    turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                    turbo:cridSymbol2 turbo:TURBO_0006510 '1' . 
                    turbo:cridSymbol2 a turbo:TURBO_0000504 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol2 .
                    turbo:cridSymbol2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    
                    turbo:patientCrid1 obo:BFO_0000051 turbo:regDen1 .
                    turbo:regDen1 a turbo:TURBO_0000505 .
                    turbo:regDen1 obo:IAO_0000219 turbo:regIDURI1 .
                    turbo:patientCrid1 obo:BFO_0000051 turbo:regDen2 .
                    turbo:regDen2 a turbo:TURBO_0000505 .
                    turbo:regDen2 obo:IAO_0000219 turbo:regIDURI1 .
                    turbo:regDen2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackSecondaryParticipantDependents(cxn)
        
        val check1: String = """
             ASK {
                   turbo:regDen1 graphBuilder:willBeCombinedWith turbo:regDen2 .
                   turbo:cridSymbol1 graphBuilder:willBeCombinedWith turbo:cridSymbol2 .
                   
                   turbo:regDen1 graphBuilder:placeholderDemotionType turbo:TURBO_0000905  .
                   turbo:cridSymbol1 graphBuilder:placeholderDemotionType turbo:TURBO_0000904  .
                 }
          """
        //helper.printAllInDatabase(cxn)
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
    }
  
    test("four participants combined into two")
    {
        val insertString: String = """
             INSERT DATA {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                turbo:part1 a turbo:TURBO_0000502 .
                turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                turbo:patientCrid1 a turbo:TURBO_0000503 .
                turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol1 .
                turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                turbo:cridSymbol1 a turbo:TURBO_0000504 .
                turbo:patientCrid1 obo:BFO_0000051 turbo:patientRegDenoter1 .
                turbo:patientRegDenoter1 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter1 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID1 a turbo:TURBO_0000506 .
                
                turbo:part2 a turbo:TURBO_0000502 .
                turbo:patientCrid2 obo:IAO_0000219 turbo:part2 .
                turbo:patientCrid2 a turbo:TURBO_0000503 .
                turbo:cridSymbol2 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCrid2 obo:BFO_0000051 turbo:cridSymbol2 .
                turbo:cridSymbol2 turbo:TURBO_0006510 '1' .
                turbo:cridSymbol2 a turbo:TURBO_0000504 .
                turbo:patientCrid2 obo:BFO_0000051 turbo:patientRegDenoter2 .
                turbo:patientRegDenoter2 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter2 obo:IAO_0000219 turbo:patientRegID1 .
                turbo:patientRegID2 a turbo:TURBO_0000506 .
               
                turbo:part3 a turbo:TURBO_0000502 .
                turbo:patientCrid3 obo:IAO_0000219 turbo:part3 .
                turbo:patientCrid3 a turbo:TURBO_0000503 .
                turbo:cridSymbol3 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCrid3 obo:BFO_0000051 turbo:cridSymbol3 .
                turbo:cridSymbol3 turbo:TURBO_0006510 '1' .
                turbo:cridSymbol3 a turbo:TURBO_0000504 .
                turbo:patientCrid3 obo:BFO_0000051 turbo:patientRegDenoter3 .
                turbo:patientRegDenoter3 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter3 obo:IAO_0000219 turbo:patientRegID2 .
                turbo:patientRegID3 a turbo:TURBO_0000506 .
                
                turbo:part4 a turbo:TURBO_0000502 .
                turbo:patientCrid4 obo:IAO_0000219 turbo:part4 .
                turbo:patientCrid4 a turbo:TURBO_0000503 .
                turbo:cridSymbol4 obo:BFO_0000050 turbo:dataset1 .
                turbo:patientCrid4 obo:BFO_0000051 turbo:cridSymbol4 .
                turbo:cridSymbol4 turbo:TURBO_0006510 '1' .
                turbo:cridSymbol4 a turbo:TURBO_0000504 .
                turbo:patientCrid4 obo:BFO_0000051 turbo:patientRegDenoter4 .
                turbo:patientRegDenoter4 a turbo:TURBO_0000505 .
                turbo:patientRegDenoter4 obo:IAO_0000219 turbo:patientRegID2 .
                turbo:patientRegID4 a turbo:TURBO_0000506 .
                
                turbo:dataset1 a obo:IAO_0000100 .
                turbo:instantiation obo:OBI_0000293 turbo:dataset1 .
                turbo:instantiation a turbo:TURBO_0000522 .        
             }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        partreftrack.reftrackParticipants(cxn)
        
        val check1: String = """
             ASK {
                   turbo:part1 graphBuilder:willBeCombinedWith ?newpart1 .
                   turbo:part2 graphBuilder:willBeCombinedWith ?newpart1 .
                   turbo:part3 graphBuilder:willBeCombinedWith ?newpart2 .
                   turbo:part4 graphBuilder:willBeCombinedWith ?newpart2 .
                   
                   turbo:part1 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                   turbo:part2 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                   turbo:part3 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                   turbo:part4 graphBuilder:placeholderDemotionType turbo:TURBO_0000902 .
                 }
          """
        //helper.printAllInDatabase(cxn)
        val bool1: Boolean = update.querySparqlBoolean(cxn, sparqlPrefixes + check1).get
        bool1 should be (true)
    }
}