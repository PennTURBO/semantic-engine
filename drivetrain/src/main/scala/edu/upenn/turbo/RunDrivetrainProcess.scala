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
    
    var inputDataValidator: InputDataValidator = null
    var graphModelValidator: GraphModelValidator = null
    
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
    def setConnections(gmCxn: RepositoryConnection, cxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
        this.cxn = cxn
        inputDataValidator = new InputDataValidator(gmCxn, cxn)
        graphModelValidator = new GraphModelValidator(gmCxn)
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
        graphModelValidator.checkAcornFilesForMissingTypes()
        if (validateAgainstOntology) graphModelValidator.validateGraphSpecificationAgainstOntology()
        
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
                    inputDataValidator.validateInputData(inputNamedGraphsList, primaryQuery.rawInputData, dataValidationMode)
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
        // create empty connection lists to populate
        var outputSingletonClasses = new HashSet[String]
        var outputSuperSingletonClasses = new HashSet[String]
        var inputSingletonClasses = new HashSet[String]
        var inputSuperSingletonClasses = new HashSet[String]
        var outputOneToOneConnections = new HashMap[String, HashMap[String, String]]
        var inputOneToOneConnections = new HashMap[String, HashMap[String, String]]
        var inputOneToManyConnections = new HashMap[String, HashMap[String, String]]
        var inputManyToOneConnections = new HashMap[String, HashMap[String, String]]
        var outputOneToManyConnections = new HashMap[String, HashMap[String, String]]
        var outputManyToOneConnections = new HashMap[String, HashMap[String, String]]
        
        // list of all literals in the output
        var literalList = new HashSet[String]
        // list of all required elements in the input
        var requiredList = new HashSet[String]
        // list of all optional elements in the input
        var optionalList = new HashSet[String]
        // list of all output nodes that need to be bound in the bind clause
        var classResourceLists = new HashSet[String]
        // list of all class resource lists
        var nodesToCreate = new HashSet[String]
        // this boolean keeps track of whether there are any 1-many or many-1 cardinalities in the input, if not we can use any input element as a cardinality enforcer
        var inputHasLevelChange = false
        // list of output nodes dependent on input nodes, as specified in acorn files
        var dependenciesList = new HashMap[String, org.eclipse.rdf4j.model.Value]
        // list of custom sparql bind rules, as specified in acorn files
        var customRulesList = new HashMap[String, org.eclipse.rdf4j.model.Value]
        // a map with keys of variables that will be bound in the where clause and values a boolean whether that variable can be used as a cardinality enforcer
        var boundInWhereClause = new HashSet[String]
        
        val modelReader = new GraphModelReader(gmCxn)
    
        if (localUUID == null) localUUID = UUID.randomUUID().toString().replaceAll("-", "")
        logger.info(s"Creating new IRIs with hash $localUUID")
        var thisProcessSpecification = helper.getProcessNameAsUri(processSpecification)
        
        graphModelValidator.validateProcessSpecification(thisProcessSpecification)
        graphModelValidator.validateConnectionRecipesInProcess(thisProcessSpecification)
        graphModelValidator.validateConnectionRecipeTypeDeclarations(thisProcessSpecification)
        
        // retrieve connections (inputs, outputs) from model graph
        // the inputs become the "where" block of the SPARQL query
        // the outputs become the "insert" block
        val inputs = modelReader.getInputs(thisProcessSpecification)
        val outputs = modelReader.getOutputs(thisProcessSpecification)
        val removals = modelReader.getRemovals(thisProcessSpecification)
        
        if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
        if (outputs.size == 0 && removals.size == 0) throw new RuntimeException("Did not receive any outputs or removals")
        
        for (row <- inputs) 
        {
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            val recipeType = row(CONNECTIONRECIPETYPE.toString).toString
            
            var objectALiteral = false
            if (recipeType == instToLiteralRecipe || recipeType == termToLiteralRecipe) 
            {
                objectALiteral = true
                literalList += objectString
            }

            if (row(INPUTTYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/hasOptionalInput" || row(OPTIONALGROUP.toString) != null)
            {
                if (!requiredList.contains(subjectString)) optionalList += subjectString
                if (!requiredList.contains(objectString)) optionalList += objectString
            }
            else
            {
                requiredList += subjectString
                requiredList += objectString
                optionalList.remove(subjectString)
                optionalList.remove(objectString)
            }
            
            var subjectAnInstance = false
            if (recipeType == instToInstRecipe || recipeType == instToTermRecipe || recipeType == instToLiteralRecipe) subjectAnInstance = true
            var objectAnInstance = false
            if (recipeType == instToInstRecipe || recipeType == termToInstRecipe) objectAnInstance = true
            var objectADescriber = false
            var subjectADescriber = false
            if (row(SUBJECTADESCRIBER.toString) != null) 
            {
                classResourceLists += subjectString
                subjectADescriber = true
            }
            if (row(OBJECTADESCRIBER.toString) != null) 
            {
                classResourceLists += objectString
                objectADescriber = true
            }
            assert (!(objectALiteral && objectAnInstance))
            assert (!(objectALiteral && objectADescriber))
        
            boundInWhereClause += subjectString
            boundInWhereClause += objectString
            
            if (recipeType == instToLiteralRecipe || recipeType == instToInstRecipe)
            {
                if (thisMultiplicity == oneToOneMultiplicity) 
                {
                    inputOneToOneConnections = handleOneToOneConnection(subjectString, objectString, connectionName, inputOneToOneConnections, objectALiteral)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/many-singleton")
                {
                    inputSingletonClasses += objectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-many")
                {
                    inputSingletonClasses += subjectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-singleton")
                {
                    inputSingletonClasses += subjectString
                    inputSingletonClasses += objectString
                }
                else if (thisMultiplicity.endsWith("superSingleton"))
                {
                    inputSuperSingletonClasses += objectString
                }
                else if (thisMultiplicity.contains("superSingleton"))
                {
                    inputSuperSingletonClasses += subjectString
                }
                else if (thisMultiplicity == oneToManyMultiplicity)
                {
                    inputHasLevelChange = true
                    if (inputOneToManyConnections.contains(subjectString)) inputOneToManyConnections(subjectString) += objectString -> connectionName
                    else inputOneToManyConnections += subjectString -> HashMap(objectString -> connectionName)
                }
                else if (thisMultiplicity == manyToOneMultiplicity)
                {
                    inputHasLevelChange = true
                    if (inputManyToOneConnections.contains(subjectString)) inputManyToOneConnections(subjectString) += objectString -> connectionName
                    else inputManyToOneConnections += subjectString -> HashMap(objectString -> connectionName)
                }
                else throw new RuntimeException(s"Unrecognized input cardinality setting: $thisMultiplicity")
            }
        }
        
        for (row <- outputs)
        {
            val recipeType = row(CONNECTIONRECIPETYPE.toString).toString

            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            var subjectAnInstance = false
            if (recipeType == instToInstRecipe || recipeType == instToTermRecipe || recipeType == instToLiteralRecipe) subjectAnInstance = true
            var objectAnInstance = false
            if (recipeType == instToInstRecipe || recipeType == termToInstRecipe) objectAnInstance = true
            var objectALiteral = false
            if (recipeType == instToLiteralRecipe || recipeType == termToLiteralRecipe) 
            {
                objectALiteral = true
                literalList += objectString
            }
            assert (!(objectALiteral && objectAnInstance))
            
            val subjectDependee = row(SUBJECTDEPENDEE.toString)
            val objectDependee = row(OBJECTDEPENDEE.toString)
            if (subjectDependee != null) dependenciesList += subjectString -> subjectDependee
            if (objectDependee != null) dependenciesList += objectString -> objectDependee
            
            val subjectCustomRule = row(SUBJECTRULE.toString)
            val objectCustomRule = row (OBJECTRULE.toString)
            if (subjectCustomRule != null) customRulesList += subjectString -> subjectCustomRule
            if (objectCustomRule != null) customRulesList += objectString -> objectCustomRule
                        
            // determine which nodes will need to be created in the bind clause
            if (!boundInWhereClause.contains(subjectString) && subjectAnInstance) nodesToCreate += subjectString
            if (!boundInWhereClause.contains(objectString) && objectAnInstance) nodesToCreate += objectString
                        
            if (recipeType == instToLiteralRecipe || recipeType == instToInstRecipe)
            {
                if (thisMultiplicity == oneToOneMultiplicity) 
                {
                    handleOneToOneConnection(subjectString, objectString, connectionName, outputOneToOneConnections, objectALiteral)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/many-singleton")
                {
                    outputSingletonClasses += objectString
                    nodesToCreate.remove(objectString)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-many")
                {
                    outputSingletonClasses += subjectString
                    nodesToCreate.remove(subjectString)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/superSingleton-many")
                {
                    outputSuperSingletonClasses += subjectString
                    nodesToCreate.remove(subjectString)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/many-superSingleton")
                {
                    outputSuperSingletonClasses += objectString
                    nodesToCreate.remove(objectString)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-singleton")
                {
                    outputSingletonClasses += subjectString
                    outputSingletonClasses += objectString
                    nodesToCreate.remove(subjectString)
                    nodesToCreate.remove(objectString)
                }
                else if (thisMultiplicity.endsWith("superSingleton"))
                {
                    outputSuperSingletonClasses += objectString
                    nodesToCreate.remove(objectString)
                }
                else if (thisMultiplicity.contains("superSingleton"))
                {
                    outputSuperSingletonClasses += subjectString
                    nodesToCreate.remove(subjectString)
                }
                else if (thisMultiplicity == oneToManyMultiplicity)
                {
                    if (outputOneToManyConnections.contains(subjectString)) outputOneToManyConnections(subjectString) += objectString -> connectionName
                    else outputOneToManyConnections += subjectString -> HashMap(objectString -> connectionName)
                }
                else if (thisMultiplicity == manyToOneMultiplicity)
                {
                    if (outputManyToOneConnections.contains(subjectString)) outputManyToOneConnections(subjectString) += objectString -> connectionName
                    else outputManyToOneConnections += subjectString -> HashMap(objectString -> connectionName)
                } 
                else throw new RuntimeException(s"Unrecognized input cardinality setting: $thisMultiplicity")
            }
        }
                       
        val inputInstanceCountMap = CardinalityCountBuilder.getInstanceCounts(inputs)
        val outputInstanceCountMap = CardinalityCountBuilder.getInstanceCounts(outputs)
        
        val setConnectionLists = HashMap("outputSingletonList" -> outputSingletonClasses, "inputSingletonList" -> inputSingletonClasses,
                                        "outputSuperSingletonList" -> outputSuperSingletonClasses, "inputSuperSingletonList" -> inputSuperSingletonClasses)
        val mapConnectionLists = HashMap("outputOneToOneList" -> outputOneToOneConnections, "inputOneToOneList" -> inputOneToOneConnections,
                                        "outputOneToManyList" -> outputOneToManyConnections, "inputOneToManyList" -> inputOneToManyConnections,
                                        "outputManyToOneList" -> outputManyToOneConnections, "inputManyToOneList" -> inputManyToOneConnections)
        
        //graphModelValidator.validateAcornResults(thisProcessSpecification, inputs, outputs, setConnectionLists, mapConnectionLists)
        
        var inputNamedGraph = inputs(0)(GRAPH.toString).toString
        
        // create primary query
        val primaryQuery = new PatternMatchQuery(gmCxn)
        primaryQuery.setProcessSpecification(thisProcessSpecification)
        primaryQuery.setProcess(process)
        primaryQuery.setInputGraph(inputNamedGraph)
        primaryQuery.setInputData(inputs)
        
        var outputNamedGraph: String = null
        primaryQuery.createWhereClause(inputs)
        primaryQuery.createBindClause(mapConnectionLists, setConnectionLists, localUUID, customRulesList, dependenciesList,
                                      nodesToCreate, inputHasLevelChange, inputInstanceCountMap, outputInstanceCountMap, 
                                      literalList, boundInWhereClause, optionalList, classResourceLists)
        
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
    
    /*
     *  Makes a map of one to one connections, where each key is an element and each value is a map where each key is a connected element
     *  and each value is the relevant connection name. Objects are not shown as "connected" with themselves.
     */
    def handleOneToOneConnection(thisSubject: String, thisObject: String, connectionName: String, listToPopulate: HashMap[String, HashMap[String, String]], objectALiteral: Boolean): HashMap[String, HashMap[String, String]] =
    {
        // make it mutable
        var listToReturn = listToPopulate
        if (listToReturn.contains(thisObject) && listToReturn.contains(thisSubject))
        {
            listToReturn(thisObject) += thisSubject -> connectionName
            listToReturn(thisSubject) += thisObject -> connectionName
            
            listToReturn = addToConnectionList(thisSubject, connectionName, listToReturn)
            listToReturn = addToConnectionList(thisObject, connectionName, listToReturn)
        }
        
        else if (listToReturn.contains(thisObject) && (!listToReturn.contains(thisSubject)))
        {
            listToReturn(thisObject) += thisSubject -> connectionName
            listToReturn.put(thisSubject, HashMap(thisObject -> connectionName))
          
            listToReturn = addToConnectionList(thisObject, connectionName, listToReturn)
        }
        else if (listToReturn.contains(thisSubject) && (!listToReturn.contains(thisObject)))
        {
            listToReturn(thisSubject) += thisObject -> connectionName
            listToReturn.put(thisObject, HashMap(thisSubject -> connectionName))
          
            listToReturn = addToConnectionList(thisSubject, connectionName, listToReturn)
        }
        else
        {
            listToReturn.put(thisObject, HashMap(thisSubject -> connectionName))
            listToReturn.put(thisSubject, HashMap(thisObject -> connectionName))
        }
        listToReturn
    }
    
    def addToConnectionList(element: String, connectionName: String, listToPopulate: HashMap[String, HashMap[String, String]]): HashMap[String, HashMap[String, String]] =
    {
        // make it mutable
        var listToReturn = listToPopulate
        val connectionsToElement = listToReturn(element)
        for ((conn,name) <- connectionsToElement)
        {
            for ((a,value) <- connectionsToElement)
            {
                if (!listToReturn(conn).contains(a) && conn != a) listToReturn(conn) += a -> connectionName
            }
        }
        listToReturn
    }
    
    /**
     * Sets instantiation and globalUUID variables, and retrieves list of all processes in the order that they should be run. Then runs each process.
     */
    def runAllDrivetrainProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String = UUID.randomUUID().toString.replaceAll("-", ""))
    {
        setGlobalUUID(globalUUID)
        setConnections(gmCxn, cxn)
        
        // get list of all processes in order
        val orderedProcessList: ArrayBuffer[String] = helper.getAllProcessesInOrder(gmCxn)
        
        graphModelValidator.validateProcessesAgainstGraphSpecification(orderedProcessList)
        
        logger.info("Drivetrain will now run the following processes in this order:")
        for (a <- orderedProcessList) logger.info(a)
        
        // run each process
        runProcess(orderedProcessList, dataValidationMode, validateAgainstOntology)
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
