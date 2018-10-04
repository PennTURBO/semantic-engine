package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class PreExpansionEntityLinkingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    val entlink: EntityLinker = new EntityLinker()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    val ask = """
    Ask
    {
        Graph pmbb:expanded
        {
            ?consenter obo:RO_0000056 ?encounter .
        }
    }
    """
    
    val countExpanded = """
    Select (count (distinct ?s) as ?scount)
    Where
    {
        Graph pmbb:expanded
        {
            ?s ?p ?o .
        }
    }
    """
    
    val countEntLink = """
    Select (count (distinct ?s) as ?scount)
    Where
    {
        Graph pmbb:entityLinkData
        {
            ?s ?p ?o .
        }
    }
    """
    
    test("pre expansion link hc enc to bb cons")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:hc1 a obo:OGMS_0000097 .
                  pmbb:hc1 turbo:ScHcEnc2UnexpandedConsenter "http://carnival/consenter/1"^^xsd:anyURI .
                  pmbb:hc1 turbo:TURBO_0010002 "http://carnival/reg1"^^xsd:anyURI .
                  
                  pmbb:hc2 a obo:OGMS_0000097 .
                  pmbb:hc2 turbo:ScHcEnc2UnexpandedConsenter "http://carnival/consenter/2"^^xsd:anyURI .
                  pmbb:hc2 turbo:TURBO_0010002 "http://carnival/reg1"^^xsd:anyURI .
              }
              Graph pmbb:shortcuts2
              {
                  <http://carnival/consenter/1> a turbo:TURBO_0000502 .
                  <http://carnival/consenter/1> turbo:TURBO_0000610 "http://carnival/reg1"^^xsd:anyURI .
                  
                  <http://carnival/consenter/2> a turbo:TURBO_0000502 .
                  <http://carnival/consenter/2> turbo:TURBO_0003610 "http://carnival/reg1"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.linkUnexpandedHcEncountersToUnexpandedConsenters(cxn, "globalUUID")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countExpanded, "scount")(0).split("\"")(1).toInt should be (2)
    }
    
    test("pre expansion link bb enc to bb cons")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:bb1 a turbo:TURBO_0000527 .
                  pmbb:bb1 turbo:ScBbEnc2UnexpandedConsenter "http://carnival/consenter/1"^^xsd:anyURI .
                  pmbb:bb1 turbo:TURBO_0010012 "http://carnival/reg1"^^xsd:anyURI .
                  
                  pmbb:bb2 a turbo:TURBO_0000527 .
                  pmbb:bb2 turbo:ScBbEnc2UnexpandedConsenter "http://carnival/consenter/2"^^xsd:anyURI .
                  pmbb:bb2 turbo:TURBO_0010012 "http://carnival/reg1"^^xsd:anyURI .
              }
              Graph pmbb:shortcuts2
              {
                  <http://carnival/consenter/1> a turbo:TURBO_0000502 .
                  <http://carnival/consenter/1> turbo:TURBO_0000610 "http://carnival/reg1"^^xsd:anyURI .
                  
                  <http://carnival/consenter/2> a turbo:TURBO_0000502 .
                  <http://carnival/consenter/2> turbo:TURBO_0003610 "http://carnival/reg1"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.linkUnexpandedBbEncountersToUnexpandedConsenters(cxn, "globalUUID")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countExpanded, "scount")(0).split("\"")(1).toInt should be (2)
    }
    
    test("pre expansion no link hc enc to bb cons - consenter not present")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:hc1 a obo:OGMS_0000097 .
                  pmbb:hc1 turbo:ScHcEnc2UnexpandedConsenter "http://carnival/consenter/1"^^xsd:anyURI .
                  pmbb:hc1 turbo:TURBO_0010002 "http://carnival/reg1"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.linkUnexpandedHcEncountersToUnexpandedConsenters(cxn, "globalUUID")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (false)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countExpanded, "scount")(0).split("\"")(1).toInt should be (0)
    }
    
    test("pre expansion no link bb enc to bb cons - consenter not present")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:bb1 a turbo:TURBO_0000527 .
                  pmbb:bb1 turbo:ScBbEnc2UnexpandedConsenter "http://carnival/consenter/1"^^xsd:anyURI .
                  pmbb:bb1 turbo:TURBO_0010012 "http://carnival/reg1"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.linkUnexpandedBbEncountersToUnexpandedConsenters(cxn, "globalUUID")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (false)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countExpanded, "scount")(0).split("\"")(1).toInt should be (0)
    }
    
    test("pre expansion no link hc enc to bb cons - registries do not match")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:hc1 a obo:OGMS_0000097 .
                  pmbb:hc1 turbo:ScHcEnc2UnexpandedConsenter "http://carnival/consenter/1"^^xsd:anyURI .
                  pmbb:hc1 turbo:TURBO_0010002 "http://carnival/reg1"^^xsd:anyURI .
              }
              Graph pmbb:shortcuts2
              {
                  <http://carnival/consenter/1> a turbo:TURBO_0000502 .
                  <http://carnival/consenter/1> turbo:TURBO_0000610 "http://carnival/reg2"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.linkUnexpandedHcEncountersToUnexpandedConsenters(cxn, "globalUUID")
   
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (false)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countExpanded, "scount")(0).split("\"")(1).toInt should be (0)
    }
    
    test("pre expansion no link bb enc to bb cons - registries do not match")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:bb1 a turbo:TURBO_0000527 .
                  pmbb:bb1 turbo:ScBbEnc2UnexpandedConsenter "http://carnival/consenter/1"^^xsd:anyURI .
                  pmbb:bb1 turbo:TURBO_0010012 "http://carnival/reg1"^^xsd:anyURI .
              }
              Graph pmbb:shortcuts2
              {
                  <http://carnival/consenter/1> a turbo:TURBO_0000502 .
                  <http://carnival/consenter/1> turbo:TURBO_0000610 "http://carnival/reg2"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.linkUnexpandedBbEncountersToUnexpandedConsenters(cxn, "globalUUID")
        
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (false)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countExpanded, "scount")(0).split("\"")(1).toInt should be (0)
    }
    
    test("create join data from unlinked hc enc")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:hcEnc1 a obo:OGMS_0000097 .
                  pmbb:hcEnc1 turbo:TURBO_0010000 '123' .
                  pmbb:hcEnc1 turbo:TURBO_0000648 '456' .
                  pmbb:hcEnc1 turbo:TURBO_0000650 "http://carnival/hcReg1"^^xsd:anyURI .
                  pmbb:hcEnc1 turbo:TURBO_0010002 "http://carnival/consReg1"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.createJoinDataFromUnlinkedHcEncounters(cxn, "globalUUID")
        val ask = """
          Ask
          {
              Graph pmbb:entityLinkData
              {
                  ?entLinkHcCrid a turbo:TURBO_0000508 .
                  ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcSymb .
                  ?entLinkHcSymb turbo:TURBO_0006510 '456'^^xsd:string .
                  ?entLinkHcSymb a turbo:TURBO_0000509 .
                  ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcRegDen .
                  ?entLinkHcRegDen a turbo:TURBO_0000510 .
                  ?entLinkHcRegDen obo:IAO_0000219 <http://carnival/hcReg1> .
                  <http://carnival/hcReg1> a turbo:TURBO_0000513 .
                  
                  ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkHcCrid .
                  
                  ?entLinkPartCrid a turbo:TURBO_0000503 .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                  ?entLinkPartSymb a turbo:TURBO_0000504 .
                  ?entLinkPartSymb turbo:TURBO_0006510 '123'^^xsd:string .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                  ?entLinkPartRegDen a turbo:TURBO_0000505 .
                  ?entLinkPartRegDen obo:IAO_0000219 <http://carnival/consReg1> .
                  <http://carnival/consReg1> a turbo:TURBO_0000506 .
              }
          }
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("create join data from unlinked bb enc")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:bbEnc1 a turbo:TURBO_0000527 .
                  pmbb:bbEnc1 turbo:TURBO_0010010 '123' .
                  pmbb:bbEnc1 turbo:TURBO_0000628 '456' .
                  pmbb:bbEnc1 turbo:TURBO_0000630 "http://carnival/bbReg1"^^xsd:anyURI .
                  pmbb:bbEnc1 turbo:TURBO_0010012 "http://carnival/consReg1"^^xsd:anyURI .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.createJoinDataFromUnlinkedBbEncounters(cxn, "globlUUID")
        val ask = """
          Ask
          {
              Graph pmbb:entityLinkData
              {
                  ?entLinkBbCrid a turbo:TURBO_0000533 .
                  ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbSymb .
                  ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbRegDen .
                  ?entLinkBbSymb turbo:TURBO_0006510 '456'^^xsd:string .
                  ?entLinkBbSymb a turbo:TURBO_0000534 .
                  ?entLinkBbRegDen a turbo:TURBO_0000535 .
                  ?entLinkBbRegDen obo:IAO_0000219 <http://carnival/bbReg1> .
                  <http://carnival/bbReg1> a turbo:TURBO_0000543 .
                  
                  ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkBbCrid .
                  
                  ?entLinkPartCrid a turbo:TURBO_0000503 .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                  ?entLinkPartSymb a turbo:TURBO_0000504 .
                  ?entLinkPartSymb turbo:TURBO_0006510 '123'^^xsd:string .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                  ?entLinkPartRegDen a turbo:TURBO_0000505 .
                  ?entLinkPartRegDen obo:IAO_0000219 <http://carnival/consReg1> .
                  <http://carnival/consReg1> a turbo:TURBO_0000506 .
              }
          }
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("no join data created from hc enc already linked")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:hcEnc1 a obo:OGMS_0000097 .
                  pmbb:hcEnc1 turbo:TURBO_0010000 '123' .
                  pmbb:hcEnc1 turbo:TURBO_0000648 '456' .
                  pmbb:hcEnc1 turbo:TURBO_0000650 "http://carnival/hcReg1"^^xsd:anyURI .
                  pmbb:hcEnc1 turbo:TURBO_0010002 "http://carnival/consReg1"^^xsd:anyURI .
              }
              Graph pmbb:expanded
              {
                  pmbb:consenter1 obo:RO_0000056 pmbb:f0fc4ee4fd16fc81547e3303a336922e .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.createJoinDataFromUnlinkedHcEncounters(cxn, "globalUUID")
        val ask = """
          Ask
          {
              Graph pmbb:entityLinkData
              {
                  ?entLinkHcCrid a turbo:TURBO_0000508 .
                  ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcSymb .
                  ?entLinkHcSymb turbo:TURBO_0006510 '456'^^xsd:string .
                  ?entLinkHcSymb a turbo:TURBO_0000509 .
                  ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcRegDen .
                  ?entLinkHcRegDen a turbo:TURBO_0000510 .
                  ?entLinkHcRegDen obo:IAO_0000219 <http://carnival/hcReg1> .
                  <http://carnival/hcReg1> a turbo:TURBO_0000513 .
                  
                  ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkHcCrid .
                  
                  ?entLinkPartCrid a turbo:TURBO_0000503 .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                  ?entLinkPartSymb a turbo:TURBO_0000504 .
                  ?entLinkPartSymb turbo:TURBO_0006510 '123'^^xsd:string .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                  ?entLinkPartRegDen a turbo:TURBO_0000505 .
                  ?entLinkPartRegDen obo:IAO_0000219 <http://carnival/consReg1> .
                  <http://carnival/consReg1> a turbo:TURBO_0000506 .
              }
          }
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (false)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countEntLink, "scount")(0).split("\"")(1).toInt should be (0)
    }
    
    test("no join data created from bb enc already linked")
    {
        val insert = """
          Insert Data
          {
              Graph pmbb:shortcuts1
              {
                  pmbb:bbEnc1 a turbo:TURBO_0000527 .
                  pmbb:bbEnc1 turbo:TURBO_0010010 '123' .
                  pmbb:bbEnc1 turbo:TURBO_0000628 '456' .
                  pmbb:bbEnc1 turbo:TURBO_0000630 "http://carnival/bbReg1"^^xsd:anyURI .
                  pmbb:bbEnc1 turbo:TURBO_0010012 "http://carnival/consReg1"^^xsd:anyURI .
              }
              Graph pmbb:expanded
              {
                  pmbb:consenter1 obo:RO_0000056 pmbb:6212cd5ecd0668f0c5e5dd95c234a149 .
              }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        entlink.createJoinDataFromUnlinkedBbEncounters(cxn, "globalUUID")
        val ask = """
          Ask
          {
              Graph pmbb:entityLinkData
              {
                  ?entLinkBbCrid a turbo:TURBO_0000533 .
                  ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbSymb .
                  ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbRegDen .
                  ?entLinkBbSymb turbo:TURBO_0006510 '456'^^xsd:string .
                  ?entLinkBbSymb a turbo:TURBO_0000534 .
                  ?entLinkBbRegDen a turbo:TURBO_0000535 .
                  ?entLinkBbRegDen obo:IAO_0000219 <http://carnival/bbReg1> .
                  <http://carnival/bbReg1> a turbo:TURBO_0000543 .
                  
                  ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkBbCrid .
                  
                  ?entLinkPartCrid a turbo:TURBO_0000503 .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                  ?entLinkPartSymb a turbo:TURBO_0000504 .
                  ?entLinkPartSymb turbo:TURBO_0006510 '123'^^xsd:string .
                  ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                  ?entLinkPartRegDen a turbo:TURBO_0000505 .
                  ?entLinkPartRegDen obo:IAO_0000219 <http://carnival/consReg1> .
                  <http://carnival/consReg1> a turbo:TURBO_0000506 .
              }
          }
          """
        update.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (false)
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + countEntLink, "scount")(0).split("\"")(1).toInt should be (0)
    }
}