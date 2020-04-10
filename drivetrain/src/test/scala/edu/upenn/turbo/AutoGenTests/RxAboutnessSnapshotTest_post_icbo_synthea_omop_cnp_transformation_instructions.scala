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
class RxAboutnessSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "10063a720dd8463698f5776519453030"
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
<http://api.stardog.com/drug_exposure_2> rdf:type <http://api.stardog.com/drug_exposure> .
<http://api.stardog.com/drug_exposure_1> rdf:type <http://api.stardog.com/drug_exposure> .
<http://api.stardog.com/drug_exposure_1> <http://api.stardog.com/drug_exposure#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_2> <http://api.stardog.com/drug_exposure#person_id> "2144189648abc"^^xsd:String .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://purl.obolibrary.org/obo/PDRO_0000024_1> rdf:type <http://purl.obolibrary.org/obo/PDRO_0000024> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> .
<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/PDRO_0000024_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/drug_exposure_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/PDRO_0000024_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/drug_exposure_2> rdf:type <http://api.stardog.com/drug_exposure> .
<http://api.stardog.com/drug_exposure_1> rdf:type <http://api.stardog.com/drug_exposure> .
<http://transformunify.org/ontologies/TURBO_0010433_2_RxDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://api.stardog.com/drug_exposure_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/PDRO_0000024_1> .
<http://transformunify.org/ontologies/TURBO_0010433_2_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/PDRO_0000024_1> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/RxAboutness")
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
"""<http://api.stardog.com/drug_exposure_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/drug_exposure>""",
"""<http://purl.obolibrary.org/obo/PDRO_0000024_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://api.stardog.com/drug_exposure_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/drug_exposure>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_RxDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/49884ebcb8a4ab576ffd35e0516a0620ab79978f71924af74f209036fa5e5778> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/3440c39f62bf2d18f367a79a5cf148d4b3ddc6c348fc63dc4952061ef2077a8e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/drug_exposure_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://api.stardog.com/drug_exposure_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://purl.obolibrary.org/obo/PDRO_0000024_1> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext>""",
"""<http://www.itmat.upenn.edu/biobank/3440c39f62bf2d18f367a79a5cf148d4b3ddc6c348fc63dc4952061ef2077a8e> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010702>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/3440c39f62bf2d18f367a79a5cf148d4b3ddc6c348fc63dc4952061ef2077a8e>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_RxDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/3440c39f62bf2d18f367a79a5cf148d4b3ddc6c348fc63dc4952061ef2077a8e>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/3440c39f62bf2d18f367a79a5cf148d4b3ddc6c348fc63dc4952061ef2077a8e> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/49884ebcb8a4ab576ffd35e0516a0620ab79978f71924af74f209036fa5e5778> <http://www.w3.org/2002/07/owl#annotatedSource> <http://purl.obolibrary.org/obo/PDRO_0000024_1>""",
"""<http://www.itmat.upenn.edu/biobank/49884ebcb8a4ab576ffd35e0516a0620ab79978f71924af74f209036fa5e5778> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/IAO_0000136>""",
"""<http://www.itmat.upenn.edu/biobank/49884ebcb8a4ab576ffd35e0516a0620ab79978f71924af74f209036fa5e5778> <http://www.w3.org/2002/07/owl#annotatedTarget> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/49884ebcb8a4ab576ffd35e0516a0620ab79978f71924af74f209036fa5e5778> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/3440c39f62bf2d18f367a79a5cf148d4b3ddc6c348fc63dc4952061ef2077a8e>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}