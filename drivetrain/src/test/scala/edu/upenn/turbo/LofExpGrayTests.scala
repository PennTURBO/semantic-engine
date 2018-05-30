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
  
  
/*1. I would not hardcode "false" as a parameter into the delete triples method, because this won't work when run in conjunction with other tests. 
  I get this might be useful for developing/debugging at this point however, but once the test is complete I would change it. 
2. I would put the line of code that clears all triples in the database in your "before" method. 
3. instead of including the prefixes on every sparql insert statement, you can just say "sparqlPrefixes + query" in your call to the update method, this is a global variable. 
  You can even go into projectwide globals and add prefixes to the list if you want. 
*/


  val expand: Expander = new Expander()
  val connect: ConnectToGraphDB = new ConnectToGraphDB()
  //  val sparqlChecks = new DrivetrainSparqlChecks()
  //  val postchecks = new SparqlPostExpansionChecks()
  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null
  
//  var 

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
      graph <http://http://www.itmat.upenn.edu/biobank/oneLofScBlock> {
alleleInfo:b78d_10bf rdf:type obo:OBI_0001352 .
alleleInfo:b78d_10bf turbo:TURBO_0007601 "annono-enc" .
alleleInfo:b78d_10bf turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000422"^^xsd:anyURI .
alleleInfo:b78d_10bf turbo:TURBO_0007604 "http://purl.obolibrary.org/obo/PR_O43657"^^xsd:anyURI .
alleleInfo:b78d_10bf turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
alleleInfo:b78d_10bf turbo:TURBO_0007605 "TSPAN6(ENSG00000000003)" .
alleleInfo:b78d_10bf turbo:TURBO_0007602 "annono-geno_id" .
alleleInfo:b78d_10bf turbo:TURBO_0007606 "1" .
alleleInfo:b78d_10bf turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^xsd:anyURI .
alleleInfo:b78d_10bf turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^xsd:anyURI .
      }}
      """

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()

    var subsequentStmtCt = HelperInstance.CountTriples(cxn, false)

    //    logger.warn("postcount: " + subsequentStmtCt.toString)

    // are there more triples in the database after the insert?
    assert(subsequentStmtCt > initialStmtCt, "doesn't look like any triples were added")

  }

}