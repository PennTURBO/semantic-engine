package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class GraphModelValidationTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    
    override def beforeAll()
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData()
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    before
    {
        helper.deleteAllTriplesInDatabase(testCxn)
        helper.clearNamedGraph(gmCxn, defaultPrefix + "instructionSet")
        helper.clearNamedGraph(gmCxn, defaultPrefix + "graphSpecification")
        
        val insert = s"""
        INSERT DATA { Graph <$defaultPrefix""" + s"""instructionSet> {
          ontologies:myProcess1 a ontologies:TURBO_0010354 ;
              drivetrain:inputNamedGraph <$expandedNamedGraph> ;
              drivetrain:outputNamedGraph <$expandedNamedGraph> ; 
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
          
          drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
              
          ontologies:object1ToObject2
              a drivetrain:InstanceToInstanceRecipe ;
              drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
              drivetrain:object turbo:object2 ;
              drivetrain:predicate turbo:pred1 ;
              drivetrain:subject turbo:object1 ;
            .
            
           ontologies:object1ToObject3
              a drivetrain:InstanceToInstanceRecipe ;
              drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
              drivetrain:object turbo:object3 ;
              drivetrain:predicate turbo:pred2 ;
              drivetrain:subject turbo:object1 ;
            .
            
          ontologies:object2ToObject3
              a drivetrain:InstanceToInstanceRecipe ;
              drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
              drivetrain:object turbo:object3 ;
              drivetrain:predicate turbo:pred3 ;
              drivetrain:subject turbo:object2 ;
            .}}"""
            
        update.updateSparql(gmCxn, insert)
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <$expandedNamedGraph>
              {
                  pmbb:obj1 a turbo:object1 .
                  pmbb:obj2 a turbo:object2 .
                  pmbb:obj3 a turbo:object3 .
                  
                  pmbb:obj1 turbo:pred2 pmbb:obj3 .
                  pmbb:obj2 turbo:pred3 pmbb:obj3 .
              }
          }
          
          """
        update.updateSparql(testCxn, insertData)
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
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object1ToObject2_2
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object2 ;
                    drivetrain:predicate turbo:pred5 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject2_2 .
                  drivetrain:many-1 a drivetrain:TurboGraphMultiplicityRule .
              }
          }
 
        """
        
        update.updateSparql(gmCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: There are multiple connections between http://transformunify.org/ontologies/object2 and http://transformunify.org/ontologies/object1 with non-matching multiplicities" ||
                e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: There are multiple connections between http://transformunify.org/ontologies/object1 and http://transformunify.org/ontologies/object2 with non-matching multiplicities")
        }
    }
    
    test ("one output recipe - new 1-many object without multiplicity context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  
                  drivetrain:1-many a drivetrain:TurboGraphMultiplicityRule .
               }
           }
        """
        
        update.updateSparql(gmCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: For process http://transformunify.org/ontologies/myProcess1, there is not sufficient context to create the following: Set(http://transformunify.org/ontologies/object4)")
        }
    }
    
    test ("one output recipe - new many-1 object without multiplicity context")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphMultiplicityRule .
               }
           }
        """
        
        update.updateSparql(gmCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: For process http://transformunify.org/ontologies/myProcess1, there is not sufficient context to create the following: Set(http://transformunify.org/ontologies/object4)")
        }
    }
    
    test ("2 output connection recipes - mixed singleton declaration")
    {
        val insert: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/many-singleton> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object1 ;
                  .
                  
                  ontologies:object2ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphMultiplicityRule .
                  drivetrain:many-singleton a drivetrain:TurboGraphMultiplicityRule .
               }
           }
        """
        
        update.updateSparql(gmCxn, insert)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: For process http://transformunify.org/ontologies/myProcess1, http://transformunify.org/ontologies/object4 has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        }
    }
    
    test ("circular multiplicity logic error in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/many-1> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object4ToObject5
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object5 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:object4 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject5 .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object4ToObject5 .
                  
                  drivetrain:many-1 a drivetrain:TurboGraphMultiplicityRule .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <$expandedNamedGraph>
              {
                  pmbb:obj4 a turbo:object4 .
                  pmbb:obj5 a turbo:object5 .
                  
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj2 turbo:pred1 pmbb:obj5 .
                  pmbb:obj4 turbo:pred1 pmbb:obj5 .
              }
          }
 
        """
        
        update.updateSparql(gmCxn, insertDataModel)
        update.updateSparql(testCxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Error in graph model: for process http://transformunify.org/ontologies/myProcess1, the multiplicity of http://transformunify.org/ontologies/object2 has not been defined consistently"))
        }
    }
    
    test ("multiplicity in input and output do not agree")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_input
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-many> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject4_output
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred4 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4_input .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4_output .
                  
                  drivetrain:1-many a drivetrain:TurboGraphMultiplicityRule .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <$expandedNamedGraph>
              {
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj4 a turbo:object4 .
              }
          }
 
        """
        
        update.updateSparql(gmCxn, insertDataModel)
        update.updateSparql(testCxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString.startsWith("java.lang.AssertionError: assertion failed: Error in graph model: for process http://transformunify.org/ontologies/myProcess1, the multiplicity of http://transformunify.org/ontologies/object4 has not been defined consistently"))
        }
    }
    
    test ("element declared as singleton in output and not as a singleton in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_input
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/many-singleton> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject4_output
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred4 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4_input .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4_output .
                  
                  drivetrain:many-singleton a drivetrain:TurboGraphMultiplicityRule .
               }
           }
        """
        
        val insertData: String = s"""
          
          INSERT DATA
          {
              Graph <$expandedNamedGraph>
              {
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj4 a turbo:object4 .
              }
          }
 
        """
        
        update.updateSparql(gmCxn, insertDataModel)
        update.updateSparql(testCxn, insertData)
        
        try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1") 
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Error in graph model: For process http://transformunify.org/ontologies/myProcess1, http://transformunify.org/ontologies/object4 has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        }
    }
    
    test ("invalid multiplicity used in input")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_input
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/thisisntamultiplicity> ;
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
              Graph <$expandedNamedGraph>
              {
                  pmbb:obj2 turbo:pred3 pmbb:obj4 .
                  pmbb:obj4 a turbo:object4 .
              }
          }
 
        """
        
        update.updateSparql(gmCxn, insertDataModel)
        update.updateSparql(testCxn, insertData)
        
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
              Graph <$defaultPrefix"""+s"""instructionSet>
              {
                  ontologies:object2ToObject4_output
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/thisisntamultiplicity> ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:predicate turbo:pred3 ;
                    drivetrain:subject turbo:object2 ;
                  .
                  ontologies:myProcess1 drivetrain:hasOutput ontologies:object2ToObject4_output .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
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
              Graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  ontologies:notPresentConnection
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:object turbo:obj1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:subject turbo:obj2 ;
                  .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses(testCxn, gmCxn)
        }
        catch
        {
            case e: AssertionError => assert(1==2)
        }
    }
    
    test("graph specification contains connection present but removed as output in instruction set")
    {
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:TURBO_0000502 ;
                  .
               }
               
               Graph <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:myProcess1 drivetrain:hasOutput ontologies:object1ToObject4 ;
                       drivetrain:precedes ontologies:myProcess2 .
                   
                   ontologies:myProcess2 drivetrain:inputNamedGraph <$expandedNamedGraph> ;
                       a turbo:TURBO_0010354 ;
                       drivetrain:outputNamedGraph <$expandedNamedGraph> ; 
                       drivetrain:hasRequiredInput ontologies:object1ToObject4 ;
                       drivetrain:removes ontologies:object1ToObject4 .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses(testCxn, gmCxn)
        }
        catch
        {
            case e: AssertionError => assert(1==2)
        }
    }
    
    test("instruction set does not create recipe required by graph specification")
    {
        helper.clearNamedGraph(gmCxn, defaultPrefix + "instructionSet")
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  ontologies:object1ToObject3
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object3 ;
                    drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists ;
                  .
 
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists ;
                  .
                  
                  ontologies:object1 a owl:Class .
                  ontologies:object2 a owl:Class .
                  ontologies:object3 a owl:Class .
                  ontologies:object4 a owl:Class .
                  
                  drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                  drivetrain:eitherSubjectOrObjectExists a drivetrain:TurboGraphRequirementSpecification .
               }
               
               Graph <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:myProcess1 a turbo:TURBO_0010354 ;
                       drivetrain:hasOutput ontologies:object1ToObject4 ;
                       drivetrain:hasRequiredInput ontologies:object1ToObject2 ;
                       drivetrain:inputNamedGraph <$expandedNamedGraph> ;
                       drivetrain:outputNamedGraph <$expandedNamedGraph> ; 
                   .
                   ontologies:object1ToObject2 a drivetrain:InstanceToInstanceRecipe ;
                       drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                       drivetrain:subject turbo:object1 ;
                       drivetrain:predicate turbo:pred1 ;
                       drivetrain:object turbo:object2 ;
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses(testCxn, gmCxn)
            assert(1==2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString() == "java.lang.AssertionError: assertion failed: Error in graph model: connection recipe http://transformunify.org/ontologies/object1ToObject3 in the Graph Specification is required due to the existence of http://transformunify.org/ontologies/object1 but is not the output of a queued process in the Instruction Set")
        }
    }
    
    test("instruction set does not create recipe not required by graph spec due to context")
    {
        helper.clearNamedGraph(gmCxn, defaultPrefix + "instructionSet")
        val insertDataModel: String = s"""
          
          INSERT DATA
          {
              Graph <$defaultPrefix"""+s"""graphSpecification>
              {
                  ontologies:object1ToObject3
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object3 ;
                    drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists ;
                    drivetrain:subjectUsesContext drivetrain:context1 ;
                  .
 
                  ontologies:object1ToObject4
                    a drivetrain:InstanceToInstanceRecipe ;
                    drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                    drivetrain:subject turbo:object1 ;
                    drivetrain:predicate turbo:pred1 ;
                    drivetrain:object turbo:object4 ;
                    drivetrain:mustExistIf drivetrain:eitherSubjectOrObjectExists ;
                    drivetrain:subjectUsesContext drivetrain:context2 ;
                  .
                  
                  ontologies:object1 a owl:Class ;
                    drivetrain:hasPossibleContext drivetrain:context1 ;
                    drivetrain:hasPossibleContext drivetrain:context2 ;
                  .
                  ontologies:object2 a owl:Class .
                  ontologies:object3 a owl:Class .
                  ontologies:object4 a owl:Class .
                  
                  drivetrain:context1 a drivetrain:TurboGraphContext .
                  drivetrain:context2 a drivetrain:TurboGraphContext .
                  
                  drivetrain:1-1 a drivetrain:TurboGraphMultiplicityRule .
                  drivetrain:eitherSubjectOrObjectExists a drivetrain:TurboGraphRequirementSpecification .
               }
               
               Graph <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:myProcess1 a turbo:TURBO_0010354 ;
                       drivetrain:hasOutput ontologies:object1ToObject4 ;
                       drivetrain:hasRequiredInput ontologies:object1ToObject2 ;
                       drivetrain:inputNamedGraph <$expandedNamedGraph> ;
                       drivetrain:outputNamedGraph <$expandedNamedGraph> ; 
                   .
                   ontologies:object1ToObject2 a drivetrain:InstanceToInstanceRecipe ;
                       drivetrain:multiplicity <https://github.com/PennTURBO/Drivetrain/1-1> ;
                       drivetrain:subject turbo:object1 ;
                       drivetrain:predicate turbo:pred1 ;
                       drivetrain:object turbo:object2 ;
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
        try
        {
            RunDrivetrainProcess.runAllDrivetrainProcesses(testCxn, gmCxn)
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
               <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:object1ToObject3 drivetrain:subject ontologies:someSubject .
                   ontologies:someSubject a owl:Class .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
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
               <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:object1ToObject3 drivetrain:referencedInGraph pmbb:namedGraph1 .
                   ontologies:object1ToObject3 drivetrain:referencedInGraph pmbb:namedGraph2 .
                   ontologies:someSubject a owl:Class .
                   pmbb:namedGraph1 a drivetrain:TurboNamedGraph .
                   pmbb:namedGraph2 a drivetrain:TurboNamedGraph .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
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
               Graph <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:object1ToObject2 drivetrain:predicate ontologies:somePredicate .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
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
               Graph <$defaultPrefix"""+s"""instructionSet>
               {
                   ontologies:myProcess1 drivetrain:inputNamedGraph pmbb:someOtherNamedGraph .
               }
           }
        """
      
        update.updateSparql(gmCxn, insertDataModel)
        
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
       val insert = s"""INSERT DATA { Graph <$defaultPrefix""" + s"""instructionSet> {
          ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4 .
          ontologies:object2ToObject4 a drivetrain:InstanceToLiteralRecipe .
          ontologies:object2ToObject4 drivetrain:subject turbo:object2 .
          ontologies:object2ToObject4 drivetrain:predicate turbo:predicate4 .
          ontologies:object2ToObject4 drivetrain:object turbo:object4 .
          ontologies:object2ToObject4 drivetrain:multiplicity drivetrain:1-1 .
          }}"""
       update.updateSparql(gmCxn, insert)
       
       try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: The object of connection http://transformunify.org/ontologies/object2ToObject4 is not a literal, but the connection is a datatype connection.")
        }
    }
    
    test("datatype connection has describer object")  
    {
       val insert = s"""INSERT DATA { Graph <$defaultPrefix""" + s"""instructionSet> {
          ontologies:myProcess1 drivetrain:hasRequiredInput ontologies:object2ToObject4 .
          ontologies:object2ToObject4 a drivetrain:InstanceToLiteralRecipe .
          ontologies:object2ToObject4 drivetrain:subject turbo:object2 .
          ontologies:object2ToObject4 drivetrain:predicate turbo:predicate4 .
          ontologies:object2ToObject4 drivetrain:object turbo:describer1 .
          ontologies:object2ToObject4 drivetrain:multiplicity drivetrain:1-1 .
          
          turbo:describer1 a drivetrain:MultiObjectDescriber .
          }}"""
       update.updateSparql(gmCxn, insert)
       
       try
        {
            RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
            assert (1 == 2)
        }
        catch
        {
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: The object of connection http://transformunify.org/ontologies/object2ToObject4 is not a literal, but the connection is a datatype connection.")
        }
    }
}