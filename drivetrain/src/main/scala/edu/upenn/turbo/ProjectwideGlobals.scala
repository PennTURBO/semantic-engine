package edu.upenn.turbo

import org.slf4j.LoggerFactory

trait ProjectwideGlobals 
{
    val helper: TurboMultiuseClass = new TurboMultiuseClass
    val logger = LoggerFactory.getLogger(getClass)
    
    val healthcareEncounterShortcutGraphs: String = 
      """
      <http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts>
      <http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts1>
      <http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts2>
      <http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts3>
      <http://www.itmat.upenn.edu/biobank/healthcareEncounterShortcuts4>
      """
    
    val healthcareEncounterShortcutGraphsWithFROM: String = 
     """
        FROM pmbb:healthcareEncounterShortcuts
        FROM pmbb:healthcareEncounterShortcuts1
        FROM pmbb:healthcareEncounterShortcuts2
        FROM pmbb:healthcareEncounterShortcuts3
        FROM pmbb:healthcareEncounterShortcuts4
     """
    
    //make sparqlPrefixes for use in all queries globally available
    val sparqlPrefixes = """
			PREFIX  :     <http://transformunify.org/ontologies/>
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
			PREFIX luceneInst: <http://www.ontotext.com/connectors/lucene/instance#>
			PREFIX lucene: <http://www.ontotext.com/connectors/lucene#>
			PREFIX efo: <http://www.ebi.ac.uk/efo/>
			PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
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
     val ontologySize = helper.retrievePropertyFromFile("ontologySize")    
     val reinferRepo = helper.retrievePropertyFromFile("reinferRepo")
     val setReasoningTo = helper.retrievePropertyFromFile("setReasoningTo")
     val solrURL = helper.retrievePropertyFromFile("solrURL")
     val SVMfile = helper.retrievePropertyFromFile("SVMfile")
     val medStandardsFile = helper.retrievePropertyFromFile("medStandardsFile")
     val dronRepo = helper.retrievePropertyFromFile("dronRepo")
}