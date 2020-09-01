# baseURI: https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/turbo_valid_graph_specification.ttl/

<https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/turbo_valid_graph_specification.ttl> a owl:Ontology ;
    owl:priorVersion "not applicable" ; 
    owl:versionInfo "unversioned" ;
    owl:versionIRI <https://raw.githubusercontent.com/PennTURBO/Drivetrain/master/drivetrain/ontologies/turbo_valid_graph_specification.ttl> ;
	rdfs:comment "that's not really a version IRI" .
  
owl:Axiom a owl:Class .

drivetrain:KeyDenotesGid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object drivetrain:GidToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.
drivetrain:GidAboutPatient
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:GidToBeTyped ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object obo:NCBITaxon_9606 ;
.
drivetrain:GidHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:GidToBeTyped ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:gender_LiteralValue ;
.
drivetrain:GidPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:GidToBeTyped ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object drivetrain:GidColumnTerm ;
.
drivetrain:GidTypingRecipe a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:GidToBeTyped  ;
  drivetrain:predicate rdf:type ;
  drivetrain:object drivetrain:GidClassList ;
.

drivetrain:GidToBeTyped 
  a drivetrain:UntypedInstance ;
.

drivetrain:GidClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:GidColumnTerm
  a drivetrain:ClassResourceList ;
.

drivetrain:KeyDenotesRid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object drivetrain:RidToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.
drivetrain:KeyDenotesDob
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object efo:EFO_0004950 ;
.
drivetrain:DobPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject efo:EFO_0004950 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object drivetrain:dobColumnTerm ;
.
drivetrain:dobColumnTerm
  a drivetrain:ClassResourceList ;
.
drivetrain:KeyDenotesPatKeySymb
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:IAO_0000028 ;
  drivetrain:objectUsesContext drivetrain:PatientCridContext ;
.

drivetrain:PatKeySymbPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010444 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
.

drivetrain:KeyDenotesPatient
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:KeyPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010442  ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:RidAboutPatient
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:RidToBeTyped ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object obo:NCBITaxon_9606 ;
.

drivetrain:RidPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010441 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject drivetrain:RidToBeTyped ;
.

drivetrain:RidHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:RidToBeTyped ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:race_LiteralValue ;
.
drivetrain:RidTypingRecipe a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:RidToBeTyped  ;
  drivetrain:predicate rdf:type ;
  drivetrain:object drivetrain:RidClassList ;
.
drivetrain:CridDenotesPatient
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
.
drivetrain:PatKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
.
drivetrain:PatKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:person_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.
obo:IAO_0000028 
  drivetrain:hasPossibleContext drivetrain:PatientCridContext ;
  drivetrain:hasPossibleContext drivetrain:EncounterCridContext ;
  drivetrain:hasPossibleContext drivetrain:TumorCridContext ;
  drivetrain:hasPossibleContext drivetrain:KeyContext ;
  drivetrain:hasPossibleContext drivetrain:IndexContext ;
  drivetrain:hasPossibleContext drivetrain:LossOfFunctionCridContext ;
  drivetrain:hasPossibleContext drivetrain:MeasurementCridContext ;
.
obo:IAO_0000578
  drivetrain:hasPossibleContext drivetrain:PatientCridContext ;
  drivetrain:hasPossibleContext drivetrain:EncounterCridContext ;
  drivetrain:hasPossibleContext drivetrain:TumorCridContext ;
  drivetrain:hasPossibleContext drivetrain:LossOfFunctionCridContext ;
  drivetrain:hasPossibleContext drivetrain:MeasurementCridContext ;
.
ontologies:TURBO_0010433 
  drivetrain:hasPossibleContext drivetrain:KeyContext ;
  drivetrain:hasPossibleContext drivetrain:IndexContext ;
.
drivetrain:KeyContext a drivetrain:TurboGraphContext .
drivetrain:PatientCridContext a drivetrain:TurboGraphContext .
drivetrain:EncounterCridContext a drivetrain:TurboGraphContext .
drivetrain:TumorCridContext a drivetrain:TurboGraphContext .
drivetrain:IndexContext a drivetrain:TurboGraphContext .

drivetrain:HardcodedPatientSourceden 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010396 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:DobAboutSns 
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject efo:EFO_0004950 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object obo:UBERON_0035946 ;
.
drivetrain:DobHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject efo:EFO_0004950 ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:object drivetrain:birth_datetime_DateLiteralValue ;
.
drivetrain:DobHasRawDateString
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject efo:EFO_0004950 ;
  drivetrain:predicate turbo:TURBO_0010094 ;
  drivetrain:object drivetrain:birth_datetime_StringLiteralValue ;
.
drivetrain:RidToBeTyped 
  a drivetrain:UntypedInstance ;
.
drivetrain:RidClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:PatientRegden 
  a drivetrain:ClassResourceList ;
.

drivetrain:HardcodedPatientRegden 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:PatientRegden ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject obo:IAO_0000578  ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
.

drivetrain:PatCridsymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:person_cridsym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
.

drivetrain:PatientBornOnSns 
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:object obo:UBERON_0035946 ;
  drivetrain:predicate ontologies:TURBO_0000303 ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.

drivetrain:SymbolPartOfCridForPatient
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:PatientCridContext ;
  drivetrain:objectUsesContext drivetrain:PatientCridContext ;
.

### health care encounters

drivetrain:EncIdPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010453 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:EncCridSymPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010455 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:EncounterCridContext ;
.

drivetrain:KeyDenotesEncCridSym
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:IAO_0000028 ;
  drivetrain:objectUsesContext drivetrain:EncounterCridContext ;
.
drivetrain:EncStartDatePartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010457 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:KeyDenotesEncStartDate
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:objectUsesContext drivetrain:StartContext ;
.

drivetrain:EndDatePartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010451 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:encEndContext ;
.

drivetrain:KeyDenotesEndDate
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:objectUsesContext drivetrain:encEndContext ;
.
drivetrain:KeyDenotesEnc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.
drivetrain:EncKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
.
drivetrain:KeyHasPartEncKeySymb
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
.
drivetrain:EncKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:encounter_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:HardcodedEncSourceden 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:EncounterRegDen ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.
drivetrain:EncounterRegDen a drivetrain:ClassResourceList .

drivetrain:CridDenotesEnc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:EncounterCridContext ;
.

drivetrain:EncTypingRecipe 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:EncToBeTyped  ;
  drivetrain:predicate rdf:type ;
  drivetrain:object drivetrain:EncClassList ;
.

drivetrain:EncToBeTyped 
  a drivetrain:UntypedInstance ;
.

drivetrain:EncCridsymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:encounter_cridsym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:EncounterCridContext ;
.

drivetrain:EncClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:EndingTmdAboutProcbound 
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object obo:BFO_0000035 ;
  drivetrain:subjectUsesContext drivetrain:encEndContext ;
  drivetrain:objectUsesContext drivetrain:encEndContext ;
.
drivetrain:encEndContext a drivetrain:TurboGraphContext .
obo:IAO_0000416 drivetrain:hasPossibleContext drivetrain:encEndContext .
obo:BFO_0000035 drivetrain:hasPossibleContext drivetrain:encEndContext .

drivetrain:EndingTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:encounter_end_datetime_DateLiteralValue ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:encEndContext ;
.

drivetrain:HardcodedRegden 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010256 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject obo:IAO_0000578  ;
.

drivetrain:ProcboundEndsEnc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:BFO_0000035 ;
  drivetrain:predicate obo:RO_0002229 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:encEndContext ;
.

drivetrain:EncStartingTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:encounter_start_datetime_DateLiteralValue ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.
drivetrain:EncStartingTmdHasRawStringVal
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:encounter_start_datetime_StringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:StartingTmdAboutProcbound 
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:BFO_0000035 ;
  drivetrain:objectUsesContext drivetrain:StartContext ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:ProcboundStartsEnc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:BFO_0000035 ;
  drivetrain:predicate obo:RO_0002223 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:StartContext a drivetrain:TurboGraphContext .
obo:IAO_0000416 drivetrain:hasPossibleContext drivetrain:StartContext .
obo:BFO_0000035 drivetrain:hasPossibleContext drivetrain:StartContext .

drivetrain:symbolPartOfCridForEnc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:EncounterCridContext ;
  drivetrain:objectUsesContext drivetrain:EncounterCridContext ;
.

drivetrain:EncTypeAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:EncToBeTyped ;
.
drivetrain:EncTypeAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object rdf:type ;
.
drivetrain:EncTypeAxiomTarget
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object drivetrain:EncClassList ;
.
drivetrain:EncTypeAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
.
drivetrain:EncTypeAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:encounter_type_code_LiteralValue ;
.
drivetrain:EncTypeAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010449 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
.

drivetrain:EncKeyDenotesTypingDbXrefProv
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

### HCE participation

drivetrain:HceKeyDenotesProv4Participation
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParticipationAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:EncKeyContext ;
.

drivetrain:EncRealizesRole
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject drivetrain:EncToBeTyped  ;
  drivetrain:predicate obo:BFO_0000055 ;
  drivetrain:object drivetrain:RoleToBeTyped ;
.

drivetrain:PatientHasRole
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:RO_0000087 ;
  drivetrain:object drivetrain:RoleToBeTyped ;
.

drivetrain:RoleToRoleType
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:RoleToBeTyped ;
  drivetrain:predicate rdf:type ;
  drivetrain:object drivetrain:RoleClassList ;
.

drivetrain:RoleToBeTyped
  a drivetrain:UntypedInstance ;
.
drivetrain:RoleClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:BiobankEncounterRealizesPuiRole
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
  drivetrain:subject ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:BFO_0000055 ;
  drivetrain:object obo:OBI_0000097 ;
.
drivetrain:PatientHasPuiRole
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0000097 ;
  drivetrain:predicate obo:RO_0000087 ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.

drivetrain:EncParticipationAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:EncParticipationAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:BFO_0000055 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:EncParticipationAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object drivetrain:RoleToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:EncParticipationAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
  drivetrain:objectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:EncParticipationAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010447 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:EncParticipationAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:person_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ParticipationAxiomContext a drivetrain:TurboGraphContext .
owl:Axiom drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext .
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext .

### HCE participation prov

drivetrain:EncKeyContext a drivetrain:TurboGraphContext .
ontologies:TURBO_0010433 drivetrain:hasPossibleContext drivetrain:EncKeyContext .

### HCE precedence

drivetrain:PrevencPrecedesEnc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:predicate obo:RO_0002090 ;
  drivetrain:subject drivetrain:Prevenc  ;
.

drivetrain:PrecedenceAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:Prevenc ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:EncPrecedenceAxiomContext ;
.

drivetrain:PrecedenceAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:RO_0002090  ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:EncPrecedenceAxiomContext ;
.

drivetrain:PrecedenceAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:EncPrecedenceAxiomContext ;
.

drivetrain:PrecedenceAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:EncPrecedenceAxiomContext ;
  drivetrain:objectUsesContext drivetrain:EncPrecedenceAxiomContext ;
.

drivetrain:PrecedenceAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:preceding_encounter_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:EncPrecedenceAxiomContext ;
.

drivetrain:PrecedenceAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010459 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:EncPrecedenceAxiomContext ;
.

drivetrain:HceKeyDenotesProv4Precedence
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:EncPrecedenceAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:EncKeyContext ;
.

drivetrain:Prevenc
  a drivetrain:UntypedInstance .

drivetrain:EncPrecedenceAxiomContext a drivetrain:TurboGraphContext .
owl:Axiom drivetrain:hasPossibleContext drivetrain:EncPrecedenceAxiomContext .
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:EncPrecedenceAxiomContext .

  
### health care procedures

drivetrain:ProcKeyDenotesTmd
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:objectUsesContext drivetrain:StartContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
.

drivetrain:ProcStartingTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:procedure_datetime_DateLiteralValue ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:ProcStartDatePartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010472 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:ProcboundStartsProc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:ProcToBeTyped ;
  drivetrain:predicate obo:RO_0002223 ;
  drivetrain:subject obo:BFO_0000035 ;
  drivetrain:subjectUsesContext drivetrain:StartContext ;
.

drivetrain:ProcKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:proc_id_IntegerLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:IAO_0000028 ;
.

drivetrain:ProcKeySymbPartOfCol
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010465 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
.

drivetrain:ProcKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object ontologies:TURBO_0010433 ;
.

drivetrain:HardcodedProcSourceden 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010407 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433   ;
.

drivetrain:ProcKeyDenotesProc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:ProcToBeTyped ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
.

drivetrain:ProcTypingRecipe 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:ProcClassList ;
  drivetrain:predicate rdf:type ;
  drivetrain:subject drivetrain:ProcToBeTyped  ;
.

drivetrain:MentioningProvAboutProc
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object drivetrain:ProcToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.

drivetrain:MentioningProvPartOfCol
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010470 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.

drivetrain:MentioningProvHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:procedure_cridsym_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.

drivetrain:MentioningProvMentionsCode
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:object drivetrain:ProcMentionedClassList ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.

drivetrain:ProcKeyDenotesMentioningProv
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:MentionsContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
.

drivetrain:ProcKeyDenotesProcTypeAxiomDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:TypingAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
.

drivetrain:ProcTypeAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:ProcToBeTyped ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:TypingAxiomContext ;
.

drivetrain:ProcTypeAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object rdf:type ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:TypingAxiomContext ;
.

drivetrain:ProcTypeAxiomTarget
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:ProcClassList ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:TypingAxiomContext ;
.

drivetrain:ProcTypeAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:TypingAxiomContext ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:TypingAxiomContext ;
.

drivetrain:ProcTypeAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:procedure_type_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:TypingAxiomContext ;
.

drivetrain:ProcTypeAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010469 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:TypingAxiomContext ;
.

drivetrain:ProcClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:ProcToBeTyped 
  a drivetrain:UntypedInstance ;
.

drivetrain:ProcMentionedClassList
  a drivetrain:ClassResourceList .

drivetrain:MentionsContext a drivetrain:TurboGraphContext .
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:MentionsContext .

### tumors

drivetrain:cancerHasMaterialBasisTumor
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010070 ;
  drivetrain:predicate obo:IDO_0000664 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:MONDO_0004992 ;
.
drivetrain:patientHasPartTumor
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object ontologies:TURBO_0010070 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
drivetrain:tumorPartOfPatient
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010070 ;
.
drivetrain:tumorCridSymbolHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:tumor_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subjectUsesContext drivetrain:TumorCridContext ;
  drivetrain:subject obo:IAO_0000028 ;
.
drivetrain:tumorCridSymbolPartOfTumorCrid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:objectUsesContext drivetrain:TumorCridContext ;
  drivetrain:subjectUsesContext drivetrain:TumorCridContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000028 ;
.
drivetrain:tumorCridDenotesTumor
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010070 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:TumorCridContext ;
.
drivetrain:tumorCridHasPartTumorCridSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:TumorCridContext ;
  drivetrain:objectUsesContext drivetrain:TumorCridContext ;
.
drivetrain:tumorCridHasPartRegDen
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:TumorRegistryClassList ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:TumorCridContext ;
.
drivetrain:tumorRegDenPartOfTumorCrid
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:objectUsesContext drivetrain:TumorCridContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject drivetrain:TumorRegistryClassList ;
.
drivetrain:TumorRegistryClassList a drivetrain:ClassResourceList .

### loss of function

drivetrain:DNAExtractionProcessHasOutputDNAExtract
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001051 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0000257 ;
.
drivetrain:DNAExtractionProcessHasInputSpecimen
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001479 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0000257 ;
.
drivetrain:DNAhasGrainDNAExtract
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:OBI_0001051 ;
  drivetrain:predicate obo:OBI_0000643 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001868 ;
.
drivetrain:DNAIsGenomeOfHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:OGG_0000000014 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001868 ;
.
drivetrain:alleleIsAboutDNA
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:OBI_0001868 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
drivetrain:alleleHasGeneSymbolGeneSymbolPrefix
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:allelePrefix_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010016 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
drivetrain:alleleHasGeneSymbolGeneSymbolSuffix
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:alleleSuffix_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010015 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
drivetrain:alleleMentionsGeneSymbolUri
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:GeneSymbolClassList ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
drivetrain:alleleHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:zygosity_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010095 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
drivetrain:alleleHasValueSpecZygosityUri
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:ZygosityClassList ;
  drivetrain:predicate obo:OBI_0001938 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
drivetrain:biobankEncounterHasPartCollectionProcess
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0600005 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject ontologies:TURBO_0000527 ;
.
drivetrain:collectionProcessPartOfBiobankEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0600005 ;
.
drivetrain:collectionProcessHasInputHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0600005 ;
.
drivetrain:collectionProcessHasOutputSpecimen
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001479 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0600005 ;
.
drivetrain:exomeSequenceProcessHasInputDNAExtract
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001051 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0002118 ;
.
drivetrain:exomeSequenceProcessHasOutputSequenceData
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001573 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0002118 ;
.
drivetrain:formProcessHasOutputAllele
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:OBI_0001352 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0200000 ;
.
drivetrain:formProcessHasInputSequenceData
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001573 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0200000 ;
.
drivetrain:genomeCridRegistryDenoterPartOfGenomeCrid
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality drivetrain:singleton-many ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject drivetrain:GenomeRegistryClassList ;
  drivetrain:objectUsesContext drivetrain:LossOfFunctionCridContext ;
.
drivetrain:genomeCridHasPartGenomeCridRegDen
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-singleton ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:object drivetrain:GenomeRegistryClassList ;
  drivetrain:subjectUsesContext drivetrain:LossOfFunctionCridContext ;
.
drivetrain:genomeCridSymbolPartOfGenomeCrid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:objectUsesContext drivetrain:LossOfFunctionCridContext ;
  drivetrain:subjectUsesContext drivetrain:LossOfFunctionCridContext ;
.
drivetrain:genomeCridHasPartGenomeCridSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:object obo:IAO_0000028 ;
  drivetrain:objectUsesContext drivetrain:LossOfFunctionCridContext ;
  drivetrain:subjectUsesContext drivetrain:LossOfFunctionCridContext ;
.
drivetrain:genomeCridDenotesSpecimen
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001479 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:LossOfFunctionCridContext ;
.
drivetrain:genomeSymbolHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate turbo:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:object drivetrain:alleleSymbol_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:LossOfFunctionCridContext ;
.
drivetrain:GeneSymbolClassList a drivetrain:ClassResourceList .
drivetrain:ZygosityClassList
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0000590 ;
  drivetrain:range ontologies:TURBO_0000591 ;
.
drivetrain:GenomeRegistryClassList a drivetrain:ClassResourceList.
drivetrain:LossOfFunctionCridContext a drivetrain:TurboGraphContext .

### health care procedure participation

drivetrain:ProcRealizesPatientRole
 a drivetrain:InstanceToInstanceRecipe ;
 drivetrain:cardinality drivetrain:1-many ;
 drivetrain:subject drivetrain:ProcToBeTyped ;
 drivetrain:predicate obo:BFO_0000055 ;
 drivetrain:object drivetrain:RoleToBeTyped ;
.

drivetrain:ProcParticipationAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:ProcToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ProcParticipationAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:BFO_0000055 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ProcParticipationAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object drivetrain:RoleToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ProcParticipationAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
  drivetrain:objectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ProcParticipationAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010464 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ProcParticipationAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:person_keysym_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParticipationAxiomContext ;
.

drivetrain:ProcKeyDenotesParticipationAxiomDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParticipationAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
.

drivetrain:ParticipationAxiomContext a drivetrain:TurboGraphContext .
drivetrain:ProcToBeTyped drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext .
owl:Axiom drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext .
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext .

drivetrain:TypingAxiomContext a drivetrain:TurboGraphContext .
drivetrain:ProcToBeTyped drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:ParticipationAxiomContext .

### health care procedures parthood

drivetrain:ProcPartOfEnc
 a drivetrain:InstanceToInstanceRecipe ;
 drivetrain:cardinality drivetrain:many-1 ;
 drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
 drivetrain:object drivetrain:EncToBeTyped ;
 drivetrain:predicate obo:BFO_0000050 ;
 drivetrain:subject drivetrain:ProcToBeTyped ;
.

drivetrain:ProcParthoodAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:ProcToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:ProcParthoodAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:BFO_0000050 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:ProcParthoodAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:ProcParthoodAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:ProcParthoodAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:encounter_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:ProcParthoodAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010466 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:ProcKeyDenotesParthoodAxiomDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
.

drivetrain:ParthoodAxiomContext a drivetrain:TurboGraphContext .
drivetrain:ProcToBeTyped drivetrain:hasPossibleContext drivetrain:ParthoodAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:ParthoodAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:ParthoodAxiomContext .

### measurement

drivetrain:MeasTypingRecipe a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject drivetrain:MeasToBeTyped  ;
  drivetrain:predicate rdf:type ;
  drivetrain:object drivetrain:MeasClassList ;
.

drivetrain:MeasToBeTyped 
  a drivetrain:UntypedInstance ;
.

drivetrain:MeasClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:MeasMentionsCode
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object drivetrain:MeasCodeMentionedClassList ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject drivetrain:MeasToBeTyped ;
.

drivetrain:MeasCodeMentionedClassList
  a drivetrain:ClassResourceList .
  
drivetrain:KeyDenotesMeas
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:CridDenotesMeas
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000578 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:MeasurementCridContext ;
.

drivetrain:SymbolPartOfCridForMeasurement
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:object obo:IAO_0000578 ;
  drivetrain:subjectUsesContext drivetrain:MeasurementCridContext ;
  drivetrain:objectUsesContext drivetrain:MeasurementCridContext ;
.

drivetrain:SymbolHasRepresentationForMeasurement
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate turbo:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:object drivetrain:measurementSymbol_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:MeasurementCridContext ;
.

drivetrain:measurementIsAbnormal
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:MeasToBeTyped ;
  drivetrain:predicate rdfs:comment ;
  drivetrain:object drivetrain:abnormalMeas_LiteralValue ;
.

drivetrain:MeasKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010433 ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
.

drivetrain:MeasKeySymbPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010482 ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
.

drivetrain:MeasKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:measurement_id_IntegerLiteralValue ;
.

drivetrain:HardcodedMeasSourceden
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010398 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:MeasHasValspec
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject drivetrain:MeasToBeTyped ;
  drivetrain:predicate obo:OBI_0001938 ;
  drivetrain:object obo:OBI_0001933 ;
.
drivetrain:MeasValspecPartOfQueryResult
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object  ontologies:TURBO_0010411 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:OBI_0001933 ;
.

drivetrain:KeyDenotesValspec
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:OBI_0001933 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:ValspecHasSpecVal
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:OBI_0001933 ;
  drivetrain:predicate obo:OBI_0002135 ;
  drivetrain:object drivetrain:value_as_number_LiteralValue ;
.

drivetrain:MeasTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:object drivetrain:measurement_datetime_DateLiteralValue ;
.

drivetrain:KeyDenotesTmd
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:MeasTmdPartOfQueryResult
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object  ontologies:TURBO_0010484 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000416 ;
.

drivetrain:measurement_datetime_DateLiteralValue 
  a drivetrain:DateLiteralResourceList ;
.

drivetrain:MeasTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:object drivetrain:measurement_datetime_DateLiteralValue ;
.

drivetrain:MeasHasTimestamp  
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000581 ;
  drivetrain:subject drivetrain:MeasToBeTyped ;
.
drivetrain:measurement_id_IntegerLiteralValue
  a drivetrain:IntegerLiteralResourceList ;
.

drivetrain:measurement_source_concept_id_IntegerLiteralValue
  a drivetrain:IntegerLiteralResourceList ;
.

drivetrain:MeasTypeAxiomSource
  drivetrain:subjectUsesContext drivetrain:MeasTypeAxiomContext ;
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
.

drivetrain:MeasTypeAxiomProp
  drivetrain:subjectUsesContext drivetrain:MeasTypeAxiomContext ;
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object rdf:type ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
.

drivetrain:MeasTypeAxiomTarget
  drivetrain:subjectUsesContext drivetrain:MeasTypeAxiomContext ;
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:MeasClassList ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
.

drivetrain:MeasTypeAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:MeasTypeAxiomContext ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:MeasTypeAxiomContext ;
.

drivetrain:KeyDenotesMeasTypeAxiomDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:MeasTypeAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:MeasTypeAxiomDbXrefHasRepresentation
  drivetrain:subjectUsesContext drivetrain:MeasTypeAxiomContext ;
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:measurement_source_concept_id_IntegerLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
.

drivetrain:MeasTypeAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010488 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:MeasTypeAxiomContext ;
.

drivetrain:MeasKeyDenotesProv4MeasType
  drivetrain:objectUsesContext drivetrain:MeasTypeAxiomContext ;
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:MeasTypeAxiomContext a drivetrain:TurboGraphContext .
drivetrain:MeasToBeTyped drivetrain:hasPossibleContext drivetrain:MeasTypeAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:MeasTypeAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:MeasTypeAxiomContext .

drivetrain:MeasMentionAxiomSource
  drivetrain:subjectUsesContext drivetrain:MeasMentionAxiomContext ;
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
.

drivetrain:MeasMentionAxiomProp
  drivetrain:subjectUsesContext drivetrain:MeasMentionAxiomContext ;
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000142 ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
.

drivetrain:MeasMentionAxiomTarget
  drivetrain:subjectUsesContext drivetrain:MeasMentionAxiomContext ;
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:MeasCodeMentionedClassList ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
.

drivetrain:MeasMentionAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:MeasMentionAxiomContext ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:MeasMentionAxiomContext ;
.

drivetrain:KeyDenotesMeasMentionAxiomDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:MeasMentionAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:MeasMentionAxiomDbXrefHasRepresentation
  drivetrain:subjectUsesContext drivetrain:MeasMentionAxiomContext ;
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:measurement_type_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
.

drivetrain:MeasMentionAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010487 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:MeasMentionAxiomContext ;
.

drivetrain:MeasKeyDenotesProv4Mention
  drivetrain:objectUsesContext drivetrain:MeasMentionAxiomContext ;
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:MeasMentionAxiomContext a drivetrain:TurboGraphContext .
drivetrain:MeasToBeTyped drivetrain:hasPossibleContext drivetrain:MeasMentionAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:MeasMentionAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:MeasMentionAxiomContext .

### measurement unit label (MUL)

drivetrain:MeasHasMul
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:MulClassList ;
  drivetrain:predicate obo:IAO_0000039 ;
  drivetrain:subject drivetrain:MeasToBeTyped  ;
.

drivetrain:MulAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:MeasToBeTyped ;
.

drivetrain:MulAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:IAO_0000039 ;
.
drivetrain:MulAxiomTarget
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object drivetrain:MulClassList ;
.
drivetrain:MulAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
.
drivetrain:MulAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:measurement_units_LiteralValue ;
.

drivetrain:MulAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010423 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
.

drivetrain:MeasKeyDenotesProv4Mul
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:MeasDenotationContext ;
.

drivetrain:MulClassList
  a drivetrain:ClassResourceList ;
.
  
### measurement aboutness

drivetrain:MeasAboutPat
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject drivetrain:MeasToBeTyped ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:object obo:NCBITaxon_9606 ;
.

drivetrain:MeasAboutnessAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:MeasAboutnessAxiomContext ;
.
drivetrain:MeasAboutnessAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:IAO_0000136 ;
  drivetrain:subjectUsesContext drivetrain:MeasAboutnessAxiomContext ;
.
drivetrain:MeasAboutnessAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:subjectUsesContext drivetrain:MeasAboutnessAxiomContext ;
.
drivetrain:MeasAboutnessAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:MeasAboutnessAxiomContext ;
  drivetrain:objectUsesContext drivetrain:MeasAboutnessAxiomContext ;
.

drivetrain:MeasAboutnessAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:person_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:MeasAboutnessAxiomContext ;
.

drivetrain:MeasAboutnessAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010483 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:MeasAboutnessAxiomContext ;
.

drivetrain:MeasAboutnessAxiomContext a drivetrain:TurboGraphContext .
drivetrain:ProcToBeTyped drivetrain:hasPossibleContext drivetrain:MeasAboutnessAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:MeasAboutnessAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:MeasAboutnessAxiomContext .

drivetrain:MeasurementCridContext a drivetrain:TurboGraphContext .

### measurement parthood

drivetrain:EncHsoMeas
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:predicate  obo:OBI_0000299 ;
  drivetrain:subject  drivetrain:EncToBeTyped ;
.

drivetrain:BiobankEncHasOutputMeas
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:predicate  obo:OBI_0000299 ;
  drivetrain:subject  ontologies:TURBO_0000527 ;
.

drivetrain:MeasParthoodAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:MeasParthoodAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:OBI_0000299 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:MeasParthoodAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:object drivetrain:MeasToBeTyped ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.
drivetrain:MeasParthoodAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:MeasParthoodAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:encounter_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:MeasParthoodAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010486 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:MeasKeyDenotesProv4Parthood
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:MeasDenotationContext ;
.

drivetrain:MeasKeyDenotesProv4Aboutness
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:MeasAboutnessAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:MeasDenotationContext ;
.

drivetrain:MeasDenotationContext a drivetrain:TurboGraphContext .
ontologies:TURBO_0010433 drivetrain:hasPossibleContext drivetrain:MeasDenotationContext .

### diagnoses 

drivetrain:KeyDenotesDiag
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:OGMS_0000073 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:DiagKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010433 ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:DiagKeySymbPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:DiagColumnTerm ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:condition_occurrence_id_LiteralValue a drivetrain:LiteralResourceList .

###

drivetrain:DiagKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object drivetrain:condition_occurrence_id_StringLiteralValue ;
  drivetrain:object drivetrain:condition_occurrence_id_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.


drivetrain:HardcodedDiagSourceden 
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:diagnosisRegDen ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.
drivetrain:diagnosisRegDen 
  a drivetrain:ClassResourceList ;
.
drivetrain:DiagMentionsCode
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:CondMentionedClassList ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:OGMS_0000073 ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.
drivetrain:DiagMentionsIcdTerm
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:IcdClassList ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:OGMS_0000073 ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.
drivetrain:IcdClassList
  a drivetrain:ClassResourceList ;
.
drivetrain:DiagMentionsSnomedTerm
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:SnomedClassList ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:OGMS_0000073 ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.
drivetrain:SnomedClassList
  a drivetrain:ClassResourceList ;
.
drivetrain:DiagHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:condition_type_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
drivetrain:diagnosisHasCodingSequence
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:diagnosis_CodingSequence ;
  drivetrain:predicate ontologies:TURBO_0010014 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
drivetrain:diagnosisIsPrimary
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:diagnosis_PrimaryBoolean ;
  drivetrain:predicate ontologies:TURBO_0010013 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
drivetrain:diagnosisHasRegistryString
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object drivetrain:diagnosis_RegistryStringLiteral ;
  drivetrain:predicate ontologies:TURBO_0006515 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
drivetrain:DiagPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:DiagColumnTerm ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
drivetrain:DiagColumnTerm
  a drivetrain:ClassResourceList ;
.
drivetrain:DiagTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:condition_start_datetime_DateLiteralValue ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:subject obo:IAO_0000416 ;
.

drivetrain:DiagHasTimestamp  
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000581 ;
  drivetrain:subject obo:OGMS_0000073 ;
.

drivetrain:DiagTmdPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010604 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000416 ;
.

drivetrain:KeyDenotesDiagTmd
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:CondMentionedClassList
  a drivetrain:ClassResourceList ;
.

drivetrain:DiagMentionAxiomContext a drivetrain:TurboGraphContext .
obo:OGMS_0000073 drivetrain:hasPossibleContext drivetrain:DiagMentionAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:DiagMentionAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:DiagMentionAxiomContext .

 
### diagnosis aboutness
drivetrain:DiagAboutPat 
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:subject obo:OGMS_0000073 ;
.

drivetrain:DiagAboutnessAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:OGMS_0000073 ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:DiagAboutnessAxiomContext ;
.

drivetrain:DiagAboutnessAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000136 ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:DiagAboutnessAxiomContext ;
.

drivetrain:DiagAboutnessAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:DiagAboutnessAxiomContext ;
.

drivetrain:DiagAboutnessAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:DiagAboutnessAxiomContext ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:DiagAboutnessAxiomContext ;
.

drivetrain:DiagAboutnessAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:person_keysym_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:DiagAboutnessAxiomContext ;
.

drivetrain:DiagAboutnessAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010701 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:DiagAboutnessAxiomContext ;
.

drivetrain:DiagKeyDenotesProv4Aboutness
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:DiagAboutnessAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:DiagDenotationContext ;
.

drivetrain:DiagAboutnessAxiomContext a drivetrain:TurboGraphContext .
drivetrain:ProcToBeTyped drivetrain:hasPossibleContext drivetrain:DiagAboutnessAxiomContext . 
owl:Axiom drivetrain:hasPossibleContext drivetrain:DiagAboutnessAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:DiagAboutnessAxiomContext .

drivetrain:DiagDenotationContext a drivetrain:TurboGraphContext .
ontologies:TURBO_0010433 drivetrain:hasPossibleContext drivetrain:DiagDenotationContext .

### diagnosis parthood

drivetrain:EncHsoDiag
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:OGMS_0000073 ;
  drivetrain:predicate  obo:OBI_0000299 ;
  drivetrain:subject  drivetrain:EncToBeTyped ;
.

drivetrain:DiagParthoodAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:DiagParthoodAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:object obo:OBI_0000299 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:DiagParthoodAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:OGMS_0000073 ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:DiagParthoodAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject owl:Axiom ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:DiagParthoodAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:object drivetrain:encounter_keysym_LiteralValue ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:DiagParthoodAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010704 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:DiagKeyDenotesProv4Parthood
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:DiagDenotationContext ;
.

### prescriptions
drivetrain:KeyDenotesRx
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:PDRO_0000024 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:RxKeySymbPartOfKey
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010433 ;
  drivetrain:objectUsesContext drivetrain:KeyContext ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:RxKeySymbPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:RxColumnTerm ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000028 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:RxKeySymbHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:prescription_id_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:IAO_0000028 ; 
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:HardcodedRxSourceden
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:RxSourcedenTerm ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:RxTmdHasMeasVal 
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:prescription_start_datetime_DateLiteralValue ;
  drivetrain:predicate obo:IAO_0000004 ;
  drivetrain:subject obo:IAO_0000416 ;
.

drivetrain:RxHasTimestamp  
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000581 ;
  drivetrain:subject obo:PDRO_0000024 ;
.

drivetrain:RxTmdPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  # drivetrain:object ontologies:TURBO_0010706 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:IAO_0000416 ;
.

drivetrain:KeyDenotesRxTmd
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:IAO_0000416 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:KeyContext ;
.

drivetrain:RxPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:RxColumnTerm ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject obo:PDRO_0000024 ;
.
drivetrain:RxColumnTerm
  a drivetrain:ClassResourceList ;
.
drivetrain:RxHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:drug_code_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:PDRO_0000024 ;
.

drivetrain:RxMentionsCode
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:RxMentionedClassList ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:PDRO_0000024 ;
  drivetrain:subjectUsesContext drivetrain:MentionsContext ;
.

drivetrain:RxMentionedClassList 
  a drivetrain:ClassResourceList ;
.

drivetrain:RxSourcedenTerm
  a drivetrain:ClassResourceList ;
.

### Rx parthood

drivetrain:EncHsoRx
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:PDRO_0000024 ;
  drivetrain:predicate  obo:OBI_0000299 ;
  drivetrain:subject  drivetrain:EncToBeTyped ;
.

drivetrain:RxParthoodAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:EncToBeTyped ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:RxParthoodAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:OBI_0000299 ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:RxParthoodAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:PDRO_0000024 ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:RxParthoodAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:RxParthoodAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object drivetrain:encounter_keysym_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:RxParthoodAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010703 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:ParthoodAxiomContext ;
.

drivetrain:RxKeyDenotesProv4Parthood
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:ParthoodAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:RxDenotationContext ;
.

drivetrain:RxDenotationContext a drivetrain:TurboGraphContext .
ontologies:TURBO_0010433 drivetrain:hasPossibleContext drivetrain:RxDenotationContext .

### Rx aboutness

drivetrain:RxAboutPat
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:subject obo:PDRO_0000024 ;
.

drivetrain:RxAboutnessAxiomSource
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object obo:PDRO_0000024 ;
  drivetrain:predicate owl:annotatedSource ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:RxAboutnessAxiomContext ;
.

drivetrain:RxAboutnessAxiomProp
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000136 ;
  drivetrain:predicate owl:annotatedProperty ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:RxAboutnessAxiomContext ;
.

drivetrain:RxAboutnessAxiomTarget
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate owl:annotatedTarget ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:RxAboutnessAxiomContext ;
.

drivetrain:RxAboutnessAxiomHasDbXref
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:RxAboutnessAxiomContext ;
  drivetrain:predicate oboInOwl:hasDbXref ;
  drivetrain:subject owl:Axiom ;
  drivetrain:subjectUsesContext drivetrain:RxAboutnessAxiomContext ;
.

drivetrain:RxAboutnessAxiomDbXrefHasRepresentation
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object drivetrain:person_keysym_LiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:RxAboutnessAxiomContext ;
.

drivetrain:RxAboutnessAxiomDbXrefPartOfColumn
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  # drivetrain:object ontologies:TURBO_0010702 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:subject ontologies:TURBO_0010404 ;
  drivetrain:subjectUsesContext drivetrain:RxAboutnessAxiomContext ;
.

drivetrain:RxKeyDenotesProv4Aboutness
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010404 ;
  drivetrain:objectUsesContext drivetrain:RxAboutnessAxiomContext ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:subject ontologies:TURBO_0010433 ;
  drivetrain:subjectUsesContext drivetrain:RxDenotationContext ;
.

drivetrain:RxAboutnessAxiomContext a drivetrain:TurboGraphContext .
owl:Axiom drivetrain:hasPossibleContext drivetrain:RxAboutnessAxiomContext . 
ontologies:TURBO_0010404 drivetrain:hasPossibleContext drivetrain:RxAboutnessAxiomContext .

###

drivetrain:WrittenNotDispensedLiteral a drivetrain:LiteralResourceList .
drivetrain:birth_datetime_DateLiteralValue a drivetrain:DateLiteralResourceList .
drivetrain:birth_datetime_StringLiteralValue a drivetrain:StringLiteralResourceList .

drivetrain:condition_start_datetime_DateLiteralValue a drivetrain:LiteralResourceList .
drivetrain:condition_type_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:drug_code_LiteralValue a drivetrain:StringLiteralResourceList .
drivetrain:encounter_cridsym_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:encounter_end_datetime_DateLiteralValue a drivetrain:LiteralResourceList .
drivetrain:encounter_keysym_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:encounter_start_datetime_DateLiteralValue a drivetrain:DateLiteralResourceList .
drivetrain:encounter_type_code_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:gender_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:hce_id_IntegerLiteralValue a drivetrain:LiteralResourceList .
drivetrain:measurement_datetime_DateLiteralValue a drivetrain:LiteralResourceList .
drivetrain:measurement_id_IntegerLiteralValue a drivetrain:LiteralResourceList .
drivetrain:measurement_source_concept_id_IntegerLiteralValue a drivetrain:LiteralResourceList .
drivetrain:measurement_type_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:measurement_units_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:measurement_vocabulary_id_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:person_cridsym_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:person_keysym_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:preceding_encounter_keysym_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:prescription_id_LiteralValue a drivetrain:StringLiteralResourceList .
drivetrain:prescription_start_datetime_DateLiteralValue a drivetrain:LiteralResourceList .
drivetrain:proc_id_IntegerLiteralValue a drivetrain:LiteralResourceList .
drivetrain:procedure_cridsym_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:procedure_datetime_DateLiteralValue a drivetrain:LiteralResourceList .
drivetrain:procedure_type_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:race_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:value_as_number_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:tumor_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:biobankEnc_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:biobankEnc_BmiLiteralValue a drivetrain:DoubleLiteralResourceList .
drivetrain:biobankEnc_lengthMeasLiteralValue a drivetrain:DoubleLiteralResourceList .
drivetrain:biobankEnc_massMeasLiteralValue a drivetrain:DoubleLiteralResourceList .
drivetrain:allelePrefix_LiteralValue a drivetrain:StringLiteralResourceList .
drivetrain:alleleSuffix_LiteralValue a drivetrain:StringLiteralResourceList .
drivetrain:zygosity_LiteralValue a drivetrain:IntegerLiteralResourceList .
drivetrain:alleleSymbol_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:encounter_start_datetime_StringLiteralValue a drivetrain:StringLiteralResourceList .
drivetrain:diagnosis_CodingSequence a drivetrain:IntegerLiteralResourceList .
drivetrain:diagnosis_PrimaryBoolean a drivetrain:BooleanLiteralResourceList .
drivetrain:diagnosis_RegistryStringLiteral a drivetrain:StringLiteralResourceList .
drivetrain:measurementSymbol_LiteralValue a drivetrain:LiteralResourceList .
drivetrain:abnormalMeas_LiteralValue a drivetrain:LiteralResourceList .

###

drivetrain:DiagAboutnessAxiomDbXref_person_id_column a drivetrain:ClassResourceList .
drivetrain:DiagParthoodAxiomDbXref_visit_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:DiagTmd_condition_start_datetime_column a drivetrain:ClassResourceList .
drivetrain:EncCridSym_visit_source_value_column a drivetrain:ClassResourceList .
drivetrain:EncId_visit_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:EncParticipationAxiomDbXref_person_id_column a drivetrain:ClassResourceList .
drivetrain:EncStartDate_visit_start_date_column a drivetrain:ClassResourceList .
drivetrain:EncTypeAxiomDbXref_visit_concept_id_column a drivetrain:ClassResourceList .
drivetrain:EndDate_visit_end_date_column a drivetrain:ClassResourceList .
drivetrain:Key_person_id_column a drivetrain:ClassResourceList .
drivetrain:MeasAboutnessAxiomDbXref_person_id_column a drivetrain:ClassResourceList .
drivetrain:MeasKeySymb_measurement_id_column a drivetrain:ClassResourceList .
drivetrain:MeasMentionAxiomDbXref_measurement_source_value_column a drivetrain:ClassResourceList .
drivetrain:MeasParthoodAxiomDbXref_visit_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:MeasTmdPartOfQueryResult_measurement_datetime_column a drivetrain:ClassResourceList .
drivetrain:MeasTypeAxiomDbXref_measurement_source_concept_id_column a drivetrain:ClassResourceList .
drivetrain:MeasValspecPartOfQueryResult_value_as_number_column a drivetrain:ClassResourceList .
drivetrain:MentioningProvPartOfCol_procedure_source_value_column a drivetrain:ClassResourceList .
drivetrain:MulAxiomDbXref_data_column a drivetrain:ClassResourceList .
drivetrain:PatKeySymb_person_source_value_column a drivetrain:ClassResourceList .
drivetrain:PrecedenceAxiomDbXref_preceding_visit_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:ProcKeySymbPartOfCol_procedure_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:ProcParthoodAxiomDbXref_visit_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:ProcParticipationAxiomDbXref_person_id_column a drivetrain:ClassResourceList .
drivetrain:ProcStartDate_procedure_date_column a drivetrain:ClassResourceList .
drivetrain:ProcTypeAxiomDbXref_procedure_source_concept_id a drivetrain:ClassResourceList .
drivetrain:Rid_race_concept_id_column a drivetrain:ClassResourceList .
drivetrain:RxAboutnessAxiomDbXref_person_id_column a drivetrain:ClassResourceList .
drivetrain:RxParthoodAxiomDbXref_visit_occurrence_id_column a drivetrain:ClassResourceList .
drivetrain:RxTmd_drug_exposure_start_datetime_column a drivetrain:ClassResourceList .

drivetrain:condition_occurrence_id_StringLiteralValue a drivetrain:LiteralResourceList .

###

drivetrain:DiagAboutnessAxiomDbXrefPartOfColumn drivetrain:object drivetrain:DiagAboutnessAxiomDbXref_person_id_column .
drivetrain:DiagParthoodAxiomDbXrefPartOfColumn drivetrain:object drivetrain:DiagParthoodAxiomDbXref_visit_occurrence_id_column .
drivetrain:DiagTmdPartOfColumn drivetrain:object drivetrain:DiagTmd_condition_start_datetime_column .
drivetrain:EncCridSymPartOfColumn drivetrain:object drivetrain:EncCridSym_visit_source_value_column .
drivetrain:EncIdPartOfColumn drivetrain:object drivetrain:EncId_visit_occurrence_id_column .
drivetrain:EncParticipationAxiomDbXrefPartOfColumn drivetrain:object drivetrain:EncParticipationAxiomDbXref_person_id_column .
drivetrain:EncStartDatePartOfColumn drivetrain:object drivetrain:EncStartDate_visit_start_date_column .
drivetrain:EncTypeAxiomDbXrefPartOfColumn drivetrain:object drivetrain:EncTypeAxiomDbXref_visit_concept_id_column .
drivetrain:EndDatePartOfColumn drivetrain:object drivetrain:EndDate_visit_end_date_column .
drivetrain:KeyPartOfColumn drivetrain:object drivetrain:Key_person_id_column .
drivetrain:MeasAboutnessAxiomDbXrefPartOfColumn drivetrain:object drivetrain:MeasAboutnessAxiomDbXref_person_id_column .
drivetrain:MeasKeySymbPartOfColumn drivetrain:object drivetrain:MeasKeySymb_measurement_id_column .
drivetrain:MeasMentionAxiomDbXrefPartOfColumn drivetrain:object drivetrain:MeasMentionAxiomDbXref_measurement_source_value_column .
drivetrain:MeasParthoodAxiomDbXrefPartOfColumn drivetrain:object drivetrain:MeasParthoodAxiomDbXref_visit_occurrence_id_column .
drivetrain:MeasTmdPartOfQueryResult drivetrain:object drivetrain:MeasTmdPartOfQueryResult_measurement_datetime_column .
drivetrain:MeasTypeAxiomDbXrefPartOfColumn drivetrain:object drivetrain:MeasTypeAxiomDbXref_measurement_source_concept_id_column .
drivetrain:MeasValspecPartOfQueryResult drivetrain:object drivetrain:MeasValspecPartOfQueryResult_value_as_number_column .
drivetrain:MentioningProvPartOfCol drivetrain:object drivetrain:MentioningProvPartOfCol_procedure_source_value_column .
drivetrain:MulAxiomDbXrefPartOfColumn drivetrain:object drivetrain:MulAxiomDbXref_data_column .
drivetrain:PatKeySymbPartOfColumn drivetrain:object drivetrain:PatKeySymb_person_source_value_column .
drivetrain:PrecedenceAxiomDbXrefPartOfColumn drivetrain:object drivetrain:PrecedenceAxiomDbXref_preceding_visit_occurrence_id_column .
drivetrain:ProcKeySymbPartOfCol drivetrain:object drivetrain:ProcKeySymbPartOfCol_procedure_occurrence_id_column .
drivetrain:ProcParthoodAxiomDbXrefPartOfColumn drivetrain:object drivetrain:ProcParthoodAxiomDbXref_visit_occurrence_id_column .
drivetrain:ProcParticipationAxiomDbXrefPartOfColumn drivetrain:object drivetrain:ProcParticipationAxiomDbXref_person_id_column .
drivetrain:ProcStartDatePartOfColumn drivetrain:object drivetrain:ProcStartDate_procedure_date_column .
drivetrain:ProcTypeAxiomDbXrefPartOfColumn drivetrain:object drivetrain:ProcTypeAxiomDbXref_procedure_source_concept_id .
drivetrain:RidPartOfColumn drivetrain:object drivetrain:Rid_race_concept_id_column .
drivetrain:RxAboutnessAxiomDbXrefPartOfColumn drivetrain:object drivetrain:RxAboutnessAxiomDbXref_person_id_column .
drivetrain:RxParthoodAxiomDbXrefPartOfColumn drivetrain:object drivetrain:RxParthoodAxiomDbXref_visit_occurrence_id_column .
drivetrain:RxTmdPartOfColumn drivetrain:object drivetrain:RxTmd_drug_exposure_start_datetime_column .

###

