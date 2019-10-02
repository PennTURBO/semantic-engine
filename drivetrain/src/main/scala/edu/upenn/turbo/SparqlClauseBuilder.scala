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
    var varsForProcessInput = new HashSet[String]
    var valuesBlock: HashMap[String, String] = new HashMap[String, String]
    var nonDefaultGraphTriples = new ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]
    
    def setGraphModelConnection(gmCxn: RepositoryConnection)
    {
        this.gmCxn = gmCxn
    }
    
    def addTripleFromRowResult(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], defaultInputGraph: String): HashSet[String] =
    {
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
          
        var valuesBlockAsString = ""
        for ((k,v) <- valuesBlock) valuesBlockAsString += v+"\n"
        clause = valuesBlockAsString + triplesGroup.buildWhereClauseFromTriplesGroup()
        varsForProcessInput
    }
    
    def makeNewTripleFromRowResult(rowResult: HashMap[String, org.eclipse.rdf4j.model.Value], defaultInputGraph: String)
    {
        var optionalGroupForThisRow: String = null
        var minusGroupForThisRow: String = null
        
        var subjectAType = false
        var objectAType = false
        
        var objectADescriber = false
        var subjectADescriber = false
        
        var graphForThisRow: String = defaultInputGraph
        if (rowResult(GRAPHOFORIGIN.toString) != null) graphForThisRow = rowResult(GRAPHOFORIGIN.toString).toString
        if (rowResult(GRAPHOFCREATINGPROCESS.toString) != null) graphForThisRow = rowResult(GRAPHOFCREATINGPROCESS.toString).toString
        val connectionName = rowResult(CONNECTIONNAME.toString).toString
        
        var required = true
        if (rowResult(INPUTTYPE.toString).toString == "http://transformunify.org/ontologies/hasOptionalInput") required = false
        if (rowResult(OPTIONALGROUP.toString) != null) optionalGroupForThisRow = rowResult(OPTIONALGROUP.toString).toString
        if (rowResult(MINUSGROUP.toString) != null) minusGroupForThisRow = rowResult(MINUSGROUP.toString).toString
        if (rowResult(OBJECTADESCRIBER.toString) != null) 
        {
            objectADescriber = true
            val valsAsString = getDescriberRangesAsString(rowResult(OBJECT.toString).toString)
            if (valsAsString.size > 0) valuesBlock += rowResult(OBJECT.toString).toString -> valsAsString
        }
        if (rowResult(SUBJECTADESCRIBER.toString) != null) 
        {
            subjectADescriber = true
            val valsAsString = getDescriberRangesAsString(rowResult(SUBJECT.toString).toString)
            if (valsAsString.size > 0) valuesBlock += rowResult(SUBJECT.toString).toString -> valsAsString
        }
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
        if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") 
        {
            assert (subjectAType || subjectADescriber, s"The object of connection $connectionName is not present in the TURBO ontology")
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/ObjectConnectionToTermRecipe") 
        {
            assert (subjectAType || subjectADescriber, s"The subject of connection $connectionName is not present in the TURBO ontology" )
        }
        else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/ObjectConnectionFromTermRecipe") 
        {
            assert (objectAType || objectADescriber, s"The object of connection $connectionName is not present in the TURBO ontology")
        }
        else
        {
            assert (subjectAType || subjectADescriber, s"The subject of connection $connectionName is not present in the TURBO ontology")
            assert (objectAType || objectADescriber, s"The object of connection $connectionName is not present in the TURBO ontology")
        }
        val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString,
                                                 subjectAType, objectAType, objectADescriber)
        
        
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
    
    def getDescriberRangesAsString(describer: String): String =
    {
        val sparql: String = s"""
          Select * Where
          {
              <$describer> turbo:range ?range .
          }
          """
        val sparqlResults = update.querySparqlAndUnpackTuple(gmCxn, sparql, "range")
        if (sparqlResults.size == 0) ""
        else 
        {
            val describerAsVar = helper.convertTypeToSparqlVariable(describer, true)
            var res = s"VALUES $describerAsVar {"
            for (item <- sparqlResults) res += "<" + item + ">"
            res + "}"
        }
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
            
            val connectionName = rowResult(CONNECTIONNAME.toString).toString
            
            var subjectAType = false
            var objectAType = false
            var subjectContext: String = ""
            var objectContext: String = ""
            
            var objectADescriber = false
            var subjectADescriber = false
            
            if (rowResult(SUBJECTTYPE.toString) != null) subjectAType = true
            if (rowResult(OBJECTTYPE.toString) != null) objectAType = true
            if (rowResult(SUBJECTCONTEXT.toString) != null) subjectContext = rowResult(SUBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTCONTEXT.toString) != null) objectContext = rowResult(OBJECTCONTEXT.toString).toString
            if (rowResult(OBJECTADESCRIBER.toString) != null) objectADescriber = true
            if (rowResult(SUBJECTADESCRIBER.toString) != null) subjectADescriber = true
            
            var objectIsLiteral = false
            
            if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/DatatypeConnectionRecipe") 
            {
                assert (subjectAType || subjectADescriber, s"The object of connection $connectionName is not present in the TURBO ontology")
                objectIsLiteral = true
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/ObjectConnectionToTermRecipe") 
            {
                objectAType = false
                if (!usedVariables.contains(rowResult(OBJECT.toString).toString)) nonVariableClasses += rowResult(OBJECT.toString).toString
                assert (subjectAType || subjectADescriber, s"The subject of connection $connectionName is not present in the TURBO ontology")
            }
            else if (rowResult(CONNECTIONRECIPETYPE.toString).toString() == "http://transformunify.org/ontologies/ObjectConnectionFromTermRecipe") 
            {
                subjectAType = false
                if (!usedVariables.contains(rowResult(SUBJECT.toString).toString)) nonVariableClasses += rowResult(SUBJECT.toString).toString
                assert (objectAType || objectADescriber, s"The object of connection $connectionName is not present in the TURBO ontology")
            }
            else
            {
                assert (subjectAType || subjectADescriber, s"The subject of connection $connectionName is not present in the TURBO ontology")
                assert (objectAType || objectADescriber, s"The object of connection $connectionName is not present in the TURBO ontology")
            }
            
            val graph = rowResult(GRAPH.toString).toString
            
            val newTriple = new Triple(rowResult(SUBJECT.toString).toString, rowResult(PREDICATE.toString).toString, rowResult(OBJECT.toString).toString, 
                                      subjectAType, objectAType, false, subjectContext, objectContext)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, graph)
            if (usedVariables.contains(newTriple.getSubjectWithContext()))
            {
                val subjectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(SUBJECT.toString).toString, false, false, false, "", subjectContext)
                triplesGroup.addRequiredTripleToRequiredGroup(subjectProcessTriple, processNamedGraph)
            }
            if (!objectIsLiteral && usedVariables.contains(newTriple.getObjectWithContext()) && newTriple.triplePredicate != "rdf:type")
            {
                val objectProcessTriple = new Triple(process, "turbo:TURBO_0010184", rowResult(OBJECT.toString).toString, false, false, false, "", objectContext)
                triplesGroup.addRequiredTripleToRequiredGroup(objectProcessTriple, processNamedGraph)
            }
        }
        for (uri <- varsForProcessInput)
        {
            val processInputTriple = new Triple(process, "obo:OBI_0000293", uri, false, false, false)
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
                                  false, false, false)
            triplesGroup.addRequiredTripleToRequiredGroup(newTriple, defaultRemovalsGraph)
            
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
    var outputOneToOneConnections = new HashMap[String, HashSet[String]]
    var outputOneToManyConnections = new HashMap[String, HashSet[String]]
    
    var inputOneToOneConnections = new HashMap[String, HashSet[String]]
    var inputOneToManyConnections = new HashMap[String, HashSet[String]]
    var inputSingletonClasses = new HashSet[String]
    var inputSuperSingletonClasses = new HashSet[String]
    
    var process: String = ""
    
    val manyToOneMultiplicity = "http://transformunify.org/ontologies/many-1"
    val oneToManyMultiplicity = "http://transformunify.org/ontologies/1-many"
    val objToInstRecipe = "http://transformunify.org/ontologies/ObjectConnectionToInstanceRecipe"
    
    def buildBindClause(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], localUUID: String, process: String, usedVariables: HashMap[String, Boolean]): HashMap[String, Boolean] =
    {   
        this.process = process
        this.usedVariables = usedVariables
        val newUsedVariables = populateConnectionLists(outputs, inputs)
        buildSingletonBindClauses(localUUID)
        buildBaseGroupBindClauses(localUUID)
        assert (multiplicityCreators.size == 0, s"Error in graph model: For process $process, there is not sufficient context to create the following: $multiplicityCreators")
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
        var discoveredMap = new HashMap[String, String]
        var scannedConnections = new HashMap[String, String]
        for (row <- outputs)
        {
            val connectionName = row(CONNECTIONNAME.toString).toString
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            var subjectString = row(SUBJECT.toString).toString
            var objectString = row(OBJECT.toString).toString
            if (row(SUBJECTCONTEXT.toString) != null) subjectString += "_"+helper.convertTypeToSparqlVariable(row(SUBJECTCONTEXT.toString).toString).substring(1)
            if (row(OBJECTCONTEXT.toString) != null) objectString += "_"+helper.convertTypeToSparqlVariable(row(OBJECTCONTEXT.toString).toString).substring(1)
            
            val discoveryCode = subjectString + objectString
            
            if (discoveredMap.contains(discoveryCode)) assert(discoveredMap(discoveryCode) == row(MULTIPLICITY.toString).toString, s"Error in graph model: There are multiple connections between $subjectString and $objectString with non-matching multiplicities")
            else discoveredMap += discoveryCode -> row(MULTIPLICITY.toString).toString
            val codePlus = discoveryCode + row(PREDICATE.toString).toString + thisMultiplicity
            if (scannedConnections.contains(connectionName)) assert(scannedConnections(connectionName) == codePlus, s"Error in graph model: recipe $connectionName may have duplicate properties")
            else scannedConnections += connectionName -> codePlus
            if (row(CONNECTIONRECIPETYPE.toString).toString == objToInstRecipe || row(SUBJECTADESCRIBER.toString) != null || row(OBJECTADESCRIBER.toString) != null) 
            {  
                if (thisMultiplicity == "http://transformunify.org/ontologies/1-1") 
                {
                    handleOneToOneConnection(subjectString, objectString, outputOneToOneConnections)
                    populateDependenciesAndCustomRuleList(row, subjectString, objectString)
                }
                else if (thisMultiplicity == "http://transformunify.org/ontologies/many-singleton")
                {
                    outputSingletonClasses += objectString
                }
                else if (thisMultiplicity == "http://transformunify.org/ontologies/singleton-many")
                {
                    outputSingletonClasses += subjectString
                }
                  else if (thisMultiplicity == "http://transformunify.org/ontologies/singleton-singleton")
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
                    if (outputOneToManyConnections.contains(objectString)) outputOneToManyConnections(objectString) += subjectString
                    else outputOneToManyConnections += objectString -> HashSet(subjectString)
                }
                else if (thisMultiplicity == oneToManyMultiplicity)
                {
                    if (!usedVariables.contains(subjectString)) multiplicityCreators += subjectString
                    if (!usedVariables.contains(objectString)) multiplicityCreators += objectString
                    if (outputOneToManyConnections.contains(subjectString)) outputOneToManyConnections(subjectString) += objectString
                    else outputOneToManyConnections += subjectString -> HashSet(objectString)
                }
                else
                {
                    assert (1==2, s"Error in graph model: Discovered invalid multiplicity $thisMultiplicity")
                }
                var subjAType = false
                if (row(SUBJECTTYPE.toString) != null) subjAType = true
                var objAType = false
                if (row(OBJECTTYPE.toString) != null) objAType = true
                variablesToBind += objectString -> objAType
                variablesToBind += subjectString -> subjAType
           }
        }
        variablesToBind
    }
    
    def populateInputs(inputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]])
    {
        var scannedConnections = new HashMap[String, String]
        for (row <- inputs)
        {
            val connectionName = row(CONNECTIONNAME.toString).toString
            val codePlus = row(SUBJECT.toString).toString + row(PREDICATE.toString).toString + row(OBJECT.toString).toString + row(MULTIPLICITY.toString).toString
            if (scannedConnections.contains(connectionName)) assert(scannedConnections(connectionName) == codePlus, s"Error in graph model: recipe $connectionName may have duplicate properties")
            else scannedConnections += connectionName -> codePlus
            val thisMultiplicity = row(MULTIPLICITY.toString).toString
            if (thisMultiplicity == "http://transformunify.org/ontologies/1-1") handleOneToOneConnection(row(SUBJECT.toString).toString, row(OBJECT.toString).toString, inputOneToOneConnections)
            else if (thisMultiplicity == oneToManyMultiplicity)
            {
                if (inputOneToManyConnections.contains(row(SUBJECT.toString).toString)) inputOneToManyConnections(row(SUBJECT.toString).toString) += row(OBJECT.toString).toString
                else inputOneToManyConnections += row(SUBJECT.toString).toString -> HashSet(row(OBJECT.toString).toString)
            }
            else if (thisMultiplicity == manyToOneMultiplicity)
            {
                if (inputOneToManyConnections.contains(row(OBJECT.toString).toString)) inputOneToManyConnections(row(OBJECT.toString).toString) += row(SUBJECT.toString).toString
                else inputOneToManyConnections += row(OBJECT.toString).toString -> HashSet(row(SUBJECT.toString).toString)
            }
            else if (thisMultiplicity == "http://transformunify.org/ontologies/many-singleton")
            {
                inputSingletonClasses += row(OBJECT.toString).toString
            }
            else if (thisMultiplicity == "http://transformunify.org/ontologies/singleton-many")
            {
                inputSingletonClasses += row(SUBJECT.toString).toString
            }
            else if (thisMultiplicity == "http://transformunify.org/ontologies/singleton-singleton")
            {
                inputSingletonClasses += row(SUBJECT.toString).toString
                inputSingletonClasses += row(OBJECT.toString).toString
            }
            else if (thisMultiplicity.endsWith("superSingleton"))
            {
                inputSuperSingletonClasses += row(OBJECT.toString).toString
            }
            else if (thisMultiplicity.contains("superSingleton"))
            {
                inputSuperSingletonClasses += row(SUBJECT.toString).toString
            }
            else
            {
                assert (1==2, s"Error in graph model: Discovered invalid multiplicity $thisMultiplicity")
            }
        }
    }
    
    def populateDependenciesAndCustomRuleList(row: HashMap[String, org.eclipse.rdf4j.model.Value], thisSubject: String, thisObject: String)
    {        
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
    
    def validateManyToOneClasses(k: String, v: HashSet[String])
    {
        for (connection <- v)
        {
            if (inputOneToManyConnections.contains(connection))
            {
                assert(!inputOneToManyConnections(connection).contains(k), s"Error in graph model: for process $process, the multiplicity of $k has not been defined consistently.")
            }
            if (outputOneToManyConnections.contains(connection))
            {
                assert(!outputOneToManyConnections(connection).contains(k), s"Error in graph model: for process $process, the multiplicity of $k has not been defined consistently.")
            }
        }
    }
    
    def addToConnectionList(element: String, listToPopulate: HashMap[String, HashSet[String]])
    {
        val connectionList = listToPopulate(element)
        for (conn <- connectionList)
        {
            for (a <- connectionList)
            {
                listToPopulate(conn) += a
            }
        }   
    }
    
    def handleOneToOneConnection(thisSubject: String, thisObject: String, listToPopulate: HashMap[String, HashSet[String]])
    {
        if (listToPopulate.contains(thisObject) && listToPopulate.contains(thisSubject))
        {
            listToPopulate(thisObject) += thisSubject
            listToPopulate(thisSubject) += thisObject
            listToPopulate(thisObject) += thisObject
            listToPopulate(thisSubject) += thisSubject
            
            addToConnectionList(thisSubject, listToPopulate)
            addToConnectionList(thisObject, listToPopulate)
        }
        
        else if (listToPopulate.contains(thisObject) && (!listToPopulate.contains(thisSubject)))
        {
            listToPopulate(thisObject) += thisSubject
            listToPopulate(thisObject) += thisObject
            listToPopulate.put(thisSubject, HashSet(thisObject))
            listToPopulate(thisSubject) += thisSubject
          
            addToConnectionList(thisObject, listToPopulate)
        }
        else if (listToPopulate.contains(thisSubject) && (!listToPopulate.contains(thisObject)))
        {
            listToPopulate(thisSubject) += thisObject
            listToPopulate(thisSubject) += thisSubject
            listToPopulate.put(thisObject, HashSet(thisSubject))
            listToPopulate(thisObject) += thisObject
          
            addToConnectionList(thisSubject, listToPopulate)
        }
        else
        {
            listToPopulate.put(thisObject, HashSet(thisSubject))
            listToPopulate.put(thisSubject, HashSet(thisObject))
            listToPopulate(thisObject) += thisObject
            listToPopulate(thisSubject) += thisSubject
        }
    }
    
    def buildSingletonBindClauses(localUUID: String)
    {
        for (singleton <- outputSingletonClasses)
        {
            if (!(usedVariables.contains(singleton)))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\",\"${process}")))) AS ${singletonAsVar})\n""" 
            }
        }
        for (singleton <- outputSuperSingletonClasses)
        {
            if (!(usedVariables.contains(singleton)))
            {
                val singletonAsVar = helper.convertTypeToSparqlVariable(singleton)
                bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",SHA256(CONCAT(\"${singletonAsVar}\",\"${localUUID}\")))) AS ${singletonAsVar})\n"""
            }
        }
    }
    
    def getMultiplicityEnforcer(changeAgent: String): String =
    {
        var multiplicityEnforcer = ""
        var enforcerAsUri = ""
        assert (outputOneToOneConnections.contains(changeAgent))
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
                        assert(inputOneToOneConnections.contains(enforcerAsUri) && inputOneToOneConnections(enforcerAsUri).contains(k),
                            s"Error in graph model: Multiple possible multiplicity enforcers for $changeAgent in process $process: $multiplicityEnforcer, $newEnforcer")
                    }
                    multiplicityEnforcer = newEnforcer
                    enforcerAsUri = k   
                }
            }  
        }
        if (multiplicityEnforcer == "")
        {
            for (conn <- outputOneToOneConnections(changeAgent))
            {
                if (usedVariables.contains(conn) && usedVariables(conn)) 
                {
                    val newEnforcer = helper.convertTypeToSparqlVariable(conn)
                    if (multiplicityEnforcer != "")
                    {
                        assert(inputOneToOneConnections.contains(enforcerAsUri) && inputOneToOneConnections(enforcerAsUri).contains(conn),
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
             assert (!customRule.contains("replacement"))
             assert (!customRule.contains("dependent"), s"No dependent for custom rule was identified, but custom rule requires a dependent. Rule string: $customRule")
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
    
            bindRules += s"""BIND(uri(concat(\"http://www.itmat.upenn.edu/biobank/\",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))) AS ${newNodeAsVar})\n"""   
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
            
            bindRules += s"""BIND(IF (BOUND(${dependeeAsVar}), uri(concat(\"http://www.itmat.upenn.edu/biobank/\",SHA256(CONCAT(\"${newNodeAsVar}\",\"${localUUID}\", str(${multiplicityEnforcerAsVar}))))), ?unbound) AS ${newNodeAsVar})\n"""   
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