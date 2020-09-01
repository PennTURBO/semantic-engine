package edu.upenn.turbo

import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer

// anything referenced in an Acorn file as a subject or an object
trait GraphPatternElement
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

// Instance is a URI with a pseudo UUID that has rdf:type some class
class Instance(newValue: String) extends GraphPatternElement
{
    value = newValue
    
    def buildInstanceType(instance: Instance)
    {
        instance.sparqlTypeString = Utilities.convertTypeToSparqlVariable(instance.value, true) + " rdf:type <" + instance.instanceType + "> .\n"
    }
    
    var instanceType: String = null
    var sparqlTypeString: String = null
    
    var isUntyped: Option[Boolean] = None
    // only one instance per update
    var isSingleton: Option[Boolean] = None
    // only one instance per graph
    var isSuperSingleton: Option[Boolean] = None 
}

// Term is a class itself, not an instance of a class
class Term(newValue: String) extends GraphPatternElement
{
    value = newValue
    
    def buildValuesBlock()
    {
        if (ranges != None) 
        {
            val asVariable = Utilities.convertTypeToSparqlVariable(value, true)
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