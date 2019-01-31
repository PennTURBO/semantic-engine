package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class CancerSumStageExpansionTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  val connect: ConnectToGraphDB = new ConnectToGraphDB()
  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null
  val clearDatabaseAfterRun: Boolean = false

  before {
    val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
    cxn = graphDBMaterials.getConnection()
    repoManager = graphDBMaterials.getRepoManager()
    repository = graphDBMaterials.getRepository()
    helper.deleteAllTriplesInDatabase(cxn)
  }
  after {
    connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
  }

  test("math") {
    var x = 4
    var y = 2
    var z = 0
    z = x + y
    //    z should be (3)

    val insertStmt: String = """
  insert data { graph pmbb:Shortcuts_sumStage { 
  <http://www.itmat.upenn.edu/biobank/TURBO_0010039_1> a <http://transformunify.org/ontologies/TURBO_0010039>;
    <http://transformunify.org/ontologies/TURBO_0010042> "IIIb";
    <http://transformunify.org/ontologies/TURBO_0010043> "HUP";
    <http://transformunify.org/ontologies/TURBO_0010044> "http://transformunify.org/ontologies/TURBO_0000410"^^xsd:anyURI;
    <http://transformunify.org/ontologies/TURBO_0010045> "7db6ef12" . }}
      """

    update.updateSparql(cxn, sparqlPrefixes + insertStmt)

    assert(z == 6, "i want 6")
  }

}