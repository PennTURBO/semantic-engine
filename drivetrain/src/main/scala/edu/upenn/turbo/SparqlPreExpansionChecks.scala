package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

class SparqlPreExpansionChecks extends ProjectwideGlobals {

//Several checks here need to be updated to support TURBO_0000527s
    
val operation: DataIntegrityCheckOperations = new DataIntegrityCheckOperations

def checkForTurboOntology (cxn: RepositoryConnection): Boolean =
{
    val check: String = "select * where {graph pmbb:ontology {?s ?p ?o .}}"
    val explanation: String = 
    """
    Did not find the TURBO ontology, which is required to run expansion. 
    Load the ontology by using command 'loadTurboOntology'.
    """
    operation.runSparqlCheck(cxn, check, ArrayBuffer(), "pre-expansion", explanation, 3)
}

def checkForValidParticipantBirthShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {   
        val check: String =
        """SELECT ?dateOK ?missing ?dataset 
          """ + graphsList + """
            WHERE 
            {
                  ?part a turbo:TURBO_0000502 .
                  OPTIONAL {
                      ?part turbo:TURBO_0000604 ?value1 .
                  }
                  OPTIONAL {
                      ?part turbo:TURBO_0000605 ?value2 .
                  }
                  BIND(IF((BOUND(?value2)) && (!(BOUND(?value1))), 'false', 'true') AS ?dateOK)
                  BIND (turbo:TURBO_0000604 AS ?missing)
                  FILTER (?dateOK = 'false')       
            }"""
          
        operation.runSparqlCheck(cxn, check, ArrayBuffer("missing", "dateOK"), "pre-expansion", "missing required birth-related shortcut")
    }
    
    def checkForValidParticipantBiosexShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?biosexOK ?missing ?dataset 
          """ + graphsList + """
          WHERE 
            {
                  ?part a turbo:TURBO_0000502 .
                  OPTIONAL {
                      ?part turbo:TURBO_0000606 ?value1 .
                  }
                  OPTIONAL {
                      ?part turbo:TURBO_0000607 ?value2 .
                  }
                  BIND(IF((BOUND(?value2)) && (!(BOUND(?value1))), 'false', 'true') AS ?biosexOK)
                  BIND (turbo:TURBO_0000606 AS ?missing)
                  FILTER (?biosexOK = 'false')        
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("missing", "biosexOK"), "pre-expansion", "missing required biosex-related shortcut")
    }
    
    def checkForValidParticipantRaceShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?raceOK ?missing ?dataset 
          """ + graphsList + """
          WHERE 
            {
                  ?part a turbo:TURBO_0000502 .
                  OPTIONAL {
                      ?part turbo:TURBO_0000614 ?value1 .
                  }
                  OPTIONAL {
                      ?part turbo:TURBO_0000615 ?value2 .
                  }
                  BIND(IF((BOUND(?value2)) && !(BOUND(?value1)) || (Bound(?value1) && !Bound(?value2)), 'false', 'true') AS ?raceOK)
                  BIND (turbo:TURBO_0000614 AS ?missing)
                  FILTER (?raceOK = 'false')        
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("missing", "raceOK"), "pre-expansion", "missing required race-related shortcut")
    }
    
    def checkBiosexURIsAreValid (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?biosexURI ?part 
          """ + graphsList + """
          WHERE 
            {
                  ?part a turbo:TURBO_0000502 .
                  ?part turbo:TURBO_0000607 ?biosexURI .
                 
            			Minus
                  {
          						?part turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI
                  }
              	  Minus
                  {
          						?part turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI
                  }
              		Minus
                  {
          						?part turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000133"^^xsd:anyURI
                  }       
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("biosexURI", "part"), "pre-expansion", "found invalid biosex URI")
    }
    
    def checkEncounterRegistryURIsAreValid (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?regURI WHERE 
            {
                Values ?g {""" + graphsList + """}
                GRAPH ?g {
                    ?enc a obo:OGMS_0000097 .
                    ?enc turbo:TURBO_0000663 ?regURI .
                    MINUS
        			      {
                        ?enc turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
              			}
              			MINUS
              			{
                        ?enc turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
        			      }
                }        
            }"""
        operation.runSparqlCheck(cxn, check, ArrayBuffer("regURI"), "pre-expansion", "found invalid encounter registry uri")
    }
    
    def checkForValidHealthcareEncounterDateShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?enc 
          """ + graphsList + """
          WHERE 
            {
                ?enc a obo:OGMS_0000097 .
                ?enc turbo:TURBO_0000645 ?value2 .
                Minus {
                    ?enc turbo:TURBO_0000644 ?value1 .
                }       
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("enc"), "pre-expansion", "missing required healthcare encounter date-related shortcut")
    }
    
    def checkForValidBiobankEncounterDateShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?dateOK ?missing 
          """ + graphsList + """
          WHERE 
            {
                  ?enc a turbo:TURBO_0000527 .
                  OPTIONAL {
                      ?enc turbo:TURBO_0000624 ?value1 .
                  }
                  OPTIONAL {
                      ?enc turbo:TURBO_0000625 ?value2 .
                  }
                  BIND(IF((BOUND(?value2)) && (!(BOUND(?value1))), 'false', 'true') AS ?dateOK)
                  BIND (turbo:TURBO_0000644 AS ?missing)
                  FILTER (?dateOK = 'false')      
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("missing", "dateOK"), "pre-expansion", "missing required biobank encounter date-related shortcut")
    }
    
    def checkForValidEncounterDiagnosisShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?diagnosis ?g WHERE 
            {
                Values ?g {""" + graphsList + """}
                GRAPH ?g {
                    ?diagnosis a obo:OGMS_0000073 .
        			      MINUS {
                        ?diagnosis turbo:TURBO_0004602 ?value1 .
                        ?enc a obo:OGMS_0000097 .
                        ?enc obo:RO_0002234 ?diagnosis .
                    }
                }
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("diagnosis", "g"), "pre-expansion", "missing required encounter diagnosis shortcut")
    }
    
    def checkForValidEncounterPrescriptionShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String =
        """SELECT ?prescription ?g WHERE 
            {
                Values ?g {""" + graphsList + """}
                GRAPH ?g {
                    ?prescription a obo:PDRO_0000001 .
        			      MINUS {
        			          ?enc a obo:OGMS_0000097 .
                        ?enc obo:RO_0002234 ?prescription .
                        # making order name optional on 5/4 due to karma issues handling a blank input field. See Issue #211.
                        # ?prescription turbo:TURBO_0005611  ?medString .
            		        ?prescription turbo:TURBO_0005601  ?medId .
                    }
                }
            }"""
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("prescription", "g"), "pre-expansion", "missing required encounter prescription shortcut")
    }
    
    def checkForRequiredParticipantShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
            SELECT distinct ?part WHERE 
            {
                Values ?g {""" + graphsList + """}
                GRAPH ?g {
                    ?part a turbo:TURBO_0000502 .
                    MINUS {
                        ?part turbo:TURBO_0000603 ?someval1 .
                  			?part turbo:TURBO_0000608 ?someval2 .
                  			?part turbo:TURBO_0000610 ?someval3 .
                    }
                }        
            }"""
        operation.runSparqlCheck(cxn, check, ArrayBuffer("part"), "pre-expansion", "minimum for participant not found")
    }
    
    def checkForRequiredHealthcareEncounterShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
            SELECT distinct ?enc
            """ + graphsList + """
            WHERE 
            {
                  ?enc a obo:OGMS_0000097 .
                  MINUS 
                  {
                      ?enc turbo:TURBO_0000643 ?somevalue1 .
                      ?enc turbo:TURBO_0000648 ?somevalue2 .
                      ?enc turbo:TURBO_0000650 ?somevalue3 .
                  }     
            }"""
            
        operation.runSparqlCheck(cxn, check, ArrayBuffer("enc"), "pre-expansion", "minimum for healthcare encounter not found")
    }
    
    def checkForRequiredBiobankEncounterShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
            SELECT distinct ?enc
            WHERE 
            {
                Values ?g {""" + graphsList + """}
                GRAPH ?g {
                    ?enc a turbo:TURBO_0000527 .
                    MINUS
                    {
                        ?enc turbo:TURBO_0000623 ?somevalue1 .
                        ?enc turbo:TURBO_0000628 ?somevalue2 .
                        ?enc turbo:TURBO_0000630 ?somevalue3 .
                    }
                }        
            }"""
        operation.runSparqlCheck(cxn, check, ArrayBuffer("enc"), "pre-expansion", "minimum for biobank encounter not found")
    }
    
    def checkForRequiredLossOfFunctionShortcuts (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
            SELECT distinct ?enc
            WHERE 
            {
                Values ?g {""" + graphsList + """}
                GRAPH ?g {
                    ?allele a obo:OBI_0001352 .
                    MINUS
                    {
                        ?allele turbo:TURBO_0007607 ?zygosityValURI ;
          	            turbo:TURBO_0007601 ?bbEncSymb ;
          	            turbo:TURBO_0007606 ?zygosityValText ;
          	            turbo:TURBO_0007602 ?genomeCridSymbLit ;
          	            turbo:TURBO_0007603 ?genomeReg ;
          	            turbo:TURBO_0007505 ?geneText ;
          	            turbo:TURBO_0007608 ?datasetTitle ;
          	            turbo:TURBO_0007609 ?bbEncReg .
                    }
                }        
            }"""
        operation.runSparqlCheck(cxn, check, ArrayBuffer("enc"), "pre-expansion", "minimum for loss of function allele not found")
    }
    
    def checkForMultipleParticipantDependentNodes (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
        select ?s
        (count (distinct ?TURBO_0000608) as ?PSCCount)
        (count (distinct ?TURBO_0000603) as ?DataSetCount)
        (count (distinct ?TURBO_0000604) as ?DOBTextCount)
        (count (distinct ?TURBO_0000605) as ?DOBXsdCount) 
        (count (distinct ?TURBO_0000606) as ?GIDTextCount) 
        (count (distinct ?TURBO_0000607) as ?GIDUriCount) 
        (count (distinct ?type) as ?typeCount) 
        (count (distinct ?TURBO_0000610) as ?registryCount)
        (count (distinct ?TURBO_0000609) as ?regDenCount)
        """ + graphsList + """
        where {
            ?s a :TURBO_0000502 .
            ?s a ?type .
            optional {
                ?s :TURBO_0000604 ?TURBO_0000604 
            } .
            optional {
                ?s   :TURBO_0000607 ?TURBO_0000607 
            } .
            optional {
                ?s   :TURBO_0000606 ?TURBO_0000606 
            } .
            optional {
                ?s   :TURBO_0000605 ?TURBO_0000605 
            } .
            optional {
                ?s   :TURBO_0000608 ?TURBO_0000608 
            } .
            optional {
                ?s   :TURBO_0000603 ?TURBO_0000603 
            } .
            optional {
                ?s   :TURBO_0000610 ?TURBO_0000610 
            } .
            optional {
                ?s   :TURBO_0000609 ?TURBO_0000609 
            } .
        } group by ?s
        having (
            ?PSCCount > 1 || 
            ?DataSetCount > 1  || 
            ?DOBTextCount > 1  || 
            ?DOBXsdCount > 1  || 
            ?GIDTextCount > 1  || 
            ?GIDUriCount > 1 ||
            ?typeCount > 1 ||
            ?registryCount > 1 ||
            ?regDenCount > 1
        )
        """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "multiple shortcut relationships on one participant")
    }
    
    def checkForMultipleBiobankEncounterDependentNodes (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
        select ?s 
        (count (distinct ?dataset) as ?datasetCount)
        (count (distinct ?encSymb) as ?encSymbCount)
        (count (distinct ?type) as ?typeCount)
        (count (distinct ?registry) as ?registryCount) 
        (count (distinct ?regDen) as ?regDenCount) 
        (count (distinct ?BMI) as ?BMICount) 
        (count (distinct ?weight) as ?weightCount) 
        (count (distinct ?height) as ?heightCount)
        (count (distinct ?dateText) as ?dateTextCount)
        (count (distinct ?dateXsd) as ?dateXsdCount)
        """ + graphsList + """
        where
        {
                ?s a turbo:TURBO_0000527 .
                ?s a ?type .
                optional {
                  ?s turbo:TURBO_0000623 ?dataset 
                } .
                optional {
                    ?s turbo:TURBO_0000628 ?encSymb . 
                } .
                optional {
                    ?s turbo:TURBO_0000630 ?registry 
                } .
                optional {
                    ?s turbo:TURBO_0000629 ?regDen  
                } .
                optional {
                    ?s turbo:TURBO_0000635 ?BMI 
                } .
                optional {
                    ?s turbo:TURBO_0000627 ?weight 
                } .
                optional {
                    ?s turbo:TURBO_0000626 ?height 
                } .
                optional {
                    ?s turbo:TURBO_0000624 ?dateText  
                } .
                optional {
                    ?s turbo:TURBO_0000625 ?dateXsd 
                } .
        }
        group by ?s
        having (
            ?datasetCount > 1 || 
            ?encSymbCount > 1  || 
            ?typeCount > 1  || 
            ?registryCount > 1  || 
            ?regDenCount > 1  || 
            ?BMICount > 1 ||
            ?weightCount > 1 ||
            ?heightCount > 1 ||
            ?dateTextCount > 1 ||
            ?dateXsdCount > 1
        )
        """
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "multiple shortcut relationships on one biobank encounter")
    }
    
    def checkForMultipleHealthcareEncounterDependentNodes (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
        select ?s 
        (count (distinct ?encSymb) as ?encSymbCount)
        (count (distinct ?type) as ?typeCount)
        (count (distinct ?registry) as ?registryCount) 
        (count (distinct ?BMI) as ?BMICount) 
        (count (distinct ?weight) as ?weightCount) 
        (count (distinct ?height) as ?heightCount)
        (count (distinct ?dateText) as ?dateTextCount)
        (count (distinct ?dateXsd) as ?dateXsdCount)
        """ + graphsList + """
        where
        {
            ?s a obo:OGMS_0000097 .
            ?s a ?type .
            optional {
                ?s turbo:TURBO_0000648 ?encSymb . 
            } .
            optional {
                ?s turbo:TURBO_0000650 ?registry 
            } .
            optional {
                ?s turbo:TURBO_0000655 ?BMI 
            } .
            optional {
                ?s turbo:TURBO_0000647 ?weight 
            } .
            optional {
                ?s turbo:TURBO_0000646 ?height 
            } .
            optional {
                ?s turbo:TURBO_0000644 ?dateText  
            } .
            optional {
                ?s turbo:TURBO_0000645 ?dateXsd 
            } .
        }
        group by ?s
        having (
            ?encSymbCount > 1  || 
            ?typeCount > 1  || 
            ?registryCount > 1  || 
            ?BMICount > 1 ||
            ?weightCount > 1 ||
            ?heightCount > 1 ||
            ?dateTextCount > 1 ||
            ?dateXsdCount > 1
        )
        """
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "multiple shortcut relationships on one healthcare encounter")
    }
    
    def checkForMultipleDiagnosisDependentNodes(cxn: RepositoryConnection, graphsList: String): Boolean =
    {
     val check: String ="""      
          select ?s
          (count (distinct ?diagType) as ?diagTypeCount)
          (count (distinct ?diagCodeRegTextVal) as ?diagCodeRegCount)
          (count (distinct ?diagCodeRegURIString) as ?diagCodeStringCount)
          (count (distinct ?diagCodeLV) as ?diagCodeLvCount)
          """ + graphsList + """
          where 
          {
                ?s a obo:OGMS_0000073 .
                ?s a ?diagType .
                
                optional {
                    ?s turbo:TURBO_0004602  ?diagCodeRegTextVal
                } .
                optional {
                    ?s turbo:TURBO_0004603  ?diagCodeRegURIString
                } .
                optional {
                    ?s turbo:TURBO_0004601  ?diagCodeLV
                } .
            } 
            group by ?s
            having (
            ?diagTypeCount > 1 ||
            ?diagCodeRegCount > 1 ||
            ?diagCodeStringCount > 1 ||
            ?diagCodeLvCount > 1 
            )
            """
           operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "multiple shortcut relationships on one healthcare encounter diagnosis")
    }
    
    def checkForMultiplePrescriptionDependentNodes(cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
            select ?s
            (count (distinct ?prescriptType) as ?prescriptTypeCount)
            (count (distinct ?medString) as ?medStringCount)
            (count (distinct ?medId) as ?medIdCount)
            (count (distinct ?drugURIString) as ?drugUriCount)
            """ + graphsList + """
            where 
            {
                ?s a obo:PDRO_0000001 .
                ?s a ?prescriptType .
                optional {
                    ?s turbo:TURBO_0005611 ?medString .
                } .
                optional {
                    ?s turbo:TURBO_0005601 ?medId .
                } .
                optional {
                    ?s turbo:TURBO_0005612 ?drugURIString .
            } .
            }
            group by ?s
            having (
            ?prescriptTypeCount > 1 ||
            ?medStringCount > 1 ||
            ?medIdCount > 1 ||
            ?drugUriCount > 1
            )"""
            
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "multiple shortcut relationships on one healthcare encounter prescription")
    }
    
    def checkForMultipleLossOfFunctionShortcutNodes(cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
            select ?s
            (count (distinct ?alleletype) as ?alleleTypeCount)
            (count (distinct ?zygosityValue) as ?zygValCount)
            (count (distinct ?bbEncSymb) as ?bbEncSymbCount)
            (count (distinct ?zygValText) as ?zygValTextCount)
            (count (distinct ?genomeCridSymbLit) as ?genomeCridSymbLitCount)
            (count (distinct ?genomeReg) as ?genomeRegCount)
            (count (distinct ?geneText) as ?geneTextCount)
            (count (distinct ?dataset) as ?datasetCount)
            (count (distinct ?bbEncReg) as ?encRegCount)
            """ + graphsList + """
            where 
            {
                ?allele a obo:OBI_0001352 .
                ?allele a ?alleletype .
                optional {
                    ?allele turbo:TURBO_0007607 ?zygosityValue .
                } .
                optional {
                    ?allele turbo:TURBO_0007601 ?bbEncSymb .
                } .
                optional {
                    ?allele turbo:TURBO_0007606 ?zygValText .
                } .
                optional {
                    ?allele turbo:TURBO_0007602 ?genomeCridSymbLit .
                } .
                optional {
                    ?allele turbo:TURBO_0007603 ?genomeReg .
                } .
                optional {
                    ?allele turbo:TURBO_0007605 ?geneText .
                } .
                optional {
                    ?allele turbo:TURBO_0007608 ?dataset .
                } .
                optional {
                    ?allele turbo:TURBO_0007609 ?bbEncReg .
                } .
            }
            group by ?s
            having (
            ?alleleTypeCount > 1 ||
            ?zygValCount > 1 ||
            ?bbEncSymbCount > 1 ||
            ?genomeCridSymbLitCount > 1 ||
            ?genomeRegCount > 1 ||
            ?geneTextCount > 1 ||
            ?datasetCount > 1 ||
            ?zygValTextCount > 1 ||
            ?encRegCount > 1
            )"""
            
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "multiple shortcut relationships on one healthcare encounter prescription")
    }

    def checkForUnexpectedClasses (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
        select * where {
            select ?c (count(?c) as ?ccount) {
                Values ?g {""" + graphsList + """}
            GRAPH ?g {
                ?s a ?c .
                # healthcare encounter
                filter (?c not in (obo:OGMS_0000097))
                # biobank encounter
                filter (?c not in (:TURBO_0000527))
                # biobank consenter
                filter (?c not in (:TURBO_0000502))
                # legal hc - diag shortcut instance
                filter (?c not in (obo:OGMS_0000073))
                # legal hc - med shortcut instance
                filter (?c not in (obo:PDRO_0000001))
                # consenter crid (in join data)
                filter (?c not in (turbo:TURBO_0000503))
                # biobank crid (in join data)
                filter (?c not in (turbo:TURBO_0000533))
                # healthcare crid (in join data)
                filter (?c not in (turbo:TURBO_0000508))
                # allele info (loss of function)
                filter (?c not in (obo:OBI_0001352))
            }} group by ?c }
        """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("c", "ccount"), "pre-expansion", "found extraneous class")
    }
    
    //this one is currently untested and Mark may decide to re-write it sometime
    def checkForPropertiesOutOfRange (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
          select distinct ?s (str(?stl) as ?stlstr) ?p (str(?pl) as ?plstr) ?o (str(?otl) as ?otlstr) (str(?rl) as ?rlstr) {
            Values ?g {""" + graphsList + """}
            GRAPH ?g {
                ?s ?p ?o .
                optional {
                    ?s rdf:type ?st .
                } .
                ?o rdf:type ?ot . 
            }
            graph pmbb:ontology
            {
                ?p a owl:ObjectProperty ;
                   rdfs:range ?r .
                ?ot rdfs:subClassOf* ?x .
                filter not exists {
                    ?ot rdfs:subClassOf* ?r 
                }
                optional {
                    ?st rdfs:label ?stl  
                }
                optional {
                    ?ot rdfs:label ?otl  
                }
                optional {
                    ?p rdfs:label ?pl  
                }
                optional {
                    ?r rdfs:label ?rl  
                }
            }
        }
          """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("p", "s", "o"), "pre-expansion", "found property out of range")
    }
    
    def checkForUnexpectedPredicates (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
          SELECT ?s ?p {
              Values ?g {""" + graphsList + """}
            GRAPH ?g 
                  {
                      ?s ?p ?o .
                  }
                  MINUS 
                  {
                      graph pmbb:ontology
                      {
                          ?p rdfs:subPropertyOf+ turbo:TURBO_0000670 .
                      }
                  }
                  FILTER (?p != rdf:type)
                  FILTER (?p != obo:RO_0002234)
                  FILTER (?p != turbo:TURBO_0000302)
                }
          """
        
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s", "p"), "pre-expansion", "found unexpected shortcut")
    }
    
    def checkAllSubjectsHaveAType (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
          SELECT * WHERE
          {
              Values ?g {""" + graphsList + """}
              GRAPH ?g
                {
                    ?s ?p ?o .
                    MINUS
                    {
                        ?s a ?type .
                    }
                }
          }
          """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("s"), "pre-expansion", "found entity without a type declaration")
    }
    
    def checkAllObjectsAreLiterals (cxn: RepositoryConnection, graphsList: String): Boolean =
    {
        val check: String = """
          SELECT * WHERE
          {
              Values ?g {""" + graphsList + """}
            GRAPH ?g 
              {
                  ?s ?p ?o .
              }
              FILTER (isURI(?o))
              FILTER (?o != turbo:TURBO_0000502)
              FILTER (?o != turbo:TURBO_0000527)
              FILTER (?o != obo:OGMS_0000097)
              FILTER (?o != obo:OGMS_0000073)
              FILTER (?o != obo:PDRO_0000001)
              FILTER (?o != turbo:TURBO_0000508)
              FILTER (?o != turbo:TURBO_0000503)
              FILTER (?o != turbo:TURBO_0000533)
              FILTER (?o != obo:OBI_0001352)
              
              FILTER (?p != obo:RO_0002234)
              FILTER (?p != turbo:TURBO_0000302)
          }
          """
        operation.runSparqlCheck(cxn, check, ArrayBuffer("o"), "pre-expansion", "found URI where literal was expected")
    }
}