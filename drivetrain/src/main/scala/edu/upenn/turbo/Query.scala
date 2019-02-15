package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import scala.collection.mutable.HashMap

class Query extends ProjectwideGlobals
{
    var insertClause: String = "INSERT {"
    var deleteClause: String = "DELETE {"
    var selectClause: String = "SELECT "
    var whereClause: String = "WHERE {"
    var bindClause: String = ""
    var filterClause: String = ""

    def buildInsertQuery(): String = {sparqlPrefixes + "\n" + insertClause + "\n" + whereClause + "\n" + 
                                      bindClause + "\n" + filterClause + "}" }
    def buildSelectQuery(): String = {sparqlPrefixes + "\n" + selectClause + "\n" + whereClause + "\n" + 
                                      bindClause + "\n" + filterClause + "}" }
    def buildDeleteQuery(): String = {sparqlPrefixes + "\n" + deleteClause + "\n" + whereClause + "\n" + 
                                      bindClause + "\n" + filterClause + "}" }
    def buildInsertWithDeleteQuery(): String = {sparqlPrefixes + "\n"+ deleteClause + "\n" + insertClause + 
                                                "\n" + whereClause + "\n" + bindClause + "\n" + filterClause + "}" }

}