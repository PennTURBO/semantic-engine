package edu.upenn.turbo

import java.net.URL
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import org.scalatest.BeforeAndAfter
import org.scalatest._

class CorrectlyPackagedTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  val conc: Conclusionator = new Conclusionator()
  val reftrackInst: ReferentTracker = new ReferentTracker()
  val joiner: EntityLinker = new EntityLinker()
  val biosexConclusionateInst: BiosexConclusionator = new BiosexConclusionator()
  val birthdateConclusionateInst: BirthdateConclusionator = new BirthdateConclusionator()
  val expand: Expander = new Expander()

  val connect: ConnectToGraphDB = new ConnectToGraphDB()
  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null

  //this is the flag of whether to keep or delete triples after the run
  val deleteTriplesOrDatabaseFileAfterRun: Boolean = false

  // TODO: give some thought to making a new helper class ?
  // make text/json -> reference SoM 
  val SparqlHelper = new FullStackPostReorgSparql()

  after {
    //Setting "false" here will cause triples to stay in database after termination of the test
    connect.closeGraphConnection(cxn, repoManager, repository, false)
  }

  // will break up
  test("one full stack test, many labelled assertions") {

    //HAYDEN 10/2: Adding parameter to method call to meet new requirement
    val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
    cxn = graphDBMaterials.getConnection()
    repoManager = graphDBMaterials.getRepoManager()
    repository = graphDBMaterials.getRepository()
    val f = repository.getValueFactory()
    val sparqlCheckInst: DrivetrainSparqlChecks = new DrivetrainSparqlChecks

    SparqlHelper.DeleteAllTriples(cxn)
    // starting with a blank triplestore ?
    // do I really want to do that?  maybe leave the ontology itself in there?

    SparqlHelper.InsertEncScTrips(cxn)

    SparqlHelper.InsertPartScTripsGidClarified(cxn)
    // TODO: ADD TEST?: were the expected shortcut triples inserted

    //HAYDEN 10/10 3:34 PM I am adding the argument for the instantiation process which now must be supplied
    expand.encounterExpansion(cxn, f.createIRI("http://transformunify.org/ontologies/r2rinst1"), "shortcut graphs here")

    // TODO: ask test... participant expansion is select... make them both selects?
    val EncExpSuccess = SparqlHelper.CheckExpandedEncScTrips(cxn)
    assert(EncExpSuccess, "...Encounters were not expanded as expected")

    expand.participantExpansion(cxn, f.createIRI("http://transformunify.org/ontologies/r2rinst1"), "shortcut graphs here", "random UUID here")

    // TODO: select test... encounter expansion is an ask... make them both selects?
    val PartExpRes = SparqlHelper.CheckExpandedPartScTripsClarifiedGID(cxn)

    // optionally delete shortcuts
    helper.clearShortcutNamedGraphs(cxn)

    val PartExpResSet = SparqlHelper.Res2Set(PartExpRes)

    //    println(PartExpResSet)

    var ExpectedSet = SparqlHelper.SendExpectedPartExpRes

    assert(PartExpResSet.equals(ExpectedSet), "... participants were not expanded as expected")

    // insert joins
    SparqlHelper.InsertEncPartJoinTrips(cxn)

    //HAYDEN 10/13 2:05 PM: Adding transfer of enc and part data into expanded named graph from staging area
    helper.moveDataFromOneNamedGraphToAnother(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck", "http://www.itmat.upenn.edu/biobank/expanded")
    helper.clearNamedGraph(cxn, "http://www.itmat.upenn.edu/biobank/postExpansionCheck")
    //HAYDEN 12/19 1:52 PM: Adding a specific inverse property so that reftracking of PSCs and enc IDs will work properly
    val addInverse: String = """
      INSERT 
      {
        graph <http://www.itmat.upenn.edu/biobank/inverses>
        {
          ?o <http://purl.obolibrary.org/obo/BFO_0000051> ?s .
        }
      }
      WHERE 
      {
          ?s <http://purl.obolibrary.org/obo/BFO_0000050> ?o .
      }
      """
    helper.updateSparql(cxn, addInverse)
    reftrackInst.reftrackParticipantsAndDependents(cxn)

    val PartRtRes = SparqlHelper.MinimalPartRefTrackTest(cxn)

    val PartRtResSet = SparqlHelper.Res2Set(PartRtRes)

    ExpectedSet = SparqlHelper.SendEpectedPartRtRes

    assert(PartRtResSet.equals(ExpectedSet), "... participants were not reftracked as expected")

    //fullstackInst.reftrackParticipantDependents(cxn)

    val PartDepsTrackedOk = SparqlHelper.CheckPartDepRt(cxn)
    assert(PartDepsTrackedOk, "... dependents of participants were not reftracked as expected")

    reftrackInst.reftrackEncountersAndDependents(cxn)

    joiner.joinParticipantsAndEncounters(cxn)

    //HAYDEN 10/12 10:13 Changing double values to conform to what is expected by tests 
    //This value must be greater than .5 and less than or equal to 1. If it is not, the program will default to using a value of 1.
    conc.runConclusionationProcess(cxn, 0.51, 0.51)

    val BiosexConclRes = SparqlHelper.BiosexConclTest(cxn)

    val BiosexConclResSet = SparqlHelper.Res2Set(BiosexConclRes)

    val BiosexConclExpectation = SparqlHelper.SendExpectedBiosexConclRes

    assert(BiosexConclResSet.equals(BiosexConclExpectation), "...The concluded biological sex types don't agree with the GIDs in the CSV")

    val DobConclRes = SparqlHelper.DobConclTest(cxn)
    val DobConclResSet = SparqlHelper.Res2Set(DobConclRes)

    val DobConclExpectation = SparqlHelper.SendExpectedDobConclRes

    assert(DobConclResSet.equals(DobConclExpectation), "...The concluded birthdates don't agree with the birthdates in the CSV")
    
    // addOntologyFromUrl in /turbo_drivetrain/src/main/scala/com/example/app/TurboMultiuseClass.scala
    // pass it the repo

    // this won't work CORRECTLY unless the ontology is loaded into graphpmbb:ontology
    // maybe should check if ontology is loaded first?

    //HAYDEN 1/11/18 new ontology URL
    val OntoUrl = new URL(ontologyURL)
    val OntoGraphName = f.createIRI("http://www.itmat.upenn.edu/biobank/ontology");

    val OntoBase = "http://transformunify.org/ontologies/"

    var TempCxn = repository.getConnection
    TempCxn.begin()
    // arguments:  
    // 1) the URL location of the RDF content (our ontology in this case)
    // 2) a string which can be used as the default prefix for any entities that are relative within the RDF upload
    // (I mistakenly thought that was the named graph destination)
    // 3) the format of the triples to be loaded
    // 4) the real destination graph, as a resource like an IRI
    TempCxn.add(OntoUrl, OntoBase, RDFFormat.RDFXML, OntoGraphName)
    TempCxn.commit()
    TempCxn.close
  
    val UndefinedClassesPres = SparqlHelper.CheckUndefinedClasses(cxn)
    assert(!UndefinedClassesPres, "...There are instances in the triplestore whose asserted classes are not defined in the turbo ontology")

    val UndefinedPropsPres = SparqlHelper.CheckUndefinedProps(cxn)
    assert(!UndefinedPropsPres, "...There are triples in the triplestore using properties/predicates that are not defined in the turbo ontology")

    val PartCount = SparqlHelper.CountParticipants(cxn)
    assert(PartCount == """"2"^^<http://www.w3.org/2001/XMLSchema#integer>""", "... The participants count is wrong.")

    val InvSexPres = SparqlHelper.CheckInverseSexConcs(cxn)
    assert(InvSexPres, "... Incorrect sex conclusions are present.")

    val MinorDobConcAbsent = SparqlHelper.UnexpectedDobConclusion(cxn)
    assert(MinorDobConcAbsent, "... at least one minor DOB was erroneously elevated to a conclusion.")

    val ExpectedRawJoins = SparqlHelper.CheckRawJoinTrips(cxn)
    assert(ExpectedRawJoins, "... The expected encounter ID-PSC join triples were not found.")

    val FinalRes = SparqlHelper.SocalledFinalTest(cxn)
    val FinalResSet = SparqlHelper.Res2Set(FinalRes)

    val FinalExpectation = SparqlHelper.SendExpectedFinalRes

    //    assert(FinalResSet.equals(FinalExpectation),
    //      "...A sample query was run to test conclusionation and the inferences of healthcare encounter participation\nThe result did not agree with the expected pattern")

    assert(FinalExpectation.subsetOf(FinalResSet),
      "...A sample query was run to test conclusionation and the inferences of healthcare encouter participation\nThe result did not agree with the expected pattern")

  }

}

