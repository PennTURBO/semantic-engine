package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.io.Reader
import java.io.FileReader

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
        /*val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()*/
    }
    after
    {
        //connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("query API with drug roles")
    {
        val inputList = Array(
        "http://purl.obolibrary.org/obo/CHEBI_36047",					
        "http://purl.obolibrary.org/obo/CHEBI_35441",					
        "http://purl.obolibrary.org/obo/CHEBI_50249",					
        "http://purl.obolibrary.org/obo/CHEBI_37890",					
        "http://purl.obolibrary.org/obo/CHEBI_48279",					
        "http://purl.obolibrary.org/obo/CHEBI_49159",					
        "http://purl.obolibrary.org/obo/CHEBI_36044",					
        "http://purl.obolibrary.org/obo/CHEBI_35474",					
        "http://purl.obolibrary.org/obo/CHEBI_35488",					
        "http://purl.obolibrary.org/obo/CHEBI_35623",					
        "http://purl.obolibrary.org/obo/CHEBI_35717",					
        "http://purl.obolibrary.org/obo/CHEBI_35475",					
        "http://purl.obolibrary.org/obo/CHEBI_49167",					
        "http://purl.obolibrary.org/obo/CHEBI_35471",					
        "http://purl.obolibrary.org/obo/CHEBI_35476",					
        "http://purl.obolibrary.org/obo/CHEBI_37956",					
        "http://purl.obolibrary.org/obo/CHEBI_48561",					
        "http://purl.obolibrary.org/obo/CHEBI_48876",					
        "http://purl.obolibrary.org/obo/CHEBI_35472",					
        "http://purl.obolibrary.org/obo/CHEBI_35941",					
        "http://purl.obolibrary.org/obo/CHEBI_50514",					
        "http://purl.obolibrary.org/obo/CHEBI_51373",					
        "http://purl.obolibrary.org/obo/CHEBI_35470",					
        "http://purl.obolibrary.org/obo/CHEBI_50267",					
        "http://purl.obolibrary.org/obo/CHEBI_35620",					
        "http://purl.obolibrary.org/obo/CHEBI_35674",					
        "http://purl.obolibrary.org/obo/CHEBI_38877",					
        "http://purl.obolibrary.org/obo/CHEBI_88188",					
        "http://purl.obolibrary.org/obo/CHEBI_35480",					
        "http://purl.obolibrary.org/obo/CHEBI_35610",					
        "http://purl.obolibrary.org/obo/CHEBI_64915",					
        "http://purl.obolibrary.org/obo/CHEBI_50919",					
        "http://purl.obolibrary.org/obo/CHEBI_55324",					
        "http://purl.obolibrary.org/obo/CHEBI_50646",					
        "http://purl.obolibrary.org/obo/CHEBI_51039",					
        "http://purl.obolibrary.org/obo/CHEBI_33231",					
        "http://purl.obolibrary.org/obo/CHEBI_47868",					
        "http://purl.obolibrary.org/obo/CHEBI_48425",					
        "http://purl.obolibrary.org/obo/CHEBI_59683",					
        "http://purl.obolibrary.org/obo/CHEBI_35482",					
        "http://purl.obolibrary.org/obo/CHEBI_38867",					
        "http://purl.obolibrary.org/obo/CHEBI_55322",					
        "http://purl.obolibrary.org/obo/CHEBI_35469",					
        "http://purl.obolibrary.org/obo/CHEBI_35640",					
        "http://purl.obolibrary.org/obo/CHEBI_50503",					
        "http://purl.obolibrary.org/obo/CHEBI_50176",					
        "http://purl.obolibrary.org/obo/CHEBI_86327",					
        "http://purl.obolibrary.org/obo/CHEBI_76595",					
        "http://purl.obolibrary.org/obo/CHEBI_90749",					
        "http://purl.obolibrary.org/obo/CHEBI_50177",					
        "http://purl.obolibrary.org/obo/CHEBI_35530",					
        "http://purl.obolibrary.org/obo/CHEBI_61016",					
        "http://purl.obolibrary.org/obo/CHEBI_62868",					
        "http://purl.obolibrary.org/obo/CHEBI_35443",					
        "http://purl.obolibrary.org/obo/CHEBI_35444",					
        "http://purl.obolibrary.org/obo/CHEBI_49201",					
        "http://purl.obolibrary.org/obo/CHEBI_77307",					
        "http://purl.obolibrary.org/obo/CHEBI_50748",					
        "http://purl.obolibrary.org/obo/CHEBI_50137",					
        "http://purl.obolibrary.org/obo/CHEBI_90755",					
        "http://purl.obolibrary.org/obo/CHEBI_35522",					
        "http://purl.obolibrary.org/obo/CHEBI_35523",					
        "http://purl.obolibrary.org/obo/CHEBI_35481",					
        "http://purl.obolibrary.org/obo/CHEBI_49110",					
        "http://purl.obolibrary.org/obo/CHEBI_35457",					
        "http://purl.obolibrary.org/obo/CHEBI_50266",					
        "http://purl.obolibrary.org/obo/CHEBI_66987",					
        "http://purl.obolibrary.org/obo/CHEBI_55323",					
        "http://purl.obolibrary.org/obo/CHEBI_48878",					
        "http://purl.obolibrary.org/obo/CHEBI_51371",					
        "http://purl.obolibrary.org/obo/CHEBI_35554",					
        "http://purl.obolibrary.org/obo/CHEBI_48675",					
        "http://purl.obolibrary.org/obo/CHEBI_50427",					
        "http://purl.obolibrary.org/obo/CHEBI_35524",					
        "http://purl.obolibrary.org/obo/CHEBI_66993",					
        "http://purl.obolibrary.org/obo/CHEBI_38325",					
        "http://purl.obolibrary.org/obo/CHEBI_35493",					
        "http://purl.obolibrary.org/obo/CHEBI_49023",					
        "http://purl.obolibrary.org/obo/CHEBI_67198",					
        "http://purl.obolibrary.org/obo/CHEBI_50733",					
        "http://purl.obolibrary.org/obo/CHEBI_77715",					
        "http://purl.obolibrary.org/obo/CHEBI_51177",					
        "http://purl.obolibrary.org/obo/CHEBI_53784",					
        "http://purl.obolibrary.org/obo/CHEBI_64571",					
        "http://purl.obolibrary.org/obo/CHEBI_38068",					
        "http://purl.obolibrary.org/obo/CHEBI_73336",					
        "http://purl.obolibrary.org/obo/CHEBI_48407",					
        "http://purl.obolibrary.org/obo/CHEBI_48560",					
        "http://purl.obolibrary.org/obo/CHEBI_66956",					
        "http://purl.obolibrary.org/obo/CHEBI_48218",					
        "http://purl.obolibrary.org/obo/CHEBI_35679",					
        "http://purl.obolibrary.org/obo/CHEBI_50247",					
        "http://purl.obolibrary.org/obo/CHEBI_75769",					
        "http://purl.obolibrary.org/obo/CHEBI_23888",					
        "http://purl.obolibrary.org/obo/CHEBI_35526",					
        "http://purl.obolibrary.org/obo/CHEBI_65259",					
        "http://purl.obolibrary.org/obo/CHEBI_38070",					
        "http://purl.obolibrary.org/obo/CHEBI_66980",					
        "http://purl.obolibrary.org/obo/CHEBI_50248",					
        "http://purl.obolibrary.org/obo/CHEBI_136860",					
        "http://purl.obolibrary.org/obo/CHEBI_50370",					
        "http://purl.obolibrary.org/obo/CHEBI_50513",					
        "http://purl.obolibrary.org/obo/CHEBI_60606",					
        "http://purl.obolibrary.org/obo/CHEBI_50847",					
        "http://purl.obolibrary.org/obo/CHEBI_35705",					
        "http://purl.obolibrary.org/obo/CHEBI_63726",					
        "http://purl.obolibrary.org/obo/CHEBI_49323",					
        "http://purl.obolibrary.org/obo/CHEBI_70709",					
        "http://purl.obolibrary.org/obo/CHEBI_38869",					
        "http://purl.obolibrary.org/obo/CHEBI_38870",					
        "http://purl.obolibrary.org/obo/CHEBI_36043",					
        "http://purl.obolibrary.org/obo/CHEBI_132992",					
        "http://purl.obolibrary.org/obo/CHEBI_50857",					
        "http://purl.obolibrary.org/obo/CHEBI_50846",					
        "http://purl.obolibrary.org/obo/CHEBI_50855",					
        "http://purl.obolibrary.org/obo/CHEBI_38147",					
        "http://purl.obolibrary.org/obo/CHEBI_35569",					
        "http://purl.obolibrary.org/obo/CHEBI_47958",					
        "http://purl.obolibrary.org/obo/CHEBI_50268",					
        "http://purl.obolibrary.org/obo/CHEBI_37930",					
        "http://purl.obolibrary.org/obo/CHEBI_51065",					
        "http://purl.obolibrary.org/obo/CHEBI_35942",					
        "http://purl.obolibrary.org/obo/CHEBI_35498",					
        "http://purl.obolibrary.org/obo/CHEBI_37962",					
        "http://purl.obolibrary.org/obo/CHEBI_50949",					
        "http://purl.obolibrary.org/obo/CHEBI_35820",					
        "http://purl.obolibrary.org/obo/CHEBI_74783",					
        "http://purl.obolibrary.org/obo/CHEBI_38323",					
        "http://purl.obolibrary.org/obo/CHEBI_37955",					
        "http://purl.obolibrary.org/obo/CHEBI_35337",					
        "http://purl.obolibrary.org/obo/CHEBI_39456",					
        "http://purl.obolibrary.org/obo/CHEBI_66981",					
        "http://purl.obolibrary.org/obo/CHEBI_51068",					
        "http://purl.obolibrary.org/obo/CHEBI_74530",					
        "http://purl.obolibrary.org/obo/CHEBI_36333",					
        "http://purl.obolibrary.org/obo/CHEBI_35816",					
        "http://purl.obolibrary.org/obo/CHEBI_48422",					
        "http://purl.obolibrary.org/obo/CHEBI_70868",					
        "http://purl.obolibrary.org/obo/CHEBI_65190",					
        "http://purl.obolibrary.org/obo/CHEBI_77035",					
        "http://purl.obolibrary.org/obo/CHEBI_37886",					
        "http://purl.obolibrary.org/obo/CHEBI_48873",					
        "http://purl.obolibrary.org/obo/CHEBI_66991",					
        "http://purl.obolibrary.org/obo/CHEBI_35842",					
        "http://purl.obolibrary.org/obo/CHEBI_35841",					
        "http://purl.obolibrary.org/obo/CHEBI_50507",					
        "http://purl.obolibrary.org/obo/CHEBI_35497",					
        "http://purl.obolibrary.org/obo/CHEBI_65191",					
        "http://purl.obolibrary.org/obo/CHEBI_77034",					
        "http://purl.obolibrary.org/obo/CHEBI_37887",					
        "http://purl.obolibrary.org/obo/CHEBI_59229",					
        "http://purl.obolibrary.org/obo/CHEBI_59010",					
        "http://purl.obolibrary.org/obo/CHEBI_48676",					
        "http://purl.obolibrary.org/obo/CHEBI_59680",					
        "http://purl.obolibrary.org/obo/CHEBI_50844",					
        "http://purl.obolibrary.org/obo/CHEBI_49326",					
        "http://purl.obolibrary.org/obo/CHEBI_50739",					
        "http://purl.obolibrary.org/obo/CHEBI_50792",					
        "http://purl.obolibrary.org/obo/CHEBI_50837",					
        "http://purl.obolibrary.org/obo/CHEBI_65265",					
        "http://purl.obolibrary.org/obo/CHEBI_49020",					
        "http://purl.obolibrary.org/obo/CHEBI_77567",					
        "http://purl.obolibrary.org/obo/CHEBI_64054",					
        "http://purl.obolibrary.org/obo/CHEBI_73296",					
        "http://purl.obolibrary.org/obo/CHEBI_59282",					
        "http://purl.obolibrary.org/obo/CHEBI_59283",					
        "http://purl.obolibrary.org/obo/CHEBI_51060",					
        "http://purl.obolibrary.org/obo/CHEBI_50691",					
        "http://purl.obolibrary.org/obo/CHEBI_36335",					
        "http://purl.obolibrary.org/obo/CHEBI_48278",					
        "http://purl.obolibrary.org/obo/CHEBI_48539",					
        "http://purl.obolibrary.org/obo/CHEBI_35477",					
        "http://purl.obolibrary.org/obo/CHEBI_64370",					
        "http://purl.obolibrary.org/obo/CHEBI_36063",					
        "http://purl.obolibrary.org/obo/CHEBI_64933",					
        "http://purl.obolibrary.org/obo/CHEBI_38956",					
        "http://purl.obolibrary.org/obo/CHEBI_71173",					
        "http://purl.obolibrary.org/obo/CHEBI_36710",					
        "http://purl.obolibrary.org/obo/CHEBI_37961",					
        "http://purl.obolibrary.org/obo/CHEBI_35821",					
        "http://purl.obolibrary.org/obo/CHEBI_50751",					
        "http://purl.obolibrary.org/obo/CHEBI_25540",					
        "http://purl.obolibrary.org/obo/CHEBI_51374",					
        "http://purl.obolibrary.org/obo/CHEBI_35473",					
        "http://purl.obolibrary.org/obo/CHEBI_35845",					
        "http://purl.obolibrary.org/obo/CHEBI_76988",					
        "http://purl.obolibrary.org/obo/CHEBI_35846",					
        "http://purl.obolibrary.org/obo/CHEBI_59727",					
        "http://purl.obolibrary.org/obo/CHEBI_51372",					
        "http://purl.obolibrary.org/obo/CHEBI_59844",					
        "http://purl.obolibrary.org/obo/CHEBI_77608",					
        "http://purl.obolibrary.org/obo/CHEBI_48525",					
        "http://purl.obolibrary.org/obo/CHEBI_50864",					
        "http://purl.obolibrary.org/obo/CHEBI_52425",					
        "http://purl.obolibrary.org/obo/CHEBI_50671",					
        "http://purl.obolibrary.org/obo/CHEBI_35442",					
        "http://purl.obolibrary.org/obo/CHEBI_50685",					
        "http://purl.obolibrary.org/obo/CHEBI_49324",					
        "http://purl.obolibrary.org/obo/CHEBI_90757",					
        "http://purl.obolibrary.org/obo/CHEBI_35678",					
        "http://purl.obolibrary.org/obo/CHEBI_51451",					
        "http://purl.obolibrary.org/obo/CHEBI_52726",					
        "http://purl.obolibrary.org/obo/CHEBI_63533",					
        "http://purl.obolibrary.org/obo/CHEBI_59886",					
        "http://purl.obolibrary.org/obo/CHEBI_61026",					
        "http://purl.obolibrary.org/obo/CHEBI_71031",					
        "http://purl.obolibrary.org/obo/CHEBI_50779",					
        "http://purl.obolibrary.org/obo/CHEBI_38324",					
        "http://purl.obolibrary.org/obo/CHEBI_50141",					
        "http://purl.obolibrary.org/obo/CHEBI_137431",					
        "http://purl.obolibrary.org/obo/CHEBI_91016",					
        "http://purl.obolibrary.org/obo/CHEBI_71027",					
        "http://purl.obolibrary.org/obo/CHEBI_50926",					
        "http://purl.obolibrary.org/obo/CHEBI_38941",					
        "http://purl.obolibrary.org/obo/CHEBI_50827",					
        "http://purl.obolibrary.org/obo/CHEBI_36053",					
        "http://purl.obolibrary.org/obo/CHEBI_90753",					
        "http://purl.obolibrary.org/obo/CHEBI_74529",					
        "http://purl.obolibrary.org/obo/CHEBI_35818",					
        "http://purl.obolibrary.org/obo/CHEBI_85384",					
        "http://purl.obolibrary.org/obo/CHEBI_50433",					
        "http://purl.obolibrary.org/obo/CHEBI_35499"							
        )
        
        println("starting request")
        val post = new HttpPost("http://localhost:8080/medications/findHopsAwayFromDrug")
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
            if (v.size > 1 && v(0).length != v(1).length)
            {
                println("mismatch: " + k) 
            }
        }
    }
}