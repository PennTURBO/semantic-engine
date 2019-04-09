/*package edu.upenn.turbo

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
import java.io.PrintWriter
import java.io.File
import scala.collection.mutable.HashMap

class Sandbox extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val clearDatabaseAfterRun: Boolean = false
    val objectOrientedExpander = new ObjectOrientedExpander
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearDatabaseAfterRun)
    }
    
    test(""){
        println("starting sandbox")
        val br = io.Source.fromFile("anurag_final_report.csv")
        val pw = new PrintWriter (new File ("anurag_report_labels_fixed.csv"))
        
        val labelMap = new HashMap[String, String]
        
        for (line <- br.getLines())
        {
            println("scanning line " + line)
            var currLabel = ""
            val lineSplit = line.split(",")
            if (labelMap.contains(lineSplit(1)))
            {
                currLabel = labelMap(lineSplit(1))
            }
            else
            {
                val mondoClass = lineSplit(1)
                val query = s"""
                  select ?label where {graph obo:mondo.owl {<$mondoClass> rdfs:label ?label . }}
                  """
                val res = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, "label")
                currLabel = res(0)
                labelMap += lineSplit(1) -> currLabel
            }
            pw.write(lineSplit(0) + "," + lineSplit(1) + "," + currLabel.split("\\^")(0) + "," + lineSplit(3) + "," + lineSplit(4) + "\n")
        }
        
        br.close()
        pw.close()
    }
}*/