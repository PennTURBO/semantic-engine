package edu.upenn.turbo

trait GraphObjectSingleton
{
    val optionalLinks: Map[String, GraphObjectSingleton] = Map()
    val mandatoryLinks: Map[String, GraphObjectSingleton] = Map()
    val optionalPatterns: Array[String] = Array()
    
    val variablesToSelect: Array[String]
    val pattern: String
    val baseVariableName: String
    val typeURI: String
}

trait GraphObjectInstance extends GraphObjectSingleton
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

trait DependentOptionalTrait 
{
    var dependent: String
}

trait ExpandedGraphObjectSingleton extends GraphObjectSingleton
{
    def create(optional: Boolean = false): GraphObjectInstance
}

trait ExpandedGraphObjectSingletonFromDataset extends ExpandedGraphObjectSingleton
{
    val dataset: String
}