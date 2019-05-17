package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet

class Triple extends ProjectwideGlobals
{
    var tripleSubject: String = null
    var triplePredicate: String = null
    var tripleObject: String = null
    var namedGraph: String = null
    
    var subjectHasType: Boolean = false
    var objectHasType: Boolean = false
    
    var required: Boolean = true
    
    def this(subject: String, predicate: String, objectVar: String, subjectHasType: Boolean, objectHasType: Boolean, namedGraph: String, required: Boolean = true)
    {
        this
        setSubject(subject)
        setPredicate(predicate)
        setObject(objectVar)
        setRequired(required)
        setSubjectHasType(subjectHasType)
        setObjectHasType(objectHasType)
        setNamedGraph(namedGraph)
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
    
    def setSubjectHasType(subjectHasType: Boolean)
    {
        this.subjectHasType = subjectHasType
    }
    
    def setObjectHasType(objectHasType: Boolean)
    {
        this.objectHasType = objectHasType
    }
    
    def setNamedGraph(namedGraph: String)
    {
        this.namedGraph = namedGraph
    }
    
    def makeTriple(): String = 
    {
        assert (namedGraph != null && namedGraph != "")
        val innerString = s"$getSubject $getPredicate $getObject .\n"
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
    
    def makeTripleWithVariables(withTypes: Boolean): String = 
    {
        assert (namedGraph != null && namedGraph != "")
        var objectAsVar = ""
        if (triplePredicate == "rdf:type" 
            || triplePredicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") objectAsVar = tripleObject
        else objectAsVar = helper.convertTypeToSparqlVariable(getObject)
        val subjectAsVar = helper.convertTypeToSparqlVariable(getSubject)
        var innerString = s"$subjectAsVar $getPredicate $objectAsVar .\n"
        if (withTypes)
        {
            if (subjectHasType) innerString += s"$subjectAsVar rdf:type $tripleSubject .\n"
            if (objectHasType) innerString += s"$objectAsVar rdf:type $tripleObject .\n"
        }
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
    
    def makeTripleWithVariablesIfPreexisting(preexistingSet: HashSet[String], withTypes: Boolean): String = 
    {
        assert (namedGraph != null && namedGraph != "")
        var subjectTypeClause = ""
        var objectTypeClause = ""
        var subjectForString = ""
        if (preexistingSet.contains(tripleSubject.replaceAll("\\<","").replaceAll("\\>","")))
        {
            subjectForString = helper.convertTypeToSparqlVariable(getSubject)
            if (withTypes && subjectHasType) subjectTypeClause += s"$subjectForString rdf:type $tripleSubject .\n"
        }
        else subjectForString = tripleSubject
        
        var objectForString = ""
        if (!preexistingSet.contains(tripleObject.replaceAll("\\<","").replaceAll("\\>","")) ||
            triplePredicate == "rdf:type" 
            || triplePredicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") objectForString = tripleObject
        else 
        {
            objectForString = helper.convertTypeToSparqlVariable(getObject)
            if (withTypes && objectHasType) objectTypeClause += s"$objectForString rdf:type $tripleObject .\n"
        }

        s"$subjectForString $getPredicate $objectForString .\n" + subjectTypeClause + objectTypeClause
    }
  
    def validateURI(uri: String)
    {
        val requiredCharacters: ArrayBuffer[Char] = ArrayBuffer(':')
        val illegalCharacters: ArrayBuffer[Char] = ArrayBuffer('<', '>', '"')
        for (char <- requiredCharacters) assert(uri.contains(char))
        for (char <- illegalCharacters) assert(!uri.contains(char))
    }
}