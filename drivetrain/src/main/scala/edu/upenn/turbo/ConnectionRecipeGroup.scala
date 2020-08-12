package edu.upenn.turbo

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

trait ConnectionRecipeGroup 
{
    val recipesByGraph: HashMap[String, HashSet[ConnectionRecipe]] = new HashMap[String, HashSet[ConnectionRecipe]]
}

class RequiredGroup extends ConnectionRecipeGroup
{
    val heading = ""
    val closing = ""
}

class MinusGroup extends ConnectionRecipeGroup
{
    val heading = "MINUS {"
    val closing = "}"
}

class OptionalGroup extends ConnectionRecipeGroup
{
    val heading = "OPTIONAL {"
    val closing = "}"
}