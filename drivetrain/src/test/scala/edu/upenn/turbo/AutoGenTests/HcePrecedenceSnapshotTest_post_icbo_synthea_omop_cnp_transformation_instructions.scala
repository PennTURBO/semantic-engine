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
class HcePrecedenceSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "c8773ba49a0341ebbd2fcbf2461f1ff2"
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
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#preceding_visit_occurrence_id> "683839312abc"^^xsd:String .
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#visit_concept_id> "1889010940abc"^^xsd:String .
<http://api.stardog.com/visit_occurrence_1> rdf:type <http://api.stardog.com/visit_occurrence> .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/EncClassList_1> <http://transformunify.org/ontologies/TURBO_0010147> "1889010940abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/EncClassList_1> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/OGMS_0000097> .
<https://github.com/PennTURBO/Drivetrain/PrevencsClass_1> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/OGMS_0000097> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/Prevenc_1> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://transformunify.org/ontologies/TURBO_0010094> "683839312abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010433_1_EncKeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_1> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://transformunify.org/ontologies/TURBO_0010433_1_EncKeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<https://github.com/PennTURBO/Drivetrain/Prevenc_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://api.stardog.com/visit_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<http://api.stardog.com/visit_occurrence_1> rdf:type <http://api.stardog.com/visit_occurrence> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/HcePrecedence")
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
"""<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<https://github.com/PennTURBO/Drivetrain/Prevenc_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_EncKeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://api.stardog.com/visit_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/Prevenc_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_EncKeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1> <http://transformunify.org/ontologies/TURBO_0010094> "683839312abc"^^<http://www.w3.org/2001/XMLSchema#String>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}