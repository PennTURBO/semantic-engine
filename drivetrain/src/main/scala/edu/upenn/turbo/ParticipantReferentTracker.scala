package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
/**
 * This class contains methods related to the process of Consenter (formerly Participant) Referent Tracking. 
 */
class ParticipantReferentTracker extends ProjectwideGlobals
{
    /**
     * Completes the Referent Tracking of Biobank Consenters by pulling relevant referent-tracked and non-referent-tracked consenter data, processing the data and determining 
     * which Consenters should be combined. Minimal pointer information is inserted into the graph using an RDF4J model; the processes of node retirement and property migration
     * are executed in the "completeReferentTracking" stage which is directed by the information provided by the pointers inserted here.
     */
    def reftrackParticipants(cxn: RepositoryConnection)
    {
        val f: ValueFactory = cxn.getValueFactory()
        val willBeCombinedWith: IRI = f.createIRI("http://graphBuilder.org/willBeCombinedWith")
        val demotionType: IRI = f.createIRI("http://graphBuilder.org/placeholderDemotionType")
        val rdfslabel: IRI = f.createIRI("http://www.w3.org/2000/01/rdf-schema#label")
        
        val nonReftrackedResult: ArrayBuffer[ArrayBuffer[Value]] = getNonReftrackedConsenterData(cxn)      
        if (nonReftrackedResult.size == 0) logger.info("No new consenter data was found in the expanded graph.")
        else
        {
            val reftrackedResult: ArrayBuffer[ArrayBuffer[Value]] = getReftrackedConsenterData(cxn)
                
            //add reftracked data to hash map
            var reftrackedMap: HashMap[String, Value] = new HashMap[String, Value]
            for (a <- reftrackedResult) reftrackedMap += a(1).toString + a(2).toString -> a(0)
                
            var model: Model = new LinkedHashModel()
            var currentIRI: Option[IRI] = None : Option[IRI]
            var currentPSC: Value = nonReftrackedResult(0)(1)
            for (a <- nonReftrackedResult)
            {
                //logger.info("Setting reg id to " + a(3).toString)
                if (currentPSC != a(1) || a(0) == nonReftrackedResult(0)(0))
                {
                    if (reftrackedMap.contains(a(1).toString + a(2).toString)) currentIRI = Some(reftrackedMap(a(1).toString + a(2).toString).asInstanceOf[IRI])
                    else currentIRI = Some(helper.genPmbbIRI(cxn)) 
                    currentPSC = a(1)
                }
                //if new IRI is being used instead of existing ref tracked IRI, hash new IRI with registry ID URI 
                if (!reftrackedMap.contains(a(1).toString + a(2).toString))
                {
                    var tempIRI = f.createIRI("http://www.itmat.upenn.edu/biobank/" + helper.md5Hash(currentIRI.get + a(2).toString))
                    model.add(a(0).asInstanceOf[IRI], willBeCombinedWith, tempIRI)
                    //logger.info("Adding: " + a(0) + " -> " + tempIRI)
                }
                else
                {
                    model.add(a(0).asInstanceOf[IRI], willBeCombinedWith, currentIRI.get)
                    //logger.info("Adding (found pre-existing) : " + a(0) + " -> " + currentIRI)
                }
                model.add(a(0).asInstanceOf[IRI], demotionType, f.createIRI("http://transformunify.org/ontologies/TURBO_0000902"))
            }
            cxn.begin()
            cxn.add(model, f.createIRI("http://www.itmat.upenn.edu/biobank/expanded"))
            cxn.commit()  
        }
    }
    
    /**
     * Submits a SPARQL update to the Graph database which executed Primary Consenter Dependent Referent Tracking. This Referent Tracks all consenter dependent nodes which are 
     * one "hop" away from the consenter itself. For proper operation, Consenters must already have been reftracked before this method is called.
     */
    def reftrackParticipantDependents (cxn: RepositoryConnection)
    {
        val reftrackDependents: String = """
            INSERT {
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?biosex graphBuilder:willBeCombinedWith ?biosexDestination .
                ?biosex graphBuilder:placeholderDemotionType turbo:TURBO_0001902 .
                ?patientCrid graphBuilder:willBeCombinedWith ?cridSymbolIdDestination .
                # replaced type according to ontology
                ?patientCrid graphBuilder:placeholderDemotionType turbo:TURBO_0000903 .
                ?birth graphBuilder:willBeCombinedWith ?birthDestination .
                ?birth graphBuilder:placeholderDemotionType turbo:TURBO_0001906 .
                ?adipose graphBuilder:willBeCombinedWith ?adiposeDestination .
                ?adipose graphBuilder:placeholderDemotionType turbo:TURBO_0001901 .
                ?height graphBuilder:willBeCombinedWith ?heightDestination .
                ?height graphBuilder:placeholderDemotionType turbo:TURBO_0001905 .
                ?weight graphBuilder:willBeCombinedWith ?weightDestination .
                ?weight graphBuilder:placeholderDemotionType turbo:TURBO_0001908 .
            }}
            WHERE { 
                GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
                ?participant a turbo:TURBO_0000502 .
                ?participant turbo:TURBO_0006500 'true'^^xsd:boolean .
                ?patientCrid obo:IAO_0000219 ?participant .
                ?patientCrid a turbo:TURBO_0000503 .
                ?biosex a obo:PATO_0000047 .
                ?participant obo:RO_0000086 ?biosex .
                ?birth a obo:UBERON_0035946 .
                ?participant turbo:TURBO_0000303 ?birth .
                ?adipose a obo:UBERON_0001013 .
    		        ?participant obo:BFO_0000051 ?adipose .
                ?participant obo:RO_0000086 ?height .
            		?height a obo:PATO_0000119 .
            		?participant obo:RO_0000086 ?weight .
            		?weight a obo:PATO_0000128 .
                
                OPTIONAL {
                  ?patientCrid2 obo:IAO_0000219 ?participant .
                  ?patientCrid2 a turbo:TURBO_0000503 .
                  ?patientCrid2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?participant obo:RO_0000086 ?biosex2 .
                  ?biosex2 a obo:PATO_0000047 .
                  ?biosex2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?birth2 a obo:UBERON_0035946 .
                  ?birth2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?participant turbo:TURBO_0000303 ?birth2 .
                  ?adipose2 a obo:UBERON_0001013 .
                  ?participant obo:BFO_0000051 ?adipose2 .
                  ?adipose2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?participant obo:RO_0000086 ?height2 .
            		  ?height2 a obo:PATO_0000119 .
            		  ?height2 turbo:TURBO_0006500 'true'^^xsd:boolean .
            		  ?participant obo:RO_0000086 ?weight2 .
            		  ?weight2 a obo:PATO_0000128 .
            		  ?weight2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?patientCrid turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?biosex turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?birth turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?adipose turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?weight turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                MINUS {
                  ?height turbo:TURBO_0006500 'true'^^xsd:boolean .
                }
                
                BIND (IF (bound(?biosex2), ?biosex2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked biosex", str(?participant)))))) AS ?biosexDestination)
                BIND (IF (bound(?patientCrid2), ?patientCrid2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked psc", str(?participant)))))) AS ?cridSymbolIdDestination)
                BIND (IF (bound(?birth2), ?birth2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked birth", str(?participant)))))) AS ?birthDestination)
                BIND (IF (bound(?adipose2), ?adipose2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked adipose", str(?participant)))))) AS ?adiposeDestination)
                BIND (IF (bound(?height2), ?height2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked height", str(?participant)))))) AS ?heightDestination)
                BIND (IF (bound(?weight2), ?weight2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked weight", str(?participant)))))) AS ?weightDestination)
            }}  
        """
                
        update.updateSparql(cxn, sparqlPrefixes + reftrackDependents)    
    }
    
    /**
     * Submits a SPARQL update to the Graph database which executed Secondary Consenter Dependent Referent Tracking. This Referent Tracks all consenter dependent nodes which are 
     * two "hops" away from the consenter itself. For proper operation, Consenters and Primary Consenter Dependents must already have been reftracked before this method is called.
     */
    def reftrackSecondaryParticipantDependents(cxn: RepositoryConnection)
    {
        val update1: String = """
          Insert
          {
              Graph pmbb:expanded
              {
                  ?consenterRegDen graphBuilder:willBeCombinedWith ?consenterRegDenDestination .
                  ?consenterRegDen graphBuilder:placeholderDemotionType turbo:TURBO_0000905 .
                  ?consenterSymb graphBuilder:willBeCombinedWith ?consenterSymbDestination .
                  ?consenterSymb graphBuilder:placeholderDemotionType turbo:TURBO_0000904 .
              }
          }
          Where
          {
              Graph pmbb:expanded
              {
                  ?consenter a turbo:TURBO_0000502 .
                  ?consenter turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?consenterCrid obo:IAO_0000219 ?consenter .
                  ?consenterCrid a turbo:TURBO_0000503 .
                  ?consenterCrid turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?consenterCrid obo:BFO_0000051 ?consenterSymb .
                  ?consenterCrid obo:BFO_0000051 ?consenterRegDen .
                  ?consenterSymb a turbo:TURBO_0000504 .
                  ?consenterRegDen a turbo:TURBO_0000505 .
                  
                  Optional
                  {
                      ?consenterCrid obo:BFO_0000051 ?consenterSymb2 .
                      ?consenterCrid obo:BFO_0000051 ?consenterRegDen2 .
                      ?consenterSymb2 a turbo:TURBO_0000504 .
                      ?consenterRegDen2 a turbo:TURBO_0000505 .
                      ?consenterSymb2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                      ?consenterRegDen2 turbo:TURBO_0006500 'true'^^xsd:boolean .
                  }
                  
                  Minus
                  {
                      ?consenterSymb turbo:TURBO_0006500 'true'^^xsd:boolean .
                  }
                  
                  Minus
                  {
                      ?consenterRegDen turbo:TURBO_0006500 'true'^^xsd:boolean .
                  }
              }
              BIND (IF (bound(?consenterSymb2), ?consenterSymb2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked consenter symb", str(?consenterCrid)))))) AS ?consenterSymbDestination)
              BIND (IF (bound(?consenterRegDen2), ?consenterRegDen2, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("reftracked consenter reg den", str(?consenterCrid)))))) AS ?consenterRegDenDestination)
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + update1)
    }
    
    /**
     * Pulls data on Non-Reftracked Consenters from the Graph DB using a SPARQL Query. Returns Consenter URI, Consenter Symbol Value, and Consenter Registry Identifier.
     * 
     * @return ArrayBuffer[ArrayBuffer[Value]] where (for each list in list of lists) index 0 = consenter URI, index 1 = symbol value, index 2 = registry identifier
     */
    def getNonReftrackedConsenterData(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val nonReftrackedConsenters: String = """
          SELECT distinct ?participant ?pscLit ?patientRegId WHERE 
          {
              GRAPH pmbb:expanded {
              ?participant a turbo:TURBO_0000502 .
              ?patientCrid obo:IAO_0000219 ?participant .
              ?patientCrid a turbo:TURBO_0000503 . 
              ?patientCrid obo:BFO_0000051 ?patientCridSymbol .
              ?patientCrid obo:BFO_0000051 ?patientRegDenoter .
              ?patientRegDenoter a turbo:TURBO_0000505 .
              ?patientRegDenoter obo:IAO_0000219 ?patientRegId .
              ?patientRegId a turbo:TURBO_0000506 .
              ?patientCridSymbol a turbo:TURBO_0000504 .
              ?patientCridSymbol turbo:TURBO_0006510 ?pscLit .
              
              MINUS 
              {
                  ?participant turbo:TURBO_0006500 'true'^^xsd:boolean .
              }
          }}
          ORDER BY ?pscLit ?patientRegId
          """
        
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + nonReftrackedConsenters, ArrayBuffer("participant", "pscLit", "patientRegId"))
    }
    
    /**
     * Pulls data on Reftracked Consenters from the Graph DB using a SPARQL Query. Returns Consenter URI, Consenter Symbol Value, and Consenter Registry Identifier.
     * 
     * @return ArrayBuffer[ArrayBuffer[Value]] where (for each list in list of lists) index 0 = consenter URI, index 1 = symbol value, index 2 = registry identifier
     */
    def getReftrackedConsenterData(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val reftrackedConsenters: String = """
          Select ?participant ?pscLit ?patientRegId 
          {
              ?participant a turbo:TURBO_0000502 .
              ?patientCrid obo:IAO_0000219 ?participant .
              ?patientCrid a turbo:TURBO_0000503 . 
              ?patientCrid obo:BFO_0000051 ?patientCridSymbol .
              ?patientCrid obo:BFO_0000051 ?patientRegDenoter .
              ?patientRegDenoter a turbo:TURBO_0000505 .
              ?patientRegDenoter obo:IAO_0000219 ?patientRegId .
              ?patientRegId a turbo:TURBO_0000506 .
              ?patientCridSymbol a turbo:TURBO_0000504 .
              ?patientCridSymbol turbo:TURBO_0006510 ?pscLit .
              
              ?participant turbo:TURBO_0006500 'true'^^xsd:boolean .
          }
          """
        
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + reftrackedConsenters, ArrayBuffer("participant", "pscLit", "patientRegId"))
    }
}