package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID
import java.io.BufferedReader
import java.io.FileReader
import scala.collection.mutable.HashSet

class Sandbox extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val clearDatabaseAfterRun: Boolean = true
    val objectOrientedExpander = new ObjectOrientedExpander
    
     before
    {
        val graphDBMaterials: TurboGraphConnection = ConnectToGraphDB.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    //find duplicates between two files
    /*{
        println("starting sandbox")
        val br = new BufferedReader (new FileReader ("empi_compare.csv"))
        val br2 = new BufferedReader (new FileReader ("empi_i639patients.txt"))
        println("opened files")
        var count = 0
        var line = ""
        var firstSet = new HashSet[String]
        var secondSet = new HashSet[String]
        
          while ({line = br.readLine ; line != null})
          {
              firstSet += line
  			  }

          while ({line = br2.readLine ; line != null})
          {
              secondSet += line
  			  }

        println("size of overlap: " + firstSet.&(secondSet).size)
        br.close()
        br2.close()
    }*/
}