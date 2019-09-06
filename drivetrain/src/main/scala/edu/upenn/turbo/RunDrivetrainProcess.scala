package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import java.util.UUID
import java.util.Calendar

object RunDrivetrainProcess extends ProjectwideGlobals
{
    var localUUID: String = null
    var variableSet = new HashSet[Value]
    var inputSet = new HashSet[Value]
    var inputProcessSet = new HashSet[String]
    var typeMap = new HashMap[String,Value]
    
    def setGlobalUUID(globalUUID: String)
    {
        this.localUUID = globalUUID
    }
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    def setOutputRepositoryConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }

    def validateProcess(cxn: RepositoryConnection, process: String): Boolean =
    {
       val ask: String = s"""
          ASK {
            values ?processSuperClass {turbo:TURBO_0001542 turbo:TURBO_0010178}
            <$process> rdfs:subClassOf ?processSuperClass .
          }
          """
        update.querySparqlBoolean(cxn, ask).get
    }
    
    def runProcess(process: String)
    {
        runProcess(ArrayBuffer(process))
    }
        
    def runProcess(processes: ArrayBuffer[String])
    {
        var processQueryMap = new HashMap[String, PatternMatchQuery]
        for (process <- processes)
        {
            if (!validateProcess(gmCxn, process)) logger.info(process + " is not a valid TURBO process")
            else processQueryMap += process -> createPatternMatchQuery(process)
        }
        for ((process, primaryQuery) <- processQueryMap)
        {
            val startTime = System.nanoTime()
            val currDate = Calendar.getInstance().getTime()
            val startingTriplesCount = helper.countTriplesInDatabase(cxn)
            val processGraphsList = new ArrayBuffer[String]
            logger.info("Starting process: " + process)
           
            val genericWhereClause = primaryQuery.whereClause
            // get list of all named graphs which match pattern specified in inputNamedGraph and include match to where clause
            //var inputNamedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph, genericWhereClause)
            var inputNamedGraphsList = helper.generateSimpleNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph)
            logger.info("input named graphs size: " + inputNamedGraphsList.size)
                
            if (inputNamedGraphsList.size == 0) logger.info(s"Cannot run process $process: no input named graphs found")
            else
            {
                // for each input named graph, run query with specified named graph
                for (graph <- inputNamedGraphsList)
                {
                    val localStartingTriplesCount = helper.countTriplesInDatabase(cxn)
                    //run validation on input graph
                    validateInputData(graph, primaryQuery.rawInputData)
                    primaryQuery.whereClause = genericWhereClause.replaceAll(primaryQuery.defaultInputGraph, graph)
                    //logger.info(primaryQuery.getQuery())
                    primaryQuery.runQuery(cxn)
                    val localEndingTriplesCount = helper.countTriplesInDatabase(cxn)
                    if (localStartingTriplesCount != localEndingTriplesCount) processGraphsList += graph
                }
                // set back to generic input named graph for storing in metadata
                primaryQuery.whereClause = genericWhereClause
                
                val endingTriplesCount = helper.countTriplesInDatabase(cxn)
                val triplesAdded = endingTriplesCount - startingTriplesCount
                val endTime = System.nanoTime()
                val runtime: String = ((endTime - startTime)/1000000000.0).toString
                logger.info("Completed process " + process + " in " + runtime + " seconds")
                
                // create metadata about process
                val metaDataQuery = new DataQuery()
                val metaInfo: HashMap[Value, ArrayBuffer[String]] = HashMap(METAQUERY -> ArrayBuffer(primaryQuery.getQuery()), 
                                                                DATE -> ArrayBuffer(currDate.toString), 
                                                                PROCESS -> ArrayBuffer(process), 
                                                                OUTPUTNAMEDGRAPH -> ArrayBuffer(primaryQuery.defaultOutputGraph, primaryQuery.defaultRemovalsGraph),
                                                                PROCESSRUNTIME -> ArrayBuffer(runtime),
                                                                TRIPLESADDED -> ArrayBuffer(triplesAdded.toString),
                                                                INPUTNAMEDGRAPHS -> processGraphsList
                                                                )
                                                                
                val metaDataTriples = createMetaDataTriples(metaInfo)
                metaDataQuery.createInsertDataClause(metaDataTriples, processNamedGraph)
                //logger.info(metaDataQuery.getQuery())
                metaDataQuery.runQuery(cxn) 
            }
        }
    }

    def createPatternMatchQuery(process: String): PatternMatchQuery =
    {
        assert (localUUID != null, "You must set the globalUUID before running any process.")
        if (!validateProcess(gmCxn, process)) 
        {
            logger.info(process + " is not a valid TURBO process")
            return null
        }
        else
        {
            // retrieve connections (inputs, outputs) from model graph
            // the inputs become the "where" block of the SPARQL query
            // the outputs become the "insert" block
            val inputs = getInputs(process)
            val outputs = getOutputs(process)
            val removals = getRemovals(process)
            
            if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
            if (outputs.size == 0 && removals.size == 0) throw new RuntimeException("Did not receive any outputs or removals")
            
            val inputNamedGraph = inputs(0)(GRAPH.toString).toString
            
            // create primary query
            val primaryQuery = new PatternMatchQuery()
            primaryQuery.setProcess(process)
            primaryQuery.setInputGraph(inputNamedGraph)
            primaryQuery.setInputData(inputs)
            
            var outputNamedGraph: String = null
    
            primaryQuery.createWhereClause(inputs)
            primaryQuery.createBindClause(outputs, inputs, localUUID)
            
            if (outputs.size != 0)
            {
               outputNamedGraph = outputs(0)(GRAPH.toString).toString   
               primaryQuery.setOutputGraph(outputNamedGraph)
            }
            var removalsNamedGraph: String = null
            if (removals.size != 0)
            {
                removalsNamedGraph = removals(0)(GRAPH.toString).toString
                primaryQuery.setRemovalsGraph(removalsNamedGraph)
                primaryQuery.createDeleteClause(removals)
            }
            primaryQuery.createInsertClause(outputs)
            primaryQuery 
        }
    }
    
    def getInputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredInputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select $variablesToSelect
         
         Where
         {
            Graph pmbb:dataModel
            {
              Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionToClassRecipe 
                                            turbo:ObjectConnectionToInstanceRecipe
                                            turbo:DatatypeConnectionRecipe}
              Values ?$INPUTTYPE {turbo:requiredInputTo turbo:optionalInputTo}
              ?connection ?$INPUTTYPE <$process> .
              ?connection a ?$CONNECTIONRECIPETYPE .
              <$process> turbo:inputNamedGraph ?$GRAPH .
              ?connection turbo:subject ?$SUBJECT .
              ?connection turbo:predicate ?$PREDICATE .
              ?connection turbo:object ?$OBJECT .
              ?connection turbo:multiplicity ?$MULTIPLICITY .
              
              Optional
              {
                  ?connection obo:BFO_0000050 ?$OPTIONALGROUP .
                  ?$OPTIONALGROUP a turbo:TurboGraphOptionalGroup .
                  <$process> turbo:buildsOptionalGroup ?$OPTIONALGROUP .
              }
              Optional
              {
                  ?connection obo:BFO_0000050 ?$MINUSGROUP .
                  ?$MINUSGROUP a turbo:TurboGraphMinusGroup .
                  <$process> turbo:buildsMinusGroup ?$MINUSGROUP .
              }
              Optional
              {
                  ?connection turbo:outputOf ?creatingProcess .
                  ?creatingProcess turbo:outputNamedGraph ?$GRAPHOFCREATINGPROCESS .
              }
              Optional
              {
                  ?connection turbo:referencedInGraph ?$GRAPHOFORIGIN .
              }
              Optional
              {
                  ?$OBJECT a ontologies:MultiObjectDescriber .
                  BIND (true AS ?$OBJECTMULTIOBJECTDESCRIBER)
              }
            }
            
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  BIND (true AS ?$SUBJECTTYPE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  BIND (true AS ?$OBJECTTYPE)
              }
         }
         
         """
       //println(query)          
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }

    def getRemovals(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select $variablesToSelect
         
         Where
         {
            Graph pmbb:dataModel
            {
                Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionToClassRecipe 
                                            turbo:ObjectConnectionToInstanceRecipe
                                            turbo:DatatypeConnectionRecipe}

                ?connection turbo:removedBy <$process> .
                ?connection a ?$CONNECTIONRECIPETYPE .
                <$process> turbo:inputNamedGraph ?$GRAPH .
                ?connection turbo:subject ?$SUBJECT .
                ?connection turbo:predicate ?$PREDICATE .
                ?connection turbo:object ?$OBJECT .
            }
         }
         
         """
       
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getOutputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select $variablesToSelect
         Where
         {
            Graph pmbb:dataModel
            {
              Values ?INPUTTO {turbo:requiredInputTo turbo:optionalInputTo}
              Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionToClassRecipe 
                                            turbo:ObjectConnectionToInstanceRecipe
                                            turbo:DatatypeConnectionRecipe}
              ?connection turbo:outputOf <$process> .
              ?connection a ?$CONNECTIONRECIPETYPE .
              <$process> turbo:outputNamedGraph ?$GRAPH .
              ?connection turbo:subject ?$SUBJECT .
              ?connection turbo:predicate ?$PREDICATE .
              ?connection turbo:object ?$OBJECT .
              ?connection turbo:multiplicity ?$MULTIPLICITY .
              
              Optional
              {
                  ?connection turbo:subjectUsesContext ?$SUBJECTCONTEXT .
                  ?$SUBJECT turbo:hasPossibleContext ?$SUBJECTCONTEXT .
                  ?$SUBJECTCONTEXT a turbo:TurboGraphContext .
              }
              Optional
              {
                  ?connection turbo:objectUsesContext ?$OBJECTCONTEXT .
                  ?$OBJECT turbo:hasPossibleContext ?$OBJECTCONTEXT .
                  ?$OBJECTCONTEXT a turbo:TurboGraphContext .
              }
              Optional
              {
                  ?$SUBJECT turbo:usesCustomVariableManipulationRule ?subjectRuleDenoter .
                  ?subjectRuleDenoter ontologies:usesSparql ?$SUBJECTRULE .
              }
              Optional
              {
                  ?$OBJECT turbo:usesCustomVariableManipulationRule ?objectRuleDenoter .
                  ?objectRuleDenoter ontologies:usesSparql ?$OBJECTRULE .
              }
              Optional
              {
                  ?$SUBJECT a turbo:MultiObjectDescriber .
                  BIND (true as ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$OBJECT a turbo:MultiObjectDescriber .
                  BIND (true as ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?recipe turbo:objectRequiredToCreate ?$OBJECT .
                  ?recipe ?INPUTTO <$process> .
                  ?recipe turbo:object ?$OBJECTDEPENDEE .
              }
              Optional
              {
                  ?recipe turbo:objectRequiredToCreate ?$SUBJECT .
                  ?recipe ?INPUTTO <$process> .
                  ?recipe turbo:object ?$SUBJECTDEPENDEE .
              }
            }
            
            Graph <$ontologyURL>
            {
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  BIND (true AS ?$SUBJECTTYPE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  BIND (true AS ?$OBJECTTYPE)
              }
            }
         }
         
         """
       //println(query)
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    /**
     * Sets instantiation and globalUUID variables, and retrieves list of all processes in the order that they should be run. Then runs each process.
     */
    def runAllDrivetrainProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String)
    {
        setGlobalUUID(globalUUID)
        setGraphModelConnection(gmCxn)
        setOutputRepositoryConnection(cxn)
      
        // get list of all processes in order
        val orderedProcessList: ArrayBuffer[String] = getAllProcessesInOrder(gmCxn)
        
        logger.info("Drivetrain will now run the following processes in this order:")
        for (a <- orderedProcessList) logger.info(a)
        
        // run each process
        runProcess(orderedProcessList)
    }

    /**
     * Searches the model graph and returns all processes in the order that they should be run
     * 
     * @return ArrayBuffer[String] where each string represents a process and the index represents where each should be run in a sequence
     */
    def getAllProcessesInOrder(gmCxn: RepositoryConnection): ArrayBuffer[String] =
    {
        val getFirstProcess: String = """
          select ?firstProcess where
          {
              ?firstProcess rdfs:subClassOf turbo:TURBO_0010178 .
              Graph pmbb:dataModel
              {
                  ?firstProcess turbo:precedes ?someProcess .
                  Minus
                  {
                      ?someOtherProcess turbo:precedes ?firstProcess .
                  }
              }
          }
        """
        
        val getProcesses: String = """
          select ?precedingProcess ?succeedingProcess where
          {
              Graph pmbb:dataModel
              {
                  ?precedingProcess turbo:precedes ?succeedingProcess .
              }
          }
        """
        
        val firstProcessRes = update.querySparqlAndUnpackTuple(gmCxn, getFirstProcess, "firstProcess")
        if (firstProcessRes.size > 1) throw new RuntimeException ("Multiple starting processes discovered in graph model")
        if (firstProcessRes.size == 0) throw new RuntimeException ("No starting process discovered in graph model")
        val res = update.querySparqlAndUnpackTuple(gmCxn, getProcesses, Array("precedingProcess", "succeedingProcess"))
        var currProcess: String = firstProcessRes(0)
        var processesInOrder: ArrayBuffer[String] = new ArrayBuffer[String]
        var processMap: HashMap[String, String] = new HashMap[String, String]
        
        for (a <- res) processMap += a(0).toString -> a(1).toString
        
        while (currProcess != null)
        {
            processesInOrder += currProcess
            if (processMap.contains(currProcess)) currProcess = processMap(currProcess)
            else currProcess = null
        }
        processesInOrder
    }
    
    def createMetaDataTriples(metaInfo: HashMap[Value, ArrayBuffer[String]]): ArrayBuffer[Triple] =
    {
        val processVal = metaInfo(PROCESS)(0)
        val currDate = metaInfo(DATE)(0)
        val queryVal = metaInfo(METAQUERY)(0)
        val outputNamedGraph = metaInfo(OUTPUTNAMEDGRAPH)(0)
        val removalsNamedGraph = metaInfo(OUTPUTNAMEDGRAPH)(1)
        val runtime = metaInfo(PROCESSRUNTIME)(0)
        val triplesAdded = metaInfo(TRIPLESADDED)(0)
        val inputNamedGraphsList = metaInfo(INPUTNAMEDGRAPHS)
        
        val timeMeasDatum = helper.genPmbbIRI()
        val processBoundary = helper.genPmbbIRI()
        
        helper.validateURI(processNamedGraph)
        var metaTriples = ArrayBuffer(
             new Triple(processVal, "turbo:TURBO_0010106", queryVal),
             new Triple(processVal, "turbo:TURBO_0010107", runtime),
             new Triple(processVal, "turbo:TURBO_0010108", triplesAdded),
             new Triple(processBoundary, "obo:RO_0002223", processVal),
             new Triple(processBoundary, "rdf:type", "obo:BFO_0000035"),
             new Triple(timeMeasDatum, "obo:IAO_0000136", processBoundary),
             new Triple(timeMeasDatum, "rdf:type", "obo:IAO_0000416"),
             new Triple(timeMeasDatum, "turbo:TURBO_0010094", currDate)
        )
        for (inputGraph <- inputNamedGraphsList) metaTriples += new Triple(processVal, "turbo:TURBO_0010187", inputGraph)
        if ((outputNamedGraph == removalsNamedGraph) || outputNamedGraph != null) metaTriples += new Triple(processVal, "turbo:TURBO_0010186", outputNamedGraph)
        else if (removalsNamedGraph != null) metaTriples += new Triple(processVal, "turbo:TURBO_0010186", removalsNamedGraph)
        metaTriples
    }
    
    def validateInputData(graph: String, inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        val f = cxn.getValueFactory()
        for (input <- inputs)
        {
            val subjectAsType = input(SUBJECT.toString)
            val objectAsType = input(OBJECT.toString)
            val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, false)
            
            val multiplicity = input(MULTIPLICITY.toString).toString
            if (!(input(INPUTTYPE.toString).toString == "http://transformunify.org/ontologies/optionalInputTo"))
            {
                val query = new PatternMatchQuery()
                query.setInputGraph(graph)
                input(MINUSGROUP.toString) = f.createIRI("http://www.itmat.upenn.edu/biobank/validatorMinusGroup")
                input(SUBJECTTYPE.toString) = null
                
                val typeTriple = new HashMap[String, org.eclipse.rdf4j.model.Value]
                typeTriple(SUBJECT.toString) = subjectAsType
                typeTriple(SUBJECTTYPE.toString) = null
                typeTriple(OBJECTTYPE.toString) = null
                typeTriple(GRAPHOFORIGIN.toString) = null
                typeTriple(GRAPHOFCREATINGPROCESS.toString) = null
                typeTriple(OBJECT.toString) = subjectAsType
                typeTriple(MINUSGROUP.toString) = null
                typeTriple(OPTIONALGROUP.toString) = null
                typeTriple(PREDICATE.toString) = f.createIRI("rdf:type")
                typeTriple(CONNECTIONRECIPETYPE.toString) = f.createIRI("http://transformunify.org/ontologies/ObjectToClassConnectionRecipe")
                typeTriple(INPUTTYPE.toString) = f.createIRI("http://transformunify.org/ontologies/requiredInputTo")
                typeTriple(MULTIPLICITY.toString) = f.createIRI("http://transformunify.org/ontologies/1-1")
                typeTriple(GRAPH.toString) = f.createIRI(graph)
                typeTriple(OBJECTMULTIOBJECTDESCRIBER.toString) = null
                
                query.createWhereClause(ArrayBuffer(input, typeTriple))
                val whereBlock = query.whereClause
                val checkRequired = s"SELECT * $whereBlock }"
                println(checkRequired)
                var firstResult = ""
                val res = update.querySparqlAndUnpackTuple(cxn, checkRequired, subjectAsVar)
                if (res.size != 0) firstResult = res(0)
                assert (firstResult == "", s"Input data error: instance $firstResult of type $subjectAsType does not have the required connection to an instance of type $objectAsType in graph $graph") 
            }
        }
    }
}