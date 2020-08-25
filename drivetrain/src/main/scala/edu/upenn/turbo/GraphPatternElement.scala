package edu.upenn.turbo

import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer

trait GraphPatternElement extends ProjectwideGlobals
{
    var value: String = null
    
    var existsInInput: Option[Boolean] = None
    var existsInOutput: Option[Boolean] = None
    
    val oneToOneConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    val oneToManyConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    val manyToOneConnections: HashSet[GraphPatternElement] = new HashSet[GraphPatternElement]
    
    val referencedByInputRecipes: HashSet[ConnectionRecipe] = new HashSet[ConnectionRecipe]
    var dependentOn: Option[GraphPatternElement] = None
    var createdWithRule: Option[String] = None
}

class Instance(newValue: String) extends GraphPatternElement
{
    value = newValue
    
    def buildInstanceType(instance: Instance)
    {
        instance.sparqlTypeString = helper.convertTypeToSparqlVariable(instance.value, true) + " rdf:type <" + instance.instanceType + "> .\n"
    }
    
    var instanceType: String = null
    var sparqlTypeString: String = null
    
    var isUntyped: Option[Boolean] = None
    var isSingleton: Option[Boolean] = None
    var isSuperSingleton: Option[Boolean] = None 
}

class Term(newValue: String) extends GraphPatternElement
{
    value = newValue
    
    def buildValuesBlock()
    {
        if (ranges != None) 
        {
            val asVariable = helper.convertTypeToSparqlVariable(value, true)
            var res = s"VALUES $asVariable {"
            for (item <- ranges.get) res += "<" + item + ">"
            res += "}\n"
            rangesAsSparqlValues = Some(res)
        }
    }
  
    var rangesAsSparqlValues: Option[String] = None
    
    var isResourceList: Option[Boolean] = None
    var ranges: Option[ArrayBuffer[String]] = None
}

class Literal(newValue: String) extends GraphPatternElement
{
    value = newValue
    
    var isResourceList: Option[Boolean] = None
}