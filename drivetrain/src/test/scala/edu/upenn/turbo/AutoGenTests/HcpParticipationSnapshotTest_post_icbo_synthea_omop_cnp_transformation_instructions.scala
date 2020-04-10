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
class HcpParticipationSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "ff2cd26fabaf4e5d99d1aefbdf22be76"
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
<http://api.stardog.com/procedure_occurrence_2> rdf:type <http://api.stardog.com/procedure_occurrence> .
<http://api.stardog.com/procedure_occurrence_1> <http://api.stardog.com/procedure_occurrence#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/procedure_occurrence_1> rdf:type <http://api.stardog.com/procedure_occurrence> .
<http://api.stardog.com/procedure_occurrence_2> <http://api.stardog.com/procedure_occurrence#person_id> "2144189648abc"^^xsd:String .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://transformunify.org/ontologies/TURBO_0010433_2> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2> .
<https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000093> .
<http://transformunify.org/ontologies/TURBO_0010433_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/procedure_occurrence_2> rdf:type <http://api.stardog.com/procedure_occurrence> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://api.stardog.com/person_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://api.stardog.com/procedure_occurrence_1> rdf:type <http://api.stardog.com/procedure_occurrence> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/procedure_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> .
<http://api.stardog.com/person_1> rdf:type <http://api.stardog.com/person> .
<http://api.stardog.com/procedure_occurrence_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/HcpParticipation")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000093>""",
"""<http://api.stardog.com/person_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/person>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://api.stardog.com/procedure_occurrence_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/procedure_occurrence>""",
"""<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://api.stardog.com/procedure_occurrence_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/procedure_occurrence>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/1b069901941e958f4982f9a5df6c1ec8a1fa400a757f9bd1be798528b184fa4b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/e258295bf689b6248b2b3b89f072f0cb78caac70dbf6f972dea0563487a45637> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://www.itmat.upenn.edu/biobank/50f8a4b0f084274ed9ebac34cac6cecae37c0e4174bd17471c9b7d1ea77bae63> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/e53aab5d1625c98b4531f29f44f667e17fa4ae51abbad73ca737330d76e36d96> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/person_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://api.stardog.com/procedure_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1>""",
"""<http://api.stardog.com/procedure_occurrence_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext>""",
"""<http://www.itmat.upenn.edu/biobank/e258295bf689b6248b2b3b89f072f0cb78caac70dbf6f972dea0563487a45637> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010464>""",
"""<http://www.itmat.upenn.edu/biobank/e53aab5d1625c98b4531f29f44f667e17fa4ae51abbad73ca737330d76e36d96> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010464>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/e258295bf689b6248b2b3b89f072f0cb78caac70dbf6f972dea0563487a45637>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/e53aab5d1625c98b4531f29f44f667e17fa4ae51abbad73ca737330d76e36d96>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/e258295bf689b6248b2b3b89f072f0cb78caac70dbf6f972dea0563487a45637> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/e53aab5d1625c98b4531f29f44f667e17fa4ae51abbad73ca737330d76e36d96> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/1b069901941e958f4982f9a5df6c1ec8a1fa400a757f9bd1be798528b184fa4b> <http://www.w3.org/2002/07/owl#annotatedSource> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/50f8a4b0f084274ed9ebac34cac6cecae37c0e4174bd17471c9b7d1ea77bae63> <http://www.w3.org/2002/07/owl#annotatedSource> <https://github.com/PennTURBO/Drivetrain/ProcToBeTyped_2>""",
"""<http://www.itmat.upenn.edu/biobank/1b069901941e958f4982f9a5df6c1ec8a1fa400a757f9bd1be798528b184fa4b> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/BFO_0000055>""",
"""<http://www.itmat.upenn.edu/biobank/50f8a4b0f084274ed9ebac34cac6cecae37c0e4174bd17471c9b7d1ea77bae63> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/BFO_0000055>""",
"""<http://www.itmat.upenn.edu/biobank/1b069901941e958f4982f9a5df6c1ec8a1fa400a757f9bd1be798528b184fa4b> <http://www.w3.org/2002/07/owl#annotatedTarget> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/50f8a4b0f084274ed9ebac34cac6cecae37c0e4174bd17471c9b7d1ea77bae63> <http://www.w3.org/2002/07/owl#annotatedTarget> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/1b069901941e958f4982f9a5df6c1ec8a1fa400a757f9bd1be798528b184fa4b> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/e258295bf689b6248b2b3b89f072f0cb78caac70dbf6f972dea0563487a45637>""",
"""<http://www.itmat.upenn.edu/biobank/50f8a4b0f084274ed9ebac34cac6cecae37c0e4174bd17471c9b7d1ea77bae63> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/e53aab5d1625c98b4531f29f44f667e17fa4ae51abbad73ca737330d76e36d96>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}