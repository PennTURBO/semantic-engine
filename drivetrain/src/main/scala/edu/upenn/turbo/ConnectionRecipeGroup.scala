package edu.upenn.turbo

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

class ConnectionRecipeGroup(newHeading: String, newClosing: String)
{
    var heading: String = newHeading
    var closing: String = newClosing
    val recipesByGraph: HashMap[String, HashSet[ConnectionRecipe]] = new HashMap[String, HashSet[ConnectionRecipe]]
}