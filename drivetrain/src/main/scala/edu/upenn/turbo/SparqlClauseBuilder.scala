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
            var objectStatic = false
            
            var required = true
            if (rowResult(INPUTTYPE.toString).toString == "http://transformunify.org/ontologies/optionalInputTo") required = false
            if (rowResult(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = rowResult(GRAPHOFCREATINGPROCESS.toString).toString
            if (rowResult(GRAPHOFORIGIN.toString) != null) graphForThisRow = rowResult(GRAPHOFORIGIN.toString).toString
            if (rowResult(OPTIONALGROUP.toString) != null) optionalGroupForThisRow = rowResult(OPTIONALGROUP.toString).toString
            if (rowResult(MINUSGROUP.toString) != null) minusGroupForThisRow = rowResult(MINUSGROUP.toString).toString
            if (rowResult(OBJECTSTATIC.toString) == null && rowResult(CONNECTIONRECIPETYPE.toString).toString == "http://transformunify.org/ontologies/ObjectConnectionToClassRecipe") objectStatic = true
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
                                                     subjectAType, objectAType, objectStatic)
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
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], process: String, varsForProcessInput: HashSet[String], usedVariables: HashSet[String])
    {
        val triplesGroup = new TriplesGroupBuilder()
        var nonVariableClasses: HashSet[String] = new HashSet[String]
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
                                      subjectAType, objectAType, false, subjectContext, objectContext)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            if (usedVariables.contains(rowResult(SUBJECT.toString).toString))
            {
                val subjectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(SUBJECT.toString).toString, false, false, false, "", subjectContext)
                triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            }
            if (!objectIsLiteral && usedVariables.contains(rowResult(OBJECT.toString).toString) && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(OBJECT.toString).toString, false, false, false, "", objectContext)
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
    def buildBindClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], localUUID: String, process: String, usedVariables: HashSet[String])
    {
        var bindRules = new HashSet[String]
        var singletonClasses = new HashSet[String]
        var superSingletonClasses = new HashSet[String]
        var multiplicityCreators = new ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]
        var currentGroups = new HashSet[String]
        var oneToOneConnections = new HashMap[String, HashSet[String]]
        
        val multiplicityCreatorRules = Array("http://transformunify.org/ontologies/1-many", "http://transformunify.org/ontologies/many-1")
        for (row <- outputs)
        {
            if (row(CONNECTIONRECIPETYPE.toString).toString == "http://transformunify.org/ontologies/ObjectConnectionToInstanceRecipe")
            {
                if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/1-1")
                {
                    val thisSubject = row(SUBJECT.toString).toString
                    val thisObject = row(OBJECT.toString).toString
                    
                    currentGroups += thisSubject
                    currentGroups += thisObject
                    
                    if (oneToOneConnections.contains(thisObject) && oneToOneConnections.contains(thisSubject))
                    {
                        oneToOneConnections(thisObject) += thisSubject
                        oneToOneConnections(thisSubject) += thisObject
                        
                        val objectConnectionList = oneToOneConnections(thisObject)
                        val subjectConnectionList = oneToOneConnections(thisSubject)
                        
                        for (conn <- objectConnectionList)
                        {
                            for (a <- objectConnectionList)
                            {
                                if (a != conn) oneToOneConnections(conn) += a
                            }
                        }
                        for (conn <- subjectConnectionList)
                        {
                            for (a <- subjectConnectionList)
                            {
                                if (a != conn) oneToOneConnections(conn) += a
                            }
                        }
                    }
                    
                    else if (oneToOneConnections.contains(thisObject) && (!oneToOneConnections.contains(thisSubject)))
                    {
                        oneToOneConnections(thisObject) += thisSubject
                        oneToOneConnections.put(thisSubject, HashSet(thisObject))
                      
                        val objectConnectionList = oneToOneConnections(thisObject)
                        
                        for (conn <- objectConnectionList)
                        {
                            for (a <- objectConnectionList)
                            {
                                if (a != conn) oneToOneConnections(conn) += a
                            }
                        }
                    }
                    else if (oneToOneConnections.contains(thisSubject) && (!oneToOneConnections.contains(thisObject)))
                    {
                        oneToOneConnections(thisSubject) += thisObject
                        oneToOneConnections.put(thisObject, HashSet(thisSubject))
                      
                        val subjectConnectionList = oneToOneConnections(thisSubject)
                        
                        for (conn <- subjectConnectionList)
                        {
                            for (a <- subjectConnectionList)
                            {
                                if (a != conn) oneToOneConnections(conn) += a
                            }
                        }
                    }
                    else
                    {
                        oneToOneConnections.put(thisObject, HashSet(thisSubject))
                        oneToOneConnections.put(thisSubject, HashSet(thisObject))
                    }
              }
              else if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/many-singleton")
              {
                  singletonClasses += row(OBJECT.toString).toString
              }
              else if (row(MULTIPLICITY.toString).toString == "http://transformunify.org/ontologies/singleton-many")
              {
                  singletonClasses += row(SUBJECT.toString).toString
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
                  multiplicityCreators += row
              }
           }
        }
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
        for (multiplicityCreator <- multiplicityCreators)
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
            currentGroups.remove(changeAgent)
            if (!(usedVariables.contains(changeAgent)))
            {
                val changeAgentAsVar = helper.convertTypeToSparqlVariable(changeAgent)
                bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                         SHA256(CONCAT(\"${changeAgentAsVar}\",\"${localUUID}\", str(${changeAgentAsVar}))))) AS ${changeAgentAsVar})\n""" 
            }
            if (oneToOneConnections.contains(changeAgent))
            {
                for (conn <- oneToOneConnections(changeAgent))
                {
                    if (!(usedVariables.contains(conn)))
                    {
                      val changeAgentAsVar = helper.convertTypeToSparqlVariable(changeAgent)
                      val connAsVar = helper.convertTypeToSparqlVariable(conn)
                      bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                           SHA256(CONCAT(\"${connAsVar}\",\"${localUUID}\", str(${changeAgentAsVar}))))) AS ${connAsVar})\n"""
                    }
                    currentGroups.remove(conn)
                }
            }
        }
        var default: String = "default"
        for (conn <- currentGroups)
        {
            if (!(usedVariables.contains(conn)))
            {
                val connAsVar = helper.convertTypeToSparqlVariable(conn)
                val defaultMultiplier = "default"
                bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",
                         SHA256(CONCAT(\"${connAsVar}\",\"${localUUID}\", str(${defaultMultiplier}))))) AS ${connAsVar})\n"""
            }
            //VERY VERY VERY BAD!!!!!
            else if (Pattern.compile("[0-9]").matcher(conn).find())
            {
                default = helper.convertTypeToSparqlVariable(conn)
            }
        }
        for (a <- bindRules) clause += a
        clause = clause.replaceAll("default", default)
        
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