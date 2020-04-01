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
import java.text.SimpleDateFormat
import scala.collection.parallel._

object RunDrivetrainProcess extends ProjectwideGlobals
{
    var localUUID: String = null
    var variableSet = new HashSet[Value]
    var inputSet = new HashSet[Value]
    var inputProcessSet = new HashSet[String]
    var typeMap = new HashMap[String,Value]
    var multithread = useMultipleThreads
    
    var useInputNamedGraphsCache: Boolean = true
    
    val taskSupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(4))
    
    def setGlobalUUID(globalUUID: String)
    {
        this.localUUID = globalUUID
    }
    def setMultithreading(multithread: Boolean)
    {
        this.multithread = multithread
    }
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
        InputDataValidator.setGraphModelConnection(gmCxn)
        GraphModelValidator.setGraphModelConnection(gmCxn)
    }
    def setOutputRepositoryConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }
    def setInputNamedGraphsCache(useInputNamedGraphsCache: Boolean)
    {
        this.useInputNamedGraphsCache = useInputNamedGraphsCache
    }
    
    def runProcess(processSpecification: String, dataValidationMode: String = dataValidationMode, validateAgainstOntology: Boolean = validateAgainstOntology): HashMap[String, PatternMatchQuery] =
    {
        runProcess(ArrayBuffer(processSpecification), dataValidationMode, validateAgainstOntology)
    }
        
    def runProcess(processSpecifications: ArrayBuffer[String], dataValidationMode: String, validateAgainstOntology: Boolean): HashMap[String, PatternMatchQuery] =
    {
        GraphModelValidator.checkAcornFilesForMissingTypes()
        if (validateAgainstOntology) GraphModelValidator.validateGraphSpecificationAgainstOntology()
        
        var processQueryMap = new HashMap[String, PatternMatchQuery]
        for (processSpecification <- processSpecifications)
        {
            val updateProcess = helper.genTurboIRI()
            processQueryMap += processSpecification -> createPatternMatchQuery(processSpecification, updateProcess)
        }
        for (processSpecification <- processSpecifications)
        {
            val startTime = System.nanoTime()
            //val currDate = Calendar.getInstance().getTime()
            val currDate = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" ).format( Calendar.getInstance().getTime())
            val startingTriplesCount = helper.countTriplesInDatabase(cxn)
            logger.info("Starting process: " + processSpecification)
           
            val primaryQuery = processQueryMap(processSpecification)
            val genericWhereClause = primaryQuery.whereClause
            primaryQuery.defaultInputGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(primaryQuery.defaultInputGraph)
            
            // get list of all named graphs which match pattern specified in inputNamedGraph and include match to where clause
            //var inputNamedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph, genericWhereClause)
            // get list of all named graphs which match pattern specified in inputNamedGraph but without match on where clause
            var inputNamedGraphsList = helper.generateSimpleNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph, useInputNamedGraphsCache)
            logger.info("\tinput named graphs size: " + inputNamedGraphsList.size)
            if (inputNamedGraphsList.size == 0) logger.info(s"\tCannot run process $processSpecification: no input named graphs found")
            else
            {
                //run validation on input graph
                if (dataValidationMode == "stop" || dataValidationMode == "log")
                {
                    logger.info(s"\tRunning on Input Data Validation Mode $dataValidationMode")
                    InputDataValidator.setOutputRepositoryConnection(cxn)
                    InputDataValidator.validateInputData(inputNamedGraphsList, primaryQuery.rawInputData, dataValidationMode)
                }
                else logger.info("\tInput Data Validation turned off for this instantiation")
                // for each input named graph, run query with specified named graph
                if (multithread)
                {
                    logger.info("Multiple threads enabled")
                    val parColl = inputNamedGraphsList.par
                    parColl.tasksupport = taskSupport
                    parColl.foreach(submitQuery(_, primaryQuery, genericWhereClause))
                }
                else 
                {
                    logger.info("Multiple threads disabled")
                    inputNamedGraphsList.foreach(submitQuery(_, primaryQuery, genericWhereClause, cxn))
                }
                // set back to generic input named graph for storing in metadata
                primaryQuery.whereClause = genericWhereClause
                
                val endingTriplesCount = helper.countTriplesInDatabase(cxn)
                val triplesAdded = endingTriplesCount.subtract(startingTriplesCount)
                val endTime = System.nanoTime()
                val runtime: String = ((endTime - startTime)/1000000000.0).toString
                logger.info("Completed process " + processSpecification + " in " + runtime + " seconds")

                // create metadata about process
                val metaDataQuery = new DataQuery()
                val metaInfo: HashMap[Value, ArrayBuffer[String]] = HashMap(METAQUERY -> ArrayBuffer(primaryQuery.getQuery()), 
                                                                DATE -> ArrayBuffer(currDate.toString), 
                                                                PROCESSSPECIFICATION -> ArrayBuffer(processSpecification), 
                                                                PROCESS -> ArrayBuffer(primaryQuery.process),
                                                                OUTPUTNAMEDGRAPH -> ArrayBuffer(primaryQuery.defaultOutputGraph, primaryQuery.defaultRemovalsGraph),
                                                                PROCESSRUNTIME -> ArrayBuffer(runtime),
                                                                TRIPLESADDED -> ArrayBuffer(triplesAdded.toString),
                                                                INPUTNAMEDGRAPHS -> inputNamedGraphsList
                                                                )
                                                                
                val metaDataTriples = createMetaDataTriples(metaInfo)
                metaDataQuery.createInsertDataClause(metaDataTriples, processNamedGraph)
                //logger.info(metaDataQuery.getQuery())
                metaDataQuery.runQuery(cxn)  
                if (triplesAdded == 0) logger.warn("Process " + processSpecification + " did not add any triples upon execution")
            }
        }
        processQueryMap
    }
    
    def submitQuery(inputNamedGraph: String, primaryQuery: PatternMatchQuery, genericWhereClause: String, paramCxn: RepositoryConnection = null)
    {
        logger.info("Now running on input named graph: " + inputNamedGraph)
        var whereClauseString = primaryQuery.whereClause
        whereClauseString = whereClauseString.replaceAll(primaryQuery.defaultInputGraph, inputNamedGraph)
        val localQuery = primaryQuery.deleteClause + "\n" + primaryQuery.insertClause + "\n" + whereClauseString + primaryQuery.bindClause + "}"
        if (paramCxn == null)
        {
            val graphConnection = ConnectToGraphDB.getNewConnectionToRepo()
            val localCxn = graphConnection.cxn
            //logger.info(localQuery)
            update.updateSparql(localCxn, localQuery)
            ConnectToGraphDB.closeGraphConnection(graphConnection)
            //logger.info("finished named graph: " + inputNamedGraph) 
        }
        else update.updateSparql(paramCxn, localQuery)
    }

    def createPatternMatchQuery(processSpecification: String, process: String = helper.genTurboIRI()): PatternMatchQuery =
    {
        if (localUUID == null) localUUID = UUID.randomUUID().toString().replaceAll("-", "")
        var thisProcessSpecification = helper.getProcessNameAsUri(processSpecification)
        
        GraphModelValidator.validateProcessSpecification(thisProcessSpecification)
        GraphModelValidator.validateConnectionRecipesInProcess(thisProcessSpecification)
        
        // retrieve connections (inputs, outputs) from model graph
        // the inputs become the "where" block of the SPARQL query
        // the outputs become the "insert" block
        val inputs = getInputs(thisProcessSpecification)
        val outputs = getOutputs(thisProcessSpecification)
        val removals = getRemovals(thisProcessSpecification)
        
        if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
        if (outputs.size == 0 && removals.size == 0) throw new RuntimeException("Did not receive any outputs or removals")
        
        GraphModelValidator.validateAcornResults(inputs)
        GraphModelValidator.validateAcornResults(outputs)
        
        var inputNamedGraph = inputs(0)(GRAPH.toString).toString
        
        // create primary query
        val primaryQuery = new PatternMatchQuery()
        primaryQuery.setProcessSpecification(thisProcessSpecification)
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
        assert(!primaryQuery.getQuery().contains("https://github.com/PennTURBO/Drivetrain/blob/master/turbo_properties.properties/"), "Could not complete properties term replacement")
        primaryQuery 
    }
    
    def getInputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredInputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         
         Where
         {
              Values ?$INPUTTYPE {drivetrain:hasRequiredInput drivetrain:hasOptionalInput}
              <$process> ?$INPUTTYPE ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              ?$CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:inputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME drivetrain:subject ?$SUBJECT .
              ?$CONNECTIONNAME drivetrain:predicate ?$PREDICATE .
              ?$CONNECTIONNAME drivetrain:object ?$OBJECT .
              ?$CONNECTIONNAME drivetrain:multiplicity ?$MULTIPLICITY .
              
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:subjectUsesContext ?$SUBJECTCONTEXT .
                  ?$SUBJECT drivetrain:hasPossibleContext ?$SUBJECTCONTEXT .
                  ?$SUBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:objectUsesContext ?$OBJECTCONTEXT .
                  ?$OBJECT drivetrain:hasPossibleContext ?$OBJECTCONTEXT .
                  ?$OBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:partOf ?$OPTIONALGROUP .
                  ?$OPTIONALGROUP a drivetrain:TurboGraphOptionalGroup .
                  <$process> drivetrain:buildsOptionalGroup ?$OPTIONALGROUP .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:partOf ?$MINUSGROUP .
                  ?$MINUSGROUP a drivetrain:TurboGraphMinusGroup .
                  <$process> drivetrain:buildsMinusGroup ?$MINUSGROUP .
              }
              Optional
              {
                  # this feature is a little sketcky. What if the creatingProcess is not queued? What if it is created by multiple processes?
                  ?creatingProcess drivetrain:hasOutput ?$CONNECTIONNAME .
                  ?creatingProcess drivetrain:outputNamedGraph ?$GRAPHOFCREATINGPROCESS .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:referencedInGraph ?$GRAPHOFORIGIN .
              }
              Optional
              {
                  ?$OBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToInstanceRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToTermRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToLiteralRecipe)
                  BIND (true AS ?$SUBJECTANINSTANCE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:InstanceToTermRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToTermRecipe)
                  BIND (true AS ?$OBJECTANINSTANCE)
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:mustExecuteIf ?$REQUIREMENT .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:predicateSuffix ?suffix .
                  ?suffix a drivetrain:PredicateSuffixSymbol .
                  ?suffix drivetrain:usesSparqlOperator ?$SUFFIXOPERATOR .
              }
              Optional
              {
                  ?$OBJECT a ?$GRAPHLITERALTYPE .
                  ?$GRAPHLITERALTYPE rdfs:subClassOf* drivetrain:LiteralResourceList .
                  minus
                  {
                      ?OBJECT a ?GRAPHLITERALTYPE2 .
                      ?GRAPHLITERALTYPE2 rdfs:subClassOf+ ?GRAPHLITERALTYPE .
                  }
                  BIND (true AS ?$OBJECTALITERAL)
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERALVALUE)
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
         
         Select distinct $variablesToSelect
         
         Where
         {
              <$process> drivetrain:removes ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              ?$CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:outputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME drivetrain:subject ?$SUBJECT .
              ?$CONNECTIONNAME drivetrain:predicate ?$PREDICATE .
              ?$CONNECTIONNAME drivetrain:object ?$OBJECT .
              Optional
              {
                  ?$OBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:ClassResourceList .
                  BIND (true AS ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToInstanceRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToTermRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToLiteralRecipe)
                  BIND (true AS ?$SUBJECTANINSTANCE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:InstanceToTermRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToTermRecipe)
                  BIND (true AS ?$OBJECTANINSTANCE)
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERALVALUE)
         }
         
         """
       
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getOutputs(process: String): ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select distinct $variablesToSelect
         Where
         {
              Values ?INPUTTO {drivetrain:hasRequiredInput drivetrain:hasOptionalInput}
              <$process> drivetrain:hasOutput ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              ?$CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              <$process> drivetrain:outputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME drivetrain:subject ?$SUBJECT .
              ?$CONNECTIONNAME drivetrain:predicate ?$PREDICATE .
              ?$CONNECTIONNAME drivetrain:object ?$OBJECT .
              ?$CONNECTIONNAME drivetrain:multiplicity ?$MULTIPLICITY .
              
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:subjectUsesContext ?$SUBJECTCONTEXT .
                  ?$SUBJECT drivetrain:hasPossibleContext ?$SUBJECTCONTEXT .
                  ?$SUBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:objectUsesContext ?$OBJECTCONTEXT .
                  ?$OBJECT drivetrain:hasPossibleContext ?$OBJECTCONTEXT .
                  ?$OBJECTCONTEXT a drivetrain:TurboGraphContext .
              }
              Optional
              {
                  ?$SUBJECT drivetrain:usesCustomVariableManipulationRule ?subjectRuleDenoter .
                  ?subjectRuleDenoter drivetrain:usesSparql ?$SUBJECTRULE .
              }
              Optional
              {
                  ?$OBJECT drivetrain:usesCustomVariableManipulationRule ?objectRuleDenoter .
                  ?objectRuleDenoter drivetrain:usesSparql ?$OBJECTRULE .
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:ClassResourceList .
                  BIND (true as ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$OBJECT a drivetrain:ClassResourceList .
                  BIND (true as ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?recipe drivetrain:objectRequiredToCreate ?$OBJECT .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe drivetrain:object ?$OBJECTDEPENDEE .
              }
              Optional
              {
                  ?recipe drivetrain:objectRequiredToCreate ?$SUBJECT .
                  <$process> ?INPUTTO ?recipe .
                  ?recipe drivetrain:object ?$SUBJECTDEPENDEE .
              }
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToInstanceRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToTermRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToLiteralRecipe)
                  BIND (true AS ?$SUBJECTANINSTANCE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:InstanceToTermRecipe)
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:TermToTermRecipe)
                  BIND (true AS ?$OBJECTANINSTANCE)
              }
              Optional
              {
                  ?$OBJECT a ?graphLiteral .
                  ?graphLiteral rdfs:subClassOf* drivetrain:LiteralResourceList .
                  BIND (true AS ?$OBJECTALITERAL)
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERALVALUE)
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
      
        // get list of all processes in order
        val orderedProcessList: ArrayBuffer[String] = getAllProcessesInOrder(gmCxn)
        
        GraphModelValidator.validateProcessesAgainstGraphSpecification(orderedProcessList)
        
        logger.info("Drivetrain will now run the following processes in this order:")
        for (a <- orderedProcessList) logger.info(a)
        
        // run each process
        runProcess(orderedProcessList, dataValidationMode, validateAgainstOntology)
    }

    /**
     * Searches the model graph and returns all processes in the order that they should be run
     * 
     * @return ArrayBuffer[String] where each string represents a process and the index represents where each should be run in a sequence
     */
    def getAllProcessesInOrder(gmCxn: RepositoryConnection): ArrayBuffer[String] =
    {
        val getFirstProcess: String = s"""
          select ?firstProcess where
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ?firstProcess a turbo:TURBO_0010354 .
                  Minus
                  {
                      ?someOtherProcess drivetrain:precedes ?firstProcess .
                      ?someOtherProcess a turbo:TURBO_0010354 .
                  }
              }
          }
        """
        
        val getProcesses: String = s"""
          select ?precedingProcess ?succeedingProcess where
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ?precedingProcess drivetrain:precedes ?succeedingProcess .
                  ?precedingProcess a turbo:TURBO_0010354 .
                  ?succeedingProcess a turbo:TURBO_0010354 .
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
        val processSpecification = metaInfo(PROCESSSPECIFICATION)(0)
        val currDate = metaInfo(DATE)(0)
        val queryVal = metaInfo(METAQUERY)(0)
        val outputNamedGraph = metaInfo(OUTPUTNAMEDGRAPH)(0)
        val removalsNamedGraph = metaInfo(OUTPUTNAMEDGRAPH)(1)
        val runtime = metaInfo(PROCESSRUNTIME)(0)
        val triplesAdded = metaInfo(TRIPLESADDED)(0)
        val inputNamedGraphsList = metaInfo(INPUTNAMEDGRAPHS)
        val updateProcess = metaInfo(PROCESS)(0)
        
        val updatePlanUri = helper.genTurboIRI()
        
        val timeMeasDatum = helper.genTurboIRI()
        val processBoundary = helper.genTurboIRI()
        
        helper.validateURI(processNamedGraph)
        var metaTriples = ArrayBuffer(
             new Triple(processSpecification, "turbo:TURBO_0010106", queryVal),
             new Triple(updateProcess, "turbo:TURBO_0010107", runtime),
             new Triple(updateProcess, "turbo:TURBO_0010108", triplesAdded),
             new Triple(updateProcess, "rdf:type", "turbo:TURBO_0010347"),
             new Triple(updateProcess, "obo:BFO_0000055", updatePlanUri),
             new Triple(updatePlanUri, "rdf:type", "turbo:TURBO_0010373"),
             new Triple(updatePlanUri, "obo:RO_0000059", processSpecification),
             new Triple(processSpecification, "rdf:type", "turbo:TURBO_0010354"),
             new Triple(processBoundary, "obo:RO_0002223", updateProcess),
             new Triple(processBoundary, "rdf:type", "obo:BFO_0000035"),
             new Triple(timeMeasDatum, "obo:IAO_0000136", processBoundary),
             new Triple(timeMeasDatum, "rdf:type", "obo:IAO_0000416")
        )
        for (inputGraph <- inputNamedGraphsList) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(inputGraph)
            metaTriples += new Triple(updateProcess, "turbo:TURBO_0010187", graphForThisRow)
        }
        if ((outputNamedGraph == removalsNamedGraph) || outputNamedGraph != null) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(outputNamedGraph)
            metaTriples += new Triple(updateProcess, "turbo:TURBO_0010186", graphForThisRow)
        }
        else if (removalsNamedGraph != null) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(removalsNamedGraph)
            metaTriples += new Triple(updateProcess, "turbo:TURBO_0010186", graphForThisRow)
        }
        
        //Triple class currently does not support literal datatypes other than strings, so making custom query to insert current date with dateTime format
        val dateInsert = s"""INSERT DATA {Graph <$processNamedGraph> { <$timeMeasDatum> obo:IAO_0000004 "$currDate"^^xsd:dateTime .}}"""
        //logger.info(dateInsert)
        update.updateSparql(cxn, dateInsert)
        
        metaTriples
    }
}
