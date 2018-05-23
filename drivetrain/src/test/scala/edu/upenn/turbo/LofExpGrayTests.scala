package edu.upenn.turbo

import java.io.FileOutputStream
import java.net.URL
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.scalatest.BeforeAndAfter
import org.scalatest._

class LofExpGrayTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  val expand: Expander = new Expander()
  val connect: ConnectToGraphDB = new ConnectToGraphDB()
  //  val sparqlChecks = new DrivetrainSparqlChecks()
  //  val postchecks = new SparqlPostExpansionChecks()
  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null

  //  //this is the flag of whether to keep or delete triples after the run
  //  val deleteTriplesOrDatabaseFileAfterRun: Boolean = false

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

  test("placeholder / stub test") {

    // the last karma instantiation didn't include the anyURi tag on the TURBO_0007609 object
    val oneLofScBlock = """
      """

    val graphString = "<http://http://www.itmat.upenn.edu/biobank/oneLofScBlock>"

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    var HelperInstance = new Helper4SparqlTests
    var initialStmtCt = HelperInstance.CountTriples(cxn, false)

    val UpdateStatement = """
      prefix : <http://transformunify.org/ontologies/>
      prefix alleleInfo: <http://localhost:8080/source/alleleInfo/>
      prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      prefix obo: <http://purl.obolibrary.org/obo/>
      prefix turbo: <http://transformunify.org/ontologies/>
      prefix xsd: <http://www.w3.org/2001/XMLSchema#>
      insert data {
alleleInfo:b78d_10bf rdf:type obo:OBI_0001352 .
alleleInfo:b78d_10bf turbo:TURBO_0007601 "66EE7A08-E660-4D7E-B24D-C62AC448E311" .
alleleInfo:b78d_10bf turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000422"^^xsd:anyURI .
alleleInfo:b78d_10bf turbo:TURBO_0007604 "http://purl.obolibrary.org/obo/PR_O43657"^^xsd:anyURI .
alleleInfo:b78d_10bf turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
alleleInfo:b78d_10bf turbo:TURBO_0007605 "TSPAN6(ENSG00000000003)" .
alleleInfo:b78d_10bf turbo:TURBO_0007602 "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
alleleInfo:b78d_10bf turbo:TURBO_0007606 "1" .
alleleInfo:b78d_10bf turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^xsd:anyURI .
alleleInfo:b78d_10bf turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^xsd:anyURI .
      }
      """

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()

    var subsequentStmtCt = HelperInstance.CountTriples(cxn, false)

    //    logger.warn("postcount: " + subsequentStmtCt.toString)

    // are there more triples in the database after the insert?
    assert(subsequentStmtCt > initialStmtCt, "doesn't look like any triples were added")
  }

}