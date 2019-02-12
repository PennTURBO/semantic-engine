package edu.upenn.turbo

abstract class ExpandedGraphObject extends GraphObject
{
   val optionalPatterns: Array[ExpandedGraphObject]
   val mandatoryPatterns: Array[ExpandedGraphObject]
}