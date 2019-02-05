package edu.upenn.turbo

import org.slf4j.LoggerFactory

trait ProjectwideGlobals 
{
    val helper: TurboMultiuseClass = new TurboMultiuseClass
    val update: SparqlUpdater = new SparqlUpdater
    val logger = LoggerFactory.getLogger(getClass)
    
    //make sparqlPrefixes for use in all queries globally available
    val sparqlPrefixes = """
			PREFIX  dc11: <http://purl.org/dc/elements/1.1/>
			PREFIX  obo:  <http://purl.obolibrary.org/obo/>
			PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
			PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>
			PREFIX  turbo: <http://transformunify.org/ontologies/>
			PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>
			PREFIX  nci:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
			PREFIX graphBuilder: <http://graphBuilder.org/>
			PREFIX pmbb: <http://www.itmat.upenn.edu/biobank/>
			PREFIX sys: <http://www.ontotext.com/owlim/system#>
			PREFIX efo: <http://www.ebi.ac.uk/efo/>
			PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
			PREFIX ns1: <http://www.geneontology.org/formats/oboInOwl#>
			"""
    
     //properties from file are global variables
     val serviceURL = helper.retrievePropertyFromFile("serviceURL")
     val ontologyURL = helper.retrievePropertyFromFile("ontologyURL")
     val namespace = helper.retrievePropertyFromFile("namespace")
     val inputFiles = helper.retrievePropertyFromFile("inputFiles")
     val inputFilesNamedGraphs = helper.retrievePropertyFromFile("inputFilesNamedGraphs")
     val inputFilesFormat = helper.retrievePropertyFromFile("inputFilesFormat")
     val importOntologies = helper.retrievePropertyFromFile("importOntologies")
     val errorLogFile = helper.retrievePropertyFromFile("errorLogFile")
     val applyLabels = helper.retrievePropertyFromFile("applyLabels")
     val inputLOFFiles = helper.retrievePropertyFromFile("inputLOFFiles")
     val loadDiseaseOntologies = helper.retrievePropertyFromFile("loadDiseaseOntologies")
     val loadDrugOntologies = helper.retrievePropertyFromFile("loadDrugOntologies")
     val bioportalAPIkey = helper.retrievePropertyFromFile("bioportalAPIKey")
     val medMappingRepo = helper.retrievePropertyFromFile("medMappingRepo")
     val loadLOFdata = helper.retrievePropertyFromFile("loadLOFData")
}