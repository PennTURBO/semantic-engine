package edu.upenn.turbo

abstract class GraphObjectSingleton 
{
    // these must be implemented by the extending objects
    val pattern: String
    val baseVariableName: String
    val typeURI: String
    val variablesToSelect: Array[String]
    
    //these can be optionally implemented on a case by case basis
    val optionalLinks: Map[String, GraphObjectSingleton] = Map()
    val mandatoryLinks: Map[String, GraphObjectSingleton] = Map()
    val optionalPattern: String = ""
}