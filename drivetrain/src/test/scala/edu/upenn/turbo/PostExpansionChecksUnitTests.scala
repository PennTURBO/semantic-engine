package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._

class PostExpansionChecksUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val postcheck = new SparqlPostExpansionChecks
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        logger.info("Running a post-expansion check test")
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    /*test("check for invalid classes - one valid class")
    {
        helper.deleteAllTriplesInDatabase(cxn)
        helper.addOntologyFromUrl(cxn, ontologyURL)
        
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 a turbo:TURBO_0000502 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForInvalidClasses(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check for invalid classes - one invalid class")
    {
        helper.deleteAllTriplesInDatabase(cxn)
        helper.addOntologyFromUrl(cxn, ontologyURL)
        
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 a turbo:notARealClass .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForInvalidClasses(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check for unidentified registry ids - two valid reg ids")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 a turbo:TURBO_0000553 .
                  turbo:entity1 obo:BFO_0000051 turbo:denoter1 .
                  turbo:denoter1 a turbo:TURBO_0000555 .
                  turbo:denoter1 obo:IAO_0000219 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
                  
                  turbo:entity2 a turbo:TURBO_0000553 .
                  turbo:entity2 obo:BFO_0000051 turbo:denoter2 .
                  turbo:denoter2 a turbo:TURBO_0000555 .
                  turbo:denoter2 obo:IAO_0000219 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForUnidentifiedRegistryIDs(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check for unidentified registry ids - one invalid reg ids")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 a turbo:TURBO_0000553 .
                  turbo:entity1 obo:BFO_0000051 turbo:denoter1 .
                  turbo:denoter1 a turbo:TURBO_0000555 .
                  turbo:denoter1 obo:IAO_0000219 turbo:notARealClass .
                  
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForUnidentifiedRegistryIDs(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check for unparseable dates - one parseable tagged date")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 '1994-11-12'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForUnparseableOrUntaggedDates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check for unparseable dates - one parseable untagged date")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 '1994-11-12' .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForUnparseableOrUntaggedDates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check for unparseable dates - one unparseable tagged date")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 'xyza-cv-ab'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForUnparseableOrUntaggedDates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check date literals have valid predicates - one date with valid predicate")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 '1994-11-12'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkThatDateLiteralsHaveValidDatePredicates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check date literals have valid predicates - one date with invalid predicate")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:notADatePredicate '1994-11-12'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkThatDateLiteralsHaveValidDatePredicates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check for invalid predicates - valid predicates")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:psc1 obo:IAO_0000219 turbo:part1 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForInvalidPredicates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check for invalid predicates - invalid predicate")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:psc1 turbo:notARealPredicate turbo:part1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForInvalidPredicates(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check for single instantiation (post expansion only) - single inst")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph pmbb:postExpansionCheck
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForSingleInstantiationProcess(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "testing") should be (true)
    }
    
    test("check for single instantiation (post expansion only) - two insts")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph pmbb:postExpansionCheck
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst2 a turbo:TURBO_0000522 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForSingleInstantiationProcess(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "testing") should be (false)
    }
    
    test("check all instantiations with dataset - one inst with dataset")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkAllInstantiationProcessesAreAttachedToDatasets(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check all instantiations with dataset - one inst without dataset")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a turbo:notADataset .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkAllInstantiationProcessesAreAttachedToDatasets(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check for instances subclass of type - negative case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 a turbo:type1 .
                  turbo:entity1 rdfs:subClassOf turbo:type2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForSubclassRelationships(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check for instances subclass of type - positive case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 a turbo:type1 .
                  turbo:entity1 rdfs:subClassOf turbo:type1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkForSubclassRelationships(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check object properties do not have literal objects - negative case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 obo:BFO_0000051 turbo:part1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkObjectPropertiesDoNotHaveLiteralObjects(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check object properties do not have literal objects - positive case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 obo:BFO_0000051 '1' .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkObjectPropertiesDoNotHaveLiteralObjects(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check datatype properties do not have uri objects - negative case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 turbo:TURBO_0006510 '1' .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkDatatypePropertiesDoNotHaveUriObjects(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }

    test("check datatype properties do not have uri objects - positive case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 turbo:TURBO_0006510 turbo:1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkDatatypePropertiesDoNotHaveUriObjects(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check participant for required dependents - everything present")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:patientCrid1 a turbo:TURBO_0000503 .
                  turbo:patientCrid1 obo:BFO_0000051 turbo:cridSymbol1 .
                  turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:cridSymbol1 a turbo:TURBO_0000504 .
                  turbo:patientCrid1 obo:BFO_0000051 turbo:patientRegDen1 .
                  turbo:patientRegDen1 a turbo:TURBO_0000505 .
                  # turbo:patientRegDen1 turbo:TURBO_0006510 'inpatient' .
                  turbo:patientRegDen1 obo:IAO_0000219 turbo:patientRegID1 .
                  turbo:patientRegID1 a turbo:TURBO_0000506 .
                  turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:part1 obo:RO_0000086 turbo:biosex1 .
                  turbo:biosex1 a obo:PATO_0000047 .
                  turbo:part1 obo:RO_0000086 turbo:height1 .
                  turbo:height1 a obo:PATO_0000119 .
                  turbo:part1 obo:RO_0000086 turbo:weight1 .
                  turbo:weight1 a obo:PATO_0000128 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                  turbo:birth1 a obo:UBERON_0035946 .
                  turbo:dob1 obo:IAO_0000136 turbo:birth1 .
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> .
                  turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                  turbo:adipose1 a obo:UBERON_0001013 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check participant for required dependents - missing psc")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:part1 obo:RO_0000086 turbo:biosex1 .
                  turbo:biosex1 a obo:PATO_0000047 .
                  turbo:part1 obo:RO_0000086 turbo:height1 .
                  turbo:height1 a obo:PATO_0000119 .
                  turbo:part1 obo:RO_0000086 turbo:weight1 .
                  turbo:weight1 a obo:PATO_0000128 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                  turbo:birth1 a obo:UBERON_0035946 .
                  turbo:dob1 obo:IAO_0000136 turbo:birth1 .
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> .
                  turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                  turbo:adipose1 a obo:UBERON_0001013 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check participant for required dependents - missing biosex")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:cridSymbol1 a turbo:TURBO_0000504 .
                  turbo:patientCrid1 a turbo:TURBO_0000503 .
                  turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:cridSymbol1 .
                  turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:patientRegDen1 .
                  turbo:patientRegDen1 a turbo:TURBO_0000505 .
                  # turbo:patientRegDen1 turbo:TURBO_0006510 'inpatient' .
                  turbo:patientRegDen1 obo:IAO_0000219 turbo:patientRegID1 .
                  turbo:patientRegID1 a turbo:TURBO_0000506 .
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:part1 obo:RO_0000086 turbo:height1 .
                  turbo:height1 a obo:PATO_0000119 .
                  turbo:part1 obo:RO_0000086 turbo:weight1 .
                  turbo:weight1 a obo:PATO_0000128 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                  turbo:birth1 a obo:UBERON_0035946 .
                  turbo:dob1 obo:IAO_0000136 turbo:birth1 .
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> .
                  turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                  turbo:adipose1 a obo:UBERON_0001013 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
        test("check participant for required dependents - missing birth")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:patientCrid1 a turbo:TURBO_0000503 .
                  turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:cridSymbol1 a turbo:TURBO_0000504 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:cridSymbol1 .
                  turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:patientRegDen1 .
                  turbo:patientRegDen1 a turbo:TURBO_0000505 .
                  # turbo:patientRegDen1 turbo:TURBO_0006510 'inpatient' .
                  turbo:patientRegDen1 obo:IAO_0000219 turbo:patientRegID1 .
                  turbo:patientRegID1 a turbo:TURBO_0000506 .
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:part1 obo:RO_0000086 turbo:biosex1 .
                  turbo:biosex1 a obo:PATO_0000047 .
                  turbo:part1 obo:RO_0000086 turbo:height1 .
                  turbo:height1 a obo:PATO_0000119 .
                  turbo:part1 obo:RO_0000086 turbo:weight1 .
                  turbo:weight1 a obo:PATO_0000128 .
                  turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                  turbo:adipose1 a obo:UBERON_0001013 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
        
    test("check participant for required dependents - missing height")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:patientCrid1 a turbo:TURBO_0000503 .
                  turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:cridSymbol1 a turbo:TURBO_0000504 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:cridSymbol1 .
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:patientRegDen1 .
                  turbo:patientRegDen1 a turbo:TURBO_0000505 .
                  # turbo:patientRegDen1 turbo:TURBO_0006510 'inpatient' .
                  turbo:patientRegDen1 obo:IAO_0000219 turbo:patientRegID1 .
                  turbo:patientRegID1 a turbo:TURBO_0000506 .
                  turbo:part1 obo:RO_0000086 turbo:biosex1 .
                  turbo:biosex1 a obo:PATO_0000047 .
                  turbo:part1 obo:RO_0000086 turbo:weight1 .
                  turbo:weight1 a obo:PATO_0000128 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                  turbo:birth1 a obo:UBERON_0035946 .
                  turbo:dob1 obo:IAO_0000136 turbo:birth1 .
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> .
                  turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                  turbo:adipose1 a obo:UBERON_0001013 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }    
    
    test("check participant for required dependents - missing weight")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:patientCrid1 a turbo:TURBO_0000503 .
                  turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:cridSymbol1 a turbo:TURBO_0000504 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:cridSymbol1 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:patientRegDen1 .
                  turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                  turbo:patientRegDen1 a turbo:TURBO_0000505 .
                  # turbo:patientRegDen1 turbo:TURBO_0006510 'inpatient' .
                  turbo:patientRegDen1 obo:IAO_0000219 turbo:patientRegID1 .
                  turbo:patientRegID1 a turbo:TURBO_0000506 .
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:part1 obo:RO_0000086 turbo:biosex1 .
                  turbo:biosex1 a obo:PATO_0000047 .
                  turbo:part1 obo:RO_0000086 turbo:height1 .
                  turbo:height1 a obo:PATO_0000119 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                  turbo:birth1 a obo:UBERON_0035946 .
                  turbo:dob1 obo:IAO_0000136 turbo:birth1 .
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> .
                  turbo:part1 obo:BFO_0000051 turbo:adipose1 .
                  turbo:adipose1 a obo:UBERON_0001013 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check participant for required dependents - missing adipose")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:cridSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:patientCrid1 a turbo:TURBO_0000503 .
                  turbo:cridSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:cridSymbol1 a turbo:TURBO_0000504 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:cridSymbol1 .
                  turbo:patientCrid1 obo:IAO_0000219 turbo:part1 .
                  turbo:patientCrid1 obo:BFO_0000050 turbo:patientRegDen1 .
                  turbo:patientRegDen1 a turbo:TURBO_0000505 .
                  # turbo:patientRegDen1 turbo:TURBO_0006510 'inpatient' .
                  turbo:patientRegDen1 obo:IAO_0000219 turbo:patientRegID1 .
                  turbo:patientRegID1 a turbo:TURBO_0000506 .
                  turbo:part1 a turbo:TURBO_0000502 .
                  turbo:part1 obo:RO_0000086 turbo:biosex1 .
                  turbo:biosex1 a obo:PATO_0000047 .
                  turbo:part1 obo:RO_0000086 turbo:height1 .
                  turbo:height1 a obo:PATO_0000119 .
                  turbo:part1 obo:RO_0000086 turbo:weight1 .
                  turbo:weight1 a obo:PATO_0000128 .
                  turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
                  turbo:birth1 a obo:UBERON_0035946 .
                  turbo:dob1 obo:IAO_0000136 turbo:birth1 .
                  turbo:dob1 a <http://www.ebi.ac.uk/efo/EFO_0004950> .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkParticipantsForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check hc encs for required dependents - everything present")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:dataset1 obo:BFO_0000051 turbo:hcRegDen1 .
                  turbo:dataset1 obo:BFO_0000051 turbo:hcSymbol1 .
                  turbo:hcRegDen1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:hcSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:encID1 a turbo:TURBO_0000508 .
                  turbo:encID1 obo:BFO_0000051 turbo:hcRegDen1 .
                  turbo:encID1 obo:BFO_0000051 turbo:hcSymbol1 .
                  turbo:hcSymbol1 a turbo:TURBO_0000509 .
                  turbo:hcRegDen1 a turbo:TURBO_0000510 .
                  turbo:hcRegDen1 obo:IAO_0000219 turbo:reg1 .
                  turbo:reg1 a turbo:TURBO_0000513 .
                  turbo:hcSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:encID1 obo:IAO_0000219 turbo:hcenc1 .
                  turbo:hcenc1 a obo:OGMS_0000097 .
                  turbo:hcenc1 obo:RO_0002234 turbo:diagnosis1 .
                  turbo:diagnosis1 a obo:OGMS_0000073 .
                  turbo:diagCrid1 obo:IAO_0000219 turbo:diagnosis1 .
                  turbo:diagCrid1 a turbo:TURBO_0000553 .
                  turbo:diagCrid1 obo:BFO_0000051 turbo:diagCodeRegID1 .
                  turbo:diagCrid1 obo:BFO_0000051 turbo:diagCodeSymbol1 .
                  turbo:diagCodeRegID1 a turbo:TURBO_0000555 .
                  turbo:diagCodeSymbol1 a turbo:TURBO_0000554 .
                  turbo:encStart1 a turbo:TURBO_0000511 .
                  turbo:encStart1 obo:RO_0002223 turbo:hcenc1 .
                  turbo:encDate1 obo:IAO_0000136 turbo:encStart1 .
                  turbo:encDate1 a turbo:TURBO_0000512 .
                  
                  turbo:hcenc1 obo:RO_0002234 turbo:prescription1 .
                  turbo:prescription1 a obo:PDRO_0000024 .
                  turbo:prescription1 obo:BFO_0000050 turbo:dataset .
                  turbo:dataset obo:BFO_0000051 turbo:prescription1 .
                  turbo:prescription1 turbo:TURBO_0006512 "some drug" .
                  turbo:medCrid1 obo:IAO_0000219 turbo:prescription1 .
                  turbo:medCrid1 a turbo:TURBO_0000561 .
                  turbo:medCrid1 obo:BFO_0000051 turbo:medSymb1 .
                  turbo:medSymb1 a turbo:TURBO_0000562 .
                  turbo:medSymb1 turbo:TURBO_0006510 "3" .
                  turbo:medSymb1 obo:BFO_0000050 turbo:dataset .
                  turbo:dataset obo:BFO_0000051 turbo:medSymb1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkHealthcareEncountersForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check hc encs for required dependents - missing identifier")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:hcenc1 a obo:OGMS_0000097 .
                  turbo:hcenc1 obo:RO_0002234 turbo:diagnosis1 .
                  turbo:diagnosis1 a obo:OGMS_0000073 .
                  turbo:diagCrid1 obo:IAO_0000219 turbo:diagnosis1 .
                  turbo:diagCrid1 a turbo:TURBO_0000553 .
                  turbo:diagCrid1 obo:BFO_0000051 turbo:diagCodeRegID1 .
                  turbo:diagCrid1 obo:BFO_0000051 turbo:diagCodeSymbol1 .
                  turbo:diagCodeRegID1 a turbo:TURBO_0000555 .
                  turbo:diagCodeSymbol1 a turbo:TURBO_0000554 .
                  turbo:encStart1 a turbo:TURBO_0000511 .
                  turbo:encStart1 obo:RO_0002223 turbo:hcenc1 .
                  turbo:encDate1 obo:IAO_0000136 turbo:encStart1 .
                  turbo:encDate1 a turbo:TURBO_0000512 .
                  
                  turbo:hcenc1 obo:RO_0002234 turbo:prescription1 .
                  turbo:prescription1 a obo:PDRO_0000024 .
                  turbo:prescription1 obo:BFO_0000050 turbo:dataset .
                  turbo:dataset obo:BFO_0000051 turbo:prescription1 .
                  turbo:prescription1 turbo:TURBO_0006512 "some drug" .
                  turbo:medCrid1 obo:IAO_0000219 turbo:prescription1 .
                  turbo:medCrid1 a turbo:TURBO_0000561 .
                  turbo:medCrid1 obo:BFO_0000051 turbo:medSymb1 .
                  turbo:medSymb1 a turbo:TURBO_0000562 .
                  turbo:medSymb1 turbo:TURBO_0006510 "3" .
                  turbo:medSymb1 obo:BFO_0000050 turbo:dataset .
                  turbo:dataset obo:BFO_0000051 turbo:medSymb1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkHealthcareEncountersForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check hc encs for required dependents - missing enc start")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:dataset1 obo:BFO_0000051 turbo:hcRegDen1 .
                  turbo:dataset1 obo:BFO_0000051 turbo:hcSymbol1 .
                  turbo:hcRegDen1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:hcSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:encID1 a turbo:TURBO_0000508 .
                  turbo:encID1 obo:BFO_0000051 turbo:hcRegDen1 .
                  turbo:encID1 obo:BFO_0000051 turbo:hcSymbol1 .
                  turbo:hcSymbol1 a turbo:TURBO_0000509 .
                  turbo:hcRegDen1 a turbo:TURBO_0000510 .
                  turbo:hcRegDen1 obo:IAO_0000219 turbo:reg1 .
                  turbo:reg1 a turbo:TURBO_0000513 .
                  turbo:hcSymbol1 turbo:TURBO_0006510 '1' .
                  turbo:encID1 obo:IAO_0000219 turbo:hcenc1 .
                  turbo:hcenc1 a obo:OGMS_0000097 .
                  turbo:hcenc1 obo:RO_0002234 turbo:diagnosis1 .
                  turbo:diagnosis1 a obo:OGMS_0000073 .
                  turbo:diagCrid1 obo:IAO_0000219 turbo:diagnosis1 .
                  turbo:diagCrid1 a turbo:TURBO_0000553 .
                  turbo:diagCrid1 obo:BFO_0000051 turbo:diagCodeRegID1 .
                  turbo:diagCrid1 obo:BFO_0000051 turbo:diagCodeSymbol1 .
                  turbo:diagCodeRegID1 a turbo:TURBO_0000555 .
                  turbo:diagCodeSymbol1 a turbo:TURBO_0000554 .
                  
                  turbo:hcenc1 obo:RO_0002234 turbo:prescription1 .
                  turbo:prescription1 a obo:PDRO_0000024 .
                  turbo:prescription1 obo:BFO_0000050 turbo:dataset .
                  turbo:dataset obo:BFO_0000051 turbo:prescription1 .
                  turbo:prescription1 turbo:TURBO_0006512 "some drug" .
                  turbo:medCrid1 obo:IAO_0000219 turbo:prescription1 .
                  turbo:medCrid1 a turbo:TURBO_0000561 .
                  turbo:medCrid1 obo:BFO_0000051 turbo:medSymb1 .
                  turbo:medSymb1 a turbo:TURBO_0000562 .
                  turbo:medSymb1 turbo:TURBO_0006510 "3" .
                  turbo:medSymb1 obo:BFO_0000050 turbo:dataset .
                  turbo:dataset obo:BFO_0000051 turbo:medSymb1 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkHealthcareEncountersForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check bb encs for required dependents - everything present")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:dataset1 obo:BFO_0000051 turbo:encRegDen1 .
                  turbo:dataset1 obo:BFO_0000051 turbo:encSymbol1 .
                  turbo:encRegDen1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:encSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:encID1 a turbo:TURBO_0000533 .
                  turbo:encID1 obo:BFO_0000051 turbo:encSymbol1 .
                  turbo:encID1 obo:BFO_0000051 turbo:encRegDen1 .
                  turbo:encSymbol1 a turbo:TURBO_0000534 . 
                  turbo:encRegDen1 a turbo:TURBO_0000535 .
                  turbo:encSymbol1 turbo:TURBO_0006510 '1' .
                  # turbo:encRegDen1 turbo:TURBO_0006510 "biobank" .
                  turbo:encRegDen1 obo:IAO_0000219 turbo:reg1 .
                  turbo:reg1 a turbo:TURBO_0000543 .
                  turbo:encID1 obo:IAO_0000219 turbo:bbenc1 .
                  turbo:bbenc1 a turbo:TURBO_0000527 .
                  turbo:encStart1 a turbo:TURBO_0000531 .
                  turbo:encStart1 obo:RO_0002223 turbo:bbenc1 .
                  turbo:encDate1 obo:IAO_0000136 turbo:encStart1 .
                  turbo:encDate1 a turbo:TURBO_0000532 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkBiobankEncountersForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("check bb encs for required dependents - missing identifier")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:bbenc1 a turbo:TURBO_0000527 .
                  turbo:encStart1 a turbo:TURBO_0000531 .
                  turbo:encStart1 obo:RO_0002223 turbo:bbenc1 .
                  turbo:encDate1 obo:IAO_0000136 turbo:encStart1 .
                  turbo:encDate1 a turbo:TURBO_0000532 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkBiobankEncountersForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("check bb encs for required dependents - missing start")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:inst1 a turbo:TURBO_0000522 .
                  turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
                  turbo:dataset1 a obo:IAO_0000100 .
                  turbo:dataset1 obo:BFO_0000051 turbo:encRegDen1 .
                  turbo:dataset1 obo:BFO_0000051 turbo:encSymbol1 .
                  turbo:encRegDen1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:encSymbol1 obo:BFO_0000050 turbo:dataset1 .
                  turbo:encID1 a turbo:TURBO_0000533 .
                  turbo:encID1 obo:BFO_0000051 turbo:encSymbol1 .
                  turbo:encID1 obo:BFO_0000051 turbo:encRegDen1 .
                  turbo:encSymbol1 a turbo:TURBO_0000534 . 
                  turbo:encRegDen1 a turbo:TURBO_0000535 .
                  turbo:encSymbol1 turbo:TURBO_0006510 '1' .
                  # turbo:encRegDen1 turbo:TURBO_0006510 "biobank" .
                  turbo:encRegDen1 obo:IAO_0000219 turbo:reg1 .
                  turbo:reg1 a turbo:TURBO_0000543 .
                  turbo:encID1 obo:IAO_0000219 turbo:bbenc1 .
                  turbo:bbenc1 a turbo:TURBO_0000527 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkBiobankEncountersForRequiredDependents(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("make sure dates are reasonable - one reasonable date")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 '1994-11-12'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkAllDatesAreReasonable(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("make sure dates are reasonable - one date too low")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 '1899-12-31'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkAllDatesAreReasonable(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("make sure dates are reasonable - one date too high")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:date1 turbo:TURBO_0006511 '2019-01-02'^^xsd:date .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.checkAllDatesAreReasonable(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("no shortcut relations in graph - negative case")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 turbo:notAShortcut turbo:entity2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noShortcutRelationsInGraph(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (true)
    }
    
    test("no shortcut relations in graph - one participant shortcut present")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 turbo:TURBO_0000607 turbo:entity2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noShortcutRelationsInGraph(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("no shortcut relations in graph - one hc enc shortcut present")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 turbo:TURBO_0000648 turbo:entity2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noShortcutRelationsInGraph(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("no shortcut relations in graph - one bb enc shortcut present")
    {
        val insert: String = """
          INSERT DATA 
          {
              graph turbo:testingGraph
              {
                  turbo:entity1 turbo:TURBO_0000624 turbo:entity2 .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noShortcutRelationsInGraph(cxn, "http://transformunify.org/ontologies/testingGraph", "testing") should be (false)
    }
    
    test("all entities are reftracked - all reftracked")
    {
        val insert: String = """
        INSERT DATA {
        GRAPH pmbb:testingGraph {
        pmbb:cons a turbo:TURBO_0000502 .
        pmbb:cons turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:consCrid a turbo:TURBO_0000503 .
        pmbb:consCrid turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:consSymb a turbo:TURBO_0000504 .
        pmbb:consSymb turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:consRegDen a turbo:TURBO_0000505 .
        pmbb:consRegDen turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:biosex a obo:PATO_0000047 .
        pmbb:biosex turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:adipose a obo:UBERON_0001013 .
        pmbb:adipose turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:birth a obo:UBERON_0035946 .
        pmbb:birth turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:height a obo:PATO_0000119 .
        pmbb:height turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:weight a obo:PATO_0000128 .
        pmbb:weight turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bbenc a turbo:TURBO_0000527 .
        pmbb:bbenc turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bbencCrid a turbo:TURBO_0000531 .
        pmbb:bbencCrid turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bbEncSymb a turbo:TURBO_0000532 .
        pmbb:bbEncSymb turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bbEncRegDen a turbo:TURBO_0000533 .
        pmbb:bbEncRegDen turbo:TURBO_0006500 'true'^^xsd:boolean .
        pmbb:bbEncStart a turbo:TURBO_0000534 .
        pmbb:bbEncStart turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bbEncDate a turbo:TURBO_0000535 .
        pmbb:bbEncDate turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:hcenc a obo:OGMS_0000097 .
        pmbb:hcenc turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:hcencCrid a turbo:TURBO_0000508 .
        pmbb:hcencCrid turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:hcEncSymb a turbo:TURBO_0000509 .
        pmbb:hcEncSymb turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:hcEncRegDen a turbo:TURBO_0000510 .
        pmbb:hcEncRegDen turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:hcEncStart a turbo:TURBO_0000511 .
        pmbb:hcEncStart turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:hcEncDate a turbo:TURBO_0000512 .
        pmbb:hcEncDate turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bmi a efo:EFO_0004340 .
        pmbb:bmi turbo:TURBO_0006500 'true'^^xsd:boolean . 
        pmbb:bmivalspec a obo:OBI_0001933 .
        pmbb:bmivalspec turbo:TURBO_0006500 'true'^^xsd:boolean . 
        }}"""
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allEntitiesAreReftracked(cxn, "http://www.itmat.upenn.edu/biobank/testingGraph", "testing") should be (true)
    }
    
    test("all entities are reftracked - nothing reftracked")
    {
        val insert: String = """
        INSERT DATA {
        GRAPH pmbb:testingGraph {
        pmbb:cons a turbo:TURBO_0000502 .
        pmbb:consCrid a turbo:TURBO_0000503 .
        pmbb:consSymb a turbo:TURBO_0000504 .
        pmbb:consRegDen a turbo:TURBO_0000505 .
        pmbb:biosex a obo:PATO_0000047 .
        pmbb:adipose a obo:UBERON_0001013 .
        pmbb:birth a obo:UBERON_0035946 .
        pmbb:height a obo:PATO_0000119 .
        pmbb:weight a obo:PATO_0000128 .
        pmbb:bbenc a turbo:TURBO_0000527 .
        pmbb:bbencCrid a turbo:TURBO_0000531 .
        pmbb:bbEncSymb a turbo:TURBO_0000532 .
        pmbb:bbEncRegDen a turbo:TURBO_0000533 .
        pmbb:bbEncStart a turbo:TURBO_0000534 .
        pmbb:bbEncDate a turbo:TURBO_0000535 .
        pmbb:hcenc a obo:OGMS_0000097 .
        pmbb:hcencCrid a turbo:TURBO_0000508 .
        pmbb:hcEncSymb a turbo:TURBO_0000509 .
        pmbb:hcEncRegDen a turbo:TURBO_0000510 .
        pmbb:hcEncStart a turbo:TURBO_0000511 .
        pmbb:hcEncDate a turbo:TURBO_0000512 . 
        pmbb:bmi a efo:EFO_0004340 .
        pmbb:bmivalspec a obo:OBI_0001933 .
        }}"""
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allEntitiesAreReftracked(cxn, "http://www.itmat.upenn.edu/biobank/testingGraph", "testing") should be (false)
    }
    
    test("all bmis are conclusionated - one conclusionated, one romk")
    {
        val insert: String = """
          Insert Data {
          
          Graph pmbb:expanded {
          
              pmbb:participant a turbo:TURBO_0000502 .
             
              pmbb:participant obo:RO_0000056 pmbb:bbEnc1 .
              pmbb:bbEnc1 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbEnc1 .
              pmbb:bbEncStart1 a turbo:TURBO_0000531 .
              pmbb:bbEncDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
              pmbb:bbEncDate1 a turbo:TURBO_0000532 .
              pmbb:bbEncDate1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              pmbb:bbEnc1 obo:OBI_0000299 pmbb:bbBMI .
              pmbb:bbBMI a efo:EFO_0004340 .
              pmbb:bbBMI obo:OBI_0001938 pmbb:bmiValSpec .
              pmbb:bmiValSpec a obo:OBI_0001933 .
            	pmbb:bmiValSpec obo:OBI_0002135 '20' .
              
              pmbb:participant obo:RO_0000056 pmbb:bbEnc2 .
              pmbb:bbEnc2 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart2 obo:RO_0002223 pmbb:bbEnc2 .
              pmbb:bbEncStart2 a turbo:TURBO_0000531 .
              pmbb:bbEncDate2 obo:IAO_0000136 pmbb:bbEncStart2 .
              pmbb:bbEncDate2 a turbo:TURBO_0000532 .
              pmbb:bbEncDate2 turbo:TURBO_0006511 '12/1/1968'^^xsd:date .
          
          }
          
          Graph pmbb:conclusions {
            
              pmbb:concBMI a efo:EFO_0004340 .
              pmbb:concBMI turbo:TURBO_0006501 'true'^^xsd:boolean .
              pmbb:concBMI obo:IAO_0000581 pmbb:bbEncDate1 .
              pmbb:concBMI obo:OBI_0001938 pmbb:concBMIValSpec .
              pmbb:concBMIValSpec a obo:OBI_0001933 .
            	pmbb:concBMIValSpec obo:OBI_0002135 '20' .
              
              pmbb:mk1 a obo:OBI_0000852 .
              pmbb:mk1 obo:IAO_0000136 pmbb:bbEnc2 .
            
          }
          }
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBMIsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (true)
    }
    
    test("all bmis are conclusionated - one conclusionated, missing record of missing knowledge")
    {
        val insert: String = """
          Insert Data {
          
          Graph pmbb:expanded {
          
              pmbb:participant a turbo:TURBO_0000502 .
             
              pmbb:participant obo:RO_0000056 pmbb:bbEnc1 .
              pmbb:bbEnc1 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbEnc1 .
              pmbb:bbEncStart1 a turbo:TURBO_0000531 .
              pmbb:bbEncDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
              pmbb:bbEncDate1 a turbo:TURBO_0000532 .
              pmbb:bbEncDate1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              pmbb:bbEnc1 obo:OBI_0000299 pmbb:bbBMI .
              pmbb:bbBMI a efo:EFO_0004340 .
              pmbb:bbBMI obo:OBI_0001938 pmbb:bmiValSpec .
              pmbb:bmiValSpec a obo:OBI_0001933 .
            	pmbb:bmiValSpec obo:OBI_0002135 '20' .
              
              pmbb:participant obo:RO_0000056 pmbb:bbEnc2 .
              pmbb:bbEnc2 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart2 obo:RO_0002223 pmbb:bbEnc2 .
              pmbb:bbEncStart2 a turbo:TURBO_0000531 .
              pmbb:bbEncDate2 obo:IAO_0000136 pmbb:bbEncStart2 .
              pmbb:bbEncDate2 a turbo:TURBO_0000532 .
              pmbb:bbEncDate2 turbo:TURBO_0006511 '12/1/1968'^^xsd:date .
          
          }
          
          Graph pmbb:conclusions {
            
              pmbb:concBMI a efo:EFO_0004340 .
              pmbb:concBMI turbo:TURBO_0006501 'true'^^xsd:boolean .
              pmbb:concBMI obo:IAO_0000581 pmbb:bbEncDate1 .
              pmbb:concBMI obo:OBI_0001938 pmbb:concBMIValSpec .
              pmbb:concBMIValSpec a obo:OBI_0001933 .
            	pmbb:concBMIValSpec obo:OBI_0002135 '20' .
            	
          }
          }
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBMIsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all bmis are conclusionated - one missing conclusionation, one romk")
    {
        val insert: String = """
          Insert Data {
          
          Graph pmbb:expanded {
          
              pmbb:participant a turbo:TURBO_0000502 .
             
              pmbb:participant obo:RO_0000056 pmbb:bbEnc1 .
              pmbb:bbEnc1 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbEnc1 .
              pmbb:bbEncStart1 a turbo:TURBO_0000531 .
              pmbb:bbEncDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
              pmbb:bbEncDate1 a turbo:TURBO_0000532 .
              pmbb:bbEncDate1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              pmbb:bbEnc1 obo:OBI_0000299 pmbb:bbBMI .
              pmbb:bbBMI a efo:EFO_0004340 .
              pmbb:bbBMI obo:OBI_0001938 pmbb:bmiValSpec .
              pmbb:bmiValSpec a obo:OBI_0001933 .
            	pmbb:bmiValSpec obo:OBI_0002135 '20' .
              
              pmbb:participant obo:RO_0000056 pmbb:bbEnc2 .
              pmbb:bbEnc2 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart2 obo:RO_0002223 pmbb:bbEnc2 .
              pmbb:bbEncStart2 a turbo:TURBO_0000531 .
              pmbb:bbEncDate2 obo:IAO_0000136 pmbb:bbEncStart2 .
              pmbb:bbEncDate2 a turbo:TURBO_0000532 .
              pmbb:bbEncDate2 turbo:TURBO_0006511 '12/1/1968'^^xsd:date .
          
          }
          
          Graph pmbb:conclusions {
              
              pmbb:mk1 a obo:OBI_0000852 .
              pmbb:mk1 obo:IAO_0000136 pmbb:bbEnc2 .
            
          }
          }
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBMIsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all bmis are conclusionated - conclusionated has invalid type")
    {
        val insert: String = """
          Insert Data {
          
          Graph pmbb:expanded {
          
              pmbb:participant a turbo:TURBO_0000502 .
             
              pmbb:participant obo:RO_0000056 pmbb:bbEnc1 .
              pmbb:bbEnc1 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbEnc1 .
              pmbb:bbEncStart1 a turbo:TURBO_0000531 .
              pmbb:bbEncDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
              pmbb:bbEncDate1 a turbo:TURBO_0000532 .
              pmbb:bbEncDate1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              pmbb:bbEnc1 obo:OBI_0000299 pmbb:bbBMI .
              pmbb:bbBMI a efo:EFO_0004340 .
              pmbb:bbBMI obo:OBI_0001938 pmbb:bmiValSpec .
              pmbb:bmiValSpec a obo:OBI_0001933 .
            	pmbb:bmiValSpec obo:OBI_0002135 '20' .
              
              pmbb:participant obo:RO_0000056 pmbb:bbEnc2 .
              pmbb:bbEnc2 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart2 obo:RO_0002223 pmbb:bbEnc2 .
              pmbb:bbEncStart2 a turbo:TURBO_0000531 .
              pmbb:bbEncDate2 obo:IAO_0000136 pmbb:bbEncStart2 .
              pmbb:bbEncDate2 a turbo:TURBO_0000532 .
              pmbb:bbEncDate2 turbo:TURBO_0006511 '12/1/1968'^^xsd:date .
          
          }
          
          Graph pmbb:conclusions {
            
              pmbb:concBMI a turbo:notAType .
              pmbb:concBMI turbo:TURBO_0006501 'true'^^xsd:boolean .
              pmbb:concBMI obo:IAO_0000581 pmbb:bbEncDate1 .
              pmbb:concBMI obo:OBI_0001938 pmbb:concBMIValSpec .
              pmbb:concBMIValSpec a obo:OBI_0001933 .
            	pmbb:concBMIValSpec obo:OBI_0002135 '20' .
              
              pmbb:mk1 a obo:OBI_0000852 .
              pmbb:mk1 obo:IAO_0000136 pmbb:bbEnc2 .
            
          }
          }
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBMIsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all bmis are conclusionated - mk has invalid type")
    {
        val insert: String = """
          Insert Data {
          
          Graph pmbb:expanded {
          
              pmbb:participant a turbo:TURBO_0000502 .
             
              pmbb:participant obo:RO_0000056 pmbb:bbEnc1 .
              pmbb:bbEnc1 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart1 obo:RO_0002223 pmbb:bbEnc1 .
              pmbb:bbEncStart1 a turbo:TURBO_0000531 .
              pmbb:bbEncDate1 obo:IAO_0000136 pmbb:bbEncStart1 .
              pmbb:bbEncDate1 a turbo:TURBO_0000532 .
              pmbb:bbEncDate1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              pmbb:bbEnc1 obo:OBI_0000299 pmbb:bbBMI .
              pmbb:bbBMI a efo:EFO_0004340 .
              pmbb:bbBMI obo:OBI_0001938 pmbb:bmiValSpec .
              pmbb:bmiValSpec a obo:OBI_0001933 .
            	pmbb:bmiValSpec obo:OBI_0002135 '20' .
              
              pmbb:participant obo:RO_0000056 pmbb:bbEnc2 .
              pmbb:bbEnc2 a turbo:TURBO_0000527 ;
                     turbo:TURBO_0006500 'true'^^xsd:boolean .
              pmbb:bbEncStart2 obo:RO_0002223 pmbb:bbEnc2 .
              pmbb:bbEncStart2 a turbo:TURBO_0000531 .
              pmbb:bbEncDate2 obo:IAO_0000136 pmbb:bbEncStart2 .
              pmbb:bbEncDate2 a turbo:TURBO_0000532 .
              pmbb:bbEncDate2 turbo:TURBO_0006511 '12/1/1968'^^xsd:date .
          
          }
          
          Graph pmbb:conclusions {
            
              pmbb:concBMI a efo:EFO_0004340 .
              pmbb:concBMI turbo:TURBO_0006501 'true'^^xsd:boolean .
              pmbb:concBMI obo:IAO_0000581 pmbb:bbEncDate1 .
              pmbb:concBMI obo:OBI_0001938 pmbb:concBMIValSpec .
              pmbb:concBMIValSpec a obo:OBI_0001933 .
            	pmbb:concBMIValSpec obo:OBI_0002135 '20' .
              
              pmbb:mk1 a turbo:notAType .
              pmbb:mk1 obo:IAO_0000136 pmbb:bbEnc2 .
            
          }
          }
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBMIsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all births are conclusionated - one conclusionated, one romk")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              pmbb:birth1 a obo:UBERON_0035946 .
              pmbb:dob1 a efo:EFO_0004950 .
              pmbb:dob1 obo:IAO_0000136 pmbb:birth1 .
              pmbb:dob1 turbo:TURBO_0006510 '12/31/1968' .
              pmbb:dob1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              
              pmbb:birth2 a obo:UBERON_0035946 .
              pmbb:dob2 a efo:EFO_0004950 .
              pmbb:dob2 obo:IAO_0000136 pmbb:birth2 .
          }
          Graph pmbb:conclusions {
          
              pmbb:concludedBirth a efo:EFO_0004950 .
              pmbb:concludedBirth obo:IAO_0000136 pmbb:birth1 .
              
              pmbb:romk a obo:OBI_0000852 .
              pmbb:romk obo:IAO_0000136 pmbb:birth2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBirthsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (true)
    }
    
    test("all births are conclusionated - one conclusionated, missing romk")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              pmbb:birth1 a obo:UBERON_0035946 .
              pmbb:dob1 a efo:EFO_0004950 .
              pmbb:dob1 obo:IAO_0000136 pmbb:birth1 .
              pmbb:dob1 turbo:TURBO_0006510 '12/31/1968' .
              pmbb:dob1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              
              pmbb:birth2 a obo:UBERON_0035946 .
              pmbb:dob2 a efo:EFO_0004950 .
              pmbb:dob2 obo:IAO_0000136 pmbb:birth2 .
          }
          Graph pmbb:conclusions {
          
              pmbb:concludedBirth a efo:EFO_0004950 .
              pmbb:concludedBirth obo:IAO_0000136 pmbb:birth1 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBirthsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all births are conclusionated - one romk, missing conclusionation")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              pmbb:birth1 a obo:UBERON_0035946 .
              pmbb:dob1 a efo:EFO_0004950 .
              pmbb:dob1 obo:IAO_0000136 pmbb:birth1 .
              pmbb:dob1 turbo:TURBO_0006510 '12/31/1968' .
              pmbb:dob1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              
              pmbb:birth2 a obo:UBERON_0035946 .
              pmbb:dob2 a efo:EFO_0004950 .
              pmbb:dob2 obo:IAO_0000136 pmbb:birth2 .
          }
          Graph pmbb:conclusions {
              
              pmbb:romk a obo:OBI_0000852 .
              pmbb:romk obo:IAO_0000136 pmbb:birth2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBirthsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all births are conclusionated - conclusionated has invalid type")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              pmbb:birth1 a obo:UBERON_0035946 .
              pmbb:dob1 a efo:EFO_0004950 .
              pmbb:dob1 obo:IAO_0000136 pmbb:birth1 .
              pmbb:dob1 turbo:TURBO_0006510 '12/31/1968' .
              pmbb:dob1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              
              pmbb:birth2 a obo:UBERON_0035946 .
              pmbb:dob2 a efo:EFO_0004950 .
              pmbb:dob2 obo:IAO_0000136 pmbb:birth2 .
          }
          Graph pmbb:conclusions {
          
              pmbb:concludedBirth a turbo:notAType .
              pmbb:concludedBirth obo:IAO_0000136 pmbb:birth1 .
              
              pmbb:romk a obo:OBI_0000852 .
              pmbb:romk obo:IAO_0000136 pmbb:birth2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBirthsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all births are conclusionated - romk has invalid type")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              pmbb:birth1 a obo:UBERON_0035946 .
              pmbb:dob1 a efo:EFO_0004950 .
              pmbb:dob1 obo:IAO_0000136 pmbb:birth1 .
              pmbb:dob1 turbo:TURBO_0006510 '12/31/1968' .
              pmbb:dob1 turbo:TURBO_0006511 '12/31/1968'^^xsd:date .
              
              pmbb:birth2 a obo:UBERON_0035946 .
              pmbb:dob2 a efo:EFO_0004950 .
              pmbb:dob2 obo:IAO_0000136 pmbb:birth2 .
          }
          Graph pmbb:conclusions {
          
              pmbb:concludedBirth a efo:EFO_0004950 .
              pmbb:concludedBirth obo:IAO_0000136 pmbb:birth1 .
              
              pmbb:romk a turbo:notAType .
              pmbb:romk obo:IAO_0000136 pmbb:birth2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBirthsAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all biosex's are conclusionated - two with conclusions")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:consenter1 a turbo:TURBO_0000502 .
              pmbb:consenter1 obo:RO_0000086 pmbb:biosex1 .
              pmbb:consenter2 a turbo:TURBO_0000502 .
              pmbb:consenter2 obo:RO_0000086 pmbb:biosex2 .
              
              pmbb:biosex1 a obo:PATO_0000047 .
              
              pmbb:biosex2 a obo:PATO_0000047 .
              pmbb:gid1 a obo:OMRSE_00000141 .
              pmbb:gid1 turbo:TURBO_0006510 'M' .
              pmbb:gid1 obo:IAO_0000136 pmbb:consenter1 .
          }
          Graph pmbb:conclusions {
              
              pmbb:biosex1 a obo:PATO_0000047 .
              pmbb:biosex1 turbo:TURBO_0006501 'true'^^xsd:boolean .
              
              pmbb:biosex2 a obo:PATO_0000384 .
              pmbb:biosex2 turbo:TURBO_0006501 'true'^^xsd:boolean .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBiosexAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (true)
    }
    
    test("all biosex's are conclusionated - generic biosex missing conclusion")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:consenter1 a turbo:TURBO_0000502 .
              pmbb:consenter1 obo:RO_0000086 pmbb:biosex1 .
              pmbb:consenter2 a turbo:TURBO_0000502 .
              pmbb:consenter2 obo:RO_0000086 pmbb:biosex2 .
              
              pmbb:biosex1 a obo:PATO_0000047 .
              
              pmbb:biosex2 a obo:PATO_0000047 .
              pmbb:gid1 a obo:OMRSE_00000141 .
              pmbb:gid1 turbo:TURBO_0006510 'M' .
              pmbb:gid1 obo:IAO_0000136 pmbb:consenter1 .
          }
          Graph pmbb:conclusions {
              
              pmbb:biosex2 a obo:PATO_0000384 .
              pmbb:biosex2 turbo:TURBO_0006501 'true'^^xsd:boolean .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBiosexAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all biosex's are conclusionated - conclusionated biosex missing")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:consenter1 a turbo:TURBO_0000502 .
              pmbb:consenter1 obo:RO_0000086 pmbb:biosex1 .
              pmbb:consenter2 a turbo:TURBO_0000502 .
              pmbb:consenter2 obo:RO_0000086 pmbb:biosex2 .
              
              pmbb:biosex1 a obo:PATO_0000047 .
              
              pmbb:biosex2 a obo:PATO_0000047 .
              pmbb:gid1 a obo:OMRSE_00000141 .
              pmbb:gid1 turbo:TURBO_0006510 'M' .
              pmbb:gid1 obo:IAO_0000136 pmbb:consenter1 .
          }
          Graph pmbb:conclusions {
              
              pmbb:biosex1 a obo:PATO_0000047 .
              pmbb:biosex1 turbo:TURBO_0006501 'true'^^xsd:boolean .
              
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBiosexAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("all biosex's are conclusionated - one conclusion is invalid type")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:consenter1 a turbo:TURBO_0000502 .
              pmbb:consenter1 obo:RO_0000086 pmbb:biosex1 .
              pmbb:consenter2 a turbo:TURBO_0000502 .
              pmbb:consenter2 obo:RO_0000086 pmbb:biosex2 .
              
              pmbb:biosex1 a obo:PATO_0000047 .
              
              pmbb:biosex2 a obo:PATO_0000047 .
              pmbb:gid1 a obo:OMRSE_00000141 .
              pmbb:gid1 turbo:TURBO_0006510 'M' .
              pmbb:gid1 obo:IAO_0000136 pmbb:consenter1 .
          }
          Graph pmbb:conclusions {
              
              pmbb:biosex1 a turbo:notAType .
              pmbb:biosex1 turbo:TURBO_0006501 'true'^^xsd:boolean .
              
              pmbb:biosex2 a obo:PATO_0000384 .
              pmbb:biosex2 turbo:TURBO_0006501 'true'^^xsd:boolean .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.allBiosexAreConclusionated(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }*/
    
    test("hc encs with multiple dates - none present")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:hc1 a obo:OGMS_0000097 .
              pmbb:encstart1 obo:RO_0002223 pmbb:hc1 .
              pmbb:encstart1 a turbo:TURBO_0000511 .
              
              pmbb:encdate1 a turbo:TURBO_0000512 .
              pmbb:encdate1 obo:IAO_0000136 pmbb:encstart1 .
              
              pmbb:hc2 a obo:OGMS_0000097 .
              pmbb:encstart2 obo:RO_0002223 pmbb:hc2 .
              pmbb:encstart2 a turbo:TURBO_0000511 .
              
              pmbb:encdate2 a turbo:TURBO_0000512 .
              pmbb:encdate2 obo:IAO_0000136 pmbb:encstart2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noHealthcareEncountersWithMultipleDates(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (true)
    }
    
    test("hc encs with multiple dates - one with 2 dates")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:hc1 a obo:OGMS_0000097 .
              pmbb:encstart1 obo:RO_0002223 pmbb:hc1 .
              pmbb:encstart1 a turbo:TURBO_0000511 .
              
              pmbb:encdate1 a turbo:TURBO_0000512 .
              pmbb:encdate1 obo:IAO_0000136 pmbb:encstart1 .
              
              pmbb:hc2 a obo:OGMS_0000097 .
              pmbb:encstart2 obo:RO_0002223 pmbb:hc2 .
              pmbb:encstart2 a turbo:TURBO_0000511 .
              
              pmbb:encdate2 a turbo:TURBO_0000512 .
              pmbb:encdate2 obo:IAO_0000136 pmbb:encstart2 .
              
              pmbb:encdate3 a turbo:TURBO_0000512 .
              pmbb:encdate3 obo:IAO_0000136 pmbb:encstart2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noHealthcareEncountersWithMultipleDates(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
    
    test("bb encs with multiple dates - none present")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:bb1 a turbo:TURBO_0000527 .
              pmbb:encstart1 obo:RO_0002223 pmbb:bb1 .
              pmbb:encstart1 a turbo:TURBO_0000531 .
              
              pmbb:encdate1 a turbo:TURBO_0000532 .
              pmbb:encdate1 obo:IAO_0000136 pmbb:encstart1 .
              
              pmbb:bb2 a turbo:TURBO_0000527 .
              pmbb:encstart2 obo:RO_0002223 pmbb:bb2 .
              pmbb:encstart2 a turbo:TURBO_0000531 .
              
              pmbb:encdate2 a turbo:TURBO_0000532 .
              pmbb:encdate2 obo:IAO_0000136 pmbb:encstart2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noBiobankEncountersWithMultipleDates(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (true)
    }
    
    test("bb encs with multiple dates - one with 2 dates")
    {
        val insert: String = 
        """
        INSERT DATA {
        
          Graph pmbb:expanded {
              
              pmbb:bb1 a turbo:TURBO_0000527 .
              pmbb:encstart1 obo:RO_0002223 pmbb:bb1 .
              pmbb:encstart1 a turbo:TURBO_0000531 .
              
              pmbb:encdate1 a turbo:TURBO_0000532 .
              pmbb:encdate1 obo:IAO_0000136 pmbb:encstart1 .
              
              pmbb:bb2 a turbo:TURBO_0000527 .
              pmbb:encstart2 obo:RO_0002223 pmbb:bb2 .
              pmbb:encstart2 a turbo:TURBO_0000531 .
              
              pmbb:encdate2 a turbo:TURBO_0000532 .
              pmbb:encdate2 obo:IAO_0000136 pmbb:encstart2 .
              
              pmbb:encdate3 a turbo:TURBO_0000532 .
              pmbb:encdate3 obo:IAO_0000136 pmbb:encstart2 .
          }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        postcheck.noBiobankEncountersWithMultipleDates(cxn, "http://www.itmat.upenn.edu/biobank/conclusions", "testing") should be (false)
    }
}