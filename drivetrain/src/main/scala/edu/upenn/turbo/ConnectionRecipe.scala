package edu.upenn.turbo

trait ConnectionRecipe 
{
    var predicate: String = null 
    var cardinality: String = null
    var name: String = null
    
    var isOptional: Option[Boolean] = None
}

class InstToInstConnRecipe extends ConnectionRecipe 
{
    var subject: Instance = null
    var crObject: Instance = null
}

class InstToTermConnRecipe extends ConnectionRecipe 
{
    var subject: Instance = null
    var crObject: Term = null
}

class TermToInstConnRecipe extends ConnectionRecipe 
{
    var subject: Term = null
    var crObject: Instance = null
}

class InstToLitConnRecipe extends ConnectionRecipe 
{
    var subject: Instance = null
    var crObject: Literal = null
}

class TermToLitConnRecipe extends ConnectionRecipe 
{
    var subject: Term = null
    var crObject: Literal = null
}

class TermToTermConnRecipe extends ConnectionRecipe 
{
    var subject: Term = null
    var crObject: Term = null
}