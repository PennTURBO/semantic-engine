package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader

import java.nio.file.Path
import java.nio.file.Paths
import java.io.PrintWriter

import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.json4s.jackson.JsonMethods._
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.impl.SimpleValueFactory

case class Input(searchList: Array[String])
case class DrugResult(resultsList: Map[String, Array[String]])

class RunQueriesFromTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    implicit val formats = DefaultFormats
    
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
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
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }

    /*test("convert string to icd9 or icd10")
    {
        val br = io.Source.fromFile("icd_code_list.txt")
        var finalList = new ArrayBuffer[String]
        var eliminatedCount = 0
        var totalCount = 0
        for (line <- br.getLines())
        {
            totalCount = totalCount + 1
            /*try
            {
                Integer.parseInt(line.charAt(0).toString)
                finalList += "<" + "http://purl.bioontology.org/ontology/ICD9CM/" + line + ">"
            }
            catch
            {
                case e: NumberFormatException =>
                {
                    if (line.charAt(0) != 'E' && line.charAt(0) != 'V')
                    {
                        finalList += "<" + "http://purl.bioontology.org/ontology/ICD10CM/" + line + ">"
                    }*/
                    if (line.charAt(0) == 'E' || line.charAt(0) == 'V')
                    {
                        println("adding code: " + line)
                        finalList += "<" + "http://purl.bioontology.org/ontology/ICD10CM/" + line + ">"
                        finalList += "<" + "http://purl.bioontology.org/ontology/ICD9CM/" + line + ">"
                        //eliminatedCount = eliminatedCount + 1
                    }
                }
            /*}
        }*/

        println("final results size: " + finalList.size)
        println("eliminated: " + eliminatedCount)
        println("total: " + totalCount)
        println()
        val pw = new PrintWriter(new File("formattedCodes.txt"))
        for (a <- finalList) pw.println(a)
        br.close()
        pw.close()
    }*/

    /*test("create mondo to icd mappings for graphdb repository")
    {
        val br = io.Source.fromFile("mondoToIcdMappings.csv")
        var builder = new ModelBuilder()
        override val namedGraph = "http://graphBuilder.org/mondoToIcdMappingsFullSemantics"
        val factory = SimpleValueFactory.getInstance()
        var count = 0
        for (line <- br.getLines())
        {
            count = count + 1
            val lineArr = line.split(",")
            builder.namedGraph(namedGraph).subject(lineArr(0)).add("http://graphBuilder.org/mapsTo", 
                factory.createIRI(lineArr(1)))
            if (count % 5000 == 0)
            {
                cxn.add(builder.build)
                println("count: " + count)
                builder = new ModelBuilder()
            }
        }
        cxn.add(builder.build)
    }*/
    
    test("find hops away")
    {
        //val inputList = Array()
        val buffsource = io.Source.fromFile("mondo_classes.csv")
        var inputListBuff = new ArrayBuffer[String]
        for (line <- buffsource.getLines())
        {
            inputListBuff += line
        }
        val inputList = inputListBuff.toArray

        println("starting request")
        val post = new HttpPost("http://localhost:8080/medications/findHopsAwayFromDrug")
        val dedupedList = inputList.toSet.toArray
        println("deduped size: " + dedupedList.size)
        val stringToPost: String = write(Input(searchList = dedupedList))
        post.setEntity(new StringEntity(stringToPost))
        val client = HttpClientBuilder.create().build()
        val response = client.execute(post)
        val responseData = response.getEntity
        val responseString: String = EntityUtils.toString(responseData)
        response.close()
        client.close()
        println("received result")
        
        // convert JSON to MedLookupResult using json4s methods
        val res = parse(responseString).extract[DrugResult]
        println("result size: " + res.resultsList.size)
        var finalResMap = new HashMap[String, Integer]
        for ((k,v) <- res.resultsList)
        {
            if (v.size != 0) finalResMap += k -> v(0).split(",").size
            else 
            {
                finalResMap += k -> 0
                println("result has no connection to top level class: " + k)
            }
        }
        /*for ((k,v) <- finalResMap)
        {
            println("key: " + k)
            for ((k1,v1) <- v)
            {
                println("value: " + k1 + " " + v1)
            }
        }*/

        val pw = new PrintWriter(new File("anurag_hops.csv"))
        pw.write("MONDO,hops")
        pw.println()

        for (a <- inputList)
        {
            pw.println(a+","+(finalResMap(a)-1))
        }
        pw.close()
    }

    /*test("add labels to diagnoses")
    {
        val buffsource = io.Source.fromFile("reports/drugs_to_roles.csv")
        val pw = new PrintWriter(new File("reports/IBD_meds_with_roles.csv"))

        var newMap = new HashMap[String, ArrayBuffer[String]]
        var count = 0
        for (line <- buffsource.getLines())
        {
            count = count + 1
            //println("processing line: " + count)
            val lineArr = line.split(",")
            if (lineArr(0) != "Drug")
            {
                if (newMap.contains(lineArr(0)))
                {
                    val int1 = Integer.parseInt(newMap(lineArr(0))(2))
                    val int2 = Integer.parseInt(lineArr(lineArr.size-1))
                    if (int1 < int2)
                    {
                        newMap.remove(lineArr(0))
                        newMap += lineArr(0) -> ArrayBuffer(lineArr(lineArr.size-3),lineArr(lineArr.size-2),lineArr(lineArr.size-1))
                    }
                    else if (int1 == int2)
                    {
                        newMap(lineArr(0))(0) = newMap(lineArr(0))(0) + " | " + lineArr(lineArr.size-3)
                        newMap(lineArr(0))(1) = newMap(lineArr(0))(1) + " | " + lineArr(lineArr.size-2)
                    }
                }
                else newMap += lineArr(0) -> ArrayBuffer(lineArr(lineArr.size-3),lineArr(lineArr.size-2),lineArr(lineArr.size-1))
            }
        }

        buffsource.close()

        val buffsource2 = io.Source.fromFile("reports/ibd_meds_rxnorm.csv")
        
        for (line <- buffsource2.getLines())
        {
            val lineArr = line.split(",")
            if (lineArr(0) == "consenterUUID") pw.println(line)
            else
            {
                if (newMap.contains(lineArr(lineArr.size-1)))
                {
                    pw.println(line + "," + newMap(lineArr(lineArr.size-1))(0) + "," + newMap(lineArr(lineArr.size-1))(1))
                }
                else pw.println(line + "," + "NA" + "," + "NA")
            }
        }

        buffsource2.close()
        pw.close()
    }*/
}   