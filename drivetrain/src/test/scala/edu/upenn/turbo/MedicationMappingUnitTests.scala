package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class MedicationMappingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val medmap: MedicationMapper = new MedicationMapper
    
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
        connect.closeConnectionDeleteTriples(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("tidy order name")
    {
        medmap.tidyOrderName("-hello-") should be ("hello")
        medmap.tidyOrderName("hell-o") should be ("hell-o")
        medmap.tidyOrderName("(hello)") should be ("(hello)")
        medmap.tidyOrderName("hell()o") should be ("hell()o")
        medmap.tidyOrderName("CEFTRIAXONE IV SYRINGE 2G/20ML (CNR)") should be ("ceftriaxone iv syringe 2g/20ml (cnr)")
    }
}