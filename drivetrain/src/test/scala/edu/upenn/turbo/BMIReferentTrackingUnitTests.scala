package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class BMIReferentTrackingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
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
    
    val twoHcBMICombinedToOne: String = """
      ASK 
      {
          Graph pmbb:expanded {
            pmbb:hcBMI1 graphBuilder:willBeCombinedWith ?dest .
            pmbb:hcBMI1 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
            pmbb:hcBMI2 graphBuilder:willBeCombinedWith ?dest .
            pmbb:hcBMI2 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
      }}
      """
    
    val twoHcBMINotCombined: String = """
      ASK 
      {
          Graph pmbb:expanded {
            pmbb:hcBMI1 graphBuilder:willBeCombinedWith ?dest1 .
            pmbb:hcBMI1 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
            pmbb:hcBMI2 graphBuilder:willBeCombinedWith ?dest2 .
            pmbb:hcBMI2 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
            Filter (?dest1 != ?dest2)
      }}
      """
    
    val twoBbBMICombinedToOne: String = """
      ASK 
      {
          Graph pmbb:expanded {
            pmbb:bbBMI1 graphBuilder:willBeCombinedWith ?dest .
            pmbb:bbBMI1 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
            pmbb:bbBMI2 graphBuilder:willBeCombinedWith ?dest .
            pmbb:bbBMI2 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
      }}
      """
    
    val twoBbBMINotCombined: String = """
      ASK 
      {
          Graph pmbb:expanded {
            pmbb:bbBMI1 graphBuilder:willBeCombinedWith ?dest1 .
            pmbb:bbBMI1 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
            pmbb:bbBMI2 graphBuilder:willBeCombinedWith ?dest2 .
            pmbb:bbBMI2 graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
            Filter (?dest1 != ?dest2)
      }}
      """
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
        val insert: String = 
          """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 .
                  pmbb:hcenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcDate1 a turbo:TURBO_0000512 .
                  pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcDate2 a turbo:TURBO_0000512 .
                  pmbb:hcDate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  pmbb:hcEncStart1 obo:RO_0002223 pmbb:hcenc1 .
                  pmbb:hcEncStart1 a turbo:TURBO_0000511 .
                  pmbb:hcEncStart1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcDate1 obo:IAO_0000136 pmbb:hcEncStart1 .
                  pmbb:hcDate2 obo:IAO_0000136 pmbb:hcEncStart1 .
                  
                  pmbb:hcenc1 obo:RO_0002234 pmbb:hcBMI1 .
                  pmbb:hcBMI1 a efo:EFO_0004340 .
                  pmbb:hcBMI1 obo:OBI_0001938 pmbb:hcBMIValSpec1 .
                  pmbb:hcBMIValSpec1 a obo:OBI_0001933 .
                  
                  pmbb:hcenc1 obo:RO_0002234 pmbb:hcBMI2 .
                  pmbb:hcBMI2 a efo:EFO_0004340 .
                  pmbb:hcBMI2 obo:OBI_0001938 pmbb:hcBMIValSpec2 .
                  pmbb:hcBMIValSpec2 a obo:OBI_0001933 .
                  
                  
                  pmbb:bbenc1 a turbo:TURBO_0000527 .
                  pmbb:bbenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbDate1 a turbo:TURBO_0000532 .
                  pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbDate2 a turbo:TURBO_0000532 .
                  pmbb:bbDate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbenc1 .
                  pmbb:bbEncStart1 a turbo:TURBO_0000531 .
                  pmbb:bbEncStart1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
                  pmbb:bbDate2 obo:IAO_0000136 pmbb:bbEncStart1 .
                  
                  pmbb:bbenc1 obo:OBI_0000299 pmbb:bbBMI1 .
                  pmbb:bbBMI1 a efo:EFO_0004340 .
                  pmbb:bbBMI1 obo:OBI_0001938 pmbb:bbBMIValSpec1 .
                  pmbb:bbBMIValSpec1 a obo:OBI_0001933 .
                  
                  pmbb:bbenc1 obo:OBI_0000299 pmbb:bbBMI2 .
                  pmbb:bbBMI2 a efo:EFO_0004340 .
                  pmbb:bbBMI2 obo:OBI_0001938 pmbb:bbBMIValSpec2 .
                  pmbb:bbBMIValSpec2 a obo:OBI_0001933 .
              }
          }
          """
        
        update.updateSparql(cxn, sparqlPrefixes + insert)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("bmi ref tracking from healthcare enc - 2 bmis same date same values")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded {
              pmbb:hcBMIValSpec1 obo:OBI_0002135 '20' .
              pmbb:hcBMI1 obo:IAO_0000581 pmbb:hcDate1 .
              pmbb:hcDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
              
              pmbb:hcBMIValSpec2 obo:OBI_0002135 '20' .
              pmbb:hcBMI2 obo:IAO_0000581 pmbb:hcDate1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackHealthcareBMI(cxn)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoHcBMICombinedToOne).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoHcBMINotCombined).get should be (false)
    }
    
    test("bmi ref tracking from biobank enc - 2 bmis same date same values")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded {
              pmbb:bbBMIValSpec1 obo:OBI_0002135 '20' .
              pmbb:bbBMI1 obo:IAO_0000581 pmbb:bbDate1 .
              pmbb:bbDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
              
              pmbb:bbBMIValSpec2 obo:OBI_0002135 '20' .
              pmbb:bbBMI2 obo:IAO_0000581 pmbb:bbDate1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackBiobankBMI(cxn)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoBbBMICombinedToOne).get should be (true)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoBbBMINotCombined).get should be (false)
    }
    
    test("bmi ref tracking from healthcare enc - 2 bmis same values diff date")
    {
        false should be (true)
    }
    
    test("bmi ref tracking from biobank enc - 2 bmis same values diff date")
    {
        false should be (true)
    }
    
    test("bmi ref tracking from healthcare enc - 2 bmis same date diff values")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded {
              pmbb:hcBMIValSpec1 obo:OBI_0002135 '20' .
              pmbb:hcBMI1 obo:IAO_0000581 pmbb:hcDate1 .
              pmbb:hcDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
              
              pmbb:hcBMIValSpec2 obo:OBI_0002135 '30' .
              pmbb:hcBMI2 obo:IAO_0000581 pmbb:hcDate1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackHealthcareBMI(cxn)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoHcBMICombinedToOne).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoHcBMINotCombined).get should be (true)
    }
    
    test("bmi ref tracking from biobank enc - 2 bmis same date diff values")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded {
              pmbb:bbBMIValSpec1 obo:OBI_0002135 '20' .
              pmbb:bbBMI1 obo:IAO_0000581 pmbb:bbDate1 .
              pmbb:bbDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
              
              pmbb:bbBMIValSpec2 obo:OBI_0002135 '30' .
              pmbb:bbBMI2 obo:IAO_0000581 pmbb:bbDate1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackBiobankBMI(cxn)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoBbBMICombinedToOne).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoBbBMINotCombined).get should be (true)
    }
    
    test("bmi ref tracking from healthcare enc - 2 bmis same value missing date")
    {
        false should be (true)
    }
    
    test("bmi ref tracking from biobank enc - 2 bmis same value missing date")
    {
        false should be (true)
    }
    
    test("bmi ref tracking from healthcare enc - 2 bmis same value same date diff encounters")
    {
        helper.deleteAllTriplesInDatabase(cxn)
        val insert: String = """
        INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:hcenc1 a obo:OGMS_0000097 .
                  pmbb:hcenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcenc2 a obo:OGMS_0000097 .
                  pmbb:hcenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcDate1 a turbo:TURBO_0000512 .
                  pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcDate2 a turbo:TURBO_0000512 .
                  pmbb:hcDate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  pmbb:hcEncStart1 obo:RO_0002223 pmbb:hcenc1 .
                  pmbb:hcEncStart1 a turbo:TURBO_0000511 .
                  pmbb:hcEncStart1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcEncStart2 obo:RO_0002223 pmbb:hcenc2 .
                  pmbb:hcEncStart2 a turbo:TURBO_0000511 .
                  pmbb:hcEncStart2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:hcDate1 obo:IAO_0000136 pmbb:hcEncStart1 .
                  pmbb:hcDate2 obo:IAO_0000136 pmbb:hcEncStart2 .
                  
                  pmbb:hcenc1 obo:RO_0002234 pmbb:hcBMI1 .
                  pmbb:hcBMI1 a efo:EFO_0004340 .
                  pmbb:hcBMI1 obo:OBI_0001938 pmbb:hcBMIValSpec1 .
                  pmbb:hcBMIValSpec1 a obo:OBI_0001933 .
                  
                  pmbb:hcenc2 obo:RO_0002234 pmbb:hcBMI2 .
                  pmbb:hcBMI2 a efo:EFO_0004340 .
                  pmbb:hcBMI2 obo:OBI_0001938 pmbb:hcBMIValSpec2 .
                  pmbb:hcBMIValSpec2 a obo:OBI_0001933 .
                  
                  pmbb:hcBMIValSpec1 obo:OBI_0002135 '20' .
                  pmbb:hcBMI1 obo:IAO_0000581 pmbb:hcDate1 .
                  pmbb:hcDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
                  
                  pmbb:hcBMIValSpec2 obo:OBI_0002135 '20' .
                  pmbb:hcBMI2 obo:IAO_0000581 pmbb:hcDate2 .
                  pmbb:hcDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
                  pmbb:hcDate2 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
                  }}"""
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackHealthcareBMI(cxn)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoHcBMICombinedToOne).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoHcBMINotCombined).get should be (true)
    }
    
    test("bmi ref tracking from biobank enc - 2 bmis same value same date diff encounters")
    {
        helper.deleteAllTriplesInDatabase(cxn)
        val insert: String = """
          INSERT DATA {
            graph pmbb:expanded {
                  pmbb:bbenc1 a turbo:TURBO_0000527 .
                  pmbb:bbenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbenc2 a turbo:TURBO_0000527 .
                  pmbb:bbenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbDate1 a turbo:TURBO_0000532 .
                  pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbDate2 a turbo:TURBO_0000532 .
                  pmbb:bbDate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  
                  pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbenc1 .
                  pmbb:bbEncStart1 a turbo:TURBO_0000531 .
                  pmbb:bbEncStart1 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbEncStart2 obo:RO_0002223 pmbb:bbenc2 .
                  pmbb:bbEncStart2 a turbo:TURBO_0000531 .
                  pmbb:bbEncStart2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  pmbb:bbDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
                  pmbb:bbDate2 obo:IAO_0000136 pmbb:bbEncStart2 .
                  
                  pmbb:bbenc1 obo:OBI_0000299 pmbb:bbBMI1 .
                  pmbb:bbBMI1 a efo:EFO_0004340 .
                  pmbb:bbBMI1 obo:OBI_0001938 pmbb:bbBMIValSpec1 .
                  pmbb:bbBMIValSpec1 a obo:OBI_0001933 .
                  
                  pmbb:bbenc2 obo:OBI_0000299 pmbb:bbBMI2 .
                  pmbb:bbBMI2 a efo:EFO_0004340 .
                  pmbb:bbBMI2 obo:OBI_0001938 pmbb:bbBMIValSpec2 .
                  pmbb:bbBMIValSpec2 a obo:OBI_0001933 .
                  
                  pmbb:bbBMIValSpec1 obo:OBI_0002135 '20' .
                  pmbb:bbBMI1 obo:IAO_0000581 pmbb:bbDate1 .
                  pmbb:bbDate1 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
                  
                  pmbb:bbBMIValSpec2 obo:OBI_0002135 '20' .
                  pmbb:bbBMI2 obo:IAO_0000581 pmbb:bbDate2 .
                  pmbb:bbDate2 turbo:TURBO_0006511 '03/09/10'^^xsd:boolean .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackBiobankBMI(cxn)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoBbBMICombinedToOne).get should be (false)
        update.querySparqlBoolean(cxn, sparqlPrefixes + twoBbBMINotCombined).get should be (true)
    }
    
    test("bmi val spec ref tracking - 2 val specs combined to 1")
    {
        helper.deleteAllTriplesInDatabase(cxn)
        val insert: String = """
          INSERT DATA { 
            GRAPH pmbb:expanded {
              pmbb:bmi1 a efo:EFO_0004340 .
              pmbb:bmi1 obo:OBI_0001938 pmbb:valspec1 .
              pmbb:bmi1 obo:OBI_0001938 pmbb:valspec2 .
              pmbb:bmi1 turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:valspec1 a obo:OBI_0001933 .
              pmbb:valspec2 a obo:OBI_0001933 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackBMIValSpecs(cxn)
        
        val ask: String = """
          ASK
          {
             Graph pmbb:expanded {   
                pmbb:valspec1 graphBuilder:willBeCombinedWith ?dest .
                pmbb:valspec1 graphBuilder:placeholderDemotionType turbo:TURBO_0001904 .
                pmbb:valspec2 graphBuilder:willBeCombinedWith ?dest .
                pmbb:valspec2 graphBuilder:placeholderDemotionType turbo:TURBO_0001904 .
          }}
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("handle 2 bmis from one hc encounter tied to multiple dates")
    {
        false should be (true)
    }
    
    test("handle 2 bmis from one bb encounter tied to multiple dates")
    {
        false should be (true)
    }
}