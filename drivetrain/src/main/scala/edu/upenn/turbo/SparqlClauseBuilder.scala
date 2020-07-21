package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection

abstract class SparqlClauseBuilder extends ProjectwideGlobals
{
    var clause: String = ""
}

class WhereClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    val triplesGroup = new TriplesGroupBuilder()
    var varsForProcessInput = new HashSet[Triple]
    var valuesBlock: HashMap[String, String] = new HashMap[String, String]
    var nonDefaultGraphTriples = new ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]
    var process: String = ""
    
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    def setProcess(process: String)
    {
        this.process = process
    }
    
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultInputGraph: String, process: String): HashSet[Triple] =
    {
        assert (process != "")
        setProcess(process)
        for (rowResult <- inputs)
        {
            for (key <- requiredInputKeysList) assert (rowResult.contains(key.toString), s"Input data does not contian required key $key")
            
            var graphForThisRow: String = defaultInputGraph
            if (rowResult(GRAPHOFORIGIN.toString) != null) graphForThisRow = rowResult(GRAPHOFORIGIN.toString).toString
            if (rowResult(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = rowResult(GRAPHOFCREATINGPROCESS.toString).toString
            if (graphForThisRow != defaultInputGraph) nonDefaultGraphTriples += rowResult
            else makeNewTripleFromRowResult(rowResult, defaultInputGraph)
        }
        for (row <- nonDefaultGraphTriples) makeNewTripleFromRowResult(row, defaultInputGraph)
        
        triplesGroup.setValuesBlock(valuesBlock)
        clause = triplesGroup.buildWhereClauseFromTriplesGroup()
        varsForProcessInput
    }
    
    def makeNewTripleFromRowResult(rowResult: HashMap[String, org.eclipse.rdf4j.model.Value], defaultInputGraph: String)
    {
        var optionalGroupForThisRow: String = null
        var minusGroupForThisRow: String = null
        
        var subjectAnInstance = false
        var objectAnInstance = false
        
        var objectADescriber = false
        var subjectADescriber = false
        
        var objectALiteralValue = false
        var objectADefinedLiteral = false
        
        var subjectContext: String = ""
        var objectContext: String = ""
        
        var suffixOperator = ""
        if (rowResult(SUFFIXOPERATOR.toString) != null) suffixOperator = rowResult(SUFFIXOPERATOR.toString).toString
     
        if (rowResult(OBJECTALITERALVALUE.toString).toString.contains("true")) objectALiteralValue = true
        if (rowResult(OBJECTALITERAL.toString) != null) objectADefinedLiteral = true
        
        if (rowResult(SUBJECTCONTEXT.toString) != null) subjectContext = rowResult(SUBJECTCONTEXT.toString).toString
        if (rowResult(OBJECTCONTEXT.toString) != null) objectContext = rowResult(OBJECTCONTEXT.toString).toString
        
        var graphForThisRow: String = defaultInputGraph
        if (rowResult(GRAPHOFORIGIN.toString) != null) graphForThisRow = rowResult(GRAPHOFORIGIN.toString).toString
        if (rowResult(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = rowResult(GRAPHOFCREATINGPROCESS.toString).toString
        val connectionName = rowResult(CONNECTIONNAME.toString).toString
        
        var required = true
        if (rowResult(INPUTTYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/hasOptionalInput") required = false
        if (rowResult(OPTIONALGROUP.toString) != null) optionalGroupForThisRow = rowResult(OPTIONALGROUP.toString).toString
        if (rowResult(MINUSGROUP.toString) != null) minusGroupForThisRow = rowResult(MINUSGROUP.toString).toString
        if (rowResult(OBJECTADESCRIBER.toString) != null) 
        {
            objectADescriber = true
            val valsAsString = helper.getDescriberRangesAsString(gmCxn, rowResult(OBJECT.toString).toString)
            if (valsAsString.size > 0) valuesBlock += rowResult(OBJECT.toString).toString -> valsAsString
        }
        if (rowResult(SUBJECTADESCRIBER.toString) != null) 
        {
            subjectADescriber = true
            val valsAsString = helper.getDescriberRangesAsString(gmCxn, rowResult(SUBJECT.toString).toString)
            if (valsAsString.size > 0) valuesBlock += rowResult(SUBJECT.toString).toString -> valsAsString
        }
        if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/InstanceToLiteralRecipe") 
        {
            assert (objectALiteralValue || objectADefinedLiteral, s"The object of connection $connectionName is not a literal, but the connection is a datatype connection.")
            objectADescriber = true
            subjectAnInstance = true
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe") 
        {
            assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type InstanceToTermRecipe")
            subjectAnInstance = true
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe") 
        {
            assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type TermToInstanceRecipe")
            objectAnInstance = true
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
        {
            assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type InstanceToInstanceRecipe")
            objectAnInstance = true
            subjectAnInstance = true
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/TermToTermRecipe")
        {
            assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type TermToTermRecipe")
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/TermToLiteralRecipe")
        {
            assert (objectALiteralValue || objectADefinedLiteral, s"The object of connection $connectionName is not a literal, but the connection is a datatype connection.")
            objectADescriber = true
        }
        val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString,
                                                 subjectAnInstance, objectAnInstance, subjectADescriber, objectADescriber, subjectContext, objectContext, objectALiteralValue, suffixOperator)
        varsForProcessInput += new Triple(process, "obo:OBI_0000293", rowResult(SUBJECT.toString).toString, false, false, false, (subjectADescriber || subjectAnInstance), "", subjectContext)
        if (!objectALiteralValue && !objectADefinedLiteral)
        {
            varsForProcessInput += new Triple(process, "obo:OBI_0000293", rowResult(OBJECT.toString).toString, false, false, false, (objectADescriber || objectAnInstance), "", objectContext)
        }
        graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(graphForThisRow)
        addNewTripleToGroup(newTriple, minusGroupForThisRow, optionalGroupForThisRow, required, graphForThisRow)
    }
    
    def addNewTripleToGroup(newTriple: Triple, minusGroupForThisRow: String, optionalGroupForThisRow: String, required: Boolean, graphForThisRow: String)
    {
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
}

class InsertClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], process: String, varsForProcessInput: HashSet[Triple], usedVariables: HashMap[String, Boolean])
    {
        val triplesGroup = new TriplesGroupBuilder()
        
        for (rowResult <- outputs)
        {
            for (key <- requiredOutputKeysList) assert (rowResult.contains(key.toString))
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(processNamedGraph)
            
            val connectionName = rowResult(CONNECTIONNAME.toString).toString
            
            var subjectAnInstance = false
            var objectAnInstance = false
            
            var subjectContext: String = ""
            var objectContext: String = ""
            
            var objectADescriber = false
            var subjectADescriber = false
            
            var objectALiteralValue = false
            var objectADefinedLiteral = false
            
            var thisSubject = rowResult(SUBJECT.toString).toString
            var thisObject = rowResult(OBJECT.toString).toString
     
            if (rowResult(OBJECTALITERALVALUE.toString).toString.contains("true")) objectALiteralValue = true
            if (rowResult(OBJECTALITERAL.toString) != null) objectADefinedLiteral = true
            
            if (rowResult(SUBJECTCONTEXT.toString) != null) subjectContext = rowResult(SUBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTCONTEXT.toString) != null) objectContext = rowResult(OBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTADESCRIBER.toString) != null) 
            {
                if (!usedVariables.contains(thisObject) && rowResult(OBJECTRULE.toString) == null)
                {
                    val ranges = helper.getDescriberRangesAsList(gmCxn, thisObject)
                    assert (ranges.size == 1, s"ResourceClassList $thisObject is not present as an input and has a range list size that is not 1")
                    thisObject = ranges(0)
                    objectADescriber = false
                }
                else objectADescriber = true
            }
            if (rowResult(SUBJECTADESCRIBER.toString) != null && rowResult(SUBJECTRULE.toString) == null)
            {
                if (!usedVariables.contains(thisSubject))
                {
                    val ranges = helper.getDescriberRangesAsList(gmCxn, thisSubject)
                    assert (ranges.size == 1, s"ResourceClassList $thisSubject is not present as an input and has a range list size that is not 1")
                    thisSubject = ranges(0)
                    objectADescriber = false
                }
                else subjectADescriber = true
            }
            
            var objectFromDatatypeConnection = false
            
            if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/InstanceToLiteralRecipe") 
            {
                assert (objectALiteralValue || objectADefinedLiteral, s"The object of connection $connectionName is not a literal, but the connection is a datatype connection.")
                objectFromDatatypeConnection = true
                subjectAnInstance = true
                objectADescriber = true
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe") 
            {
                assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type InstanceToTermRecipe")
                subjectAnInstance = true
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe") 
            {
                assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type TermToInstanceRecipe")
                objectAnInstance = true
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
            {
                assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type InstanceToInstanceRecipe")
                subjectAnInstance = true
                objectAnInstance = true
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/TermToTermRecipe")
            {
                assert (!objectADefinedLiteral && !objectALiteralValue, s"Found literal object for connection $connectionName of type TermToTermRecipe")
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "https://github.com/PennTURBO/Drivetrain/TermToLiteralRecipe")
            {
                assert (objectALiteralValue || objectADefinedLiteral, s"The object of connection $connectionName is not a literal, but the connection is a datatype connection.")
                objectFromDatatypeConnection = true
                objectADescriber = true
            }
            
            var graph = rowResult(GRAPH.toString).toString
            
            val newTriple = new Triple(thisSubject, rowResult(PREDICATE.toString).toString, thisObject,
                                      subjectAnInstance, objectAnInstance, subjectADescriber, objectADescriber, subjectContext, objectContext, objectALiteralValue)
            graph = helper.checkAndConvertPropertiesReferenceToNamedGraph(graph)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            
            val subjectProcessTriple = new Triple(process, "turbo:TURBO_0010184", thisSubject, false, false, false, (subjectAnInstance || subjectADescriber), "", subjectContext)
            triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            if (!objectFromDatatypeConnection && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:TURBO_0010184", thisObject, false, false, false, (objectAnInstance || objectADescriber), "", objectContext)
                triplesGroup.addRequiredTripleToRequiredGroup(objectProcessTriple, processNamedGraph)
            }
        }
        for (processInputTriple <- varsForProcessInput)
        {
            triplesGroup.addRequiredTripleToRequiredGroup(processInputTriple, processNamedGraph)
        }
        clause = triplesGroup.buildInsertClauseFromTriplesGroup()
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
            
            var subjectAnInstance = false
            var objectAnInstance = false
            
            var subjectContext: String = ""
            var objectContext: String = ""
            
            var objectADescriber = false
            var subjectADescriber = false
            
            var objectALiteral = false
     
            if (rowResult(OBJECTALITERALVALUE.toString).toString.contains("true")) objectALiteral = true
            
            if (rowResult(SUBJECTANINSTANCE.toString) != null) subjectAnInstance = true
            if (rowResult(OBJECTANINSTANCE.toString) != null) objectAnInstance = true
            
            if (rowResult(SUBJECTCONTEXT.toString) != null) subjectContext = rowResult(SUBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTCONTEXT.toString) != null) objectContext = rowResult(OBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTADESCRIBER.toString) != null) objectADescriber = true
            if (rowResult(SUBJECTADESCRIBER.toString) != null) subjectADescriber = true
        
            assert (!rowResult.contains(OPTIONALGROUP.toString))
            helper.validateURI(defaultRemovalsGraph)
        
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                  false, false, true, true, subjectContext, objectContext, objectALiteral)
            val graphForThisRow = helper.checkAndConvertPropertiesReferenceToNamedGraph(defaultRemovalsGraph)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graphForThisRow)
            
            clause = triplesGroup.buildDeleteClauseFromTriplesGroup()
        }
    }
}

class BindClauseBuilder extends SparqlClauseBuilder with ProjectwideGlobals
{
    var bindRules = new HashSet[String]
    var multiplicityCreators = new HashSet[String]
    var currentGroups = new HashMap[String, HashMap[String, org.eclipse.rdf4j.model.Value]]
    var usedVariables = new HashMap[String, Boolean]
    
    var outputSingletonClasses = new HashSet[String]
    var outputSuperSingletonClasses = new HashSet[String]
    var outputOneToOneConnections = new HashMap[String, HashMap[String, String]]
    var outputOneToManyConnections = new HashMap[String, HashMap[String, String]]
    
    var inputOneToOneConnections = new HashMap[String, HashMap[String, String]]
    var inputOneToManyConnections = new HashMap[String, HashMap[String, String]]
    var inputSingletonClasses = new HashSet[String]
    var inputSuperSingletonClasses = new HashSet[String]
    var inputNonInstanceClasses = new HashSet[String]
    
    var process: String = ""
    
    def buildBindClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], localUUID: String, process: String, usedVariables: HashMap[String, Boolean]): HashMap[String, Boolean] =
    {   
        this.process = process
        this.usedVariables = usedVariables
        val newUsedVariables = populateConnectionLists(outputs, inputs)
        
        buildSingletonBindClauses(localUUID)
        buildBaseGroupBindClauses(localUUID)
        for (a <- bindRules) clause += a
        newUsedVariables
    }
    
    def populateConnectionLists(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]): HashMap[String, Boolean] =
    {
        val variablesToBind = populateOutputs(outputs)
        populateInputs(inputs)
        validateConnectionLists()
        variablesToBind
    }
    
    def populateOutputs(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]): HashMap[String, Boolean] =
    {
        var variablesToBind = new HashMap[String,Boolean]
        for (row <- outputs)
        {
            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            val subjectCustomRule = row(SUBJECTRULE.toString)
            val objectCustomRule = row (OBJECTRULE.toString)
            if (subjectCustomRule != null) populateDependenciesAndCustomRuleList(subjectString, subjectCustomRule, row(SUBJECTDEPENDEE.toString))
            if (objectCustomRule != null) populateDependenciesAndCustomRuleList(objectString, objectCustomRule, row(OBJECTDEPENDEE.toString))
            
            if (row(CONNECTIONRECIPETYPE.toString).toString == objToInstRecipe) 
            {  
                if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/1-1") 
                {
                    handleOneToOneConnection(subjectString, objectString, connectionName, outputOneToOneConnections)
                    populateDependenciesAndCustomRuleList(subjectString, subjectCustomRule, row(SUBJECTDEPENDEE.toString))
                    populateDependenciesAndCustomRuleList(objectString, objectCustomRule, row(OBJECTDEPENDEE.toString))
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/many-singleton")
                {
                    outputSingletonClasses += objectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-many")
                {
                    outputSingletonClasses += subjectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/superSingleton-many")
                {
                    outputSuperSingletonClasses += subjectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/many-superSingleton")
                {
                    outputSuperSingletonClasses += objectString
                }
                  else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-singleton")
                {
                    outputSingletonClasses += subjectString
                    outputSingletonClasses += objectString
                }
                else if (thisMultiplicity.endsWith("superSingleton"))
                {
                    outputSuperSingletonClasses += objectString
                }
                else if (thisMultiplicity.contains("superSingleton"))
                {
                    outputSuperSingletonClasses += subjectString
                }
                else if (thisMultiplicity == manyToOneMultiplicity)
                {
                    if (!usedVariables.contains(subjectString)) multiplicityCreators += subjectString
                    if (!usedVariables.contains(objectString)) multiplicityCreators += objectString
                    if (outputOneToManyConnections.contains(objectString)) outputOneToManyConnections(objectString) += subjectString -> connectionName
                    else outputOneToManyConnections += objectString -> HashMap(subjectString -> connectionName)
                }
                else if (thisMultiplicity == oneToManyMultiplicity)
                {
                    if (!usedVariables.contains(subjectString)) multiplicityCreators += subjectString
                    if (!usedVariables.contains(objectString)) multiplicityCreators += objectString
                    if (outputOneToManyConnections.contains(subjectString)) outputOneToManyConnections(subjectString) += objectString -> connectionName
                    else outputOneToManyConnections += subjectString -> HashMap(objectString -> connectionName)
                }
                else
                {
                    assert (1==2, s"Error in graph model: Discovered invalid multiplicity $thisMultiplicity")
                }
                var subjectAnInstance = false
                if (row(SUBJECTANINSTANCE.toString) != null) subjectAnInstance = true
                var objectAnInstance = false
                if (row(OBJECTANINSTANCE.toString) != null) objectAnInstance = true
                variablesToBind += objectString -> objectAnInstance
                variablesToBind += subjectString -> subjectAnInstance
           }
           else 
           {
             if (row(SUBJECTADESCRIBER.toString) != null && row(CONNECTIONRECIPETYPE.toString).toString == objFromTermRecipe 
                 && subjectCustomRule != null) variablesToBind += subjectString -> false
             if (row(OBJECTADESCRIBER.toString) != null && row(CONNECTIONRECIPETYPE.toString).toString == objToTermRecipe
                 && objectCustomRule != null) variablesToBind += objectString -> false
          }
        }
        variablesToBind
    }
    
    def populateInputs(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        for (row <- inputs)
        {
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            if (row(CONNECTIONRECIPETYPE.toString).toString == objToInstRecipe)
            {
                if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/1-1") handleOneToOneConnection(subjectString, objectString, connectionName, inputOneToOneConnections)
                else if (thisMultiplicity == oneToManyMultiplicity)
                {
                    if (inputOneToManyConnections.contains(subjectString)) inputOneToManyConnections(subjectString) += objectString -> connectionName
                    else inputOneToManyConnections += subjectString -> HashMap(objectString -> connectionName)
                }
                else if (thisMultiplicity == manyToOneMultiplicity)
                {
                    if (inputOneToManyConnections.contains(objectString)) inputOneToManyConnections(objectString) += subjectString -> connectionName
                    else inputOneToManyConnections += objectString -> HashMap(subjectString -> connectionName)
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/many-singleton")
                {
                    inputSingletonClasses += objectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-many")
                {
                    inputSingletonClasses += subjectString
                }
                else if (thisMultiplicity == "https://github.com/PennTURBO/Drivetrain/singleton-singleton")
                {
                    inputSingletonClasses += subjectString
                    inputSingletonClasses += objectString
                }
                else if (thisMultiplicity.endsWith("superSingleton"))
                {
                    inputSuperSingletonClasses += objectString
                }
                else if (thisMultiplicity.contains("superSingleton"))
                {
                    inputSuperSingletonClasses += subjectString
                }
                else
                {
                    assert (1==2, s"Error in graph model: Discovered invalid multiplicity $thisMultiplicity")
                } 
            }
            else if (row(CONNECTIONRECIPETYPE.toString).toString == objToTermRecipe || row(CONNECTIONRECIPETYPE.toString).toString == datatypeRecipe)
            {
                inputNonInstanceClasses += subjectString
            }
            else if (row(CONNECTIONRECIPETYPE.toString).toString == objFromTermRecipe)
            {
                inputNonInstanceClasses += objectString
            }
        }
        // we don't care about non-InstanceToInstanceRecipes if there were InstanceToInstanceRecipes present
        if (inputOneToOneConnections.size != 0 || inputOneToManyConnections.size != 0) inputNonInstanceClasses = new HashSet[String]
    }
    
    def populateDependenciesAndCustomRuleList(node: String, customRule: org.eclipse.rdf4j.model.Value, dependent: org.eclipse.rdf4j.model.Value)
    {        
        if (!currentGroups.contains(node)) currentGroups += node -> HashMap("customRule" -> customRule, "dependee" -> dependent)
        else 
        {
            if (currentGroups(node)("dependee") != null && dependent != null) assert (currentGroups(node)("dependee") == dependent)
            if (currentGroups(node)("customRule") != null && customRule != null) assert (currentGroups(node)("customRule") == customRule)
            if (currentGroups(node)("dependee") == null) currentGroups(node)("dependee") = dependent
            if (currentGroups(node)("customRule") == null) currentGroups(node)("customRule") = customRule
        }
    }
    
    def validateConnectionLists()
    {
        for ((k,v) <- outputOneToOneConnections)
        {
            validateSingletonClasses(k)
            validateManyToOneClasses(k,v)
        }
        for ((k,v) <- inputOneToOneConnections)
        {
            validateSingletonClasses(k)
            validateManyToOneClasses(k,v)
        }
        for (item <- multiplicityCreators) validateSingletonClasses(item)
    }
    
    def validateSingletonClasses(k: String)
    {
        assert (!inputSingletonClasses.contains(k), s"Error in graph model: For process $process, $k has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        assert (!inputSuperSingletonClasses.contains(k), s"Error in graph model: For process $process, $k has a 1-1, 1-many, or many-1 relationship and is also considered a SuperSingleton")
        assert (!outputSingletonClasses.contains(k), s"Error in graph model: For process $process, $k has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        assert (!outputSuperSingletonClasses.contains(k), s"Error in graph model: For process $process, $k has a 1-1, 1-many, or many-1 relationship and is also considered a SuperSingleton")
    }
    
    def validateManyToOneClasses(k: String, v: HashMap[String, String])
    {
        for ((connection,connectionName) <- v)
        {
            if (inputOneToManyConnections.contains(connection) && inputOneToManyConnections(connection).contains(k))
            {
                 val invalidConnectionName = inputOneToManyConnections(connection)(k)
                 var connList1 = ""
                 for ((entity, connName) <- v) if (entity != k) connList1 += entity+"\n"
                 var connList2 = ""
                 for ((entity, connName) <- inputOneToManyConnections(connection)) connList2 += entity+"\n"
                 assert (1==2, s"Error in graph model: for process $process, the multiplicity of $k has not been defined consistently in its relationship with $connection. The incompatible connections are $connectionName and $invalidConnectionName.\n\n$k has direct or indirect 1-1 relationships with the following entities: \n$connList1 \n$connection has direct or indirect 1-many relationships with the following entities: \n$connList2")
            }
            if (outputOneToManyConnections.contains(connection) && outputOneToManyConnections(connection).contains(k))
            {
                val invalidConnectionName = outputOneToManyConnections(connection)(k)
                var connList1 = ""
                for ((entity, connName) <- v) if (entity != k) connList1 += entity+"\n"
                var connList2 = ""
                for ((entity, connName) <- outputOneToManyConnections(connection)) connList2 += entity+"\n"
                assert (1==2, s"Error in graph model: for process $process, the multiplicity of $k has not been defined consistently in its relationship with $connection. The incompatible connections are $connectionName and $invalidConnectionName. \n\n$k has direct or indirect 1-1 relationships with the following entities: \n$connList1 \n$connection has direct or indirect 1-many relationships with the following entities: \n$connList2")
            }
        }
    }
    
    def handleOneToOneConnection(thisSubject: String, thisObject: String, connectionName: String, listToPopulate: HashMap[String, HashMap[String, String]])
    {
        if (listToPopulate.contains(thisObject) && listToPopulate.contains(thisSubject))
        {
            listToPopulate(thisObject) += thisSubject -> connectionName
            listToPopulate(thisSubject) += thisObject -> connectionName
            listToPopulate(thisObject) += thisObject -> connectionName
            listToPopulate(thisSubject) += thisSubject -> connectionName
            
            addToConnectionList(thisSubject, connectionName, listToPopulate)
            addToConnectionList(thisObject, connectionName, listToPopulate)
        }
        
        else if (listToPopulate.contains(thisObject) && (!listToPopulate.contains(thisSubject)))
        {
            listToPopulate(thisObject) += thisSubject -> connectionName
            listToPopulate(thisObject) += thisObject -> connectionName
            listToPopulate.put(thisSubject, HashMap(thisObject -> connectionName))
            listToPopulate(thisSubject) += thisSubject -> connectionName
          
            addToConnectionList(thisObject, connectionName, listToPopulate)
        }
        else if (listToPopulate.contains(thisSubject) && (!listToPopulate.contains(thisObject)))
        {
            listToPopulate(thisSubject) += thisObject -> connectionName
            listToPopulate(thisSubject) += thisSubject -> connectionName
            listToPopulate.put(thisObject, HashMap(thisSubject -> connectionName))
            listToPopulate(thisObject) += thisObject -> connectionName
          
            addToConnectionList(thisSubject, connectionName, listToPopulate)
        }
        else
        {
            listToPopulate.put(thisObject, HashMap(thisSubject -> connectionName))
            listToPopulate.put(thisSubject, HashMap(thisObject -> connectionName))
            listToPopulate(thisObject) += thisObject -> connectionName
            listToPopulate(thisSubject) += thisSubject -> connectionName
        }
    }
    
    def addToConnectionList(element: String, connectionName: String, listToPopulate: HashMap[String, HashMap[String, String]])
    {
        val connectionList = listToPopulate(element)
        for ((conn,v) <- connectionList)
        {
            for ((a,value) <- connectionList)
            {
                if (!listToPopulate(conn).contains(a)) listToPopulate(conn) += a -> connectionName
            }
        }   
    }
    
    def buildSingletonBindClauses(localUUID: String)
    {
        for (singleton <- outputSingletonClasses)
        {
            if (!(usedVariables.contains(singleton)))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\",\"${process}")))) AS ${singletonAsVar})\n""" 
            }
        }
        for (singleton <- outputSuperSingletonClasses)
        {
            if (!(usedVariables.contains(singleton)))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\")))) AS ${singletonAsVar})\n"""
            }
        }
    }
    
    def getMultiplicityEnforcer(changeAgent: String): String =
    {
        var multiplicityEnforcer = ""
        var enforcerAsUri = ""
        // this is for if there are no one-to-one or one-to-many connections in the connection lists, it means there is only one possible choice
        if (inputNonInstanceClasses.size == 1)
        {
            enforcerAsUri = inputNonInstanceClasses.iterator.next()
            multiplicityEnforcer = helper.convertTypeToSparqlVariable(enforcerAsUri)
        }
        // if there are no one-many connections that are qualified enforcers in input, you can choose any element from the input as a multiplicity enforcer
        var useAnyQualified = true
        for ((k,v) <- inputOneToOneConnections)
        {
            if (usedVariables.contains(k) && usedVariables(k)) 
            {
                for ((key,value) <- inputOneToManyConnections)
                {
                    if (k != key && usedVariables.contains(key) && usedVariables(key)) useAnyQualified = false
                }
                if (useAnyQualified)
                {
                    val newEnforcer = helper.convertTypeToSparqlVariable(k)
                    if (multiplicityEnforcer != "")
                    {
                        if(!(inputOneToOneConnections.contains(enforcerAsUri) && inputOneToOneConnections(enforcerAsUri).contains(k)))
                            useAnyQualified = false
                    }
                    multiplicityEnforcer = newEnforcer
                    enforcerAsUri = k   
                }
            }
        }
        if ((multiplicityEnforcer == "" || !useAnyQualified) && outputOneToOneConnections.contains(changeAgent))
        {
            multiplicityEnforcer = ""
            for ((conn,v) <- outputOneToOneConnections(changeAgent))
            {
                if (usedVariables.contains(conn) && usedVariables(conn)) 
                {
                    val newEnforcer = helper.convertTypeToSparqlVariable(conn)
                    if (multiplicityEnforcer != "")
                    {
                        assert((inputOneToOneConnections.contains(enforcerAsUri) && inputOneToOneConnections(enforcerAsUri).contains(conn)
                            || (outputOneToOneConnections.contains(enforcerAsUri) && outputOneToOneConnections(enforcerAsUri).contains(conn))),
                            s"Error in graph model: Multiple possible multiplicity enforcers for $changeAgent in process $process: $multiplicityEnforcer, $newEnforcer")
                    }
                    multiplicityEnforcer = newEnforcer
                    enforcerAsUri = conn
                }
            }   
        }
        assert (multiplicityEnforcer != "", s"Error in graph model: For process $process, there is not sufficient context to create: $changeAgent")
        multiplicityEnforcer
    }
    
    def buildBaseGroupBindClauses(localUUID: String)
    {
        for ((k,v) <- currentGroups)
        {
            /*println("key: " + k)
            for ((key, value) <- v)
            {
                println("k: " + key)
                println("v: " + value)
            }*/
            if (!(usedVariables.contains(k)))
            {
                var multiplicityEnforcer = ""
                // if object uses custom rule which does not require multiplicityEnforcer, bypass search step
                if (currentGroups(k)("customRule") == null || 
                    (currentGroups(k)("customRule") != null && 
                        (currentGroups(k)("customRule").toString().
                            contains("multiplicityEnforcer"))))
                {
                    multiplicityEnforcer = getMultiplicityEnforcer(k)
                }
                val connAsVar = helper.convertTypeToSparqlVariable(k)
                addBindRule(v, localUUID, multiplicityEnforcer, connAsVar)
                multiplicityCreators.remove(k)
            }
        }
        // currentGroups holds info about 1-1 connections. If there is an instance with no 1-1 connections, maybe we can still find an enforcer by default
        for (unassignedCreator <- multiplicityCreators)
        {
            val multiplicityEnforcer = getMultiplicityEnforcer(unassignedCreator)
            val connAsVar = helper.convertTypeToSparqlVariable(unassignedCreator)
            addBindRule(HashMap("customRule" -> null, "dependee" -> null), localUUID, multiplicityEnforcer, connAsVar)
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
                  val dependee = helper.convertTypeToSparqlVariable(v("dependee"), true)
                  addDependentBindRule(connAsVar, localUUID, multiplicityEnforcer, dependee)
              }
        }
        else
        {
             var customRule = helper.removeQuotesFromString(v("customRule").toString.split("\\^")(0))+"\n"
             if (v("dependee") != null && customRule.contains("dependent"))
             {
                 val dependee = helper.convertTypeToSparqlVariable(v("dependee"), true)
                 customRule = customRule.replaceAll("\\$\\{dependent\\}", dependee)
             }
             customRule = customRule.replaceAll("\\$\\{replacement\\}", connAsVar)
             customRule = customRule.replaceAll("\\$\\{localUUID\\}", localUUID)
             customRule = customRule.replaceAll("\\$\\{multiplicityEnforcer\\}", multiplicityEnforcer)
             customRule = customRule.replaceAll("\\$\\{defaultPrefix\\}", defaultPrefix)
             // these assertions may not be valid if a user decides to create a prefix or term with one of these words in it
             assert (!customRule.contains("replacement"))
             assert (!customRule.contains("dependent"), s"No dependent for custom rule was identified, but custom rule requires a dependent. Rule string: $customRule")
             assert (!customRule.contains("localUUID"))
             assert (!customRule.contains("multiplicityEnforcer"))
             assert (!customRule.contains("defaultPrefix"))
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
    
            bindRules += s"""BIND(uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))) AS ${newNodeAsVar})\n"""
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
            
            bindRules += s"""BIND(IF (BOUND(${dependeeAsVar}), uri(concat("$defaultPrefix",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))), ?unbound) AS ${newNodeAsVar})\n"""   
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