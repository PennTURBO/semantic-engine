package edu.upenn.turbo

// this (/turbo_drivetrain/src/test/scala/com/example/app/NewExceptionTests.scala)
// will supersede /turbo_drivetrain/src/test/scala/com/example/app/PartExpExceptionTests.scala
// finish writing out what is is expected of the checker in the various exception cases
// add similar stuff for encounters IF IT CAN'T ALREADY BE FOUND in CorrectlypackagedTests and WheatNotChaff

import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.query.QueryLanguage
import org.scalatest.BeforeAndAfter
import org.scalatest._

class NewExceptionTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  val biosexConcInst: BiosexConclusionator = new BiosexConclusionator
  val biosexConclusionateInst: BiosexConclusionator = new BiosexConclusionator
  val birthdateConclusionateInst: BirthdateConclusionator = new BirthdateConclusionator
  val connect: ConnectToGraphDB = new ConnectToGraphDB
  val partTurboMultiuseClass: TurboMultiuseClass = new TurboMultiuseClass
  val precheck: SparqlPreExpansionChecks = new SparqlPreExpansionChecks
  val reftrackInst: ReferentTracker = new ReferentTracker
  val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks
  val SparqlHelper = new FullStackPostReorgSparql
  val expand: Expander = new Expander

  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null

  //this is the flag of whether to keep or delete triples after the run
  val deleteTriplesOrDatabaseFileAfterRun: Boolean = false

  // TODO: give some thought to making a new helper class ?
  // make text/json -> reference set-of-map methods

  val sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")

  val expectedOntologyTriples = ontologySize.toInt

  val ErrorLogFile = new File(errorLogFile)

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

  /*
   * TEST
   */

  test("complete participant shortcuts") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    val f = repository.getValueFactory()

    // SEE BELOW... test that the log size and modified data don't change over the course of a test

    SparqlHelper.DeleteAllTriples(cxn)
    var RepoEmpty = SparqlHelper.CheckTripleStoreEmpty(cxn)
    assert(RepoEmpty, "The triplestore wasn't cleared")

    var graphList = SparqlHelper.ListNamedGraphs(cxn)
    var expectedGraphs = Set[String]()
    assert(graphList.equals(expectedGraphs), "there are already named graphs in the triplestore")

    // Hayden's method for connecting to a triplestore can load an ontology, 
    // but I am explicitly clearing the triplestore because I don't use Hayden's afterwards-clearer
    // so will have to load the ontology manually
    // centralize this method?
    // already appears in ConnectToGraphDB.scala and CorrectlyPackagedTests.scala
    //HAYDEN 1/11 updated to new ontology URL
    PostConOntoLoad(repository,
      ontologyURL,
      "http://www.itmat.upenn.edu/biobank/ontology",
      "http://transformunify.org/ontologies/")

    // confirm ontology loaded?  how?  number of triples?
    val statementCount = SparqlHelper.CountAcrossTriplestore(cxn)
    assert(statementCount >= expectedOntologyTriples, "after ontology loading, the number of triples\n is less than the expected number for the properties file")

    graphList = SparqlHelper.ListNamedGraphs(cxn)
    //    println(graphList)
    expectedGraphs = Set[String]("http://www.itmat.upenn.edu/biobank/ontology")
    assert(graphList.equals(expectedGraphs), "the observed named graphs don't match the expectations\nafter loading the ontology")

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    // Why had this been inserting  ... turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/PATO_0000384"^^xsd:anyURI ... ???
    // make sure it doesn't appear in any of the participant CSV file
    // or in the participant Karma models/PyTransforms

    val TriplesString = """
<http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000606 "M" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
      """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)
    val shortcutsCount = SparqlHelper.CountOneGraph(cxn, DestinationGraph)

    graphList = SparqlHelper.ListNamedGraphs(cxn)
    expectedGraphs = Set[String](
      "http://transformunify.org/ontologies/participantShortcuts",
      "http://www.itmat.upenn.edu/biobank/ontology")
    assert(graphList.equals(expectedGraphs), "the observed named graphs don't match the expectations\nafter loading shortcuts")

    // confirm that those are the only triples (except for the ontology graph?)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(preOK, "The pre-expansion checkers rejected the comlete/aceptible input participant triples")

    expand.participantExpansion(cxn, f.createIRI("http://transformunify.org/ontologies/r2rinst1"), "shortcut graphs here", "random UUID here")

    graphList = SparqlHelper.ListNamedGraphs(cxn)
    expectedGraphs = Set[String](
      "http://transformunify.org/ontologies/participantShortcuts",
      "http://www.itmat.upenn.edu/biobank/postExpansionCheck",
      "http://www.itmat.upenn.edu/biobank/ontology")
    assert(graphList.equals(expectedGraphs), "the observed named graphs don't match the expectations\nafter expanding the shortcuts")

    helper.clearShortcutNamedGraphs(cxn)

    graphList = SparqlHelper.ListNamedGraphs(cxn)
    expectedGraphs = Set[String](
      "http://www.itmat.upenn.edu/biobank/postExpansionCheck",
      "http://www.itmat.upenn.edu/biobank/ontology")
    //    println(graphList)
    assert(graphList.equals(expectedGraphs), "the observed named graphs don't match the expectations\nafter clearing the shortcuts")

    val postOK = sparqlCheckInst.postExpansionChecks(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "post-expansion")
    assert(postOK, "The post-expansion checkers rejected the expanded participant triples")

    //HAYDEN 10/13 2:05 PM: Adding transfer of enc and part data into expanded named graph from staging area
    helper.moveDataFromOneNamedGraphToAnother(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "http://www.itmat.upenn.edu/biobank/expanded")
    
    //helper.applyInverses(cxn)

    graphList = SparqlHelper.ListNamedGraphs(cxn)
    expectedGraphs = Set[String](
      "http://www.itmat.upenn.edu/biobank/postExpansionCheck",
      "http://www.itmat.upenn.edu/biobank/ontology",
      "http://www.itmat.upenn.edu/biobank/expanded",
      "http://www.itmat.upenn.edu/biobank/inverses")
    assert(graphList.equals(expectedGraphs), "the observed named graphs don't match the expectations\nafter moving from :postExpansionCheck topmbb:expanded")

    //HAYDEN 11/7 12:02 pm failure here is becuase applyInverses has not been called, so the inverses graph cannot be expected to exist.
    
    // confirm PostExpansionGraph cleared ???
    helper.clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck")
    graphList = SparqlHelper.ListNamedGraphs(cxn)
    expectedGraphs = Set[String](
      "http://www.itmat.upenn.edu/biobank/ontology",
      "http://www.itmat.upenn.edu/biobank/expanded",
      "http://www.itmat.upenn.edu/biobank/inverses")
    assert(graphList.equals(expectedGraphs), "the observed named graphs don't match the expectations\nafter moving clearing :postExpansionCheck")

    var ClasslessPresence = SparqlHelper.CheckClasslessInstances(cxn)

    assert(!ClasslessPresence, "classless instance(s) detected!")

    var SinglePartExpRes = SparqlHelper.CheckSinglePartExp(cxn)
    val SinglePartExpSet = SparqlHelper.Res2Set(SinglePartExpRes)
    val expectedSet = Set(Map(
      """dstitle""" -> """"handcrafted_parts.csv"^^<http://www.w3.org/2001/XMLSchema#string>""",
      """dobTval""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
      """gidVal""" -> """"M"^^<http://www.w3.org/2001/XMLSchema#string>""",
      """dobXval""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
      """pscVal""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
      """gidclass""" -> """http://purl.obolibrary.org/obo/OMRSE_00000141"""))
    val matchFlag = SinglePartExpSet.equals(expectedSet)
    assert(matchFlag, "The expanded particpant triples don't have the right pattern")

    // test that the log size and modified data don't change ove the course of a test

    //    println("initial log file size: " + InitialErrorLogFs)
    //    println("initial log file last modified: " + InitialErrorLogMdString)

    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)

    //    println("new log file size: " + FinalErrorLogFs)
    //    println("new log file last modified: " + FinalErrorLogMdString)

    assert((FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file size has changed")
    assert((FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size has been modified")

  }

  /*
     * TEST
     */

  test("participant is of wrong class") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    // SEE BELOW... test that the log size and modified data don't change over the course of a test

    SparqlHelper.DeleteAllTriples(cxn)

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
          a owl:NamedIndividual ;
          turbo:TURBO_0000608 "121" ;
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000604 "12/30/1971" ;
          turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
          turbo:TURBO_0000606 "M" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
    """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    //HAYDEN 10/25 10:04 am: shouldn't this be assert !individualRes? I expect this function to return false due to the bad class declaration 
    // you are correct sir
    // but now using whole preExpansionChecks super-method

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about the class asserted for\n:participant/f5a1aa29115347b080c061198fbc09a8")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

    // count triples in graphs other than ontology ?

    // ---

    assert(!precheck.checkForUnexpectedClasses(cxn, "shortcut graphs here"), "checker didn't identify owl:NamedIndividual as an extraneous class")

  }

  test("PSC missing") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
          a turbo:TURBO_0000502 ;
    #      turbo:TURBO_0000608 "121" ;
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000604 "12/30/1971" ;
          turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
          turbo:TURBO_0000606 "M" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
    """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about mising PSC")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("multiple PSCs") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
          a turbo:TURBO_0000502 ;
          turbo:TURBO_0000608 "121", "999" ;
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000604 "12/30/1971" ;
          turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
          turbo:TURBO_0000606 "M" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
    """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about multiple PSCs")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  // add in additional tests for multiple dependents ?

  test("shortcut prop in ontology but unexpeceted") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
          a turbo:TURBO_0000502 ;
          turbo:TURBO_0000608 "121", "999" ;
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000604 "12/30/1971" ;
          turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
          turbo:TURBO_0000606 "M" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI ;
          turbo:TURBO_0000648 "999" .
    """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about unexpected shortcut")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("shortcut prop undefined") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
          a turbo:TURBO_0000502 ;
          turbo:TURBO_0000608 "121", "999" ;
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000604 "12/30/1971" ;
          turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
          turbo:TURBO_0000606 "M" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI ;
          turbo:ScUndefined "999" .
    """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about undefined shortcut")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("illegal object of object property") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
          a turbo:TURBO_0000502 ;
          turbo:TURBO_0000608 "121", "999" ;
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000604 "12/30/1971" ;
          turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
          turbo:TURBO_0000606 "M" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI ;
          obo:RO_0000087 <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8> .
    """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about illegal object of object property")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("dataset title missing") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
  #      turbo:TURBO_0000603 "handcrafted_parts.csv" ;
        turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
        turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "the checker methods failed to complain about mising dataset title")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("textual DOB missing") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  #      turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
        turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "xsd DOB present without textual DOB, but checker methods failed to complain")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("xsd-formatted DOB missing") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
        # turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
        turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """
    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "xsd DOB present without textual DOB and checker methods failed to complain\nMAY ACTUALLY BE ACCEPTIBLE")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("no DOB data present") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  #      turbo:TURBO_0000604 "12/30/1971" ;
  #      turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
        turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """
//    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)
//
//    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
//    assert(!preOK, "no DOB data at all and checker methods failed to complain\nMAY ACTUALLY BE ACCEPTIBLE")
//
//    // Check log size and mod date
//    val FinalErrorLogFs = ErrorLogFile.length
//    val FinalErrorLogMd = ErrorLogFile.lastModified()
//    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
//    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
//    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("textual GID missing") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
        turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  #      turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "GID URI present without textual GID, but checker methods failed to complain")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("GID URI missing") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
        turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
        # turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
    assert(!preOK, "GID URI present without textual GID, but checker methods failed to complain\nMAY ACTUALLY BE ACCEPTIBLE")

    // Check log size and mod date
    val FinalErrorLogFs = ErrorLogFile.length
    val FinalErrorLogMd = ErrorLogFile.lastModified()
    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("no GID data present") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
        turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  #      turbo:TURBO_0000606 "M" ;
  #      turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

//    val preOK = sparqlCheckInst.preExpansionChecks(cxn)
//    assert(!preOK, "GID URI present without textual GID, but checker methods failed to complain\nMAY ACTUALLY BE ACCEPTIBLE")
//
//    // Check log size and mod date
//    val FinalErrorLogFs = ErrorLogFile.length
//    val FinalErrorLogMd = ErrorLogFile.lastModified()
//    val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
//    assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
//    assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")

  }

  test("complete expanded participant") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"

    val TriplesString = """
<http://transformunify.org/ontologies/11c76c937bd14f1b961a2a6ebb971fbb>
  dc11:title "handcrafted_parts.csv" ;
  a <http://purl.obolibrary.org/obo/IAO_0000100> .

<http://transformunify.org/ontologies/1b7a84283cc24f68bb688800e2e9e7ad>
  turbo:member turbo:InstOutpCont ;
  a <http://purl.obolibrary.org/obo/BFO_0000035> .

turbo:37b3dcff749944b0bf416b8663584b43
  obo:BFO_0000050 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:IAO_0000136 turbo:b75f188bda374f4eb4fe08113d3e1be5 ;
  turbo:TURBO_0006510 "M" ;
  a obo:OMRSE_00000141 .

turbo:4e011ffb599a4692bab75d70838e0d2a
  obo:BFO_0000050 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:IAO_0000136 turbo:1b7a84283cc24f68bb688800e2e9e7ad ;
  turbo:TURBO_0006511 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0006510 "12/30/1971" ;
  a <http://www.ebi.ac.uk/efo/EFO_0004950> .

turbo:InstOutpCont a turbo:Container .
turbo:b75f188bda374f4eb4fe08113d3e1be5
  obo:RO_0000086 turbo:fecca6faac414aa6b78f02eb475e166f ;
  turbo:TURBO_0000303 turbo:1b7a84283cc24f68bb688800e2e9e7ad ;
  turbo:member turbo:InstOutpCont ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8" ;
  a turbo:TURBO_0000502 .

turbo:e8c847c8fee343fe9911b030edec8b10
  obo:BFO_0000050 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:IAO_0000219 turbo:b75f188bda374f4eb4fe08113d3e1be5 ;
  turbo:TURBO_0006510 "121" ;
  a turbo:TURBO_0000503 .

turbo:fecca6faac414aa6b78f02eb475e166f
  turbo:member turbo:InstOutpCont ;
  a obo:PATO_0000047 .

turbo:r2rinst1
  obo:OBI_0000293 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:OBI_0000299 turbo:InstOutpCont ;
  a turbo:TURBO_0000522 .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

  }

  // PartExpExceptionTests.scala calls com.example.app.FullStackPostReorgSparql.ExpandPartScTrips 
  // which can partially expand shortcuts

  // com.example.app.FullStackAllSparql.participantExpansion assumes that the shortcuts have been vetted with
  // com.example.app.DrivetrainSparqlChecks.preExpansionChecks
  // and therefore expands completely

  // the pattern for GID textual/URI is similar

  test("textual DOB missing, expand anyway") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val TriplesString = """
      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0000608 "121" ;
        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
#        turbo:TURBO_0000604 "12/30/1971" ;
        turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
        turbo:TURBO_0000606 "M" ;
        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    expand.participantExpansion(cxn, f.createIRI("http://transformunify.org/ontologies/r2rinst1"), "shortcut graphs here", "random UUID here")

    // this should be true after com.example.app.FullStackPostReorgSparql.ExpandPartScTrips
    // OR com.example.app.FullStackAllSparql.participantExpansion

    val OtherThanDobQ = """
      ask  {
    ?dataset dc11:title ?dstitle ;
             a <http://purl.obolibrary.org/obo/IAO_0000100> .
    ?birth turbo:member turbo:InstOutpCont ;
           a <http://purl.obolibrary.org/obo/BFO_0000035> .
    ?gid
        obo:BFO_0000050 ?dataset ;
        obo:IAO_0000136 ?part ;
        turbo:TURBO_0006510 ?gidTextVal ;
        a obo:OMRSE_00000141 .
    turbo:InstOutpCont a turbo:Container .
    ?part obo:RO_0000086 ?biosex ;
          turbo:TURBO_0000303 ?birth ;
          turbo:member turbo:InstOutpCont ;
          turbo:TURBO_0006601 ?prevUriStr ;
          a turbo:TURBO_0000502 .
    ?psc obo:BFO_0000050 ?dataset ;
         obo:IAO_0000219 ?part ;
         turbo:TURBO_0006510 ?pscVal ;
         a turbo:TURBO_0000503 .
    ?biosex turbo:member turbo:InstOutpCont ;
            a obo:PATO_0000047 .
    turbo:r2rinst1
        obo:OBI_0000293 ?dataset ;
        obo:OBI_0000299 turbo:InstOutpCont ;
        a turbo:TURBO_0000522 .
}
"""

    var CurrentBooleanQ = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlPrefixes + OtherThanDobQ)
    var result = CurrentBooleanQ.evaluate()
    assert(result, "Even the non-DOB bits weren't expanded")

    // this would be true after com.example.app.FullStackAllSparql.participantExpansion 
    // but not com.example.app.FullStackPostReorgSparql.ExpandPartScTrips

    val WithDobExceptTextualQ = """
      ask  {
    ?dataset dc11:title ?dstitle ;
             a <http://purl.obolibrary.org/obo/IAO_0000100> .
    ?birth turbo:member turbo:InstOutpCont ;
           a <http://purl.obolibrary.org/obo/BFO_0000035> .
    ?gid
        obo:BFO_0000050 ?dataset ;
        obo:IAO_0000136 ?part ;
        turbo:TURBO_0006510 ?gidTextVal ;
        a obo:OMRSE_00000141 .
    ?dob obo:BFO_0000050 ?dataset ;
         obo:IAO_0000136 ?birth ;
         turbo:TURBO_0006511 ?dobXsdVal ;
         #  turbo:TURBO_0006510 ?dobTextVal ;
         a <http://www.ebi.ac.uk/efo/EFO_0004950> .
    turbo:InstOutpCont a turbo:Container .
    ?part obo:RO_0000086 ?biosex ;
          turbo:TURBO_0000303 ?birth ;
          turbo:member turbo:InstOutpCont ;
          turbo:TURBO_0006601 ?prevUriStr ;
          a turbo:TURBO_0000502 .
    ?psc obo:BFO_0000050 ?dataset ;
         obo:IAO_0000219 ?part ;
         turbo:TURBO_0006510 ?pscVal ;
         a turbo:TURBO_0000503 .
    ?biosex turbo:member turbo:InstOutpCont ;
            a obo:PATO_0000047 .
    turbo:r2rinst1
        obo:OBI_0000293 ?dataset ;
        obo:OBI_0000299 turbo:InstOutpCont ;
        a turbo:TURBO_0000522 .
}
"""

    CurrentBooleanQ = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlPrefixes + WithDobExceptTextualQ)
    result = CurrentBooleanQ.evaluate()
    assert(result, "Even the non-textual-DOB bits weren't expanded")

    // this should NOT be true after EITHER com.example.app.FullStackPostReorgSparql.ExpandPartScTrips
    // OR com.example.app.FullStackAllSparql.participantExpansion

    val WithAllDobQ = """
      ask  {
    ?dataset dc11:title ?dstitle ;
             a <http://purl.obolibrary.org/obo/IAO_0000100> .
    ?birth turbo:member turbo:InstOutpCont ;
           a <http://purl.obolibrary.org/obo/BFO_0000035> .
    ?gid
        obo:BFO_0000050 ?dataset ;
        obo:IAO_0000136 ?part ;
        turbo:TURBO_0006510 ?gidTextVal ;
        a obo:OMRSE_00000141 .
    ?dob obo:BFO_0000050 ?dataset ;
         obo:IAO_0000136 ?birth ;
         turbo:TURBO_0006511 ?dobXsdVal ;
         turbo:TURBO_0006510 ?dobTextVal ;
         a <http://www.ebi.ac.uk/efo/EFO_0004950> .
    turbo:InstOutpCont a turbo:Container .
    ?part obo:RO_0000086 ?biosex ;
          turbo:TURBO_0000303 ?birth ;
          turbo:member turbo:InstOutpCont ;
          turbo:TURBO_0006601 ?prevUriStr ;
          a turbo:TURBO_0000502 .
    ?psc obo:BFO_0000050 ?dataset ;
         obo:IAO_0000219 ?part ;
         turbo:TURBO_0006510 ?pscVal ;
         a turbo:TURBO_0000503 .
    ?biosex turbo:member turbo:InstOutpCont ;
            a obo:PATO_0000047 .
    turbo:r2rinst1
        obo:OBI_0000293 ?dataset ;
        obo:OBI_0000299 turbo:InstOutpCont ;
        a turbo:TURBO_0000522 .
}
"""

    CurrentBooleanQ = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlPrefixes + WithAllDobQ)
    result = CurrentBooleanQ.evaluate()
    assert(!result, "Somehow a textual DOB was expanded")

  }

  test("expanded participant with classless instance") {

    var InitialErrorLogFs = ErrorLogFile.length
    var InitialErrorLogMd = ErrorLogFile.lastModified()
    var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)

    SparqlHelper.DeleteAllTriples(cxn)

    val f = repository.getValueFactory()

    val DestinationGraph = "http://www.itmat.upenn.edu/biobank/postExpansionCheck"

    val TriplesString = """
<http://transformunify.org/ontologies/11c76c937bd14f1b961a2a6ebb971fbb>
  dc11:title "handcrafted_parts.csv" ;
  a <http://purl.obolibrary.org/obo/IAO_0000100> .

<http://transformunify.org/ontologies/1b7a84283cc24f68bb688800e2e9e7ad>
  turbo:member turbo:InstOutpCont ;
  a <http://purl.obolibrary.org/obo/BFO_0000035> .

turbo:37b3dcff749944b0bf416b8663584b43
  obo:BFO_0000050 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:IAO_0000136 turbo:b75f188bda374f4eb4fe08113d3e1be5 ;
  turbo:TURBO_0006510 "M" ;
  a obo:OMRSE_00000141 .

turbo:4e011ffb599a4692bab75d70838e0d2a
  obo:BFO_0000050 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:IAO_0000136 turbo:1b7a84283cc24f68bb688800e2e9e7ad ;
  turbo:TURBO_0006511 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0006510 "12/30/1971" ;
  a <http://www.ebi.ac.uk/efo/EFO_0004950> .

turbo:InstOutpCont a turbo:Container .
turbo:b75f188bda374f4eb4fe08113d3e1be5
  obo:RO_0000086 turbo:fecca6faac414aa6b78f02eb475e166f ;
  turbo:TURBO_0000303 turbo:1b7a84283cc24f68bb688800e2e9e7ad ;
  turbo:member turbo:InstOutpCont ;
  turbo:TURBO_0006601 "http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8" .

turbo:e8c847c8fee343fe9911b030edec8b10
  obo:BFO_0000050 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:IAO_0000219 turbo:b75f188bda374f4eb4fe08113d3e1be5 ;
  turbo:TURBO_0006510 "121" ;
  a turbo:TURBO_0000503 .

turbo:fecca6faac414aa6b78f02eb475e166f
  turbo:member turbo:InstOutpCont ;
  a obo:PATO_0000047 .

turbo:r2rinst1
  obo:OBI_0000293 turbo:11c76c937bd14f1b961a2a6ebb971fbb ;
  obo:OBI_0000299 turbo:InstOutpCont ;
  a turbo:TURBO_0000522 .
  """

    SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)

    var ClasslessPresence = SparqlHelper.CheckClasslessInstances(cxn)

    assert(ClasslessPresence, "classless instance not detected")

  }

  /*
     * xsd date etc... save for post expansion
     */

  //    test("DOB xsd value tagged wtih wrong type") {
  //  
  //      var InitialErrorLogFs = ErrorLogFile.length
  //      var InitialErrorLogMd = ErrorLogFile.lastModified()
  //      var InitialErrorLogMdString = sdf.format(InitialErrorLogMd)
  //  
  //      SparqlHelper.DeleteAllTriples(cxn)
  //  
  //      val f = repository.getValueFactory()
  //  
  //      val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"
  //  
  //      val TriplesString = """
  //        <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  //          a turbo:TURBO_0000502 ;
  //          turbo:TURBO_0000608 "121" ;
  //          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  //          turbo:TURBO_0000604 "12/30/1971" ;
  //          turbo:TURBO_0000605 "1971-12-30"^^xsd:int ;
  //          turbo:TURBO_0000606 "M" ;
  //          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  //    """
  //  
  //      SparqlHelper.InsertFromString(cxn, TriplesString, DestinationGraph)
  //  
  //      val preOK = sparqlCheckInst.preExpansionChecks(cxn)
  //      assert(!preOK, "DOB xsd value tagged wtih wrong type, but checker methods failed to complain")
  //  
  //      // Check log size and mod date
  //      val FinalErrorLogFs = ErrorLogFile.length
  //      val FinalErrorLogMd = ErrorLogFile.lastModified()
  //      val FinalErrorLogMdString = sdf.format(FinalErrorLogMd)
  //      assert((!FinalErrorLogFs.equals(InitialErrorLogFs)), "The log file didn't change")
  //      assert((!FinalErrorLogMd.equals(InitialErrorLogMd)), "The log file size wasn't modified")
  //  
  //      // count triples in graphs other than ontology
  //  
  //    }
  //
  //    test("DOB xsd value contains wrong type (float)") {
  //  
  //      SparqlHelper.DeleteAllTriples(cxn)
  //  
  //      val f = repository.getValueFactory()
  //  
  //      val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"
  //  
  //      val TriplesString = """
  //      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  //        a turbo:TURBO_0000502 ;
  //        turbo:TURBO_0000608 "121" ;
  //        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  //        turbo:TURBO_0000604 "12/30/1971" ;
  //        turbo:TURBO_0000605 "123.4"^^xsd:date ;
  //        turbo:TURBO_0000606 "M" ;
  //        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  //  """
  //  
  //    }
  //  
  //    test("DOB xsd value contains value in correct format for a date\nbut with an illegal value") {
  //  
  //      SparqlHelper.DeleteAllTriples(cxn)
  //  
  //      val f = repository.getValueFactory()
  //  
  //      val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"
  //  
  //      val TriplesString = """
  //      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  //        a turbo:TURBO_0000502 ;
  //        turbo:TURBO_0000608 "121" ;
  //        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  //        turbo:TURBO_0000604 "12/30/1971" ;
  //        turbo:TURBO_0000605 "1971-99-99"^^xsd:int ;
  //        turbo:TURBO_0000606 "M" ;
  //        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  //  """
  //    }
  //  
  //    test("DOB xsd value contains value in correct format for a date\nbut with an illegal value\nsubtle lep year error") {
  //  
  //      SparqlHelper.DeleteAllTriples(cxn)
  //  
  //      val f = repository.getValueFactory()
  //  
  //      val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"
  //  
  //      val TriplesString = """
  //      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  //        a turbo:TURBO_0000502 ;
  //        turbo:TURBO_0000608 "121" ;
  //        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  //        turbo:TURBO_0000604 "12/30/1971" ;
  //        turbo:TURBO_0000605 "1971-02-29"^^xsd:int ;
  //        turbo:TURBO_0000606 "M" ;
  //        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  //  """
  //  
  //    }
  //  
  //    test("DOB xsd value contains value in correct format for a date\nbut in an unreasonable range\n") {
  //  
  //      SparqlHelper.DeleteAllTriples(cxn)
  //  
  //      val f = repository.getValueFactory()
  //  
  //      val DestinationGraph = "http://transformunify.org/ontologies/participantShortcuts"
  //  
  //      val TriplesString = """
  //      <http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  //        a turbo:TURBO_0000502 ;
  //        turbo:TURBO_0000608 "121" ;
  //        turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  //        turbo:TURBO_0000604 "12/30/1971" ;
  //        turbo:TURBO_0000605 "1771-12-30"^^xsd:int ;
  //        turbo:TURBO_0000606 "M" ;
  //        turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
  //  """
  //  
  //    }

  /*
   * a related test would be: DOB > some encounter date
   */

  /*
   * still need to write tests in which a date tagged-literal is the object toa property expecting soem other type
   * do that at the post expansion stage?
   */

  def PostConOntoLoad(repo: Repository, OntoUrlStr: String, GraphStr: String, BaseStr: String) = {
    // Hayden's method for connecting to a triplestore can load an ontolgy, 
    // but I am explicitly clearing the triplestore because I don't use Hayden's afterwards-clearer
    // so will have to load the ontology manually
    // make this a function
    // already appears in ConnectToGraphDB.scala and CorrectlyPackagedTests.scala

    val f = repo.getValueFactory()

    val OntoUrl = new URL(OntoUrlStr)
    val OntoGraphName = f.createIRI(GraphStr);

    var TempCxn = repository.getConnection
    TempCxn.begin()
    // arguments:  
    // 1) the URL location of the RDF content (our ontology in this case)
    // 2) a string which can be used as the default prefix for any entities that are relative within the RDF upload
    // (I mistakenly thought that was the named graph destination)
    // 3) the format of the triples to be loaded
    // 4) the real destination graph, as a resource like an IRI
    TempCxn.add(OntoUrl, BaseStr, RDFFormat.RDFXML, OntoGraphName)
    TempCxn.commit()
    TempCxn.close

  }

}