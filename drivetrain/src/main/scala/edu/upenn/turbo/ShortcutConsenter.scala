package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenter(newInstantiation: String, newNamedGraph: String) extends ShortcutGraphObject
{

    val instantiation = newInstantiation
    val baseVariableName = "shortcutPart"
    val valuesKey = "consenterSymbolValue"
    val registryKey = "consenterRegistryString"
    
    val pattern = s"""
          
          ?$baseVariableName a turbo:TURBO_0000502 .
          ?shortcutCrid a turbo:TURBO_0000503 .
          ?shortcutCrid obo:IAO_0000219 ?$baseVariableName .
          ?shortcutCrid turbo:TURBO_0003603 ?shortcutDatasetTitle .
          ?shortcutCrid turbo:TURBO_0003610 ?$registryKey .
          ?shortcutCrid turbo:TURBO_0003608 ?$valuesKey .

          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000604  ?dateOfBirthStringValue .
          }
          OPTIONAL
          {
           ?$baseVariableName turbo:TURBO_0000605   ?dateOfBirthDateValue
          }
          OPTIONAL
          {
            ?$baseVariableName  turbo:TURBO_0000606  ?genderIdentityDatumValue .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000607   ?gidTypeString .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000614 ?ridTypeString .
          }
          OPTIONAL
          {
            ?$baseVariableName turbo:TURBO_0000615 ?ridString .
          }
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array(baseVariableName, registryKey, valuesKey)

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array("instantiation", "partReg", "ridType"),
                              URIToString -> Array("shortcutPartName"),
                              MD5LocalRandom -> Array("biosex", "birth", "height", "weight", "adipose"),
                              MD5GlobalRandom -> Array("part"),
                              DatasetIRI -> Array("dataset"),
                              RandomUUID -> Array("consenterCrid", "partSymbol", "partRegDen"),
                              BindIfBoundMD5LocalRandom -> Array("gid", "rid", "rip", "dob"),
                              BiologicalSexIRI -> Array("gidType"),
                              BindIfBoundDataset -> Array("dateDataset", "raceDataset", "genderDataset"),
                              BindAs -> Array("dobValue", "ridValue", "gidValue", "dobDate", "partSymbolValue", "datasetTitle")
                            )

    val expandedVariableShortcutDependencies = Map( 
                                          "gid" -> "genderIdentityDatumValue", 
                                          "rid" -> "ridTypeString", 
                                          "rip" -> "ridTypeString", 
                                          "dateDataset" -> "dateOfBirthStringValue", 
                                          "raceDataset" -> "ridTypeString", 
                                          "genderDataset" -> "genderIdentityDatumValue",
                                          "dob" -> "dateOfBirthStringValue",
                                          "gidType" -> "gidTypeString"
                                        )

    val expandedVariableShortcutBindings = Map(
                                          "ridValue" -> "ridString",
                                          "dobValue" -> "dateOfBirthStringValue",
                                          "dobDate" -> "dateOfBirthDateValue",
                                          "partSymbolValue" -> valuesKey,
                                          "partReg" -> registryKey, 
                                          "gidValue" -> "genderIdentityDatumValue", 
                                          "ridType" -> "ridTypeString",
                                          "shortcutPartName" -> baseVariableName,
                                          "instantiation" -> "instantiationUUID",
                                          "datasetTitle" -> "shortcutDatasetTitle"
                                        )
                                        
     val appendToBind = """"""
     
     val optionalLinks: Map[String, ExpandedGraphObject] = Map()
     val mandatoryLinks: Map[String, ExpandedGraphObject] = Map()
}