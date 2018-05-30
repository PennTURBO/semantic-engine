package edu.upenn.turbo

import java.io.FileOutputStream
import java.net.URL
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.scalatest.BeforeAndAfter
import org.scalatest._

class LofExpGrayTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  /*1. I would not hardcode "false" as a parameter into the delete triples method, because this won't work when run in conjunction with other tests. 
  I get this might be useful for developing/debugging at this point however, but once the test is complete I would change it. 
  
2. I would put the line of code that clears all triples in the database in your "before" method. 

--- done ---

3. instead of including the prefixes on every sparql insert statement, you can just say "sparqlPrefixes + query" in your call to the update method, this is a global variable. 
  You can even go into projectwide globals and add prefixes to the list if you want. 
*/

  // graphs

  /*
	http://www.itmat.upenn.edu/biobank/Conclusionations20180406133311 	
	http://www.itmat.upenn.edu/biobank/ICD10Ontology 	
	http://www.itmat.upenn.edu/biobank/ICD9Ontology 	
	http://www.itmat.upenn.edu/biobank/chebi_dron_eqilabs 	
	http://www.itmat.upenn.edu/biobank/chebilite 	
	http://www.itmat.upenn.edu/biobank/diag2disease
	http://www.itmat.upenn.edu/biobank/drugOntologies 	
	http://www.itmat.upenn.edu/biobank/entityLinkData 	
	http://www.itmat.upenn.edu/biobank/expanded 	
	http://www.itmat.upenn.edu/biobank/inverses 	
	http://www.itmat.upenn.edu/biobank/mondoOntology 	
	http://www.itmat.upenn.edu/biobank/ontology 	
*/

  val expand: Expander = new Expander()
  val connect: ConnectToGraphDB = new ConnectToGraphDB()
  //  val sparqlChecks = new DrivetrainSparqlChecks()
  //  val postchecks = new SparqlPostExpansionChecks()
  var cxn: RepositoryConnection = null
  var repoManager: RemoteRepositoryManager = null
  var repository: Repository = null

  //  var 

  //  //this is the flag of whether to keep or delete triples after the run
  //  val deleteTriplesOrDatabaseFileAfterRun: Boolean = false

  before {
    val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
    cxn = graphDBMaterials.getConnection()
    repoManager = graphDBMaterials.getRepoManager()
    repository = graphDBMaterials.getRepository()
  }

  after {
    //Setting "false" here will cause triples to stay in database after termination of the test
    connect.closeGraphConnection(cxn, repoManager, repository, false)
  }

  test("placeholder / stub test") {

    // the last karma instantiation didn't include the anyURi tag on the TURBO_0007609 object

    val graphString = "http://http://www.itmat.upenn.edu/biobank/oneLofScBlock"

    cxn.prepareUpdate(QueryLanguage.SPARQL, "clear all").execute()

    var HelperInstance = new Helper4SparqlTests
    var initialStmtCt = HelperInstance.CountTriples(cxn, false)

    // some lof shortcuts
    var UpdateStatement = sparqlPrefixes + """
      insert data {
      graph <""" + graphString + """> {
pmbb:b78d_10bf rdf:type obo:OBI_0001352 .
pmbb:b78d_10bf turbo:TURBO_0007601 "annono-enc" .
pmbb:b78d_10bf turbo:TURBO_0007609 "http://transformunify.org/ontologies/TURBO_0000422"^^xsd:anyURI .
pmbb:b78d_10bf turbo:TURBO_0007604 "http://purl.obolibrary.org/obo/PR_O43657"^^xsd:anyURI .
pmbb:b78d_10bf turbo:TURBO_0007608 "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
pmbb:b78d_10bf turbo:TURBO_0007605 "TSPAN6(ENSG00000000003)" .
pmbb:b78d_10bf turbo:TURBO_0007602 "annono-geno_id" .
pmbb:b78d_10bf turbo:TURBO_0007606 "1" .
pmbb:b78d_10bf turbo:TURBO_0007607 "http://transformunify.org/ontologies/TURBO_0000591"^^xsd:anyURI .
pmbb:b78d_10bf turbo:TURBO_0007603 "http://transformunify.org/ontologies/TURBO_0000451"^^xsd:anyURI .
      }}
      """

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()

    // background statements about encounter and consenter, for testing post-expansion liking
    UpdateStatement = sparqlPrefixes + """
      insert data {
      graph <http://www.itmat.upenn.edu/biobank/expanded> {
pmbb:8002b326-1d4e-42a7-93ec-b6f9a1aaa9a3 rdf:type turbo:TURBO_0000527 .
pmbb:9d908250-29a4-4583-8d8b-65fabc910eee obo:IAO_0000219 pmbb:8002b326-1d4e-42a7-93ec-b6f9a1aaa9a3 .
pmbb:9d908250-29a4-4583-8d8b-65fabc910eee rdf:type turbo:TURBO_0000533 .
pmbb:b4f5872d-1576-468f-97d9-ecf1519d8ff7 obo:BFO_0000050 pmbb:9d908250-29a4-4583-8d8b-65fabc910eee .
pmbb:b4f5872d-1576-468f-97d9-ecf1519d8ff7 turbo:TURBO_0006510 "annono-enc" .
pmbb:b4f5872d-1576-468f-97d9-ecf1519d8ff7 rdf:type turbo:TURBO_0000534 .
pmbb:c08e6d55-4f94-4590-96e4-185ec8a81ddd obo:RO_0000056 pmbb:8002b326-1d4e-42a7-93ec-b6f9a1aaa9a3 .
pmbb:c08e6d55-4f94-4590-96e4-185ec8a81ddd rdf:type turbo:TURBO_0000502 .
pmbb:c509cf26-afed-461c-89e0-deed241bd519 obo:BFO_0000050 pmbb:9d908250-29a4-4583-8d8b-65fabc910eee .
# pmbb:c509cf26-afed-461c-89e0-deed241bd519 obo:IAO_0000219 turbo:TURBO_0000422 .
pmbb:c509cf26-afed-461c-89e0-deed241bd519 rdf:type turbo:TURBO_0000535 .
pmbb:b4f5872d-1576-468f-97d9-ecf1519d8ff7 obo:BFO_0000050 pmbb:8d5b5560-d488-42c4-9dbd-82a9d8b05a11 .
pmbb:8d5b5560-d488-42c4-9dbd-82a9d8b05a11 <http://purl.org/dc/elements/1.1/title> "non-lof data" .
pmbb:8d5b5560-d488-42c4-9dbd-82a9d8b05a11 rdf:type obo:IAO_0000100 .
      }}
      """

    cxn.prepareUpdate(QueryLanguage.SPARQL, UpdateStatement).execute()

    var subsequentStmtCt = HelperInstance.CountTriples(cxn, false)

    //    logger.warn("postcount: " + subsequentStmtCt.toString)

    // are there more triples in the database after the insert?
    //    assert(subsequentStmtCt > initialStmtCt, "doesn't look like any triples were added")

    val expInst = new Expander
    val helper = new TurboMultiuseClass
    val entLinker = new EntityLinker
    val instantiation: IRI = helper.genPmbbIRI(cxn)

    expInst.expandLossOfFunctionShortcuts(cxn, instantiation, "<" + graphString + ">")

    entLinker.connectLossOfFunctionToBiobankEncounters(cxn)

    var askStr = """
   ask {
    ?alleleInf <http://purl.obolibrary.org/obo/BFO_0000050> ?dataset .
    ?alleleInf <http://purl.obolibrary.org/obo/IAO_0000136> ?dna .
    ?alleleInf <http://purl.obolibrary.org/obo/IAO_0000142> <http://purl.obolibrary.org/obo/PR_O43657> .
    ?alleleInf <http://purl.obolibrary.org/obo/OBI_0001938> <http://transformunify.org/ontologies/TURBO_0000591> .
    ?alleleInf <http://transformunify.org/ontologies/TURBO_0006512> "TSPAN6(ENSG00000000003)" .
    ?alleleInf <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
    ?dataXformProc <http://purl.obolibrary.org/obo/OBI_0000293> ?seqData .
    ?dataXformProc <http://purl.obolibrary.org/obo/OBI_0000299> ?alleleInf .
    ?dataXformProc <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0200000> .
    ?dataset <http://purl.org/dc/elements/1.1/title> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
    ?dataset <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/IAO_0000100> .
    ?dna <http://purl.obolibrary.org/obo/BFO_0000050> ?specimen .
    ?dna <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/CHEBI_16991> .
    ?exSeqProc <http://purl.obolibrary.org/obo/OBI_0000293> ?extract .
    ?exSeqProc <http://purl.obolibrary.org/obo/OBI_0000299> ?seqData .
    ?exSeqProc <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0002118> .
    ?extrProc <http://purl.obolibrary.org/obo/OBI_0000293> ?specimen .
    ?extrProc <http://purl.obolibrary.org/obo/OBI_0000299> ?extract .
    ?extrProc <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0000257> .
    ?extract <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001051> .
    ?genoCridRegDen <http://purl.obolibrary.org/obo/BFO_0000050> ?specCrid .
    ?genoCridRegDen <http://purl.obolibrary.org/obo/IAO_0000219> <http://transformunify.org/ontologies/TURBO_0000451> .
    ?genoCridRegDen <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000567> .
    ?sampCollProc <http://purl.obolibrary.org/obo/OBI_0000299> ?specimen .
    ?sampCollProc <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0600005> .
    ?seqData <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001573> .
    ?specCrid <http://purl.obolibrary.org/obo/IAO_0000219> ?specimen .
    ?specCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000566> .
    ?specCridSymb <http://purl.obolibrary.org/obo/BFO_0000050> ?dataset .
    ?specCridSymb <http://purl.obolibrary.org/obo/BFO_0000050> ?specCrid .
    ?specCridSymb <http://transformunify.org/ontologies/TURBO_0006510> "annono-geno_id" .
    ?specCridSymb <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000568> .
    ?specimen <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001479> .
    # nonsense triple for demonstrating what a false result would look like
    # ?specimen ?specimen ?specimen  .
}
"""

    var testRes = tinySparqlTest(askStr, "lof expansion failed", true)
    
    // not sure what's require here regarding teh abckgorund data
    // wrong named graph?
    // need to inverse some of teh props?

    askStr = """
   ask {
?dna <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/CHEBI_16991> .
?dna <http://purl.obolibrary.org/obo/BFO_0000050> ?consenter .
?sampCollProc <http://purl.obolibrary.org/obo/OBI_0000293> ?consenter .
?sampCollProc <http://purl.obolibrary.org/obo/BFO_0000050> ?bbEnc .
?bbEnc a turbo:TURBO_0000527 .
}
"""

    testRes = tinySparqlTest(askStr, "post-expansion LOF-encounter linking failed", true)

    /*
-/+ zygosity value specification
no, it is not part of a dataset and it does not have a specified value... update full model?!
looks like Hayden correctly omitted the zvs part-of data-set triple
do need to say that the allele info has two textual (TURBO_0006512) or literal (TURBO_0006510) values?  (the mentioned gene/protein and the numerical zygoisty value)

triples from full instantiation:
<http://transformunify.org/ontologies/TURBO_0000591> <http://purl.obolibrary.org/obo/BFO_0000050> ?dataset .
<http://transformunify.org/ontologies/TURBO_0000591> <http://transformunify.org/ontologies/TURBO_0006512> "1" .

ask { 
	?s <http://transformunify.org/ontologies/TURBO_0006512> "1"
}

*/
    
// underspecified test to see if the numerical zygosity value specification has been bound anywhere
    askStr = """
ask {
?s <http://transformunify.org/ontologies/TURBO_0006512>|<http://transformunify.org/ontologies/TURBO_0006510> "1"
}
"""

    testRes = tinySparqlTest(askStr, "the numerical zygosity value specification has noy been bound anywhere", true)

  }

  def tinySparqlTest(query: String, failureMsg: String, expectedVal: Boolean) = {
    // probably should write a separate function for when the expected result is false
    val PrefixedQuery = sparqlPrefixes + query
    val expectationQuery = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, PrefixedQuery)
    val AskRes = expectationQuery.evaluate()
    val TidyMsg = "..." + failureMsg
    assert(AskRes == expectedVal, TidyMsg)

    // return X
  }

}