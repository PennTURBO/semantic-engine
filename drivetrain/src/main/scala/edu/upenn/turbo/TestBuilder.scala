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
    def buildTest(testCxn: RepositoryConnection, gmCxn: RepositoryConnection, process: String)
    {
        val testFileName = helper.getPostfixfromURI(process) + "AutomaticSnapshotTest"
        val testFilePath = new File("src//test//scala//edu//upenn//turbo//AutoGenTests//" + testFileName + ".scala")
        testFilePath.getParentFile().mkdirs()
        assert(!testFilePath.exists(), s"File $testFileName.scala already exists")

        val inputs = RunDrivetrainProcess.getInputs(process)
        
        val inputTriplesArr = generateInputTriples(process, inputs, gmCxn)
        val fullTripleSet = inputTriplesArr(0)
        val minimumTripleSet = inputTriplesArr(1)
        
        println(fullTripleSet)
        
        update.updateSparql(testCxn, fullTripleSet)
        val queryResultMax = RunDrivetrainProcess.runProcess(process)
        val outputNamedGraph = queryResultMax(process).defaultOutputGraph

        val outputPredsMax = getOutputPredicates(testCxn, outputNamedGraph)
        
        helper.deleteAllTriplesInDatabase(testCxn)
        
        update.updateSparql(testCxn, minimumTripleSet)
        val queryResultMin = RunDrivetrainProcess.runProcess(process)
        val outputPredsMin = getOutputPredicates(testCxn, outputNamedGraph)
        
        helper.deleteAllTriplesInDatabase(testCxn)
        
        writeTestFile(testFileName, testFilePath, fullTripleSet, outputPredsMax, minimumTripleSet, outputPredsMin)
    }
    
    def writeTestFile(testFileName: String, testFilePath: File, requiredInputTriples: String, outputPredsMax: ArrayBuffer[String], minimumInputTriples: String, outputPredsMin: ArrayBuffer[String])
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

        val classAssertion = s"class $testFileName extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers {\n"
        
        val clearTestingRepo = "val clearTestingRepositoryAfterRun: Boolean = false\n"

        val beforesAndAfters = """
        override def beforeAll()
        {
            graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(true)
            testCxn = graphDBMaterials.getTestConnection()
            gmCxn = graphDBMaterials.getGmConnection()
            helper.deleteAllTriplesInDatabase(testCxn)
            
            RunDrivetrainProcess.setGraphModelConnection(gmCxn)
            RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
            RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
            RunDrivetrainProcess.setInputNamedGraphsCache(false)
        }

        override def afterAll()
        {
            ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
        }
        
        before
        {
            helper.deleteAllTriplesInDatabase(testCxn)
        }
        
        """
        
        val insertFullInputDataset = s"""
          val insertFullInputDataset = 
          \"\"\"$requiredInputTriples\"\"\"
          update.updateSparql(cxn, insertFullInputDataset)
          
          """

        val pw = new PrintWriter(testFilePath)
        var fullTestClassString = packageString + imports + classAssertion + clearTestingRepo + beforesAndAfters + insertFullInputDataset + "}"
        pw.write(fullTestClassString)
        pw.close()
    }
    
    def generateInputTriples(process: String, inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]], gmCxn: RepositoryConnection): Array[String] =
    {
        var generatedRequiredTriples = new HashMap[String, HashSet[String]]
        var generatedOptionalTriples = new HashMap[String, HashSet[String]]
        
        val defaultInputGraph = inputs(0)(GRAPH.toString).toString
        
        val optionalInputs = new ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]]
        for (input <- inputs)
        {
            var thisGraph = defaultInputGraph
            if (input(GRAPHOFCREATINGPROCESS.toString) != null) thisGraph = input(GRAPHOFCREATINGPROCESS.toString).toString
            if (input(GRAPHOFORIGIN.toString) != null) thisGraph = input(GRAPHOFORIGIN.toString).toString
            
            val inputType = input(INPUTTYPE.toString).toString
            val optionalBlock = input(OPTIONALGROUP.toString)
            if (optionalBlock == null && inputType == "https://github.com/PennTURBO/Drivetrain/hasRequiredInput")
            {
                if (generatedRequiredTriples.contains(thisGraph)) generatedRequiredTriples(thisGraph) += makeInstanceDataFromTriple(gmCxn, input)
                else generatedRequiredTriples += thisGraph -> HashSet(makeInstanceDataFromTriple(gmCxn, input))
            }
            else optionalInputs += input
        }
        for (input <- optionalInputs)
        {
            var thisGraph = defaultInputGraph
            if (input(GRAPHOFCREATINGPROCESS.toString) != null) thisGraph = input(GRAPHOFCREATINGPROCESS.toString).toString
            if (input(GRAPHOFORIGIN.toString) != null) thisGraph = input(GRAPHOFORIGIN.toString).toString
            
            if (generatedOptionalTriples.contains(thisGraph)) generatedOptionalTriples(thisGraph) += makeInstanceDataFromTriple(gmCxn, input)
            else generatedOptionalTriples += thisGraph -> HashSet(makeInstanceDataFromTriple(gmCxn, input))
        }
        var generatedRequiredTriplesAsString = ""
        var generatedOptionalTriplesAsString = ""
        for ((graph,triples) <- generatedRequiredTriples) 
        {
            generatedRequiredTriplesAsString += "GRAPH <" + graph + "> {\n"
            for (triple <- triples) generatedRequiredTriplesAsString += triple + "\n"
            generatedRequiredTriplesAsString += "}"
        }
        for ((graph,triples) <- generatedOptionalTriples)
        {
            generatedOptionalTriplesAsString += "GRAPH <" + graph + "> {\n"
            for (triple <- triples) generatedOptionalTriplesAsString += triple + "\n"
            generatedOptionalTriplesAsString += "}"
        }
        Array(
        s"""
            INSERT DATA {
                   $generatedRequiredTriplesAsString
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
    
    def makeInstanceDataFromTriple(gmCxn: RepositoryConnection, input: HashMap[String,org.eclipse.rdf4j.model.Value]): String =
    {
        var thisTripleAsData = ""
        
        val connectionType = input(CONNECTIONRECIPETYPE.toString).toString
        val subjectString = input(SUBJECT.toString).toString
        val predicateString = input(PREDICATE.toString).toString
        val objectString = input(OBJECT.toString).toString
        
        val objectADescriber = input(OBJECTADESCRIBER.toString)
        val subjectADescriber = input(SUBJECTADESCRIBER.toString)
        
        val subjectURI = helper.genTurboIRI()
        val objectURI = helper.genTurboIRI()
        
        if (connectionType == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
        {
            thisTripleAsData += s"<$subjectURI> <$predicateString> <$objectURI> .\n"
            thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"
            thisTripleAsData += s"<$objectURI> rdf:type <$objectString> .\n"
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
            thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"
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
            thisTripleAsData += s"<$objectURI> rdf:type <$objectString> .\n"
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
            if (literalType == "https://github.com/PennTURBO/Drivetrain/StringLiteralResourceList" || literalType == "https://github.com/PennTURBO/Drivetrain/LiteralResourceList") 
            {
                val literalValue = "\"" + UUID.randomUUID().toString().replaceAll("-", "") + "\""
                thisTripleAsData += s"<$subjectURI> <$predicateString> $literalValue^^xsd:String .\n"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/IntegerLiteralResourceList") 
            {
                val literalValue = Math.abs(LocalDateTime.now().hashCode())
                thisTripleAsData += s"<$subjectURI> <$predicateString> $literalValue^^xsd:Integer .\n"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/DoubleLiteralResourceList") 
            {
                val literalValue = Math.abs(LocalDateTime.now().hashCode()) + ".00"
                thisTripleAsData += s"<$subjectURI> <$predicateString> $literalValue^^xsd:Double .\n"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/BooleanLiteralResourceList") 
            {
                val literalValue = true
                thisTripleAsData += s"<$subjectURI> <$predicateString> $literalValue^^xsd:Boolean .\n"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/DateLiteralResourceList") 
            {
                val literalValue = "\"" + LocalDateTime.now() + "\""
                thisTripleAsData += s"<$subjectURI> <$predicateString> $literalValue^^xsd:Date .\n"
            }
        }
        else if (connectionType == "https://github.com/PennTURBO/Drivetrain/TermToLiteralRecipe")
        {
            val literalType = input(GRAPHLITERALTYPE.toString).toString
            var literalValue = ""
            if (literalType == "https://github.com/PennTURBO/Drivetrain/StringLiteralResourceList" || literalType == "https://github.com/PennTURBO/Drivetrain/LiteralResourceList") 
            {
                literalValue = "\"" + UUID.randomUUID().toString().replaceAll("-", "") + "\"^^xsd:String"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/IntegerLiteralResourceList") 
            {
                literalValue = Math.abs(LocalDateTime.now().hashCode()) + "^^xsd:Integer"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/DoubleLiteralResourceList") 
            {
                literalValue = Math.abs(LocalDateTime.now().hashCode()) + ".00^^xsd:Double"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/BooleanLiteralResourceList") 
            {
                literalValue = "true^^xsd:Boolean"
            }
            else if (literalType == "https://github.com/PennTURBO/Drivetrain/DateLiteralResourceList") 
            {
                literalValue = "\"" + LocalDateTime.now().toString() + "\"^^xsd:Date"
            }
            var localSubjectString = subjectString
            if (subjectADescriber != null)
            {
                val descRanges = helper.getDescriberRangesAsList(gmCxn, subjectString)
                if (descRanges.size == 0) localSubjectString = "http://purl.obolibrary.org/obo/BFO_0000001"
                else localSubjectString = descRanges(0)
            }
            thisTripleAsData += s"<$localSubjectString> <$predicateString> $literalValue .\n"
        }
        thisTripleAsData
    }
}