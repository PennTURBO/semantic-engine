package edu.upenn.turbo

abstract class GraphObject 
{
    val pattern: String
    val namedGraph: String
    val baseVariableName: String
    val typeURI: String = ""
    val variablesToSelect: Array[String]
    val connections: Map[String, String] = Map()
    val optionalLinks: Map[String, GraphObject] = Map()
    val mandatoryLinks: Map[String, GraphObject] = Map()
    val optionalPattern: String = ""
}