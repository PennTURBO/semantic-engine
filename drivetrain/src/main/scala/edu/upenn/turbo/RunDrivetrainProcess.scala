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
    var globalUUID: String = null
    var instantiation: String = null
    var variableSet = new HashSet[Value]
    var inputSet = new HashSet[Value]
    var inputProcessSet = new HashSet[String]
    var typeMap = new HashMap[String,Value]
    
    def setInstantiation(instantiation: String)
    {
        this.instantiation = instantiation
    }
    def setGlobalUUID(globalUUID: String)
    {
        this.globalUUID = globalUUID
    }
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    def setOutputRepositoryConnection(cxn: RepositoryConnection)
    {
        this.cxn = cxn
    }
        
    def runProcess(process: String)
    {
        val startTime = System.nanoTime()
        val currDate = Calendar.getInstance().getTime()
        logger.info("Starting process: " + process)
        val localUUID = java.util.UUID.randomUUID().toString.replaceAll("-","")
        
        // retrieve connections (inputs, outputs) and expansion rules (binds) from model graph
        // the inputs become the "where" block of the SPARQL query
        // the outputs become the "insert" block
        val inputs = getInputs(process)
        val outputs = getOutputs(process)
        val binds = getBind(process)
        
        if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
        if (outputs.size == 0) throw new RuntimeException("Received a list of 0 outputs")
        
        val inputNamedGraph = inputs(0)(graphFromSparql).toString
        val outputNamedGraph = outputs(0)(graphFromSparql).toString
        
        // process base type is an ontology class which is manipulated in some way by the process
        val processBaseType = inputs(0)(baseType).toString
        // get list of all named graphs which match pattern specified in inputNamedGraph and include processBaseType
        var inputNamedGraphsList = getInputNamedGraphsList(inputNamedGraph, processBaseType)
        logger.info("input named graphs size: " + inputNamedGraphsList.size)
        
        // for each input named graph, create and run query
        var primaryQuery: PatternMatchQuery = null
        for (graph <- inputNamedGraphsList)
        {
            // create primary query
            primaryQuery = new PatternMatchQuery()
            primaryQuery.setProcess(process)
            primaryQuery.setInputGraph(graph)
            primaryQuery.setOutputGraph(outputNamedGraph)
            
            primaryQuery.createBindClause(binds, localUUID)
            primaryQuery.createWhereClause(inputs)
            primaryQuery.createInsertClause(outputs)
            
            logger.info(primaryQuery.getQuery())
            primaryQuery.runQuery(cxn)
        }
        
        val endTime = System.nanoTime()
        val runtime: String = ((endTime - startTime)/1000000000.0).toString
        logger.info("Completed process " + process + " in " + runtime + " seconds")
        
        // create metadata about process
        val metaDataQuery = new DataQuery()
        val metaInfo: HashMap[String, String] = HashMap(metaQuery -> primaryQuery.getQuery(), 
                                                        date -> currDate.toString, 
                                                        processVar -> process, 
                                                        outputNamedGraphVal -> outputNamedGraph,
                                                        processRuntime -> runtime
                                                        )
                                                        
        val metaDataTriples = createMetaDataTriples(metaInfo)
        metaDataQuery.createInsertDataClause(metaDataTriples)
        logger.info(metaDataQuery.getQuery())
        metaDataQuery.runQuery(cxn)
    }
    
    /*def createInsertClause(outputs: ArrayBuffer[HashMap[String, Value]], outputNamedGraph: String, inputNamedGraph: String, process: String): String =
    {
        var insertClause = "INSERT { Graph <" + outputNamedGraph + "> { \n"
        var outputProcessSet = new HashSet[String]
        var typeSet = new HashSet[Value]
        for (triple <- outputs)
        {
            var formattedSubjectVariable = ""
            var formattedObjectVariable = ""
            if (variableSet.contains(triple(subject)) || inputSet.contains(triple(subject))) formattedSubjectVariable = "?" + convertTypeToVariable(triple(subject))
            else formattedSubjectVariable = "<" + triple(subject) + ">"
            if (variableSet.contains(triple(objectVar)) || inputSet.contains(triple(objectVar))) formattedObjectVariable = "?" + convertTypeToVariable(triple(objectVar))
            else formattedObjectVariable = "<" + triple(objectVar) + ">"
            insertClause += formattedSubjectVariable + " <" + triple(predicate).toString + "> " + formattedObjectVariable + " .\n"
            if (triple(subjectType) != null && !typeSet.contains(triple(subject)))
            {
                insertClause += formattedSubjectVariable + " a <" + triple(subject) + "> .\n"
                typeSet += triple(subject)
            }
            if (triple(objectType) != null && !typeSet.contains(triple(objectVar)))
            {
                insertClause += formattedObjectVariable + " a <" + triple(objectVar) + "> .\n"
                typeSet += triple(objectVar)
            }
            if (triple(connectionRecipeType).toString() == "http://transformunify.org/ontologies/ObjectConnectionRecipe")
            {
                outputProcessSet += formattedSubjectVariable
                if (!(triple(predicate).toString == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                {
                    outputProcessSet += formattedObjectVariable
                } 
            }
        }
        insertClause += "}\n"
        insertClause += "Graph pmbb:processes {\n"
        insertClause += s"<$process> turbo:sourcedInputFrom <$inputNamedGraph> ."
        for (a <- inputProcessSet) insertClause += s"<$process> obo:OBI_0000293 $a .\n"
        for (a <- outputProcessSet) insertClause += s"<$process> turbo:createdTripleAbout $a .\n"
        insertClause += "}}\n"
        insertClause
    }*/
    
    def getInputs(process: String): ArrayBuffer[HashMap[String, Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredInputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select $variablesToSelect
         
         Where
         {
            Values ?$connectionRecipeType {turbo:ObjectConnectionRecipe turbo:DatatypeConnectionRecipe}
            ?connection turbo:inputTo <$process> .
            ?connection a ?$connectionRecipeType .
            <$process> turbo:inputNamedGraph ?$graphFromSparql .
            <$process> turbo:manipulatesBaseEntity ?$baseType .
            ?connection turbo:subject ?$sparqlSubject .
            ?connection turbo:predicate ?$sparqlPredicate .
            ?connection turbo:object ?$sparqlObject .
            ?connection turbo:required ?$requiredBool .
            
            Optional
            {
                ?connection obo:BFO_0000050 ?$sparqlOptionalGroup .
            }
            Optional
            {
                ?connection turbo:outputOf ?creatingProcess .
                ?creatingProcess turbo:outputNamedGraph ?$graphOfCreatingProcess .
            }
            
            Graph pmbb:ontology {
              Optional
              {
                  ?$sparqlSubject a owl:Class .
                  BIND (true AS ?$subjectType)
              }
              Optional
              {
                  ?$sparqlObject a owl:Class .
                  BIND (true AS ?$objectType)
              }
         }}
         
         """
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getOutputs(process: String): ArrayBuffer[HashMap[String, Value]] =
    {
       var variablesToSelect = ""
       for (key <- requiredOutputKeysList) variablesToSelect += "?" + key + " "
       
       val query = s"""
         
         Select $variablesToSelect
         Where
         {
            Values ?$connectionRecipeType {turbo:ObjectConnectionRecipe turbo:DatatypeConnectionRecipe}
            ?connection turbo:outputOf <$process> .
            ?connection a ?$connectionRecipeType .
            <$process> turbo:outputNamedGraph ?$graphFromSparql .
            <$process> turbo:manipulatesBaseEntity ?$baseType .
            ?connection turbo:subject ?$sparqlSubject .
            ?connection turbo:predicate ?$sparqlPredicate .
            ?connection turbo:object ?$sparqlObject .
            
            Graph pmbb:ontology 
            {
              Optional
              {
                  ?$sparqlSubject a owl:Class .
                  BIND (true AS ?$subjectType)
              }
              Optional
              {
                  ?$sparqlObject a owl:Class .
                  BIND (true AS ?$objectType)
              }
            }
         }
         
         """
       update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    def getBind(process: String): ArrayBuffer[HashMap[String, Value]] =
    {
        val query = s"""
          
          Select distinct ?$expandedEntity ?$sparqlString ?$shortcutEntity ?$dependee ?$baseType
          Where
          {
    		      values ?manipulationRuleType {turbo:VariableManipulationForIntermediateNode turbo:VariableManipulationForLiteralValue}
              <$process> turbo:usesVariableManipulationRule ?variableManipulationRule .
              <$process> turbo:manipulatesBaseEntity ?$baseType .
              
              ?variableManipulationRule a ?manipulationRuleType .
              ?variableManipulationRule turbo:manipulationCreates ?$expandedEntity .
              ?variableManipulationRule turbo:usesSparqlLogic ?logic .
              ?logic turbo:usesSparql ?$sparqlString .
              
              Optional
              {
                  ?variableManipulationRule turbo:hasOriginalVariable ?$shortcutEntity .
              }
              Optional
              {
                  ?variableManipulationRule turbo:manipulationDependsOn ?$dependee .
              }
          }
          
        """
        
        update.querySparqlAndUnpackToListOfMap(gmCxn, query)
    }
    
    /**
     * Sets instantiation and globalUUID variables, and retrieves list of all processes in the order that they should be run. Then runs each process.
     */
    def runAllDrivetrainProcesses(cxn: RepositoryConnection, gmCxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI())
    {
        setInstantiation(instantiation)
        setGlobalUUID(globalUUID)
        setGraphModelConnection(gmCxn)
        setOutputRepositoryConnection(cxn)
        
        //load the TURBO ontology
        OntologyLoader.addOntologyFromUrl(cxn)
      
        // get list of all processes in order
        val orderedProcessList: ArrayBuffer[String] = getAllProcessesInOrder(gmCxn)
        
        logger.info("Drivetrain will now run the following processes in this order:")
        for (a <- orderedProcessList) logger.info(a)
        
        // run each process
        for (process <- orderedProcessList) runProcess(process)
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
              ?firstProcess a turbo:TurboGraphProcess .
              Minus
              {
                  ?something turbo:precedes ?firstProcess .
              }
          }
        """
        
        val getProcesses: String = """
          select ?precedingProcess ?succeedingProcess where
          {
              ?precedingProcess a turbo:TurboGraphProcess .
              ?succeedingProcess a turbo:TurboGraphProcess .
              ?precedingProcess turbo:precedes ?succeedingProcess .
          }
        """
        
        val firstProcessRes = update.querySparqlAndUnpackTuple(gmCxn, getFirstProcess, "firstProcess")
        if (firstProcessRes.size > 1) throw new RuntimeException ("Multiple starting processes discovered in graph model")
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
    
    /*
     * Get all named graphs in the target repository which contain the base type assigned to the process and match the input named graph provided from the process
     * 
     * @return ArrayBuffer[String] of all input named graphs to run generated query over
     */
    def getInputNamedGraphsList(inputNamedGraph: String, processBaseType: String): ArrayBuffer[String] =
    {
        // In the model graph, an input named graph ending in '_' indicates a wildcard
        if (inputNamedGraph.charAt(inputNamedGraph.size-1) == '_') 
        {
            helper.generateNamedGraphsListFromPrefix(cxn, inputNamedGraph, processBaseType)
        }
        else ArrayBuffer(inputNamedGraph)
    }
    
    def createMetaDataTriples(metaInfo: HashMap[String, String]): ArrayBuffer[Triple] =
    {
        val processVal = metaInfo(processVar)
        val currDate = metaInfo(date)
        val queryVal = metaInfo(metaQuery)
        val outputNamedGraph = metaInfo(outputNamedGraphVal)
        val runtime = metaInfo(processRuntime)
        helper.validateURI(processNamedGraph)
        var metaTriples = ArrayBuffer(
             new Triple(processVal, "rdfs:comment", queryVal, false, false, processNamedGraph),
             new Triple(processVal, "turbo:hasDate", currDate, false, false, processNamedGraph),
             new Triple(processVal, "rdf:type", "turbo:TurboGraphProcess", false, false, processNamedGraph),
             new Triple(processVal, "turbo:addedTriplesTo", outputNamedGraph, false, false, processNamedGraph),
             new Triple(processVal, "turbo:completionTimeInSeconds", runtime, false, false, processNamedGraph)
        )
        metaTriples
    }
}