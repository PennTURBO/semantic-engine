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
import org.eclipse.rdf4j.model.Literal
import scala.collection.mutable.Queue

class TestBuilder extends ProjectwideGlobals
{
    //only make all fields? in the case that there are no optional triples
    var onlyAllFields = false
    
    def postMinTripleOutput(cxn: RepositoryConnection, gmCxn: RepositoryConnection, processList: ArrayBuffer[String])
    {
        val UUIDKey = UUID.randomUUID().toString().replaceAll("-", "")
        RunDrivetrainProcess.setGlobalUUID(UUIDKey)
        for (process <- processList)
        {
            val processAsURI = helper.getProcessNameAsUri(process)
            GraphModelValidator.validateProcessSpecification(processAsURI)
            val inputs = RunDrivetrainProcess.getInputs(processAsURI)
            val inputTriplesArr = generateInputTriples(processAsURI, inputs, gmCxn)
            val minimumTripleSet = inputTriplesArr(1)
            update.updateSparql(cxn, minimumTripleSet)
            val queryResultMin = RunDrivetrainProcess.runProcess(processAsURI)
        }
    }
    
    def postMaxTripleOutput(cxn: RepositoryConnection, gmCxn: RepositoryConnection, processList: ArrayBuffer[String])
    {
        val UUIDKey = UUID.randomUUID().toString().replaceAll("-", "")
        RunDrivetrainProcess.setGlobalUUID(UUIDKey)
        for (process <- processList)
        {
            val processAsURI = helper.getProcessNameAsUri(process)
            GraphModelValidator.validateProcessSpecification(processAsURI)
            val inputs = RunDrivetrainProcess.getInputs(processAsURI)
            val inputTriplesArr = generateInputTriples(processAsURI, inputs, gmCxn)
            val fullTripleSet = inputTriplesArr(0)
            update.updateSparql(cxn, fullTripleSet)
            val queryResultMax = RunDrivetrainProcess.runProcess(processAsURI)   
        }
    }
    
    def buildTest(cxn: RepositoryConnection, gmCxn: RepositoryConnection, process: String)
    {
        val UUIDKey = UUID.randomUUID().toString().replaceAll("-", "")
        RunDrivetrainProcess.setGlobalUUID(UUIDKey)
        
        val testFileName = helper.getPostfixfromURI(process) + "SnapshotTest"
        val instructionSetName = instructionSetFile.split("\\.")(0)
        val testFilePath = new File("src//test//scala//edu//upenn//turbo//AutoGenTests//" + testFileName + "_" + instructionSetName + ".snapshot")
        testFilePath.getParentFile().mkdirs()
        
        val inputs = RunDrivetrainProcess.getInputs(process)
        
        val inputTriplesArr = generateInputTriples(process, inputs, gmCxn)
        val fullTripleSet = inputTriplesArr(0)
        val minimumTripleSet = inputTriplesArr(1)
        
        println(fullTripleSet)
        
        update.updateSparql(cxn, "INSERT DATA {" + fullTripleSet + "}")
        val queryResultMax = RunDrivetrainProcess.runProcess(process)
        val outputNamedGraph = helper.checkAndConvertPropertiesReferenceToNamedGraph(queryResultMax(process).defaultOutputGraph)
        val outputPredsMax = getOutputPredicates(cxn, outputNamedGraph)
        assert (outputPredsMax.size > 0, s"""Process $process did not create any output based on the "all fields" input.
        This is likely a bug in the TestBuilder code.
        Check that the required and optional triples above are formatted as expected for an input to this process, and call Hayden.""")
        
        helper.deleteAllTriplesInDatabase(cxn)
        
        update.updateSparql(cxn, "INSERT DATA {" + minimumTripleSet + "}")
        val queryResultMin = RunDrivetrainProcess.runProcess(process)
        val outputPredsMin = getOutputPredicates(cxn, outputNamedGraph)
        assert (outputPredsMax.size > 0, s"""Process $process did not create any output based on the "minimum fields" input.
        This is likely a bug in the TestBuilder code.
        Check that the required triples above are formatted as expected for an input to this process, and call Hayden.""")
        
        helper.deleteAllTriplesInDatabase(cxn)

        logger.info("Writing test file...")
        
        writeTestFile(testFileName, process, outputNamedGraph, testFilePath, fullTripleSet, outputPredsMax, 
                      minimumTripleSet, outputPredsMin, instructionSetName, UUIDKey)

        logger.info("Test created in src//test//scala//edu//upenn//turbo//AutoGenTests//")

    }
    
    def writeTestFile(testFileName: String, process: String, outputNamedGraph: String, 
                      testFilePath: File, maximumInputTriples: String, outputPredsMax: ArrayBuffer[ArrayBuffer[org.eclipse.rdf4j.model.Value]], 
                      minimumInputTriples: String, outputPredsMin: ArrayBuffer[ArrayBuffer[org.eclipse.rdf4j.model.Value]], instructionSetName: String,
                      UUIDKey: String)
    {
        val maxOutputTriplesAsString = convertOutputTriplesToString(outputPredsMax)
        val minOutputTriplesAsString = convertOutputTriplesToString(outputPredsMin)
        
        val outputNamedGraphDec = s"outputNamedGraph -> $outputNamedGraph"
        val instructionSetFileDec = s"instructionSetFile -> $instructionSetName"
        val UUIDKeyDec = s"UUIDKey -> $UUIDKey"
        val processDec = s"UpdateSpecificationURI -> $process"
        val maxInputTriplesDec = s"allInputTriples -> $maximumInputTriples"
        val minInputTriplesDec = s"minimumInputTriples -> $minimumInputTriples"
        val maxOutputTriplesDec = s"maximumOutputTriples -> $maxOutputTriplesAsString"
        val minOutputTriplesDec = s"minimumOutputTriples -> $minOutputTriplesAsString"

        val pw = new PrintWriter(testFilePath)
        var fullTestClassString = instructionSetFileDec + "\n" + outputNamedGraphDec + "\n" + UUIDKeyDec + "\n" + processDec + "\n" + 
                                  maxInputTriplesDec + "\n" + minInputTriplesDec + "\n" + maxOutputTriplesDec + "\n\n" + minOutputTriplesDec
        pw.write(fullTestClassString)
        pw.close()
    }
    
    def convertOutputTriplesToString(outputPredsList: ArrayBuffer[ArrayBuffer[org.eclipse.rdf4j.model.Value]]): String =
    {
        var fullPredsAsString = ""
        for (index <- 0 to outputPredsList.size-1) 
        {
            if (!outputPredsList(index)(2).isInstanceOf[Literal]) fullPredsAsString += "\"\"\"<"+outputPredsList(index)(0)+"> <"+outputPredsList(index)(1)+"> <"+outputPredsList(index)(2)+">\"\"\""
            else fullPredsAsString += "\"\"\"<"+outputPredsList(index)(0)+"> <"+outputPredsList(index)(1)+"> "+outputPredsList(index)(2)+"\"\"\""
            if (index != outputPredsList.size-1) fullPredsAsString += ",\n"
        }
        fullPredsAsString
    }
    
    def getInstanceCountsFromInput(inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]]): HashMap[String, Integer] =
    {
        var instanceCountMap = new HashMap[String, Integer]
        val instancesWithOnlyTermsOrLiterals = new HashSet[String]
        val connectionsMap = new HashMap[String, HashSet[String]]
        for (input <- inputs)
        {
            val subjectString = input(SUBJECT.toString).toString
            val objectString = input(OBJECT.toString).toString
            val subjectExistsInMap: Boolean = instanceCountMap.contains(subjectString)
            val objectExistsInMap: Boolean = instanceCountMap.contains(objectString)
            val multiplicity = input(MULTIPLICITY.toString).toString
            if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
            {
                if (!subjectExistsInMap && !objectExistsInMap) 
                {
                    if (multiplicity == oneToOneMultiplicity)
                    {
                        instanceCountMap += subjectString -> 1
                        instanceCountMap += objectString -> 1
                    }
                    else if (multiplicity == oneToManyMultiplicity)
                    {
                        instanceCountMap += subjectString -> 1
                        instanceCountMap += objectString -> 2
                    }
                    else if (multiplicity == manyToOneMultiplicity)
                    {
                        instanceCountMap += subjectString -> 2
                        instanceCountMap += objectString -> 1
                    }
                }
                else if (!subjectExistsInMap && objectExistsInMap)
                {
                    if (multiplicity == oneToOneMultiplicity) instanceCountMap += subjectString -> instanceCountMap(objectString)
                    else if (multiplicity == oneToManyMultiplicity)
                    {
                        if (instanceCountMap(objectString) > 1) instanceCountMap += subjectString -> instanceCountMap(objectString)/2
                        else
                        {
                            instanceCountMap += subjectString -> 1
                            instanceCountMap(objectString) = 2
                            // recursively iterate through all connections of objectString, multiply by 2
                        }
                    }
                    else if (multiplicity == manyToOneMultiplicity) instanceCountMap += subjectString -> instanceCountMap(objectString)*2
                }
                else if (subjectExistsInMap && !objectExistsInMap)
                {
                    if (multiplicity == oneToOneMultiplicity) instanceCountMap += objectString -> instanceCountMap(subjectString)
                    else if (multiplicity == manyToOneMultiplicity)
                    {
                        if (instanceCountMap(subjectString) > 1) instanceCountMap += objectString -> instanceCountMap(subjectString)/2
                        else
                        {
                            instanceCountMap += objectString -> 1
                            instanceCountMap(subjectString) = 2
                            // recursively iterate through all connections of subjectString, multiply by 2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, subjectString, 2)
                        }
                    }
                    else if (multiplicity == oneToManyMultiplicity) instanceCountMap += objectString -> instanceCountMap(subjectString)*2
                }
                else if (subjectExistsInMap && objectExistsInMap)
                {
                    if (multiplicity == oneToOneMultiplicity && (instanceCountMap(subjectString) != instanceCountMap(objectString)))
                    {
                        if (instanceCountMap(subjectString) > instanceCountMap(objectString)) instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, objectString, instanceCountMap(subjectString)/instanceCountMap(objectString))
                        else instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, subjectString, instanceCountMap(objectString)/instanceCountMap(subjectString))
                    }
                    else if (multiplicity == manyToOneMultiplicity)
                    {
                        if (instanceCountMap(subjectString) <= instanceCountMap(objectString))
                        {
                            val multiplier = instanceCountMap(objectString)*2/instanceCountMap(subjectString)
                            instanceCountMap(subjectString) = instanceCountMap(objectString)*2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, subjectString, multiplier)
                        }  
                    }
                    else if (multiplicity == oneToManyMultiplicity)
                    {
                        if (instanceCountMap(objectString) <= instanceCountMap(subjectString))
                        {
                            val multiplier = instanceCountMap(subjectString)*2/instanceCountMap(objectString)
                            instanceCountMap(objectString) = instanceCountMap(subjectString)*2
                            instanceCountMap = updateMultiplicityChangeThroughConnectionsList(instanceCountMap, connectionsMap, objectString, multiplier)
                        }  
                    }
                }
                if (connectionsMap.contains(subjectString)) connectionsMap(subjectString) += objectString
                else connectionsMap += subjectString -> HashSet(objectString)
                if (connectionsMap.contains(objectString)) connectionsMap(objectString) += subjectString
                else connectionsMap += objectString -> HashSet(subjectString)
            }
            else if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToTermRecipe" && !subjectExistsInMap) instancesWithOnlyTermsOrLiterals += subjectString
            else if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/TermToInstanceRecipe" && !objectExistsInMap) instancesWithOnlyTermsOrLiterals += objectString
            else if (input(CONNECTIONRECIPETYPE.toString).toString == "https://github.com/PennTURBO/Drivetrain/InstanceToLiteralRecipe" && !subjectExistsInMap) instancesWithOnlyTermsOrLiterals += subjectString
        }
        for (instance <- instancesWithOnlyTermsOrLiterals)
        {
            if (!instanceCountMap.contains(instance)) instanceCountMap += instance -> 1
        }
        println("Creating test data with the following instance counts:")
        for ((k,v) <- instanceCountMap) println("Instance: " + k + " Count: " + v)
        instanceCountMap
    }
    
    def updateMultiplicityChangeThroughConnectionsList(instanceCountMap: HashMap[String, Integer], connectionsList: HashMap[String, HashSet[String]], start: String, multiplier: Integer): HashMap[String, Integer] =
    {
        val updateQueue = new Queue[String]
        val alreadyUpdated = HashSet(start)
        for (cxn <- connectionsList(start)) updateQueue += cxn
        while (!updateQueue.isEmpty)
        {
            val firstElement = updateQueue.dequeue
            instanceCountMap(firstElement) = instanceCountMap(firstElement)*multiplier
            alreadyUpdated += firstElement
            for (cxn <- connectionsList(firstElement)) if (!alreadyUpdated.contains(cxn)) updateQueue += cxn
        }
        instanceCountMap
    }
    
    def generateInputTriples(process: String, inputs: ArrayBuffer[HashMap[String,org.eclipse.rdf4j.model.Value]], gmCxn: RepositoryConnection): Array[String] =
    {
        var generatedRequiredTriples = new HashMap[String, HashSet[String]]
        var generatedOptionalTriples = new HashMap[String, HashSet[String]]
        
        val defaultInputGraph = inputs(0)(GRAPH.toString).toString
        
        val instanceCounts = getInstanceCountsFromInput(inputs)
        
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
                for (triple <- makeInstanceDataFromTriple(gmCxn, input, instanceCounts)) generatedRequiredTriples(thisGraph) += triple
            }
            else
            {
                if (!generatedOptionalTriples.contains(thisGraph)) generatedOptionalTriples += thisGraph -> new HashSet[String]
                for (triple <- makeInstanceDataFromTriple(gmCxn, input, instanceCounts)) generatedOptionalTriples(thisGraph) += triple
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
             # Required triples
             $generatedRequiredTriplesAsString
             # Optional triples
             $generatedOptionalTriplesAsString
        """,
        s"""
             $generatedRequiredTriplesAsString
        """
        )
    }
    
    def getOutputPredicates(cxn: RepositoryConnection, outputNamedGraph: String): ArrayBuffer[ArrayBuffer[org.eclipse.rdf4j.model.Value]] =
    {
        update.querySparqlAndUnpackTuple(cxn, s"Select * Where { Graph <$outputNamedGraph> { ?s ?p ?o . }}", Array("s", "p", "o"))
    }
    
    def makeInstanceDataFromTriple(gmCxn: RepositoryConnection, input: HashMap[String,org.eclipse.rdf4j.model.Value], instanceCounts: HashMap[String, Integer]): ArrayBuffer[String] =
    {
        var thisTripleAsData = new ArrayBuffer[String]
        
        val connectionType = input(CONNECTIONRECIPETYPE.toString).toString
        val subjectString = input(SUBJECT.toString).toString
        val predicateString = input(PREDICATE.toString).toString
        val objectString = input(OBJECT.toString).toString
        
        val objectADescriber = input(OBJECTADESCRIBER.toString)
        val subjectADescriber = input(SUBJECTADESCRIBER.toString)
        
        val subjectContext = input(SUBJECTCONTEXT.toString)
        val objectContext = input(OBJECTCONTEXT.toString)
        
        val subjectInstanceArray = new ArrayBuffer[String]
        val objectInstanceArray = new ArrayBuffer[String]
        
        if (instanceCounts.contains(subjectString))
        {
            for (instance <- 1 to instanceCounts(subjectString))
            {
                var subjectURI = subjectString + "_" + instance
                if (subjectContext != null) subjectURI += "_" + helper.convertTypeToSparqlVariable(subjectContext.toString, false)
                subjectInstanceArray += subjectURI
            } 
        }
        if (instanceCounts.contains(objectString))
        {
            for (instance <- 1 to instanceCounts(objectString))
            {
                var objectURI = objectString + "_" + instance
                if (objectContext != null) objectURI += "_" + helper.convertTypeToSparqlVariable(objectContext.toString, false)
                objectInstanceArray += objectURI
            }   
        }
        
        if (connectionType == "https://github.com/PennTURBO/Drivetrain/InstanceToInstanceRecipe")
        {
            if (subjectInstanceArray.size == objectInstanceArray.size)
            {
                for (index <- 0 to subjectInstanceArray.size-1)
                {
                    thisTripleAsData += "<"+subjectInstanceArray(index)+"> <"+predicateString+"> <"+objectInstanceArray(index)+"> .\n"
                    if (subjectADescriber == null) thisTripleAsData += "<"+subjectInstanceArray(index)+"> rdf:type <"+subjectString+"> .\n"
                    if (objectADescriber == null) thisTripleAsData += s"<"+objectInstanceArray(index)+"> rdf:type <"+objectString+"> .\n"   
                }
            }
            else if ((subjectInstanceArray.size / objectInstanceArray.size) > 0)
            {
                val divisor = subjectInstanceArray.size / objectInstanceArray.size
                for (index <- 0 to subjectInstanceArray.size-1)
                {
                    thisTripleAsData += "<"+subjectInstanceArray(index)+"> <"+predicateString+"> <"+objectInstanceArray(index/divisor)+"> .\n"
                    if (subjectADescriber == null) thisTripleAsData += "<"+subjectInstanceArray(index)+"> rdf:type <"+subjectString+"> .\n"
                    if (objectADescriber == null) thisTripleAsData += s"<"+objectInstanceArray(index/divisor)+"> rdf:type <"+objectString+"> .\n"   
                }
            }
            else if ((objectInstanceArray.size / subjectInstanceArray.size) > 0)
            {
                val divisor = objectInstanceArray.size / subjectInstanceArray.size
                for (index <- 0 to objectInstanceArray.size-1)
                {
                    thisTripleAsData += "<"+subjectInstanceArray(index/divisor)+"> <"+predicateString+"> <"+objectInstanceArray(index)+"> .\n"
                    if (subjectADescriber == null) thisTripleAsData += "<"+subjectInstanceArray(index/divisor)+"> rdf:type <"+subjectString+"> .\n"
                    if (objectADescriber == null) thisTripleAsData += s"<"+objectInstanceArray(index)+"> rdf:type <"+objectString+"> .\n"   
                }
            }
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
            for (subjectURI <- subjectInstanceArray)
            {
                thisTripleAsData += s"<$subjectURI> <$predicateString> <$localObjectString> .\n"
                if (subjectADescriber == null) thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"   
            }
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
            for (objectURI <- objectInstanceArray)
            {
                thisTripleAsData += s"<$localSubjectString> <$predicateString> <$objectURI> .\n"
                if (objectADescriber == null) thisTripleAsData += s"<$objectURI> rdf:type <$objectString> .\n" 
            }
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
            for (subjectURI <- subjectInstanceArray)
            {
                thisTripleAsData += makeTripleWithLiteral(literalType, objectString, subjectURI, predicateString)
                if (subjectADescriber == null) thisTripleAsData += s"<$subjectURI> rdf:type <$subjectString> .\n"   
            }
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
              val lh = objectString.hashCode().toString()
              val literalValue = "\"" + lh.charAt(1)+lh.charAt(2)+"/"+lh.charAt(3)+lh.charAt(4)+"/"+lh.charAt(5)+lh.charAt(6)+ "\""
              thisTripleAsData = s"<$subjectURI> <$predicateString> $literalValue^^xsd:Date .\n"
          }
         thisTripleAsData
    }
}