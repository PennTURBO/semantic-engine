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
class HealthCareEncountersSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "bfec814e7d6f47139ee8f09a9fc754ae"
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
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#visit_source_value> "728182065abc"^^xsd:String .
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#visit_end_datetime> "1488614749abc"^^xsd:String .
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#visit_start_datetime> "15/07/70"^^xsd:Date .
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#visit_occurrence_id> "316978036abc"^^xsd:String .
<http://api.stardog.com/visit_occurrence_1> <http://api.stardog.com/visit_occurrence#visit_concept_id> "1889010940abc"^^xsd:String .
<http://api.stardog.com/visit_occurrence_1> rdf:type <http://api.stardog.com/visit_occurrence> .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<https://github.com/PennTURBO/Drivetrain/EncClassList_1> <http://transformunify.org/ontologies/TURBO_0010147> "1889010940abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/EncClassList_1> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/OGMS_0000097> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/HealthCareEncounters")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://api.stardog.com/visit_occurrence_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/visit_occurrence>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/eb0f60788a303dc590a0a1087e045c47fb42fc3e2c07f90355438995300362c9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/EncClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/56988c5315502f581c5748c655eb3ff8d6d8dcaa8a8ea14b9fd957d8ca18de46> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/f22b71b2373f4e61d9c0b3aaf0bb7e931e78f5a60ff3addb101ebf4ee987284f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000035>""",
"""<http://www.itmat.upenn.edu/biobank/d9b0694af8d92f5c19e8da85777b2286c9ef08c0811b71f6c2b31ea6628b3c2d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/ab6ad4be5a66bb8c0f5ea86ec0812886e91381c33494dd088146456b68739591> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://www.itmat.upenn.edu/biobank/ee701355476451c2ca38fdd5ce77ab44ce46ba995baa2f3c56d3aed19af05193> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/749f6c5f39c91871370bd0671de12c0233e6b38c40b50de4b7900301fa1bef0e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000035>""",
"""<http://www.itmat.upenn.edu/biobank/9a8b90c1fe7fb3a114097fadd0b6c06a15c1f74efa73b346f542eba3fd038497> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Axiom>""",
"""<http://www.itmat.upenn.edu/biobank/e22add40f41cd00227427506763e7a27d79ae51598f92fa09a17dfbb1a8d5e33> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010404>""",
"""<http://api.stardog.com/visit_occurrence_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a>""",
"""<http://www.itmat.upenn.edu/biobank/eb0f60788a303dc590a0a1087e045c47fb42fc3e2c07f90355438995300362c9> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/f22b71b2373f4e61d9c0b3aaf0bb7e931e78f5a60ff3addb101ebf4ee987284f>""",
"""<http://www.itmat.upenn.edu/biobank/ab6ad4be5a66bb8c0f5ea86ec0812886e91381c33494dd088146456b68739591> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/749f6c5f39c91871370bd0671de12c0233e6b38c40b50de4b7900301fa1bef0e>""",
"""<http://www.itmat.upenn.edu/biobank/eb0f60788a303dc590a0a1087e045c47fb42fc3e2c07f90355438995300362c9> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010457>""",
"""<http://www.itmat.upenn.edu/biobank/56988c5315502f581c5748c655eb3ff8d6d8dcaa8a8ea14b9fd957d8ca18de46> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010453>""",
"""<http://www.itmat.upenn.edu/biobank/56988c5315502f581c5748c655eb3ff8d6d8dcaa8a8ea14b9fd957d8ca18de46> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f>""",
"""<http://www.itmat.upenn.edu/biobank/d9b0694af8d92f5c19e8da85777b2286c9ef08c0811b71f6c2b31ea6628b3c2d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010455>""",
"""<http://www.itmat.upenn.edu/biobank/d9b0694af8d92f5c19e8da85777b2286c9ef08c0811b71f6c2b31ea6628b3c2d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/ee701355476451c2ca38fdd5ce77ab44ce46ba995baa2f3c56d3aed19af05193>""",
"""<http://www.itmat.upenn.edu/biobank/ab6ad4be5a66bb8c0f5ea86ec0812886e91381c33494dd088146456b68739591> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010451>""",
"""<http://www.itmat.upenn.edu/biobank/e22add40f41cd00227427506763e7a27d79ae51598f92fa09a17dfbb1a8d5e33> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010449>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/eb0f60788a303dc590a0a1087e045c47fb42fc3e2c07f90355438995300362c9>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/d9b0694af8d92f5c19e8da85777b2286c9ef08c0811b71f6c2b31ea6628b3c2d>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/ab6ad4be5a66bb8c0f5ea86ec0812886e91381c33494dd088146456b68739591>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/e22add40f41cd00227427506763e7a27d79ae51598f92fa09a17dfbb1a8d5e33>""",
"""<http://www.itmat.upenn.edu/biobank/ee701355476451c2ca38fdd5ce77ab44ce46ba995baa2f3c56d3aed19af05193> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a>""",
"""<http://www.itmat.upenn.edu/biobank/276a6689459ba7e727ce648c65db3e936b58a6f8a277d3cd9ad6e9567592984f> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010256>""",
"""<http://www.itmat.upenn.edu/biobank/f22b71b2373f4e61d9c0b3aaf0bb7e931e78f5a60ff3addb101ebf4ee987284f> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a>""",
"""<http://www.itmat.upenn.edu/biobank/56988c5315502f581c5748c655eb3ff8d6d8dcaa8a8ea14b9fd957d8ca18de46> <http://transformunify.org/ontologies/TURBO_0010094> "316978036abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/d9b0694af8d92f5c19e8da85777b2286c9ef08c0811b71f6c2b31ea6628b3c2d> <http://transformunify.org/ontologies/TURBO_0010094> "728182065abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/e22add40f41cd00227427506763e7a27d79ae51598f92fa09a17dfbb1a8d5e33> <http://transformunify.org/ontologies/TURBO_0010094> "1889010940abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/eb0f60788a303dc590a0a1087e045c47fb42fc3e2c07f90355438995300362c9> <http://purl.obolibrary.org/obo/IAO_0000004> "15/07/70"^^<http://www.w3.org/2001/XMLSchema#Date>""",
"""<http://www.itmat.upenn.edu/biobank/ab6ad4be5a66bb8c0f5ea86ec0812886e91381c33494dd088146456b68739591> <http://purl.obolibrary.org/obo/IAO_0000004> "1488614749abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/749f6c5f39c91871370bd0671de12c0233e6b38c40b50de4b7900301fa1bef0e> <http://purl.obolibrary.org/obo/RO_0002229> <http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a>""",
"""<http://www.itmat.upenn.edu/biobank/9a8b90c1fe7fb3a114097fadd0b6c06a15c1f74efa73b346f542eba3fd038497> <http://www.w3.org/2002/07/owl#annotatedSource> <http://www.itmat.upenn.edu/biobank/ebe0bcb7a32a468f7145e3ac7f33f1cb67e06ad7fcd6db03b0c8df832cc8cf1a>""",
"""<http://www.itmat.upenn.edu/biobank/9a8b90c1fe7fb3a114097fadd0b6c06a15c1f74efa73b346f542eba3fd038497> <http://www.w3.org/2002/07/owl#annotatedProperty> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>""",
"""<http://www.itmat.upenn.edu/biobank/9a8b90c1fe7fb3a114097fadd0b6c06a15c1f74efa73b346f542eba3fd038497> <http://www.w3.org/2002/07/owl#annotatedTarget> <https://github.com/PennTURBO/Drivetrain/EncClassList_1>""",
"""<http://www.itmat.upenn.edu/biobank/9a8b90c1fe7fb3a114097fadd0b6c06a15c1f74efa73b346f542eba3fd038497> <http://www.geneontology.org/formats/oboInOwl#hasDbXref> <http://www.itmat.upenn.edu/biobank/e22add40f41cd00227427506763e7a27d79ae51598f92fa09a17dfbb1a8d5e33>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}