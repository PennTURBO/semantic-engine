package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
class EncounterExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
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
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2SymbVal> "316978036abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RoleType> <http://purl.obolibrary.org/obo/OBI_0000097> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RegDen> <http://transformunify.org/ontologies/TURBO_0000535> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2ScHs> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2EncType> <http://transformunify.org/ontologies/TURBO_0000527> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <https://github.com/PennTURBO/Drivetrain/scMeasHasUnitLabel> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <https://github.com/PennTURBO/Drivetrain/scMeasHasType> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <https://github.com/PennTURBO/Drivetrain/scMeasHasValue> "33737254.00"^^xsd:Double .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2DateXsd> "2020-04-02T13:49:48.665"^^xsd:Date .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2ScMeas> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RawDate> "241702925abc"^^xsd:String .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://transformunify.org/ontologies/TURBO_0010113",
"http://transformunify.org/ontologies/TURBO_0010113","http://transformunify.org/ontologies/TURBO_0010113",
"http://purl.obolibrary.org/obo/IAO_0000136","http://purl.obolibrary.org/obo/IAO_0000136",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/IAO_0000219","http://purl.obolibrary.org/obo/BFO_0000051",
"http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/RO_0002223",
"http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
"http://purl.obolibrary.org/obo/OBI_0000299","http://purl.obolibrary.org/obo/IAO_0000039",
"http://purl.obolibrary.org/obo/IAO_0000004","http://purl.obolibrary.org/obo/BFO_0000055",
"http://purl.obolibrary.org/obo/RO_0000087","http://purl.obolibrary.org/obo/OBI_0001938",
"http://purl.obolibrary.org/obo/OBI_0002135"
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
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2SymbVal> "316978036abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RoleType> <http://purl.obolibrary.org/obo/OBI_0000097> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RegDen> <http://transformunify.org/ontologies/TURBO_0000535> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2ScHs> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2EncType> <http://transformunify.org/ontologies/TURBO_0000527> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess")
val count: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")

val checkPredicates = Array(
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
"http://transformunify.org/ontologies/TURBO_0010113","http://transformunify.org/ontologies/TURBO_0010113",
"http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/IAO_0000219",
"http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
"http://transformunify.org/ontologies/TURBO_0010094","http://purl.obolibrary.org/obo/BFO_0000055",
"http://purl.obolibrary.org/obo/RO_0000087"
)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 }}