package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class GraphModelValidationTests extends FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    var graphDBMaterials: TurboGraphConnection = null
    
    override def beforeAll()
    {
        assert("test" === System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"test\"; check your build.sbt file")
        
        graphDBMaterials = ConnectToGraphDB.initializeGraph()
        DrivetrainDriver.updateModel(graphDBMaterials, "carnival_transformation_instructions.tis", "turbo_valid_graph_specification.gs")
        Globals.cxn = graphDBMaterials.getConnection()
        Globals.gmCxn = graphDBMaterials.getGmConnection()
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    before
    {
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "graphSpecification")
        
        val insert = s"""
        INSERT DATA { Graph <${Globals.defaultPrefix}""" + s"""instructionSet> {
          ontologies:myProcess1 a ontologies:TURBO_0010354 ;
              drivetrain:inputNamedGraph <${Globals.expandedNamedGraph}> ;
              drivetrain:outputNamedGraph <${Globals.expandedNamedGraph}> ; 
              drivetrain:hasOutput ontologies:object1ToObject2 ;
              drivetrain:hasRequiredInput ontologies:object1ToObject3 ;
              drivetrain:hasRequiredInput ontologies:object2ToObject3 ;
          .
              
          turbo:object1 a owl:Class .
          turbo:object2 a owl:Class .
          turbo:object3 a owl:Class .
          turbo:object4 a owl:Class .
          turbo:object5 a owl:Class .
          turbo:object6 a owl:Class .
          turbo:object7 a owl:Class .
          turbo:object8 a owl:Class .
          turbo:object9 a owl:Class .
          turbo:object10 a owl:Class .
          
          drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
              
          ontologies:object1ToObject2
              a drivetrain:InstanceToInstanceRecipe ;
              drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
              drivetrain:object turbo:object2 ;
              drivetrain:predicate turbo:pred1 ;
              drivetrain:subject turbo:object1 ;
            .
            
           ontologies:object1ToObject3
              a drivetrain:InstanceToInstanceRecipe ;
              drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
              drivetrain:object turbo:object3 ;
              drivetrain:predicate turbo:pred2 ;
              drivetrain:subject turbo:object1 ;
            .
            
          ontologies:object2ToObject3
              a drivetrain:InstanceToInstanceRecipe ;
              drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
              drivetrain:object turbo:object3 ;
              drivetrain:predicate turbo:pred3 ;
              drivetrain:subject turbo:object2 ;
            .}}"""
            
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  pmbb:obj1 a turbo:object1 .
                  pmbb:obj2 a turbo:object2 .
                  pmbb:obj3 a turbo:object3 .
                  
                  pmbb:obj1 turbo:pred2 pmbb:obj3 .
                  pmbb:obj2 turbo:pred3 pmbb:obj3 .
              }
          }
          
          """
        SparqlUpdater.updateSparql(Globals.cxn, insertData)
    }
    
    test("run process normally")
    {
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
    }
    
    test ("2 output connection recipes - same subject and object, different multiplicity")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject2_2
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object2 ;
                    drivetrain:predicate turbo:pred5 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject2_2 .
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
              }
          }
 
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: There are multiple connections between http://transformunify.org/ontologies/object2 and http://transformunify.org/ontologies/object1 with non-matching cardinality" ||
                e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: There are multiple connections between http://transformunify.org/ontologies/object1 and http://transformunify.org/ontologies/object2 with non-matching cardinality")
        }
    }
    
    // due to the change on 7/21/20, object4 should still be assigned a multiplicity enforcer because there are only 1-1 connections in the input
    test ("one output recipe - new 1-many object without multiplicity context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  
                  drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
    }
    
    // due to the change on 7/21/20, object4 should still be assigned a multiplicity enforcer because there are only 1-1 connections in the input
    test ("one output recipe - new many-1 object without multiplicity context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
    }
    
    test("many-1 object in output with no context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:object1ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object1ToObject5 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
                  drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: RuntimeException => assert(e.toString == "java.lang.RuntimeException: Could not assign cardinality enforcer for element http://transformunify.org/ontologies/object4")
        }
    }
    
    test("1-many object in output with no context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:object1ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object1ToObject5 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
                  drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: RuntimeException => assert(e.toString == "java.lang.RuntimeException: Could not assign cardinality enforcer for element http://transformunify.org/ontologies/object4")
        }
    }
    
    // These tests test the assignment of cardinality enforcers using a cardinality count map from the CardinalityCountBuilder.
    // This feature hasn't been implemented yet, so these tests are commented.
    /*test("many-1 object in output with context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:object1ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object1ToObject5 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
    }
    
    test("1-many object in output with context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:object1ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object1ToObject5 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
    }*/
    
    test ("2 output connection recipes - mixed singleton declaration")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-singleton> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  
                  ontologies:object2ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
                  drivetrain:many-singleton a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Instance http://transformunify.org/ontologies/object4 cannot be a Singleton and have a many-1 connection with another element")
        }
    }
    
    test ("circular multiplicity logic error in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object4ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object4 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject5 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object4ToObject5 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  pmbb:obj4 a turbo:object4 .
                  pmbb:obj5 a turbo:object5 .
                  
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj2 turbo:pred1 pmbb:obj5 .
                  pmbb:obj4 turbo:pred1 pmbb:obj5 .
              }
          }
 
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        SparqlUpdater.updateSparql(Globals.cxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Inconsistent cardinality found involving elements"))
        }
    }
    
    test ("multiplicity in input and output do not agree")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_input
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject4_output
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred4 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4_input .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4_output .
                  
                  drivetrain:1-many a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj4 a turbo:object4 .
              }
          }
 
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        SparqlUpdater.updateSparql(Globals.cxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Inconsistent cardinality found involving elements"))
        }
    }
    
    test ("element declared as singleton in output and not as a singleton in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_input
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-singleton> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject4_output
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred4 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4_input .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4_output .
                  
                  drivetrain:many-singleton a drivetrain:TurboGraphCardinalityRule .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj4 a turbo:object4 .
              }
          }
 
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        SparqlUpdater.updateSparql(Globals.cxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Instance http://transformunify.org/ontologies/object4 cannot be a Singleton and have a 1-1 connection with another element")
        }
    }
    
    test ("invalid multiplicity used in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_input
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/thisisntamultiplicity> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4_input .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj4 a turbo:object4 .
              }
          }
 
        """
        
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        SparqlUpdater.updateSparql(Globals.cxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: https://github.com/PennTURBO/Drivetrain/thisisntamultiplicity does not have a type")
        }
    }
    
    test ("invalid multiplicity used in output")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_output
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/thisisntamultiplicity> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4_output .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: https://github.com/PennTURBO/Drivetrain/thisisntamultiplicity does not have a type")
        }
    }
    
    test("graph specification contains connection not present as output in instruction set")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  ontologies:notPresentConnection
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:obj1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:obj2 ;
                  .

                  turbo:obj1 a owl:Class .
                  turbo:obj2 a owl:Class .
                  turbo:pred1 a rdf:Property .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses()
        }
        catch
        {
            case e: AssertionError => assert(1==2, e.toString())
        }
    }
    
    test("graph specification contains connection present but removed as output in instruction set")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:TURBO_0000502 ;
                  .

                  turbo:pred1 a rdf:Property .
               }
               
               Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 ;
                       drivetrain:precedes ontologies:myProcess2 .
                   
                   ontologies:myProcess2 drivetrain:inputNamedGraph <${Globals.expandedNamedGraph}> ;
                       a turbo:TURBO_0010354 ;
                       drivetrain:outputNamedGraph <${Globals.expandedNamedGraph}> ; 
                       drivetrain:hasRequiredInput ontologies:object1ToObject4 ;
                       drivetrain:removes ontologies:object1ToObject4 .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses()
        }
        catch
        {
            case e: AssertionError => assert(1==2, e.toString())
        }
    }
    
    test("instruction set does not create recipe required by graph specification")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  ontologies:object1ToObject3
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object3 ;
                    drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
                  .
 
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
                  .
                  
                  ontologies:object1 a owl:Class .
                  ontologies:object2 a owl:Class .
                  ontologies:object3 a owl:Class .
                  ontologies:object4 a owl:Class .

                  ontologies:pred1 a rdf:Property .
                  
                  drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                  drivetrain:eitherSubjectOrObjectExists a drivetrain:TurboGraphRequirementSpecification .
               }
               
               Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:myProcess1 a turbo:TURBO_0010354 ;
                       drivetrain:hasOutput ontologies:object1ToObject4 ;
                       drivetrain:hasRequiredInput ontologies:object1ToObject2 ;
                       drivetrain:inputNamedGraph <${Globals.expandedNamedGraph}> ;
                       drivetrain:outputNamedGraph <${Globals.expandedNamedGraph}> ; 
                   .
                   ontologies:object1ToObject2 a drivetrain:InstanceToInstanceRecipe ;
                       drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                       drivetrain:subject turbo:object1 ;
                       drivetrain:predicate turbo:pred1 ;
                       drivetrain:object turbo:object2 ;
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses()
            assert(1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString() == "java.lang.AssertionError: assertion failed: Error in graph model: connection recipe http://transformunify.org/ontologies/object1ToObject3 in the Graph Specification is required due to the existence of http://transformunify.org/ontologies/object1 but is not the output of a queued process in the Instruction Set")
        }
    }
    
    test("instruction set does not create recipe not required by graph spec due to context")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <${Globals.defaultPrefix}"""+s"""graphSpecification>
              {
                  ontologies:object1ToObject3
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object3 ;
                    drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
                    drivetrain:subjectUsesContext drivetrain:context1 ;
                  .
 
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
                    drivetrain:subjectUsesContext drivetrain:context2 ;
                  .
                  
                  ontologies:object1 a owl:Class ;
                    drivetrain:hasPossibleContext drivetrain:context1 ;
                    drivetrain:hasPossibleContext drivetrain:context2 ;
                  .
                  ontologies:object2 a owl:Class .
                  ontologies:object3 a owl:Class .
                  ontologies:object4 a owl:Class .

                  ontologies:pred1 a rdf:Property .
                  
                  drivetrain:context1 a drivetrain:TurboGraphContext .
                  drivetrain:context2 a drivetrain:TurboGraphContext .
                  
                  drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                  drivetrain:eitherSubjectOrObjectExists a drivetrain:TurboGraphRequirementSpecification .
               }
               
               Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:myProcess1 a turbo:TURBO_0010354 ;
                       drivetrain:hasOutput ontologies:object1ToObject4 ;
                       drivetrain:hasRequiredInput ontologies:object1ToObject2 ;
                       drivetrain:inputNamedGraph <${Globals.expandedNamedGraph}> ;
                       drivetrain:outputNamedGraph <${Globals.expandedNamedGraph}> ; 
                   .
                   ontologies:object1ToObject2 a drivetrain:InstanceToInstanceRecipe ;
                       drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
                       drivetrain:subject turbo:object1 ;
                       drivetrain:predicate turbo:pred1 ;
                       drivetrain:object turbo:object2 ;
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses()
        }
        catch
        {
            case e: AssertionError => assert(1==2, e.toString())
        }
    }
    
    test("duplicate subject property of recipe in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
               <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:object1ToObject3 drivetrain:subject ontologies:someSubject .
                   ontologies:someSubject a owl:Class .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: recipe http://transformunify.org/ontologies/object1ToObject3 may have duplicate properties")
        }
    }
    
    test("duplicate referencedInGraph property of recipe in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
               <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:object1ToObject3 drivetrain:referencedInGraph pmbb:namedGraph1 .
                   ontologies:object1ToObject3 drivetrain:referencedInGraph pmbb:namedGraph2 .
                   ontologies:someSubject a owl:Class .
                   pmbb:namedGraph1 a drivetrain:TurboNamedGraph .
                   pmbb:namedGraph2 a drivetrain:TurboNamedGraph .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: recipe http://transformunify.org/ontologies/object1ToObject3 may have duplicate properties")
        }
    }
    
    test("duplicate predicate property of recipe in output")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
               Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:object1ToObject2 drivetrain:predicate ontologies:somePredicate .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: recipe http://transformunify.org/ontologies/object1ToObject2 may have duplicate properties")
        }
    }
    
    test("process has multiple input graphs")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
               Graph <${Globals.defaultPrefix}"""+s"""instructionSet>
               {
                   ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:someOtherNamedGraph .
               }
           }
        """
      
        SparqlUpdater.updateSparql(Globals.gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Process http://transformunify.org/ontologies/myProcess1 has duplicate properties")
        }
    }
    
    test("datatype connection has class instance object")  
    {
       val insert = s"""INSERT DATA { Graph <${Globals.defaultPrefix}""" + s"""instructionSet> {
          ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4 .
          ontologies:object2ToObject4 a drivetrain:InstanceToLiteralRecipe .
          ontologies:object2ToObject4 drivetrain:subject turbo:object2 .
          ontologies:object2ToObject4 drivetrain:predicate turbo:predicate4 .
          ontologies:object2ToObject4 drivetrain:object turbo:object4 .
          ontologies:object2ToObject4 drivetrain:cardinality drivetrain:1-1 .
          
          turbo:object4 a owl:Class .
          }}"""
       SparqlUpdater.updateSparql(Globals.gmCxn, insert)
       
       try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: The following objects were declared as literals by at least one recipe, but were not typed as literals: http://transformunify.org/ontologies/object4 ")
        }
    }
    
    test("datatype connection has describer object")  
    {
       val insert = s"""INSERT DATA { Graph <${Globals.defaultPrefix}""" + s"""instructionSet> {
          ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4 .
          ontologies:object2ToObject4 a drivetrain:InstanceToLiteralRecipe .
          ontologies:object2ToObject4 drivetrain:subject turbo:object2 .
          ontologies:object2ToObject4 drivetrain:predicate turbo:predicate4 .
          ontologies:object2ToObject4 drivetrain:object turbo:describer1 .
          ontologies:object2ToObject4 drivetrain:cardinality drivetrain:1-1 .
          
          turbo:describer1 a drivetrain:ClassResourceList .
          }}"""
       SparqlUpdater.updateSparql(Globals.gmCxn, insert)
       
       try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: The following objects were declared as literals by at least one recipe, but were not typed as literals: http://transformunify.org/ontologies/describer1 ")
        }
    }
    
    // Due to the change made on 8/25, input patterns with only optionals are allowed, and optionals can be used as cardinality enforcers
    // if there are no required enforcers that qualify
    test("only optional input")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    ontologies:myProcess1 drivetrain:hasOptionalInput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:term3 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    pmbb:term3 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)

          RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
    }
    
    test("literal asserted as subject of instance recipes")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    ontologies:myProcess1 drivetrain:hasOptionalInput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject "1" .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToTermRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:literal1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:term3 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == """java.lang.AssertionError: assertion failed: The following subjects were declared as instances by at least one recipe, but were not typed as instances: "1"^^<http://www.w3.org/2001/XMLSchema#string> http://www.itmat.upenn.edu/biobank/literal1 """)
        }
    }
    
    test("literal asserted as object of instance recipes")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasRequiredInput pmbb:connection1 .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object "1" .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection2 drivetrain:object pmbb:term2 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    
                    pmbb:predicate1 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == """java.lang.AssertionError: assertion failed: The following objects were declared as instances by at least one recipe, but were not typed as instances: "1"^^<http://www.w3.org/2001/XMLSchema#string> """)
        }
    }
    
    test("literal asserted as object of term recipes - literal resource list")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToTermRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term2 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object pmbb:literal1 .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                    
                    pmbb:literal1 a drivetrain:LiteralResourceList .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == """java.lang.AssertionError: assertion failed: The following objects were declared as terms by at least one recipe, but were typed as literals: http://www.itmat.upenn.edu/biobank/literal1 """)
        }
    }
    
    test("literal asserted as object of term recipes - raw literal value")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasRequiredInput pmbb:connection2 .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    
                    pmbb:connection1 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToTermRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term2 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate2 .
                    pmbb:connection2 drivetrain:object "1" .

                    pmbb:term1 a owl:Class .
                    pmbb:term2 a owl:Class .
                    pmbb:predicate1 a rdf:Property .
                    pmbb:predicate2 a rdf:Property .
                                      
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == """java.lang.AssertionError: assertion failed: The following objects were declared as terms by at least one recipe, but were typed as literals: "1"^^<http://www.w3.org/2001/XMLSchema#string> """)
        }
    }
    
    test("instance asserted as object of literal recipes")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection1 .
                    ontologies:myProcess1 drivetrain:hasOptionalInput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToLiteralRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:TermToLiteralRecipe .
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
          
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: The following objects were declared as literals by at least one recipe, but were not typed as literals: http://www.itmat.upenn.edu/biobank/term2 http://www.itmat.upenn.edu/biobank/term1 ")
        }
    }
    
    test("literal resource list defined with two different datatypes")
    {
        Utilities.clearNamedGraph(Globals.gmCxn, Globals.defaultPrefix + "instructionSet")
        val insert = s"""
            INSERT DATA
            {
                <${Globals.defaultPrefix}""" + s"""instructionSet>
                {
                    ontologies:myProcess1 a turbo:TURBO_0010354 .
                    ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:Shortcuts .
                    ontologies:myProcess1 drivetrain:outputNamedGraph properties:expandedNamedGraph .
                    ontologies:myProcess1 drivetrain:hasRequiredInput pmbb:connection1 .
                    ontologies:myProcess1 drivetrain:hasOutput pmbb:connection2 .
                    
                    pmbb:connection1 a drivetrain:InstanceToLiteralRecipe .
                    pmbb:connection1 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection1 drivetrain:subject pmbb:term1 .
                    pmbb:connection1 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection1 drivetrain:object pmbb:term2 .
                    
                    pmbb:connection2 a drivetrain:InstanceToInstanceRecipe .
                    pmbb:connection2 drivetrain:cardinality drivetrain:1-1 .
                    pmbb:connection2 drivetrain:subject pmbb:term1 .
                    pmbb:connection2 drivetrain:predicate pmbb:predicate1 .
                    pmbb:connection2 drivetrain:object pmbb:term3 .

                    pmbb:term1 a owl:Class .
                    pmbb:term3 a owl:Class .
                    pmbb:predicate1 a rdf:Property .
                    drivetrain:1-1 a drivetrain:TurboGraphCardinalityRule .
                    
                    pmbb:term2 a drivetrain:StringLiteralResourceList .
                    pmbb:term2 a drivetrain:IntegerLiteralResourceList .
                }
            }
          """
          SparqlUpdater.updateSparql(Globals.gmCxn, insert)
          
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: recipe http://www.itmat.upenn.edu/biobank/connection1 may have duplicate properties")
        }
    }
}