package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class Triple extends ProjectwideGlobals
{
    var tripleSubject: String = null
    var triplePredicate: String = null
    var tripleObject: String = null
    
    var required: Boolean = true
    
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
        if (subject.contains('/'))
        {
            if (subject.charAt(0) != '<') this.tripleSubject = "<" + subject + ">"
            else this.tripleSubject = subject
        }
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
        if (!objectVar.contains(':') || objectVar.contains(' ')) 
        {
            if (objectVar.contains("\n")) this.tripleObject = s"'''$objectVar'''"
            else this.tripleObject = "\"" + objectVar + "\""
        }
        else if (objectVar.contains('/')) 
        {
            if (objectVar.charAt(0) != '<') this.tripleObject = "<" + objectVar + ">"
            else this.tripleObject = objectVar
        }
        else this.tripleObject = objectVar
    }
    
    def getObject: String = tripleObject
    
    def setRequired(required: Boolean)
    {
        this.required = required
    }
    
    def getRequired(): Boolean = required
    
    def makeTriple(): String = 
    {
        val innerString = s"$getSubject $getPredicate $getObject .\n"
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
    
    def makeTripleWithVariables(): String = 
    {
        var objectAsVar = ""
        if (triplePredicate == "rdf:type" 
            || triplePredicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") objectAsVar = tripleObject
        else objectAsVar = helper.convertTypeToSparqlVariable(getObject)
        val subjectAsVar = helper.convertTypeToSparqlVariable(getSubject)
        val innerString = s"$subjectAsVar $getPredicate $objectAsVar .\n"
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
    
    def makeTripleWithVariablesIfPreexisting(preexistingSet: HashSet[String]): String = 
    {
        var subjectForString = ""
        if (preexistingSet.contains(tripleSubject.replaceAll("\\<","").replaceAll("\\>",""))) subjectForString = helper.convertTypeToSparqlVariable(getSubject)
        else subjectForString = tripleSubject
        var objectForString = ""
        if (!preexistingSet.contains(tripleObject.replaceAll("\\<","").replaceAll("\\>","")) ||
            triplePredicate == "rdf:type" 
            || triplePredicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") objectForString = tripleObject
        else objectForString = helper.convertTypeToSparqlVariable(getObject)

        s"$subjectForString $getPredicate $objectForString .\n"
    }
  
    def validateURI(uri: String)
    {
        val requiredCharacters: ArrayBuffer[Char] = ArrayBuffer(':')
        val illegalCharacters: ArrayBuffer[Char] = ArrayBuffer('<', '>', '"')
        for (char <- requiredCharacters) assert(uri.contains(char))
        for (char <- illegalCharacters) assert(!uri.contains(char))
    }
}