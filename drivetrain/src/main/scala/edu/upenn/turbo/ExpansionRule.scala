package edu.upenn.turbo

abstract class ExpansionRule extends DependentOptionalTrait
{
    var rule: String
    var expandedVariableName: String
    var shortcutVariableName: String
}