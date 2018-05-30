package edu.upenn.turbo

import java.io.FileOutputStream
import java.net.URL
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.scalatest.BeforeAndAfter
import org.scalatest._
//import com.example.incubator.karmaClientHelper

// write more tests checking for processes, inputs and outputs

// test if  shortcut predicates and  classes have been matched properly

/*
 * gathering bests bits from:
- /turbo_drivetrain/src/test/scala/com/example/app/EncExp.scala
    - full suite of tests on encounter expansion... still relevant in any way?
- /turbo_drivetrain/src/test/scala/com/example/app/EncPartComfortZone.scala
- /turbo_drivetrain/src/test/scala/com/example/app/EncWithPartExp.scala
- /turbo_drivetrain/src/test/scala/com/example/app/full5tests.scala
- /turbo_drivetrain/src/test/scala/com/example/app/FullStackPostReorg.scala
- /turbo_drivetrain/src/test/scala/com/example/app/FullStackPostReorgSparql.scala
- /turbo_drivetrain/src/test/scala/com/example/app/HaydensBack.scala
- /turbo_drivetrain/src/test/scala/com/example/app/Helper4
import edu.upenn.turbo.Helper4SparqlTests
import edu.upenn.turbo.FullStackPostReorgSparqlSparqlTests.scala
    - handy methods for converting strings and sparql results into models
    - only useful if we knwo what the URIs are going to be
    - have moved on to other approaches now that we are creating UUID URIs in real time
- /turbo_drivetrain/src/test/scala/com/example/app/PartExpExceptionTests.scala
    - ?
- /turbo_drivetrain/src/test/scala/com/example/app/PostInstantiationCleanupTests.scala
- /turbo_drivetrain/src/test/scala/com/example/app/SparqlPartRefTrackTest.scala

eventually move into /turbo_drivetrain/src/test/scala/com/example/app/CorrectlyPackagedTests.scala

when are processes required?  input and output?
no separate process for cleanup/expansion?
container for non-data items from instantiation?
rdfs labels?

phases
publish (shortcut) triples in karma, outside of drivetrain (or could do it as a karma service, or try with ontorefine?)
load
checks on shortcut triples
expand, uniquify, cleanup?
any checks?
referent track
conclusionate

break up tests as much as possible, but focus on data edge cases for now (early October)
provide static data at each intermediate stage

make text/json -> reference SoM methods

 */

//logging levels... see https://jessehu.wordpress.com/2009/11/17/log4j-levels-all-trace-debug-info-warn-error-fatal-off/
// debug, error, info, trace, warn
//          // MOST VERBOSE
//        logger.trace("trace")
// SHOULD I USE DEBUG AND SURPRESS HAYDEN'S INFOs?
//        logger.debug("debug")
// HAYDEN USES INFO
//        logger.info("info")
//        logger.warn("warn")
//        logger.error("error")
// LEAST VERBOSE

// configure in src/main/resources/log4j.properties ??
// but aren't we using slf4j on top of logback?!

// one way to dump from a REPO (embedded?) to a RDF file
//    var out = new FileOutputStream("file.rdf") 
//    Rio.write(repo, out, RDFFormat.RDFXML);

//what do we have to work with?
//rdf file, like turtle
//string representation of rdf contents (streams/buffers/array lists)
//RDF4J repo and connection
//RDF4J IUIs, statements, rdf model 
//  with java/scala conversion helpers, can loop for(statement <- model)
//sparql select -> binding set, with .getvalue method
//sparql ask -> boolean
//sparql construct -> writer/stream/file
//map, list of lists, etc.

class WheatNotChaff extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  val biosexConcInst: BiosexConclusionator = new BiosexConclusionator()
  val reftrackInst: ReferentTracker = new ReferentTracker()
  val biosexConclusionateInst: BiosexConclusionator = new BiosexConclusionator()
  val birthdateConclusionateInst: BirthdateConclusionator = new BirthdateConclusionator()
  val expand: Expander = new Expander()

  val connect: ConnectToGraphDB = new ConnectToGraphDB()
  val sparqlChecks = new DrivetrainSparqlChecks()
  val postchecks = new SparqlPostExpansionChecks()
  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null

  //  //this is the flag of whether to keep or delete triples after the run
  //  val deleteTriplesOrDatabaseFileAfterRun: Boolean = false

  //   TODO: give some thought to making a new helper class ?
  val SparqlHelper = new FullStackPostReorgSparql()
  
  before {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
  }

  after {
    //Setting "false" here will cause triples to stay in database after termination of the test
    connect.closeGraphConnection(cxn, repoManager, repository, false)
  }

  test("LoadWorked old style") {

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    // METHOD:  graph (not sparql) operation

    var HelperInstance = new Helper4SparqlTests
    var initialStmtCt = HelperInstance.CountTriples(cxn, false)

    //    logger.warn("precount: " + initialStmtCt.toString)

    val UpdateStatement = """
      prefix : <http://example.com/>
      insert data {
      :statement :was :inserted
      }
      """

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()

    var subsequentStmtCt = HelperInstance.CountTriples(cxn, false)

    //    logger.warn("postcount: " + subsequentStmtCt.toString)

    // are there more triples in the database after the insert?
    assert(subsequentStmtCt > initialStmtCt, "doesn't look like any triples were added")

  }

  test("Insert Worked") {

    // is triplestore able to receive and return triples?
    // Previously had done this as a RDF4J graph operation

    // any reason these triples should be drivetrain related in any way?

    // assumes triplestore is empty
    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    val UpdateStatement = """
      prefix : <http://example.com/>
      insert data {
      :statement :was :inserted
      }
      """

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()
    // insert and check

    val QueryStatement = """
      select * {?s ?p ?o}
      """

    val PreppedResult = cxn.prepareTupleQuery(QueryLanguage.SPARQL, QueryStatement)
    val QueryResult = PreppedResult.evaluate()

    val QueryStructure = SparqlHelper.Res2Set(QueryResult)

    val ExpectedStructure = Set(Map(
      "s" -> "http://example.com/statement",
      "p" -> "http://example.com/was",
      "o" -> "http://example.com/inserted"))

    assert(QueryStructure.equals(ExpectedStructure), "...couldn't confirm the insertion of a single triple")

  }

  test("expand complete encounter") {

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    //  \turbo\data\csv\handcrafted_encs.csv

    // turbo\karma_models\encounters_with_shortcuts-model.ttl

    // tersified with easyrdf converter web site, prefixes fixed
    // TO-DO add prefix constant to class and concatenate

    val UpdateStatement = """
INSERT DATA {
  GRAPH turbo:healthcareEncounterShortcuts {
<http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095>
  turbo:TURBO_0000649 "ICD-10" ;
  turbo:TURBO_0000661 "J44.9" ;
  turbo:TURBO_0000645 "2015-12-05" ;
  turbo:TURBO_0000648 "102" ;
  turbo:TURBO_0000643 "handcrafted_encs.csv" ;
  turbo:TURBO_0000644 "12/05/2015" ;
  a obo:OGMS_0000097 ;
  turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^xsd:anyURI .

<http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e>
  turbo:TURBO_0000648 "103" ;
  turbo:TURBO_0000645 "2015-11-25" ;
  turbo:TURBO_0000649 "ICD-9" ;
  turbo:TURBO_0000643 "handcrafted_encs.csv" ;
  a obo:OGMS_0000097 ;
  turbo:TURBO_0000661 "602.9" ;
  turbo:TURBO_0000644 "11/25/2015" ;
  turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890"^^xsd:anyURI .

<http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380>
  turbo:TURBO_0000643 "handcrafted_encs.csv" ;
  turbo:TURBO_0000649 "ICD-10" ;
  a obo:OGMS_0000097 ;
  turbo:TURBO_0000648 "102" ;
  turbo:TURBO_0000645 "2015-12-05" ;
  turbo:TURBO_0000644 "12/05/2015" ;
  turbo:TURBO_0000661 "I50.9" ;
  turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^xsd:anyURI .
}}
"""

    cxn.prepareUpdate(QueryLanguage.SPARQL, sparqlPrefixes + UpdateStatement).execute()
    // insert and check

    // expand

    //HAYDEN 10/13 supplying IRI
    val f: ValueFactory = cxn.getValueFactory()
    expand.encounterExpansion(cxn, f.createIRI("http://transformunify.org/ontologies/R2Rinst1"), "shortcut graphs here")

    helper.moveDataFromOneNamedGraphToAnother(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "http://www.itmat.upenn.edu/biobank/expanded")
    
    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear graph <http://transformunify.org/ontologies/participantShortcuts>").execute()
    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear graph <http://transformunify.org/ontologies/healthcareEncounterShortcuts>").execute()

    //HAYDEN 10/16 4:22 pm changing to call method in application
    //helper.applyInverses(cxn)

    DumpRepoToFile(cxn, "MAM_dumps/expanded_encs.ttl")

    val QueryStatement = """
PREFIX obo: <http://purl.obolibrary.org/obo/>
PREFIX turbo: <http://transformunify.org/ontologies/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
select 
?previousUriText ?EncID_LV ?dsTitle ?encDateTextVal ?encDateDateVal ?diagCodeLV ?diagCodeRegURI ?diagCodeRegTextVal
where     {
    GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
        ?Encounter1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OGMS_0000097 .
        ?Encounter1 turbo:TURBO_0006601 ?previousUriText .
        ?Encounter1 turbo:member ?OutpContainer .
        ?EncID1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:EncounterID .
        ?EncID1 obo:IAO_0000219 ?Encounter1 .
        ?EncID1 turbo:TURBO_0006510 ?EncID_LV .
        ?Dataset1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:IAO_0000100 .
        ?Dataset1 obo:BFO_0000051 ?EncID1 .
        ?Dataset1 <http://purl.org/dc/elements/1.1/title> ?dsTitle .
        ?Instantiation1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
        ?Instantiation1 obo:OBI_0000293 ?Dataset1 .
        ?Instantiation1 obo:OBI_0000299 ?OutpContainer .
        ?Instantiation1 rdfs:label "Inst/Exp Proc" .
        ?OutpContainer a turbo:Container .
        ?OutpContainer rdfs:label "Inst/Exp Outp Cont" .
        ?EncDate1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:ProcStartTimeMeas .
        ?EncDate1 turbo:TURBO_0006512 ?encDateTextVal .
        ?EncDate1 turbo:TURBO_0006511 ?encDateDateVal .
        ?EncDate1 obo:IAO_0000136 ?EncStart1 .
        ?Dataset1 obo:BFO_0000051 ?EncDate1 .
        ?EncStart1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        ?EncStart1 obo:RO_0002223 ?Encounter1 .
        ?EncStart1 turbo:member ?OutpContainer .
        ?Encounter1 obo:OBI_0000299 ?DiagCrid1 .
        ?Dataset1 obo:BFO_0000051 ?DiagCodeRegID1 .
        ?Dataset1 obo:BFO_0000051 ?DiagCodeSymb1 .
        ?DiagCrid1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000553 .
        ?DiagCrid1 obo:BFO_0000051 ?DiagCodeRegID1 .
        ?DiagCrid1 obo:BFO_0000051 ?DiagCodeSymb1 .
        ?DiagCrid1 turbo:member ?OutpContainer .
        ?DiagCodeSymb1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000554 .
        ?DiagCodeSymb1 turbo:TURBO_0006510 ?diagCodeLV .
        ?DiagCodeRegID1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000555 .
        ?DiagCodeRegID1 obo:IAO_0000219 ?diagCodeRegURI .
        ?DiagCodeRegID1 turbo:TURBO_0006512 ?diagCodeRegTextVal .
    }
}
      """

    val PreppedResult = cxn.prepareTupleQuery(QueryLanguage.SPARQL, QueryStatement)
    val QueryResult = PreppedResult.evaluate()

    val QueryStructure = SparqlHelper.Res2Set(QueryResult)

    //println(QueryStructure.toString())

    val ExpectedStructure = Set(
      Map(
        """previousUriText""" -> """"http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeRegURI""" -> """http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892""",
        """encDateDateVal""" -> """"2015-12-05"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """encDateTextVal""" -> """"12/05/2015"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeRegTextVal""" -> """"ICD-10"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """EncID_LV""" -> """"102"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dsTitle""" -> """"handcrafted_encs.csv"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"I50.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """previousUriText""" -> """"http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeRegURI""" -> """http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892""",
        """encDateDateVal""" -> """"2015-12-05"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """encDateTextVal""" -> """"12/05/2015"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeRegTextVal""" -> """"ICD-10"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """EncID_LV""" -> """"102"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dsTitle""" -> """"handcrafted_encs.csv"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"J44.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """previousUriText""" -> """"http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeRegURI""" -> """http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890""",
        """encDateDateVal""" -> """"2015-11-25"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """encDateTextVal""" -> """"11/25/2015"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeRegTextVal""" -> """"ICD-9"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """EncID_LV""" -> """"103"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dsTitle""" -> """"handcrafted_encs.csv"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"602.9"^^<http://www.w3.org/2001/XMLSchema#string>"""))

    // if the overall expansion test fails, we could do tiny incremental tests:  
    // are there any encounters in teh expanded graph?  are they denoted by antyhing?
    // are those denoting things encounter ids?
    // etc.

    tinySparqlTest("""
ask {
?encid a turbo:EncounterID  .
	} """, "did not find any encounter identifiers", true)

    tinySparqlTest("""
ask {
?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" .
	} """, "did not find an encounter identifier with the value of 102", true)

    //    tinySparqlTest("""
    //    ask {
    //    ?encid a turbo:EncounterID ;
    //    	turbo:TURBO_0006510 "102" ;
    //    	rdfs:label ?lab .
    //    	} """, "encounter identifier with the value of 102 has no label", true)

    tinySparqlTest("""
ask {
?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  } """, "identifier with the value of 102 doesn't denote anything", true)

    tinySparqlTest("""
ask {
?encinst a obo:OGMS_0000097  .

?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  } """, "identifier with the value of 102 doesn't denote any encounter", true)

    tinySparqlTest("""
ask {
?encinst a obo:OGMS_0000097  ;
	obo:OBI_0000299 ?diagcode .
?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  } """, "encounter denoted by an identifier with the value of 102 doesn't have any output", true)

    tinySparqlTest("""
ask {
?diagCrid a turbo:TURBO_0000553 .

?encinst a obo:OGMS_0000097  ;
	obo:OBI_0000299 ?diagCrid .

?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  } """, "the output of the encounter denoted by an identifier with the value of 102 isn't a diagnosis crid", true)

    tinySparqlTest("""
ask {

?diagCrid a turbo:TURBO_0000553 ;
obo:BFO_0000051 ?diagcode .

?diagcode a turbo:TURBO_0000554 .

?encinst a obo:OGMS_0000097  ;
	obo:OBI_0000299 ?diagCrid .

?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  
	
	} """, "the diagnosis crid doesn't have a diagnosis code as a part", true)

    tinySparqlTest("""
ask {

?diagCrid a turbo:TURBO_0000553 ;
obo:BFO_0000051 ?diagcode .

?diagcode a turbo:TURBO_0000554 ;
turbo:TURBO_0006510 "J44.9" .

?encinst a obo:OGMS_0000097  ;
	obo:OBI_0000299 ?diagCrid .

?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  
	
	} """, "the diagnosis code  doesn't have a literal value of J44.9", true)

    tinySparqlTest("""
ask {

?diagreg  a turbo:TURBO_0000555  .

?diagCrid a turbo:TURBO_0000553 ;
obo:BFO_0000051 ?diagcode, ?diagreg .

?diagcode a turbo:TURBO_0000554 ;
turbo:TURBO_0006510 "J44.9" 	.

?encinst a obo:OGMS_0000097  ;
	obo:OBI_0000299 ?diagCrid .

?encid a turbo:EncounterID ;
	turbo:TURBO_0006510 "102" ;
	obo:IAO_0000219 ?encinst .  

  } """, "the diagnosis crid doesn't also have a registry label as a part", true)

    tinySparqlTest("""
    ask {
    
    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  
    
      } """, "the diagnosis registry label doesn't have ICD-10 as its turbo:TURBO_0006512", true)

    tinySparqlTest("""
    ask {
    
    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  
    
      } """, "the diagnosis registry label doesn't have denote the ICD9 entity from the NCIT", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  .

  } """, "the encounter id, the diagnosis code and the diagnosis code registry label aren't part of anything ", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  
    	
?dataset rdf:type              obo:IAO_0000100 ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  .
              
  } """, "the encounter id, the diagnosis code and the diagnosis code registry label aren't part of a dataset ", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  .
              
  } """, "the dataset isn't entitled handcrafted_encs.csv", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas .
              
  } """, "the dataset doesn't also have a timestamp as a part", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" .
              
  } """, "the timestamp doesn't have a date value of 2015-12-05", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" ;
              turbo:TURBO_0006512  "12/05/2015" .
              
  } """, "the timestamp doesn't have a textual value of 12/05/2015", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" ;
              turbo:TURBO_0006512  "12/05/2015" ;
              obo:IAO_0000136       ?encstart .
              
  } """, "the timestamp isn't about anything", true)

    tinySparqlTest("""

ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" ;
              turbo:TURBO_0006512  "12/05/2015" ;
              obo:IAO_0000136       ?encstart .
              
?encstart  rdf:type             turbo:TURBO_0000511 .
              
  } """, "the timestamp isn't about a temporal boundary", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" ;
              turbo:TURBO_0006512  "12/05/2015" ;
              obo:IAO_0000136       ?encstart .
              
?encstart  rdf:type             turbo:TURBO_0000511  ;
              obo:RO_0002223        ?encinst .
              
  } """, "the temporal boundary doesn't start the encounter process", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" ;
              turbo:TURBO_0006512  "12/05/2015" ;
              obo:IAO_0000136       ?encstart .
              
?encstart  rdf:type             turbo:TURBO_0000511  ;
              obo:RO_0002223        ?encinst .
              
?instproc  rdf:type             turbo:TURBO_0000522 .              
              
  } """, "there's no instantiation process", true)

    tinySparqlTest("""
ask {

    ?diagreg  a turbo:TURBO_0000555  ;
                  turbo:TURBO_0006512  "ICD-10" ;
     obo:IAO_0000219  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
    
    ?diagCrid a turbo:TURBO_0000553 ;
    obo:BFO_0000051 ?diagcode, ?diagreg .
    
    ?diagcode a turbo:TURBO_0000554 ;
    turbo:TURBO_0006510 "J44.9" 	.
    
    ?encinst a obo:OGMS_0000097  ;
    	obo:OBI_0000299 ?diagCrid .
    
    ?encid a turbo:EncounterID ;
    	turbo:TURBO_0006510 "102" ;
    	obo:IAO_0000219 ?encinst .  

?dataset rdf:type              obo:IAO_0000100 ;
              dc11:title            "handcrafted_encs.csv" ;
              obo:BFO_0000051       ?encid ;
              obo:BFO_0000051       ?diagcode ;
              obo:BFO_0000051       ?diagreg  ;
              obo:BFO_0000051       ?encstamp  .
              
?encstamp  rdf:type             turbo:ProcStartTimeMeas ;
              turbo:TURBO_0006511       "2015-12-05" ;
              turbo:TURBO_0006512  "12/05/2015" ;
              obo:IAO_0000136       ?encstart .
              
?encstart  rdf:type             turbo:TURBO_0000511  ;
              obo:RO_0002223        ?encinst .
              
?instproc  rdf:type             turbo:TURBO_0000522 ;
              obo:OBI_0000293       ?dataset  .            
              
  } """, "the instantiation process doesn't have the tabular/relational dataset as input", true)

    assert(QueryStructure.equals(ExpectedStructure), "...failed to expand two rows of complete encounters")

  }

  test("encounter reftracking") {

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    // insert known-good, static, handcrafted expanded encounter triples

    val UpdateStatement = """
INSERT DATA {
  GRAPH pmbb:expanded {
<http://transformunify.org/ontologies/InstOutpCont>
  a <http://transformunify.org/ontologies/Container> ;
  rdfs:label "Inst/Exp Outp Cont" .

<http://transformunify.org/ontologies/dataset1>
  a <http://purl.obolibrary.org/obo/IAO_0000100> ;
  obo:BFO_0000051 <http://transformunify.org/ontologies/4fdb691a88f5486884a0c7967a0fb5f6>, <http://transformunify.org/ontologies/d248d9e2d3574867b1595b138fc67c1b>, <http://transformunify.org/ontologies/3228bdbaf8d14136b755746de3f2b75e>, <http://transformunify.org/ontologies/59ddaa80df10463392e73006d21ace10>, <http://transformunify.org/ontologies/022037501ea0404fb632709e76504a5c>, <http://transformunify.org/ontologies/e4ba9d1ac2a64b04a59753d73c81d25d>, <http://transformunify.org/ontologies/8409d2b2d98c49cbb0a026663a89e56b>, <http://transformunify.org/ontologies/5eb8f43833d34241baa4af38c4c208d5>, <http://transformunify.org/ontologies/3eb9bfe2aa5d46ec9a7a04153e402323>, <http://transformunify.org/ontologies/6ccab051ed6841a7afab34f80d970b64>, <http://transformunify.org/ontologies/e4e3ea8fd69c4544ae4ed07dc9f58a5e>, <http://transformunify.org/ontologies/af23c78dd44f4bb0bbf860c23f103115> ;
  dc11:title "handcrafted_encs.csv" .

<http://transformunify.org/ontologies/R2R_instantiation_process1>
  a <http://transformunify.org/ontologies/R2RInstantiation> ;
  obo:OBI_0000293 <http://transformunify.org/ontologies/dataset1> ;
  obo:OBI_0000299 <http://transformunify.org/ontologies/InstOutpCont> ;
  rdfs:label "Inst/Exp Proc" .

<http://transformunify.org/ontologies/0f19ba992cfc45e2ac292ee5874e2ae5>
  a obo:OGMS_0000097 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:OBI_0000299 turbo:b8b1291a39c1439f88889b96ee232f1c .

<http://transformunify.org/ontologies/4fdb691a88f5486884a0c7967a0fb5f6>
  a turbo:EncounterID ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/0f19ba992cfc45e2ac292ee5874e2ae5> ;
  turbo:TURBO_0006510 "102" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:d248d9e2d3574867b1595b138fc67c1b
  a turbo:ProcStartTimeMeas ;
  turbo:TURBO_0006512 "12/05/2015" ;
  turbo:TURBO_0006511 "2015-12-05" ;
  obo:IAO_0000136 turbo:013d8b328e8d4f1bae37a408932d18f7 ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:013d8b328e8d4f1bae37a408932d18f7
  a turbo:TURBO_0000511 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0002223 <http://transformunify.org/ontologies/0f19ba992cfc45e2ac292ee5874e2ae5> .

turbo:b8b1291a39c1439f88889b96ee232f1c
  a turbo:TURBO_0000553 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:BFO_0000051 turbo:3228bdbaf8d14136b755746de3f2b75e, turbo:59ddaa80df10463392e73006d21ace10 .

turbo:3228bdbaf8d14136b755746de3f2b75e
  a turbo:TURBO_0000555 ;
  obo:IAO_0000219 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> ;
  turbo:TURBO_0006512 "ICD-10" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1>, turbo:b8b1291a39c1439f88889b96ee232f1c .

turbo:59ddaa80df10463392e73006d21ace10
  a turbo:TURBO_0000554 ;
  turbo:TURBO_0006510 "J44.9" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1>, turbo:b8b1291a39c1439f88889b96ee232f1c .

<http://transformunify.org/ontologies/38d0de37213d45b4a1f9328c0c21eb13>
  a obo:OGMS_0000097 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:OBI_0000299 turbo:273f896efdba47ae8d1c13c8342aed31 .

<http://transformunify.org/ontologies/022037501ea0404fb632709e76504a5c>
  a turbo:EncounterID ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/38d0de37213d45b4a1f9328c0c21eb13> ;
  turbo:TURBO_0006510 "103" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:e4ba9d1ac2a64b04a59753d73c81d25d
  a turbo:ProcStartTimeMeas ;
  turbo:TURBO_0006512 "11/25/2015" ;
  turbo:TURBO_0006511 "2015-11-25" ;
  obo:IAO_0000136 turbo:3d022aa776a240089e3454cd2e2c1198 ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:3d022aa776a240089e3454cd2e2c1198
  a turbo:TURBO_0000511 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0002223 <http://transformunify.org/ontologies/38d0de37213d45b4a1f9328c0c21eb13> .

turbo:273f896efdba47ae8d1c13c8342aed31
  a turbo:TURBO_0000553 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:BFO_0000051 turbo:8409d2b2d98c49cbb0a026663a89e56b, turbo:5eb8f43833d34241baa4af38c4c208d5 .

turbo:8409d2b2d98c49cbb0a026663a89e56b
  a turbo:TURBO_0000555 ;
  obo:IAO_0000219 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
  turbo:TURBO_0006512 "ICD-9" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1>, turbo:273f896efdba47ae8d1c13c8342aed31 .

turbo:5eb8f43833d34241baa4af38c4c208d5
  a turbo:TURBO_0000554 ;
  turbo:TURBO_0006510 "602.9" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1>, turbo:273f896efdba47ae8d1c13c8342aed31 .

<http://transformunify.org/ontologies/ae0681495042444eb789a29f5933c0f5>
  a obo:OGMS_0000097 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:OBI_0000299 turbo:aface5961f48472f84a62b4de169c10f .

<http://transformunify.org/ontologies/3eb9bfe2aa5d46ec9a7a04153e402323>
  a turbo:EncounterID ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/ae0681495042444eb789a29f5933c0f5> ;
  turbo:TURBO_0006510 "102" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:6ccab051ed6841a7afab34f80d970b64
  a turbo:ProcStartTimeMeas ;
  turbo:TURBO_0006512 "12/05/2015" ;
  turbo:TURBO_0006511 "2015-12-05" ;
  obo:IAO_0000136 turbo:6c9331299fcf40349b09b64a698b50e3 ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:6c9331299fcf40349b09b64a698b50e3
  a turbo:TURBO_0000511 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0002223 <http://transformunify.org/ontologies/ae0681495042444eb789a29f5933c0f5> .

turbo:aface5961f48472f84a62b4de169c10f
  a turbo:TURBO_0000553 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:BFO_0000051 turbo:e4e3ea8fd69c4544ae4ed07dc9f58a5e, turbo:af23c78dd44f4bb0bbf860c23f103115 .

turbo:e4e3ea8fd69c4544ae4ed07dc9f58a5e
  a turbo:TURBO_0000555 ;
  obo:IAO_0000219 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> ;
  turbo:TURBO_0006512 "ICD-10" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1>, turbo:aface5961f48472f84a62b4de169c10f .

turbo:af23c78dd44f4bb0bbf860c23f103115
  a turbo:TURBO_0000554 ;
  turbo:TURBO_0006510 "I50.9" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1>, turbo:aface5961f48472f84a62b4de169c10f .
      }}
      """

    val WithPrefixes = sparqlPrefixes + UpdateStatement

    cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes).execute()

    reftrackInst.reftrackEncountersAndDependents(cxn)

    tinySparqlTest("""
ASK WHERE
{
    ?ds a obo:IAO_0000100 ;
        obo:BFO_0000051 ?i .
    ?i a <http://transformunify.org/ontologies/EncounterID> ;
       <http://purl.obolibrary.org/obo/IAO_0000219> ?rte ;
       <http://transformunify.org/ontologies/TURBO_0006510> "102" .
    ?rte <http://purl.obolibrary.org/obo/OBI_0000299> ?edc1 , ?edc2 ;
         <http://transformunify.org/ontologies/reftracked> "true"^^<http://www.w3.org/2001/XMLSchema#boolean> ;
                                                                   a <http://transformunify.org/ontologies/OGMS_0000097>  .
    ?instEnc1 <http://purl.obolibrary.org/obo/IAO_0000225> <http://purl.obolibrary.org/obo/IAO_0000226> ;
              <http://transformunify.org/ontologies/TURBO_0001700> ?rte ;
              a turbo:TURBO_0000907 .
    ?instEnc2 <http://purl.obolibrary.org/obo/IAO_0000225> <http://purl.obolibrary.org/obo/IAO_0000226> ;
              <http://transformunify.org/ontologies/TURBO_0001700> ?rte ;
              a turbo:TURBO_0000907 .
    filter (?edc1 != ?edc2 )
    filter (?instEnc1 != ?instEnc2 )    
}
""", "construct: the referent tracked encounter with id 102 should have two different outputs and should replace two differnet instantiated encounters ", true)

    //    tinySparqlTest("""
    //ask WHERE
    //{
    //    ?i a <http://transformunify.org/ontologies/EncounterID> ;
    //       <http://transformunify.org/ontologies/TURBO_0006510> "102" ;
    //                                                                      <http://purl.obolibrary.org/obo/IAO_0000219> ?rte .
    //    ?rte a <http://transformunify.org/ontologies/OGMS_0000097> ;
    //         <http://transformunify.org/ontologies/reftracked> "true"^^xsd:boolean .
    //    ?ds a obo:IAO_0000100 ;
    //        obo:BFO_0000051 ?i .
    //    ?rtProcUri a <http://transformunify.org/ontologies/referentTracking> ;
    //               <http://purl.obolibrary.org/obo/OBI_0000293> ?ds .
    //}
    //""", "there should be referent tracking proceses with input and output ", true)

    tinySparqlTest("""
ASK WHERE
  { 
?rte a <http://transformunify.org/ontologies/OGMS_0000097> ;
<http://transformunify.org/ontologies/reftracked> "true"^^<http://www.w3.org/2001/XMLSchema#boolean> .
	
  }
""", "there should be at least one reftracked encounter ", true)

    tinySparqlTest("""
ASK WHERE
{
    ?rte a <http://transformunify.org/ontologies/OGMS_0000097> .
    minus {
        ?rte a <http://transformunify.org/ontologies/OGMS_0000097> ;
             <http://transformunify.org/ontologies/reftracked> "true"^^<http://www.w3.org/2001/XMLSchema#boolean> .
    }
}
""", "all encounters should be reftracked now ", false)

    /*
 * do i really mean this?
 * the relationships between reatired instances get lost... should we keep them?
 * should retired encounter id placeholders denote retired encounters?
 */

    tinySparqlTest("""
ASK WHERE {
    ?i a <http://transformunify.org/ontologies/EncounterID> ;
       <http://purl.obolibrary.org/obo/IAO_0000219> ?rte .
    minus {
        ?i a <http://transformunify.org/ontologies/EncounterID> ;
           <http://purl.obolibrary.org/obo/IAO_0000219> ?rte .
        ?rte a <http://transformunify.org/ontologies/OGMS_0000097> .
    }
}
""", "encounter ids should only identify reftracked encounters ", false)

    tinySparqlTest("""
ASK WHERE {
    ?rte a <http://transformunify.org/ontologies/OGMS_0000097> ;
    turbo:TURBO_0001700 ?retired .
    
    ?retired a <http://transformunify.org/ontologies/OGMS_0000097> 
    	
      }
    """, "the thing that was replaced with a referent tracked encounter shouldn't itself be an encounter ", false)

    /*
 * do i really mean this?
 * this will only work if the database wasn't emptied after the last test
 * this is not an ask, so can't be directly rewritten as a tiny sparql test
 */

  }

  test("data integrity: should be one and only one data set") {

    val tupleQuery = cxn.prepareTupleQuery(QueryLanguage.SPARQL, """
select (count(?s) as ?scount)
where 
{
    ?s a <http://purl.obolibrary.org/obo/IAO_0000100>
}
group by ?s
  								""")
    val SparqlRes = tupleQuery.evaluate()
    val bindingSet = SparqlRes.next()
    val scount = bindingSet.getValue("scount").toString()

    assert(scount == "\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>",
      """the number of data sets != 1""")

  }

  /*
 * do i really mean this?
 * this will only work if the database wasn't emptied after the last test
 * this is not an ask, so can't be directly rewritten as a tiny sparql test
 */

  test("data integrity?: should be one and only one instantiation process") {

    val tupleQuery = cxn.prepareTupleQuery(QueryLanguage.SPARQL, """
prefix turbo: <http://transformunify.org/ontologies/> 
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
select (count(?s) as ?scount)
where 
{
    ?s a <http://transformunify.org/ontologies/R2RInstantiation>
}
group by ?s
  								""")
    val SparqlRes = tupleQuery.evaluate()
    val bindingSet = SparqlRes.next()
    val scount = bindingSet.getValue("scount").toString()

    assert(scount == "\"1\"^^<http://www.w3.org/2001/XMLSchema#integer>",
      """the number of instantiation processes != 1""")

  }

  /*
 * do i really mean this?
 * this will only work if the database wasn't emptied after the last test
 */

  test("data integrity: the dataset is specified input into the instantiation process") {

    val tupleQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, """
prefix turbo: <http://transformunify.org/ontologies/> 
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
ASK WHERE
{
    ?instProc a <http://transformunify.org/ontologies/R2RInstantiation> ;
              <http://purl.obolibrary.org/obo/OBI_0000293> ?dataSet .
    ?dataSet a <http://purl.obolibrary.org/obo/IAO_0000100> 
}
  								""")
    val AskRes = tupleQuery.evaluate()

    assert(AskRes, "there are no instantiation processes with a dataset as input")

  }

  // what about expansion and reftracking processes, etc.?  see above...

  /*
     * probably not relevant anymore
 * this will only work if the database wasn't emptied after the last test
 */

  test("cleanup: ?s can't be an instanceOf and a subClass of ?c") {

    //var CleanClassInst = new InstantiateKarmaData
    //CleanClassInst.karmaCleanup(cxn)

    val tupleQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, """
prefix turbo: <http://transformunify.org/ontologies/> 
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
ASK
WHERE
{
    graph ?g {
        ?s a ?c .
        ?s rdfs:subClassOf ?c .
        }
        filter (?g != pmbb:ontology)
}
  								""")
    val AskRes = tupleQuery.evaluate()

    assert(!AskRes, "some entities ?s are considerd both an instance of and a subClass of some ?c")

  }

  test("encounters have the expected previous URI strings") {

    /*
     * this is probably not the best way to say "this is exactly what i expect to see"
     * see Res2Set method
 */

    var CurrentExpansionAskCore = """
ASK WHERE
  { GRAPH <http://www.itmat.upenn.edu/biobank/expanded>
      { FILTER ( ?count = 3 )
        { SELECT  (COUNT(?NewEnc) AS ?count)
          WHERE
            { VALUES ?previousUriTextVal { 
            "http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095" 
            "http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e" 
            "http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380" }
              ?NewEnc  a                     <http://transformunify.org/ontologies/OGMS_0000097> ;
                       <http://transformunify.org/ontologies/TURBO_0006601>  ?previousUriTextVal
            }
        }
      }
  }
 """

    var CurrentExpansionAsk = sparqlPrefixes + CurrentExpansionAskCore

    var AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, CurrentExpansionAsk).evaluate()

    assert(AskRes == true, "the encounters don't have the expected previous URI values")

  }

  // it doesn't seem like we're testing for the pre-referent tracking uri... is that possible?

  // no tests of degenearte cases up to this point

  test("turbo:TURBO_0000648 expansion") {

    // test with sparql
    var CurrentExpansionAskCore = """
ASK WHERE
{
    GRAPH <http://www.itmat.upenn.edu/biobank/expanded>
    {
        FILTER ( ?count = 3 )
        {
            SELECT  (COUNT(?NewEnc) AS ?count)
            WHERE
            {
                VALUES (?previousUriText ?EncIDVal ) {
                    ("http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095" "102" ) 
                    ("http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e" "103" )
                    ("http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380" "102" )
                }
                ?NewEnc a  <http://transformunify.org/ontologies/OGMS_0000097> .
                ?NewEnc <http://transformunify.org/ontologies/TURBO_0006601> ?previousUriText .
                ?EncID a <http://transformunify.org/ontologies/EncounterID> .
                ?EncID <http://purl.obolibrary.org/obo/IAO_0000219> ?NewEnc .
                ?EncID <http://transformunify.org/ontologies/TURBO_0006510> ?EncIDVal .
            }
        }
    }
}
 """

    var CurrentExpansionAsk = sparqlPrefixes + CurrentExpansionAskCore

    var AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, CurrentExpansionAsk).evaluate()

    assert(AskRes == true, "the encounter ID shortcuts have not been expanded properly")

  }

  test("TURBO_0000661 expansion") {

    var CurrentExpansionAskCore = """
ASK WHERE
{
    GRAPH <http://www.itmat.upenn.edu/biobank/expanded>
    {
        FILTER ( ?count = 3 )
        {
            SELECT  (COUNT( ?NewEnc) AS ?count)
            WHERE
            {
                VALUES (?previousUriTextVal ?DiagSymbVal ) {
                    ("http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095" "J44.9" ) 
                    ("http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e" "602.9" )
                    ("http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380" "I50.9" )
                }
                ?NewEnc   a                     <http://transformunify.org/ontologies/OGMS_0000097> ;
                          <http://transformunify.org/ontologies/TURBO_0006601>  ?previousUriTextVal ;
                          <http://purl.obolibrary.org/obo/OBI_0000299>  ?DiagCrid .
                ?DiagCrid  a                    <http://transformunify.org/ontologies/DiagCrid> ;
                           <http://purl.obolibrary.org/obo/BFO_0000051>  ?DiagSymb .
                ?DiagSymb  a                    <http://transformunify.org/ontologies/EncounterDiagCodeSymbol> ;
                           <http://transformunify.org/ontologies/TURBO_0006510>  ?DiagSymbVal
            }
        }
    }
}
  """

    var CurrentExpansionAsk = sparqlPrefixes + CurrentExpansionAskCore

    var AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, CurrentExpansionAsk).evaluate()

    assert(AskRes == true, "the diagnosis code symbol shortcuts have not been expanded properly")

  }

  // like i said before, the Res2Set methods is a better way to test "i expect this exactly"
  // this finds 5 rows with two distinct new encounter IDs because one of the encounters only has ICD10 encounter codes
  // where block still relevant

  test("turbo:TURBO_0000649 expansion") {

    var CurrentExpansionAskCore = """
ASK WHERE
{
    GRAPH <http://www.itmat.upenn.edu/biobank/expanded>
    {
        FILTER ( ?count = 2 )
        {
            SELECT  (COUNT(distinct ?NewEnc) AS ?count)
            WHERE
            {
                VALUES (?previousUriText ?DiagCodeRegText ) {
                    ("http://transformunify.org/ontologies/encounter/30425586c6ba476aa6a1d76897d93095" "ICD-10" ) 
                    ("http://transformunify.org/ontologies/encounter/3119269537cf4371b0a2ed518e726d5e" "ICD-9"  )
                    ("http://transformunify.org/ontologies/encounter/5ff4c000d94546529df085f79abb2380" "ICD-10" )
                }
                ?NewEnc a  <http://transformunify.org/ontologies/OGMS_0000097> .
                ?NewEnc    <http://transformunify.org/ontologies/TURBO_0006601> ?previousUriText .
                ?NewEnc    <http://purl.obolibrary.org/obo/OBI_0000299> ?DiagCrid .
                ?DiagCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/DiagCrid> .
                ?DiagCrid <http://purl.obolibrary.org/obo/BFO_0000051> ?DiagCodeRegDenoter .
                ?DiagCodeRegDenoter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/DiagCodeRegistryID> .
                ?DiagCodeRegDenoter <http://transformunify.org/ontologies/TURBO_0006512> ?DiagCodeRegText .
                ?DiagCodeRegDenoter <http://purl.obolibrary.org/obo/IAO_0000219> ?DiagCodeRegInstance .
            }
        }
    }
}
  """

    var CurrentExpansionAsk = sparqlPrefixes + CurrentExpansionAskCore

    var AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, CurrentExpansionAsk).evaluate()

    assert(AskRes == true, "the diagnosis code registry shortcuts have not been expanded properly")

  }

  test("comprehensive enc exp as ask") {

    var CurrentExpansionAskCore = """
ASK WHERE
{
    GRAPHpmbb:expanded
    {
        VALUES ( ?EncID_LV ?diagCodeLV ?diagCodeRegURI ?encDateMeasVal ?dsTitle ) {
            ( "102" "J44.9" <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> "2015-12-05" "handcrafted_encs.csv" )
            ( "103" "602.9" <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> "2015-11-25" "handcrafted_encs.csv" )
            ( "9102" "I50.9" <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> "2015-12-05" "handcrafted_encs.csv" )
        }
        ?EncDate1  rdf:type           :ProcStartTimeMeas ;
                   :TURBO_0006512    ?encDateTextVal ;
                   :TURBO_0006511     ?encDateMeasVal ;
                   obo:IAO_0000136     ?EncStart1 .
        ?Encounter1  rdf:type         obo:OGMS_0000097 ;
                     obo:OBI_0000299     ?DiagCrid1 .
        ?EncID1   rdf:type            :EncounterID ;
                  obo:IAO_0000219     ?Encounter1 ;
                  :TURBO_0006510  ?EncID_LV .
        ?EncStart1  rdf:type          :TURBO_0000511 ;
                    obo:RO_0002223      ?Encounter1 .
        ?DiagCrid1  rdf:type          :DiagCrid ;
                    obo:BFO_0000051     ?DiagCodeRegID1 ;
                    obo:BFO_0000051     ?DiagCodeSymb1 .
        ?Dataset1  rdf:type           obo:IAO_0000100 ;
                   obo:BFO_0000051     ?DiagCodeSymb1 ;
                   obo:BFO_0000051     ?EncDate1 ;
                   obo:BFO_0000051     ?EncID1 ;
                   dc11:title          ?dsTitle .
        ?Instantiation1
            rdf:type            :R2RInstantiation ;
            obo:OBI_0000293     ?Dataset1 .
        ?DiagCodeRegID1
            rdf:type            :DiagCodeRegistryID ;
            obo:IAO_0000219     ?diagCodeRegURI ;
            :TURBO_0006512    ?diagCodeRegTextVal .
        ?DiagCodeSymb1
            rdf:type            :EncounterDiagCodeSymbol ;
            :TURBO_0006510  ?diagCodeLV
    }
}
"""

    var CurrentExpansionAsk = sparqlPrefixes + CurrentExpansionAskCore

    var AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, CurrentExpansionAsk).evaluate()

    assert(AskRes == true, "the diagnosis code registry shortcuts have not been expanded properly")

  }

  test("turbo:TURBO_0000670 and subclasses defined (ontology loaded)") {

    // load ontology!
    //HAYDEN 1/11 updated to new ontology URL
    LoadOntology(repository, ontologyURL, "http://www.itmat.upenn.edu/biobank/ontology", "http://transformunify.org/ontologies/")

    val LingeringShortcutsAsk = """
PREFIX turbo: <http://transformunify.org/ontologies/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
ask where {
    graph pmbb:ontology {
        ?p rdfs:subPropertyOf turbo:TURBO_0000670 .
    }
}
"""

    val AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, LingeringShortcutsAsk).evaluate()

    assert(AskRes == true, "ontology doesn't appear to be loaded")
  }

  test("shorcuts cleared?") {
    val LingeringShortcutsAsk = """
PREFIX turbo: <http://www.itmat.upenn.edu/biobank/ontology/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
ask where {
    ?p rdfs:subPropertyOf turbo:TURBO_0000670 .
    ?s ?p ?o
}
"""

    val AskRes = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, LingeringShortcutsAsk).evaluate()

    assert(AskRes == false, "shortcut encounter triples are still in the triplestore")
  }

  test("participant expansion") {

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    //  \turbo\data\csv\handcrafted_parts.csv

    // turbo\karma_models\participants_GID_clarified_with_shortcuts-model.ttl

    // tersified with easyrdf converter web site, prefixes fixed
    // TO-DO add prefix constant to class and concatenate

    val UpdateStatement = """
PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>
PREFIX  turbo: <http://transformunify.org/ontologies/>

INSERT DATA {
  GRAPH pmbb:participantShortcuts {
<http://transformunify.org/ontologies/participant/ef2fec6605054d64bb235145dc09f924>
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000606 "M" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000608 "121" .

<http://transformunify.org/ontologies/participant/7e557b66e87a4994b5c4beb2f36e2c31>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000606 "M" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .

<http://transformunify.org/ontologies/participant/277efd6bd9504635b1f7ab12d952c62c>
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000604 "12/30/1971" .

<http://transformunify.org/ontologies/participant/1403749e36984e72832da89540afd4c3>
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  turbo:TURBO_0000608 "131" ;
  turbo:TURBO_0000604 "4/4/2000" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000605 "2000-04-04"^^xsd:date .

<http://transformunify.org/ontologies/participant/92c8b0a2b01c40df8bc154fbbbc58a22>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000604 "4/4/2000" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  turbo:TURBO_0000608 "131" ;
  turbo:TURBO_0000605 "2000-04-04"^^xsd:date ;
  turbo:TURBO_0000606 "F" .

<http://transformunify.org/ontologies/participant/11998620772345ee85874a5b01103c11>
  turbo:TURBO_0000605 "2000-04-05"^^xsd:date ;
  turbo:TURBO_0000604 "4/5/2000" ;
  turbo:TURBO_0000608 "131" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  turbo:TURBO_0000606 "F" .
}}
"""

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()
    // insert and check

    // expand

    //HAYDEN 10/13 3:53 pm adding IRI to method call
    val f: ValueFactory = cxn.getValueFactory()
    expand.participantExpansion(cxn, f.createIRI("http://transformunify.org/ontologies/R2RInst2"), "shortcut graphs here")

    helper.moveDataFromOneNamedGraphToAnother(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "http://www.itmat.upenn.edu/biobank/expanded")

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear graph <http://transformunify.org/ontologies/participantShortcuts>").execute
    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear graph <http://transformunify.org/ontologies/encounterShortcuts>").execute

    //HAYDEN 11/7 11:35 am This test is failing because the ontology is not imported. The newest version of the applyInverses method requires that the ontology be imported to work.
    
    helper.addOntologyFromUrl(cxn)
    
    //HAYDEN 10/16 4:22 pm changing to call method in application
    //helper.applyInverses(cxn)

    DumpRepoToFile(cxn, "MAM_dumps/expanded_parts.ttl")

    val QueryStatement = """
select distinct 
?pscLV ?dsTitle ?previousUriText ?gidtype ?gidTextVal ?dobTextVal ?dobMeasVal 
where {
    ?PSC      rdf:type            :TURBO_0000503 ;
              obo:IAO_0000219     ?participant ;
              :TURBO_0006510  ?pscLV .
    ?participant  rdf:type        :TURBO_0000502 ;
                  :TURBO_0006601    ?previousUriText ;
                  obo:RO_0000086      ?biosexGT ;
                  :TURBO_0000303     ?birth .
    ?biosexGT  rdf:type           ?biosexValue .
    ?dob      rdf:type            <http://www.ebi.ac.uk/efo/EFO_0004950> ;
              :TURBO_0006510    ?dobTextVal ;
              :TURBO_0006511       ?dobMeasVal ;
              obo:IAO_0000136 ?birth .
    ?Dataset  rdf:type            obo:IAO_0000100 ;
              dc11:title          ?dsTitle ;
              obo:BFO_0000051     ?PSC ;
              obo:BFO_0000051     ?gid ;
              obo:BFO_0000051     ?dob .
    ?Instantiation1
        rdf:type            :R2RInstantiation ;
        obo:OBI_0000293     ?Dataset .
    ?gid      rdf:type            ?gidtype ;
              obo:IAO_0000136     ?participant ;
              :TURBO_0006510  ?gidTextVal .
}
"""
    val PrefixedQuery = sparqlPrefixes + QueryStatement

    val PreppedResult = cxn.prepareTupleQuery(QueryLanguage.SPARQL, PrefixedQuery)
    val QueryResult = PreppedResult.evaluate()

    val QueryStructure = SparqlHelper.Res2Set(QueryResult)
    
    println(QueryStructure)

    val ExpectedStructure = Set(
      Map(
        """gidTextVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """previousUriText""" -> """"http://transformunify.org/ontologies/participant/92c8b0a2b01c40df8bc154fbbbc58a22"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobMeasVal""" -> """"2000-04-04"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """pscLV""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobTextVal""" -> """"4/4/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """gidtype""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dsTitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidTextVal""" -> """"M"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """previousUriText""" -> """"http://transformunify.org/ontologies/participant/7e557b66e87a4994b5c4beb2f36e2c31"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobMeasVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """pscLV""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """gidtype""" -> """http://purl.obolibrary.org/obo/OMRSE_00000141""",
        """dsTitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidTextVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """previousUriText""" -> """"http://transformunify.org/ontologies/participant/1403749e36984e72832da89540afd4c3"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobMeasVal""" -> """"2000-04-04"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """pscLV""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobTextVal""" -> """"4/4/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """gidtype""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dsTitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidTextVal""" -> """"M"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """previousUriText""" -> """"http://transformunify.org/ontologies/participant/ef2fec6605054d64bb235145dc09f924"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobMeasVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """pscLV""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """gidtype""" -> """http://purl.obolibrary.org/obo/OMRSE_00000141""",
        """dsTitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidTextVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """previousUriText""" -> """"http://transformunify.org/ontologies/participant/277efd6bd9504635b1f7ab12d952c62c"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobMeasVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """pscLV""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """gidtype""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dsTitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidTextVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """previousUriText""" -> """"http://transformunify.org/ontologies/participant/11998620772345ee85874a5b01103c11"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobMeasVal""" -> """"2000-04-05"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """pscLV""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobTextVal""" -> """"4/5/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """gidtype""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dsTitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>"""))

    assert(QueryStructure.equals(ExpectedStructure), "...participants weren't expanded as expected")

  }

  test("referent tracking of participants") {

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    // insert known-good, static, handcrafted expanded encounter triples
//HAYDEN 1/11: adding required height and weight to each participant
    val UpdateStatement = """
INSERT DATA {
  GRAPH pmbb:expanded {
  <http://transformunify.org/ontologies/InstOutpCont> a <http://transformunify.org/ontologies/Container> .
<http://transformunify.org/ontologies/dataset1>
  a <http://purl.obolibrary.org/obo/IAO_0000100> ;
  obo:BFO_0000051 <http://transformunify.org/ontologies/4d90c84b4dc94502b577d81ee382b5fa>, <http://transformunify.org/ontologies/9f4641252c224dabae380aed30ce1ab7>, <http://transformunify.org/ontologies/bd580f9d5c824de8bcd0d42d6d8ddd43>, <http://transformunify.org/ontologies/27bb9f959cd842309be8b3e299eb709b>, <http://transformunify.org/ontologies/96cf1d5c0d2446018a15beec673a57b5>, <http://transformunify.org/ontologies/dc86a2553587495aa55fdea08fd10f68>, <http://transformunify.org/ontologies/82e5e30b3832419694678cc9d5bc36bb>, <http://transformunify.org/ontologies/59d245d8395040c3845c7a0dc7d281cd>, <http://transformunify.org/ontologies/b4f1fb5242cc45f99a39db317c0481ea>, <http://transformunify.org/ontologies/853035948b56403b9c8a6335311395ca>, <http://transformunify.org/ontologies/e05c6ca5b72f4996931e428b47b9a78f>, <http://transformunify.org/ontologies/910000b90ee84e8dad3a76b22ce0281d>, <http://transformunify.org/ontologies/46901c63ec474cc0ad8d6f1af9f9abd7>, <http://transformunify.org/ontologies/5f05942298234bfcbb9162e3c2589375>, <http://transformunify.org/ontologies/8d1d3c5f74374c188533655e6dc0f6c8>, <http://transformunify.org/ontologies/7b09ce53390b4a3881d649ca25efa568>, <http://transformunify.org/ontologies/233f26463ecc408b946a538fa82eb6fd>, <http://transformunify.org/ontologies/efbb82aa057b4c8788de2355b71c6da3> ;
  dc11:title "handcrafted_parts.csv" .

<http://transformunify.org/ontologies/R2R_instantiation_process1>
  a <http://transformunify.org/ontologies/R2RInstantiation> ;
  obo:OBI_0000293 <http://transformunify.org/ontologies/dataset1> ;
  obo:OBI_0000299 <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/73d032c461784a2fa200d50462901e67>
  a obo:UBERON_0035946 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/28c94dd63ad544c69eecefe8d7182dc2>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/ef2fec6605054d64bb235145dc09f924" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0000086 turbo:5a93f044dd564a03af0eae54112e2c68 ;
  turbo:TURBO_0000303 turbo:73d032c461784a2fa200d50462901e67 .
  
  <http://transformunify.org/ontologies/39dcf7d8-c72d-4766-9ef0-6fd6d368f9d9> a obo:UBERON_0001013 .
  <http://transformunify.org/ontologies/28c94dd63ad544c69eecefe8d7182dc2> obo:BFO_0000051 <http://transformunify.org/ontologies/39dcf7d8-c72d-4766-9ef0-6fd6d368f9d9> .
  
  <http://transformunify.org/ontologies/d6c7c01e-5d0b-45e6-98d5-79bf86aee704> rdf:type obo:PATO_0000119 .
  <http://transformunify.org/ontologies/28c94dd63ad544c69eecefe8d7182dc2> obo:RO_0000086 <http://transformunify.org/ontologies/d6c7c01e-5d0b-45e6-98d5-79bf86aee704> .
  
  <http://transformunify.org/ontologies/6958e05e-b8c4-4cb4-b76b-dd53b53c133d> rdf:type obo:PATO_0000128 .
  <http://transformunify.org/ontologies/28c94dd63ad544c69eecefe8d7182dc2> obo:RO_0000086 <http://transformunify.org/ontologies/6958e05e-b8c4-4cb4-b76b-dd53b53c133d> .

turbo:5a93f044dd564a03af0eae54112e2c68
  a obo:PATO_0000047 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

turbo:4d90c84b4dc94502b577d81ee382b5fa
  a obo:OMRSE_00000141 ;
  turbo:TURBO_0006510 "M" ;
  obo:IAO_0000136 <http://transformunify.org/ontologies/28c94dd63ad544c69eecefe8d7182dc2> ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

<http://transformunify.org/ontologies/9f4641252c224dabae380aed30ce1ab7>
  a turbo:TURBO_0000503 ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/28c94dd63ad544c69eecefe8d7182dc2> ;
  turbo:TURBO_0006510 "121" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:bd580f9d5c824de8bcd0d42d6d8ddd43
  a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
  turbo:TURBO_0006510 "12/30/1971" ;
  turbo:TURBO_0006511 "1971-12-30"^^xsd:date ;
  obo:IAO_0000136 turbo:73d032c461784a2fa200d50462901e67 ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:070a0f89b6894610957f67917ff3c1ef
  a obo:UBERON_0035946 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/9810e21d7c6e4e50be1ce25e0b87c8f6>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/7e557b66e87a4994b5c4beb2f36e2c31" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0000086 turbo:ddf7a70aafd440af81baf7fff8bccc02 ;
  turbo:TURBO_0000303 turbo:070a0f89b6894610957f67917ff3c1ef .
  
  <http://transformunify.org/ontologies/3ccd711f-bc6c-4d9a-bf56-2c779ce55c9c> a obo:UBERON_0001013 .
  <http://transformunify.org/ontologies/9810e21d7c6e4e50be1ce25e0b87c8f6> obo:BFO_0000051 <http://transformunify.org/ontologies/3ccd711f-bc6c-4d9a-bf56-2c779ce55c9c> .
  
  <http://transformunify.org/ontologies/4bf7d737-f207-46b9-85e8-0652fde19685> rdf:type obo:PATO_0000119 .
  <http://transformunify.org/ontologies/9810e21d7c6e4e50be1ce25e0b87c8f6> obo:RO_0000086 <http://transformunify.org/ontologies/4bf7d737-f207-46b9-85e8-0652fde19685> .
  
  <http://transformunify.org/ontologies/d0f2514a-be54-4d3b-aa11-a39d2cf35243> rdf:type obo:PATO_0000128 .
  <http://transformunify.org/ontologies/9810e21d7c6e4e50be1ce25e0b87c8f6> obo:RO_0000086 <http://transformunify.org/ontologies/d0f2514a-be54-4d3b-aa11-a39d2cf35243> .

turbo:ddf7a70aafd440af81baf7fff8bccc02
  a obo:PATO_0000047 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

turbo:27bb9f959cd842309be8b3e299eb709b
  a obo:OMRSE_00000141 ;
  turbo:TURBO_0006510 "M" ;
  obo:IAO_0000136 <http://transformunify.org/ontologies/9810e21d7c6e4e50be1ce25e0b87c8f6> ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

<http://transformunify.org/ontologies/96cf1d5c0d2446018a15beec673a57b5>
  a turbo:TURBO_0000503 ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/9810e21d7c6e4e50be1ce25e0b87c8f6> ;
  turbo:TURBO_0006510 "121" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:dc86a2553587495aa55fdea08fd10f68
  a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
  turbo:TURBO_0006510 "12/30/1971" ;
  turbo:TURBO_0006511 "1971-12-30"^^xsd:date ;
  obo:IAO_0000136 turbo:070a0f89b6894610957f67917ff3c1ef ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:38ca03e9eaa447be967fda063789ce8d
  a obo:UBERON_0035946 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/1a036b3ffe15468086de63cf52cae721>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/277efd6bd9504635b1f7ab12d952c62c" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0000086 turbo:2920f02385814b45b189f3110aa2b28a ;
  turbo:TURBO_0000303 turbo:38ca03e9eaa447be967fda063789ce8d .
  
  <http://transformunify.org/ontologies/0e0480a1-f45f-429b-ae37-8f9906474a23> a obo:UBERON_0001013 .
  <http://transformunify.org/ontologies/1a036b3ffe15468086de63cf52cae721> obo:BFO_0000051 <http://transformunify.org/ontologies/0e0480a1-f45f-429b-ae37-8f9906474a23> .
  
  <http://transformunify.org/ontologies/ed2a4156-4340-4a53-addd-d3207783e286> rdf:type obo:PATO_0000119 .
  <http://transformunify.org/ontologies/1a036b3ffe15468086de63cf52cae721> obo:RO_0000086 <http://transformunify.org/ontologies/ed2a4156-4340-4a53-addd-d3207783e286> .
  
  <http://transformunify.org/ontologies/3ba41752-f836-4d17-b5eb-e1ad924f82f7> rdf:type obo:PATO_0000128 .
  <http://transformunify.org/ontologies/1a036b3ffe15468086de63cf52cae721> obo:RO_0000086 <http://transformunify.org/ontologies/3ba41752-f836-4d17-b5eb-e1ad924f82f7> .


turbo:2920f02385814b45b189f3110aa2b28a
  a obo:PATO_0000047 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

turbo:82e5e30b3832419694678cc9d5bc36bb
  a obo:OMRSE_00000138 ;
  turbo:TURBO_0006510 "F" ;
  obo:IAO_0000136 <http://transformunify.org/ontologies/1a036b3ffe15468086de63cf52cae721> ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

<http://transformunify.org/ontologies/59d245d8395040c3845c7a0dc7d281cd>
  a turbo:TURBO_0000503 ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/1a036b3ffe15468086de63cf52cae721> ;
  turbo:TURBO_0006510 "121" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:b4f1fb5242cc45f99a39db317c0481ea
  a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
  turbo:TURBO_0006510 "12/30/1971" ;
  turbo:TURBO_0006511 "1971-12-30"^^xsd:date ;
  obo:IAO_0000136 turbo:38ca03e9eaa447be967fda063789ce8d ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:e960b8494ff24ed4b94e1dc5e4455614
  a obo:UBERON_0035946 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/bf71fc62e3a74971942e69baa74d7db5>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/1403749e36984e72832da89540afd4c3" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0000086 turbo:3a49a3ac49fb492cb9d084907e08cd18 ;
  turbo:TURBO_0000303 turbo:e960b8494ff24ed4b94e1dc5e4455614 .
  
  <http://transformunify.org/ontologies/056e0092-0343-43ce-9d80-1269948fb0c1> a obo:UBERON_0001013 .
  <http://transformunify.org/ontologies/bf71fc62e3a74971942e69baa74d7db5> obo:BFO_0000051 <http://transformunify.org/ontologies/056e0092-0343-43ce-9d80-1269948fb0c1> .
  
  <http://transformunify.org/ontologies/95d85038-c89f-4362-98f8-fac9778aed06> rdf:type obo:PATO_0000119 .
  <http://transformunify.org/ontologies/bf71fc62e3a74971942e69baa74d7db5> obo:RO_0000086 <http://transformunify.org/ontologies/95d85038-c89f-4362-98f8-fac9778aed06> .
  
  <http://transformunify.org/ontologies/f3dbba3c-fbaf-4487-84ec-072f4681e449> rdf:type obo:PATO_0000128 .
  <http://transformunify.org/ontologies/bf71fc62e3a74971942e69baa74d7db5> obo:RO_0000086 <http://transformunify.org/ontologies/f3dbba3c-fbaf-4487-84ec-072f4681e449> .

turbo:3a49a3ac49fb492cb9d084907e08cd18
  a obo:PATO_0000047 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

turbo:853035948b56403b9c8a6335311395ca
  a obo:OMRSE_00000138 ;
  turbo:TURBO_0006510 "F" ;
  obo:IAO_0000136 <http://transformunify.org/ontologies/bf71fc62e3a74971942e69baa74d7db5> ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

<http://transformunify.org/ontologies/e05c6ca5b72f4996931e428b47b9a78f>
  a turbo:TURBO_0000503 ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/bf71fc62e3a74971942e69baa74d7db5> ;
  turbo:TURBO_0006510 "131" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:910000b90ee84e8dad3a76b22ce0281d
  a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
  turbo:TURBO_0006510 "4/4/2000" ;
  turbo:TURBO_0006511 "2000-04-04"^^xsd:date ;
  obo:IAO_0000136 turbo:e960b8494ff24ed4b94e1dc5e4455614 ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:6707ba235db245159fd654b99a5f4a2e
  a obo:UBERON_0035946 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/cc515c71005a45b0ab9e15d8047b2282>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/92c8b0a2b01c40df8bc154fbbbc58a22" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0000086 turbo:9653ab29408e4eab9236cc0c5a97ec74 ;
  turbo:TURBO_0000303 turbo:6707ba235db245159fd654b99a5f4a2e .
  
  <http://transformunify.org/ontologies/1a46e8b5-8c29-4cb7-839f-1eab514e0e50> a obo:UBERON_0001013 .
  <http://transformunify.org/ontologies/cc515c71005a45b0ab9e15d8047b2282> obo:BFO_0000051 <http://transformunify.org/ontologies/1a46e8b5-8c29-4cb7-839f-1eab514e0e50> .
  
  <http://transformunify.org/ontologies/3fc332b6-2926-4f2a-a165-fcd0652775c4> rdf:type obo:PATO_0000119 .
  <http://transformunify.org/ontologies/cc515c71005a45b0ab9e15d8047b2282> obo:RO_0000086 <http://transformunify.org/ontologies/3fc332b6-2926-4f2a-a165-fcd0652775c4> .
  
  <http://transformunify.org/ontologies/fdb2ae58-8c8b-4f8f-a82e-61048e9d839c> rdf:type obo:PATO_0000128 .
  <http://transformunify.org/ontologies/cc515c71005a45b0ab9e15d8047b2282> obo:RO_0000086 <http://transformunify.org/ontologies/fdb2ae58-8c8b-4f8f-a82e-61048e9d839c> .


turbo:9653ab29408e4eab9236cc0c5a97ec74
  a obo:PATO_0000047 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

turbo:46901c63ec474cc0ad8d6f1af9f9abd7
  a obo:OMRSE_00000138 ;
  turbo:TURBO_0006510 "F" ;
  obo:IAO_0000136 <http://transformunify.org/ontologies/cc515c71005a45b0ab9e15d8047b2282> ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

<http://transformunify.org/ontologies/5f05942298234bfcbb9162e3c2589375>
  a turbo:TURBO_0000503 ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/cc515c71005a45b0ab9e15d8047b2282> ;
  turbo:TURBO_0006510 "131" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:8d1d3c5f74374c188533655e6dc0f6c8
  a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
  turbo:TURBO_0006510 "4/4/2000" ;
  turbo:TURBO_0006511 "2000-04-04"^^xsd:date ;
  obo:IAO_0000136 turbo:6707ba235db245159fd654b99a5f4a2e ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:e1947eca39294ef7bb64aff1b160e18d
  a obo:UBERON_0035946 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

<http://transformunify.org/ontologies/33604dea64584b248fb4e43dc454c7cd>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/11998620772345ee85874a5b01103c11" ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> ;
  obo:RO_0000086 turbo:544881619f7a4d568af24f578406a193 ;
  turbo:TURBO_0000303 turbo:e1947eca39294ef7bb64aff1b160e18d .
  
  <http://transformunify.org/ontologies/c3dd0829-b319-4ba1-9381-0b79c3401539> a obo:UBERON_0001013 .
  <http://transformunify.org/ontologies/33604dea64584b248fb4e43dc454c7cd> obo:BFO_0000051 <http://transformunify.org/ontologies/c3dd0829-b319-4ba1-9381-0b79c3401539> .
  
  <http://transformunify.org/ontologies/2a2b9fe8-dc47-4810-94d1-11f0c72ba9cf> rdf:type obo:PATO_0000119 .
  <http://transformunify.org/ontologies/33604dea64584b248fb4e43dc454c7cd> obo:RO_0000086 <http://transformunify.org/ontologies/2a2b9fe8-dc47-4810-94d1-11f0c72ba9cf> .
  
  <http://transformunify.org/ontologies/660f79f0-e85f-4e90-a838-b121498fa820> rdf:type obo:PATO_0000128 .
  <http://transformunify.org/ontologies/33604dea64584b248fb4e43dc454c7cd> obo:RO_0000086 <http://transformunify.org/ontologies/660f79f0-e85f-4e90-a838-b121498fa820> .

turbo:544881619f7a4d568af24f578406a193
  a obo:PATO_0000047 ;
  turbo:member <http://transformunify.org/ontologies/InstOutpCont> .

turbo:7b09ce53390b4a3881d649ca25efa568
  a obo:OMRSE_00000138 ;
  turbo:TURBO_0006510 "F" ;
  obo:IAO_0000136 <http://transformunify.org/ontologies/33604dea64584b248fb4e43dc454c7cd> ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

<http://transformunify.org/ontologies/233f26463ecc408b946a538fa82eb6fd>
  a turbo:TURBO_0000503 ;
  obo:IAO_0000219 <http://transformunify.org/ontologies/33604dea64584b248fb4e43dc454c7cd> ;
  turbo:TURBO_0006510 "131" ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .

turbo:efbb82aa057b4c8788de2355b71c6da3
  a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
  turbo:TURBO_0006510 "4/5/2000" ;
  turbo:TURBO_0006511 "2000-04-05"^^xsd:date ;
  obo:IAO_0000136 turbo:e1947eca39294ef7bb64aff1b160e18d ;
  obo:BFO_0000050 <http://transformunify.org/ontologies/dataset1> .
        }}
      """

    val WithPrefixes = sparqlPrefixes + UpdateStatement

    cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes).execute()

    reftrackInst.reftrackParticipantsAndDependents(cxn)
//    FSASHelper.reftrackParticipantDependents(cxn)

  }

  test("New At Least 1 Rt Part") {

    // METHOD:  sparql select 

    var RtPartCount = 0
    //Hayden: changed "true" to Boolean

    val tupleQuery = cxn.prepareTupleQuery(QueryLanguage.SPARQL, """
prefix turbo: <http://transformunify.org/ontologies/> 
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
select ?s
where 
{
    graph pmbb:expanded {
        ?s rdf:type turbo:TURBO_0000502 .
    }
}
""")

    val result = tupleQuery.evaluate()

    try {
      while (result.hasNext()) {
        RtPartCount += 1
        val bindingSet = result.next()
        //                    println(bindingSet)
        logger.warn("bindingSet = " + bindingSet)
      }
    } finally {
      result.close()
      //                println(RtPartCount + " rows counted")
      logger.warn(RtPartCount + " rows counted")
    }

    //    are there any referent tracked participants now?
    assert(RtPartCount > 0, "no referent tracked participants are in the database yet")

  }

  test("All Participants Ref Tracked") {

    // METHOD:  sparql ask

    val tupleQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, """
prefix turbo: <http://transformunify.org/ontologies/> 
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX : <http://transformunify.org/ontologies/>
ask
where 
{
    graphpmbb:expanded 
    {
        ?s rdf:type turbo:studyPartWithBBDonation .
    } minus {
        ?s rdf:type turbo:studyPartWithBBDonation ;
           turbo:TURBO_0006500 'true'^^xsd:boolean
    }
}
""")
    val AskRes = tupleQuery.evaluate()
    assert(!AskRes, "there are still non-referent tracked participants in the database")

  }

  test("PscsOnlyDenoteRtParts") {

    // METHOD:  another sparql ask

    val tupleQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, """
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX turbo: <http://transformunify.org/ontologies/>
PREFIX : <http://transformunify.org/ontologies/>
ask where
{
    graphpmbb:expanded
    {
        ?psc obo:IAO_0000219 ?part .
        ?psc a turbo:TURBO_0000503 .
    } minus 
    {
        ?psc obo:IAO_0000219 ?part .
        ?psc a turbo:TURBO_0000503 .
        ?part a turbo:TURBO_0000502
    }
}
          """)
    var AskResult = tupleQuery.evaluate()

    // are there any PSCs that denote something other
    // than a participant?
    assert(!AskResult, "there are PSCs that denote something other than a participant")

  }

  test("Check part obsolescence with model from turtle") {

    // not well suited when the constructed triples will contain unknown UUIDs?

      //HAYDEN 12/19 3:19 PM Added filtering out "rdfs:label" predicate from ConstructQuery
      
    var ConstructQuery = """
PREFIX : <http://transformunify.org/ontologies/>
prefix turbo: <http://transformunify.org/ontologies/> 
prefix obo: <http://purl.obolibrary.org/obo/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
construct {
    :placeholder ?p ?o .
} where
{
    graph ?g {
        ?denoter obo:IAO_0000219 ?denoted .
        ?denoter turbo:TURBO_0006510 "121" .
        ?previous turbo:TURBO_0001700 ?denoted .
        ?previous ?p ?o .
        filter ( ?p != turbo:TURBO_0001700 )
        filter ( ?p != turbo:TURBO_0006602 )
        #HAYDEN: I added the line below to make the test pass
        filter ( ?p != rdfs:label )
    }
}
"""

    var HelperInstance = new Helper4SparqlTests

    var resultModel = HelperInstance.ModelFromSparqlConstruct(cxn, ConstructQuery)
    println("result model" + resultModel.toString)
    //  create an expected model from a  string
    var ExpectedTurtle = """
@prefix turbo: <http://transformunify.org/ontologies/> .
@prefix obo: <http://purl.obolibrary.org/obo/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix sesame: <http://www.openrdf.org/schema/sesame#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix fn: <http://www.w3.org/2005/xpath-functions#> .

turbo:placeholder a turbo:RetiredParticipantPlaceholder ;
	obo:IAO_0000225 obo:IAO_0000226 ;
	a turbo:RetiredParticipantPlaceholder ;
	obo:IAO_0000225 obo:IAO_0000226 ;
	a turbo:RetiredParticipantPlaceholder ;
	obo:IAO_0000225 obo:IAO_0000226 .
"""

    var expectedMod = HelperInstance.ModelFromTurtleString(ExpectedTurtle)

    assert(expectedMod.equals(resultModel), "hardcoded triples expressing obsolescence of instantiated participant do not match construct query results")

  }

  test("explicit KevinFred PSC GID test, no off-line reprocessing") {

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
}
""",
      "instantiated participant was not replaced", true)

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
    ?previous obo:IAO_0000225 obo:IAO_0000226 .
}
""",
      "instantiated participant was not obsoleted as expected (obo:IAO_0000225 obo:IAO_0000226)", true)

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
    ?previous a turbo:RetiredParticipantPlaceholder
}
""",
      "instantiated participant was not demoted to the right type", true)

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
    ?previous a turbo:RetiredParticipantPlaceholder .
    ?denoted turbo:TURBO_0006500 true .
}
""",
      "replacement for instantiated participant is not flagged as reftracked", true)

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
    ?previous a turbo:RetiredParticipantPlaceholder .
    ?denoted turbo:TURBO_0006500 true .
    ?denoted a turbo:TURBO_0000502 .
}
""",
      "replacement for instantiated participant is not of the right type", true)

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
    ?previous a turbo:RetiredParticipantPlaceholder .
    ?denoted turbo:TURBO_0006500 true .
    ?denoted a turbo:TURBO_0000502 .
    ?denoted obo:RO_0000086 ?biosex .
    ?biosex a obo:PATO_0000047 .
}
""",
      "replacement for instantiated participant does not have a biological sex", true)

    tinySparqlTest("""
ask where {
    ?denoter obo:IAO_0000219 ?denoted .
    ?denoter turbo:TURBO_0006510 "121" .
    ?previous turbo:TURBO_0001700 ?denoted .
    ?previous a turbo:RetiredParticipantPlaceholder .
    ?denoted turbo:TURBO_0006500 true .
    ?denoted a turbo:TURBO_0000502 .
    ?denoted obo:RO_0000086 ?biosex .
    ?biosex a obo:PATO_0000047 .
}
""",
      "replacement for instantiated participant does not have a biological sex", true)

    // should the retired participant have the retired biological sex as a quality?

    // not explicitly looking in the expanded graph

    // these ask if the pattern occurs at least once, not "always"

    tinySparqlTest("""
ask where {
    ?retbs a  turbo:TURBO_0001902 .
}
""",
      "there aren't any retired biological sexes", true)

    tinySparqlTest("""
ask where {
    ?retbs a  turbo:TURBO_0001902 ;
    obo:IAO_0000225 obo:IAO_0000226
}
""",
      "retired biological sexes weren't obsoleted as expected", true)

    tinySparqlTest("""
ask where {
    ?retbs a  turbo:TURBO_0001902 ;
    turbo:TURBO_0001700 ?o
}
""",
      "retired biological sexes weren't replaced", true)

    tinySparqlTest("""
ask where {
    ?retbs a  turbo:TURBO_0001902 ;
    turbo:TURBO_0001700 ?o .
    ?o a obo:PATO_0000047 .
}
""",
      "retired biological sexes weren't replaced with the right type", true)

    tinySparqlTest("""
ask where {
    ?retbs a  turbo:TURBO_0001902 ;
    turbo:TURBO_0001700 ?o .
    ?o a obo:PATO_0000047 .
    ?o turbo:TURBO_0006500 true .
}
""",
      "replacement for retired biological sexes is not flagged as reftracked", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder .
}
""",
      "there aren't any retired PSCs", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 .
}
""",
      "retired PSCs weren't obsoleted as expected", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
}
""",
      "retired PSCs weren't replaced", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503
}
""",
      "retired PSCs weren't replaced with the right type", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:BFO_0000050 ?ds .
}
""",
      "replacements for retired PSCs aren't part of anything", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:BFO_0000050 ?ds .
    ?ds a obo:IAO_0000100
}
""",
      "replacements for retired PSCs aren't part of a dataset", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds .
    ?ds a obo:IAO_0000100 .
}
""",
      "replacements for retired PSCs don't denote anything", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds .
    ?ds a obo:IAO_0000100 .
    ?part a turbo:TURBO_0000502
}
""",
      "replacements for retired PSCs don't denote study participants", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" .
    ?ds a obo:IAO_0000100 .
    ?part a turbo:TURBO_0000502
}
""",
      "there's no replacement for a retired PSCs with a value of 121", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
         turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 .
    ?part a turbo:TURBO_0000502
}
""",
      "the replacement for the retired PSCs with a value of 121 is not marked reftracked", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
         turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
    dc11:title "handcrafted_parts.csv" .
    ?part a turbo:TURBO_0000502
}
""",
      "the dataset with part replacement for the retired PSCs with a value of 121 doesn't have the expected title", true)

    // check for processes, inputs, outputs

    // load ontology!
    //HAYDEN 1/11 updated to new ontology URL
    LoadOntology(repository, ontologyURL, "http://www.itmat.upenn.edu/biobank/ontology", "http://transformunify.org/ontologies/")

    val ProcReport = """
      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
select * where {
    graph ?g {
        ?proc rdfs:subClassOf+ <http://purl.obolibrary.org/obo/OBI_0000011> .
        ?inst a ?proc .
        optional {
            ?inst <http://purl.obolibrary.org/obo/OBI_0000293> ?inp 
        }
        optional {
            ?inst <http://purl.obolibrary.org/obo/OBI_0000299> ?outp 
        }
    }
}
"""

    // not checking DOB yet

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a ?GIDtype .
    ?GIDtype rdfs:subClassOf obo:OMRSE_00000133 .
    ?part a turbo:TURBO_0000502
}
""",
      "the dataset doesn't contain any GIDs", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a ?GIDtype ;
         obo:IAO_0000136 ?x .
    ?GIDtype rdfs:subClassOf obo:OMRSE_00000133 .
    ?part a turbo:TURBO_0000502
}
""",
      "the GIDs aren't about anything", true)

    // distinction between testing of expansion adn referent trackign getting blurry here

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a ?GIDtype ;
         obo:IAO_0000136 ?x .
    ?x a turbo:TURBO_0000502 .
    ?GIDtype rdfs:subClassOf obo:OMRSE_00000133 .
    ?part a turbo:TURBO_0000502
}
""",
      "the GIDs aren't about study participants", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a ?GIDtype ;
         obo:IAO_0000136 ?x ;
         turbo:TURBO_0006510 ?gidlv .
    ?x a turbo:TURBO_0000502 .
    ?part a turbo:TURBO_0000502 .
}
""",
      "the GID for participant 121 doesn't have a literal value", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a ?GIDtype ;
         obo:IAO_0000136 ?x ;
         turbo:TURBO_0006510 "F" .
         
    ?x a turbo:TURBO_0000502 .
    ?part a turbo:TURBO_0000502 .
}
""",
      "the GID for participant 121 doesn't have a literal value of F", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a obo:OMRSE_00000138 ;
         obo:IAO_0000136 ?x ;
         turbo:TURBO_0006510 "F" .
         
    ?x a turbo:TURBO_0000502 .
    ?part a turbo:TURBO_0000502 .
}
""",
      "the GID for participant 121 doesn't have a literal value of F and a type of OMRSE_00000138", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a ?GIDtype ;
         obo:IAO_0000136 ?x ;
         turbo:TURBO_0006510 "M" .
         
    ?x a turbo:TURBO_0000502 .
    ?part a turbo:TURBO_0000502 .
}
""",
      "the GID for participant 121 doesn't ALSO have a literal value of M", true)

    tinySparqlTest("""
ask where {
    ?retPSC a  turbo:RetiredPscPlaceholder ;
            obo:IAO_0000225 obo:IAO_0000226 ;
            turbo:TURBO_0001700 ?psc .
    ?psc a turbo:TURBO_0000503 ;
         obo:IAO_0000219 ?part ;
         obo:BFO_0000050 ?ds ;
         turbo:TURBO_0006510 "121" ;
                                       turbo:TURBO_0006500 true .
    ?ds a obo:IAO_0000100 ;
        dc11:title "handcrafted_parts.csv" ;
                                           obo:BFO_0000051 ?GID .
    ?GID a obo:OMRSE_00000141 ;
         obo:IAO_0000136 ?x ;
         turbo:TURBO_0006510 "M" .
         
    ?x a turbo:TURBO_0000502 .
    ?part a turbo:TURBO_0000502 .
}
""",
      "the GID for participant 121 doesn't ALSO have a literal value of M and a type of OMRSE_00000141", true)

  }

  // join testing

  var CurrentExpansionAskCore = """
ASK WHERE
{
    GRAPH pmbb:expanded_part2enc
    {
        VALUES ( ?pscLv ?encIdLv ?dsTitle ) {
            ( "102" "102" "part2enc_10.csv" )
            ( "103" "105" "part2enc_10.csv" )
        }
        ?participant  a                 turbo:TURBO_0000502 ;
                      turbo:previousUriText  ?previousUriText ;
                      obo:RO_0000087        ?PUIrole ;
                      obo:RO_0000056        ?Encounter .
        ?Encounter  a                   obo:OGMS_0000097 .
        ?PSC      a                     turbo:TURBO_0000503 ;
                  obo:IAO_0000219       ?participant ;
                  turbo:TURBO_0006510  ?pscLv .
        ?EncID    a                     turbo:EncounterID ;
                  obo:IAO_0000219       ?Encounter ;
                  turbo:TURBO_0006510  ?encIdLv .
        ?PUIrole  a                     obo:OBI_0000097 ;
                  obo:BFO_0000054       ?Encounter .
        ?Dataset  a                     obo:IAO_0000100 ;
                  dc11:title              ?dsTitle ;
                  obo:BFO_0000051       ?PSC ;
                  obo:BFO_0000051       ?EncID .
        ?Instantiation1
            a                     turbo:TURBO_0000522 ;
            obo:OBI_0000293       ?Dataset
    }
}
"""

  def DumpRepoToFile(cxn: RepositoryConnection, DesiredFileName: String) = {
    // this seems to be less important now,
    // as browsing in GraphDB is pretty productive

    // example file name used in the past
    //    var out = new FileOutputStream("MAM_dumps/old_construct_like_dump.ttl")
    var out = new FileOutputStream(DesiredFileName)
    var writer = Rio.createWriter(RDFFormat.TURTLE, out)
    cxn.prepareGraphQuery(QueryLanguage.SPARQL,
      "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o } ").evaluate(writer)
  }

  def tinySparqlTest(query: String, failureMsg: String, expectedVal: Boolean) = {
    // probably should write a separate function for when the expected result is false
    val PrefixedQuery = sparqlPrefixes + query
    val expectationQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, PrefixedQuery)
    val AskRes = expectationQuery.evaluate()
    val TidyMsg = "..." + failureMsg
    assert(AskRes == expectedVal, TidyMsg)

    // return X
  }

  def CreateInverses(cxn: RepositoryConnection) = {
    // return value / exception handling?

    var InverseSparql = """
      insert { 
      graph <http://www.itmat.upenn.edu/biobank/inverses> {
      ?o obo:BFO_0000050 ?s 
      }} where { 
	?s obo:BFO_0000051 ?o .
}
"""

    var WithPrefixes = sparqlPrefixes + InverseSparql
    var PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    var result = PreparedSparql.execute()

    InverseSparql = """
insert { 
graph <http://www.itmat.upenn.edu/biobank/inverses> {
?o obo:BFO_0000051 ?s }} where { 
	?s obo:BFO_0000050 ?o .
} 
"""

    WithPrefixes = sparqlPrefixes + InverseSparql
    PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    result = PreparedSparql.execute()

  }

  def InsertFromString(cxn: RepositoryConnection, TriplesString: String, DestinationGraph: String) = {
    // return value / exception handling?

    val InsertPrefix = """
							INSERT DATA 
							{ 
							graph <""" + DestinationGraph + """>
							{
							"""

    val InsertSuffix = """
							}
							}
							"""

    val Infixed = InsertPrefix + TriplesString + InsertSuffix

    val tupleAdd = cxn.prepareUpdate(QueryLanguage.SPARQL, Infixed)
    val result = tupleAdd.execute()
  }

  def LoadOntology(passedRepo: Repository, ontoLoc: String, destGraph: String, uriBase: String) {
    // "http://transformunify.org/ontologies/turbo_merged.ttl"
    val OntoUrl = new URL(ontoLoc)
    val f = passedRepo.getValueFactory()
    //     http://www.itmat.upenn.edu/biobank/ontology
    val OntoGraphName = f.createIRI(destGraph);

    // sample uriBase
    // http://transformunify.org/ontologies/

    var TempCxn = passedRepo.getConnection
    TempCxn.begin()
    // arguments:  
    // 1) the URL location of the RDF content (our ontology in this case)
    // 2) a string which can be used as the default prefix for any entities that are relative within the RDF upload
    // (I mistakenly thought that was the named graph destination)
    // 3) the format of the triples to be loaded
    // 4) the real destination graph, as a resource like an IRI
    TempCxn.add(OntoUrl, uriBase, RDFFormat.RDFXML, OntoGraphName)
    TempCxn.commit()
  }

  def Submit2KarmaService {

    // we won't generally want to do this, although it could be useful for "proving a point"
    // when ti comes to creation of degenerate data

    // is service running?

    var MyRdfServiceURL = """http://transformunify.org:9090/rdf/r2rml/rdf"""

    var MyModelLocURL = """http://transformunify.org/karma_models/enc_only_20170824-model.ttl"""

    // data like this (for participants, encounters or participant-to-encounter joins)
    // can be synthesized with https://github.com/pennbiobank/turbo/blob/master/r_scripts/part_enc_synth.R
    // that doesn't intentionally create illegal dates or other known problem cases

    // but might be better to hand-craft a small interesting data snippet
    // Guidelines: 
    // all legal values to start
    // encid should appear twice with different diagnosis codes
    // one additional encid
    // at least two diag registries

    var MyCSVContent = """EncID,EncDate,DiagCode,CodeType
						102,12/05/2015,J44.9,ICD-10
						103,11/25/2015,602.9,ICD-9
						102,12/05/2015,I50.9,ICD-10
						"""

    // could convert this to triples with the karma web service and a rdf4j compliant triple store

    // do we want to include UUIDs in the URIs?  one per table row?  one for the whole instnatiation process?
    // leave UUID out and trust turbo cleanup methods to load these shortcut triples into a named graph and 
    // expand and uniquify them before relateasgin into teh default named graph?

//    System.out.println(MyCSVContent)
//
//    val RDFService = new karmaClientHelper
//
//    var EncountersTurtleString = RDFService.CSV2TTL(MyRdfServiceURL, MyModelLocURL, MyCSVContent)
//
//    System.out.println("encounters triples from karma")
//
//    System.out.println(EncountersTurtleString)
//
//    // but best practice is to use static, handcrafted triples that aren't' dependent on any  (or minimal) external services
//
//    //    // the triplestore defined by cxn should now contain all of the shortcut triples about encounters 
//    //    //    in graph <http://transformunify.org/participants_from_karma>, and nothing else
//    //    //    this may not be useful if we want to test the expansion of single shortcuts individually
//
//    //    // instances of encounters are created even in the shortcut Karma model
//    //    // (you can't have RDF triples without at least one entity)
//    //    // (also the shortcut encounter triples don't instantiate any other individuals)
//    //    // the expansion method must ensure that the URI for any one helathcare encounter in reality is universally unique 
//    //    //    how to test that?!
//    //    // it doesn't necessarily have to be the same URI as provided by Karma,
//    //    // although the previous textual value should be captured
//    //
//    //    // PS output from Karma is in N-triples, not Turtle, so it won't be prefixed.  Sticking with that pattern for the hadncrafties
//
//    var InsertSuccess = InsertFromString(cxn, EncountersTurtleString, "http://transformunify.org/ontologies/participants_from_karma")

  }

}


