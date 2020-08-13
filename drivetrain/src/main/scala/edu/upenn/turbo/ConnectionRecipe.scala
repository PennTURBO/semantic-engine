package edu.upenn.turbo

trait ConnectionRecipe 
{
    var asSparql: String = null
    
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
    
    def addSparqlSnippet()
    {
        val snippetBuilder = new SparqlSnippetBuilder()
        asSparql = snippetBuilder.buildSnippet(this)
    }
}

class InstToInstConnRecipe(newSubject: Instance, newPredicate: String, newObject: Instance) extends ConnectionRecipe 
{
    subject = newSubject
    predicate = newPredicate
    crObject = newObject
}

class InstToTermConnRecipe(newSubject: Instance, newPredicate: String, newObject: Term) extends ConnectionRecipe 
{
    subject = newSubject
    predicate = newPredicate
    crObject = newObject
}

class TermToInstConnRecipe(newSubject: Term, newPredicate: String, newObject: Instance) extends ConnectionRecipe 
{
    subject = newSubject
    predicate = newPredicate
    crObject = newObject
}

class InstToLitConnRecipe(newSubject: Instance, newPredicate: String, newObject: Literal) extends ConnectionRecipe 
{
    subject = newSubject
    predicate = newPredicate
    crObject = newObject
}

class TermToLitConnRecipe(newSubject: Term, newPredicate: String, newObject: Literal) extends ConnectionRecipe 
{
    subject = newSubject
    predicate = newPredicate
    crObject = newObject
}

class TermToTermConnRecipe(newSubject: Term, newPredicate: String, newObject: Term) extends ConnectionRecipe 
{
    subject = newSubject
    predicate = newPredicate
    crObject = newObject
}