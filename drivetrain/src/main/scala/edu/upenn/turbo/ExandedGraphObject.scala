package edu.upenn.turbo

abstract class ExpandedGraphObject extends GraphObject
{
   val optionalLinks: Array[ExpandedGraphObject]
   val mandatoryLinks: Array[ExpandedGraphObject]
   val optionalPattern: String
}