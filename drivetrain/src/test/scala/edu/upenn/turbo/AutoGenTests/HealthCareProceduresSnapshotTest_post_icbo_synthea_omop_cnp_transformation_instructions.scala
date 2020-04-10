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
class HealthCareProceduresSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "7b93a6b0ff2449bf9bb9627ee21d99eb"
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
<http://api.stardog.com/procedure_occurrence_1> <http://api.stardog.com/procedure_occurrence#procedure_source_concept_id> "937675002abc"^^xsd:String .
<http://api.stardog.com/procedure_occurrence_1> <http://api.stardog.com/procedure_occurrence#procedure_occurrence_id> "95590532abc"^^xsd:String .
<http://api.stardog.com/procedure_occurrence_1> rdf:type <http://api.stardog.com/procedure_occurrence> .
<http://api.stardog.com/procedure_occurrence_1> <http://api.stardog.com/procedure_occurrence#procedure_source_value> "374691729abc"^^xsd:String .
<http://api.stardog.com/procedure_occurrence_1> <http://api.stardog.com/procedure_occurrence#procedure_datetime> "1990395943abc"^^xsd:String .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/ProcClassList_1> <http://transformunify.org/ontologies/TURBO_0010147> "937675002abc"^^xsd:String .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/HealthCareProcedures")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://api.stardog.com/procedure_occurrence_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/procedure_occurrence>""",
"""<http://www.itmat.upenn.edu/biobank/cdb47d9801186a358bb9fef58dd2b9e1f2978db63aa60c8028c4802d1e4c3b28> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://www.itmat.upenn.edu/biobank/83585470a98adb6ff656a594c6e1fb2bc2d380348aaf6696676ef68c3453a20d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000035>""",
"""<http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/23c0fbe2a0d41a6f7c27ef04aa48404b969681c9ffc979ba24946a9ef2acf379> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/ProcClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/1b8ee17d4edf088595a02438f4aa13e02320e033dbf6a5278a1fac61a551ca42> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/b2711423838610b13478e56cbc2494de3f69801a926e13d8c1eb47a941396e98> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://www.itmat.upenn.edu/biobank/421e0d5fbadf65e065dd73710303a35e2a06808feb5d7b5497b7ed7819b603dd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://www.itmat.upenn.edu/biobank/62955c1f579e9b388a597933fb626461f795b6517bee77eee0f945b1a811c26a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://api.stardog.com/procedure_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/23c0fbe2a0d41a6f7c27ef04aa48404b969681c9ffc979ba24946a9ef2acf379>""",
"""<http://www.itmat.upenn.edu/biobank/cdb47d9801186a358bb9fef58dd2b9e1f2978db63aa60c8028c4802d1e4c3b28> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/83585470a98adb6ff656a594c6e1fb2bc2d380348aaf6696676ef68c3453a20d>""",
"""<http://www.itmat.upenn.edu/biobank/b2711423838610b13478e56cbc2494de3f69801a926e13d8c1eb47a941396e98> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/23c0fbe2a0d41a6f7c27ef04aa48404b969681c9ffc979ba24946a9ef2acf379>""",
"""<http://www.itmat.upenn.edu/biobank/cdb47d9801186a358bb9fef58dd2b9e1f2978db63aa60c8028c4802d1e4c3b28> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010472>""",
"""<http://www.itmat.upenn.edu/biobank/1b8ee17d4edf088595a02438f4aa13e02320e033dbf6a5278a1fac61a551ca42> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010465>""",
"""<http://www.itmat.upenn.edu/biobank/1b8ee17d4edf088595a02438f4aa13e02320e033dbf6a5278a1fac61a551ca42> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d>""",
"""<http://www.itmat.upenn.edu/biobank/b2711423838610b13478e56cbc2494de3f69801a926e13d8c1eb47a941396e98> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010470>""",
"""<http://www.itmat.upenn.edu/biobank/421e0d5fbadf65e065dd73710303a35e2a06808feb5d7b5497b7ed7819b603dd> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010469>""",
"""<http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/cdb47d9801186a358bb9fef58dd2b9e1f2978db63aa60c8028c4802d1e4c3b28>""",
"""<http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/23c0fbe2a0d41a6f7c27ef04aa48404b969681c9ffc979ba24946a9ef2acf379>""",
"""<http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/b2711423838610b13478e56cbc2494de3f69801a926e13d8c1eb47a941396e98>""",
"""<http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/421e0d5fbadf65e065dd73710303a35e2a06808feb5d7b5497b7ed7819b603dd>""",
"""<http://www.itmat.upenn.edu/biobank/0ae4e8ac30ff9bbc948c2b88484b1b5dc1172015b504d50caa42830d463d661d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010407>""",
"""<http://www.itmat.upenn.edu/biobank/83585470a98adb6ff656a594c6e1fb2bc2d380348aaf6696676ef68c3453a20d> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/23c0fbe2a0d41a6f7c27ef04aa48404b969681c9ffc979ba24946a9ef2acf379>""",
"""<http://www.itmat.upenn.edu/biobank/1b8ee17d4edf088595a02438f4aa13e02320e033dbf6a5278a1fac61a551ca42> <http://transformunify.org/ontologies/TURBO_0010094> "95590532abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b2711423838610b13478e56cbc2494de3f69801a926e13d8c1eb47a941396e98> <http://transformunify.org/ontologies/TURBO_0010094> "374691729abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/421e0d5fbadf65e065dd73710303a35e2a06808feb5d7b5497b7ed7819b603dd> <http://transformunify.org/ontologies/TURBO_0010094> "937675002abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/cdb47d9801186a358bb9fef58dd2b9e1f2978db63aa60c8028c4802d1e4c3b28> <http://purl.obolibrary.org/obo/IAO_0000004> "1990395943abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/62955c1f579e9b388a597933fb626461f795b6517bee77eee0f945b1a811c26a> <http://www.w3.org/2002/07/owl#annotatedSource> <http://www.itmat.upenn.edu/biobank/23c0fbe2a0d41a6f7c27ef04aa48404b969681c9ffc979ba24946a9ef2acf379>""",
"""<http://www.itmat.upenn.edu/biobank/62955c1f579e9b388a597933fb626461f795b6517bee77eee0f945b1a811c26a> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>""",
"""<http://www.itmat.upenn.edu/biobank/62955c1f579e9b388a597933fb626461f795b6517bee77eee0f945b1a811c26a> <http://www.w3.org/2002/07/owl#annotatedTarget> <https://github.com/PennTURBO/Drivetrain/ProcClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/62955c1f579e9b388a597933fb626461f795b6517bee77eee0f945b1a811c26a> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/421e0d5fbadf65e065dd73710303a35e2a06808feb5d7b5497b7ed7819b603dd>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}