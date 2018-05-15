package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel

/**
 *  BiosexConclusionator contains methods relevant to the process of Biological Sex conclusionating. This process involves pulling relevant biosex data from the graph and processing
 *  the gender identity datums attached to the biosex nodes for each biobank consenter. Results are inserted into a specific conclusionation named graph which is created by the
 *  Conclusionation driver method.
 */

class BiosexConclusionator extends ProjectwideGlobals
{
    /**
     * Calls the data retrieve function and receives the relevant biosex/GID data from the expanded graph. Processes each unique biosex node by counting all male and female gender
     * identity data attached to it, then passes all info to "addConclusionatedStatementToModel" 
     */
    def conclusionateBiosex(cxn: RepositoryConnection, namedGraph: IRI, threshold: Double, masterConclusionation: IRI, masterPlanspec: IRI, masterPlan: IRI)
    {
        val f: ValueFactory = cxn.getValueFactory()
        // get all biosex and gid info from graph
        val result: ArrayBuffer[ArrayBuffer[Value]] = retrieveBiosexConclusionationData(cxn)
        // create model to be shared and updated by helper methods
        var model: Model = new LinkedHashModel()
        if (result.size == 0) logger.info("No biosex data was found in the expanded graph.")
        else
        {
            var biosexInstance: Value = result(0)(0)
            var maleCount = 0
            var femaleCount = 0
            val maleType: Value = f.createIRI("http://purl.obolibrary.org/obo/OMRSE_00000141")
            val femaleType: Value = f.createIRI("http://purl.obolibrary.org/obo/OMRSE_00000138")
            // loop through all biosex/gid data, counting all gender identity data for each instance of biosex
            for (a <- result)
            {
                if (a(0) != biosexInstance)
                {
                    model = addConclusionatedStatementToModel(model, maleCount, femaleCount, threshold, biosexInstance, f)
                    biosexInstance = a(0)
                    maleCount = 0
                    femaleCount = 0
                }
                if (a(1) == maleType) 
                {
                    maleCount = maleCount + 1
                    //logger.info("found a male")
                }
                if (a(1) == femaleType)
                {
                    femaleCount = femaleCount + 1
                    //logger.info("found a female")
                }
            }
            model = addConclusionatedStatementToModel(model, maleCount, femaleCount, threshold, biosexInstance, f)
            // commit changes in model to graph DB
            cxn.begin()
            cxn.add(model, namedGraph)
            cxn.commit()
            
            insertStandardConclusionatingInfo(cxn, masterConclusionation, masterPlanspec, masterPlan, namedGraph, threshold)    
        }
    }
    
    /**
     * Receives a biosex instance and counts of relevant male and female gender identity data attached to the biosex instance. Calls "getConclusionResult" to determine result
     * of Conclusionation for this biosex instance, then adds minimal info describing the decision to the shared RDF Model
     */
    def addConclusionatedStatementToModel(model: Model, maleCount: Integer, femaleCount: Integer, threshold: Double, biosexInstance: Value, f: ValueFactory): Model =
    {
        //logger.info("end of biosex instance " + biosexInstance)
        val indexResult: Option[Integer] = getConclusionResult(Array(maleCount, femaleCount), threshold)
        var sexType: Option[IRI] = None : Option[IRI]
        if (indexResult == None) sexType = Some(f.createIRI("http://purl.obolibrary.org/obo/PATO_0000047"))
        else if (indexResult.get == 0) sexType = Some(f.createIRI("http://purl.obolibrary.org/obo/PATO_0000384"))
        else if (indexResult.get == 1) sexType = Some(f.createIRI("http://purl.obolibrary.org/obo/PATO_0000383"))
        model.add(biosexInstance.asInstanceOf[IRI], f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), sexType.get)
        //logger.info("generated conclusion " + sexType.get)
        model
    }
    
    /**
     * Receives array of gender identity counts for a given biological sex and a user-specified threshold which must be met to declare the conclusionated biosex of a certain type.
     * maleCount is counts(0), femaleCount is counts(1). Returns an Option[Integer] with value Some(0) to declare a male type, Some(1) to declare a female type, and None to declare
     * inconclusive.
     */
    def getConclusionResult(counts: Array[Integer], threshold: Double): Option[Integer] =
    {
        if (threshold <= .5 || threshold > 1) throw new RuntimeException ("The supplied biosex conclusionating threshold " + threshold + " is not between .5 and 1")
        var totalCount: Double = 0
        //logger.info("using threshold: " + threshold)
        var result: Option[Integer] = None : Option[Integer]
        for (count <- counts)
        {
            totalCount += count
        }
        //logger.info("total number of data: " + totalCount)
        if (totalCount == 0) result = None
        else
        {
            var index = 0
            while (result == None && index <= counts.size - 1)
            {
                //logger.info("found percentage " + counts(index)/totalCount)
                if (counts(index) / totalCount >= threshold) result = Some(index) 
                index = index + 1
            }
        }
        result
    }
    
    /**
     * Leverages SPARQL to find where conclusions have been made and generate relevant Conclusionating info. This method is responsible for adding Conclusionation processes, 
     * plans, Statements, thresholds, evidence, etc. to the Conclusiontaed Named graph for each biosex instance which has been Conclusionated.
     */
    def insertStandardConclusionatingInfo(cxn: RepositoryConnection, masterConclusionation: IRI, masterPlanspec: IRI, masterPlan: IRI, namedGraph: IRI, biosexThreshold: Double)
    {
        val plan: String = helper.genPmbbIRI(cxn).toString
        val planSpec: String = helper.genPmbbIRI(cxn).toString
        val midlevelConc: String = helper.genPmbbIRI(cxn).toString
        val threshValSpec: String = helper.genPmbbIRI(cxn).toString
        val actionThreshold: String = helper.genPmbbIRI(cxn).toString
        
        val insert1 = """
        INSERT {
          GRAPH <""" + namedGraph + """> {       
            ?biosex turbo:TURBO_0006501 'true'^^xsd:boolean .
            ?bigConclusionation a turbo:TURBO_0002500 ;
                                obo:BFO_0000051 ?midConclusionation ;
                                rdfs:label "all inclusive conclusionating process" ;
                                obo:OBI_0000299 <""" + namedGraph + """> ;
                                obo:BFO_0000055 ?masterPlan .
            ?masterPlan a obo:OBI_0000260 ;
                            obo:RO_0000059 ?masterPlanspec ;
                            rdfs:label "All inclusive plan ".
            ?masterPlanspec a obo:IAO_0000104 ;
                            obo:BFO_0000051 ?planSpec ;
                            rdfs:label "All inclusive plan specification" .
            ?midConclusionation a turbo:TURBO_0002500 ;
                                rdfs:label "biosex conclusionation process" ;
                                obo:BFO_0000051 ?littleConclusionation .
            ?littleConclusionation obo:BFO_0000055 ?plan ;
                                   rdf:type turbo:TURBO_0002500 ;
                                   obo:OBI_0000299 ?statement ;
                                   obo:OBI_0000299 ?missingKnowledge ;
                                   rdfs:label "Biological sex conclusionation process" .
            ?statement a rdf:Statement ;
                       rdf:subject ?biosex ;
                       rdf:predicate rdf:type ;
                       rdf:object ?conclusionatedType ;
                       rdfs:label "Statement about biological sex conclusionation" .
            ?missingKnowledge a obo:OBI_0000852 ;
                              obo:IAO_0000136 ?biosex ;
                              rdfs:label "Record of Missing Knowledge" .
            ?plan a obo:OBI_0000260 ;
                  obo:RO_0000059 ?planSpec ;
                  rdfs:label "Plan for Biological sex conclusionation" .
            ?planSpec a obo:IAO_0000104 ;
                      rdfs:seeAlso 'FullStackAllSparql.scala' ;
                      owl:versionInfo 'GithubVersionInfoPlaceholder' ;
                      rdfs:label "Plan specification for biological sex conclusionation" ;
                      rdfs:comment "At least """ + biosexThreshold + """/1 gender identity data must be in agreement for conclusion to be drawn." .
            ?threshValSpec a obo:OBI_0001933 ;
                           obo:OBI_0001937 """" + biosexThreshold + """"^^xsd:real ;
                           rdfs:label "Threshold Value Specification for biological sex Conclusionation " .
            ?actionThreshold a obo:IAO_0000007 ;
                             obo:BFO_0000050 ?plan ;
                             obo:OBI_0001938 ?threshValSpec ;
                             rdfs:label "Action Threshold for biological sex Conclusionation " .
          }
        }
        WHERE
        {
          graph pmbb:expanded {
              ?biosex a obo:PATO_0000047 ;
                      turbo:TURBO_0006500 'true'^^xsd:boolean .
          
          }
          graph <"""+namedGraph+"""> {
              ?biosex a ?conclusionatedType .
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
            BIND (IF (?conclusionatedType = obo:PATO_0000047, ?unbound, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", "")))) AS ?statement)
            BIND (IF (BOUND(?statement), ?unbound, uri(CONCAT("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", "")))) AS ?missingKnowledge)
          }
        """
       //println(insert1)
       helper.updateSparql(cxn, sparqlPrefixes + insert1)
       
       val addDatumsAsEvidence: String = """
           INSERT 
           {
               GRAPH <""" + namedGraph + """>
               {
                   ?statementOrMK obo:OBI_0000124 ?gid .
               }
           }
           WHERE 
           {
               GRAPH pmbb:expanded 
               {
                  ?part    rdf:type        turbo:TURBO_0000502 ;
                           obo:RO_0000086  ?biosex ;
                           turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?biosex  rdf:type        obo:PATO_0000047 ;
                           turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?gid     obo:IAO_0000136 ?part ;
                           rdf:type        ?sexclass .
                  VALUES ?sexclass { obo:OMRSE_00000133 obo:OMRSE_00000138 obo:OMRSE_00000141 }
               }
               GRAPH <""" + namedGraph + """>
               {
                  ?biosex  turbo:TURBO_0006501 'true'^^xsd:boolean ;
                  
                  OPTIONAL {
                      ?statement a rdf:Statement ;
                                 rdf:subject ?biosex .
                  }  
                  OPTIONAL {
                      ?missingKnowledge a obo:OBI_0000852 ;
                                 obo:IAO_0000136 ?biosex .
                  }
               BIND(IF (bound(?statement), ?statement, ?missingKnowledge) AS ?statementOrMK)             
               }
           }  
       """
       //println(addDatumsAsEvidence)
       helper.updateSparql(cxn, sparqlPrefixes + addDatumsAsEvidence)
    }
    
    /**
     * Pulls every instance of biosex and gender identity datum nodes from the expanded graph and returns it as a list of list of Values
     */
    def retrieveBiosexConclusionationData(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val dataRetrieve: String = """
          SELECT * WHERE 
          {
          graph pmbb:expanded {
              # this values is not having the effect I expected...
              # VALUES ?gidType {obo:OMRSE_00000141 obo:OMRSE_00000138}
              ?biosex a obo:PATO_0000047 ;
                      turbo:TURBO_0006500 'true'^^xsd:boolean .
              ?participant obo:RO_0000086 ?biosex ;
                      a turbo:TURBO_0000502 ;
                      turbo:TURBO_0006500 'true'^^xsd:boolean .
              OPTIONAL {
                  ?gid obo:IAO_0000136 ?participant ;
                       a ?gidType . 
              }
          }}
          ORDER BY ?biosex
          """   
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + dataRetrieve, ArrayBuffer("biosex", "gidType"))
    }
}