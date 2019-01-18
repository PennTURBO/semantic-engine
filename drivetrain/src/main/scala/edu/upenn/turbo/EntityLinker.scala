package edu.upenn.turbo

import org.eclipse.rdf4j.repository.RepositoryConnection
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import org.eclipse.rdf4j.model.Value

/**
 * The EntityLinker class is a generic class containing methods which create ontology-based connections between nodes in the graph. This class was initially created to 
 * connect Healthcare and Biobank Encounters to Biobank Consenters, as this data is often available only in a separate join table. It has since been expanded to contain 
 * methods performing any linkage between some data and an Encounter or Consenter.
 */
class EntityLinker extends ProjectwideGlobals
{   
    val twoFieldMatch: MatchOnTwoFields = new MatchOnTwoFields
    
    /**
     * This is the driver method to complete the original "EntityLinking" to connect Healthcare and Biobank encounters to Biobank consenters.
     */
    def joinParticipantsAndEncounters (cxn: RepositoryConnection)
    {
        val consResult: ArrayBuffer[ArrayBuffer[Value]] = getConsenterInfo(cxn)
        logger.info("starting hc join")
        joinParticipantsAndHealthcareEncounters(cxn, consResult)
        logger.info("starting biobank join")
        joinParticipantsAndBiobankEncounters(cxn, consResult)
        logger.info("connect bmi to adipose")
        connectBMIToAdipose(cxn)
        logger.info("Participants and Encounters have been linked using join data")
    }
    
    def connectLossOfFunctionToBiobankEncounters(cxn: RepositoryConnection, lofGraphs: ArrayBuffer[String])
    {
        convertLOFRegStringToRegURI(cxn, lofGraphs)
        val bbEncResult: HashMap[String, ArrayBuffer[Value]] = twoFieldMatch.createHashMapFromTable(getBiobankEncounterWithConsenterInfo(cxn), true)
        for (graph <- lofGraphs)
        {
            val lossOfFunctionJoinData: ArrayBuffer[ArrayBuffer[Value]] = getLossOfFunctionJoinData(cxn, graph)
            twoFieldMatch.executeMatchWithTwoTables(cxn, bbEncResult, lossOfFunctionJoinData, graph, true)
        }
    }
    
    /**
     * Pulls relevant consenter info for linking from the graph: consenter URI, consenter identifier, consenter registry. This data is used for both
     * biobank encounter and healthcare encounter joins.
     */
    def getConsenterInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getBbConsInfo: String = 
        """
            Select ?part ?pilv ?bbConsRegId
            Where {
            Graph pmbb:expanded
            {
              ?bbConsSymb turbo:TURBO_0006510 ?pilv ;
  		               a turbo:TURBO_0000504 .
      		    ?partCrid a turbo:TURBO_0000503 ;
      		              obo:BFO_0000051 ?bbConsSymb ;
      		              obo:IAO_0000219 ?part ;
      		              obo:BFO_0000051 ?partRegDen .
      		    ?partRegDen a turbo:TURBO_0000505 ;
      		                obo:IAO_0000219 ?bbConsRegId .
      		    ?bbConsRegId a turbo:TURBO_0000506 .
          		?part  a  turbo:TURBO_0000502 ;
          		       turbo:TURBO_0006500 'true'^^xsd:boolean .
            }}
        """
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getBbConsInfo, ArrayBuffer("part", "pilv", "bbConsRegId"))
    }
    
    /**
     * Executes Healthcare encounter to biobank consenter joins.
     */
    def joinParticipantsAndHealthcareEncounters(cxn: RepositoryConnection, consResult: ArrayBuffer[ArrayBuffer[Value]])
    {
      val joinResult: ArrayBuffer[ArrayBuffer[Value]] = getHealthcareToConsenterJoinInfo(cxn)
      val encResult: ArrayBuffer[ArrayBuffer[Value]] = getHealthcareEncounterInfo(cxn)
      logger.info("got encounter results")
      
      twoFieldMatch.executeMatchWithThreeTables(cxn, joinResult, encResult, consResult)
      logger.info("completed match preprocess")
      val completeHealthcareEncounterJoin: String = 
      """
          INSERT {
          	GRAPH pmbb:expanded
          	{
          		?part obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?enc .
          		?hcEncCrid turbo:TURBO_0000302 ?partCrid .
          		#
          		?massDatum obo:IAO_0000136 ?part .
          		?heightDatum obo:IAO_0000136 ?part .
          		?massDatum obo:IAO_0000221 ?weight .
          		?heightDatum obo:IAO_0000221 ?height .
          		?massMeas obo:OBI_0000293 ?part .
          		?heightMeas obo:OBI_0000293 ?part .
          	}
        	}
        	Where
        	{
        	    Graph pmbb:expanded
        	    {
          	    ?part obo:RO_0000056 ?enc .
          	    ?part a turbo:TURBO_0000502 ;
          	        obo:RO_0000086 ?height ;
        		        obo:RO_0000086 ?weight .
        		    ?height rdf:type obo:PATO_0000119 .
        		    ?weight rdf:type obo:PATO_0000128 .
        		    
          	    ?enc a obo:OGMS_0000097 .
          	    
          	    ?hcEncCrid a turbo:TURBO_0000508 .
          	    ?hcEncCrid obo:IAO_0000219 ?enc .
          	    
          	    ?partCrid a turbo:TURBO_0000503 .
          	    ?partCrid obo:IAO_0000219 ?part .
          	    
          	    OPTIONAL 
            		{
              		?massMeas obo:BFO_0000050 ?enc ;
        		          rdf:type obo:OBI_0000445 ;
        		          obo:OBI_0000299 ?massDatum .
              		?massDatum a obo:IAO_0000414.
              	}
              	OPTIONAL
              	{
              		?heightMeas obo:BFO_0000050 ?enc ;
        		          rdf:type turbo:TURBO_0001511 ;
        		          obo:OBI_0000299 ?heightDatum .
              		?heightDatum a obo:IAO_0000408 .
              	}
              }
            	BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("new hc puirole", str(?enc))))) AS ?puirole)
        	}
      """
      update.updateSparql(cxn, sparqlPrefixes + completeHealthcareEncounterJoin)
      logger.info("join complete")
    }
    
    /**
     * Executes biobank encounter to biobank consenter joins.
     */
    def joinParticipantsAndBiobankEncounters(
        cxn: RepositoryConnection, consResult: ArrayBuffer[ArrayBuffer[Value]])
    {
      val encResult: ArrayBuffer[ArrayBuffer[Value]] = getBiobankEncounterInfo(cxn)
      val joinResult: ArrayBuffer[ArrayBuffer[Value]] = getBiobankToConsenterJoinInfo(cxn)
      logger.info("got join results")
      
      twoFieldMatch.executeMatchWithThreeTables(cxn, joinResult, encResult, consResult)
      logger.info("completed match preprocess")
      val completeBiobankEncounterJoin: String = 
      """
          INSERT {
          	GRAPH pmbb:expanded
          	{
          		?part obo:RO_0000087 ?puirole .
          		?puirole a obo:OBI_0000097 .
          		?puirole obo:BFO_0000054 ?enc .
          		?bbEncCrid turbo:TURBO_0000302 ?partCrid .
          		#
          		?massDatum obo:IAO_0000136 ?part .
          		?heightDatum obo:IAO_0000136 ?part .
          		?massDatum obo:IAO_0000221 ?weight .
          		?heightDatum obo:IAO_0000221 ?height .
          		?massMeas obo:OBI_0000293 ?part .
          		?heightMeas obo:OBI_0000293 ?part .
          	}
        	}
        	Where
        	{
        	    Graph pmbb:expanded
        	    {
          	    ?part obo:RO_0000056 ?enc .
          	    ?part a turbo:TURBO_0000502 ;
          	        obo:RO_0000086 ?height ;
        		        obo:RO_0000086 ?weight .
        		    ?height rdf:type obo:PATO_0000119 .
        		    ?weight rdf:type obo:PATO_0000128 .
        		    
          	    ?enc a turbo:TURBO_0000527 .
          	    
          	    ?bbEncCrid a turbo:TURBO_0000533 .
          	    ?bbEncCrid obo:IAO_0000219 ?enc .
          	    
          	    ?partCrid a turbo:TURBO_0000503 .
          	    ?partCrid obo:IAO_0000219 ?part .
          	    
          	    OPTIONAL 
            		{
              		?massMeas obo:BFO_0000050 ?enc ;
        		          rdf:type obo:OBI_0000445 ;
        		          obo:OBI_0000299 ?massDatum .
              		?massDatum a obo:IAO_0000414.
              	}
              	OPTIONAL
              	{
              		?heightMeas obo:BFO_0000050 ?enc ;
        		          rdf:type turbo:TURBO_0001511 ;
        		          obo:OBI_0000299 ?heightDatum .
              		?heightDatum a obo:IAO_0000408 .
              	}
              }
            	BIND(uri(CONCAT("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("new bb puirole", str(?enc))))) AS ?puirole)
        	}
      """
      update.updateSparql(cxn, sparqlPrefixes + completeBiobankEncounterJoin)
      logger.info("join complete")
      
    }
    
    /**
     * Pulls relevant healthcare encounter to consenter join data from the entityLinkData named graph.
     */
    def getHealthcareToConsenterJoinInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getJoinInfo: String = 
        """
            Select ?eilv ?hcEncRegId ?pilv ?bbConsRegId ?entLinkPartCrid ?entLinkHcCrid
            Where {
            Graph pmbb:entityLinkData
            {
                ?entLinkHcCrid a turbo:TURBO_0000508 .
                ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcSymb .
                ?entLinkHcSymb turbo:TURBO_0006510 ?eilv .
                ?entLinkHcSymb a turbo:TURBO_0000509 .
                ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcRegDen .
                ?entLinkHcRegDen a turbo:TURBO_0000510 .
                ?entLinkHcRegDen obo:IAO_0000219 ?hcEncRegId .
                ?hcEncRegId a turbo:TURBO_0000513 .
                
                ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkHcCrid .
                
                ?entLinkPartCrid a turbo:TURBO_0000503 .
                ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                ?entLinkPartSymb a turbo:TURBO_0000504 .
                ?entLinkPartSymb turbo:TURBO_0006510 ?pilv .
                ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                ?entLinkPartRegDen a turbo:TURBO_0000505 .
                ?entLinkPartRegDen obo:IAO_0000219 ?bbConsRegId .
                ?bbConsRegId a turbo:TURBO_0000506 .
            }}
        """
      update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getJoinInfo, ArrayBuffer("eilv", "hcEncRegId", "pilv", "bbConsRegId", "entLinkPartCrid", "entLinkHcCrid"))
    }
    
    /**
     * Pulls relevant healthcare encounter data from the graph: healthcare encounter URI, identifier, and registry.
     */
    def getHealthcareEncounterInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getHcEncInfo: String = 
        """
            Select ?enc ?eilv ?hcEncRegId
            Where {
            Graph pmbb:expanded
            {
              ?hcSymb turbo:TURBO_0006510 ?eilv ;
          		               turbo:TURBO_0006500 'true'^^xsd:boolean ;
          		               a turbo:TURBO_0000509 .
          		?hcEncCrid a turbo:TURBO_0000508 ;
          		           obo:BFO_0000051 ?hcSymb ;
          		           obo:IAO_0000219 ?enc ;
          		           obo:BFO_0000051 ?hcEncRegDen .
          		?hcEncRegDen a turbo:TURBO_0000510 ;
          		             obo:IAO_0000219 ?hcEncRegId .
          		?hcEncRegId a turbo:TURBO_0000513 .
          		?enc  a  obo:OGMS_0000097 ;
          		       turbo:TURBO_0006500 'true'^^xsd:boolean .
          		Minus
          		{
          		    ?consenter obo:RO_0000056 ?enc .
          		    ?consenter a turbo:TURBO_0000502 .
          		}
            }}
        """
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getHcEncInfo, ArrayBuffer("enc", "eilv", "hcEncRegId"))
    }
    
    /**
     * Pulls relevant biobank encounter to consenter join data from the entityLinkData named graph.
     */
    def getBiobankToConsenterJoinInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getJoinInfo: String = 
        """
            Select ?eilv ?bbEncRegId ?pilv ?bbConsRegId ?entLinkPartCrid ?entLinkBbCrid
            Where {
            Graph pmbb:entityLinkData
            {
                ?entLinkBbCrid a turbo:TURBO_0000533 .
                ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbSymb .
                ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbRegDen .
                ?entLinkBbSymb turbo:TURBO_0006510 ?eilv .
                ?entLinkBbSymb a turbo:TURBO_0000534 .
                ?entLinkBbRegDen a turbo:TURBO_0000535 .
                ?entLinkBbRegDen obo:IAO_0000219 ?bbEncRegId .
                ?bbEncRegId a turbo:TURBO_0000543 .
                
                ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkBbCrid .
                
                ?entLinkPartCrid a turbo:TURBO_0000503 .
                ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                ?entLinkPartSymb a turbo:TURBO_0000504 .
                ?entLinkPartSymb turbo:TURBO_0006510 ?pilv .
                ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                ?entLinkPartRegDen a turbo:TURBO_0000505 .
                ?entLinkPartRegDen obo:IAO_0000219 ?bbConsRegId .
                ?bbConsRegId a turbo:TURBO_0000506 .
            }}
        """
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getJoinInfo, ArrayBuffer("eilv", "bbEncRegId", "pilv", "bbConsRegId", "entLinkPartCrid", "entLinkBbCrid"))
    }
    
    /**
     * Pulls relevant biobank encounter data from the graph: biobank encounter URI, identifier, and registry.
     */
    def getBiobankEncounterInfo(cxn: RepositoryConnection, withoutConsenter: Boolean = true): ArrayBuffer[ArrayBuffer[Value]] =
    {
        var getBbEncInfo: String = 
        """
            Select ?enc ?eilv ?bbEncRegId
            Where {
            Graph pmbb:expanded
            {
              ?bbSymb turbo:TURBO_0006510 ?eilv ;
          		               a turbo:TURBO_0000534 .
          		?bbEncCrid a turbo:TURBO_0000533 ;
          		           obo:BFO_0000051 ?bbSymb ;
          		           obo:IAO_0000219 ?enc ;
          		           obo:BFO_0000051 ?bbEncRegDen .
          		?bbEncRegDen a turbo:TURBO_0000535 ;
          		             obo:IAO_0000219 ?bbEncRegId .
          		?bbEncRegId a turbo:TURBO_0000543 .
          		?enc  a  turbo:TURBO_0000527 ;
          		       turbo:TURBO_0006500 'true'^^xsd:boolean .
          		""" 
          		if (withoutConsenter) getBbEncInfo += """
          		Minus
          		{
          		    ?consenter obo:RO_0000056 ?enc .
          		    ?consenter a turbo:TURBO_0000502 .
          		}"""
            getBbEncInfo += "}}"
      
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getBbEncInfo, ArrayBuffer("enc", "eilv", "bbEncRegId"))
    }
    
    def getBiobankEncounterWithConsenterInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        var getBbEncInfo: String = 
        """
            Select ?enc ?eilv ?bbEncRegId
            Where {
            Graph pmbb:expanded
            {
              ?bbSymb turbo:TURBO_0006510 ?eilv ;
          		               a turbo:TURBO_0000534 .
          		?bbEncCrid a turbo:TURBO_0000533 ;
          		           obo:BFO_0000051 ?bbSymb ;
          		           obo:IAO_0000219 ?enc ;
          		           obo:BFO_0000051 ?bbEncRegDen .
          		?bbEncRegDen a turbo:TURBO_0000535 ;
          		             obo:IAO_0000219 ?bbEncRegId .
          		?bbEncRegId a turbo:TURBO_0000543 .
          		?enc  a  turbo:TURBO_0000527 ;
          		       turbo:TURBO_0006500 'true'^^xsd:boolean .
      		    ?consenter obo:RO_0000056 ?enc .
      		    ?consenter a turbo:TURBO_0000502 .
      		    ?consenter turbo:TURBO_0006500 'true'^^xsd:boolean .
            }}"""
      
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getBbEncInfo, ArrayBuffer("enc", "eilv", "bbEncRegId"))
    }
    
    /**
     * After healthcare and biobank encounters have been joined with biobank consenters, this SPARQL-based method
     * finds instances of BMI data attached to linked encounters and connects the BMI with the relevant consenter's
     * adipose tissue node.
     */
    def connectBMIToAdipose(cxn: RepositoryConnection)
    {
        val attachBMIToAdipose: String = """
            INSERT
            {
                graph pmbb:expanded
                {
                    ?BMI obo:IAO_0000136 ?adipose .
                }
            }
            WHERE 
            {
                graph pmbb:expanded
                {
                    VALUES ?output {obo:OBI_0000299 obo:RO_0002234}
                    ?participant a turbo:TURBO_0000502 .
                    ?participant obo:BFO_0000051 ?adipose .
                    ?adipose a obo:UBERON_0001013 .
                    ?participant obo:RO_0000087 ?puirole .
                    ?puirole a obo:OBI_0000097 .
                    ?puirole obo:BFO_0000054 ?encounter .
                    ?encounter ?output ?BMI .
                    ?BMI a <http://www.ebi.ac.uk/efo/EFO_0004340> .
                }
            MINUS
            {
                ?BMI obo:IAO_0000136 ?adipose .
            }
        }"""
         
       update.updateSparql(cxn, sparqlPrefixes + attachBMIToAdipose)
    }
    
    def getLossOfFunctionJoinData(cxn: RepositoryConnection, lofGraph: String): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val query: String = 
        """
            select ?allele ?encLit ?encReg where
            {
                graph <""" + lofGraph + """>
                {
                    ?allele a obo:OBI_0001352 ;
          	            turbo:TURBO_0007601 ?encLit ;
          	            turbo:TURBO_0007602 ?genomeCridSymbLit ;
          	            turbo:TURBO_0007603 ?genomeReg ;
          	            turbo:TURBO_0007605 ?geneText ;
          	            turbo:TURBO_0007607 ?zygosityValURI ;
          	            turbo:TURBO_0007608 ?datasetTitle ;
          	            turbo:TURBO_0007609 ?encReg .
          	            # this shortcut becoming optional on 11/2/18 because our files don't contain this shortcut
          	            # turbo:TURBO_0007610 ?geneTerm .
                }
            }  
        """
        update.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, ArrayBuffer("allele", "encLit", "encReg"))
    }
    
    def convertLOFRegStringToRegURI(cxn: RepositoryConnection, lofGraphs: ArrayBuffer[String])
    {
        for (graph <- lofGraphs)
        {
            val makeURIs: String = """
                Delete 
                {
                    Graph <""" + graph + """>
                    {
                        ?allele turbo:TURBO_0007609 ?bbRegString .
                    }
                }
                Insert
                {
                    Graph <""" + graph + """>
                    {
                        ?allele turbo:TURBO_0007609 ?bbRegURI .
                    }
                }
                Where
                {
                    Graph <""" + graph + """>
                    {
                        ?allele turbo:TURBO_0007609 ?bbRegString .
                    }
                    Bind (uri(?bbRegString) AS ?bbRegURI)  
                }  
                """
            update.updateSparql(cxn, sparqlPrefixes + makeURIs)
        }
    }
    
    def runPreExpansionLinking(cxn: RepositoryConnection, globalUUID: String)
    {
        linkUnexpandedHcEncountersToUnexpandedConsenters(cxn, globalUUID)
        linkUnexpandedBbEncountersToUnexpandedConsenters(cxn, globalUUID)
        createJoinDataFromUnlinkedHcEncounters(cxn, globalUUID)
        createJoinDataFromUnlinkedBbEncounters(cxn, globalUUID)
    }
    
    def linkUnexpandedHcEncountersToUnexpandedConsenters(cxn: RepositoryConnection, globalUUID: String)
    {
        val createLinks_oldExpansionPattern: String = """
          Insert
          {
              Graph pmbb:expanded
              {
                  ?expandedConsenter obo:RO_0000056 ?expandedEncounter .
              }
          }
          Where
          {
              Graph ?g1
              {
                  ?hcEnc a obo:OGMS_0000097 .
                  ?hcEnc turbo:ScHcEnc2UnexpandedConsenter ?consenterUriString .
                  ?hcEnc turbo:TURBO_0010002 ?consenterRegistryUriString .
                  Bind(uri(?consenterUriString) as ?consenter)
              }
              Graph ?g2
              {
                  ?consenter a turbo:TURBO_0000502 .
                  ?consenter turbo:TURBO_0000610 ?consenterRegistryUriString .
              }
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("consenter", """" + globalUUID + """", str(?consenter))))) AS ?expandedConsenter)
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc encounter", """" + globalUUID + """", str(?hcEnc))))) AS ?expandedEncounter)
          }
          """
          update.updateSparql(cxn, sparqlPrefixes + createLinks_oldExpansionPattern)

          val createLinks_newExpansionPattern: String = """
          Insert
          {
              Graph pmbb:expanded
              {
                  ?expandedConsenter obo:RO_0000056 ?expandedEncounter .
              }
          }
          Where
          {
              Graph ?g1
              {
                  ?hcEnc a obo:OGMS_0000097 .
                  ?hcEnc turbo:ScHcEnc2UnexpandedConsenter ?consenterUriString .
                  ?hcEnc turbo:TURBO_0010002 ?consenterRegistryUriString .
                  Bind(uri(?consenterUriString) as ?consenter)
              }
              Graph ?g2
              {
                  ?consenter a turbo:TURBO_0000502 .
                  ?crid a turbo:TURBO_0000503 .
                  ?crid obo:IAO_0000219 ?consenter .
                  ?crid turbo:TURBO_0003610 ?consenterRegistryUriString .
              }
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("consenter", """" + globalUUID + """", str(?consenter))))) AS ?expandedConsenter)
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc encounter", """" + globalUUID + """", str(?hcEnc))))) AS ?expandedEncounter)
          }
          """
          update.updateSparql(cxn, sparqlPrefixes + createLinks_newExpansionPattern)
    }
    
    def linkUnexpandedBbEncountersToUnexpandedConsenters(cxn: RepositoryConnection, globalUUID: String)
    {
        val createLinks_oldExpansionPattern: String = """
          Insert
          {
              Graph pmbb:expanded
              {
                  ?expandedConsenter obo:RO_0000056 ?expandedEncounter .
              }
          }
          Where
          {
              Graph ?g1
              {
                  ?bbEnc a turbo:TURBO_0000527 .
                  ?bbEnc turbo:ScBbEnc2UnexpandedConsenter ?consenterUriString .
                  ?bbEnc turbo:TURBO_0010012 ?consenterRegistryUriString .
                  Bind(uri(?consenterUriString) as ?consenter)
              }
              Graph ?g2
              {
                  ?consenter a turbo:TURBO_0000502 .
                  ?consenter turbo:TURBO_0000610 ?consenterRegistryUriString .
              }
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("consenter", """" + globalUUID + """", str(?consenter))))) AS ?expandedConsenter)
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("bb encounter", """" + globalUUID + """", str(?bbEnc))))) AS ?expandedEncounter)
          }
          """
          update.updateSparql(cxn, sparqlPrefixes + createLinks_oldExpansionPattern)

          val createLinks_newExpansionPattern: String = """
          Insert
          {
              Graph pmbb:expanded
              {
                  ?expandedConsenter obo:RO_0000056 ?expandedEncounter .
              }
          }
          Where
          {
              Graph ?g1
              {
                  ?bbEnc a turbo:TURBO_0000527 .
                  ?bbEnc turbo:ScBbEnc2UnexpandedConsenter ?consenterUriString .
                  ?bbEnc turbo:TURBO_0010012 ?consenterRegistryUriString .
                  Bind(uri(?consenterUriString) as ?consenter)
              }
              Graph ?g2
              {
                  ?consenter a turbo:TURBO_0000502 .
                  ?crid a turbo:TURBO_0000503 .
                  ?crid obo:IAO_0000219 ?consenter .
                  ?crid turbo:TURBO_0003610 ?consenterRegistryUriString .
              }
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("consenter", """" + globalUUID + """", str(?consenter))))) AS ?expandedConsenter)
              BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("bb encounter", """" + globalUUID + """", str(?bbEnc))))) AS ?expandedEncounter)
          }
          """
          update.updateSparql(cxn, sparqlPrefixes + createLinks_newExpansionPattern)
    }
    
    def createJoinDataFromUnlinkedHcEncounters(cxn: RepositoryConnection, globalUUID: String)
    {
        val createJoinData: String = """
            Insert
            {
                Graph pmbb:entityLinkData
                {
                    ?entLinkHcCrid a turbo:TURBO_0000508 .
                    ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcSymb .
                    ?entLinkHcSymb turbo:TURBO_0006510 ?encId .
                    ?entLinkHcSymb a turbo:TURBO_0000509 .
                    ?entLinkHcCrid obo:BFO_0000051 ?entLinkHcRegDen .
                    ?entLinkHcRegDen a turbo:TURBO_0000510 .
                    ?entLinkHcRegDen obo:IAO_0000219 ?hcEncRegId .
                    ?hcEncRegId a turbo:TURBO_0000513 .
                    
                    ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkHcCrid .
                    
                    ?entLinkPartCrid a turbo:TURBO_0000503 .
                    ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                    ?entLinkPartSymb a turbo:TURBO_0000504 .
                    ?entLinkPartSymb turbo:TURBO_0006510 ?consId .
                    ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                    ?entLinkPartRegDen a turbo:TURBO_0000505 .
                    ?entLinkPartRegDen obo:IAO_0000219 ?bbConsRegId .
                    ?bbConsRegId a turbo:TURBO_0000506 .
                }
            }
            Where
            {
                Graph ?g 
                {
                    ?hcEnc a obo:OGMS_0000097 .
                    ?hcEnc turbo:TURBO_0010000 ?consId . 
                    ?hcEnc turbo:TURBO_0000648 ?encId .
                    ?hcEnc turbo:TURBO_0000650 ?hcRegistryUriString .
                    ?hcEnc turbo:TURBO_0010002 ?consRegistryUriString .
                    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("hc encounter", """" + globalUUID + """", str(?hcEnc))))) AS ?expandedEncounter)
                }
                Minus
                {
                    Graph pmbb:expanded
                    {
                        ?consenter obo:RO_0000056 ?expandedEncounter .
                    }
                }
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkHcCrid)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkHcSymb)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkHcRegDen)
                BIND(uri(?hcRegistryUriString) AS ?hcEncRegId)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkPartCrid)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkPartSymb)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkPartRegDen)
                BIND(uri(?consRegistryUriString) AS ?bbConsRegId)
            }
        """
        update.updateSparql(cxn, sparqlPrefixes + createJoinData)
    }
    
    def createJoinDataFromUnlinkedBbEncounters(cxn: RepositoryConnection, globalUUID: String)
    {
        val createJoinData: String = """
            Insert
            {
                Graph pmbb:entityLinkData
                {
                    ?entLinkBbCrid a turbo:TURBO_0000533 .
                    ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbSymb .
                    ?entLinkBbCrid obo:BFO_0000051 ?entLinkBbRegDen .
                    ?entLinkBbSymb turbo:TURBO_0006510 ?encId .
                    ?entLinkBbSymb a turbo:TURBO_0000534 .
                    ?entLinkBbRegDen a turbo:TURBO_0000535 .
                    ?entLinkBbRegDen obo:IAO_0000219 ?bbEncRegId .
                    ?bbEncRegId a turbo:TURBO_0000543 .
                    
                    ?entLinkPartCrid turbo:TURBO_0000302 ?entLinkBbCrid .
                    
                    ?entLinkPartCrid a turbo:TURBO_0000503 .
                    ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartSymb .
                    ?entLinkPartSymb a turbo:TURBO_0000504 .
                    ?entLinkPartSymb turbo:TURBO_0006510 ?consId .
                    ?entLinkPartCrid obo:BFO_0000051 ?entLinkPartRegDen .
                    ?entLinkPartRegDen a turbo:TURBO_0000505 .
                    ?entLinkPartRegDen obo:IAO_0000219 ?bbConsRegId .
                    ?bbConsRegId a turbo:TURBO_0000506 .
                }
            }
            Where
            {
                Graph ?g
                {
                    ?bbEnc a turbo:TURBO_0000527 .
                    ?bbEnc turbo:TURBO_0010010 ?consId .
                    ?bbEnc turbo:TURBO_0000628 ?encId .
                    ?bbEnc turbo:TURBO_0000630 ?bbRegistryUriString .
                    ?bbEnc turbo:TURBO_0010012 ?consRegistryUriString .
                    BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("bb encounter", """" + globalUUID + """", str(?bbEnc))))) AS ?expandedEncounter)
                }
                Minus
                {
                    Graph pmbb:expanded
                    {
                        ?consenter obo:RO_0000056 ?expandedEncounter .
                    }
                }
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkBbCrid)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkBbSymb)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkBbRegDen)
                BIND(uri(?bbRegistryUriString) AS ?bbEncRegId)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkPartCrid)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkPartSymb)
                BIND(uri(concat("http://www.itmat.upenn.edu/biobank/", REPLACE(struuid(), "-", ""))) AS ?entLinkPartRegDen)
                BIND(uri(?consRegistryUriString) AS ?bbConsRegId)
            }
        """
        update.updateSparql(cxn, sparqlPrefixes + createJoinData)
    }
}