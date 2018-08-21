package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel

/**
 *  BirthdateConclusionator contains methods relevant to the process of Date of Birth conclusionating. This process involves pulling relevant birth data from the graph and processing
 *  the date of birth datums attached to the biobank consenter nodes connected to each birth node. Results are inserted into a specific conclusionation named graph which is created by
 *  the Conclusionation driver method.
 */
class BirthdateConclusionator extends ProjectwideGlobals
{   
    /**
     * Calls the data retrieve function and receives the relevant birth/DOB data from the expanded graph. Processes each unique birth node by counting all date of birth
     * data attached to it, then passes all info to "addConclusionatedStatementToModel" 
     */
    def conclusionateBirthdate(cxn: RepositoryConnection, namedGraph: IRI, threshold: Double, masterConclusionation: IRI, masterPlanspec: IRI, masterPlan: IRI)
    {
        val f: ValueFactory = cxn.getValueFactory()
        // receive all birth and dob data
        val result: ArrayBuffer[ArrayBuffer[Value]] = retrieveBirthdateConclusionatingData(cxn)
        if (result.size == 0) logger.info("No birth data was found in the expanded graph.")
        else
        {
            // create model to be shared and updated by helper methods
            var model: Model = new LinkedHashModel()
            var dateInstance = result(0)(0)
            var mostOccurringDate: Option[Value] = None : Option[Value]
            var frequencyOfMOD: Int = 0
            var totalNumberOfDataPerBirth: Double = 0
            var resultToMap: HashMap[Int, HashMap[Value, Int]] = new HashMap[Int, HashMap[Value, Int]]
            // loop through all births/dobs, creating a conclusion for each unique birth
            for (a <- result)
            {
                //logger.info("started on date " + a(0))
                if (a(0) != dateInstance)
                {
                    //logger.info("for " + a(0) + " found concluded date to be " + mostOccurringDate)
                    //logger.info("frequency of mod " + frequencyOfMOD)
                    //logger.info("total data " + totalNumberOfDataPerBirth)
                    model = addConclusionatedStatementToModel(model, mostOccurringDate, frequencyOfMOD, totalNumberOfDataPerBirth, dateInstance, threshold, f)
                    resultToMap = new HashMap[Int, HashMap[Value, Int]]
                    totalNumberOfDataPerBirth = 0
                    mostOccurringDate = None : Option[Value]
                    dateInstance = a(0)
                    frequencyOfMOD = 0
                }
                if (a(1) != null)
                {
                    totalNumberOfDataPerBirth += 1
                    // CODEREVIEW112 is calling .hashcode on key lookups redundant?
                    if (resultToMap contains a(1).hashCode()) resultToMap(a(1).hashCode())(a(1)) += 1
                    else resultToMap += a(1).hashCode() -> HashMap(a(1) -> 1)
                    if (resultToMap(a(1).hashCode())(a(1)) > frequencyOfMOD)
                    {
                        frequencyOfMOD = resultToMap(a(1).hashCode())(a(1))
                        mostOccurringDate = Some(a(1))
                    }   
                }
            }
            //logger.info("found concluded date to be " + mostOccurringDate)
            //logger.info("frequency of mod " + frequencyOfMOD)
            //logger.info("total data " + totalNumberOfDataPerBirth)
            model = addConclusionatedStatementToModel(model, mostOccurringDate, frequencyOfMOD, totalNumberOfDataPerBirth, dateInstance, threshold, f)
            cxn.begin()
            cxn.add(model, namedGraph)
            cxn.commit()
            
            insertStandardConclusionatingInfo(cxn, masterConclusionation, masterPlanspec, masterPlan, namedGraph, threshold)
        }
    }
    
    /**
     * Receives a birth instance, the most frequently occurring dob attached to the birth instance, the total number of dob instances attached to the birth instance, and a user-supplied
     * threshold. Determines whether the most frequently occurring dob meets the threshold or not, and adds minimal information to the model bu creating a new dob, and giving it a literal
     * value of the most occurring dob if the threshodl was met.
     */
    def addConclusionatedStatementToModel(model: Model, mostOccurringDate: Option[Value], frequencyOfMOD: Integer, totalNumberOfDataPerBirth: Double, dateInstance: Value, threshold: Double, f: ValueFactory): Model =
    {
        val newDOB: IRI = helper.genPmbbIRI(f)
        if (frequencyOfMOD / totalNumberOfDataPerBirth >= threshold)
        {
            model.add(newDOB, f.createIRI("http://transformunify.org/ontologies/TURBO_0006511"), mostOccurringDate.get.asInstanceOf[Literal])
            model.add(newDOB, f.createIRI("http://purl.obolibrary.org/obo/IAO_0000136"), dateInstance.asInstanceOf[IRI])
        }
        else 
        {
            model.add(newDOB, f.createIRI("http://purl.obolibrary.org/obo/IAO_0000136"), dateInstance.asInstanceOf[IRI])
        }
        model
    }
    
    /**
     * Leverages SPARQL to find where conclusions have been made and generate relevant Conclusionating info. This method is responsible for adding Conclusionation processes, 
     * plans, Statements, thresholds, evidence, etc. to the Conclusiontaed Named graph for each birth instance which has been Conclusionated.
     */
    def insertStandardConclusionatingInfo(cxn: RepositoryConnection, masterConclusionation: IRI, masterPlanspec: IRI, masterPlan: IRI, namedGraph: IRI, birthThreshold: Double)
    {
        val plan: String = helper.genPmbbIRI(cxn).toString
        val planSpec: String = helper.genPmbbIRI(cxn).toString
        val midlevelConc: String = helper.genPmbbIRI(cxn).toString
        val threshValSpec: String = helper.genPmbbIRI(cxn).toString
        val actionThreshold: String = helper.genPmbbIRI(cxn).toString
        val insert1 = """
        INSERT {
          GRAPH <""" + namedGraph + """> {
            ?dob turbo:TURBO_0006501 'true'^^xsd:boolean .
            ?dob rdf:type efo:EFO_0004950 .
            ?dob rdfs:label ?doblabel .
            ?bigConclusionation a turbo:TURBO_0002500 ;
                                obo:BFO_0000051 ?midConclusionation ;
                                rdfs:label "all inclusive conclusionating process" ;
                                obo:OBI_0000299 <""" + namedGraph + """> ;
                                obo:BFO_0000055 ?masterPlan .
            ?masterPlan a obo:OBI_0000260 ;
                        obo:RO_0000059 ?masterPlanspec ;
                        rdfs:label "all inclusive plan" .
            ?masterPlanspec a obo:IAO_0000104 ;
                            obo:BFO_0000051 ?planSpec ;
                            rdfs:label "all inclusive plan specification" .
            ?midConclusionation a turbo:TURBO_0002500 ;
                                rdfs:label "date of birth conclusionation process" ;
                                obo:BFO_0000051 ?littleConclusionation .
            ?littleConclusionation a turbo:TURBO_0002500 ; 
                    obo:OBI_0000299 ?statement ;
                    obo:OBI_0000299 ?missingKnowledge ;
                    rdfs:label "Birthdate Conclusionation Process" .
            ?statement a rdf:Statement ;
                       rdf:subject ?dob ;
                       rdf:predicate  turbo:TURBO_0006511 ;
                       rdf:object ?dateLit ;
                       rdfs:label "Statement about Birthdate Conclusionation" .
            ?littleConclusionation obo:BFO_0000055 ?plan .
            ?plan      a obo:OBI_0000260 ;
                  obo:RO_0000059 ?planSpec ;
                  rdfs:label "Plan for Birthdate Conclusionation" .
            ?planSpec a obo:IAO_0000104 ;
                      rdfs:seeAlso 'FullStackAllSparql.scala' ;
                      owl:versionInfo 'GithubVersionInfoPlaceholder' ;
                      rdfs:label "Plan Specification for Birthdate Conclusionation " ;
                      rdfs:comment "At least """ + birthThreshold + """/1 date of birth data must be in agreement for conclusion to be drawn." .
            ?threshValSpec a obo:OBI_0001933 ;
                           obo:OBI_0001937 """" + birthThreshold + """"^^xsd:real ;
                           rdfs:label "Threshold Value Specification for Birthdate Conclusionation " .
            ?actionThreshold a obo:IAO_0000007 ;
                             obo:BFO_0000050 ?plan ;
                             obo:OBI_0001938 ?threshValSpec ;
                             rdfs:label "Action Threshold for Birthdate Conclusionation " .
            ?missingKnowledge rdf:type obo:OBI_0000852 ;
                             obo:IAO_0000136 ?dob ;
                             rdfs:label "Record of Missing Knowledge " .
          }
        }
        WHERE
          {   
              graph pmbb:expanded {
                  ?birth a obo:UBERON_0035946 ;
                          turbo:TURBO_0006500 'true'^^xsd:boolean .
              }
              graph <"""+namedGraph+"""> {
                  ?dob obo:IAO_0000136 ?birth .
                  OPTIONAL {
                      ?dob turbo:TURBO_0006511 ?dateLit .
                  }
              }
            BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?littleConclusionation)
            BIND(uri(""""+plan+"""") AS ?plan)
            BIND(uri(""""+ masterConclusionation + """") AS ?bigConclusionation)
            BIND(uri(""""+midlevelConc+"""") AS ?midConclusionation)
    		    BIND(uri(""""+planSpec+"""") AS ?planSpec)
    	    	BIND(uri(""""+ masterPlan + """") AS ?masterPlan)
            BIND(uri(""""+ masterPlanspec + """") AS ?masterPlanspec)
            BIND(uri(""""+ actionThreshold + """") AS ?actionThreshold)
            BIND(uri(""""+ threshValSpec + """") AS ?threshValSpec)
            BIND (IF (!(BOUND(?dateLit)), ?unbound, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", "")))) AS ?statement)
            BIND (IF (BOUND(?statement), ?unbound, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", "")))) AS ?missingKnowledge)
            BIND(concat("ConclusionatedDateOfBirth/", substr(str(?dob), 38, 4)) AS ?doblabel)
          }
        """
       //println(insert1)    
       update.updateSparql(cxn, sparqlPrefixes + insert1)
       
       val addDatumsAsEvidence: String = """
           INSERT 
           {
               GRAPH <""" + namedGraph + """>
               {
                   ?statementOrMK obo:OBI_0000124 ?dob .
               }
           }
           WHERE 
           {
               GRAPH pmbb:expanded
               {
                  ?part  rdf:type             turbo:TURBO_0000502 ;
                         turbo:TURBO_0006500 'true'^^xsd:boolean ;
                         turbo:TURBO_0000303  ?birth .
                         
                  OPTIONAL {
                  ?dob obo:IAO_0000136 ?birth ;
                          turbo:TURBO_0006511   ?xsddate ;
                          rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> . }
                          
                  ?birth turbo:TURBO_0006500 'true'^^xsd:boolean ;
                         rdf:type obo:UBERON_0035946 .
               }
               GRAPH <""" + namedGraph + """>
               {
                  ?dob2  turbo:TURBO_0006501 'true'^^xsd:boolean ;
                         a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                         obo:IAO_0000136 ?birth .
                  
                  OPTIONAL {
                      ?statement a rdf:Statement ;
                                 rdf:subject ?dob2 .
                  }  
                  OPTIONAL {
                      ?missingKnowledge a obo:OBI_0000852 ;
                                 obo:IAO_0000136 ?dob2 .
                  }
               BIND(IF (bound(?statement), ?statement, ?missingKnowledge) AS ?statementOrMK)             
               }
           }  
       """
       //println(addDatumsAsEvidence)
       update.updateSparql(cxn, sparqlPrefixes + addDatumsAsEvidence)
    }
    
    /**
     * Pulls every instance of birth and date of birth datum nodes from the expanded graph and returns it as a list of list of Values
     */
    def retrieveBirthdateConclusionatingData(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val dataRetrieve: String = """
          SELECT ?birth ?dateLit WHERE 
          {
          graph pmbb:expanded {
              ?birth a obo:UBERON_0035946 ;
                      turbo:TURBO_0006500 'true'^^xsd:boolean .
              OPTIONAL 
              {
                  ?birthdate obo:IAO_0000136 ?birth ;
                       a efo:EFO_0004950 .
                  ?birthdate turbo:TURBO_0006511 ?dateLit .
              }
          }}
          ORDER BY ?birth
          """   
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + dataRetrieve, ArrayBuffer("birth", "dateLit"))
    }
}