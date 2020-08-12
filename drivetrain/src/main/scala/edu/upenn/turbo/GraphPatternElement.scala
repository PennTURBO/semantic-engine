package edu.upenn.turbo

import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer

trait GraphPatternElement 
{
    var value: String = null
    
    var existsInInput: Option[Boolean] = None
    var existsInOutput: Option[Boolean] = None
    
    val oneToOneConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    val oneToManyConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    val manyToOneConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    
    val referencedByRecipes: HashSet[ConnectionRecipe] = new HashSet[ConnectionRecipe]
    var dependentOn: Option[GraphPatternElement] = None
    var createdWithRule: Option[String] = None
}

class Instance extends GraphPatternElement
{
    var isUntyped: Option[Boolean] = None
    var isSingleton: Option[Boolean] = None
    var isSuperSingleton: Option[Boolean] = None 
}

class Term extends GraphPatternElement
{
    var isResourceList: Option[Boolean] = None
    var ranges: Option[ArrayBuffer[String]] = None
}

class Literal extends GraphPatternElement
{
    var isResourceList: Option[Boolean] = None
}