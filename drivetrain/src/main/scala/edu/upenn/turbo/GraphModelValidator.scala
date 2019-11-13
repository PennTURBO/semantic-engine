package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

object GraphModelValidator extends ProjectwideGlobals 
{   
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    
    def checkAllSubjectsAndObjectsHaveType()
    {
        val checkSubjects = s"""
          Select ?s
          where
          {
              Values ?graph { <$defaultPrefix""" + s"""instructionSet> <$defaultPrefix""" + s"""instructionSet>}
              Graph ?graph
              {
                  ?s ?p ?o .
              }
              Minus
              {
                  ?s a ?stype .
              }
              filter (?p != rdf:type)
          }
          """
        //logger.info(checkSubjects)  
        
        val checkObjects = s"""
          Select ?o
          where
          {
              Values ?graph { <$defaultPrefix""" + s"""instructionSet> <$defaultPrefix""" + s"""instructionSet>}
              Graph ?graph
              {
                  ?s ?p ?o .
              }
              Minus
              {
                  ?o a ?stype .
              }
              filter (?p != rdf:type)
              filter (?p != drivetrain:predicate)
              filter (?p != drivetrain:inputNamedGraph)
              filter (?p != drivetrain:outputNamedGraph)
              filter (?p != drivetrain:referencedInGraph)
              filter (!isLiteral(?o))
          }
          """
          //logger.info(checkObjects)
          
          val subjectRes = update.querySparqlAndUnpackTuple(gmCxn, checkSubjects, "s")
          var firstRes = ""
          if (subjectRes.size != 0) firstRes = subjectRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
          
          val objectRes = update.querySparqlAndUnpackTuple(gmCxn, checkObjects, "o")
          firstRes = ""
          if (objectRes.size != 0) firstRes = objectRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
    }
    
    def validateProcessSpecification(process: String)
    {
       val select: String = s"""
          Select * Where {
            <$process> a turbo:TURBO_0010354 .
            <$process> drivetrain:inputNamedGraph ?inputNamedGraph .
            <$process> drivetrain:outputNamedGraph ?outputNamedGraph .
          }
          """
        //logger.info(select)
        val res = update.querySparqlAndUnpackTuple(gmCxn, select, Array("inputNamedGraph", "outputNamedGraph"))
        assert (res.size == 1, (if (res.size == 0) s"Process $process does not exist, ensure required input and output graphs are present" else s"Process $process has duplicate properties"))
    }
    
    def validateAcornResults(results: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        var scannedConnections = new HashMap[String, String]
        var multiplicityMap = new HashMap[String, String]
        for (row <- results)
        {
            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            val thisGraph = (if (row.contains(GRAPHOFORIGIN.toString) && row(GRAPHOFORIGIN.toString) != null) row(GRAPHOFORIGIN.toString).toString else "")
            val subjectContext = (if (row.contains(SUBJECTCONTEXT.toString) && row(SUBJECTCONTEXT.toString) != null) row(SUBJECTCONTEXT.toString).toString else "")
            val objectContext = (if (row.contains(OBJECTCONTEXT.toString) && row(OBJECTCONTEXT.toString) != null) row(OBJECTCONTEXT.toString).toString else "")
            val requirement = (if (row.contains(REQUIREMENT.toString) && row(REQUIREMENT.toString) != null) row(REQUIREMENT.toString).toString else "")
            val suffixOperator = (if (row.contains(SUFFIXOPERATOR.toString) && row(SUFFIXOPERATOR.toString) != null) row(SUFFIXOPERATOR.toString).toString else "")
            val subjectRule = (if (row.contains(SUBJECTRULE.toString) && row(SUBJECTRULE.toString) != null) row(SUBJECTRULE.toString).toString else "")
            val objectRule = (if (row.contains(OBJECTRULE.toString) && row(OBJECTRULE.toString) != null) row(OBJECTRULE.toString).toString else "")
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            val subjectObjectString = subjectString + objectString
            if (multiplicityMap.contains(subjectObjectString)) assert(multiplicityMap(subjectObjectString) == 
              thisMultiplicity, s"Error in graph model: There are multiple connections between $subjectString and $objectString with non-matching multiplicities")
            else multiplicityMap += subjectObjectString -> thisMultiplicity
            
            val fullConnectionString = subjectObjectString + row(PREDICATE.toString).toString + thisMultiplicity + thisGraph +
                                       subjectContext + objectContext + requirement + suffixOperator + subjectRule + objectRule
            if (scannedConnections.contains(connectionName)) assert(scannedConnections(connectionName) == fullConnectionString, s"Error in graph model: recipe $connectionName may have duplicate properties")
            else scannedConnections += connectionName -> fullConnectionString 
        }
    }
    
    def validateGraphSpecificationAgainstOntology()
    {
        val rangeQuery: String = s"""
          select * where
          {
              graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  Values ?CONNECTIONRECIPETYPE {drivetrain:TermToInstanceRecipe 
                                                drivetrain:InstanceToInstanceRecipe
                                                drivetrain:InstanceToTermRecipe
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
        assert(firstRes == "", s"The object of recipe $firstRes is not within the range allowed by its predicate")

        val domainQuery: String = s"""
          select * where
          {
              graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  Values ?CONNECTIONRECIPETYPE {drivetrain:TermToInstanceRecipe 
                                                drivetrain:InstanceToInstanceRecipe
                                                drivetrain:InstanceToTermRecipe
                                                drivetrain:InstanceToLiteralRecipe
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
        assert(firstRes == "", s"The subject of recipe $firstRes is not within the range allowed by its predicate")
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
                    
                    Filter (?recipeType IN (drivetrain:InstanceToInstanceRecipe,
                                            drivetrain:InstanceToTermRecipe,
                                            drivetrain:TermToInstanceRecipe,
                                            drivetrain:InstanceToLiteralRecipe))
                }
            }
          """
        val res = update.querySparqlAndUnpackTuple(gmCxn, checkRecipes, "recipe")
        var firstRes = ""
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Process $process references undefined recipe $firstRes")
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
        
        findRequiredAndUnqueuedRecipes(processListAsString)
        findQueuedAndUnrequiredRecipes(processList, processListAsString)
    }
    
    def validateGraphModelTerms()
    {
        validatePredicates()
        validateTypes()
    }
    
    def validatePredicates()
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
                      drivetrain:partOf,
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
    }
    
    def validateTypes()
    {
        val checkTypes: String = s"""
          Select distinct ?type Where
          {
              Values ?g {<$defaultPrefix"""+s"""instructionSet> <$defaultPrefix"""+"""graphSpecification>}
              Graph ?g
              {
                  ?subject a ?type .
                  Filter (?type NOT IN (
                      drivetrain:InstanceToTermRecipe,
                      drivetrain:InstanceToInstanceRecipe,
                      drivetrain:TermToInstanceRecipe,
                      drivetrain:InstanceToLiteralRecipe,
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
                      drivetrain:TurboGraphLiteralValue,
                      drivetrain:TurboGraphDoubleLiteralValue,
                      drivetrain:TurboGraphIntegerLiteralValue,
                      drivetrain:TurboGraphBooleanLiteralValue,
                      drivetrain:TurboGraphRequirementSpecification,
                      drivetrain:TurboGraphMultiplicityRule,
                      drivetrain:PredicateSuffixSymbol
                  ))
              }
          }
        """
        //println(checkTypes)
        var firstRes = ""
        var res = update.querySparqlAndUnpackTuple(gmCxn, checkTypes, "type")
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Error in graph model: type $firstRes is not known in the Acorn language")
    }
    
    def findRequiredAndUnqueuedRecipes(processListAsString: String)
    {   
        for (singleClass <- getAllSubjectsAndObjectsInQueuedRecipesWithContext(processListAsString))
        {
            val findRequiredButUncreatedRecipes = s"""
              Select ?recipe Where
              {
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject ?subject .
                          ?recipe drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists .
                          Optional 
                          {
                              ?recipe drivetrain:subjectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?subject drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?subject), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject ?subject .
                          ?recipe drivetrain:mustExistIf drivetrain:subjectExists .
                          Optional 
                          {
                              ?recipe drivetrain:subjectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?subject drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?subject), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object ?object .
                          ?recipe drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists .
                          Optional 
                          {
                              ?recipe drivetrain:objectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?object drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?object), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
                      }
                  }
                  UNION
                  {
                      Graph <$defaultPrefix"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object ?object .
                          ?recipe drivetrain:mustExistIf drivetrain:objectExists .
                          Optional 
                          {
                              ?recipe drivetrain:objectUsesContext ?context .
                              ?context a drivetrain:TurboGraphContext .
                              ?object drivetrain:hasPossibleContext ?context .
                          }
                          Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                          Bind(Concat(str(?object), "__", str(?contextOrBlank)) as ?classWithContext)
                          filter (?classWithContext = $singleClass)
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
            val singleClassCleaned = helper.removeQuotesFromString(singleClass.split("\\^")(0)).split(("__"))
            val singleClassCleaned1 = singleClassCleaned(0)
            var errMsg = s"Error in graph model: connection recipe $firstRes in the Graph Specification is required due to the existence of $singleClassCleaned1 "
            if (singleClassCleaned.size > 1) errMsg += "with context " + singleClassCleaned(1) + " "
            errMsg += "but is not the output of a queued process in the Instruction Set"
            assert(firstRes == "", errMsg)
        }
    }
    
    def getAllSubjectsAndObjectsInQueuedRecipesWithContext(processListAsString: String): ArrayBuffer[String] =
    {
        val getSubjectAndObjectOutputs = s"""
          Select distinct ?classWithContext Where
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
                          ?connection a drivetrain:TermToInstanceRecipe ;
                      }
                  }
                  Optional 
                  {
                      ?connection drivetrain:subjectUsesContext ?context .
                      ?context a drivetrain:TurboGraphContext .
                      ?class drivetrain:hasPossibleContext ?context .
                  }
                  Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                  Bind(Concat(str(?class), "__", str(?contextOrBlank)) as ?classWithContext)
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
                          ?connection a drivetrain:InstanceToTermRecipe ;
                      }
                  }
                  Optional 
                  {
                      ?connection drivetrain:objectUsesContext ?context .
                      ?context a drivetrain:TurboGraphContext .
                      ?class drivetrain:hasPossibleContext ?context .
                  }
                  Bind(If(Bound(?context), ?context, "") as ?contextOrBlank)
                  Bind(Concat(str(?class), "__", str(?contextOrBlank)) as ?classWithContext)
                  ?class a owl:Class .
                  filter (?process IN ($processListAsString))
              }
          }
          """
        //logger.info(getSubjectAndObjectOutputs)
        update.querySparqlAndUnpackTuple(gmCxn, getSubjectAndObjectOutputs, "classWithContext")
    }
    
    def findQueuedAndUnrequiredRecipes(processList: ArrayBuffer[String], processListAsString: String)
    {
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
                  Values ?CONNECTIONRECIPETYPE {drivetrain:InstanceToTermRecipe 
                                            drivetrain:InstanceToInstanceRecipe
                                            drivetrain:InstanceToLiteralRecipe
                                            drivetrain:TermToInstanceRecipe}
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
}