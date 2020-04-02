package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class HomoSapiensExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "carnival_instructionSet.ttl")
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

val insertInputDataset = 
"""
            INSERT DATA {
                   # Required triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010079> "285031059abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010194> "1003789261abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010098> "708384193abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010191> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010100> "1543800849abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010086> "2020-04-02T13:49:37.590"^^xsd:Date .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010085> "28852662abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010277> <http://transformunify.org/ontologies/TURBO_0010274> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010089> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010090> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


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

val insertInputDataset = 
"""
            INSERT DATA {
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010079> "285031059abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


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