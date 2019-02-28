package edu.upenn.turbo

abstract class ExpandedGraphObjectSingleton extends GraphObjectSingleton
{
    def create(optional: Boolean): GraphObjectInstance
    val dataset: String
}