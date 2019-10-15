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
    
    var objectADescriber: Boolean = false
    var subjectADescriber: Boolean = false
    
    var objectALiteral: Boolean = false
    
    var subjectContext: String = ""
    var objectContext: String = ""
    
    def this(subject: String, predicate: String, objectVar: String, subjectAType: Boolean = false, 
            objectAType: Boolean = false, subjectADescriber: Boolean = false, objectADescriber: Boolean = false, subjectContext: String = "", 
            objectContext: String = "", objectALiteral: Boolean = false, predicateSuffixOperator: String = "")
    {
        this
        this.subjectAType = subjectAType
        this.objectAType = objectAType
        this.objectADescriber = objectADescriber
        this.subjectADescriber = subjectADescriber
        this.objectALiteral = objectALiteral
        
        this.subjectContext = helper.convertTypeToSparqlVariable(subjectContext)
        this.objectContext = helper.convertTypeToSparqlVariable(objectContext)
        if (this.subjectContext.size > 1) this.subjectContext = this.subjectContext.substring(1)
        if (this.objectContext.size > 1) this.objectContext = this.objectContext.substring(1)
        
        setSubject(subject)
        setPredicate(predicate, predicateSuffixOperator)
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
    
    def setPredicate(predicate: String, suffixOperator: String)
    {
        val allowedOperators = Array("*", "+")
        var formattedSuffixOperator = suffixOperator.split("\\^")(0).replaceAll("\"", "")
        if (!allowedOperators.contains(formattedSuffixOperator)) formattedSuffixOperator = ""
        if (predicate == "a" || predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") this.triplePredicate = "rdf:type" + formattedSuffixOperator
        else
        {
            helper.validateURI(predicate)
            if (predicate.contains('/')) this.triplePredicate = "<" + predicate + ">" + formattedSuffixOperator
            else this.triplePredicate = predicate + formattedSuffixOperator
        }
    }
    
    def setObject(objectVar: String)
    {
        if (objectALiteral) this.tripleObject = objectVar.split("\\^")(0)
        else
        {
            if (objectVar.charAt(0) == '?')
            {
                //helper.validateVariable(objectVar)
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
    }

    def getSubjectWithContext(): String =
    {
        var subjectWithContext = helper.removeAngleBracketsFromString(tripleSubject)
        if (subjectContext != "") subjectWithContext += "_" + subjectContext
        subjectWithContext
    }

    def getObjectWithContext(): String = 
    {
        var objectWithContext = helper.removeAngleBracketsFromString(tripleObject)
        if (objectContext != "") objectWithContext += "_" + objectContext
        objectWithContext
    }
    
    def makeTriple(): String = 
    {
        s"$tripleSubject $triplePredicate $tripleObject .\n"
    }
    
    def makeTripleWithVariables(): String = 
    {
        var objectAsVar = ""
        if (!objectADescriber && !objectAType || objectALiteral)
        {
            objectAsVar = tripleObject   
        }
        else
        {
            objectAsVar = helper.convertTypeToSparqlVariable(tripleObject)
            if (objectContext != "") objectAsVar += s"_$objectContext" 
        }
        
        var subjectAsVar = ""
        if (!subjectADescriber && !subjectAType)
        {
            subjectAsVar = tripleSubject   
        }
        else
        {
            subjectAsVar = helper.convertTypeToSparqlVariable(tripleSubject)
            if (subjectContext != "") subjectAsVar += s"_$subjectContext"   
        }
        
        s"$subjectAsVar $triplePredicate $objectAsVar .\n"
    }
    
    def makeTripleWithVariablesExcludeList(excludeList: HashSet[String]): String = 
    {
        var subjectForString = ""
        if (!excludeList.contains(tripleSubject.replaceAll("\\<","").replaceAll("\\>","")))
        {
            subjectForString = helper.convertTypeToSparqlVariable(tripleSubject)
            if (subjectContext != "") subjectForString += s"_$subjectContext"
        }
        else subjectForString = tripleSubject
        
        var objectForString = ""
        if (excludeList.contains(tripleObject.replaceAll("\\<","").replaceAll("\\>",""))) objectForString = tripleObject
        else 
        {
            if (!objectALiteral) 
            {
                objectForString = helper.convertTypeToSparqlVariable(tripleObject)
                if (objectContext != "") objectForString += s"_$objectContext"
            }
            else objectForString = tripleObject
        }

        s"$subjectForString $triplePredicate $objectForString .\n"
    }
    
    def makeSubjectTypeTriple(): String =
    {
        var subjectAsVar = helper.convertTypeToSparqlVariable(tripleSubject)
        if (subjectContext != "") subjectAsVar += s"_$subjectContext"
        s"$subjectAsVar rdf:type $tripleSubject .\n"
    }
    
    def makeObjectTypeTriple(): String =
    {
        var objectAsVar = helper.convertTypeToSparqlVariable(tripleObject)
        if (objectContext != "") objectAsVar += s"_$objectContext"
        s"$objectAsVar rdf:type $tripleObject .\n"
    }
    
    def isSameAs(triple: Triple): Boolean =
    {
        if (triple.tripleSubject == this.tripleSubject 
            && triple.triplePredicate == this.triplePredicate
            && triple.tripleObject == this.tripleObject
            && triple.subjectContext == this.subjectContext
            && triple.objectContext == this.objectContext) true
        else false
    }
}