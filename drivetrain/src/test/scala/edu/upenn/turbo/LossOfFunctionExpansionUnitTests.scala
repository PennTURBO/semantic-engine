package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID

class LossOfFunctionExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val clearTestingRepositoryAfterRun: Boolean = false

    DrivetrainProcessFromGraphModel.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    DrivetrainProcessFromGraphModel.setInstantiation("http://www.itmat.upenn.edu/biobank/test_instantiation_1")
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphLoadData(false)
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        testRepoManager = graphDBMaterials.getTestRepoManager()
        testRepository = graphDBMaterials.getTestRepository()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        DrivetrainProcessFromGraphModel.setGraphModelConnection(gmCxn)
        DrivetrainProcessFromGraphModel.setOutputRepositoryConnection(testCxn)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
  
    test("single allele expansion")
    {
        val insert: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:part1 a obo:NCBITaxon_9606 .
                  pmbb:part1 obo:RO_0000056 pmbb:bbenc1 .
                  pmbb:bbenc1 a turbo:TURBO_0000527 .
                  pmbb:shortcutBbEnc1 a turbo:shortcut_turbo_TURBO_0000527 .
                  pmbb:shortcutBbEnc1 turbo:TURBO_0010113 ?bbenc1 .
              }
              graph pmbb:Shortcuts_LofShortcuts
              {
                  pmbb:allele1 a turbo:shortcut_obo_OBI_0001352 .
                  pmbb:allele1 turbo:TURBO_0007605 "ERLEC1(ENSG00000068912)"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0007602 "UPENN_UPENN10000047_someDigits"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0007607 turbo:TURBO_0000590 .
                  pmbb:allele1 turbo:TURBO_0010095 "2"^^xsd:String .
                  pmbb:allele1 turbo:TURBO_0010142 "http://www.itmat.upenn.edu/biobank/shortcutBbEnc1"^^xsd:anyURI .
                  pmbb:allele1 turbo:TURBO_0007608 "lof_data_from_tests"^^xsd:String .
              }
          }
          
          """
        
        // expand here (process doesn't exists yet)
        // DrivetrainProcessFromGraphModel.runProcess("http://transformunify.org/ontologies/lossOfFunctionExpansionProcess")
        
        val output: String = """
          
          ASK
          {
              Graph pmbb:expanded
              {
                  ?allele a obo:OBI_0001352 .
                  
                  ?allele obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?allele .
                  ?genomeCridSymb obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?genomeCridSymb .
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc:title "lof_data_from_tests"^^xsd:String .
                  
                  ?allele obo:IAO_0000142 <http://rdf.ebi.ac.uk/resource/ensembl/ENSG00000068912> .
                  ?allele turbo:TURBO_0010016 "ERLEC1"^^xsd:String .
                  ?allele turbo:TURBO_0010015 "ENSG00000068912"^^xsd:String .
                  ?allele obo:OBI_0001938 turbo:TURBO_0000590 .
                  turbo:TURBO_0000590 a turbo:TURBO_0000571 .
                  ?allele turbo:TURBO_0010095 "2"^^xsd:String .
                  
                  ?formProcess a obo:OBI_0200000 .
                  ?formProcess obo:OBI_0000299 ?allele .
                  ?formProcess obo:OBI_0000293 ?sequenceData .
                  ?sequenceData a obo:OBI_0001573 .
                  
                  ?allele obo:IAO_0000136 ?DNA .
                  ?DNA a obo:OBI_0001868 .
                  ?DNA obo:OGG_0000000014 pmbb:part1 .
                  
                  ?DNA obo:OBI_0000643 ?DNAextract .
                  ?DNAextract a obo:OBI_0001051 .
                  ?DNAextractionProcess a obo:OBI_0000257 .
                  ?DNAextractionProcess obo:OBI_0000299 ?DNAextract .
                  ?DNAextractionProcess obo:OBI_0000293 ?specimen .
                  
                  ?exomeSequenceProcess a obo:OBI_0002118 .
                  ?exomeSequenceProcess obo:OBI_0000293 ?DNAextract .
                  ?exomeSequenceProcess obo:OBI_0000299 ?sequenceData .
                  
                  ?specimen a obo:OBI_0001479 .
                  ?collectionProcess a obo:OBI_0600005 .
                  ?collectionProcess obo:OBI_0000299 ?specimen .
                  ?collectionProcess obo:BFO_0000050 pmbb:bbenc1 .
                  pmbb:bbenc1 obo:BFO_0000051 ?collectionProcess .
                  ?collectionProcess obo:OBI_0000293 pmbb:part1 .
                  
                  ?genomeCrid a turbo:TURBO_0000566 .
                  ?genomeCrid obo:IAO_0000219 ?specimen .
                  ?genomeCrid obo:BFO_0000051 ?genomeCridSymb .
                  ?genomeCridSymb obo:BFO_0000050 ?genomeCrid .
                  ?genomeCridSymb turbo:TURBO_0006510 "UPENN_UPENN10000047_someDigits"^^xsd:String . .
                  ?genomeCridSymb a turbo:TURBO_0000568 .
              
              }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, output).get should be (true)
    }
}