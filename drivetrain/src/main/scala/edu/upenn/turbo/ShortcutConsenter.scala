package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenter(newInstantiation: String, newNamedGraph: String, consenter:Consenter) extends ShortcutGraphObject
{
    val consenterIdentifier = consenter.mandatoryLinks("Identifier").asInstanceOf[ConsenterIdentifier]
    val dateOfBirthDatum = consenter.optionalLinks("DateOfBirthDatum").asInstanceOf[DateOfBirthDatum]
    val genderIdentityDatum = consenter.optionalLinks("GenderIdentityDatum").asInstanceOf[GenderIdentityDatum]
    val raceIdentityDatum = consenter.optionalLinks("RaceIdentityDatum").asInstanceOf[RaceIdentityDatum]

    val instantiation = newInstantiation
    val instantiationKey = "instantiation"
    val baseVariableName = "shortcutPart"
    
    val shortcutName = "shortcutPart"
    val valuesKey = "consenterSymbolValue"
    val registryKey = "consenterRegistryString"
    
    val cridKey = "shortcutCrid"
    val datasetTitle = "shortcutDatasetTitle"
    
    val dateOfBirthString = "dateOfBirthStringValue"
    val dateOfBirthDate = "dateOfBirthDateValue"
    
    val genderIdentityValue = "genderIdentityDatumValue"
    val genderIdentityType = "genderIdentityTypeString"
    
    val raceIdentityValue = "raceIdentityDatumValue"
    val raceIdentityType = "raceIdentityTypeString"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?$cridKey a turbo:TURBO_0000503 .
          ?$cridKey obo:IAO_0000219 ?$baseVariableName .
          ?$cridKey turbo:TURBO_0003603 ?$datasetTitle .
          ?$cridKey turbo:TURBO_0003610 ?$registryKey .
          ?$cridKey turbo:TURBO_0003608 ?$valuesKey .

          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000604  ?$dateOfBirthString .
          }
          OPTIONAL
          {
           ?$baseVariableName turbo:TURBO_0000605   ?$dateOfBirthDate .
          }
          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000606  ?$genderIdentityValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000607   ?$genderIdentityType .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000614 ?$raceIdentityType .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000615 ?$raceIdentityValue .
          }
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName, registryKey, valuesKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array(consenterIdentifier.registryKey, raceIdentityDatum.raceIdentityType),
                              InstantiationStringToURI -> Array(consenterIdentifier.instantiation),
                              URIToString -> Array(consenter.shortcutName),
                              MD5LocalRandom -> Array(consenter.biosexKey, consenter.birthVariableName, consenter.heightKey, consenter.weightKey,
                                                      consenter.adiposeKey),
                              MD5GlobalRandom -> Array(consenter.baseVariableName),
                              DatasetIRI -> Array(consenterIdentifier.dataset),
                              RandomUUID -> Array(consenterIdentifier.baseVariableName, consenterIdentifier.consenterSymbol, consenterIdentifier.consenterRegistry),
                              BindIfBoundMD5LocalRandom -> Array(genderIdentityDatum.baseVariableName, raceIdentityDatum.baseVariableName, 
                                                                 raceIdentityDatum.raceIdentificationProcess, dateOfBirthDatum.baseVariableName),
                              BiologicalSexIRI -> Array(genderIdentityDatum.genderIdentityType),
                              BindIfBoundDataset -> Array(dateOfBirthDatum.dataset, raceIdentityDatum.dataset, genderIdentityDatum.dataset),
                              BindAs -> Array(dateOfBirthDatum.dateOfBirthDate, raceIdentityDatum.raceIdentityValue, genderIdentityDatum.genderIdentityValue, 
                                              dateOfBirthDatum.dateOfBirthString, consenterIdentifier.valuesKey, consenterIdentifier.datasetTitle)
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          genderIdentityDatum.baseVariableName -> genderIdentityValue, 
                                          raceIdentityDatum.baseVariableName -> raceIdentityType, 
                                          raceIdentityDatum.raceIdentificationProcess -> raceIdentityType, 
                                          dateOfBirthDatum.dataset -> dateOfBirthString, 
                                          raceIdentityDatum.dataset -> raceIdentityType, 
                                          genderIdentityDatum.dataset -> genderIdentityValue,
                                          dateOfBirthDatum.baseVariableName -> dateOfBirthString,
                                          genderIdentityDatum.genderIdentityType -> genderIdentityType
                                        )

    val expandedVariableShortcutBindings = Map(
                                          raceIdentityDatum.raceIdentityValue -> raceIdentityValue,
                                          dateOfBirthDatum.dateOfBirthString -> dateOfBirthString,
                                          dateOfBirthDatum.dateOfBirthDate -> dateOfBirthDate,
                                          consenterIdentifier.valuesKey -> valuesKey,
                                          consenterIdentifier.registryKey -> registryKey, 
                                          genderIdentityDatum.genderIdentityValue -> genderIdentityValue, 
                                          raceIdentityDatum.raceIdentityType -> raceIdentityType,
                                          consenter.shortcutName -> shortcutName,
                                          consenterIdentifier.instantiation -> instantiationKey,
                                          consenterIdentifier.datasetTitle -> datasetTitle
                                        )
                                        
     val appendToBind = """"""
     
     val optionalLinks: Map[String, ExpandedGraphObject] = Map()
     val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
}