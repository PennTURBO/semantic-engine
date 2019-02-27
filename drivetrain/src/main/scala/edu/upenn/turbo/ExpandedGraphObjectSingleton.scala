package edu.upenn.turbo

abstract class ExpandedGraphObjectSingleton extends GraphObjectSingleton
{
    var namedGraph: String
    def create(optional: Boolean): GraphObjectInstance
}