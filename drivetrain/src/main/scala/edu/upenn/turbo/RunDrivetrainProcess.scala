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
            
            // get list of all named graphs which match pattern specified in inputNamedGraph and include match to where clause
            //var inputNamedGraphsList = helper.generateNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph, genericWhereClause)
            // get list of all named graphs which match pattern specified in inputNamedGraph but without match on where clause
            var inputNamedGraphsList = helper.generateSimpleNamedGraphsListFromPrefix(cxn, helper.checkAndConvertPropertiesReferenceToNamedGraph(primaryQuery.defaultInputGraph), useInputNamedGraphsCache)
            logger.info("\tinput named graphs size: " + inputNamedGraphsList.size)
        
            if (inputNamedGraphsList.size == 0) logger.info(s"\tCannot run process $processSpecification: no input named graphs found")
            else
            {
                //run validation on input graph
                if (dataValidationMode == "stop" || dataValidationMode == "log")
                {
                    logger.info(s"\tRunning on Input Data Validation Mode $dataValidationMode")
                    inputDataValidator.validateInputData(inputNamedGraphsList, primaryQuery.inputDataForValidation, dataValidationMode)
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
                val metaInfo: HashMap[Value, ArrayBuffer[String]] = HashMap(METAQUERY -> ArrayBuffer(primaryQuery.getQuery()), 
                                                                DATE -> ArrayBuffer(currDate.toString), 
                                                                PROCESSSPECIFICATION -> ArrayBuffer(processSpecification), 
                                                                PROCESS -> ArrayBuffer(primaryQuery.process),
                                                                OUTPUTNAMEDGRAPH -> ArrayBuffer(primaryQuery.defaultOutputGraph, primaryQuery.defaultRemovalsGraph),
                                                                PROCESSRUNTIME -> ArrayBuffer(runtime),
                                                                TRIPLESADDED -> ArrayBuffer(triplesAdded.toString),
                                                                INPUTNAMEDGRAPHS -> inputNamedGraphsList
                                                                )
                                                                
                addMetaDataTriples(metaInfo, processNamedGraph)
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

    def createPatternMatchQuery(processSpecification: String, process: String = helper.genTurboIRI()) =
    {          
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
        
        val modelInterpreter = new GraphModelInterpreter(gmCxn)
        var (inputRecipeList, outputRecipeList, removalsRecipeList) = modelInterpreter.handleAcornData(inputs, outputs, removals)
        
        // This needs to be re-implemented using the object model
        //graphModelValidator.validateAcornResults(thisProcessSpecification, inputs, outputs, setConnectionLists, mapConnectionLists)
        
        // create primary query
        val primaryQuery = new PatternMatchQuery(gmCxn)
        primaryQuery.setProcessSpecification(thisProcessSpecification)
        primaryQuery.setProcess(process)
        primaryQuery.setInputDataForValidation(inputs)
        
        var inputNamedGraph = inputs(0)(GRAPH.toString).toString
        primaryQuery.setInputGraph(inputNamedGraph)
        
        var outputNamedGraph: String = null
        primaryQuery.createWhereClause(inputRecipeList)
        primaryQuery.createBindClause(inputRecipeList, outputRecipeList, localUUID)
        
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
            primaryQuery.createDeleteClause(removalsRecipeList)
        }
        primaryQuery.createInsertClause(inputRecipeList, outputRecipeList)
        assert(!primaryQuery.getQuery().contains("https://github.com/PennTURBO/Drivetrain/blob/master/turbo_properties.properties/"), "Could not complete properties term replacement")
        //logger.info(primaryQuery.getQuery())
        primaryQuery
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
    
    def addMetaDataTriples(metaInfo: HashMap[Value, ArrayBuffer[String]], processNamedGraph: String)
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
             new TermToLitConnRecipe(new Term(processSpecification), "http://transformunify.org/ontologies/TURBO_0010106", new Literal("\"\"\""+queryVal+"\"\"\"")),
             new TermToLitConnRecipe(new Term(updateProcess), "http://transformunify.org/ontologies/TURBO_0010107", new Literal(runtime)),
             new TermToLitConnRecipe(new Term(updateProcess), "http://transformunify.org/ontologies/TURBO_0010108", new Literal(triplesAdded)),
             new TermToTermConnRecipe(new Term(updateProcess), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", new Term("http://transformunify.org/ontologies/TURBO_0010347")),
             new TermToTermConnRecipe(new Term(updateProcess), "http://purl.obolibrary.org/obo/BFO_0000055", new Term(updatePlanUri)),
             new TermToTermConnRecipe(new Term(updatePlanUri), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", new Term("http://transformunify.org/ontologies/TURBO_0010373")),
             new TermToTermConnRecipe(new Term(updatePlanUri), "http://purl.obolibrary.org/obo/RO_0000059", new Term(processSpecification)),
             new TermToTermConnRecipe(new Term(processSpecification), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", new Term("http://transformunify.org/ontologies/TURBO_0010354")),
             new TermToTermConnRecipe(new Term(processBoundary), "http://purl.obolibrary.org/obo/RO_0002223", new Term(updateProcess)),
             new TermToTermConnRecipe(new Term(processBoundary), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", new Term("http://purl.obolibrary.org/obo/BFO_0000035")),
             new TermToTermConnRecipe(new Term(timeMeasDatum), "http://purl.obolibrary.org/obo/IAO_0000136", new Term(processBoundary)),
             new TermToTermConnRecipe(new Term(timeMeasDatum), "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", new Term("http://purl.obolibrary.org/obo/IAO_0000416"))
        )
        for (inputGraph <- inputNamedGraphsList) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(inputGraph)
            metaTriples += new TermToTermConnRecipe(new Term(updateProcess), "http://transformunify.org/ontologies/TURBO_0010187", new Term(graphForThisRow))
        }
        if ((outputNamedGraph == removalsNamedGraph) || outputNamedGraph != null) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(outputNamedGraph)
            metaTriples += new TermToTermConnRecipe(new Term(updateProcess), "http://transformunify.org/ontologies/TURBO_0010186", new Term(graphForThisRow))
        }
        else if (removalsNamedGraph != null) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(removalsNamedGraph)
            metaTriples += new TermToTermConnRecipe(new Term(updateProcess), "http://transformunify.org/ontologies/TURBO_0010186", new Term(graphForThisRow))
        }
        
        var metaDataQuery = s"INSERT DATA {\n GRAPH <$processNamedGraph> {"
        for (recipe <- metaTriples) 
        {
            if (recipe.subject.isInstanceOf[Term]) recipe.subject.asInstanceOf[Term].isResourceList = Some(false)
            if (recipe.crObject.isInstanceOf[Term]) recipe.crObject.asInstanceOf[Term].isResourceList = Some(false)
            if (recipe.crObject.isInstanceOf[Literal]) recipe.crObject.asInstanceOf[Literal].isResourceList = Some(false)
            recipe.addSparqlSnippet()
            metaDataQuery += recipe.asSparql
        }
        metaDataQuery += "}\n}\n"
        //logger.info(metaDataQuery)
        update.updateSparql(cxn, metaDataQuery)
        
        //Literal class currently does not support literal datatypes other than strings, so making custom query to insert current date with dateTime format
        val dateInsert = s"""INSERT DATA {Graph <$processNamedGraph> { <$timeMeasDatum> obo:IAO_0000004 "$currDate"^^xsd:dateTime .}}"""
        //logger.info(dateInsert)
        update.updateSparql(cxn, dateInsert)
    }
}
