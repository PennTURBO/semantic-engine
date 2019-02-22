package edu.upenn.turbo

class ExpansionFromShortcutValue extends ExpansionRule
{
    def this(shortcutVariableName: String, expandedName: String, rule: String, dependent: String = null)
    {
        this()
        this.shortcutVariableName = shortcutVariableName
        this.expandedVariableName = expandedName
        this.rule = rule
        this.dependent = dependent
    }
    
    var expandedVariableName: String = null
    var rule: String = null
    var dependent: String = null
    var shortcutVariableName: String = null
}

object ExpansionFromShortcutValue
{
    def create(expandedName: String, shortcutVariableName: String, rule: String, dependent: String = null): ExpansionFromShortcutValue =
    {
        new ExpansionFromShortcutValue(shortcutVariableName, expandedName, rule, dependent)
    }
}