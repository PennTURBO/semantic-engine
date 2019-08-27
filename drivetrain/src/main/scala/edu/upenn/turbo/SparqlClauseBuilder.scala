package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern

abstract class SparqlClauseBuilder extends ProjectwideGlobals
{
    var clause: String = ""
}

class WhereClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultInputGraph: String): HashSet[String] =
    {
        var varsForProcessInput = new HashSet[String]
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- inputs)
        {
            for (key <- requiredInputKeysList) assert (rowResult.contains(key.toString))
            
            var graphForThisRow: String = defaultInputGraph
            var optionalGroupForThisRow: String = null
            var minusGroupForThisRow: String = null
            var subjectAType = false
            var objectAType = false
            
            var required = true
            if (rowResult(INPUTTYPE.toString).toString == "http://transformunify.org/ontologies/optionalInputTo") required = false
            if (rowResult(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = rowResult(GRAPHOFCREATINGPROCESS.toString).toString
            if (rowResult(GRAPHOFORIGIN.toString) != null) graphForThisRow = rowResult(GRAPHOFORIGIN.toString).toString
            if (rowResult(OPTIONALGROUP.toString) != null) optionalGroupForThisRow = rowResult(OPTIONALGROUP.toString).toString
            if (rowResult(MINUSGROUP.toString) != null) minusGroupForThisRow = rowResult(MINUSGROUP.toString).toString
            if (rowResult(SUBJECTTYPE.toString) != null) 
            {
                subjectAType = true
                varsForProcessInput += rowResult(SUBJECT.toString).toString
            }
            if (rowResult(OBJECTTYPE.toString) != null) 
            {
                objectAType = true
                varsForProcessInput += rowResult(OBJECT.toString).toString
            }
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString,
                                                     subjectAType, objectAType)
            if (minusGroupForThisRow != null)
            {
                triplesGroup.addTripleToMinusGroup(newTriple, graphForThisRow, minusGroupForThisRow)
            }
            else if (required && optionalGroupForThisRow == null)
            {
                triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
            }
            else if (optionalGroupForThisRow != null)
            {
                triplesGroup.addToOptionalGroup(newTriple, graphForThisRow, optionalGroupForThisRow, required)
            }
            else
            {
                triplesGroup.addOptionalTripleToRequiredGroup(newTriple, graphForThisRow)
            }
        }
        clause = triplesGroup.buildWhereClauseFromTriplesGroup()
        varsForProcessInput
    }
}

class InsertClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], process: String, varsForProcessInput: HashSet[String], usedVariables: HashMap[String, Boolean])
    {
        val triplesGroup = new TriplesGroupBuilder()
        var nonVariableClasses: HashSet[String] = new HashSet[String]
        
        // this ensures that the process will be represented as a static URI, not a variable
        nonVariableClasses += process
        
        for (rowResult <- outputs)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key.toString))
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(processNamedGraph)
            
            var subjectAType = false
            var objectAType = false
            var subjectContext: String = ""
            var objectContext: String = ""
            
            if (rowResult(SUBJECTTYPE.toString) != null) subjectAType = true
            if (rowResult(OBJECTTYPE.toString) != null) objectAType = true
            if (rowResult(SUBJECTCONTEXT.toString) != null) subjectContext = rowResult(SUBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTCONTEXT.toString) != null) objectContext = rowResult(OBJECTCONTEXT.toString).toString
            
            var objectIsLiteral = false
            if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") objectIsLiteral = true
            if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/ObjectConnectionToClassRecipe") 
            {
                objectAType = false
                if (!usedVariables.contains(rowResult(OBJECT.toString).toString)) nonVariableClasses += rowResult(OBJECT.toString).toString
            }
            val graph = rowResult(GRAPH.toString).toString
            
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                      subjectAType, objectAType, subjectContext, objectContext)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            if (usedVariables.contains(rowResult(SUBJECT.toString).toString))
            {
                val subjectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(SUBJECT.toString).toString, false, false, "", subjectContext)
                triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            }
            if (!objectIsLiteral && usedVariables.contains(rowResult(OBJECT.toString).toString) && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(OBJECT.toString).toString, false, false, "", objectContext)
                triplesGroup.addRequiredTripleToRequiredGroup(objectProcessTriple, processNamedGraph)
            }
        }
        for (uri <- varsForProcessInput)
        {
            val processInputTriple = new Triple(process, "obo:OBI_0000293", uri, false, false)
            triplesGroup.addRequiredTripleToRequiredGroup(processInputTriple, processNamedGraph)
        }
        clause = triplesGroup.buildInsertClauseFromTriplesGroup(nonVariableClasses)
    }
}

class DeleteClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def addTripleFromRowResult(removals: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultRemovalsGraph: String)
    {
        val triplesGroup = new TriplesGroupBuilder()
        for (rowResult <- removals)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key.toString))
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(defaultRemovalsGraph)
        
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                  false, false)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, defaultRemovalsGraph)
            
            clause = triplesGroup.buildDeleteClauseFromTriplesGroup()
        }
    }
}

class BindClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    var bindRules = new HashSet[String]
    var singletonClasses = new HashSet[String]
    var superSingletonClasses = new HashSet[String]
    var multiplicityCreators = new HashSet[String]
    var currentGroups = new HashMap[String, HashMap[String, org.eclipse.rdf4j.model.Value]]
    var oneToOneConnections = new HashMap[String, HashSet[String]]
    var usedVariables = new HashMap[String, Boolean]
    
    var process: String = ""
    
    val multiplicityCreatorRules = Array("http://transformunify.org/ontologies/1-many", "http://transformunify.org/ontologies/many-1")
    val objToInstRecipe = "http://transformunify.org/ontologies/ObjectConnectionToInstanceRecipe"
    
    def buildBindClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], localUUID: String, process: String, usedVariables: HashMap[String, Boolean]): HashMap[String, Boolean] =
    {   
        this.process = process
        this.usedVariables = usedVariables
        val newUsedVariables = populateConnectionLists(outputs)
        buildSingletonBindClauses(localUUID)
        buildBaseGroupBindClauses(localUUID)
        assert (multiplicityCreators.size == 0, s"Error in graph model: For process $process, there is not sufficient context to create the following: $multiplicityCreators")
        for (a <- bindRules) clause += a
        newUsedVariables
    }
    
    def populateConnectionLists(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]): HashMap[String, Boolean] =
    {
        var variablesToBind = new HashMap[String,Boolean]
        var discoveredMap = new HashMap[String, String]
        for (row <- outputs)
        {
            val subjectString = row(OBJECT.toString).toString
            val objectString = row(SUBJECT.toString).toString
            val discoveryCode = subjectString + objectString
            if (discoveredMap.contains(discoveryCode)) assert(discoveredMap(discoveryCode) == row(MULTIPLICITY.toString).toString, s"There are multiple connections between $subjectString and $objectString with non-matching multiplicities")
            else discoveredMap += discoveryCode -> row(MULTIPLICITY.toString).toString
            if (row(CONNECTIONRECIPETYPE.toString).toString == objToInstRecipe || row(SUBJECTADESCRIBER.toString) != null || row(OBJECTADESCRIBER.toString) != null) 
            {  
                if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/1-1") handleOneToOneConnection(row)
                else if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/many-singleton")
                {
                    singletonClasses += row(OBJECT.toString).toString
                }
                else if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/singleton-many")
                {
                    singletonClasses += row(SUBJECT.toString).toString
                }
                  else if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/singleton-singleton")
                {
                    singletonClasses += row(SUBJECT.toString).toString
                    singletonClasses += row(OBJECT.toString).toString
                }
                else if (row(MULTIPLICITY.toString).toString.endsWith("superSingleton"))
                {
                    superSingletonClasses += row(OBJECT.toString).toString
                }
                else if (row(MULTIPLICITY.toString).toString.contains("superSingleton"))
                {
                    superSingletonClasses += row(SUBJECT.toString).toString
                }
                else if (multiplicityCreatorRules.contains(row(MULTIPLICITY.toString).toString))
                {
                    val ca = findChangeAgent(row)
                    if (!usedVariables.contains(ca)) multiplicityCreators += ca
                }
                val obj = row(OBJECT.toString).toString
                val subj = row(SUBJECT.toString).toString
                var subjAType = false
                if (row(SUBJECTTYPE.toString) != null) subjAType = true
                var objAType = false
                if (row(OBJECTTYPE.toString) != null) objAType = true
                variablesToBind += obj -> objAType
                variablesToBind += subj -> subjAType
           }
        }
        validateConnectionLists()
        variablesToBind
    }
    
    def validateConnectionLists()
    {
        for ((k,v) <- oneToOneConnections)
        {
            assert (!singletonClasses.contains(k), s"For process $process, $k has a 1-1 relationship and is also considered a Singleton")
            assert (!superSingletonClasses.contains(k), s"For process $process, $k has a 1-1 relationship and is also considered a SuperSingleton")
        }
    }
    
    def addToConnectionList(element: String)
    {
        val connectionList = oneToOneConnections(element)
        for (conn <- connectionList)
        {
            for (a <- connectionList)
            {
                oneToOneConnections(conn) += a
            }
        }   
    }
    
    def handleOneToOneConnection(row: HashMap[String, org.eclipse.rdf4j.model.Value])
    {
        val thisSubject = row(SUBJECT.toString).toString
        val thisObject = row(OBJECT.toString).toString
        
        val specialSubjectRule = row(SUBJECTRULE.toString)
        val specialObjectRule = row(OBJECTRULE.toString)
        
        val subjectDependent = row(SUBJECTDEPENDEE.toString)
        val objectDependent = row(OBJECTDEPENDEE.toString)
        
        if (!currentGroups.contains(thisSubject)) currentGroups += thisSubject -> HashMap("customRule" -> specialSubjectRule, "dependee" -> subjectDependent)
        else 
        {
            if (currentGroups(thisSubject)("dependee") != null && subjectDependent != null) assert (currentGroups(thisSubject)("dependee") == subjectDependent)
            if (currentGroups(thisSubject)("customRule") != null && specialSubjectRule != null) assert (currentGroups(thisSubject)("customRule") == specialSubjectRule)
            if (currentGroups(thisSubject)("dependee") == null) currentGroups(thisSubject)("dependee") = subjectDependent
            if (currentGroups(thisSubject)("customRule") == null) currentGroups(thisSubject)("customRule") = specialSubjectRule
        }
        if (!currentGroups.contains(thisObject)) currentGroups += thisObject -> HashMap("customRule" -> specialObjectRule, "dependee" -> objectDependent)
        else 
        {
            if (currentGroups(thisObject)("dependee") != null && objectDependent != null) assert (currentGroups(thisObject)("dependee") == objectDependent)
            if (currentGroups(thisObject)("customRule") != null && specialObjectRule != null) assert (currentGroups(thisObject)("customRule") == specialObjectRule)
            if (currentGroups(thisObject)("dependee") == null) currentGroups(thisObject)("dependee") = objectDependent
            if (currentGroups(thisObject)("customRule") == null) currentGroups(thisObject)("customRule") = specialObjectRule
        }
        
        if (oneToOneConnections.contains(thisObject) && oneToOneConnections.contains(thisSubject))
        {
            oneToOneConnections(thisObject) += thisSubject
            oneToOneConnections(thisSubject) += thisObject
            oneToOneConnections(thisObject) += thisObject
            oneToOneConnections(thisSubject) += thisSubject
            
            addToConnectionList(thisSubject)
            addToConnectionList(thisObject)
        }
        
        else if (oneToOneConnections.contains(thisObject) && (!oneToOneConnections.contains(thisSubject)))
        {
            oneToOneConnections(thisObject) += thisSubject
            oneToOneConnections(thisObject) += thisObject
            oneToOneConnections.put(thisSubject, HashSet(thisObject))
            oneToOneConnections(thisSubject) += thisSubject
          
            addToConnectionList(thisObject)
        }
        else if (oneToOneConnections.contains(thisSubject) && (!oneToOneConnections.contains(thisObject)))
        {
            oneToOneConnections(thisSubject) += thisObject
            oneToOneConnections(thisSubject) += thisSubject
            oneToOneConnections.put(thisObject, HashSet(thisSubject))
            oneToOneConnections(thisObject) += thisObject
          
            addToConnectionList(thisSubject)
        }
        else
        {
            oneToOneConnections.put(thisObject, HashSet(thisSubject))
            oneToOneConnections.put(thisSubject, HashSet(thisObject))
            oneToOneConnections(thisObject) += thisObject
            oneToOneConnections(thisSubject) += thisSubject
        }
    }
    
    def buildSingletonBindClauses(localUUID: String)
    {
        for (singleton <- singletonClasses)
        {
            if (!(usedVariables.contains(singleton)))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                        SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\",\"${process}")))) AS ${singletonAsVar})\n""" 
            }
        }
        for (singleton <- superSingletonClasses)
        {
            if (!(usedVariables.contains(singleton)))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                            SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\")))) AS ${singletonAsVar})\n"""
            }
        }
    }
    
    def findChangeAgent(multiplicityCreator: HashMap[String, org.eclipse.rdf4j.model.Value]): String =
    {
        var changeAgent: String = ""
        if (multiplicityCreator(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/1-many")
        {
            changeAgent = multiplicityCreator(OBJECT.toString).toString   
        }
        if (multiplicityCreator(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/many-1")
        {
            changeAgent = multiplicityCreator(SUBJECT.toString).toString
        }
        assert (changeAgent != "")
        changeAgent
    }
    
    def getMultiplicityEnforcer(changeAgent: String): String =
    {
        var multiplicityEnforcer = ""
        assert (oneToOneConnections.contains(changeAgent))
        for (conn <- oneToOneConnections(changeAgent))
        {
            if (usedVariables.contains(conn) && usedVariables(conn)) 
            {
                val newEnforcer = helper.convertTypeToSparqlVariable(conn)
                assert (multiplicityEnforcer == "", s"Error in graph model: Multiple possible multiplicity enforcers for $changeAgent in process $process: $multiplicityEnforcer, $newEnforcer")
                multiplicityEnforcer = newEnforcer
            }
        }  
        assert (multiplicityEnforcer != "")
        multiplicityEnforcer
    }
    
    def buildBaseGroupBindClauses(localUUID: String)
    {
        for ((k,v) <- currentGroups)
        {
            if (!(usedVariables.contains(k)))
            {
                val multiplicityEnforcer = getMultiplicityEnforcer(k)
                val connAsVar = helper.convertTypeToSparqlVariable(k)
                addBindRule(v, localUUID, multiplicityEnforcer, connAsVar)
                multiplicityCreators.remove(k)
            }
        }
    }
    
    def addBindRule(v: HashMap[String, org.eclipse.rdf4j.model.Value], localUUID: String, multiplicityEnforcer: String, connAsVar: String)
    {
        if (v("customRule") == null)
        {
            if (v("dependee") == null)
              {
                  addStandardBindRule(connAsVar, localUUID, multiplicityEnforcer)  
              }
              else
              {
                  val dependee = helper.convertTypeToSparqlVariable(v("dependee"))
                  addDependentBindRule(connAsVar, localUUID, multiplicityEnforcer, dependee)
              }
        }
        else
        {
             var customRule = helper.removeQuotesFromString(v("customRule").toString.split("\\^")(0))+"\n"
             if (v("dependee") != null && customRule.contains("dependent"))
             {
                 val dependee = helper.convertTypeToSparqlVariable(v("dependee"))
                 customRule = customRule.replaceAll("\\$\\{dependent\\}", dependee)
             }
             customRule = customRule.replaceAll("\\$\\{replacement\\}", connAsVar)
             customRule = customRule.replaceAll("\\$\\{localUUID\\}", localUUID)
             customRule = customRule.replaceAll("\\$\\{multiplicityEnforcer\\}", multiplicityEnforcer)
             assert (!customRule.contains("replacement"))
             assert (!customRule.contains("dependent"))
             assert (!customRule.contains("localUUID"))
             assert (!customRule.contains("multiplicityEnforcer"))
             bindRules += customRule
        }
    }
    
    def addStandardBindRule(newNode: String, localUUID: String, multiplicityEnforcer: String)
    {   
        if (newNode != "")
        {
            var newNodeAsVar = newNode
            var multiplicityEnforcerAsVar = multiplicityEnforcer
            if (!newNode.contains('?'))
            {
                newNodeAsVar = helper.convertTypeToSparqlVariable(newNode)
            }
            if (!multiplicityEnforcer.contains('?'))
            {
                multiplicityEnforcerAsVar = helper.convertTypeToSparqlVariable(multiplicityEnforcer)
            }
    
            bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                 SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))) AS ${newNodeAsVar})\n"""   
        }
    }
    
    def addDependentBindRule(newNode: String, localUUID: String, multiplicityEnforcer: String, dependee: String)
    {
        if (newNode != "")
        {
            var newNodeAsVar = newNode
            var multiplicityEnforcerAsVar = multiplicityEnforcer
            var dependeeAsVar = dependee
            if (!newNode.contains('?'))
            {
                newNodeAsVar = helper.convertTypeToSparqlVariable(newNode)
            }
            if (!multiplicityEnforcer.contains('?'))
            {
                multiplicityEnforcerAsVar = helper.convertTypeToSparqlVariable(multiplicityEnforcer)
            }
            if (!dependee.contains('?'))
            {
                dependeeAsVar = helper.convertTypeToSparqlVariable(dependee)
            }
            
            bindRules += s"""BIND(IF (BOUND(${dependeeAsVar}), uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                       SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))), ?unbound) AS ${newNodeAsVar})\n"""   
        }
    }
}

class InsertDataClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def buildInsertDataClauseFromTriplesList(triplesList: ArrayBuffer[Triple], graph: String)
    {
        val triplesGroup = new TriplesGroupBuilder()
        for (triple <- triplesList) triplesGroup.addRequiredTripleToRequiredGroup(triple, graph)
        clause = triplesGroup.buildInsertDataClauseFromTriplesGroup()
    }
}