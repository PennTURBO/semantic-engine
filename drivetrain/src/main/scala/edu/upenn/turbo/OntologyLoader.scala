package edu.upenn.turbo

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import java.util.Properties
import java.io.FileInputStream
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.util.Calendar
import java.text.SimpleDateFormat
import java.net.URL
import java.net.ConnectException
import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.OpenRDFException
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.query.BooleanQuery
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.slf4j.LoggerFactory
import java.io.InputStreamReader
import java.io.Reader
import java.io.BufferedInputStream
import java.io.InputStream
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.Model
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import java.net.SocketException

object OntologyLoader extends ProjectwideGlobals
{   
    def addGeneOntologies(cxn: RepositoryConnection)
    {
        val geneOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "http://purl.obolibrary.org/obo/go.owl" -> Map("http://purl.obolibrary.org/obo/go.owl" -> RDFFormat.RDFXML),
        "https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl_variation_ontology.owl" -> Map("https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl_variation_ontology.owl" -> RDFFormat.RDFXML),
        "https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl-mapping.owl" -> Map("https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl-mapping.owl" -> RDFFormat.RDFXML),
        "https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl-terms.rdf" -> Map("https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl-terms.rdf" -> RDFFormat.RDFXML),
        "https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl-void.ttl" -> Map("https://raw.githubusercontent.com/Ensembl/VersioningService/master/rdf-support-files/ensembl-void.ttl" -> RDFFormat.TURTLE)
        )
        addOntologiesFromMap(cxn, geneOntologies)
    }
    
    def addDrugOntologies(cxn: RepositoryConnection)
    {
        val drugOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl" -> Map("ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-pro.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-pro.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ndc.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ndc.owl" -> RDFFormat.RDFXML),
        "http://data.bioontology.org/ontologies/RXNORM/submissions/"+getBioportalSubmissionInfo("RXNORM").get+"/download?apikey="+bioportalApiKey -> 
          Map("http://data.bioontology.org/ontologies/RXNORM/" -> RDFFormat.TURTLE)
        )
        
        addOntologiesFromMap(cxn, drugOntologies)
    }
    
    def addDiseaseOntologies(cxn: RepositoryConnection)
    {
        val diseaseOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "https://github.com/monarch-initiative/mondo/releases/download/current/mondo.owl" -> Map("https://github.com/monarch-initiative/mondo/releases/download/current/mondo.owl" -> RDFFormat.RDFXML),
        "http://data.bioontology.org/ontologies/ICD10CM/submissions/"+getBioportalSubmissionInfo("ICD10CM").get+"/download?apikey="+bioportalApiKey -> 
                Map("http://data.bioontology.org/ontologies/ICD10CM/" -> RDFFormat.TURTLE),
        "http://data.bioontology.org/ontologies/ICD9CM/submissions/"+getBioportalSubmissionInfo("ICD9CM").get+"/download?apikey="+bioportalApiKey -> 
                Map("http://data.bioontology.org/ontologies/ICD9CM/" -> RDFFormat.TURTLE)
        )
        
        addOntologiesFromMap(cxn, diseaseOntologies)
    }
    
    def addMiscOntologies(cxn: RepositoryConnection)
    {
        val miscOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro_reasoned.owl" -> Map("ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro_reasoned.owl" -> RDFFormat.RDFXML)
        )

        addOntologiesFromMap(cxn, miscOntologies)
    }
    
    def addOntologiesFromMap(cxn: RepositoryConnection, ontMap: Map[String, Map[String, RDFFormat]])
    {
        for((ontology, formatting) <- ontMap) addOntologyFromUrl(cxn, ontology, formatting) 
    }
    
     /**
     * Adds an RDF.XML formatted set of triples (usually an ontology) received from a given URL to the specified named graph.
     */
    def addOntologyFromUrl(cxn: RepositoryConnection, ontology: String = ontologyURL, 
        formatting: Map[String, RDFFormat] = Map(ontologyURL -> RDFFormat.RDFXML)) 
    {
        if (formatting.size > 1) throw new RuntimeException ("Formatting map size > 1, internal error occurred.")
        logger.info("Adding ontology " + ontology)
        try
        {

            val f = cxn.getValueFactory
            val OntoUrl = new URL(ontology)
            val OntoGraphName = f.createIRI(formatting.head._1)
        
            val OntoBase = "http://transformunify.org/ontologies/"
         
            //logger.info("At add step...")
            helper.clearNamedGraph(cxn, OntoGraphName.toString)
            cxn.add(OntoUrl, OntoBase, formatting.head._2, OntoGraphName)
            //logger.info("Finished try block.")
        }
        catch
        {
            case g: SocketException => 
            {
                logger.info("The ontology " + ontology + " was not loaded - socket exception thrown")
                logger.info("" + g.printStackTrace)
            }
            case e: RuntimeException => 
            {
                logger.info("The ontology " + ontology + " was not loaded - runtime exception.")
                logger.info("" + e.printStackTrace)
            }
        }
        //logger.info("Committing transaction...")
        //logger.info("Committing complete.")
    }
    
    def getBioportalSubmissionInfo(ontology: String): Option[Int] =
    {
        val url = "http://data.bioontology.org/ontologies/"+ontology+"/latest_submission?apikey="+bioportalApiKey
        try
        {
            var optReturn: Option[Int] = None:Option[Int]
    
            val httpClient = HttpClientBuilder.create().build()
            val httpResponse = httpClient.execute(new HttpGet(url))
            val entity = httpResponse.getEntity()
            var content = ""
            if (entity != null) 
            {
                val inputStream = entity.getContent()
                content = io.Source.fromInputStream(inputStream).getLines.mkString
                inputStream.close
            }
            httpClient.close()
            httpResponse.close()
            val list = content.substring(1, content.length - 1).split(",").map(_.split(":"))
            var map: HashMap[String, String] = new HashMap[String, String]
            for (row <- list) if (row.size > 1) map += row(0).substring(1,row(0).length-1) -> row(1)
            try
            {
                Some(map("submissionId").toInt)
            }
            catch 
            {
                case e: NumberFormatException => logger.info("Whoops! It looks like we received non-numeric version info from " + url)
                None
            }
        }
        catch
        {
            case e: RuntimeException => logger.info("Something went wrong when looking up bioportal submission info from " + url)
            None
        }
    }
}