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
import org.slf4j.LoggerFactory

/**
 * This class contains all method relating to connecting the Drivetrain application to an instance of Ontotext Graph DB, and loading
 * the initial dataset into the specified repository. Methods in this class are responsible for reading repository, connection URL, 
 * and data file information from the TURBO properties file.
 */

object ConnectToGraphDB
{   
    val logger = LoggerFactory.getLogger(getClass)
    
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
            val connProps = retrieveConnectionPropertiesBasedOnBuildEnvironment()
            
            val service = connProps("serviceURL")
            val repoManager: RemoteRepositoryManager = new RemoteRepositoryManager(service)
            repoManager.setUsernameAndPassword(connProps("username"), connProps("password"))
            repoManager.initialize()
            val repoName = connProps("repository")
            val repository: Repository = repoManager.getRepository(repoName)
            val cxn: RepositoryConnection = repository.getConnection()
            
            val gmRepoManager: RemoteRepositoryManager = new RemoteRepositoryManager(Globals.modelServiceURL)
            gmRepoManager.setUsernameAndPassword(Globals.modelUsername, Globals.modelPassword)
            gmRepoManager.initialize()
            val gmRepository: Repository = gmRepoManager.getRepository(Globals.modelRepository)
            val gmCxn: RepositoryConnection = gmRepository.getConnection()
            
            graphConnect.setConnection(cxn)
            graphConnect.setRepoManager(repoManager)
            graphConnect.setRepository(repository)
            
            graphConnect.setGmConnection(gmCxn)
            graphConnect.setGmRepoManager(gmRepoManager)
            graphConnect.setGmRepository(gmRepository)
        }
        graphConnect
    }
    
    /**
     * Used to disconnect Drivetrain from Ontotext Graph DB by closing the relevant RepositoryConnection, RemoteRepositoryManager, and Repository objects.
     * Optionally delete all triples in database before connection close by specifying boolean parameter.
     */
    def closeGraphConnection(graphCxn: TurboGraphConnection, deleteAllTriples: Boolean = false)
    {
        if (graphCxn != null)
        {
            val cxn = graphCxn.getConnection()
            val repoManager = graphCxn.getRepoManager()
            val repository = graphCxn.getRepository() 
            
            val gmCxn = graphCxn.getGmConnection()
            val gmRepoManager = graphCxn.getGmRepoManager()
            val gmRepository = graphCxn.getGmRepository()
            
            if (deleteAllTriples)
            {
                 if (!cxn.isActive()) cxn.begin()
                 val deleteAll: String = "DELETE {?s ?p ?o} WHERE {?s ?p ?o .} "
                 val tupleDelete = cxn.prepareUpdate(QueryLanguage.SPARQL, deleteAll)
                 tupleDelete.execute()
                 cxn.commit()
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
        }
        else logger.info("There was an issue connecting to the specified repository. Make sure serviceURL, username, password, and repository name have been set correctly.")
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
        var requiredProperties: ArrayBuffer[String] = ArrayBuffer("productionServiceURL",
            "productionPassword","productionUsername","productionRepository",
            "testingServiceURL", "testingUsername", "testingPassword",
            "testingRepository", "modelServiceURL", "modelUsername",
            "modelPassword", "modelRepository", "ontologyURL",
            "processNamedGraph", "reinferRepo", "loadAdditionalOntologies",
            "instructionSetFile", "graphSpecificationFile", "defaultPrefix",
            "dataValidationMode", "errorLogFile", "expandedNamedGraph",
            "clearGraphsAtStart", "acornOntologyFile", "validateAgainstOntology",
            "useMultipleThreads")
        var a = 0
        while (optToReturn == None && a < requiredProperties.size)
        {
            //println("testing property " + requiredProperties(a))
            try
            {
                props.getProperty(requiredProperties(a)).isEmpty()
                if (requiredProperties(a) == "loadAdditionalOntologies")
                {
                    if (Globals.getBooleanProperty("loadAdditionalOntologies")) 
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
    
    def getNewConnectionToRepo(): TurboGraphConnection =
    {
        val connProps = retrieveConnectionPropertiesBasedOnBuildEnvironment()
        
        val repoManager: RemoteRepositoryManager = new RemoteRepositoryManager(connProps("serviceURL"))
        repoManager.setUsernameAndPassword(connProps("username"), connProps("password"))
        repoManager.initialize()
        val repository: Repository = repoManager.getRepository(connProps("repository"))
        val cxn: RepositoryConnection = repository.getConnection()
        
        val graphConnection = new TurboGraphConnection
        graphConnection.setConnection(cxn)
        graphConnection.setRepoManager(repoManager)
        graphConnection.setRepository(repository)
        
        graphConnection
    }

    def getTestRepositoryConnection(): TurboGraphConnection =
    {
        logger.info(s"Connecting to repository "+Globals.testingRepository+ " at " +Globals.testingServiceURL+ " as " +Globals.testingUsername)

        val repoManager: RemoteRepositoryManager = new RemoteRepositoryManager(Globals.testingServiceURL)
        repoManager.setUsernameAndPassword(Globals.testingUsername, Globals.testingPassword)
        repoManager.initialize()
        val repository: Repository = repoManager.getRepository(Globals.testingRepository)
        val testCxn: RepositoryConnection = repository.getConnection()
        
        val graphConnection = new TurboGraphConnection
        graphConnection.setConnection(testCxn)
        graphConnection.setRepoManager(repoManager)
        graphConnection.setRepository(repository)
        
        val gmRepoManager: RemoteRepositoryManager = new RemoteRepositoryManager(Globals.modelServiceURL)
        gmRepoManager.setUsernameAndPassword(Globals.modelUsername, Globals.modelPassword)
        gmRepoManager.initialize()
        val gmRepository: Repository = gmRepoManager.getRepository(Globals.modelRepository)
        val gmCxn: RepositoryConnection = gmRepository.getConnection()
        
        graphConnection.setGmConnection(gmCxn)
        graphConnection.setGmRepoManager(gmRepoManager)
        graphConnection.setGmRepository(gmRepository)
        
        DrivetrainDriver.updateModel(graphConnection)
        OntologyLoader.addOntologyFromUrl(graphConnection.getGmConnection())
               
        graphConnection
    }
    
    def retrieveConnectionPropertiesBasedOnBuildEnvironment(): Map[String, String] =
    {
        var serviceURL = ""
        var username = ""
        var password = ""
        var repository = ""
        
        if ("main" == System.getenv("SCALA_ENV"))
        {
            logger.info("Running in production mode")
            serviceURL = Globals.productionServiceURL
            username = Globals.productionUsername
            password = Globals.productionPassword
            repository = Globals.productionRepository   
        }
        else if ("test" == System.getenv("SCALA_ENV"))
        {
            logger.info("Running in testing mode")
            serviceURL = Globals.testingServiceURL
            username = Globals.testingUsername
            password = Globals.testingPassword
            repository = Globals.testingRepository   
        }
        else throw new RuntimeException("System variable SCALA_ENV must be set to \"main\" or \"test\"; check your build.sbt file")
        logger.info(s"Connecting to repository $repository at $serviceURL as $username")
        Map("serviceURL" -> serviceURL, "username" -> username, "password" -> password, "repository" -> repository)
    }
}