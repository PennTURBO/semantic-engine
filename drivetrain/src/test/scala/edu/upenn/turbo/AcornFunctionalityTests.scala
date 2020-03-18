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
    
    test("delete function works")
    {
        val insert = s"""
            INSERT DATA
            {
                <$defaultPrefix""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:removes pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class4 .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)

          val expectedQuery = s"""DELETE {
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            }
            }
            INSERT {
            GRAPH <$processNamedGraph> {
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
            ?class2 <http://www.itmat.upenn.edu/biobank/predicate3> ?class3 .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
             }
             """
          
         helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true) 
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
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:optionalGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
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
                    
                    drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <$processNamedGraph> {
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
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             }
             """
          
         helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true) 
    }
    
    // this test currently fails - commented out so jenkins job will pass. feature has not yet been implemented.
    /*test("minus group using multiple named graphs")
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
                    pmbb:myProcess1 drivetrain:buildsMinusGroup pmbb:minusGroup1 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:minusGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class4 .
                    pmbb:connection4 drivetrain:partOf pmbb:minusGroup1 .
                    pmbb:connection4 drivetrain:referencedInGraph properties:expandedNamedGraph .
                    
                    pmbb:minusGroup1 a drivetrain:TurboGraphMinusGroup .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <$processNamedGraph> {
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
            MINUS {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?class2 <http://www.itmat.upenn.edu/biobank/predicate3> ?class3 .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             }
             """
          
         helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }*/
    
    test("multiple optional groups in input")
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
                    pmbb:myProcess1 drivetrain:buildsOptionalGroup pmbb:optionalGroup2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:optionalGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class4 .
                    pmbb:connection4 drivetrain:partOf pmbb:optionalGroup2 .
                    
                    pmbb:optionalGroup1 a drivetrain:TurboGraphOptionalGroup .
                    pmbb:optionalGroup2 a drivetrain:TurboGraphOptionalGroup .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <$processNamedGraph> {
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
            OPTIONAL {
            ?class2 <http://www.itmat.upenn.edu/biobank/predicate3> ?class3 .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            OPTIONAL {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             }
             """
          
         helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true) 
    }
    
    test("multiple minus groups in input")
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
                    pmbb:myProcess1 drivetrain:buildsMinusGroup pmbb:minusGroup1 .
                    pmbb:myProcess1 drivetrain:buildsMinusGroup pmbb:minusGroup2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:minusGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class4 .
                    pmbb:connection4 drivetrain:partOf pmbb:minusGroup2 .
                    
                    pmbb:minusGroup1 a drivetrain:TurboGraphMinusGroup .
                    pmbb:minusGroup2 a drivetrain:TurboGraphMinusGroup .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <$expandedNamedGraph> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <$processNamedGraph> {
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
            MINUS {
            ?class2 <http://www.itmat.upenn.edu/biobank/predicate3> ?class3 .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            MINUS {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
             }
             """
          
         helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
    
    test("term to term and term to literal recipes works")
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
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    
                    pmbb:connection1 a drivetrain:TermToTermRecipe .
                    pmbb:connection1 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:TermToLiteralRecipe .
                    pmbb:connection2 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection2 drivetrain:object pmbb:literal1 .
                    
                    pmbb:connection3 a drivetrain:TermToTermRecipe .
                    pmbb:connection3 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:term2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection3 drivetrain:object pmbb:term1 .
                    
                    pmbb:connection4 a drivetrain:TermToLiteralRecipe .
                    pmbb:connection4 drivetrain:multiplicity drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:term2 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection4 drivetrain:object pmbb:literal1 .
                    
                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                }
            }
          """
          update.updateSparql(gmCxn, insert)
          
          val expectedQuery = s"""INSERT {
            GRAPH <$expandedNamedGraph> {
            <http://www.itmat.upenn.edu/biobank/term2> <http://www.itmat.upenn.edu/biobank/predicate2> <http://www.itmat.upenn.edu/biobank/term1> .
            <http://www.itmat.upenn.edu/biobank/term2> <http://www.itmat.upenn.edu/biobank/predicate2> ?literal1 .
            }
            GRAPH <$processNamedGraph> {
            <processURI> turbo:TURBO_0010184 <http://www.itmat.upenn.edu/biobank/term1> .
            <processURI> turbo:TURBO_0010184 <http://www.itmat.upenn.edu/biobank/term2> .
            <processURI> obo:OBI_0000293 <http://www.itmat.upenn.edu/biobank/term1> .
            <processURI> obo:OBI_0000293 <http://www.itmat.upenn.edu/biobank/term2> .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            <http://www.itmat.upenn.edu/biobank/term1> <http://www.itmat.upenn.edu/biobank/predicate1> <http://www.itmat.upenn.edu/biobank/term2> .
            <http://www.itmat.upenn.edu/biobank/term1> <http://www.itmat.upenn.edu/biobank/predicate1> ?literal1 .
            }
             }
             """
          
         helper.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
}