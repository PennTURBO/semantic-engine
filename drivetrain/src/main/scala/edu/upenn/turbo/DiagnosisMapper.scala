package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFFormat
import scala.collection.mutable.ArrayBuffer

/**
 * Contains methods related to mapping diagnosis codes to ontology classes.
 */

// we are not currently using this class as of 8-17-2018
class DiagnosisMapper extends ProjectwideGlobals
{
    val connection: ConnectToGraphDB = new ConnectToGraphDB()
    val ontLoad: OntologyLoader = new OntologyLoader
    
    /**
     * Adds relevant ontologies to Graph database.
     */
    def addDiseaseOntologies(cxn: RepositoryConnection)
    {
        logger.info("adding ICD9 ontology")
        helper.loadDataFromFile(cxn, "../ontologies/ICD9CM.ttl", RDFFormat.TURTLE, "http://www.itmat.upenn.edu/biobank/ICD9Ontology")
        logger.info("adding ICD10 ontology")
        helper.loadDataFromFile(cxn, "../ontologies/ICD10CM.ttl", RDFFormat.TURTLE, "http://www.itmat.upenn.edu/biobank/ICD10Ontology")
        logger.info("Successfully loaded ontologies")
    }
    
    /**
     * Calls relevant diagnosis mapping methods.
     */
    def performDiagnosisMapping(cxn: RepositoryConnection)
    {
         mapToMondo(cxn)
         mapToICD9(cxn)
         mapToICD10(cxn)
         logger.info("diagnosis mapping complete")
    }
    
    /**
     * Attempts to map unmapped diagnoses to classes in the Mondo ontology.
     */
    def mapToMondo(cxn: RepositoryConnection)
    {
        val insert1 = """insert {
        graph pmbb:diag2disease {
            ?diagnosis <http://purl.obolibrary.org/obo/IAO_0000142> ?d
        }
        }
        where {
              graph pmbb:expanded
              {
                  ?encounter a <http://purl.obolibrary.org/obo/OGMS_0000097> ;
                       obo:RO_0002234 ?diagnosis .
                  ?diagnosis a obo:OGMS_0000073 .
                  ?diagCrid obo:IAO_0000219 ?diagnosis .
                  ?diagCrid a :TURBO_0000553 ;
                            <http://purl.obolibrary.org/obo/BFO_0000051> ?diagcode, ?regdenoter .
                  ?diagcode a :TURBO_0000554;
                            turbo:TURBO_0006510 ?dclv .
                  ?regdenoter a  :TURBO_0000555;
                              :TURBO_0006512  ?regdenlv ;
                              <http://purl.obolibrary.org/obo/IAO_0000219> ?reguri .
              }
              graph pmbb:ontology
              {
                  ?reguri a nci:C49474 .
              }
              bind(concat(replace(?regdenlv, "-", ""),":",?dclv) as ?crid_rewrite)
              graph pmbb:mondoOntology
              {
                  ?d <http://www.geneontology.org/formats/oboInOwl#hasDbXref> ?xref .
              }
             filter(?crid_rewrite = ?xref)
        }"""
    
        update.updateSparql(cxn, sparqlPrefixes + insert1)
    }
    
    /**
     * Attempts to map unmapped diagnoses to classes in the ICD9 ontology.
     */
    def mapToICD9(cxn: RepositoryConnection)
    {
        val insertICD9matches = """
          insert { graph pmbb:diag2disease {
         ?diagnosis obo:IAO_0000142 ?d
          }}
          where {
              ?encounter a <http://purl.obolibrary.org/obo/OGMS_0000097> ;
                        obo:RO_0002234 ?diagnosis .
             ?diagnosis a obo:OGMS_0000073 .
             ?diagcrid obo:IAO_0000219 ?diagnosis .
             ?diagcrid a :TURBO_0000553 ;
                       <http://purl.obolibrary.org/obo/BFO_0000051> ?diagcode, ?regdenoter .
             ?diagcode a :TURBO_0000554;
                       turbo:TURBO_0006510 ?dclv .
             ?regdenoter a  :TURBO_0000555;
                         :TURBO_0006512  ?regdenlv .
                 ?regdenoter obo:IAO_0000219 nci:C71890 .
                 nci:C71890 a nci:C49474 .
                 
            Graph pmbb:ICD9Ontology 
            {
               ?d skos:notation ?dclv .
            }}
          """
        
        update.updateSparql(cxn, sparqlPrefixes + insertICD9matches)
    }
    
    /**
     * Attempts to map unmapped diagnoses to classes in the ICD10 ontology.
     */
    def mapToICD10(cxn: RepositoryConnection)
    {
        val insertICD10matches = """
          insert { graph pmbb:diag2disease {
         ?diagnosis obo:IAO_0000142 ?d
          }}
          where {
              ?encounter a <http://purl.obolibrary.org/obo/OGMS_0000097> ;
                        obo:RO_0002234 ?diagnosis .
             ?diagnosis a obo:OGMS_0000073 .
             ?diagcrid obo:IAO_0000219 ?diagnosis .
             ?diagcrid a :TURBO_0000553 ;
                       <http://purl.obolibrary.org/obo/BFO_0000051> ?diagcode, ?regdenoter .
             ?diagcode a :TURBO_0000554;
                       turbo:TURBO_0006510 ?dclv .
             ?regdenoter a  :TURBO_0000555;
                         :TURBO_0006512  ?regdenlv .
                 ?regdenoter obo:IAO_0000219 nci:C71892 .
                 nci:C71892 a nci:C49474 .
                 
            Graph pmbb:ICD10Ontology 
            {
               ?d skos:notation ?dclv .
            }}
          """
        
         update.updateSparql(cxn, sparqlPrefixes + insertICD10matches)
    }
}