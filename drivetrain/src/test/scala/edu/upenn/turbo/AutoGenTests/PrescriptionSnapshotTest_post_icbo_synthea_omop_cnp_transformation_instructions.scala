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
class PrescriptionSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "e69e9a28d25a4b51b6674e9edd15d896"
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
<http://api.stardog.com/drug_exposure_1> <http://api.stardog.com/drug_exposure#drug_exposure_start_datetime> "362454259abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_1> <http://api.stardog.com/drug_exposure#drug_type_concept_id> "868359611abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_1> <http://api.stardog.com/drug_exposure#drug_source_value> "808753002abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_1> rdf:type <http://api.stardog.com/drug_exposure> .
<http://api.stardog.com/drug_exposure_1> <http://api.stardog.com/drug_exposure#drug_exposure_id> "1006779358abc"^^xsd:String .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/WrittenNotDispensedMOD_1> <http://transformunify.org/ontologies/TURBO_0010147> "868359611abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/WrittenNotDispensedMOD_1> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/OBI_0000011> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/Prescription")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://api.stardog.com/drug_exposure_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/drug_exposure>""",
"""<http://www.itmat.upenn.edu/biobank/cce1571052d991ed4244761a0652344de0fbeaf71141f979e21a849720e2a3ef> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/7e1627d3e896b806736010c81c3e559e25e1d3be2039fd314896a08f930c5e1e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/b634e5f0a506d1c850167c409df36b60e2b42c134327357074d819302e1988d4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/341d979e116ef661b0eb705a63a27de9f532d19d58e6b2edcfd12cda1a58e548> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://api.stardog.com/drug_exposure_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/7e1627d3e896b806736010c81c3e559e25e1d3be2039fd314896a08f930c5e1e>""",
"""<http://www.itmat.upenn.edu/biobank/7e1627d3e896b806736010c81c3e559e25e1d3be2039fd314896a08f930c5e1e> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010707>""",
"""<http://www.itmat.upenn.edu/biobank/b634e5f0a506d1c850167c409df36b60e2b42c134327357074d819302e1988d4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010707>""",
"""<http://www.itmat.upenn.edu/biobank/b634e5f0a506d1c850167c409df36b60e2b42c134327357074d819302e1988d4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cce1571052d991ed4244761a0652344de0fbeaf71141f979e21a849720e2a3ef>""",
"""<http://www.itmat.upenn.edu/biobank/341d979e116ef661b0eb705a63a27de9f532d19d58e6b2edcfd12cda1a58e548> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010706>""",
"""<http://www.itmat.upenn.edu/biobank/cce1571052d991ed4244761a0652344de0fbeaf71141f979e21a849720e2a3ef> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/7e1627d3e896b806736010c81c3e559e25e1d3be2039fd314896a08f930c5e1e>""",
"""<http://www.itmat.upenn.edu/biobank/cce1571052d991ed4244761a0652344de0fbeaf71141f979e21a849720e2a3ef> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/341d979e116ef661b0eb705a63a27de9f532d19d58e6b2edcfd12cda1a58e548>""",
"""<http://www.itmat.upenn.edu/biobank/cce1571052d991ed4244761a0652344de0fbeaf71141f979e21a849720e2a3ef> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010511>""",
"""<http://www.itmat.upenn.edu/biobank/7e1627d3e896b806736010c81c3e559e25e1d3be2039fd314896a08f930c5e1e> <http://transformunify.org/ontologies/TURBO_0010094> "808753002abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b634e5f0a506d1c850167c409df36b60e2b42c134327357074d819302e1988d4> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/341d979e116ef661b0eb705a63a27de9f532d19d58e6b2edcfd12cda1a58e548> <http://purl.obolibrary.org/obo/IAO_0000004> "362454259abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/7e1627d3e896b806736010c81c3e559e25e1d3be2039fd314896a08f930c5e1e> <http://purl.obolibrary.org/obo/IAO_0000581> <http://www.itmat.upenn.edu/biobank/341d979e116ef661b0eb705a63a27de9f532d19d58e6b2edcfd12cda1a58e548>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}