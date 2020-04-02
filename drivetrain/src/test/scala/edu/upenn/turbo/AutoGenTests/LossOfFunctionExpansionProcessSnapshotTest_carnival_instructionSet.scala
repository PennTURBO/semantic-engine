package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class LossOfFunctionExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
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
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010015> "334459855abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010016> "210815534abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010095> "1216405858"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0007605> "953377515abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010285> <http://transformunify.org/ontologies/TURBO_0000567> .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0007607> <http://transformunify.org/ontologies/TURBO_0000590> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010142> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> .
<http://transformunify.org/ontologies/TURBO_0010144_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010144> .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://transformunify.org/ontologies/TURBO_0000527_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <http://purl.obolibrary.org/obo/OBI_0000097_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<http://purl.obolibrary.org/obo/OBI_0000097_1> rdf:type <http://purl.obolibrary.org/obo/OBI_0000097> .
<http://transformunify.org/ontologies/TURBO_0000527_1> <http://purl.obolibrary.org/obo/BFO_0000055> <http://purl.obolibrary.org/obo/OBI_0000097_1> .
<http://transformunify.org/ontologies/TURBO_0000527_1> rdf:type <http://transformunify.org/ontologies/TURBO_0000527> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/LossOfFunctionExpansionProcess")
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
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://purl.obolibrary.org/obo/OBI_0000293","http://purl.obolibrary.org/obo/OBI_0000293",
"http://purl.obolibrary.org/obo/OBI_0000293","http://purl.obolibrary.org/obo/OBI_0000293",
"http://transformunify.org/ontologies/TURBO_0010113","http://transformunify.org/ontologies/TURBO_0010113",
"http://purl.obolibrary.org/obo/IAO_0000136","http://purl.obolibrary.org/obo/BFO_0000050",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
"http://transformunify.org/ontologies/TURBO_0010095","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
"http://purl.obolibrary.org/obo/BFO_0000051","http://transformunify.org/ontologies/TURBO_0010094",
"http://purl.obolibrary.org/obo/OBI_0000299","http://purl.obolibrary.org/obo/OBI_0000299",
"http://purl.obolibrary.org/obo/OBI_0000299","http://purl.obolibrary.org/obo/OBI_0000299",
"http://purl.obolibrary.org/obo/BFO_0000055","http://purl.obolibrary.org/obo/RO_0000087",
"http://purl.obolibrary.org/obo/IAO_0000142","http://transformunify.org/ontologies/TURBO_0010015",
"http://transformunify.org/ontologies/TURBO_0010016","http://purl.obolibrary.org/obo/OBI_0000643",
"http://purl.obolibrary.org/obo/OGG_0000000014","http://purl.obolibrary.org/obo/OBI_0001938"

)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }}