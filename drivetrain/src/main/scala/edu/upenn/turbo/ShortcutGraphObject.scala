package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

abstract class ShortcutGraphObject (instantiation: String = "", namedGraph: String = "", optional: Boolean = false) extends GraphObject with IRIConstructionRules
{
    var appendToBind: String = ""
    var expansionRules: Array[ExpansionRule] = Array()
    var instantiation = ""
}