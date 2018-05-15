package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class EntityLinkingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val entLink = new EntityLinker
    
    var conclusionationNamedGraph: IRI = null
    var masterConclusionation: IRI = null
    var masterPlanspec: IRI = null
    var masterPlan: IRI = null
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
        val insert: String = """
            INSERT DATA {GRAPH pmbb:expanded {
            turbo:part1 a turbo:TURBO_0000502 .
            turbo:part1 turbo:TURBO_0006500 'true'^^xsd:boolean .
            turbo:partCrid obo:IAO_0000219 turbo:part1 .
            turbo:partCrid a turbo:TURBO_0000503 .
            turbo:partCrid obo:BFO_0000051 turbo:partSymb .
            turbo:partCrid obo:BFO_0000051 turbo:partRegDen .
            turbo:partSymb turbo:TURBO_0006510 '1' .
            turbo:partSymb a turbo:TURBO_0000504 .
            turbo:partRegDen a turbo:TURBO_0000505 .
            turbo:partRegDen obo:IAO_0000219 turbo:reg1 .
            turbo:reg1 a turbo:TURBO_0000506 .
            turbo:biosex1 a obo:PATO_0000047 .
            turbo:part1 obo:RO_0000086 turbo:biosex1 .
            turbo:biosex1 a obo:UBERON_0035946 .
            turbo:part1 turbo:TURBO_0000303 turbo:birth1 .
            turbo:adipose1 a obo:UBERON_0001013 .
    	      turbo:part1 obo:BFO_0000051 turbo:adipose1 .
            turbo:part1 obo:RO_0000086 turbo:height1 .
        		turbo:height1 a obo:PATO_0000119 .
        		turbo:part1 obo:RO_0000086 turbo:weight1 .
        		turbo:weight1 a obo:PATO_0000128 .
    		
        		turbo:part1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:biosex1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:birth1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:adipose1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:partCrid turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:partSymb turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:partRegDen turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:height1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:weight1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		
        		turbo:hcenc1 a obo:OGMS_0000097 .
            turbo:hcCrid1 obo:IAO_0000219 turbo:hcenc1 .
            turbo:hcCrid1 a turbo:TURBO_0000508 .
            turbo:hcCrid1 obo:BFO_0000051 turbo:hcSymb1 .
            turbo:hcCrid1 obo:BFO_0000051 turbo:hcRegDen1 .
            turbo:hcSymb1 turbo:TURBO_0006510 '1' .
            turbo:hcSymb1 a turbo:TURBO_0000509 .
            turbo:hcRegDen1 a turbo:TURBO_0000510 .
            turbo:hcRegDen1 obo:IAO_0000219 turbo:hcreg1 .
            turbo:hcreg1 a turbo:TURBO_0000513 .
            turbo:inst1 a turbo:TURBO_0000522 .
            turbo:inst1 obo:OBI_0000293 turbo:dataset1 .
            turbo:dataset1 a obo:IAO_0000100 .
            turbo:dataset1 obo:BFO_0000051 turbo:hcenc1ID .
            turbo:encdate1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:ProcStartTimeMeas .
        		turbo:encdate1 turbo:TURBO_0006511 '12/12/1994'^^xsd:date .
        		turbo:encdate1 obo:IAO_0000136 turbo:encstart1 .
        		turbo:encstart1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		turbo:encstart1 obo:RO_0002223 turbo:hcenc1 .
        		turbo:mass1 obo:BFO_0000050 turbo:hcenc1 ;
            		          rdf:type obo:OBI_0000445 ;
            		          obo:OBI_0000299 turbo:massdatum1 .
            turbo:massdatum1 a obo:IAO_0000414.
            turbo:length1 obo:BFO_0000050 turbo:hcenc1 ;
            		          rdf:type turbo:TURBO_0001511 ;
            		          obo:OBI_0000299 turbo:lengthdatum1 .
            turbo:lengthdatum1 a obo:IAO_0000408 .
            turbo:hcenc1 obo:OBI_0000299 turbo:BMI1 .
            turbo:BMI1 a <http://www.ebi.ac.uk/efo/EFO_0004340> .
    		
    		    turbo:hcenc2 a obo:OGMS_0000097 .
            turbo:hcCrid2 obo:IAO_0000219 turbo:hcenc2 .
            turbo:hcCrid2 a turbo:TURBO_0000508 .
            turbo:hcCrid2 obo:BFO_0000051 turbo:hcSymb2 .
            turbo:hcCrid2 obo:BFO_0000051 turbo:hcRegDen2 .
            turbo:hcSymb2 turbo:TURBO_0006510 '2' .
            turbo:hcSymb2 a turbo:TURBO_0000509 .
            turbo:hcRegDen2 a turbo:TURBO_0000510 .
            turbo:hcRegDen2 obo:IAO_0000219 turbo:hcreg1 .
            turbo:hcreg2 a turbo:TURBO_0000513 .
            turbo:inst2 a turbo:TURBO_0000522 .
            turbo:inst2 obo:OBI_0000293 turbo:dataset2 .
            turbo:dataset2 a obo:IAO_0000100 .
            turbo:dataset2 obo:BFO_0000051 turbo:hcenc2ID .
            turbo:encdate2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:ProcStartTimeMeas .
        		turbo:encdate2 turbo:TURBO_0006511 '12/12/1994'^^xsd:date .
        		turbo:encdate2 obo:IAO_0000136 turbo:encstart2 .
        		turbo:encstart2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		turbo:encstart2 obo:RO_0002223 turbo:hcenc2 .
        		turbo:mass2 obo:BFO_0000050 turbo:hcenc2 ;
            		          rdf:type obo:OBI_0000445 ;
            		          obo:OBI_0000299 turbo:massdatum2 .
            turbo:massdatum2 a obo:IAO_0000414.
            turbo:length2 obo:BFO_0000050 turbo:hcenc2 ;
            		          rdf:type turbo:TURBO_0001511 ;
            		          obo:OBI_0000299 turbo:lengthdatum2 .
            turbo:lengthdatum2 a obo:IAO_0000408 .
            turbo:hcenc2 obo:OBI_0000299 turbo:BMI2 .
            turbo:BMI2 a <http://www.ebi.ac.uk/efo/EFO_0004340> .
    		
    		    turbo:bbenc3 a turbo:TURBO_0000527 .
            turbo:bbCrid1 obo:IAO_0000219 turbo:bbenc3 .
            turbo:bbCrid1 a turbo:TURBO_0000533 .
            turbo:bbCrid1 obo:BFO_0000051 turbo:bbSymb1 .
            turbo:bbCrid1 obo:BFO_0000051 turbo:bbRegDen1 .
            turbo:bbSymb1 turbo:TURBO_0006510 '3' .
            turbo:bbSymb1 a turbo:TURBO_0000534 .
            turbo:bbRegDen1 a turbo:TURBO_0000535 .
            turbo:bbRegDen1 obo:IAO_0000219 turbo:bbreg1 .
            turbo:bbreg1 a turbo:TURBO_0000543 .
            turbo:inst3 a turbo:TURBO_0000522 .
            turbo:inst3 obo:OBI_0000293 turbo:dataset3 .
            turbo:dataset3 a obo:IAO_0000100 .
            turbo:dataset3 obo:BFO_0000051 turbo:bbencid3 .
            turbo:encdate3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:ProcStartTimeMeas .
        		turbo:encdate3 turbo:TURBO_0006511 '12/12/1994'^^xsd:date .
        		turbo:encdate3 obo:IAO_0000136 turbo:encstart3 .
        		turbo:encstart3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
        		turbo:encstart3 obo:RO_0002223 turbo:bbenc3 .
        		turbo:mass3 obo:BFO_0000050 turbo:bbenc3 ;
            		          rdf:type obo:OBI_0000445 ;
            		          obo:OBI_0000299 turbo:massdatum3 .
            turbo:massdatum3 a obo:IAO_0000414.
            turbo:length3 obo:BFO_0000050 turbo:bbenc3 ;
            		          rdf:type turbo:TURBO_0001511 ;
            		          obo:OBI_0000299 turbo:lengthdatum3 .
            turbo:lengthdatum3 a obo:IAO_0000408 .
            turbo:bbenc3 obo:OBI_0000299 turbo:BMI3 .
            turbo:BMI3 a <http://www.ebi.ac.uk/efo/EFO_0004340> .
    		
    		    turbo:bbenc4 a turbo:TURBO_0000527 .
            turbo:bbCrid2 obo:IAO_0000219 turbo:bbenc4 .
            turbo:bbCrid2 a turbo:TURBO_0000533 .
            turbo:bbCrid2 obo:BFO_0000051 turbo:bbSymb2 .
            turbo:bbCrid2 obo:BFO_0000051 turbo:bbRegDen2 .
            turbo:bbSymb2 turbo:TURBO_0006510 '4' .
            turbo:bbSymb2 a turbo:TURBO_0000534 .
            turbo:bbRegDen2 a turbo:TURBO_0000535 .
            turbo:bbRegDen2 obo:IAO_0000219 turbo:bbreg2 .
            turbo:bbreg2 a turbo:TURBO_0000543 .
            turbo:inst4 a turbo:TURBO_0000522 .
            turbo:inst4 obo:OBI_0000293 turbo:dataset4 .
            turbo:dataset4 a obo:IAO_0000100 .
            turbo:dataset4 obo:BFO_0000051 turbo:bbencid4 .
            turbo:encdate4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:ProcStartTimeMeas .
        		turbo:encdate4 turbo:TURBO_0006511 '12/12/1994'^^xsd:date .
        		turbo:encdate4 obo:IAO_0000136 turbo:encstart3 .
        		turbo:encstart4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
        		turbo:encstart4 obo:RO_0002223 turbo:bbenc3 .
        		turbo:mass4 obo:BFO_0000050 turbo:bbenc4 ;
            		          rdf:type obo:OBI_0000445 ;
            		          obo:OBI_0000299 turbo:massdatum4 .
            turbo:massdatum4 a obo:IAO_0000414.
            turbo:length4 obo:BFO_0000050 turbo:bbenc4 ;
            		          rdf:type turbo:TURBO_0001511 ;
            		          obo:OBI_0000299 turbo:lengthdatum4 .
            turbo:lengthdatum4 a obo:IAO_0000408 .
            turbo:bbenc4 obo:OBI_0000299 turbo:BMI4 .
            turbo:BMI4 a <http://www.ebi.ac.uk/efo/EFO_0004340> .
    		
        		turbo:hcenc1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcenc2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbenc3 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbenc4 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcCrid1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcCrid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbCrid1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbCrid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcSymb2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbSymb1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbSymb2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:hcRegDen2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbRegDen1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:bbRegDen2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:encdate1 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:encdate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:encdate3 turbo:TURBO_0006500 'true'^^xsd:boolean .
        		turbo:encdate4 turbo:TURBO_0006500 'true'^^xsd:boolean .
    		}}"""
        
            helper.updateSparql(cxn, sparqlPrefixes + insert)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("join 2 hc encs to 1 participant")
    {
        val insert: String = """
        INSERT DATA {GRAPH pmbb:entityLinkData {
            turbo:joinHcEncCrid1 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid1 obo:BFO_0000051 turbo:joinHcEncSymb1 .
            turbo:joinHcEncCrid1 obo:BFO_0000051 turbo:joinHcEncRegDen1 .
            turbo:joinHcEncSymb1 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinHcEncRegDen1 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen1 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:joinHcEncCrid2 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid2 obo:BFO_0000051 turbo:joinHcEncSymb2 .
            turbo:joinHcEncCrid2 obo:BFO_0000051 turbo:joinHcEncRegDen2 .
            turbo:joinHcEncSymb2 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb2 turbo:TURBO_0006510 '2' .
            turbo:joinHcEncRegDen2 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen2 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:hcreg1 a turbo:TURBO_0000513 .
            
            turbo:joinPartCrid1 a turbo:TURBO_0000503 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartSymb1 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartRegDen1 .
            turbo:joinPartSymb1 a turbo:TURBO_0000504 .
            turbo:joinPartSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen1 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen1 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid2 a turbo:TURBO_0000503 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartSymb2 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartRegDen2 .
            turbo:joinPartSymb2 a turbo:TURBO_0000504 .
            turbo:joinPartSymb2 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen2 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen2 obo:IAO_0000219 turbo:reg1 .
            
            turbo:reg1 a turbo:TURBO_0000506 .
            
            turbo:joinPartCrid1 turbo:TURBO_0000302 turbo:joinHcEncCrid1 .
            turbo:joinPartCrid2 turbo:TURBO_0000302 turbo:joinHcEncCrid2 .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        entLink.joinParticipantsAndHealthcareEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc1 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc1 .
        		turbo:hcCrid1 turbo:TURBO_0000302 turbo:partCrid .
        	}}
          """
        val ask2: String = """
          ASK {GRAPH pmbb:expanded 
          {
            turbo:massdatum1 obo:IAO_0000136 turbo:part1 .
        		turbo:lengthdatum1 obo:IAO_0000136 turbo:part1 .
        		turbo:massdatum1 obo:IAO_0000221 turbo:weight1 .
        		turbo:lengthdatum1 obo:IAO_0000221 turbo:height1 .
        		turbo:mass1 obo:OBI_0000293 turbo:part1 .
        		turbo:mass1 obo:OBI_0000293 turbo:part1 .
          }}
          """
        val ask3: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc2 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc2 .
        		turbo:hcCrid2 turbo:TURBO_0000302 turbo:partCrid .
        	}}
          """
        val ask4: String = """
          ASK {GRAPH pmbb:expanded 
          {
            turbo:massdatum2 obo:IAO_0000136 turbo:part1 .
        		turbo:lengthdatum2 obo:IAO_0000136 turbo:part1 .
        		turbo:massdatum2 obo:IAO_0000221 turbo:weight1 .
        		turbo:lengthdatum2 obo:IAO_0000221 turbo:height1 .
        		turbo:mass2 obo:OBI_0000293 turbo:part1 .
        		turbo:mass2 obo:OBI_0000293 turbo:part1 .
          }}
          """
        
        val ask5: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newPSC1 ?p ?o .
          }}
          """
        val ask6: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newPSC2 ?p ?o .
          }}
          """
        val ask7: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newEncID1 ?p ?o .
          }}
          """
        val ask8: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newEncID2 ?p ?o .
          }}
          """
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (true)
        val bool4: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask4).get
        bool4 should be (true)
        val bool5: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask5).get
        bool5 should be (false)
        val bool6: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask6).get
        bool6 should be (false)
        val bool7: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask7).get
        bool7 should be (false)
        val bool8: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask8).get
        bool8 should be (false)
    }
    
    test("join 2 bb encs to 1 participant")
    {
        val insert: String = """
        INSERT DATA {GRAPH pmbb:entityLinkData {
          
          turbo:joinBbCrid1 a turbo:TURBO_0000533 .
          turbo:joinBbCrid1 obo:BFO_0000051 turbo:joinBbSymb1 .
          turbo:joinBbCrid1 obo:BFO_0000051 turbo:joinBbRegDen1 .
          turbo:joinBbSymb1 turbo:TURBO_0006510 '3' .
          turbo:joinBbSymb1 a turbo:TURBO_0000534 .
          turbo:joinBbRegDen1 a turbo:TURBO_0000535 .
          turbo:joinBbRegDen1 obo:IAO_0000219 turbo:bbreg1 .
          turbo:bbreg1 a turbo:TURBO_0000543 .
          
          turbo:joinBbCrid2 a turbo:TURBO_0000533 .
          turbo:joinBbCrid2 obo:BFO_0000051 turbo:joinBbSymb2 .
          turbo:joinBbCrid2 obo:BFO_0000051 turbo:joinBbRegDen2 .
          turbo:joinBbSymb2 turbo:TURBO_0006510 '4' .
          turbo:joinBbSymb2 a turbo:TURBO_0000534 .
          turbo:joinBbRegDen2 a turbo:TURBO_0000535 .
          turbo:joinBbRegDen2 obo:IAO_0000219 turbo:bbreg2 .
          turbo:bbreg2 a turbo:TURBO_0000543 .
          
          turbo:joinPartCrid1 a turbo:TURBO_0000503 .
          turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartSymb1 .
          turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartRegDen1 .
          turbo:joinPartSymb1 a turbo:TURBO_0000504 .
          turbo:joinPartSymb1 turbo:TURBO_0006510 '1' .
          turbo:joinPartRegDen1 a turbo:TURBO_0000505 .
          turbo:joinPartRegDen1 obo:IAO_0000219 turbo:reg1 .
          
          turbo:joinPartCrid2 a turbo:TURBO_0000503 .
          turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartSymb2 .
          turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartRegDen2 .
          turbo:joinPartSymb2 a turbo:TURBO_0000504 .
          turbo:joinPartSymb2 turbo:TURBO_0006510 '1' .
          turbo:joinPartRegDen2 a turbo:TURBO_0000505 .
          turbo:joinPartRegDen2 obo:IAO_0000219 turbo:reg1 .
          
          turbo:reg1 a turbo:TURBO_0000506 .
          
          turbo:joinPartCrid1 turbo:TURBO_0000302 turbo:joinBbCrid1 .
          turbo:joinPartCrid2 turbo:TURBO_0000302 turbo:joinBbCrid2 .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        entLink.joinParticipantsAndBiobankEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc3 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc3 .
        		turbo:bbCrid1 turbo:TURBO_0000302 turbo:partCrid .
        	}}
          """
        val ask2: String = """
          ASK {GRAPH pmbb:expanded 
          {
            turbo:massdatum3 obo:IAO_0000136 turbo:part1 .
        		turbo:lengthdatum3 obo:IAO_0000136 turbo:part1 .
        		turbo:massdatum3 obo:IAO_0000221 turbo:weight1 .
        		turbo:lengthdatum3 obo:IAO_0000221 turbo:height1 .
        		turbo:mass3 obo:OBI_0000293 turbo:part1 .
        		turbo:mass3 obo:OBI_0000293 turbo:part1 .
          }}
          """
        val ask3: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc4 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc4 .
        		turbo:bbCrid2 turbo:TURBO_0000302 turbo:partCrid .
        	}}
          """
        val ask4: String = """
          ASK {GRAPH pmbb:expanded 
          {
            turbo:massdatum4 obo:IAO_0000136 turbo:part1 .
        		turbo:lengthdatum4 obo:IAO_0000136 turbo:part1 .
        		turbo:massdatum4 obo:IAO_0000221 turbo:weight1 .
        		turbo:lengthdatum4 obo:IAO_0000221 turbo:height1 .
        		turbo:mass4 obo:OBI_0000293 turbo:part1 .
        		turbo:mass4 obo:OBI_0000293 turbo:part1 .
          }}
          """
        
        val ask5: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newPSC1 ?p ?o .
          }}
          """
        val ask6: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newPSC2 ?p ?o .
          }}
          """
        val ask7: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newEncID1 ?p ?o .
          }}
          """
        val ask8: String = """
          ASK {GRAPH pmbb:expanded 
          {
                turbo:newEncID2 ?p ?o .
          }}
          """
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (true)
        val bool4: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask4).get
        bool4 should be (true)
        val bool5: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask5).get
        bool5 should be (false)
        val bool6: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask6).get
        bool6 should be (false)
        val bool7: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask7).get
        bool7 should be (false)
        val bool8: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask8).get
        bool8 should be (false)
    }
    
    test("connect 4 bmis to 1 adipose")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:expanded {
          
              turbo:part1 obo:RO_0000087 turbo:puirole1 .
              turbo:puirole1 a obo:OBI_0000097 .
              turbo:puirole1 obo:BFO_0000054 turbo:hcenc1 .
              
              turbo:part1 obo:RO_0000087 turbo:puirole2 .
              turbo:puirole2 a obo:OBI_0000097 .
              turbo:puirole2 obo:BFO_0000054 turbo:hcenc2 .
              
              turbo:part1 obo:RO_0000087 turbo:puirole3 .
              turbo:puirole3 a obo:OBI_0000097 .
              turbo:puirole3 obo:BFO_0000054 turbo:bbenc3 .
              
              turbo:part1 obo:RO_0000087 turbo:puirole4 .
              turbo:puirole4 a obo:OBI_0000097 .
              turbo:puirole4 obo:BFO_0000054 turbo:bbenc4 .
              
          }}
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        entLink.connectBMIToAdipose(cxn)
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded {
              turbo:BMI1 obo:IAO_0000136 turbo:adipose1 .
          }}
          """
        
        val ask2: String = """
          ASK {GRAPH pmbb:expanded {
              turbo:BMI2 obo:IAO_0000136 turbo:adipose1 .
          }}
          """
        
        val ask3: String = """
          ASK {GRAPH pmbb:expanded {
              turbo:BMI3 obo:IAO_0000136 turbo:adipose1 .
          }}
          """
        
        val ask4: String = """
          ASK {GRAPH pmbb:expanded {
              turbo:BMI4 obo:IAO_0000136 turbo:adipose1 .
          }}
          """
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (true)
        val bool4: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask4).get
        bool4 should be (true)  
    }
    
    test("an additional hc enc with different registry")
    {    
        val insert: String = """
        INSERT DATA {
        GRAPH pmbb:entityLinkData {
            turbo:joinHcEncCrid1 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid1 obo:BFO_0000051 turbo:joinHcEncSymb1 .
            turbo:joinHcEncCrid1 obo:BFO_0000051 turbo:joinHcEncRegDen1 .
            turbo:joinHcEncSymb1 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinHcEncRegDen1 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen1 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:joinHcEncCrid2 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid2 obo:BFO_0000051 turbo:joinHcEncSymb2 .
            turbo:joinHcEncCrid2 obo:BFO_0000051 turbo:joinHcEncRegDen2 .
            turbo:joinHcEncSymb2 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb2 turbo:TURBO_0006510 '2' .
            turbo:joinHcEncRegDen2 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen2 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:joinHcEncCrid3 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid3 obo:BFO_0000051 turbo:joinHcEncSymb3 .
            turbo:joinHcEncCrid3 obo:BFO_0000051 turbo:joinHcEncRegDen3 .
            turbo:joinHcEncSymb3 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb3 turbo:TURBO_0006510 '3' .
            turbo:joinHcEncRegDen3 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen3 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:hcreg1 a turbo:TURBO_0000513 .
            
            turbo:joinPartCrid1 a turbo:TURBO_0000503 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartSymb1 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartRegDen1 .
            turbo:joinPartSymb1 a turbo:TURBO_0000504 .
            turbo:joinPartSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen1 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen1 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid2 a turbo:TURBO_0000503 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartSymb2 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartRegDen2 .
            turbo:joinPartSymb2 a turbo:TURBO_0000504 .
            turbo:joinPartSymb2 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen2 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen2 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid3 a turbo:TURBO_0000503 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartSymb3 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartRegDen3 .
            turbo:joinPartSymb3 a turbo:TURBO_0000504 .
            turbo:joinPartSymb3 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen3 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen3 obo:IAO_0000219 turbo:reg1 .
            
            turbo:reg1 a turbo:TURBO_0000506 .
            
            turbo:joinPartCrid1 turbo:TURBO_0000302 turbo:joinHcEncCrid1 .
            turbo:joinPartCrid2 turbo:TURBO_0000302 turbo:joinHcEncCrid2 .
            turbo:joinPartCrid3 turbo:TURBO_0000302 turbo:joinHcEncCrid3 .
          }
          Graph pmbb:expanded
          {
              turbo:hcenc3 a obo:OGMS_0000097 .
              turbo:hcCrid3 obo:IAO_0000219 turbo:hcenc3 .
              turbo:hcCrid3 a turbo:TURBO_0000508 .
              turbo:hcCrid3 obo:BFO_0000051 turbo:hcSymb3 .
              turbo:hcCrid3 obo:BFO_0000051 turbo:hcRegDen3 .
              turbo:hcSymb3 turbo:TURBO_0006510 '3' .
              turbo:hcSymb3 a turbo:TURBO_0000509 .
              turbo:hcRegDen3 a turbo:TURBO_0000510 .
              turbo:hcRegDen3 obo:IAO_0000219 turbo:hcreg3 .
              turbo:hcreg3 a turbo:TURBO_0000513 .
              turbo:hcenc3 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcCrid3 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcSymb3 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcRegDen3 turbo:TURBO_0006500 'true'^^xsd:boolean .
          }
          }"""
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.joinParticipantsAndHealthcareEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc2 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc2 .
        	}}
          """
        
        val ask2: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc1 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc1 .
        	}}
          """
        
        val ask3: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc3 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc3 .
        	}}
          """
        
        val count: String = """
          SELECT * where
          {
              graph pmbb:expanded
              {
                  ?cons a turbo:TURBO_0000502 .
                  ?cons obo:RO_0000056 ?enc .
                  ?enc a obo:OGMS_0000097 .
              }
          }
          """
        
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (false)
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, ArrayBuffer("cons", "enc")).size should be (2)
    }
    
    test("an additional hc enc with same reg diff symbol value")
    {    
        val insert: String = """
        INSERT DATA {
        GRAPH pmbb:entityLinkData {
            turbo:joinHcEncCrid1 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid1 obo:BFO_0000051 turbo:joinHcEncSymb1 .
            turbo:joinHcEncCrid1 obo:BFO_0000051 turbo:joinHcEncRegDen1 .
            turbo:joinHcEncSymb1 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinHcEncRegDen1 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen1 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:joinHcEncCrid2 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid2 obo:BFO_0000051 turbo:joinHcEncSymb2 .
            turbo:joinHcEncCrid2 obo:BFO_0000051 turbo:joinHcEncRegDen2 .
            turbo:joinHcEncSymb2 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb2 turbo:TURBO_0006510 '2' .
            turbo:joinHcEncRegDen2 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen2 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:joinHcEncCrid3 a turbo:TURBO_0000508 .
            turbo:joinHcEncCrid3 obo:BFO_0000051 turbo:joinHcEncSymb3 .
            turbo:joinHcEncCrid3 obo:BFO_0000051 turbo:joinHcEncRegDen3 .
            turbo:joinHcEncSymb3 a turbo:TURBO_0000509 .
            turbo:joinHcEncSymb3 turbo:TURBO_0006510 '3' .
            turbo:joinHcEncRegDen3 a turbo:TURBO_0000510 .
            turbo:joinHcEncRegDen3 obo:IAO_0000219 turbo:hcreg1 .
            
            turbo:hcreg1 a turbo:TURBO_0000513 .
            
            turbo:joinPartCrid1 a turbo:TURBO_0000503 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartSymb1 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartRegDen1 .
            turbo:joinPartSymb1 a turbo:TURBO_0000504 .
            turbo:joinPartSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen1 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen1 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid2 a turbo:TURBO_0000503 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartSymb2 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartRegDen2 .
            turbo:joinPartSymb2 a turbo:TURBO_0000504 .
            turbo:joinPartSymb2 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen2 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen2 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid3 a turbo:TURBO_0000503 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartSymb3 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartRegDen3 .
            turbo:joinPartSymb3 a turbo:TURBO_0000504 .
            turbo:joinPartSymb3 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen3 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen3 obo:IAO_0000219 turbo:reg1 .
            
            turbo:reg1 a turbo:TURBO_0000506 .
            
            turbo:joinPartCrid1 turbo:TURBO_0000302 turbo:joinHcEncCrid1 .
            turbo:joinPartCrid2 turbo:TURBO_0000302 turbo:joinHcEncCrid2 .
            turbo:joinPartCrid3 turbo:TURBO_0000302 turbo:joinHcEncCrid3 .
          }
          Graph pmbb:expanded
          {
              turbo:hcenc3 a obo:OGMS_0000097 .
              turbo:hcCrid3 obo:IAO_0000219 turbo:hcenc3 .
              turbo:hcCrid3 a turbo:TURBO_0000508 .
              turbo:hcCrid3 obo:BFO_0000051 turbo:hcSymb3 .
              turbo:hcCrid3 obo:BFO_0000051 turbo:hcRegDen3 .
              turbo:hcSymb3 turbo:TURBO_0006510 '4' .
              turbo:hcSymb3 a turbo:TURBO_0000509 .
              turbo:hcRegDen3 a turbo:TURBO_0000510 .
              turbo:hcRegDen3 obo:IAO_0000219 turbo:hcreg1 .
              turbo:hcreg1 a turbo:TURBO_0000513 .
              turbo:hcenc3 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcCrid3 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcSymb3 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:hcRegDen3 turbo:TURBO_0006500 'true'^^xsd:boolean .
          }
          }"""
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.joinParticipantsAndHealthcareEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc2 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc2 .
        	}}
          """
        
        val ask2: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc1 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc1 .
        	}}
          """
        
        val ask3: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:hcenc3 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:hcenc3 .
        	}}
          """
        
        val count: String = """
          SELECT * where
          {
              graph pmbb:expanded
              {
                  ?cons a turbo:TURBO_0000502 .
                  ?cons obo:RO_0000056 ?enc .
                  ?enc a obo:OGMS_0000097 .
              }
          }
          """
        
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (false)
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, ArrayBuffer("cons", "enc")).size should be (2)
    }
    
    test("an additional bb enc with different registry")
    {
        val insert: String = """
        INSERT DATA {
        GRAPH pmbb:entityLinkData {
            turbo:joinBbEncCrid1 a turbo:TURBO_0000533 .
            turbo:joinBbEncCrid1 obo:BFO_0000051 turbo:joinBbEncSymb1 .
            turbo:joinBbEncCrid1 obo:BFO_0000051 turbo:joinBbEncRegDen1 .
            turbo:joinBbEncSymb1 a turbo:TURBO_0000534 .
            turbo:joinBbEncSymb1 turbo:TURBO_0006510 '3' .
            turbo:joinBbEncRegDen1 a turbo:TURBO_0000535 .
            turbo:joinBbEncRegDen1 obo:IAO_0000219 turbo:bbreg1 .
            
            turbo:joinBbEncCrid2 a turbo:TURBO_0000533 .
            turbo:joinBbEncCrid2 obo:BFO_0000051 turbo:joinBbEncSymb2 .
            turbo:joinBbEncCrid2 obo:BFO_0000051 turbo:joinBbEncRegDen2 .
            turbo:joinBbEncSymb2 a turbo:TURBO_0000534 .
            turbo:joinBbEncSymb2 turbo:TURBO_0006510 '4' .
            turbo:joinBbEncRegDen2 a turbo:TURBO_0000535 .
            turbo:joinBbEncRegDen2 obo:IAO_0000219 turbo:bbreg2 .
            
            turbo:joinBbEncCrid3 a turbo:TURBO_0000533 .
            turbo:joinBbEncCrid3 obo:BFO_0000051 turbo:joinBbEncSymb3 .
            turbo:joinBbEncCrid3 obo:BFO_0000051 turbo:joinBbEncRegDen3 .
            turbo:joinBbEncSymb3 a turbo:TURBO_0000534 .
            turbo:joinBbEncSymb3 turbo:TURBO_0006510 '5' .
            turbo:joinBbEncRegDen3 a turbo:TURBO_0000535 .
            turbo:joinBbEncRegDen3 obo:IAO_0000219 turbo:bbreg1 .
            
            turbo:bbreg1 a turbo:TURBO_0000543 .
            turbo:bbreg2 a turbo:TURBO_0000543 .
            
            turbo:joinPartCrid1 a turbo:TURBO_0000503 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartSymb1 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartRegDen1 .
            turbo:joinPartSymb1 a turbo:TURBO_0000504 .
            turbo:joinPartSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen1 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen1 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid2 a turbo:TURBO_0000503 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartSymb2 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartRegDen2 .
            turbo:joinPartSymb2 a turbo:TURBO_0000504 .
            turbo:joinPartSymb2 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen2 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen2 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid3 a turbo:TURBO_0000503 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartSymb3 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartRegDen3 .
            turbo:joinPartSymb3 a turbo:TURBO_0000504 .
            turbo:joinPartSymb3 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen3 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen3 obo:IAO_0000219 turbo:reg1 .
            
            turbo:reg1 a turbo:TURBO_0000506 .
            
            turbo:joinPartCrid1 turbo:TURBO_0000302 turbo:joinBbEncCrid1 .
            turbo:joinPartCrid2 turbo:TURBO_0000302 turbo:joinBbEncCrid2 .
            turbo:joinPartCrid3 turbo:TURBO_0000302 turbo:joinBbEncCrid3 .
          }
          Graph pmbb:expanded
          {
              turbo:bbenc5 a turbo:TURBO_0000527 .
              turbo:bbCrid5 obo:IAO_0000219 turbo:bbenc5 .
              turbo:bbCrid5 a turbo:TURBO_0000533 .
              turbo:bbCrid5 obo:BFO_0000051 turbo:bbSymb5 .
              turbo:bbCrid5 obo:BFO_0000051 turbo:bbRegDen5 .
              turbo:bbSymb5 turbo:TURBO_0006510 '5' .
              turbo:bbSymb5 a turbo:TURBO_0000534 .
              turbo:bbRegDen5 a turbo:TURBO_0000535 .
              turbo:bbRegDen5 obo:IAO_0000219 turbo:bbreg2 .
              turbo:bbreg5 a turbo:TURBO_0000513 .
              turbo:bbenc5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbCrid5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbSymb5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbRegDen5 turbo:TURBO_0006500 'true'^^xsd:boolean .
          }
          }"""
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.joinParticipantsAndBiobankEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc3 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc3 .
        	}}
          """
        
        val ask2: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc4 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc4 .
        	}}
          """
        
        val ask3: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc5 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc5 .
        	}}
          """
        
        val count: String = """
          SELECT * where
          {
              graph pmbb:expanded
              {
                  ?cons a turbo:TURBO_0000502 .
                  ?cons obo:RO_0000056 ?enc .
                  ?enc a turbo:TURBO_0000527 .
              }
          }
          """
        
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (false)
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, ArrayBuffer("cons", "enc")).size should be (2)
    }
    
    test("an additional bb enc with sam reg diff symbol value")
    {
         val insert: String = """
        INSERT DATA {
        GRAPH pmbb:entityLinkData {
            turbo:joinBbEncCrid1 a turbo:TURBO_0000533 .
            turbo:joinBbEncCrid1 obo:BFO_0000051 turbo:joinBbEncSymb1 .
            turbo:joinBbEncCrid1 obo:BFO_0000051 turbo:joinBbEncRegDen1 .
            turbo:joinBbEncSymb1 a turbo:TURBO_0000534 .
            turbo:joinBbEncSymb1 turbo:TURBO_0006510 '3' .
            turbo:joinBbEncRegDen1 a turbo:TURBO_0000535 .
            turbo:joinBbEncRegDen1 obo:IAO_0000219 turbo:bbreg1 .
            
            turbo:joinBbEncCrid2 a turbo:TURBO_0000533 .
            turbo:joinBbEncCrid2 obo:BFO_0000051 turbo:joinBbEncSymb2 .
            turbo:joinBbEncCrid2 obo:BFO_0000051 turbo:joinBbEncRegDen2 .
            turbo:joinBbEncSymb2 a turbo:TURBO_0000534 .
            turbo:joinBbEncSymb2 turbo:TURBO_0006510 '4' .
            turbo:joinBbEncRegDen2 a turbo:TURBO_0000535 .
            turbo:joinBbEncRegDen2 obo:IAO_0000219 turbo:bbreg2 .
            
            turbo:joinBbEncCrid3 a turbo:TURBO_0000533 .
            turbo:joinBbEncCrid3 obo:BFO_0000051 turbo:joinBbEncSymb3 .
            turbo:joinBbEncCrid3 obo:BFO_0000051 turbo:joinBbEncRegDen3 .
            turbo:joinBbEncSymb3 a turbo:TURBO_0000534 .
            turbo:joinBbEncSymb3 turbo:TURBO_0006510 '5' .
            turbo:joinBbEncRegDen3 a turbo:TURBO_0000535 .
            turbo:joinBbEncRegDen3 obo:IAO_0000219 turbo:bbreg1 .
            
            turbo:bbreg1 a turbo:TURBO_0000543 .
            turbo:bbreg2 a turbo:TURBO_0000543 .
            
            turbo:joinPartCrid1 a turbo:TURBO_0000503 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartSymb1 .
            turbo:joinPartCrid1 obo:BFO_0000051 turbo:joinPartRegDen1 .
            turbo:joinPartSymb1 a turbo:TURBO_0000504 .
            turbo:joinPartSymb1 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen1 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen1 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid2 a turbo:TURBO_0000503 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartSymb2 .
            turbo:joinPartCrid2 obo:BFO_0000051 turbo:joinPartRegDen2 .
            turbo:joinPartSymb2 a turbo:TURBO_0000504 .
            turbo:joinPartSymb2 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen2 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen2 obo:IAO_0000219 turbo:reg1 .
            
            turbo:joinPartCrid3 a turbo:TURBO_0000503 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartSymb3 .
            turbo:joinPartCrid3 obo:BFO_0000051 turbo:joinPartRegDen3 .
            turbo:joinPartSymb3 a turbo:TURBO_0000504 .
            turbo:joinPartSymb3 turbo:TURBO_0006510 '1' .
            turbo:joinPartRegDen3 a turbo:TURBO_0000505 .
            turbo:joinPartRegDen3 obo:IAO_0000219 turbo:reg1 .
            
            turbo:reg1 a turbo:TURBO_0000506 .
            
            turbo:joinPartCrid1 turbo:TURBO_0000302 turbo:joinBbEncCrid1 .
            turbo:joinPartCrid2 turbo:TURBO_0000302 turbo:joinBbEncCrid2 .
            turbo:joinPartCrid3 turbo:TURBO_0000302 turbo:joinBbEncCrid3 .
          }
          Graph pmbb:expanded
          {
              turbo:bbenc5 a turbo:TURBO_0000527 .
              turbo:bbCrid5 obo:IAO_0000219 turbo:bbenc5 .
              turbo:bbCrid5 a turbo:TURBO_0000533 .
              turbo:bbCrid5 obo:BFO_0000051 turbo:bbSymb5 .
              turbo:bbCrid5 obo:BFO_0000051 turbo:bbRegDen5 .
              turbo:bbSymb5 turbo:TURBO_0006510 '6' .
              turbo:bbSymb5 a turbo:TURBO_0000534 .
              turbo:bbRegDen5 a turbo:TURBO_0000535 .
              turbo:bbRegDen5 obo:IAO_0000219 turbo:bbreg1 .
              turbo:bbreg5 a turbo:TURBO_0000513 .
              turbo:bbenc5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbCrid5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbSymb5 turbo:TURBO_0006500 'true'^^xsd:boolean .
              turbo:bbRegDen5 turbo:TURBO_0006500 'true'^^xsd:boolean .
          }
          }"""
        
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        entLink.joinParticipantsAndBiobankEncounters(cxn, entLink.getConsenterInfo(cxn))
        
        val ask1: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc3 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc3 .
        	}}
          """
        
        val ask2: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc4 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc4 .
        	}}
          """
        
        val ask3: String = """
          ASK {GRAPH pmbb:expanded
        	{
        		turbo:part1 obo:RO_0000056 turbo:bbenc5 .
        		turbo:part1 obo:RO_0000087 ?puirole .
        		?puirole a obo:OBI_0000097 .
        		?puirole obo:BFO_0000054 turbo:bbenc5 .
        	}}
          """
        
        val count: String = """
          SELECT * where
          {
              graph pmbb:expanded
              {
                  ?cons a turbo:TURBO_0000502 .
                  ?cons obo:RO_0000056 ?enc .
                  ?enc a turbo:TURBO_0000527 .
              }
          }
          """
        
        val bool1: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get
        bool1 should be (true)
        val bool2: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get
        bool2 should be (true)
        val bool3: Boolean = helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get
        bool3 should be (false)
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + count, ArrayBuffer("cons", "enc")).size should be (2)
    }
}