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

    def validateProcess(process: String): Boolean =
    {
       val ask: String = s"""
          ASK {
            <$process> a turbo:TURBO_0010178 .
          }
          """
        update.querySparqlBoolean(gmCxn, ask).get
    }
    
    def runProcess(process: String): HashMap[String, PatternMatchQuery] =
    {
        runProcess(ArrayBuffer(process))
    }
        
    def runProcess(processes: ArrayBuffer[String]): HashMap[String, PatternMatchQuery] =
    {
        var processQueryMap = new HashMap[String, PatternMatchQuery]
        for (process <- processes)
        {
            if (!validateProcess(process)) logger.info(process + " is not a valid TURBO process")
            else processQueryMap += process -> createPatternMatchQuery(process)
        }
        for (process <- processes)
        {
            val startTime = System.nanoTime()
            val currDate = Calendar.getInstance().getTime()
            val startingTriplesCount = helper.countTriplesInDatabase(cxn)
            val processGraphsList = new ArrayBuffer[String]
            logger.info("Starting process: " + process)
           
            val primaryQuery = processQueryMap(process)
            val genericWhereClause = primaryQuery.whereClause
            // get list of all named graphs which match pattern specified in inputNamedGraph and include match to where clause
            var inputNamedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph, genericWhereClause)
            //var inputNamedGraphsList = helper.generateSimpleNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph)
            logger.info("input named graphs size: " + inputNamedGraphsList.size)
                
            if (inputNamedGraphsList.size == 0) logger.info(s"Cannot run process $process: no input named graphs found")
            else
            {
                // for each input named graph, run query with specified named graph
                for (graph <- inputNamedGraphsList)
                {
                    logger.info("Now running on input graph " + graph)
                    //val localStartingTriplesCount = helper.countTriplesInDatabase(cxn)
                    //run validation on input graph
                    validateInputData(graph, primaryQuery.rawInputData)
                    primaryQuery.whereClause = genericWhereClause.replaceAll(primaryQuery.defaultInputGraph, graph)
                    //logger.info(primaryQuery.getQuery())
                    primaryQuery.runQuery(cxn)
                    /*val localEndingTriplesCount = helper.countTriplesInDatabase(cxn)
                    if (localStartingTriplesCount != localEndingTriplesCount) processGraphsList += graph*/
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
                                                                INPUTNAMEDGRAPHS -> /*processGraphsList*/ inputNamedGraphsList
                                                                )
                                                                
                val metaDataTriples = createMetaDataTriples(metaInfo)
                metaDataQuery.createInsertDataClause(metaDataTriples, processNamedGraph)
                //logger.info(metaDataQuery.getQuery())
                metaDataQuery.runQuery(cxn)  
            }
        }
        processQueryMap
    }

    def createPatternMatchQuery(process: String): PatternMatchQuery =
    {
        assert (localUUID != null, "You must set the globalUUID before running any process.")
        if (!validateProcess(process)) 
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
            primaryQuery.setGraphModelConnection(gmCxn)
            
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
              Values ?$CONNECTIONRECIPETYPE {turbo:ObjectConnectionToTermRecipe 
                                            turbo:ObjectConnectionToInstanceRecipe
                                            turbo:DatatypeConnectionRecipe
                                            turbo:ObjectConnectionFromTermRecipe}
              Values ?$INPUTTYPE {turbo:hasRequiredInput turbo:hasOptionalInput}
              <$process> ?$INPUTTYPE ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              <$process> turbo:inputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME turbo:subject ?$SUBJECT .
              ?$CONNECTIONNAME turbo:predicate ?$PREDICATE .
              ?$CONNECTIONNAME turbo:object ?$OBJECT .
              ?$CONNECTIONNAME turbo:multiplicity ?$MULTIPLICITY .
              
              Optional
              {
                  ?$CONNECTIONNAME obo:BFO_0000050 ?$OPTIONALGROUP .
                  ?$OPTIONALGROUP a turbo:TurboGraphOptionalGroup .
                  <$process> turbo:buildsOptionalGroup ?$OPTIONALGROUP .
              }
              Optional
              {
                  ?$CONNECTIONNAME obo:BFO_0000050 ?$MINUSGROUP .
                  ?$MINUSGROUP a turbo:TurboGraphMinusGroup .
                  <$process> turbo:buildsMinusGroup ?$MINUSGROUP .
              }
              Optional
              {
                  ?creatingProcess turbo:hasOutput ?$CONNECTIONNAME .
                  ?creatingProcess turbo:outputNamedGraph ?$GRAPHOFCREATINGPROCESS .
              }
              Optional
              {
                  ?$CONNECTIONNAME turbo:referencedInGraph ?$GRAPHOFORIGIN .
              }
              Optional
              {
                  ?$OBJECT a ontologies:MultiObjectDescriber .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a ontologies:MultiObjectDescriber .
                  BIND (true AS ?$SUBJECTADESCRIBER)
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
              Optional
              {
                  ?$CONNECTIONNAME turbo:required ?$REQUIREMENT .
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
              Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionToTermRecipe 
                                          turbo:ObjectConnectionToInstanceRecipe
                                          turbo:DatatypeConnectionRecipe
                                          turbo:ObjectConnectionFromTermRecipe}
  
              <$process> turbo:removes ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              <$process> turbo:inputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME turbo:subject ?$SUBJECT .
              ?$CONNECTIONNAME turbo:predicate ?$PREDICATE .
              ?$CONNECTIONNAME turbo:object ?$OBJECT .
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
              Values ?INPUTTO {turbo:hasRequiredInput turbo:hasOptionalInput}
              Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionToTermRecipe 
                                            turbo:ObjectConnectionToInstanceRecipe
                                            turbo:DatatypeConnectionRecipe
                                            turbo:ObjectConnectionFromTermRecipe}
              <$process> turbo:hasOutput ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              <$process> turbo:outputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME turbo:subject ?$SUBJECT .
              ?$CONNECTIONNAME turbo:predicate ?$PREDICATE .
              ?$CONNECTIONNAME turbo:object ?$OBJECT .
              ?$CONNECTIONNAME turbo:multiplicity ?$MULTIPLICITY .
              
              Optional
              {
                  ?$CONNECTIONNAME turbo:subjectUsesContext ?$SUBJECTCONTEXT .
                  ?$SUBJECT turbo:hasPossibleContext ?$SUBJECTCONTEXT .
                  ?$SUBJECTCONTEXT a turbo:TurboGraphContext .
              }
              Optional
              {
                  ?$CONNECTIONNAME turbo:objectUsesContext ?$OBJECTCONTEXT .
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
                  <$process> ?INPUTTO ?recipe .
                  ?recipe turbo:object ?$OBJECTDEPENDEE .
              }
              Optional
              {
                  ?recipe turbo:objectRequiredToCreate ?$SUBJECT .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe turbo:object ?$SUBJECTDEPENDEE .
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
    
    /**
     * Sets instantiation and globalUUID variables, and retrieves list of all processes in the order that they should be run. Then runs each process.
     */
    def runAllDrivetrainProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String = UUID.randomUUID().toString.replaceAll("-", ""))
    {
        setGlobalUUID(globalUUID)
        setGraphModelConnection(gmCxn)
        setOutputRepositoryConnection(cxn)

        validateGraphModelTerms()
        //validateGraphSpecificationAgainstOntology()
      
        // get list of all processes in order
        val orderedProcessList: ArrayBuffer[String] = getAllProcessesInOrder(gmCxn)

        validateProcessesAgainstGraphSpecification(orderedProcessList)
        
        logger.info("Drivetrain will now run the following processes in this order:")
        for (a <- orderedProcessList) logger.info(a)
        
        // run each process
        runProcess(orderedProcessList)
    }

    def validateGraphModelTerms()
    {
        val checkPredicates: String = """
          Select distinct ?predicate Where
          {
              Values ?g {pmbb:dataModel pmbb:graphSpecification}
              Graph ?g
              {
                  ?subject ?predicate ?object .
                  Filter (?predicate NOT IN (
                      turbo:subject,
                      turbo:predicate,
                      turbo:object,
                      rdf:type,
                      turbo:usesCustomVariableManipulationRule,
                      turbo:usesSparql,
                      obo:BFO_0000050,
                      turbo:referencedInGraph,
                      turbo:required,
                      turbo:multiplicity,
                      turbo:inputNamedGraph,
                      turbo:outputNamedGraph,
                      turbo:hasOutput,
                      turbo:hasRequiredInput,
                      turbo:hasOptionalInput,
                      turbo:removes,
                      rdfs:label,
                      turbo:buildsOptionalGroup,
                      turbo:buildsMinusGroup,
                      turbo:precedes,
                      turbo:subjectRequiredToCreate,
                      turbo:objectRequiredToCreate,
                      turbo:subjectUsesContext,
                      turbo:objectUsesContext,
                      turbo:hasPossibleContext,
                      turbo:range,
                      owl:versionInfo,
                      owl:imports,
                      rdfs:subClassOf,
                      rdfs:domain,
                      rdfs:range
                  ))
              }
          }
        """
        //println(checkPredicates)
        var firstRes = ""
        var res = update.querySparqlAndUnpackTuple(gmCxn, checkPredicates, "predicate")
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Error in graph model: predicate $firstRes is not known in the Acorn language")
    
        val checkTypes: String = """
          Select distinct ?type Where
          {
              Values ?g {pmbb:dataModel pmbb:graphSpecification}
              Graph ?g
              {
                  ?subject a ?type .
                  Filter (?type NOT IN (
                      turbo:ObjectConnectionToTermRecipe,
                      turbo:ObjectConnectionToInstanceRecipe,
                      turbo:ObjectConnectionFromTermRecipe,
                      turbo:DatatypeConnectionRecipe,
                      turbo:MultiObjectDescriber,
                      owl:Class,
                      turbo:TurboGraphContext,
                      turbo:TurboGraphMinusGroup,
                      turbo:TurboGraphOptionalGroup,
                      turbo:TurboGraphVariableManipulationLogic,
                      turbo:TurboNamedGraph,
                      owl:Ontology,
                      turbo:TURBO_0010178,
                      owl:ObjectProperty,
                      owl:DatatypeProperty,
                      turbo:TurboGraphStringLiteralValue,
                      turbo:TurboGraphDateLiteralValue,
                      turbo:TurboGraphMultiplicityRule,
                      turbo:TurboGraphDoubleLiteralValue,
                      turbo:TurboGraphIntegerLiteralValue,
                      turbo:TurboGraphBooleanLiteralValue,
                      turbo:TurboGraphRequirementSpecification
                  ))
              }
          }
        """
        //println(checkTypes)
        firstRes = ""
        res = update.querySparqlAndUnpackTuple(gmCxn, checkTypes, "type")
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Error in graph model: type $firstRes is not known in the Acorn language")
    }
    
    def validateProcessesAgainstGraphSpecification(processList: ArrayBuffer[String])
    {
        var processListAsString = ""
        for (process <- processList)
        {
            processListAsString += " <" + process + ">,"
        }
        processListAsString = processListAsString.substring(0, processListAsString.size-1)
        assert (processListAsString != "")

        var filterMultipleProcesses = ""
        if (processList.size > 1)
        {
             filterMultipleProcesses = s"""
                Filter Not Exists
                {
                    ?someOtherProcess ontologies:removes ?recipe .
                }
                filter (?process != ?someOtherProcess)
                filter (?someOtherProcess IN ($processListAsString))
              """
        }
        
        val getOutputsOfAllProcesses = s"""
          Select ?recipe Where
          {
              Graph pmbb:graphSpecification
              {
                  Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionToTermRecipe 
                                            turbo:ObjectConnectionToInstanceRecipe
                                            turbo:DatatypeConnectionRecipe
                                            turbo:ObjectConnectionFromTermRecipe}
                  ?recipe a ?CONNECTIONRECIPETYPE .
              }
              Minus
              {
                  Graph pmbb:dataModel
                  {
                      ?process ontologies:hasOutput ?recipe .
                      $filterMultipleProcesses
                      filter (?process IN ($processListAsString))
                  }
              }
          }
          """
        //println(getOutputsOfAllProcesses)
        var firstRes = ""
        val res = update.querySparqlAndUnpackTuple(gmCxn, getOutputsOfAllProcesses, "recipe")
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Error in graph model: connection $firstRes in graph specification is not the output of a queued process in the data model")
    }
    
    def validateGraphSpecificationAgainstOntology()
    {
        val rangeQuery: String = """
          select * where
          {
              graph pmbb:graphSpecification
              {
                  Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionFromTermRecipe 
                                                turbo:ObjectConnectionToInstanceRecipe
                                                turbo:ObjectConnectionToTermRecipe
                                                }
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe turbo:object ?object .
                  ?recipe turbo:predicate ?predicate .
                  minus
                  {
                      ?object a turbo:MultiObjectDescriber .
                  }
              }
              graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl>
              {
                  ?predicate rdfs:subPropertyOf* ?superPredicate .
                  ?superPredicate rdfs:range ?range .
                  minus
                  {
                      ?object rdfs:subClassOf* ?range .
                  }
              }
          }
          """
        var res = update.querySparqlAndUnpackTuple(gmCxn, rangeQuery, "recipe")
        var firstRes = ""
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "")

        val domainQuery: String = """
          select * where
          {
              graph pmbb:graphSpecification
              {
                  Values ?CONNECTIONRECIPETYPE {turbo:ObjectConnectionFromTermRecipe 
                                                turbo:ObjectConnectionToInstanceRecipe
                                                turbo:ObjectConnectionToTermRecipe
                                                turbo:DatatypeConnectionRecipe
                                                }
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe turbo:subject ?subject .
                  ?recipe turbo:predicate ?predicate .
                  minus
                  {
                      ?subject a turbo:MultiObjectDescriber .
                  }
              }
              graph <https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl>
              {
                  ?predicate rdfs:subPropertyOf* ?superPredicate .
                  ?superPredicate rdfs:domain ?domain .
                  minus
                  {
                      ?subject rdfs:subClassOf* ?domain .
                  }
              }
          }
          """
        res = update.querySparqlAndUnpackTuple(gmCxn, domainQuery, "recipe")
        firstRes = ""
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "")
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
              Graph pmbb:dataModel
              {
                  ?firstProcess a turbo:TURBO_0010178 .
                  Minus
                  {
                      ?someOtherProcess turbo:precedes ?firstProcess .
                      ?someOtherProcess a turbo:TURBO_0010178 .
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
                  ?precedingProcess a turbo:TURBO_0010178 .
                  ?succeedingProcess a turbo:TURBO_0010178 .
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
        for (input <- inputs)
        {   
            if (input(REQUIREMENT.toString) != null && input(REQUIREMENT.toString).toString != "http://transformunify.org/ontologies/notRequired")
            {
                if (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/bothRequired")
                {
                    validateSubjectAgainstObject(graph, input)
                    validateObjectAgainstSubject(graph, input)
                }
                else if (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/subjectRequired")
                {
                    validateSubjectAgainstObject(graph, input)
                }
                else if (input(REQUIREMENT.toString).toString == "http://transformunify.org/ontologies/objectRequired")
                {
                    validateObjectAgainstSubject(graph, input)
                }
            }
        }
    }
    
    def validateSubjectAgainstObject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val f = cxn.getValueFactory()
        val subjectAsType = input(SUBJECT.toString)
        val objectAsType = input(OBJECT.toString)
        val objectAsVar = helper.convertTypeToSparqlVariable(objectAsType, false)
        val multiplicity = input(MULTIPLICITY.toString).toString
        input(MINUSGROUP.toString) = f.createIRI("http://www.itmat.upenn.edu/biobank/validatorMinusGroup")
        
        input(OBJECTTYPE.toString) = null
        input(OBJECTADESCRIBER.toString) = f.createLiteral(true)
        val objectTypeInput = helper.makeGenericTypeInput(f, objectAsType, graph)
        
        val query = new PatternMatchQuery()
        query.setGraphModelConnection(gmCxn)
        query.setInputGraph(graph)
        
        query.createWhereClause(ArrayBuffer(input, objectTypeInput))
        val whereBlock = query.whereClause
        val checkRequired = s"SELECT * $whereBlock }"
        println(checkRequired)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, checkRequired, objectAsVar)
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $objectAsType does not have the required connection to an instance of type $subjectAsType in graph $graph") 
    }
    
    def validateObjectAgainstSubject(graph: String, input: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val f = cxn.getValueFactory()
        val subjectAsType = input(SUBJECT.toString)
        val objectAsType = input(OBJECT.toString)
        val subjectAsVar = helper.convertTypeToSparqlVariable(subjectAsType, false)
        val multiplicity = input(MULTIPLICITY.toString).toString
        
        input(MINUSGROUP.toString) = f.createIRI("http://www.itmat.upenn.edu/biobank/validatorMinusGroup")
        input(SUBJECTTYPE.toString) = null
        input(SUBJECTADESCRIBER.toString) = f.createLiteral(true)
        
        val subjectTypeInput = helper.makeGenericTypeInput(f, subjectAsType, graph)
        
        val query = new PatternMatchQuery()
        query.setGraphModelConnection(gmCxn)
        query.setInputGraph(graph)
        
        query.createWhereClause(ArrayBuffer(input, subjectTypeInput))
        val whereBlock = query.whereClause
        val checkRequired = s"SELECT * $whereBlock }"
        println(checkRequired)
        var firstResult = ""
        val res = update.querySparqlAndUnpackTuple(cxn, checkRequired, subjectAsVar)
        if (res.size != 0) firstResult = res(0)
        assert (firstResult == "", s"Input data error: instance $firstResult of type $subjectAsType does not have the required connection to an instance of type $objectAsType in graph $graph") 
    }
}