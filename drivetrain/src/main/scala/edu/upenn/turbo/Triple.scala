package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class Triple extends ProjectwideGlobals
{
    var tripleSubject: String = null
    var triplePredicate: String = null
    var tripleObject: String = null
    
    def this(subject: String, predicate: String, objectVar: String)
    {
        this
        setSubject(subject)
        setPredicate(predicate)
        setObject(objectVar)
    }
    
    def setSubject(subject: String)
    {
        helper.validateURI(subject)
        if (subject.contains('/'))
        {
            if (subject.charAt(0) != '<') this.tripleSubject = "<" + subject + ">"
            else this.tripleSubject = subject
        }
        else this.tripleSubject = subject
    }
    
    def setPredicate(predicate: String)
    {
        if (predicate == "a" || predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") this.triplePredicate = "rdf:type"
        else
        {
            helper.validateURI(predicate)
            if (predicate.contains('/')) this.triplePredicate = "<" + predicate + ">"
            else this.triplePredicate = predicate
        }
    }
    
    def setObject(objectVar: String)
    {
        if (objectVar.charAt(0) == '?')
        {
            helper.validateVariable(objectVar)
            this.tripleObject = objectVar
        }
        else if (!objectVar.contains(':') || objectVar.contains(' ')) 
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
    
    def makeTriple(): String = 
    {
        s"$tripleSubject $triplePredicate $tripleObject .\n"
    }
    
    def makeTripleWithVariables(withTypes: Boolean): String = 
    {
        val objectAsVar = helper.convertTypeToSparqlVariable(tripleObject)
        val subjectAsVar = helper.convertTypeToSparqlVariable(tripleSubject)
        s"$subjectAsVar $triplePredicate $objectAsVar .\n"
    }
    
    def makeTripleWithVariablesIfPreexisting(preexistingSet: HashSet[String]): String = 
    {
        var subjectForString = ""
        if (preexistingSet.contains(tripleSubject.replaceAll("\\<","").replaceAll("\\>","")))
        {
            subjectForString = helper.convertTypeToSparqlVariable(tripleSubject)
        }
        else subjectForString = tripleSubject
        
        var objectForString = ""
        if (!preexistingSet.contains(tripleObject.replaceAll("\\<","").replaceAll("\\>",""))) objectForString = tripleObject
        else 
        {
            objectForString = helper.convertTypeToSparqlVariable(tripleObject)
        }

        s"$subjectForString $triplePredicate $objectForString .\n"
    }
    
    def isSameAs(triple: Triple): Boolean =
    {
        if (triple.tripleSubject == this.tripleSubject 
            && triple.triplePredicate == this.triplePredicate
            && triple.tripleObject == this.tripleObject) true
        else false
    }
}