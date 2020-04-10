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
class PatientsSnapshotTest_post_icbo_synthea_omop_cnp_transformation_instructions extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {
val clearTestingRepositoryAfterRun: Boolean = false

override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "post_icbo_synthea_omop_cnp_transformation_instructions.ttl")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    val UUIDKey = "d03ac348f406425fbc322ad88f2aedaf"
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
<http://api.stardog.com/person_1> <http://api.stardog.com/person#person_source_value> "285031059abc"^^xsd:String .
<http://api.stardog.com/person_1> <http://api.stardog.com/person#birth_datetime> "65/25/19"^^xsd:Date .
<http://api.stardog.com/person_1> rdf:type <http://api.stardog.com/person> .
<http://api.stardog.com/person_1> <http://api.stardog.com/person#person_id> "2144189648abc"^^xsd:String .
<http://api.stardog.com/person_1> <http://api.stardog.com/person#race_concept_id> "1543800849abc"^^xsd:String .
<http://api.stardog.com/person_1> <http://api.stardog.com/person#gender_concept_id> "708384193abc"^^xsd:String .
}
GRAPH <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl> {
<http://purl.obolibrary.org/obo/BFO_0000001> <http://transformunify.org/ontologies/TURBO_0010147> "1543800849abc"^^xsd:String .
<http://purl.obolibrary.org/obo/BFO_0000001> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/OMRSE_00000133> .
<http://purl.obolibrary.org/obo/BFO_0000001> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://purl.obolibrary.org/obo/OMRSE_00000098> .
<http://purl.obolibrary.org/obo/BFO_0000001> <http://transformunify.org/ontologies/TURBO_0010147> "708384193abc"^^xsd:String .
}

                   # Optional triples
                   
            }
        """
update.updateSparql(cxn, insertInputDataset)


RunDrivetrainProcess.runProcess("https://github.com/PennTURBO/Drivetrain/Patients")
val query: String = s"SELECT * WHERE {GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, query, Array("s", "p", "o"))
val resArr = new ArrayBuffer[String]
for (index <- 0 to result.size-1) 
{
    if (!result(index)(2).isInstanceOf[Literal]) resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> <"+result(index)(2)+">"
    else resArr += "<"+result(index)(0)+"> <"+result(index)(1)+"> "+result(index)(2)
}



val checkTriples = Array(
"""<http://api.stardog.com/person_1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://api.stardog.com/person>""",
"""<http://www.itmat.upenn.edu/biobank/f41eb05dce7dda0322b9cbda4f8c21e8865cd10110e7ba819f696b78b14b761a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/NCBITaxon_9606>""",
"""<http://www.itmat.upenn.edu/biobank/6ad75ed7a10d4732f988fef72726c9b1619992fd1327cc60ef9183c92e67d58f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/BFO_0000001>""",
"""<http://www.itmat.upenn.edu/biobank/a24bc8a5628030cd081b8da55f870a89afabf03abcf943071ebb50fcbdc46829> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000578>""",
"""<http://www.itmat.upenn.edu/biobank/b7806bc4a9ddf75783ab0d044d0f7618ae91baa0c95fdbac089a0e9ea5b2bbff> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.ebi.ac.uk/efo/EFO_0004950>""",
"""<http://www.itmat.upenn.edu/biobank/835d036bec0a648afcc605001eaade4c3150c54d62c1541b11f639a1744e9ec8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/UBERON_0035946>""",
"""<http://www.itmat.upenn.edu/biobank/87a66a5e9e2be6436f8fed5e9e45908eb9cee00d42b7bdf0bcb7f8462a05101d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://www.itmat.upenn.edu/biobank/d25d84cc5dceba0550ae4ba9d9ac3977361ba3b7572d627d5bcd791dc62d720b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000093>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0010433>""",
"""<http://www.itmat.upenn.edu/biobank/596e5ece8eae82c25bd745717c3a6985ed3a997c0a9bd08014bf264880362990> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000028>""",
"""<http://api.stardog.com/person_1> <http://transformunify.org/ontologies/TURBO_0010113> <http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e>""",
"""<http://www.itmat.upenn.edu/biobank/f41eb05dce7dda0322b9cbda4f8c21e8865cd10110e7ba819f696b78b14b761a> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e>""",
"""<http://www.itmat.upenn.edu/biobank/6ad75ed7a10d4732f988fef72726c9b1619992fd1327cc60ef9183c92e67d58f> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e>""",
"""<http://www.itmat.upenn.edu/biobank/b7806bc4a9ddf75783ab0d044d0f7618ae91baa0c95fdbac089a0e9ea5b2bbff> <http://purl.obolibrary.org/obo/IAO_0000136> <http://www.itmat.upenn.edu/biobank/835d036bec0a648afcc605001eaade4c3150c54d62c1541b11f639a1744e9ec8>""",
"""<http://www.itmat.upenn.edu/biobank/f41eb05dce7dda0322b9cbda4f8c21e8865cd10110e7ba819f696b78b14b761a> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010431>""",
"""<http://www.itmat.upenn.edu/biobank/6ad75ed7a10d4732f988fef72726c9b1619992fd1327cc60ef9183c92e67d58f> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010441>""",
"""<http://www.itmat.upenn.edu/biobank/b7806bc4a9ddf75783ab0d044d0f7618ae91baa0c95fdbac089a0e9ea5b2bbff> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010443>""",
"""<http://www.itmat.upenn.edu/biobank/87a66a5e9e2be6436f8fed5e9e45908eb9cee00d42b7bdf0bcb7f8462a05101d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010444>""",
"""<http://www.itmat.upenn.edu/biobank/87a66a5e9e2be6436f8fed5e9e45908eb9cee00d42b7bdf0bcb7f8462a05101d> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/a24bc8a5628030cd081b8da55f870a89afabf03abcf943071ebb50fcbdc46829>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/BFO_0000050> <http://transformunify.org/ontologies/TURBO_0010442>""",
"""<http://www.itmat.upenn.edu/biobank/596e5ece8eae82c25bd745717c3a6985ed3a997c0a9bd08014bf264880362990> <http://purl.obolibrary.org/obo/BFO_0000050> <http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4>""",
"""<http://www.itmat.upenn.edu/biobank/a24bc8a5628030cd081b8da55f870a89afabf03abcf943071ebb50fcbdc46829> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/f41eb05dce7dda0322b9cbda4f8c21e8865cd10110e7ba819f696b78b14b761a>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/6ad75ed7a10d4732f988fef72726c9b1619992fd1327cc60ef9183c92e67d58f>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/b7806bc4a9ddf75783ab0d044d0f7618ae91baa0c95fdbac089a0e9ea5b2bbff>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/IAO_0000219> <http://www.itmat.upenn.edu/biobank/87a66a5e9e2be6436f8fed5e9e45908eb9cee00d42b7bdf0bcb7f8462a05101d>""",
"""<http://www.itmat.upenn.edu/biobank/a24bc8a5628030cd081b8da55f870a89afabf03abcf943071ebb50fcbdc46829> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010255>""",
"""<http://www.itmat.upenn.edu/biobank/4828e3c6fe41941b283d2454e328c4eedd13fc1d06c537718ec1d76cc32d78e4> <http://purl.obolibrary.org/obo/BFO_0000051> <http://transformunify.org/ontologies/TURBO_0010396>""",
"""<http://www.itmat.upenn.edu/biobank/f41eb05dce7dda0322b9cbda4f8c21e8865cd10110e7ba819f696b78b14b761a> <http://transformunify.org/ontologies/TURBO_0010094> "708384193abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/6ad75ed7a10d4732f988fef72726c9b1619992fd1327cc60ef9183c92e67d58f> <http://transformunify.org/ontologies/TURBO_0010094> "1543800849abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/87a66a5e9e2be6436f8fed5e9e45908eb9cee00d42b7bdf0bcb7f8462a05101d> <http://transformunify.org/ontologies/TURBO_0010094> "285031059abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/596e5ece8eae82c25bd745717c3a6985ed3a997c0a9bd08014bf264880362990> <http://transformunify.org/ontologies/TURBO_0010094> "2144189648abc"^^<http://www.w3.org/2001/XMLSchema#String>""",
"""<http://www.itmat.upenn.edu/biobank/b7806bc4a9ddf75783ab0d044d0f7618ae91baa0c95fdbac089a0e9ea5b2bbff> <http://purl.obolibrary.org/obo/IAO_0000004> "65/25/19"^^<http://www.w3.org/2001/XMLSchema#Date>""",
"""<http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e> <http://transformunify.org/ontologies/TURBO_0000303> <http://www.itmat.upenn.edu/biobank/835d036bec0a648afcc605001eaade4c3150c54d62c1541b11f639a1744e9ec8>""",
"""<http://www.itmat.upenn.edu/biobank/2b489fd8c33edddcdbe15b187644cdc8740e549ce279913707e3fccdb1b9937e> <http://purl.obolibrary.org/obo/RO_0000087> <http://www.itmat.upenn.edu/biobank/d25d84cc5dceba0550ae4ba9d9ac3977361ba3b7572d627d5bcd791dc62d720b>"""
)

helper.checkStringArraysForEquivalency(checkTriples, resArr.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkTriples.size)

 }}