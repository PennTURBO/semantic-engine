package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import java.util.UUID
import scala.collection.mutable.ArrayBuffer
import org.slf4j.LoggerFactory

class HealthcareEncounterExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with BeforeAndAfterAll with Matchers
{
    val logger = LoggerFactory.getLogger(getClass)
    val clearTestingRepositoryAfterRun: Boolean = false
    
    var graphDBMaterials: TurboGraphConnection = null
    
    RunDrivetrainProcess.setGlobalUUID(UUID.randomUUID().toString.replaceAll("-", ""))
    
    val instantiationAndDataset: String = s"""
      ASK { GRAPH <${Globals.expandedNamedGraph}> {
            ?instantiation <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
        		?instantiation obo:OBI_0000293 ?dataset .
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title "enc_expand.csv"^^xsd:string .
       }}"""
    
    val healthcareEncounterMinimum: String = s"""
      ASK { GRAPH <${Globals.expandedNamedGraph}> {
            ?encounter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OGMS_0000097 .
        		?encounterCrid <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000508 .
        		?encounterCrid obo:IAO_0000219 ?encounter .
        		
        		?encounterCrid obo:BFO_0000051 ?encounterSymb .
        		?encounterCrid obo:BFO_0000051 turbo:TURBO_0000510 .
        		?encounterSymb obo:BFO_0000050 ?encounterCrid .
        		turbo:TURBO_0000510 obo:BFO_0000050 ?encounterCrid .
        		
        		?encounterSymb a turbo:TURBO_0000509 .
        	
       }}
      """
    
    val healthcareSymbolAndRegistry: String = s"""
      ASK {GRAPH <${Globals.expandedNamedGraph}> {
          ?encounter a obo:OGMS_0000097 .
          ?encounterCrid a turbo:TURBO_0000508 .
          ?encounterCrid obo:IAO_0000219 ?encounter .
          ?encounterCrid obo:BFO_0000051 ?encsymb .
      		?encounterCrid obo:BFO_0000051 turbo:TURBO_0000510 .
      		turbo:TURBO_0000510 obo:BFO_0000050 ?encounterCrid .
      		?encsymb a turbo:TURBO_0000509 .
      		?encsymb turbo:TURBO_0010094 '20' .
      		?encSymb obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?encSymb .
      		?dataset a obo:IAO_0000100 .
      }}
      """
   
    val healthcareDiagnosis: String = s"""
          ASK { GRAPH <${Globals.expandedNamedGraph}> {
          
                ?dataset a obo:IAO_0000100 .
                ?encounter a obo:OGMS_0000097 .
            		?encounter obo:OBI_0000299 ?diagnosis .
            		?diagnosis a obo:OGMS_0000073 .
            		?diagnosis turbo:TURBO_0010094 "401.9" .
            		?diagnosis turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
            		?diagnosis turbo:TURBO_0006515 "ICD-9" .
            		?diagnosis obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
            		?diagnosis obo:BFO_0000050 ?dataset .
            		?dataset obo:BFO_0000051 ?diagnosis .
            		
            		?diagnosis turbo:TURBO_0010013 "true"^^xsd:Boolean .
            		?diagnosis turbo:TURBO_0010014 "1"^^xsd:Integer .
        	}}
          """
    
    val healthcareMedications: String = s"""
      ask {graph <${Globals.expandedNamedGraph}> {
          ?dataset a obo:IAO_0000100 .
          ?encounter a obo:OGMS_0000097 .
          ?encounter obo:OBI_0000299 ?drugPrescript .
      		?drugPrescript a obo:PDRO_0000001 .
      		?drugPrescript turbo:TURBO_0010094 "holistic soil from the ganges" .
      		?medCrid obo:IAO_0000219 ?drugPrescript .
      		?medCrid a turbo:TURBO_0000561 .
      		?medCrid obo:BFO_0000051 ?medCridSymbol .
      		?medCridSymbol a turbo:TURBO_0000562 .
      		?medCridSymbol turbo:TURBO_0010094 "3" .
      		?drugPrescript obo:IAO_0000142 turbo:someDrug .
      		
      		?drugPrescript obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?drugPrescript .
      		?medCridSymbol obo:BFO_0000050 ?dataset .
      		?dataset obo:BFO_0000051 ?medCridSymbol .
      		}}
      """
    
    val healthcareMeasurements: String = s"""
          ASK { GRAPH <${Globals.expandedNamedGraph}> {
          
                ?encounter obo:OBI_0000299 ?BMI .
                ?encounter turbo:TURBO_0010139 ?heightDatum .
                ?encounter turbo:TURBO_0010139 ?weightDatum .
                ?encounter obo:BFO_0000051 ?bpMeasProcess .
                ?bpMeasProcess obo:BFO_0000050 ?encounter .
                
                ?encounter a obo:OGMS_0000097 .
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
            		?BMI obo:BFO_0000050 ?dataset ;
            		  turbo:TURBO_0010094 "26.2577659792"^^xsd:float .
            		?dataset a obo:IAO_0000100 .
            		
          	    ?heightDatum rdf:type turbo:TURBO_0010138 ;
          	                 obo:IAO_0000039 obo:UO_0000015 ;
          	                 turbo:TURBO_0010094 "177.8"^^xsd:float ;
          	                 obo:BFO_0000050 ?dataset .
          	    
          	    ?weightDatum rdf:type obo:OBI_0001929 ;
          	                 obo:BFO_0000050 ?dataset ;
          	                 obo:IAO_0000039 obo:UO_0000009 ;
          	                 turbo:TURBO_0010094 "83.0082554658"^^xsd:float  .
          	                 
          	    ?bpMeasProcess a obo:VSO_0000006 ;
          	        obo:OBI_0000299 ?systolicBpMeasDatum ;
          	        obo:OBI_0000299 ?diastolicBpMeasDatum .
          	    
          	    ?systolicBpMeasDatum a obo:HTN_00000001 ;
          	        obo:OBI_0001938 ?systolicBpValSpec .
          	    ?diastolicBpMeasDatum a obo:HTN_00000000 ;
          	        obo:OBI_0001938 ?diastolicBpValSpec .
          	        
          	    ?systolicBpValSpec a turbo:TURBO_0010149 ;
          	        turbo:TURBO_0010094 "120"^^xsd:Float ;
          	        obo:IAO_0000039 obo:UO_0000272 .
          	    ?diastolicBpValSpec a turbo:TURBO_0010150 ;
          	        turbo:TURBO_0010094 "80"^^xsd:Float ;
          	        obo:IAO_0000039 obo:UO_0000272 .
          	                   
          	    ?dataset obo:BFO_0000051 ?BMI .
          		  ?dataset obo:BFO_0000051 ?weightDatum .
          		  ?dataset obo:BFO_0000051 ?heightDatum .
          		  ?dataset obo:BFO_0000051 ?systolicBpMeasDatum .
          		  ?dataset obo:BFO_0000051 ?diastolicBpMeasDatum .
          		  ?systolicBpMeasDatum obo:BFO_0000050 ?dataset .
          		  ?diastolicBpMeasDatum obo:BFO_0000050 ?dataset .
        	}}
        	"""
    
    val healthcareEncounterDate: String = s"""
          ASK { GRAPH <${Globals.expandedNamedGraph}> {
            ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encDate turbo:TURBO_0010095 "15/Jan/2017" .
        		?encDate turbo:TURBO_0010096 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?dataset obo:BFO_0000051 ?encDate .
        		?encDate obo:BFO_0000050 ?dataset .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?dataset a obo:IAO_0000100 .
        		?encounter a obo:OGMS_0000097 .
           }}"""
    
    val processMeta = Utilities.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess",
                                                  Array("http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts"))
    
    val anyProcess: String = s"""
      ASK
      {
          Graph <${Globals.processNamedGraph}>
          {
              ?s ?p ?o .
          }
      }
      """
    
    val healthcareQuery: String = s"""
      INSERT {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?TURBO_0000508 <http://purl.obolibrary.org/obo/BFO_0000051> ?HealthcareEncounterRegistryOfVariousTypes .
      ?TURBO_0000508 rdf:type <http://transformunify.org/ontologies/TURBO_0000508> .
      ?TURBO_0010138 <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000015> .
      ?TURBO_0010138 rdf:type <http://transformunify.org/ontologies/TURBO_0010138> .
      ?OBI_0001929 <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000009> .
      ?OBI_0001929 rdf:type <http://purl.obolibrary.org/obo/OBI_0001929> .
      ?TURBO_0010149 <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000272> .
      ?TURBO_0010149 rdf:type <http://transformunify.org/ontologies/TURBO_0010149> .
      ?TURBO_0010150 <http://purl.obolibrary.org/obo/IAO_0000039> <http://purl.obolibrary.org/obo/UO_0000272> .
      ?TURBO_0010150 rdf:type <http://transformunify.org/ontologies/TURBO_0010150> .
      ?TURBO_0000508 <http://purl.obolibrary.org/obo/IAO_0000219> ?OGMS_0000097 .
      ?OGMS_0000097 rdf:type <http://purl.obolibrary.org/obo/OGMS_0000097> .
      ?EFO_0004340 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?EFO_0004340 rdf:type <http://www.ebi.ac.uk/efo/EFO_0004340> .
      ?IAO_0000100 rdf:type <http://purl.obolibrary.org/obo/IAO_0000100> .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?EFO_0004340 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000512 .
      ?TURBO_0000512 rdf:type <http://transformunify.org/ontologies/TURBO_0000512> .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000509 .
      ?TURBO_0000509 rdf:type <http://transformunify.org/ontologies/TURBO_0000509> .
      ?TURBO_0000512 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0000512 <http://purl.obolibrary.org/obo/IAO_0000136> ?TURBO_0000511 .
      ?TURBO_0000511 rdf:type <http://transformunify.org/ontologies/TURBO_0000511> .
      ?TURBO_0000508 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000509 .
      ?TURBO_0000511 <http://purl.obolibrary.org/obo/RO_0002223> ?OGMS_0000097 .
      ?TURBO_0000509 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0000509 <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0000508 .
      ?OGMS_0000097 <http://purl.obolibrary.org/obo/OBI_0000299> ?EFO_0004340 .
      ?TURBO_0000522 <http://purl.obolibrary.org/obo/OBI_0000293> ?IAO_0000100 .
      ?TURBO_0000522 rdf:type <http://transformunify.org/ontologies/TURBO_0000522> .
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010113> ?OGMS_0000097 .
      ?TURBO_0010158 rdf:type <http://transformunify.org/ontologies/TURBO_0010158> .
      ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000056> ?OGMS_0000097 .
      ?NCBITaxon_9606 rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
      ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000086> ?PATO_0000119 .
      ?PATO_0000119 rdf:type <http://purl.obolibrary.org/obo/PATO_0000119> .
      ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000086> ?PATO_0000128 .
      ?PATO_0000128 rdf:type <http://purl.obolibrary.org/obo/PATO_0000128> .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0010138 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?OBI_0001929 .
      ?OGMS_0000097 <http://transformunify.org/ontologies/TURBO_0010139> ?TURBO_0010138 .
      ?OGMS_0000097 <http://transformunify.org/ontologies/TURBO_0010139> ?OBI_0001929 .
      ?TURBO_0010138 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0010138 <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
      ?TURBO_0010138 <http://purl.obolibrary.org/obo/IAO_0000221> ?PATO_0000119 .
      ?OBI_0001929 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?OBI_0001929 <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
      ?OBI_0001929 <http://purl.obolibrary.org/obo/IAO_0000221> ?PATO_0000128 .
      ?NCBITaxon_9606 <http://purl.obolibrary.org/obo/RO_0000087> ?OBI_0000093 .
      ?OBI_0000093 rdf:type <http://purl.obolibrary.org/obo/OBI_0000093> .
      ?OBI_0000093 <http://purl.obolibrary.org/obo/BFO_0000054> ?OGMS_0000097 .
      ?EFO_0004340 <http://purl.obolibrary.org/obo/IAO_0000136> ?NCBITaxon_9606 .
      ?VSO_0000006 <http://purl.obolibrary.org/obo/BFO_0000050> ?OGMS_0000097 .
      ?VSO_0000006 rdf:type <http://purl.obolibrary.org/obo/VSO_0000006> .
      ?VSO_0000006 <http://purl.obolibrary.org/obo/OBI_0000299> ?HTN_00000001 .
      ?HTN_00000001 rdf:type <http://purl.obolibrary.org/obo/HTN_00000001> .
      ?VSO_0000006 <http://purl.obolibrary.org/obo/OBI_0000299> ?HTN_00000000 .
      ?HTN_00000000 rdf:type <http://purl.obolibrary.org/obo/HTN_00000000> .
      ?HTN_00000000 <http://purl.obolibrary.org/obo/OBI_0001938> ?TURBO_0010150 .
      ?HTN_00000001 <http://purl.obolibrary.org/obo/OBI_0001938> ?TURBO_0010149 .
      ?OGMS_0000097 <http://purl.obolibrary.org/obo/BFO_0000051> ?VSO_0000006 .
      ?HTN_00000000 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?HTN_00000000 .
      ?HTN_00000001 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?HTN_00000001 .
      ?VSO_0000004 <http://purl.obolibrary.org/obo/RO_0000052> ?NCBITaxon_9606 .
      ?VSO_0000004 rdf:type <http://purl.obolibrary.org/obo/VSO_0000004> .
      ?HTN_00000000 <http://purl.obolibrary.org/obo/IAO_0000221> ?VSO_0000004 .
      ?HTN_00000001 <http://purl.obolibrary.org/obo/IAO_0000221> ?VSO_0000004 .
      ?EFO_0004340 <http://transformunify.org/ontologies/TURBO_0010094> ?bmiDoubleLiteralValue .
      ?IAO_0000100 <http://purl.org/dc/elements/1.1/title> ?datasetTitleStringLiteralValue .
      ?TURBO_0000512 <http://transformunify.org/ontologies/TURBO_0010096> ?healthcareEncounterDateLiteralValue .
      ?TURBO_0000512 <http://transformunify.org/ontologies/TURBO_0010095> ?healthcareEncounterDateStringLiteralValue .
      ?TURBO_0000509 <http://transformunify.org/ontologies/TURBO_0010094> ?healthcareEncounterSymbolLiteralValue .
      ?TURBO_0010138 <http://transformunify.org/ontologies/TURBO_0010094> ?lengthMeasurementDoubleLiteralValue .
      ?OBI_0001929 <http://transformunify.org/ontologies/TURBO_0010094> ?massMeasurementDoubleLiteralValue .
      ?TURBO_0010149 <http://transformunify.org/ontologies/TURBO_0010094> ?systolicBloodPressureDoubleLiteralValue .
      ?TURBO_0010150 <http://transformunify.org/ontologies/TURBO_0010094> ?diastolicBloodPressureDoubleLiteralValue .
      ?HealthcareEncounterRegistryOfVariousTypes <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0000508 .
      }
      GRAPH <${Globals.processNamedGraph}> {
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000508 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?HealthcareEncounterRegistryOfVariousTypes .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0010138 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?OBI_0001929 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0010149 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0010150 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?OGMS_0000097 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?EFO_0004340 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?IAO_0000100 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000512 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000509 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000511 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000522 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0010158 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?VSO_0000006 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?HTN_00000001 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?HTN_00000000 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?NCBITaxon_9606 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> <http://purl.obolibrary.org/obo/UO_0000015> .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> <http://purl.obolibrary.org/obo/UO_0000272> .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> <http://purl.obolibrary.org/obo/UO_0000009> .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?PATO_0000119 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?PATO_0000128 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?OBI_0000093 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?VSO_0000004 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010158 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010161 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?NCBITaxon_9606 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?HealthcareEncounterRegistryOfVariousTypes .
      }
      }
      WHERE {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?TURBO_0010161 <http://transformunify.org/ontologies/TURBO_0010113> ?NCBITaxon_9606 .
      ?NCBITaxon_9606 rdf:type <http://purl.obolibrary.org/obo/NCBITaxon_9606> .
      }
      GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010110> ?HealthcareEncounterRegistryOfVariousTypes .
      ?TURBO_0010158 rdf:type <http://transformunify.org/ontologies/TURBO_0010158> .
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000643> ?datasetTitleStringLiteralValue .
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000648> ?healthcareEncounterSymbolLiteralValue .
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010131> ?TURBO_0010161 .
      ?TURBO_0010161 rdf:type <http://transformunify.org/ontologies/TURBO_0010161> .
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000655> ?bmiDoubleLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000644> ?healthcareEncounterDateStringLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000645> ?healthcareEncounterDateLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000646> ?lengthMeasurementDoubleLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000647> ?massMeasurementDoubleLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010259> ?diastolicBloodPressureDoubleLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010258> ?systolicBloodPressureDoubleLiteralValue .
       }
      }
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(str(?datasetTitleStringLiteralValue),"localUUID")))) AS ?IAO_0000100)
      BIND(IF (BOUND(?bmiDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?EFO_0004340","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?EFO_0004340)
      BIND(IF (BOUND(?diastolicBloodPressureDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0010150","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?TURBO_0010150)
      BIND(IF (BOUND(?systolicBloodPressureDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0010149","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?TURBO_0010149)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000509","localUUID", str(?TURBO_0010158))))) AS ?TURBO_0000509)
      BIND(IF (BOUND(?lengthMeasurementDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0010138","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?TURBO_0010138)
      BIND(IF ((BOUND(?systolicBloodPressureDoubleLiteralValue) || BOUND(?diastolicBloodPressureDoubleLiteralValue)), uri(concat("${Globals.defaultPrefix}", SHA256(CONCAT("?VSO_0000006", "localUUID", str(?TURBO_0010158))))), ?unbound) AS ?VSO_0000006)
      BIND(IF (BOUND(?healthcareEncounterDateStringLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000512","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?TURBO_0000512)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000508","localUUID", str(?TURBO_0010158))))) AS ?TURBO_0000508)
      BIND(IF (BOUND(?healthcareEncounterDateStringLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000511","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?TURBO_0000511)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?OGMS_0000097","localUUID", str(?TURBO_0010158))))) AS ?OGMS_0000097)
      BIND(IF (BOUND(?systolicBloodPressureDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?HTN_00000001","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?HTN_00000001)
      BIND(IF (BOUND(?massMeasurementDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?OBI_0001929","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?OBI_0001929)
      BIND(IF (BOUND(?diastolicBloodPressureDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?HTN_00000000","localUUID", str(?TURBO_0010158))))), ?unbound) AS ?HTN_00000000)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000522","localUUID")))) AS ?TURBO_0000522)
      BIND(IF ((BOUND(?systolicBloodPressureDoubleLiteralValue) || BOUND(?diastolicBloodPressureDoubleLiteralValue)), uri(concat("${Globals.defaultPrefix}", SHA256(CONCAT("?VSO_0000004", "localUUID", str(?NCBITaxon_9606))))), ?unbound) AS ?VSO_0000004)
      BIND(IF (BOUND(?lengthMeasurementDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?PATO_0000119","localUUID", str(?NCBITaxon_9606))))), ?unbound) AS ?PATO_0000119)
      BIND(IF (BOUND(?massMeasurementDoubleLiteralValue), uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?PATO_0000128","localUUID", str(?NCBITaxon_9606))))), ?unbound) AS ?PATO_0000128)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?OBI_0000093","localUUID")))) AS ?OBI_0000093)
      }
      """
    
    val diagnosisQuery = s"""
      INSERT {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?OGMS_0000073 <http://purl.obolibrary.org/obo/IAO_0000142> ?IcdTermOfVariousTypes .
      ?OGMS_0000073 rdf:type <http://purl.obolibrary.org/obo/OGMS_0000073> .
      ?OGMS_0000073 <http://purl.obolibrary.org/obo/IAO_0000142> ?SnomedTermOfVariousTypes .
      ?OGMS_0000073 <http://transformunify.org/ontologies/TURBO_0000703> ?DiagnosisRegistryOfVariousTypes .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?OGMS_0000073 .
      ?IAO_0000100 rdf:type <http://purl.obolibrary.org/obo/IAO_0000100> .
      ?OGMS_0000073 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?OGMS_0000097 <http://purl.obolibrary.org/obo/OBI_0000299> ?OGMS_0000073 .
      ?OGMS_0000097 rdf:type <http://purl.obolibrary.org/obo/OGMS_0000097> .
      ?TURBO_0010160 <http://transformunify.org/ontologies/TURBO_0010113> ?OGMS_0000073 .
      ?TURBO_0010160 rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
      ?OGMS_0000073 <http://transformunify.org/ontologies/TURBO_0010094> ?diagnosisTermSuffixStringLiteralValue .
      ?OGMS_0000073 <http://transformunify.org/ontologies/TURBO_0010014> ?diagnosisCodingSequenceIntegerLiteralValue .
      ?OGMS_0000073 <http://transformunify.org/ontologies/TURBO_0006515> ?diagnosisRegistryStringLiteralValue .
      ?OGMS_0000073 <http://transformunify.org/ontologies/TURBO_0010013> ?primaryDiagnosisBooleanLiteralValue .
      }
      GRAPH <${Globals.processNamedGraph}> {
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?OGMS_0000073 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?IcdTermOfVariousTypes .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?SnomedTermOfVariousTypes .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?DiagnosisRegistryOfVariousTypes .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?IAO_0000100 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?OGMS_0000097 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0010160 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010158 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?OGMS_0000097 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010160 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?DiagnosisRegistryOfVariousTypes .
      }
      }
      WHERE {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010113> ?OGMS_0000097 .
      ?OGMS_0000097 rdf:type <http://purl.obolibrary.org/obo/OGMS_0000097> .
      }
      GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
      ?TURBO_0010158 <http://purl.obolibrary.org/obo/OBI_0000299> ?TURBO_0010160 .
      ?TURBO_0010158 rdf:type <http://transformunify.org/ontologies/TURBO_0010158> .
      ?TURBO_0010160 rdf:type <http://transformunify.org/ontologies/TURBO_0010160> .
      ?TURBO_0010160 <http://transformunify.org/ontologies/TURBO_0004602> ?diagnosisRegistryStringLiteralValue .
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000643> ?datasetTitleStringLiteralValue .
      OPTIONAL {
       ?TURBO_0010160 <http://transformunify.org/ontologies/TURBO_0004603> ?DiagnosisRegistryOfVariousTypes .
       }
      OPTIONAL {
       ?TURBO_0010160 <http://transformunify.org/ontologies/TURBO_0010014> ?diagnosisCodingSequenceIntegerLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010160 <http://transformunify.org/ontologies/TURBO_0004601> ?diagnosisTermSuffixStringLiteralValue .
       }
      OPTIONAL {
       ?TURBO_0010160 <http://transformunify.org/ontologies/TURBO_0010013> ?primaryDiagnosisBooleanLiteralValue .
       }
      }
      BIND(IF (?DiagnosisRegistryOfVariousTypes = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, uri(concat("http://purl.bioontology.org/ontology/ICD9CM/", ?diagnosisTermSuffixStringLiteralValue)), ?unbound) AS ?icd9term)
      BIND(IF (?DiagnosisRegistryOfVariousTypes = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, uri(concat("http://purl.bioontology.org/ontology/ICD10CM/", ?diagnosisTermSuffixStringLiteralValue)), ?unbound) AS ?icd10term)
      BIND(IF (bound(?icd9term) && !bound(?icd10term),?icd9term,?unbound) as ?IcdTermOfVariousTypes)
      BIND(IF (bound(?icd10term) && !bound(?icd9term),?icd10term,?IcdTermOfVariousTypes) as ?IcdTermOfVariousTypes)
      BIND(IF (?DiagnosisRegistryOfVariousTypes = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C53489>, uri(concat("http://purl.bioontology.org/ontology/SNOMEDCT/", ?diagnosisTermSuffixStringLiteralValue)), ?unbound) AS ?SnomedTermOfVariousTypes)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?OGMS_0000073","localUUID", str(?TURBO_0010160))))) AS ?OGMS_0000073)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(str(?datasetTitleStringLiteralValue),"localUUID")))) AS ?IAO_0000100)
      }
      """
    
    val medicationQuery = s"""
      INSERT {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?PDRO_0000001 <http://purl.obolibrary.org/obo/IAO_0000142> ?DrugTermOfVariousTypes .
      ?PDRO_0000001 rdf:type <http://purl.obolibrary.org/obo/PDRO_0000001> .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?PDRO_0000001 .
      ?IAO_0000100 rdf:type <http://purl.obolibrary.org/obo/IAO_0000100> .
      ?OGMS_0000097 <http://purl.obolibrary.org/obo/OBI_0000299> ?PDRO_0000001 .
      ?OGMS_0000097 rdf:type <http://purl.obolibrary.org/obo/OGMS_0000097> .
      ?TURBO_0000561 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000562 .
      ?TURBO_0000561 rdf:type <http://transformunify.org/ontologies/TURBO_0000561> .
      ?TURBO_0000562 rdf:type <http://transformunify.org/ontologies/TURBO_0000562> .
      ?TURBO_0000561 <http://purl.obolibrary.org/obo/IAO_0000219> ?PDRO_0000001 .
      ?TURBO_0000562 <http://purl.obolibrary.org/obo/BFO_0000050> ?TURBO_0000561 .
      ?PDRO_0000001 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?TURBO_0000562 <http://purl.obolibrary.org/obo/BFO_0000050> ?IAO_0000100 .
      ?IAO_0000100 <http://purl.obolibrary.org/obo/BFO_0000051> ?TURBO_0000562 .
      ?TURBO_0010159 <http://transformunify.org/ontologies/TURBO_0010113> ?PDRO_0000001 .
      ?TURBO_0010159 rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
      ?TURBO_0000562 <http://transformunify.org/ontologies/TURBO_0010094> ?medicationSymbolStringLiteralValue .
      ?PDRO_0000001 <http://transformunify.org/ontologies/TURBO_0010094> ?medicationOrderNameStringLiteralValue .
      }
      GRAPH <${Globals.processNamedGraph}> {
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?PDRO_0000001 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?DrugTermOfVariousTypes .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?IAO_0000100 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?OGMS_0000097 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000561 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0000562 .
      <processURI> <http://transformunify.org/ontologies/TURBO_0010184> ?TURBO_0010159 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010158 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?OGMS_0000097 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?TURBO_0010159 .
      <processURI> <http://purl.obolibrary.org/obo/OBI_0000293> ?DrugTermOfVariousTypes .
      }
      }
      WHERE {
      GRAPH <${Globals.expandedNamedGraph}> {
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0010113> ?OGMS_0000097 .
      ?OGMS_0000097 rdf:type <http://purl.obolibrary.org/obo/OGMS_0000097> .
      }
      GRAPH <http://www.itmat.upenn.edu/biobank/Shortcuts_> {
      ?TURBO_0010158 <http://purl.obolibrary.org/obo/OBI_0000299> ?TURBO_0010159 .
      ?TURBO_0010158 rdf:type <http://transformunify.org/ontologies/TURBO_0010158> .
      ?TURBO_0010159 rdf:type <http://transformunify.org/ontologies/TURBO_0010159> .
      ?TURBO_0010158 <http://transformunify.org/ontologies/TURBO_0000643> ?datasetTitleStringLiteralValue .
      ?TURBO_0010159 <http://transformunify.org/ontologies/TURBO_0005601> ?medicationSymbolStringLiteralValue .
      OPTIONAL {
       ?TURBO_0010159 <http://transformunify.org/ontologies/TURBO_0005612> ?DrugTermOfVariousTypes .
       }
      OPTIONAL {
       ?TURBO_0010159 <http://transformunify.org/ontologies/TURBO_0005611> ?medicationOrderNameStringLiteralValue .
       }
      }
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000561","localUUID", str(?TURBO_0010159))))) AS ?TURBO_0000561)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT(str(?datasetTitleStringLiteralValue),"localUUID")))) AS ?IAO_0000100)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?TURBO_0000562","localUUID", str(?TURBO_0010159))))) AS ?TURBO_0000562)
      BIND(uri(concat("${Globals.defaultPrefix}",SHA256(CONCAT("?PDRO_0000001","localUUID", str(?TURBO_0010159))))) AS ?PDRO_0000001)
      }
      """
    
    override def beforeAll()
    {
        assert("test" === System.getenv("SCALA_ENV"), "System variable SCALA_ENV must be set to \"test\"; check your build.sbt file")
        
        graphDBMaterials = ConnectToGraphDB.initializeGraph()
        DrivetrainDriver.updateModel(graphDBMaterials, "testing_instruction_set.tis", "testing_graph_specification.gs")
        Globals.cxn = graphDBMaterials.getConnection()
        Globals.gmCxn = graphDBMaterials.getGmConnection()
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
        
        RunDrivetrainProcess.setInputNamedGraphsCache(false)
    }
    
    override def afterAll()
    {
        ConnectToGraphDB.closeGraphConnection(graphDBMaterials, clearTestingRepositoryAfterRun)
    }
    
    before
    {
        Utilities.deleteAllTriplesInDatabase(Globals.cxn)
    }
    
    test("generated query matched expected query - healthcare expansion")
    {
        Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", healthcareQuery) should be (true) 
    }
    
    test("generated query matched expected query - diagnosis expansion")
    {
        Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", diagnosisQuery) should be (true) 
    }
    
    test("generated query matched expected query - medications expansion")
    {
        Utilities.checkGeneratedQueryAgainstMatchedQuery("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", medicationQuery) should be (true) 
    }
    
    test("hc encounter with all fields")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          
          <http://www.itmat.upenn.edu/biobank/hcenc1>
            turbo:TURBO_0000655 "26.2577659792"^^xsd:float ;
            turbo:TURBO_0000644 "15/Jan/2017" ;
            turbo:TURBO_0000648 "20" ;
            turbo:TURBO_0000647 "83.0082554658"^^xsd:float ;
            turbo:TURBO_0000646 "177.8"^^xsd:float ;
            turbo:TURBO_0000645 "2017-01-15"^^xsd:date ;
            a turbo:TURBO_0010158 ;
            turbo:TURBO_0010110 turbo:TURBO_0000510 ;
            turbo:TURBO_0000643 "enc_expand.csv" ;
            turbo:TURBO_0010131 pmbb:part1 ;
            turbo:TURBO_0010259 "80"^^xsd:Float ;
            turbo:TURBO_0010258 "120"^^xsd:Float ;
          
          obo:OBI_0000299 pmbb:diagnosis1 .
          pmbb:diagnosis1 a turbo:TURBO_0010160 ;
            turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
            turbo:TURBO_0004602 "ICD-9" ;
            turbo:TURBO_0004601 "401.9" ;
            turbo:TURBO_0010013 "true"^^xsd:Boolean ;
            turbo:TURBO_0010014 "1"^^xsd:Integer .
          
          pmbb:hcenc1 obo:OBI_0000299 pmbb:prescription1 .
          pmbb:prescription1 a turbo:TURBO_0010159 ;
            turbo:TURBO_0005601 "3" ;
            turbo:TURBO_0005611 "holistic soil from the ganges" ;
            turbo:TURBO_0005612 turbo:someDrug .
            
            pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <${Globals.expandedNamedGraph}>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}
          """
        SparqlUpdater.updateSparql(Globals.cxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", Globals.dataValidationMode, false)
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, instantiationAndDataset).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterMinimum).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareDiagnosis).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMedications).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMeasurements).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterDate).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareSymbolAndRegistry).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, processMeta).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <${Globals.expandedNamedGraph}> {?s ?p ?o .}}"
        val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/OBI_0000299", 
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/IAO_0000142",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://transformunify.org/ontologies/TURBO_0000703",
            "http://purl.obolibrary.org/obo/RO_0002223", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010096", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000039", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/IAO_0000039", 
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010013",
            "http://purl.obolibrary.org/obo/IAO_0000142", "http://transformunify.org/ontologies/TURBO_0010014",
            "http://transformunify.org/ontologies/TURBO_0010113", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/OBI_0001938", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039","http://purl.obolibrary.org/obo/IAO_0000039",
            "http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/RO_0000052",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000221","http://purl.obolibrary.org/obo/IAO_0000221",
            "http://purl.obolibrary.org/obo/RO_0000056","http://purl.obolibrary.org/obo/RO_0000086",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://purl.obolibrary.org/obo/RO_0000087",
            "http://purl.obolibrary.org/obo/RO_0000086","http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000136","http://purl.obolibrary.org/obo/IAO_0000221",
            "http://purl.obolibrary.org/obo/IAO_0000221","http://purl.obolibrary.org/obo/BFO_0000054"
        )
        
        Utilities.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (checkPredicates.size)
        
        val healthcareInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000510 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000511 ;
                  ontologies:TURBO_0010184 ?TURBO_0000512 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  
                  ontologies:TURBO_0010184 ?VSO_0000004 ;
                  ontologies:TURBO_0010184 ?OBI_0000093 ;
                  ontologies:TURBO_0010184 ?PATO_0000119 ;
                  ontologies:TURBO_0010184 ?PATO_0000128 ;
                  
                  ontologies:TURBO_0010184 <http://purl.obolibrary.org/obo/UO_0000009> ;
                  ontologies:TURBO_0010184 <http://purl.obolibrary.org/obo/UO_0000015> ;
                  ontologies:TURBO_0010184 <http://purl.obolibrary.org/obo/UO_0000272> ;
                  
                  ontologies:TURBO_0010184 ?VSO_0000006 ;
                  ontologies:TURBO_0010184 ?HTN_00000000 ;
                  ontologies:TURBO_0010184 ?HTN_00000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0010150 ;
                  ontologies:TURBO_0010184 ?TURBO_0010149 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ?instantiation .
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000511 a turbo:TURBO_0000511 .
                ?TURBO_0000512 a turbo:TURBO_0000512 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?VSO_0000006 a obo:VSO_0000006 .
                ?HTN_00000000 a obo:HTN_00000000 .
                ?HTN_00000001 a obo:HTN_00000001 .
                ?TURBO_0010150 a turbo:TURBO_0010150 .
                ?TURBO_0010149 a turbo:TURBO_0010149 .
                ?instantiation a turbo:TURBO_0000522 .
                ?VSO_0000004 a obo:VSO_0000004 .
                ?OBI_0000093 a obo:OBI_0000093 .
                ?PATO_0000119 a obo:PATO_0000119 .
                ?PATO_0000128 a obo:PATO_0000128 .
            }
          }
          
          """
        
        val diagnosisInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagnosis1 ;
                  
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD9CM/401.9> ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  
                  ontologies:TURBO_0010184 pmbb:diagnosis1 ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
            }
          }
          
          """
        
        val medicationsInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:prescription1 ;

                  ontologies:TURBO_0010184 turbo:someDrug ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:prescription1 ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, diagnosisInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, medicationsInputsOutputs).get should be (true)
    }
    
    test("hc encounter with minimum required for expansion")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <${Globals.expandedNamedGraph}>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}
          """
        SparqlUpdater.updateSparql(Globals.cxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", Globals.dataValidationMode, false)
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, instantiationAndDataset).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterMinimum).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMeasurements).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareDiagnosis).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMedications).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterDate).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareSymbolAndRegistry).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, processMeta).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <${Globals.expandedNamedGraph}> {?s ?p ?o .}}"
        val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/IAO_0000219", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/BFO_0000054",
            "http://purl.obolibrary.org/obo/RO_0000056","http://purl.obolibrary.org/obo/RO_0000087",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
        )
        
        Utilities.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (checkPredicates.size)
        
        val processInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000510 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ?instantiation ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, processInputsOutputs).get should be (true)
    }
    
    test("hc encounter with text but not xsd values and only diag code without reg info")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000655 "26.2577659792"^^xsd:float ;
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000644 "15/Jan/2017" ;
          turbo:TURBO_0000648 "20" ;
          turbo:TURBO_0000647 "83.0082554658"^^xsd:float ;
          turbo:TURBO_0000646 "177.8"^^xsd:float ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010259 "80"^^xsd:Float ;
          turbo:TURBO_0010258 "120"^^xsd:Float ;
          
          obo:OBI_0000299 pmbb:diagnosis1 .
          pmbb:diagnosis1 a turbo:TURBO_0010160 ;
              turbo:TURBO_0004602 "ICD-9" .
              
          pmbb:hcenc1 obo:OBI_0000299 pmbb:prescription1 .
          pmbb:prescription1 a turbo:TURBO_0010159 ;
          turbo:TURBO_0005601 "3" .
          
          pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          
          }
          Graph <${Globals.expandedNamedGraph}>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}
          """
        SparqlUpdater.updateSparql(Globals.cxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", Globals.dataValidationMode, false)
        
        val diagnosisNoXsd: String = s"""
          ASK { GRAPH <${Globals.expandedNamedGraph}> {
                ?encounter a obo:OGMS_0000097 .
                ?dataset a obo:IAO_0000100 .
        		?encounter obo:OBI_0000299 ?diagnosis.
        		?diagnosis a obo:OGMS_0000073 .
        		?diagnosis turbo:TURBO_0006515 "ICD-9" .
        		?diagnosis obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?diagnosis .
        	}}
          """
        
        val dateNoXsd: String = s"""
          ASK { GRAPH <${Globals.expandedNamedGraph}> {
                ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
        		?encDate turbo:TURBO_0010095 "15/Jan/2017" .
        		# ?encDate turbo:TURBO_0010096 "2017-01-15"^^xsd:date .
        		?encDate obo:IAO_0000136 ?encStart .
        		?dataset obo:BFO_0000051 ?encDate .
        		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		?dataset a obo:IAO_0000100 .
        		?encounter a obo:OGMS_0000097 .
           }}"""
        
        val healthcareMedicationsMinimum: String = s"""
        ask {graph <${Globals.expandedNamedGraph}> {
            ?dataset a obo:IAO_0000100 .
            ?encounter a obo:OGMS_0000097 .
            ?encounter obo:OBI_0000299 ?drugPrescript .
        		?drugPrescript a obo:PDRO_0000001 .
  
        		?medCrid obo:IAO_0000219 ?drugPrescript .
        		?medCrid a turbo:TURBO_0000561 .
        		?medCrid obo:BFO_0000051 ?medCridSymbol .
        		?medCridSymbol a turbo:TURBO_0000562 .
        		?medCridSymbol turbo:TURBO_0010094 "3" .
        		
        		?drugPrescript obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?drugPrescript .
        		?medCridSymbol obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?medCridSymbol .
        		}}
        """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, instantiationAndDataset).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterMinimum).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMeasurements).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareDiagnosis).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMedicationsMinimum).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterDate).get should be (false)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, diagnosisNoXsd).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, dateNoXsd).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareSymbolAndRegistry).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, processMeta).get should be (true)
        
        val count: String = s"SELECT * WHERE {GRAPH <${Globals.expandedNamedGraph}> {?s ?p ?o .}}"
        val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, count, "p")
        
        val checkPredicates = Array (
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/OBI_0000293",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://purl.obolibrary.org/obo/OBI_0000299", 
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://purl.obolibrary.org/obo/BFO_0000051", 
            "http://purl.org/dc/elements/1.1/title", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000136",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/RO_0002223",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010095",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000051", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://purl.obolibrary.org/obo/IAO_0000219",
            "http://transformunify.org/ontologies/TURBO_0010094", "http://purl.obolibrary.org/obo/BFO_0000050",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0010113",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://transformunify.org/ontologies/TURBO_0010139", "http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039", "http://transformunify.org/ontologies/TURBO_0006515",
            "http://purl.obolibrary.org/obo/BFO_0000050", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://purl.obolibrary.org/obo/OBI_0000299", "http://purl.obolibrary.org/obo/OBI_0000299",
            "http://purl.obolibrary.org/obo/OBI_0001938", "http://purl.obolibrary.org/obo/OBI_0001938",
            "http://transformunify.org/ontologies/TURBO_0010094","http://transformunify.org/ontologies/TURBO_0010094",
            "http://purl.obolibrary.org/obo/IAO_0000039","http://purl.obolibrary.org/obo/IAO_0000039",
            "http://purl.obolibrary.org/obo/BFO_0000051","http://purl.obolibrary.org/obo/BFO_0000051",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000050",
            "http://purl.obolibrary.org/obo/BFO_0000050","http://purl.obolibrary.org/obo/BFO_0000051",
            "http://transformunify.org/ontologies/TURBO_0010113","http://purl.obolibrary.org/obo/RO_0000052",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000221","http://purl.obolibrary.org/obo/IAO_0000221",
            "http://purl.obolibrary.org/obo/RO_0000056","http://purl.obolibrary.org/obo/RO_0000086",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://purl.obolibrary.org/obo/RO_0000087",
            "http://purl.obolibrary.org/obo/RO_0000086","http://purl.obolibrary.org/obo/IAO_0000136",
            "http://purl.obolibrary.org/obo/IAO_0000136","http://purl.obolibrary.org/obo/IAO_0000221",
            "http://purl.obolibrary.org/obo/IAO_0000221","http://purl.obolibrary.org/obo/BFO_0000054",
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type","http://transformunify.org/ontologies/TURBO_0010113"
        )
        
        Utilities.checkStringArraysForEquivalency(checkPredicates, result.toArray)("equivalent").asInstanceOf[String] should be ("true")
        
        result.size should be (checkPredicates.size)
        
        val healthcareInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000510 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000511 ;
                  ontologies:TURBO_0010184 ?TURBO_0000512 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  
                  ontologies:TURBO_0010184 ?VSO_0000006 ;
                  ontologies:TURBO_0010184 ?HTN_00000000 ;
                  ontologies:TURBO_0010184 ?HTN_00000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0010150 ;
                  ontologies:TURBO_0010184 ?TURBO_0010149 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ?instantiation .
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000511 a turbo:TURBO_0000511 .
                ?TURBO_0000512 a turbo:TURBO_0000512 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?VSO_0000006 a obo:VSO_0000006 .
                ?HTN_00000000 a obo:HTN_00000000 .
                ?HTN_00000001 a obo:HTN_00000001 .
                ?TURBO_0010150 a turbo:TURBO_0010150 .
                ?TURBO_0010149 a turbo:TURBO_0010149 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        val diagnosisInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagnosis1 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  
                  ontologies:TURBO_0010184 pmbb:diagnosis1 ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
            }
          }
          
          """
        
        val medicationsInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:prescription1 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:prescription1 ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, diagnosisInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, medicationsInputsOutputs).get should be (true)
    }
    
    test("ensure diagnosis info stays together with duplicate hc enc URI")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          obo:OBI_0000299 pmbb:diagnosis1 .
          pmbb:diagnosis1 a turbo:TURBO_0010160 ;
          turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
          turbo:TURBO_0004602 "ICD-9" ;
          turbo:TURBO_0004601 "401.9" .
          
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
          obo:OBI_0000299 pmbb:diagnosis2 .
          pmbb:diagnosis2 a turbo:TURBO_0010160 ;
          turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> ;
          turbo:TURBO_0004602 "ICD-10" ;
          turbo:TURBO_0004601 "177.8" .
          
          pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <${Globals.expandedNamedGraph}>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}"""
        
        SparqlUpdater.updateSparql(Globals.cxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", Globals.dataValidationMode, false)
        
        val checkDiag: String = s"""
          Ask
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?diagnosis1 .
                  ?diagnosis1 a obo:OGMS_0000073 .
                  ?diagnosis2 a obo:OGMS_0000073 .
                  
                  ?diagnosis1 turbo:TURBO_0006515 "ICD-9" .
                  ?diagnosis1 turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
                  ?diagnosis1 turbo:TURBO_0010094 "401.9" .
                  ?diagnosis1 obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
                  
                  ?diagnosis2 turbo:TURBO_0006515 "ICD-10" .
                  ?diagnosis2 turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892> .
                  ?diagnosis2 turbo:TURBO_0010094 "177.8" .
                  ?diagnosis2 obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD10CM/177.8> .
                  
                  Filter (?diagnosis1 != ?diagnosis2) 
              }
          }
          """
        
        val countDiag: String = s"""
          Select (count (distinct ?diagnosis) as ?diagnosisCount)
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?diagnosis .
                  ?diagnosis a obo:OGMS_0000073 .
              }
          }
          """
         SparqlUpdater.querySparqlBoolean(Globals.cxn, instantiationAndDataset).get should be (true)
         SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterMinimum).get should be (true)
         SparqlUpdater.querySparqlBoolean(Globals.cxn, checkDiag).get should be (true)
         SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, countDiag, "diagnosisCount")(0) should startWith ("\"2")
         SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareSymbolAndRegistry).get should be (true)
         SparqlUpdater.querySparqlBoolean(Globals.cxn, processMeta).get should be (true)
        
        val healthcareInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000510 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ?instantiation .
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        val diagnosisInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagnosis1 ;
                  obo:OBI_0000293 pmbb:diagnosis2 ;
                  
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD9CM/401.9> ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  
                  ontologies:TURBO_0010184 pmbb:diagnosis1 ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
            }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, diagnosisInputsOutputs).get should be (true)
    }
    
    test("ensure medication info stays together with duplicate hc enc URI")
    {
        val insert: String = s"""
          INSERT DATA { GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts {
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 turbo:TURBO_0000510 ;
          obo:OBI_0000299 pmbb:prescription1 .
          pmbb:prescription1 a turbo:TURBO_0010159 ;
          turbo:TURBO_0005601 "3" ;
          turbo:TURBO_0005611 "holistic soil from the ganges" .
          
          pmbb:hcenc1
          turbo:TURBO_0000643 "enc_expand.csv" ;
          turbo:TURBO_0000648 "20" ;
          a turbo:TURBO_0010158 ;
          turbo:TURBO_0010110 turbo:TURBO_0000510 ;
          obo:OBI_0000299 pmbb:prescription2 .
          pmbb:prescription2 a turbo:TURBO_0010159 ;
          turbo:TURBO_0005601 "4" ;
          turbo:TURBO_0005611 "medicinal purple kush" . 
          
          pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
          pmbb:part1 a turbo:TURBO_0010161 .
          }
          Graph <${Globals.expandedNamedGraph}>
          {
              pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
              pmbb:expandedPart a obo:NCBITaxon_9606 .
          }}"""
        
        SparqlUpdater.updateSparql(Globals.cxn, insert)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", Globals.dataValidationMode, false)
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", Globals.dataValidationMode, false)
        
        val checkDiag: String = s"""
          Ask
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?prescription1 .
                  ?prescription1 a obo:PDRO_0000001 .
                  ?prescription1 turbo:TURBO_0010094 "holistic soil from the ganges" .
                  ?medCrid1 obo:IAO_0000219 ?prescription1 .
                  ?medCrid1 a turbo:TURBO_0000561 .
                  ?enc obo:OBI_0000299 ?prescription2 .
                  ?prescription2 a obo:PDRO_0000001 .
                  ?prescription2 turbo:TURBO_0010094 "medicinal purple kush" .
                  ?medCrid2 obo:IAO_0000219 ?prescription2 .
                  ?medCrid2 a turbo:TURBO_0000561 .
                  
                  ?medCrid1 obo:BFO_0000051 ?medCridSymbol1 .
                  ?medCridSymbol1 a turbo:TURBO_0000562 .
                  ?medCridSymbol1 turbo:TURBO_0010094 "3" .
                  
                  ?medCrid2 obo:BFO_0000051 ?medCridSymbol2 .
                  ?medCridSymbol2 a turbo:TURBO_0000562 .
                  ?medCridSymbol2 turbo:TURBO_0010094 "4" .
                  
                  Filter (?medCrid1 != ?medCrid2) 
                  Filter (?prescription1 != ?prescription2) 
              }
          }
          """
        
        val countDiag: String = s"""
          Select (count (distinct ?prescription) as ?prescriptCount) (count (distinct ?medCrid) as ?medCridCount)
          {
              Graph <${Globals.expandedNamedGraph}>
              {
                  ?enc a obo:OGMS_0000097 .
                  ?enc obo:OBI_0000299 ?prescription .
                  ?prescription a obo:PDRO_0000001 .
                  ?medCrid obo:IAO_0000219 ?prescription .
              }
          }
          """
         SparqlUpdater.querySparqlBoolean(Globals.cxn, instantiationAndDataset).get should be (true)
         SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterMinimum).get should be (true)
         SparqlUpdater.querySparqlBoolean(Globals.cxn, checkDiag).get should be (true)
         SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, countDiag, "prescriptCount")(0) should startWith ("\"2")
         SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, countDiag, "medCridCount")(0) should startWith ("\"2")
         SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareSymbolAndRegistry).get should be (true)
         SparqlUpdater.querySparqlBoolean(Globals.cxn, processMeta).get should be (true)
        
        val healthcareInputsOutputs: String = s"""
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:expandedPart ;
                  obo:OBI_0000293 pmbb:part1 ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000510 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ?instantiation .
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
        """
        
        val medicationsInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:prescription1 ;
                  obo:OBI_0000293 pmbb:prescription2 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:prescription1 ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, medicationsInputsOutputs).get should be (true)
    }
    
    test("expand hc encs over multiple named graphs")
    {
        logger.info("starting triples count: " + Utilities.countTriplesInDatabase(Globals.cxn))
        val insert1: String = s"""
          INSERT DATA
          {
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'identifierAndRegistry.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> .
                      
                      pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
                      pmbb:part1 a turbo:TURBO_0010161 .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts1
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'diagnosis.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
                      obo:OBI_0000299 pmbb:diagCridSC .
                  pmbb:diagCridSC a turbo:TURBO_0010160 ;
                      turbo:TURBO_0004602 'ICD-9' ;
                      turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                      turbo:TURBO_0004601 '401.9' ;
                      turbo:TURBO_0010013 "true"^^xsd:Boolean ;
                      turbo:TURBO_0010014 "1"^^xsd:Integer .
                      
                  pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
                  pmbb:part1 a turbo:TURBO_0010161 .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts2
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'meds.csv' ;
                      obo:OBI_0000299 pmbb:prescription ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> .
                      pmbb:prescription a turbo:TURBO_0010159 ;
                      turbo:TURBO_0005611 "holistic soil from the ganges" ;
                      turbo:TURBO_0005612 turbo:someDrug ;
                      turbo:TURBO_0005601 "3" .
                      
                      pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
                      pmbb:part1 a turbo:TURBO_0010161 .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts3
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'bmiAndHeightWeight.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
                      turbo:TURBO_0000646 '177.8'^^xsd:float ;
                      turbo:TURBO_0000647 '83.0082554658'^^xsd:float ;
                      turbo:TURBO_0000655 '26.2577659792'^^xsd:float ;
                      turbo:TURBO_0010259 "80"^^xsd:Float ;
                      turbo:TURBO_0010258 "120"^^xsd:Float .
                      
                      pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
                      pmbb:part1 a turbo:TURBO_0010161 .
              }
              
              GRAPH pmbb:Shortcuts_healthcareEncounterShortcuts4
              {
                  pmbb:hcenc1 a turbo:TURBO_0010158 ;
                      turbo:TURBO_0000643 'date.csv' ;
                      turbo:TURBO_0000648 '20' ;
                      turbo:TURBO_0010110 <http://transformunify.org/ontologies/TURBO_0000510> ;
                      turbo:TURBO_0000644 '15/Jan/2017' ;
                      turbo:TURBO_0000645 '2017-01-15'^^xsd:date .
                      
                      pmbb:hcenc1 turbo:TURBO_0010131 pmbb:part1 .
                      pmbb:part1 a turbo:TURBO_0010161 .
              }
              Graph <${Globals.expandedNamedGraph}>
              {
                  pmbb:part1 turbo:TURBO_0010113 pmbb:expandedPart .
                  pmbb:expandedPart a obo:NCBITaxon_9606 .
              }
          }
          """
        SparqlUpdater.updateSparql(Globals.cxn, insert1)
        logger.info("triples count after insert: " + Utilities.countTriplesInDatabase(Globals.cxn))
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess", Globals.dataValidationMode, false)
        logger.info("triples count after hc process: " + Utilities.countTriplesInDatabase(Globals.cxn))
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", Globals.dataValidationMode, false)
        logger.info("triples count after diagnoses process: " + Utilities.countTriplesInDatabase(Globals.cxn))
        RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", Globals.dataValidationMode, false)
        logger.info("triples count after meds process: " + Utilities.countTriplesInDatabase(Globals.cxn))
        val datasetCheck1: String = s"""
          ASK
          {
              GRAPH <${Globals.expandedNamedGraph}>
              {
                  ?encounter a obo:OGMS_0000097 .
                  ?encounterCrid a turbo:TURBO_0000508 .
                  ?encounterCrid obo:IAO_0000219 ?encounter .
                  ?encounterCrid obo:BFO_0000051 ?encsymb .
              		?encounterCrid obo:BFO_0000051 turbo:TURBO_0000510 .
              		?encsymb a turbo:TURBO_0000509 .
              		
              		?encSymb obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?encSymb .
              		
              		?dataset a obo:IAO_0000100 .
              		?dataset dc11:title 'identifierAndRegistry.csv'^^xsd:string .
              		?instantiation obo:OBI_0000293 ?dataset .
              		?instantiation a turbo:TURBO_0000522 .
              }
          }
          """
        
        val datasetCheck2: String = s"""
          ASK
          {
              GRAPH <${Globals.expandedNamedGraph}>
              {
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title 'diagnosis.csv'^^xsd:string .
                  ?instantiation obo:OBI_0000293 ?dataset .
                  
                  ?encounter a obo:OGMS_0000097 .
              		?encounter obo:OBI_0000299 ?diagnosis .
              		?diagnosis a obo:OGMS_0000073 .
              		?diagnosis turbo:TURBO_0010094 "401.9" .
            		  ?diagnosis turbo:TURBO_0000703 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> .
            		  ?diagnosis turbo:TURBO_0006515 "ICD-9" .
            		  ?diagnosis obo:IAO_0000142 <http://purl.bioontology.org/ontology/ICD9CM/401.9> .
              		
              		?dataset obo:BFO_0000051 ?diagnosis .
              		?diagnosis obo:BFO_0000050 ?dataset .
              }
          }
          """
        
        val datasetCheck3: String = s"""
          ASK
          {
              GRAPH <${Globals.expandedNamedGraph}>
              {
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title 'meds.csv'^^xsd:string .
                  ?instantiation obo:OBI_0000293 ?dataset .
                  
                  ?encounter a obo:OGMS_0000097 .
              		?encounter obo:OBI_0000299 ?drugPrescript .
              		?drugPrescript a obo:PDRO_0000001 .
              		?medCrid obo:IAO_0000219 ?drugPrescript .
              		?medCrid a turbo:TURBO_0000561 .
              		?medCrid obo:BFO_0000051 ?medCridSymbol .
              		?medCridSymbol a turbo:TURBO_0000562 .
              		
              		?drugPrescript obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?drugPrescript .
              		?medCridSymbol obo:BFO_0000050 ?dataset .
              		?dataset obo:BFO_0000051 ?medCridSymbol .
              }
          }
          """
        
        val datasetCheck4: String = s"""
          ASK
          {
              GRAPH <${Globals.expandedNamedGraph}>
              {
                ?dataset a obo:IAO_0000100 .
                ?dataset dc11:title 'bmiAndHeightWeight.csv'^^xsd:string .
                ?instantiation obo:OBI_0000293 ?dataset .
              
                ?encounter a obo:OGMS_0000097 .
                ?encounter obo:OBI_0000299 ?BMI .
                ?encounter turbo:TURBO_0010139 ?heightDatum .
                ?encounter turbo:TURBO_0010139 ?weightDatum .
                
            		?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> ;
            		    turbo:TURBO_0010094 "26.2577659792"^^xsd:float .
            		
        	      ?heightDatum rdf:type turbo:TURBO_0010138 ;
        	                 obo:IAO_0000039 obo:UO_0000015 ;
        	                 turbo:TURBO_0010094 "177.8"^^xsd:float ;
        	                 obo:BFO_0000050 ?dataset .
        	    
        	      ?weightDatum rdf:type obo:OBI_0001929 ;
        	                 obo:BFO_0000050 ?dataset ;
        	                 obo:IAO_0000039 obo:UO_0000009 ;
        	                 turbo:TURBO_0010094 "83.0082554658"^^xsd:float .
          	   
          	    ?BMI obo:BFO_0000050 ?dataset .   
          	    ?weightDatum obo:BFO_0000050 ?dataset .
          	    ?heightDatum obo:BFO_0000050 ?dataset .            
          	    ?dataset obo:BFO_0000051 ?BMI .
          	    
          		  ?dataset obo:BFO_0000051 ?weightDatum .
          		  ?dataset obo:BFO_0000051 ?heightDatum .
              }
          }
          """
        
        val datasetCheck5: String = s"""
          ASK
          {
              GRAPH <${Globals.expandedNamedGraph}>
              {
                ?dataset a obo:IAO_0000100 .
                ?dataset dc11:title 'date.csv' .
                ?instantiation obo:OBI_0000293 ?dataset .
              
                ?encounter a obo:OGMS_0000097 .
                ?encDate <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000512 .
            		?encDate obo:IAO_0000136 ?encStart .
            		?encStart <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
            		?encStart obo:RO_0002223 ?encounter .
            		
            		?dataset obo:BFO_0000051 ?encDate .
            		?encDate obo:BFO_0000050 ?dataset .
              }
          }
          """
        
        val thereShouldOnlyBeOneEncounter: String = s"""
          Select ?enc Where
          {
              Graph <${Globals.expandedNamedGraph}>{
              ?enc a obo:OGMS_0000097 .}
          }
          """
        
        val thereShouldBeFiveDatasets: String = s"""
          Select ?dataset Where
          {
              Graph <${Globals.expandedNamedGraph}> {
              ?dataset a obo:IAO_0000100 .}
          }
          """
        
        val processMetaMultipleDatasets = Utilities.buildProcessMetaQuery("http://www.itmat.upenn.edu/biobank/HealthcareEncounterExpansionProcess",
                                                  Array("http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts",
                                                      "http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts1",
                                                      "http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts2",
                                                      "http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts3",
                                                      "http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts4"))
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, processMetaMultipleDatasets).get should be (true)
        SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, thereShouldOnlyBeOneEncounter, "enc").size should be (1)
        SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, thereShouldBeFiveDatasets, "dataset").size should be (5)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterMinimum).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareDiagnosis).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMedications).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareMeasurements).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareEncounterDate).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareSymbolAndRegistry).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, datasetCheck1).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, datasetCheck2).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, datasetCheck3).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, datasetCheck4).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, datasetCheck5).get should be (true)
        
        val healthcareInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:part1 ;
                  obo:OBI_0000293 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ontologies:TURBO_0000510 ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OBI_0001929 ;
                  ontologies:TURBO_0010184 ?TURBO_0000511 ;
                  ontologies:TURBO_0010184 ?TURBO_0000512 ;
                  ontologies:TURBO_0010184 ?TURBO_0000508 ;
                  ontologies:TURBO_0010184 ?TURBO_0000509 ;
                  ontologies:TURBO_0010184 ?TURBO_0010138 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?EFO_0004340 ;
                  
                  ontologies:TURBO_0010184 ?VSO_0000006 ;
                  ontologies:TURBO_0010184 ?HTN_00000000 ;
                  ontologies:TURBO_0010184 ?HTN_00000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0010150 ;
                  ontologies:TURBO_0010184 ?TURBO_0010149 ;
                  
                  ontologies:TURBO_0010184 pmbb:hcenc1 ;
                  ontologies:TURBO_0010184 pmbb:expandedPart ;
                  
                  ontologies:TURBO_0010184 ?instantiation .
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OBI_0001929 a obo:OBI_0001929 .
                ?TURBO_0000511 a turbo:TURBO_0000511 .
                ?TURBO_0000512 a turbo:TURBO_0000512 .
                ?TURBO_0000508 a turbo:TURBO_0000508 .
                ?TURBO_0000509 a turbo:TURBO_0000509 .
                ?TURBO_0010138 a turbo:TURBO_0010138 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?EFO_0004340 a efo:EFO_0004340 .
                ?VSO_0000006 a obo:VSO_0000006 .
                ?HTN_00000000 a obo:HTN_00000000 .
                ?HTN_00000001 a obo:HTN_00000001 .
                ?TURBO_0010150 a turbo:TURBO_0010150 .
                ?TURBO_0010149 a turbo:TURBO_0010149 .
                ?instantiation a turbo:TURBO_0000522 .
            }
          }
          
          """
        
        val diagnosisInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:diagCridSC ;
                  
                  ontologies:TURBO_0010184 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                  ontologies:TURBO_0010184 <http://purl.bioontology.org/ontology/ICD9CM/401.9> ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?OGMS_0000097 ;
                  ontologies:TURBO_0010184 ?OGMS_0000073 ;
                  
                  ontologies:TURBO_0010184 pmbb:diagCridSC ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?OGMS_0000097 a obo:OGMS_0000097 .
                ?OGMS_0000073 a obo:OGMS_0000073 .
            }
          }
          
          """
        
        val medicationsInputsOutputs: String = s"""
          
          ASK 
          { 
            Graph <${Globals.processNamedGraph}>
            {
                ?process a turbo:TURBO_0010347 ;
                
                  obo:OBI_0000293 pmbb:hcenc1 ;
                  obo:OBI_0000293 pmbb:prescription ;

                  ontologies:TURBO_0010184 turbo:someDrug ;
                  
                  ontologies:TURBO_0010184 ?IAO_0000100 ;
                  ontologies:TURBO_0010184 ?PDRO_0000001 ;
                  ontologies:TURBO_0010184 ?TURBO_0000562 ;
                  ontologies:TURBO_0010184 ?TURBO_0000561 ;
                  
                  ontologies:TURBO_0010184 pmbb:prescription ;
            }
            Graph <${Globals.expandedNamedGraph}>
            {
                ?IAO_0000100 a obo:IAO_0000100 .
                ?PDRO_0000001 a obo:PDRO_0000001 .
                ?TURBO_0000562 a turbo:TURBO_0000562 .
                ?TURBO_0000561 a turbo:TURBO_0000561 .
            }
          }
          
          """
        
        SparqlUpdater.querySparqlBoolean(Globals.cxn, healthcareInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, diagnosisInputsOutputs).get should be (true)
        SparqlUpdater.querySparqlBoolean(Globals.cxn, medicationsInputsOutputs).get should be (true)
    }
    
    test("diagnosis not expanded by itself")
    {
       val insert = """
         INSERT DATA {
         Graph pmbb:Shortcuts_diagnosisShortcuts {
               pmbb:diagCridSC a turbo:TURBO_0010160 ;
                      turbo:TURBO_0004602 'ICD-9' ;
                      turbo:TURBO_0004603 <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890> ;
                      turbo:TURBO_0004601 '401.9' ;
                      turbo:TURBO_0010013 "true"^^xsd:Boolean ;
                      turbo:TURBO_0010014 "1"^^xsd:Integer . }}
         """
       SparqlUpdater.updateSparql(Globals.cxn, insert)
       
       RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/DiagnosisExpansionProcess", "none", false)
       
       val count: String = s"SELECT * WHERE {GRAPH <${Globals.expandedNamedGraph}> {?s ?p ?o .}}"
       val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, count, "s")
       result.size should be (0)
    }
    
    test("prescription not expanded by itself")
    {
        val insert = """
         INSERT DATA {
         Graph pmbb:Shortcuts_medicationShortcuts {
               pmbb:prescription a turbo:TURBO_0010159 ;
                      turbo:TURBO_0005611 "holistic soil from the ganges" ;
                      turbo:TURBO_0005612 turbo:someDrug ;
                      turbo:TURBO_0005601 "3" . }}
         """
       SparqlUpdater.updateSparql(Globals.cxn, insert)
       
       RunDrivetrainProcess.runProcess("http://www.itmat.upenn.edu/biobank/MedicationExpansionProcess", "none", false)
       
       val count: String = s"SELECT * WHERE {GRAPH <${Globals.expandedNamedGraph}> {?s ?p ?o .}}"
       val result = SparqlUpdater.querySparqlAndUnpackTuple(Globals.cxn, count, "s")
       result.size should be (0)
    }
}