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

    def validateProcessSpecification(process: String): Boolean =
    {
       val ask: String = s"""
          ASK {
            <$process> a turbo:TURBO_0010354 .
          }
          """
        update.querySparqlBoolean(gmCxn, ask).get
    }
    
    def runProcess(processSpecification: String, dataValidationMode: String = dataValidationMode): HashMap[String, PatternMatchQuery] =
    {
        runProcess(ArrayBuffer(processSpecification), dataValidationMode)
    }
        
    def runProcess(processSpecifications: ArrayBuffer[String], dataValidationMode: String): HashMap[String, PatternMatchQuery] =
    {
        var processQueryMap = new HashMap[String, PatternMatchQuery]
        for (processSpecification <- processSpecifications)
        {
            val updateProcess = helper.genTurboIRI()
            if (!validateProcessSpecification(processSpecification)) logger.info(processSpecification + " is not a valid TURBO process")
            else processQueryMap += processSpecification -> createPatternMatchQuery(processSpecification, updateProcess)
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
            var inputNamedGraphsList = helper.generateSimpleNamedGraphsListFromPrefix(cxn, primaryQuery.defaultInputGraph)
            logger.info("input named graphs size: " + inputNamedGraphsList.size)
            if (inputNamedGraphsList.size == 0) logger.info(s"Cannot run process $processSpecification: no input named graphs found")
            else
            {
                //run validation on input graph
                if (dataValidationMode == "stop" || dataValidationMode == "log")
                {
                    logger.info(s"Running on Input Data Validation Mode $dataValidationMode")
                    InputDataValidator.setGraphModelConnection(gmCxn)
                    InputDataValidator.setOutputRepositoryConnection(cxn)
                    InputDataValidator.validateInputData(inputNamedGraphsList, primaryQuery.rawInputData, dataValidationMode)
                }
                else logger.info("Input Data Validation turned off for this instantiation")
                // for each input named graph, run query with specified named graph
                for (graph <- inputNamedGraphsList)
                {
                    logger.info("Now running on input graph " + graph)
                    primaryQuery.whereClause = genericWhereClause.replaceAll(primaryQuery.defaultInputGraph, graph)
                    //logger.info(primaryQuery.getQuery())
                    primaryQuery.runQuery(cxn)
                }
                // set back to generic input named graph for storing in metadata
                primaryQuery.whereClause = genericWhereClause
                
                val endingTriplesCount = helper.countTriplesInDatabase(cxn)
                val triplesAdded = endingTriplesCount - startingTriplesCount
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
            }
        }
        processQueryMap
    }

    def createPatternMatchQuery(processSpecification: String, process: String = helper.genTurboIRI()): PatternMatchQuery =
    {
        assert (localUUID != null, "You must set the globalUUID before running any process.")
        var thisProcessSpecification = helper.getProcessNameAsUri(processSpecification)
        if (!validateProcessSpecification(thisProcessSpecification)) 
        {
            logger.info(thisProcessSpecification + " is not a valid TURBO process")
            return null
        }
        else
        {
            validateConnectionRecipesInProcess(thisProcessSpecification)
            
            // retrieve connections (inputs, outputs) from model graph
            // the inputs become the "where" block of the SPARQL query
            // the outputs become the "insert" block
            val inputs = getInputs(thisProcessSpecification)
            val outputs = getOutputs(thisProcessSpecification)
            val removals = getRemovals(thisProcessSpecification)
            
            if (inputs.size == 0) throw new RuntimeException("Received a list of 0 inputs")
            if (outputs.size == 0 && removals.size == 0) throw new RuntimeException("Did not receive any outputs or removals")
            
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
            assert(!primaryQuery.getQuery().contains("http://turboProperties.org/"), "Could not complete properties term replacement")
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
              Values ?$CONNECTIONRECIPETYPE {drivetrain:ObjectConnectionToTermRecipe 
                                            drivetrain:ObjectConnectionToInstanceRecipe
                                            drivetrain:DatatypeConnectionRecipe
                                            drivetrain:ObjectConnectionFromTermRecipe}
              Values ?$INPUTTYPE {drivetrain:hasRequiredInput drivetrain:hasOptionalInput}
              <$process> ?$INPUTTYPE ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
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
                  ?$CONNECTIONNAME obo:BFO_0000050 ?$OPTIONALGROUP .
                  ?$OPTIONALGROUP a drivetrain:TurboGraphOptionalGroup .
                  <$process> drivetrain:buildsOptionalGroup ?$OPTIONALGROUP .
              }
              Optional
              {
                  ?$CONNECTIONNAME obo:BFO_0000050 ?$MINUSGROUP .
                  ?$MINUSGROUP a drivetrain:TurboGraphMinusGroup .
                  <$process> drivetrain:buildsMinusGroup ?$MINUSGROUP .
              }
              Optional
              {
                  ?creatingProcess drivetrain:hasOutput ?$CONNECTIONNAME .
                  ?creatingProcess drivetrain:outputNamedGraph ?$GRAPHOFCREATINGPROCESS .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:referencedInGraph ?$GRAPHOFORIGIN .
              }
              Optional
              {
                  ?$OBJECT a drivetrain:MultiObjectDescriber .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:MultiObjectDescriber .
                  BIND (true AS ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:ObjectConnectionFromTermRecipe)
                  BIND (true AS ?$SUBJECTTYPE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:ObjectConnectionToTermRecipe)
                  BIND (true AS ?$OBJECTTYPE)
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:mustExistIf ?$REQUIREMENT .
              }
              Optional
              {
                  ?$CONNECTIONNAME drivetrain:predicateSuffix ?suffix .
                  ?suffix a drivetrain:PredicateSuffixSymbol .
                  ?suffix drivetrain:usesSparqlOperator ?$SUFFIXOPERATOR .
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERAL)
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
              Values ?CONNECTIONRECIPETYPE {drivetrain:ObjectConnectionToTermRecipe 
                                          drivetrain:ObjectConnectionToInstanceRecipe
                                          drivetrain:DatatypeConnectionRecipe
                                          drivetrain:ObjectConnectionFromTermRecipe}
  
              <$process> drivetrain:removes ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
              <$process> drivetrain:inputNamedGraph ?$GRAPH .
              ?$CONNECTIONNAME drivetrain:subject ?$SUBJECT .
              ?$CONNECTIONNAME drivetrain:predicate ?$PREDICATE .
              ?$CONNECTIONNAME drivetrain:object ?$OBJECT .
              Optional
              {
                  ?$OBJECT a drivetrain:MultiObjectDescriber .
                  BIND (true AS ?$OBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a drivetrain:MultiObjectDescriber .
                  BIND (true AS ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$SUBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:ObjectConnectionFromTermRecipe)
                  BIND (true AS ?$SUBJECTTYPE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:ObjectConnectionToTermRecipe)
                  BIND (true AS ?$OBJECTTYPE)
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERAL)
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
              Values ?INPUTTO {drivetrain:hasRequiredInput drivetrain:hasOptionalInput}
              Values ?CONNECTIONRECIPETYPE {drivetrain:ObjectConnectionToTermRecipe 
                                            drivetrain:ObjectConnectionToInstanceRecipe
                                            drivetrain:DatatypeConnectionRecipe
                                            drivetrain:ObjectConnectionFromTermRecipe}
              <$process> drivetrain:hasOutput ?$CONNECTIONNAME .
              ?$CONNECTIONNAME a ?$CONNECTIONRECIPETYPE .
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
                  ?$SUBJECT a drivetrain:MultiObjectDescriber .
                  BIND (true as ?$SUBJECTADESCRIBER)
              }
              Optional
              {
                  ?$OBJECT a drivetrain:MultiObjectDescriber .
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
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:ObjectConnectionFromTermRecipe)
                  BIND (true AS ?$SUBJECTTYPE)
              }
              Optional
              {
                  ?$OBJECT a owl:Class .
                  filter (?$CONNECTIONRECIPETYPE != drivetrain:ObjectConnectionToTermRecipe)
                  BIND (true AS ?$OBJECTTYPE)
              }
              BIND (isLiteral(?$OBJECT) as ?$OBJECTALITERAL)
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
        runProcess(orderedProcessList, dataValidationMode)
    }

    def validateGraphModelTerms()
    {
        val checkPredicates: String = s"""
          Select distinct ?predicate Where
          {
              Values ?g {<$defaultPrefix"""+s"""instructionSet> <$defaultPrefix"""+"""graphSpecification>}
              Graph ?g
              {
                  ?subject ?predicate ?object .
                  Filter (?predicate NOT IN (
                      drivetrain:subject,
                      drivetrain:predicate,
                      drivetrain:object,
                      rdf:type,
                      drivetrain:usesCustomVariableManipulationRule,
                      drivetrain:usesSparql,
                      obo:BFO_0000050,
                      drivetrain:referencedInGraph,
                      drivetrain:mustExistIf,
                      drivetrain:multiplicity,
                      drivetrain:inputNamedGraph,
                      drivetrain:outputNamedGraph,
                      drivetrain:hasOutput,
                      drivetrain:hasRequiredInput,
                      drivetrain:hasOptionalInput,
                      drivetrain:removes,
                      rdfs:label,
                      drivetrain:buildsOptionalGroup,
                      drivetrain:buildsMinusGroup,
                      drivetrain:precedes,
                      drivetrain:subjectRequiredToCreate,
                      drivetrain:objectRequiredToCreate,
                      drivetrain:subjectUsesContext,
                      drivetrain:objectUsesContext,
                      drivetrain:hasPossibleContext,
                      drivetrain:range,
                      owl:versionInfo,
                      owl:imports,
                      rdfs:subClassOf,
                      rdfs:domain,
                      rdfs:range,
                      drivetrain:usesSparqlOperator,
                      drivetrain:predicateSuffix
                  ))
              }
          }
        """
        //println(checkPredicates)
        var firstRes = ""
        var res = update.querySparqlAndUnpackTuple(gmCxn, checkPredicates, "predicate")
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Error in graph model: predicate $firstRes is not known in the Acorn language")
    
        val checkTypes: String = s"""
          Select distinct ?type Where
          {
              Values ?g {<$defaultPrefix"""+s"""instructionSet> <$defaultPrefix"""+"""graphSpecification>}
              Graph ?g
              {
                  ?subject a ?type .
                  Filter (?type NOT IN (
                      drivetrain:ObjectConnectionToTermRecipe,
                      drivetrain:ObjectConnectionToInstanceRecipe,
                      drivetrain:ObjectConnectionFromTermRecipe,
                      drivetrain:DatatypeConnectionRecipe,
                      drivetrain:MultiObjectDescriber,
                      owl:Class,
                      drivetrain:TurboGraphContext,
                      drivetrain:TurboGraphMinusGroup,
                      drivetrain:TurboGraphOptionalGroup,
                      drivetrain:TurboGraphVariableManipulationLogic,
                      drivetrain:TurboNamedGraph,
                      owl:Ontology,
                      turbo:TURBO_0010354,
                      owl:ObjectProperty,
                      owl:DatatypeProperty,
                      drivetrain:TurboGraphStringLiteralValue,
                      drivetrain:TurboGraphDateLiteralValue,
                      drivetrain:TurboGraphMultiplicityRule,
                      drivetrain:TurboGraphDoubleLiteralValue,
                      drivetrain:TurboGraphIntegerLiteralValue,
                      drivetrain:TurboGraphBooleanLiteralValue,
                      drivetrain:TurboGraphRequirementSpecification,
                      drivetrain:PredicateSuffixSymbol
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
        
        // first collect list of all subjects and objects used in queued processes
        val getSubjectAndObjectOutputs = s"""
          Select distinct ?class Where
          {
              {
                  Graph <$defaultPrefix"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?connection .
                  }
                  Graph <$defaultPrefix"""+s"""graphSpecification>
                  {
                      ?connection drivetrain:subject ?class .
                      Minus
                      {
                          ?connection a drivetrain:ObjectConnectionFromTermRecipe ;
                      }
                  }
                  ?class a owl:Class .
                  filter (?process IN ($processListAsString))
              }
              UNION
              {
                  Graph <$defaultPrefix"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?connection .
                  }
                  Graph <$defaultPrefix"""+s"""graphSpecification>
                  {
                      ?connection drivetrain:object ?class .
                      Minus
                      {
                          ?connection a drivetrain:ObjectConnectionToTermRecipe ;
                      }
                  }
                  ?class a owl:Class .
                  filter (?process IN ($processListAsString))
              }
          }
          """
        //logger.info(getSubjectAndObjectOutputs)
        val allClasses = update.querySparqlAndUnpackTuple(gmCxn, getSubjectAndObjectOutputs, "class")
        
        for (singleClass <- allClasses)
        {
            val findRequiredButUncreatedRecipes = s"""
              Select ?recipe Where
              {
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject <$singleClass> .
                          ?recipe drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists .
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject <$singleClass> .
                          ?recipe drivetrain:mustExistIf drivetrain:subjectExists .
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object <$singleClass> .
                          ?recipe drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists .
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object <$singleClass> .
                          ?recipe drivetrain:mustExistIf drivetrain:objectExists .
                      }
                  }
                  MINUS
                  {
                      Graph <$defaultPrefix"""+s"""instructionSet>
                      {
                          ?process drivetrain:hasOutput ?recipe .
                          filter (?process IN ($processListAsString))
                      }
                  }
              }
            """
            //logger.info(findRequiredButUncreatedRecipes)      
            var firstRes = ""
            val res = update.querySparqlAndUnpackTuple(gmCxn, findRequiredButUncreatedRecipes, "recipe")
            if (res.size > 0) firstRes = res(0)
            assert(firstRes == "", s"Error in graph model: connection recipe $firstRes in the Graph Specification is required due to the existence of $singleClass but is not the output of a queued process in the Instruction Set")
        }
        
        var filterMultipleProcesses = ""
        if (processList.size > 1)
        {
             filterMultipleProcesses = s"""
                Filter Not Exists
                {
                    ?someOtherProcess drivetrain:removes ?recipe .
                }
                filter (?process != ?someOtherProcess)
                filter (?someOtherProcess IN ($processListAsString))
              """
        }
        
        val getOutputsOfAllProcesses = s"""
          Select ?recipe Where
          {
              Graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  Values ?CONNECTIONRECIPETYPE {drivetrain:ObjectConnectionToTermRecipe 
                                            drivetrain:ObjectConnectionToInstanceRecipe
                                            drivetrain:DatatypeConnectionRecipe
                                            drivetrain:ObjectConnectionFromTermRecipe}
                  ?recipe a ?CONNECTIONRECIPETYPE .
              }
              Minus
              {
                  Graph <$defaultPrefix"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?recipe .
                      $filterMultipleProcesses
                      filter (?process IN ($processListAsString))
                  }
              }
          }
          """
        //println(getOutputsOfAllProcesses)
        var firstRes = ""
        val res = update.querySparqlAndUnpackTuple(gmCxn, getOutputsOfAllProcesses, "recipe")
        for (recipe <- res) logger.warn(s"Connection recipe $recipe in the Graph Specification is not the output of a queued process in the Instruction Set, but it is not a required recipe.")
    }
    
    def validateGraphSpecificationAgainstOntology()
    {
        val rangeQuery: String = s"""
          select * where
          {
              graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  Values ?CONNECTIONRECIPETYPE {drivetrain:ObjectConnectionFromTermRecipe 
                                                drivetrain:ObjectConnectionToInstanceRecipe
                                                drivetrain:ObjectConnectionToTermRecipe
                                                }
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe drivetrain:object ?object .
                  ?recipe drivetrain:predicate ?predicate .
                  minus
                  {
                      ?object a drivetrain:MultiObjectDescriber .
                  }
              }
              graph <$ontologyURL>
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

        val domainQuery: String = s"""
          select * where
          {
              graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  Values ?CONNECTIONRECIPETYPE {drivetrain:ObjectConnectionFromTermRecipe 
                                                drivetrain:ObjectConnectionToInstanceRecipe
                                                drivetrain:ObjectConnectionToTermRecipe
                                                drivetrain:DatatypeConnectionRecipe
                                                }
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe drivetrain:subject ?subject .
                  ?recipe drivetrain:predicate ?predicate .
                  minus
                  {
                      ?subject a drivetrain:MultiObjectDescriber .
                  }
              }
              graph <$ontologyURL>
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
    
    def validateConnectionRecipesInProcess(process: String)
    {
        val checkRecipes = s"""
            Select ?recipe Where
            {
                Values ?hasRecipe {drivetrain:hasRequiredInput drivetrain:hasOptionalInput drivetrain:hasOutput}
                <$process> ?hasRecipe ?recipe .
                Minus
                {
                    ?recipe a ?recipeType .
                    ?recipe drivetrain:subject ?subject .
                    ?recipe drivetrain:predicate ?predicate .
                    ?recipe drivetrain:object ?object .
                    ?recipe drivetrain:multiplicity ?multiplicity .
                    
                    Filter (?recipeType IN (drivetrain:ObjectConnectionToInstanceRecipe,
                                            drivetrain:ObjectConnectionToTermRecipe,
                                            drivetrain:ObjectConnectionFromTermRecipe,
                                            drivetrain:DatatypeConnectionRecipe))
                }
            }
          """
        val res = update.querySparqlAndUnpackTuple(gmCxn, checkRecipes, "recipe")
        var firstRes = ""
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Process $process references undefined recipe $firstRes")
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