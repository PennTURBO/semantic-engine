package edu.upenn.turbo

import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader
import scala.collection.mutable.HashMap
import java.io.PrintWriter
import org.ddahl.rscala
import scala.collection.mutable.ArrayBuffer
import java.util.Arrays
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.json4s.jackson.JsonMethods._
import org.eclipse.rdf4j.query.TupleQueryResult

case class MedFullName(fullName: List[String])
case class MedLookupResult(medFullName: String, mappedTerm: String)

class MedicationMapper extends ProjectwideGlobals
{   
    implicit val formats = DefaultFormats
    val connect: ConnectToGraphDB = new ConnectToGraphDB
    
    def runMedicationMapping(cxn: RepositoryConnection)
    {
        //val medsToMap: ArrayBuffer[ArrayBuffer[Value]] = getAllUnmappedMedsInfo(cxn)
        val mappingResult: List[MedLookupResult] = getMappingsFromService(
            MedFullName(fullName = List("INSULIN ASPART 100 UNIT/ML SC SOLN", "ONDANSETRON HCL 4 MG/2ML INJECTION SOLN", "sodium chloride 0.9% -")))
        logger.info("mapping1: " + mappingResult(2).mappedTerm + " " + mappingResult(2).medFullName)
    }
    
    def getMappingsFromService(mappedStrings: MedFullName): List[MedLookupResult] =
    {
        val post = new HttpPost("http://localhost:8080/medications")
        val stringToPost: String = write(mappedStrings)
        post.setEntity(new StringEntity(stringToPost))
        val client = HttpClientBuilder.create().build()
        val response = client.execute(post)
        val responseData = response.getEntity
        val responseString: String = EntityUtils.toString(responseData)
        logger.info(responseString)
        response.close()
        client.close()
        
        // convert JSON to MedLookupResult using json4s methods
        parse(responseString).extract[List[MedLookupResult]]
    }
    
    def getAllUnmappedMedsInfo(cxn: RepositoryConnection): Option[TupleQueryResult] =
    {
        val getInfo: String = 
         """
             Select ?prescript ?ordername Where
             {
                 ?prescript a obo:PDRO_0000001 .
                 ?prescript turbo:TURBO_0006512 ?ordername .
             }         
         """
        update.querySparql(cxn, sparqlPrefixes + getInfo)
    }
}