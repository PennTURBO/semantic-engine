package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest._

class BiosexConclusionationUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
    val biosexconc = new BiosexConclusionator
    
    
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
        conclusionationNamedGraph = cxn.getValueFactory.createIRI("http://transformunify.org/ontologies/ConclusionatedNamedGraph_" + helper.getCurrentTimestamp("_"))
        masterConclusionation = helper.genTurboIRI(cxn)
        masterPlanspec = helper.genTurboIRI(cxn)
        masterPlan = helper.genTurboIRI(cxn)
        
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:part1 a turbo:TURBO_0000502 ;
                  obo:RO_0000086 turbo:biosex1 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:biosex1 a obo:PATO_0000047 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }

    test("no biosex data")
    {
        biosexconc.conclusionateBiosex(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        
        val ask = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?mk a obo:OBI_0000852 .
              ?mk obo:IAO_0000136 turbo:biosex1 .
            }
          }
          """
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get)
    }
    
    test("multiple gids not conflicting")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded 
          {
              turbo:gid1 a obo:OMRSE_00000138 ;
                obo:IAO_0000136 turbo:part1 .
              turbo:gid2 a obo:OMRSE_00000138 ;
                obo:IAO_0000136 turbo:part1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        biosexconc.conclusionateBiosex(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        
        val ask = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              turbo:biosex1 a obo:PATO_0000383 .
            }
          }
          """
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get)
        
        val ask2 = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?mk a obo:OBI_0000852 .
              ?mk obo:IAO_0000136 turbo:biosex1 .
            }
          }
          """
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get)
    }
    
    test("conflicting gids does not meet threshold")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded 
          {
              turbo:gid1 a obo:OMRSE_00000138 ;
                obo:IAO_0000136 turbo:part1 .
              turbo:gid2 a obo:OMRSE_00000141 ;
                obo:IAO_0000136 turbo:part1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        biosexconc.conclusionateBiosex(cxn, conclusionationNamedGraph, .99, masterConclusionation, masterPlanspec, masterPlan)
        
        val ask = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?mk a obo:OBI_0000852 .
              ?mk obo:IAO_0000136 turbo:biosex1 .
            }
          }
          """
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get)
        
        val ask2 = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              turbo:biosex1 a obo:PATO_0000383 .
            }
          }
          """
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get)
        
        val ask3 = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              turbo:biosex1 a obo:PATO_0000384 .
            }
          }
          """
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get)
    }
    
    test("conflicting gids does meet threshold")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded 
          {
              turbo:gid1 a obo:OMRSE_00000138 ;
                obo:IAO_0000136 turbo:part1 .
              turbo:gid2 a obo:OMRSE_00000141 ;
                obo:IAO_0000136 turbo:part1 .
              turbo:gid3 a obo:OMRSE_00000141 ;
                obo:IAO_0000136 turbo:part1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        biosexconc.conclusionateBiosex(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        
        val ask = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              turbo:biosex1 a obo:PATO_0000384 .
            }
          }
          """
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get)
        
        val ask2 = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?mk a obo:OBI_0000852 .
              ?mk obo:IAO_0000136 turbo:biosex1 .
            }
          }
          """
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get)
    }
    
    test("invalid threshold causes exception")
    {
        try
        {
            val insert = """
              INSERT DATA { GRAPH pmbb:expanded 
              {
                  turbo:gid1 a obo:OMRSE_00000138 ;
                    obo:IAO_0000136 turbo:part1 .
              }}
              """
            update.updateSparql(cxn, sparqlPrefixes + insert)
            biosexconc.conclusionateBiosex(cxn, conclusionationNamedGraph, .01, masterConclusionation, masterPlanspec, masterPlan)
            assert(false)
        }
        catch
        {
            case e: RuntimeException => assert(true)
        }
    }
}