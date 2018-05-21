package edu.upenn.turbo

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class MedicationMappingUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val medmap: MedicationMapper = new MedicationMapper
    
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
    
    test("tidy order name")
    {
        medmap.tidyOrderName("-hello-") should be ("hello")
        medmap.tidyOrderName("hell-o") should be ("hell-o")
        medmap.tidyOrderName("(hello)") should be ("(hello)")
        medmap.tidyOrderName("hell()o") should be ("hell()o")
        medmap.tidyOrderName("CEFTRIAXONE IV SYRINGE 2G/20ML (CNR)") should be ("ceftriaxone iv syringe 2g/20ml (cnr)")
    }
    
    test("pull medmap data")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:prescript1 a obo:PDRO_0000024 .
                  pmbb:prescript2 a obo:PDRO_0000024 .
                  pmbb:prescript3 a obo:PDRO_0000024 .
                  pmbb:prescript4 a obo:PDRO_0000024 .
                  
                  pmbb:prescript1 turbo:TURBO_0006512 "RYTHMOL 150 MG OR TABS" .
                  pmbb:prescript2 turbo:TURBO_0006512 "ATENOLOL 25 MG OR TABS" .
                  pmbb:prescript3 turbo:TURBO_0006512 "OXYCODONE-ACETAMINOPHEN 5 MG-325 MG -" .
                  pmbb:prescript4 turbo:TURBO_0006512 "CARDIZEM LA 180 MG OR TAB SR 24HR" .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        val result: ArrayBuffer[ArrayBuffer[Value]] = medmap.getAllUnmappedMedsInfo(cxn)
        val convertedResult: ArrayBuffer[ArrayBuffer[String]] = helper.convertSparqlResultToStringArray(result)
        convertedResult.contains(ArrayBuffer("http://www.itmat.upenn.edu/biobank/prescript1", "\"RYTHMOL 150 MG OR TABS\"^^<http://www.w3.org/2001/XMLSchema#string>")) should be (true)
        convertedResult.contains(ArrayBuffer("http://www.itmat.upenn.edu/biobank/prescript2", "\"ATENOLOL 25 MG OR TABS\"^^<http://www.w3.org/2001/XMLSchema#string>")) should be (true)
        convertedResult.contains(ArrayBuffer("http://www.itmat.upenn.edu/biobank/prescript3", "\"OXYCODONE-ACETAMINOPHEN 5 MG-325 MG -\"^^<http://www.w3.org/2001/XMLSchema#string>")) should be (true)
        convertedResult.contains(ArrayBuffer("http://www.itmat.upenn.edu/biobank/prescript4", "\"CARDIZEM LA 180 MG OR TAB SR 24HR\"^^<http://www.w3.org/2001/XMLSchema#string>")) should be (true)
    }
    
    test("four valid order names")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:prescript1 a obo:PDRO_0000024 .
                  pmbb:prescript2 a obo:PDRO_0000024 .
                  pmbb:prescript3 a obo:PDRO_0000024 .
                  pmbb:prescript4 a obo:PDRO_0000024 .
                  
                  pmbb:prescript1 turbo:TURBO_0006512 "RYTHMOL 150 MG OR TABS" .
                  pmbb:prescript2 turbo:TURBO_0006512 "ATENOLOL 25 MG OR TABS" .
                  pmbb:prescript3 turbo:TURBO_0006512 "OXYCODONE-ACETAMINOPHEN 5 MG-325 MG -" .
                  pmbb:prescript4 turbo:TURBO_0006512 "CARDIZEM LA 180 MG OR TAB SR 24HR" .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        medmap.runMedicationMapping(cxn)
        
        val ask1: String = """
          ASK
          {
              pmbb:prescript1 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask2: String = """
          ASK
          {
              pmbb:prescript2 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask3: String = """
          ASK
          {
              pmbb:prescript3 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask4: String = """
          ASK
          {
              pmbb:prescript4 obo:IAO_0000142 ?somemapping .
          }
          """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask4).get should be (true)
    }
    
    test("two valid input one non med input")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:prescript1 a obo:PDRO_0000024 .
                  pmbb:prescript2 a obo:PDRO_0000024 .
                  pmbb:prescript3 a obo:PDRO_0000024 .
                  
                  pmbb:prescript1 turbo:TURBO_0006512 "RYTHMOL 150 MG OR TABS" .
                  pmbb:prescript2 turbo:TURBO_0006512 "ATENOLOL 25 MG OR TABS" .
                  pmbb:prescript3 turbo:TURBO_0006512 "blah blah blah not a medication" .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        medmap.runMedicationMapping(cxn)
        
        val ask1: String = """
          ASK
          {
              pmbb:prescript1 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask2: String = """
          ASK
          {
              pmbb:prescript2 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask3: String = """
          ASK
          {
              pmbb:prescript3 obo:IAO_0000142 ?somemapping .
          }
          """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (false)
    }
    
    test("two valid input one malformed input")
    {
        val insert: String = """
          INSERT DATA
          {
              Graph pmbb:expanded
              {
                  pmbb:prescript1 a obo:PDRO_0000024 .
                  pmbb:prescript2 a obo:PDRO_0000024 .
                  pmbb:prescript3 a obo:PDRO_0000024 .
                  
                  pmbb:prescript1 turbo:TURBO_0006512 "RYTHMOL 150 MG OR TABS" .
                  pmbb:prescript2 turbo:TURBO_0006512 "ATENOLOL 25 MG OR TABS" .
                  pmbb:prescript3 turbo:TURBO_0006512 "1234%^&*%%" .
              }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        
        medmap.runMedicationMapping(cxn)
        
        val ask1: String = """
          ASK
          {
              pmbb:prescript1 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask2: String = """
          ASK
          {
              pmbb:prescript2 obo:IAO_0000142 ?somemapping .
          }
          """
        val ask3: String = """
          ASK
          {
              pmbb:prescript3 obo:IAO_0000142 ?somemapping .
          }
          """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask1).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (true)
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask3).get should be (false)
    }
    
    test("no input to med mapper")
    {    
         medmap.runMedicationMapping(cxn)
         
         val ask: String = "ask {?s ?p ?o .}"
         helper.querySparqlBoolean(cxn, ask).get should be (false)
    }
}