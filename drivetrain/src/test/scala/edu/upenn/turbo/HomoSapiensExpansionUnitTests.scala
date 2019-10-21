package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import java.util.UUID

class HomoSapiensExpansionUnitTests extends ProjectwideGlobals with FunSuiteLike with BeforeAndAfter with Matchers
{
    val clearTestingRepositoryAfterRun: Boolean = false
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
    val instantiationAndDataset: String = s"""
      ASK { GRAPH <$expandedNamedGraph> {
          
        ?instantiation <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
    		?instantiation obo:OBI_0000293 ?dataset .
    		?dataset a obo:IAO_0000100 .
    		?dataset dc11:title "part_expand" .
       }}"""
    
    val minimumPartRequirements: String = s"""
      ASK { GRAPH <$expandedNamedGraph> {
          
          ?part a obo:NCBITaxon_9606 .
          
          ?partCrid a turbo:TURBO_0010092 .
          ?partCrid obo:IAO_0000219 ?part .
          ?partCrid obo:BFO_0000051 ?partSymbol .
          ?partCrid obo:BFO_0000051 turbo:TURBO_0000505 .
          ?partSymbol a turbo:TURBO_0000504 .
          ?partSymbol turbo:TURBO_0010094 "4" .
          
          ?partSymbol obo:BFO_0000050 ?dataset .
          ?dataset a obo:IAO_0000100 .
          
       }}"""
    
    val processMeta = helper.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess", 
                                                   "http://www.itmat.upenn.edu/biobank/Shortcuts_homoSapiensShortcuts")
    
    val anyProcess: String = s"""
      ASK
      {
          Graph <$processNamedGraph>
          {
              ?s ?p ?o .
          }
      }
      """
    
    val expectedQuery: String = s"""
      INSERT {
      GRAPH <$expandedNamedGraph> {
      ?GenderIdentityDatumOfVariousTypes rdf:type ?GenderIdentityDatumType .
      ?RaceIdentityDatumOfVariousTypes rdf:type ?RaceIdentityDatumType .
      ?TURBO_0010092 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000504 .
      ?TURBO_0010092 rdf:type <http://transformunify.org/ontologies/TURBO_0010092> .
      ?TURBO_0000504 rdf:type <http://transformunify.org/ontologies/TURBO_0000504> .
      ?TURBO_0010092 <http://purl.obolibrary.org/obo/BFO_0000051> ?HomoSapiensRegistryOfVariousTypes .
      ?HomoSapiensRegistryOfVariousTypes <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0010092 .
      ?GenderIdentityDatumOfVariousTypes <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
      ?NCBITaxon_9606 rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
      ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000086> ?PATO_0000047 .
      ?PATO_0000047 rdf:type <http://purl.obolibrary.org/obo/PATO_0000047> .
      ?TURBO_0000504 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 rdf:type <http://purl.obolibrary.org/obo/IAO_0000100> .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000504 .
      ?GenderIdentityDatumOfVariousTypes <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?GenderIdentityDatumOfVariousTypes .
      ?RaceIdentityDatumOfVariousTypes <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
      ?RaceIdentityDatumOfVariousTypes <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?RaceIdentityDatumOfVariousTypes .
      ?EFO_0004950 <http://purl.obolibrary.org/obo/IAO_0000136> ?UBERON_0035946 .
      ?EFO_0004950 rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
      ?UBERON_0035946 rdf:type <http://purl.obolibrary.org/obo/UBERON_0035946> .
      ?NCBITaxon_9606 <http://transformunify.org/ontologies/TURBO_0000303> ?UBERON_0035946 .
      ?EFO_0004950 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?EFO_0004950 .
      ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010113> ?NCBITaxon_9606 .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      ?TURBO_0010092 <http://purl.obolibrary.org/obo/IAO_0000219> ?NCBITaxon_9606 .
      ?TURBO_0000522 <http://purl.obolibrary.org/obo/OBI_0000293> ?IAO_0000100 .
      ?TURBO_0000522 rdf:type <http://transformunify.org/ontologies/TURBO_0000522> .
      ?TURBO_0000504 <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0010092 .
      ?TURBO_0010168 <http://transformunify.org/ontologies/TURBO_0010113> ?TURBO_0010092 .
      ?TURBO_0010168 rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
      ?MONDO_0004992 <http://purl.obolibrary.org/obo/RO_0000052> ?NCBITaxon_9606 .
      ?MONDO_0004992 rdf:type <http://purl.obolibrary.org/obo/MONDO_0004992> .
      ?TURBO_0010191 <http://transformunify.org/ontologies/TURBO_0010113> ?TURBO_0010070 .
      ?TURBO_0010191 rdf:type <http://transformunify.org/ontologies/TURBO_0010191> .
      ?TURBO_0010070 rdf:type <http://transformunify.org/ontologies/TURBO_0010070> .
      ?TURBO_0010070 <http://purl.obolibrary.org/obo/BFO_0000050> ?NCBITaxon_9606 .
      ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0010070 .
      ?TURBO_0010188 <http://purl.obolibrary.org/obo/IAO_0000219> ?TURBO_0010070 .
      ?TURBO_0010188 rdf:type <http://transformunify.org/ontologies/TURBO_0010188> .
      ?IAO_0000577 <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0010188 .
      ?IAO_0000577 rdf:type <http://purl.obolibrary.org/obo/IAO_0000577> .
      ?TURBO_0010188 <http://purl.obolibrary.org/obo/BFO_0000051> ?IAO_0000577 .
      ?TURBO_0010188 <http://purl.obolibrary.org/obo/BFO_0000051> ?TumorRegistryDenoterOfVariousTypes .
      ?TumorRegistryDenoterOfVariousTypes <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0010188 .
      ?MONDO_0004992 <http://purl.obolibrary.org/obo/IDO_0000664> ?TURBO_0010070 .
      ?TURBO_0010188 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0010188 .
      ?TURBO_0000504 <http://transformunify.org/ontologies/TURBO_0010094> ?homoSapiensSymbolStringLiteralValue .
      ?GenderIdentityDatumOfVariousTypes <http://transformunify.org/ontologies/TURBO_0010094> ?homoSapiensGenderIdentityStringLiteralValue .
      ?RaceIdentityDatumOfVariousTypes <http://transformunify.org/ontologies/TURBO_0010094> ?homoSapiensRaceIdentityStringLiteralValue .
      ?EFO_0004950 <http://transformunify.org/ontologies/TURBO_0010095> ?homoSapiensDateOfBirthStringLiteralValue .
      ?EFO_0004950 <http://transformunify.org/ontologies/TURBO_0010096> ?homoSapiensDateOfBirthDateLiteralValue .
      ?IAO_0000100 <http://purl.org/dc/elements/1.1/title> ?datasetTitleStringLiteralValue .
      ?IAO_0000577 <http://transformunify.org/ontologies/TURBO_0010094> ?tumorSymbolStringLiteralValue .
      }
      GRAPH <$processNamedGraph> {
      <processURI> turbo:TURBO_0010184 ?GenderIdentityDatumOfVariousTypes .
      <processURI> turbo:TURBO_0010184 ?RaceIdentityDatumOfVariousTypes .
      <processURI> turbo:TURBO_0010184 ?TURBO_0010092 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0000504 .
      <processURI> turbo:TURBO_0010184 ?HomoSapiensRegistryOfVariousTypes .
      <processURI> turbo:TURBO_0010184 ?NCBITaxon_9606 .
      <processURI> turbo:TURBO_0010184 ?PATO_0000047 .
      <processURI> turbo:TURBO_0010184 ?IAO_0000100 .
      <processURI> turbo:TURBO_0010184 ?EFO_0004950 .
      <processURI> turbo:TURBO_0010184 ?UBERON_0035946 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0010161 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0000522 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0010168 .
      <processURI> turbo:TURBO_0010184 ?MONDO_0004992 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0010191 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0010070 .
      <processURI> turbo:TURBO_0010184 ?TURBO_0010188 .
      <processURI> turbo:TURBO_0010184 ?IAO_0000577 .
      <processURI> turbo:TURBO_0010184 ?TumorRegistryDenoterOfVariousTypes .
      <processURI> obo:OBI_0000293 ?TURBO_0010191 .
      <processURI> obo:OBI_0000293 ?TURBO_0010161 .
      <processURI> obo:OBI_0000293 ?TURBO_0010168 .
      }
      }
      WHERE {
      VALUES ?HomoSapiensRegistryOfVariousTypes {<http://transformunify.org/ontologies/TURBO_0000505><http://transformunify.org/ontologies/TURBO_0010275><http://transformunify.org/ontologies/TURBO_0010295>}
      VALUES ?TumorRegistryDenoterOfVariousTypes {<http://transformunify.org/ontologies/TURBO_0010274>}
      GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
      ?TURBO_0010168 <http://transformunify.org/ontologies/TURBO_0010282> ?HomoSapiensRegistryOfVariousTypes .
      ?TURBO_0010168 rdf:type <http://transformunify.org/ontologies/TURBO_0010168> .
      ?TURBO_0010168 <http://purl.obolibrary.org/obo/IAO_0000219> ?TURBO_0010161 .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      ?TURBO_0010168 <http://transformunify.org/ontologies/TURBO_0010079> ?homoSapiensSymbolStringLiteralValue .
      ?TURBO_0010168 <http://transformunify.org/ontologies/TURBO_0010084> ?datasetTitleStringLiteralValue .
      OPTIONAL {
       ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010089> ?ShortcutGenderIdentityDatumOfVariousTypes .
       }
      OPTIONAL {
       ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010090> ?ShortcutRaceIdentityDatumOfVariousTypes .
       }
      OPTIONAL {
       ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010085> ?homoSapiensDateOfBirthStringLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010086> ?homoSapiensDateOfBirthDateLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010098> ?homoSapiensGenderIdentityStringLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010100> ?homoSapiensRaceIdentityStringLiteralValue .
       }
      OPTIONAL {
      ?TURBO_0010191 <http://transformunify.org/ontologies/TURBO_0010277> ?TumorRegistryDenoterOfVariousTypes .
      ?TURBO_0010191 rdf:type <http://transformunify.org/ontologies/TURBO_0010191> .
      ?TURBO_0010191 <http://purl.obolibrary.org/obo/IAO_0000219> ?TURBO_0010161 .
      ?TURBO_0010191 <http://transformunify.org/ontologies/TURBO_0010194> ?tumorSymbolStringLiteralValue .
      }
      }
      BIND(IF (BOUND(?homoSapiensGenderIdentityStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?PATO_0000047","localUUID", str(?TURBO_0010161))))), ?unbound) AS ?PATO_0000047)
      BIND(IF (BOUND(?homoSapiensRaceIdentityStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?RaceIdentityDatumOfVariousTypes","localUUID", str(?TURBO_0010161))))), ?unbound) AS ?RaceIdentityDatumOfVariousTypes)
      BIND(IF(BOUND(?ShortcutGenderIdentityDatumOfVariousTypes), ?ShortcutGenderIdentityDatumOfVariousTypes, obo:OMRSE_00000133) AS ?GenderIdentityDatumType)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?IAO_0000577","localUUID", str(?TURBO_0010191))))) AS ?IAO_0000577)
      BIND(IF (BOUND(?homoSapiensGenderIdentityStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?GenderIdentityDatumOfVariousTypes","localUUID", str(?TURBO_0010161))))), ?unbound) AS ?GenderIdentityDatumOfVariousTypes)
      BIND(IF (BOUND(?homoSapiensDateOfBirthStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?UBERON_0035946","localUUID", str(?TURBO_0010161))))), ?unbound) AS ?UBERON_0035946)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0010188","localUUID", str(?TURBO_0010191))))) AS ?TURBO_0010188)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000504","localUUID", str(?TURBO_0010168))))) AS ?TURBO_0000504)
      BIND(IF (BOUND(?homoSapiensDateOfBirthStringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?EFO_0004950","localUUID", str(?TURBO_0010161))))), ?unbound) AS ?EFO_0004950)
      BIND(IF(BOUND(?ShortcutRaceIdentityDatumOfVariousTypes), ?ShortcutRaceIdentityDatumOfVariousTypes, obo:OMRSE_00000098) AS ?RaceIdentityDatumType)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0010070","localUUID", str(?TURBO_0010191))))) AS ?TURBO_0010070)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?MONDO_0004992","localUUID", str(?TURBO_0010191))))) AS ?MONDO_0004992)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0000522","localUUID")))) AS ?TURBO_0000522)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?NCBITaxon_9606","localUUID", str(?TURBO_0010161))))) AS ?NCBITaxon_9606)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?TURBO_0010092","localUUID", str(?TURBO_0010168))))) AS ?TURBO_0010092)
      BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT(str(?datasetTitleStringLiteralValue),"localUUID")))) AS ?IAO_0000100)
      }
      """
    
    before
    {
        graphDBMaterials = ConnectToGraphDB.initializeGraphUpdateData()
        testCxn = graphDBMaterials.getTestConnection()
        gmCxn = graphDBMaterials.getGmConnection()
        helper.deleteAllTriplesInDatabase(testCxn)
        
        RunDrivetrainProcess.setGraphModelConnection(gmCxn)
        RunDrivetrainProcess.setOutputRepositoryConnection(testCxn)
    }
    after
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    test("generated query matched expected query")
    {
        var expectedQueryListBuffer = new ArrayBuffer[String]
        val processQueryMap = RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
        val query = processQueryMap("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
        val queryText = query.getQuery().replaceAll(" ", "").split("\\n")
        val process = query.process
        for (a <- expectedQuery.replaceAll(" ","").split("\\n"))
        {
            val replacement = a.substring(0,a.length()-1).replace("localUUID", RunDrivetrainProcess.localUUID).replace("processURI", process)
            expectedQueryListBuffer += replacement
        }
        var expectedQueryList = expectedQueryListBuffer.toArray
        helper.checkStringArraysForEquivalency(queryText, expectedQueryList)("equivalent").asInstanceOf[String] should be ("true")
    }
    
    test("participant with all fields")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              turbo:TURBO_0010089 <http://purl.obolibrary.org/obo/OMRSE_00000138> ;
              turbo:TURBO_0010086 "1969-05-04"^^xsd:date ;
              a turbo:TURBO_0010161 ;
              turbo:TURBO_0010085 "04/May/1969" ;
              turbo:TURBO_0010098 "F" ;
              turbo:TURBO_0000609 'inpatient' ;
              
              # adding race data 7/31/18
              turbo:TURBO_0010090 <http://purl.obolibrary.org/obo/OMRSE_00000181> ;
              turbo:TURBO_0010100 'asian' .
              
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 ;
              turbo:TURBO_0010079 '4' ;
              turbo:TURBO_0010084 "part_expand" .
              
              pmbb:scTumorCrid1 a turbo:TURBO_0010191 ;
              obo:IAO_0000219 pmbb:part1 ;
              turbo:TURBO_0010194 '123' ;
              turbo:TURBO_0010277 <http://transformunify.org/ontologies/TURBO_0010274> .
              
              pmbb:scTumorCrid2 a turbo:TURBO_0010191 ;
              obo:IAO_0000219 pmbb:part1 ;
              turbo:TURBO_0010194 '456' ;
              turbo:TURBO_0010277 <http://transformunify.org/ontologies/TURBO_0010274> .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
        
        val extraFields: String = s"""
          ASK {GRAPH <$expandedNamedGraph> {
        		
        		?dataset a obo:IAO_0000100 .
        		?part rdf:type obo:NCBITaxon_9606 .

        		?gid turbo:TURBO_0010094 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?gid rdf:type obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?part turbo:TURBO_0000303 ?birth .
        		?birth rdf:type obo:UBERON_0035946 .
        		?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .
        		
        		?patientCrid obo:IAO_0000219 ?part .
        		?patientCrid a turbo:TURBO_0010092 .
        		?patientCrid obo:BFO_0000051 turbo:TURBO_0000505 .
        		turbo:TURBO_0000505 obo:BFO_0000050 ?patientCrid .
        		# ?patientRegDen turbo:TURBO_0010094 'inpatient' .
        		
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0010094 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .
        		
        		?disease1 a obo:MONDO_0004992 ;
            obo:RO_0000052 ?part .
            ?disease2 a obo:MONDO_0004992 ;
            obo:RO_0000052 ?part .
            ?disease1 obo:IDO_0000664 ?tumor1 .
            ?disease2 obo:IDO_0000664 ?tumor2 .
            
           ?tumor1 a turbo:TURBO_0010070  ;
            obo:BFO_0000050 ?part .
            ?part obo:BFO_0000051 ?tumor1 .
            
            ?tumor2 a turbo:TURBO_0010070  ;
            obo:BFO_0000050 ?part .
            ?part obo:BFO_0000051 ?tumor2 .
            
            ?tumorCrid1 a ontologies:TURBO_0010188 ;
            obo:IAO_0000219 ?tumor1 .
            
            ?tumorCrid2 a ontologies:TURBO_0010188 ;
            obo:IAO_0000219 ?tumor2 .
            
           ?tumorCridSymb1 a obo:IAO_0000577 ;
            obo:BFO_0000050 ?tumorCrid1 ;
            ontologies:TURBO_0010094 "123" .
           ontologies:TURBO_0010274 obo:BFO_0000050 ?tumorCrid1 .
           ?tumorCrid1 obo:BFO_0000051 ontologies:TURBO_0010274 .
           ?tumorCrid1 obo:BFO_0000051 ?tumorCridSymb1 .
            
           ?tumorCridSymb2 a obo:IAO_0000577 ;
            obo:BFO_0000050 ?tumorCrid2 ;
            ontologies:TURBO_0010094 "456" .
           ontologies:TURBO_0010274 obo:BFO_0000050 ?tumorCrid2 .
           ?tumorCrid2 obo:BFO_0000051 ontologies:TURBO_0010274 .
           ?tumorCrid2 obo:BFO_0000051 ?tumorCridSymb2 .

           ?tumorCrid1 obo:BFO_0000050 ?dataset .
           ?tumorCrid2 obo:BFO_0000050 ?dataset .

           ?dataset obo:BFO_0000051 ?tumorCrid1 .
           ?dataset obo:BFO_0000051 ?tumorCrid2 .
           
          }
           filter (?disease1 != ?disease2)
           filter (?tumor1 != ?tumor2)
           filter (?tumorCrid1 != ?tumorCrid2)
           filter (?tumorCridSymbol1 != ?tumorCridSymbol2)
          }
          """
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(testCxn, extraFields).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val expectedPredicates = Array (
            
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0000052",
            "http://purl.obolibrary.org/obo/RO_0000052", "http://purl.obolibrary.org/obo/BFO_0000050", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050", 
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/IDO_0000664",
            "http://purl.obolibrary.org/obo/IDO_0000664"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (expectedPredicates.size)
        
        val processInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:crid1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:scTumorCrid1 ;
                  obo:OBI_0000293 pmbb:scTumorCrid2 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000138 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000181 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  ontologies:TURBO_0010184 ?MONDO_0004992_1 ;
                  ontologies:TURBO_0010184 ?MONDO_0004992_2 ;
                  ontologies:TURBO_0010184 ?UBERON_0000465_1 ;
                  ontologies:TURBO_0010184 ?UBERON_0000465_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0010188_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0010188_2 ;
                  ontologies:TURBO_0010184 ?IAO_0000577_1 ;
                  ontologies:TURBO_0010184 ?IAO_0000577_2 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph <$expandedNamedGraph>
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?TURBO_0000504 a turbo:TURBO_0000504 .
                ?TURBO_0010092 a turbo:TURBO_0010092 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000138 a obo:OMRSE_00000138 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000181 a obo:OMRSE_00000181 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                ?instantiation a turbo:TURBO_0000522 .
                ?MONDO_0004992_1 a obo:MONDO_0004992 .
                ?MONDO_0004992_2 a obo:MONDO_0004992 .
                ?UBERON_0000465_1 a turbo:TURBO_0010070 .
                ?UBERON_0000465_2 a turbo:TURBO_0010070 .
                ?TURBO_0010188_1 a turbo:TURBO_0010188 .
                ?TURBO_0010188_2 a turbo:TURBO_0010188 .
                ?IAO_0000577_1 a obo:IAO_0000577 .
                ?IAO_0000577_2 a obo:IAO_0000577 .
            }
            
            filter (?MONDO_0004992_1 != ?MONDO_0004992_2)
            filter (?UBERON_0000465_1 != ?UBERON_0000465_2)
            filter (?TURBO_0010188_1 != ?TURBO_0010188_2)
            filter (?IAO_0000577_1 != ?IAO_0000577_2)
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("participant with minimum required for expansion")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1> a turbo:TURBO_0010161 .
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010079 "4" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 .
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")        
        
        //compare expected predicates to received predicates
        //only checking predicates because many of the subjects/objects in expanded triples are unique UUIDs
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (expectedPredicates.size) 
        
        val processInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:crid1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph <$expandedNamedGraph>
            {
                ?TURBO_0000504 a turbo:TURBO_0000504 .
                ?TURBO_0010092 a turbo:TURBO_0010092 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("participant with text but not xsd values")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
              <http://www.itmat.upenn.edu/biobank/part1>
              
              a turbo:TURBO_0010161 ;
              turbo:TURBO_0010085 "04/May/1969" ;
              turbo:TURBO_0010100 'asian' ;
              turbo:TURBO_0010098 "F" .
             
              pmbb:crid1 obo:IAO_0000219 pmbb:part1 ;
              a turbo:TURBO_0010168 ;
              turbo:TURBO_0000609 "inpatient" ;
              turbo:TURBO_0010282 turbo:TURBO_0000505 ;
              turbo:TURBO_0010084 "part_expand" ;
              turbo:TURBO_0010079 "4" .
              
          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
        
        val dateNoXsd: String = s"""
          ASK {GRAPH <$expandedNamedGraph> {
        		?part rdf:type obo:NCBITaxon_9606 .
        		?part turbo:TURBO_0000303 ?birth .
        		?birth rdf:type obo:UBERON_0035946 .
        		?dob rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob obo:IAO_0000136 ?birth .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .
        		?dataset a obo:IAO_0000100 .
        		# ?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
          }}
          """
        
        val gidNoXsd: String = s"""
          ASK {GRAPH <$expandedNamedGraph> {
        		?part rdf:type obo:NCBITaxon_9606 .
        		?gid obo:IAO_0000136 ?part .
        		?gid a obo:OMRSE_00000133 .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?dataset a obo:IAO_0000100 .
          }}"""
        
        update.querySparqlBoolean(testCxn, instantiationAndDataset).get should be (true)
        update.querySparqlBoolean(testCxn, minimumPartRequirements).get should be (true)
        update.querySparqlBoolean(testCxn, dateNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, gidNoXsd).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val expectedPredicates = Array (
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010095", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://transformunify.org/ontologies/TURBO_0010113"
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (expectedPredicates.size)
        
        val processInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:crid1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504 ;
                  ontologies:TURBO_0010184 turbo:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000133 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000098 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph <$expandedNamedGraph>
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?TURBO_0000504 a turbo:TURBO_0000504 .
                ?TURBO_0010092 a turbo:TURBO_0010092 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000133 a obo:OMRSE_00000133 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000098 a obo:OMRSE_00000098 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("expand homoSapiens with multiple identifiers - single dataset")
    {
        val insert: String = """
          INSERT DATA {GRAPH pmbb:Shortcuts_homoSapiensShortcuts {
            pmbb:part1
            turbo:TURBO_0010089 <http://purl.obolibrary.org/obo/OMRSE_00000138> ;
            turbo:TURBO_0010086 "1969-05-04"^^xsd:date ;
            a turbo:TURBO_0010161 ;
            turbo:TURBO_0010085 "04/May/1969" ;
            turbo:TURBO_0010098 "F" ;
            turbo:TURBO_0010090 <http://purl.obolibrary.org/obo/OMRSE_00000181> ;
            turbo:TURBO_0010100 'asian' .
            
            pmbb:shortcutCrid1 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid1 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid2 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid3 a turbo:TURBO_0010168 .
            
            pmbb:shortcutCrid1 turbo:TURBO_0010084 'dataset1' .
            pmbb:shortcutCrid2 turbo:TURBO_0010084 'dataset1' .
            pmbb:shortcutCrid3 turbo:TURBO_0010084 'dataset1' .
            
            pmbb:shortcutCrid1 turbo:TURBO_0010079 'jerry' .
            pmbb:shortcutCrid2 turbo:TURBO_0010079 'kramer' .
            pmbb:shortcutCrid3 turbo:TURBO_0010079 'elaine' .
            
            pmbb:shortcutCrid1 turbo:TURBO_0010282 <http://transformunify.org/ontologies/TURBO_0000505> .
            pmbb:shortcutCrid2 turbo:TURBO_0010282 <http://transformunify.org/ontologies/TURBO_0010275> .
            pmbb:shortcutCrid3 turbo:TURBO_0010282 <http://transformunify.org/ontologies/TURBO_0000505> .

          }}"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
    
        val output: String = s"""
          ASK {GRAPH <$expandedNamedGraph> {
        	
        		?part a obo:NCBITaxon_9606 .
        		?instantiation a turbo:TURBO_0000522 .
        		?instantiation obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "dataset1" .

        		?part turbo:TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .

        		?gid turbo:TURBO_0010094 "F" .
        		?gid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?dob .

        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0010094 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0010092 .
        		?patientCrid1 obo:BFO_0000051 turbo:TURBO_0000505 .
        		turbo:TURBO_0000505 obo:BFO_0000050 ?patientCrid1 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0010094 "jerry"^^xsd:string .
            ?partSymb1 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0010092 .
        		?patientCrid2 obo:BFO_0000051 turbo:TURBO_0010275 .
        		turbo:TURBO_0010275 obo:BFO_0000050 ?patientCrid2 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0010094 "kramer"^^xsd:string .
            ?partSymb2 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0010092 .
        		?patientCrid3 obo:BFO_0000051 turbo:TURBO_0000505 .
        		turbo:TURBO_0000505 obo:BFO_0000050 ?patientCrid3 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0010094 "elaine"^^xsd:string .
            ?partSymb3 obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?partSymb3 .
        		
          }}
          """
        
        val oneConsenter = """
          select (count (?homosapiens) as ?homosapienscount) where
          {
              ?homosapiens a obo:NCBITaxon_9606 .
          }
          """
        
        val threeIdentifiers = """
          select (count (?crid) as ?cridcount) where
          {
              ?crid a turbo:TURBO_0010092 .
          }
          """
        
        val threeSymbols = """
          select (count (?symbol) as ?symbolcount) where
          {
              ?symbol a turbo:TURBO_0000504 .
          }
          """
        
        update.querySparqlAndUnpackTuple(testCxn, oneConsenter, "homosapienscount")(0).split("\"")(1) should be ("1")
        update.querySparqlAndUnpackTuple(testCxn, threeIdentifiers, "cridcount")(0).split("\"")(1) should be ("3")
        update.querySparqlAndUnpackTuple(testCxn, threeSymbols, "symbolcount")(0).split("\"")(1) should be ("3")
        
        update.querySparqlBoolean(testCxn, output).get should be (true)
        update.querySparqlBoolean(testCxn, processMeta).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        
        val expectedPredicates = Array (
            
            "http://purl.obolibrary.org/obo/OBI_0000293", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.org/dc/elements/1.1/title",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/RO_0000086", "http://transformunify.org/ontologies/TURBO_0000303",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/IAO_0000136", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
            
        )
        
        helper.checkStringArraysForEquivalency(expectedPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")  
        
        result.size should be (expectedPredicates.size)
        
        val processInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:shortcutCrid1 ;
                  obo:OBI_0000293 pmbb:shortcutCrid2 ;
                  obo:OBI_0000293 pmbb:shortcutCrid3 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000505 ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0010275 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000504_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_3 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092_3 ;
                  
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000138 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000181 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph <$expandedNamedGraph>
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000138 a obo:OMRSE_00000138 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000181 a obo:OMRSE_00000181 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                
                ?TURBO_0000504_1 a turbo:TURBO_0000504 .
                ?TURBO_0010092_1 a turbo:TURBO_0010092 .
                ?TURBO_0000504_2 a turbo:TURBO_0000504 .
                ?TURBO_0010092_2 a turbo:TURBO_0010092 .
                ?TURBO_0000504_3 a turbo:TURBO_0000504 .
                ?TURBO_0010092_3 a turbo:TURBO_0010092 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
    
    test("expand homoSapiens with multiple identifiers - multiple datasets")
    {
        val insert: String = """
          INSERT DATA {
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts1 {
            pmbb:part1
            turbo:TURBO_0010089 <http://purl.obolibrary.org/obo/OMRSE_00000138> ;
            a turbo:TURBO_0010161 ;
            turbo:TURBO_0010098 "F" .
            pmbb:shortcutCrid1 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid1 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid1 turbo:TURBO_0010084 'dataset1' .
            pmbb:shortcutCrid1 turbo:TURBO_0010079 'jerry' .
            pmbb:shortcutCrid1 turbo:TURBO_0010282 <http://transformunify.org/ontologies/TURBO_0010275> .  
          }
          
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts2 {
            pmbb:part1 a turbo:TURBO_0010161 ;
            turbo:TURBO_0010086 "1969-05-04"^^xsd:date ;
            turbo:TURBO_0010085 "04/May/1969" .
            pmbb:shortcutCrid2 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid2 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid2 turbo:TURBO_0010084 'dataset2' .
            pmbb:shortcutCrid2 turbo:TURBO_0010079 'kramer' .
            pmbb:shortcutCrid2 turbo:TURBO_0010282 <http://transformunify.org/ontologies/TURBO_0000505> .
          }
          
          GRAPH pmbb:Shortcuts_homoSapiensShortcuts3 {
            pmbb:part1 a turbo:TURBO_0010161 ;
            turbo:TURBO_0010090 <http://purl.obolibrary.org/obo/OMRSE_00000181> ;
            turbo:TURBO_0010100 'asian' .
            pmbb:shortcutCrid3 obo:IAO_0000219 pmbb:part1 .
            pmbb:shortcutCrid3 a turbo:TURBO_0010168 .
            pmbb:shortcutCrid3 turbo:TURBO_0010084 'dataset3' .
            pmbb:shortcutCrid3 turbo:TURBO_0010079 'elaine' .
            pmbb:shortcutCrid3 turbo:TURBO_0010282 <http://transformunify.org/ontologies/TURBO_0000505> .
          }
          
          }"""
        update.updateSparql(testCxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HomoSapiensExpansionProcess")
        
          val output: String = s"""
          ASK {GRAPH <$expandedNamedGraph> {
        	
        		?part a obo:NCBITaxon_9606 .
        		?instantiation a turbo:TURBO_0000522 .
        		?instantiation obo:OBI_0000293 ?dataset1 .
        		?dataset1 a obo:IAO_0000100 .
        		?dataset1 dc11:title "dataset1" .
        		?instantiation obo:OBI_0000293 ?dataset2 .
        		?dataset2 a obo:IAO_0000100 .
        		?dataset2 dc11:title "dataset2" .
        		?instantiation obo:OBI_0000293 ?dataset3 .
        		?dataset3 a obo:IAO_0000100 .
        		?dataset3 dc11:title "dataset3" .
        		
        		?part turbo:TURBO_0000303 ?birth .
        		?birth a obo:UBERON_0035946 .
        		?part obo:RO_0000086 ?biosex .
        		?biosex a obo:PATO_0000047 .

        		?gid turbo:TURBO_0010094 "F" .
        		?gid obo:BFO_0000050 ?dataset1 .
        		?dataset1 obo:BFO_0000051 ?gid .
        		?gid a obo:OMRSE_00000138 .
        		?gid obo:IAO_0000136 ?part .
        		
        		?dob a <http://www.ebi.ac.uk/efo/EFO_0004950> .
        		?dob turbo:TURBO_0010095 "04/May/1969" .
        		?dob turbo:TURBO_0010096 "1969-05-04"^^xsd:date .
        		?dob obo:IAO_0000136 ?birth .
        		?dob obo:BFO_0000050 ?dataset2 .
        		?dataset2 obo:BFO_0000051 ?dob .
        		
        		?rid obo:IAO_0000136 ?part .
        		?rid turbo:TURBO_0010094 "asian"^^xsd:string .
        		?rid a obo:OMRSE_00000181 .
        		?rid obo:BFO_0000050 ?dataset3 .
        		?dataset3 obo:BFO_0000051 ?rid .
        		
        		?patientCrid1 obo:IAO_0000219 ?part .
        		?patientCrid1 a turbo:TURBO_0010092 .
        		?patientCrid1 obo:BFO_0000051 turbo:TURBO_0010275 .
        		turbo:TURBO_0010275 obo:BFO_0000050 ?patientCrid1 .
        		?patientCrid1 obo:BFO_0000051 ?partSymbol1 .
        		?partSymbol1 obo:BFO_0000050 ?patientCrid1 .
            ?partSymbol1 a turbo:TURBO_0000504 .
            ?partSymbol1 turbo:TURBO_0010094 "jerry"^^xsd:string .
            ?partSymb1 obo:BFO_0000050 ?dataset1 .
            ?dataset1 obo:BFO_0000051 ?partSymb1 .
            
            ?patientCrid2 obo:IAO_0000219 ?part .
        		?patientCrid2 a turbo:TURBO_0010092 .
        		?patientCrid2 obo:BFO_0000051 turbo:TURBO_0000505 .
        		turbo:TURBO_0000505 obo:BFO_0000050 ?patientCrid2 .
        		?patientCrid2 obo:BFO_0000051 ?partSymbol2 .
        		?partSymbol2 obo:BFO_0000050 ?patientCrid2 .
            ?partSymbol2 a turbo:TURBO_0000504 .
            ?partSymbol2 turbo:TURBO_0010094 "kramer"^^xsd:string .
            ?partSymb2 obo:BFO_0000050 ?dataset2 .
            ?dataset2 obo:BFO_0000051 ?partSymb2 .
            
            ?patientCrid3 obo:IAO_0000219 ?part .
        		?patientCrid3 a turbo:TURBO_0010092 .
        		?patientCrid3 obo:BFO_0000051 turbo:TURBO_0000505 .
        		turbo:TURBO_0000505 obo:BFO_0000050 ?patientCrid3 .
        		?patientCrid3 obo:BFO_0000051 ?partSymbol3 .
        		?partSymbol3 obo:BFO_0000050 ?patientCrid3 .
            ?partSymbol3 a turbo:TURBO_0000504 .
            ?partSymbol3 turbo:TURBO_0010094 "elaine"^^xsd:string .
            ?partSymb3 obo:BFO_0000050 ?dataset3 .
            ?dataset3 obo:BFO_0000051 ?partSymb3 .
            
            filter (?dataset1 != ?dataset2)
            filter (?dataset2 != ?dataset3)
            filter (?dataset3 != ?dataset1)
        		
          }}
          """
        
        update.querySparqlBoolean(testCxn, output).get should be (true)
        val count: String = s"SELECT * WHERE {GRAPH <$expandedNamedGraph> {?s ?p ?o .}}"
        val result = update.querySparqlAndUnpackTuple(testCxn, count, "p")
        result.size should be (69)
        
        val processMetaMultipleDatasets: String = s"""
        ASK 
          { 
            Graph <$processNamedGraph>
            {
                ?processBoundary obo:RO_0002223 ?updateProcess .
                ?processBoundary a obo:BFO_0000035 .
                ?timeMeasDatum obo:IAO_0000136 ?processBoundary .
                ?timeMeasDatum a obo:IAO_0000416 .
                ?timeMeasDatum turbo:TURBO_0010094 ?someDateTime .
                
                ?updateProcess
                    a turbo:TURBO_0010347 ;
                    turbo:TURBO_0010107 ?someRuntime ;
                    turbo:TURBO_0010108 ?someNumberOfTriples;
                    turbo:TURBO_0010186 <$expandedNamedGraph> ;
                    turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts1 ;
                    turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts2 ;
                    turbo:TURBO_0010187 pmbb:Shortcuts_homoSapiensShortcuts3 ;
                    obo:BFO_0000055 ?updatePlan .
                
                ?updatePlan a turbo:TURBO_0010373 ;
                    obo:RO_0000059 pmbb:HomoSapiensExpansionProcess .
                
                pmbb:HomoSapiensExpansionProcess a turbo:TURBO_0010354 ;
                    turbo:TURBO_0010106 ?query .
            }
          }
        """
        update.querySparqlBoolean(testCxn, processMetaMultipleDatasets).get should be (true)
        
        val processInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <$processNamedGraph>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:shortcutCrid1 ;
                  obo:OBI_0000293 pmbb:shortcutCrid2 ;
                  obo:OBI_0000293 pmbb:shortcutCrid3 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0010275 ;
                  ontologies:TURBO_0010184 ontologies:TURBO_0000505 ;
                  
                  ontologies:TURBO_0010184 ?TURBO_0000504_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092_1 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092_2 ;
                  ontologies:TURBO_0010184 ?TURBO_0000504_3 ;
                  ontologies:TURBO_0010184 ?TURBO_0010092_3 ;
                  
                  ontologies:TURBO_0010184 ?UBERON_0035946 ;
                  ontologies:TURBO_0010184 ?PATO_0000047 ;
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000138 ;
                  ontologies:TURBO_0010184 ?EFO_0004950 ;
                  ontologies:TURBO_0010184 ?OMRSE_00000181 ;
                  ontologies:TURBO_0010184 ?NCBITaxon_9606 ;
                  
                  ontologies:TURBO_0010184 pmbb:part1 ;
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph <$expandedNamedGraph>
            {
                ?UBERON_0035946 a obo:UBERON_0035946 .
                ?PATO_0000047 a obo:PATO_0000047 .
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OMRSE_00000138 a obo:OMRSE_00000138 .
                ?EFO_0004950 a efo:EFO_0004950 .
                ?OMRSE_00000181 a obo:OMRSE_00000181 .
                ?NCBITaxon_9606 a obo:NCBITaxon_9606 .
                
                ?TURBO_0000504_1 a turbo:TURBO_0000504 .
                ?TURBO_0010092_1 a turbo:TURBO_0010092 .
                ?TURBO_0000504_2 a turbo:TURBO_0000504 .
                ?TURBO_0010092_2 a turbo:TURBO_0010092 .
                ?TURBO_0000504_3 a turbo:TURBO_0000504 .
                ?TURBO_0010092_3 a turbo:TURBO_0010092 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        update.querySparqlBoolean(testCxn, processInputsOutputs).get should be (true)
    }
}