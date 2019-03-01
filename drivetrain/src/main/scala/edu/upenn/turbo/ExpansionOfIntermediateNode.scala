package edu.upenn.turbo

class ExpansionOfIntermediateNode extends ExpansionRule
{
    def this(expandedVariableName: String, rule: String, dependent: String, shortcutVariableName: String)
    {
        this()
        this.expandedVariableName = expandedVariableName
        this.rule = rule
        this.dependent = dependent
        this.shortcutVariableName = shortcutVariableName
    }
    
    var expandedVariableName: String = null
    var rule: String = null
    var dependent: String = null
    var shortcutVariableName: String = null
}

object ExpansionOfIntermediateNode
{
    def create(expandedName: String, rule: String, dependent: String = null, shortcutVariableName: String = null): ExpansionOfIntermediateNode =
    {
        new ExpansionOfIntermediateNode(expandedName, rule, dependent, shortcutVariableName)
    }
}