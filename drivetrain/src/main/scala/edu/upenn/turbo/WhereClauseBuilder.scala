package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class WhereClauseBuilder(cxn: RepositoryConnection) extends SparqlClauseBuilder with ProjectwideGlobals
{
    this.gmCxn = cxn
    
    val triplesGroup = new TriplesGroupBuilder()
    var varsForProcessInput = new HashSet[Triple]
    var valuesBlock: HashMap[String, String] = new HashMap[String, String]
    var nonDefaultGraphTriples = new ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]]
    var process: String = ""
    
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
        else
        {
            val recipeType = rowResult(CONNECTIONRECIPETYPE.toString).toString
            if (recipeType == instToLiteralRecipe || recipeType == termToLiteralRecipe) objectADefinedLiteral = true 
        }
        
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