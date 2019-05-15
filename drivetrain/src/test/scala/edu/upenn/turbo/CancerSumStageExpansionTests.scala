package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class CancerSumStageExpansionTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals {

  val clearTestingRepositoryAfterRun: Boolean = true
  
  RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
  RunDrivetrainProcess.setInstantiation("http://www.itmat.upenn.edu/biobank/test_instantiation_1")

  before
  {
      graphDBMaterials = ConnectToGraphDB.initializeGraphLoadData(false)
      testCxn = graphDBMaterials.getTestConnection()
      gmCxn = graphDBMaterials.getGmConnection()
      testRepoManager = graphDBMaterials.getTestRepoManager()
      testRepository = graphDBMaterials.getTestRepository()
      helper.deleteAllTriplesInDatabase(testCxn)
      
      RunDrivetrainProcess.setGraphModelConnection(gmCxn)
      RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
  }
  after
  {
      ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
  }

  test("monolithic") {
    
    // the fact that the patient registry is of a particular type
    // should come from the ontology, not from data triples

    val insertScStmt: String = """
  insert data { graph pmbb:Shortcuts_sumStage { 
  <http://www.itmat.upenn.edu/biobank/TURBO_0010039_1> a <http://transformunify.org/ontologies/TURBO_0010039>;
    <http://transformunify.org/ontologies/TURBO_0010042> "IIIb";
    <http://transformunify.org/ontologies/TURBO_0010043> "HUP";
    <http://transformunify.org/ontologies/TURBO_0010044> "http://transformunify.org/ontologies/TURBO_0000410"^^xsd:anyURI;
    <http://transformunify.org/ontologies/TURBO_0010045> "7db6ef12" . }}
      """

    update.updateSparql(testCxn, insertScStmt)

    val insertCheatStmt: String = """
        insert data { graph pmbb:expanded { 
            <http://www.itmat.upenn.edu/biobank/IAO_0000100_1> a obo:IAO_0000100 .
            
            <http://www.itmat.upenn.edu/biobank/af2e789-529b-41bf-93d2-57beefeff91e> a obo:OGMS_0000063;
              obo:BFO_0000055 <http://www.itmat.upenn.edu/biobank/ed5ab769-2f94-4c69-91ed-e0456a728909> .
            
            <http://www.itmat.upenn.edu/biobank/ed5ab769-2f94-4c69-91ed-e0456a728909> a obo:OGMS_0000031;
              obo:RO_0000052 <http://www.itmat.upenn.edu/biobank/patient>;
              obo:RO_0004026 <http://www.itmat.upenn.edu/biobank/UBERON_0000479_2> .
            
            <http://www.itmat.upenn.edu/biobank/OBI_0200000_1> a obo:OBI_0200000;
              obo:OBI_0000299 <http://www.itmat.upenn.edu/biobank/IAO_0000100_1> .
            
            <http://www.itmat.upenn.edu/biobank/BFO_0000035_7c93892a> a obo:BFO_0000035;
              obo:RO_0002223 <http://www.itmat.upenn.edu/biobank/OBI_0200000_1> .
            
            <http://www.itmat.upenn.edu/biobank/IAO_0000416_1> a turbo:TURBO_0000530;
              obo:IAO_0000136 <http://www.itmat.upenn.edu/biobank/BFO_0000035_7c93892a> .
            
            turbo:TURBO_0000410 a turbo:TURBO_0001500 .
            
            <http://www.itmat.upenn.edu/biobank/gdbMatEnt> a obo:BFO_0000040 .
            
            <http://www.itmat.upenn.edu/biobank/patient> a obo:NCBITaxon_9606;
              obo:RO_0000056 <http://www.itmat.upenn.edu/biobank/af2e789-529b-41bf-93d2-57beefeff91e> .
            
            <http://www.itmat.upenn.edu/biobank/consenter_crid1> a turbo:TURBO_0000503;
              obo:IAO_0000219 <http://www.itmat.upenn.edu/biobank/patient> .
            
            <http://www.itmat.upenn.edu/biobank/crid_sym1> a turbo:TURBO_0000504;
              obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/IAO_0000100_1>, <http://www.itmat.upenn.edu/biobank/consenter_crid1>;
              turbo:TURBO_0010094 "7db6ef12" .
            
            <http://www.itmat.upenn.edu/biobank/regden1> a turbo:TURBO_0000505;
              obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/IAO_0000100_1>, <http://www.itmat.upenn.edu/biobank/consenter_crid1>;
              obo:IAO_0000219 turbo:TURBO_0000410 .
            
            <http://www.itmat.upenn.edu/biobank/UBERON_0000479_2> a obo:UBERON_0002367;
              obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/patient> .
            
            <http://www.itmat.upenn.edu/biobank/gdbAssay> a obo:OBI_0000070;
              obo:OBI_0000293 <http://www.itmat.upenn.edu/biobank/gdbMatEnt>;
              obo:OBI_0000299 <http://www.itmat.upenn.edu/biobank/gdbDataItem> .
            
            <http://www.itmat.upenn.edu/biobank/gdbDataItem> a obo:IAO_0000027 .
            
            <http://www.itmat.upenn.edu/biobank/TURBO_0010040_1> a turbo:TURBO_0010040;
              obo:BFO_0000055 <http://www.itmat.upenn.edu/biobank/OMRSE_00000012_05fabf92>;
              obo:OBI_0000293 <http://www.itmat.upenn.edu/biobank/gdbDataItem>;
              obo:OBI_0000299 <http://www.itmat.upenn.edu/biobank/TURBO_0010039_1> .
            
            <http://www.itmat.upenn.edu/biobank/d5d8c255> a obo:BFO_0000035;
              obo:RO_0002223 <http://www.itmat.upenn.edu/biobank/TURBO_0010040_1> .
            
            <http://www.itmat.upenn.edu/biobank/be8bfaff> a turbo:TURBO_0000530;
              obo:IAO_0000136 <http://www.itmat.upenn.edu/biobank/d5d8c255> .
            
            <http://www.itmat.upenn.edu/biobank/NCBITaxon_9606_dd6847d7> a obo:NCBITaxon_9606;
              obo:RO_0000056 <http://www.itmat.upenn.edu/biobank/TURBO_0010040_1>;
              obo:RO_0000087 <http://www.itmat.upenn.edu/biobank/OMRSE_00000012_05fabf92> .
            
            <http://www.itmat.upenn.edu/biobank/OMRSE_00000012_05fabf92> a obo:OMRSE_00000012 .
            
            <http://www.itmat.upenn.edu/biobank/TURBO_0010039_1> a turbo:TURBO_0010039;
              obo:BFO_0000050 <http://www.itmat.upenn.edu/biobank/IAO_0000100_1>;
              obo:IAO_0000136 <http://www.itmat.upenn.edu/biobank/af2e789-529b-41bf-93d2-57beefeff91e>;
              turbo:TURBO_0010094 "IIIb" . }}
      """

    update.updateSparql(testCxn, insertCheatStmt)

    val monolithicAsk: String = """
      ask {
          graph pmbb:expanded
          {
              ?dataSet rdf:type obo:IAO_0000100 .
              ?disCourseProc obo:BFO_0000055 ?diseaseDisp .
              ?disCourseProc rdf:type obo:OGMS_0000063 .
              ?diseaseDisp obo:RO_0000052 ?patient .
              ?diseaseDisp obo:RO_0004026 ?patientsProstate .
              ?diseaseDisp rdf:type obo:OGMS_0000031 .
              ?etl obo:OBI_0000299 ?dataSet .
              ?etl rdf:type obo:OBI_0200000 .
              ?etlStart obo:RO_0002223 ?etl .
              ?etlStart rdf:type obo:BFO_0000035 .
              ?etlStartDate obo:IAO_0000136 ?etlStart .
              ?etlStartDate rdf:type turbo:TURBO_0000530 .
              ?hupMrnRegistry rdf:type turbo:TURBO_0001500 .
              ?matEnt rdf:type obo:BFO_0000040 .
              ?patient obo:RO_0000056 ?disCourseProc .
              ?patient rdf:type obo:NCBITaxon_9606 .
              ?patientCrid obo:IAO_0000219 ?patient .
              ?patientCrid rdf:type turbo:TURBO_0000503 .
              ?patientCridSym obo:BFO_0000050 ?dataSet .
              ?patientCridSym obo:BFO_0000050 ?patientCrid .
              ?patientCridSym rdf:type turbo:TURBO_0000504 .
              ?patientCridSym turbo:TURBO_0010094 "7db6ef12" .
              ?patientRegDen obo:BFO_0000050 ?dataSet .
              ?patientRegDen obo:BFO_0000050 ?patientCrid .
              ?patientRegDen obo:IAO_0000219 ?hupMrnRegistry .
              ?patientRegDen rdf:type turbo:TURBO_0000505 .
              ?patientsProstate obo:BFO_0000050 ?patient .
              ?patientsProstate rdf:type obo:UBERON_0002367 .
              ?preStagingAssay obo:OBI_0000293 ?matEnt .
              ?preStagingAssay obo:OBI_0000299 ?stagProcInpDat .
              ?preStagingAssay rdf:type obo:OBI_0000070 .
              ?stagProcInpDat rdf:type obo:IAO_0000027 .
              ?sumStageConcDrawProc obo:BFO_0000055 ?sumStageDrawerHcpRole .
              ?sumStageConcDrawProc obo:OBI_0000293 ?stagProcInpDat .
              ?sumStageConcDrawProc obo:OBI_0000299 ?sumStageTextEnt .
              ?sumStageConcDrawProc rdf:type turbo:TURBO_0010040 .
              ?sumStageConcDrawProcStart obo:RO_0002223 ?sumStageConcDrawProc .
              ?sumStageConcDrawProcStart rdf:type obo:BFO_0000035 .
              ?sumStageConcDrawProcStartDate obo:IAO_0000136 ?sumStageConcDrawProcStart .
              ?sumStageConcDrawProcStartDate rdf:type turbo:TURBO_0000530 .
              ?sumStageDrawer obo:RO_0000056 ?sumStageConcDrawProc .
              ?sumStageDrawer obo:RO_0000087 ?sumStageDrawerHcpRole .
              ?sumStageDrawer rdf:type obo:NCBITaxon_9606 .
              ?sumStageDrawerHcpRole rdf:type obo:OMRSE_00000012 .
              ?sumStageTextEnt obo:BFO_0000050 ?dataSet .
              ?sumStageTextEnt obo:IAO_0000136 ?disCourseProc .
              ?sumStageTextEnt  turbo:TURBO_0010094	"IIIb" .
              ?sumStageTextEnt rdf:type turbo:TURBO_0010039 .
          }
      }
      """

    var askTruth = false

    askTruth = update.querySparqlBoolean(testCxn, monolithicAsk).get

    assert(askTruth == true, "monolithic failure")
    
  }

}