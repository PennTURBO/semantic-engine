package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class EncounterReferentTrackingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
    val encreftrack = new EncounterReferentTracker
    
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

    test("sparql bb enc referent track pre process one non reftracked one reftracked same identifier and reg")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:bbenc1 a turbo:TURBO_0000527 .
                    turbo:bbencid1 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid1 obo:IAO_0000219 turbo:bbenc1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencsymb1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencregden1 .
                    turbo:bbencsymb1 a turbo:TURBO_0000534 .
                    turbo:bbencregden1 a turbo:TURBO_0000535 .
                    turbo:bbencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden1 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden1 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate1 a turbo:TURBO_0000532 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000531 .
                		turbo:encstart1 obo:RO_0002223 turbo:bbenc1 .
                		
                		turbo:bbenc2 a turbo:TURBO_0000527 .
                    turbo:bbencid2 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid2 obo:IAO_0000219 turbo:bbenc2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencsymb2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencregden2 .
                    turbo:bbencsymb2 a turbo:TURBO_0000534 .
                    turbo:bbencregden2 a turbo:TURBO_0000535 .
                    turbo:bbencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden2 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden2 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate2 a turbo:TURBO_0000532 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000531 .
                		turbo:encstart2 obo:RO_0002223 turbo:bbenc2 .
                		
                		turbo:bbenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		turbo:bbencid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackBiobankEncounters(cxn)
        
        val check1: String = """ 
             ASK {
                 turbo:bbenc1 graphBuilder:willBeCombinedWith turbo:bbenc2 .
                 turbo:bbenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 . 
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
    
    test("sparql hc enc referent track preprocess one non ref tracked one ref tracked same id and reg")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:hcenc1 a obo:OGMS_0000097 .
                    turbo:hcencid1 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid1 obo:IAO_0000219 turbo:hcenc1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencsymb1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencregden1 .
                    turbo:hcencsymb1 a turbo:TURBO_0000509 .
                    turbo:hcencregden1 a turbo:TURBO_0000510 .
                    turbo:hcencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden1 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden1 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate1 a turbo:TURBO_0000512 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000511 .
                		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
                		
                		turbo:hcenc2 a obo:OGMS_0000097 .
                    turbo:hcencid2 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid2 obo:IAO_0000219 turbo:hcenc2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencsymb2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencregden2 .
                    turbo:hcencsymb2 a turbo:TURBO_0000509 .
                    turbo:hcencregden2 a turbo:TURBO_0000510 .
                    turbo:hcencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden2 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden2 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate2 a turbo:TURBO_0000512 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000511 .
                		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
                		
                		turbo:hcenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		turbo:hcencid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackHealthcareEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:hcenc1 graphBuilder:willBeCombinedWith turbo:hcenc2 .
                 turbo:hcenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
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
    
    test("bb encs two non reftracked same reg same id diff date")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:bbenc1 a turbo:TURBO_0000527 .
                    turbo:bbencid1 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid1 obo:IAO_0000219 turbo:bbenc1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencsymb1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencregden1 .
                    turbo:bbencsymb1 a turbo:TURBO_0000534 .
                    turbo:bbencregden1 a turbo:TURBO_0000535 .
                    turbo:bbencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden1 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden1 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate1 a turbo:TURBO_0000532 .
                		turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000531 .
                		turbo:encstart1 obo:RO_0002223 turbo:bbenc1 .
            		
            		    turbo:bbenc2 a turbo:TURBO_0000527 .
                    turbo:bbencid2 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid2 obo:IAO_0000219 turbo:bbenc2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencsymb2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencregden2 .
                    turbo:bbencsymb2 a turbo:TURBO_0000534 .
                    turbo:bbencregden2 a turbo:TURBO_0000535 .
                    turbo:bbencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden2 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden2 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate2 a turbo:TURBO_0000532 .
                		turbo:encdate2 turbo:TURBO_0006511 "1970-08-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000531 .
                		turbo:encstart2 obo:RO_0002223 turbo:bbenc2 .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackBiobankEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:bbenc1 graphBuilder:willBeCombinedWith ?o .
                 turbo:bbenc2 graphBuilder:willBeCombinedWith ?o .
                 turbo:bbenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 turbo:bbenc2 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
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
        check2results.size should be (2)
    }
    
    test("hc encs two non reftracked same reg same id diff date")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:hcenc1 a obo:OGMS_0000097 .
                    turbo:hcencid1 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid1 obo:IAO_0000219 turbo:hcenc1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencsymb1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencregden1 .
                    turbo:hcencsymb1 a turbo:TURBO_0000509 .
                    turbo:hcencregden1 a turbo:TURBO_0000510 .
                    turbo:hcencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden1 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden1 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate1 a turbo:TURBO_0000512 .
                		turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000511 .
                		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
                		
                		turbo:hcenc2 a obo:OGMS_0000097 .
                    turbo:hcencid2 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid2 obo:IAO_0000219 turbo:hcenc2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencsymb2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencregden2 .
                    turbo:hcencsymb2 a turbo:TURBO_0000509 .
                    turbo:hcencregden2 a turbo:TURBO_0000510 .
                    turbo:hcencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden2 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden2 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate2 a turbo:TURBO_0000512 .
                		turbo:encdate2 turbo:TURBO_0006511 "1970-08-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000511 .
                		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
            		
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackHealthcareEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:hcenc1 graphBuilder:willBeCombinedWith ?o .
                 turbo:hcenc2 graphBuilder:willBeCombinedWith ?o .
                 turbo:hcenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 turbo:hcenc2 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
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
        check2results.size should be (2)
    }
    
    test("bb encs two non reftracked same reg diff id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:bbenc1 a turbo:TURBO_0000527 .
                    turbo:bbencid1 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid1 obo:IAO_0000219 turbo:bbenc1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencsymb1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencregden1 .
                    turbo:bbencsymb1 a turbo:TURBO_0000534 .
                    turbo:bbencregden1 a turbo:TURBO_0000535 .
                    turbo:bbencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden1 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden1 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate1 a turbo:TURBO_0000532 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000531 .
                		turbo:encstart1 obo:RO_0002223 turbo:bbenc1 .
                		
                		turbo:bbenc2 a turbo:TURBO_0000527 .
                    turbo:bbencid2 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid2 obo:IAO_0000219 turbo:bbenc2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencsymb2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencregden2 .
                    turbo:bbencsymb2 a turbo:TURBO_0000534 .
                    turbo:bbencregden2 a turbo:TURBO_0000535 .
                    turbo:bbencsymb2 turbo:TURBO_0006510 '2' .
                    turbo:bbencregden2 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden2 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate2 a turbo:TURBO_0000532 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000531 .
                		turbo:encstart2 obo:RO_0002223 turbo:bbenc2 .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackBiobankEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:bbenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:bbenc2 graphBuilder:willBeCombinedWith ?o2 .
                 turbo:bbenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 turbo:bbenc2 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 FILTER (?o1 != ?o2)
                 FILTER (?o1 != turbo:bbenc1)
                 FILTER (?o2 != turbo:bbenc2)
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
        check2results.size should be (2)
    }
    
    test("hc encs two non reftracked same reg diff id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:hcenc1 a obo:OGMS_0000097 .
                    turbo:hcencid1 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid1 obo:IAO_0000219 turbo:hcenc1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencsymb1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencregden1 .
                    turbo:hcencsymb1 a turbo:TURBO_0000509 .
                    turbo:hcencregden1 a turbo:TURBO_0000510 .
                    turbo:hcencsymb1 turbo:TURBO_0006510 '2' .
                    turbo:hcencregden1 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden1 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate1 a turbo:TURBO_0000512 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000511 .
                		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
                		
                		turbo:hcenc2 a obo:OGMS_0000097 .
                    turbo:hcencid2 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid2 obo:IAO_0000219 turbo:hcenc2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencsymb2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencregden2 .
                    turbo:hcencsymb2 a turbo:TURBO_0000509 .
                    turbo:hcencregden2 a turbo:TURBO_0000510 .
                    turbo:hcencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden2 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden2 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate2 a turbo:TURBO_0000512 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000511 .
                		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackHealthcareEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:hcenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:hcenc2 graphBuilder:willBeCombinedWith ?o2 .
                 turbo:hcenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 turbo:hcenc2 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 FILTER (?o1 != ?o2)
                 FILTER (?o1 != turbo:hcenc1)
                 FILTER (?o2 != turbo:hcenc2)
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
        check2results.size should be (2)
    }
    
    test("bb encs two non reftracked diff reg same id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:bbenc1 a turbo:TURBO_0000527 .
                    turbo:bbencid1 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid1 obo:IAO_0000219 turbo:bbenc1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencsymb1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencregden1 .
                    turbo:bbencsymb1 a turbo:TURBO_0000534 .
                    turbo:bbencregden1 a turbo:TURBO_0000535 .
                    turbo:bbencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden1 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden1 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate1 a turbo:TURBO_0000532 .
            		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-07"^^xsd:date .
            		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
            		turbo:encstart1 a turbo:TURBO_0000531 .
            		turbo:encstart1 obo:RO_0002223 turbo:bbenc1 .
            		
            		turbo:bbenc2 a turbo:TURBO_0000527 .
                    turbo:bbencid2 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid2 obo:IAO_0000219 turbo:bbenc2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencsymb2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencregden2 .
                    turbo:bbencsymb2 a turbo:TURBO_0000534 .
                    turbo:bbencregden2 a turbo:TURBO_0000535 .
                    turbo:bbencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden2 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden2 obo:IAO_0000219 turbo:bbencregid2 .
                    turbo:bbencregid2 a turbo:TURBO_0000543 .
                    turbo:encdate2 a turbo:TURBO_0000532 .
            		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
            		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
            		turbo:encstart2 a turbo:TURBO_0000531 .
            		turbo:encstart2 obo:RO_0002223 turbo:bbenc2 .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackBiobankEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:bbenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:bbenc2 graphBuilder:willBeCombinedWith ?o2 .
                 turbo:bbenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 turbo:bbenc2 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 FILTER (?o1 != ?o2)
                 FILTER (?o1 != turbo:bbenc1)
                 FILTER (?o2 != turbo:bbenc2)
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
        check2results.size should be (2)
    }
    
    test("hc encs two non reftracked diff reg same id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:hcenc1 a obo:OGMS_0000097 .
                    turbo:hcencid1 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid1 obo:IAO_0000219 turbo:hcenc1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencsymb1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencregden1 .
                    turbo:hcencsymb1 a turbo:TURBO_0000509 .
                    turbo:hcencregden1 a turbo:TURBO_0000510 .
                    turbo:hcencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden1 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden1 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate1 a turbo:TURBO_0000512 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-07"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000511 .
                		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
                		
                		turbo:hcenc2 a obo:OGMS_0000097 .
                    turbo:hcencid2 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid2 obo:IAO_0000219 turbo:hcenc2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencsymb2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencregden2 .
                    turbo:hcencsymb2 a turbo:TURBO_0000509 .
                    turbo:hcencregden2 a turbo:TURBO_0000510 .
                    turbo:hcencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden2 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden2 obo:IAO_0000219 turbo:hcencregid2 .
                    turbo:hcencregid2 a turbo:TURBO_0000513 .
                    turbo:encdate2 a turbo:TURBO_0000512 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000511 .
                		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackHealthcareEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:hcenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:hcenc2 graphBuilder:willBeCombinedWith ?o2 .
                 turbo:hcenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 turbo:hcenc2 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 FILTER (?o1 != ?o2)
                 FILTER (?o1 != turbo:hcenc1)
                 FILTER (?o2 != turbo:hcenc2)
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
        check2results.size should be (2)
    }
    
    test("bb encs one non reftracked one reftracked diff reg same id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:bbenc1 a turbo:TURBO_0000527 .
                    turbo:bbencid1 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid1 obo:IAO_0000219 turbo:bbenc1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencsymb1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencregden1 .
                    turbo:bbencsymb1 a turbo:TURBO_0000534 .
                    turbo:bbencregden1 a turbo:TURBO_0000535 .
                    turbo:bbencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden1 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden1 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate1 a turbo:TURBO_0000532 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-07"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000531 .
                		turbo:encstart1 obo:RO_0002223 turbo:bbenc1 .
            		
            		    turbo:bbenc2 a turbo:TURBO_0000527 .
                    turbo:bbencid2 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid2 obo:IAO_0000219 turbo:bbenc2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencsymb2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencregden2 .
                    turbo:bbencsymb2 a turbo:TURBO_0000534 .
                    turbo:bbencregden2 a turbo:TURBO_0000535 .
                    turbo:bbencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden2 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden2 obo:IAO_0000219 turbo:bbencregid2 .
                    turbo:bbencregid2 a turbo:TURBO_0000543 .
                    turbo:inst2 a turbo:TURBO_0000522 .
                    turbo:inst2 obo:OBI_0000293 turbo:dataset2 .
                    turbo:dataset2 a obo:IAO_0000100 .
                    turbo:encdate2 a turbo:TURBO_0000532 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000531 .
                		turbo:encstart2 obo:RO_0002223 turbo:bbenc2 .
                		
                		turbo:bbenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		turbo:bbencid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackBiobankEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:bbenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:bbenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 FILTER (?o1 != turbo:bbenc2)
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
    
    test("hc encs one non reftracked one reftracked diff reg same id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:hcenc1 a obo:OGMS_0000097 .
                    turbo:hcencid1 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid1 obo:IAO_0000219 turbo:hcenc1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencsymb1 .
                    turbo:hcencid1 obo:BFO_0000051 turbo:hcencregden1 .
                    turbo:hcencsymb1 a turbo:TURBO_0000509 .
                    turbo:hcencregden1 a turbo:TURBO_0000510 .
                    turbo:hcencsymb1 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden1 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden1 obo:IAO_0000219 turbo:hcencregid1 .
                    turbo:hcencregid1 a turbo:TURBO_0000513 .
                    turbo:encdate1 a turbo:TURBO_0000512 .
            		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-07"^^xsd:date .
            		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
            		turbo:encstart1 a turbo:TURBO_0000511 .
            		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
            		
            		turbo:hcenc2 a obo:OGMS_0000097 .
                    turbo:hcencid2 rdf:type turbo:TURBO_0000508 .
                    turbo:hcencid2 obo:IAO_0000219 turbo:hcenc2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencsymb2 .
                    turbo:hcencid2 obo:BFO_0000051 turbo:hcencregden2 .
                    turbo:hcencsymb2 a turbo:TURBO_0000509 .
                    turbo:hcencregden2 a turbo:TURBO_0000510 .
                    turbo:hcencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:hcencregden2 turbo:TURBO_0006510 'inpatient' .
                    turbo:hcencregden2 obo:IAO_0000219 turbo:hcencregid2 .
                    turbo:hcencregid2 a turbo:TURBO_0000513 .
                    turbo:encdate2 a turbo:TURBO_0000512 .
            		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
            		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
            		turbo:encstart2 a turbo:TURBO_0000511 .
            		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
            		
            		turbo:hcenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            		turbo:hcencid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackHealthcareEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:hcenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:hcenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 FILTER (?o1 != turbo:hcenc2)
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
    
    test("bb encs one non reftracked one reftracked same reg diff id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                 GRAPH pmbb:expanded {
                    turbo:bbenc1 a turbo:TURBO_0000527 .
                    turbo:bbencid1 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid1 obo:IAO_0000219 turbo:bbenc1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencsymb1 .
                    turbo:bbencid1 obo:BFO_0000051 turbo:bbencregden1 .
                    turbo:bbencsymb1 a turbo:TURBO_0000534 .
                    turbo:bbencregden1 a turbo:TURBO_0000535 .
                    turbo:bbencsymb1 turbo:TURBO_0006510 '2' .
                    turbo:bbencregden1 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden1 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate1 a turbo:TURBO_0000532 .
                		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
                		turbo:encstart1 a turbo:TURBO_0000531 .
                		turbo:encstart1 obo:RO_0002223 turbo:bbenc1 .
                		
                		turbo:bbenc2 a turbo:TURBO_0000527 .
                    turbo:bbencid2 rdf:type turbo:TURBO_0000533 .
                    turbo:bbencid2 obo:IAO_0000219 turbo:bbenc2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencsymb2 .
                    turbo:bbencid2 obo:BFO_0000051 turbo:bbencregden2 .
                    turbo:bbencsymb2 a turbo:TURBO_0000534 .
                    turbo:bbencregden2 a turbo:TURBO_0000535 .
                    turbo:bbencsymb2 turbo:TURBO_0006510 '1' .
                    turbo:bbencregden2 turbo:TURBO_0006510 'biobank' .
                    turbo:bbencregden2 obo:IAO_0000219 turbo:bbencregid1 .
                    turbo:bbencregid1 a turbo:TURBO_0000543 .
                    turbo:encdate2 a turbo:TURBO_0000532 .
                		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
                		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
                		turbo:encstart2 a turbo:TURBO_0000531 .
                		turbo:encstart2 obo:RO_0002223 turbo:bbenc2 .
                		
                		turbo:bbenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		turbo:bbencid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackBiobankEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:bbenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:bbenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
                 FILTER (?o1 != turbo:bbenc2)
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
    
    test("hc encs one non reftracked one reftracked same reg diff id")
    {
        val insertString: String = """
             INSERT DATA 
             {
                GRAPH pmbb:expanded {
                turbo:hcenc1 a obo:OGMS_0000097 .
                turbo:hcencid1 rdf:type turbo:TURBO_0000508 .
                turbo:hcencid1 obo:IAO_0000219 turbo:hcenc1 .
                turbo:hcencid1 obo:BFO_0000051 turbo:hcencsymb1 .
                turbo:hcencid1 obo:BFO_0000051 turbo:hcencregden1 .
                turbo:hcencsymb1 a turbo:TURBO_0000509 .
                turbo:hcencregden1 a turbo:TURBO_0000510 .
                turbo:hcencsymb1 turbo:TURBO_0006510 '2' .
                turbo:hcencregden1 turbo:TURBO_0006510 'inpatient' .
                turbo:hcencregden1 obo:IAO_0000219 turbo:hcencregid1 .
                turbo:hcencregid1 a turbo:TURBO_0000513 .
                turbo:encdate1 a turbo:TURBO_0000512 .
            		# turbo:encdate1 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
            		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
            		turbo:encstart1 a turbo:TURBO_0000511 .
            		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
            		
            		turbo:hcenc2 a obo:OGMS_0000097 .
                turbo:hcencid2 rdf:type turbo:TURBO_0000508 .
                turbo:hcencid2 obo:IAO_0000219 turbo:hcenc2 .
                turbo:hcencid2 obo:BFO_0000051 turbo:hcencsymb2 .
                turbo:hcencid2 obo:BFO_0000051 turbo:hcencregden2 .
                turbo:hcencsymb2 a turbo:TURBO_0000509 .
                turbo:hcencregden2 a turbo:TURBO_0000510 .
                turbo:hcencsymb2 turbo:TURBO_0006510 '1' .
                turbo:hcencregden2 turbo:TURBO_0006510 'inpatient' .
                turbo:hcencregden2 obo:IAO_0000219 turbo:hcencregid1 .
                turbo:hcencregid1 a turbo:TURBO_0000513 .
                turbo:encdate2 a turbo:TURBO_0000512 .
            		# turbo:encdate2 turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
            		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
            		turbo:encstart2 a turbo:TURBO_0000511 .
            		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
            		
            		turbo:hcenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            		turbo:hcencid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            	}
             }
          """
        update.updateSparql(cxn, sparqlPrefixes + insertString)
        encreftrack.reftrackHealthcareEncounters(cxn)
        
        val check1: String = """
             ASK {
                 turbo:hcenc1 graphBuilder:willBeCombinedWith ?o1 .
                 turbo:hcenc1 graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
                 FILTER (?o1 != turbo:hcenc2)
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
}