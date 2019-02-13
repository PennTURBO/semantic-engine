package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer
import java.util.UUID
import scala.collection.mutable.HashMap

trait IRIConstructionRules
{
    val RandomUUID: String = """BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",REPLACE(struuid(), "-", ""))) AS ?replacement)"""

    val MD5LocalRandom: String = """BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",
                                md5(CONCAT("replacement","localUUID", str(?mainExpansionTypeVariableName))))) AS ?replacement)"""

    val MD5GlobalRandom: String = """BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",
                                md5(CONCAT("replacement","globalUUID", str(?mainExpansionTypeVariableName))))) AS ?replacement)"""

    val StringToURI: String = """BIND(uri(?original) AS ?replacement)"""

    val URIToString: String = """BIND(str(?original) AS ?replacement)"""

    val DatasetIRI: String = """BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/",
                                md5(CONCAT("replacement", "globalUUID", str(?shortcutDatasetTitle))))) AS ?replacement)"""

    val BiologicalSexIRI: String = """BIND(IF(BOUND(?dependent), uri(?dependent), obo:OMRSE_00000133) AS ?replacement)"""

    val BindIfBoundDataset: String = """BIND(IF(BOUND(?dependent), ?dataset, ?unbound) AS ?replacement)"""

    val BindIfBoundMD5LocalRandom: String = """BIND (IF (BOUND(?dependent), uri(concat("http://www.itmat.upenn.edu/biobank/",
                                        md5(CONCAT("replacement", "localUUID", str(?mainExpansionTypeVariableName))))), ?unbound) AS ?replacement)"""

    val BindIfBoundRandomUUID: String = """BIND(IF (BOUND(?dependent), uri(concat("http://www.itmat.upenn.edu/biobank/", 
                                            REPLACE(struuid(), "-", ""))), ?unbound) AS ?replacement)"""

    val BindAs: String = """BIND (?original AS ?replacement)"""
}