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
class DiagnosisExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "carnival_instructionSet.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "f7ef379f6dd049cb81bac215ed484ffc"
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
<http://transformunify.org/ontologies/TURBO_0010160_3> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_4> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_3> .
<http://transformunify.org/ontologies/TURBO_0010160_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_2> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_4> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_2> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<http://transformunify.org/ontologies/TURBO_0010160_4> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_1> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_3> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^xsd:Boolean .
<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^xsd:Boolean .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004601> "1058648386abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0004603> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489> .
<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010160_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0004601> "1058648386abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0004601> "1058648386abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0004601> "1058648386abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^xsd:Boolean .
<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0004603> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489> .
<http://transformunify.org/ontologies/TURBO_0010160_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_4> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^xsd:Boolean .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004603> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0004603> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489> .
<http://transformunify.org/ontologies/TURBO_0010160_3> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/3f08f791dbb4451988dd0a43755ef9e6684463c93a19d7aa2be7093cbbab7caf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/d44c6306b6839d23585d5b3b851fc1722da42d939aac4db5f67338d5315dffaf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/82a2b4ff570370686b6dee8f071a4393a2c22dec279cee609c982f5ac42c1600> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/52d49d77b5844a330d7f2477ca38d5f244649536b4aa476c61ffe796a674eec1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/3f08f791dbb4451988dd0a43755ef9e6684463c93a19d7aa2be7093cbbab7caf> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49>""",
"""<http://www.itmat.upenn.edu/biobank/d44c6306b6839d23585d5b3b851fc1722da42d939aac4db5f67338d5315dffaf> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee>""",
"""<http://www.itmat.upenn.edu/biobank/82a2b4ff570370686b6dee8f071a4393a2c22dec279cee609c982f5ac42c1600> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176>""",
"""<http://www.itmat.upenn.edu/biobank/52d49d77b5844a330d7f2477ca38d5f244649536b4aa476c61ffe796a674eec1> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7>""",
"""<http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549>""",
"""<http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858>""",
"""<http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000>""",
"""<http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b>""",
"""<http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49> <http://purl.obolibrary.org/obo/BFO_0000051> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489>""",
"""<http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee> <http://purl.obolibrary.org/obo/BFO_0000051> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489>""",
"""<http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176> <http://purl.obolibrary.org/obo/BFO_0000051> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489>""",
"""<http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7> <http://purl.obolibrary.org/obo/BFO_0000051> <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://transformunify.org/ontologies/TURBO_0010094> "1058648386abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://transformunify.org/ontologies/TURBO_0010094> "1058648386abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://transformunify.org/ontologies/TURBO_0010094> "1058648386abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://transformunify.org/ontologies/TURBO_0010094> "1058648386abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://transformunify.org/ontologies/TURBO_0010013> "true"^^<http://www.w3.org/2001/XMLSchema#Boolean>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://transformunify.org/ontologies/TURBO_0010014> "1441169530"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }
test("minimum fields test")
{

val insertInputDataset = 
"""
            INSERT DATA {
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010160_3> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_4> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_3> .
<http://transformunify.org/ontologies/TURBO_0010160_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0004602> "2025287662abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_2> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<http://transformunify.org/ontologies/TURBO_0010160_4> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_2> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<http://transformunify.org/ontologies/TURBO_0010160_4> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010160_1> .
<http://transformunify.org/ontologies/TURBO_0010160_1> <https://github.com/PennTURBO/Drivetrain/scDiag2PrimaryKey> "1816606446abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010160_3> rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> .
<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010160>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/3f08f791dbb4451988dd0a43755ef9e6684463c93a19d7aa2be7093cbbab7caf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/d44c6306b6839d23585d5b3b851fc1722da42d939aac4db5f67338d5315dffaf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/82a2b4ff570370686b6dee8f071a4393a2c22dec279cee609c982f5ac42c1600> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000073>""",
"""<http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/52d49d77b5844a330d7f2477ca38d5f244649536b4aa476c61ffe796a674eec1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_3> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000>""",
"""<http://transformunify.org/ontologies/TURBO_0010160_4> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/3f08f791dbb4451988dd0a43755ef9e6684463c93a19d7aa2be7093cbbab7caf> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49>""",
"""<http://www.itmat.upenn.edu/biobank/d44c6306b6839d23585d5b3b851fc1722da42d939aac4db5f67338d5315dffaf> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee>""",
"""<http://www.itmat.upenn.edu/biobank/82a2b4ff570370686b6dee8f071a4393a2c22dec279cee609c982f5ac42c1600> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176>""",
"""<http://www.itmat.upenn.edu/biobank/52d49d77b5844a330d7f2477ca38d5f244649536b4aa476c61ffe796a674eec1> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7>""",
"""<http://www.itmat.upenn.edu/biobank/acea36b5e921ddc0f8c6a91f6c87e3a7d3563e2bb548ddf478eb6763d99f9a49> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549>""",
"""<http://www.itmat.upenn.edu/biobank/c20f481551f14fb0c0df9641b1bf213fabb5d2313cab493d3e7e1a5f481930ee> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858>""",
"""<http://www.itmat.upenn.edu/biobank/f19eac8d3b1cd55e51c99695874bb6dc1d2246007e9b0ff76bafe5515f0ab176> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000>""",
"""<http://www.itmat.upenn.edu/biobank/71db34f3b471452d84cf9b6fc752b9133b3299ddd4a310a19bb7f067d9dcd2f7> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/b279b639767da50007c719a15ed87289a50f9706f929793843305edb20793549> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/3e9bf82349387757050593eb8903730ce372e3a7f7dd99c830e229aed5fb0858> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/1a922079e4f94f2567486a0340377de14555f90fac73bfa4f628ddad4cf82000> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/cab35e6d04cef8e617ac08174ab17888bddaf667d9a1a61a23d4d1c0742e810b> <http://transformunify.org/ontologies/TURBO_0006515> "2025287662abc"^^<http://www.w3.org/2001/XMLSchema#String>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}