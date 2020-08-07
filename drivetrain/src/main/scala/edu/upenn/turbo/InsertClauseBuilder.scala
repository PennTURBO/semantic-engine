package edu.upenn.turbo

import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value
import java.util.regex.Pattern
import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.util.control._

class InsertClauseBuilder(cxn: RepositoryConnection) extends SparqlClauseBuilder with ProjectwideGlobals
{
    this.gmCxn = cxn
    
    def addTripleFromRowResult(outputs: ArrayBuffer[HashMap[String, org.eclipse.rdf4j.model.Value]], process: String, varsForProcessInput: HashSet[Triple], usedVariables: HashSet[String])
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
            else
            {
                val recipeType = rowResult(CONNECTIONRECIPETYPE.toString).toString
                if (recipeType == instToLiteralRecipe || recipeType == termToLiteralRecipe) objectADefinedLiteral = true 
            }
            
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