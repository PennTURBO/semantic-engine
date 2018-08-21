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
    private val drugOntologies: Map[String, String] = Map(
        "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl" -> "ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology/chebi_lite.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/6bcc56a003c6c4db6ffbcbca04e10d2712fadfd8/dron-rxnorm.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-chebi.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-hand.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-upper.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ingredient.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-pro.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-pro.owl",
        "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ndc.owl" -> "https://bitbucket.org/uamsdbmi/dron/raw/master/dron-ndc.owl"
    )
        
    private val diseaseOntologies: Map[String, String] = Map(
        "https://raw.githubusercontent.com/monarch-initiative/monarch-disease-ontology/master/src/mondo/mondo.owl" -> "https://raw.githubusercontent.com/monarch-initiative/monarch-disease-ontology/master/src/mondo/mondo.owl"
    )
    
    private val miscOntologies: Map[String, String] = Map(
        "ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro_reasoned.owl" -> "ftp://ftp.pir.georgetown.edu/databases/ontology/pro_obo/pro_reasoned.owl"  
    )
    
    def addDrugOntologies(cxn: RepositoryConnection)
    {
        for((ontology, namedGraph) <- drugOntologies) addOntologyFromUrl(cxn, ontology, namedGraph)
    }
    
    def addDiseaseOntologies(cxn: RepositoryConnection)
    {
        for((ontology, namedGraph) <- diseaseOntologies) addOntologyFromUrl(cxn, ontology, namedGraph)
    }
    
    def addMiscOntologies(cxn: RepositoryConnection)
    {
        for((ontology, namedGraph) <- miscOntologies) addOntologyFromUrl(cxn, ontology, namedGraph)
    }
    
     /**
     * Adds an RDF.XML formatted set of triples (usually an ontology) received from a given URL to the specified named graph.
     */
    def addOntologyFromUrl(cxn: RepositoryConnection, 
        ontology: String = "https://raw.githubusercontent.com/PennTURBO/Turbo-Ontology/master/ontologies/turbo_merged.owl", 
        namedGraph: String = "http://www.itmat.upenn.edu/biobank/ontology") 
    {
        try
        {
            val f = cxn.getValueFactory
            val OntoUrl = new URL(ontology)
            val OntoGraphName = f.createIRI(namedGraph);
        
            val OntoBase = "http://transformunify.org/ontologies/"
         
            cxn.begin()
            cxn.add(OntoUrl, OntoBase, RDFFormat.RDFXML, OntoGraphName)
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