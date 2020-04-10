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
class PatientMeasAboutnessSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "a52035149535422aa5ab33428d3dadac"
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
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/measurement_2> rdf:type <http://api.stardog.com/measurement> .
<http://api.stardog.com/measurement_2> <http://api.stardog.com/measurement#person_id> "2144189648abc"^^xsd:String .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://api.stardog.com/measurement_1> rdf:type <http://api.stardog.com/measurement> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://api.stardog.com/measurement_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/person_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://api.stardog.com/measurement_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<http://api.stardog.com/measurement_2> rdf:type <http://api.stardog.com/measurement> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010433_2_MeasDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/person_1> rdf:type <http://api.stardog.com/person> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> .
<http://transformunify.org/ontologies/TURBO_0010433_2_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/PatientMeasAboutness")
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
"""<http://api.stardog.com/person_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/person>""",
"""<http://api.stardog.com/measurement_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/measurement>""",
"""<https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://api.stardog.com/measurement_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/measurement>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_MeasDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/1c1a03bb41d7454ff8a5ec43c127fbd6a3f422af445df6a0d9455a30e083c5fc> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/768ca69d4c16ab3e830f3b6dfe28b808e528d1e32fee01ba7fd3cdb1586abcc9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://www.itmat.upenn.edu/biobank/844060b5dba2573cda0260dd0b61aa588820d9c01e516fc6600d8606243aa439> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/3f0e8ff1c14a4a45f6b060d96ffc5c1f7d46fec33d4461edd5f54ceee65e2577> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/person_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://api.stardog.com/measurement_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://api.stardog.com/measurement_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext>""",
"""<http://www.itmat.upenn.edu/biobank/768ca69d4c16ab3e830f3b6dfe28b808e528d1e32fee01ba7fd3cdb1586abcc9> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010483>""",
"""<http://www.itmat.upenn.edu/biobank/3f0e8ff1c14a4a45f6b060d96ffc5c1f7d46fec33d4461edd5f54ceee65e2577> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010483>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/768ca69d4c16ab3e830f3b6dfe28b808e528d1e32fee01ba7fd3cdb1586abcc9>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_MeasDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/3f0e8ff1c14a4a45f6b060d96ffc5c1f7d46fec33d4461edd5f54ceee65e2577>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/768ca69d4c16ab3e830f3b6dfe28b808e528d1e32fee01ba7fd3cdb1586abcc9> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/3f0e8ff1c14a4a45f6b060d96ffc5c1f7d46fec33d4461edd5f54ceee65e2577> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/1c1a03bb41d7454ff8a5ec43c127fbd6a3f422af445df6a0d9455a30e083c5fc> <http://www.w3.org/2002/07/owl#annotatedSource> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/844060b5dba2573cda0260dd0b61aa588820d9c01e516fc6600d8606243aa439> <http://www.w3.org/2002/07/owl#annotatedSource> <https://github.com/PennTURBO/Drivetrain/MeasToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/1c1a03bb41d7454ff8a5ec43c127fbd6a3f422af445df6a0d9455a30e083c5fc> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/IAO_0000136>""",
"""<http://www.itmat.upenn.edu/biobank/844060b5dba2573cda0260dd0b61aa588820d9c01e516fc6600d8606243aa439> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/IAO_0000136>""",
"""<http://www.itmat.upenn.edu/biobank/1c1a03bb41d7454ff8a5ec43c127fbd6a3f422af445df6a0d9455a30e083c5fc> <http://www.w3.org/2002/07/owl#annotatedTarget> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/844060b5dba2573cda0260dd0b61aa588820d9c01e516fc6600d8606243aa439> <http://www.w3.org/2002/07/owl#annotatedTarget> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/1c1a03bb41d7454ff8a5ec43c127fbd6a3f422af445df6a0d9455a30e083c5fc> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/768ca69d4c16ab3e830f3b6dfe28b808e528d1e32fee01ba7fd3cdb1586abcc9>""",
"""<http://www.itmat.upenn.edu/biobank/844060b5dba2573cda0260dd0b61aa588820d9c01e516fc6600d8606243aa439> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/3f0e8ff1c14a4a45f6b060d96ffc5c1f7d46fec33d4461edd5f54ceee65e2577>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}