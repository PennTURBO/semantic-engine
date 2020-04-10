package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import org.eclipse.rdf4j.model.Literal
class DiagnosisSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "527bd8a8ebf14246a2fa86154c70ab9c"
    RunDrivetrainProcess.setGlobalUUID(UUIDKey)
    RunDrivetrainProcess.setInputNamedGraphsCache(false)
}

override def afterAll()
{
    ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
}

before
{
    helper.deleteAllTriplesInDatabase(cxn)
}


test("all fields test")
{

val insertInputDataset = 
"""
            INSERT DATA {
                   # Required triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://api.stardog.com/condition_occurrence_1> <http://api.stardog.com/condition_occurrence#condition_occurrence_id> "1050745981"^^xsd:Integer .
<http://api.stardog.com/condition_occurrence_1> <http://api.stardog.com/condition_occurrence#condition_start_datetime> "648648338abc"^^xsd:String .
<http://api.stardog.com/condition_occurrence_1> <http://api.stardog.com/condition_occurrence#condition_source_value> "1058648386abc"^^xsd:String .
<http://api.stardog.com/condition_occurrence_1> rdf:type <http://api.stardog.com/condition_occurrence> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/Diagnosis")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://api.stardog.com/condition_occurrence_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/condition_occurrence>""",
"""<http://www.itmat.upenn.edu/biobank/74301ba2683237d5cedbf4226d7508329e94fb6f6d90f86a3a26e342201ca005> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/3cf3ce592c1ff4a8772083a2f9c8b7619dcb58ffd5c2e62aa482acec403b3882> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/76ed9714fecfb4b7614032d56cb15c5a6871921d88fda6a1b88f33b22b998797> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/190e5b82948b28637330c1eb0e1b671b69b6ec581c35013e2a950901e6d01f55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://api.stardog.com/condition_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/3cf3ce592c1ff4a8772083a2f9c8b7619dcb58ffd5c2e62aa482acec403b3882>""",
"""<http://www.itmat.upenn.edu/biobank/3cf3ce592c1ff4a8772083a2f9c8b7619dcb58ffd5c2e62aa482acec403b3882> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010605>""",
"""<http://www.itmat.upenn.edu/biobank/76ed9714fecfb4b7614032d56cb15c5a6871921d88fda6a1b88f33b22b998797> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010605>""",
"""<http://www.itmat.upenn.edu/biobank/76ed9714fecfb4b7614032d56cb15c5a6871921d88fda6a1b88f33b22b998797> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/74301ba2683237d5cedbf4226d7508329e94fb6f6d90f86a3a26e342201ca005>""",
"""<http://www.itmat.upenn.edu/biobank/190e5b82948b28637330c1eb0e1b671b69b6ec581c35013e2a950901e6d01f55> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010604>""",
"""<http://www.itmat.upenn.edu/biobank/74301ba2683237d5cedbf4226d7508329e94fb6f6d90f86a3a26e342201ca005> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/3cf3ce592c1ff4a8772083a2f9c8b7619dcb58ffd5c2e62aa482acec403b3882>""",
"""<http://www.itmat.upenn.edu/biobank/74301ba2683237d5cedbf4226d7508329e94fb6f6d90f86a3a26e342201ca005> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/190e5b82948b28637330c1eb0e1b671b69b6ec581c35013e2a950901e6d01f55>""",
"""<http://www.itmat.upenn.edu/biobank/74301ba2683237d5cedbf4226d7508329e94fb6f6d90f86a3a26e342201ca005> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010510>""",
"""<http://www.itmat.upenn.edu/biobank/3cf3ce592c1ff4a8772083a2f9c8b7619dcb58ffd5c2e62aa482acec403b3882> <http://transformunify.org/ontologies/TURBO_0010094> "1058648386abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/76ed9714fecfb4b7614032d56cb15c5a6871921d88fda6a1b88f33b22b998797> <http://transformunify.org/ontologies/TURBO_0010094> "1050745981"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/190e5b82948b28637330c1eb0e1b671b69b6ec581c35013e2a950901e6d01f55> <http://purl.obolibrary.org/obo/IAO_0000004> "648648338abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/3cf3ce592c1ff4a8772083a2f9c8b7619dcb58ffd5c2e62aa482acec403b3882> <http://purl.obolibrary.org/obo/IAO_0000581> <http://www.itmat.upenn.edu/biobank/190e5b82948b28637330c1eb0e1b671b69b6ec581c35013e2a950901e6d01f55>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}