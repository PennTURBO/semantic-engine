package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class AcornFunctionalityTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = true
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    
    override def beforeAll()
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(false)
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
        OntologyLoader.addOntologyFromUrl(gmCxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    after
    {
        helper.clearNamedGraph(gmCxn, defaultPrefix + "instructionSet")
        helper.clearNamedGraph(gmCxn, defaultPrefix + "graphSpecification")
    }
    
    test("optional group using multiple named graphs")
    {
        val insert = s"""
            INSERT DATA
            {
                <$defaultPrefix""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    pmbb:myProcess1 drivetrain:buildsOptionalGroup pmbb:optionalGroup1 .
                    
                    pmbb:connection1 a drivetrain:ObjectConnectionToInstanceRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:ObjectConnectionToInstanceRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:ObjectConnectionToInstanceRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:optionalGroup1 .
                    
                    pmbb:connection4 a drivetrain:ObjectConnectionToInstanceRecipe .
                    pmbb:connection4 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class4 .
                    pmbb:connection4 drivetrain:partOf pmbb:optionalGroup1 .
                    pmbb:connection4 drivetrain:referencedInGraph properties:expandedNamedGraph .
                    
                    pmbb:optionalGroup1 a drivetrain:TurboGraphOptionalGroup .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                }
            }
          """
          update.updateSparql(gmCxn, insert)

          val expectedQuery = """
            INSERT {
            GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <http://www.itmat.upenn.edu/biobank/processes> {
            <processURI> turbo:TURBO_0010184 ?class1 .
            <processURI> turbo:TURBO_0010184 ?class2 .
            <processURI> obo:OBI_0000293 ?class3 .
            <processURI> obo:OBI_0000293 ?class4 .
            <processURI> obo:OBI_0000293 ?class1 .
            <processURI> obo:OBI_0000293 ?class2 .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate2> ?class3 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class3 rdf:type <http://www.itmat.upenn.edu/biobank/class3> .
            }
            OPTIONAL {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?class2 <http://www.itmat.upenn.edu/biobank/predicate3> ?class3 .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             }
         """
          
         checkQueriesForEquivalency("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) 
    }
    
    def checkQueriesForEquivalency(processSpec: String, expectedQuery: String)
    {
        var expectedQueryListBuffer = new ArrayBuffer[String]
        val processQueryMap = RunDrivetrainProcess.runProcess(processSpec)
        val query = processQueryMap(processSpec)
        val queryText = query.getQuery().replaceAll(" ", "").split("\\n")
        val process = query.process
        //logger.info(query.getQuery())
        for (a <- expectedQuery.replaceAll(" ","").split("\\n"))
        {
            if (a.length() != 0)
            {
                val replacement = a/*.substring(0,a.length()-1)*/.replace("localUUID", RunDrivetrainProcess.localUUID).replace("processURI", process)
                expectedQueryListBuffer += replacement 
            }
        }
        var expectedQueryList = expectedQueryListBuffer.toArray
        helper.checkOrderedStringArraysForEquivalency(queryText, expectedQueryList) should be (true)
    }
}