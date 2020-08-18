package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

trait QueryClauseBuilder extends ProjectwideGlobals
{
    var includeValuesBlocks: Boolean = false
}

class DeleteClauseBuilder extends QueryClauseBuilder
{
    def buildDeleteGroup(removals: HashSet[ConnectionRecipe], defaultGraph: String): String =
    {
        val deleteClauseStructure = new QueryClauseStructure()
        for (recipe <- removals) deleteClauseStructure.addToDefaultGraphsRequireds(recipe)
        "DELETE {\n " + deleteClauseStructure.buildClause(defaultGraph, includeValuesBlocks) + "}\n"
    }
}
    
class InsertClauseBuilder extends QueryClauseBuilder
{
    def buildInsertGroup(outputs: HashSet[ConnectionRecipe], defaultGraph: String): String =
    {
        val insertClauseStructure = new QueryClauseStructure()
        for (recipe <- outputs) insertClauseStructure.addToDefaultGraphsRequireds(recipe)
        "INSERT {\n " + insertClauseStructure.buildClause(defaultGraph, includeValuesBlocks) + "}\n"
    }
}

class WhereClauseBuilder extends QueryClauseBuilder
{
    includeValuesBlocks = true
    def buildWhereGroup(inputs: HashSet[ConnectionRecipe], defaultGraph: String): String =
    {   
        val whereClauseStructure = new QueryClauseStructure()
        for (recipe <- inputs)
        {   
            var graph = defaultGraph
            if (recipe.foundInGraph != None) graph = recipe.foundInGraph.get
            
            if (recipe.minusGroup != None) whereClauseStructure.addToMinusGroups(recipe.minusGroup.get, graph, recipe)
            else if (recipe.optionalGroup != None) whereClauseStructure.addToOptionalGroups(recipe.optionalGroup.get, graph, recipe)
            else if (recipe.isOptional.get)
            {
                if (graph == defaultGraph) whereClauseStructure.addToDefaultGraphOptionals(recipe)
                else whereClauseStructure.addToAlternateGraphsOptionals(graph, recipe)
            }
            else if (graph != defaultGraph) whereClauseStructure.addToAlternateGraphsRequireds(graph, recipe)
            else whereClauseStructure.addToDefaultGraphsRequireds(recipe)
        }
        "WHERE {\n " + whereClauseStructure.buildClause(defaultGraph, includeValuesBlocks)
    } 
}