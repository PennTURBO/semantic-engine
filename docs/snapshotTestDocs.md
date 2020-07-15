# Snapshot Test Docs

The Semantic Engine includes the ability to automatically generate tests to capture the input and output of a given update specification at a given point in time. These tests help to ensure that changes to the Semantic Engine codebase do not disrupt the functionality of current Transformation Instruction Sets. Before generating a test for a given update specification, it is up to the user to ensure that the output of the update is as expected. The easiest way to do this is to use the debug mode.

The Snapshot Test builder automatically generates a toy triples dataset based on the provided update specification's inputs, and then runs the update to discover the outputs. The toy dataset is generated with the following rules:

1. URIs representing instances of classes are created by appending an underscore to the class URI, and appending an integer that is incremented by one each time a new instance of the class is created. For example, the first instance of class `obo:NCBITaxon_9606` would have URI `obo:NCBITaxon_9606_1`. The second instance would have URI `obo:NCBITaxon_9606_2`.
2. The number of instances of a given class to be created is determined by the cardinality specified in the Connection Recipes. Each `1-many` or `many-1` cardinality statement increases the number of instances of the class on the `many` side of the relationship by a factor of 2.
3. Placeholders for a set of possible terms (referenced in the Semantic Engine Language as ClassResourceLists) are assigned class values based on the first element in the range list provided. If no range list is provided, the top-level BFO term `obo:BFO_0000001` is used.
4. Literal values (referenced in the Semantic Engine Languge as LiteralResourceLists) are created based on the type of the literal. The table below shows how the type of the literal effects how the literal is created.

| Literal Type                       | Method of Generation                                                                           | XSD type assigned |
|------------------------------------|------------------------------------------------------------------------------------------------|-------------------|
| LiteralResourceList (generic type) | Absolute Value of HashCode of LiteralResourceList's URI + "abc"                                |    xsd:String     |
| StringLiteralResourceList          | Absolute Value of HashCode of LiteralResourceList's URI + "abc"                                |    xsd:String     |
| IntegerLiteralResourceList         | Absolute Value of HashCode of LiteralResourceList's URI                                        |    xsd:Integer    |
| DoubleLiteralResourceList          | Absolute Value of HashCode of LiteralResourceList's URI + ".00"                                |    xsd:Double     |
| BooleanLiteralResourceList         | "true"                                                                                         |    xsd:Boolean    |
| DateLiteralResourceList            | First six characters of HashCode of LiteralResourceList's URI, with slashes every 2 characters |    xsd:Date       |

To see a code-based specification of the expected output of the triples generation service, see [this ScalaTest class](../drivetrain/src/test/scala/edu/upenn/turbo/TestBuilderIntegrationTests.scala).
