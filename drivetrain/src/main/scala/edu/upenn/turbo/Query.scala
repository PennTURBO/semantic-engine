package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import org.eclipse.rdf4j.model.Value
import java.util.UUID

abstract class Query extends ProjectwideGlobals
{
    var query: String = ""
    var defaultOutputGraph: String = null
    
    def runQuery(cxn: RepositoryConnection)
    {
        assert (query != "" && query != null)
        update.updateSparql(cxn, query)
    }
    
    def getQuery(): String = query
}

class PatternMatchQuery(cxn: RepositoryConnection) extends Query
{
    this.gmCxn = cxn
    
    var bindClause: String = ""
    var whereClause: String = ""
    var insertClause: String = ""
    var deleteClause: String = ""
    
    var defaultInputGraph: String = null
    var defaultRemovalsGraph: String = null
    
    var processSpecification: String = null
    var process: String = null

    var bindClauseBuilder: BindClauseBuilder = new BindClauseBuilder()
        
    /* Contains set of of variables used in bind and where clauses, so the insert clause knows that these have already been defined. Any URI present in the 
     in the insert clause will not be converted to a variable unless it is included in this list. The boolean value represents whether a URI is qualified
     to be a multiplicity enforcer for the bind clause. To be qualified, it must have a type declared in the Where block and at some point be listed as required
     input to the process. */
    var usedVariables: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    
    override def runQuery(cxn: RepositoryConnection)
    {
        query = getQuery()
        assert (query != "" && query != null)
        val graphUUID = UUID.randomUUID().toString().replaceAll("-", "")
        logger.info(query)
        update.updateSparql(cxn, query)
    }
    
    override def getQuery(): String = deleteClause + "\n" + insertClause + "\n" + whereClause + bindClause + "}"
    
    def setInputGraph(inputGraph: String)
    {
        this.defaultInputGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(inputGraph)
        helper.validateURI(this.defaultInputGraph)
    }
    
    def setOutputGraph(outputGraph: String)
    {
        this.defaultOutputGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(outputGraph)
        helper.validateURI(this.defaultOutputGraph)
    }
    
    def setRemovalsGraph(removalsGraph: String)
    {
        this.defaultRemovalsGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(removalsGraph)
        helper.validateURI(this.defaultRemovalsGraph)
    }
    
    def setProcessSpecification(processSpecification: String)
    {
        assert (processSpecification.contains(':'))
        this.processSpecification = processSpecification
    }
    
    def setProcess(process: String)
    {
        assert (process.contains(':'))
        this.process = process
    }
    
    def createInsertClause(outputs: HashSet[ConnectionRecipe])
    {
        assert (insertClause == "")
        if (whereClause == null || whereClause.size == 0 || bindClause == null || bindClause.size == 0) 
        {
            throw new RuntimeException("Insert clause cannot be built before where or bind clauses are built.")
        }
        val insertGroup = new ConnectionRecipeGroup("","")
        var graph = defaultOutputGraph
        for (recipe <- outputs)
        {
            if (insertGroup.recipesByGraph.contains(graph)) insertGroup.recipesByGraph(graph) += recipe
            else insertGroup.recipesByGraph += graph -> HashSet(recipe) 
        }
        val groupBuilder = new InsertClauseBuilder
        insertClause = groupBuilder.buildInsertGroup(insertGroup)
    }

    def createDeleteClause(removals: HashSet[ConnectionRecipe])
    {
        assert (deleteClause == "")
        assert (defaultRemovalsGraph != null && defaultRemovalsGraph != "")
        
        val deleteGroup = new ConnectionRecipeGroup("","")
        for (recipe <- removals)
        {
            var graph = defaultRemovalsGraph
            if (deleteGroup.recipesByGraph.contains(graph)) deleteGroup.recipesByGraph(graph) += recipe
            else deleteGroup.recipesByGraph += graph -> HashSet(recipe) 
        }
        val groupBuilder = new DeleteClauseBuilder
        deleteClause = groupBuilder.buildDeleteGroup(deleteGroup)
    }
    
    def createWhereClause(inputs: HashSet[ConnectionRecipe])
    {
        assert (inputs.size != 0)
        assert (whereClause == "")
        assert (defaultInputGraph != null && defaultInputGraph != "", "No default input named graph set")
        val defaultInputGraphRequiredGroup = new ConnectionRecipeGroup("","")
        val alternateGraphRequiredGroup = new ConnectionRecipeGroup("","")
        val minusGroups: HashMap[String, ConnectionRecipeGroup] = new HashMap[String, ConnectionRecipeGroup]
        val optionalGroups: HashMap[String, ConnectionRecipeGroup] = new HashMap[String, ConnectionRecipeGroup]
        for (recipe <- inputs) 
        {
            var graph = defaultInputGraph
            if (recipe.foundInGraph != None) graph = recipe.foundInGraph.get
            if (recipe.minusGroup != None)
            {
                val minusGroup = recipe.minusGroup.get
                if (minusGroups.contains(minusGroup))
                {
                    val thisGroup = minusGroups(minusGroup)
                    if (thisGroup.recipesByGraph.contains(graph)) thisGroup.recipesByGraph(graph) += recipe
                    else thisGroup.recipesByGraph += graph -> HashSet(recipe)
                }
                else 
                {
                    val newMinusGroup = new ConnectionRecipeGroup("MINUS{\n","}\n")
                    newMinusGroup.recipesByGraph += graph -> HashSet(recipe)
                    minusGroups += minusGroup -> newMinusGroup
                }
            }
            if (recipe.optionalGroup != None)
            {
                val optionalGroup = recipe.optionalGroup.get
                if (optionalGroups.contains(optionalGroup))
                {
                    val thisGroup = optionalGroups(optionalGroup)
                    if (thisGroup.recipesByGraph.contains(graph)) thisGroup.recipesByGraph(graph) += recipe
                    else thisGroup.recipesByGraph += graph -> HashSet(recipe)
                }
                else
                {
                    val newOptionalGroup = new ConnectionRecipeGroup("OPTIONAL{\n","}\n")
                    newOptionalGroup.recipesByGraph += graph -> HashSet(recipe)
                    optionalGroups += optionalGroup -> newOptionalGroup
                }
            }
            if (recipe.minusGroup == None && recipe.optionalGroup == None)
            {
                if (graph == defaultInputGraph) 
                {
                    if (defaultInputGraphRequiredGroup.recipesByGraph.contains(defaultInputGraph)) defaultInputGraphRequiredGroup.recipesByGraph(defaultInputGraph) += recipe
                    else defaultInputGraphRequiredGroup.recipesByGraph += defaultInputGraph -> HashSet(recipe)
                }
                else
                {
                     if (alternateGraphRequiredGroup.recipesByGraph.contains(graph)) alternateGraphRequiredGroup.recipesByGraph(graph) += recipe
                     else alternateGraphRequiredGroup.recipesByGraph += graph -> HashSet(recipe) 
                }
            }
        }
        val groupBuilder = new WhereClauseBuilder
        whereClause = groupBuilder.buildWhereGroup(defaultInputGraphRequiredGroup, alternateGraphRequiredGroup, optionalGroups, minusGroups)
    }

    def createBindClause(inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe], localUUID: String)
    {
        assert (inputs.size != 0)
        assert (bindClause == "")
        if (whereClause == null || whereClause.size == 0) 
        {
            throw new RuntimeException("Bind clause cannot be built before where clause is built.")
        }
        usedVariables = bindClauseBuilder.buildBindClause(process, localUUID, inputs, outputs)
     
        bindClause = bindClauseBuilder.clause
        // if we attempted to build the bind clause and no binds were necessary, make the clause contain a single empty character so that other clauses can be built
        if (bindClause == "") bindClause += " "
    }
}