package edu.upenn.turbo

trait ConnectionRecipe 
{
    var subject: GraphPatternElement = null
    var crObject: GraphPatternElement = null
    
    var predicate: String = null 
    var cardinality: String = null
    var name: String = null
    
    var isOptional: Option[Boolean] = None
    var foundInGraph: Option[String] = None
    var predicateSuffixOperator: Option[String] = None
    var optionalGroup: Option[String] = None
    var minusGroup: Option[String] = None
}

class InstToInstConnRecipe(newSubject: Instance, newObject: Instance) extends ConnectionRecipe 
{
    subject = newSubject
    crObject = newObject
}

class InstToTermConnRecipe(newSubject: Instance, newObject: Term) extends ConnectionRecipe 
{
    subject = newSubject
    crObject = newObject
}

class TermToInstConnRecipe(newSubject: Term, newObject: Instance) extends ConnectionRecipe 
{
    subject = newSubject
    crObject = newObject
}

class InstToLitConnRecipe(newSubject: Instance, newObject: Literal) extends ConnectionRecipe 
{
    subject = newSubject
    crObject = newObject
}

class TermToLitConnRecipe(newSubject: Term, newObject: Literal) extends ConnectionRecipe 
{
    subject = newSubject
    crObject = newObject
}

class TermToTermConnRecipe(newSubject: Term, newObject: Term) extends ConnectionRecipe 
{
    subject = newSubject
    crObject = newObject
}