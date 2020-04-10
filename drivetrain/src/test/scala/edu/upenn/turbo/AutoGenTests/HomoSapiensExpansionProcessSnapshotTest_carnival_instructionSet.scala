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
class HomoSapiensExpansionProcessSnapshotTest_carnival_instructionSet extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "carnival_instructionSet.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "a913a1fbc02d495388af6a4d09bb7470"
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
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010168_2> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010168_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010168_2> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
<http://transformunify.org/ontologies/TURBO_0010168_2> <http://transformunify.org/ontologies/TURBO_0010079> "285031059abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010079> "285031059abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
}

                   # Optional triples
                   GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
<http://transformunify.org/ontologies/TURBO_0010191_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010191> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010100> "1543800849abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010277> <http://transformunify.org/ontologies/TURBO_0010274> .
<http://transformunify.org/ontologies/TURBO_0010191_2> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010194> "1003789261abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010098> "708384193abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_2> <http://transformunify.org/ontologies/TURBO_0010194> "1003789261abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010191_2> <http://transformunify.org/ontologies/TURBO_0010277> <http://transformunify.org/ontologies/TURBO_0010274> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010085> "28852662abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010191_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010191> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010086> "65/25/19"^^xsd:Date .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010089> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010090> <http://purl.obolibrary.org/obo/BFO_0000001> .
<http://transformunify.org/ontologies/TURBO_0010191_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://transformunify.org/ontologies/TURBO_0010161_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010168>""",
"""<http://transformunify.org/ontologies/TURBO_0010191_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010191>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010168>""",
"""<http://transformunify.org/ontologies/TURBO_0010191_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010191>""",
"""<http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<http://www.itmat.upenn.edu/biobank/f0d47ff933f08a2ab8343b2410ee2742c4d4e8c1ea8fb2b88c0c7da09a2501ac> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010070>""",
"""<http://www.itmat.upenn.edu/biobank/c84200d59bb5ea51b8c066951fdc8ed7855264fb040b1c99e5fcceb3367cf64b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/d910d86434dc434f4407ad6c3b09427f56d12bddfc4f90854d623196052a3141> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/b392ea35dc1b13daa480100472b428ca2b8bb666fd775c77b7198be737b659ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.ebi.ac.uk/efo/EFO_0004950>""",
"""<http://www.itmat.upenn.edu/biobank/be03dc10071044fce02ed7e003edb89d1cf786c41028c6fee3803a57d1c5fdb8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/UBERON_0035946>""",
"""<http://www.itmat.upenn.edu/biobank/e43e4eb1e4a5c24a18650d2a210a5a4bd8b8591dc788fb65cbfe53d4ec65d0d6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/ae1417b028354c80e37636b4a204c3296580719d3c57624c66b067e64790b9f3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/MONDO_0004992>""",
"""<http://www.itmat.upenn.edu/biobank/db700e4fd1011efb6bb9e82fe981c96fee8d167a22017a92a59fc2c7a26a9fa8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/ec1bd296ac92cb703be92250901e4173d06f0892fa9bf73dc9b0dc9f89593a3d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/19282628f7e12c41f67c61be1f84045250e0d0bdc9da6799a077a731dea7cc4a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010070>""",
"""<http://www.itmat.upenn.edu/biobank/addeae9b224c8f72cb4d1b0105f802d3a45e27ee4f4f446337aae91e08e01ee6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/MONDO_0004992>""",
"""<http://www.itmat.upenn.edu/biobank/9f706092609bcdc6ef85f425b734af9e911bdc11e93ee2cf10e7a7b444277a6b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/598a39c9d59de2bb2a621fd56a78f30cf5142552b3a3a70000c2e373492a5721> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/cdc97b8885d637ff7c6a61a3b778d1c9e5e4ca2421676cc49ac79d1aee88b380> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e>""",
"""<http://transformunify.org/ontologies/TURBO_0010191_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/f0d47ff933f08a2ab8343b2410ee2742c4d4e8c1ea8fb2b88c0c7da09a2501ac>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d>""",
"""<http://transformunify.org/ontologies/TURBO_0010191_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/19282628f7e12c41f67c61be1f84045250e0d0bdc9da6799a077a731dea7cc4a>""",
"""<http://www.itmat.upenn.edu/biobank/c84200d59bb5ea51b8c066951fdc8ed7855264fb040b1c99e5fcceb3367cf64b> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/d910d86434dc434f4407ad6c3b09427f56d12bddfc4f90854d623196052a3141> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/b392ea35dc1b13daa480100472b428ca2b8bb666fd775c77b7198be737b659ad> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/be03dc10071044fce02ed7e003edb89d1cf786c41028c6fee3803a57d1c5fdb8>""",
"""<http://transformunify.org/ontologies/TURBO_0010274> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/ec1bd296ac92cb703be92250901e4173d06f0892fa9bf73dc9b0dc9f89593a3d>""",
"""<http://transformunify.org/ontologies/TURBO_0010274> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/598a39c9d59de2bb2a621fd56a78f30cf5142552b3a3a70000c2e373492a5721>""",
"""<http://www.itmat.upenn.edu/biobank/f0d47ff933f08a2ab8343b2410ee2742c4d4e8c1ea8fb2b88c0c7da09a2501ac> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/e43e4eb1e4a5c24a18650d2a210a5a4bd8b8591dc788fb65cbfe53d4ec65d0d6> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e>""",
"""<http://www.itmat.upenn.edu/biobank/db700e4fd1011efb6bb9e82fe981c96fee8d167a22017a92a59fc2c7a26a9fa8> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/ec1bd296ac92cb703be92250901e4173d06f0892fa9bf73dc9b0dc9f89593a3d>""",
"""<http://www.itmat.upenn.edu/biobank/19282628f7e12c41f67c61be1f84045250e0d0bdc9da6799a077a731dea7cc4a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/9f706092609bcdc6ef85f425b734af9e911bdc11e93ee2cf10e7a7b444277a6b> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/598a39c9d59de2bb2a621fd56a78f30cf5142552b3a3a70000c2e373492a5721>""",
"""<http://www.itmat.upenn.edu/biobank/cdc97b8885d637ff7c6a61a3b778d1c9e5e4ca2421676cc49ac79d1aee88b380> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d>""",
"""<http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/ec1bd296ac92cb703be92250901e4173d06f0892fa9bf73dc9b0dc9f89593a3d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/f0d47ff933f08a2ab8343b2410ee2742c4d4e8c1ea8fb2b88c0c7da09a2501ac>""",
"""<http://www.itmat.upenn.edu/biobank/598a39c9d59de2bb2a621fd56a78f30cf5142552b3a3a70000c2e373492a5721> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/19282628f7e12c41f67c61be1f84045250e0d0bdc9da6799a077a731dea7cc4a>""",
"""<http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000505>""",
"""<http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/f0d47ff933f08a2ab8343b2410ee2742c4d4e8c1ea8fb2b88c0c7da09a2501ac>""",
"""<http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/19282628f7e12c41f67c61be1f84045250e0d0bdc9da6799a077a731dea7cc4a>""",
"""<http://www.itmat.upenn.edu/biobank/ec1bd296ac92cb703be92250901e4173d06f0892fa9bf73dc9b0dc9f89593a3d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010274>""",
"""<http://www.itmat.upenn.edu/biobank/ec1bd296ac92cb703be92250901e4173d06f0892fa9bf73dc9b0dc9f89593a3d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/db700e4fd1011efb6bb9e82fe981c96fee8d167a22017a92a59fc2c7a26a9fa8>""",
"""<http://www.itmat.upenn.edu/biobank/598a39c9d59de2bb2a621fd56a78f30cf5142552b3a3a70000c2e373492a5721> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010274>""",
"""<http://www.itmat.upenn.edu/biobank/598a39c9d59de2bb2a621fd56a78f30cf5142552b3a3a70000c2e373492a5721> <http://purl.obolibrary.org/obo/BFO_0000051> <http://www.itmat.upenn.edu/biobank/9f706092609bcdc6ef85f425b734af9e911bdc11e93ee2cf10e7a7b444277a6b>""",
"""<http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000505>""",
"""<http://www.itmat.upenn.edu/biobank/c84200d59bb5ea51b8c066951fdc8ed7855264fb040b1c99e5fcceb3367cf64b> <http://transformunify.org/ontologies/TURBO_0010094> "708384193abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/d910d86434dc434f4407ad6c3b09427f56d12bddfc4f90854d623196052a3141> <http://transformunify.org/ontologies/TURBO_0010094> "1543800849abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b392ea35dc1b13daa480100472b428ca2b8bb666fd775c77b7198be737b659ad> <http://transformunify.org/ontologies/TURBO_0010094> "28852662abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/e43e4eb1e4a5c24a18650d2a210a5a4bd8b8591dc788fb65cbfe53d4ec65d0d6> <http://transformunify.org/ontologies/TURBO_0010094> "285031059abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/cdc97b8885d637ff7c6a61a3b778d1c9e5e4ca2421676cc49ac79d1aee88b380> <http://transformunify.org/ontologies/TURBO_0010094> "285031059abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b392ea35dc1b13daa480100472b428ca2b8bb666fd775c77b7198be737b659ad> <http://purl.obolibrary.org/obo/IAO_0000004> "65/25/19"^^<http://www.w3.org/2001/XMLSchema#Date>""",
"""<http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594> <http://transformunify.org/ontologies/TURBO_0000303> <http://www.itmat.upenn.edu/biobank/be03dc10071044fce02ed7e003edb89d1cf786c41028c6fee3803a57d1c5fdb8>""",
"""<http://www.itmat.upenn.edu/biobank/ae1417b028354c80e37636b4a204c3296580719d3c57624c66b067e64790b9f3> <http://purl.obolibrary.org/obo/IDO_0000664> <http://www.itmat.upenn.edu/biobank/f0d47ff933f08a2ab8343b2410ee2742c4d4e8c1ea8fb2b88c0c7da09a2501ac>""",
"""<http://www.itmat.upenn.edu/biobank/addeae9b224c8f72cb4d1b0105f802d3a45e27ee4f4f446337aae91e08e01ee6> <http://purl.obolibrary.org/obo/IDO_0000664> <http://www.itmat.upenn.edu/biobank/19282628f7e12c41f67c61be1f84045250e0d0bdc9da6799a077a731dea7cc4a>"""
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
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010168_2> <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0010161_1> .
<http://transformunify.org/ontologies/TURBO_0010168_2> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010161_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
<http://transformunify.org/ontologies/TURBO_0010168_2> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
<http://transformunify.org/ontologies/TURBO_0010168_2> <http://transformunify.org/ontologies/TURBO_0010079> "285031059abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010079> "285031059abc"^^xsd:String .
<http://transformunify.org/ontologies/TURBO_0010168_1> rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010282> <http://transformunify.org/ontologies/TURBO_0000505> .
}

            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://transformunify.org/ontologies/TURBO_0010161_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010161>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010168>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010168>""",
"""<http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<http://www.itmat.upenn.edu/biobank/e43e4eb1e4a5c24a18650d2a210a5a4bd8b8591dc788fb65cbfe53d4ec65d0d6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/cdc97b8885d637ff7c6a61a3b778d1c9e5e4ca2421676cc49ac79d1aee88b380> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://transformunify.org/ontologies/TURBO_0010161_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e>""",
"""<http://transformunify.org/ontologies/TURBO_0010168_2> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d>""",
"""<http://www.itmat.upenn.edu/biobank/e43e4eb1e4a5c24a18650d2a210a5a4bd8b8591dc788fb65cbfe53d4ec65d0d6> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e>""",
"""<http://www.itmat.upenn.edu/biobank/cdc97b8885d637ff7c6a61a3b778d1c9e5e4ca2421676cc49ac79d1aee88b380> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d>""",
"""<http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/d2cb8e8e3142e1e01210c1ad91d2b26f00c6a4303d46c0c2d578d31595851594>""",
"""<http://www.itmat.upenn.edu/biobank/81087169b82aec41928fd72f8db1df2ff2a6b2ed1d662702a249a50bec9a235e> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000505>""",
"""<http://www.itmat.upenn.edu/biobank/fc9362612797956035bd820adac792480ca5f378335e13d22bc04e100193786d> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0000505>""",
"""<http://www.itmat.upenn.edu/biobank/e43e4eb1e4a5c24a18650d2a210a5a4bd8b8591dc788fb65cbfe53d4ec65d0d6> <http://transformunify.org/ontologies/TURBO_0010094> "285031059abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/cdc97b8885d637ff7c6a61a3b778d1c9e5e4ca2421676cc49ac79d1aee88b380> <http://transformunify.org/ontologies/TURBO_0010094> "285031059abc"^^<http://www.w3.org/2001/XMLSchema#String>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}