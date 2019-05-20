package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

class Triple extends ProjectwideGlobals
{
    var tripleSubject: String = null
    var triplePredicate: String = null
    var tripleObject: String = null
    var namedGraph: String = null
    
    var subjectHasType: Boolean = false
    var objectHasType: Boolean = false
    
    var required: Boolean = true
    
    var optionalGroup: String = null
    
    def this(subject: String, predicate: String, objectVar: String, subjectHasType: Boolean, objectHasType: Boolean, namedGraph: String, required: Boolean = true)
    {
        this
        setSubject(subject)
        setPredicate(predicate)
        setObject(objectVar)
        this.required = required
        this.subjectHasType = subjectHasType
        this.objectHasType = objectHasType
        this.namedGraph = namedGraph
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
    
    def setOptionalGroup(optionalGroup: String)
    {
        this.optionalGroup = optionalGroup
    }
    
    def makeTriple(): String = 
    {
        assert (namedGraph != null && namedGraph != "")
        val innerString = s"$tripleSubject $triplePredicate $tripleObject .\n"
        if (!required)
        {
            s"OPTIONAL {\n $innerString }\n"
        }
        else innerString
    }
    
    def makeTripleWithVariables(withTypes: Boolean): HashMap[String, ArrayBuffer[String]] = 
    {
        assert (namedGraph != null && namedGraph != "")
        val objectAsVar = helper.convertTypeToSparqlVariable(tripleObject)
        val subjectAsVar = helper.convertTypeToSparqlVariable(tripleSubject)
        val tripleString = s"$subjectAsVar $triplePredicate $objectAsVar .\n"
        var res = HashMap(tripleString -> new ArrayBuffer[String]())
        if (withTypes)
        {
            if (subjectHasType) res(tripleString) += s"$subjectAsVar rdf:type $tripleSubject .\n"
            if (objectHasType) res(tripleString) += s"$objectAsVar rdf:type $tripleObject .\n"
        }
        res
    }
    
    def makeTripleWithVariablesIfPreexisting(preexistingSet: HashSet[String], withTypes: Boolean): HashMap[String, ArrayBuffer[String]] = 
    {
        assert (namedGraph != null && namedGraph != "")
        var subjectTypeClause = ""
        var objectTypeClause = ""
        var subjectForString = ""
        var res = new HashMap[String, ArrayBuffer[String]]
        var subjectTypeTriple: String = null
        if (preexistingSet.contains(tripleSubject.replaceAll("\\<","").replaceAll("\\>","")))
        {
            subjectForString = helper.convertTypeToSparqlVariable(tripleSubject)
            if (withTypes && subjectHasType) subjectTypeTriple = s"$subjectForString rdf:type $tripleSubject .\n"
        }
        else subjectForString = tripleSubject
        
        var objectForString = ""
        var objectTypeTriple: String = null
        if (!preexistingSet.contains(tripleObject.replaceAll("\\<","").replaceAll("\\>",""))) objectForString = tripleObject
        else 
        {
            objectForString = helper.convertTypeToSparqlVariable(tripleObject)
            if (withTypes && objectHasType) objectTypeTriple = s"$objectForString rdf:type $tripleObject .\n"
        }
        val tripleString = s"$subjectForString $triplePredicate $objectForString .\n"
        res += tripleString -> new ArrayBuffer[String]()
        if (subjectTypeTriple != null) res(tripleString) += subjectTypeTriple
        if (objectTypeTriple != null) res(tripleString) += objectTypeTriple
        res
    }
}