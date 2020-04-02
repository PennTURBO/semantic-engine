package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet
import java.io.PrintWriter
import org.eclipse.rdf4j.model.Value
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class TestBuilder extends ProjectwideGlobals
{
    //only make all fields? in the case that there are no optional triples
    var onlyAllFields = false
    
    def buildTest(cxn: RepositoryConnection, gmCxn: RepositoryConnection, process: String)
    {
        val testFileName = helper.getPostfixfromURI(process) + "SnapshotTest"
        val instructionSetName = instructionSetFile.split("\\.")(0)
        val testFilePath = new File("src//test//scala//edu//upenn//turbo//AutoGenTests//" + testFileName + "_" + instructionSetName + ".scala")
        testFilePath.getParentFile().mkdirs()
        //assert(!testFilePath.exists(), s"File $testFileName.scala already exists")

        val inputs = RunDrivetrainProcess.getInputs(process)
        
        val inputTriplesArr = generateInputTriples(process, inputs, gmCxn)
        val fullTripleSet = inputTriplesArr(0)
        val minimumTripleSet = inputTriplesArr(1)
        
        println(fullTripleSet)
        
        update.updateSparql(cxn, fullTripleSet)
        val queryResultMax = RunDrivetrainProcess.runProcess(process)
        val outputNamedGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(queryResultMax(process).defaultOutputGraph)
        val outputPredsMax = getOutputPredicates(cxn, outputNamedGraph)
        assert (outputPredsMax.size > 0, s"""Process $process did not create any output based on the "all fields" input.
        This is likely a bug in the TestBuilder code.
        Check that the required and optional triples above are formatted as expected for an input to this process, and call Hayden.""")
        
        helper.deleteAllTriplesInDatabase(cxn)
        
        update.updateSparql(cxn, minimumTripleSet)
        val queryResultMin = RunDrivetrainProcess.runProcess(process)
        val outputPredsMin = getOutputPredicates(cxn, outputNamedGraph)
        assert (outputPredsMax.size > 0, s"""Process $process did not create any output based on the "minimum fields" input.
        This is likely a bug in the TestBuilder code.
        Check that the required triples above are formatted as expected for an input to this process, and call Hayden.""")
        
        helper.deleteAllTriplesInDatabase(cxn)

        logger.info("Writing test file...")
        
        writeTestFile(testFileName, process, outputNamedGraph, testFilePath, fullTripleSet, outputPredsMax, 
                      minimumTripleSet, outputPredsMin, instructionSetName)

        logger.info("Test created in src//test//scala//edu//upenn//turbo//AutoGenTests//")

    }
    
    def writeTestFile(testFileName: String, process: String, outputNamedGraph: String, 
                      testFilePath: File, maximumInputTriples: String, outputPredsMax: ArrayBuffer[String], 
                      minimumInputTriples: String, outputPredsMin: ArrayBuffer[String], instructionSetName: String)
    {
        val packageString = "package edu.upenn.turbo\n"

val imports = """
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID    
"""

        val classAssertion = s"class "+testFileName+"_"+instructionSetName+" extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {\n"
        
        val clearTestingRepo = "val clearTestingRepositoryAfterRun: Boolean = false\n"

val beforesAndAfters = s"""
override def beforeAll()
{
    graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true, "$instructionSetFile")
    cxn = graphDBMaterials.getConnection()
    gmCxn = graphDBMaterials.getGmConnection()
    helper.deleteAllTriplesInDatabase(cxn)
    
    RunDrivetrainProcess.setGraphModelConnection(gmCxn)
    RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    RunDrivetrainProcess.setInputNamedGraphsCache(false)
}

override def afterAll()
{
    ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
}

before
{
    helper.deleteAllTriplesInDatabase(cxn)
}

"""

val allFieldsTest = buildTestingTemplate("all fields test", maximumInputTriples, process, outputNamedGraph, outputPredsMax)
var minFieldsTest = ""
if (!onlyAllFields) minFieldsTest = buildTestingTemplate("minimum fields test", minimumInputTriples, process, outputNamedGraph, outputPredsMin)


        val pw = new PrintWriter(testFilePath)
        var fullTestClassString = packageString + imports + classAssertion + clearTestingRepo + beforesAndAfters +
                                  allFieldsTest + minFieldsTest + "}"
        pw.write(fullTestClassString)
        pw.close()
    }
    
    def buildTestingTemplate(testName: String, inputTriples: String, process: String, outputNamedGraph: String, outputPredsList: ArrayBuffer[String]): String =
    {
val startTest: String = s"""
test("$testName")
{
"""

val insertInputDataset = s"""
val insertInputDataset = 
\"\"\"$inputTriples\"\"\"
update.updateSparql(cxn, insertInputDataset)

"""

val queryPredicates = s"""
RunDrivetrainProcess.runProcess("$process")
val count: String = s"SELECT * WHERE {GRAPH <$outputNamedGraph> {?s ?p ?o .}}"
val result = update.querySparqlAndUnpackTuple(cxn, count, "p")
"""

var fullPredsAsString = ""
for (index <- 0 to outputPredsList.size-1) 
{
    fullPredsAsString += "\""+outputPredsList(index)+"\""
    if (index != outputPredsList.size-1) fullPredsAsString += ","
    if (index % 2 != 0) fullPredsAsString += "\n"
}
val predicateCheck = s"""
val checkPredicates = Array(
$fullPredsAsString
)

helper.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")

result.size should be (checkPredicates.size)

 """

val endTest = "}"

startTest + insertInputDataset + queryPredicates + predicateCheck + endTest
    }
    
    def generateInputTriples(process: String, inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]], gmCxn: RepositoryConnection): Array[String] =
    {
        var generatedRequiredTriples = new HashMap[String, HashSet[String]]
        var generatedOptionalTriples = new HashMap[String, HashSet[String]]
        
        val defaultInputGraph = inputs(0)(GRAPH.toString).toString
        
        for (input <- inputs)
        {
            var thisGraph = defaultInputGraph
            if (input(GRAPHOFCREATINGPROCESS.toString) != null) thisGraph = input(GRAPHOFCREATINGPROCESS.toString).toString
            if (input(GRAPHOFORIGIN.toString) != null) thisGraph = input(GRAPHOFORIGIN.toString).toString
            
            val inputType = input(INPUTTYPE.toString).toString
            val optionalBlock = input(OPTIONALGROUP.toString)
            if (optionalBlock == null && inputType == "https://github.com/PennTURBO/Drivetrain/hasRequiredInput")
            {
                if (!generatedRequiredTriples.contains(thisGraph)) generatedRequiredTriples += thisGraph -> new HashSet[String]
                for (triple <- makeInstanceDataFromTriple(gmCxn, input)) generatedRequiredTriples(thisGraph) += triple
            }
            else
            {
                if (!generatedOptionalTriples.contains(thisGraph)) generatedOptionalTriples += thisGraph -> new HashSet[String]
                for (triple <- makeInstanceDataFromTriple(gmCxn, input)) generatedOptionalTriples(thisGraph) += triple
            }
        }
            
        var generatedRequiredTriplesAsString = ""
        var generatedOptionalTriplesAsString = ""
        for ((graph,triples) <- generatedRequiredTriples) 
        {
            generatedRequiredTriplesAsString += "GRAPH <" + helper.checkAndConvertPropertiesReferenceToNamedGraph(graph) + "> {\n"
            for (triple <- triples) generatedRequiredTriplesAsString += triple
            generatedRequiredTriplesAsString += "}\n"
        }
        for ((graph,triples) <- generatedOptionalTriples)
        {
            generatedOptionalTriplesAsString += "GRAPH <" + helper.checkAndConvertPropertiesReferenceToNamedGraph(graph) + "> {\n"
            for (triple <- triples) generatedOptionalTriplesAsString += triple
            generatedOptionalTriplesAsString += "}\n"
        }
        if (generatedOptionalTriplesAsString == "")
        {
            logger.info(s"""No optional triples found for process $process. Only "all fields" test will be created.""")
            onlyAllFields = true
        }
        Array(
        s"""
            INSERT DATA {
                   # Required triples
                   $generatedRequiredTriplesAsString
                   # Optional triples
                   $generatedOptionalTriplesAsString
            }
        """,
        s"""
            INSERT DATA {
                   $generatedRequiredTriplesAsString
            }
        """
        )
    }
    
    def getOutputPredicates(cxn: RepositoryConnection, outputNamedGraph: String): ArrayBuffer[String] =
    {
        update.querySparqlAndUnpackTuple(cxn, s"Select ?p Where { Graph <$outputNamedGraph> { ?s ?p ?o . }}", "p")
    }
    
    def makeInstanceDataFromTriple(gmCxn: RepositoryConnection, input: HashMap[String,org.eclipse.rdf4j.model.Value]): ArrayBuffer[String] =
    {
        var thisTripleAsData = new ArrayBuffer[String]
        
        val connectionType = input(CONNECTIONRECIPETYPE.toString).toString
        val subjectString = input(SUBJECT.toString).toString
        val predicateString = input(PREDICATE.toString).toString
        val objectString = input(OBJECT.toString).toString
        
        val objectADescriber = input(OBJECTADESCRIBER.toString)
        val subjectADescriber = input(SUBJECTADESCRIBER.toString)
        
        val subjectURI = subjectString+"_1"
        val objectURI = objectString+"_1"
               
        if (connectionType == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
        {
            thisTripleAsData += s"<$subjectURI> <$predicateString> <$objectURI> .\n"
            if (subjectADescriber == null) thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"
            if (objectADescriber == null) thisTripleAsData += s"<$objectURI> rdf:type <$objectString> .\n"
        }
        else if (connectionType == "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe")
        {
            var localObjectString = objectString
            if (objectADescriber != null)
            {
                val descRanges = helper.getDescriberRangesAsList(gmCxn, objectString)
                if (descRanges.size == 0) localObjectString = "http://purl.obolibrary.org/obo/BFO_0000001"
                else localObjectString = descRanges(0)
            }
            thisTripleAsData += s"<$subjectURI> <$predicateString> <$localObjectString> .\n"
            if (subjectADescriber == null) thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"
        }
        else if (connectionType == "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe")
        {
            var localSubjectString = subjectString
            if (subjectADescriber != null)
            {
                val descRanges = helper.getDescriberRangesAsList(gmCxn, subjectString)
                if (descRanges.size == 0) localSubjectString = "http://purl.obolibrary.org/obo/BFO_0000001"
                else localSubjectString = descRanges(0)
            }
            thisTripleAsData += s"<$localSubjectString> <$predicateString> <$objectURI> .\n"
            if (objectADescriber == null) thisTripleAsData += s"<$objectURI> rdf:type <$objectString> .\n"
        }
        else if (connectionType == "https://github.com/PennTURBO/Drivetrain/TermToTermRecipe")
        {
            var localSubjectString = subjectString
            var localObjectString = objectString
            if (subjectADescriber != null)
            {
                val descRanges = helper.getDescriberRangesAsList(gmCxn, subjectString)
                if (descRanges.size == 0) localSubjectString = "http://purl.obolibrary.org/obo/BFO_0000001"
                else localSubjectString = descRanges(0)
            }
            if (objectADescriber != null)
            {
                val descRanges = helper.getDescriberRangesAsList(gmCxn, objectString)
                if (descRanges.size == 0) localObjectString = "http://purl.obolibrary.org/obo/BFO_0000001"
                else localObjectString = descRanges(0)
            }
            thisTripleAsData += s"<$localSubjectString> <$predicateString> <$localObjectString> .\n"
        }
        else if (connectionType == "https://github.com/PennTURBO/Drivetrain/InstanceToLiteralRecipe")
        {
            val literalType = input(GRAPHLITERALTYPE.toString).toString
            thisTripleAsData += makeTripleWithLiteral(literalType, objectString, subjectURI, predicateString)
            if (subjectADescriber == null) thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"
        }
        else if (connectionType == "https://github.com/PennTURBO/Drivetrain/TermToLiteralRecipe")
        {
            val literalType = input(GRAPHLITERALTYPE.toString).toString
            var localSubjectString = subjectString
            if (subjectADescriber != null)
            {
                val descRanges = helper.getDescriberRangesAsList(gmCxn, subjectString)
                if (descRanges.size == 0) localSubjectString = "http://purl.obolibrary.org/obo/BFO_0000001"
                else localSubjectString = descRanges(0)
            }
            thisTripleAsData += makeTripleWithLiteral(literalType, objectString, localSubjectString, predicateString)
        }
        thisTripleAsData
    }
    
    def makeTripleWithLiteral(literalType: String, objectString: String, subjectURI: String, predicateString: String): String =
    {
         var thisTripleAsData = ""
         if (literalType == "https://github.com/PennTURBO/Drivetrain/StringLiteralResourceList" || literalType == "https://github.com/PennTURBO/Drivetrain/LiteralResourceList") 
          {
              val literalValue = "\"" + Math.abs(objectString.hashCode())+"abc" + "\""
              thisTripleAsData = s"<$subjectURI> <$predicateString> $literalValue^^xsd:String .\n"
          }
          else if (literalType == "https://github.com/PennTURBO/Drivetrain/IntegerLiteralResourceList") 
          {
              val literalValue = "\""+Math.abs(objectString.hashCode())+"\""
              thisTripleAsData = s"<$subjectURI> <$predicateString> $literalValue^^xsd:Integer .\n"
          }
          else if (literalType == "https://github.com/PennTURBO/Drivetrain/DoubleLiteralResourceList") 
          {
              val literalValue = "\""+Math.abs(objectString.hashCode()) + ".00"+"\""
              thisTripleAsData = s"<$subjectURI> <$predicateString> $literalValue^^xsd:Double .\n"
          }
          else if (literalType == "https://github.com/PennTURBO/Drivetrain/BooleanLiteralResourceList") 
          {
              val literalValue = "\"true\""
              thisTripleAsData = s"<$subjectURI> <$predicateString> $literalValue^^xsd:Boolean .\n"
          }
          else if (literalType == "https://github.com/PennTURBO/Drivetrain/DateLiteralResourceList") 
          {
              val literalValue = "\"" + LocalDateTime.now() + "\""
              thisTripleAsData = s"<$subjectURI> <$predicateString> $literalValue^^xsd:Date .\n"
          }
         thisTripleAsData
    }
}