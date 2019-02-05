package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import scala.collection.mutable.HashMap

class Query
{
    var insertClause: String = "INSERT {"
    var deleteClause: String = "DELETE {"
    var selectClause: String = "SELECT "
    var whereClause: String = "WHERE {"
    var bindClause: String = ""

    def buildInsertQuery(): String = {insertClause + "\n" + whereClause + "\n" + bindClause + "}"}
    def buildSelectQuery(): String = {selectClause + "\n" + whereClause + "\n" + bindClause + "}"}
    def buildDeleteQuery(): String = {deleteClause + "\n" + whereClause + "\n" + bindClause + "}"}
    def buildInsertWithDeleteQuery(): String = {deleteClause + "\n" + insertClause + "\n" + whereClause + "\n" + bindClause + "}"}

}