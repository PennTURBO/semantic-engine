package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import java.io._


class GraphModelValidationTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = true
    
    val uuid = UUID.randomUUID().toString.replaceAll("-", "")
    RunDrivetrainProcess.setGlobalUUID(uuid)
    
    var pw: PrintWriter = null
    var file: File = null
    
    before
    {
        def testModelFile = s"graphModelTestFile_$uuid.ttl"
        file = new File(s"ontologies//$testModelFile")
        pw = new PrintWriter(file)
        
        pw.write("""
          
          ontologies:myProcess1 rdfs:subClassOf ontologies:TURBO_0010178 ;
              ontologies:inputNamedGraph pmbb:expanded ;
              ontologies:outputNamedGraph pmbb:expanded ; 
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
              
          ontologies:object1ToObject2
              a ontologies:ObjectConnectionToInstanceRecipe ;
              ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
              ontologies:object turbo:object2 ;
              ontologies:outputOf ontologies:myProcess1 ;
              ontologies:predicate turbo:pred1 ;
              ontologies:subject turbo:object1 ;
            .
            
           ontologies:object1ToObject3
              a ontologies:ObjectConnectionToInstanceRecipe ;
              ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
              ontologies:object turbo:object3 ;
              ontologies:requiredInputTo ontologies:myProcess1 ;
              ontologies:predicate turbo:pred2 ;
              ontologies:subject turbo:object1 ;
            .
            
          ontologies:object2ToObject3
              a ontologies:ObjectConnectionToInstanceRecipe ;
              ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
              ontologies:object turbo:object3 ;
              ontologies:requiredInputTo ontologies:myProcess1 ;
              ontologies:predicate turbo:pred3 ;
              ontologies:subject turbo:object2 ;
            .""")
            
        pw.close()
                      
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData(testModelFile)
        //graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData("graphModelTestFile_107e35f622ce485b896643cd1b2af715.ttl")
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        testRepoManager = graphDBMaterials.getTestRepoManager()
        testRepository = graphDBMaterials.getTestRepository()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
        
        val insertData: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
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
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
        file.delete()
    }
    
    /*test("run process normally")
    {
        RunDrivetrainProcess.runProcess("http://transformunify.org/ontologies/myProcess1")
    }
    
    test ("2 output connection recipes - same subject and object, different multiplicity")
    {
        val insert: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object1ToObject2_2
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/many-1> ;
                    ontologies:object turbo:object2 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object1 ;
                  .
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
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: There are multiple connections between http://transformunify.org/ontologies/object2 and http://transformunify.org/ontologies/object1 with non-matching multiplicities")
        }
    }
    
    test ("one output recipe - new 1-many object without multiplicity context")
    {
        val insert: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object1ToObject4
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/1-many> ;
                    ontologies:object turbo:object4 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object1 ;
                  .
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
        val insert: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object1ToObject4
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/many-1> ;
                    ontologies:object turbo:object4 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object1 ;
                  .
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
        val insert: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object1ToObject4
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/many-singleton> ;
                    ontologies:object turbo:object4 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object1 ;
                  .
                  
                  ontologies:object2ToObject4
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/many-1> ;
                    ontologies:object turbo:object4 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object2 ;
                  .
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
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: For process http://transformunify.org/ontologies/myProcess1, http://transformunify.org/ontologies/object4 has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        }
    }
    
    test ("circular multiplicity logic error in input")
    {
        val insertDataModel: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object2ToObject4
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
                    ontologies:object turbo:object4 ;
                    ontologies:requiredInputTo ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred3 ;
                    ontologies:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject5
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/many-1> ;
                    ontologies:object turbo:object5 ;
                    ontologies:requiredInputTo ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object2 ;
                  .
                  
                  ontologies:object4ToObject5
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
                    ontologies:object turbo:object5 ;
                    ontologies:requiredInputTo ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred1 ;
                    ontologies:subject turbo:object4 ;
                  .
               }
           }
        """
        
        val insertData: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
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
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Logical graph model error: for process http://transformunify.org/ontologies/myProcess1, the multiplicity of http://transformunify.org/ontologies/object2 has not been defined consistently.")
        }
    }
    
    test ("multiplicity in input and output do not agree")
    {
        val insertDataModel: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object2ToObject4_input
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/1-many> ;
                    ontologies:object turbo:object4 ;
                    ontologies:requiredInputTo ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred3 ;
                    ontologies:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject4_output
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
                    ontologies:object turbo:object4 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred4 ;
                    ontologies:subject turbo:object2 ;
                  .
               }
           }
        """
        
        val insertData: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
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
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: Logical graph model error: for process http://transformunify.org/ontologies/myProcess1, the multiplicity of http://transformunify.org/ontologies/object4 has not been defined consistently.")
        }
    }
    
    test ("element declared as singleton in output and not as a singleton in input")
    {
        val insertDataModel: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object2ToObject4_input
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/many-singleton> ;
                    ontologies:object turbo:object4 ;
                    ontologies:requiredInputTo ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred3 ;
                    ontologies:subject turbo:object2 ;
                  .
                  
                  ontologies:object2ToObject4_output
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/1-1> ;
                    ontologies:object turbo:object4 ;
                    ontologies:outputOf ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred4 ;
                    ontologies:subject turbo:object2 ;
                  .
               }
           }
        """
        
        val insertData: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
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
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: For process http://transformunify.org/ontologies/myProcess1, http://transformunify.org/ontologies/object4 has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        }
    }*/
    
    test ("invalid multiplicity used")
    {
        val insertDataModel: String = """
          
          INSERT DATA
          {
              Graph pmbb:dataModel
              {
                  ontologies:object2ToObject4_input
                    a ontologies:ObjectConnectionToInstanceRecipe ;
                    ontologies:multiplicity <http://transformunify.org/ontologies/thisisntamultiplicity> ;
                    ontologies:object turbo:object4 ;
                    ontologies:requiredInputTo ontologies:myProcess1 ;
                    ontologies:predicate turbo:pred3 ;
                    ontologies:subject turbo:object2 ;
                  .
               }
           }
        """
        
        val insertData: String = """
          
          INSERT DATA
          {
              Graph pmbb:expanded
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
            case e: AssertionError => assert(e.toString == "java.lang.AssertionError: assertion failed: For process http://transformunify.org/ontologies/myProcess1, http://transformunify.org/ontologies/object4 has a 1-1, 1-many, or many-1 relationship and is also considered a Singleton")
        }
    }
}