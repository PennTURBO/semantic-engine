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
import org.eclipse.rdf4j.query.BindingSet

case class MedFullName(fullName: List[String])
case class MedLookupResult(medFullName: String, mappedTerm: String)

class MedicationMapper extends ProjectwideGlobals
{   
    implicit val formats = DefaultFormats
    val connect: ConnectToGraphDB = new ConnectToGraphDB
    
    def runMedicationMapping(cxn: RepositoryConnection)
    {
        // retrieve all instances of prescription with an order name
        val medsIterator: TupleQueryResult = getAllUnmappedMedsInfo(cxn).get
        // iterate through result, creating mapping of prescriptions -> orderNames, and single list of orderNames for mapping service
        var prescriptionToOrderNameMap: HashMap[String, ArrayBuffer[String]] = new HashMap[String, ArrayBuffer[String]]
        var medsStringBuffer: ArrayBuffer[String] = new ArrayBuffer[String]
        while(medsIterator.hasNext())
        {
            val bSet: BindingSet = medsIterator.next
            val orderName: String = bSet.getBinding("ordername").toString.split("\\^")(0).split("\\=")(1).replaceAll("\"","")
            if (prescriptionToOrderNameMap contains orderName) prescriptionToOrderNameMap(orderName) += bSet.getBinding("prescription").toString
            else prescriptionToOrderNameMap += orderName -> ArrayBuffer(bSet.getBinding("prescription").toString)
            medsStringBuffer += orderName
            //logger.info("order name: " + orderName + " prescription: " + bSet.getBinding("prescription").toString)
        }
        
        // convert to set for removing duplicates, then to list
        val medsStringList: List[String] = medsStringBuffer.toSet.toList
        
        for (name <- medsStringList) println(name)
        // send list of order names to service for mapping
        val mappingResult: List[MedLookupResult] = getMappingsFromService(MedFullName(fullName = medsStringList))
        
        // match up mapped meds with results from hashmap matching on order name, store results in model
        var model: Model = new LinkedHashModel()
        val f: ValueFactory = cxn.getValueFactory()
        for (lookupResult <- mappingResult)
        {
            val mappedOrderName: String = lookupResult.medFullName
            val mappedMedication: String = lookupResult.mappedTerm
            
            for (prescription <- prescriptionToOrderNameMap(mappedOrderName))
            {
                model.add(f.createIRI(prescription), f.createIRI("http://transformunify.org/ontologies/relevant_pred"), f.createIRI(mappedMedication))
                logger.info("adding triple: " + prescription + " predicate " + mappedMedication)
            }
        }
        //cxn.add(model)
    }
    
    def getMappingsFromService(mappedStrings: MedFullName): List[MedLookupResult] =
    {
        val post = new HttpPost("http://localhost:8080/medications/orderNameLookup")
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
             Select ?prescription ?ordername Where
             {
                 ?prescription a obo:PDRO_0000001 .
                 ?prescription turbo:TURBO_0006512 ?ordername .
             }         
             #LIMIT 20
         """
        update.querySparql(cxn, sparqlPrefixes + getInfo)
    }
}