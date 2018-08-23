package edu.upenn.turbo

import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import java.io.Reader
import java.io.FileReader
import java.io.BufferedReader
import scala.collection.mutable.HashMap
import java.io.PrintWriter
import org.ddahl.rscala
import scala.collection.mutable.ArrayBuffer
import java.util.Arrays
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.ValueFactory

class MedicationMapper extends ProjectwideGlobals
{   
    val connect: ConnectToGraphDB = new ConnectToGraphDB
    
    def runMedicationMapping(cxn: RepositoryConnection)
    {
        // make connection to med mapping repo
        var medConnection: RepositoryConnection = null
        var medRepository: Repository = null
        var medRepoManager: RemoteRepositoryManager = null
        try
        {
            val medGraphConnect: TurboGraphConnection = connect.initializeGraph(medMappingRepo)
            medConnection = medGraphConnect.getConnection()
            medRepoManager = medGraphConnect.getRepoManager()
            medRepository = medGraphConnect.getRepository() 
            
            
        }
        finally 
        {
            connect.closeGraphConnection(medConnection, medRepoManager, medRepository, false)
        }
    }
    
    def getAllUnmappedMedsInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getInfo: String = 
         """
             Select ?prescript ?ordername Where
             {
                 ?prescript a obo:PDRO_0000001 .
                 ?prescript turbo:TURBO_0006512 ?ordername .
             }         
         """
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getInfo, ArrayBuffer("prescript", "ordername"))
    }
}