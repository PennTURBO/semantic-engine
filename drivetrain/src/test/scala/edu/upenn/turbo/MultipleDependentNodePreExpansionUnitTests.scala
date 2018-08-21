package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._

class MultipleDependentNodePreExpansionUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val precheck = new SparqlPreExpansionChecks
    
    val healthcareEncounterShortcutGraphs: String = 
    """
        FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts> 
        FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts1> 
        FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts2>
    """
    
    before
    {
        logger.info("Running a multiple dependent node test")
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
  
    test("test check for multiple participant dependent nodes - one of each")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1
          turbo:TURBO_0000603 "handcrafted_parts.csv" ;
          turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
          turbo:TURBO_0000605 "1969-05-04"^^xsd:date ;
          a turbo:TURBO_0000502 ;
          turbo:TURBO_0000608 "4" ;
          turbo:TURBO_0000604 "04/May/1969" ;
          turbo:TURBO_0000606 "F" ;
          turbo:TURBO_0000609 'registry1' ;
          turbo:TURBO_0000610 turbo:reg1 . }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (true)
    }
    
    test("test check for multiple participant dependent nodes - two pscs")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000608 '2' . }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two types")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 a obo:OGMS_0000097 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' . }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two datasets")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000603 'dataset2' .
          turbo:part1 turbo:TURBO_0000608 '1' .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two gids")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI .
          turbo:part1 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two dob text")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000604 "04/May/1969" .
          turbo:part1 turbo:TURBO_0000604 "04/May/1970" .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two dob xsd")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000605 "1969-05-04"^^xsd:date .
          turbo:part1 turbo:TURBO_0000605 "1970-05-04"^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two gid text")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000606 "F" .
          turbo:part1 turbo:TURBO_0000606 "M" .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two registries")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000606 "F" .
          turbo:part1 turbo:TURBO_0000610 turbo:reg1 .
          turbo:part1 turbo:TURBO_0000610 turbo:reg2 .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two reg dens")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000606 "F" .
          turbo:part1 turbo:TURBO_0000610 turbo:reg1 .
          turbo:part1 turbo:TURBO_0000609 'registry1' .
          turbo:part1 turbo:TURBO_0000609 'registry2' .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("test check for multiple participant dependent nodes - two reg denoters")
    {
         val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000606 "F" .
          turbo:part1 turbo:TURBO_0000606 "M" .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleParticipantDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - one of each")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (true)
    }
    
    test("check for multiple biobank encounter dependent nodes - two types")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 a turbo:TURBO_0000502 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two datasets")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000623 'dataset2' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two symbols")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000628 'B' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two registries")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000630 turbo:reg2 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two reg denoters")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000629 'registry2' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two BMIs")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000635 '30' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two weights")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '30' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two heights")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000626 '30' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two date strings")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000624 '12/1/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple biobank encounter dependent nodes - two xsd dates")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 'A' . 
          turbo:enc1 turbo:TURBO_0000630 turbo:reg1 .
          turbo:enc1 turbo:TURBO_0000629 'registry1' .
          turbo:enc1 turbo:TURBO_0000635 '20' .
          turbo:enc1 turbo:TURBO_0000627 '20' .
          turbo:enc1 turbo:TURBO_0000626 '20' .
          turbo:enc1 turbo:TURBO_0000624 '12/31/1968' .
          turbo:enc1 turbo:TURBO_0000625 '12/31/1968'^^xsd:date .
          turbo:enc1 turbo:TURBO_0000625 '12/1/1968'^^xsd:date .
           }}
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleBiobankEncounterDependentNodes(cxn, " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> ") should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - one of each")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (true)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two encounter types")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 a turbo:TURBO_0000527 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - multiple datasets")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
            turbo:enc1 turbo:TURBO_0000643 'dataset2' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
            turbo:enc1 turbo:TURBO_0000643 'dataset3' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (true)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two symbols")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000648 'B' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two registries")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000650 turbo:reg2 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two BMIs")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000655 '30' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two weights")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '30' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two heights")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000646 '30' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two date strings")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000644 '12/12/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two date xsd")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
            turbo:enc1 turbo:TURBO_0000645 '12/31/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two prescriptions")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
            
            turbo:enc1 obo:RO_0002234 turbo:prescript2 .
            turbo:prescript2 a obo:PDRO_0000001 .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (true)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two prescription types")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 a turbo:notAType .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultiplePrescriptionDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two prescription 5611")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005611 'something else' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultiplePrescriptionDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two prescription 5601")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 'something else' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultiplePrescriptionDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two prescription 5612")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005612 'something else' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultiplePrescriptionDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two diagnoses")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
            
            turbo:enc1 obo:RO_0002234 turbo:diagnosis2 .
            turbo:diagnosis2 a obo:OGMS_0000073 .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleHealthcareEncounterDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (true)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two diagnosis types")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 a turbo:notAType .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleDiagnosisDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two diagnosis 4602")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004602 'medicinal housecat excrement' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleDiagnosisDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two diagnosis 4603")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004603 'medicinal housecat excrement' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleDiagnosisDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
    
    test("check for multiple healthcare encounter dependent nodes - two diagnosis 4601")
    {
        val insert: String = """
          INSERT DATA 
          {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
            turbo:enc1 turbo:TURBO_0000648 'A' . 
            turbo:enc1 turbo:TURBO_0000650 turbo:reg1 .
            turbo:enc1 turbo:TURBO_0000655 '20' .
            turbo:enc1 turbo:TURBO_0000647 '20' .
            turbo:enc1 turbo:TURBO_0000646 '20' .
            turbo:enc1 turbo:TURBO_0000644 '12/1/1968' .
            turbo:enc1 turbo:TURBO_0000645 '12/12/1968'^^xsd:date .
           }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
            turbo:enc1 obo:RO_0002234 turbo:prescript1 .
            turbo:prescript1 a obo:PDRO_0000001 .
            turbo:prescript1 turbo:TURBO_0005611 'blood of a firstborn caterpillar' .
            turbo:prescript1 turbo:TURBO_0005601 '333.3' .
            turbo:prescript1 turbo:TURBO_0005612 'ICD-10' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
            turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
            turbo:diagnosis1 a obo:OGMS_0000073 .
            turbo:diagnosis1 turbo:TURBO_0004602 'something here' .
            turbo:diagnosis1 turbo:TURBO_0004601 'medicinal housecat excrement' .
            turbo:diagnosis1 turbo:TURBO_0004603 'something else' .
            turbo:diagnosis1 turbo:TURBO_0004601 'another thing' .
          }
          }
          """
        update.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForMultipleDiagnosisDependentNodes(cxn, healthcareEncounterShortcutGraphs) should be (false)
    }
}