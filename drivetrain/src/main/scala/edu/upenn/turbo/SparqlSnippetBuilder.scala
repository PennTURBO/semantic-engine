package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import org.slf4j.LoggerFactory

class SparqlSnippetBuilder
{
    val logger = LoggerFactory.getLogger(getClass)
    
    def buildSnippet(recipe: ConnectionRecipe): String =
    {
        Utilities.validateURI(recipe.subject.value)
        val subject = buildSubject(recipe)
        val predicate = buildPredicate(recipe)
        val crObject = buildObject(recipe)
        subject + " " + predicate + " " + crObject + " .\n"
    }
    
    def buildSubject(recipe: ConnectionRecipe): String =
    {
        if (recipe.subject.isInstanceOf[Term]) assert(recipe.subject.asInstanceOf[Term].isResourceList != None, "isResourceList property for Term " + recipe.subject.value + " cannot be None")
        var subject = "<" + recipe.subject.value + ">"
        if (recipe.subject.isInstanceOf[Instance] || (recipe.subject.isInstanceOf[Term] && recipe.subject.asInstanceOf[Term].isResourceList.get)) subject = Utilities.convertTypeToSparqlVariable(recipe.subject.value, true)
        subject
    }

    def buildPredicate(recipe: ConnectionRecipe): String =
    {
        var predicate = recipe.predicate
        var formattedSuffixOperator = ""
        if (recipe.predicateSuffixOperator != None)
        {
            val allowedOperators = Array("*", "+")
            formattedSuffixOperator = recipe.predicateSuffixOperator.get.split("\\^")(0).replaceAll("\"", "")
            if (!allowedOperators.contains(formattedSuffixOperator)) formattedSuffixOperator = ""
            if (predicate == "a" || predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") predicate = "rdf:type" + formattedSuffixOperator
        }
            Utilities.validateURI(predicate)
            "<" + predicate + ">" + formattedSuffixOperator
    }
    
    def buildObject(recipe: ConnectionRecipe): String =
    {
        var crObject = ""
        if (recipe.crObject.isInstanceOf[Literal])
        {
            if (!recipe.crObject.asInstanceOf[Literal].isResourceList.get) crObject = recipe.crObject.value
            else crObject = Utilities.convertTypeToSparqlVariable(recipe.crObject.value, true)
        }
        else
        {
            if (recipe.crObject.isInstanceOf[Term]) assert(recipe.crObject.asInstanceOf[Term].isResourceList != None, "isResourceList property for Term " + recipe.crObject.value + " cannot be None")
            crObject = "<" + recipe.crObject.value + ">"
            if (recipe.crObject.isInstanceOf[Instance] || (recipe.crObject.isInstanceOf[Term] && recipe.crObject.asInstanceOf[Term].isResourceList.get)) crObject = Utilities.convertTypeToSparqlVariable(recipe.crObject.value, true) 
        }
        crObject
    }
}