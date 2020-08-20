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
        "DELETE {\n " + deleteClauseStructure.buildClause(defaultGraph, includeValuesBlocks, false) + "}\n"
    }
}
    
class InsertClauseBuilder extends QueryClauseBuilder
{
    def buildInsertGroup(inputs: HashSet[ConnectionRecipe], outputs: HashSet[ConnectionRecipe], defaultGraph: String, process: String): String =
    {
        val insertClauseStructure = new QueryClauseStructure()
        val processTerm = new Term(process)
        processTerm.isResourceList = Some(false)
        
        val inputInstances = new HashSet[Instance]
        val inputTerms = new HashSet[Term]
        val outputInstances = new HashSet[Instance]
        val outputTerms = new HashSet[Term]
        
        for (recipe <- outputs) 
        {
            // if ClassResourceList only appears in output and has range of 1, use that term instead of the ResourceList's URI
            if (recipe.subject.isInstanceOf[Term] && 
                recipe.subject.asInstanceOf[Term].isResourceList.get && 
                recipe.subject.asInstanceOf[Term].ranges != None && 
                recipe.subject.asInstanceOf[Term].ranges.get.size == 1 && 
                !recipe.subject.asInstanceOf[Term].existsInInput.get) 
            {
                recipe.subject.value = recipe.subject.asInstanceOf[Term].ranges.get(0)
                recipe.subject.asInstanceOf[Term].isResourceList = Some(false)
            }
            if (recipe.crObject.isInstanceOf[Term] && 
                recipe.crObject.asInstanceOf[Term].isResourceList.get && 
                recipe.crObject.asInstanceOf[Term].ranges != None && 
                recipe.crObject.asInstanceOf[Term].ranges.get.size == 1 && 
                !recipe.crObject.asInstanceOf[Term].existsInInput.get) 
            {
                recipe.crObject.value = recipe.crObject.asInstanceOf[Term].ranges.get(0)
                recipe.crObject.asInstanceOf[Term].isResourceList = Some(false)
            }
            insertClauseStructure.addToDefaultGraphsRequireds(recipe)
            
            if (recipe.subject.isInstanceOf[Term] && !outputTerms.contains(recipe.subject.asInstanceOf[Term]))
            {
                val outputRecipe = new TermToTermConnRecipe(processTerm, "http://transformunify.org/ontologies/TURBO_0010184", recipe.subject.asInstanceOf[Term])
                outputRecipe.isOptional = Some(false)
                outputTerms += recipe.subject.asInstanceOf[Term]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, outputRecipe)
            }
            if (recipe.crObject.isInstanceOf[Term] && !outputTerms.contains(recipe.crObject.asInstanceOf[Term]))
            {
                val outputRecipe = new TermToTermConnRecipe(processTerm, "http://transformunify.org/ontologies/TURBO_0010184", recipe.crObject.asInstanceOf[Term])
                outputRecipe.isOptional = Some(false)
                outputTerms += recipe.crObject.asInstanceOf[Term]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, outputRecipe)
            }
            if (recipe.subject.isInstanceOf[Instance] && !outputInstances.contains(recipe.subject.asInstanceOf[Instance]))
            {
                val outputRecipe = new TermToInstConnRecipe(processTerm, "http://transformunify.org/ontologies/TURBO_0010184", recipe.subject.asInstanceOf[Instance])
                outputRecipe.isOptional = Some(false)
                outputInstances += recipe.subject.asInstanceOf[Instance]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, outputRecipe)
            }
            if (recipe.crObject.isInstanceOf[Instance] && !outputInstances.contains(recipe.crObject.asInstanceOf[Instance]))
            {
                val outputRecipe = new TermToInstConnRecipe(processTerm, "http://transformunify.org/ontologies/TURBO_0010184", recipe.crObject.asInstanceOf[Instance])
                outputRecipe.isOptional = Some(false)
                outputInstances += recipe.crObject.asInstanceOf[Instance]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, outputRecipe)
            }
        }
        for (recipe <- inputs)
        {
            if (recipe.subject.isInstanceOf[Term] && !inputTerms.contains(recipe.subject.asInstanceOf[Term]))
            {
                val inputRecipe = new TermToTermConnRecipe(processTerm, "http://purl.obolibrary.org/obo/OBI_0000293", recipe.subject.asInstanceOf[Term])
                inputRecipe.isOptional = Some(false)
                inputTerms += recipe.subject.asInstanceOf[Term]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, inputRecipe)
            }
            if (recipe.crObject.isInstanceOf[Term] && !inputTerms.contains(recipe.crObject.asInstanceOf[Term]))
            {
                val inputRecipe = new TermToTermConnRecipe(processTerm, "http://purl.obolibrary.org/obo/OBI_0000293", recipe.crObject.asInstanceOf[Term])
                inputRecipe.isOptional = Some(false)
                inputTerms += recipe.crObject.asInstanceOf[Term]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, inputRecipe)
            }
            if (recipe.subject.isInstanceOf[Instance] && !inputInstances.contains(recipe.subject.asInstanceOf[Instance]))
            {
                val inputRecipe = new TermToInstConnRecipe(processTerm, "http://purl.obolibrary.org/obo/OBI_0000293", recipe.subject.asInstanceOf[Instance])
                inputRecipe.isOptional = Some(false)
                inputInstances += recipe.subject.asInstanceOf[Instance]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, inputRecipe)
            }
            if (recipe.crObject.isInstanceOf[Instance] && !inputInstances.contains(recipe.crObject.asInstanceOf[Instance]))
            {
                val inputRecipe = new TermToInstConnRecipe(processTerm, "http://purl.obolibrary.org/obo/OBI_0000293", recipe.crObject.asInstanceOf[Instance])
                inputRecipe.isOptional = Some(false)
                inputInstances += recipe.crObject.asInstanceOf[Instance]
                insertClauseStructure.addToAlternateGraphsRequireds(processNamedGraph, inputRecipe)
            }
        }
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