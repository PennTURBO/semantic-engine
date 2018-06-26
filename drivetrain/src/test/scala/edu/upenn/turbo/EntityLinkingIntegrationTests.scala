package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class EntityLinkingIntegrationTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    
    before
    {
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
        helper.deleteAllTriplesInDatabase(cxn)
        
        //load TURBO ontology manually
        helper.addOntologyFromUrl(cxn)
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    test("3 drivetrain runs - biobank encounter, ent link data, consenter")
    {
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_biobankEncounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            }
        }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)

        runDrivetrainTestStack()
        
        val insertEntLinkData: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }  
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLinkData)
        
        runDrivetrainTestStack()
        
        val insertConsenter: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val ask: String = 
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?consenter obo:RO_0000056 ?encounter .
                ?encounter a turbo:TURBO_0000527 .
                ?consenter a turbo:TURBO_0000502 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("3 drivetrain runs - consenter, biobank encounter, ent link data")
    {
        val insertConsenter: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_biobankEncounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            }
        }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)

        runDrivetrainTestStack()
        
        val insertEntLinkData: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }  
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLinkData)
        
        runDrivetrainTestStack()
        
        val ask: String = 
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?consenter obo:RO_0000056 ?encounter .
                ?encounter a turbo:TURBO_0000527 .
                ?consenter a turbo:TURBO_0000502 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true) 
    }
    
    test("3 drivetrain runs - ent link data, consenter, biobank encounter")
    {
        val insertEntLinkData: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }  
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLinkData)
        
        runDrivetrainTestStack()
        
        val insertConsenter: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_biobankEncounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            }
        }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)

        runDrivetrainTestStack()
        
        val ask: String = 
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?consenter obo:RO_0000056 ?encounter .
                ?encounter a turbo:TURBO_0000527 .
                ?consenter a turbo:TURBO_0000502 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("3 drivetrain runs - healthcare encounter, ent link data, consenter")
    {
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_healthcareEncounterShortcuts
            {
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000650> "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000645> "2016-11-11"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000646> "177.8"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000648> "20" .
                <http://localhost:8080/source/hcEncounter/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000097> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000647> "82.9931972789"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000644> "11-Nov-16" .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000655> "26.2574965457"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000643> "encounters_only_hc_2_pk_encounter_id.csv" .
            }
        }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)

        runDrivetrainTestStack()
        
        val insertEntLinkData: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://transformunify.org/ontologies/TURBO_0002610> "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003603> "participants_2_mrn_to_encounters_hc_2_pk_encounter_id_join.csv" .
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://transformunify.org/ontologies/TURBO_0002608> "20" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000508> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000411" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003608> "2" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003609> "PMC" .
            }  
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLinkData)
        
        runDrivetrainTestStack()
        
        val insertConsenter: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000604> "2-Mar-63" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000608> "2" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000609> "PMC" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000411"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000605> "1963-03-02"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val ask: String = 
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?consenter obo:RO_0000056 ?encounter .
                ?encounter a obo:OGMS_0000097 .
                ?consenter a turbo:TURBO_0000502 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
    }
    
    test("3 drivetrain runs - consenter, healthcare encounter, ent link data")
    {
        val insertConsenter: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000604> "2-Mar-63" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000608> "2" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000609> "PMC" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000411"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000605> "1963-03-02"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_healthcareEncounterShortcuts
            {
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000650> "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000645> "2016-11-11"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000646> "177.8"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000648> "20" .
                <http://localhost:8080/source/hcEncounter/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000097> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000647> "82.9931972789"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000644> "11-Nov-16" .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000655> "26.2574965457"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000643> "encounters_only_hc_2_pk_encounter_id.csv" .
            }
        }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)

        runDrivetrainTestStack()
        
        val insertEntLinkData: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://transformunify.org/ontologies/TURBO_0002610> "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003603> "participants_2_mrn_to_encounters_hc_2_pk_encounter_id_join.csv" .
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://transformunify.org/ontologies/TURBO_0002608> "20" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000508> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000411" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003608> "2" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003609> "PMC" .
            }  
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLinkData)
        
        runDrivetrainTestStack()
        
        val ask: String = 
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?consenter obo:RO_0000056 ?encounter .
                ?encounter a obo:OGMS_0000097 .
                ?consenter a turbo:TURBO_0000502 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)       
    }
    
    test("3 drivetrain runs - ent link data, consenter, healthcare encounter")
    {
        val insertEntLinkData: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://transformunify.org/ontologies/TURBO_0002610> "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003603> "participants_2_mrn_to_encounters_hc_2_pk_encounter_id_join.csv" .
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://transformunify.org/ontologies/TURBO_0002608> "20" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/encounterCrid/e161a3608e264710a8c63ee3b09aa7e9> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000508> .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000411" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003608> "2" .
                <http://localhost:8080/source/consenterCrid/7b6ff26f7802414baa18d87abd2eec06> <http://transformunify.org/ontologies/TURBO_0003609> "PMC" .
            }  
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLinkData)
        
        runDrivetrainTestStack()
        
        val insertConsenter: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000604> "2-Mar-63" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000608> "2" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000609> "PMC" .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000411"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000605> "1963-03-02"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/bc86a2ec0e4c4f85ba736a621bac0a3c> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_healthcareEncounterShortcuts
            {
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000650> "http://transformunify.org/ontologies/TURBO_0000440"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000645> "2016-11-11"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000646> "177.8"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000648> "20" .
                <http://localhost:8080/source/hcEncounter/20> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OGMS_0000097> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000647> "82.9931972789"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000644> "11-Nov-16" .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000655> "26.2574965457"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/hcEncounter/20> <http://transformunify.org/ontologies/TURBO_0000643> "encounters_only_hc_2_pk_encounter_id.csv" .
            }
        }
        """
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)

        runDrivetrainTestStack()
        
        val ask: String = 
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?consenter obo:RO_0000056 ?encounter .
                ?encounter a obo:OGMS_0000097 .
                ?consenter a turbo:TURBO_0000502 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)   
    }
    
    test("4 drivetrain runs - LOF, biobank encounter, entlink, consenter")
    {
        val insertLOF: String = 
        """
        Insert Data
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007601> "B" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007605> "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007606> "1"^^<http://www.w3.org/2001/XMLSchema#integer> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertLOF)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_encounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            
            }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)
        
        runDrivetrainTestStack()
        
        val insertEntLink: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLink)
        
        runDrivetrainTestStack()
        
        val insertConsenter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val ask: String =
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
        
        val ask2: String =
        """
        Ask
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    test("4 drivetrain runs - LOF, biobank consenter, entlink, encounter")
    {
        val insertLOF: String = 
        """
        Insert Data
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007601> "B" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007605> "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007606> "1"^^<http://www.w3.org/2001/XMLSchema#integer> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertLOF)
        
        runDrivetrainTestStack()
        
        val insertConsenter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEntLink: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLink)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_encounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            
            }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)
        
        runDrivetrainTestStack()
        
        val ask: String =
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
        
        val ask2: String =
        """
        Ask
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    test("4 drivetrain runs - biobank consenter, entlink, LOF, encounter")
    {
        val insertConsenter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEntLink: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLink)
        
        runDrivetrainTestStack()
        
        val insertLOF: String = 
        """
        Insert Data
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007601> "B" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007605> "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007606> "1"^^<http://www.w3.org/2001/XMLSchema#integer> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertLOF)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_encounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            
            }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)
        
        runDrivetrainTestStack()
        
        val ask: String =
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
        
        val ask2: String =
        """
        Ask
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    test("3 drivetrain runs - LOF with consenter, biobank encounter, entlink")
    {
        val insertLOF: String = 
        """
        Insert Data
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.obolibrary.org/obo/OBI_0001352> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007601> "B" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007609> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007604> "http://purl.obolibrary.org/obo/PR_O43657"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007608> "eve.UPENN_Freeze_One.L2.M3.lofMatrix.txt" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007605> "TSPAN6(ENSG00000000003)"^^<http://www.w3.org/2001/XMLSchema#string> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007602> "UPENN_UPENN2358_96b80197-66d2-4d59-97f5-632bd13b95e5" .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007606> "1"^^<http://www.w3.org/2001/XMLSchema#integer> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007607> "http://transformunify.org/ontologies/TURBO_0000591"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/alleleInfo/b78dc9117b0f4654a380cf20b08010bf> <http://transformunify.org/ontologies/TURBO_0007603> "http://transformunify.org/ontologies/TURBO_0000451"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertLOF)
        
        val insertConsenter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_consenterShortcuts
            {
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000608> "4" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000604> "4-May-69" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000502> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000605> "1969-05-04"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000607> "http://purl.obolibrary.org/obo/OMRSE_00000138"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000603> "participants_1_mrn.csv" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000609> "CCH" .
                <http://localhost:8080/source/consenter/51b2b027ed3744569b287292606bd033> <http://transformunify.org/ontologies/TURBO_0000606> "F" .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertConsenter)
        
        runDrivetrainTestStack()
        
        val insertEncounter: String =
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_encounterShortcuts
            {
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000629> "CGI" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000527> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000628> "B" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000630> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000627> "61.2244897959"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000635> "18.8284850727"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000624> "1/15/2017" .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000625> "2017-01-15"^^<http://www.w3.org/2001/XMLSchema#date> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000626> "180.34"^^<http://www.w3.org/2001/XMLSchema#float> .
                <http://localhost:8080/source/encounter/0d1f8f8df8164ba6a7501252e2f15ba7> <http://transformunify.org/ontologies/TURBO_0000623> "encounters_bb_1_encounter_pack_id.csv" .
            
            }
        }
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEncounter)
        
        val insertEntLink: String = 
        """
        Insert Data
        {
            Graph pmbb:Shortcuts_entLinkShortcuts
            {
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001608> "B" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003603> "participants_1_mrn_to_encounters_bb_1_encounter_pack_id_join.csv" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003610> "http://transformunify.org/ontologies/TURBO_0000413"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001609> "CGI" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://transformunify.org/ontologies/TURBO_0001610> "http://transformunify.org/ontologies/TURBO_0000420"^^<http://www.w3.org/2001/XMLSchema#anyURI> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000503> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0000302> <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003608> "4" .
                <http://localhost:8080/source/consenterCrid/2ddd1880c8da448d8742f07da37b1310> <http://transformunify.org/ontologies/TURBO_0003609> "CCH" .
                <http://localhost:8080/source/encounterCrid/d0be2b74a1b742848306a991be2a1597> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://transformunify.org/ontologies/TURBO_0000533> .
            }
        }  
        """
        
        helper.updateSparql(cxn, sparqlPrefixes + insertEntLink)
        
        runDrivetrainTestStack()
        
        val ask: String =
        """
        Ask
        {
            Graph pmbb:expanded
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask).get should be (true)
        
        val ask2: String =
        """
        Ask
        {
            Graph pmbb:LOFShortcuts_LOFShortcuts
            {
                ?allele a obo:OBI_0001352 .
            }
        }  
        """
        
        helper.querySparqlBoolean(cxn, sparqlPrefixes + ask2).get should be (false)
    }
    
    def runDrivetrainTestStack()
    {
        DrivetrainDriver.main(Array("expand"))
        DrivetrainDriver.main(Array("reftrack")) 
        DrivetrainDriver.main(Array("entlink", "false")) 
    }
}