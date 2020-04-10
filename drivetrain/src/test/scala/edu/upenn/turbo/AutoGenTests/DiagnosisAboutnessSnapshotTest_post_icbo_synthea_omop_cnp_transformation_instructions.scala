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
class DiagnosisAboutnessSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "562783805f5b4da38876b9abfbfa06d7"
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
<http://api.stardog.com/condition_occurrence_2> <http://api.stardog.com/condition_occurrence#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/condition_occurrence_1> <http://api.stardog.com/condition_occurrence#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/condition_occurrence_1> rdf:type <http://api.stardog.com/condition_occurrence> .
<http://api.stardog.com/condition_occurrence_2> rdf:type <http://api.stardog.com/condition_occurrence> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://transformunify.org/ontologies/TURBO_0010433_1_DiagDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/OGMS_0000073_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> .
<http://transformunify.org/ontologies/TURBO_0010433_2_DiagDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/condition_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/OGMS_0000073_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> rdf:type <http://purl.obolibrary.org/obo/IAO_0000028> .
<http://transformunify.org/ontologies/TURBO_0010433_2_DiagDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/OGMS_0000073_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<http://api.stardog.com/condition_occurrence_1> rdf:type <http://api.stardog.com/condition_occurrence> .
<http://purl.obolibrary.org/obo/OGMS_0000073_1> rdf:type <http://purl.obolibrary.org/obo/OGMS_0000073> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/condition_occurrence_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/OGMS_0000073_1> .
<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> .
<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010433_1_DiagDenotationContext> rdf:type <http://transformunify.org/ontologies/TURBO_0010433> .
<http://api.stardog.com/condition_occurrence_2> rdf:type <http://api.stardog.com/condition_occurrence> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/DiagnosisAboutness")
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
"""<http://api.stardog.com/condition_occurrence_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/condition_occurrence>""",
"""<http://purl.obolibrary.org/obo/OGMS_0000073_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://api.stardog.com/condition_occurrence_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/condition_occurrence>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_DiagDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_DiagDenotationContext> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/2ac8262b70b572ba12c502bdaf17334acbb3004a857ca2716a6ebb7ca4d30792> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/ef375ba6aa498339d1049417e61d567890dbf99d7b030c24eb6655af8ce148bf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/condition_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/OGMS_0000073_1>""",
"""<http://api.stardog.com/condition_occurrence_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/OGMS_0000073_1>""",
"""<http://purl.obolibrary.org/obo/OGMS_0000073_1> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext>""",
"""<http://www.itmat.upenn.edu/biobank/ef375ba6aa498339d1049417e61d567890dbf99d7b030c24eb6655af8ce148bf> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010701>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_DiagDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/OGMS_0000073_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_DiagDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/ef375ba6aa498339d1049417e61d567890dbf99d7b030c24eb6655af8ce148bf>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_DiagDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://purl.obolibrary.org/obo/OGMS_0000073_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_DiagDenotationContext> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/ef375ba6aa498339d1049417e61d567890dbf99d7b030c24eb6655af8ce148bf>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_1_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://transformunify.org/ontologies/TURBO_0010433_2_KeyContext> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_1_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://purl.obolibrary.org/obo/IAO_0000028_2_KeyContext> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/ef375ba6aa498339d1049417e61d567890dbf99d7b030c24eb6655af8ce148bf> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/2ac8262b70b572ba12c502bdaf17334acbb3004a857ca2716a6ebb7ca4d30792> <http://www.w3.org/2002/07/owl#annotatedSource> <http://purl.obolibrary.org/obo/OGMS_0000073_1>""",
"""<http://www.itmat.upenn.edu/biobank/2ac8262b70b572ba12c502bdaf17334acbb3004a857ca2716a6ebb7ca4d30792> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/IAO_0000136>""",
"""<http://www.itmat.upenn.edu/biobank/2ac8262b70b572ba12c502bdaf17334acbb3004a857ca2716a6ebb7ca4d30792> <http://www.w3.org/2002/07/owl#annotatedTarget> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/2ac8262b70b572ba12c502bdaf17334acbb3004a857ca2716a6ebb7ca4d30792> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/ef375ba6aa498339d1049417e61d567890dbf99d7b030c24eb6655af8ce148bf>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}