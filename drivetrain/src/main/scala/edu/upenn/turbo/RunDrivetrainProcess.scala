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
            var inputNamedGraphsList = helper.generateSimpleNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph, useInputNamedGraphsCache)
            logger.info("\tinput named graphs size: " + inputNamedGraphsList.size)
            if (inputNamedGraphsList.size == 0) logger.info(s"\tCannot run process $processSpecification: no input named graphs found")
            else
            {
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

    def createPatternMatchQuery(processSpecification: String, process: String = helper.genTurboIRI()): PatternMatchQuery =
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
        
        var (inputRecipeList, outputRecipeList, removalsRecipeList) = handleAcornData(inputs, outputs, removals)
        
        // This needs to be re-implemented using the object model
        //graphModelValidator.validateAcornResults(thisProcessSpecification, inputs, outputs, setConnectionLists, mapConnectionLists)
        //run validation on input graph
        if (dataValidationMode == "stop" || dataValidationMode == "log")
        {
            logger.info(s"\tRunning on Input Data Validation Mode $dataValidationMode")
            // This needs to be re-worked to use connection recipes
            //inputDataValidator.validateInputData(inputNamedGraphsList, primaryQuery.rawInputData, dataValidationMode)
        }
        else logger.info("\tInput Data Validation turned off for this instantiation")
        
        // create primary query
        val primaryQuery = new PatternMatchQuery(gmCxn)
        primaryQuery.setProcessSpecification(thisProcessSpecification)
        primaryQuery.setProcess(process)
        
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
        primaryQuery.createInsertClause(outputRecipeList)
        assert(!primaryQuery.getQuery().contains("https://github.com/PennTURBO/Drivetrain/blob/master/turbo_properties.properties/"), "Could not complete properties term replacement")
        primaryQuery 
    }
    
    def handleAcornData(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]) =
    {
        var discoveredInstances = new HashMap[String, Instance]
        var discoveredTerms = new HashMap[String, Term]
        var discoveredLiterals = new HashMap[String, Literal]
        
        val (inputRecipeList, inpDisInst, inpDisTerm, inpDisLit) = processAcornRowResults("input", inputs, discoveredInstances, discoveredTerms, discoveredLiterals)
        discoveredInstances = inpDisInst; discoveredTerms = inpDisTerm; discoveredLiterals = inpDisLit
        val (outputRecipeList, outDisInst, outDisTerm, outDisLit) = processAcornRowResults("output", outputs, discoveredInstances, discoveredTerms, discoveredLiterals)
        discoveredInstances = outDisInst; discoveredTerms = outDisTerm; discoveredLiterals = outDisLit
        val (removalsRecipeList, remDisInst, remDisTerm, remDisLit) = processAcornRowResults("removal", removals, discoveredInstances, discoveredTerms, discoveredLiterals)
        (inputRecipeList, outputRecipeList, removalsRecipeList)
    }
    
    def processAcornRowResults(typeOfData: String, data: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], disInst: HashMap[String, Instance], disTerm: HashMap[String, Term], disLit: HashMap[String, Literal]) =
    {        
        val recipesList = new HashSet[ConnectionRecipe]
        for (row <- data)
        {
            val (subjectString, objectString, predicate, connectionName, thisMultiplicity, recipeType, optional, subjectExplicit, 
                objectExplicit, subjectIsSingleton, subjectIsSuper, objectIsSingleton, objectIsSuper, graphForThisRow, suffixOperator,
                minusGroup, optionalGroup, subjectCustomRule, objectCustomRule, subjectDependee, objectDependee) 
                = interpretRowData(row)
                
            var subjectWithContext = subjectString
            var objectWithContext = objectString
            if (row(SUBJECTCONTEXT.toString) != null) subjectWithContext += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectWithContext += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            if (recipeType == instToInstRecipe)
            {
                val subjInst = findOrCreateNewInstance(typeOfData, disInst, subjectString, subjectWithContext, subjectExplicit, subjectIsSingleton, subjectIsSuper, subjectCustomRule)
                disInst += subjInst.value -> subjInst
                val obInst = findOrCreateNewInstance(typeOfData, disInst, objectString, objectWithContext, objectExplicit, objectIsSingleton, objectIsSuper, objectCustomRule)
                disInst += obInst.value -> obInst
                
                // right now only updating connection lists for instance-to-instance relations
                //it would be possible to enforce cardinality based on a LiteralResourceList or maybe even a ClassResourceList...but leaving that alone for now
                updateConnectionLists(subjInst, obInst, thisMultiplicity)
                                                
                val recipe = new InstToInstConnRecipe(subjInst, predicate, obInst)
                subjInst.referencedByRecipes += recipe
                obInst.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjInst, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, obInst, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == instToTermRecipe)
            {
                val subjInst = findOrCreateNewInstance(typeOfData, disInst, subjectString, subjectWithContext, subjectExplicit, subjectIsSingleton, subjectIsSuper, subjectCustomRule)
                disInst += subjInst.value -> subjInst
                val objTerm = findOrCreateNewTerm(typeOfData, disTerm, objectWithContext, objectExplicit, objectCustomRule)
                disTerm += objTerm.value -> objTerm
                
                val recipe = new InstToTermConnRecipe(subjInst, predicate, objTerm)
                subjInst.referencedByRecipes += recipe
                objTerm.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjInst, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objTerm, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == termToInstRecipe)
            {
                val subjTerm = findOrCreateNewTerm(typeOfData, disTerm, subjectWithContext, subjectExplicit, subjectCustomRule)
                disTerm += subjTerm.value -> subjTerm
                val objInst = findOrCreateNewInstance(typeOfData, disInst, objectString, objectWithContext, objectExplicit, objectIsSingleton, objectIsSuper, objectCustomRule)
                disInst += objInst.value -> objInst
                
                val recipe = new TermToInstConnRecipe(subjTerm, predicate, objInst)
                subjTerm.referencedByRecipes += recipe
                objInst.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjTerm, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objInst, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == instToLiteralRecipe)
            {
                val subjInst = findOrCreateNewInstance(typeOfData, disInst, subjectString, subjectWithContext, subjectExplicit, subjectIsSingleton, subjectIsSuper, subjectCustomRule)
                disInst += subjInst.value -> subjInst
                val objLit = findOrCreateNewLiteral(typeOfData, disLit, objectWithContext, objectExplicit, objectCustomRule)
                disLit += objLit.value -> objLit
                
                val recipe = new InstToLitConnRecipe(subjInst, predicate, objLit)
                subjInst.referencedByRecipes += recipe
                objLit.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjInst, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objLit, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == termToLiteralRecipe)
            {
                val subjTerm = findOrCreateNewTerm(typeOfData, disTerm, subjectWithContext, subjectExplicit, subjectCustomRule)
                disTerm += subjTerm.value -> subjTerm
                val objLit = findOrCreateNewLiteral(typeOfData, disLit, objectWithContext, objectExplicit, objectCustomRule)
                disLit += objLit.value -> objLit
                
                val recipe = new TermToLitConnRecipe(subjTerm, predicate, objLit)
                subjTerm.referencedByRecipes += recipe
                objLit.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjTerm, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objLit, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else if (recipeType == termToTermRecipe)
            {
                val subjTerm = findOrCreateNewTerm(typeOfData, disTerm, subjectWithContext, subjectExplicit, subjectCustomRule)
                disTerm += subjTerm.value -> subjTerm
                val objTerm = findOrCreateNewTerm(typeOfData, disTerm, objectWithContext, objectExplicit, objectCustomRule)
                disTerm += objTerm.value -> objTerm
                
                val recipe = new TermToTermConnRecipe(subjTerm, predicate, objTerm)
                subjTerm.referencedByRecipes += recipe
                objTerm.referencedByRecipes += recipe
                if (subjectDependee != null) addDependent(subjectDependee, subjTerm, disInst, disTerm, disLit)
                if (objectDependee != null) addDependent(objectDependee, objTerm, disInst, disTerm, disLit)
                updateRecipeWithNonTypeData(recipe, connectionName, thisMultiplicity, predicate, optional, graphForThisRow, suffixOperator, minusGroup, optionalGroup)
                recipe.addSparqlSnippet()
                recipesList += recipe
            }
            else throw new RuntimeException(s"Unrecognized input cardinality setting: $thisMultiplicity")
        }
        (recipesList, disInst, disTerm, disLit)
    }
    
    def addDependent(dependee: String, element: GraphPatternElement, disInst: HashMap[String,Instance], disTerm: HashMap[String,Term], disLit:HashMap[String,Literal])
    {
        var dependeeElement: GraphPatternElement = null
        if (disInst.contains(dependee)) dependeeElement = disInst(dependee)
        if (disTerm.contains(dependee)) dependeeElement = disTerm(dependee)
        if (disLit.contains(dependee)) dependeeElement = disLit(dependee)
        if (dependeeElement == null) 
        {
            val dependentElement = element.value
            throw new RuntimeException("Element $dependentElement was declared dependent on $dependee, but $dependee was not found as an input")
        }
        element.dependentOn = Some(dependeeElement)
    }
    
    def updateRecipeWithNonTypeData(recipe: ConnectionRecipe, connectionName: String, thisMultiplicity: String, predicate: String, optional: Boolean, 
        graphForThisRow: String, suffixOperator: String, minusGroup: String, optionalGroup: String)
    {
        recipe.name = connectionName
        recipe.cardinality = thisMultiplicity
        recipe.isOptional = Some(optional)
        if (graphForThisRow != null) recipe.foundInGraph = Some(graphForThisRow)
        if (suffixOperator != null) recipe.predicateSuffixOperator = Some(suffixOperator)
        if (minusGroup != null) recipe.minusGroup = Some(minusGroup)
        if (optionalGroup != null) recipe.optionalGroup = Some(optionalGroup)
    }
    
    def updateConnectionLists(subj: Instance, obj: Instance, cardinality: String)
    {
        if (cardinality == oneToOneMultiplicity)
        {
            val subjList = subj.oneToOneConnections
            val objList = obj.oneToOneConnections
            
            subjList += obj
            objList += subj
            
            for (instance <- subjList)
            {
                if (obj != instance && subj != instance) 
                {
                    instance.oneToOneConnections += obj   
                    obj.oneToOneConnections += instance
                }
            }
            for (instance <- objList) 
            {
                if (subj != instance && obj != instance) 
                {
                    instance.oneToOneConnections += subj
                    subj.oneToOneConnections += instance
                }
            }
            
            //for (element <- subjList) logger.info(subj.value + " is connected with " + element.value)
            //for (element <- objList) logger.info(obj.value + " is connected with " + element.value)
        }
        else if (cardinality == oneToManyMultiplicity)
        {
            val subjList = subj.oneToManyConnections
            val objList = subj.oneToManyConnections
            
            subjList += obj
            objList += subj
        }
        else if (cardinality == manyToOneMultiplicity)
        {
            val subjList = subj.manyToOneConnections
            val objList = subj.manyToOneConnections
            
            subjList += obj
            objList += subj
        }
    }
    
    def interpretRowData(row: HashMap[String, org.eclipse.rdf4j.model.Value]) =
    {
        var subjectString = row(SUBJECT.toString).toString
        var objectString = row(OBJECT.toString).toString
        
        val predicate = row(PREDICATE.toString).toString
        
        var suffixOperator: String = null
        if (row.contains(SUFFIXOPERATOR.toString) && row(SUFFIXOPERATOR.toString) != null) suffixOperator = row(SUFFIXOPERATOR.toString).toString
        
        val connectionName = row(CONNECTIONNAME.toString).toString
        val thisMultiplicity = row(MULTIPLICITY.toString).toString
        val recipeType = row(CONNECTIONRECIPETYPE.toString).toString
        
        var optional: Boolean = false
        if (row.contains(INPUTTYPE.toString) && row(INPUTTYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/hasOptionalInput") optional = true
        
        var subjectExplicit = true
        if (row(SUBJECTADESCRIBER.toString) != null) subjectExplicit = false
        var objectExplicit = true
        if (row(OBJECTADESCRIBER.toString) != null) objectExplicit = false
        if (row(OBJECTALITERALVALUE.toString) != null) objectExplicit = true
        
        // SPARQL searches process that created this input as an output and returns their graph (GRAPHOFCREATINGPROCESS). We override that if the user provided a graph explicitly (GRAPHOFORIGIN)
        var graphForThisRow: String = null
        if (row.contains(GRAPHOFCREATINGPROCESS.toString) && row(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = row(GRAPHOFCREATINGPROCESS.toString).toString
        if (row.contains(GRAPHOFORIGIN.toString) && row(GRAPHOFORIGIN.toString) != null) graphForThisRow = row(GRAPHOFORIGIN.toString).toString
        
        var subjectIsSingleton = false
        var subjectIsSuper = false
        var objectIsSingleton = false
        var objectIsSuper = false
        if (subjectSingleton.contains(thisMultiplicity)) subjectIsSingleton = true
        else if (subjectSuperSingleton.contains(thisMultiplicity)) subjectIsSuper = true
        if (objectSingleton.contains(thisMultiplicity)) objectIsSingleton = true
        else if (objectSuperSingleton.contains(thisMultiplicity)) objectIsSuper = true
        
        var minusGroup: String = null
        var optionalGroup: String = null
        if (row.contains(MINUSGROUP.toString) && row(MINUSGROUP.toString) != null) minusGroup = row(MINUSGROUP.toString).toString
        if (row.contains(OPTIONALGROUP.toString) && row(OPTIONALGROUP.toString) != null) optionalGroup = row(OPTIONALGROUP.toString).toString
        
        var subjectDependee: String = null
        var objectDependee: String = null
        var subjectCustomRule: String = null
        var objectCustomRule: String = null
        if (row.contains(SUBJECTRULE.toString) && row(SUBJECTRULE.toString) != null) subjectCustomRule = row(SUBJECTRULE.toString).toString
        if (row.contains(OBJECTRULE.toString) && row(OBJECTRULE.toString) != null) objectCustomRule = row(OBJECTRULE.toString).toString
        if (row.contains(SUBJECTDEPENDEE.toString) && row(SUBJECTDEPENDEE.toString) != null) subjectDependee = row(SUBJECTDEPENDEE.toString).toString
        if (row.contains(OBJECTDEPENDEE.toString) && row(OBJECTDEPENDEE.toString) != null) objectDependee = row(OBJECTDEPENDEE.toString).toString
        
        // null check
        val nonNulls = Array(subjectString, objectString, predicate, connectionName, thisMultiplicity, recipeType, optional, subjectExplicit,
            objectExplicit, subjectExplicit, objectExplicit, subjectIsSingleton, subjectIsSuper, objectIsSingleton, objectIsSuper)
        for (nonNull <- nonNulls) assert(nonNull != null, "Found null object")
        
        (subjectString, objectString, predicate, connectionName, thisMultiplicity, recipeType, optional, subjectExplicit, 
            objectExplicit, subjectIsSingleton, subjectIsSuper, objectIsSingleton, objectIsSuper, graphForThisRow, suffixOperator,
            minusGroup, optionalGroup, subjectCustomRule, objectCustomRule, subjectDependee, objectDependee)
    }
    
    def findOrCreateNewInstance(typeOfData: String, disInst: HashMap[String, Instance], stringVal: String, valWithContext: String, explicit: Boolean, singleton: Boolean, superSingleton: Boolean, customRule: String): Instance =
    {
        var newInst: Instance = null
        if (disInst.contains(valWithContext)) 
        {
            // might be a good place to validate the discovered element with the informations gathered about it that are parameters to this method
            newInst = disInst(valWithContext)
        }
        else
        {
          newInst = new Instance(valWithContext)
          newInst.isUntyped = Some(!explicit)
          newInst.isSingleton = Some(singleton)
          newInst.isSuperSingleton = Some(superSingleton)
          newInst.instanceType = stringVal
          newInst.buildInstanceType(newInst)
          if (customRule != null) newInst.createdWithRule = Some(customRule)
        }
        if (typeOfData == "input") newInst.existsInInput = Some(true)
        else if (newInst.existsInInput == None) newInst.existsInInput = Some(false)
        if (typeOfData == "output") newInst.existsInOutput = Some(true)
        else if (newInst.existsInOutput == None) newInst.existsInOutput = Some(false)
        newInst
    }
    
    def findOrCreateNewTerm(typeOfData: String, disTerm: HashMap[String, Term], stringVal: String, explicit: Boolean, customRule: String): Term =
    {
        var newTerm: Term = null
        if (disTerm.contains(stringVal)) 
        {
            // might be a good place to validate the discovered element with the informations gathered about it that are parameters to this method
            newTerm = disTerm(stringVal)
        }
        else
        {
          newTerm = new Term(stringVal)
          newTerm.isResourceList = Some(!explicit)
          if (!explicit) newTerm.ranges = Some(helper.getDescriberRangesAsList(gmCxn, stringVal))
          if (customRule != null) newTerm.createdWithRule = Some(customRule)
        }
        if (typeOfData == "input") newTerm.existsInInput = Some(true)
        else if (newTerm.existsInInput == None) newTerm.existsInInput = Some(false)
        if (typeOfData == "output") newTerm.existsInOutput = Some(true)
        else if (newTerm.existsInOutput == None) newTerm.existsInOutput = Some(false)
        newTerm
    }
    
    def findOrCreateNewLiteral(typeOfData: String, disLit: HashMap[String, Literal], stringVal: String, explicit: Boolean, customRule: String): Literal =
    {
        var newLit: Literal = null
        if (disLit.contains(stringVal)) 
        {
            // might be a good place to validate the discovered element with the informations gathered about it that are parameters to this method
            newLit = disLit(stringVal)
        }
        else
        {
          newLit = new Literal(stringVal)
          newLit.isResourceList = Some(!explicit)
          if (customRule != null) newLit.createdWithRule = Some(customRule)
        }
        if (typeOfData == "input") newLit.existsInInput = Some(true)
        else if (newLit.existsInInput == None) newLit.existsInInput = Some(false)
        if (typeOfData == "output") newLit.existsInOutput = Some(true)
        else if (newLit.existsInOutput == None) newLit.existsInOutput = Some(false)
        newLit
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
             new TermToLitConnRecipe(new Term(processSpecification), "turbo:TURBO_0010106", new Literal(queryVal)),
             new TermToLitConnRecipe(new Term(updateProcess), "turbo:TURBO_0010107", new Literal(runtime)),
             new TermToLitConnRecipe(new Term(updateProcess), "turbo:TURBO_0010108", new Literal(triplesAdded)),
             new TermToTermConnRecipe(new Term(updateProcess), "rdf:type", new Term("turbo:TURBO_0010347")),
             new TermToTermConnRecipe(new Term(updateProcess), "obo:BFO_0000055", new Term(updatePlanUri)),
             new TermToTermConnRecipe(new Term(updatePlanUri), "rdf:type", new Term("turbo:TURBO_0010373")),
             new TermToTermConnRecipe(new Term(updatePlanUri), "obo:RO_0000059", new Term(processSpecification)),
             new TermToTermConnRecipe(new Term(processSpecification), "rdf:type", new Term("turbo:TURBO_0010354")),
             new TermToTermConnRecipe(new Term(processBoundary), "obo:RO_0002223", new Term(updateProcess)),
             new TermToTermConnRecipe(new Term(processBoundary), "rdf:type", new Term("obo:BFO_0000035")),
             new TermToTermConnRecipe(new Term(timeMeasDatum), "obo:IAO_0000136", new Term(processBoundary)),
             new TermToTermConnRecipe(new Term(timeMeasDatum), "rdf:type", new Term("obo:IAO_0000416"))
        )
        for (inputGraph <- inputNamedGraphsList) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(inputGraph)
            metaTriples += new TermToTermConnRecipe(new Term(updateProcess), "turbo:TURBO_0010187", new Term(graphForThisRow))
        }
        if ((outputNamedGraph == removalsNamedGraph) || outputNamedGraph != null) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(outputNamedGraph)
            metaTriples += new TermToTermConnRecipe(new Term(updateProcess), "turbo:TURBO_0010186", new Term(graphForThisRow))
        }
        else if (removalsNamedGraph != null) 
        {
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(removalsNamedGraph)
            metaTriples += new TermToTermConnRecipe(new Term(updateProcess), "turbo:TURBO_0010186", new Term(graphForThisRow))
        }
        
        var metaDataQuery = s"INSERT DATA {\n GRAPH {$processNamedGraph "
        for (recipe <- metaTriples) 
        {
            recipe.addSparqlSnippet()
            metaDataQuery += recipe.asSparql
        }
        metaDataQuery += "}\n}\n"
        update.updateSparql(cxn, metaDataQuery)
        
        //Literal class currently does not support literal datatypes other than strings, so making custom query to insert current date with dateTime format
        val dateInsert = s"""INSERT DATA {Graph <$processNamedGraph> { <$timeMeasDatum> obo:IAO_0000004 "$currDate"^^xsd:dateTime .}}"""
        //logger.info(dateInsert)
        update.updateSparql(cxn, dateInsert)
    }
}
