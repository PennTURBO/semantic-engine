package edu.upenn.turbo

trait ShortcutGraphObjectSingleton extends GraphObjectSingleton with IRIConstructionRules
{
    val appendToBind: String = ""
    val expansionRules: Array[ExpansionRule]
}

trait ShortcutGraphObjectSingletonWithCreate extends ShortcutGraphObjectSingleton
{
    def create(instantiation: String, namedGraph: String, optional: Boolean = false): ShortcutGraphObjectInstance
}