package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._

class BirthdateConclusionationUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
    val birthdateconc = new BirthdateConclusionator
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    var askForMK: String = null 
    var askForAnyDate: String = null
    
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
              turbo:part1 turbo:TURBO_0000303 turbo:birth1 ;
                  a turbo:TURBO_0000502 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:birth1 a obo:UBERON_0035946 ;
                  turbo:TURBO_0006500 'true'^^xsd:boolean .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        askForMK = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?dob obo:IAO_0000136 turbo:birth1 .
              ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
              ?mk obo:IAO_0000136 ?dob .
              ?mk a obo:OBI_0000852 .
            }
          }
          """
            
          askForAnyDate = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?dob obo:IAO_0000136 turbo:birth1 .
              ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
              ?dob turbo:TURBO_0006511 ?anyDate .
            }
          }
          """
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }

    test("no birthdate data")
    {
        birthdateconc.conclusionateBirthdate(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + askForMK).get)
        
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + askForAnyDate).get)
    }
    
    test("multiple dob not conflicting")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "1970-05-06"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
              turbo:dob2 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "1970-05-06"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        birthdateconc.conclusionateBirthdate(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        
        val ask = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?dob obo:IAO_0000136 turbo:birth1 .
              ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
              ?dob turbo:TURBO_0006511 "1970-05-06"^^xsd:date .
            }
          }
          """
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get)
        
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + askForMK).get)
    }
    
    test("conflicting dob does not meet threshold")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "1970-05-06"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
              turbo:dob2 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "2015-08-12"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        birthdateconc.conclusionateBirthdate(cxn, conclusionationNamedGraph, .99, masterConclusionation, masterPlanspec, masterPlan)
        
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + askForMK).get)
        
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + askForAnyDate).get)
    }
    
    test("conflicting dob does meet threshold")
    {
        val insert = """
          INSERT DATA { GRAPH pmbb:expanded {
              turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "1970-05-06"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
              turbo:dob2 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "2015-08-12"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
              turbo:dob3 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                  turbo:TURBO_0006511 "2015-08-12"^^xsd:date ;
                  obo:IAO_0000136 turbo:birth1 .
          }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        
        birthdateconc.conclusionateBirthdate(cxn, conclusionationNamedGraph, .51, masterConclusionation, masterPlanspec, masterPlan)
        
         val ask = """
          ASK 
          {
            GRAPH <""" + conclusionationNamedGraph + """>
            {
              ?dob obo:IAO_0000136 turbo:birth1 .
              ?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
              ?dob turbo:TURBO_0006511 "2015-08-12"^^xsd:date .
            }
          }
          """
        assert(update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get)
        
        assert(!update.querySparqlBoolean(cxn, sparqlPrefixes + askForMK).get)
    }
    
    test("invalid threshold causes exception")
    {
        try
        {
            val insert = """
              INSERT DATA { GRAPH pmbb:expanded 
              {
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                      turbo:TURBO_0006511 "1970-05-06"^^xsd:date ;
                      obo:IAO_0000136 turbo:birth1 .
              }}
              """
            update.updateSparql(cxn, sparqlPrefixes + insert)
            birthdateconc.conclusionateBirthdate(cxn, conclusionationNamedGraph, .01, masterConclusionation, masterPlanspec, masterPlan)
            assert(false)
        }
        catch
        {
            case e: RuntimeException => assert(true)
        }
    }
}