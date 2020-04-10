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
class LossOfFunctionExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "carnival_instructionSet.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "b25fe040852543e0bd4174c39c7d2d66"
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
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010015> "334459855abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0007605> "953377515abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0007605> "953377515abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0010142> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0007607> <http://transformunify.org/ontologies/TURBO_0000590> .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0007607> <http://transformunify.org/ontologies/TURBO_0000590> .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010144_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010144> .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0010016> "210815534abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010016> "210815534abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010095> "1216405858"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010285> <http://transformunify.org/ontologies/TURBO_0000567> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0010095> "1216405858"^^xsd:Integer .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0010285> <http://transformunify.org/ontologies/TURBO_0000567> .
<http://transformunify.org/ontologies/TURBO_0010144_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010144> .
<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010142> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> .
<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0010015> "334459855abc"^^xsd:String .
}
GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://transformunify.org/ontologies/TURBO_0000527_2> .
<http://transformunify.org/ontologies/TURBO_0000527_2> rdf:type <http://transformunify.org/ontologies/TURBO_0000527> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://transformunify.org/ontologies/TURBO_0000527_1> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> rdf:type <https://github.com/PennTURBO/Drivetrain/shortcutEncounter> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <http://purl.obolibrary.org/obo/OBI_0000097_1> .
<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
<http://purl.obolibrary.org/obo/OBI_0000097_1> rdf:type <http://purl.obolibrary.org/obo/OBI_0000097> .
<http://transformunify.org/ontologies/TURBO_0000527_1> <http://purl.obolibrary.org/obo/BFO_0000055> <http://purl.obolibrary.org/obo/OBI_0000097_1> .
<http://transformunify.org/ontologies/TURBO_0000527_2> <http://purl.obolibrary.org/obo/BFO_0000055> <http://purl.obolibrary.org/obo/OBI_0000097_1> .
<http://transformunify.org/ontologies/TURBO_0000527_1> rdf:type <http://transformunify.org/ontologies/TURBO_0000527> .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/LossOfFunctionExpansionProcess")
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
"""<http://transformunify.org/ontologies/TURBO_0010144_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010144>""",
"""<http://transformunify.org/ontologies/TURBO_0000527_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<http://purl.obolibrary.org/obo/OBI_0000097_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000097>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/PennTURBO/Drivetrain/shortcutEncounter>""",
"""<http://transformunify.org/ontologies/TURBO_0010144_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010144>""",
"""<http://transformunify.org/ontologies/TURBO_0000527_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352>""",
"""<http://www.itmat.upenn.edu/biobank/a7a9fdc35f3ab991620ba6fbc33aaf31bbb26f3cf6a8053822b9660e0ed57b1a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000257>""",
"""<http://www.itmat.upenn.edu/biobank/8096d64bcc98ac8719e00c0387b3ee7012613d08db905778a26cad59db9def55> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001051>""",
"""<http://www.itmat.upenn.edu/biobank/72ee25db793b315117bbf7e2f5e4aa64dcec14c568b490d70a3eda456821f5c8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001479>""",
"""<http://www.itmat.upenn.edu/biobank/5b99adabf6dba60b13ad72c03e7c7a0d61914c22dcb375c17f1b6a7fd9597949> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001868>""",
"""<http://www.itmat.upenn.edu/biobank/e104dceb64fb4e603a8f4f1c108027d0fdeebcb341db260e7680f57bdacde0a4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0600005>""",
"""<http://www.itmat.upenn.edu/biobank/b089192774841e216807027f379b277423bb34fa1b2b41cec6adc1c03d330711> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0002118>""",
"""<http://www.itmat.upenn.edu/biobank/cb9eb6960e1b3fae59663fa2a45f0575e0c700227d9ffc16f84da766524a8e21> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001573>""",
"""<http://www.itmat.upenn.edu/biobank/30e1ce0d5fabbe1ecdfcaf89dad18c2505b86b0d7d7b5982bbb92bf5375f8cc9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0200000>""",
"""<http://www.itmat.upenn.edu/biobank/30f510117e938b8e40bb09c777611b9e0791ae1af2445105ed564a881bcdb9fc> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/e37b32c91bb2eb5218f209acecb8cedb84ee0e7339040f04ca8b783d4d7327c6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352>""",
"""<http://www.itmat.upenn.edu/biobank/5d86183266090af29204e086e9236f3e068058ddca7c63050a6a05945f1cf111> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000257>""",
"""<http://www.itmat.upenn.edu/biobank/82811aeee23af88bc27b44a780f946dfd8a90461cd23c7b30bc6501114310bae> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001051>""",
"""<http://www.itmat.upenn.edu/biobank/2047aa76588c97f8bedfc20deb71e3be6a247889830823bb732961873ab981cd> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001479>""",
"""<http://www.itmat.upenn.edu/biobank/0ef8c1727ff617484088a0b020118d3dd166ea056d6aeb4e31f2bea2a546c99b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0600005>""",
"""<http://www.itmat.upenn.edu/biobank/ebef670b343e58302e1e37ab3d08435373d5d9144efeaf51b545ead63f2d1ad4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0002118>""",
"""<http://www.itmat.upenn.edu/biobank/d3d83f71d119532fd098f1c0279d1ec48b332ddfd4bcee2fdc1937c54cdfe82c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001573>""",
"""<http://www.itmat.upenn.edu/biobank/d40ade8974b9381252fe9f7ef2cf2d53a68f480e7e43efa6f50291f2d960d431> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0200000>""",
"""<http://www.itmat.upenn.edu/biobank/f4a93f5f2f900f68526a4497b7805fea9362d9b70732771b9a444633007828f0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/ef731e19997537c0b19f38ebaffa3ea1e719b80910670741f1dd26b2278d875a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/a7a9fdc35f3ab991620ba6fbc33aaf31bbb26f3cf6a8053822b9660e0ed57b1a> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/72ee25db793b315117bbf7e2f5e4aa64dcec14c568b490d70a3eda456821f5c8>""",
"""<http://www.itmat.upenn.edu/biobank/e104dceb64fb4e603a8f4f1c108027d0fdeebcb341db260e7680f57bdacde0a4> <http://purl.obolibrary.org/obo/OBI_0000293> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/b089192774841e216807027f379b277423bb34fa1b2b41cec6adc1c03d330711> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/8096d64bcc98ac8719e00c0387b3ee7012613d08db905778a26cad59db9def55>""",
"""<http://www.itmat.upenn.edu/biobank/30e1ce0d5fabbe1ecdfcaf89dad18c2505b86b0d7d7b5982bbb92bf5375f8cc9> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/cb9eb6960e1b3fae59663fa2a45f0575e0c700227d9ffc16f84da766524a8e21>""",
"""<http://www.itmat.upenn.edu/biobank/5d86183266090af29204e086e9236f3e068058ddca7c63050a6a05945f1cf111> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/2047aa76588c97f8bedfc20deb71e3be6a247889830823bb732961873ab981cd>""",
"""<http://www.itmat.upenn.edu/biobank/0ef8c1727ff617484088a0b020118d3dd166ea056d6aeb4e31f2bea2a546c99b> <http://purl.obolibrary.org/obo/OBI_0000293> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/ebef670b343e58302e1e37ab3d08435373d5d9144efeaf51b545ead63f2d1ad4> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/82811aeee23af88bc27b44a780f946dfd8a90461cd23c7b30bc6501114310bae>""",
"""<http://www.itmat.upenn.edu/biobank/d40ade8974b9381252fe9f7ef2cf2d53a68f480e7e43efa6f50291f2d960d431> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/d3d83f71d119532fd098f1c0279d1ec48b332ddfd4bcee2fdc1937c54cdfe82c>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://transformunify.org/ontologies/TURBO_0000527_1>""",
"""<http://transformunify.org/ontologies/TURBO_0010144_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38>""",
"""<https://github.com/PennTURBO/Drivetrain/shortcutEncounter_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://transformunify.org/ontologies/TURBO_0000527_2>""",
"""<http://transformunify.org/ontologies/TURBO_0010144_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/5b99adabf6dba60b13ad72c03e7c7a0d61914c22dcb375c17f1b6a7fd9597949>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/5b99adabf6dba60b13ad72c03e7c7a0d61914c22dcb375c17f1b6a7fd9597949>""",
"""<http://transformunify.org/ontologies/TURBO_0000567> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/30f510117e938b8e40bb09c777611b9e0791ae1af2445105ed564a881bcdb9fc>""",
"""<http://transformunify.org/ontologies/TURBO_0000567> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/f4a93f5f2f900f68526a4497b7805fea9362d9b70732771b9a444633007828f0>""",
"""<http://www.itmat.upenn.edu/biobank/e104dceb64fb4e603a8f4f1c108027d0fdeebcb341db260e7680f57bdacde0a4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0000527_1>""",
"""<http://www.itmat.upenn.edu/biobank/e37b32c91bb2eb5218f209acecb8cedb84ee0e7339040f04ca8b783d4d7327c6> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/30f510117e938b8e40bb09c777611b9e0791ae1af2445105ed564a881bcdb9fc>""",
"""<http://www.itmat.upenn.edu/biobank/0ef8c1727ff617484088a0b020118d3dd166ea056d6aeb4e31f2bea2a546c99b> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0000527_2>""",
"""<http://www.itmat.upenn.edu/biobank/ef731e19997537c0b19f38ebaffa3ea1e719b80910670741f1dd26b2278d875a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/f4a93f5f2f900f68526a4497b7805fea9362d9b70732771b9a444633007828f0>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://transformunify.org/ontologies/TURBO_0010095> "1216405858"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://transformunify.org/ontologies/TURBO_0010095> "1216405858"^^<http://www.w3.org/2001/XMLSchema#Integer>""",
"""<http://www.itmat.upenn.edu/biobank/30f510117e938b8e40bb09c777611b9e0791ae1af2445105ed564a881bcdb9fc> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/72ee25db793b315117bbf7e2f5e4aa64dcec14c568b490d70a3eda456821f5c8>""",
"""<http://www.itmat.upenn.edu/biobank/f4a93f5f2f900f68526a4497b7805fea9362d9b70732771b9a444633007828f0> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/2047aa76588c97f8bedfc20deb71e3be6a247889830823bb732961873ab981cd>""",
"""<http://transformunify.org/ontologies/TURBO_0000527_1> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/e104dceb64fb4e603a8f4f1c108027d0fdeebcb341db260e7680f57bdacde0a4>""",
"""<http://transformunify.org/ontologies/TURBO_0000527_2> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/0ef8c1727ff617484088a0b020118d3dd166ea056d6aeb4e31f2bea2a546c99b>""",
"""<http://www.itmat.upenn.edu/biobank/30f510117e938b8e40bb09c777611b9e0791ae1af2445105ed564a881bcdb9fc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000567>""",
"""<http://www.itmat.upenn.edu/biobank/30f510117e938b8e40bb09c777611b9e0791ae1af2445105ed564a881bcdb9fc> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/e37b32c91bb2eb5218f209acecb8cedb84ee0e7339040f04ca8b783d4d7327c6>""",
"""<http://www.itmat.upenn.edu/biobank/f4a93f5f2f900f68526a4497b7805fea9362d9b70732771b9a444633007828f0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000567>""",
"""<http://www.itmat.upenn.edu/biobank/f4a93f5f2f900f68526a4497b7805fea9362d9b70732771b9a444633007828f0> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/ef731e19997537c0b19f38ebaffa3ea1e719b80910670741f1dd26b2278d875a>""",
"""<http://www.itmat.upenn.edu/biobank/e37b32c91bb2eb5218f209acecb8cedb84ee0e7339040f04ca8b783d4d7327c6> <http://transformunify.org/ontologies/TURBO_0010094> "953377515abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/ef731e19997537c0b19f38ebaffa3ea1e719b80910670741f1dd26b2278d875a> <http://transformunify.org/ontologies/TURBO_0010094> "953377515abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/a7a9fdc35f3ab991620ba6fbc33aaf31bbb26f3cf6a8053822b9660e0ed57b1a> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/8096d64bcc98ac8719e00c0387b3ee7012613d08db905778a26cad59db9def55>""",
"""<http://www.itmat.upenn.edu/biobank/e104dceb64fb4e603a8f4f1c108027d0fdeebcb341db260e7680f57bdacde0a4> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/72ee25db793b315117bbf7e2f5e4aa64dcec14c568b490d70a3eda456821f5c8>""",
"""<http://www.itmat.upenn.edu/biobank/b089192774841e216807027f379b277423bb34fa1b2b41cec6adc1c03d330711> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/cb9eb6960e1b3fae59663fa2a45f0575e0c700227d9ffc16f84da766524a8e21>""",
"""<http://www.itmat.upenn.edu/biobank/30e1ce0d5fabbe1ecdfcaf89dad18c2505b86b0d7d7b5982bbb92bf5375f8cc9> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38>""",
"""<http://www.itmat.upenn.edu/biobank/5d86183266090af29204e086e9236f3e068058ddca7c63050a6a05945f1cf111> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/82811aeee23af88bc27b44a780f946dfd8a90461cd23c7b30bc6501114310bae>""",
"""<http://www.itmat.upenn.edu/biobank/0ef8c1727ff617484088a0b020118d3dd166ea056d6aeb4e31f2bea2a546c99b> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/2047aa76588c97f8bedfc20deb71e3be6a247889830823bb732961873ab981cd>""",
"""<http://www.itmat.upenn.edu/biobank/ebef670b343e58302e1e37ab3d08435373d5d9144efeaf51b545ead63f2d1ad4> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/d3d83f71d119532fd098f1c0279d1ec48b332ddfd4bcee2fdc1937c54cdfe82c>""",
"""<http://www.itmat.upenn.edu/biobank/d40ade8974b9381252fe9f7ef2cf2d53a68f480e7e43efa6f50291f2d960d431> <http://purl.obolibrary.org/obo/OBI_0000299> <http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4>""",
"""<http://transformunify.org/ontologies/TURBO_0000527_1> <http://purl.obolibrary.org/obo/BFO_0000055> <http://purl.obolibrary.org/obo/OBI_0000097_1>""",
"""<http://transformunify.org/ontologies/TURBO_0000527_2> <http://purl.obolibrary.org/obo/BFO_0000055> <http://purl.obolibrary.org/obo/OBI_0000097_1>""",
"""<http://purl.obolibrary.org/obo/NCBITaxon_9606_1> <http://purl.obolibrary.org/obo/RO_0000087> <http://purl.obolibrary.org/obo/OBI_0000097_1>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://transformunify.org/ontologies/TURBO_0010015> "210815534abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://transformunify.org/ontologies/TURBO_0010015> "210815534abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://transformunify.org/ontologies/TURBO_0010016> "334459855abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://transformunify.org/ontologies/TURBO_0010016> "334459855abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/5b99adabf6dba60b13ad72c03e7c7a0d61914c22dcb375c17f1b6a7fd9597949> <http://purl.obolibrary.org/obo/OBI_0000643> <http://www.itmat.upenn.edu/biobank/8096d64bcc98ac8719e00c0387b3ee7012613d08db905778a26cad59db9def55>""",
"""<http://www.itmat.upenn.edu/biobank/5b99adabf6dba60b13ad72c03e7c7a0d61914c22dcb375c17f1b6a7fd9597949> <http://purl.obolibrary.org/obo/OBI_0000643> <http://www.itmat.upenn.edu/biobank/82811aeee23af88bc27b44a780f946dfd8a90461cd23c7b30bc6501114310bae>""",
"""<http://www.itmat.upenn.edu/biobank/5b99adabf6dba60b13ad72c03e7c7a0d61914c22dcb375c17f1b6a7fd9597949> <http://purl.obolibrary.org/obo/OGG_0000000014> <http://purl.obolibrary.org/obo/NCBITaxon_9606_1>""",
"""<http://www.itmat.upenn.edu/biobank/6b5ba9d6b62396861660661afc8165a837a4015312cc52f4b7745603431ccc38> <http://purl.obolibrary.org/obo/OBI_0001938> <http://transformunify.org/ontologies/TURBO_0000590>""",
"""<http://www.itmat.upenn.edu/biobank/7f39132ea587e02ab54d98607cd50ce4df661bb10cc54fd9c2e3de99d19d31e4> <http://purl.obolibrary.org/obo/OBI_0001938> <http://transformunify.org/ontologies/TURBO_0000590>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}