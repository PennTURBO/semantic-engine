package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class AcornFunctionalityTests extends FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = true
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    var graphDBMaterials: TurboGraphConnection = null
    
    override def beforeAll()
    {
        assert("test" === System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"test\"; check your build.sbt file")
        
        graphDBMaterials = ConnectToGraphDB.initializeGraph()
        DrivetrainDriver.updateModel(graphDBMaterials)
        Globals.cxn = graphDBMaterials.getConnection()
        Globals.gmCxn = graphDBMaterials.getGmConnection()
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
        
        OntologyLoader.addOntologyFromUrl(Globals.gmCxn)
        
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "graphSpecification")
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    after
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "graphSpecification")
    }
    
    test("delete function works")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:removes pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:class1 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate4 .
                    pmbb:connection4 drivetrain:object pmbb:class4 .
                    
                    pmbb:class1 a owl:Class .
                    pmbb:class2 a owl:Class .
                    pmbb:class3 a owl:Class .
                    pmbb:class4 a owl:Class .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)

          val expectedQuery = s"""DELETE {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            }
            }
            INSERT {
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class4 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class2 .
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
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true) 
    }
    
    test("optional group using multiple named graphs")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
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
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:optionalGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-1 .
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
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class1 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class2 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class4 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class2 .
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
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true) 
    }
    
    test("minus group using multiple named graphs")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
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
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:minusGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-1 .
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
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class1 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class2 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class4 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class2 .
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
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
    
    test("multiple optional groups in input")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
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
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:optionalGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-1 .
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
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)

          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class1 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class2 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class4 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class2 .
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
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true) 
    }
    
    test("multiple minus groups in input")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
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
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:class1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:class2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:class1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:class3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:class2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate3 .
                    pmbb:connection3 drivetrain:object pmbb:class3 .
                    pmbb:connection3 drivetrain:partOf pmbb:minusGroup1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-1 .
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
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)

          // The Minus Group implementation creates a new GRAPH clause even if the GRAPH referenced by the minus group is the same
          // as a graph that already exists in the query. This is different than the Optional Group implementation which "tucks"
          // Optional Groups into the GRAPH clause that already exists when applicable (see above test). There isn't necessarily a strong
          // reason that Minus Groups does not do things that way, and the code can be changed to work that way in the QueryClauseStructure class
          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate1> ?class2 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class1 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?class2 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class4 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?class2 .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate2> ?class3 .
            ?class1 rdf:type <http://www.itmat.upenn.edu/biobank/class1> .
            ?class3 rdf:type <http://www.itmat.upenn.edu/biobank/class3> .
            }
             }
           MINUS {
           GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?class2 <http://www.itmat.upenn.edu/biobank/predicate3> ?class3 .
            ?class2 rdf:type <http://www.itmat.upenn.edu/biobank/class2> .
            }
            }
            MINUS {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?class1 <http://www.itmat.upenn.edu/biobank/predicate4> ?class4 .
            ?class4 rdf:type <http://www.itmat.upenn.edu/biobank/class4> .
            }
            }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
    
    test("term to term and term to literal recipes works")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    
                    pmbb:connection1 a drivetrain:TermToTermRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:TermToLiteralRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection2 drivetrain:object pmbb:literal1 .
                    
                    pmbb:connection3 a drivetrain:TermToTermRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:term2 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection3 drivetrain:object pmbb:term1 .
                    
                    pmbb:connection4 a drivetrain:TermToLiteralRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection4 drivetrain:subject pmbb:term2 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection4 drivetrain:object pmbb:literal1 .
                    
                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            <http://www.itmat.upenn.edu/biobank/term2> <http://www.itmat.upenn.edu/biobank/predicate2> <http://www.itmat.upenn.edu/biobank/term1> .
            <http://www.itmat.upenn.edu/biobank/term2> <http://www.itmat.upenn.edu/biobank/predicate2> ?literal1 .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> <http://www.itmat.upenn.edu/biobank/term1> .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> <http://www.itmat.upenn.edu/biobank/term2> .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/term1> .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/term2> .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            <http://www.itmat.upenn.edu/biobank/term1> <http://www.itmat.upenn.edu/biobank/predicate1> <http://www.itmat.upenn.edu/biobank/term2> .
            <http://www.itmat.upenn.edu/biobank/term1> <http://www.itmat.upenn.edu/biobank/predicate1> ?literal1 .
            }
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
    
    // The next two tests test the ability to use literals as cardinality enforcers, which is not implemented.
    /*test("multiplicity check for 1-many instance to literal - direct method")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection3 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection2 drivetrain:subject pmbb:term2 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection2 drivetrain:object pmbb:term3 .
                    
                    pmbb:connection3 a drivetrain:InstanceToLiteralRecipe .
                    pmbb:connection3 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection3 drivetrain:subject pmbb:term3 .
                    pmbb:connection3 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection3 drivetrain:object pmbb:literal1 .
                    
                    pmbb:connection4 a drivetrain:InstanceToLiteralRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection4 drivetrain:subject pmbb:term4 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection4 drivetrain:object pmbb:literal1 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    pmbb:term3 a owl:Class .
                    pmbb:term4 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                    drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(gmCxn, insert)
          
          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?term1 <http://www.itmat.upenn.edu/biobank/predicate1> ?term2 .
            ?term1 rdf:type <http://www.itmat.upenn.edu/biobank/term1> .
            ?term2 <http://www.itmat.upenn.edu/biobank/predicate1> ?term3 .
            ?term2 rdf:type <http://www.itmat.upenn.edu/biobank/term2> .
            ?term3 <http://www.itmat.upenn.edu/biobank/predicate1> ?literal1 .
            ?term3 rdf:type <http://www.itmat.upenn.edu/biobank/term3> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term1 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term2 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?term4 .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?term4 <http://www.itmat.upenn.edu/biobank/predicate1> ?literal1 .
            ?term4 rdf:type <http://www.itmat.upenn.edu/biobank/term4> .
            }
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term1\",\"localUUID\", str(?term4))))) AS ?term1)
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term2\",\"localUUID\", str(?term4))))) AS ?term2)
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term3\",\"localUUID\", str(?literal1), str(?term4))))) AS ?term4)
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
    
    test("multiplicity check for 1-many instance to literal - instance count method")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection2 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection4 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection2 drivetrain:subject pmbb:term2 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection2 drivetrain:object pmbb:term3 .
                    
                    pmbb:connection4 a drivetrain:InstanceToLiteralRecipe .
                    pmbb:connection4 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection4 drivetrain:subject pmbb:term4 .
                    pmbb:connection4 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection4 drivetrain:object pmbb:literal1 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    pmbb:term3 a owl:Class .
                    pmbb:term4 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                    
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                    drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(gmCxn, insert)
          
          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?term1 <http://www.itmat.upenn.edu/biobank/predicate1> ?term2 .
            ?term1 rdf:type <http://www.itmat.upenn.edu/biobank/term1> .
            ?term2 <http://www.itmat.upenn.edu/biobank/predicate1> ?term3 .
            ?term2 rdf:type <http://www.itmat.upenn.edu/biobank/term2> .
            ?term3 rdf:type <http://www.itmat.upenn.edu/biobank/term3> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term1 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term2 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term3 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?term4 .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?term4 <http://www.itmat.upenn.edu/biobank/predicate1> ?literal1 .
            ?term4 rdf:type <http://www.itmat.upenn.edu/biobank/term4> .
            }
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term1\",\"localUUID\", str(?term4))))) AS ?term1)
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term2\",\"localUUID\", str(?term4))))) AS ?term2)
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term3\",\"localUUID\", str(?literal1), str(?term4))))) AS ?term4)
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }*/
    
    test("cardinality enforcement with only instance-to-term in input")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToTermRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:term3 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?term1 <http://www.itmat.upenn.edu/biobank/predicate1> ?term2 .
            ?term2 rdf:type <http://www.itmat.upenn.edu/biobank/term2> .
            ?term1 rdf:type <http://www.itmat.upenn.edu/biobank/term1> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term2 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?term1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/term3> .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            ?term1 <http://www.itmat.upenn.edu/biobank/predicate2> <http://www.itmat.upenn.edu/biobank/term3> .
            ?term1 rdf:type <http://www.itmat.upenn.edu/biobank/term1> .
            }
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term2\",\"localUUID\", str(?term1))))) AS ?term2)
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
    
    test("cardinality enforcement with only term-to-instance in input")
    {
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    pmbb:myProcess1 a turbo:TURBO_0010354 .
                    pmbb:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    pmbb:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    pmbb:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    pmbb:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-many .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:TermToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term3 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:term1 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
          val expectedQuery = s"""INSERT {
            GRAPH <${Globals.expandedNamedGraph}> {
            ?term1 <http://www.itmat.upenn.edu/biobank/predicate1> ?term2 .
            ?term2 rdf:type <http://www.itmat.upenn.edu/biobank/term2> .
            ?term1 rdf:type <http://www.itmat.upenn.edu/biobank/term1> .
            }
            GRAPH <${Globals.processNamedGraph}> {
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term2 .
            <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?term1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?term1 .
            <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> <http://www.itmat.upenn.edu/biobank/term3> .
            }
            }
            WHERE {
            GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts> {
            <http://www.itmat.upenn.edu/biobank/term3> <http://www.itmat.upenn.edu/biobank/predicate2> ?term1 .
            ?term1 rdf:type <http://www.itmat.upenn.edu/biobank/term1> .
            }
            BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(\"?term2\",\"localUUID\", str(?term1))))) AS ?term2)
             }
             """
          
         Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/myProcess1", expectedQuery) should be (true)
    }
}