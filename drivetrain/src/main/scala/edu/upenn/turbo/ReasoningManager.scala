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

class ReasoningManager extends ProjectwideGlobals
{
    private val supportedReasoningLevels: ArrayBuffer[String] = ArrayBuffer(
        "empty",
        "rdfsplus-optimized",
        "owl-horst-optimized"
    )
  
    def changeReasoningLevel(cxn: RepositoryConnection, newLevel: String)
    {
        if (!supportedReasoningLevels.contains(newLevel)) logger.info("Reasoning level " + newLevel + " is not supported.")
        else 
        {
            logger.info("Attempting to change reasoning level to " + newLevel)
            val addRuleset: String = """ INSERT DATA {_:b sys:addRuleset """"+newLevel+"""" } """
            val setDefaultRuleset: String = """ INSERT DATA {_:b sys:defaultRuleset """"+newLevel+"""" } """    
            update.updateSparql(cxn, addRuleset)
            update.updateSparql(cxn, setDefaultRuleset)
        }
    }
    
    def reinferRepository(cxn: RepositoryConnection)
    {
        logger.info("Reinferring...(this may take several hours)")
        val reinferRepo: String = """ INSERT DATA {[] <http://www.ontotext.com/owlim/system#reinfer> []} """
        update.updateSparql(cxn, reinferRepo)
        logger.info("Reinferring complete")
    }
}