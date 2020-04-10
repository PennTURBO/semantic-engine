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
class PatientMulSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "53bad0b055cd455586011ab5b6af6f38"
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
<http://api.stardog.com/measurement_1> rdf:type <http://api.stardog.com/measurement> .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#more_units> "2093775254abc"^^xsd:String .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/MulClassList_1> <http://transformunify.org/ontologies/TURBO_0010147> "2093775254abc"^^xsd:String .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://api.stardog.com/measurement_1> rdf:type <http://api.stardog.com/measurement> .
<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/measurement_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/PatientMul")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://api.stardog.com/measurement_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/measurement>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/4dab1b32dae08fbefc61c6116165434d87b5afc66fd06269da50f4bb1c3cf627> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/0479a48b5838ff0c56ba112d60f4b87c4eefbcc0ffb86e3c0a40fd21c73f50ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/measurement_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/0479a48b5838ff0c56ba112d60f4b87c4eefbcc0ffb86e3c0a40fd21c73f50ad> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010423>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/0479a48b5838ff0c56ba112d60f4b87c4eefbcc0ffb86e3c0a40fd21c73f50ad>""",
"""<http://www.itmat.upenn.edu/biobank/0479a48b5838ff0c56ba112d60f4b87c4eefbcc0ffb86e3c0a40fd21c73f50ad> <http://transformunify.org/ontologies/TURBO_0010094> "2093775254abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> <http://purl.obolibrary.org/obo/IAO_0000039> <https://github.com/PennTURBO/Drivetrain/MulClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/4dab1b32dae08fbefc61c6116165434d87b5afc66fd06269da50f4bb1c3cf627> <http://www.w3.org/2002/07/owl#annotatedSource> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/4dab1b32dae08fbefc61c6116165434d87b5afc66fd06269da50f4bb1c3cf627> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/IAO_0000039>""",
"""<http://www.itmat.upenn.edu/biobank/4dab1b32dae08fbefc61c6116165434d87b5afc66fd06269da50f4bb1c3cf627> <http://www.w3.org/2002/07/owl#annotatedTarget> <https://github.com/PennTURBO/Drivetrain/MulClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/4dab1b32dae08fbefc61c6116165434d87b5afc66fd06269da50f4bb1c3cf627> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/0479a48b5838ff0c56ba112d60f4b87c4eefbcc0ffb86e3c0a40fd21c73f50ad>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}