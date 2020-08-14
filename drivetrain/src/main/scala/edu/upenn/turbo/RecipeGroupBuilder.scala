package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

trait RecipeGroupBuilder extends ProjectwideGlobals
{
    var clause = ""
    var includeValuesBlocks: Boolean = false
    var typedInstances = new HashSet[Instance]
    var termsWithValuesBlocks = new HashSet[Term]
    
    def searchAndAddFromOtherGroups(group: ConnectionRecipeGroup, otherGroups: HashMap[String, ConnectionRecipeGroup])
    {
        for ((groupName, otherGroup) <- otherGroups)
        {
            for ((graph,recipes) <- group.recipesByGraph) 
            {
                addGroupIfContainsGraph(graph, otherGroup) 
            }
        }
    }
    
    def addGroup(group: ConnectionRecipeGroup)
    {
        clause += group.heading
        for ((graph, recipes) <- group.recipesByGraph)
        {
            clause += s"GRAPH <$graph> {\n"
            for (recipe <- recipes) addRecipe(recipe)
            group.recipesByGraph.remove(graph)
            clause += "}\n"
        }
        clause += group.closing
    }
    
    def addGroupIfContainsGraph(graph: String, group: ConnectionRecipeGroup)
    {
        if (group.recipesByGraph.contains(graph))
        {
            clause += group.heading
            for (recipe <- group.recipesByGraph(graph)) addRecipe(recipe)
            group.recipesByGraph.remove(graph)
            clause += group.heading
        }    
    }
    
    def addRecipe(recipe: ConnectionRecipe)
    {
        if (recipe.isOptional.get) clause += "OPTIONAL {\n"
        clause += recipe.asSparql
        if (recipe.subject.isInstanceOf[Instance] && !recipe.subject.asInstanceOf[Instance].isUntyped.get && !typedInstances.contains(recipe.subject.asInstanceOf[Instance]))
        {
            clause += recipe.subject.asInstanceOf[Instance].sparqlTypeString
            typedInstances += recipe.subject.asInstanceOf[Instance]
        }
        if (recipe.crObject.isInstanceOf[Instance] && !recipe.crObject.asInstanceOf[Instance].isUntyped.get && !typedInstances.contains(recipe.crObject.asInstanceOf[Instance]))
        {
            clause += recipe.crObject.asInstanceOf[Instance].sparqlTypeString
            typedInstances += recipe.crObject.asInstanceOf[Instance]
        }
        if (includeValuesBlocks)
        {
            if (recipe.subject.isInstanceOf[Term] && recipe.subject.asInstanceOf[Term].rangesAsSparqlValues != None && !termsWithValuesBlocks.contains(recipe.subject.asInstanceOf[Term]))
            {
                clause += recipe.subject.asInstanceOf[Term].rangesAsSparqlValues.get
                termsWithValuesBlocks += recipe.subject.asInstanceOf[Term]
            }
            if (recipe.crObject.isInstanceOf[Term] && recipe.crObject.asInstanceOf[Term].rangesAsSparqlValues != None && !termsWithValuesBlocks.contains(recipe.crObject.asInstanceOf[Term]))
            {
                clause += recipe.crObject.asInstanceOf[Term].rangesAsSparqlValues.get
                termsWithValuesBlocks += recipe.crObject.asInstanceOf[Term]
            } 
        }
        if (recipe.isOptional.get) clause += "}\n"
    }
}

class DeleteClauseBuilder extends RecipeGroupBuilder
{
    def buildDeleteGroup(insertGroupRecipes: ConnectionRecipeGroup): String =
    {
        addGroup(insertGroupRecipes)
        if (clause == "") ""
        else s"DELETE {\n $clause }\n"
    }
}
    
class InsertClauseBuilder extends RecipeGroupBuilder
{
    def buildInsertGroup(insertGroupRecipes: ConnectionRecipeGroup): String =
    {
        addGroup(insertGroupRecipes)
        if (clause == "") ""
        else s"INSERT {\n $clause }\n"
    }
}

class WhereClauseBuilder extends RecipeGroupBuilder
{
    includeValuesBlocks = true
    def buildWhereGroup(defaultInputGraphRecipes: ConnectionRecipeGroup, requiredGroup: ConnectionRecipeGroup, optionalGroups: HashMap[String, ConnectionRecipeGroup], minusGroups: HashMap[String, ConnectionRecipeGroup]): String =
    {   
        addGroup(defaultInputGraphRecipes)
        searchAndAddFromOtherGroups(defaultInputGraphRecipes, optionalGroups)
        searchAndAddFromOtherGroups(defaultInputGraphRecipes, minusGroups)
        
        addGroup(requiredGroup)
        searchAndAddFromOtherGroups(requiredGroup, optionalGroups)
        searchAndAddFromOtherGroups(requiredGroup, minusGroups)
        
        for ((groupName,optionalGroup) <- optionalGroups) addGroup(optionalGroup)
        for ((groupName,minusGroup) <- minusGroups) addGroup(minusGroup)
        
        s"WHERE {\n $clause }\n"
    } 
}