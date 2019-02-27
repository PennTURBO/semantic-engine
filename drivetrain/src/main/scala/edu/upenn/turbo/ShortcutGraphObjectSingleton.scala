package edu.upenn.turbo

trait ShortcutGraphObjectSingleton extends GraphObjectSingleton with IRIConstructionRules
{
    val appendToBind: String = ""
    val expansionRules: Array[ExpansionRule]
    
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutGraphObjectInstance
}