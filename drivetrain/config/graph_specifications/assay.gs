# baseURI: https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/turbo_valid_graph_specification.ttl/

@prefix : <https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/post_icbo_synthea_omop_cnp_transformation_instructions.ttl/> .
@prefix properties: <http://turboProperties.org/> .
@prefix turbo: <http://transformunify.org/ontologies/> .

@prefix drivetrain: <https://github.com/PennTURBO/Drivetrain/> .
@prefix efo: <http://www.ebi.ac.uk/efo/> .
@prefix obo: <http://purl.obolibrary.org/obo/> .
@prefix oboInOwl: <http://www.geneontology.org/formats/oboInOwl#> .
@prefix ontologies: <http://transformunify.org/ontologies/> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix sdvg: <http://api.stardog.com/> .
@prefix xml: <http://www.w3.org/XML/1998/namespace> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

@prefix mydata: <http://example.com/resource/> .

# assert part of + has part and other inverses?

<https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/turbo_valid_graph_specification.ttl> a owl:Ontology ;
    owl:priorVersion "not applicable" ; 
    owl:versionInfo "unversioned" ;
    owl:versionIRI <https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/turbo_valid_graph_specification.ttl> ;
	rdfs:comment "that's not really a version IRI" .

owl:Axiom a owl:Class .

drivetrain:star a drivetrain:PredicateSuffixSymbol ;
  drivetrain:usesSparqlOperator '*' ;
.

drivetrain:AssayKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:assay_keysym_LiteralValue ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subjectUsesContext drivetrain:AssayKeyContext ;
.

drivetrain:AssayKeyContext  a drivetrain:TurboGraphContext .

#   :mustExecuteIf :objectExists ;

drivetrain:assay_keysym_LiteralValue a drivetrain:LiteralResourceList .

drivetrain:AssayKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object ontologies:TURBO_0010433 ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subjectUsesContext drivetrain:AssayKeyContext ;
  drivetrain:objectUsesContext drivetrain:AssayKeyContext ;
.

# need a registry or whatever keys use

# multiple keys could denote an assay etc
drivetrain:AssayKeyDenotesAssay
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:OBI_0000070 ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subjectUsesContext drivetrain:AssayKeyContext ;
.


# be prepared for scalar datums and categotical value specifications
# could an assasy have multiple outputs
drivetrain:AssayHasSpecifiedOutputDataItem
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject obo:OBI_0000070 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:object obo:IAO_0000027 ;
  drivetrain:cardinality drivetrain:1-1 ;
.

drivetrain:AssayDataItemHasValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject obo:IAO_0000027 ;
  drivetrain:predicate obo:OBI_0001938 ;
  drivetrain:object obo:OBI_0001933 ;
  drivetrain:cardinality drivetrain:1-1 ;
.

drivetrain:assay_valnum_LiteralValue a  drivetrain:LiteralResourceList .


drivetrain:AssayValspecHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:subject obo:OBI_0001933 ;
  drivetrain:predicate turbo:TURBO_0010094 ;
  drivetrain:object drivetrain:assay_valnum_LiteralValue ;
  drivetrain:cardinality drivetrain:1-1 ;
.


drivetrain:AssayDataItemIsAboutPerson
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject obo:IAO_0000027 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:cardinality drivetrain:1-1 ;
.


drivetrain:CridDenotesPerson
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
.

drivetrain:PatientCridContext a drivetrain:TurboGraphContext .

drivetrain:PersonSymbolPartOfCrid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
  drivetrain:objectUsesContext drivetrain:PatientCridContext ;
.



