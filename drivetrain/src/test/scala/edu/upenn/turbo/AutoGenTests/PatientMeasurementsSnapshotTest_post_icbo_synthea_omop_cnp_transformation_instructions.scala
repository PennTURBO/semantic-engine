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
class PatientMeasurementsSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "735d708034d34a45bf63bbba7b169350"
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
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_datetime> "13/06/34"^^xsd:Date .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_source_concept_id> "914375085"^^xsd:Integer .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_id> "463207488"^^xsd:Integer .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#measurement_source_value> "755924451abc"^^xsd:String .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#value_as_number> "33737254abc"^^xsd:String .
<http://api.stardog.com/measurement_1> <http://api.stardog.com/measurement#vocabulary_id> "774008303abc"^^xsd:String .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/MeasClassList_1> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/IAO_0000109> .
<https://github.com/PennTURBO/Drivetrain/MeasClassList_1> <http://transformunify.org/ontologies/TURBO_0010147> "914375085"^^xsd:Integer .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/PatientMeasurements")
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
"""<http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/MeasClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/0b553e9283443b3f04268f67c87adf1e6d67604a10060e54eff998773d06847c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001933>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/46b999ec7c703234d574753dfc3421e5023fbdc023aa6b668ce78a174ff085cc> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/54f519c75465208af1b240ab4862c794c79b2286e985122a2031ab92a8d8dcfa> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://www.itmat.upenn.edu/biobank/7a114354f6577a035eee850be69103aba4b2a43b907236fd78ae26a306c3eead> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/57846d31cfade266ef994769233cab701b88b7461a718249163721cf0d88a30b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://www.itmat.upenn.edu/biobank/37710a046c38a32170098c5dcc6fee4528bc7f56cbc477573817ca947df92417> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/fd150d0bf19c5c12da5acc456e63d919e3d417420650d8ba631157931da9f74d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/measurement_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d>""",
"""<http://www.itmat.upenn.edu/biobank/0b553e9283443b3f04268f67c87adf1e6d67604a10060e54eff998773d06847c> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010411>""",
"""<http://www.itmat.upenn.edu/biobank/46b999ec7c703234d574753dfc3421e5023fbdc023aa6b668ce78a174ff085cc> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010482>""",
"""<http://www.itmat.upenn.edu/biobank/46b999ec7c703234d574753dfc3421e5023fbdc023aa6b668ce78a174ff085cc> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf>""",
"""<http://www.itmat.upenn.edu/biobank/54f519c75465208af1b240ab4862c794c79b2286e985122a2031ab92a8d8dcfa> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010484>""",
"""<http://www.itmat.upenn.edu/biobank/57846d31cfade266ef994769233cab701b88b7461a718249163721cf0d88a30b> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010488>""",
"""<http://www.itmat.upenn.edu/biobank/fd150d0bf19c5c12da5acc456e63d919e3d417420650d8ba631157931da9f74d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010487>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/0b553e9283443b3f04268f67c87adf1e6d67604a10060e54eff998773d06847c>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/54f519c75465208af1b240ab4862c794c79b2286e985122a2031ab92a8d8dcfa>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/57846d31cfade266ef994769233cab701b88b7461a718249163721cf0d88a30b>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/fd150d0bf19c5c12da5acc456e63d919e3d417420650d8ba631157931da9f74d>""",
"""<http://www.itmat.upenn.edu/biobank/d52bff814d282ed6253db1ee1b16da2d9bf138306b9f45efd11024b9cd3107cf> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010398>""",
"""<http://www.itmat.upenn.edu/biobank/46b999ec7c703234d574753dfc3421e5023fbdc023aa6b668ce78a174ff085cc> <http://transformunify.org/ontologies/TURBO_0010094> "463207488"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/57846d31cfade266ef994769233cab701b88b7461a718249163721cf0d88a30b> <http://transformunify.org/ontologies/TURBO_0010094> "914375085"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/fd150d0bf19c5c12da5acc456e63d919e3d417420650d8ba631157931da9f74d> <http://transformunify.org/ontologies/TURBO_0010094> "755924451abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/54f519c75465208af1b240ab4862c794c79b2286e985122a2031ab92a8d8dcfa> <http://purl.obolibrary.org/obo/IAO_0000004> "13/06/34"^^<http://www.w3.org/2001/XMLSchema#Date>""",
"""<http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d> <http://purl.obolibrary.org/obo/IAO_0000581> <http://www.itmat.upenn.edu/biobank/54f519c75465208af1b240ab4862c794c79b2286e985122a2031ab92a8d8dcfa>""",
"""<http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d> <http://purl.obolibrary.org/obo/IAO_0000142> <urn:uuid:63007293-b235-4721-b7b7-3756cccba903>""",
"""<http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d> <http://purl.obolibrary.org/obo/OBI_0001938> <http://www.itmat.upenn.edu/biobank/0b553e9283443b3f04268f67c87adf1e6d67604a10060e54eff998773d06847c>""",
"""<http://www.itmat.upenn.edu/biobank/0b553e9283443b3f04268f67c87adf1e6d67604a10060e54eff998773d06847c> <http://purl.obolibrary.org/obo/OBI_0002135> "33737254abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/7a114354f6577a035eee850be69103aba4b2a43b907236fd78ae26a306c3eead> <http://www.w3.org/2002/07/owl#annotatedSource> <http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d>""",
"""<http://www.itmat.upenn.edu/biobank/37710a046c38a32170098c5dcc6fee4528bc7f56cbc477573817ca947df92417> <http://www.w3.org/2002/07/owl#annotatedSource> <http://www.itmat.upenn.edu/biobank/653656a6342b2465dfd3a5d49c155d2a559c21a3d90112538d82573df265973d>""",
"""<http://www.itmat.upenn.edu/biobank/7a114354f6577a035eee850be69103aba4b2a43b907236fd78ae26a306c3eead> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>""",
"""<http://www.itmat.upenn.edu/biobank/37710a046c38a32170098c5dcc6fee4528bc7f56cbc477573817ca947df92417> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://purl.obolibrary.org/obo/IAO_0000142>""",
"""<http://www.itmat.upenn.edu/biobank/7a114354f6577a035eee850be69103aba4b2a43b907236fd78ae26a306c3eead> <http://www.w3.org/2002/07/owl#annotatedTarget> <https://github.com/PennTURBO/Drivetrain/MeasClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/37710a046c38a32170098c5dcc6fee4528bc7f56cbc477573817ca947df92417> <http://www.w3.org/2002/07/owl#annotatedTarget> <urn:uuid:63007293-b235-4721-b7b7-3756cccba903>""",
"""<http://www.itmat.upenn.edu/biobank/7a114354f6577a035eee850be69103aba4b2a43b907236fd78ae26a306c3eead> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/57846d31cfade266ef994769233cab701b88b7461a718249163721cf0d88a30b>""",
"""<http://www.itmat.upenn.edu/biobank/37710a046c38a32170098c5dcc6fee4528bc7f56cbc477573817ca947df92417> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/fd150d0bf19c5c12da5acc456e63d919e3d417420650d8ba631157931da9f74d>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}