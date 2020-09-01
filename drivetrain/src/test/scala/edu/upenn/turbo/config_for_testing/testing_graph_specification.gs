ontologies:DNAExtractionProcessToDNAExtract
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001051 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0000257 ;
.
ontologies:DNAExtractionProcessToSpecimen
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001479 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0000257 ;
.
ontologies:DNAToDNAExtract
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:OBI_0001051 ;
  drivetrain:predicate obo:OBI_0000643 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001868 ;
.
ontologies:DNAToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:OGG_0000000014 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001868 ;
.
ontologies:DatasetToDiastolicBloodPressureMeasurementDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:HTN_00000000 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:DatasetToSystolicBloodPressureMeasurementDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:HTN_00000001 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:DiastolicBloodPressureMeasurementDatumToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:HTN_00000000 ;
.
ontologies:DiastolicBloodPressureMeasurementDatumToValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010150 ;
  drivetrain:predicate obo:OBI_0001938 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:HTN_00000000 ;
.
ontologies:DiastolicBloodPressureValueSpecificationToMercuryMillimeters
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:UO_0000272 ;
  drivetrain:predicate obo:IAO_0000039 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010150 ;
.
ontologies:DiastolicBloodPressureValueSpecificationToValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:diastolicBloodPressureDoubleLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010150 ;
.
ontologies:SystolicBloodPressureMeasurementDatumToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:HTN_00000001 ;
.
ontologies:SystolicBloodPressureMeasurementDatumToValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010149 ;
  drivetrain:predicate obo:OBI_0001938 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:HTN_00000001 ;
.
ontologies:SystolicBloodPressureValueSpecificationToMercuryMillimeters
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:UO_0000272 ;
  drivetrain:predicate obo:IAO_0000039 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010149 ;
.
ontologies:SystolicBloodPressureValueSpecificationToValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:systolicBloodPressureDoubleLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010149 ;
.
ontologies:alleleCridSymbolToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000568 ;
.
ontologies:alleleToDNA
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:OBI_0001868 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:alleleToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:alleleToGeneSymbolFirstPart
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:alleleGeneSymbolFirstPartStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010016 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:alleleToGeneSymbolSecondPart
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:alleleGeneSymbolSecondPartStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010015 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:alleleToGeneSymbolUri
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:GeneSymbolUriOfVariousTypes ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:alleleToZygosityIntegerValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:alleleZygosityIntegerLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010095 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:alleleToZygosityUri
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:ZygosityUriOfVariousTypes ;
  drivetrain:predicate obo:OBI_0001938 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001352 ;
.
ontologies:biobankEncounterDateToBiobankEncounterStart
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000531 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000532 ;
.
ontologies:biobankEncounterDateToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000532 ;
.
ontologies:biobankEncounterDateToDateLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:biobankEncounterDateLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010096 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000532 ;
.
ontologies:biobankEncounterDateToStringLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:biobankEncounterDateStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010095 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000532 ;
.
ontologies:biobankEncounterIdToBiobankEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000533 ;
.
ontologies:biobankEncounterIdToRegistryDenoter
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-singleton ;
  drivetrain:object ontologies:BiobankEncounterRegistryOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000533 ;
.
ontologies:biobankEncounterIdToSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000534 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000533 ;
.
ontologies:biobankEncounterRegistryDenoterToId
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality drivetrain:singleton-many ;
  drivetrain:object ontologies:TURBO_0000533 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:BiobankEncounterRegistryOfVariousTypes ;
.
ontologies:biobankEncounterStartToBiobankEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:RO_0002223 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000531 ;
.
ontologies:biobankEncounterSymbolToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000534 ;
.
ontologies:biobankEncounterSymbolToId
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000533 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000534 ;
.
ontologies:biobankEncounterSymbolToSymbolValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:biobankEncounterSymbolStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000534 ;
.
ontologies:biobankEncounterToBMI
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object efo:EFO_0004340 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:subject ontologies:TURBO_0000527 ;
.
ontologies:biobankEncounterToCollectionProcess
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0600005 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject ontologies:TURBO_0000527 ;
.
ontologies:biobankEncounterToLengthMeasurementValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010138 ;
  drivetrain:predicate ontologies:TURBO_0010139 ;
  drivetrain:subject ontologies:TURBO_0000527 ;
.
ontologies:biobankEncounterToMassMeasurementValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001929 ;
  drivetrain:predicate ontologies:TURBO_0010139 ;
  drivetrain:subject ontologies:TURBO_0000527 ;
.
ontologies:bloodPressureMeasurementProcessToDiastolicBloodPressureMeasurementDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:HTN_00000000 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:VSO_0000006 ;
.
ontologies:bloodPressureMeasurementProcessToHealthcareEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OGMS_0000097 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:VSO_0000006 ;
.
ontologies:bloodPressureMeasurementProcessToSystolicBloodPressureMeasurementDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:HTN_00000001 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:VSO_0000006 ;
.
ontologies:bloodPressureToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:RO_0000052 ;
  drivetrain:subject obo:VSO_0000004 ;
.
ontologies:bmiToBmiDoubleLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:bmiDoubleLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject efo:EFO_0004340 ;
.
ontologies:bmiToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject efo:EFO_0004340 ;
.
ontologies:bmiToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:subject efo:EFO_0004340 ;
.
ontologies:cancerToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:RO_0000052 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:MONDO_0004992 ;
.
ontologies:cancerToTumor
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:TURBO_0010070 ;
  drivetrain:predicate obo:IDO_0000664 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:MONDO_0004992 ;
.
ontologies:collectionProcessToBiobankEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0600005 ;
.
ontologies:collectionProcessToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0600005 ;
.
ontologies:collectionProcessToSpecimen
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001479 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0600005 ;
.
ontologies:datasetToAllele
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:OBI_0001352 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToAlleleCridSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000568 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToBiobankEncounterDate
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000532 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToBiobankEncounterSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000534 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToBmi
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object efo:EFO_0004340 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToDatasetTitle
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality drivetrain:1-1 ;
  drivetrain:object ontologies:datasetTitleStringLiteralValue ;
  drivetrain:predicate dc11:title ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToDateOfBirthDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object efo:EFO_0004950 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToDiagnosis
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:OGMS_0000073 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToGenderIdentityDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:GenderIdentityDatumOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToHealthcareEncounterDate
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000512 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToHealthcareEncounterSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000509 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToHomoSapiensSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000504 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToLengthMeasurementValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0010138 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToMassMeasurementValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:OBI_0001929 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToMedicationSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:TURBO_0000562 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToPrescription
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object obo:PDRO_0000001 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:datasetToRaceIdentityDatum
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:1-many ;
  drivetrain:object ontologies:RaceIdentityDatumOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:IAO_0000100 ;
.
ontologies:dateOfBirthDatumToBirth
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:UBERON_0035946 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject efo:EFO_0004950 ;
.
ontologies:dateOfBirthDatumToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject efo:EFO_0004950 ;
.
ontologies:dateOfBirthDatumToDateLiteral
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:homoSapiensDateOfBirthDateLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010096 ;
  drivetrain:subject efo:EFO_0004950 ;
.
ontologies:dateOfBirthDatumToStringLiteral
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:homoSapiensDateOfBirthStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010095 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject efo:EFO_0004950 ;
.
ontologies:diagnosisToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToDiagnosisCodeSuffixLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:diagnosisTermSuffixStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToDiagnosisCodingSequenceLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:diagnosisCodingSequenceIntegerLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010014 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToDiagnosisRegistry
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:DiagnosisRegistryOfVariousTypes ;
  drivetrain:predicate ontologies:TURBO_0000703 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToDiagnosisRegistryStringLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:diagnosisRegistryStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0006515 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToIcdTerm
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:IcdTermOfVariousTypes ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToPrimaryDiagnosisBooleanLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:primaryDiagnosisBooleanLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010013 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diagnosisToSnomedTerm
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:SnomedTermOfVariousTypes ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:OGMS_0000073 ;
.
ontologies:diastolicBloodPressureMeasurementDatumToBloodPressure
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:VSO_0000004 ;
  drivetrain:predicate obo:IAO_0000221 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:HTN_00000000 ;
.
ontologies:exomeSequenceProcessToDNAExtract
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001051 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0002118 ;
.
ontologies:exomeSequenceProcessToSequenceData
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001573 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0002118 ;
.
ontologies:formProcessToAllele
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:OBI_0001352 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0200000 ;
.
ontologies:formProcessToSequenceData
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001573 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0200000 ;
.
ontologies:genderIdentityDatumToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:GenderIdentityDatumOfVariousTypes ;
.
ontologies:genderIdentityDatumToGenderIdentityDatumLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:homoSapiensGenderIdentityStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:GenderIdentityDatumOfVariousTypes ;
.
ontologies:genderIdentityDatumToGenderIdentityDatumType
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:GenderIdentityDatumType ;
  drivetrain:predicate rdf:type ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:GenderIdentityDatumOfVariousTypes ;
.
ontologies:genderIdentityDatumToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:GenderIdentityDatumOfVariousTypes ;
.
ontologies:genomeCridRegistryDenoterToGenomeCrid
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality drivetrain:singleton-many ;
  drivetrain:object ontologies:TURBO_0000566 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:GenomeRegistryOfVariousTypes ;
.
ontologies:genomeCridSymbolToGenomeCrid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000566 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000568 ;
.
ontologies:genomeCridSymbolToSymbolValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:alleleGenoIdStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000568 ;
.
ontologies:genomeCridToGenomeCridRegistryDenoter
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-singleton ;
  drivetrain:object ontologies:GenomeRegistryOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000566 ;
.
ontologies:genomeCridToGenomeCridSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000568 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000566 ;
.
ontologies:genomeCridToSpecimen
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001479 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000566 ;
.
ontologies:healthcareEncounterDateToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000512 ;
.
ontologies:healthcareEncounterDateToDateLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:healthcareEncounterDateLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010096 ;
  drivetrain:subject ontologies:TURBO_0000512 ;
.
ontologies:healthcareEncounterDateToHealthcareEncounterStart
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000511 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000512 ;
.
ontologies:healthcareEncounterDateToStringLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:healthcareEncounterDateStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010095 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000512 ;
.
ontologies:healthcareEncounterIdToRegistryDenoter
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-singleton ;
  drivetrain:object ontologies:HealthcareEncounterRegistryOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000508 ;
.
ontologies:healthcareEncounterIdToSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000509 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000508 ;
.
ontologies:healthcareEncounterRegistryDenoterToId
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality drivetrain:singleton-many ;
  drivetrain:object ontologies:TURBO_0000508 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:HealthcareEncounterRegistryOfVariousTypes ;
.
ontologies:healthcareEncounterStartToHealthcareEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OGMS_0000097 ;
  drivetrain:predicate obo:RO_0002223 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000511 ;
.
ontologies:healthcareEncounterSymbolToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000509 ;
.
ontologies:healthcareEncounterSymbolToId
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000508 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000509 ;
.
ontologies:healthcareEncounterSymbolToSymbolValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:healthcareEncounterSymbolLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000509 ;
.
ontologies:healthcareEncounterToBMI
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object efo:EFO_0004340 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:subject obo:OGMS_0000097 ;
.
ontologies:healthcareEncounterToBloodPressureMeasurementProcess
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:VSO_0000006 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:OGMS_0000097 ;
.
ontologies:healthcareEncounterToDiagnosis
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:OGMS_0000073 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:OGMS_0000097 ;
.
ontologies:healthcareEncounterToLengthMeasurementValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010138 ;
  drivetrain:predicate ontologies:TURBO_0010139 ;
  drivetrain:subject obo:OGMS_0000097 ;
.
ontologies:healthcareEncounterToMassValueSpecification
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OBI_0001929 ;
  drivetrain:predicate ontologies:TURBO_0010139 ;
  drivetrain:subject obo:OGMS_0000097 ;
.
ontologies:healthcareEncounterToPrescription
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:PDRO_0000001 ;
  drivetrain:predicate obo:OBI_0000299 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:OGMS_0000097 ;
.
ontologies:homoSapiensIdToRegistryDenoter
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality drivetrain:many-singleton ;
  drivetrain:object ontologies:HomoSapiensRegistryOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010092 ;
.
ontologies:homoSapiensIdToSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000504 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010092 ;
.
ontologies:homoSapiensIdentifierToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010092 ;
.
ontologies:homoSapiensRegistryDenoterToId
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality drivetrain:singleton-many ;
  drivetrain:object ontologies:TURBO_0010092 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:HomoSapiensRegistryOfVariousTypes ;
.
ontologies:homoSapiensSymbolToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000504 ;
.
ontologies:homoSapiensSymbolToSymbolValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:homoSapiensSymbolStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000504 ;
.
ontologies:homoSapiensToBiobankEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:RO_0000056 ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToBiosex
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:PATO_0000047 ;
  drivetrain:predicate obo:RO_0000086 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToBirth
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:UBERON_0035946 ;
  drivetrain:predicate ontologies:TURBO_0000303 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToHealthcareEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object obo:OGMS_0000097 ;
  drivetrain:predicate obo:RO_0000056 ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToHeight
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:PATO_0000119 ;
  drivetrain:predicate obo:RO_0000086 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToInvestigationRole
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-superSingleton ;
  drivetrain:object obo:OBI_0000097 ;
  drivetrain:predicate obo:RO_0000087 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToPatientRole
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-superSingleton ;
  drivetrain:object obo:OBI_0000093 ;
  drivetrain:predicate obo:RO_0000087 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToTumor
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:object ontologies:TURBO_0010070 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:homoSapiensToWeight
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:PATO_0000128 ;
  drivetrain:predicate obo:RO_0000086 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:subject obo:NCBITaxon_9606 ;
.
ontologies:identifierToHealthcareEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:OGMS_0000097 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000508 ;
.
ontologies:identifierToMedicationSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000562 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000561 ;
.
ontologies:identifierToPrescription
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:PDRO_0000001 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000561 ;
.
ontologies:instantiationToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:superSingleton-many ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:OBI_0000293 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000522 ;
.
ontologies:investigationRoleToBiobankEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:superSingleton-many ;
  drivetrain:object ontologies:TURBO_0000527 ;
  drivetrain:predicate obo:BFO_0000054 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0000097 ;
.
ontologies:lengthMeasurementValueSpecificationToCentimeter
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:UO_0000015 ;
  drivetrain:predicate obo:IAO_0000039 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010138 ;
.
ontologies:lengthMeasurementValueSpecificationToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010138 ;
.
ontologies:lengthMeasurementValueSpecificationToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:subject ontologies:TURBO_0010138 ;
.
ontologies:lengthMeasurementValueSpecificationToLengthMeasurementLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:lengthMeasurementDoubleLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010138 ;
.
ontologies:massMeasurementValueSpecificationToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001929 ;
.
ontologies:massMeasurementValueSpecificationToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:subject obo:OBI_0001929 ;
.
ontologies:massMeasurementValueSpecificationToHomoSapiensWeight
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:PATO_0000128 ;
  drivetrain:predicate obo:IAO_0000221 ;
  drivetrain:subject obo:OBI_0001929 ;
.
ontologies:lengthMeasurementValueSpecificationToHomoSapiensHeight
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:PATO_0000119 ;
  drivetrain:predicate obo:IAO_0000221 ;
  drivetrain:subject turbo:TURBO_0010138 ;
.
ontologies:massMeasurementValueSpecificationToKilogram
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:UO_0000009 ;
  drivetrain:predicate obo:IAO_0000039 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0001929 ;
.
ontologies:massMeasurementValueSpecificationToMassMeasurementLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:massMeasurementDoubleLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:OBI_0001929 ;
.
ontologies:medicationSymbolToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0000562 ;
.
ontologies:medicationSymbolToMedicationSymbolStringLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:medicationSymbolStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000562 ;
.
ontologies:medicationSymbolToidentifier
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0000561 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000562 ;
.
ontologies:patientRoleToHealthcareEncounter
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:superSingleton-many ;
  drivetrain:object obo:OGMS_0000097 ;
  drivetrain:predicate obo:BFO_0000054 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:OBI_0000093 ;
.
ontologies:prescriptionToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:PDRO_0000001 ;
.
ontologies:prescriptionToDrug
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:DrugTermOfVariousTypes ;
  drivetrain:predicate obo:IAO_0000142 ;
  drivetrain:subject obo:PDRO_0000001 ;
.
ontologies:prescriptionToMedicationOrderNameStringLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:medicationOrderNameStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:subject obo:PDRO_0000001 ;
.
ontologies:raceIdentityDatumToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:RaceIdentityDatumOfVariousTypes ;
.
ontologies:raceIdentityDatumToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:IAO_0000136 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:RaceIdentityDatumOfVariousTypes ;
.
ontologies:raceIdentityDatumToRaceIdentityDatumLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:homoSapiensRaceIdentityStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:RaceIdentityDatumOfVariousTypes ;
.
ontologies:raceIdentityDatumToRaceIdentityDatumType
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:RaceIdentityDatumType ;
  drivetrain:predicate rdf:type ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:RaceIdentityDatumOfVariousTypes ;
.
ontologies:symbolToHomoSapiensId
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010092 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0000504 ;
.
ontologies:systolicBloodPressureMeasurementDatumToBloodPressure
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:VSO_0000004 ;
  drivetrain:predicate obo:IAO_0000221 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject obo:HTN_00000001 ;
.
ontologies:tumorCridSymbolToSymbolLiteralValue
  a drivetrain:InstanceToLiteralRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:tumorSymbolStringLiteralValue ;
  drivetrain:predicate ontologies:TURBO_0010094 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000577 ;
.
ontologies:tumorCridSymbolToTumorCrid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010188 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject obo:IAO_0000577 ;
.
ontologies:tumorCridToTumor
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010070 ;
  drivetrain:predicate obo:IAO_0000219 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010188 ;
.
ontologies:tumorCridToDataset
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/many-1> ;
  drivetrain:object obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010188 ;
.
ontologies:datasetToTumorCrid
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-many> ;
  drivetrain:subject obo:IAO_0000100 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:objectExists ;
  drivetrain:object ontologies:TURBO_0010188 ;
.
ontologies:tumorCridToTumorCridSymbol
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object obo:IAO_0000577 ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010188 ;
.
ontologies:tumorCridToTumorRegistryDenoter
  a drivetrain:InstanceToTermRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TumorRegistryDenoterOfVariousTypes ;
  drivetrain:predicate obo:BFO_0000051 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TURBO_0010188 ;
.
ontologies:tumorRegistryDenoterToTumorCrid
  a drivetrain:TermToInstanceRecipe ;
  drivetrain:cardinality <https://github.com/PennTURBO/Drivetrain/1-1> ;
  drivetrain:object ontologies:TURBO_0010188 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:eitherSubjectOrObjectExists ;
  drivetrain:subject ontologies:TumorRegistryDenoterOfVariousTypes ;
.
ontologies:tumorToHomoSapiens
  a drivetrain:InstanceToInstanceRecipe ;
  drivetrain:cardinality drivetrain:many-1 ;
  drivetrain:object obo:NCBITaxon_9606 ;
  drivetrain:predicate obo:BFO_0000050 ;
  drivetrain:mustExecuteIf drivetrain:subjectExists ;
  drivetrain:subject ontologies:TURBO_0010070 ;
.
ontologies:BiobankEncounterRegistryOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0000535 ;
  drivetrain:range ontologies:TURBO_0010297 ;
  drivetrain:range ontologies:TURBO_0010298 ;
  drivetrain:range ontologies:TURBO_0010299 ;
.
ontologies:HealthcareEncounterDatabaseProvenanceTerm
  a drivetrain:ClassResourceList ;
.
ontologies:HealthcareEncounterSchemaProvenanceTerm
  a drivetrain:ClassResourceList ;
.
ontologies:HealthcareEncounterTypeCodeColumnTerm
  a drivetrain:ClassResourceList ;
.
ontologies:HealthcareEncounterTableProvenanceTerm
  a drivetrain:ClassResourceList ;
.
ontologies:DiagnosisRegistryOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range Thesaurus:C53489 ;
  drivetrain:range Thesaurus:C71890 ;
  drivetrain:range Thesaurus:C71892 ;
.
ontologies:DrugTermOfVariousTypes
  a drivetrain:ClassResourceList ;
.
ontologies:GenderIdentityDatumOfVariousTypes
  a drivetrain:UntypedInstance ;
.
ontologies:GenderIdentityDatumType
  a drivetrain:ClassResourceList ;
  drivetrain:range obo:OMRSE_00000133 ;
  drivetrain:range obo:OMRSE_00000138 ;
  drivetrain:range obo:OMRSE_00000141 ;
.
ontologies:GeneSymbolUriOfVariousTypes
  a drivetrain:ClassResourceList ;
.
ontologies:GenomeRegistryOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0000567 ;
.
ontologies:HealthcareEncounterRegistryOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0000510 ;
  drivetrain:range ontologies:TURBO_0010256 ;
.
ontologies:HomoSapiensRegistryOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0000505 ;
  drivetrain:range ontologies:TURBO_0010275 ;
  drivetrain:range ontologies:TURBO_0010295 ;
.
ontologies:IcdTermOfVariousTypes
  a drivetrain:ClassResourceList ;
.
ontologies:RaceIdentityDatumOfVariousTypes
  a drivetrain:UntypedInstance ;
.
ontologies:RaceIdentityDatumType
  a drivetrain:ClassResourceList ;
  drivetrain:range obo:OBI_0000852 ;
  drivetrain:range obo:OMRSE_00000098 ;
  drivetrain:range obo:OMRSE_00000180 ;
  drivetrain:range obo:OMRSE_00000181 ;
  drivetrain:range obo:OMRSE_00000182 ;
  drivetrain:range obo:OMRSE_00000183 ;
  drivetrain:range obo:OMRSE_00000184 ;
  drivetrain:range ontologies:TURBO_0001551 ;
.
ontologies:SnomedTermOfVariousTypes
  a drivetrain:ClassResourceList ;
.
ontologies:TumorRegistryDenoterOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0010274 ;
.
ontologies:ZygosityUriOfVariousTypes
  a drivetrain:ClassResourceList ;
  drivetrain:range ontologies:TURBO_0000590 ;
  drivetrain:range ontologies:TURBO_0000591 ;
.
ontologies:alleleGeneSymbolFirstPartStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:alleleGeneSymbolSecondPartStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:alleleGeneSymbolStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:alleleGenoIdStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:alleleZygosityIntegerLiteralValue
  a drivetrain:IntegerLiteralResourceList ;
.
ontologies:biobankEncounterDateLiteralValue
  a drivetrain:DateLiteralResourceList ;
.
ontologies:biobankEncounterDateStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:biobankEncounterSymbolStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:bmiDoubleLiteralValue
  a drivetrain:DoubleLiteralResourceList ;
.
ontologies:encounterTypeCodeLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:datasetTitleStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:diagnosisCodingSequenceIntegerLiteralValue
  a drivetrain:IntegerLiteralResourceList ;
.
ontologies:diagnosisRegistryStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:diagnosisTermSuffixStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:diastolicBloodPressureDoubleLiteralValue
  a drivetrain:DoubleLiteralResourceList ;
.
ontologies:healthcareEncounterDateLiteralValue
  a drivetrain:DateLiteralResourceList ;
.
ontologies:healthcareEncounterDateStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:healthcareEncounterSymbolLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:homoSapiensDateOfBirthDateLiteralValue
  a drivetrain:DateLiteralResourceList ;
.
ontologies:homoSapiensDateOfBirthStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:homoSapiensGenderIdentityStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:homoSapiensRaceIdentityStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:homoSapiensSymbolStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:lengthMeasurementDoubleLiteralValue
  a drivetrain:DoubleLiteralResourceList ;
.
drivetrain:many-1
  a drivetrain:TurboGraphCardinalityRule ;
.
drivetrain:many-singleton
  a drivetrain:TurboGraphCardinalityRule ;
.
drivetrain:many-superSingleton
  a drivetrain:TurboGraphCardinalityRule ;
.
ontologies:massMeasurementDoubleLiteralValue
  a drivetrain:DoubleLiteralResourceList ;
.
ontologies:medicationOrderNameStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:medicationSymbolStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.
ontologies:primaryDiagnosisBooleanLiteralValue
  a drivetrain:BooleanLiteralResourceList ;
.
ontologies:systolicBloodPressureDoubleLiteralValue
  a drivetrain:DoubleLiteralResourceList ;
.
ontologies:tumorSymbolStringLiteralValue
  a drivetrain:StringLiteralResourceList ;
.