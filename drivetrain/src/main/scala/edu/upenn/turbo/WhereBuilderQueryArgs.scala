package edu.upenn.turbo

class WhereBuilderQueryArgs 
{
    def this(buildList: Array[_ <: GraphObjectInstance], valuesList: Map[String, Array[String]], limit: Integer)
    {
        this()
        this.buildList = buildList
        this.valuesList = valuesList
        this.limit = limit
    }
    
    var buildList: Array[_ <: GraphObjectInstance] = null
    var valuesList: Map[String, Array[String]] = null
    var limit: Integer = null
}

object WhereBuilderQueryArgs
{
    def create(buildList: Array[_ <: GraphObjectInstance], valuesList: Map[String, Array[String]] = Map(), limit: Integer = null): WhereBuilderQueryArgs =
    {
        new WhereBuilderQueryArgs(buildList, valuesList, limit)
    }
}