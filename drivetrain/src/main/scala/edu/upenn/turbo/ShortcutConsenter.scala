package edu.upenn.turbo

import scala.collection.mutable.LinkedHashMap

class ShortcutConsenter(newInstantiation: String, newNamedGraph: String) extends ShortcutGraphObject
{

    val instantiation = newInstantiation
    
    val pattern = """
          
          ?shortcutPart a turbo:TURBO_0000502 .
          ?shortcutCrid a turbo:TURBO_0000503 .
          ?shortcutCrid obo:IAO_0000219 ?shortcutPart .
          ?shortcutCrid turbo:TURBO_0003603 ?datasetTitle .
          ?shortcutCrid turbo:TURBO_0003610 ?consenterRegistryString .
          ?shortcutCrid turbo:TURBO_0003608 ?consenterSymbolValue .

          OPTIONAL
          {
            ?shortcutPart  turbo:TURBO_0000604  ?dateOfBirthStringValue .
          }
          OPTIONAL
          {
          ?shortcutPart turbo:TURBO_0000605   ?dateOfBirthDateValue
          }
          OPTIONAL
          {
            ?shortcutPart  turbo:TURBO_0000606  ?genderIdentityDatumValue .
          }
          OPTIONAL
          {
            ?shortcutPart turbo:TURBO_0000607   ?gidTypeString .
          }
          OPTIONAL
          {
            ?shortcutPart turbo:TURBO_0000614 ?ridTypeString .
          }
          OPTIONAL
          {
            ?shortcutPart turbo:TURBO_0000615 ?ridString .
          }
      """

    val connections = Map("" -> "")
    
    val namedGraph = newNamedGraph
    
    val baseVariableName = "shortcutPart"
    
    val typeURI = "http://transformunify.org/ontologies/TURBO_0000502"
    
    val variablesToSelect = Array("shortcutPart", "consenterRegistryString", "consenterSymbolValue")

    def getValuesKey(): String = "consenterSymbolValue"
    def getRegistryKey(): String = "consenterRegistryString"

    val variableExpansions = LinkedHashMap(
                              StringToURI -> Array("instantiation", "partReg", "ridType"),
                              URIToString -> Array("shortcutPartName"),
                              MD5LocalRandom -> Array("biosex", "birth", "height", "weight", "adipose"),
                              MD5GlobalRandom -> Array("part"),
                              DatasetIRI -> Array("dataset"),
                              RandomUUID -> Array("partCrid", "partSymbol", "partRegDen"),
                              BindIfBoundMD5LocalRandom -> Array("gid", "rid", "rip", "dob"),
                              BiologicalSexIRI -> Array("gidType"),
                              BindIfBoundDataset -> Array("dateDataset", "raceDataset", "genderDataset"),
                              BindAs -> Array("dobValue", "ridValue", "gidValue", "dobDate", "partSymbolValue")
                            )

    val expandedVariableShortcutDependencies = Map(
                                          "instantiation" -> "instantiationUUID", 
                                          "partReg" -> "consenterRegistryString", 
                                          "gidValue" -> "genderIdentityDatumValue", 
                                          "ridType" -> "ridTypeString", 
                                          "shortcutPartName" -> "shortcutPart", 
                                          "gid" -> "genderIdentityDatumValue", 
                                          "rid" -> "ridTypeString", 
                                          "rip" -> "ridTypeString", 
                                          "gidType" -> "gidTypeString", 
                                          "dateDataset" -> "dateOfBirthStringValue", 
                                          "raceDataset" -> "ridTypeString", 
                                          "genderDataset" -> "genderIdentityDatumValue",
                                          "dob" -> "dateOfBirthStringValue",
                                          "ridValue" -> "ridString",
                                          "dobValue" -> "dateOfBirthStringValue",
                                          "dobDate" -> "dateOfBirthDateValue",
                                          "partSymbolValue" -> "consenterSymbolValue"
                                        )
}