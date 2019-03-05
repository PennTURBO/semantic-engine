package edu.upenn.turbo

trait SimpleGraphObject
{
    var pattern: String
    val baseVariableName: String
    val typeURI: String
}

trait GraphObjectSingleton extends SimpleGraphObject
{
    val optionalLinks: Map[String, GraphObjectSingleton] = Map()
    val mandatoryLinks: Map[String, GraphObjectSingleton] = Map()
    val optionalPatterns: Array[String] = Array()
    val variablesToSelect: Array[String]
}

abstract class SimpleGraphObjectInstance extends SimpleGraphObject
{
    def this(optional: Boolean)
    {
        this()
        this.optional = optional
        this.pattern = buildPattern(this)
    }
    var optional: Boolean = false
    
    def buildPattern(input:SimpleGraphObjectInstance): String = {""}
    def withPattern(input:GraphObjectSingleton) {}
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