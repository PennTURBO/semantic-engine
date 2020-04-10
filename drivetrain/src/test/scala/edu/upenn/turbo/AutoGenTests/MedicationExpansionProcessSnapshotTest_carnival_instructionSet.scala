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
class MedicationExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "carnival_instructionSet.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "a042d3b2646742049f815550f76b2d3a"
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
<http://transformunify.org/ontologies/TURBO_0010159_3> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_4> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_3> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_2> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_1> .
<http://transformunify.org/ontologies/TURBO_0010159_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_4> .
<http://transformunify.org/ontologies/TURBO_0010159_1> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_3> .
<http://transformunify.org/ontologies/TURBO_0010159_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<http://transformunify.org/ontologies/TURBO_0010159_2> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_4> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
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
<http://transformunify.org/ontologies/TURBO_0010159_4> <http://transformunify.org/ontologies/TURBO_0005612> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010159_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<http://transformunify.org/ontologies/TURBO_0010159_3> <http://transformunify.org/ontologies/TURBO_0005611> "808753002abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_2> <http://transformunify.org/ontologies/TURBO_0005611> "808753002abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_1> <http://transformunify.org/ontologies/TURBO_0005611> "808753002abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_3> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<http://transformunify.org/ontologies/TURBO_0010159_4> <http://transformunify.org/ontologies/TURBO_0005611> "808753002abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_1> <http://transformunify.org/ontologies/TURBO_0005612> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010159_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<http://transformunify.org/ontologies/TURBO_0010159_2> <http://transformunify.org/ontologies/TURBO_0005612> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010159_4> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<http://transformunify.org/ontologies/TURBO_0010159_3> <http://transformunify.org/ontologies/TURBO_0005612> <http://purl.obolibrary.org/obo/BFO_0000001> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess")
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
"""<http://transformunify.org/ontologies/TURBO_0010159_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/c636f93d86d85a60a668ee3d97b121853a17d4a20ee73b01b4370c45131da652> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/b5bb565bfb27e2450f1dfefda81e80c3e730b0d07a634017c97c324bba6c8f7a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/06028325eaef4219bd9544e4e4bcf0cb19eabaf1571888a8630b0504f8cb33fd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/376f190f500de2737e339d20157cb933295c092fefc2cd5de6c244a4bb61216e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_3> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_4> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23>""",
"""<http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/c636f93d86d85a60a668ee3d97b121853a17d4a20ee73b01b4370c45131da652> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f>""",
"""<http://www.itmat.upenn.edu/biobank/b5bb565bfb27e2450f1dfefda81e80c3e730b0d07a634017c97c324bba6c8f7a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8>""",
"""<http://www.itmat.upenn.edu/biobank/06028325eaef4219bd9544e4e4bcf0cb19eabaf1571888a8630b0504f8cb33fd> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c>""",
"""<http://www.itmat.upenn.edu/biobank/376f190f500de2737e339d20157cb933295c092fefc2cd5de6c244a4bb61216e> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57>""",
"""<http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd>""",
"""<http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78>""",
"""<http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09>""",
"""<http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23>""",
"""<http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd> <http://transformunify.org/ontologies/TURBO_0010094> "808753002abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/c636f93d86d85a60a668ee3d97b121853a17d4a20ee73b01b4370c45131da652> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78> <http://transformunify.org/ontologies/TURBO_0010094> "808753002abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b5bb565bfb27e2450f1dfefda81e80c3e730b0d07a634017c97c324bba6c8f7a> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09> <http://transformunify.org/ontologies/TURBO_0010094> "808753002abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/06028325eaef4219bd9544e4e4bcf0cb19eabaf1571888a8630b0504f8cb33fd> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23> <http://transformunify.org/ontologies/TURBO_0010094> "808753002abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/376f190f500de2737e339d20157cb933295c092fefc2cd5de6c244a4bb61216e> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001>"""
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
<http://transformunify.org/ontologies/TURBO_0010159_3> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_4> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_3> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_2> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_1> .
<http://transformunify.org/ontologies/TURBO_0010159_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_4> .
<http://transformunify.org/ontologies/TURBO_0010159_1> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://transformunify.org/ontologies/TURBO_0010159_3> .
<http://transformunify.org/ontologies/TURBO_0010159_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
<http://transformunify.org/ontologies/TURBO_0010159_2> <http://transformunify.org/ontologies/TURBO_0005601> "1006779358abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010159_4> rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
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


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess")
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
"""<http://transformunify.org/ontologies/TURBO_0010159_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010159>""",
"""<http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/c636f93d86d85a60a668ee3d97b121853a17d4a20ee73b01b4370c45131da652> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/b5bb565bfb27e2450f1dfefda81e80c3e730b0d07a634017c97c324bba6c8f7a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/06028325eaef4219bd9544e4e4bcf0cb19eabaf1571888a8630b0504f8cb33fd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/PDRO_0000024>""",
"""<http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/376f190f500de2737e339d20157cb933295c092fefc2cd5de6c244a4bb61216e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_3> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78>""",
"""<http://transformunify.org/ontologies/TURBO_0010159_4> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23>""",
"""<http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/c636f93d86d85a60a668ee3d97b121853a17d4a20ee73b01b4370c45131da652> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f>""",
"""<http://www.itmat.upenn.edu/biobank/b5bb565bfb27e2450f1dfefda81e80c3e730b0d07a634017c97c324bba6c8f7a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8>""",
"""<http://www.itmat.upenn.edu/biobank/06028325eaef4219bd9544e4e4bcf0cb19eabaf1571888a8630b0504f8cb33fd> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c>""",
"""<http://www.itmat.upenn.edu/biobank/376f190f500de2737e339d20157cb933295c092fefc2cd5de6c244a4bb61216e> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57>""",
"""<http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd>""",
"""<http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78>""",
"""<http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09>""",
"""<http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23>""",
"""<http://www.itmat.upenn.edu/biobank/cf19a10404cfb1652861c02792c6b6d3311cf33e2878f8d77d549587cb0ed41f> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/c7ffaf3d7ede82e2ac1fd5bb80ad1734864967aee676c27f699b23a23d2582d8> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/35d055e24f1c29f731cb73015e8a1f7e0ec3ba03595c3d77b273fccd2b1c122c> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/04d06fdde0bda733514966333fdc3969f58a2a960a4922e8b3198cf019811d57> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010714>""",
"""<http://www.itmat.upenn.edu/biobank/c636f93d86d85a60a668ee3d97b121853a17d4a20ee73b01b4370c45131da652> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b5bb565bfb27e2450f1dfefda81e80c3e730b0d07a634017c97c324bba6c8f7a> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/06028325eaef4219bd9544e4e4bcf0cb19eabaf1571888a8630b0504f8cb33fd> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/376f190f500de2737e339d20157cb933295c092fefc2cd5de6c244a4bb61216e> <http://transformunify.org/ontologies/TURBO_0010094> "1006779358abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/6b34144e2916822e30f9bad25586aac8597467b69962a23f502579b40ff8b5cd>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/18f5a0544e59a9b0d0a6d2666cfd499a5fc669b8e634a0494cdf4c398d3f8b78>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/535cb7f1da188f183251cd04f44ea6c72d496fc97f680912767be134a8628f09>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/daa18a001756e2ca498c256bb07b0851463fc86fdf164f0702dd088e57895f23>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_1> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<https://github.com/PennTURBO/Drivetrain/EncToBeTyped_2> <http://purl.obolibrary.org/obo/BFO_0000055> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped_1>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}