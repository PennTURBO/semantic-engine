package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class Triple extends ProjectwideGlobals
{
    var tripleSubject: String = null
    var triplePredicate: String = null
    var tripleObject: String = null
    
    var subjectAType: Boolean = false
    var objectAType: Boolean = false
    var objectStatic: Boolean = false
    
    def this(subject: String, predicate: String, objectVar: String, subjectAType: Boolean, objectAType: Boolean, objectStatic: Boolean = false)
    {
        this
        setSubject(subject)
        setPredicate(predicate)
        setObject(objectVar)
        this.subjectAType = subjectAType
        this.objectAType = objectAType
        this.objectStatic = objectStatic
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
    
    def makeTripleWithVariables(): String = 
    {
        var objectAsVar: String = ""
        if (!objectStatic) objectAsVar = helper.convertTypeToSparqlVariable(tripleObject)
        else objectAsVar = tripleObject
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
        if (!preexistingSet.contains(tripleObject.replaceAll("\\<","").replaceAll("\\>","")) || objectStatic) objectForString = tripleObject
        else 
        {
            objectForString = helper.convertTypeToSparqlVariable(tripleObject)
        }

        s"$subjectForString $triplePredicate $objectForString .\n"
    }
    
    def makeSubjectTypeTriple(): String =
    {
        val subjectAsVar = helper.convertTypeToSparqlVariable(tripleSubject)
        s"$subjectAsVar rdf:type $tripleSubject .\n"
    }
    
    def makeObjectTypeTriple(): String =
    {
        val objectAsVar = helper.convertTypeToSparqlVariable(tripleObject)
        s"$objectAsVar rdf:type $tripleObject .\n"
    }
    
    def isSameAs(triple: Triple): Boolean =
    {
        if (triple.tripleSubject == this.tripleSubject 
            && triple.triplePredicate == this.triplePredicate
            && triple.tripleObject == this.tripleObject) true
        else false
    }
}