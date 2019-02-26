package edu.upenn.turbo

abstract class GraphObject 
{
    var pattern: String = ""
    var baseVariableName: String = ""
    var typeURI: String = ""
    var variablesToSelect: Array[String] = Array()
    var connections: Map[String, String] = Map()
    var optionalLinks: Map[String, GraphObject] = Map()
    var mandatoryLinks: Map[String, GraphObject] = Map()
    var optionalPattern: String = ""
    
    var optional: Boolean = false
    var namedGraph: String = ""
}