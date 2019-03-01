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
    val optionalPatterns: Array[String] = Array()
}

abstract class GraphObjectInstance extends GraphObjectSingleton
{
    var optional: Boolean
    var namedGraph: String
}

trait ShortcutGraphObjectSingleton extends GraphObjectSingleton with IRIConstructionRules
{
    val appendToBind: String = ""
    val expansionRules: Array[ExpansionRule]
}

trait ShortcutGraphObjectSingletonWithCreate extends ShortcutGraphObjectSingleton
{
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutGraphObjectInstance
}

trait ShortcutGraphObjectInstance extends GraphObjectInstance with ShortcutGraphObjectSingleton
{
    var instantiation: String
}

abstract class ExpandedGraphObjectSingleton extends GraphObjectSingleton
{
    def create(optional: Boolean = false): GraphObjectInstance
}

abstract class ExpandedGraphObjectSingletonFromDataset extends ExpandedGraphObjectSingleton
{
    val dataset: String
}