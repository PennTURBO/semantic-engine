package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class HomoSapiensExpansionProcessSnapshotTest extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
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
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010079> "32d0e5efed2f47eeb9a8716510951fa3"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010086> "2020-04-01T12:34:25.863"^^xsd:Date .
<http://transformunify.org/ontologies/TURBO_0010191_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010191> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010085> "05a883556ebe4a4fb262e7ee1b634011"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010194> "2e7f852a2b7c408a83535d2cb8f7ecbb"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010100> "192dd56bf82846bcb844d8da06b5aeef"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010098> "6b75eb76a4d34a8eb0b06c44411f7279"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010277> <http://transformunify.org/ontologies/TURBO_0010274> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010089> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010090> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
}

            }
        """
update.updateSparql(cxn, insertFullInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://transformunify.org/ontologies/TURBO_0010113","http://transformunify.org/ontologies/TURBO_0010113",
"http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/IAO_0000136",
"http://purl.obolibrary.org/obo/IAO_0000136","http://purl.obolibrary.org/obo/IAO_0000136",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/IAO_0000219","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
"http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
"http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
"http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
"http://purl.obolibrary.org/obo/IAO_0000004","http://transformunify.org/ontologies/TURBO_0000303",
"http://purl.obolibrary.org/obo/IDO_0000664"
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
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010079> "32d0e5efed2f47eeb9a8716510951fa3"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
}

            }
        """
update.updateSparql(cxn, insertFullInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://transformunify.org/ontologies/TURBO_0010113",
"http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/IAO_0000219","http://purl.obolibrary.org/obo/BFO_0000051",
"http://transformunify.org/ontologies/TURBO_0010094"
)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }}