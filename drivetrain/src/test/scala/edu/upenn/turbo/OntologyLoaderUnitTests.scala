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
        val result: String = ontLoad.getBioportalSubmissionInfo("ICD10CM")
        logger.info(result)
    }
}