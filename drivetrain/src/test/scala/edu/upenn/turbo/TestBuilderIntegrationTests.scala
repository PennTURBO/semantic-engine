package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class TestBuilderIntegrationTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    
    override def beforeAll()
    {
        assert("test" === System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"test\"; check your build.sbt file")
        
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(false)
        cxn = graphDBMaterials.getConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(cxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(cxn)
        OntologyLoader.addOntologyFromUrl(gmCxn)
        
        helper.clearNamedGraph(gmCxn, defaultPrefix + "instructionSet")
        helper.clearNamedGraph(gmCxn, defaultPrefix + "graphSpecification")
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
    
    test("triples generation test")
    {
        val insert = s"""
            INSERT DATA
            {
                <$defaultPrefix""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection5 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection6 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection7 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection8 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection9 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection2 drivetrain:subject pmbb:class2 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:many-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class4 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class5 .
                    
                    pmbb:connection5 a drivetrain:InstanceToTermRecipe .
                    pmbb:connection5 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection5 drivetrain:subject pmbb:class1 .
                    pmbb:connection5 drivetrain:predicate pmbb:predicate5 .
                    pmbb:connection5 drivetrain:object pmbb:term1 .
                    
                    pmbb:connection6 a drivetrain:TermToInstanceRecipe .
                    pmbb:connection6 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection6 drivetrain:subject pmbb:term2 .
                    pmbb:connection6 drivetrain:predicate pmbb:predicate6 .
                    pmbb:connection6 drivetrain:object pmbb:class1 .
                    
                    pmbb:connection7 a drivetrain:InstanceToLiteralRecipe .
                    pmbb:connection7 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection7 drivetrain:subject pmbb:class1 .
                    pmbb:connection7 drivetrain:predicate pmbb:predicate7 .
                    pmbb:connection7 drivetrain:object pmbb:literal1 .
                    
                    pmbb:connection8 a drivetrain:TermToLiteralRecipe .
                    pmbb:connection8 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection8 drivetrain:subject pmbb:term1 .
                    pmbb:connection8 drivetrain:predicate pmbb:predicate8 .
                    pmbb:connection8 drivetrain:object pmbb:literal2 .
                    
                    pmbb:connection9 a drivetrain:TermToTermRecipe .
                    pmbb:connection9 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection9 drivetrain:subject pmbb:term1 .
                    pmbb:connection9 drivetrain:predicate pmbb:predicate9 .
                    pmbb:connection9 drivetrain:object pmbb:term2 .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                    pmbb:class5 a owl:Class .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                    pmbb:literal2 a drivetrain:LiteralResourceList .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                    drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
                    drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)
          val inputs = RunDrivetrainProcess.getInputs("http://www.itmat.upenn.edu/biobank/myProcess1")
          val testBuilder = new TestBuilder()
          val generatedTriples = testBuilder.generateInputTriples("http://www.itmat.upenn.edu/biobank/myProcess1", inputs, gmCxn)
          val fullTripleSet = generatedTriples(0)
          update.updateSparql(cxn, "INSERT DATA {" + fullTripleSet + "}")
          
          val checkTriples = """
             ASK
             {
                 Graph pmbb:Shortcuts
                 {
                     pmbb:class1_1 a pmbb:class1 .
                     pmbb:class2_1 a pmbb:class2 .
                     pmbb:class3_1 a pmbb:class3 .
                     pmbb:class3_2 a pmbb:class3 .
                     pmbb:class4_1 a pmbb:class4 .
                     pmbb:class4_2 a pmbb:class4 .
                     pmbb:class4_3 a pmbb:class4 .
                     pmbb:class4_4 a pmbb:class4 .
                     pmbb:class5_1 a pmbb:class5 .
                     pmbb:class5_2 a pmbb:class5 .
                     
                     pmbb:class1_1 pmbb:predicate1 pmbb:class2_1 .
                     pmbb:class2_1 pmbb:predicate2 pmbb:class3_1 .
                     pmbb:class2_1 pmbb:predicate2 pmbb:class3_2 .
                     pmbb:class4_1 pmbb:predicate3 pmbb:class3_1 .
                     pmbb:class4_2 pmbb:predicate3 pmbb:class3_1 .
                     pmbb:class4_3 pmbb:predicate3 pmbb:class3_2 .
                     pmbb:class4_4 pmbb:predicate3 pmbb:class3_2 .
                     pmbb:class1_1 pmbb:predicate4 pmbb:class5_1 .
                     pmbb:class1_1 pmbb:predicate4 pmbb:class5_2 .
                     
                     pmbb:class1_1 pmbb:predicate5 pmbb:term1 .
                     pmbb:term2 pmbb:predicate6 pmbb:class1_1 .
                     pmbb:class1_1 pmbb:predicate7 ?literal1 .
                     pmbb:term1 pmbb:predicate8 ?literal2 .
                     pmbb:term1 pmbb:predicate9 pmbb:term2 .
                     
                     filter(isLiteral(?literal1))
                     filter(isLiteral(?literal2))
                 }
             }
            """
          update.querySparqlBoolean(cxn, checkTriples).get should be (true)
          val count: String = s"SELECT * WHERE {?s ?p ?o .}"
          val result = update.querySparqlAndUnpackTuple(cxn, count, "p")
          result.size should be (24)
      }
}