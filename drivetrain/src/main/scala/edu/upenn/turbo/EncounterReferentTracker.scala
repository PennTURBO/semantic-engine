package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection

/**
 * Contains methods relevant to the process of Encounter Referent Tracking, encompassing both
 * Healthcare Encounters and Biobank Encounters.
 */
class EncounterReferentTracker extends ProjectwideGlobals
{
   /**
    * The driver method for referent tracking of healthcare encounters and biobank encounters.
    */
   def reftrackEncounters (cxn: RepositoryConnection)
    {
        logger.info("starting bb enc rt'ing")
        reftrackBiobankEncounters(cxn)
        logger.info("finished bb enc rt'ing, starting hc enc rt'ing")
        reftrackHealthcareEncounters(cxn)
        logger.info("finished hc enc rt'ing")
    }

    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked Biobank Encounters
     * and insert pointers to the new destination into the graph.
     */
    def reftrackBiobankEncounters(cxn: RepositoryConnection)
    {
        val reftrackBBEncs: String = """
            INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter graphBuilder:willBeCombinedWith ?destination .
                ?encounter graphBuilder:placeholderDemotionType turbo:TURBO_0000927 .
            }}
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a turbo:TURBO_0000527 .
                ?encounterID rdf:type turbo:TURBO_0000533 .
                ?encounterID obo:IAO_0000219 ?encounter .
                ?encounterID obo:BFO_0000051 ?encSymb .
                ?encSymb a turbo:TURBO_0000534 .
                ?encSymb turbo:TURBO_0006510 ?encounterIdLV .
                ?encounterID obo:BFO_0000051 ?encRegDen .
                ?encRegDen a turbo:TURBO_0000535 .
                ?encRegDen obo:IAO_0000219 ?encRegId .
                ?encRegId a turbo:TURBO_0000543 .
                ?EncDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000532 .
            		# ?EncDate turbo:TURBO_0006511 ?encDateMeasVal .
            		?EncDate obo:IAO_0000136 ?EncStart .
            		?EncStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
            		?EncStart obo:RO_0002223 ?encounter .
                
                OPTIONAL {
                  ?encounter2 a turbo:TURBO_0000527 .
                  ?encounterID2 rdf:type turbo:TURBO_0000533 .
                  ?encounterID2 obo:IAO_0000219 ?encounter2 .
                  ?encounterID2 obo:BFO_0000051 ?encSymb2 .
                  ?encSymb2 a turbo:TURBO_0000534 .
                  ?encSymb2 turbo:TURBO_0006510 ?encounterIdLV .
                  ?encounterID2 obo:BFO_0000051 ?encRegDen2 .
                  ?encRegDen2 a turbo:TURBO_0000535 .
                  ?encRegDen2 obo:IAO_0000219 ?encRegId .
                  ?encRegId2 a turbo:TURBO_0000543 .
                  ?encounter2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?encounterID2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?EncDate2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000532 .
            		  # ?EncDate2 turbo:TURBO_0006511 ?encDateMeasVal2 .
            		  ?EncDate2 obo:IAO_0000136 ?EncStart2 .
            		  ?EncStart2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000531 .
            		  ?EncStart2 obo:RO_0002223 ?encounter2 .
                }
                
                MINUS {
                    ?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                BIND (IF (bound(?encounter2), ?encounter2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked encounter", str(?encounterIdLV), str(?encRegId)))))) AS ?destination)
            }}  
        """
                
        helper.updateSparql(cxn, sparqlPrefixes + reftrackBBEncs)  
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked Healthcare Encounters
     * and insert pointers to the new destination into the graph.
     */
    def reftrackHealthcareEncounters (cxn: RepositoryConnection)
    {
        val reftrackHCEncs: String = """
            INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter graphBuilder:willBeCombinedWith ?destination .
                ?encounter graphBuilder:placeholderDemotionType turbo:TURBO_0000907 .
            }}
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a obo:OGMS_0000097 .
                ?encounterID rdf:type turbo:TURBO_0000508 .
                ?encounterID obo:IAO_0000219 ?encounter .
                ?encounterID obo:BFO_0000051 ?encSymb .
                ?encSymb a turbo:TURBO_0000509 .
                ?encSymb turbo:TURBO_0006510 ?encounterIdLV .
                ?encounterID obo:BFO_0000051 ?encRegDen .
                ?encRegDen a turbo:TURBO_0000510 .
                ?encRegDen obo:IAO_0000219 ?encRegId .
                ?encRegId a turbo:TURBO_0000513 .
                ?EncDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
            		# ?EncDate turbo:TURBO_0006511 ?encDateMeasVal .
            		?EncDate obo:IAO_0000136 ?EncStart .
            		?EncStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
            		?EncStart obo:RO_0002223 ?encounter .
                
                OPTIONAL {
                  ?encounter2 a obo:OGMS_0000097 .
                  ?encounterID2 obo:IAO_0000219 ?encounter2 .
                  ?encounterID2 obo:BFO_0000051 ?encSymb2 .
                  ?encSymb2 a turbo:TURBO_0000509 .
                  ?encSymb2 turbo:TURBO_0006510 ?encounterIdLV .
                  ?encounterID2 obo:BFO_0000051 ?encRegDen2 .
                  ?encRegDen2 a turbo:TURBO_0000510 .
                  ?encRegDen2 obo:IAO_0000219 ?encRegId .
                  ?encRegId2 a turbo:TURBO_0000513 .
                  ?encounterID2 rdf:type turbo:TURBO_0000508 .
                  ?encounter2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?encounterID2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?EncDate2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
            		  # ?EncDate2 turbo:TURBO_0006511 ?encDateMeasVal2 .
            		  ?EncDate2 obo:IAO_0000136 ?EncStart2 .
            		  ?EncStart2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
            		  ?EncStart2 obo:RO_0002223 ?encounter2 .
                }
                
                MINUS {
                    ?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                BIND (IF (bound(?encounter2), ?encounter2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked encounter", str(?encounterIdLV), str(?encRegId)))))) AS ?destination)
            }}  
        """
                
        helper.updateSparql(cxn, sparqlPrefixes + reftrackHCEncs)  
    }
    
    /**
     * The driver method for referent tracking all dependents of Biobank and Healthcare encounters.
     */
    def reftrackEncounterDependents(cxn: RepositoryConnection)
    {
        logger.info("started bb enc primary dependents rt'ing")
        reftrackPrimaryBiobankEncounterDependents(cxn)
        logger.info("finished bb enc primary dependents rt'ing, starting hc enc dependents rt'ing")
        reftrackPrimaryHealthcareEncounterDependents(cxn)
        logger.info("finished hc enc primary dependents rt'ing")
        helper.completeReftrackProcess(cxn)
        logger.info("starting encounter secondary dependent ref tracking")
        reftrackSecondaryBiobankEncounterDependents(cxn)
        reftrackSecondaryHealthcareEncounterDependents(cxn)
        helper.completeReftrackProcess(cxn)
        logger.info("starting bmi ref tracking")
        reftrackBiobankBMI(cxn)
        reftrackHealthcareBMI(cxn)
        helper.completeReftrackProcess(cxn)
        reftrackBMIValSpecs(cxn)
        helper.completeReftrackProcess(cxn)
        logger.info("finished encounter dependent ref tracking")
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked primary biobank encounter dependents
     * and insert pointers to the new destination into the graph.
     */
    def reftrackPrimaryBiobankEncounterDependents(cxn: RepositoryConnection)
    {
        val reftrackBBDependents: String = """
            INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounterID graphBuilder:willBeCombinedWith ?encounterIdDestination .
                ?encounterID graphBuilder:placeholderDemotionType turbo:TURBO_0000933 .
                ?encStart graphBuilder:willBeCombinedWith ?biobankStartDestination .
                ?encStart graphBuilder:placeholderDemotionType turbo:TURBO_0000931 .
                }}          
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a turbo:TURBO_0000527 .
                ?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?encounterID obo:IAO_0000219 ?encounter .
                ?encounterID a turbo:TURBO_0000533 .
                ?encStart a turbo:TURBO_0000531 .
        		    ?encStart obo:RO_0002223 ?encounter .
                
            OPTIONAL {
                ?encounterID2 obo:IAO_0000219 ?encounter .
                ?encounterID2 a turbo:TURBO_0000533 .
                ?encounterID2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?encStart2 a turbo:TURBO_0000531 .
            		?encStart2 obo:RO_0002223 ?encounter .
            		?encStart2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?encounterID turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?encStart turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                BIND (IF (bound(?encounterID2), ?encounterID2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked encounter ID", str(?encounter)))))) AS ?encounterIdDestination)
                BIND (IF (bound(?encStart2), ?encStart2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked encounter start", str(?encounter)))))) AS ?biobankStartDestination)
            }}  
        """
                
        helper.updateSparql(cxn, sparqlPrefixes + reftrackBBDependents)  
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked primary healthcare encounter dependents
     * and insert pointers to the new destination into the graph.
     */
    def reftrackPrimaryHealthcareEncounterDependents(cxn: RepositoryConnection)
    {
        val reftrackHCDependents: String = """
            INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounterID graphBuilder:willBeCombinedWith ?encounterIdDestination .
                ?encounterID graphBuilder:placeholderDemotionType turbo:TURBO_0000908 .
                ?encStart graphBuilder:willBeCombinedWith ?healthcareStartDestination .
                ?encStart graphBuilder:placeholderDemotionType turbo:TURBO_0000911 .
                }}          
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a obo:OGMS_0000097 .
                ?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?encounterID obo:IAO_0000219 ?encounter .
                ?encounterID a turbo:TURBO_0000508 .
                ?encStart a turbo:TURBO_0000511 .
        		    ?encStart obo:RO_0002223 ?encounter .
                
            OPTIONAL {
                ?encounterID2 obo:IAO_0000219 ?encounter .
                ?encounterID2 a turbo:TURBO_0000508 .
                ?encounterID2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?encStart2 a turbo:TURBO_0000511 .
        		    ?encStart2 obo:RO_0002223 ?encounter .
        		    ?encStart2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?encounterID turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?encStart turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                BIND (IF (bound(?encounterID2), ?encounterID2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked encounter ID", str(?encounter)))))) AS ?encounterIdDestination)
                BIND (IF (bound(?encStart2), ?encStart2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked encounter start", str(?encounter)))))) AS ?healthcareStartDestination)
            }}  
        """
                
        helper.updateSparql(cxn, sparqlPrefixes + reftrackHCDependents)    
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked secondary biobank encounter dependents
     * and insert pointers to the new destination into the graph.
     */
    def reftrackSecondaryBiobankEncounterDependents(cxn: RepositoryConnection)
    {
        val reftrackBiobankEncDates: String = """
          INSERT 
          {
              GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encDate graphBuilder:willBeCombinedWith ?encDateDestination .
                ?encDate graphBuilder:placeholderDemotionType turbo:TURBO_0000932 .
                ?encSymb graphBuilder:willBeCombinedWith ?encSymbDestination .
                ?encSymb graphBuilder:placeholderDemotionType turbo:TURBO_0000934 .
                ?encRegDen graphBuilder:willBeCombinedWith ?encRegDenDestination .
                ?encRegDen graphBuilder:placeholderDemotionType turbo:TURBO_0000935 .
                }
          }
          WHERE 
          {
              GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encStart a turbo:TURBO_0000531 .
                ?encDate obo:IAO_0000136 ?encStart .
            		?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000532 .
            		Optional
            		{
            		    ?encDate turbo:TURBO_0006511 ?dateLit .
            		}
            		?encStart obo:RO_0002223 ?encounter .
            		?encounter a turbo:TURBO_0000527 .
            		?crid obo:IAO_0000219 ?encounter .
            		?crid a turbo:TURBO_0000533 .
            		?crid obo:BFO_0000051 ?encSymb .
            		?crid obo:BFO_0000051 ?encRegDen .
            		?encSymb a turbo:TURBO_0000534 .
            		?encRegDen a turbo:TURBO_0000535 .
            		?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
            		?crid turbo:TURBO_0006500 'true'^^xsd:boolean .
            		?encStart turbo:TURBO_0006500 'true'^^xsd:boolean .
                    
                OPTIONAL {
                    ?encDate2 obo:IAO_0000136 ?encStart .
                    ?encDate2 a turbo:TURBO_0000532 .
                    ?encDate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    Optional
                    {
            		        ?encDate2 turbo:TURBO_0006511 ?dateLit2 .
            		    }
                    ?encounter a turbo:TURBO_0000527 .
                		?crid obo:IAO_0000219 ?encounter .
                		?crid a turbo:TURBO_0000533 .
                		?crid obo:BFO_0000051 ?encSymb2 .
                		?crid obo:BFO_0000051 ?encRegDen2 .
                		?encSymb2 a turbo:TURBO_0000534 .
                		?encRegDen2 a turbo:TURBO_0000535 .
                		?encRegDen2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		?encSymb2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                   }
                
                MINUS {
                  ?encDate turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                MINUS {
                  ?encSymb turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                MINUS {
                  ?encRegDen turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                BIND (IF (bound(?dateLit), ?dateLit, uri("http://graphBuilder.org/noDateFound")) AS ?dateLitMod)
                BIND (IF (bound(?dateLit2), ?dateLit2, uri("http://graphBuilder.org/noDateFound")) AS ?dateLitMod2)
                BIND (IF ((bound(?encDate2) && str(?dateLitMod) = str(?dateLitMod2)), ?encDate2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked enc start", str(?encStart), str(?dateLitMod)))))) AS ?encDateDestination)
                BIND (IF (bound(?encSymb2), ?encSymb2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked enc symb", str(?crid)))))) AS ?encSymbDestination)
                BIND (IF (bound(?encRegDen2), ?encRegDen2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked enc reg den", str(?crid)))))) AS ?encRegDenDestination)
            }
          }
          """
      
        logger.info("sending sparql string to server")
        helper.updateSparql(cxn, sparqlPrefixes + reftrackBiobankEncDates) 
        logger.info("submitted secondary ref track statement to server")
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked secondary healthcare encounter dependents
     * and insert pointers to the new destination into the graph.
     */
    def reftrackSecondaryHealthcareEncounterDependents(cxn: RepositoryConnection)
    {
        val reftrackHealthcareEncDates: String = """
          INSERT 
          {
              GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encDate graphBuilder:willBeCombinedWith ?encDateDestination .
                ?encDate graphBuilder:placeholderDemotionType turbo:TURBO_0000912 .
                ?encSymb graphBuilder:willBeCombinedWith ?encSymbDestination .
                ?encSymb graphBuilder:placeholderDemotionType turbo:TURBO_0000909 .
                ?encRegDen graphBuilder:willBeCombinedWith ?encRegDenDestination .
                ?encRegDen graphBuilder:placeholderDemotionType turbo:TURBO_0000910 .
                }
          }
          WHERE 
          {
              GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encStart a turbo:TURBO_0000511 .
                ?encDate obo:IAO_0000136 ?encStart .
            		?encDate a turbo:TURBO_0000512 .
            		Optional
            		{
            		    ?encDate turbo:TURBO_0006511 ?dateLit .
            		}
            		?encStart obo:RO_0002223 ?encounter .
            		?encounter a obo:OGMS_0000097 .
            		?crid obo:IAO_0000219 ?encounter .
            		?crid a turbo:TURBO_0000508 .
            		?crid obo:BFO_0000051 ?encSymb .
            		?crid obo:BFO_0000051 ?encRegDen .
            		?encSymb a turbo:TURBO_0000509 .
            		?encRegDen a turbo:TURBO_0000510 .
            		?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
            		?crid turbo:TURBO_0006500 'true'^^xsd:boolean .
            		?encStart turbo:TURBO_0006500 'true'^^xsd:boolean .
                    
                OPTIONAL {
                    ?encDate2 obo:IAO_0000136 ?encStart .
                    ?encDate2 a turbo:TURBO_0000512 .
                    ?encDate2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                    Optional
                		{
                		    ?encDate2 turbo:TURBO_0006511 ?dateLit2 .
                		}
                    ?encounter a obo:OGMS_0000097 .
                		?crid obo:IAO_0000219 ?encounter .
                		?crid a turbo:TURBO_0000508 .
                		?crid obo:BFO_0000051 ?encSymb2 .
                		?crid obo:BFO_0000051 ?encRegDen2 .
                		?encSymb2 a turbo:TURBO_0000509 .
                		?encRegDen2 a turbo:TURBO_0000510 .
                		?encRegDen2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		?encSymb2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                   }
                
                MINUS {
                  ?encDate turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                MINUS {
                  ?encSymb turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                MINUS {
                  ?encRegDen turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                BIND (IF (bound(?dateLit), ?dateLit, uri("http://graphBuilder.org/noDateFound")) AS ?dateLitMod)
                BIND (IF (bound(?dateLit2), ?dateLit2, uri("http://graphBuilder.org/noDateFound")) AS ?dateLitMod2)
                BIND (IF ((bound(?encDate2) && str(?dateLitMod) = str(?dateLitMod2)), ?encDate2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked enc start", str(?encStart), str(?dateLitMod)))))) AS ?encDateDestination)
                BIND (IF (bound(?encSymb2), ?encSymb2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked enc symb", str(?crid)))))) AS ?encSymbDestination)
                BIND (IF (bound(?encRegDen2), ?encRegDen2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked enc reg den", str(?crid)))))) AS ?encRegDenDestination)
            }
          }
          """
      
        logger.info("sending sparql string to server")
        helper.updateSparql(cxn, sparqlPrefixes + reftrackHealthcareEncDates) 
        logger.info("submitted secondary ref track statement to server")
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked biobank BMIs
     * and insert pointers to the new destination into the graph.
     */
    def reftrackBiobankBMI(cxn: RepositoryConnection)
    {
          val reftrackBiobankBMIs: String = """
          INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?BMI graphBuilder:willBeCombinedWith ?BMIDestination .
                ?BMI graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
                }}          
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a turbo:TURBO_0000527 .
                ?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?encounter obo:OBI_0000299 ?BMI .
                ?encStart a turbo:TURBO_0000531 .
        		    ?encStart obo:RO_0002223 ?encounter .
        		    ?encStart turbo:TURBO_0006500 'true'^^xsd:boolean .
        		    ?encDate obo:IAO_0000136 ?encStart .
                ?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:OBI_0001938 ?BMIvalspec .
            		?BMIvalspec a obo:OBI_0001933 .
            		?BMIvalspec obo:OBI_0002135 ?BMILit .
            		?BMI obo:IAO_0000581 ?encDate .
            		?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000532 .
            		?encDate turbo:TURBO_0006500 'true'^^xsd:boolean .
                    
                OPTIONAL {
                    ?encounter obo:OBI_0000299 ?BMI2 .
                    ?BMI2 a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		    ?BMI2 obo:OBI_0001938 ?BMIvalspec2 .
                    ?BMI2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		?BMIvalspec2 a obo:OBI_0001933 .
                		?BMIvalspec2 obo:OBI_0002135 ?BMILit .
                		?BMI2 obo:IAO_0000581 ?encDate .
               }
                
                MINUS {
                  ?BMI turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                BIND (IF (bound(?BMI2), ?BMI2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked bb BMI", str(?encounter), str(?BMILit)))))) AS ?BMIDestination)
            }}
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + reftrackBiobankBMIs) 
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked healthcare BMIs
     * and insert pointers to the new destination into the graph.
     */
    def reftrackHealthcareBMI(cxn: RepositoryConnection)
    {
        val reftrackHealthcareBMIs: String = """
          INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?BMI graphBuilder:willBeCombinedWith ?BMIDestination .
                ?BMI graphBuilder:placeholderDemotionType turbo:TURBO_0001903 .
                }}          
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?encounter a obo:OGMS_0000097 .
                ?encounter turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?encStart a turbo:TURBO_0000511 .
        		    ?encStart obo:RO_0002223 ?encounter .
        		    ?encStart turbo:TURBO_0006500 'true'^^xsd:boolean .
        		    ?encDate obo:IAO_0000136 ?encStart .
                ?encounter obo:RO_0002234 ?BMI .
                ?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:OBI_0001938 ?BMIvalspec .
            		?BMIvalspec a obo:OBI_0001933 .
            		?BMIvalspec obo:OBI_0002135 ?BMILit .
            		?BMI obo:IAO_0000581 ?encDate .
            		?encDate a turbo:TURBO_0000512 .
                    
                OPTIONAL {
                    ?encounter obo:RO_0002234 ?BMI2 .
                    ?BMI2 a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		    ?BMI2 obo:OBI_0001938 ?BMIvalspec2 .
                    ?BMI2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                		?BMIvalspec2 a obo:OBI_0001933 .
                		?BMIvalspec2 obo:OBI_0002135 ?BMILit .
                		?BMI2 obo:IAO_0000581 ?encDate .
               }
                
                MINUS {
                  ?BMI turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                BIND (IF (bound(?BMI2), ?BMI2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked hc BMI", str(?encounter), str(?BMILit)))))) AS ?BMIDestination)
            }}
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + reftrackHealthcareBMIs)  
    }
    
    /**
     * A SPARQL-based update to determine referent tracking destinations for non-reftracked BMI Value Specifications
     * and insert pointers to the new destination into the graph.
     */
    def reftrackBMIValSpecs(cxn: RepositoryConnection)
    {
        val reftrackBMIValSpecs: String = """
          INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?BMIvalspec graphBuilder:willBeCombinedWith ?valspecDest .
                ?BMIvalspec graphBuilder:placeholderDemotionType turbo:TURBO_0001904 .
                }}          
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:OBI_0001938 ?BMIvalspec .
            		?BMI turbo:TURBO_0006500 'true'^^xsd:boolean .
            		?BMIvalspec a obo:OBI_0001933 .
                
            OPTIONAL {
                ?BMI obo:OBI_0001938 ?BMIvalspec2 .
            		?BMIvalspec2 a obo:OBI_0001933 .
            		?BMIvalspec2 turbo:TURBO_0006500 'true'^^xsd:boolean .
               }
                
                MINUS {
                  ?BMIvalspec turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                BIND (IF (bound(?BMIvalspec2), ?BMIvalspec2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked BMI val spec", str(?BMI)))))) AS ?valspecDest)
            }}
          """
        
        helper.updateSparql(cxn, sparqlPrefixes + reftrackBMIValSpecs)
    }
}
