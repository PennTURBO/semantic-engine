package edu.upenn.turbo

import java.util.UUID
import scala.collection.mutable.ArrayBuffer

abstract class GraphObjectSingleton
{
    val pattern: String
    val baseVariableName: String
    val typeURI: String
    
    val optionalPatterns: Array[String] = Array()
    val variablesToSelect: Array[String]
}

abstract class GraphObjectInstance extends GraphObjectSingleton
{
    var optional: Boolean
    var namedGraph: String
}

trait ShortcutGraphObjectPropertiesForShortcutInstance extends GraphObjectSingleton with IRIConstructionRules
{
    val appendToBind: String = ""
    val expansionRules: Array[ExpansionRule]
    val whereTypesForExpansion: Array[GraphObjectSingleton]
    val insertTypesForExpansion: Array[GraphObjectSingleton]
    val optionalWhereTypesForExpansion: Array[GraphObjectSingleton]
}

trait ShortcutGraphObjectSingleton extends ShortcutGraphObjectPropertiesForShortcutInstance
{    
    def create(instantiation: String, namedGraph: String, globalUUID: String, optional: Boolean): ShortcutGraphObjectInstance
}

trait ShortcutGraphObjectInstance extends GraphObjectInstance with ShortcutGraphObjectPropertiesForShortcutInstance
{
    var instantiation: String
    var globalUUID: String
    
    def expand(): String =
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val queryBuilder = new QueryBuilder()
        
        var whereListAsInstances: ArrayBuffer[GraphObjectInstance] = new ArrayBuffer()
        var insertListAsInstances: ArrayBuffer[GraphObjectInstance] = new ArrayBuffer()
        
        for (a <- this.whereTypesForExpansion)
        {
            if (a.isInstanceOf[ShortcutGraphObjectSingleton])
            {
                val asInstance = a.asInstanceOf[ShortcutGraphObjectSingleton].create(instantiation, namedGraph, globalUUID, false)
                whereListAsInstances += asInstance
            }
            else if (a.isInstanceOf[ExpandedGraphObjectSingleton])
            {
                val asInstance = a.asInstanceOf[ExpandedGraphObjectSingleton].create(false)
                whereListAsInstances += asInstance
            }
        }
        
        for (a <- this.optionalWhereTypesForExpansion)
        {
            if (a.isInstanceOf[ShortcutGraphObjectSingleton])
            {
                val asInstance = a.asInstanceOf[ShortcutGraphObjectSingleton].create(instantiation, namedGraph, globalUUID, true)
                whereListAsInstances += asInstance
            }
            else if (a.isInstanceOf[ExpandedGraphObjectSingleton])
            {
                val asInstance = a.asInstanceOf[ExpandedGraphObjectSingleton].create(true)
                whereListAsInstances += asInstance
            }
        }
        
        for (a <- this.insertTypesForExpansion)
        {
            if (a.isInstanceOf[ShortcutGraphObjectSingleton])
            {
                val asInstance = a.asInstanceOf[ShortcutGraphObjectSingleton].create(instantiation, namedGraph, globalUUID, false)
                insertListAsInstances += asInstance
            }
            else if (a.isInstanceOf[ExpandedGraphObjectSingleton])
            {
                val asInstance = a.asInstanceOf[ExpandedGraphObjectSingleton].create(false)
                insertListAsInstances += asInstance
            }
        }
        
        val whereBuilderArgs = WhereBuilderQueryArgs.create(whereListAsInstances.toArray)
        
        queryBuilder.whereBuilder(whereBuilderArgs)
        queryBuilder.bindBuilder(Array(this), randomUUID, globalUUID)
        queryBuilder.insertBuilder(insertListAsInstances.toArray)
        
        queryBuilder.buildInsertQuery()
    }
}

trait DependentOptionalTrait 
{
    var dependent: String
}

abstract class ExpandedGraphObjectSingleton extends GraphObjectSingleton
{
    def create(optional: Boolean): GraphObjectInstance
}