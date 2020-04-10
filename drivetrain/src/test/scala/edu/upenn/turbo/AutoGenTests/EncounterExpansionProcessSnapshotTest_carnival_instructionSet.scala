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
    val UUIDKey = "320213e37cea4093b2e46b7596369a49"
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
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2EncType> <http://transformunify.org/ontologies/TURBO_0000527> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2RegDen> <http://transformunify.org/ontologies/TURBO_0000535> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2SymbVal> "316978036abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RoleType> <http://purl.obolibrary.org/obo/OBI_0000097> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2RoleType> <http://purl.obolibrary.org/obo/OBI_0000097> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RegDen> <http://transformunify.org/ontologies/TURBO_0000535> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2ScHs> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2ScHs> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2SymbVal> "316978036abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2EncType> <http://transformunify.org/ontologies/TURBO_0000527> .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2ScMeas> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> <https://github.com/PennTURBO/Drivetrain/scMeasHasUnitLabel> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2ScMeas> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> <https://github.com/PennTURBO/Drivetrain/scMeasHasType> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> <https://github.com/PennTURBO/Drivetrain/scMeasHasValue> "33737254.00"^^xsd:Double .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2ScMeas> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> <https://github.com/PennTURBO/Drivetrain/scMeasHasValue> "33737254.00"^^xsd:Double .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> <https://github.com/PennTURBO/Drivetrain/scMeasHasType> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> <https://github.com/PennTURBO/Drivetrain/scMeasHasUnitLabel> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2ScMeas> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> <https://github.com/PennTURBO/Drivetrain/scMeasHasValue> "33737254.00"^^xsd:Double .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2RawDate> "241702925abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> <https://github.com/PennTURBO/Drivetrain/scMeasHasType> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> <https://github.com/PennTURBO/Drivetrain/scMeasHasUnitLabel> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <https://github.com/PennTURBO/Drivetrain/scMeasHasUnitLabel> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <https://github.com/PennTURBO/Drivetrain/scMeasHasType> <http://purl.obolibrary.org/obo/BFO_0000001> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2DateXsd> "15/07/70"^^xsd:Date .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <https://github.com/PennTURBO/Drivetrain/scMeasHasValue> "33737254.00"^^xsd:Double .
<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <https://github.com/PennTURBO/Drivetrain/scEnc2RawDate> "241702925abc"^^xsd:String .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <https://github.com/PennTURBO/Drivetrain/scEnc2DateXsd> "15/07/70"^^xsd:Date .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/EncounterExpansionProcess")
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
"""<http://transformunify.org/ontologies/TURBO_0010161_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutMeasurement>""",
"""<http://www.itmat.upenn.edu/biobank/d756450b7b21f01946b22eea46e4dc8603d2c154ff94eaf7daf78b3fb8ccf765> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<http://www.itmat.upenn.edu/biobank/86a1f110132faf80c6f2bd150a588cce1742d837c417da69854ca2bf2f1dbbd4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/bfb99ff6718c629599c98de28d07be706dfeef3a81f4f272f26c7ccaf2eeebe5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://www.itmat.upenn.edu/biobank/6eb1636d2e495ce3ebc96dc1514e6a8cd54bc22266f896d0983e53ff9cb60ea7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/8d248bac4284d8cf4807d833e002988d6752946195e002c2a39fd57c6bbedc9d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000035>""",
"""<http://www.itmat.upenn.edu/biobank/9e59957a1399a016d9dbe05993d1cbd4b48398d57b1fde5f54eba5263d4492db> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000097>""",
"""<http://www.itmat.upenn.edu/biobank/9e59957a1399a016d9dbe05993d1cbd4b48398d57b1fde5f54eba5263d4492db> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/RoleToBeTyped>""",
"""<http://www.itmat.upenn.edu/biobank/9214ce3358de82cf297ad168bf136b16083b55d1979932587ce8b90113fad7fd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001933>""",
"""<http://www.itmat.upenn.edu/biobank/0101123eaec6e0c4a42eeab17816b21acc657acfcdc2ba7b1b257773af32e109> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/5a3f770fe3c80709504e2f1a9facdae9015bc56b7080670478ade3918abb7cd3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001933>""",
"""<http://www.itmat.upenn.edu/biobank/f84fbffd64fa821eff5fd5550372ea52ac51f758925a718669611bad03041f5c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<http://www.itmat.upenn.edu/biobank/d6d916c803531c175028ef27fde240fe90b15b9617426c8e5a563c38da430710> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/5d1f181b0e29e4fd3eeb9028b8ceec17819707e2e255aae7823085faa0f5c3ce> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000416>""",
"""<http://www.itmat.upenn.edu/biobank/6ba4140de6872eabf1849a3a1ebc6ddb7172f05acae9adf8b2a948261de0f712> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/42d36b14707e201ea1ce915662a16318346e2ebcd948829418fe51c6017c1c9d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000035>""",
"""<http://www.itmat.upenn.edu/biobank/fa6c2bea2704dd4bfba4a15ba8d4a1b7a76f4903f3e9dc4fa4dfc972d875c0ea> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001933>""",
"""<http://www.itmat.upenn.edu/biobank/cd4587adbcefc7a8493ae11a605287165db733703480b4975220ff2317c5b3cf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/504dd3c5b2b78cd1132d6db35e6b45c25b116f67dd14746a532e50670b48ae06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001933>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54>""",
"""<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/d756450b7b21f01946b22eea46e4dc8603d2c154ff94eaf7daf78b3fb8ccf765>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_4> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/f84fbffd64fa821eff5fd5550372ea52ac51f758925a718669611bad03041f5c>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/0101123eaec6e0c4a42eeab17816b21acc657acfcdc2ba7b1b257773af32e109>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutMeasurement_3> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/cd4587adbcefc7a8493ae11a605287165db733703480b4975220ff2317c5b3cf>""",
"""<http://www.itmat.upenn.edu/biobank/d756450b7b21f01946b22eea46e4dc8603d2c154ff94eaf7daf78b3fb8ccf765> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/bfb99ff6718c629599c98de28d07be706dfeef3a81f4f272f26c7ccaf2eeebe5> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/8d248bac4284d8cf4807d833e002988d6752946195e002c2a39fd57c6bbedc9d>""",
"""<http://www.itmat.upenn.edu/biobank/0101123eaec6e0c4a42eeab17816b21acc657acfcdc2ba7b1b257773af32e109> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/f84fbffd64fa821eff5fd5550372ea52ac51f758925a718669611bad03041f5c> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/5d1f181b0e29e4fd3eeb9028b8ceec17819707e2e255aae7823085faa0f5c3ce> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/42d36b14707e201ea1ce915662a16318346e2ebcd948829418fe51c6017c1c9d>""",
"""<http://www.itmat.upenn.edu/biobank/cd4587adbcefc7a8493ae11a605287165db733703480b4975220ff2317c5b3cf> <http://purl.obolibrary.org/obo/IAO_0000136> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/6eb1636d2e495ce3ebc96dc1514e6a8cd54bc22266f896d0983e53ff9cb60ea7> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/86a1f110132faf80c6f2bd150a588cce1742d837c417da69854ca2bf2f1dbbd4>""",
"""<http://www.itmat.upenn.edu/biobank/6ba4140de6872eabf1849a3a1ebc6ddb7172f05acae9adf8b2a948261de0f712> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/d6d916c803531c175028ef27fde240fe90b15b9617426c8e5a563c38da430710>""",
"""<http://www.itmat.upenn.edu/biobank/86a1f110132faf80c6f2bd150a588cce1742d837c417da69854ca2bf2f1dbbd4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54>""",
"""<http://www.itmat.upenn.edu/biobank/86a1f110132faf80c6f2bd150a588cce1742d837c417da69854ca2bf2f1dbbd4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/bfb99ff6718c629599c98de28d07be706dfeef3a81f4f272f26c7ccaf2eeebe5>""",
"""<http://www.itmat.upenn.edu/biobank/d6d916c803531c175028ef27fde240fe90b15b9617426c8e5a563c38da430710> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40>""",
"""<http://www.itmat.upenn.edu/biobank/d6d916c803531c175028ef27fde240fe90b15b9617426c8e5a563c38da430710> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/5d1f181b0e29e4fd3eeb9028b8ceec17819707e2e255aae7823085faa0f5c3ce>""",
"""<http://www.itmat.upenn.edu/biobank/86a1f110132faf80c6f2bd150a588cce1742d837c417da69854ca2bf2f1dbbd4> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000535>""",
"""<http://www.itmat.upenn.edu/biobank/86a1f110132faf80c6f2bd150a588cce1742d837c417da69854ca2bf2f1dbbd4> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/6eb1636d2e495ce3ebc96dc1514e6a8cd54bc22266f896d0983e53ff9cb60ea7>""",
"""<http://www.itmat.upenn.edu/biobank/d6d916c803531c175028ef27fde240fe90b15b9617426c8e5a563c38da430710> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000535>""",
"""<http://www.itmat.upenn.edu/biobank/d6d916c803531c175028ef27fde240fe90b15b9617426c8e5a563c38da430710> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/6ba4140de6872eabf1849a3a1ebc6ddb7172f05acae9adf8b2a948261de0f712>""",
"""<http://www.itmat.upenn.edu/biobank/8d248bac4284d8cf4807d833e002988d6752946195e002c2a39fd57c6bbedc9d> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54>""",
"""<http://www.itmat.upenn.edu/biobank/42d36b14707e201ea1ce915662a16318346e2ebcd948829418fe51c6017c1c9d> <http://purl.obolibrary.org/obo/RO_0002223> <http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40>""",
"""<http://www.itmat.upenn.edu/biobank/bfb99ff6718c629599c98de28d07be706dfeef3a81f4f272f26c7ccaf2eeebe5> <http://transformunify.org/ontologies/TURBO_0010094> "241702925abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/6eb1636d2e495ce3ebc96dc1514e6a8cd54bc22266f896d0983e53ff9cb60ea7> <http://transformunify.org/ontologies/TURBO_0010094> "316978036abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/5d1f181b0e29e4fd3eeb9028b8ceec17819707e2e255aae7823085faa0f5c3ce> <http://transformunify.org/ontologies/TURBO_0010094> "241702925abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/6ba4140de6872eabf1849a3a1ebc6ddb7172f05acae9adf8b2a948261de0f712> <http://transformunify.org/ontologies/TURBO_0010094> "316978036abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/d756450b7b21f01946b22eea46e4dc8603d2c154ff94eaf7daf78b3fb8ccf765>""",
"""<http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/0101123eaec6e0c4a42eeab17816b21acc657acfcdc2ba7b1b257773af32e109>""",
"""<http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/f84fbffd64fa821eff5fd5550372ea52ac51f758925a718669611bad03041f5c>""",
"""<http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/cd4587adbcefc7a8493ae11a605287165db733703480b4975220ff2317c5b3cf>""",
"""<http://www.itmat.upenn.edu/biobank/d756450b7b21f01946b22eea46e4dc8603d2c154ff94eaf7daf78b3fb8ccf765> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/0101123eaec6e0c4a42eeab17816b21acc657acfcdc2ba7b1b257773af32e109> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/f84fbffd64fa821eff5fd5550372ea52ac51f758925a718669611bad03041f5c> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/cd4587adbcefc7a8493ae11a605287165db733703480b4975220ff2317c5b3cf> <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/bfb99ff6718c629599c98de28d07be706dfeef3a81f4f272f26c7ccaf2eeebe5> <http://purl.obolibrary.org/obo/IAO_0000004> "15/07/70"^^<http://www.w3.org/2001/XMLSchema#Date>""",
"""<http://www.itmat.upenn.edu/biobank/5d1f181b0e29e4fd3eeb9028b8ceec17819707e2e255aae7823085faa0f5c3ce> <http://purl.obolibrary.org/obo/IAO_0000004> "15/07/70"^^<http://www.w3.org/2001/XMLSchema#Date>""",
"""<http://www.itmat.upenn.edu/biobank/bd9c8c66da90e284fc20d8c429723e0b424854cb9af17b32029eddd077f0eb54> <http://purl.obolibrary.org/obo/BFO_0000055> <http://www.itmat.upenn.edu/biobank/9e59957a1399a016d9dbe05993d1cbd4b48398d57b1fde5f54eba5263d4492db>""",
"""<http://www.itmat.upenn.edu/biobank/4ae4bfcfde910baeb0c4f7909d32a9d4640dfc39d4322fe3c84e68411fad7e40> <http://purl.obolibrary.org/obo/BFO_0000055> <http://www.itmat.upenn.edu/biobank/9e59957a1399a016d9dbe05993d1cbd4b48398d57b1fde5f54eba5263d4492db>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <http://www.itmat.upenn.edu/biobank/9e59957a1399a016d9dbe05993d1cbd4b48398d57b1fde5f54eba5263d4492db>""",
"""<http://www.itmat.upenn.edu/biobank/d756450b7b21f01946b22eea46e4dc8603d2c154ff94eaf7daf78b3fb8ccf765> <http://purl.obolibrary.org/obo/OBI_0001938> <http://www.itmat.upenn.edu/biobank/9214ce3358de82cf297ad168bf136b16083b55d1979932587ce8b90113fad7fd>""",
"""<http://www.itmat.upenn.edu/biobank/0101123eaec6e0c4a42eeab17816b21acc657acfcdc2ba7b1b257773af32e109> <http://purl.obolibrary.org/obo/OBI_0001938> <http://www.itmat.upenn.edu/biobank/5a3f770fe3c80709504e2f1a9facdae9015bc56b7080670478ade3918abb7cd3>""",
"""<http://www.itmat.upenn.edu/biobank/f84fbffd64fa821eff5fd5550372ea52ac51f758925a718669611bad03041f5c> <http://purl.obolibrary.org/obo/OBI_0001938> <http://www.itmat.upenn.edu/biobank/fa6c2bea2704dd4bfba4a15ba8d4a1b7a76f4903f3e9dc4fa4dfc972d875c0ea>""",
"""<http://www.itmat.upenn.edu/biobank/cd4587adbcefc7a8493ae11a605287165db733703480b4975220ff2317c5b3cf> <http://purl.obolibrary.org/obo/OBI_0001938> <http://www.itmat.upenn.edu/biobank/504dd3c5b2b78cd1132d6db35e6b45c25b116f67dd14746a532e50670b48ae06>""",
"""<http://www.itmat.upenn.edu/biobank/9214ce3358de82cf297ad168bf136b16083b55d1979932587ce8b90113fad7fd> <http://purl.obolibrary.org/obo/OBI_0002135> "33737254.00"^^<http://www.w3.org/2001/XMLSchema#Double>""",
"""<http://www.itmat.upenn.edu/biobank/5a3f770fe3c80709504e2f1a9facdae9015bc56b7080670478ade3918abb7cd3> <http://purl.obolibrary.org/obo/OBI_0002135> "33737254.00"^^<http://www.w3.org/2001/XMLSchema#Double>""",
"""<http://www.itmat.upenn.edu/biobank/fa6c2bea2704dd4bfba4a15ba8d4a1b7a76f4903f3e9dc4fa4dfc972d875c0ea> <http://purl.obolibrary.org/obo/OBI_0002135> "33737254.00"^^<http://www.w3.org/2001/XMLSchema#Double>""",
"""<http://www.itmat.upenn.edu/biobank/504dd3c5b2b78cd1132d6db35e6b45c25b116f67dd14746a532e50670b48ae06> <http://purl.obolibrary.org/obo/OBI_0002135> "33737254.00"^^<http://www.w3.org/2001/XMLSchema#Double>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}