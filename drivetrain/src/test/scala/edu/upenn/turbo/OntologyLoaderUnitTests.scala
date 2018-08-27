package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class OntologyLoaderUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    val ontLoad: OntologyLoader = new OntologyLoader
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = false
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("get bioportal submission version")
    {
        val result: Option[Int] = ontLoad.getBioportalSubmissionInfo("ICD10CM")
        result.get should be (14)
        val result2: Option[Int] = ontLoad.getBioportalSubmissionInfo("ICD9CM")
        result2.get should be (14)
        val result3: Option[Int] = ontLoad.getBioportalSubmissionInfo("RXNORM")
        result3.get should be (15)
        val result4: Option[Int] = ontLoad.getBioportalSubmissionInfo("this_isnt_an_ontology")
        result4 should be (None)
    }
}