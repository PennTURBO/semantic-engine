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

class OntologyLoader extends ProjectwideGlobals
{   
    private val drugOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl" -> Map("ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-pro.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-pro.owl" -> RDFFormat.RDFXML),
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ndc.owl" -> Map("https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ndc.owl" -> RDFFormat.RDFXML)
    )
        
    private val diseaseOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "https://raw.githubusercontent.com/monarch-initiative/monarch-disease-ontology/master/src/mondo/mondo.owl" -> Map("https://raw.githubusercontent.com/monarch-initiative/monarch-disease-ontology/master/src/mondo/mondo.owl" -> RDFFormat.RDFXML),
        "http://data.bioontology.org/ontologies/ICD10CM/submissions/14/download?apikey=5095cf97-751f-46c6-81fe-428b8d124480" -> Map("http://data.bioontology.org/ontologies/ICD10CM/submissions/14/" -> RDFFormat.TURTLE),
        "http://data.bioontology.org/ontologies/ICD9CM/submissions/14/download?apikey=5095cf97-751f-46c6-81fe-428b8d124480" -> Map("http://data.bioontology.org/ontologies/ICD9CM/submissions/14/" -> RDFFormat.TURTLE)
    )
    
    private val miscOntologies: Map[String, Map[String, RDFFormat]] = Map(
        "ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro_reasoned.owl" -> Map("ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro_reasoned.owl" -> RDFFormat.RDFXML),  
        "http://purl.obolibrary.org/obo/go.owl" -> Map("http://purl.obolibrary.org/obo/go.owl" -> RDFFormat.RDFXML)
    )
    
    def addDrugOntologies(cxn: RepositoryConnection)
    {
        for((ontology, formatting) <- drugOntologies) addOntologyFromUrl(cxn, ontology, formatting)
    }
    
    def addDiseaseOntologies(cxn: RepositoryConnection)
    {
        for((ontology, formatting) <- diseaseOntologies) addOntologyFromUrl(cxn, ontology, formatting)
    }
    
    def addMiscOntologies(cxn: RepositoryConnection)
    {
        for((ontology, formatting) <- miscOntologies) 
        {
            logger.info("adding ontology: " + ontology)
            addOntologyFromUrl(cxn, ontology, formatting)
        }
    }
    
     /**
     * Adds an RDF.XML formatted set of triples (usually an ontology) received from a given URL to the specified named graph.
     */
    def addOntologyFromUrl(cxn: RepositoryConnection, ontology: String = ontologyURL, 
        formatting: Map[String, RDFFormat] = Map("http://www.itmat.upenn.edu/biobank/ontology" -> RDFFormat.RDFXML)) 
    {
        if (formatting.size > 1) throw new RuntimeException ("Formatting map size > 1, internal error occurred.")
        try
        {
            val f = cxn.getValueFactory
            val OntoUrl = new URL(ontology)
            val OntoGraphName = f.createIRI(formatting.head._1)
        
            val OntoBase = "http://transformunify.org/ontologies/"
         
            cxn.begin()
            cxn.add(OntoUrl, OntoBase, formatting.head._2, OntoGraphName)
            cxn.commit()
        }
        catch
        {
            case f: ConnectException => logger.info("The ontology was not loaded. Please ensure that your URL in the properties file is correct and that the server is online.")
            throw new RuntimeException ("A connection to the ontology could not be established.")
            case e: RuntimeException => logger.info("The ontology was not loaded. Please ensure that your URL in the properties file is correct and that the server is online.")
            throw new RuntimeException ("The ontology could not be accessed at the specified URL.")
        }
    }
}