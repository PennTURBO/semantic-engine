package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.slf4j.LoggerFactory

class GraphModelValidator
{   
    val logger = LoggerFactory.getLogger(getClass)
    
    def checkAcornFilesForMissingTypes()
    {
        val checkSubjects = s"""
          Select ?s
          where
          {
              Values ?graphList { <${Globals.defaultPrefix}""" + s"""instructionSet> 
                                   <${Globals.defaultPrefix}""" + s"""graphSpecification>
                                   <${Globals.defaultPrefix}""" + s"""acornOntology>}
              Graph ?graphList
              {
                  ?s ?p ?o .
              }
              Minus
              {
                  Graph ?g
                  {
                      ?s a ?type .
                  }
              }
              filter (?s != owl:Class)
          }
          """
        //logger.info(checkSubjects)  
                              
        val checkPredicates = s"""
          Select ?p
          where
          {
              Values ?graphList { <${Globals.defaultPrefix}""" + s"""instructionSet> 
                                  <${Globals.defaultPrefix}""" + s"""graphSpecification>
                                  <${Globals.defaultPrefix}""" + s"""acornOntology>}
              Graph ?graphList
              {
                  ?s ?p ?o .
              }
              Minus
              {
                  Graph ?g
                  {
                      ?p a ?type .
                  }
              }
              filter (?p != rdfs:subClassOf)
          }
          """
        //logger.info(checkPredicates)
        
        val checkObjects = s"""
          Select ?o
          where
          {
              {
                  Values ?graphList { <${Globals.defaultPrefix}""" + s"""instructionSet> 
                                      <${Globals.defaultPrefix}""" + s"""acornOntology>}
                  Graph ?graphList
                  {
                      ?s ?p ?o .
                  }
                  Minus
                  {
                      Graph ?g
                      {
                          ?o a ?type .
                      }
                  }
                  filter (?p != drivetrain:inputNamedGraph)
                  filter (?p != drivetrain:outputNamedGraph)
                  filter (?p != drivetrain:referencedInGraph)
                  filter (!isLiteral(?o))
                  filter (?o != owl:Class)
                  filter (?p != drivetrain:subject)
                  filter (?p != drivetrain:predicate)
                  filter (?p != drivetrain:object)
                  filter (?o != rdf:Property)
              }
              UNION
              {
                  Values ?graphList { <${Globals.defaultPrefix}""" + s"""graphSpecification> 
                                      <${Globals.defaultPrefix}""" + s"""acornOntology>}
                  Graph ?graphList
                  {
                      ?s ?p ?o .
                  }
                  Minus
                  {
                      Graph ?g
                      {
                          ?o a ?type .
                      }
                  }
                  filter (?p != drivetrain:inputNamedGraph)
                  filter (?p != drivetrain:outputNamedGraph)
                  filter (?p != drivetrain:referencedInGraph)
                  filter (!isLiteral(?o))
                  filter (?o != owl:Class)
                  filter (?o != rdf:Property)
              }
              
          }
          """
          //logger.info(checkObjects)
                                      
          var firstRes = ""
          
          val subjectRes = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, checkSubjects, "s")
          if (subjectRes.size != 0) firstRes = subjectRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
          
          val predRes = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, checkPredicates, "p")
          if (predRes.size != 0) firstRes = predRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
          
          val objectRes = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, checkObjects, "o")
          if (objectRes.size != 0) firstRes = objectRes(0)
          assert (firstRes == "", s"Error in graph model: $firstRes does not have a type")
    }
    
    def validateProcessSpecification(process: String)
    {
       Utilities.validateURI(process)
       
       val select: String = s"""
          Select * Where {
            <$process> a turbo:TURBO_0010354 .
            <$process> drivetrain:inputNamedGraph ?inputNamedGraph .
            <$process> drivetrain:outputNamedGraph ?outputNamedGraph .
          }
          """
        //logger.info(select)
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, select, Array("inputNamedGraph", "outputNamedGraph"))
        assert (res.size == 1, (if (res.size == 0) s"Process $process does not exist, ensure required input and output graphs are present" else s"Process $process has duplicate properties"))
    }
    
    def validateAcornResults(inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe])
    {
        checkForDuplicateAcornProperties(inputs)
        checkForDuplicateAcornProperties(outputs)
        
        validateSingletonClasses(inputs)
        validateSingletonClasses(outputs)
        
        validateManyToOneClasses(inputs)
        validateManyToOneClasses(outputs)
    }
    
    def checkForDuplicateAcornProperties(results: HashSet[ConnectionRecipe])
    {
        var scannedConnections = new HashSet[String]
        var multiplicityMap = new HashMap[String, String]
        for (recipe <- results)
        {
            val subjectString = recipe.subject.value
            val objectString = recipe.crObject.value
            val subjectObjectString = subjectString + objectString
            if (multiplicityMap.contains(subjectObjectString)) assert(multiplicityMap(subjectObjectString) == 
              recipe.cardinality, s"Error in graph model: There are multiple connections between $subjectString and $objectString with non-matching cardinality")
            else multiplicityMap += subjectObjectString -> recipe.cardinality
            
            val connectionName = recipe.name
            assert(!scannedConnections.contains(connectionName), s"Error in graph model: recipe $connectionName may have duplicate properties")
            scannedConnections += connectionName 
        }
    }
    
    // checks to see whether any connection recipes create an illegal domain or range according to the application ontology
    def validateGraphSpecificationAgainstOntology()
    {
        val rangeQuery: String = s"""
          select * where
          {
              graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  ?recipe a ?${Globals.CONNECTIONRECIPETYPE} .
                  ?recipe drivetrain:object ?object .
                  ?recipe drivetrain:predicate ?predicate .
                  minus
                  {
                      ?object a drivetrain:ClassResourceList .
                  }
              }
              graph <${Globals.defaultPrefix}"""+s"""acornOntology>
              {
                  ?${Globals.CONNECTIONRECIPETYPE} rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              }
              graph <${Globals.ontologyURL}>
              {
                  ?predicate rdfs:subPropertyOf* ?superPredicate .
                  ?superPredicate rdfs:range ?range .
                  minus
                  {
                      ?object rdfs:subClassOf* ?range .
                  }
              }
              Minus
              {
                  ?object a ?resourceList .
                  ?resourceList rdfs:subClassOf* drivetrain:ResourceList .
              }
          }
          """
        //logger.info(rangeQuery)
        var res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, rangeQuery, "recipe")
        var allRes = ""
        for (singleRes <- res)
        {
            allRes += singleRes+"\n"
        }
        assert(allRes == "", s"The objects of the following recipes are not within the ranges allowed by their predicates: \n$allRes")

        val domainQuery: String = s"""
          select * where
          {
              graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  ?recipe a ?CONNECTIONRECIPETYPE .
                  ?recipe drivetrain:subject ?subject .
                  ?recipe drivetrain:predicate ?predicate .
                  minus
                  {
                      ?subject a drivetrain:ClassResourceList .
                  }
              }
              graph <${Globals.defaultPrefix}"""+s"""acornOntology>
              {
                  ?CONNECTIONRECIPETYPE rdfs:subClassOf drivetrain:TurboGraphConnectionRecipe .
              }
              graph <${Globals.ontologyURL}>
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
        //logger.info(domainQuery)
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, domainQuery, "recipe")
        allRes = ""
        for (singleRes <- res)
        {
            allRes += singleRes+"\n"
        }
        assert(allRes == "", s"The subjects of the following recipes are not within the domains allowed by their predicates: \n$allRes")
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
                    ?recipe drivetrain:cardinality ?multiplicity .
                    
                    Filter (?recipeType IN (drivetrain:InstanceToInstanceRecipe,
                                            drivetrain:InstanceToTermRecipe,
                                            drivetrain:TermToInstanceRecipe,
                                            drivetrain:InstanceToLiteralRecipe,
                                            drivetrain:TermToTermRecipe,
                                            drivetrain:TermToLiteralRecipe))
                }
            }
          """
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, checkRecipes, "recipe")
        var firstRes = ""
        if (res.size > 0) firstRes = res(0)
        assert(firstRes == "", s"Process $process references undefined recipe $firstRes")
    }
    
    // Does validation - if fails, will throw assert error or print warning text
    // Only runs when "run all" is called
    def validateProcessesAgainstGraphSpecification(processList: ArrayBuffer[String])
    {
        var processListAsString = ""
        for (process <- processList)
        {
            processListAsString += " <" + process + ">,"
        }
        processListAsString = processListAsString.substring(0, processListAsString.size-1)
        assert (processListAsString != "")
        
        // This ensures that all required recipes have been called as outputs of queued update specifications
        findRequiredAndUnqueuedRecipes(processListAsString)
        // This reminds the user that there may be recipes in the GS that are not outputs, but that is probably ok
        findQueuedAndUnrequiredRecipes(processList, processListAsString)
    }
    
    def findRequiredAndUnqueuedRecipes(processListAsString: String)
    {   
        for (singleClass <- getAllSubjectsAndObjectsInQueuedRecipesWithContext(processListAsString))
        {
            val findRequiredButUncreatedRecipes = s"""
              Select ?recipe Where
              {
                  {
                      Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject ?subject .
                          ?recipe drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists .
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
                      Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:subject ?subject .
                          ?recipe drivetrain:mustExecuteIf drivetrain:subjectExists .
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
                      Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
                      {
                          ?recipe drivetrain:object ?object .
                          ?recipe drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists .
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
                      Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
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
                      Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
                      {
                          ?process drivetrain:hasOutput ?recipe .
                          filter (?process IN ($processListAsString))
                      }
                  }
              }
            """
            //logger.info(findRequiredButUncreatedRecipes)      
            var firstRes = ""
            val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, findRequiredButUncreatedRecipes, "recipe")
            if (res.size > 0) firstRes = res(0)
            val singleClassCleaned = Utilities.removeQuotesFromString(singleClass.split("\\^")(0)).split(("__"))
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
                  Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?connection .
                  }
                  Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
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
                  Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
                  {
                      ?process drivetrain:hasOutput ?connection .
                  }
                  Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
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
        SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, getSubjectAndObjectOutputs, "classWithContext")
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
              Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  Values ?${Globals.CONNECTIONRECIPETYPE} {drivetrain:InstanceToTermRecipe 
                                            drivetrain:InstanceToInstanceRecipe
                                            drivetrain:InstanceToLiteralRecipe
                                            drivetrain:TermToInstanceRecipe
                                            drivetrain:TermToTermRecipe
                                            drivetrain:TermToLiteralRecipe}
                  ?recipe a ?${Globals.CONNECTIONRECIPETYPE} .
              }
              Minus
              {
                  Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
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
        val res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, getOutputsOfAllProcesses, "recipe")
        for (recipe <- res) logger.warn(s"Connection recipe $recipe in the Graph Specification is not the output of a queued process in the Instruction Set. It is optional to implement; this is just a warning.")
    }
    
    def validateManyToOneClasses(results: HashSet[ConnectionRecipe])
    {
        for (recipe <- results)
        {
            for (oneToOneConn <- recipe.subject.oneToOneConnections)
            {
                for (oneToManyConn <- oneToOneConn.oneToManyConnections) 
                {
                    val element1 = oneToManyConn.value
                    val element2 = oneToOneConn.value
                    val element3 = recipe.subject.value
                    assert(!recipe.subject.oneToOneConnections.contains(oneToManyConn), s"Inconsistent cardinality found involving elements $element1, $element2, and $element3")
                }
                for (manyToOneConn <- oneToOneConn.manyToOneConnections) 
                {
                    val element1 = manyToOneConn.value
                    val element2 = oneToOneConn.value
                    val element3 = recipe.subject.value
                    assert(!recipe.subject.oneToOneConnections.contains(manyToOneConn), s"Inconsistent cardinality found involving elements $element1 and $element2, and $element3")
                }
            }
            for (oneToOneConn <- recipe.crObject.oneToOneConnections)
            {
                for (oneToManyConn <- oneToOneConn.oneToManyConnections) 
                {
                    val element1 = oneToManyConn.value
                    val element2 = oneToOneConn.value
                    val element3 = recipe.crObject.value
                    assert(!recipe.crObject.oneToOneConnections.contains(oneToManyConn), s"Inconsistent cardinality found involving elements $element1 and $element2, and $element3")
                }
                for (manyToOneConn <- oneToOneConn.manyToOneConnections) 
                {
                    val element1 = manyToOneConn.value
                    val element2 = oneToOneConn.value
                    val element3 = recipe.crObject.value
                    assert(!recipe.crObject.oneToOneConnections.contains(manyToOneConn), s"Inconsistent cardinality found involving elements $element1 and $element2, and $element3")
                }
            }
        }
    }
    
    def validateSingletonClasses(results: HashSet[ConnectionRecipe])
    {
        for (recipe <- results)
        {
            for (conn <- recipe.subject.oneToOneConnections) if (conn.isInstanceOf[Instance]) 
            {
                val connName = conn.value
                assert(!conn.asInstanceOf[Instance].isSingleton.get && !conn.asInstanceOf[Instance].isSuperSingleton.get, s"Instance $connName cannot be a Singleton and have a 1-1 connection with another element")
            }
            for (conn <- recipe.crObject.oneToOneConnections) if (conn.isInstanceOf[Instance]) 
            {
                val connName = conn.value
                assert(!conn.asInstanceOf[Instance].isSingleton.get && !conn.asInstanceOf[Instance].isSuperSingleton.get, s"Instance $connName cannot be a Singleton and have a 1-1 connection with another element")
            }
            for (conn <- recipe.subject.oneToManyConnections) if (conn.isInstanceOf[Instance]) 
            {
                val connName = conn.value
                assert(!conn.asInstanceOf[Instance].isSingleton.get && !conn.asInstanceOf[Instance].isSuperSingleton.get, s"Instance $connName cannot be a Singleton and have a 1-many connection with another element")
            }
            for (conn <- recipe.crObject.oneToManyConnections) if (conn.isInstanceOf[Instance]) 
            {
                val connName = conn.value
                assert(!conn.asInstanceOf[Instance].isSingleton.get && !conn.asInstanceOf[Instance].isSuperSingleton.get, s"Instance $connName cannot be a Singleton and have a 1-many connection with another element")
            }
            for (conn <- recipe.subject.manyToOneConnections) if (conn.isInstanceOf[Instance]) 
            {
                val connName = conn.value
                assert(!conn.asInstanceOf[Instance].isSingleton.get && !conn.asInstanceOf[Instance].isSuperSingleton.get, s"Instance $connName cannot be a Singleton and have a many-1 connection with another element")
            }
            for (conn <- recipe.crObject.manyToOneConnections) if (conn.isInstanceOf[Instance]) 
            {
                val connName = conn.value
                assert(!conn.asInstanceOf[Instance].isSingleton.get && !conn.asInstanceOf[Instance].isSuperSingleton.get, s"Instance $connName cannot be a Singleton and have a many-1 connection with another element")
            }
        }
    }
    
    def validateConnectionRecipeTypeDeclarations(process: String)
    {
        // check all recipes with instances as subjects
        val instanceSubjectCheck = 
          """select distinct ?subject where
          {
            Values ?instanceSubjectRecipe {drivetrain:InstanceToInstanceRecipe drivetrain:InstanceToTermRecipe drivetrain:InstanceToLiteralRecipe}
            ?recipe a ?instanceSubjectRecipe .
            ?recipe drivetrain:subject ?subject .
            Minus
            {
               ?subject a ?subjectType .
               Values ?subjectType {owl:Class drivetrain:UntypedInstance}
            }
        }"""
        var res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, instanceSubjectCheck, "subject")
        var err = ""
        for (subj <- res) err += subj + " "
        assert (err == "", s"The following subjects were declared as instances by at least one recipe, but were not typed as instances: $err")
        
        // check all recipes with instances as objects
        val instanceObjectCheck = 
          """select distinct ?object where
          {
            Values ?instanceObjectRecipe {drivetrain:InstanceToInstanceRecipe drivetrain:TermToInstanceRecipe}
            ?recipe a ?instanceObjectRecipe .
            ?recipe drivetrain:object ?object .
            Minus
            {
               ?object a ?objectType .
               Values ?objectType {owl:Class drivetrain:UntypedInstance}
            }
        }"""
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, instanceObjectCheck, "object")
        err = ""
        for (obj <- res) err += obj + " "
        assert (err == "", s"The following objects were declared as instances by at least one recipe, but were not typed as instances: $err")
        
        // check all recipes with literals as objects
        val literalObjectCheck = 
          """select distinct ?object where
          {
            Values ?literalObjectRecipe {drivetrain:InstanceToLiteralRecipe drivetrain:TermToLiteralRecipe}
            ?recipe a ?literalObjectRecipe .
            ?recipe drivetrain:object ?object .
        		filter(!isLiteral(?object))
        		Minus
                {
    				    ?object a ?objectType .
            		?objectType rdfs:subClassOf* drivetrain:LiteralResourceList .
                }
        }"""
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, literalObjectCheck, "object")
        err = ""
        for (obj <- res) err += obj + " "
        assert (err == "", s"The following objects were declared as literals by at least one recipe, but were not typed as literals: $err")
        
        // check all recipes with terms as subjects
        val termSubjectCheck = 
          """select distinct ?subject where
          {
            Values ?termSubjectRecipe {drivetrain:TermToInstanceRecipe drivetrain:TermToTermRecipe drivetrain:TermToLiteralRecipe}
            ?recipe a ?termSubjectRecipe .
            ?recipe drivetrain:subject ?subject .
        		?subject a ?subjectType .
        		?subjectType rdfs:subClassOf* drivetrain:LiteralResourceList .
        }"""
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, termSubjectCheck, "subject")
        err = ""
        for (subj <- res) err += subj + " "
        assert (err == "", s"The following subjects were declared as terms by at least one recipe, but were typed as literals: $err")
        
        // check all recipes with terms as objects
        val termObjectCheck = 
          """select distinct ?object where
          {
            Values ?termObjectRecipe {drivetrain:InstanceToTermRecipe drivetrain:TermToTermRecipe}
            ?recipe a ?termObjectRecipe .
            ?recipe drivetrain:object ?object .
        		?object a ?objectType .
        		?objectType rdfs:subClassOf* drivetrain:LiteralResourceList .
        }"""
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, termObjectCheck, "object")
        err = ""
        for (obj <- res) err += obj + " "
        assert (err == "", s"The following objects were declared as terms by at least one recipe, but were typed as literals: $err")
        
        // check all recipes with terms as subjects
        val termLiteralSubjectCheck = 
          """select distinct ?subject where
          {
            Values ?termSubjectRecipe {drivetrain:TermToInstanceRecipe drivetrain:TermToTermRecipe drivetrain:TermToLiteralRecipe}
            ?recipe a ?termSubjectRecipe .
            ?recipe drivetrain:subject ?subject .
        		filter(isLiteral(?subject))
        }"""
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, termLiteralSubjectCheck, "subject")
        err = ""
        for (subj <- res) err += subj + " "
        assert (err == "", s"The following subjects were declared as terms by at least one recipe, but were typed as literals: $err")
        
        // check all recipes with terms as objects
        val termLiteralObjectCheck = 
          """select distinct ?object where
          {
            Values ?termObjectRecipe {drivetrain:InstanceToTermRecipe drivetrain:TermToTermRecipe}
            ?recipe a ?termObjectRecipe .
            ?recipe drivetrain:object ?object .
        		filter(isLiteral(?object))
        }"""
        res = SparqlUpdater.querySparqlAndUnpackTuple(Globals.gmCxn, termLiteralObjectCheck, "object")
        err = ""
        for (obj <- res) err += obj + " "
        assert (err == "", s"The following objects were declared as terms by at least one recipe, but were typed as literals: $err")
    }
}