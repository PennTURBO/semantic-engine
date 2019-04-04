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
import scala.collection.mutable.HashSet
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
case class TwoDimensionalArrListResults(resultsList: Array[Array[String]])

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
        val br = io.Source.fromFile("anurag_icd_codes.csv")
        var listToPost = new ArrayBuffer[String]
        var totalCount = 0
        val pw = new PrintWriter(new File("anurag_intermediate_results.csv"))
        for (line <- br.getLines())
        {
            totalCount = totalCount + 1
            val splitLine = line.split(",")
            if (splitLine(0) == "ICD-9") listToPost += "http://purl.bioontology.org/ontology/ICD9CM/" + splitLine(1)
            if (splitLine(0) == "ICD-10") listToPost += "http://purl.bioontology.org/ontology/ICD10CM/" + splitLine(1)
            if (listToPost.size % 1000 == 0)
            {
                println("list to post size: " + listToPost.size)
                val post = new HttpPost("http://localhost:8080/diagnoses/getDiseaseURIsFromICDCodes")
                val stringToPost: String = write(Input(searchList = listToPost.toArray))
                post.setEntity(new StringEntity(stringToPost))
                val client = HttpClientBuilder.create().build()
                val response = client.execute(post)
                val responseData = response.getEntity
                val responseString: String = EntityUtils.toString(responseData)
                response.close()
                client.close()
                val res = parse(responseString).extract[TwoDimensionalArrListResults].resultsList
                println("result size: " + res.size)
                for (a <- res)
                {
                    pw.write(a(0) + "," + a(1) + "," + a(2) + "," + a(3) + "\n")
                }
                listToPost = new ArrayBuffer[String]
            }
        }
        println("list to post size: " + listToPost.size)
        val post = new HttpPost("http://localhost:8080/diagnoses/getDiseaseURIsFromICDCodes")
        val stringToPost: String = write(Input(searchList = listToPost.toArray))
        post.setEntity(new StringEntity(stringToPost))
        val client = HttpClientBuilder.create().build()
        val response = client.execute(post)
        val responseData = response.getEntity
        val responseString: String = EntityUtils.toString(responseData)
        response.close()
        client.close()
        val res = parse(responseString).extract[TwoDimensionalArrListResults].resultsList
        println("result size: " + res.size)
        for (a <- res)
        {
            pw.write(a(0) + "," + a(1) + "," + a(2) + "," + a(3) + "\n")
        }
        
        br.close()
        pw.close()
        println("totalcount: " + totalCount)
    }*/
    
    /*test("next steps anurag")
    {
        val br = io.Source.fromFile("anurag_intermediate_results.csv")
        val pw = new PrintWriter(new File(""))
        var currentIcd = ""
        var currentMondo = ""
        var count = 0
        var methodList: ArrayBuffer[String] = ArrayBuffer()
        for (line <- br.getLines())
        {
            println("scanning line: " + line)
            val lineSplit = line.split(",")
            if (count == 0)
            {
                currentIcd = lineSplit(0)
                currentMondo = lineSplit(1)
            }
            if (!(lineSplit(0) == currentIcd && lineSplit(1) == currentMondo))
            {
                var stringToWrite = currentIcd + "," + currentMondo + ","
                for (a <- 2 to lineSplit.size - 2)
                {
                    stringToWrite += lineSplit(a)
                }
                stringToWrite += ","
                for (a <- methodList)
                {
                    stringToWrite += a + ";"
                }
                pw.write(stringToWrite + "\n")
                currentIcd = lineSplit(0)
                currentMondo = lineSplit(1)
                methodList = ArrayBuffer()
                println("reset")
            }
            methodList += lineSplit(lineSplit.size - 1)
            count = count + 1
        }
        println("final count: " + count)
        pw.close()
        br.close()
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
    
    /*test("find hops away")
    {
        //val inputList = Array()
        val buffsource = io.Source.fromFile("anurag_results_without_hops.csv")
        var inputListBuff = new HashSet[String]
      
        var finalResMap = new HashMap[String, Integer]
        
        val buffsource2 = io.Source.fromFile("mondo_hops.csv")
        for (line <- buffsource2.getLines())
        {
            val lineSplit = line.split(",")
            if (lineSplit(1).toInt != -1) finalResMap += lineSplit(0) -> lineSplit(1).toInt
        }
        for (line <- buffsource.getLines())
        {
            val lineSplit = line.split(",")
            if (!finalResMap.contains(lineSplit(1))) inputListBuff += lineSplit(1)
        }

        val inputList = inputListBuff.toArray
        println("size of unmapped terms list: " + inputListBuff.size)
        println("starting request")
        val post = new HttpPost("http://localhost:8080/medications/findHopsAwayFromDrug")
        println("deduped size: " + inputList.size)
        val stringToPost: String = write(Input(searchList = inputList))
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

        val pw = new PrintWriter(new File("mondo_hops_1.csv"))
        pw.write("MONDO,hops")
        pw.println()

        for (a <- inputList)
        {
            pw.println(a+","+(finalResMap(a)-1))
        }
        pw.close()
        buffsource.close()
        buffsource2.close()
    }*/

    /*test("remove non specific mappings")
    {
        val buffsource = io.Source.fromFile("mondo_hops.csv")
        val pw = new PrintWriter(new File("anurag_final_report_1.csv"))
        var mondoToHops = new HashMap[String, Integer]
        
        for (line <- buffsource.getLines())
        {
            val lineSplit = line.split(",")
            mondoToHops += lineSplit(0) -> Integer.parseInt(lineSplit(1))
        }
        
        buffsource.close()
        
        var currentIcd = ""
        var count = 0
        var mondoMap: HashMap[String, String] = new HashMap[String, String]
        var mondoArray: ArrayBuffer[String] = ArrayBuffer()
        
        val buffsource2 = io.Source.fromFile("anurag_results_without_hops.csv")
        for (line <- buffsource2.getLines())
        {
            //println("reading line: " + line)
            val lineSplit = line.split(",")
            if (count == 0)
            {
                currentIcd = lineSplit(0)
            }
            
            if (!(lineSplit(0) == currentIcd))
            {
                var mostSpecific = 0
                var mondoClasses: ArrayBuffer[String] = ArrayBuffer()
                for (a <- mondoArray)
                {
                    if (mondoToHops.contains(a))
                    {
                        if (mondoToHops(a) > mostSpecific)
                        {
                            mostSpecific = mondoToHops(a)
                            mondoClasses = ArrayBuffer(a)
                            //println("reset: " + println(mondoClasses.toArray.deep.mkString("\n")))
                        }
                        else if (mondoToHops(a) == mostSpecific)
                        {
                            mondoClasses += a
                            //println("addition: " + println(mondoClasses.toArray.deep.mkString("\n")))
                        }
                    }
                }
                for (a <- mondoClasses)
                {
                    pw.write(mondoMap(a) + "," + mostSpecific + "\n")
                    //println("writing: " + mondoMap(a) + "," + mostSpecific + "\n")
                }
                mondoMap = new HashMap[String, String]
                mondoArray = ArrayBuffer()
            }
            
            mondoMap += lineSplit(1) -> line
            mondoArray += lineSplit(1)
            
            currentIcd = lineSplit(0)
            
            count = count + 1
        }

        pw.close()
        buffsource2.close()
    }*/
    
    test("fix labels")
    {
        val buffsource = io.Source.fromFile("anurag_final_report_1.csv")
        val pw = new PrintWriter(new File("anurag_labels_fixed.csv"))
        var hm = new HashMap[String, String]
        for (line <- buffsource.getLines())
        {
            val linesplit = line.split(",")
            if (hm.contains(linesplit(1))) pw.write(linesplit(0) + "," + linesplit(1) + "," + hm(linesplit(1)) + "," + linesplit(3) + "," + linesplit(4)+"\n")
            else
            {
                println("looking up: " + linesplit(1))
                val mondo = linesplit(1)
                val sparql = s"""select ?label where {graph obo:mondo.owl { <$mondo> rdfs:label ?label }}"""
                val res = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + sparql, "label")
                val label = res(0).split("\\^")(0)
                hm += mondo -> label
                pw.write(linesplit(0) + "," + linesplit(1) + "," + label + "," + linesplit(3) + "," + linesplit(4)+"\n")
            }
        }
        
        buffsource.close()
        pw.close()
    }
}   