package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._

class BMIConclusionationUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
    val bmiconc = new BMIConclusionator
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    // 21.0 is the value that should be used for BMIs linked to Healthcare encounters. 42.0 for Biobank encounters.
    
    val missingKnowledge: String = 
      """ASK {
          ?mk a obo:OBI_0000852 ;
              obo:IAO_0000136 turbo:bbenc1 .
          }"""
    
    val usedHealthcareBMI = """ASK {
                      ?concBMI a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                          turbo:TURBO_0006501 'true'^^xsd:boolean ;
                          obo:OBI_0001938 ?concbmivalspec ;
                          obo:IAO_0000136 turbo:adipose1 ;
                          obo:IAO_0000581 turbo:bbencdate1 .
                      ?concbmivalspec a obo:OBI_0001933 ;
                          obo:OBI_0002135 '21.0'^^xsd:float .
                      ?statement2 a rdf:Statement;
                          rdf:subject ?concbmivalspec ;
                          obo:OBI_0000124 turbo:hcbmi1 .
                          }"""
    
    val usedBiobankBMI = """ASK {
                      ?concBMI a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                          turbo:TURBO_0006501 'true'^^xsd:boolean ;
                          obo:OBI_0001938 ?concbmivalspec ;
                          obo:IAO_0000136 turbo:adipose1 ;
                          obo:IAO_0000581 turbo:bbencdate1 .
                      ?concbmivalspec a obo:OBI_0001933 ;
                          obo:OBI_0002135 '42.0'^^xsd:float .
                      ?statement2 a rdf:Statement;
                          rdf:subject ?concbmivalspec ;
                          obo:OBI_0000124 turbo:bbbmi1 .}"""
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        conclusionationNamedGraph = cxn.getValueFactory.createIRI("http://transformunify.org/ontologies/ConclusionatedNamedGraph_" + helper.getCurrentTimestamp("_"))
        masterConclusionation = helper.genTurboIRI(cxn)
        masterPlanspec = helper.genTurboIRI(cxn)
        masterPlan = helper.genTurboIRI(cxn)
        
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:part1 a turbo:TURBO_0000502 ;
                  obo:RO_0000056 turbo:bbenc1 ;
                  obo:RO_0000056 turbo:hcenc1 ;
                  obo:BFO_0000051 turbo:adipose1 .
              turbo:adipose1 a obo:UBERON_0001013 .
              turbo:bbenc1 a turbo:TURBO_0000527 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbencstart1 obo:RO_0002223 turbo:bbenc1 ;
                  a turbo:TURBO_0000531 .
              turbo:bbencdate1 obo:IAO_0000136 turbo:bbencstart1 ;
                  a turbo:TURBO_0000532 ;
                  turbo:TURBO_0006511 '1/1/15' .
              turbo:hcenc1 a obo:OGMS_0000097 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcencstart1 obo:RO_0002223 turbo:hcenc1 ;
                  a turbo:TURBO_0000511 .
              turbo:hcencdate1 obo:IAO_0000136 turbo:hcencstart1 ;
                  a turbo:TURBO_0000512 ;
                  turbo:TURBO_0006511 '1/1/15' .
              
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }

    test("hc and bb meas on same date diff values")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("no hc bb meas")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)       
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get)  
    }
    
    test("no hc no bb")
    {
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("hc and bb meas on same date hc out of range")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.2'^^xsd:float .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42'^^xsd:float .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("hc and bb meas on same date bb out of range")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.2' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("one hc with two bmis one bb diff values all same date")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
                  
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi2 .
              turbo:hcbmi2 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec2 .
              turbo:hcbmivalspec2 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '33' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("one bb with two bmis one hc diff values all same date")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi2 .
              turbo:bbbmi2 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec2 .
              turbo:bbbmivalspec2 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '33' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("two hc encs one bb enc diff values all same date")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
              
              turbo:part1 obo:RO_0000056 turbo:hcenc2 .
              turbo:hcenc2 a obo:OGMS_0000097 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcencstart2 obo:RO_0002223 turbo:hcenc2 ;
                  a turbo:TURBO_0000511 .
              turbo:hcencdate2 obo:IAO_0000136 turbo:hcencstart2 ;
                  a turbo:TURBO_0000512 ;
                  turbo:TURBO_0006511 '1/1/15' .
              
              turbo:hcenc2 obo:RO_0002234 turbo:hcbmi2 .
              turbo:hcbmi2 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec2 .
              turbo:hcbmivalspec2 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '33' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("two bb encs one hc enc diff values all same date")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
              
              turbo:part1 obo:RO_0000056 turbo:bbenc2 .
              turbo:bbenc2 a turbo:TURBO_0000527 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbencstart2 obo:RO_0002223 turbo:bbenc2 ;
                  a turbo:TURBO_0000531 .
              turbo:bbencdate2 obo:IAO_0000136 turbo:bbencstart2 ;
                  a turbo:TURBO_0000532 ;
                  turbo:TURBO_0006511 '1/1/15' .
              
              turbo:bbenc2 obo:OBI_0000299 turbo:bbbmi2 .
              turbo:bbbmi2 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec2 .
              turbo:bbbmivalspec2 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '33' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("two bb encs one hc enc bb values out of range all same date")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21' .
              
              turbo:part1 obo:RO_0000056 turbo:bbenc2 .
              turbo:bbenc2 a turbo:TURBO_0000527 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbencstart2 obo:RO_0002223 turbo:bbenc2 ;
                  a turbo:TURBO_0000531 .
              turbo:bbencdate2 obo:IAO_0000136 turbo:bbencstart2 ;
                  a turbo:TURBO_0000532 ;
                  turbo:TURBO_0006511 '1/1/15' .
              
              turbo:bbenc2 obo:OBI_0000299 turbo:bbbmi2 .
              turbo:bbbmi2 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec2 .
              turbo:bbbmivalspec2 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.2' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.2' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("two hc encs one bb enc hc values out of range all same date")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.2' .
              
              turbo:part1 obo:RO_0000056 turbo:hcenc2 .
              turbo:hcenc2 a obo:OGMS_0000097 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcencstart2 obo:RO_0002223 turbo:hcenc2 ;
                  a turbo:TURBO_0000511 .
              turbo:hcencdate2 obo:IAO_0000136 turbo:hcencstart2 ;
                  a turbo:TURBO_0000512 ;
                  turbo:TURBO_0006511 '1/1/15' .
              
              turbo:hcenc2 obo:RO_0002234 turbo:hcbmi2 .
              turbo:hcbmi2 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec2 .
              turbo:hcbmivalspec2 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.2' .
                  
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '42' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("bb BMI out of range - only BMI")
    {
         val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:bbenc1 obo:OBI_0000299 turbo:bbbmi1 .
              turbo:bbbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:bbbmivalspec1 .
              turbo:bbbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '500.6' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("hc BMI out of range - only BMI")
    {
         val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '.003' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
    
    test("hc BMI in range - only BMI")
    {
         val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:hcenc1 obo:RO_0002234 turbo:hcbmi1 .
              turbo:hcbmi1 a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
                  obo:OBI_0001938 turbo:hcbmivalspec1 .
              turbo:hcbmivalspec1 a obo:OBI_0001933 ;
                  obo:OBI_0002135 '21.0' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        bmiconc.conclusionateBMI(cxn, conclusionationNamedGraph, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(helper.querySparqlBoolean(cxn, sparqlPrefixes + usedHealthcareBMI).get)
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + usedBiobankBMI).get) 
        assert(!helper.querySparqlBoolean(cxn, sparqlPrefixes + missingKnowledge).get) 
    }
}