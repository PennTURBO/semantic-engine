package edu.upenn.turbo

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

class QueryClauseStructure extends ProjectwideGlobals
{
  	val defaultGraphRequireds = new HashSet[ConnectionRecipe]
  	val defaultGraphOptionals = new HashSet[ConnectionRecipe]
  	val defaultGraphOptionalGroups = new HashMap[String, HashSet[ConnectionRecipe]]
  
  	val alternateGraphsRequireds = new HashMap[String, HashSet[ConnectionRecipe]]
  	val alternateGraphsOptionals = new HashMap[String, HashSet[ConnectionRecipe]]
  	val alternateGraphsOptionalGroups = new HashMap[String, HashMap[String, HashSet[ConnectionRecipe]]]
  
  	val minusGroups = new HashMap[String, HashMap[String, HashSet[ConnectionRecipe]]]
  	val optionalGroups = new HashMap[String, HashMap[String, HashSet[ConnectionRecipe]]]
  	
  	val typedInstances = new HashSet[Instance]
    val termsWithValuesBlocks = new HashSet[Term]
  	
  	var graphToClauseSnippetMap = new HashMap[String, String]
  	var optionalGroupsSnippetMap = new HashMap[String, HashMap[String, String]]
  	var minusGroupsSnippetMap = new HashMap[String, HashMap[String, String]]
  	
  	def buildClause(defaultGraph: String, includeValuesBlocks: Boolean): String =
  	{
  	    differentiateOptionalGroups(defaultGraph)
  	    
  	    buildGraphSnippetsFromLists(defaultGraphRequireds, alternateGraphsRequireds, defaultGraph, includeValuesBlocks)
  	    buildGraphSnippetsFromLists(defaultGraphOptionals, alternateGraphsOptionals, defaultGraph, includeValuesBlocks)
  	    buildOptionalGroupsWithinGraphClauses(defaultGraph, includeValuesBlocks)
  	    buildSpecialGroupsOutsideOfGraphClauses(optionalGroups, optionalGroupsSnippetMap, includeValuesBlocks)
  	    buildSpecialGroupsOutsideOfGraphClauses(minusGroups, minusGroupsSnippetMap, includeValuesBlocks)
  	    
  	    var clause: String = ""
  	    for ((graph, snippet) <- graphToClauseSnippetMap)
  	    {
  	        val convertedGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(graph)
  	        clause += s"GRAPH <$convertedGraph> {\n"
  	        clause += snippet
  	        clause += "}\n"
  	    }
  	    for ((groupName, group) <- optionalGroupsSnippetMap)
  	    {
  	        clause += "OPTIONAL {\n"
  	        for ((graph, snippet) <- group)
  	        {
  	            val convertedGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(graph)
  	            clause += s"GRAPH <$convertedGraph> {\n"
      	        clause += snippet
      	        clause += "}\n"
  	        }
  	        clause += "}\n"
  	    }
  	    for ((groupName, group) <- minusGroupsSnippetMap)
  	    {
  	        clause += "MINUS {\n"
  	        for ((graph, snippet) <- group)
  	        {
  	            val convertedGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(graph)
  	            clause += s"GRAPH <$convertedGraph> {\n"
      	        clause += snippet
      	        clause += "}\n"
  	        }
  	        clause += "}\n"
  	    }
  	    clause
  	}
  	
  	def buildGraphSnippetsFromLists(defaultGraphRecipes: HashSet[ConnectionRecipe], alternateGraphRecipes: HashMap[String, HashSet[ConnectionRecipe]], defaultGraph: String, includeValuesBlocks: Boolean)
  	{
  	    for (recipe <- defaultGraphRecipes) 
  	    {
  	        if (graphToClauseSnippetMap.contains(defaultGraph)) graphToClauseSnippetMap(defaultGraph) += buildRecipe(recipe, includeValuesBlocks)
  	        else graphToClauseSnippetMap += defaultGraph -> buildRecipe(recipe, includeValuesBlocks)
  	    }
  	    for ((graph, recipes)<- alternateGraphRecipes)
  	    {
  	        for (recipe <- recipes)
  	        {
  	            if (graphToClauseSnippetMap.contains(graph)) graphToClauseSnippetMap(graph) += buildRecipe(recipe, includeValuesBlocks)
  	            else graphToClauseSnippetMap += graph -> buildRecipe(recipe, includeValuesBlocks)
  	        }
  	    }
  	}
  	
  	def buildOptionalGroupsWithinGraphClauses(defaultGraph: String, includeValuesBlocks: Boolean)
  	{
  	    for ((group, recipes) <- defaultGraphOptionalGroups)
  	    {
  	        if (graphToClauseSnippetMap.contains(defaultGraph)) graphToClauseSnippetMap(defaultGraph) += buildOptionalGroupWithoutGraphClauses(recipes, includeValuesBlocks)
  	        else graphToClauseSnippetMap += defaultGraph -> buildOptionalGroupWithoutGraphClauses(recipes, includeValuesBlocks)
  	    }
  	    for ((graph, group) <- alternateGraphsOptionalGroups)
  	    {
  	        for ((groupName, recipes) <- group)
  	        {
  	            if (graphToClauseSnippetMap.contains(graph)) graphToClauseSnippetMap(graph) += buildOptionalGroupWithoutGraphClauses(recipes, includeValuesBlocks)
  	            else graphToClauseSnippetMap += graph -> buildOptionalGroupWithoutGraphClauses(recipes, includeValuesBlocks)
  	        }
  	    }
  	}
  	
  	def buildSpecialGroupsOutsideOfGraphClauses(specialGroups: HashMap[String, HashMap[String, HashSet[ConnectionRecipe]]], snippetMap: HashMap[String, HashMap[String, String]], includeValuesBlocks: Boolean)
  	{
  	    for ((groupName, group) <- specialGroups)
  	    {
  	        snippetMap += groupName -> new HashMap[String, String]
  	        for ((graph, recipes) <- group)
  	        { 
  	            snippetMap(groupName) += graph -> ""
  	            for (recipe <- recipes) 
  	            {
  	                if (!recipe.isOptional.get)
  	                {
  	                    snippetMap(groupName)(graph) += buildRecipe(recipe, includeValuesBlocks) 
  	                    specialGroups(groupName)(graph).remove(recipe)
  	                }
  	            }
  	        }
  	    }
  	    for ((groupName, group) <- specialGroups)
        {
            for ((graph, recipes) <- group)
            {
                for (recipe <- recipes) snippetMap(groupName)(graph) += buildRecipe(recipe, includeValuesBlocks) 
            }
        }
  	}
  	
  	def buildOptionalGroupWithoutGraphClauses(recipes: HashSet[ConnectionRecipe], includeValuesBlocks: Boolean): String =
  	{
  	    var str = "OPTIONAL {\n"
  	    var optionalRecipes = new HashSet[ConnectionRecipe]
  	    for (recipe <- recipes) 
  	    {    
  	        if (recipe.isOptional.get) optionalRecipes += recipe
  	        else str += buildRecipe(recipe, includeValuesBlocks)
  	    }
  	    for (optionalRecipe <- optionalRecipes) str += buildRecipe(optionalRecipe, includeValuesBlocks)
  	    str + "}\n"
  	}

  	def buildRecipe(recipe: ConnectionRecipe, includeValuesBlocks: Boolean): String =
    {
  	    var thisRecipe = ""
  	    if (recipe.isOptional.get) thisRecipe += "OPTIONAL {\n"
        thisRecipe += recipe.asSparql
        if (recipe.subject.isInstanceOf[Instance] && !recipe.subject.asInstanceOf[Instance].isUntyped.get && !typedInstances.contains(recipe.subject.asInstanceOf[Instance]))
        {
            thisRecipe += recipe.subject.asInstanceOf[Instance].sparqlTypeString
            typedInstances += recipe.subject.asInstanceOf[Instance]
        }
        if (recipe.crObject.isInstanceOf[Instance] && !recipe.crObject.asInstanceOf[Instance].isUntyped.get && !typedInstances.contains(recipe.crObject.asInstanceOf[Instance]))
        {
            thisRecipe += recipe.crObject.asInstanceOf[Instance].sparqlTypeString
            typedInstances += recipe.crObject.asInstanceOf[Instance]
        }
        if (includeValuesBlocks)
        {
            if (recipe.subject.isInstanceOf[Term] && recipe.subject.asInstanceOf[Term].rangesAsSparqlValues != None && !termsWithValuesBlocks.contains(recipe.subject.asInstanceOf[Term]))
            {
                thisRecipe += recipe.subject.asInstanceOf[Term].rangesAsSparqlValues.get
                termsWithValuesBlocks += recipe.subject.asInstanceOf[Term]
            }
            if (recipe.crObject.isInstanceOf[Term] && recipe.crObject.asInstanceOf[Term].rangesAsSparqlValues != None && !termsWithValuesBlocks.contains(recipe.crObject.asInstanceOf[Term]))
            {
                thisRecipe += recipe.crObject.asInstanceOf[Term].rangesAsSparqlValues.get
                termsWithValuesBlocks += recipe.crObject.asInstanceOf[Term]
            } 
        }
        if (recipe.isOptional.get) thisRecipe += "}\n"
        thisRecipe
    }
  	
  	def differentiateOptionalGroups(defaultGraph: String)
  	{
  	    for ((groupName, group) <- optionalGroups)
  	    {
  	        // if there is only one graph in the group, and that graph also contains required elements, tuck this optional group inside that graph's clause
  	        if (group.size == 1)
  	        {
  	            val graph = group.head._1
  	            val recipes = group.head._2
  	            if (graph == defaultGraph) 
  	            {
  	                defaultGraphOptionalGroups += groupName -> recipes
  	                optionalGroups.remove(groupName)
  	            }
  	            else if (alternateGraphsRequireds.contains(graph)) 
  	            {
  	                if (alternateGraphsOptionalGroups.contains(graph)) alternateGraphsOptionalGroups(graph) += groupName -> recipes
  	                else alternateGraphsOptionalGroups += graph -> HashMap(groupName -> recipes)
  	                optionalGroups.remove(groupName)
  	            }
  	        }
  	    }
  	}
  	
  	def addToMinusGroups(groupName: String, graph: String, recipe: ConnectionRecipe)
  	{
  	    if (minusGroups.contains(groupName)) 
  	    {
  	        if (minusGroups(groupName).contains(graph)) minusGroups(groupName)(graph) += recipe
  	        else minusGroups(groupName) += graph -> HashSet(recipe)
  	    }
  	    else minusGroups += groupName -> HashMap(graph -> HashSet(recipe))
  	}
  	
  	def addToOptionalGroups(groupName: String, graph: String, recipe: ConnectionRecipe)
  	{
  	    if (optionalGroups.contains(groupName)) 
  	    {
  	        if (optionalGroups(groupName).contains(graph)) optionalGroups(groupName)(graph) += recipe
  	        else optionalGroups(groupName) += graph -> HashSet(recipe)
  	    }
  	    else optionalGroups += groupName -> HashMap(graph -> HashSet(recipe))
  	}
  	
  	def addToDefaultGraphOptionals(recipe: ConnectionRecipe)
  	{
  	    defaultGraphOptionals += recipe
  	}
  	
  	def addToDefaultGraphsRequireds(recipe: ConnectionRecipe)
  	{
  	    defaultGraphRequireds += recipe
  	}
  	
  	def addToAlternateGraphsOptionals(graph: String, recipe: ConnectionRecipe)
  	{
  	    if (alternateGraphsOptionals.contains(graph)) alternateGraphsOptionals(graph) += recipe
  	    else alternateGraphsOptionals += graph -> HashSet(recipe)
  	}
  	
  	def addToAlternateGraphsRequireds(graph: String, recipe: ConnectionRecipe)
  	{
  	    if (alternateGraphsRequireds.contains(graph)) alternateGraphsRequireds(graph) += recipe
  	    else alternateGraphsRequireds += graph -> HashSet(recipe)
  	}
}