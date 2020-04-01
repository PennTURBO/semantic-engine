package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class DiagnosisExpansionProcessSnapshotTest extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true)
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
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

val insertFullInputDataset = 
"""
            INSERT DATA {
                   # Required triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010160_1> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "52d4e62939ae46b5a56d1b7b10f13b83"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004602> "5c6fb256b8bb4d7f973b182cc5d71c89"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_1> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^xsd:Boolean .
<http://transformunify.org/ontologies/TURBO_0010160_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0010014> "601371260"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004603> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004601> "0efda42fa110412092c8e185467e6426"^^xsd:String .
}

            }
        """
update.updateSparql(cxn, insertFullInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://transformunify.org/ontologies/TURBO_0010113",
"http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/IAO_0000136",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/BFO_0000051","http://transformunify.org/ontologies/TURBO_0010094",
"http://purl.obolibrary.org/obo/OBI_0000299","http://purl.obolibrary.org/obo/BFO_0000055",
"http://purl.obolibrary.org/obo/RO_0000087","http://transformunify.org/ontologies/TURBO_0010013",
"http://transformunify.org/ontologies/TURBO_0010014","http://transformunify.org/ontologies/TURBO_0006515"

)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }
test("minimum fields test")
{

val insertFullInputDataset = 
"""
            INSERT DATA {
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010160_1> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "52d4e62939ae46b5a56d1b7b10f13b83"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004602> "5c6fb256b8bb4d7f973b182cc5d71c89"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_1> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

            }
        """
update.updateSparql(cxn, insertFullInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://transformunify.org/ontologies/TURBO_0010113",
"http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/IAO_0000136",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/OBI_0000299","http://purl.obolibrary.org/obo/BFO_0000055",
"http://purl.obolibrary.org/obo/RO_0000087","http://transformunify.org/ontologies/TURBO_0006515"

)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }}