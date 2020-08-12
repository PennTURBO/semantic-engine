package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import java.util.LinkedHashSet
import java.util.LinkedHashMap

class RecipeGroupBuilder extends ProjectwideGlobals
{
    val typesUsed = new HashSet[String]

    def buildWhereGroup(requiredGroup: ConnectionRecipeGroup, optionalGroups: HashMap[String, HashSet[ConnectionRecipeGroup]], minusGroups: HashMap[String, HashSet[ConnectionRecipeGroup]]): String =
    {
        var subWhereClause = ""
        val requiredGroupGraphs = requiredGroup.recipesByGraph
        var whereClause = s"WHERE {\n $subWhereClause }\n"
        whereClause
    }
}