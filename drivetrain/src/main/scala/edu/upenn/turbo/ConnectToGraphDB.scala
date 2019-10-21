package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.io.File
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.Properties
import java.io.InputStreamReader
import java.io.Reader
import org.eclipse.rdf4j.rio._
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager

/**
 * This class contains all method relating to connecting the Drivetrain application to an instance of Ontotext Graph DB, and loading
 * the initial dataset into the specified repository. Methods in this class are responsible for reading repository, connection URL, 
 * and data file information from the TURBO properties file.
 */

object ConnectToGraphDB extends ProjectwideGlobals
{   
    /**
     * Calls the initialize graph method, then loads the data files specified in the TURBO properties file if the loadFromProperties Boolean
     * in the TURBO properties file is set to true. Also loads ontology if importOntologies Boolean in the TURBO properties file is set to true.
     * Returns a TurboGraphConnection containing RepositoryConnection, Repository, and RemoteRepositoryManager objects to be handled and closed
     * by the calling class.
     */
    def initializeGraphUpdateData(graphModelFile: String = graphModelFile): TurboGraphConnection =
    {
        val graphConnect: TurboGraphConnection = initializeGraph()
        if (graphConnect.getConnection() != null)
        {
            try
            {
                // update data model and ontology upon establishing connection
                OntologyLoader.addOntologyFromUrl(graphConnect.getGmConnection())
                DrivetrainDriver.updateModel(graphConnect.getGmConnection(), graphModelFile)
            }
            catch
            {
                case e: RuntimeException => closeGraphConnection(graphConnect, false)
            }
        }
        graphConnect
    }
    
    /**
     * Initializes the connection to the Graph DB instance after checking that the TURBO Properties file is in a valid state. Validates connection
     * with username and password specified in TURBO properties file and returns TurboGraphConnection object.
     */
    def initializeGraph(): TurboGraphConnection =
    {
        val missingProperty: Option[String] = checkForMissingProperties()
        val graphConnect: TurboGraphConnection = new TurboGraphConnection
        if (missingProperty != None)
        {
            logger.info("Missing Required Property " + missingProperty.get)
            throw new RuntimeException ("Missing property: " + missingProperty.get)
        }
        else
        {
            val repoManager: RemoteRepositoryManager = new RemoteRepositoryManager(serviceURL)
            repoManager.setUsernameAndPassword(helper.retrievePropertyFromFile("username"), helper.retrievePropertyFromFile("password"))
            repoManager.initialize()
            val repository: Repository = repoManager.getRepository(helper.retrievePropertyFromFile("productionRepository"))
            val cxn: RepositoryConnection = repository.getConnection()
            
            val gmRepoManager: RemoteRepositoryManager = new RemoteRepositoryManager(serviceURL)
            gmRepoManager.setUsernameAndPassword(helper.retrievePropertyFromFile("username"), helper.retrievePropertyFromFile("password"))
            gmRepoManager.initialize()
            val gmRepository: Repository = gmRepoManager.getRepository(helper.retrievePropertyFromFile("modelRepository"))
            val gmCxn: RepositoryConnection = gmRepository.getConnection()
            
            val testRepoManager: RemoteRepositoryManager = new RemoteRepositoryManager(serviceURL)
            testRepoManager.setUsernameAndPassword(helper.retrievePropertyFromFile("username"), helper.retrievePropertyFromFile("password"))
            testRepoManager.initialize()
            val testRepository: Repository = testRepoManager.getRepository(helper.retrievePropertyFromFile("testingRepository"))
            val testCxn: RepositoryConnection = testRepository.getConnection()
            
            graphConnect.setConnection(cxn)
            graphConnect.setRepoManager(repoManager)
            graphConnect.setRepository(repository)
            
            graphConnect.setGmConnection(gmCxn)
            graphConnect.setGmRepoManager(gmRepoManager)
            graphConnect.setGmRepository(gmRepository)
            
            graphConnect.setTestConnection(testCxn)
            graphConnect.setTestRepoManager(testRepoManager)
            graphConnect.setTestRepository(testRepository)
        }
        graphConnect
    }
    
    /**
     * Used to disconnect Drivetrain from Ontotext Graph DB by closing the relevant RepositoryConnection, RemoteRepositoryManager, and Repository objects.
     * Optionally delete all triples in database before connection close by specifying boolean parameter.
     */
    def closeGraphConnection(graphCxn: TurboGraphConnection, deleteAllTriples: Boolean = false)
    {
        val cxn = graphCxn.getConnection()
        val repoManager = graphCxn.getRepoManager()
        val repository = graphCxn.getRepository() 
        
        val gmCxn = graphCxn.getGmConnection()
        val gmRepoManager = graphCxn.getGmRepoManager()
        val gmRepository = graphCxn.getGmRepository()
        
        val testCxn = graphCxn.getTestConnection()
        val testRepoManager = graphCxn.getTestRepoManager()
        val testRepository = graphCxn.getTestRepository()
        
        if (deleteAllTriples)
        {
             if (!testCxn.isActive()) testCxn.begin()
             val deleteAll: String = "DELETE {?s ?p ?o} WHERE {?s ?p ?o .} "
             val tupleDelete = testCxn.prepareUpdate(QueryLanguage.SPARQL, deleteAll)
             tupleDelete.execute()
             testCxn.commit()
        }
        if (cxn != null)
        {
            cxn.close()
            repository.shutDown()
            repoManager.shutDown() 
        }
        if (gmCxn != null)
        {
            gmCxn.close()
            gmRepository.shutDown()
            gmRepoManager.shutDown()
        }
        if (testCxn != null)
        {
            testCxn.close()
            testRepository.shutDown()
            testRepoManager.shutDown()
        }
    }
    
    /**
     * Helper method called by graph connection methods in order to validate properties file. Ensures that all required properties for connection to Graph DB
     * are present in properties file.
     */
    def checkForMissingProperties(): Option[String] =
    { 
        val input: FileInputStream = new FileInputStream("..//turbo_properties.properties")
        val props: Properties = new Properties()
        props.load(input)
        input.close()
        var optToReturn: Option[String] = None : Option[String]
        val proceed: Boolean = true
        var requiredProperties: ArrayBuffer[String] = ArrayBuffer("serviceURL",
            "password","username","productionRepository",
            "ontologyURL", "modelRepository", "testingRepository", 
            "processNamedGraph", "reinferRepo", "loadAdditionalOntologies",
            "graphModelFile", "graphSpecificationFile", "defaultPrefix",
            "dataValidationMode", "errorLogFile", "expandedNamedGraph",
            "clearGraphsAtStart")
        var a = 0
        while (optToReturn == None && a < requiredProperties.size)
        {
            //println("testing property " + requiredProperties(a))
            try
            {
                props.getProperty(requiredProperties(a)).isEmpty()
                if (requiredProperties(a) == "loadAdditionalOntologies")
                {
                    if (getBooleanProperty("loadAdditionalOntologies")) 
                    try
                    {
                        props.getProperty("bioportalApiKey").isEmpty()
                    }
                    catch
                    {
                        
                        case e: NullPointerException => optToReturn = Some("bioportalApiKey")
                    }
                }
            }
            catch
            {
                
                case e: NullPointerException => optToReturn = Some(requiredProperties(a))
            }
            a = a + 1
        }
        optToReturn
    }
}