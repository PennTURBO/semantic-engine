package edu.upenn.turbo

import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager
import org.eclipse.rdf4j.repository.Repository
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.model.IRI
import org.scalatest.BeforeAndAfter
import org.scalatest._

class PreExpansionChecksUnitTests extends FunSuiteLike with BeforeAndAfter with Matchers with ProjectwideGlobals
{
    val connect: ConnectToGraphDB = new ConnectToGraphDB()
    var cxn: RepositoryConnection = null
    var repoManager: RemoteRepositoryManager = null
    var repository: Repository = null
    val clearDatabaseAfterRun: Boolean = true
    val precheck = new SparqlPreExpansionChecks
    
    before
    {
        logger.info("Running a pre-expansion check test")
        val graphDBMaterials: TurboGraphConnection = connect.initializeGraphLoadData(false)
        cxn = graphDBMaterials.getConnection()
        repoManager = graphDBMaterials.getRepoManager()
        repository = graphDBMaterials.getRepository()
    }
    after
    {
        connect.closeGraphConnection(cxn, repoManager, repository, clearDatabaseAfterRun)
    }
    
    val participantAsFrom: String = " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> "
    val participantGraph: String = " <http://www.itmat.upenn.edu/biobank/Shortcuts_participantShortcuts> "
    val biobankAsFrom: String = " FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> "
    val biobankGraph: String = " <http://www.itmat.upenn.edu/biobank/Shortcuts_biobankEncounterShortcuts> "
    val healthcareAsFrom: String = 
      """ FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts>
          FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts1>
          FROM <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts2> """
    val healthcareGraph: String = 
      """ <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts>
          <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts1>
          <http://www.itmat.upenn.edu/biobank/Shortcuts_healthcareEncounterShortcuts2> """

    test("test check for valid birth shortcuts - all required info")
    {
        //helper.deleteAllTriplesInDatabase(cxn)
        
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000604 '1/15/1994' .
          turbo:part1 turbo:TURBO_0000605 '01-15-1994'^^xsd:date . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantBirthShortcuts(cxn, participantAsFrom) should be (true)
    }
    
    test("test check for valid birth shortcuts - just text")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000604 '1/15/1994' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantBirthShortcuts(cxn, participantAsFrom) should be (true)
    }
    
    test("test check for valid birth shortcuts - just xsd")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000605 '01-15-1994'^^xsd:date . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantBirthShortcuts(cxn, participantAsFrom) should be (false)
    }
    
    test("test check for valid biosex shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000606 'F' .
          turbo:part1 turbo:TURBO_0000607 turbo:female . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantBiosexShortcuts(cxn, participantAsFrom) should be (true)
    }
    
    test("test check for valid biosex shortcuts - just text")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000606 'F' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantBiosexShortcuts(cxn, participantAsFrom) should be (true)
    }
    
    test("test check for valid biosex shortcuts - just xsd")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000607 turbo:female . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantBiosexShortcuts(cxn, participantAsFrom) should be (false)
    }
    
    test("test check for valid race shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000614 turbo:someRaceURI .
          turbo:part1 turbo:TURBO_0000615 'white' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantRaceShortcuts(cxn, participantAsFrom) should be (true)
    }
    
    test("test check for valid race shortcuts - just text")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000615 'black' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantRaceShortcuts(cxn, participantAsFrom) should be (false)
    }
    
    test("test check for valid race shortcuts - just xsd")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000614 'asian' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidParticipantRaceShortcuts(cxn, participantAsFrom) should be (false)
    }
    
    test("test check biosex uris are valid - three valid")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI . 
          turbo:part2 a turbo:TURBO_0000502 .
          turbo:part2 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .
          turbo:part3 a turbo:TURBO_0000502 .
          turbo:part3 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000133"^^xsd:anyURI . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkBiosexURIsAreValid(cxn, participantAsFrom) should be (true)
    }
    
    test("test check biosex uris are valid - invalid male")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI . 
          turbo:part2 a turbo:TURBO_0000502 .
          turbo:part2 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/not_a_gid"^^xsd:anyURI . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkBiosexURIsAreValid(cxn, participantAsFrom) should be (false)
    }
    
    test("test check biosex uris are valid - invalid female")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/not_a_gid"^^xsd:anyURI . 
          turbo:part2 a turbo:TURBO_0000502 .
          turbo:part2 turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkBiosexURIsAreValid(cxn, participantAsFrom) should be (false)
    }
    
    test("test check for valid hc encounter date shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000644 '1/15/1994' .
          turbo:enc1 turbo:TURBO_0000645 '01-15-1994'^^xsd:date . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidHealthcareEncounterDateShortcuts(cxn, healthcareAsFrom) should be (true)
    }
    
    test("test check for valid hc encounter date shortcuts - just text")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000644 '1/15/1994' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidHealthcareEncounterDateShortcuts(cxn, healthcareAsFrom) should be (true)
    }
    
    test("test check for valid hc encounter date shortcuts - just xsd")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000645 '01-15-1994'^^xsd:date . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidHealthcareEncounterDateShortcuts(cxn, healthcareAsFrom) should be (false)
    }
    
    test("test check for valid bb encounter date shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000624 '1/15/1994' .
          turbo:enc1 turbo:TURBO_0000625 '01-15-1994'^^xsd:date . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidBiobankEncounterDateShortcuts(cxn, biobankAsFrom) should be (true)
    }
    
    test("test check for valid bb encounter date shortcuts - just text")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000624 '1/15/1994' . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidBiobankEncounterDateShortcuts(cxn, biobankAsFrom) should be (true)
    }
    
    test("test check for valid bb encounter date shortcuts - just xsd")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000625 '01-15-1994'^^xsd:date . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidBiobankEncounterDateShortcuts(cxn, biobankAsFrom) should be (false)
    }
    
    test("test check for valid healthcare encounter diagnosis shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 .
          turbo:diagnosis1 turbo:TURBO_0004601 "401.9" .
          turbo:diagnosis1 turbo:TURBO_0004602 "ICD9" .
          turbo:diagnosis1 turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890" }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterDiagnosisShortcuts(cxn, healthcareGraph) should be (true)
    }
    
    test("test check for valid healthcare diagnosis registry shortcuts - missing reg text")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 .
          turbo:diagnosis1 turbo:TURBO_0004601 "401.9" .
          turbo:diagnosis1 turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890" }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterDiagnosisShortcuts(cxn, healthcareGraph) should be (false)
    }
    
    test("test check for valid healthcare diagnosis shortcuts - missing reg uri")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 .
          turbo:diagnosis1 turbo:TURBO_0004601 "401.9" .
          turbo:diagnosis1 turbo:TURBO_0004602 "ICD9" . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterDiagnosisShortcuts(cxn, healthcareGraph) should be (true)
    }
    
    test("test check for valid healthcare encounter diagnosis shortcuts - missing diag code")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:diagnosis1 .
          turbo:diagnosis1 a obo:OGMS_0000073 .
          turbo:diagnosis1 turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890" .
          turbo:diagnosis1 turbo:TURBO_0004602 "ICD9" . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterDiagnosisShortcuts(cxn, healthcareGraph) should be (true)
    }
    
    test("test check for valid healthcare encounter diagnosis shortcuts - not attached to an encounter")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:diagnosis1 a obo:OGMS_0000073 .
          turbo:diagnosis1 turbo:TURBO_0004603 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890" .
          turbo:diagnosis1 turbo:TURBO_0004602 "ICD9" .
          turbo:diagnosis1 turbo:TURBO_0004601 "401.9" . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterDiagnosisShortcuts(cxn, healthcareGraph) should be (false)
    }
    
    test("test check for valid healthcare encounter prescription shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:prescription1 .
          turbo:prescription1 a obo:PDRO_0000001 .
  		    turbo:prescription1 turbo:TURBO_0005611 'dried semen of the Western Lowland gorilla' .
  		    turbo:prescription1 turbo:TURBO_0005601  '999' . 
  		    }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterPrescriptionShortcuts(cxn, healthcareGraph) should be (true)
    }
    
    // medicine string used to be required, has since been made optional
    test("test check for valid healthcare encounter prescription shortcuts - missing medicine string")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:prescription1 .
          turbo:prescription1 a obo:PDRO_0000001 .
  		    # turbo:prescription1 turbo:TURBO_0005611 'dried semen of the Western Lowland gorilla' .
  		    turbo:prescription1 turbo:TURBO_0005601  '999' . 
  		    }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterPrescriptionShortcuts(cxn, healthcareGraph) should be (true)
    }
    
    test("test check for valid healthcare encounter prescription shortcuts - missing identifier")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 obo:RO_0002234 turbo:prescription1 .
          turbo:prescription1 a obo:PDRO_0000001 .
  		    turbo:prescription1 turbo:TURBO_0005611 'dried semen of the Western Lowland gorilla' .
  		    # turbo:prescription1 turbo:TURBO_0005601  '999' . 
  		    }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterPrescriptionShortcuts(cxn, healthcareGraph) should be (false)
    }
    
    test("test check for valid healthcare encounter prescription shortcuts - not attached to an encounter")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:prescription1 a obo:PDRO_0000001 .
  		    turbo:prescription1 turbo:TURBO_0005611 'dried semen of the Western Lowland gorilla' .
  		    turbo:prescription1 turbo:TURBO_0005601  '999' . 
  		    }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForValidEncounterPrescriptionShortcuts(cxn, healthcareGraph) should be (false)
    }
    
    test("test check for valid participant shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          turbo:part1 turbo:TURBO_0000610 turbo:registry1 .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredParticipantShortcuts(cxn, participantGraph) should be (true)
    }
    
    test("test check for valid participant shortcuts - missing registry")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000608 '1' .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredParticipantShortcuts(cxn, participantGraph) should be (false)
    }
    
    test("test check for valid participant shortcuts - missing dataset")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000608 '1' . 
          turbo:part1 turbo:TURBO_0000610 turbo:registry1 .
         }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredParticipantShortcuts(cxn, participantGraph) should be (false)
    }
    
    test("test check for valid participant shortcuts - missing psc")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_participantShortcuts {
          turbo:part1 a turbo:TURBO_0000502 .
          turbo:part1 turbo:TURBO_0000603 'dataset1' .
          turbo:part1 turbo:TURBO_0000610 turbo:registry1 .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredParticipantShortcuts(cxn, participantGraph) should be (false)
    }
    
    test("test check for valid healthcare encounter shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000643 'dataset1' .
          turbo:enc1 turbo:TURBO_0000648 '1' .
          turbo:enc1 turbo:TURBO_0000650 'http://transformunify.org/ontologies/TURBO_0000440'^^xsd:anyURI .}}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredHealthcareEncounterShortcuts(cxn, healthcareAsFrom) should be (true)
    }
    
    test("test check for valid healthcare encounter shortcuts - missing dataset")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000648 '1' .
          turbo:enc1 turbo:TURBO_0000650 'http://transformunify.org/ontologies/TURBO_0000440'^^xsd:anyURI .}}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredHealthcareEncounterShortcuts(cxn, healthcareAsFrom) should be (false)
    }
    
     test("test check for valid healthcare encounter shortcuts - missing psc")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000643 'dataset1' .
          turbo:enc1 turbo:TURBO_0000650 'http://transformunify.org/ontologies/TURBO_0000440'^^xsd:anyURI .
          }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredHealthcareEncounterShortcuts(cxn, healthcareAsFrom) should be (false)
    }
     
    test("test check for valid healthcare encounter shortcuts - missing registry")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_healthcareEncounterShortcuts {
          turbo:enc1 a obo:OGMS_0000097 .
          turbo:enc1 turbo:TURBO_0000643 'dataset1' .
          turbo:enc1 turbo:TURBO_0000648 '1' .}}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredHealthcareEncounterShortcuts(cxn, healthcareAsFrom) should be (false)
    }
    
    test("check for valid healthcare encounters shortcuts - using multiple named graphs")
    {
        val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
            turbo:enc1 a obo:OGMS_0000097 .
            turbo:enc1 turbo:TURBO_0000643 'dataset1' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts1 {
              turbo:enc1 turbo:TURBO_0000648 '1' .
              turbo:enc1 turbo:TURBO_0000643 'dataset2 ' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts2 {
              turbo:enc1 turbo:TURBO_0000650 'http://transformunify.org/ontologies/TURBO_0000440'^^xsd:anyURI .
              turbo:enc1 turbo:TURBO_0000643 'dataset3 ' .
          }
          }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredHealthcareEncounterShortcuts(cxn, healthcareAsFrom) should be (true)
    }
     
    test("test check for valid biobank encounter shortcuts - all required info")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 '1' .
          turbo:enc1 turbo:TURBO_0000630 "http://transformunify.org/ontologies/biobankRegistry"^^xsd:anyURI .}}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredBiobankEncounterShortcuts(cxn, biobankGraph) should be (true)
    }
    
    test("test check for valid biobank encounter shortcuts - missing registry")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000628 '1' .}}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredBiobankEncounterShortcuts(cxn, biobankGraph) should be (false)
    }
    
    test("test check for valid biobank encounter shortcuts - missing dataset")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000628 '1' .
          turbo:enc1 turbo:TURBO_0000630 "http://transformunify.org/ontologies/biobankRegistry"^^xsd:anyURI .}}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredBiobankEncounterShortcuts(cxn, biobankGraph) should be (false)
    }
    
    test("test check for valid biobank encounter shortcuts - missing psc")
    {
        val insert: String = """
          INSERT DATA {graph pmbb:Shortcuts_biobankEncounterShortcuts {
          turbo:enc1 a turbo:TURBO_0000527 .
          turbo:enc1 turbo:TURBO_0000623 'dataset1' .
          turbo:enc1 turbo:TURBO_0000630 "http://transformunify.org/ontologies/biobankRegistry"^^xsd:anyURI . }}
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForRequiredBiobankEncounterShortcuts(cxn, biobankGraph) should be (false)
    }
    
    test("test check for unexpected classes - all valid classes")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts
          {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:hcenc1 obo:RO_0002234 turbo:diagnosis1 .
              turbo:diagnosis1 a obo:OGMS_0000073 .
              turbo:hcenc1 obo:RO_0002234 turbo:prescription1 .
              turbo:prescription1 a obo:PDRO_0000001 .
          }
          # this check should only check shortcut graphs, so the data below should not cause a failure
          graph pmbb:postExpansionCheck {
              turbo:someData a turbo:someDataClass .
          }
          graph pmbb:expanded {
              turbo:someData a turbo:someDataClass .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedClasses(cxn, healthcareGraph + biobankGraph + participantGraph) should be (true)
    }
    
    test("test check for unexpected classes - unexpected in participant named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
              turbo:part2 a turbo:notAParticipant .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedClasses(cxn, participantGraph) should be (false)
    }
    
    test("test check for unexpected classes - unexpected in biobank encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:enc2 a turbo:notAnEncounter .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedClasses(cxn, participantGraph + biobankGraph) should be (false)
    }
    
    test("test check for unexpected classes - unexpected in healthcare encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:enc2 a turbo:notAnEncounter .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedClasses(cxn, participantGraph + healthcareGraph) should be (false)
    }
    
    test("test check for unexpected predicates - all valid predicates")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedPredicates(cxn, participantGraph + biobankGraph + healthcareGraph) should be (true)
    }
    
    test("test check for unexpected predicates - unexpected predicate in participant named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 turbo:notAShortcut 'abc' .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts
          {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedPredicates(cxn, participantGraph + biobankGraph + healthcareGraph) should be (false)
    }
    
    test("test check for unexpected predicates - unexpected predicate in biobank encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
              turbo:bbenc1 turbo:notAShortcut 'abc' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedPredicates(cxn,  participantGraph + biobankGraph) should be (false)
    }
    
    test("test check for unexpected predicates - unexpected predicate in healthcare encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
              turbo:hcenc1 turbo:notAShortcut 'abc' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkForUnexpectedPredicates(cxn, participantGraph + healthcareGraph) should be (false)
    }
    
    test("test check all subjects have a type - all subjects have type")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 a turbo:TURBO_0000502 .
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts
          {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllSubjectsHaveAType(cxn, participantGraph + biobankGraph + healthcareGraph) should be (true)
    }
    
    test("test check all subjects have a type - typeless subject in participant named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts
          {
              turbo:hcenc1 a obo:OGMS_0000097 .
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllSubjectsHaveAType(cxn, participantGraph + biobankGraph + healthcareGraph) should be (false)
    }
    
    test("test check all subjects have a type - typeless subject in biobank encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllSubjectsHaveAType(cxn, participantGraph + biobankGraph) should be (false)
    }
    
    test("test check all subjects have a type - typeless subject in healthcare encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllSubjectsHaveAType(cxn, participantGraph + healthcareGraph) should be (false)
    }
    
    test("test check all objects are literals - all objects in range")
    {    
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 'dataset1' .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 '1' .
              turbo:hcenc1 a obo:OGMS_0000097 .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 a turbo:TURBO_0000527 .
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 '1' .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllObjectsAreLiterals(cxn, participantGraph + biobankGraph + healthcareGraph) should be (true)
    }
    
    test("test check all objects are literals - object out of range in participant named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 turbo:dataset1 .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 a turbo:TURBO_0000502 .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllObjectsAreLiterals(cxn, participantGraph) should be (false)
    }
    
    test("test check all objects are literals - object out of range in healthcare encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 turbo:dataset1 .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_healthcareEncounterShortcuts {
              turbo:hcenc1 turbo:TURBO_0000643 'dataset1' .
              turbo:hcenc1 turbo:TURBO_0000648 turbo:1 .
              turbo:hcenc1 a obo:OGMS_0000097 .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllObjectsAreLiterals(cxn, participantGraph + healthcareGraph) should be (false)
    }
    
    test("test check all objects are literals - object out of range in biobank encounter named graph")
    {
         val insert: String = """
          INSERT DATA {
          graph pmbb:Shortcuts_participantShortcuts {
              turbo:part1 turbo:TURBO_0000603 turbo:dataset1 .
              turbo:part1 turbo:TURBO_0000608 '1' .
              turbo:part1 a turbo:TURBO_0000502 .
          }
          graph pmbb:Shortcuts_biobankEncounterShortcuts {
              turbo:bbenc1 turbo:TURBO_0000623 'dataset1' .
              turbo:bbenc1 turbo:TURBO_0000628 turbo:1 .
              turbo:bbenc1 a turbo:TURBO_0000527 .
          }
               }
          """
        helper.updateSparql(cxn, sparqlPrefixes + insert)
        precheck.checkAllObjectsAreLiterals(cxn, participantGraph + biobankGraph) should be (false)
    }
}