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
 *  BMIConclusionator contains methods relevant to the process of BMI conclusionating. This process involves pulling relevant BMI data from the graph and creating a conclusionated BMI
 *  for each biobank encounter. Results are inserted into a specific conclusionation named graph which is created by the Conclusionation driver method.
 */
class BMIConclusionator extends ProjectwideGlobals
{
    // this flag set to 'true' indicates that healthcare BMI data for a specific date was not found and biobank BMI data for that date should be used if available
    var useBiobank: Boolean = false
    
     //adding comments for clarity (1/10/2018)
    //This class will only operate over biobank encounters that have date data. If there is no date data for a biobank encounter, it will not receive a record of missing knowledge
    //associated with it in the conclusionated graph at the current state of affairs. This should perhaps be fixed.
    
    /**
     * This is the driver method for BMI Conclusionation. Receives IRIs which specify context of the results, including named graph to insert results. 
     * Pulls relevant BMI data and processes it to determine proper conluclusion, then inserts minimal conclusionated information and calls helper 
     * method to insert the rest.
     */
    def conclusionateBMI(cxn: RepositoryConnection, namedGraph: IRI, masterConclusionation: IRI, masterPlanspec: IRI, masterPlan: IRI)
    {
        val f: ValueFactory = cxn.getValueFactory()
        //obtain necessary projection from graph db
        val result: ArrayBuffer[ArrayBuffer[Value]] = retrieveBMIData(cxn)
        var model: Model = new LinkedHashModel()
        //if projection list of lists is empty, no BMI data to conclusionate
        if (result.size == 0) logger.info("No BMI data was found in the expanded graph.")
        var a = 0
        while (a <= result.size - 1)
        {
            //this variable indicates whether the conclusionator should proceed to the next step, false if a conclusion/inconclusion has been drawn
            var proceed: Boolean = true
            //this variable indicates whether valid Healthcare BMI data has been found on the same date as a specific Biobank encounter
            var validHCBMI: Boolean = true
            //this variable indicates whether valid Biobank BMI data has been found for a specific biobank encounter
            var validBBBMI: Boolean = true
            //this variable holds the textual explanation for why a certain conclusion was made
            var explanation: String = ""
            if (a != result.size-1)
            {
                //special case if the same biobank encounter appears multiple times in the projection...this could occur due to multiple relevant BMI recordings
                if (result(a)(7) == result(a+1)(7)) 
                {
                    val objArr: Array[Object] = handleMultipleBMIRecordingsForSingleEncounter(result, a, f)
                    /* We set the index to the end of the occurrence of the duplicate Biobank encounters to avoid the rest of the program iterating over 
                     * these already-handled values.
                     */
                    a = objArr(0).asInstanceOf[Int]
                    validHCBMI = objArr(1).asInstanceOf[Boolean]
                    validBBBMI = objArr(2).asInstanceOf[Boolean]
                    //If both biobank and healthcare BMI data conflicts no conclusion can be drawn
                    if (validHCBMI == false && validBBBMI == false)
                    {
                        //logger.info(result(a)(7) + "Adding inconclusive BMI statement due to conflicting data")
                        proceed = false
                        model = addBMIInconclusion(model, result(a)(7), "Multiple and Contradicting BMI Data", f)
                    }
                    //If healthcare BMI data is invalid but biobank BMI data is valid, set flag to 'true'
                    if (validHCBMI == false && validBBBMI == true) useBiobank = true
                }
            }
            //proceed to "normal operation" bmi conclusionation
            if (proceed) model = checkForValidBMIData(model, result(a), f, validHCBMI)
            a = a + 1
            validHCBMI = true
            validBBBMI = true
            proceed = true
        }
        cxn.begin()
        //add all generated statements to named graph
        cxn.add(model, namedGraph)
        cxn.commit()
        
        insertStandardBMIConclusionatingInfo(cxn, masterConclusionation, masterPlanspec, masterPlan, namedGraph)
    }
    
    /**
     * Receives a single row of data describing BMI recordings for a given biobank encounter, as well as a Boolean which indicates whether
     * the row of data contains valid healthcare BMI information. If there is valid healthcare BMI information, the method adds a statement
     * to the model. Otherwise, it checks whether there is valid Biobank BMI information. If so it adds a statement to the model, otherwise
     * it adds an Inconclusion.
     * 
     * IT'S VERY CONFUSING WHAT DIFFERENTIATES THIS METHOD'S FUNCTIONALITY FROM addToModel(). THIS SHOULD REALLY BE CLEANED UP.
     */
    def checkForValidBMIData(model: Model, result: ArrayBuffer[Value], f: ValueFactory, validHCBMI: Boolean): Model =
    {
        var model_mut = model
        var explanation = ""
        var map: HashMap[String, Object] = null
        //If no healthcare BMI data exists, set flag to try biobank data
        if (result(2) == null)
        {
            useBiobank = true
            explanation += "Healthcare BMI data does not exist for this date. "
        }
        //If healthcare data, create conclusion using healthcare data
        else if (validHCBMI)
        {
            map = addToModel(model, f, "healthcare", result(2), result(7), result(10), result(5), result(9), result(11), explanation)
            explanation = map("explanation").asInstanceOf[String]
            model_mut = map("model").asInstanceOf[Model]
        }
        
        if (useBiobank)
        {
            if (!validHCBMI) explanation += "Contradicting Healthcare BMI data was found. "
            //If no biobank data, create inconclusion statements
            if (result(1) == null)
            {
                explanation += "Biobank BMI data does not exist for this date. "
                //model.add(result(a)(7).asInstanceOf[IRI], hasInconclusiveBMI, f.createLiteral(explanation))
                model_mut = addBMIInconclusion(model_mut, result(7), explanation, f)
                //logger.info(result(7) + "Adding inconclusive BMI statement")
            }
            //If biobank data, create conclusion using biobank data
            else map = addToModel(model, f, "biobank", result(1), result(7), result(10), result(4), result(9), result(10), explanation)
        }
        useBiobank = false
        model_mut
    }
    
    /**
     * IT'S VERY CONFUSING WHAT DIFFERENTIATES THIS METHOD'S FUNCTIONALITY FROM checkForValidBMIData(). THIS SHOULD REALLY BE CLEANED UP.
     */
    def addToModel(model: Model, f: ValueFactory, encountertype: String, hcBMIVal: Value, bbEnc: Value, bbEncDate: Value, hcBMI: Value, 
        adipose: Value, hcEncDate: Value, explanation: String): HashMap[String, Object] =
    {
        var explanation_mut: String = explanation
        var model_mut = model
        try
        {
            //ensure BMI is parseable
            val bmiAsDouble: Double = hcBMIVal.asInstanceOf[Literal].doubleValue()
            //ensure BMI is within reasonable range
            if (bmiAsDouble > 70 || bmiAsDouble < 10) 
            {
                //logger.info("BMI literal " + bmiAsDouble + " is out of reasonable range.")
                useBiobank = true
                explanation_mut += encountertype + " BMI data for this date is out of reasonable range. "
                if (encountertype == "biobank") model_mut = addBMIInconclusion(model_mut, bbEnc, explanation, f)
            }
            else
            {
                explanation_mut += "Found valid " + encountertype + " BMI data for this date. " 
                //logger.info(result7 + "adding valid BMI statement - " + encountertype)
                model_mut = addBMIConclusion(model_mut, explanation, bbEncDate, hcBMI, bmiAsDouble, adipose, hcEncDate, f)
            }
        }
        catch
        {
            case e: NumberFormatException => 
                logger.info("Unable to parse BMI literal " + hcBMIVal) 
                useBiobank = true 
                explanation_mut += encountertype + " BMI data for this date cannot be parsed to a number. "
        }
        HashMap("model" -> model, "explanation" -> explanation)
    }
    
    /**
     * Receives an RDF model as well as all necessary information for creating a BMI Conclusion in the event that a Conclusion has been determined appropriate by the upstream methods.
     * Adds relevant information to the model.
     * 
     * @return an RDF Model with the relevant Conclusion information
     */
    def addBMIConclusion(model: Model, explanation: String, date: Value, BMI: Value, BMILit: Double, adipose: Value, dateURI: Value, f: ValueFactory): Model =
    {
        val statement1: IRI = helper.genPmbbIRI(f)
        val statement2: IRI = helper.genPmbbIRI(f)
        val concludedBMI: IRI = helper.genPmbbIRI(f)
        val concludedBMIValSpec: IRI = helper.genPmbbIRI(f)
        val littleConc: IRI = helper.genPmbbIRI(f)
        model.add(littleConc, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://transformunify.org/ontologies/TURBO_0002500"));
        model.add(littleConc, f.createIRI("http://purl.obolibrary.org/obo/OBI_0000299"), statement1);
        model.add(littleConc, f.createIRI("http://purl.obolibrary.org/obo/OBI_0000299"), statement2);
        model.add(concludedBMI, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://www.ebi.ac.uk/efo/EFO_0004340"));
        model.add(concludedBMI, f.createIRI("http://transformunify.org/ontologies/TURBO_0006501"), f.createLiteral(true))
        model.add(concludedBMI, f.createIRI("http://purl.obolibrary.org/obo/OBI_0001938"), concludedBMIValSpec)
        model.add(concludedBMI, f.createIRI("http://purl.obolibrary.org/obo/IAO_0000581"), date.asInstanceOf[IRI])
        model.add(concludedBMIValSpec, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://purl.obolibrary.org/obo/OBI_0001933"))
        model.add(concludedBMIValSpec, f.createIRI("http://purl.obolibrary.org/obo/OBI_0002135"), f.createLiteral(BMILit.toFloat))
        model.add(statement1, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"))
        model.add(statement2, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"))
        model.add(statement1, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"), concludedBMI)
        model.add(statement1, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"), f.createIRI("http://purl.obolibrary.org/obo/IAO_0000581"))
        model.add(statement1, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object"), date.asInstanceOf[IRI])
        model.add(statement1, f.createIRI("http://www.w3.org/2000/01/rdf-schema#comment"), f.createLiteral(explanation))
        model.add(statement1, f.createIRI("http://purl.obolibrary.org/obo/OBI_0000124"), dateURI.asInstanceOf[IRI])
        model.add(statement2, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"), concludedBMIValSpec)
        model.add(statement2, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"), f.createIRI("http://purl.obolibrary.org/obo/OBI_0002135"))
        model.add(statement2, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#object"), f.createLiteral(BMILit.toString))
        model.add(statement2, f.createIRI("http://purl.obolibrary.org/obo/OBI_0000124"), BMI.asInstanceOf[IRI])
        model.add(statement2, f.createIRI("http://www.w3.org/2000/01/rdf-schema#comment"), f.createLiteral(explanation))
        model.add(concludedBMI, f.createIRI("http://purl.obolibrary.org/obo/IAO_0000136"), adipose.asInstanceOf[IRI])
        model
    }
    
    /**
     * Receives an RDF model as well as all necessary information for creating a BMI Inconclusion in the event that the upstream methods have determined a Conclusion is not possible.
     * Adds relevant information to the model.
     * 
     * @return an RDF Model with the relevant Inconclusion information.
     */
    def addBMIInconclusion(model: Model, encounter: Value, explanation: String, f: ValueFactory): Model =
    {
        //println("model size" + model.size())
        val littleConc: IRI = helper.genPmbbIRI(f)
        val mk: IRI = helper.genPmbbIRI(f)
        model.add(mk, f.createIRI("http://purl.obolibrary.org/obo/IAO_0000136"), encounter.asInstanceOf[IRI])
        model.add(littleConc, f.createIRI("http://purl.obolibrary.org/obo/OBI_0000299"), mk)
        model.add(littleConc, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://transformunify.org/ontologies/TURBO_0002500"));
        model.add(mk, f.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), f.createIRI("http://purl.obolibrary.org/obo/OBI_0000852"));
        model.add(mk, f.createIRI("http://www.w3.org/2000/01/rdf-schema#comment"), f.createLiteral(explanation))
        //println("model size" + model.size())
        model
    }
    
    /**
     * Reads the graph for minimal BMI Conclusionation or Inconclusion information and inserts supporting statements into the specified conclusionated named graph.
     * Supporting statements include statements about conclusionation processes, plans and plan specifications, evidence for the conclusion, etc.
     */
    def insertStandardBMIConclusionatingInfo(cxn: RepositoryConnection, masterConclusionation: IRI, masterPlanspec: IRI, masterPlan: IRI, namedGraph: IRI)
    {
        val plan: String = helper.genPmbbIRI(cxn).toString
        val planSpec: String = helper.genPmbbIRI(cxn).toString
        val midlevelConc: String = helper.genPmbbIRI(cxn).toString
        val threshValSpec: String = helper.genPmbbIRI(cxn).toString
        val actionThreshold: String = helper.genPmbbIRI(cxn).toString
        
        val insert1 = """
        INSERT {
          GRAPH <""" + namedGraph + """> {       
            ?bigConclusionation a turbo:TURBO_0002500 ;
                                obo:BFO_0000051 ?midConclusionation ;
                                rdfs:label "all inclusive conclusionating process" ;
                                obo:OBI_0000299 <""" + namedGraph + """> ;
                                obo:BFO_0000055 ?masterPlan .
            ?masterPlan a obo:OBI_0000260 ;
                            rdfs:label "master plan" ;
                            obo:RO_0000059 ?masterPlanspec .
            ?masterPlanspec a obo:IAO_0000104 ;
                            rdfs:label "master plan specification" ;
                            obo:BFO_0000051 ?planSpec .
            ?midConclusionation a turbo:TURBO_0002500 ;
                                rdfs:label "BMI conclusionation process" ;
                                obo:BFO_0000051 ?littleConc .
            ?littleConc rdfs:label "BMI Conclusionation Sub-Process" .
            ?concludedBMI rdfs:label ?bmilabel .
            ?concludedBMIValSpec rdfs:label ?bmivalspeclabel .
            ?plan a obo:OBI_0000260 ;
                  obo:RO_0000059 ?planSpec ;
                  rdfs:label "Plan for BMI Conclusionation" .
            ?planSpec a obo:IAO_0000104 ;
                      rdfs:seeAlso 'FullStackAllSparql.scala' ;
                      owl:versionInfo 'GithubVersionInfoPlaceholder' ;
                      rdfs:label "Plan Specification for BMI Conclusionation" ;
                      rdfs:comment "BMI data for a Healthcare encounter on the same date as the Biobank encounter is used if available. Otherwise Biobank BMI data is used." .
            ?statement1 rdfs:label "BMI Conclusionating Statement about Date" .
            ?statement2 rdfs:label "BMI Conclusionating Statement about BMI Value" .
          }
        }
        WHERE
        {     
            ?statement1 a rdf:Statement ;
                        rdf:subject ?concludedBMI ;
                        rdf:predicate obo:IAO_0000581 ;
                        rdf:object ?dateVal .
            ?concludedBMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            ?statement2 a rdf:Statement ;
                        rdf:subject ?concludedBMIValSpec ;
                        rdf:predicate obo:OBI_0002135 ;
                        rdf:object ?BMILit .
            ?concludedBMIValSpec a obo:OBI_0001933 .
            ?littleConc a turbo:TURBO_0002500 ;
                        obo:OBI_0000299 ?statement1 ;
                        obo:OBI_0000299 ?statement2 .
            
              BIND(uri(""""+plan+"""") AS ?plan)
              BIND(uri(""""+ masterConclusionation + """") AS ?bigConclusionation)
              BIND(uri(""""+midlevelConc+"""") AS ?midConclusionation)
    	      BIND(uri(""""+planSpec+"""") AS ?planSpec)
   		      BIND(uri(""""+ masterPlan + """") AS ?masterPlan)
              BIND(uri(""""+ masterPlanspec + """") AS ?masterPlanspec)
              BIND(uri(""""+ actionThreshold + """") AS ?actionThreshold)
              BIND(uri(""""+ threshValSpec + """") AS ?threshValSpec)
              BIND(concat("ConclusionatedBMI/", substr(str(?concludedBMI), 38, 4)) AS ?bmilabel)
              BIND(concat("ConclusionatedBMIValueSpecification/", substr(str(?concludedBMIValSpec), 38, 4)) AS ?bmivalspeclabel)
          }
        """
              
       val insert2 = """
         INSERT 
         {
             GRAPH <"""+namedGraph+""">
             {
                 ?midConclusionation obo:BFO_0000051 ?littleConc .
                 ?littleConc rdfs:label "BMI Conclusionation Process" .
                 ?mk rdfs:label "Record of Missing Knowledge" .
             }
         }
         WHERE 
         {
             ?littleConc a turbo:TURBO_0002500 ;
                         obo:OBI_0000299 ?mk .
             ?mk a obo:OBI_0000852 ;
                 obo:IAO_0000136 ?encounter .
             ?encounter a turbo:TURBO_0000527 . 
             
             BIND(uri(""""+midlevelConc+"""") AS ?midConclusionation)
         }
         """
       //println(insert1)
       helper.updateSparql(cxn, sparqlPrefixes + insert1)
       helper.updateSparql(cxn, sparqlPrefixes + insert2)
    }
    
    /**
     * This function will analyze the multiple BMI recordings, treating Biobank BMI and Healthcare BMI separately. If they are just 
     * repeated values, the relevant boolean will return 'true', otherwise if they are conflicting values it returns 'false'. 
     */
    def handleMultipleBMIRecordingsForSingleEncounter(result: ArrayBuffer[ArrayBuffer[Value]], a: Int, f: ValueFactory): Array[Object] = 
    {
        var hcBMI: Boolean = true
        var bbBMI: Boolean = true
        var currHCBMI: Value = result(a)(2)
        var currBBBMI: Value = result(a)(1)
        var index = a
        var next = result(index+1)(7)
        while (result(index)(7) == next)
        {
            index = index + 1
            if (hcBMI == true)
            {
                if (result(index)(2) != currHCBMI) hcBMI = false  
                currHCBMI = result(index)(2)
            }
            if (bbBMI == true)
            {
                if (result(index)(1) != currBBBMI) bbBMI = false
                currBBBMI = result(index)(1)
            } 
            if (result.size == index+1) next = f.createLiteral("stop")
            else next = result(index+1)(7)
        }
        Array(index.asInstanceOf[Object], hcBMI.asInstanceOf[Object], bbBMI.asInstanceOf[Object])
    }
   
    /**
     * Pull relevant data for use by the BMI Conclusionator. 
     * 
     * @return a list of list of values containing relevant data for the BMI Conclusionator
     */
    def retrieveBMIData (cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val dataRetrieve: String = """
          SELECT distinct ?bbEnc ?BMILit1 ?BMILit2 ?dateVal ?bbBMI ?hcBMI ?dateVal ?hcEnc ?adipose ?bbEncDate ?hcEncDate
          WHERE { GRAPH pmbb:expanded {
                  ?participant a turbo:TURBO_0000502 .
                  ?participant obo:RO_0000056 ?bbEnc .
                  ?participant obo:BFO_0000051 ?adipose .
                  ?adipose a obo:UBERON_0001013 .
                  ?bbEnc a turbo:TURBO_0000527 ;
                         turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?bbEncStart obo:RO_0002223 ?bbEnc .
                  ?bbEncStart a turbo:TURBO_0000531 .
                  ?bbEncDate obo:IAO_0000136 ?bbEncStart .
                  ?bbEncDate a turbo:TURBO_0000532 .
                  ?bbEncDate turbo:TURBO_0006511 ?dateVal .
                           
              OPTIONAL 
              {
                  ?bbEnc obo:OBI_0000299 ?bbBMI .
                  ?bbBMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
                  ?bbBMI obo:OBI_0001938 ?bbBMIvalspec .
                  ?bbBMIvalspec a obo:OBI_0001933 .
                  ?bbBMIvalspec obo:OBI_0002135 ?BMILit1 .
              }
              
              OPTIONAL
              {
                  ?participant obo:RO_0000056 ?hcEnc .
                  ?hcEnc a obo:OGMS_0000097 ;
                         turbo:TURBO_0006500 'true'^^xsd:boolean .
                  ?hcEncStart obo:RO_0002223 ?hcEnc .
                  ?hcEncStart a turbo:TURBO_0000511 .
                  ?hcEncDate obo:IAO_0000136 ?hcEncStart .
                  ?hcEncDate a turbo:TURBO_0000512 .
                  ?hcEncDate turbo:TURBO_0006511 ?dateVal .
                  ?hcEnc obo:RO_0002234 ?hcBMI .
                  ?hcBMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
                  ?hcBMI obo:OBI_0001938 ?hcBMIvalspec .
                  ?hcBMIvalspec a obo:OBI_0001933 .
                  ?hcBMIvalspec obo:OBI_0002135 ?BMILit2 .
              }
          }}
          """   
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + dataRetrieve, 
        ArrayBuffer("none", "BMILit1", "BMILit2", "dateVal", "bbBMI", "hcBMI", "dateVal", "bbEnc", "hcEnc", "adipose", "bbEncDate", "hcEncDate"))
    }
}