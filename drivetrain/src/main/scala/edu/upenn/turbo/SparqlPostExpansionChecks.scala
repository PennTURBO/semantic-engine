package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

class SparqlPostExpansionChecks extends ProjectwideGlobals {
    
val operation: DataIntegrityCheckOperations = new DataIntegrityCheckOperations
    
def checkForInvalidClasses (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
           val check: String = """
            SELECT *
            WHERE {
                GRAPH  <"""+namedGraph+"""> 
                {
                    ?s a ?c .
                }
                filter not exists {
                    graph pmbb:ontology {
                        ?c a owl:Class
                    }
                }
                FILTER (?c != rdf:Statement)
                FILTER (?c != rdf:object)
                FILTER (?c != rdf:predicate)
                FILTER (?c != rdf:subject)
            }
        """
                
        operation.runSparqlCheck(cxn, check, ArrayBuffer("c", "s"), stage, "found invalid class")
    }
    
    def checkForUnidentifiedRegistryIDs (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
           val check: String = """
            select * where {
            GRAPH  <"""+namedGraph+"""> 
            {
                ?s a turbo:TURBO_0000553 .
                ?s obo:BFO_0000051 ?denoter .
                ?denoter a turbo:TURBO_0000555 .
                ?denoter obo:IAO_0000219 ?diseaseReg
            } 
            minus {
                graph pmbb:ontology {
                    ?diseaseReg a ?i 
                }
            }
        }
        """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("diseaseReg", "denoter"), stage, "found invalid registry id")
    }

    def checkForUnparseableOrUntaggedDates (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
           val check: String = """
            select * where {
            graph pmbb:ontology {
            ?p rdfs:range xsd:date ;
                a owl:DatatypeProperty
            }
            GRAPH  <"""+namedGraph+""">  {
            ?s ?p ?o
            }
            optional {
            bind(year(?o) as ?boundyear)
            }
            filter (!bound(?boundyear))
            }
        """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("o", "s"), stage, "found unparseable date")
    }
    
    def checkThatDateLiteralsHaveValidDatePredicates (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          select * where {
            GRAPH  <"""+namedGraph+""">  {
                ?s ?p ?o
            }
            filter(isliteral(?o))
            filter(datatype(?o) = xsd:date)
            minus {
                graph pmbb:ontology {
                    ?p rdfs:range xsd:date
                } 
            }
            FILTER (?p != rdf:object)
            }
          """
          operation.runSparqlCheck(cxn, check, ArrayBuffer("p", "s", "o"), stage, "found invalid predicate")
    }
    
    def checkForInvalidPredicates (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
        SELECT *
        where {
            {
                GRAPH  <"""+namedGraph+"""> 
                {
                    ?s ?p ?o .
                }
                filter not exists {
                    {
                        graph pmbb:ontology {
                            ?p a ?x
                        }
                    }   
                }
            }
            minus
            {
                ?s rdf:type ?o 
            }
            # use "values" or "in"
            filter (?p !=  rdf:subject)
            filter (?p !=  rdf:predicate)    
            filter (?p !=  rdf:object)
            filter (?p !=  rdfs:label)
        }"""
        operation.runSparqlCheck(cxn, check, ArrayBuffer("p", "s"), stage, "found invalid property")
    }
    
    def checkForSingleInstantiationProcess (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
         if (namedGraph == "http://www.itmat.upenn.edu/biobank/postExpansionCheck")
         {
             val check: String = """
             select ?s
             where {
             GRAPH  <"""+namedGraph+"""> 
             {
                 ?s a turbo:TURBO_0000522 .
    		 }}"""
             operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), stage, "the number of instantiations found was not 1", 2)
         }
         else true
    }
    
    def checkAllInstantiationProcessesAreAttachedToDatasets (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
         val check: String = """
         select ?s
         where {
         GRAPH  <"""+namedGraph+"""> 
         {
             ?s a turbo:TURBO_0000522 .
             MINUS
             {
                 ?s obo:OBI_0000293 ?dataset .
                 ?dataset a obo:IAO_0000100 .
             }
		 }}"""
         operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), stage, "instantiation found without associated dataset", 1)
    }
    
    def checkForSubclassRelationships (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          SELECT ?s ?c
          WHERE {
          GRAPH  <"""+namedGraph+"""> 
          {
              ?s a ?c .
              ?s rdfs:subClassOf ?c .
          }}
          """   
        operation.runSparqlCheck(cxn, check, ArrayBuffer("c", "s"), stage, "instance is subclass of its type")
    }
    
    def checkObjectPropertiesDoNotHaveLiteralObjects (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          SELECT ?p ?o WHERE {
              GRAPH  <"""+namedGraph+"""> 
              {
                  ?s ?p ?o .
              }
              GRAPH pmbb:ontology
              {
                  ?p a owl:ObjectProperty .
              }
              FILTER (isLiteral(?o))
          }
          """
          operation.runSparqlCheck(cxn, check, ArrayBuffer("o", "p"), stage, "Literal found where URI expected")
    }
    
    def checkDatatypePropertiesDoNotHaveUriObjects (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          SELECT ?p ?o WHERE {
              GRAPH  <"""+namedGraph+"""> 
              {
                  ?s ?p ?o .
              }
              GRAPH pmbb:ontology
              {
                  ?p a owl:DatatypeProperty .
              }
              FILTER (isUri(?o))
          }
          """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("o", "p"), stage, "URI found where Literal expected")
    }
    
    def checkParticipantsForRequiredDependents (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          SELECT ?participant WHERE {
              GRAPH  <"""+namedGraph+"""> 
              {
                  ?participant a turbo:TURBO_0000502 .
                  MINUS
                  {
                      ?inst a turbo:TURBO_0000522 .
                      ?inst obo:OBI_0000293 ?dataset .
                      ?dataset a obo:IAO_0000100 .
                      ?pcSymbol obo:BFO_0000050 ?dataset .
                      ?patientCrid a turbo:TURBO_0000503 .
                      ?patientCrid obo:BFO_0000051 ?pcSymbol .
                      ?patientCrid obo:BFO_0000051 ?consenterRegDenoter .
                      ?consenterRegDenoter a turbo:TURBO_0000505 .
                      # ?consenterRegDenoter turbo:TURBO_0006510 ?someVal .
                      ?consenterRegDenoter obo:IAO_0000219 ?consenterRegId .
                      ?consenterRegId a turbo:TURBO_0000506 .
                      ?pcSymbol turbo:TURBO_0006510 ?litVal .
                      ?pcSymbol a turbo:TURBO_0000504 .
                      ?patientCrid obo:IAO_0000219 ?participant .
                      ?participant obo:RO_0000086 ?biosex .
                      ?biosex a obo:PATO_0000047 .
                      ?participant turbo:TURBO_0000303 ?birth .
                      ?birth a obo:UBERON_0035946 .
                      ?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
                      ?dob obo:IAO_0000136 ?birth .
                      ?adipose a obo:UBERON_0001013 .
                      ?participant obo:BFO_0000051 ?adipose .
                      ?participant obo:RO_0000086 ?height .
                		  ?height a obo:PATO_0000119 .
                		  ?participant obo:RO_0000086 ?weight .
                		  ?weight a obo:PATO_0000128 .
                  }
              }
          }
          """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("participant"), stage, "participant has missing dependents")
    }
    
    def checkHealthcareEncountersForRequiredDependents (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val checkHCEnc: String = """
          SELECT ?encounter WHERE {
              GRAPH  <"""+namedGraph+"""> 
              {
                  ?encounter a obo:OGMS_0000097 .
                  MINUS
                  {
                      # commented out a couple lines that were causing the server to crash
                      
                      ?inst a turbo:TURBO_0000522 .
                      ?inst obo:OBI_0000293 ?dataset .
                      ?dataset a obo:IAO_0000100 .
                      
                      #?dataset obo:BFO_0000051 ?hcEncRegDen .
                      #?dataset obo:BFO_0000051 ?hcEncSymb .
                      ?hcEncRegDen obo:BFO_0000050 ?dataset .
                      ?hcEncSymb obo:BFO_0000050 ?dataset .
                      
                      ?hcEncRegId a turbo:TURBO_0000513 .
                      ?hcEncSymb a turbo:TURBO_0000509 .
                      ?hcEncSymb turbo:TURBO_0006510 ?hcEncSymbLit .
                      ?hcEncCrid obo:BFO_0000051 ?hcEncRegDen .
                      ?hcEncRegDen a turbo:TURBO_0000510 .
                      ?hcEncRegDen obo:IAO_0000219 ?hcEncRegId .
                      ?hcEncCrid obo:BFO_0000051 ?hcEncSymb .
                      ?hcEncCrid a turbo:TURBO_0000508 .
                      ?hcEncCrid obo:IAO_0000219 ?encounter .
                      ?encStart obo:RO_0002223 ?encounter .
                      ?encStart a turbo:TURBO_0000511 .
                      ?encDate obo:IAO_0000136 ?encStart .
                      ?encDate a turbo:TURBO_0000512 .
                  }
              }
          }
          """
         operation.runSparqlCheck(cxn, checkHCEnc, ArrayBuffer("encounter"), stage, "healthcare encounter has missing dependents")
    }
    
    def checkBiobankEncountersForRequiredDependents (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val checkBBEnc: String = """
          SELECT ?encounter WHERE {
              GRAPH  <"""+namedGraph+"""> 
              {
                  ?encounter a turbo:TURBO_0000527 .
                  MINUS
                  {
                      ?inst a turbo:TURBO_0000522 .
                      ?inst obo:OBI_0000293 ?dataset .
                      ?dataset a obo:IAO_0000100 .
                      ?bbEncSymb obo:BFO_0000050 ?dataset .
            		      ?bbEncRegDen obo:BFO_0000050 ?dataset .
                      ?bbEncRegId a turbo:TURBO_0000543 .
                      ?bbEncSymb a turbo:TURBO_0000534 .
                      ?bbEncSymb turbo:TURBO_0006510 ?bbEncSymbLit .
                      ?bbEncCrid obo:BFO_0000051 ?bbEncRegDen .
                      ?bbEncRegDen a turbo:TURBO_0000535 .
                      ?bbEncRegDen obo:IAO_0000219 ?bbEncRegId .
                      # ?bbEncRegDen turbo:TURBO_0006510 ?regLit .
                      ?bbEncCrid obo:BFO_0000051 ?bbEncSymb .
                      ?bbEncCrid a turbo:TURBO_0000533 .
                      ?bbEncCrid obo:IAO_0000219 ?encounter .
                      ?encStart obo:RO_0002223 ?encounter .
                      ?encStart a turbo:TURBO_0000531 .
                      ?encDate obo:IAO_0000136 ?encStart .
                      ?encDate a turbo:TURBO_0000532 .
                  }
              }
          }
          """
        operation.runSparqlCheck(cxn, checkBBEnc, ArrayBuffer("encounter"), stage, "biobank encounter has missing dependents")
    }
    
    //eventually make dynamic using now function again 
    def checkAllDatesAreReasonable(cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          SELECT * WHERE {
              GRAPH  <"""+namedGraph+"""> 
              {
                  ?date ?p ?dateLit .
              }
              GRAPH pmbb:ontology
              {
                  ?p rdfs:range xsd:date ;
                  a owl:DatatypeProperty
              }
        		  # BIND (STRDT(substr(str(NOW()),1,10), xsd:date) AS ?nowTime)
                  FILTER (?dateLit < "1900-01-01"^^xsd:date || ?dateLit > "2019-01-01"^^xsd:date)
          }
          """
              
        operation.runSparqlCheck(cxn, check, ArrayBuffer("dateLit", "date", "dateType"), stage, "found unreasonable date")
    }
    
    def noShortcutRelationsInGraph (cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
           val check: String = """
              SELECT * WHERE
              {
                  VALUES ?shortcutCategory {turbo:TURBO_0000621 turbo:TURBO_0000641 turbo:TURBO_0000601}
                  GRAPH <"""+namedGraph+""">
                  {
                      ?s ?p ?o .
                  }
                  GRAPH pmbb:ontology
                  {
                      ?p rdfs:subPropertyOf ?shortcutCategory .
                  }
              }
              """
            
            operation.runSparqlCheck(cxn, check, ArrayBuffer("p"), stage, "found illegal shortcut in expanded graph") 
    }
    
    def allEntitiesAreReftracked(cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
              SELECT ?instance ?type WHERE
              {
                  graph <"""+namedGraph+""">
                  {
                      VALUES ?type 
                      {
                          turbo:TURBO_0000502
                          turbo:TURBO_0000503
                          turbo:TURBO_0000504
                          turbo:TURBO_0000505
                          obo:PATO_0000047
                          obo:UBERON_0035946
                          obo:UBERON_0001013
                          obo:PATO_0000119
                          obo:PATO_0000128
                          turbo:TURBO_0000527
                          turbo:TURBO_0000531
                          turbo:TURBO_0000532
                          turbo:TURBO_0000533
                          turbo:TURBO_0000534
                          turbo:TURBO_0000535
                          obo:OGMS_0000097
                          turbo:TURBO_0000508
                          turbo:TURBO_0000509
                          turbo:TURBO_0000510
                          turbo:TURBO_0000511
                          turbo:TURBO_0000512
                          
                          #Bmi/Bmi Val spec
                          efo:EFO_0004340
                          obo:OBI_0001933
                      }
                      ?instance a ?type .
                      MINUS
                      {
                          ?instance turbo:TURBO_0006500 'true'^^xsd:boolean .
                      }
                  }
              }
              """
            
            operation.runSparqlCheck(cxn, check, ArrayBuffer("instance", "type"), stage, "found a non-reftracked entity") 
    }
    
    def allBMIsAreConclusionated(cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val countBBEncountersWithoutConc: String = """
          Select ?bbEnc Where
          {
              GRAPH pmbb:expanded 
              {
                  ?participant a turbo:TURBO_0000502 .
                  ?participant obo:RO_0000056 ?bbEnc .
                  ?bbEnc a turbo:TURBO_0000527 ;
                         turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?bbEncStart obo:RO_0002223 ?bbEnc .
                  ?bbEncStart a turbo:TURBO_0000531 .
                  ?bbEncDate obo:IAO_0000136 ?bbEncStart .
                  ?bbEncDate a turbo:TURBO_0000532 .
                  ?bbEncDate turbo:TURBO_0006511 ?dateVal .
              }
                  
                  Minus
                  {
                      Graph <"""+namedGraph+""">
                      {
                          ?mk a obo:OBI_0000852 .
                          ?mk obo:IAO_0000136 ?bbEnc .
                      }
                  }
                  
                  Minus
                  {
                      Graph <"""+namedGraph+""">
                      {
                          ?concBMI a efo:EFO_0004340 .
                          ?concBMI turbo:TURBO_0006501 'true'^^xsd:boolean .
                          ?concBMI obo:IAO_0000581 ?bbEncDate .
                          ?concBMI obo:OBI_0001938 ?concBMIValSpec .
                          ?concBMIValSpec a obo:OBI_0001933 .
                        	?concBMIValSpec obo:OBI_0002135 ?someVal .
                      }
                  }
          }
          """
        operation.runSparqlCheck(cxn, countBBEncountersWithoutConc, ArrayBuffer("bbEnc"), stage, "found a biobank encounter w/o conclusionation")
    }
    
    def allBiosexAreConclusionated(cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        val check: String = """
          Select ?biosex Where
          {
              Graph pmbb:expanded
              {
                  ?biosex a obo:PATO_0000047 .
              }
              MINUS
              {
                  Graph <"""+namedGraph+""">
                  {
                      VALUES ?biosextype {obo:PATO_0000047 obo:PATO_0000383 obo:PATO_0000384}
                      ?biosex a ?biosextype .
                      ?biosex turbo:TURBO_0006501 'true'^^xsd:boolean .
                  }
              }
          }
          """
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("biosex"), stage, "found a non-conclusionated Biosex")
    }
    
    def allBirthsAreConclusionated(cxn: RepositoryConnection, namedGraph: String, stage: String): Boolean =
    {
        //should this also check that there is a value associated with the conclusion?
        val check: String = """
          Select ?birth Where
          {
              Graph pmbb:expanded
              {
                  ?birth a obo:UBERON_0035946 .
              }
              MINUS
              {
                  Graph <"""+namedGraph+""">
                  {
                      VALUES ?conctype {efo:EFO_0004950 obo:OBI_0000852}
                      ?conc obo:IAO_0000136 ?birth .
                      ?conc a ?conctype .
                  }
              }
          }
          """
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("birth"), stage, "found a non-conclusionated Birth")
    }
}    
    