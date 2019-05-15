package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer

class Triple extends ProjectwideGlobals
{
    var tripleSubject: String = null
    var triplePredicate: String = null
    var tripleObject: String = null
    
    var required: Boolean = false
    
    def this(subject: String, predicate: String, objectVar: String, required: Boolean = true)
    {
        this
        setSubject(subject)
        setPredicate(predicate)
        setObject(objectVar)
        setRequired(required)
    }
    
    def setSubject(subject: String)
    {
        validateURI(subject)
        if (subject.contains('/')) this.tripleSubject = "<" + subject + ">"
        else this.tripleSubject = subject
    }
    
    def getSubject(): String = tripleSubject
    
    def setPredicate(predicate: String)
    {
        validateURI(predicate)
        if (predicate.contains('/')) this.triplePredicate = "<" + predicate + ">"
        else this.triplePredicate = predicate
    }
    
    def getPredicate: String = triplePredicate
    
    def setObject(objectVar: String)
    {
        if (!objectVar.contains(':') || objectVar.contains(' ')) this.tripleObject = "\"" + objectVar + "\""
        else if (objectVar.contains('/')) this.tripleObject = "<" + objectVar + ">"
        else this.tripleObject = objectVar
    }
    
    def getObject: String = tripleObject
    
    def setRequired(optional: Boolean)
    {
        this.required = required
    }
    
    def getRequired(): Boolean = required
    
    def makeTriple(): String = 
    {
        val innerString = "$getSubject $getPredicate $getObject .\n"
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
    
    def makeTripleWithVariables(): String = 
    {
        var objectAsVar = ""
        if (triplePredicate == "a" 
            || triplePredicate == "rdf:type" 
            || triplePredicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") objectAsVar = tripleObject
        else objectAsVar = helper.convertTypeToSparqlVariable(getObject)
        val subjectAsVar = helper.convertTypeToSparqlVariable(getSubject)
        val innerString = "$subjectAsVar $getPredicate $objectAsVar .\n"
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
  
    def validateURI(uri: String)
    {
        val requiredCharacters: ArrayBuffer[CharSequence] = ArrayBuffer("\\:")
        val illegalCharacters: ArrayBuffer[CharSequence] = ArrayBuffer("\\<", "\\>", "\"")
        for (char <- requiredCharacters) assert(tripleSubject.contains(char))
        for (char <- illegalCharacters) assert(!tripleSubject.contains(char))
    }
}