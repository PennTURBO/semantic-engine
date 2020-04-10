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
class RxParthoodSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "2e340558db704d1c9e6a55e25dd573d3"
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
<http://api.stardog.com/drug_exposure_1> <http://api.stardog.com/drug_exposure#visit_occurrence_id> "316978036abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_1> rdf:type <http://api.stardog.com/drug_exposure> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://purl.obolibrary.org/obo/PDRO_0000024_1> rdf:type <http://purl.obolibrary.org/obo/PDRO_0000024> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/PDRO_0000024_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010256> .
<http://api.stardog.com/visit_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<http://api.stardog.com/drug_exposure_1> rdf:type <http://api.stardog.com/drug_exposure> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "316978036abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/PDRO_0000024_1> .
<http://api.stardog.com/visit_occurrence_1> rdf:type <http://api.stardog.com/visit_occurrence> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/RxParthood")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://api.stardog.com/visit_occurrence_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/visit_occurrence>""",
"""<http://api.stardog.com/drug_exposure_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/drug_exposure>""",
"""<http://purl.obolibrary.org/obo/PDRO_0000024_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/ad69fb32437e3e5eabe679faa138e48ad2ce391b74e7593fa0bd094b5711ff79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/ff934a17849a91f6b529a53eb1eff64cf3da43c22af42dd57570e18055396790> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/visit_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://api.stardog.com/drug_exposure_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext>""",
"""<http://www.itmat.upenn.edu/biobank/ff934a17849a91f6b529a53eb1eff64cf3da43c22af42dd57570e18055396790> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010703>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/ff934a17849a91f6b529a53eb1eff64cf3da43c22af42dd57570e18055396790>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010256>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "316978036abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/ff934a17849a91f6b529a53eb1eff64cf3da43c22af42dd57570e18055396790> <http://transformunify.org/ontologies/TURBO_0010094> "316978036abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://www.itmat.upenn.edu/biobank/ad69fb32437e3e5eabe679faa138e48ad2ce391b74e7593fa0bd094b5711ff79> <http://www.w3.org/2002/07/owl#annotatedSource> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/ad69fb32437e3e5eabe679faa138e48ad2ce391b74e7593fa0bd094b5711ff79> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/OBI_0000299>""",
"""<http://www.itmat.upenn.edu/biobank/ad69fb32437e3e5eabe679faa138e48ad2ce391b74e7593fa0bd094b5711ff79> <http://www.w3.org/2002/07/owl#annotatedTarget> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://www.itmat.upenn.edu/biobank/ad69fb32437e3e5eabe679faa138e48ad2ce391b74e7593fa0bd094b5711ff79> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/ff934a17849a91f6b529a53eb1eff64cf3da43c22af42dd57570e18055396790>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}