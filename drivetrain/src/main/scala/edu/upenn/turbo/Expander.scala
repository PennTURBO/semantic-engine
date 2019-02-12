package edu.upenn.turbo

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.repository.RepositoryConnection
import java.util.UUID
import scala.collection.mutable.ArrayBuffer
import org.eclipse.rdf4j.model.Value

/**
 * The Expander class is responsible for all Shortcut Expansion. This is the first step in the Drivetrain process to create the fully ontologized model.
 * Shortcut data is received in specific named graphs and Expander output is loaded into the Expanded named graph.
 */

class Expander extends ProjectwideGlobals
{
    /**
     * Calls all methods responsible for expanding all shortcut entities available in shortcut named graphs. 
     */
    def expandAllShortcutEntities (cxn: RepositoryConnection, globalUUID: String, instantiation: String = helper.genPmbbIRI()): String =
    {   
        val instantiationIRI: IRI = cxn.getValueFactory.createIRI(instantiation)
        val graphsString: String = helper.generateShortcutNamedGraphsString(cxn)
        logger.info("Expanding from these named graphs: " + graphsString)
        encounterExpansion(cxn, instantiationIRI, graphsString, globalUUID)
        logger.info("finished encounter expansion")
        expandAllParticipants(cxn, instantiationIRI, graphsString, globalUUID)
        logger.info("finished participant expansion")
        biobankEncounterParticipantJoinExpansion(cxn, instantiationIRI, graphsString, globalUUID)
        logger.info("finished biobank join expansion, starting healthcare join expansion")
        healthcareEncounterParticipantJoinExpansion(cxn, instantiationIRI, graphsString, globalUUID)
        logger.info("finished join expansion")
        instantiation
    }
    
    def expandAllParticipants(cxn: RepositoryConnection, instantiation: IRI, graphString: String, globalUUID: String)
    {
        val randomUUID = UUID.randomUUID().toString.replaceAll("-", "")
        participantExpansion(cxn, instantiation, graphString, randomUUID, globalUUID)
        expandParticipantsMultipleIdentifiers(cxn, instantiation, graphString, randomUUID, globalUUID)
    }
    
    /**
     * Calls all methods responsible for expanding all shortcut entities available in encounter shortcut named graphs.
     */
    def encounterExpansion (cxn: RepositoryConnection, instantiation: IRI, graphsString: String, globalUUID: String)
    {     
        logger.info("starting hc enc expansion")
        expandHealthcareEncounterShortcuts(cxn, instantiation, graphsString, globalUUID)
        logger.info("finished hc enc expansion, starting bb enc expansion")
        expandBiobankEncounterShortcuts(cxn, instantiation, graphsString, globalUUID)
        logger.info("finished bb enc expansion")
    }
    
    /** 
     * Submits a SPARQL expansion update to the graph server which expands biobank encounter to biobank consenter entity linking shortcuts
     * to their fully ontologied form.
     */
    def biobankEncounterParticipantJoinExpansion(cxn: RepositoryConnection, instantiation: IRI, graphsString: String, globalUUID: String)
    {
        //val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val expand: String = """
          Insert
          {
              Graph pmbb:entityLinkData
              {
                  ?instantiation a turbo:TURBO_0000522 .
        		      ?instantiation obo:OBI_0000293 ?dataset .
        		      
                  ?bbEncCrid a turbo:TURBO_0000533 .
                  ?bbEncCrid obo:BFO_0000051 ?bbEncSymb .
                  ?bbEncCrid obo:BFO_0000051 ?bbEncRegDen .
                  ?bbEncSymb a turbo:TURBO_0000534 .
                  ?bbEncSymb turbo:TURBO_0006510 ?bbEncSymbLit .
                  ?bbEncRegDen a turbo:TURBO_0000535 .
                  ?bbEncRegDen turbo:TURBO_0006510 ?bbEncRegDenLit .
                  ?bbEncRegDen obo:IAO_0000219 ?bbEncRegURI .
                  ?bbEncRegURI a turbo:TURBO_0000543 .
                  ?bbEncSymb obo:BFO_0000050 ?dataset .
                  ?bbEncRegDen obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?bbEncSymb .
                  ?dataset obo:BFO_0000051 ?bbEncRegDen .
                  
                  ?partCrid a turbo:TURBO_0000503 .
                  ?partCrid obo:BFO_0000051 ?partSymb .
                  ?partCrid obo:BFO_0000051 ?partRegDen .
                  ?partSymb a turbo:TURBO_0000504 .
                  ?partSymb turbo:TURBO_0006510 ?partSymbLit .
                  ?partRegDen a turbo:TURBO_0000505 .
                  ?partRegDen turbo:TURBO_0006510 ?partRegDenLit .
                  ?partRegDen obo:IAO_0000219 ?partRegURI .
                  ?partRegURI a turbo:TURBO_0000506 .
                  ?partSymb obo:BFO_0000050 ?dataset .
                  ?partRegDen obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?partSymb .
                  ?dataset obo:BFO_0000051 ?partRegDen .
                  
                  ?partCrid turbo:TURBO_0000302 ?bbEncCrid .
                  
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title ?datasetTitle .
              }
          }
          Where
          {
              Values ?g { """ + graphsString + """ }
              Graph ?g
              {
                  ?bbEncCridSC a turbo:TURBO_0000533 .
                  ?bbEncCridSC turbo:TURBO_0001608 ?bbEncSymbLit .
                  Optional
                  {
                      ?bbEncCridSC turbo:TURBO_0001609 ?bbEncRegDenLit .
                  }
                  ?bbEncCridSC turbo:TURBO_0001610 ?bbEncRegUriLit .
                  
                  ?partCridSC a turbo:TURBO_0000503 .
                  ?partCridSC turbo:TURBO_0003608 ?partSymbLit .
                  Optional
                  {
                      ?partCridSC turbo:TURBO_0003609 ?partRegDenLit .
                  }
                  ?partCridSC turbo:TURBO_0003610 ?partRegUriLit .
                  ?partCridSC turbo:TURBO_0003603 ?datasetTitle .
                  
                  ?partCridSC turbo:TURBO_0000302 ?bbEncCridSC .
              }
              Bind(Uri(?bbEncRegUriLit) As ?bbEncRegURI)
              Bind(Uri(?partRegUriLit) As ?partRegURI)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?bbEncCrid)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?partCrid)
              BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?datasetTitle))))) AS ?dataset)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?bbEncSymb)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?bbEncRegDen)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?partSymb)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?partRegDen)
              BIND(uri("""" + instantiation + """") AS ?instantiation)
          }
          """    
        
        update.updateSparql(cxn, sparqlPrefixes + expand)
    }
    
    /**
     * Submits a SPARQL expansion update to the graph server which expands healthcare encounter to biobank consenter entity linking shortcuts
     * to their fully ontologied form.
     */
    def healthcareEncounterParticipantJoinExpansion(cxn: RepositoryConnection, instantiation: IRI, graphsString: String, globalUUID: String)
    {
        //val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val expand: String = """
          Insert
          {
              Graph pmbb:entityLinkData
              {
                  ?instantiation a turbo:TURBO_0000522 .
        		      ?instantiation obo:OBI_0000293 ?dataset .
        		      
                  ?hcEncCrid a turbo:TURBO_0000508 .
                  ?hcEncCrid obo:BFO_0000051 ?hcEncSymb .
                  ?hcEncCrid obo:BFO_0000051 ?hcEncRegDen .
                  ?hcEncSymb a turbo:TURBO_0000509 .
                  ?hcEncSymb turbo:TURBO_0006510 ?hcEncSymbLit .
                  ?hcEncRegDen a turbo:TURBO_0000510 .
                  ?hcEncRegDen turbo:TURBO_0006510 ?hcEncRegDenLit .
                  ?hcEncRegDen obo:IAO_0000219 ?hcEncRegURI .
                  ?hcEncRegURI a turbo:TURBO_0000513 .
                  ?hcEncSymb obo:BFO_0000050 ?dataset .
                  ?hcEncRegDen obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?hcEncSymb .
                  ?dataset obo:BFO_0000051 ?hcEncRegDen .
                  
                  ?partCrid a turbo:TURBO_0000503 .
                  ?partCrid obo:BFO_0000051 ?partSymb .
                  ?partCrid obo:BFO_0000051 ?partRegDen .
                  ?partSymb a turbo:TURBO_0000504 .
                  ?partSymb turbo:TURBO_0006510 ?partSymbLit .
                  ?partRegDen a turbo:TURBO_0000505 .
                  ?partRegDen turbo:TURBO_0006510 ?partRegDenLit .
                  ?partRegDen obo:IAO_0000219 ?partRegURI .
                  ?partRegURI a turbo:TURBO_0000506 .
                  ?partSymb obo:BFO_0000050 ?dataset .
                  ?partRegDen obo:BFO_0000050 ?dataset .
                  ?dataset obo:BFO_0000051 ?partSymb .
                  ?dataset obo:BFO_0000051 ?partRegDen .
                  
                  ?partCrid turbo:TURBO_0000302 ?hcEncCrid .
                  
                  ?dataset a obo:IAO_0000100 .
                  ?dataset dc11:title ?datasetTitle .
              }
          }
          Where
          {
              Values ?g { """ + graphsString + """ }
              Graph ?g
              {
                  ?hcEncCridSC a turbo:TURBO_0000508 .
                  ?hcEncCridSC turbo:TURBO_0002608 ?hcEncSymbLit .
                  ?hcEncCridSC turbo:TURBO_0002610 ?hcEncRegUriLit .
                  Optional
                  {
                      ?hcEncCridSC turbo:TURBO_0002609 ?hcEncRegDenLit .
                  }
                  ?partCridSC a turbo:TURBO_0000503 .
                  ?partCridSC turbo:TURBO_0003608 ?partSymbLit .
                  Optional
                  {
                      ?partCridSC turbo:TURBO_0003609 ?partRegDenLit .
                  }
                  ?partCridSC turbo:TURBO_0003610 ?partRegUriLit .
                  ?partCridSC turbo:TURBO_0003603 ?datasetTitle .
                  
                  ?partCridSC turbo:TURBO_0000302 ?hcEncCridSC .
              }
              Bind(Uri(?hcEncRegUriLit) As ?hcEncRegURI)
              Bind(Uri(?partRegUriLit) As ?partRegURI)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?hcEncCrid)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?partCrid)
              BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?datasetTitle))))) AS ?dataset)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?hcEncSymb)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?hcEncRegDen)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?partSymb)
              Bind(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?partRegDen)
              BIND(uri("""" + instantiation + """") AS ?instantiation)
          }
          """    
        
        update.updateSparql(cxn, sparqlPrefixes + expand)
    }
    
    /**
     * Submits a SPARQL expansion update to the graph server which expands biobank consenter shortcuts to their fully ontologied form. This method is used for deprecated
     * shortcut triples which we still are supporting. This shortcut model does not include a CRID and therefore supports only one identifier per consenter.
     */
    def participantExpansion (cxn: RepositoryConnection, instantiation: IRI, graphsString: String, randomUUID: String, globalUUID: String)
    {
        logger.info("running part expansion")
        val participantExpansion = """
        INSERT {
        	GRAPH pmbb:postExpansionCheck {
        	
        		# create data instantiation process
            ?instantiation obo:OBI_0000293 ?dataset .
            ?instantiation rdf:type turbo:TURBO_0000522 .
            
            # connect data to dataset
            ?dataset rdf:type obo:IAO_0000100 .
            ?dataset dc11:title ?datasetTitle .
            ?genderIdentityDatum obo:BFO_0000050 ?genderDataset .
            ?genderDataset obo:BFO_0000051 ?genderIdentityDatum .
            ?dateOfBirth obo:BFO_0000050 ?dateDataset .
            ?dateDataset obo:BFO_0000051 ?dateOfBirth .
            ?consenterRegistryDenoter obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?consenterRegistryDenoter .
            ?consenterSymbol obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?consenterSymbol .
            ?raceIdentityDatum obo:BFO_0000050 ?raceDataset .
            ?raceDataset obo:BFO_0000051 ?raceIdentityDatum .
            
            # properties of consenter
            ?consenter obo:RO_0000086 ?biosex .
            ?consenter turbo:TURBO_0000303 ?birth .
            ?consenter obo:BFO_0000051 ?adipose ;
                         obo:RO_0000086 ?height ;
                         obo:RO_0000086 ?weight .
            ?consenter rdf:type turbo:TURBO_0000502 .
            ?adipose obo:BFO_0000050 ?consenter .
            
            # stores the previous URI value of the consenter
            ?consenter turbo:TURBO_0006601 ?previousUriText .
                         
            # properties of consenter CRID
            ?consenterCrid obo:IAO_0000219 ?consenter .
            ?consenterCrid rdf:type turbo:TURBO_0000503 .
            ?consenterCrid obo:BFO_0000051 ?consenterRegistryDenoter .
            ?consenterCrid obo:BFO_0000051 ?consenterSymbol .
            
            # properties of consenter Symbol
            ?consenterSymbol obo:BFO_0000050 ?consenterCrid .
            ?consenterSymbol turbo:TURBO_0006510 ?consenterSymbolValue .
            ?consenterSymbol rdf:type turbo:TURBO_0000504 .
            
            # properties of consenter Registry Denoter
            ?consenterRegistryDenoter obo:BFO_0000050 ?consenterCrid .
            ?consenterRegistryDenoter turbo:TURBO_0006510 ?registryDenoterString .
            ?consenterRegistryDenoter rdf:type turbo:TURBO_0000505 .
            ?consenterRegistryDenoter obo:IAO_0000219 ?consenterRegistry .
            ?consenterRegistry rdf:type turbo:TURBO_0000506 .
            
            # properties of Gender Identity Datum
            ?genderIdentityDatum turbo:TURBO_0006510 ?genderIdentityDatumValue .
            ?genderIdentityDatum rdf:type ?genderIdentityDatumType .
            ?genderIdentityDatum obo:IAO_0000136 ?consenter .
            
            # properties of Date of Birth
            ?dateOfBirth rdf:type efo:EFO_0004950 .
            ?dateOfBirth turbo:TURBO_0006510 ?dateOfBirthStringValue .
            ?dateOfBirth turbo:TURBO_0006511 ?dateOfBirthDateValue .
            ?dateOfBirth obo:IAO_0000136 ?birth .
            
            # properties of Race Identity Datum
            ?raceIdentityProcess a obo:OMRSE_00000099 .
            ?raceIdentityProcess obo:OBI_0000299 ?raceIdentityDatum .
            ?raceIdentityDatum a ?ridType .
            ?raceIdentityDatum obo:IAO_0000136 ?consenter .
            ?raceIdentityDatum turbo:TURBO_0006512 ?ridString .
            
            # type declarations for Consenter properties
            ?biosex rdf:type obo:PATO_0000047 .
            ?birth rdf:type obo:UBERON_0035946 .
            ?adipose rdf:type obo:UBERON_0001013 .
            ?height rdf:type obo:PATO_0000119 .
            ?weight rdf:type obo:PATO_0000128 .
        	}
        }
        WHERE {
        	Values ?g { """ + graphsString + """ }
          Graph ?g
        	# bind each one of these literal values to an individual above
        	{
        		?shortcutPart  rdf:type     :TURBO_0000502 ;
        					  :TURBO_0000603  ?datasetTitle ;
        					  :TURBO_0000608      ?consenterSymbolValue ;
        					  :TURBO_0000610 ?consenterRegistryString .
                Optional
                {
                    ?shortcutPart :TURBO_0000609 ?registryDenoterString ;
                }
        		BIND(str(?shortcutPart) AS ?previousUriText)
        		OPTIONAL
        		{
        			?shortcutPart  :TURBO_0000604  ?dateOfBirthStringValue .
        		}
        		OPTIONAL
        		{
        		    ?shortcutPart :TURBO_0000605   ?dateOfBirthDateValue
        		}
        		OPTIONAL
        		{
        			?shortcutPart  :TURBO_0000606  ?genderIdentityDatumValue .
        			BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("gid", """" + randomUUID + """", str(?shortcutPart))))) AS ?genderIdentityDatum)
        		}
        		OPTIONAL
        		{
        			?shortcutPart :TURBO_0000607   ?gidString .

        		}
        		OPTIONAL
        		{
        		  ?shortcutPart turbo:TURBO_0000614 ?ridTypeString .
        		  BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("rid", """" + randomUUID + """", str(?shortcutPart))))) AS ?raceIdentityDatum)
        		  BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("rip", """" + randomUUID + """", str(?shortcutPart))))) AS ?raceIdentityProcess)
        		}
        		OPTIONAL
        		{
        		  ?shortcutPart turbo:TURBO_0000615 ?ridString .
        		}
        		# what IRIs should be held constant for all of this expansion process, across all of the  shortcut patterns?
        		# dataset "iao_0000100", instantiation process "r2rinstantiation", instantiation output container "container"
        		# can be created as UUIDs outside of sparql
        		# for referent tracking and conclusionating, create URIs based on MD5s of the input variables
        		# probably won't work with more complex ref-tracking/conclusionating algorithms
        		#
        		# for each of these bindings, create a type assertion in the construct/insert block
        		BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?datasetTitle))))) AS ?dataset)
        		BIND(uri("""" + instantiation + """") AS ?instantiation)
        		#
        		BIND(uri(?consenterRegistryString) AS ?consenterRegistry)
        		BIND(uri(?gidString) AS ?gidType_1)
        		BIND(uri(?ridTypeString) AS ?ridType)
        		BIND (IF (BOUND(?gidType_1), ?gidType_1, obo:OMRSE_00000133) AS ?genderIdentityDatumType)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("consenter", """" + globalUUID + """", str(?shortcutPart))))) AS ?consenter)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?consenterCrid)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?consenterRegistryDenoter)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?consenterSymbol)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("biosex", """" + randomUUID + """", str(?shortcutPart))))) AS ?biosex)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("birth", """" + randomUUID + """", str(?shortcutPart))))) AS ?birth)
        		# We are currently creating dob datum even if no dob data exists...is this a good idea? So we can make statements about it in conclusionation
        		# BIND(IF(bound(?dobTextVal), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?dateOfBirth)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dob", """" + randomUUID + """", str(?shortcutPart))))) AS ?dateOfBirth)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("adipose", """" + randomUUID + """", str(?shortcutPart))))) AS ?adipose)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("height", """" + randomUUID + """", str(?shortcutPart))))) AS ?height)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("weight", """" + randomUUID + """", str(?shortcutPart))))) AS ?weight)
        		BIND(IF (BOUND(?dateOfBirthStringValue), ?dataset, ?unbound) AS ?dateDataset)
        		BIND(IF (BOUND(?ridTypeString), ?dataset, ?unbound) AS ?raceDataset)
        		BIND(IF (BOUND(?genderIdentityDatumValue), ?dataset, ?unbound) AS ?genderDataset)
        	}
        }          
        """
                
        update.updateSparql(cxn, sparqlPrefixes + participantExpansion)
    }
    
    /**
     * Submits a SPARQL expansion update to the graph server which expands biobank consenter shortcuts to their fully ontologied form. This method uses the latest
     * consenter shortcut properties including potentially multiple CRIDs, which allows for multiple identifiers per consenter.
     */
    def expandParticipantsMultipleIdentifiers(cxn: RepositoryConnection, instantiation: IRI, graphsString: String, randomUUID: String, globalUUID: String)
    {
        val participantExpansion = """
        INSERT {
        	GRAPH pmbb:postExpansionCheck {
        	
        		# create data instantiation process
            ?instantiation obo:OBI_0000293 ?dataset .
            ?instantiation a turbo:TURBO_0000522 .
            
            # connect data to dataset
            ?dataset a obo:IAO_0000100 .
            ?dataset dc11:title ?datasetTitle .
            ?genderIdentityDatum obo:BFO_0000050 ?genderDataset .
            ?genderDataset obo:BFO_0000051 ?genderIdentityDatum .
            ?dateOfBirth obo:BFO_0000050 ?dateDataset .
            ?dateDataset obo:BFO_0000051 ?dateOfBirth .
            ?consenterRegistryDenoter obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?consenterRegistryDenoter .
            ?consenterSymbol obo:BFO_0000050 ?dataset .
            ?dataset obo:BFO_0000051 ?consenterSymbol .
            ?raceIdentityDatum obo:BFO_0000050 ?raceDataset .
            ?raceDataset obo:BFO_0000051 ?raceIdentityDatum .
            
            # properties of consenter
            ?consenter obo:RO_0000086 ?biosex .
            ?consenter turbo:TURBO_0000303 ?birth .
            ?consenter obo:BFO_0000051 ?adipose ;
                         obo:RO_0000086 ?height ;
                         obo:RO_0000086 ?weight .
            ?consenter a turbo:TURBO_0000502 .
            ?adipose obo:BFO_0000050 ?consenter .
            
            # stores the previous URI value of the consenter
            ?consenter turbo:TURBO_0006601 ?previousUriText .
                         
            # properties of consenter CRID
            ?consenterCrid obo:IAO_0000219 ?consenter .
            ?consenterCrid a turbo:TURBO_0000503 .
            ?consenterCrid obo:BFO_0000051 ?consenterRegistryDenoter .
            ?consenterCrid obo:BFO_0000051 ?consenterSymbol .
            
            # properties of consenter Symbol
            ?consenterSymbol obo:BFO_0000050 ?consenterCrid .
            ?consenterSymbol turbo:TURBO_0006510 ?consenterSymbolValue .
            ?consenterSymbol a turbo:TURBO_0000504 .
            
            # properties of consenter Registry Denoter
            ?consenterRegistryDenoter obo:BFO_0000050 ?consenterCrid .
            ?consenterRegistryDenoter turbo:TURBO_0006510 ?registryDenoterString .
            ?consenterRegistryDenoter a turbo:TURBO_0000505 .
            ?consenterRegistryDenoter obo:IAO_0000219 ?consenterRegistry .
            ?consenterRegistry a turbo:TURBO_0000506 .
            
            # properties of Gender Identity Datum
            ?genderIdentityDatum turbo:TURBO_0006510 ?genderIdentityDatumValue .
            ?genderIdentityDatum a ?genderIdentityDatumType .
            ?genderIdentityDatum obo:IAO_0000136 ?consenter .
            
            # properties of Date of Birth
            ?dateOfBirth a efo:EFO_0004950 .
            ?dateOfBirth turbo:TURBO_0006510 ?dateOfBirthStringValue .
            ?dateOfBirth turbo:TURBO_0006511 ?dateOfBirthDateValue .
            ?dateOfBirth obo:IAO_0000136 ?birth .
            
            # properties of Race Identity Datum
            ?raceIdentityProcess a obo:OMRSE_00000099 .
            ?raceIdentityProcess obo:OBI_0000299 ?raceIdentityDatum .
            ?raceIdentityDatum a ?ridType .
            ?raceIdentityDatum obo:IAO_0000136 ?consenter .
            ?raceIdentityDatum turbo:TURBO_0006512 ?ridString .
            
            # type declarations for Consenter properties
            ?biosex a obo:PATO_0000047 .
            ?birth a obo:UBERON_0035946 .
            ?adipose a obo:UBERON_0001013 .
            ?height a obo:PATO_0000119 .
            ?weight a obo:PATO_0000128 .
        	}
        }
        WHERE {
        	Values ?g { """ + graphsString + """ }
          Graph ?g
        	# bind each one of these literal values to an individual above
        	{
        		?shortcutPart a turbo:TURBO_0000502 .
        	  ?shortcutCrid a turbo:TURBO_0000503 .
        	  ?shortcutCrid obo:IAO_0000219 ?shortcutPart .
        	  ?shortcutCrid turbo:TURBO_0003603 ?datasetTitle .
        	  ?shortcutCrid turbo:TURBO_0003610 ?consenterRegistryString .
        	  ?shortcutCrid turbo:TURBO_0003608 ?consenterSymbolValue .
        	  
        		BIND(str(?shortcutPart) AS ?previousUriText)
        		OPTIONAL
        		{
        			?shortcutPart  :TURBO_0000604  ?dateOfBirthStringValue .
        		}
        		OPTIONAL
        		{
        		    ?shortcutPart :TURBO_0000605   ?dateOfBirthDateValue
        		}
        		OPTIONAL
        		{
        			?shortcutPart  :TURBO_0000606  ?genderIdentityDatumValue .
        			BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("gid", """" + randomUUID + """", str(?shortcutPart))))) AS ?genderIdentityDatum)
        		}
        		OPTIONAL
        		{
        			?shortcutPart :TURBO_0000607   ?gidString .
        		}
        		OPTIONAL
        		{
        		  ?shortcutPart turbo:TURBO_0000614 ?ridTypeString .
        		  BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("rid", """" + randomUUID + """", str(?shortcutPart))))) AS ?raceIdentityDatum)
        		  BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("rip", """" + randomUUID + """", str(?shortcutPart))))) AS ?raceIdentityProcess)
        		}
        		OPTIONAL
        		{
        		  ?shortcutPart turbo:TURBO_0000615 ?ridString .
        		}
        		BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?datasetTitle))))) AS ?dataset)
        		BIND(uri("""" + instantiation + """") AS ?instantiation)
        		#
        		BIND(uri(?consenterRegistryString) AS ?consenterRegistry)
        		BIND(uri(?gidString) AS ?gidType_1)
        		BIND(uri(?ridTypeString) AS ?ridType)
        		BIND (IF (BOUND(?gidType_1), ?gidType_1, obo:OMRSE_00000133) AS ?genderIdentityDatumType)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("consenter", """" + globalUUID + """", str(?shortcutPart))))) AS ?consenter)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?consenterCrid)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?consenterRegistryDenoter)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?consenterSymbol)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("biosex", """" + randomUUID + """", str(?shortcutPart))))) AS ?biosex)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("birth", """" + randomUUID + """", str(?shortcutPart))))) AS ?birth)
        		# We are currently creating dob datum even if no dob data exists...is this a good idea? So we can make statements about it in conclusionation
        		# BIND(IF(bound(?dobTextVal), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?dateOfBirth)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dob", """" + randomUUID + """", str(?shortcutPart))))) AS ?dateOfBirth)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("adipose", """" + randomUUID + """", str(?shortcutPart))))) AS ?adipose)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("height", """" + randomUUID + """", str(?shortcutPart))))) AS ?height)
        		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("weight", """" + randomUUID + """", str(?shortcutPart))))) AS ?weight)
        		BIND(IF (BOUND(?dateOfBirthStringValue), ?dataset, ?unbound) AS ?dateDataset)
        		BIND(IF (BOUND(?ridTypeString), ?dataset, ?unbound) AS ?raceDataset)
        		BIND(IF (BOUND(?genderIdentityDatumValue), ?dataset, ?unbound) AS ?genderDataset)
        	}
        }          
        """
        update.updateSparql(cxn, sparqlPrefixes + participantExpansion)
    }
    
    /**
     * Submits a SPARQL expansion update to the graph server which expands biobank encounter shortcuts to their fully ontologied form.
     */
    def expandBiobankEncounterShortcuts(cxn: RepositoryConnection, instantiation: IRI, graphsString: String, globalUUID: String)
    {
        /*val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        val biobankEncounterExpansion = """
          insert {
        	GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
        	    
        	  ?instantiation a turbo:TURBO_0000522 .
        		?instantiation obo:OBI_0000293 ?dataset .
        		
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title ?dsTitle .
        		?dataset obo:BFO_0000051 ?weightDatum .
        		?weightDatum obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?heightDatum .
        		?heightDatum obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?BMI .
        		?BMI obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?bbEncRegDen .
        		?bbEncRegDen obo:BFO_0000050 ?dataset .
        		?encounterDate obo:BFO_0000050 ?dateDataset .
        		?dateDataset obo:BFO_0000051 ?encounterDate .
        		?bbEncSymb obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?bbEncSymb .
        	    
        		?encounter a turbo:TURBO_0000527 .
        		?encounter turbo:TURBO_0006601 ?previousUriText .
        		?encounter obo:OBI_0000299 ?BMI .
        		?encounter obo:BFO_0000051 ?heightAssay .
        		?heightAssay obo:BFO_0000050 ?encounter .
        		?encounter obo:BFO_0000051 ?weightAssay .
        		?weightAssay obo:BFO_0000050 ?encounter .

        		?encounterCrid a turbo:TURBO_0000533 .
        		?encounterCrid obo:IAO_0000219 ?encounter .
        		?encounterCrid obo:BFO_0000051 ?bbEncSymb .
        		?encounterCrid obo:BFO_0000051 ?bbEncRegDen .
        		
         		?bbEncSymb obo:BFO_0000050 ?encounterCrid .
         		?bbEncSymb turbo:TURBO_0006510 ?EncID_LV .
         		?bbEncSymb a turbo:TURBO_0000534 .
        		
        		?bbEncRegDen obo:BFO_0000050 ?encounterCrid .
        		?bbEncRegDen turbo:TURBO_0006510 ?encRegDenText .
        		?bbEncRegDen a turbo:TURBO_0000535 .
        		?bbEncRegDen obo:IAO_0000219 ?bbEncRegId .
        		?bbEncRegId a turbo:TURBO_0000543 .

        		?encStart a turbo:TURBO_0000531 .
        		?encStart obo:RO_0002223 ?encounter .

        		?encounterDate a turbo:TURBO_0000532 .
        		?encounterDate turbo:TURBO_0006512 ?encDateTextVal .
        		?encounterDate turbo:TURBO_0006511 ?encDateMeasVal .
        		?encounterDate obo:IAO_0000136 ?encStart .
        		
        		?BMI a efo:EFO_0004340 .
        		?BMI obo:OBI_0001938 ?BMIvalspec .
        		?BMIvalspec a obo:OBI_0001933 .
        		?BMIvalspec obo:OBI_0002135 ?BMILit .
        		?BMI obo:IAO_0000581 ?encounterDate .

        		?heightValSpec a obo:OBI_0001931 ;
        		               obo:IAO_0000039 obo:UO_0000015 ;
        		               obo:OBI_0002135 ?heightCM .
        		               
      	    ?heightAssay a turbo:TURBO_0001511 ;
      	                 obo:OBI_0000299 ?heightDatum .

      	    ?heightDatum a obo:IAO_0000408 ;
      	                 obo:OBI_0001938 ?heightValSpec .

      	    ?weightAssay a obo:OBI_0000445 ;
      	                 obo:OBI_0000299 ?weightDatum .

      	    ?weightDatum a obo:IAO_0000414 ;
      	                 obo:OBI_0001938 ?weightValSpec .

      	    ?weightValSpec a obo:OBI_0001931 ;
      	                   obo:IAO_0000039 obo:UO_0000009 ;
      	                   obo:OBI_0002135 ?weightKG .
        	}
        }
            WHERE
            {
            	Values ?g { """ + graphsString + """ }
              Graph ?g
            	{
            		?encFromKarma
            			a                     turbo:TURBO_0000527 ;
            			turbo:TURBO_0000623   ?dsTitle;
            			turbo:TURBO_0000628     ?EncID_LV ;
            			turbo:TURBO_0000630 ?bbEncRegIdString .
            	    optional {
            	        ?encFromKarma turbo:TURBO_0000629 ?encRegDenText .
            	    }
            		BIND(str(?encFromKarma) AS ?previousUriText)
            		optional {
            			?encFromKarma turbo:TURBO_0000635  ?BMILit .
            	    }
            		optional {
            		    ?encFromKarma turbo:TURBO_0000627  ?weightKG .
            		}
            		optional {
            		    ?encFromKarma turbo:TURBO_0000626  ?heightCM .
            		}
            		optional {
            			?encFromKarma         turbo:TURBO_0000624  ?encDateTextVal .
            	    }
            		optional {
            			?encFromKarma         turbo:TURBO_0000625  ?encDateMeasVal .
            		}
            		BIND(uri(?bbEncRegIdString) AS ?bbEncRegId)
            		BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?dsTitle))))) AS ?dataset)
            		BIND(uri("""" + instantiation + """") AS ?instantiation)
            		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("bb encounter", """" + globalUUID + """", str(?encFromKarma))))) AS ?encounter)
            		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?encounterCrid)
            		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?encounterDate)
          	    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?encStart)
          	    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?bbEncRegDen)
          	    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?bbEncSymb)
          	    BIND(IF (BOUND(?BMILit), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?BMI)
          	    BIND(IF (BOUND(?BMI), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?BMIvalspec)
          	    BIND(IF (BOUND(?heightCM), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?heightValSpec)
          	    BIND(IF (BOUND(?heightCM), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?heightAssay)
          	    BIND(IF (BOUND(?heightCM), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?heightDatum)
          	    BIND(IF (BOUND(?weightKG), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?weightValSpec)
          	    BIND(IF (BOUND(?weightKG), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?weightAssay)
          	    BIND(IF (BOUND(?weightKG), uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))), ?unbound) AS ?weightDatum)
            	  BIND(IF (BOUND(?encDateTextVal), ?dataset, ?unbound) AS ?dateDataset)
            	}
            }"""
                
          update.updateSparql(cxn, sparqlPrefixes + biobankEncounterExpansion)  */
          DrivetrainDriver.main(Array("buildQuery"))
    }
    
    /**
     * Submits a SPARQL expansion update to the graph server which expands healthcare consenter shortcuts to their fully ontologized form.
     */
    def expandHealthcareEncounterShortcuts(cxn: RepositoryConnection, instantiation: IRI, graphsString: String, globalUUID: String)
    {
        //collect list of all healthcare encounters
        logger.info("running hc enc expansion")
        val getShortcutHcEncs: String = """
          select * where
          {
              Values ?g { """ + graphsString + """ }
              Graph ?g
            	{
            	    ?enc a obo:OGMS_0000097 .
            	}
          }
          """
        val allScHcEncs: ArrayBuffer[String] = update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getShortcutHcEncs, "enc")
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        
        //batch healthcare encounter expansion
        var encounterList: String = ""
        var count = 1
        val hcEncSize = allScHcEncs.size
        for (encounter <- allScHcEncs)
        {
            encounterList += "<" + encounter + ">"
            count = count + 1
            if (count % 1000 == 0)
            {
                logger.info("expanded " + count + " out of " + hcEncSize + " healthcare encounters")
                runBatchedHealthcareEncounterExpansion(cxn, randomUUID, globalUUID, graphsString, encounterList, instantiation)
                encounterList = ""
            }
        }
        runBatchedHealthcareEncounterExpansion(cxn, randomUUID, globalUUID, graphsString, encounterList, instantiation)
    }
    
    def expandLossOfFunctionShortcuts(cxn: RepositoryConnection, instantiation: String, lofGraphs: ArrayBuffer[String], globalUUID: String)
    {
        val randomUUID = UUID.randomUUID().toString().replaceAll("-", "")
        for (graph <- lofGraphs)
        {
            val expandLOF: String = """
              Delete
              {
                  Graph <"""+graph+""">
                  {
                      ?alleleSC a obo:OBI_0001352 ;
                	            turbo:TURBO_0007607 ?zygosityValURI ;
                	            turbo:TURBO_0007601 ?bbEncSymb ;
                	            turbo:TURBO_0007602 ?genomeCridSymbLit ;
                	            turbo:TURBO_0007603 ?genomeReg ;
                	            turbo:TURBO_0007605 ?geneText ;
                	            turbo:TURBO_0007608 ?datasetTitle ;
                	            turbo:TURBO_0007609 ?bbEncReg ;
                	            turbo:TURBO_0007610 ?geneTerm ;
                	            turbo:TURBO_0007604 ?unnecessary .
                	            
                	    ?alleleSC graphBuilder:willBeLinkedWith ?bbEnc .
                	   
                  }
                  Graph pmbb:errorLogging
                  {
                      ?alleleSC graphBuilder:reasonNotExpanded graphBuilder:noMatchFound .
                  }
              }
              # Working on Graph """ + graph + """
              Insert
              {
                  Graph pmbb:expanded
                  {
                      ?instantiation a turbo:TURBO_0000522 .
            		      ?instantiation obo:OBI_0000293 ?dataset .
            		      
                      ?dataset dc11:title ?datasetTitle .
                      ?dataset a obo:IAO_0000100 .
                      
                      # connections to dataset
                      ?allele obo:BFO_0000050 ?dataset .
                      ?dataset obo:BFO_0000051 ?allele .
                      ?genomeCridSymb obo:BFO_0000050 ?dataset .
                      ?dataset obo:BFO_0000051 ?genomeCridSymb .
                      
                      ?allele a obo:OBI_0001352 .
                      ?allele obo:OBI_0001938 ?zygVal .
                      ?allele obo:IAO_0000136 ?DNA .
                      ?allele turbo:TURBO_0006512 ?geneText .
                      ?allele turbo:TURBO_0000305 ?geneTermURI .
                      
                      ?DNAextract a obo:OBI_0001051 .
                      
                      ?formProcess a obo:OBI_0200000 .
                      ?formProcess obo:OBI_0000299 ?allele .
                      ?formProcess obo:OBI_0000293 ?sequenceData .
                      
                      ?genomeCridSymb turbo:TURBO_0006510 ?genomeCridSymbLit .
                      ?genomeCridSymb a turbo:TURBO_0000568 .
                      ?genomeCridSymb obo:BFO_0000050 ?genomeCrid .
                      
                      ?genomeRegDen obo:BFO_0000050 ?genomeCrid .
                      ?genomeRegDen a turbo:TURBO_0000567 .
                      ?genomeRegDen obo:IAO_0000219 ?genomeRegURI .
                      
                      ?genomeCrid a turbo:TURBO_0000566 .
                      ?genomeCrid obo:IAO_0000219 ?specimen .
                      ?genomeCrid obo:BFO_0000051 ?genomeRegDen .
                      ?genomeCrid obo:BFO_0000051 ?genomeCridSymb .
                      
                      ?DNAextractionProcess a obo:OBI_0000257 .
                      ?DNAextractionProcess obo:OBI_0000299 ?DNAextract .
                      ?DNAextractionProcess obo:OBI_0000293 ?specimen .
                      
                      ?zygVal a turbo:TURBO_0000571 .
                      
                      ?specimen a obo:OBI_0001479 .
                      
                      ?DNA a obo:OBI_0001868 .
                      ?DNA obo:OBI_0000643 ?DNAextract .
                      
                      ?collectionProcess a obo:OBI_0600005 .
                      ?collectionProcess obo:OBI_0000299 ?specimen .
                      
                      ?exomeSequenceProcess a obo:OBI_0002118 .
                      ?exomeSequenceProcess obo:OBI_0000293 ?DNAextract .
                      ?exomeSequenceProcess obo:OBI_0000299 ?sequenceData .
                      
                      ?sequenceData a obo:OBI_0001573 .
                      
                      # connections to bb encounter and consenter
                      ?DNA obo:OGG_0000000014 ?consenter .
                      ?collectionProcess obo:OBI_0000293 ?consenter .
                      ?bbSymb obo:BFO_0000050 ?dataset .
                      ?dataset obo:BFO_0000051 ?bbSymb .
                      ?collectionProcess obo:BFO_0000050 ?bbEnc .
                      ?bbEnc obo:BFO_0000051 ?collectionProcess .
                  }
              }
              Where
              {
                  Graph <"""+graph+""">
                	{
                	    ?alleleSC a obo:OBI_0001352 ;
                	            turbo:TURBO_0007607 ?zygosityValURI ;
                	            turbo:TURBO_0007601 ?bbEncSymb ;
                	            turbo:TURBO_0007602 ?genomeCridSymbLit ;
                	            turbo:TURBO_0007603 ?genomeReg ;
                	            turbo:TURBO_0007605 ?geneText ;
                	            turbo:TURBO_0007608 ?datasetTitle ;
                	            turbo:TURBO_0007609 ?bbEncReg .
                	            # this shortcut becoming optional on 11/2/18 because our files don't contain this shortcut
                	            optional
                	            {
                	                ?alleleSC turbo:TURBO_0007610 ?geneTerm .
                	            }
                	            # this shortcut is deprecated but may still appear and should be deleted
                	            optional
                	            {
                	                ?alleleSC turbo:TURBO_0007604 ?unnecessary .
                	            }
                	            
                	    ?alleleSC graphBuilder:willBeLinkedWith ?bbEnc .
                	}
                	Graph pmbb:expanded 
                	{
                	    ?bbEnc a turbo:TURBO_0000527 .
                	    ?bbEncCrid a turbo:TURBO_0000533 .
                	    ?bbEncCrid obo:IAO_0000219 ?bbEnc .
                	    ?bbEncCrid obo:BFO_0000051 ?bbSymb .
                	    ?bbSymb a turbo:TURBO_0000534 .
                	    ?consenter obo:RO_0000056 ?bbEnc .
                	    ?consenter a turbo:TURBO_0000502 .
                	}    
                	    
                	BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?datasetTitle))))) AS ?dataset)
                	Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?allele)
                	Bind (uri(?zygosityValURI) AS ?zygVal) 
                	Bind (uri(?genomeReg) AS ?genomeRegURI)  
                	Bind (uri(?protein) AS ?proteinURI)     
                	Bind (uri(?geneTerm) AS ?geneTermURI) 
                	Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("DNAextract", str(?consenter), str(?genomeCridSymbLit))))) AS ?DNAextract)
                	Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("formprocess", str(?consenter), str(?genomeCridSymbLit))))) AS ?formProcess)
                	Bind (uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("genomeCridSymb", str(?consenter), str(?genomeCridSymbLit))))) AS ?genomeCridSymb)
                	Bind (uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("DNA", str(?consenter))))) AS ?DNA)
                	Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("DNAextractionprocess", str(?consenter), str(?genomeCridSymbLit))))) AS ?DNAextractionProcess)
                	Bind (uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("genomeRegDen", str(?consenter), str(?genomeCridSymbLit))))) AS ?genomeRegDen)
                	Bind (uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("genomeCrid", str(?consenter), str(?genomeCridSymbLit))))) AS ?genomeCrid)
                	Bind (uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("specimen", str(?consenter), str(?genomeCridSymbLit))))) AS ?specimen)
                	Bind (uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("collProc", str(?consenter), str(?genomeCridSymbLit))))) AS ?collectionProcess)
                	Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("sequencedata", str(?consenter), str(?genomeCridSymbLit))))) AS ?sequenceData)
                	Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("exomesequence", str(?consenter), str(?genomeCridSymbLit))))) AS ?exomeSequenceProcess)
                	Bind (uri("""" + instantiation + """") AS ?instantiation)
              }
              """
            
            update.updateSparql(cxn, sparqlPrefixes + expandLOF)  
        }
    }
    
    def runBatchedHealthcareEncounterExpansion(cxn: RepositoryConnection, randomUUID: String, globalUUID: String, graphsString: String, encounterList: String, instantiation: IRI)
    {
        val healthcareEncounterExpansion = """
          insert {
        	GRAPH <http://www.itmat.upenn.edu/biobank/postExpansionCheck> {
        	
        	  ?instantiation a turbo:TURBO_0000522 .
        		?instantiation obo:OBI_0000293 ?dataset .
        	
        		?encounter a obo:OGMS_0000097 .
        		?encounter turbo:TURBO_0006601 ?previousUriText .
        		?encounter obo:RO_0002234 ?diagnosis .
        		?encounter obo:RO_0002234 ?BMI .
        		?encounter obo:RO_0002234 ?drugPrescript .
        		?encounter obo:BFO_0000051 ?weightAssay .
        		?weightAssay obo:BFO_0000050 ?encounter .
        		?encounter obo:BFO_0000051 ?heightAssay .
        		?heightAssay obo:BFO_0000050 ?encounter .
        		
        		?dataset a obo:IAO_0000100 .
        		?dataset dc11:title ?dsTitle1 .
        		?cridSymbDataset obo:BFO_0000051 ?hcEncSymb .
        		?hcEncSymb obo:BFO_0000050 ?cridSymbDataset .
        		?dataset obo:BFO_0000051 ?weightDatum .
        		?weightDatum obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?heightDatum .
        		?heightDatum obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?BMI .
        		?BMI obo:BFO_0000050 ?dataset .
        		?regDenDataset obo:BFO_0000051 ?hcEncRegDen .
        		?hcEncRegDen obo:BFO_0000050 ?regDenDataset .
        		?dataset obo:BFO_0000051 ?diagnosis .
        		?diagnosis obo:BFO_0000050 ?dataset .
        		?dateDataset obo:BFO_0000051 ?encounterDate .
        		?encounterDate obo:BFO_0000050 ?dateDataset .
        		?dataset obo:BFO_0000051 ?medSymb .
        		?medSymb obo:BFO_0000050 ?dataset .
        		?dataset obo:BFO_0000051 ?drugPrescript .
        		?drugPrescript obo:BFO_0000050 ?dataset .
        		
        		?encounterCrid a turbo:TURBO_0000508 .
        		?encounterCrid obo:IAO_0000219 ?encounter .
        		?encounterCrid obo:BFO_0000051 ?hcEncSymb .
        		?encounterCrid obo:BFO_0000051 ?hcEncRegDen .
        		
        		?hcEncSymb obo:BFO_0000050 ?encounterCrid .
        		?hcEncSymb a turbo:TURBO_0000509 .
        		?hcEncSymb turbo:TURBO_0006510 ?EncID_LV .
        		
        		?hcEncRegDen obo:BFO_0000050 ?encounterCrid .
        		?hcEncRegDen a turbo:TURBO_0000510 .
        		# the below triple is not created, as we do not expect string representation of the hc enc registry
        		# ?hcEncRegDen turbo:TURBO_0006510 ?hcRegIdStringVal .
        		?hcEncRegDen obo:IAO_0000219 ?hcEncRegId .
        		?hcEncRegId a turbo:TURBO_0000513  .
        		            
        		?encStart a turbo:TURBO_0000511 .
        		?encStart obo:RO_0002223 ?encounter .
        		            
        		?encounterDate a turbo:TURBO_0000512 .
        		?encounterDate turbo:TURBO_0006512 ?encDateTextVal .
        		?encounterDate turbo:TURBO_0006511 ?encDateMeasVal .
        		?encounterDate obo:IAO_0000136 ?encStart .
        		
        		?diagnosis a obo:OGMS_0000073 .
        		?diagnosis turbo:TURBO_0000306 ?concatIcdTerm .
        		?diagnosis turbo:TURBO_0000703 ?diagCodeRegURI .
        		?diagnosis turbo:TURBO_0006515 ?diagCodeRegTextVal .
        		?diagnosis turbo:TURBO_0006512 ?diagCodeLV .
        		?diagnosis turbo:TURBO_0010013 ?primaryDiag .
        		?diagnosis turbo:TURBO_0010014 ?diagSequence .

        		?BMI a efo:EFO_0004340 .
        		?BMI obo:OBI_0001938 ?BMIvalspec .
        		?BMIvalspec a obo:OBI_0001933 .
        		?BMIvalspec obo:OBI_0002135 ?BMILit .
        		?BMI obo:IAO_0000581 ?encounterDate .
        		
        		?heightValSpec rdf:type obo:OBI_0001931 ;
        		               obo:IAO_0000039 obo:UO_0000015 ;
        		               obo:OBI_0002135 ?heightCM .
        	    
      	    ?heightAssay rdf:type turbo:TURBO_0001511 ;
      	                 obo:OBI_0000299 ?heightDatum .   
      	    
      	    ?heightDatum rdf:type obo:IAO_0000408 ;
      	                 obo:OBI_0001938 ?heightValSpec .
      	    
      	    ?weightAssay rdf:type obo:OBI_0000445 ;
      	                 obo:OBI_0000299 ?weightDatum .
      	    
      	    ?weightDatum rdf:type obo:IAO_0000414 ;
      	                 obo:OBI_0001938 ?weightValSpec .
      	    
      	    ?weightValSpec rdf:type obo:OBI_0001931 ;
      	                   obo:IAO_0000039 obo:UO_0000009 ;
      	                   obo:OBI_0002135 ?weightKG .
        	    
      	    ?drugPrescript a obo:PDRO_0000001 .
      	    # medString is the ORDER_NAME
      	    ?drugPrescript turbo:TURBO_0006512 ?medString .
      	    
      	    #temporary workaround triple
      	    ?drugPrescript turbo:TURBO_0000307 ?drugURI .
      	    
      	    ?medCrid obo:IAO_0000219 ?drugPrescript .
      	    ?medCrid a turbo:TURBO_0000561 .
      	    ?medCrid obo:BFO_0000051 ?medSymb .
      	    
      	    ?medSymb obo:BFO_0000050 ?medCrid .
      	    ?medSymb a turbo:TURBO_0000562 .
      	    # medId is a string of letters and/or numbers representing a medication
      	    ?medSymb turbo:TURBO_0006510 ?medId .
        	}
        }
            WHERE
            {
              Values ?g { """ + graphsString + """ }
              Values ?encFromKarma { """ + encounterList + """ } 
              Graph ?g
            	{
            		?encFromKarma
            			a                     obo:OGMS_0000097 ;
            		turbo:TURBO_0000643   ?dsTitle1 .
            	  BIND(str(?encFromKarma) AS ?previousUriText)
            			
            		Optional {
          			?encFromKarma turbo:TURBO_0000648     ?EncID_LV .
            		}
            		Optional
            		{
            		    ?encFromKarma turbo:TURBO_0000650 ?hcRegIdURIString .
            		}
            		
            		optional 
            		{
            		
            		    ?encFromKarma obo:RO_0002234 ?diagSC .
            		    ?diagSC a obo:OGMS_0000073 .
            		    ?diagSC turbo:TURBO_0004602  ?diagCodeRegTextVal .
            		    
            		    optional 
            		    {
            			    ?diagSC turbo:TURBO_0004603  ?diagCodeRegURIString .
                	    }
                		optional 
                		{
                		    ?diagSC turbo:TURBO_0004601  ?diagCodeLV .
                		}
                		optional
                		{
                		    ?diagSC turbo:TURBO_0010013 ?primaryDiag .
                		}
                		optional
                		{
                		    ?diagSC turbo:TURBO_0010014 ?diagSequence .
                		}
            		}
            		
            		optional 
            		{
            		    ?encFromKarma obo:RO_0002234 ?prescription .
            		    ?prescription a obo:PDRO_0000001 .
            		    ?prescription turbo:TURBO_0005601  ?medId .
            		    
            		    # making the medString optional occurred due to discussions with Mark on 5/4/18. In Karma, a blank order name is not instantiated as "", meaning that
            		    # this property will be missing on valid prescriptions with blank order names. See Issue #211.
            		    optional
            		    {
            		        ?prescription turbo:TURBO_0005611  ?medString .
            		    }
            		    # Note that the TURBO_0005612 relationship below is a temporary work-around, until we have med mapping implemented in the MedicationMapper class
            		    optional
            		    {
            		        ?prescription turbo:TURBO_0005612 ?drugURIString 
            		    }
            		}
            		
            		optional {
            			?encFromKarma         turbo:TURBO_0000644  ?encDateTextVal .
            	    }
            		optional {
            			?encFromKarma         turbo:TURBO_0000645  ?encDateMeasVal .
            		}
            		optional {
            		    ?encFromKarma         turbo:TURBO_0000646         ?heightCM .
            		}
            		optional {
            		    ?encFromKarma         turbo:TURBO_0000647         ?weightKG .
            		}
            		optional
            		{
            		    ?encFromKarma         turbo:TURBO_0000655 ?BMILit .
            		}
            		#
            		# what IRIs should be held constant for all of this expansion process, across all of the  shortcut patterns?
            		# dataset "Dataset1", instantiation process "Instantiation1", instantiation output container "OutpContainer"
            		# can be created as UUIDs outside of sparql
            		# for referent tracking and conclusionating, create URIs based on MD5s of the input variables
            		# probably won't work with more complex ref-tracking/conclusionating algorithms
            		#
            		# for each of these bindings, create a type assertion in the construct/insert block
            		# FILTER (?dsTitle1 != ?dsTitle2)
            		BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("dataset", """" + globalUUID + """", str(?dsTitle1))))) AS ?dataset)
            		BIND(uri("""" + instantiation + """") AS ?instantiation)
            		#
            		BIND(uri(?hcRegIdURIString) AS ?hcEncRegId)
            		BIND(uri(?drugURIString) AS ?drugURI)
            		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc encounter", """" + globalUUID + """", str(?encFromKarma))))) AS ?encounter)
            		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc enc ID", """" + randomUUID + """", str(?encFromKarma))))) AS ?encounterCrid)
            		BIND(IF (BOUND(?diagSC), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("diagnosis ", """" + randomUUID + """", str(?encFromKarma), str(?diagSC))))), ?unbound) AS ?diagnosis)
            		BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("enc date", """" + randomUUID + """", str(?encFromKarma))))) AS ?encounterDate)
          	    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("enc start", """" + randomUUID + """", str(?encFromKarma))))) AS ?encStart)
          	    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc enc reg den", """" + randomUUID + """", str(?encFromKarma))))) AS ?hcEncRegDen)
          	    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc enc symb", """" + randomUUID + """", str(?encFromKarma))))) AS ?hcEncSymb)
          	    BIND(IF (BOUND(?heightCM), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc height val spec", """" + randomUUID + """", str(?encFromKarma), str(?heightCM))))), ?unbound) AS ?heightValSpec)
          	    BIND(IF (BOUND(?heightCM), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc height assay", """" + randomUUID + """", str(?encFromKarma), str(?heightCM))))), ?unbound) AS ?heightAssay)
          	    BIND(IF (BOUND(?heightCM), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc height datum", """" + randomUUID + """", str(?encFromKarma), str(?heightCM))))), ?unbound) AS ?heightDatum)
          	    BIND(IF (BOUND(?weightKG), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc weight val spec", """" + randomUUID + """", str(?encFromKarma), str(?weightKG))))), ?unbound) AS ?weightValSpec)
          	    BIND(IF (BOUND(?weightKG), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc weight assay", """" + randomUUID + """", str(?encFromKarma), str(?weightKG))))), ?unbound) AS ?weightAssay)
          	    BIND(IF (BOUND(?weightKG), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc weight datum", """" + randomUUID + """", str(?encFromKarma), str(?weightKG))))), ?unbound) AS ?weightDatum)
          	    BIND(uri(?diagCodeRegURIString) AS ?diagCodeRegURI)
          	    BIND(IF (BOUND(?BMILit), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc bmi", """" + randomUUID + """", str(?encFromKarma), str(?BMILit))))), ?unbound) AS ?BMI)
          	    BIND(IF (BOUND(?BMI), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc bmi val spec", """" + randomUUID + """", str(?encFromKarma), str(?BMILit))))), ?unbound) AS ?BMIvalspec)
          	    BIND(IF (BOUND(?prescription), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("drug prescription", """" + randomUUID + """", str(?encFromKarma), str(?prescription))))), ?unbound) AS ?drugPrescript)
          	    BIND(IF (BOUND(?prescription), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("med crid", """" + randomUUID + """", str(?encFromKarma), str(?prescription))))), ?unbound) AS ?medCrid)
          	    BIND(IF (BOUND(?prescription), uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("med symb", """" + randomUUID + """", str(?encFromKarma), str(?prescription))))), ?unbound) AS ?medSymb)
          	    BIND(IF (BOUND(?encDateTextVal), ?dataset, ?unbound) AS ?dateDataset)
          	    BIND(IF (BOUND(?EncID_LV), ?dataset, ?unbound) AS ?cridSymbDataset)
          	    BIND(IF (BOUND(?hcRegIdURIString), ?dataset, ?unbound) AS ?regDenDataset)
          	    BIND(IF (?diagCodeRegURI = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890>, uri(concat("http://purl.bioontology.org/ontology/ICD9CM/", ?diagCodeLV)), ?unbound) AS ?icd9term)
                BIND(IF (?diagCodeRegURI = <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892>, uri(concat("http://purl.bioontology.org/ontology/ICD10CM/", ?diagCodeLV)), ?unbound) AS ?icd10term)
                BIND(IF (bound(?icd9term) && !bound(?icd10term),?icd9term,?unbound) as ?concatIcdTerm)
                BIND(IF (bound(?icd10term) && !bound(?icd9term),?icd10term,?concatIcdTerm) as ?concatIcdTerm)
          	  }
            }"""
                
          update.updateSparql(cxn, sparqlPrefixes + healthcareEncounterExpansion) 
    }
    
    def createErrorTriplesForUnexpandedAlleles(cxn: RepositoryConnection, lofGraphs: ArrayBuffer[String])
    {
        for (graph <- lofGraphs)
        {
            val logError: String = """
              Insert
              {
                  Graph pmbb:errorLogging
                  {
                      ?allele graphBuilder:reasonNotExpanded graphBuilder:dataFormatError .
                  }
              }
              Where
              {
                  Graph <"""+graph+""">
                  {
                      ?allele ?p ?o .
                  }
                  Minus
                  {
                      Graph pmbb:errorLogging
                      {
                          ?allele graphBuilder:reasonNotExpanded ?reason .
                      }
                  }
              }
              """
            
            update.updateSparql(cxn, sparqlPrefixes + logError)    
        }
    }
}