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
        /*val rangeQuery: String = s"""
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
        assert(firstRes == "")*/
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
        var firstRes = ""
        var res = update.querySparqlAndUnpackTuple(gmCxn, checkTypes, "type")
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Error in graph model: type $firstRes is not known in the Acorn language")
    }
    
    def findRequiredAndUnqueuedRecipes(processListAsString: String)
    {   
        for (singleClass <- getAllSubjectsAndObjectsInQueuedRecipes(processListAsString))
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
    }
    
    def getAllSubjectsAndObjectsInQueuedRecipes(processListAsString: String): ArrayBuffer[String] =
    {
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
        update.querySparqlAndUnpackTuple(gmCxn, getSubjectAndObjectOutputs, "class")
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
}