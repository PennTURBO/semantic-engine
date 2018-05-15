package edu.upenn.turbo

import org.eclipse.rdf4j.query.QueryLanguage
import org.eclipse.rdf4j.query.TupleQueryResult
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.scalatest._

// STILL USING OPENRDF SESAME
// MOVE TO RDF4J MAY HAVE GROWING PAINS
// BUT GOTTA DO IT EVENTUALLY
// (WOULD PROBABLY BREAK COMPATIBILITY WITH BLAZEGRAPH ANS STARDOG.)

class FullStackPostReorgSparql {

  val InstantiationUUID = java.util.UUID.randomUUID().toString()
  val InstOutpContUUID = java.util.UUID.randomUUID().toString()

  val ReftrackingUUID = java.util.UUID.randomUUID().toString()
  val ReftrackedOutpContUUID = java.util.UUID.randomUUID().toString()
  val RetiredOutpContUUID = java.util.UUID.randomUUID().toString()

  val Share2PartProcUUID = java.util.UUID.randomUUID().toString()
  val Share2PartOutpContUUID = java.util.UUID.randomUUID().toString()

  val RetirementUUID = java.util.UUID.randomUUID().toString()
  val UntrackedRetiredOutpContUUID = java.util.UUID.randomUUID().toString()

  val ConclusionationUUID = java.util.UUID.randomUUID().toString()
  val ConclusionsOutpContUUID = java.util.UUID.randomUUID().toString()

  val SexRecodingContUUID = java.util.UUID.randomUUID().toString()

  val SparqlPrefixes = """
			PREFIX  :     <http://transformunify.org/ontologies/>
			PREFIX  dc11: <http://purl.org/dc/elements/1.1/>
		  PREFIX  dc: <http://purl.org/dc/elements/1.1/>
			PREFIX  obo:  <http://purl.obolibrary.org/obo/>
			PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
			PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
			PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>
			PREFIX  turbo: <http://transformunify.org/ontologies/>
			PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>
			PREFIX  nci:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>
			"""

  def InsertEncScTrips(cxn: RepositoryConnection) = {

    // this comes from turbo/data/csv/handcrafted_encs.csv instantiated with turbo/karma_models/encounters_with_shortcuts-model.ttl
    // the UUIDs in URIs won't be the same if it is re-instantiated

    // DATASET TITLES WERE EDITED AND NO LONGER MATCH TEH REFERENCED CSV & MODEL
    //HAYDEN 10/12 9:48 Changing named graph to match what the code is expecting ("encounterShortcuts")
    val ScGraph = "http://transformunify.org/ontologies/encounterShortcuts"

    /*
     * MAM 30oct17 added BMI shortcuts
     */
    
    val ScEncsTurtle = """
<http://transformunify.org/ontologies/encounter/e0b106472b144bf183d4a613c61dd147>
  turbo:TURBO_0000649 "ICD-10" ;
  turbo:TURBO_0000643 "embedded_handcrafted_encs.csv" ;
  turbo:TURBO_0000661 "J44.9" ;
  a <http://transformunify.org/ontologies/OGMS_0000097> ;
  turbo:TURBO_0000645 "2015-12-05"^^xsd:date ;
  turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^xsd:anyURI ;
  turbo:TURBO_0000644 "12/05/2015" ;
  turbo:TURBO_0000655 "24.5"^^xsd:float ;
  turbo:TURBO_0000648 "102" .

<http://transformunify.org/ontologies/encounter/5494052271bf492f849ad29da0a5ae20>
  turbo:TURBO_0000643 "embedded_handcrafted_encs.csv" ;
  a <http://transformunify.org/ontologies/OGMS_0000097> ;
  turbo:TURBO_0000661 "602.9" ;
  turbo:TURBO_0000644 "11/25/2015" ;
  turbo:TURBO_0000648 "103" ;
  turbo:TURBO_0000645 "2015-11-25"^^xsd:date ;
  turbo:TURBO_0000649 "ICD-9" ;
  turbo:TURBO_0000655 "24.5"^^xsd:float ;
  turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71890"^^xsd:anyURI .

<http://transformunify.org/ontologies/encounter/dc6d7d89dfab4bbcbf449729e4d666d4>
  a <http://transformunify.org/ontologies/OGMS_0000097> ;
  turbo:TURBO_0000645 "2015-12-05"^^xsd:date ;
  turbo:TURBO_0000648 "102" ;
  turbo:TURBO_0000661 "I50.9" ;
  turbo:TURBO_0000649 "ICD-10" ;
  turbo:TURBO_0000643 "embedded_handcrafted_encs.csv" ;
  turbo:TURBO_0000663 "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C71892"^^xsd:anyURI ;
  turbo:TURBO_0000655 "24.5"^^xsd:float ;
  turbo:TURBO_0000644 "12/05/2015" .
"""
    InsertFromString(cxn, ScEncsTurtle, ScGraph)
  }

  def ExpandEncScTrips(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // create a container ID ?

    // instantiation process ID... not dependent on anything in the graph
    //    val InstantiationUUID = "placeholder"

    val ExpansionSparql = """
  INSERT {
          # get graph name from scala variable?
          GRAPH <http://www.itmat.upenn.edu/biobank/expanded> {
            ?EncDate1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:ProcStartTimeMeas .
            ?EncDate1 turbo:TURBO_0006512 ?encDateTextVal .
            ?EncDate1 turbo:TURBO_0006511 ?encDateMeasVal .
            ?EncDate1 obo:IAO_0000136 ?EncStart1 .

            ?Encounter1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OGMS_0000097 .
            ?Encounter1 obo:OBI_0000299 ?DiagCrid1 .
            ?Encounter1 turbo:previousUriText ?previousUriText .
            
            ?EncID1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:EncounterID .
            ?EncID1 obo:IAO_0000219 ?Encounter1 .
            ?EncID1 turbo:TURBO_0006510 ?EncID_LV .
            
            ?EncStart1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000511 .
            ?EncStart1 obo:RO_0002223 ?Encounter1 .
            
            ?DiagCrid1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000553 .
            ?DiagCrid1 obo:BFO_0000051 ?DiagCodeRegID1 .
            ?DiagCrid1 obo:BFO_0000051 ?DiagCodeSymb1 .
            
            ?Dataset1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:IAO_0000100 .
            ?Dataset1 obo:BFO_0000051 ?DiagCodeRegID1 .
            ?Dataset1 obo:BFO_0000051 ?DiagCodeSymb1 .
            ?Dataset1 obo:BFO_0000051 ?EncDate1 .
            ?Dataset1 obo:BFO_0000051 ?EncID1 .
            ?Dataset1 <http://purl.org/dc/elements/1.1/title> ?dsTitle .
            
            ?Instantiation1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000522 .
            ?Instantiation1 obo:OBI_0000293 ?Dataset1 .
            ?Instantiation1 rdfs:label "Inst/Exp Proc" .

            ?DiagCodeSymb1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000554 .
            ?DiagCodeSymb1 turbo:TURBO_0006510 ?diagCodeLV .
            
            ?DiagCodeRegID1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> turbo:TURBO_0000555 .
            ?DiagCodeRegID1 obo:IAO_0000219 ?diagCodeRegURI .
            ?DiagCodeRegID1 turbo:TURBO_0006512 ?diagCodeRegTextVal .
            
            ?OutpContainer a turbo:Container .
            ?OutpContainer rdfs:label "Inst/Exp Outp Cont" .

            ?Encounter1 turbo:member ?OutpContainer .
            ?EncStart1 turbo:member ?OutpContainer .
            ?DiagCrid1 turbo:member ?OutpContainer .
            
          }
        }
        WHERE
            { GRAPH <http://transformunify.org/ontologies/shortcuts>
            {
                      ?encFromKarma
                      a                     obo:OGMS_0000097 ;
                      turbo:TURBO_0000648     ?EncID_LV ;
                      turbo:TURBO_0000643   ?dsTitle.
                      
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?Encounter1)
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?EncID1)
            BIND(str(?encFromKarma) AS ?previousUriText)
            BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("dataset", ?dsTitle)))) AS ?Dataset1)
                      
                      optional {
                      ?encFromKarma turbo:TURBO_0000663  ?diagCodeRegURIString ;
                      turbo:TURBO_0000649  ?diagCodeRegTextVal ;
                      turbo:TURBO_0000661  ?diagCodeLV .
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?DiagCrid1)
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?DiagCodeRegID1)
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?DiagCodeSymb1)
            BIND(uri(?diagCodeRegURIString) AS ?diagCodeRegURI)
                      }
                      
                      optional {
                      ?encFromKarma turbo:TURBO_0000644  ?encDateTextVal ;
                      turbo:TURBO_0000645  ?encDateMeasVal .
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?EncDate1)
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?EncStart1)
                      }
                      

            
            BIND(uri("http://transformunify.org/ontologies/""" + InstantiationUUID + """") AS ?Instantiation1)
              
            BIND(uri("http://transformunify.org/ontologies/""" + InstOutpContUUID + """") AS ?OutpContainer)
            
            # BIND(strafter(str(?p), "http://transformunify.org/ontologies/participant/") AS ?pk)
          }}
      """

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def CheckSinglePartExp(cxn: RepositoryConnection) = {

    val CheckSparql = """
select 
?dstitle ?gidclass ?gidVal ?pscVal ?dobTval ?dobXval
where {
    ?r2rInst
        a turbo:TURBO_0000522 ;
        obo:OBI_0000293 ?dataset ;
        obo:OBI_0000299 ?InstOutpCont .
    ?InstOutpCont a turbo:Container .
    ?birth
        a obo:UBERON_0035946 ;
        turbo:member ?InstOutpCont .
    ?participant
        a turbo:TURBO_0000502 ;
        turbo:TURBO_0006601 ?TURBO_0006601 ;
        turbo:member ?InstOutpCont ;
        obo:RO_0000086 ?bioSex ;
        turbo:TURBO_0000303 ?birth .
    # "http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8" ;
    ?dataset
        a obo:IAO_0000100 ;
        obo:BFO_0000051 ?gid, ?psc, ?dob ;
        dc11:title ?dstitle .
    # "handcrafted_parts.csv"
    ?bioSex
        a obo:PATO_0000047 ;
        turbo:member ?InstOutpCont .
    ?gid
        a ?gidclass ;
        turbo:TURBO_0006510 ?gidVal ;
        obo:IAO_0000136 ?participant ;
        obo:BFO_0000050 ?dataset .
    # "M"
    # http://purl.obolibrary.org/obo/OMRSE_00000141
    ?psc
        a turbo:TURBO_0000503 ;
        obo:IAO_0000219 ?participant ;
        turbo:TURBO_0006510 ?pscVal ;
        obo:BFO_0000050 ?dataset .
    # "121" 
    ?dob
        a <http://www.ebi.ac.uk/efo/EFO_0004950> ;
        turbo:TURBO_0006510 ?dobTval ;
        turbo:TURBO_0006511 ?dobXval ;
        obo:IAO_0000136 ?birth ;
        obo:BFO_0000050 ?dataset .
    # turbo:TURBO_0006510 "12/30/1971" ;
    # turbo:TURBO_0006511 "1971-12-30"^^xsd:date ;
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def CheckExpandedEncScTrips(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val CheckSparql = """
ASK
WHERE
  { GRAPH :postExpansionCheck
      { FILTER ( ?count = 3 )
        { SELECT  (COUNT(?previousUriText) AS ?count)
          WHERE
            { { VALUES ( ?encDateTextVal ?encDateMeasVal ?diagCodeLV ?EncID_LV ?dsTitle ?diagCodeRegURI ?diagCodeRegTextVal ) {
                  ( "12/05/2015" "2015-12-05"^^xsd:date "J44.9" "102" "embedded_handcrafted_encs.csv" nci:C71892 "ICD-10" )
                  ( "12/05/2015" "2015-12-05"^^xsd:date "I50.9" "102" "embedded_handcrafted_encs.csv" nci:C71892 "ICD-10" )
                  ( "11/25/2015" "2015-11-25"^^xsd:date "602.9" "103" "embedded_handcrafted_encs.csv" nci:C71890 "ICD-9" )
                }
                ?EncDate1  rdf:type           :ProcStartTimeMeas ;
                          :TURBO_0006512    ?encDateTextVal ;
                          turbo:TURBO_0006511     ?encDateMeasVal ;
                          obo:IAO_0000136     ?EncStart1 .
                ?DiagCodeSymb1
                          rdf:type            :EncounterDiagCodeSymbol ;
                          :TURBO_0006510  ?diagCodeLV .
                ?Encounter1  rdf:type         obo:OGMS_0000097 ;
                          obo:OBI_0000299     ?DiagCrid1 ;
#                          :previousUriText    ?previousUriText .
                           :TURBO_0006601    ?previousUriText .
                ?EncID1   rdf:type            :EncounterID ;
                          obo:IAO_0000219     ?Encounter1 ;
                          :TURBO_0006510  ?EncID_LV .
                ?EncStart1  rdf:type          :TURBO_0000511 ;
                          obo:RO_0002223      ?Encounter1 .
                ?DiagCrid1  rdf:type          :DiagCrid ;
                          obo:BFO_0000051     ?DiagCodeRegID1 ;
                          obo:BFO_0000051     ?DiagCodeSymb1 .
                ?Dataset1  rdf:type           obo:IAO_0000100 ;
                          obo:BFO_0000051     ?DiagCodeRegID1 ;
                          obo:BFO_0000051     ?DiagCodeSymb1 ;
                          obo:BFO_0000051     ?EncDate1 ;
                          obo:BFO_0000051     ?EncID1 ;
                          dc11:title          ?dsTitle .
                ?Instantiation1
                          rdf:type            :R2RInstantiation ;
                          obo:OBI_0000293     ?Dataset1 .
                ?DiagCodeRegID1
                          rdf:type            :DiagCodeRegistryID ;
                          obo:IAO_0000219     ?diagCodeRegURI ;
                          :TURBO_0006512    ?diagCodeRegTextVal .
                ?OutpContainer
                          rdf:type            :Container .
                ?Encounter1  :member          ?OutpContainer .
                ?EncStart1  :member           ?OutpContainer .
                ?DiagCrid1  :member           ?OutpContainer
              }
            }
        }
      }
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def InsertPartScTrips(cxn: RepositoryConnection) = {

    // this comes from turbo/data/csv/handcrafted_encs.csv instantiated with turbo/karma_models/encounters_with_shortcuts-model.ttl
    // the UUIDs in URIs won't be the same if it is re-instantiated

    val ScGraph = "http://transformunify.org/ontologies/shortcuts"

    val ScPartsTurtle = """
<http://transformunify.org/ontologies/participant/f5a1aa29115347b080c061198fbc09a8>
  turbo:ScPart2BioSexUri "http://purl.obolibrary.org/obo/PATO_0000384"^^xsd:anyURI ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000603 "embedded_handcrafted_parts.csv" ;
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000606 "M" ;
  a turbo:TURBO_0000502 .

<http://transformunify.org/ontologies/participant/b9bbdb47c1b54cf2a4cbb974b7ef78f7>
  turbo:ScPart2BioSexUri "http://purl.obolibrary.org/obo/PATO_0000384"^^xsd:anyURI ;
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000603 "embedded_handcrafted_parts.csv" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000606 "M" .

<http://transformunify.org/ontologies/participant/946778837282486b9eab64640c4aadd3>
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000603 "embedded_handcrafted_parts.csv" ;
  turbo:TURBO_0000606 "F" ;
  turbo:ScPart2BioSexUri "http://purl.obolibrary.org/obo/PATO_0000383"^^xsd:anyURI ;
  turbo:TURBO_0000604 "12/30/1971" .

<http://transformunify.org/ontologies/participant/cc500523ea544310ac35cfe727323f3b>
  turbo:TURBO_0000604 "4/4/2000" ;
  turbo:TURBO_0000603 "embedded_handcrafted_parts.csv" ;
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000608 "131" ;
  turbo:TURBO_0000605 "2000-04-04"^^xsd:date ;
  a turbo:TURBO_0000502 ;
  turbo:ScPart2BioSexUri "http://purl.obolibrary.org/obo/PATO_0000383"^^xsd:anyURI .

<http://transformunify.org/ontologies/participant/1e122641fdd7462a86811f079d80ffcc>
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000608 "131" ;
  a turbo:TURBO_0000502 ;
  turbo:ScPart2BioSexUri "http://purl.obolibrary.org/obo/PATO_0000383"^^xsd:anyURI ;
  turbo:TURBO_0000603 "embedded_handcrafted_parts.csv" ;
  turbo:TURBO_0000604 "4/4/2000" ;
  turbo:TURBO_0000605 "2000-04-04"^^xsd:date .

<http://transformunify.org/ontologies/participant/487fdf891e464d6598f3935a85287582>
  turbo:TURBO_0000605 "2000-04-05"^^xsd:date ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000603 "embedded_handcrafted_parts.csv" ;
  turbo:TURBO_0000604 "4/5/2000" ;
  turbo:ScPart2BioSexUri "http://purl.obolibrary.org/obo/PATO_0000383"^^xsd:anyURI ;
  turbo:TURBO_0000608 "131" .
  """
    InsertFromString(cxn, ScPartsTurtle, ScGraph)
  }

  def InsertPartScTripsGidClarified(cxn: RepositoryConnection) = {

    // this comes from turbo/data/csv/handcrafted_encs.csv instantiated with turbo/karma_models/encounters_with_shortcuts-model.ttl
    // the UUIDs in URIs won't be the same if it is re-instantiated

    //HAYDEN 10/12 9:51 Changing named graph to match what the code is expecting ("participantShortcuts")
    val ScGraph = "http://transformunify.org/ontologies/participantShortcuts"

    val ScPartsTurtle = """
<http://transformunify.org/ontologies/participant/5fdde1e0f54f4a4fa9855be71240f0f8>
  a <http://transformunify.org/ontologies/TURBO_0000502> ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI ;
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000606 "M" .

<http://transformunify.org/ontologies/participant/e4f53c6f407947019fdcde317de0462d>
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000604 "12/30/1971" ;
  turbo:TURBO_0000606 "M" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000141"^^xsd:anyURI .

<http://transformunify.org/ontologies/participant/fb08701bac2f4a169612fd23500b2e9f>
  turbo:TURBO_0000604 "12/30/1971" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  turbo:TURBO_0000608 "121" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000605 "1971-12-30"^^xsd:date .

<http://transformunify.org/ontologies/participant/37fd0431e04140da973af908f5fdebfc>
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  turbo:TURBO_0000608 "131" ;
  turbo:TURBO_0000604 "4/4/2000" ;
  turbo:TURBO_0000605 "2000-04-04"^^xsd:date ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000606 "F" .

<http://transformunify.org/ontologies/participant/ed7c74146c0d4bde80a7e4bc12b42619>
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  turbo:TURBO_0000604 "4/4/2000" ;
  turbo:TURBO_0000608 "131" ;
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000605 "2000-04-04"^^xsd:date .

<http://transformunify.org/ontologies/participant/ca667124f8bb4a839eb6b436e68aed5f>
  turbo:TURBO_0000606 "F" ;
  turbo:TURBO_0000603 "handcrafted_parts.csv" ;
  a turbo:TURBO_0000502 ;
  turbo:TURBO_0000604 "4/5/2000" ;
  turbo:TURBO_0000605 "2000-04-05"^^xsd:date ;
  turbo:TURBO_0000608 "131" ;
  turbo:TURBO_0000607 "http://purl.obolibrary.org/obo/OMRSE_00000138"^^xsd:anyURI .
  """
    InsertFromString(cxn, ScPartsTurtle, ScGraph)
  }

  def InsertEncPartJoinTrips(cxn: RepositoryConnection) = {

    // this comes from turbo/data/csv/synth_100k_enc_part_join.csv 
    // instantiated with turbo/karma_models/enc_part_join_direct_with_shortcuts-model.ttl
    // the UUIDs in URIs won't be the same if it is re-instantiated

    val CntrLessGraph = "http://transformunify.org/ontologies/containerless"

    val JoinTurtle = """
<http://transformunify.org/ontologies/PSC/0cba22da51ce40ca899828f68a0b79f0>
  a <http://transformunify.org/ontologies/TURBO_0000503> ;
  turbo:TURBO_0006510 "121" .

<http://transformunify.org/ontologies/encounterID/f5c027feec0e4af9ba2b186f65cfc1bd>
  a turbo:EncounterID ;
  turbo:TURBO_0006510 "102" ;
  turbo:TURBO_0000302 <http://transformunify.org/ontologies/PSC/0cba22da51ce40ca899828f68a0b79f0> .

<http://transformunify.org/ontologies/PSC/72f5729d81d145f4bc8a272ed49b60a4>
  turbo:TURBO_0006510 "131" ;
  a turbo:TURBO_0000503 .

<http://transformunify.org/ontologies/encounterID/7591318514b64ee68f32c47c5757ff2b>
  turbo:TURBO_0000302 <http://transformunify.org/ontologies/PSC/72f5729d81d145f4bc8a272ed49b60a4> ;
  a turbo:EncounterID ;
  turbo:TURBO_0006510 "103" .

<http://transformunify.org/ontologies/PSC/0a90ec8a020249f38ff89b55721a0292>
  turbo:TURBO_0006510 "141" ;
  a turbo:TURBO_0000503 .

<http://transformunify.org/ontologies/encounterID/6d143dea3c2149bd8866393d4b353f59>
  turbo:TURBO_0006510 "104" ;
  turbo:TURBO_0000302 <http://transformunify.org/ontologies/PSC/0a90ec8a020249f38ff89b55721a0292> ;
  a turbo:EncounterID .
  """

    InsertFromString(cxn, JoinTurtle, CntrLessGraph)

  }

  // expander

  def ExpandPartScTrips(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
INSERT {
  GRAPH pmbb:expanded {
    ?newPartUUID rdf:type turbo:TURBO_0000502 .
    ?newPartUUID turbo:previousUriText ?previousUriText .
    ?newPartUUID turbo:hasBirthDateO ?DOB .
    ?newPartUUID obo:RO_0000086 ?bioSexGT .
    
    ?DOB rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
    ?DOB turbo:TURBO_0006512 ?dobtv .
    ?DOB turbo:TURBO_0006511 ?dobXsd .
    ?DOB obo:BFO_0000050 ?Dataset .
    
    ?Dataset rdf:type obo:IAO_0000100 .
    ?Dataset <http://purl.org/dc/elements/1.1/title> ?dsTitle .
    ?Dataset rdfs:label ?dsTitle .
    
    ?Instantiation rdf:type turbo:TURBO_0000522 .
    ?Instantiation rdfs:label "Inst/Exp Proc" .
    ?Instantiation obo:OBI_0000293 ?Dataset .
    ?Instantiation obo:OBI_0000299 ?outpContainer .

    ?outpContainer rdf:type turbo:Container .
    ?outpContainer rdfs:label "Inst/Exp Outp Cont" .
    
    ?newPartUUID turbo:member ?outpContainer .
    ?DOB turbo:member ?outpContainer .
    ?PSC turbo:member ?outpContainer .
    ?bioSexGT turbo:member ?outpContainer .
    ?GID turbo:member ?outpContainer .
    
    ?PSC rdf:type turbo:TURBO_0000503 .
    ?PSC obo:BFO_0000050 ?Dataset .
    ?PSC turbo:TURBO_0006510 ?pscLv .
    ?PSC obo:IAO_0000219 ?newPartUUID .
    
    ?bioSexGT rdf:type ?bioSexUri .
    
    ?GID turbo:TURBO_0006510 ?gidLv .
    ?GID obo:BFO_0000050 ?Dataset .
    ?GID rdf:type obo:OMRSE_00000133 .
    ?GID obo:IAO_0000136 ?newPartUUID .
    
    ?sexRecoding rdf:type turbo:SexRecodingProcess .
    ?sexRecoding obo:OBI_0000293 ?GID .
    ?sexRecoding obo:OBI_0000299 ?bioSexGT .
    ?sexRecoding rdfs:label "sex recoding" .
    ?sexRecoding turbo:member ?SexRecContainer .
    
    ?SexRecContainer a turbo:Container ;
    rdfs:label "collection of sex recodings" .
    
  }
}
WHERE
  { GRAPH turbo:shortcuts
      { ?p  rdf:type              turbo:TURBO_0000502 ;
            turbo:TURBO_0000608      ?pscLv ;
            turbo:TURBO_0000603  ?dsTitle
        OPTIONAL
          { ?p  turbo:TURBO_0000604  ?dobtv ;
                turbo:TURBO_0000605   ?dobXsd
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?DOB)
          }
        OPTIONAL
          { ?p  turbo:TURBO_0000606  ?gidLv ;
                turbo:ScPart2BioSexUri  ?bioSexUriString
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?GID)
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?bioSexGT)
            BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?sexRecoding)
          }
        BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?newPartUUID)
        BIND(str(?p) AS ?previousUriText)
        BIND(uri(?bioSexUriString) AS ?bioSexUri)
        BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("dataset", ?dsTitle)))) AS ?Dataset)
        BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?PSC)

        BIND(uri("http://transformunify.org/ontologies/""" + InstantiationUUID + """") AS ?Instantiation)
        BIND(uri("http://transformunify.org/ontologies/""" + InstOutpContUUID + """") AS ?outpContainer)
          
        BIND(uri("http://transformunify.org/ontologies/""" + SexRecodingContUUID + """") AS ?SexRecContainer)
        
        # BIND(strafter(str(?p), "http://transformunify.org/ontologies/participant/") AS ?pk)
                
      }
  }
  """

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  // expander
  def CheckExpandedPartScTrips(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // NOT CURRENTY TESTING FOR THE RECODED/CAST VARIABLES
    // LIKE DOB XSD AND BIOSEX URI

    val CheckSparql = """
ASK
WHERE
  { GRAPHpmbb:expanded
      { FILTER ( ?count = 6 )
        { SELECT  (COUNT(?newPartUUID) AS ?count)
          WHERE
            { VALUES ( ?pscLv ?dobtv ?gidLv ?dsTitle ) {
                ( "121" "12/30/1971" "M" "embedded_handcrafted_parts.csv" )
                ( "121" "12/30/1971" "F" "embedded_handcrafted_parts.csv" )
                ( "131" "4/4/2000" "F" "embedded_handcrafted_parts.csv" )
                ( "131" "4/5/2000" "F" "embedded_handcrafted_parts.csv" )
              }
              ?newPartUUID  rdf:type        :TURBO_0000502 ;
                        :previousUriText    ?previousUriText ;
                        :hasBirthDateO      ?DOB ;
                        obo:RO_0000086      ?bioSexGT .
              ?DOB      rdf:type            <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                        :TURBO_0006512    ?dobtv ;
                        :TURBO_0006511       ?dobXsd ;
                        obo:BFO_0000050     ?Dataset .
              ?Dataset  rdf:type            obo:IAO_0000100 ;
                        dc11:title          ?dsTitle ;
                        rdfs:label          ?dsTitle .
              ?Instantiation
                        rdf:type            :R2RInstantiation ;
                        rdfs:label          "Inst/Exp Proc" ;
                        obo:OBI_0000293     ?Dataset ;
                        obo:OBI_0000299     ?outpContainer .
              ?outpContainer
                        rdf:type            :Container ;
                        rdfs:label          "Inst/Exp Outp Cont" .
              ?newPartUUID  :member         ?outpContainer .
              ?DOB      :member             ?outpContainer .
              ?PSC      :member             ?outpContainer .
              ?bioSexGT  :member            ?outpContainer .
              ?GID      :member             ?outpContainer .
              ?PSC      rdf:type            :TURBO_0000503 ;
                        obo:BFO_0000050     ?Dataset ;
                        :TURBO_0006510  ?pscLv ;
                        obo:IAO_0000219     ?newPartUUID .
              ?bioSexGT  rdf:type           ?bioSexUri .
              ?GID      :TURBO_0006510  ?gidLv ;
                        obo:BFO_0000050     ?Dataset ;
                        rdf:type            obo:OMRSE_00000133 .
              ?sexRecoding  rdf:type        :SexRecodingProcess ;
                        obo:OBI_0000293     ?GID ;
                        obo:OBI_0000299     ?bioSexGT ;
                        rdfs:label          "sex recoding"
            }
        }
      }
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def CheckExpandedPartScTripsClarifiedGID(cxn: RepositoryConnection) = {

    //   this is a select and should return a rdf4j result
    // which can be compared toa pre-compsed data structure liek a set of maps

    val CheckSparql = """
select ?pscVal ?dobTextVal ?dobXsdVal ?gidVal ?gidType
# ?iao_0000100
where {
    {
        ?container rdf:type :Container .
        ?bfo_0000035 :member ?container .
        ?studypartwithbbdonation :member ?container .
        # ?r2rinstantiation obo:OBI_0000299 ?container .
        # ?r2rinstantiation obo:OBI_0000293 ?iao_0000100 .
        # ?sexrecodingprocess rdf:type :SexRecodingProcess .
        # ?sexrecodingprocess obo:OBI_0000293 ?omrse_00000133 .
        # ?sexrecodingprocess obo:OBI_0000299 ?misslink .
        # ?misslink rdf:type :Container .
        # ?pato_0000047 :member ?misslink .
        ?omrse_00000133 :TURBO_0006510 ?gidVal .
        ?omrse_00000133 obo:BFO_0000050 ?iao_0000100 .
        # ?pato_0000047 rdf:type ?sextype .
        ?pato_0000047 rdf:type obo:PATO_0000047 .
        # ?omrse_00000133 rdf:type obo:OMRSE_00000133 .
        ?omrse_00000133 rdf:type ?gidType .
        # MAM 210709260925
        ?omrse_00000133 obo:IAO_0000136 ?studypartwithbbdonation .
        ?iao_0000100 rdf:type obo:IAO_0000100 .
        ?r2rinstantiation rdf:type turbo:TURBO_0000522 .
        ?studypartwithbbdonation rdf:type :TURBO_0000502 .
        ?bfo_0000035 rdf:type obo:UBERON_0035946 .
        ?studypartwithbbdonation rdf:type :TURBO_0000502 .
        ?studypartwithbbdonation turbo:TURBO_0006601 ?previousUriText .       
        # ?studypartwithbbdonation turbo:previousUriText ?previousUriText .
        ?psc_id rdf:type :TURBO_0000503 .
        ?efo_0004950 rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
        ?efo_0004950 :TURBO_0006510 ?dobTextVal .
        ?efo_0004950 :TURBO_0006511 ?dobXsdVal .
        ?iao_0000100 dc11:title ?dsTitleVal .
        ?psc_id :TURBO_0006510 ?pscVal .
        ?studypartwithbbdonation obo:RO_0000086 ?pato_0000047 .
        ?studypartwithbbdonation :TURBO_0000303 ?bfo_0000035 .
        ?psc_id obo:BFO_0000050 ?iao_0000100 .
        ?psc_id obo:IAO_0000219 ?studypartwithbbdonation .
        ?efo_0004950 obo:BFO_0000050 ?iao_0000100 .
        ?efo_0004950 obo:IAO_0000136 ?bfo_0000035 .
    } 
    # doesn't seem to make any difference
    # minus { ?gidType a obo:OMRSE_00000133  }
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()

    println(result.toString())

    result
  }

  def CheckPartDepRt(cxn: RepositoryConnection) = {

    val CheckSparql = """
ask
where {
    graphpmbb:expanded {
        ?s a :TURBO_0000502 .
        ?s :TURBO_0006500 ?rtstat .
        ?s :TURBO_0006601  ?prevPartUri .
        ?retPart :TURBO_0001700 ?s .
        ?retPart a :RetiredParticipantPlaceholder .
        ?retPart obo:IAO_0000225 obo:IAO_0000226 .
        ?s obo:RO_0000086 ?biosex .
        ?biosex a obo:PATO_0000047 .
        ?biosex :TURBO_0006602  ?prevSexUri .
        ?retSex :TURBO_0001700 ?biosex .
        ?retSex a :TURBO_0001902 .
        ?retSex obo:IAO_0000225 obo:IAO_0000226 .
        ?s turbo:TURBO_0000303  ?birth .
        ?birth a obo:UBERON_0035946  .
        ?birth :TURBO_0006602  ?prevBirthUri .
        ?retBirth :TURBO_0001700 ?birth .
        ?retBirth a :TURBO_0001906 .
        ?retBirth obo:IAO_0000225 obo:IAO_0000226 .
        ?psc obo:IAO_0000219  ?s .
        ?psc a turbo:TURBO_0000503  .
        ?psc :TURBO_0006510 ?pscval .
        ?psc :TURBO_0006602  ?prevPscUri .
        ?retPsc :TURBO_0001700 ?psc .
        # replaced type according to ontology
        ?retPsc a turbo:RetiredPscPlaceholder .
        # ?retPsc a turbo:RetiredPartStudyCodePlaceholder .
        ?retPsc obo:IAO_0000225 obo:IAO_0000226 .
    }
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result

  }

  def CheckRawJoinTrips(cxn: RepositoryConnection) = {
    // return value / exception handling?
    // added example of filtering out previous uri strings,
    //    which use a sub-property of thing literal value
    // also, the joins are not ending up in the containerless graph that I used to use
    // so now scanning the entire triplestore

    val CheckSparql = """
ASK
WHERE
#  { GRAPH <http://transformunify.org/ontologies/containerless>
{
    FILTER ( ?count = 3 )
    {
        SELECT  (COUNT(?encID) AS ?count)
        WHERE
        {
            VALUES ( ?encIDLv ?pscLv ) {
                ( "102" "121" )
                ( "103" "131" )
                ( "104" "141" )
            }
            ?encID  <http://transformunify.org/ontologies/TURBO_0000302>  ?PSC ;
                    a                     <http://transformunify.org/ontologies/EncounterID> .
            ?PSC    a                     <http://transformunify.org/ontologies/TURBO_0000503> .
            ?encID  <http://transformunify.org/ontologies/TURBO_0006510>  ?encIDLv .
            ?PSC    <http://transformunify.org/ontologies/TURBO_0006510>  ?pscLv .
            filter not exists {
                ?s turbo:TURBO_0006602 ?pscLv 
            }
            filter not exists {
                ?s turbo:TURBO_0006602 ?encIDLv 
            }
        }
    }
}
#  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def ContainJoinTriples(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ContJoinContUUID = java.util.UUID.randomUUID().toString()

    val ExpansionSparql = """
      INSERT {
#  GRAPH :contained_joins 
  GRAPHpmbb:expanded 
  {
    ?s :member ?container .
    ?container rdf:type :Container ;
    rdfs:label "join identifiers" .
  }
}
WHERE
  { GRAPH <http://transformunify.org/ontologies/containerless>
      { ?s  rdf:type  ?c
        BIND(uri("http://transformunify.org/ontologies/""" + ContJoinContUUID + """") AS ?container)
        # want something that is unique to this update but not unique for each instance 
        # can't use uuid... would create a different container for every instance
        # not instantiated as shortcuts, so no dataset title
      }
  }
      """

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def ReftrackPscs(cxn: RepositoryConnection) = {

    System.out.println("start psc reftracking")

    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // had been looking for PSCs to track in the "expanded" named graph... 
    // but this works fine looking globally

    val ExpansionSparql = """
      INSERT {
  GRAPH :TURBO_0006500 {
    ?rtPSC rdf:type :TURBO_0000503 .
    ?rtPSC :TURBO_0006510 ?pscLv .
    ?rtPSC :TURBO_0006500 true .
    ?rtPSC ?allfp ?allfo .
    ?rtPSC :previousUriText ?previousUriText .
    ?rtPSC :member ?RToutpContainer . 
    ?allrs ?allrp ?rtPSC .
    
    ?retiredPSC rdf:type :RetiredPscPlaceholder .
    ?rtPSC :TURBO_0006510 ?pscLv .
    ?rtPSC ?fp ?fo .
    ?rtPSC :previousUriText ?previousUriText .
    ?rtPSC :member ?retiredOutpContainer . 
    ?allrs ?allrp ?rtPSC .

    ?reftracking rdf:type :ReferentTracking .
    ?reftracking obo:OBI_0000299 ?RToutpContainer .
    ?reftracking obo:OBI_0000299 ?retiredOutpContainer .
    ?reftracking rdfs:label "referent tracking" . 

    ?RToutpContainer rdf:type :Container .
    ?RToutpContainer rdfs:label "referent tracked output" .
    
    ?retiredOutpContainer rdf:type :Container .
    ?retiredOutpContainer rdfs:label "retired output" .
  }
}
WHERE
  { ?PSC  rdf:type            :TURBO_0000503 ;
          :TURBO_0006510  ?pscLv ;
          ?allfp              ?allfo ;
          ?fp                 ?fo .

    OPTIONAL
      { ?allrs  ?allrp  ?PSC }
    OPTIONAL
      { ?rs  ?rp  ?PSC }
           
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("reftracked PSC with value = ", ?pscLv)))) AS ?rtPSC)
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("retired", str(?PSC))))) AS ?retiredPSC)
    
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackingUUID + """") AS ?reftracking)
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackedOutpContUUID + """") AS ?RToutpContainer)
    BIND(uri("http://transformunify.org/ontologies/""" + RetiredOutpContUUID + """") AS ?retiredOutpContainer)
       
    BIND(str(?PSC) AS ?previousUriText)
    FILTER ( ?fp != :member )
    FILTER ( ?rp != :member )
  }
   """

    //    System.out.println(ExpansionSparql)

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

    System.out.println("end psc reftracking")

  }

  def ClarifyRetiredPscs(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?PSC rdf:type :TURBO_0000503 .
}
WHERE
  { ?PSC  rdf:type  :TURBO_0000503 ;
          rdf:type  :RetiredPscPlaceholder
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def DeleteLimboPscs(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?PSC ?fp ?fo .
  ?rs ?rp ?PSC .
}
WHERE
  { ?PSC  rdf:type  :TURBO_0000503
    OPTIONAL
      { ?PSC  ?fp  ?fo }
    OPTIONAL
      { ?rs  ?rp  ?PSC }
    FILTER NOT EXISTS { ?PSC  :TURBO_0006500  ?rtstate }
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def ReftrackParts(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
INSERT {
  GRAPH :TURBO_0006500  {
    ?rtPart rdf:type :TURBO_0000502 .
    ?rtPart ?fp ?fo .
    ?rtPart :previousUriText ?previousUriText .
    ?rtPart :TURBO_0006500 true .
    ?rtPart :member ?rtCont .  
    ?rs ?rp ?rtPart .
    
    ?retiredPart rdf:type :RetiredParticipantPlaceholder .
    ?retiredPart ?fp ?fo .
    ?retiredPart :previousUriText ?previousUriText .
    ?retiredPart :member ?retiredCont .  
    ?rs ?rp ?retiredPart .

    ?reftracking rdf:type :ReferentTracking .
    ?reftracking obo:OBI_0000299 ?rtCont .
    ?reftracking obo:OBI_0000299 ?retiredCont .
    ?reftracking rdfs:label "referent tracking" . 

    ?RToutpContainer rdf:type :Container .
    ?RToutpContainer rdfs:label "referent tracked output" .
    
    ?retiredOutpContainer rdf:type :Container .
    ?retiredOutpContainer rdfs:label "retired output" .

  }
}
WHERE
  { ?PSC   rdf:type         :TURBO_0000503 ;
           obo:IAO_0000219  ?part .
    ?part  rdf:type         :TURBO_0000502 ;
           ?allfp           ?allfo ;
           ?fp              ?fo ;
    OPTIONAL
      { ?allrs  ?allrp  ?part }
    OPTIONAL
      { ?rs  ?rp  ?part }
      
      # this URI construction for the referent tracked participant URIs essentially DOES the collapsing
      # won't work as expected if other variables are being taken into consideration
      # of they're matched in a fuzzy way
      
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("reftracked particpant with PSC =", str(?PSC))))) AS ?rtPart)
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("retired", str(?part))))) AS ?retiredPart)
    
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackingUUID + """") AS ?reftracking)
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackedOutpContUUID + """") AS ?rtCont)
    BIND(uri("http://transformunify.org/ontologies/""" + RetiredOutpContUUID + """") AS ?retiredCont)
      
    BIND(str(?part) AS ?previousUriText)
    
    FILTER ( ?fp != :member )
    FILTER ( ?rp != :member )
  }  
  """

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def MinimalPartRefTrackTest(cxn: RepositoryConnection) = {
    val CheckSparql = """
select 
# * 
distinct 
# ?s  
?pscval ?rtstat ?prevurival
where {
    graphpmbb:expanded {
        ?s a :TURBO_0000502 .
        optional {
            ?s :TURBO_0006500 ?rtstat
        }
        optional {
            ?s :TURBO_0006601 ?prevurival
        }
        ?rs :TURBO_0001700 ?s .
        ?psc obo:IAO_0000219 ?s .
        ?psc :TURBO_0006510 ?pscval
    } 
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  // in the past, the participant reftracker was creating instances that didn't have a class asserted  
  // doens't seem to be a problem any longer, but still check  
  def CheckClasslessInstances(cxn: RepositoryConnection) = {
    // return value / exception handling?

    val CheckSparql = """
ASK
WHERE
{
    graph ?g {
        ?s  ?p  ?o
        FILTER NOT EXISTS {
            ?s  a ?c 
        }
    }
    filter (?g !=pmbb:ontology )
    filter (?g != :inverses )
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def RetireScPartsDontDelete(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
#construct {
 INSERT {
  GRAPH  turbo:shortcuts  {
    ?retiredPart ?fp ?fo .    
    ?rs ?rp ?retiredPart .
    ?retiredPart rdf:type :RetiredParticipantPlaceholder .
    ?retiredPart :previousUriText ?previousUriText .
    ?retiredPart :member ?retiredCont .
    
    ?retirement rdf:type :ReferentTracking .
    ?retirement obo:OBI_0000299 ?retiredCont .
    ?retirement rdfs:label "Shortcut retirement (untracked)" .
    
    ?retiredCont a turbo:Container .
    ?retiredCont rdfs:label "Output from non-tracked retirement" .

  }
 }
 
# add container for shortcuts at this point or somewhere earlier?

WHERE
  { graph turbo:shortcuts
    { ?part  rdf:type         :TURBO_0000502 ;
           ?fp              ?fo .
    OPTIONAL
      { ?rs  ?rp  ?part }
      
    # maybe should use prefixes in all md5 calculations?
    # to avoid name collisions
      
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("participant retired from shortcuts", str(?part))))) AS ?retiredPart)
    
    BIND(uri("http://transformunify.org/ontologies/""" + UntrackedRetiredOutpContUUID + """") AS ?retiredCont)
    BIND(uri("http://transformunify.org/ontologies/""" + RetirementUUID + """") AS ?retirement)
    
    BIND(str(?part) AS ?previousUriText)
    FILTER ( ?fp != :member )
    FILTER ( ?rp != :member )
    }  }

"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def RetireScEncsDontDelete(cxn: RepositoryConnection) = {

    val ExpansionSparql = """
 INSERT {
  GRAPH  turbo:shortcuts  {
    ?retiredEnc ?fp ?fo .    
    ?rs ?rp ?retiredEnc .
    
    ?retiredEnc rdf:type :TURBO_0000907 .
    ?retiredEnc :previousUriText ?previousUriText .
    ?retiredEnc :member ?retiredCont .
    
    ?retirement rdf:type :ReferentTracking .
    ?retirement obo:OBI_0000299 ?retiredCont .
    ?retirement rdfs:label "Shortcut retirement (untracked)" .
    
    ?retiredCont a turbo:Container .
    ?retiredCont rdfs:label "Output from non-tracked retirement" .

  }
 }
 
# add container for shortcuts at this point or somewhere earlier?

WHERE
  { graph turbo:shortcuts
    { ?enc  rdf:type         <http://transformunify.org/ontologies/OGMS_0000097> ;
           ?fp              ?fo .
    OPTIONAL
      { ?rs  ?rp  ?enc }
      
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("participant retired from shortcuts", str(?enc))))) AS ?retiredEnc)
    
    BIND(uri("http://transformunify.org/ontologies/""" + UntrackedRetiredOutpContUUID + """") AS ?retiredCont)
    BIND(uri("http://transformunify.org/ontologies/""" + RetirementUUID + """") AS ?retirement)
    
    BIND(str(?enc) AS ?previousUriText)
    FILTER ( ?fp != :member )
    FILTER ( ?rp != :member )
    }  }

"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql
    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()
  }

  def ClarifyRetiredParts(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?part rdf:type :TURBO_0000502 .
}
WHERE
  { ?part  rdf:type  :RetiredParticipantPlaceholder ;
           rdf:type  :TURBO_0000502
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  // also deletes triples about participants in the shortcuts graph...
  // to keep them , run the retire method first!
  def DeleteLimboParts(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?part ?fp ?fo .
  ?rs ?rp ?part .
}
WHERE
  { ?part  rdf:type  :TURBO_0000502
    OPTIONAL
      { ?part  ?fp  ?fo }
    OPTIONAL
      { ?rs  ?rp  ?part }
    FILTER NOT EXISTS { ?part  :TURBO_0006500  ?rtstate }
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  // ADD PROCESS AND INPUT / OUTPUT CONTAINERS?

  // having trouble figuring how to connect the input container

  def DrawDobConclusions(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // input, output (containers), process

    val ExpansionSparql = """
INSERT {
  GRAPH turbo:conclusions {
    ?part turbo:hasBirthDateO ?DOBconc .
    ?DOBconc turbo:TURBO_0006511 ?xsddate .
    ?DOBconc turbo:TURBO_0006501 'true'^^xsd:boolean .
    ?DOBconc rdf:type <http://www.ebi.ac.uk/efo/EFO_0004950> .
    
    ?DOBconc turbo:member ?conclCont .
    
    ?conclusionation a turbo:TURBO_0002500 ;
    rdfs:label "conclusionation process" ;
    obo:OBI_0000299 ?conclCont .
    
#    obo:OBI_0000 ?inpCont .
    
    ?conclCont a turbo:Container ;
    rdfs:label  "conclusionation output"
  }
}
WHERE
  { { SELECT  ?part ?xsddate (COUNT(?xsddate) AS ?datecount)
      WHERE
        { ?part  rdf:type             turbo:TURBO_0000502 ;
                 turbo:hasBirthDateO  ?dob .
          ?dob   turbo:TURBO_0006511   ?xsddate ;
        }
      GROUP BY ?part ?xsddate
    }
    .
    { SELECT  ?part ?xsddate (COUNT(?xsddate) AS ?datecount2)
      WHERE
        { ?part  rdf:type             turbo:TURBO_0000502 ;
                 turbo:hasBirthDateO  ?dob .
          ?dob   turbo:TURBO_0006511   ?xsddate
        }
      GROUP BY ?part ?xsddate
    }
    FILTER ( ?datecount = ?countmax )
    BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?DOBconc)
    
    BIND(uri("http://transformunify.org/ontologies/""" + ConclusionsOutpContUUID + """") AS ?conclCont)
    BIND(uri("http://transformunify.org/ontologies/""" + ConclusionationUUID + """") AS ?conclusionation)
    
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def DrawSexConclusions(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // input, output (containers), process

    val ExpansionSparql = """
INSERT {
  GRAPH turbo:conclusions {
    ?part obo:RO_0000086 ?BioSexConc .
    ?BioSexConc rdf:type ?sexclass .
    ?BioSexConc turbo:TURBO_0006501 'true'^^xsd:boolean .
    
    ?BioSexConc turbo:member ?conclCont .
    
    ?conclusionation a turbo:TURBO_0002500 ;
    rdfs:label "conclusionation process" ;
    obo:OBI_0000299 ?conclCont .
    
#    obo:OBI_0000 ?inpCont .
    
    ?conclCont a turbo:Container ;
    rdfs:label  "conclusionation output"
  }
}
WHERE
  { { SELECT  ?part ?sexclass (COUNT(?sexclass) AS ?sexcount)
      WHERE
        { ?part    rdf:type        turbo:TURBO_0000502 ;
                   obo:RO_0000086  ?biosex .
          ?biosex  rdf:type        ?sexclass
          VALUES ?sexclass { obo:PATO_0000047 obo:PATO_0000383 obo:PATO_0000384 }
        }
      GROUP BY ?part ?sexclass
    }
    { SELECT  ?part (MAX(?sexcount2) AS ?countmax)
      WHERE
        { SELECT  ?part ?sexclass2 (COUNT(?sexclass2) AS ?sexcount2)
          WHERE
            { ?part    rdf:type        turbo:TURBO_0000502 ;
                       obo:RO_0000086  ?biosex .
              ?biosex  rdf:type        ?sexclass2
              VALUES ?sexclass2 { obo:PATO_0000047 obo:PATO_0000383 obo:PATO_0000384 }
            }
          GROUP BY ?part ?sexclass2
        }
      GROUP BY ?part
    }
    FILTER ( ?sexcount = ?countmax )
    BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?BioSexConc)
    
    BIND(uri("http://transformunify.org/ontologies/""" + ConclusionsOutpContUUID + """") AS ?conclCont)
    BIND(uri("http://transformunify.org/ontologies/""" + ConclusionationUUID + """") AS ?conclusionation)
    
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  // are any of these absent?
  // return NEGATED value "ok, they're all here (others may be, too)"
  def CheckExpectedSexConcs(cxn: RepositoryConnection) = {
    // return value / exception handling?

    val CheckSparql = """
ASK
WHERE
  { 
  VALUES ( ?pscval ?sextype ) {
      ( "121" obo:PATO_0000384 )
      ( "131" obo:PATO_0000383 )
    }
    FILTER NOT EXISTS { 
	                    ?psc     obo:IAO_0000219       ?part ;
                                 turbo:TURBO_0006510  ?pscval ;
                                 rdf:type              turbo:TURBO_0000503 .
                        ?part    rdf:type              turbo:TURBO_0000502 ;
                                 obo:RO_0000086        ?biosex .
                        ?biosex  rdf:type              ?sextype ;
                                 turbo:conclusionated  ?concstat
                      }
  }
  """

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    !result
  }

  // are any of the inverse conclusions present?
  // negation means "ok, they're not present"
  def CheckInverseSexConcs(cxn: RepositoryConnection) = {
    // return value / exception handling?

    val CheckSparql = """
ASK
WHERE
  { 
  VALUES ( ?pscval ?sextype ) {
      ( "121" obo:PATO_0000383 )
      ( "131" obo:PATO_0000384 )
    }
	                    ?psc     obo:IAO_0000219       ?part ;
                                 turbo:TURBO_0006510  ?pscval ;
                                 rdf:type              turbo:TURBO_0000503 .
                        ?part    rdf:type              turbo:TURBO_0000502 ;
                                 obo:RO_0000086        ?biosex .
                        ?biosex  rdf:type              ?sextype ;
                                 turbo:conclusionated  ?concstat
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    !result
  }

  // a generic ask with values determines if any of the values patterns are present

  // this query will be false if any values pattern is MISSING
  // negation mean none are missing, and maybe others are present, too
  def CheckExpectedDobs(cxn: RepositoryConnection) = {
    // return value / exception handling?

    val CheckSparql = """
ASK
WHERE
  { 
  VALUES ( ?pscval ?dv ?concstat ) {
        ( "121" "1971-12-30"^^xsd:date true )
        ( "131" "2000-04-04"^^xsd:date true )
      }
    FILTER NOT EXISTS { 
	                    ?psc     obo:IAO_0000219       ?part ;
                                 turbo:TURBO_0006510  ?pscval ;
                                 rdf:type              turbo:TURBO_0000503 .
                        ?part    rdf:type              turbo:TURBO_0000502 ;
                                 turbo:hasBirthDateO   ?dob .
                          ?dob   a                     <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                                 turbo:TURBO_0006511    ?dv ;
                                 turbo:conclusionated  ?concstat .
                      }
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    !result
  }

  // how to check for incorrect literals?
  // this asks if one specific minor DOB has been concluded to be true
  // negation means "that erroneous conclusion is absent"
  def UnexpectedDobConclusion(cxn: RepositoryConnection) = {
    // return value / exception handling?

    val CheckSparql = """
ASK
WHERE
  { 
  VALUES ( ?pscval ?dv ?concstat ) {
        ( "131" "2000-04-05"^^xsd:date true )
      }
                          ?part  a                     turbo:TURBO_0000502 ;
                                 turbo:hasBirthDateO   ?dob .
                          ?dob   a                     <http://www.ebi.ac.uk/efo/EFO_0004950> ;
                                 turbo:TURBO_0006511    ?dv ;
                                 turbo:conclusionated  ?concstat .
                          ?psc   obo:IAO_0000219       ?part ;
                                 turbo:TURBO_0006510  ?pscval ;
                                 turbo:TURBO_0006500      ?rtstat
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    !result
  }

  def ReftrackEncIds(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // had been looking for EncIDs to track in the "expanded" named graph... 
    // but this works fine looking globally

    val ExpansionSparql = """
INSERT {
  GRAPH turbo:TURBO_0006500 {
    ?rtEncId rdf:type turbo:EncounterID .
    ?rtEncId turbo:TURBO_0006510 ?encIDLv .
	  ?rtEncId turbo:TURBO_0006500 true .
    ?rtEncId ?fp ?fo .
    ?rs ?rp ?rtEncId .
    ?rtEncId turbo:previousUriText ?previousUriText .
    ?rtEncId turbo:member ?rtCont .
    	
    ?retiredEncId rdf:type turbo:RetiredEncIdPlaceholder .
    ?retiredEncId turbo:TURBO_0006510 ?encIDLv .
    ?retiredEncId ?fp ?fo .
    ?rs ?rp ?retiredEncId .
    ?retiredEncId turbo:previousUriText ?previousUriText .
    ?retiredEncId turbo:member ?retiredCont .
    
    ?reftracking rdf:type :ReferentTracking .
    ?reftracking obo:OBI_0000299 ?rtCont .
    ?reftracking obo:OBI_0000299 ?retiredCont .
    ?reftracking rdfs:label "referent tracking" . 

    ?rtCont rdf:type :Container .
    ?rtCont rdfs:label "referent tracked output" .
    
    ?retiredCont rdf:type :Container .
    ?retiredCont rdfs:label "retired output" .

  }
}
WHERE
      {
       ?encid  rdf:type              turbo:EncounterID ;
              turbo:TURBO_0006510  ?encIDLv ;
              ?fp                   ?fo .
        OPTIONAL
          { ?rs  ?rp  ?encid }
        BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("reftracked encounter id with value = ", ?encIDLv)))) AS ?rtEncId)
        BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("retired encounter id", str(?encid))))) AS ?retiredEncId)
        BIND(str(?encid) AS ?previousUriText)
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackingUUID + """") AS ?reftracking)
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackedOutpContUUID + """") AS ?rtCont)
    BIND(uri("http://transformunify.org/ontologies/""" + RetiredOutpContUUID + """") AS ?retiredCont)
        }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def ClarifyRetiredEncIds(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?EncId rdf:type :EncounterID .
}
WHERE
  { ?EncId  rdf:type  :EncounterID ;
          rdf:type  :RetiredEncIdPlaceholder
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def DeleteLimboEncIds(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?EncId ?fp ?fo .
  ?rs ?rp ?EncId .
}
WHERE
  { ?EncId  rdf:type  :EncounterID
    OPTIONAL
      { ?EncId  ?fp  ?fo }
    OPTIONAL
      { ?rs  ?rp  ?EncId }
    FILTER NOT EXISTS { ?EncId  :TURBO_0006500  ?rtstate }
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def ReftrackEncounters(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // had been looking for EncIDs to track in the "expanded" named graph... 
    // but this works fine looking globally

    val ExpansionSparql = """
INSERT {
  GRAPH turbo:TURBO_0006500 {
   
    ?rtEnc rdf:type obo:OGMS_0000097 .
	  ?rtEnc turbo:TURBO_0006500 true .
    ?rtEnc ?fp ?fo .
    ?rs ?rp ?rtEnc .
    ?rtEnc turbo:previousUriText ?previousUriText .
    ?rtEnc turbo:member ?rtCont .

    ?retiredEnc rdf:type turbo:TURBO_0000907 .
    ?retiredEnc ?fp ?fo .
    ?rs ?rp ?retiredEnc .
    ?retiredEnc turbo:previousUriText ?previousUriText .
    ?retiredEnc turbo:member ?retiredCont .

    ?reftracking rdf:type :ReferentTracking .
    ?reftracking obo:OBI_0000299 ?rtCont .
    ?reftracking obo:OBI_0000299 ?retiredCont .
    ?reftracking rdfs:label "referent tracking" . 

    ?rtCont rdf:type :Container .
    ?rtCont rdfs:label "referent tracked output" .
    
    ?retiredCont rdf:type :Container .
    ?retiredCont rdfs:label "retired output" .

  }
}
WHERE
  { ?encId  rdf:type         turbo:EncounterID ;
            obo:IAO_0000219  ?enc .
    ?enc    rdf:type         obo:OGMS_0000097 ;
            ?fp              ?fo
    OPTIONAL
      { ?rs  ?rp  ?enc }
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("rt particpant", str(?encId))))) AS ?rtEnc)
    BIND(uri(concat("http://transformunify.org/ontologies/", MD5(concat("retired", str(?enc))))) AS ?retiredEnc)
    
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackingUUID + """") AS ?reftracking)
    BIND(uri("http://transformunify.org/ontologies/""" + ReftrackedOutpContUUID + """") AS ?rtCont)
    BIND(uri("http://transformunify.org/ontologies/""" + RetiredOutpContUUID + """") AS ?retiredCont)

    BIND(str(?part) AS ?previousUriText)
  }
  """

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def ClarifyRetiredEncounters(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?enc rdf:type obo:OGMS_0000097 .
}
WHERE
  { ?enc  rdf:type  obo:OGMS_0000097 ;
          rdf:type  :TURBO_0000907
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def DeleteLimboEncounters(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val ExpansionSparql = """
DELETE {
  ?enc ?fp ?fo .
  ?rs ?rp ?enc .
}
WHERE
  { ?enc  rdf:type  obo:OGMS_0000097
    OPTIONAL
      { ?enc  ?fp  ?fo }
    OPTIONAL
      { ?rs  ?rp  ?enc }
    FILTER NOT EXISTS { ?enc  :TURBO_0006500  ?rtstate }
  }
"""

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def RowSharing2EncounterParticipation(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    // the ability to create the right role here depends on somebody being of class biobank study participant
    // I have been think about saying that these people aren't biobank study participants
    // theyr're homo sapiens who have at least one PUI role or have donated a sample or have enrolled or soemthign like that
    val ExpansionSparql = """
INSERT {
#  GRAPH pmbb:expanded_joins 
  GRAPH turbo:sharing2participation
  {
    ?part obo:RO_0000056 ?enc .
    ?part obo:RO_0000087 ?puirole .
    ?puirole <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> obo:OBI_0000097 .
    ?puirole obo:BFO_0000054 ?enc .
    
    ?puirole turbo:member ?retiredOutpContainer .

    ?share2partProc rdf:type :share2partInf .
    ?share2partProc obo:OBI_0000299 ?share2partOutp .
    ?share2partProc rdfs:label "participation inference from rowsharing" . 

    ?share2partOutp rdf:type :Container .
    ?share2partOutp rdfs:label "participation inferences" .

  }
}
WHERE
  { ?eId  turbo:TURBO_0000302   ?pId ;
          a                     turbo:EncounterID ;
          turbo:TURBO_0006510  ?eilv .
    ?pId  a                     turbo:TURBO_0000503 ;
          turbo:TURBO_0006510  ?pilv
    OPTIONAL
      { ?pId   obo:IAO_0000219       ?part .
        ?part  a                     turbo:TURBO_0000502
      }
    OPTIONAL
      { ?eId  obo:IAO_0000219       ?enc .
        ?enc  a                     obo:OGMS_0000097
      }
    BIND(uri(concat("http://transformunify.org/ontologies/", struuid())) AS ?puirole)

    BIND(uri("http://transformunify.org/ontologies/""" + Share2PartProcUUID + """") AS ?share2partProc)
    BIND(uri("http://transformunify.org/ontologies/""" + Share2PartOutpContUUID + """") AS ?share2partOutp)

  }
  """

    val WithPrefixes = SparqlPrefixes + ExpansionSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  /* 
   * UTILITIES
   */

  def InsertFromString(cxn: RepositoryConnection, TriplesString: String, DestinationGraph: String) = {
    // return value / exception handling?

    val InsertPrefix = """
							INSERT DATA 
							{ 
							graph <""" + DestinationGraph + """>
							{
							"""

    val InsertSuffix = """
							}
							}
							"""

    val Infixed = SparqlPrefixes + InsertPrefix + TriplesString + InsertSuffix

    //    System.out.println(Infixed)

    val tupleAdd = cxn.prepareUpdate(QueryLanguage.SPARQL, Infixed)
    val result = tupleAdd.execute()
  }

  def ClearNamedGraph(cxn: RepositoryConnection, GraphName: String) = {
    // return value / exception handling?

    val ClearSparql = """CLEAR GRAPH <""" + GraphName + """>"""

    val WithPrefixes = SparqlPrefixes + ClearSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def CheckGraphEmpty(cxn: RepositoryConnection, GraphName: String) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val CheckSparql = """
ASK
WHERE
  { GRAPH <""" + GraphName + """>
      { ?s ?p ?o }
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def DeleteAllTriples(cxn: RepositoryConnection) = {
    // return value / exception handling?

    val ClearSparql = """ clear all """

    val WithPrefixes = SparqlPrefixes + ClearSparql

    val PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.execute()

  }

  def CreateInverses(cxn: RepositoryConnection) = {
    // return value / exception handling?

    var InverseSparql = """
      insert { 
      graph <http://www.itmat.upenn.edu/biobank/inverses> {
      ?o obo:BFO_0000050 ?s 
      }} where { 
	?s obo:BFO_0000051 ?o .
}
"""

    var WithPrefixes = SparqlPrefixes + InverseSparql
    var PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    var result = PreparedSparql.execute()

    InverseSparql = """
insert { 
graph <http://www.itmat.upenn.edu/biobank/inverses> {
?o obo:BFO_0000051 ?s }} where { 
	?s obo:BFO_0000050 ?o .
} 
"""

    WithPrefixes = SparqlPrefixes + InverseSparql
    PreparedSparql = cxn.prepareUpdate(QueryLanguage.SPARQL, WithPrefixes)
    result = PreparedSparql.execute()

  }

  def CountParticipants(cxn: RepositoryConnection) = {
    // return value / exception handling?

    // rdfs:labels
    // containers or named graphs?
    // names: fixed, or come combination of task, table & UUID?

    val CheckSparql = """
  select (count(?s) as ?count) where { 
  graph ?g {
	?s a turbo:TURBO_0000502  .
	}
	filter (?g !=pmbb:ontology)
	}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()

    // Not checking if there IS a bindingset
    val bindingSet = result.next()

    //    System.out.println(bindingSet.toString())

    var CountValue = bindingSet.getValue("count")

    val CountString = CountValue.toString()

    //    System.out.println(CountString)

    CountString
  }
  // get the PSC values of reftracked participants
  // gets 121 and 131 and also returning 141, because it rally is a psc
  // just doesn't denote any participant

  //  PREFIX turbo: <http://transformunify.org/ontologies/>
  //select * where { 
  //    # (count(?s) as ?reftrackedPscCount )
  //	?s a turbo:TURBO_0000503 .
  //    ?s turbo:TURBO_0006510 ?pscval
  //}

  //  select * where { 
  //	?s a turbo:TURBO_0000502  .
  //
  //}
  //  

  // count the retired participants... six from participant table and three implied by join table

  //  PREFIX turbo: <http://transformunify.org/ontologies/>
  //select * where { 
  //    # (count(?s) as ?reftrackedPscCount )
  //	?s a turbo:TURBO_0000503 .
  //    ?s turbo:TURBO_0006510 ?pscval
  //} 

  // add a dump-to-file method?
  //  construct { ?s ?p ?o . }
  //   where { ?s ?p ?o . }

  def SocalledFinalTest(cxn: RepositoryConnection) = {

    val CheckSparql = """
SELECT  ?pscval ?sextype ?encIdVal ?diagCodeLV
WHERE
  { ?part     a                     turbo:TURBO_0000502 ;
              obo:RO_0000086        ?bioSex .
    ?bioSex   turbo:conclusionated  true ;
              a                     ?sextype .
    ?psc      obo:IAO_0000219       ?part ;
              turbo:TURBO_0006510  ?pscval ;
              a                     turbo:TURBO_0000503 .
    ?part     obo:RO_0000056        ?enc .
    ?encId    obo:IAO_0000219       ?enc ;
              turbo:TURBO_0006510  ?encIdVal ;
              a                     turbo:EncounterID .
    ?enc      obo:OBI_0000299       ?DiagCrid1 .
    ?DiagCrid1  a                   turbo:TURBO_0000553 ;
              obo:BFO_0000051       ?DiagCodeRegID1 ;
              obo:BFO_0000051       ?DiagCodeSymb1 .
    ?DiagCodeSymb1
              a                     turbo:TURBO_0000554 ;
              turbo:TURBO_0006510  ?diagCodeLV .
    ?DiagCodeRegID1
              a                     turbo:TURBO_0000555 ;
              obo:IAO_0000219       ?diagCodeRegURI ;
              turbo:TURBO_0006512  ?diagCodeRegTextVal
  }
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()

    result

  }

  def BiosexConclTest(cxn: RepositoryConnection) = {

    val CheckSparql = """
select distinct ?pscval ?sexclass
where { 
	?s a turbo:TURBO_0000502  .
    ?gid obo:IAO_0000136 ?s .
    ?gid turbo:TURBO_0006510  ?gidval .
    ?psc obo:IAO_0000219 ?s .
    ?psc turbo:TURBO_0006510 ?pscval .
    ?s obo:RO_0000086 ?biosex .
    ?biosex a obo:PATO_0000047 .
    ?biosex a ?sexclass
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()

    result

  }

  def DobConclTest(cxn: RepositoryConnection) = {

    val CheckSparql = """
select ?pscval ?conclstat ?dobval
where {
    ?s a turbo:TURBO_0000502  .
    ?s turbo:TURBO_0000303 ?b .
    ?dob obo:IAO_0000136  ?b  .
#    optional { 
    ?dob turbo:conclusionated ?conclstat .
# } .
    ?dob turbo:TURBO_0006511 ?dobval .
    ?psc obo:IAO_0000219 ?s .
    ?psc turbo:TURBO_0006510 ?pscval .
    #    ?s obo:RO_0000086 ?biosex .
    #    ?biosex a obo:PATO_0000047 .
    #    ?biosex a ?sexclass
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()

    result

  }

  def CheckUndefinedClasses(cxn: RepositoryConnection) = {

    val CheckSparql = """
ASK
where {
    # will need to annotate these named graphs with categories?
    # really want to search all named graphs except ontology itself
    # filtering could be costly?
    # does this check the default "unnamed" graph?
    # date stamp components should be ordered decreasing like YYYY MM DD HH mm SS
    #    turbo:ConclusionatedNamedGraph_09_26_2017_16_55_30 
#    values ?g {
#        :containerlesspmbb:expanded :sharing2participation :ConclusionatedNamedGraph_09_26_2017_16_55_30 
#    }
    graph ?g
    {
        ?s a ?c .
    }
    filter not exists {
        {
            graphpmbb:ontology {
                ?c a owl:Class
            }
        }   
    }
    filter (?g != pmbb:ontology )
        filter (?c != rdf:Statement )
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def CheckUndefinedProps(cxn: RepositoryConnection) = {

    val CheckSparql = """
ask
where {
    {
        graph ?g
        {
            ?s ?p ?o .
        }
        filter (?g != pmbb:ontology )
        filter not exists {
            {
                graphpmbb:ontology {
                    ?p a ?x .
                    values ?x {
                        owl:DatatypeProperty owl:ObjectProperty  
                    }
                }
            }   
        }
    }
    minus
    {
        ?s rdf:type ?o 
    }
    # use "values" or "in"
    filter (?p !=  rdf:subject)
    filter (?p !=  rdf:predicate)    
    filter (?p !=  rdf:object)
    filter (?p !=  dc:title)
    filter (?p !=  rdfs:label)    
    filter (?p !=  rdfs:seeAlso)    
    filter (?p !=  owl:versionInfo)
    filter (?p !=  :TURBO_0001700)
    filter (?p !=  obo:IAO_0000225)
    filter (?p !=  rdfs:comment)
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    result
  }

  def CheckTripleStoreEmpty(cxn: RepositoryConnection) = {

    val CheckSparql = """
ask {?s ?p ?o}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareBooleanQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    !result
  }

  def CATbyRegex(cxn: RepositoryConnection) = {

    val CheckSparql = """
select (count(?s) as ?scount) where {
    ?s ?p ?o .
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    val ResBindSet = result.next()
    val tripleCount = ResBindSet.getValue("scount")
    val tripleCountString = tripleCount.toString()

    val pattern = """"(\d+)"""".r

    //    pattern.findAllIn(tripleCountString).matchData foreach {
    //      m => println(m.group(1))
    //    }

    val faimg1 = pattern.findAllIn(tripleCountString).matchData

    faimg1.next.group(1)

  }

  def CountAcrossTriplestore(cxn: RepositoryConnection) = {

    val CheckSparql = """
select (count(?s) as ?scount) where {
    ?s ?p ?o .
}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    val ResBindSet = result.next()
    val tripleCount = ResBindSet.getValue("scount")

    // could int be too small in some cases?
    // see also https://stackoverflow.com/questions/46941167/rdf4j-method-for-splitting-literal-into-value-and-datatype
    tripleCount.stringValue().toInt

  }

  def CountOneGraph(cxn: RepositoryConnection, GraphString: String) = {

    val CheckSparql = """
select (count(?s) as ?scount) where {
graph <""" + GraphString + """> {
    ?s ?p ?o .
}}
"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()
    val ResBindSet = result.next()
    val tripleCount = ResBindSet.getValue("scount")

    // could int be too small in some cases?
    // see also https://stackoverflow.com/questions/46941167/rdf4j-method-for-splitting-literal-into-value-and-datatype
    tripleCount.stringValue().toInt

  }

  def ListNamedGraphs(cxn: RepositoryConnection) = {

    val CheckSparql = """
    select distinct ?g where {
    graph ?g {
        ?s ?p ?o .
    } 
}

"""

    val WithPrefixes = SparqlPrefixes + CheckSparql

    val PreparedSparql = cxn.prepareTupleQuery(QueryLanguage.SPARQL, WithPrefixes)
    val result = PreparedSparql.evaluate()

    //    println("printing named graph list now.")

    var GraphNames = scala.collection.mutable.Set[String]()

    while (result.hasNext()) {
      val ResBindSet = result.next()
      val OneName = ResBindSet.getValue("g")
      GraphNames += OneName.stringValue()
    }

    GraphNames

  }

  def Res2Set(ResAsRes: TupleQueryResult): scala.collection.mutable.Set[Map[String, String]] = {
    var ResAsSet = scala.collection.mutable.Set[Map[String, String]]()

    while (ResAsRes.hasNext()) {
      var nextbs = ResAsRes.next
      var LocalMap: Map[String, String] = Map.empty

      var temp = nextbs.getBindingNames.toArray

      for (e <- temp) {
        val eString = e.toString()
        val TempVal = nextbs.getValue(eString).toString()
        LocalMap += (eString -> TempVal)
      }
      ResAsSet += LocalMap
    }
    ResAsSet
  }

  /*
   * SEND RESULTS
   */

  def SendEpectedPartRtRes: Set[Map[String, String]] = {
    Set(
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """rtstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """prevurival""" -> """"http://transformunify.org/ontologies/participant/fb08701bac2f4a169612fd23500b2e9f"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """rtstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """prevurival""" -> """"http://transformunify.org/ontologies/participant/5fdde1e0f54f4a4fa9855be71240f0f8"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """rtstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """prevurival""" -> """"http://transformunify.org/ontologies/participant/37fd0431e04140da973af908f5fdebfc"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """rtstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """prevurival""" -> """"http://transformunify.org/ontologies/participant/ed7c74146c0d4bde80a7e4bc12b42619"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """rtstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """prevurival""" -> """"http://transformunify.org/ontologies/participant/e4f53c6f407947019fdcde317de0462d"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """rtstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """prevurival""" -> """"http://transformunify.org/ontologies/participant/ca667124f8bb4a839eb6b436e68aed5f"^^<http://www.w3.org/2001/XMLSchema#string>"""))
  }

  def SendExpectedPartExpRes: Set[Map[String, String]] = {
    Set(
      Map(
        """gidVal""" -> """"M"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobXsdVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000141""",
        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """pscVal""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobXsdVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """pscVal""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidVal""" -> """"M"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobXsdVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000141""",
        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """pscVal""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      //      Map(
      //        """gidVal""" -> """"M"^^<http://www.w3.org/2001/XMLSchema#string>""",
      //        """dobXsdVal""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>""",
      //        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000133""",
      //        """dobTextVal""" -> """"12/30/1971"^^<http://www.w3.org/2001/XMLSchema#string>""",
      //        """pscVal""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobXsdVal""" -> """"2000-04-05"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dobTextVal""" -> """"4/5/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """pscVal""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobXsdVal""" -> """"2000-04-04"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dobTextVal""" -> """"4/4/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """pscVal""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """gidVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """dobXsdVal""" -> """"2000-04-04"^^<http://www.w3.org/2001/XMLSchema#date>""",
        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000138""",
        """dobTextVal""" -> """"4/4/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """pscVal""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""") //      Map(
        //        """gidVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        //        """dobXsdVal""" -> """"2000-04-04"^^<http://www.w3.org/2001/XMLSchema#date>""",
        //        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000133""",
        //        """dobTextVal""" -> """"4/4/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        //        """pscVal""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>"""),
        //      Map(
        //        """gidVal""" -> """"F"^^<http://www.w3.org/2001/XMLSchema#string>""",
        //        """dobXsdVal""" -> """"2000-04-05"^^<http://www.w3.org/2001/XMLSchema#date>""",
        //        """gidType""" -> """http://purl.obolibrary.org/obo/OMRSE_00000133""",
        //        """dobTextVal""" -> """"4/5/2000"^^<http://www.w3.org/2001/XMLSchema#string>""",
        //        """pscVal""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""")
        )
  }

  // 20170926 pm:  biosex conclusions don't look quite right
  // 210709271020 LOKS GOOD NOW

  /* 
CSV input:
PSC	DOB	GID
121	12/30/1971	M
121	12/30/1971	M
121	12/30/1971	F
131	4/4/2000	F
131	4/4/2000	F
131	4/5/2000	F

ALL GID data after exp, rt & concl
?pscval	?gidval
"121"	M
"121"	M
"121"	F
"131"	F
"131"	F
"131"	F

Expected conclusion:
121 -> male,   PATO_0000384
131 -> female, PATO_0000383

Actual biological sexes present in triplestore
?pscval	?sexclass
"121"	<http://purl.obolibrary.org/obo/PATO_0000384>
"121"	<http://purl.obolibrary.org/obo/PATO_0000047>
"131"	<http://purl.obolibrary.org/obo/PATO_0000384>
"131"	<http://purl.obolibrary.org/obo/PATO_0000047>

NO, participant with PSC 131 is female and should have a biological sex of type PATO_0000383

 */

  def SendExpectedBiosexConclRes: Set[Map[String, String]] = {
    Set(
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sexclass""" -> """http://purl.obolibrary.org/obo/PATO_0000047"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sexclass""" -> """http://purl.obolibrary.org/obo/PATO_0000383"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sexclass""" -> """http://purl.obolibrary.org/obo/PATO_0000384"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sexclass""" -> """http://purl.obolibrary.org/obo/PATO_0000047"""))

  }

  def SendExpectedDobConclRes: Set[Map[String, String]] = {

    Set(
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """conclstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """dobval""" -> """"1971-12-30"^^<http://www.w3.org/2001/XMLSchema#date>"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """conclstat""" -> """"true"^^<http://www.w3.org/2001/XMLSchema#boolean>""",
        """dobval""" -> """"2000-04-04"^^<http://www.w3.org/2001/XMLSchema#date>"""))

  }

  def SendExpectedFinalRes: Set[Map[String, String]] = {

    Set(
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sextype""" -> """http://purl.obolibrary.org/obo/PATO_0000047""",
        """encIdVal""" -> """"103"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"602.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"131"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sextype""" -> """http://purl.obolibrary.org/obo/PATO_0000383""",
        """encIdVal""" -> """"103"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"602.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sextype""" -> """http://purl.obolibrary.org/obo/PATO_0000047""",
        """encIdVal""" -> """"102"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"J44.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sextype""" -> """http://purl.obolibrary.org/obo/PATO_0000384""",
        """encIdVal""" -> """"102"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"J44.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sextype""" -> """http://purl.obolibrary.org/obo/PATO_0000047""",
        """encIdVal""" -> """"102"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"I50.9"^^<http://www.w3.org/2001/XMLSchema#string>"""),
      Map(
        """pscval""" -> """"121"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """sextype""" -> """http://purl.obolibrary.org/obo/PATO_0000384""",
        """encIdVal""" -> """"102"^^<http://www.w3.org/2001/XMLSchema#string>""",
        """diagCodeLV""" -> """"I50.9"^^<http://www.w3.org/2001/XMLSchema#string>"""))

  }

}


