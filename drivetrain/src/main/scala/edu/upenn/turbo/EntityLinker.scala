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
    
    def runAllEntityLinking(cxn: RepositoryConnection)
    {
        joinParticipantsAndEncounters(cxn)
        connectLossOfFunctionToBiobankEncounters(cxn)
    }
    
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
    
    def connectLossOfFunctionToBiobankEncounters(cxn: RepositoryConnection)
    {
        val bbEncResult: ArrayBuffer[ArrayBuffer[Value]] = getBiobankEncounterWithConsenterInfo(cxn)
        val lossOfFunctionJoinData: ArrayBuffer[ArrayBuffer[Value]] = getLossOfFunctionJoinData(cxn)
        twoFieldMatch.executeMatchWithTwoTables(cxn, bbEncResult, lossOfFunctionJoinData)
        
        val insertSupplTrips: String = 
        """
            Delete
            {
                Graph pmbb:expanded
                {
                    ?allele graphBuilder:willBeLinkedWith ?bbEnc .
                    ?allele turbo:TURBO_0007601 ?encSymbLit .
                    ?allele turbo:TURBO_0007609 ?bbReg .
                }
            }
            Insert
            {
                Graph pmbb:expanded
                {
                    ?DNA a obo:CHEBI_16991 .
                    ?DNA obo:BFO_0000050 ?specimen .
                    ?DNA obo:BFO_0000050 ?consenter .
                    ?specimen a obo:OBI_0001479 .
                    ?specimen obo:BFO_0000051 ?DNA .
                    ?collectionProcess a obo:OBI_0600005 .
                    ?collectionProcess obo:OBI_0000299 ?specimen .
                    ?allele obo:IAO_0000136 ?DNA .
                    
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
                    
                    ?genomeCridSymb obo:BFO_0000050 ?dataset .
                    ?dataset obo:BFO_0000051 ?genomeCridSymb .
                    
                    ?consenter obo:BFO_0000051 ?DNA .
                    ?collProc obo:OBI_0000293 ?consenter .
                    ?consenter obo:OBI_0000299 ?collProc .
                    ?bbSymb obo:BFO_0000050 ?dataset .
                    ?dataset obo:BFO_0000051 ?bbSymb .
                    ?collProc obo:BFO_0000050 ?bbEnc .
                    ?bbEnc obo:BFO_0000051 ?collProc .
                }
            }
            Where
            {
                Graph pmbb:expanded
                {
                    ?allele a obo:OBI_0001352 .
                    ?allele obo:BFO_0000050 ?dataset .
                		?dataset a obo:IAO_0000100 .
                		?allele turbo:TURBO_0007602 ?genomeCridSymbLit .
                		?allele turbo:TURBO_0007603 ?genomeRegURI .
                		
                    ?allele graphBuilder:willBeLinkedWith ?bbEnc .
                    
                    ?bbEnc a turbo:TURBO_0000527 .
                    ?bbEncCrid obo:IAO_0000219 ?bbEnc .
                    ?bbEncCrid a turbo:TURBO_0000533 .
                		?bbEncCrid obo:BFO_0000051 ?bbSymb .
                		?bbEncCrid obo:BFO_0000051 ?bbRegDen .
                	  ?bbSymb a turbo:TURBO_0000534 .
                	  ?bbSymb turbo:TURBO_0006510 ?encSymbLit .
                	  ?bbRegDen a turbo:TURBO_0000535 .
                	  ?bbRegDen obo:IAO_0000219 ?bbReg .
                	  ?bbReg a turbo:TURBO_0000543 .

            		    ?consenter obo:RO_0000056 ?bbEnc .
        		        ?consenter a turbo:TURBO_0000502 .
        		        
        		        Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("DNA", str(?consenter))))) AS ?DNA)
        		        Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("collProc", str(?consenter))))) AS ?collProc)
        		        Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("specimen", str(?consenter))))) AS ?specimen)
        		        Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("genomeCrid", str(?consenter))))) AS ?genomeCrid)
        		        Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("genomeCridSymb", str(?consenter))))) AS ?genoneCridSymb)
        		        Bind (uri(concat("http://www.itmat.upenn.edu/biobank/", md5(CONCAT("genomeCridRegDen", str(?consenter))))) AS ?genomeCridRegDen)
                }
            }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertSupplTrips)
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
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getBbConsInfo, ArrayBuffer("part", "pilv", "bbConsRegId"))
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
      helper.updateSparql(cxn, sparqlPrefixes + completeHealthcareEncounterJoin)
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
      helper.updateSparql(cxn, sparqlPrefixes + completeBiobankEncounterJoin)
      logger.info("join complete")
      
    }
    
    /**
     * Pulls relevant healthcare encounter to consenter join data from the entityLinkData named graph.
     */
    def getHealthcareToConsenterJoinInfo(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val getJoinInfo: String = 
        """
            Select ?eilv ?hcEncRegId ?pilv ?BbConsRegId ?entLinkPartCrid ?entLinkHcCrid
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
                ?entLinkPartRegDen obo:IAO_0000219 ?BbConsRegId .
                ?BbConsRegId a turbo:TURBO_0000506 .
            }}
        """
      helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getJoinInfo, ArrayBuffer("eilv", "hcEncRegId", "pilv", "BbConsRegId", "entLinkPartCrid", "entLinkHcCrid"))
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
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getHcEncInfo, ArrayBuffer("enc", "eilv", "hcEncRegId"))
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
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getJoinInfo, ArrayBuffer("eilv", "bbEncRegId", "pilv", "bbConsRegId", "entLinkPartCrid", "entLinkBbCrid"))
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
      
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getBbEncInfo, ArrayBuffer("enc", "eilv", "bbEncRegId"))
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
            }}"""
      
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + getBbEncInfo, ArrayBuffer("enc", "eilv", "bbEncRegId"))
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
         
       helper.updateSparql(cxn, sparqlPrefixes + attachBMIToAdipose)
    }
    
    def getLossOfFunctionJoinData(cxn: RepositoryConnection): ArrayBuffer[ArrayBuffer[Value]] =
    {
        val query: String = 
        """
            select ?allele ?encLit ?encReg where
            {
                graph pmbb:expanded
                {
                    ?allele a obo:OBI_0001352 .
                    ?allele turbo:TURBO_0007601 ?encLit .
                    ?allele turbo:TURBO_0007609 ?encReg .
                }
            }  
        """
        helper.querySparqlAndUnpackTuple(cxn, sparqlPrefixes + query, ArrayBuffer("allele", "encLit", "encReg"))
    }
}