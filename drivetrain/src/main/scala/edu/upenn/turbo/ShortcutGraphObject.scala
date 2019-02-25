package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

abstract class ShortcutGraphObject extends GraphObject with IRIConstructionRules
{
    val instantiation: String = ""
    val appendToBind: String = ""
    val expansionRules: Array[ExpansionRule] = Array()
}