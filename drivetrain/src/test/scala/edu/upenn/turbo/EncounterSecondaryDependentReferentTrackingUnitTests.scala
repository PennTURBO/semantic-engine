package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class EncounterSecondaryDependentReferentTrackingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
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
    
    val healthcareSecondaryReftrackCombineDate: String = """
          ASK {GRAPH pmbb:expanded {
          pmbb:hcDate1 graphBuilder:willBeCombinedWith ?encDateDestination .
          pmbb:hcDate1 graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
          pmbb:hcDate2 graphBuilder:willBeCombinedWith ?encDateDestination .
          pmbb:hcDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
          pmbb:hcSymb1 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:hcSymb1 graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
          pmbb:hcSymb2 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:hcSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
          pmbb:hcRegDen1 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:hcRegDen1 graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
          pmbb:hcRegDen2 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:hcRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
          }}
          """
    
    val healthcareSecondaryReftrackNoCombineDate: String = """
      ASK {GRAPH pmbb:expanded {
          pmbb:hcDate1 graphBuilder:willBeCombinedWith ?encDateDestination1 .
          pmbb:hcDate1 graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
          pmbb:hcDate2 graphBuilder:willBeCombinedWith ?encDateDestination2 .
          pmbb:hcDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
          pmbb:hcSymb1 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:hcSymb1 graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
          pmbb:hcSymb2 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:hcSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
          pmbb:hcRegDen1 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:hcRegDen1 graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
          pmbb:hcRegDen2 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:hcRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
          }
          Filter (?encDateDestination1 != ?encDateDestination2)
          }
          """
    
    val biobankSecondaryReftrackCombineDate: String = """
          ASK {GRAPH pmbb:expanded {
          pmbb:bbDate1 graphBuilder:willBeCombinedWith ?encDateDestination .
          pmbb:bbDate1 graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
          pmbb:bbDate2 graphBuilder:willBeCombinedWith ?encDateDestination .
          pmbb:bbDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
          pmbb:bbSymb1 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:bbSymb1 graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
          pmbb:bbSymb2 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:bbSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
          pmbb:bbRegDen1 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:bbRegDen1 graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
          pmbb:bbRegDen2 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:bbRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
          }}
          """
    
    val biobankSecondaryReftrackNoCombineDate: String = """
      ASK {GRAPH pmbb:expanded {
          pmbb:bbDate1 graphBuilder:willBeCombinedWith ?encDateDestination1 .
          pmbb:bbDate1 graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
          pmbb:bbDate2 graphBuilder:willBeCombinedWith ?encDateDestination2 .
          pmbb:bbDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
          pmbb:bbSymb1 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:bbSymb1 graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
          pmbb:bbSymb2 graphBuilder:willBeCombinedWith ?encSymbDestination .
          pmbb:bbSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
          pmbb:bbRegDen1 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:bbRegDen1 graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
          pmbb:bbRegDen2 graphBuilder:willBeCombinedWith ?encRegDenDestination .
          pmbb:bbRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
          }
          Filter (?encDateDestination1 != ?encDateDestination2)
          }
          """
    
        val healthcareSecondaryReftrackCombineDateToPre: String = """
          ASK {GRAPH pmbb:expanded {
          pmbb:hcDate2 graphBuilder:willBeCombinedWith pmbb:hcDate1 .
          pmbb:hcDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
          pmbb:hcSymb2 graphBuilder:willBeCombinedWith pmbb:hcSymb1 .
          pmbb:hcSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
          pmbb:hcRegDen2 graphBuilder:willBeCombinedWith pmbb:hcRegDen1 .
          pmbb:hcRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
          }}
          """
    
    val healthcareSecondaryReftrackNoCombineDateToPre: String = """
      ASK {GRAPH pmbb:expanded {
          pmbb:hcDate2 graphBuilder:willBeCombinedWith ?destination .
          pmbb:hcDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
          pmbb:hcSymb2 graphBuilder:willBeCombinedWith pmbb:hcSymb1 .
          pmbb:hcSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
          pmbb:hcRegDen2 graphBuilder:willBeCombinedWith pmbb:hcRegDen1 .
          pmbb:hcRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
          }
          Filter (?destination != pmbb:hcDate1)
          }
          """
    
    val biobankSecondaryReftrackCombineDateToPre: String = """
          ASK {GRAPH pmbb:expanded {
          pmbb:bbDate2 graphBuilder:willBeCombinedWith pmbb:bbDate1 .
          pmbb:bbDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
          pmbb:bbSymb2 graphBuilder:willBeCombinedWith pmbb:bbSymb1 .
          pmbb:bbSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
          pmbb:bbRegDen2 graphBuilder:willBeCombinedWith pmbb:bbRegDen1 .
          pmbb:bbRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
          }}
          """
    
    val biobankSecondaryReftrackNoCombineDateToPre: String = """
      ASK {GRAPH pmbb:expanded {
          pmbb:bbDate2 graphBuilder:willBeCombinedWith ?destination .
          pmbb:bbDate2 graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
          pmbb:bbSymb2 graphBuilder:willBeCombinedWith pmbb:bbSymb1 .
          pmbb:bbSymb2 graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
          pmbb:bbRegDen2 graphBuilder:willBeCombinedWith pmbb:bbRegDen1 .
          pmbb:bbRegDen2 graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
          }
          Filter (?destination != pmbb:bbDate1)
          }
          """
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
        val insertBiobankAndHealthcareData: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
            
              pmbb:bbenc1 a turbo:TURBO_0000527 .
              
              pmbb:bbStart1 a turbo:TURBO_0000531 .
              pmbb:bbDate1 obo:IAO_0000136 pmbb:bbStart1 .
          		pmbb:bbDate1 a turbo:TURBO_0000532 .
          		pmbb:bbStart1 obo:RO_0002223 pmbb:bbenc1 .
          		
              pmbb:bbDate2 obo:IAO_0000136 pmbb:bbStart1 .
          		pmbb:bbDate2 a turbo:TURBO_0000532 .
          		
          		pmbb:bbCrid1 obo:IAO_0000219 pmbb:bbenc1 .
          		
          		pmbb:bbCrid1 a turbo:TURBO_0000533 .
          		pmbb:bbCrid1 obo:BFO_0000051 pmbb:bbSymb1 .
          		pmbb:bbCrid1 obo:BFO_0000051 pmbb:bbRegDen1 .
          		pmbb:bbSymb1 a turbo:TURBO_0000534 .
          		pmbb:bbRegDen1 a turbo:TURBO_0000535 .
          		
          		pmbb:bbCrid1 obo:BFO_0000051 pmbb:bbSymb2 .
          		pmbb:bbCrid1 obo:BFO_0000051 pmbb:bbRegDen2 .
          		pmbb:bbSymb2 a turbo:TURBO_0000534 .
          		pmbb:bbRegDen2 a turbo:TURBO_0000535 .
          		
          		pmbb:bbCrid1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbStart1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		
          		pmbb:hcenc1 a obo:OGMS_0000097 .
              
              pmbb:hcStart1 a turbo:TURBO_0000511 .
              pmbb:hcDate1 obo:IAO_0000136 pmbb:hcStart1 .
          		pmbb:hcDate1 a turbo:TURBO_0000512 .
          		pmbb:hcStart1 obo:RO_0002223 pmbb:hcenc1 .
          		
              pmbb:hcDate2 obo:IAO_0000136 pmbb:hcStart1 .
          		pmbb:hcDate2 a turbo:TURBO_0000512 .
          		
          		pmbb:hcCrid1 obo:IAO_0000219 pmbb:hcenc1 .
          		
          		pmbb:hcCrid1 a turbo:TURBO_0000508 .
          		pmbb:hcCrid1 obo:BFO_0000051 pmbb:hcSymb1 .
          		pmbb:hcCrid1 obo:BFO_0000051 pmbb:hcRegDen1 .
          		pmbb:hcSymb1 a turbo:TURBO_0000509 .
          		pmbb:hcRegDen1 a turbo:TURBO_0000510 .
          		
          		pmbb:hcCrid1 obo:BFO_0000051 pmbb:hcSymb2 .
          		pmbb:hcCrid1 obo:BFO_0000051 pmbb:hcRegDen2 .
          		pmbb:hcSymb2 a turbo:TURBO_0000509 .
          		pmbb:hcRegDen2 a turbo:TURBO_0000510 .
          		
          		pmbb:hcCrid1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcStart1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insertBiobankAndHealthcareData)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("hc encs secondary dependent ref tracking - two non ref tracked same dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
              pmbb:hcDate1 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
              pmbb:hcDate2 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - two non reftracked same dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
                pmbb:bbDate1 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
                pmbb:bbDate2 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("hc encs secondary dependent ref tracking - two non reftracked diff dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
                  pmbb:hcDate1 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
                  pmbb:hcDate2 turbo:TURBO_0006511 "03/10/09"^^xsd:date .
              }
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - two non reftracked diff dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
                pmbb:bbDate1 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
                pmbb:bbDate2 turbo:TURBO_0006511 "03/10/09"^^xsd:date .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("hc encs secondary dependent ref tracking - two non reftracked no dates")
    {
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - two non reftracked no dates")
    {
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    //I may be ok with these tests failing for now
    test("hc encs secondary dependent ref tracking - two non reftracked one date present one missing")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
                pmbb:hcDate1 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - two non reftracked one date present one missing")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
                pmbb:bbDate1 turbo:TURBO_0006511 "03/09/09"^^xsd:date .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
    }
    
    test("hc encs secondary dependent ref tracking - one ref tracked one non reftracked same dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
            
              pmbb:hcDate1 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
              pmbb:hcDate2 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
          		
          		pmbb:hcSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - one ref tracked one non reftracked same dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
              pmbb:bbDate1 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
              pmbb:bbDate2 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
          		
          		pmbb:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
    }
    
    test("hc encs secondary dependent ref tracking - one ref tracked one non reftracked diff dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
            
              pmbb:hcDate1 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
              pmbb:hcDate2 turbo:TURBO_0006511 "03/10/09"^^xsd:date .
          		
          		pmbb:hcSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (true)
    }
    
    test("bb encs secondary dependent ref tracking - one ref tracked one non reftracked diff dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
              pmbb:bbDate1 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
              pmbb:bbDate2 turbo:TURBO_0006511 "03/10/09"^^xsd:date .
          		
          		pmbb:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
    }
    
    //I may be ok with these tests failing for now
    test("hc encs secondary dependent ref tracking - one ref tracked one non reftracked only reftracked date present")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
            
              pmbb:hcDate1 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
          		
          		pmbb:hcSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("hc encs secondary dependent ref tracking - one ref tracked one non reftracked only non reftracked date present")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
           
              pmbb:hcDate2 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
          		
          		pmbb:hcSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - one ref tracked one non reftracked only ref tracked date present")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
              pmbb:bbDate1 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
          		
          		pmbb:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - one ref tracked one non reftracked only non ref tracked date present")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
              pmbb:bbDate2 turbo:TURBO_0006511 "03/03/09"^^xsd:date .
          		
          		pmbb:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
    }
    
    test("hc encs secondary dependent ref tracking - one ref tracked one non reftracked no dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
          		pmbb:hcSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:hcDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryHealthcareEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + healthcareSecondaryReftrackNoCombineDateToPre).get should be (false)
    }
    
    test("bb encs secondary dependent ref tracking - one ref tracked one non reftracked no dates")
    {
        val insert: String = """
          INSERT DATA {
            GRAPH pmbb:expanded {
          		pmbb:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          		pmbb:bbDate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
          	}
         }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        encreftrack.reftrackSecondaryBiobankEncounterDependents(cxn)
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDateToPre).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDateToPre).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackCombineDate).get should be (false)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + biobankSecondaryReftrackNoCombineDate).get should be (false)
    }
}