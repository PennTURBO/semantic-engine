package edu.upenn.turbo
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader
import java.io.PrintWriter
import org.eclipse.rdf4j.model.Value

// if there was a global variable for the conclusionated graph(s),
// we could include them instead of excluding everything else
// using values ?g {}
// would that execute faster?

// just inserted dron (without NDC)... a medication mapper class is being developed that will load them from bitbucket URIs

class I2I2C2C extends ProjectwideGlobals {
  def runAllI2I2C2CQueries(cxn: RepositoryConnection) {
    
    val queriesList: ArrayBuffer[String] = ArrayBuffer(
        
        // get object relations
        """
            insert 
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    ?sc ?p  ?oc
                }
            }
            where {
                graph ?g {
                    ?s ?p ?o .
                    optional {
                        ?s a ?sc
                    }
                    optional {
                        ?o a ?oc
                    }
                }
                # filter(?g!=<http://transformunify.org/ontologies/ontology>)
                filter (?g not in ( <http://www.itmat.upenn.edu/biobank/dron>, <http://www.itmat.upenn.edu/biobank/ICD10Ontology>, 
                  <http://www.itmat.upenn.edu/biobank/ICD9Ontology>, <http://www.itmat.upenn.edu/biobank/mondoOntology>, 
                  <http://www.itmat.upenn.edu/biobank/ontology>, <http://www.itmat.upenn.edu/biobank/i2i2c2c>))
                filter(isuri(?o))
                filter(?p!=rdf:type)
                filter(bound(?sc))
                filter(bound(?oc))
            }""",
        
        // get data relations
       """
            insert 
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    ?sc ?p  ?odt
                }
            }
            where {
                graph ?g {
                    ?s ?p ?o .
                    optional {
                        ?s a ?sc
                    }
                    optional {
                        bind(datatype(?o) as ?odt)
                    }
                }
                # filter(?g!=<http://transformunify.org/ontologies/ontology>)
                filter (?g not in ( <http://www.itmat.upenn.edu/biobank/dron>, <http://www.itmat.upenn.edu/biobank/ICD10Ontology>, 
                  <http://www.itmat.upenn.edu/biobank/ICD9Ontology>, <http://www.itmat.upenn.edu/biobank/mondoOntology>, 
                  <http://www.itmat.upenn.edu/biobank/ontology>, <http://www.itmat.upenn.edu/biobank/i2i2c2c>))                
                filter(?p!=rdfs:label)
                # could filter out turbo 1700, 6601, 6602, 6500, 6501
                # currerntly doing that in visual graph expansion statmetn
                filter(bound(?sc))
                filter(bound(?odt))
            }
          """,
          
          // get class object relations from statements
        """
            insert
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    rdf:Statement ?p ?ot 
                }
            }
            where 
            {
                values ?st {
                    rdf:Statement
                }
                ?o a ?ot .
                ?s a ?st .
                ?s ?p ?o .
                filter (?p != rdf:type)
                filter (?p != rdfs:label)
                filter (!isblank(?ot))
                FILTER( ?p != rdf:predicate )
            }
          """,
    
          
          // get property object relations from statements
           """
            insert
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    rdf:Statement ?p ?prop 
                }
            }
            where 
            {
                values ?st {
                    rdf:Statement
                }
                values ?p {
                    rdf:predicate
                }
                ?s a ?st .
                ?s ?p ?prop .
            }
          """,
          
          // get type assertions from statements
           """
            insert
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    rdf:Statement rdf:predicate rdf:type .
                    rdf:Statement rdf:object ?x .
                }
            }
            where {
                ?s a rdf:Statement .
                ?s rdf:predicate rdf:type .
                ?s rdf:object ?x
            } 
          """,
          
          // get data relations from statements
            """
            insert 
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    rdf:Statement ?p  ?odt
                }
            }
            where {
                graph ?g {
                    ?s a rdf:Statement .
                    ?s ?p ?o .
                    optional {
                        ?s a ?sc
                    }
                    optional {
                        bind(datatype(?o) as ?odt)
                    }
                }
                # filter(?g!=<http://transformunify.org/ontologies/ontology>)
                filter (?g not in ( <http://www.itmat.upenn.edu/biobank/dron>, <http://www.itmat.upenn.edu/biobank/ICD10Ontology>, 
                  <http://www.itmat.upenn.edu/biobank/ICD9Ontology>, <http://www.itmat.upenn.edu/biobank/mondoOntology>, 
                  <http://www.itmat.upenn.edu/biobank/ontology>, <http://www.itmat.upenn.edu/biobank/i2i2c2c>))                
                filter(?p!=rdfs:label)
                # could filter out turbo 1700, 6601, 6602, 6500, 6501
                # currerntly doing that in visual graph expansion statmetn
                filter(bound(?sc))
                filter(bound(?odt))
            }
          """,
          
          // get subclass relations of classes actually instantiated 
            """
            insert 
            {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    ?c rdfs:subClassOf ?s .
                }
            }
            where {
                graph ?g {
                    ?i a ?c .
                }
                graph <http://www.itmat.upenn.edu/biobank/ontology> {
                    ?c rdfs:subClassOf ?s .
                }
                            filter (?g not in ( <http://www.itmat.upenn.edu/biobank/dron>, <http://www.itmat.upenn.edu/biobank/ICD10Ontology>, 
                              <http://www.itmat.upenn.edu/biobank/ICD9Ontology>, <http://www.itmat.upenn.edu/biobank/mondoOntology>, 
                              <http://www.itmat.upenn.edu/biobank/ontology>, <http://www.itmat.upenn.edu/biobank/i2i2c2c>))
                            filter(!isblank(?s))
            }
          """,
          
          // diagnoses and prescriptions mention things, but those things are classes
          // so these queries don't say what "kind of classes" they are
          // do that by hand
      
          """
          insert data {
          graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
          {
              <http://purl.obolibrary.org/obo/PDRO_0000024> <http://purl.obolibrary.org/obo/IAO_0000142> obo:DRON_00000005 .
              obo:OGMS_0000073 obo:IAO_0000142 <http://purl.obolibrary.org/obo/DOID_4> .
              <http://purl.obolibrary.org/obo/DOID_4> rdfs:label "disease" .
          }
          }
          """,
          
          // add all turbo labels to i2i2c2c
          // could limit this to just instanatiated classes

          """
          insert {
              graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
              {
                  ?c rdfs:label ?l     
              }
          } where {
              graph <http://www.itmat.upenn.edu/biobank/ontology> {
                  ?c rdfs:label ?l
              }
          }
          """,
          
          // remove statements with blank node subjects
          // may not be necessary with more careful filtering of named graphs in insert statements
          // or running i2i2c2c before loading mondo, icdx, dron, etc.
      
          """
            delete {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    ?s ?p ?o    
                }
            } 
            where {
                graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                {
                    ?s ?p ?o  
                }
                filter(isblank(?s))
            }
                """,
                
             // remove statements with blank node objects
              """
              delete {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                  {
                      ?s ?p ?o    
                  }
              } 
              where {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                      ?s ?p ?o  
                  }
                  filter(isblank(?o))
              }
                    """, 
                    
              // remove statements with owl vocabulary objects
              """
              delete {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                  {
                      ?s ?p ?o  
                  }
              }
              where
              {
                  values ?o {
                      <http://www.w3.org/2002/07/owl#NamedIndividual> <http://www.w3.org/2002/07/owl#Thing> 
                  }
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                      ?s ?p ?o  
                  }
              }
                        """,
              
                  // remove statements with selected predicates
                  """
              delete {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c> 
                  {
                      ?s ?p ?o  
                  }
              }
              where
              {
                  values ?p {
                      #<http://www.w3.org/2000/01/rdf-schema#seeAlso>
                      #<http://www.w3.org/2000/01/rdf-schema#subClassOf>
                      <http://www.w3.org/2002/07/owl#deprecated>
                      #<http://www.w3.org/2002/07/owl#versionInfo>
                      dc11:contributor
                      dc11:creator
                      dc11:date
                      obo:IAO_0000111
                      obo:IAO_0000112
                      obo:IAO_0000114
                      obo:IAO_0000115
                      obo:IAO_0000116
                      obo:IAO_0000117
                      obo:IAO_0000118
                      obo:IAO_0000119
                  }
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                      ?s ?p ?o  
                  }
              }
                        """,
              
                  // remove statements with owl vocabulary subjects
                 """
              delete {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                      ?s ?p ?o  
                  }
              }
              where
              {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                  ?s ?p ?o .
                  filter(strstarts(str(?s), "http://www.w3.org/2002/07/owl#"))
                  }
              }
                        """,
              
                  // remove statements with ontologies as subjects
                  """
              delete {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                      ?s ?p ?o  
                  }
              }
              where
              {
                  graph <http://www.itmat.upenn.edu/biobank/i2i2c2c>
                  {
                  ?s ?p ?o .
                  filter(regex(str(?s), ".owl$"))
                  }
              }
          """
    )
   
    //run all queries in arraybuffer
    var count = 0
    for (query <- queriesList)
    {
        count = count + 1
        helper.updateSparql(cxn, sparqlPrefixes + query)
        logger.info("Ran query " + count + " starting with " + query.replaceAll(" ", "").substring(0, 8))
    }
    logger.info("Ran " + count + " i2i2c2c queries")
  }
  
  def exportCSVsFromQueries(cxn: RepositoryConnection)
  {
      exportThreeColumnSpreadsheet(cxn)
      exportFourColumnSpreadsheet(cxn)
  }
  
    def exportThreeColumnSpreadsheet(cxn: RepositoryConnection)
    {
        //create 3 column spreadsheet
        logger.info("Creating three column spreadsheet")
        val threeClmnSht: PrintWriter = new PrintWriter (new File ("i2i2c2cThreeColumns" + helper.getCurrentTimestamp() + ".csv"))
        threeClmnSht.println("sc/ot,count,class or lit")
        
        logger.info("Collecting instance type assertion counts")
        val instanceTypeAssertionCounts: String = """
          select ?sc (count(?s) as ?somecount) ("class" as ?classorlit) {
            graph ?g {
                ?s a ?sc .
            }
            filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
            }
            group by ?sc
          """
        val instanceTypeAssertionCountsResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
            sparqlPrefixes + instanceTypeAssertionCounts, Array("sc", "somecount", "classorlit"))
        for (result <- instanceTypeAssertionCountsResults) threeClmnSht.println(result(0) + "," + result(1).toString.split("\\^")(0) + "," + result(2).toString.split("\\^")(0))
        
        logger.info("Collecting literal type instance counts")
        val literalTypeInstanceCounts: String = """
          select 
          ?ot (count(?s) as ?somecount) ("literal" as ?classorlit)
          where {
              graph ?g {
                  ?s ?p ?o .
                  bind(datatype(?o) as ?ot)
                  filter(isliteral(?o))
                  #        filter(?p != rdf:type)
              }
            filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
          } 
          group by ?ot
          """
          val literalTypeInstanceCountsResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + literalTypeInstanceCounts, Array("ot", "somecount", "classorlit"))
          for (result <- literalTypeInstanceCountsResults) threeClmnSht.println(result(0) + "," + result(1).toString.split("\\^")(0) + "," + result(2).toString.split("\\^")(0))
          
          threeClmnSht.close()
    }
    
    def exportFourColumnSpreadsheet(cxn: RepositoryConnection)
    {
        //create 4 column spreadsheet
        logger.info("Creating four column spreadsheet")
        val fourClmnSht: PrintWriter = new PrintWriter (new File ("i2i2c2cFourColumns" + helper.getCurrentTimestamp() + ".csv"))
        fourClmnSht.println("sc,p,oc,count")
        
        logger.info("Collecting datatype patterns")
        val datatypePatterns: String = """
          select ?sc ?p ?oc (count(?s) as ?somecount) where {
          graph ?g {
              ?s ?p ?o .
              ?s a ?sc .
              filter (isliteral(?o))
              bind(datatype(?o) as ?oc ).
          }
          filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
          } 
          #order by ?sc ?p ?oc
          #limit 100
          group by ?sc ?p ?oc"""
        
          val datatypePatternsResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + datatypePatterns, Array("sc", "p", "oc", "somecount"))
          for (result <- datatypePatternsResults) fourClmnSht.println(result(0) + "," + result(1) + "," + result(2) + "," + result(3).toString.split("\\^")(0))
        
          logger.info("Collecting object rel counts")
          val objRelCnts: String = """
          select ?sc ?p ?oc (count(?s) as ?somecount) where {
          graph ?g {
              ?s ?p ?o .
              ?s a ?sc .
              ?o a ?oc .
          }
          filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
          } 
          #order by ?sc ?p ?oc
          #limit 100
          group by ?sc ?p ?oc"""
        
          val objRelCntsResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + objRelCnts, Array("sc", "p", "oc", "somecount"))
          for (result <- objRelCntsResults) fourClmnSht.println(result(0) + "," + result(1) + "," + result(2) + "," + result(3).toString.split("\\^")(0))
          
          logger.info("Collecting predicate usage - non owl objects")
          val predUsgeNonOwl: String = """
          select  ?sc ?p ?oc (count(?s) as ?somecount)
          where {
              values ?sc {
                  <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> 
              }
              ?s a  ?sc .
              ?s ?p ?o .
              ?o a ?oc
              filter(!strstarts(str(?oc), "http://www.w3.org/2002/07/owl#"))
          } 
          group by ?sc ?p ?oc"""
        
          val predUsgeNonOwlResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + predUsgeNonOwl, Array("sc", "p", "oc", "somecount"))
          for (result <- predUsgeNonOwlResults) fourClmnSht.println(result(0) + "," + result(1) + "," + result(2) + "," + result(3).toString.split("\\^")(0))
          
          logger.info("Collecting statement class objects")
          val stmtClsObj: String = """
          select  ?sc ?p ?oc (count(?s) as ?somecount) where {
          graph ?g {
              ?s a ?sc .
              values ?sc {
                  rdf:Statement 
              }
              ?s ?p ?oc .
              values ?p {
                  rdf:object
              }
              filter(isuri(?oc))
          }
          graph pmbb:ontology {
              ?oc a owl:Class 
          }
          filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
          } 
          #order by ?sc ?p ?oc
          #limit 100
          group by  ?sc ?p ?oc
          #pmbb:ontology
          #pmbb:dron
          #pmbb:mondoOntology
          #pmbb:ICD9Ontology
          #pmbb:ICD10Ontology
          #
          #pmbb:i2i2c2c
          order by asc(?oc )"""
        
          val stmtClsObjResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + stmtClsObj, Array("sc", "p", "oc", "somecount"))
          for (result <- stmtClsObjResults) fourClmnSht.println(result(0) + "," + result(1) + "," + result(2) + "," + result(3).toString.split("\\^")(0))
          
          logger.info("Collecting statement pred counts")
          val stmtPredCnts: String = """
          select ?sc ?p ?oc (count(?s) as ?somecount) where {
          graph ?g {
              values ?p {
                  rdf:predicate
              }
              values ?sc {
                  rdf:Statement 
              }
              ?s ?p ?oc .
              ?s a ?sc .
          }
          filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
          } 
          #order by ?sc ?p ?oc
          #limit 100
          group by ?sc ?p ?oc"""
        
          val stmtPredCntResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + stmtPredCnts, Array("sc", "p", "oc", "somecount"))
          for (result <- stmtPredCntResults) fourClmnSht.println(result(0) + "," + result(1) + "," + result(2) + "," + result(3).toString.split("\\^")(0))
          
          logger.info("Collecting statement subject counts")
          val stmtSubjCnts: String = """
          select  ?sc ?p ?oc (count(?s) as ?somecount) where {
          graph ?g {
              ?s a ?sc .
              values ?sc {
                  rdf:Statement 
              }
              ?s ?p ?o .
              values ?p {
                  rdf:subject
              }
              ?o a ?oc
              filter(isuri(?oc))
          }
          filter (?g not in (
            pmbb:ontology,
            pmbb:mondoOntology,
            pmbb:mondoOntology,
            pmbb:drugOntologies,
            pmbb:ICD9Ontology,
            pmbb:ICD10Ontology))
          } 
          #order by ?sc ?p ?oc
          #limit 100
          group by  ?sc ?p ?oc
          
          order by asc(?oc )"""
        
          val stmtSubjCntResults: ArrayBuffer[ArrayBuffer[Value]] = helper.querySparqlAndUnpackTuple(cxn, 
              sparqlPrefixes + stmtSubjCnts, Array("sc", "p", "oc", "somecount"))
          for (result <- stmtSubjCntResults) fourClmnSht.println(result(0) + "," + result(1) + "," + result(2) + "," + result(3).toString.split("\\^")(0))
          
          fourClmnSht.close()
    }
}