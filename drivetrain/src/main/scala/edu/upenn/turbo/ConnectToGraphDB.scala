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
    def initializeGraphLoadData(loadFromProperties: Boolean): TurboGraphConnection =
    {
        val graphConnect: TurboGraphConnection = initializeGraph(loadFromProperties)
        if (graphConnect.getConnection() != null)
        {
            if (loadFromProperties) loadDataFromPropertiesFile(graphConnect.getConnection())
            if (importOntologies == "true") loadOntologyFromURLIfNotAlreadyLoaded(graphConnect.getConnection(), graphConnect.getRepository())
        }
        graphConnect
    }
    
    /**
     * Initializes the connection to the Graph DB instance after checking that the TURBO Properties file is in a valid state. Validates connection
     * with username and password specified in TURBO properties file and returns TurboGraphConnection object.
     */
    def initializeGraph(loadFromProperties: Boolean): TurboGraphConnection =
    {
        val missingProperty: Option[String] = checkForMissingProperties(loadFromProperties)
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
     * Handles loading data files into their respective named graphs, as declared in TURBO Properties file. Checks to make sure that 
     * data files and named graphs are declared properly, then passes information to helper method to complete the data load.
     */
    def loadDataFromPropertiesFile(cxn: RepositoryConnection, inputProp: String = inputFiles, ngPrefix: String = "Shortcuts", allInOneGraph: Boolean = false)
    {
        val filesToLoadList: HashMap[String, HashMap[String, RDFFormat]] = generateLoadList(inputProp, ngPrefix, allInOneGraph)
        for ((k,v) <- filesToLoadList)
        {
            var namedgraph: String = ""
            var format: Option[RDFFormat] = None : Option[RDFFormat]
            for ((a,b) <- v)
            {
                namedgraph = a
                format = Some(v(a))
            }
            helper.loadDataFromFile(cxn, k, format.get, namedgraph)
        }
        if (importOntologies == "true") OntologyLoader.addOntologyFromUrl(cxn, ontologyURL)
    }
    
    /**
     * Checks to see whether there is already data in named graph pmbb:ontology, and if there is not, inserts the TURBO ontology into the pmbb:ontology
     * named graph. Neither of these variables are specified in call to helper.addOntologyFromURL(cxn) because they are default values for named graph
     * and ontology URL variables in that method.
     */
    def loadOntologyFromURLIfNotAlreadyLoaded(cxn: RepositoryConnection, repo: Repository)
    {
        val f: ValueFactory = cxn.getValueFactory()
        val ontoGraphName = f.createIRI("http://www.itmat.upenn.edu/biobank/ontology");
        //check to see if there is already an ontology
        if (helper.isThereDataInNamedGraph(cxn, ontoGraphName)) logger.info("There is already data in named graph \"pmbb:ontology\"")
        //check to see if 
        else
        {
            OntologyLoader.addOntologyFromUrl(cxn)
        } 
    }
    
    /**
     * Reads the files-to-load list and their respective named graphs from the TURBO properties file and checks to make sure that these lists are valid.
     * If they are, returns a mapping of file names to named graphs/RDF format of the files. 
     */
    def generateLoadList(inputProp: String, ngPrefix: String, allInOneGraph: Boolean): HashMap[String, HashMap[String, RDFFormat]] =
    {
        val mapToReturn: HashMap[String, HashMap[String, RDFFormat]] = new HashMap[String, HashMap[String, RDFFormat]]
        
        val filesToLoad: String = inputProp
        val namedGraphs: String = inputFilesNamedGraphs
        val formats: String = inputFilesFormat
        
        val ftlList: ArrayBuffer[String] = parseCSVString(filesToLoad)
        val ngList: ArrayBuffer[String] = generateShortcutNamedGraphsList(ftlList, ngPrefix, allInOneGraph)
        var formatList: ArrayBuffer[String] = parseCSVString(formats)
        
        // If only one format is required, we infer that this is meant to apply to all files.
        if (formatList.size == 1 && ftlList.size > 1)
        {
            for (a <- 1 to ftlList.size - 1) formatList += formatList(0)
        }
        
        if (ftlList.size != ngList.size) 
        {
            logger.info("Internal Drivetrain Error: The files to load list and named graphs list are of different sizes.")
            throw new RuntimeException ("Error in properties file input, check logger message for details.")
        }
        if (formatList.size != ftlList.size)
        {
            logger.info("Internal Drivetrain Error: The input files format list and files to load list are of different sizes.")
            throw new RuntimeException ("Error in properties file input, check logger message for details.")
        }
        if (formatList.size != ngList.size)
        {
            logger.info("Internal Drivetrain Error: The input files format list and named graphs list are of different sizes.")
            throw new RuntimeException ("Error in properties file input, check logger message for details.")
        }
      
        for (a <- 0 to ftlList.size - 1)
        {
            var format: Option[RDFFormat] = None : Option[RDFFormat]
            //println("format list: " + formatList(a))
            if (formatList(a) == "TURTLE") format = Some(RDFFormat.TURTLE)
            if (formatList(a) == "RDFXML") format = Some(RDFFormat.RDFXML)
            mapToReturn += ftlList(a) -> HashMap(ngList(a) -> format.get)
        }
        mapToReturn
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
        
        cxn.close()
        repository.shutDown()
        repoManager.shutDown()
        
        gmCxn.close()
        gmRepository.shutDown()
        gmRepoManager.shutDown()
        
        testCxn.close()
        testRepository.shutDown()
        testRepoManager.shutDown()
    }
    
    /**
     * Helper method called by generateLoadList method. Receives a string and separates values in string into a list based on comma placement in string.
     */
    def parseCSVString(a: String): ArrayBuffer[String] = 
    {
        val listToReturn: ArrayBuffer[String] = new ArrayBuffer[String]
        var stringInProgress: String = ""
        for (char <- a)
        {
            if (char != ',') stringInProgress += char
            else if (char == ',')
            {
                listToReturn += stringInProgress
                stringInProgress = ""
            }
        }
        listToReturn += stringInProgress
        logger.info("returning this list: " + listToReturn)
        listToReturn
    }
    
    /**
     * Helper method called by graph connection methods in order to validate properties file. Ensures that all required properties for connection to Graph DB
     * are present in properties file.
     */
    def checkForMissingProperties(requiredInputFileProps: Boolean): Option[String] =
    { 
        val input: FileInputStream = new FileInputStream("..//turbo_properties.properties")
        val props: Properties = new Properties()
        props.load(input)
        input.close()
        var optToReturn: Option[String] = None : Option[String]
        val proceed: Boolean = true
        var requiredProperties: ArrayBuffer[String] = ArrayBuffer("serviceURL",
            "password","username","productionRepository",
            "ontologyURL", "modelRepository", "testingRepository", "processNamedGraph")
        var a = 0
        while (optToReturn == None && a < requiredProperties.size)
        {
            //println("testing property " + requiredProperties(a))
            try
            {
                props.getProperty(requiredProperties(a)).isEmpty()
            }
            catch
            {
                
                case e: NullPointerException => optToReturn = Some(requiredProperties(a))
            }
            a = a + 1
        }
        // check for deprecated input graphs property
        if (props.getProperty("inputFilesNamedGraphs") != null) logger.warn("Ignoring deprecated property 'inputFilesNamedGraphs'")
        optToReturn
    }
    
    /**
     * Receives a list of all files being loaded into the graph. Generates the name of a named graph for each of the files to be loaded into based on
     * the name of the file.
     * 
     * @return a list of strings representing the named graph which each file will be loaded into at that specific index in the input list
     */
    def generateShortcutNamedGraphsList(newFiles: ArrayBuffer[String], ngPrefix: String, allInOneGraph: Boolean): ArrayBuffer[String] =
    {
        var listToReturn: ArrayBuffer[String] = new ArrayBuffer[String]
        for (file <- newFiles)
        {
            var namedGraph: String = "http://www.itmat.upenn.edu/biobank/" + ngPrefix 
            if (!allInOneGraph) namedGraph += "_" +
                java.util.UUID.randomUUID().toString.replaceAll("-","") + "_" + helper.getPostfixfromURI(file).replaceAll(" ", "")
            listToReturn += namedGraph
        }
        listToReturn
    }
}