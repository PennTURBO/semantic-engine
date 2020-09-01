# Semantic Engine Language Feature Guide

By the time you are reading this guide, you should have a fundamental understanding of the purpose of the Semantic Engine, the Semantic Engine Language, and the functions of its various components: Graph Specification, Transformation Instruction Set, Application Ontology. [This Word document](Semantic%20Engine%20Configuration%20Tutorial.docx) may be helpful.

This document covers all features included in the Semantic Engine Language, including how-to-use examples and derived SPARQL clause snippets. The Semantic Engine Language is used to create both Transformation Instruction Sets (TIS) and Graph Specifications (GS). Although it is conventional to use the TIS to define everything related to an incoming dataset and the GS to define everything related to the output model, this is a matter of convention only. All triples from both of these files will be combined in the Model Repository and read from there by the Semantic Engine, so there is some flexibility allowed. To encourage best practices, the features that are intended to be applied only in the TIS are denoted with an asterisk in this document.

"*" = Transformation Instruction Set-specific feature

## Namespace

For the remainder of this document, the prefix `:` will be used to denote `https://github.com/PennTURBO/Drivetrain/` which is the namespace for the Semantic Engine Language. Prefixes can be defined in the file `prefixes.txt`. Although a blank prefix is used for simplicity of example in this document, blank prefixes in the prefix file are not allowed.

## Semantic Engine Language Graph Pattern Elements

In the Semantic Engine Language, relationships are declared between Graph Pattern Elements by Connection Recipes. All Graph Pattern Elements must be an Instance, a Term, or a Literal.

For the rest of this document, Semantic Engine Language Instances will be referred to as Instances (uppercase). Actual instances of classes will be referred to as instances (lowercase). The basis of the distinction is that an Instance is a component of a graph pattern implemented in SPARQL, and an instance is an actual data entity that is a member of a specific class. Likewise, a Literal represents part of a graph pattern, whereas a literal represents a data value.

**Instances**

Instances are generally expressed as explicit references to some class that is itself defined as an instance of `owl:Class`in the application ontology. As a workaround the `owl:Class` declaration could be placed in a TIS or GS file. If a Connection Recipe explicitly references a class as an Instance, the Semantic Engine will understand to expect or create instances of this class. If creating, URI generation and assignment of type will be handled automatically.

Instances can also be defined without a type. This can come in handy when the type is not known until execution time, perhaps because it is dependent on some element in the incoming dataset. An instance of type `UntypedInstance` can be supplied instead of a class URI.

**Terms**

Terms are typically explicit references to some class that is intended to appear statically in the input or output. There is no requirement that this class is an instance of `owl:Class`. If a Connection Recipe explicitly references a class as a Term, the Semantic Engine will understand to expect or create a static reference to the given class. Unlike an Instance, a Term will appear in the graph as the URI of the referenced class itself.

Terms can also be defined as placeholders and not reference a class explicitly. This can come in handy when the class the Term should reference is not known until execution time, perhaps because it is dependent on some element in the incoming dataset. For this purpose, a ClassResourceList can be supplied instead of a class URI.

**Literals**

Literals are references to data values in a graph pattern. If a Connection Recipe expects a Literal, it should be supplied either a hardcoded data value or a LiteralResourceList. Hardcoded data values should be used when the value is known and constant; LiteralResourceLists should be used when the value is not known until execution time, perhaps because it is dependent on some element in the incoming dataset.

## Connection Recipes

Relationships between Graph Pattern Elements are defined via Connection Recipes. At minimum, Connection Recipes must define the following relationships: `:subject`, `:predicate`, `:object`, and `:cardinality`. Additionally, the type assigned to a Connection Recipe indicates the types of Graph Pattern Elements that can be expected as the `:subject` and the `:object`.

The table below shows the 6 types of Connection Recipes, as well as examples and resulting SPARQL snippets. Note that the Cardinality setting for any Connection Recipe involving a Term is ignored.

|     Connection   Recipe Type          |     Example   Representation in Semantic Engine Language Configuration                                                                                                                                            |     Example   Representation in SPARQL                                                                                                             |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
|     Instance-to-Instance    |     :homoSapiensHasQualityBioSex a   :InstanceToInstanceConnectionRecipe ;      :subject   :homoSapiens ;      :predicate   :hasQuality ;      :object   :biologicalSex ;      :cardinality   :1-1 ;     .          |     ?homoSapiens :hasQuality ?biologicalSex .           ?homoSapiens rdf:type :homoSapiens .           ?biologicalSex rdf:type :biologicalSex .    |
|     Instance-to-Term                  |     :keyHasPartRegistryDenoter      a   :InstanceToTermRecipe ;      :subject :key   ;      :predicate   :hasPart ;      :object   :someRegistryURI ;      :cardinality   :1-1 ;     .                                 |     ?key :hasPart :someRegistryURI .            ?key rdf:type :key .                                                                                  |
|     Term-to-Instance             |     :registryDenoterPartOfKey      a   :TermToInstanceRecipe ;      :subject   :someRegistryURI ;      :predicate   :partOf ;      :object :key   ;      :cardinality   :1-1 ;     .                                   |     :someRegistryURI :partOf ?key .           ?key rdf:type :key .                                                                                    |
|     Instance-to-Literal               |     :cridSymbolHasRepresentation      a   :InstanceToLiteralRecipe ;      :subject   :cridSymbol ;      :predicate   :hasRepresentation ;      :object   :tumor_LiteralValue ;      :cardinality   :1-1 ;     .  :tumor_LiteralValue a :StringLiteralResourceList .   |     ?cridSymbol   :hasRepresentation ?tumor_LiteralValue .           ?cridSymbol   rdf:type :cridSymbol .                                          |
|     Term-to-Literal              |     :datumHasOmopConceptId      a   :TermToLiteralRecipe ;       :subject   :datum;       :predicate   :hasOmopConceptId ;       :object   "omop_1234" ;       :cardinality   :1-1 ;     .                     |     :datum   :hasOmopConceptId "omop_1234" .                                                                                                  |
|     Term-to-Term                 |     :genderIdentityDatumSubclassOfDatum       a   :TermToTermRecipe ;       :subject   :genderIdentityDatum ;       :predicate   rdfs:subClassOf ;       :object   :datum ;       :cardinality   :1-1 ;     .       |     :genderIdentityDatum   rdfs:subClassOf :datum .                                                                                                |

This table is referenced later in this document as the Connection Recipe table.

## Cardinality and URI Construction

The following are valid Semantic Engine Cardinality settings: `:1-1`, `:1-many`, `:many-1`, `:many-singleton`, `:singleton-many`, `:singleton-singleton`, `:many-superSingleton`, `:superSingleton-many`, `:singleton-superSingleton`, `:superSingleton-singleton`. `:superSingleton-superSingleton` is not currently valid but could be added.

A `singleton` is defined as an Instance that a single Update Specification can only create once, but can be created again by other Update Specifications. A `superSingleton` is defined as an Instance that exists only once in the entire graph.

The most important concept to grasp when assigning Cardinality settings is that Connection Recipes do not represent a single graph relationship, they represent a graph pattern that can be replicated multiple times. A single Connection Recipe could be responsible for the reading or writing of millions of relationships to the graph. Cardinality is important to specify because when creating new instances, the Semantic Engine needs some context for how many of each Instance to create. Instances are created using a SPARQL Bind clause. An example is given below.

```
BIND(uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?MONDO_0004992","fcb96fee01d94924abf3e25c07c109c9", str(?TURBO_0010191))))) AS ?MONDO_0004992)
```

This Bind clause has been generated to bind the SPARQL variable `?MONDO_0004992`, which will be used to create new instances of class `obo:MONDO_0004992`. Instead of randomly assigning URIs, the Semantic Engine creates URIs based on the URIs of pre-existing instances of an associated class, and thus enforces cardinality. We will break down the construction of the new URI piece by piece.

`http://www.itmat.upenn.edu/biobank/`: This is the default prefix. It can be set in the Properties file using the key `defaultPrefix`

`SHA256`: SPARQL Hash function to generate a non-human readable URI string based on the supplied values

`CONCAT`: SPARQL function to combine several supplied strings into a single string

`?MONDO_0004992`: String representation of the class that will be assigned as the type of the created instances. Ensures that the generated URIs will not collide with URIs generated for instances of other classes. Question mark is simply a result of implementation, and is irrelevant to the function.

`fcb96fee01d94924abf3e25c07c109c9`: UUID generated at the start of each Semantic Engine instantiation. It is constant between Update Specifications during a single instantiation. Ensures that the generated URI will not collide with any URIs previously in the graph.

`str(?TURBO_0010191)`: Cardinality Enforcer element. This captures the existing URIs associated with a specific non-optional Instance included in the input pattern and uses it to lock in the cardinality of the new Instance. The determination of which Enforcer element to use is made by an algorithm that processes the supplied Cardinality settings as a group and searches for a `:1-1` connection between an Instance that exists in the input pattern and an Instance that exists in the output pattern. In the example above, we have asserted that an Instance referencing class `turbo:TURBO_0010191` exists in the input pattern, is not optional, and has a `:1-1` Cardinality relationship either with `obo:MONDO_0004992` directly, or with another Instance in the output pattern that has a `:1-1` connection with `obo:MONDO_0004992`.

Note that it is possible to configure Cardinality settings in such a way that there are logical inconsistencies. For example, consider the simple pattern below, that introduces a logical inconsistency. If Cardinality inconsistencies exist, they will be flagged by the Semantic Engine, and the instantiation will be cancelled. To fix this inconsistency, the cardinality of `:ClassBtoClassC` should be set as `:1-1`.
```
:ClassAtoClassB a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classB ;
  :cardinality :1-1 ;
.

:ClassBtoClassC a :InstanceToInstanceRecipe ;
  :subject :classB ;
  :predicate :relatesTo ;
  :object :classC ;
  :cardinality :many-1 ;
.

:ClassAtoClassC a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classC ;
  :cardinality :1-1 ;
.
```

## Update Specifications*

Update Specifications define how Connection Recipes will be grouped together to represent graph patterns. An Update Specification selects a specific group of Connection Recipes as inputs and another group as outputs; the inputs typically are defined in the TIS and the outputs in the GS. Each Update Specification specifies how to construct a single SPARQL Update statement. The input graph pattern is represented in the WHERE clause of the generated SPARQL query, and the output appears in the INSERT clause. Intermediary URI construction is completed in the BIND clause.

Below is an example Update Specification. Note that Update Specification instances must be typed as `turbo:TURBO_0010354`, the TURBO ontology class for Updates.

```
:myFirstUpdate
  a turbo:TURBO_0010354 ;
  :precedes :mySecondUpdate ;
  :inputNamedGraph :inputDataGraph ;
  :outputNamedGraph :outputDataGraph ;
  :hasRequiredInput :connection1 ;
  :hasRequiredInput :connection2 ;
  :hasOptionalInput :connection3 ;
  :hasOutput :connection4 ;
  :hasOutput :connection5 ;
  :removes :connection1 ;
.  
:inputDataGraph a :TurboNamedGraph .
:outputDataGraph a :TurboNamedGraph .
```

The `:precedes` predicate may be used to link Update Specifications. If the `run all` command is used to start the Semantic Engine, Update Specifications will be executed in the order that they are linked using this predicate. The last Update Specification to run in the intended order should not be set to `:precedes` another Update Specification. Note that order can be very important, if some Update Specifications use Connection Recipes as input that are the output of other Update Specifications.

The `printQuery` command (shown in main page README) can be extremely helpful for debugging Update Specifications.

**Inputs and Outputs**

The following predicates may be used to connect an Update Specification to a Connection Recipe: `:hasRequiredInput`, `:hasOptionalInput`, `:hasOutput`, `:removes`. The subject of each triple that uses one of these predicates should be a valid Update Specification, and the object should be a valid Connection Recipe. 

The table below shows the relationship between predicates connecting Update Specifications and Connection Recipes, and where the representation of a referenced Connection Recipe will appear in the generated SPARQL statement.

|     Relationship   between Update Specification and Connection Recipe    |     SPARQL Section               |
|--------------------------------------------------------------------------|----------------------------------|
|     :hasRequiredInput                                                    |     WHERE                        |
|     :hasOptionalInput                                                    |     WHERE (IN OPTIONAL BLOCK)    |
|     :hasOutput                                                           |     INSERT                       |
|     :removes                                                             |     DELETE                       |

**Named Graphs**

A valid Update Specification must have relationships with predicates `:inputNamedGraph` and  `:outputNamedGraph`. This specifies which graph the input data will be read from, and where the output patterns should be created. The object of the triple that uses one of these predicates must be defined as a `:TurboNamedGraph`.

Wildcard patterns for input named graphs can be used, if multiple input graphs are necessary. The wildcard can only be applied at the end of the `:TurboNamedGraph` URI, and uses an underscore syntax. For example, the following declaration would use all named graphs that start with `:inputs_` as input:

```
:myFirstUpdate :inputNamedGraph :input_ .
:input_ a :TurboNamedGraph .
```

Named graphs can also be referenced from the properties file. For example, the following declaration instructs the Semantic Engine to create the output patterns in a graph named by the value of a properties entry with key `expandedNamedGraph`:

```
:myFirstUpdate :outputNamedGraph properties:expandedNamedGraph .
properties:expandedNamedGraph a :TurboNamedGraph .
```

## Resource Lists

**Literal Resource Lists**

Literal Resource Lists can be used to instruct SPARQL generated from an Update Specification to expect literal values at a specific location in the graph pattern. The table below shows how Literal Resource List types map to XSD datatypes. Type enforcement is not currently implemented in the Semantic Engine; for example, currently a `DateLiteralResourceList` could be used for data of type `xsd:String`. Strict enforcement of literal types would be a good feature to implement in the future.

Literal Resource Lists can only be the object of a Connection Recipe, not the subject. This is because, in RDF, literal values cannot be the subjects of triples. To see an example of a `LiteralResourceList` as the object of a Connection Recipe, see the Instance-to-Literal example recipe in the Connection Recipe table.

Literal Resource Lists cannot appear in the output of an Update Specification without also appearing in the input, because they have to store a specific set of values from the input. If a hardcoded data value needs to be inserted in the output, that value should be inserted directly as the object of a Connection Recipe instead of using a Literal Resource List.

Unlike Class Resource Lists, no range feature has been implemented for Literal Resource Lists. This might be useful to implement for validation. 

| Literal Type                       | Related XSD Type  |
|------------------------------------|-------------------|
| LiteralResourceList (generic type) |    Any            |
| StringLiteralResourceList          |    xsd:String     |
| IntegerLiteralResourceList         |    xsd:Integer    |
| DoubleLiteralResourceList          |    xsd:Double     |
| BooleanLiteralResourceList         |    xsd:Boolean    |
| DateLiteralResourceList            |    xsd:Date       |

**Class Resource Lists**

Class Resource Lists can be used to instruct SPARQL generated from an Update Specification to expect static references to a class at a specific location in the graph pattern. Currently they can also be used as placeholders for Instances or Literals, but this should not be considered a valid use case going forwards. A range feature defines what classes are allowed for a Class Resource List to represent.  For example, a Class Resource List could be defined with ranges in the following manner:

```
:genderIdentityClassList
  a :ClassResourceList ;
  :range obo:OMRSE_00000133 ;
  :range obo:OMRSE_00000138 ;
  :range obo:OMRSE_00000141 ;
.
```
This declaration ensures that static classes in the input in the location specified by the Class Resource List are within this list of ranges, or the pattern will not match. This feature is implemented using a `VALUES` clause in SPARQL.

Class Resource Lists can appear in the output of an Update Specification without appearing in the input, but only if they declare a range list of size 1. This is essentially equivalent to hardcoding a class as the value of a Term.

## SPARQL Groups*

In SPARQL, graph patterns may be enclosed in bracketed groups to designate alternative functions. The Semantic Engine Language has implementations for `OPTIONAL` and `MINUS` SPARQL groups.

**Optional Groups**

A SPARQL `OPTIONAL` group may be used to designate part of a graph pattern that is not required to exist for the query to match. If the optional pattern is not present, the query will still match against the required pattern.

In the Semantic Engine Language, Optional Groups can be created like this:

```
:myOptionalGroup
  a :TurboGraphOptionalGroup ;
.
```
The group can then be applied to a particular Update Specification:
```
:myFirstUpdate
  a turbo:TURBO_0010354 ;
  :buildsOptionalGroup :myOptionalGroup ;
  :hasRequiredInput :connection1 ;
 ```
The use of the `:hasRequiredInput` predicate here may be confusing. There are two ways to declare a Connection Recipe as optional: connecting it to an Update Specification with predicate `:hasOptionalInput`, or including it in an Optional Group. When using an Optional Group, the `:hasRequiredInput` predicate can be interpreted to mean required within the Optional Group. Use of `:hasOptionalInput` and an Optional Group in conjunction will lead to a Connection Recipe being represented optionally within an Optional Group.
 
 Finally, individual Connection Recipes that should appear in the Group can be annotated:
 ```
 :connection1 a :InstanceToInstanceRecipe ;
     :partOf :myOptionalGroup ;
 ```
Only Connection Recipes that are inputs to an Update Specification are relevant to include in an Optional Group. Outputs will be created or not created based on the input pattern, so there is no need to declare anything in the output as required or optional.

**Minus Groups**

A SPARQL `MINUS` group may be used to designate part of a graph pattern, the absence of which is matched by the query. No patterns that include the sub-pattern included in the `MINUS` clause will be matched; only queries that do not include this sub-pattern will be matched.

In the Semantic Engine Language, Minus Groups can be created like this:

```
:myMinusGroup
  a :TurboGraphMinusGroup ;
.
```
The group can then be applied to a particular Update Specification:
```
:myFirstUpdate
  a turbo:TURBO_0010354 ;
  :buildsMinusGroup :myOptionalGroup ;
  :hasRequiredInput :connection1 ;
 ``` 
 Finally, individual Connection Recipes that should appear in the Group can be annotated:
 ```
 :connection1 a :InstanceToTermRecipe ;
     :partOf :myMinusGroup ;
 ```
Only Connection Recipes that are inputs to an Update Specification are relevant to include in a Minus Group.

There may be some implementation issues if trying to use a graph pattern that spans over multiple named graphs in a MINUS group.

## Dependents*

Sometimes, a Semantic Engine user may wish to specify that an ouput should only be created if a specific input exists in the graph. You can use Dependents to do this.

To use Dependents, annotate a Connection Recipe with either `:subjectRequiredToCreate` or `:objectRequiredToCreate` and an output Instance.

For example:
```
:ClassAtoClassB a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classB ;
  :cardinality :1-1 ;
  :subjectRequiredToCreate :classC ;
.
```
This instruction informs the Semantic Engine to only create instances of `classC` when there are instances of `classA`. Of course, it is assumed that `classA` has been declared as optional, because if it was required, the entire pattern would not match if `classA` were not present.

Dependents are implemented using a SPARQL `IF` statement within the standard `BIND` clause that creates new URIs. An example is below.
```
BIND(IF (BOUND(?birth_datetime_StringLiteralValue), uri(concat("http://www.itmat.upenn.edu/biobank/",SHA256(CONCAT("?EFO_0004950","fcb96fee01d94924abf3e25c07c109c9", str(?TURBO_0010161))))), ?unbound) AS ?EFO_0004950)
```
This example shows the conditional creation of instances of class `efo:EFO_0004950`, where the Literal Resource List `birth_datetime_StringLiteralValue` is bound. Note that the use of the `?unbound` variable causes nothing to be bound to the target variable. This is not because `unbound` is a reserved variable name in SPARQL, but just because nothing has been explicitly bound to that variable name. It can be considered a reserved word in the Semantic Engine Language, in that no Graph Pattern Elements should be referenced called `unbound`.

## Execution Requirements

The Semantic Engine Language predicate `:mustExecuteIf` relating a Connection Recipe and a `:TurboGraphRequirementSpecification` can be utilized to indicate a condition when the Connection Recipe must be the output of at least one Update Specification. This allows for enforcement of "required" properties of a class. As an example, consider the Connection Recipe below:
```
:ClassAtoClassB a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classB ;
  :cardinality :1-1 ;
  :mustExecuteIf :subjectExists ;
.
```
This Recipe must be the output of at least one Update Specification, if the `:subject` of the Recipe is the output of any Update Specification. Thus, we can ensure that in the final graph, any instance of `classA` will be required to have a `:relatesTo` relationship with an instance of `classB`.

The following are valid `:TurboGraphRequirementSpecification`s:
`:subjectExists`
`:objectExists`
`:eitherSubjectOrObjectExists`

Enforcement of the Execution Requirement occurs before any Update Specifications are executed, by the `GraphModelValidator`. If errors are found, the user will be alerted and the Semantic Engine instantiation will be cancelled.

## Contexts

Sometimes, an Update Specification might include patterns with multiple Instances that mention the same class. Consider a graph pattern that describes the case of a `:homoSapiens` (representing a real-life patient) that was prescribed a particular `:medication`, that was itself prescribed by another `:homoSapiens` (representing a real-life doctor). In the Semantic Engine Language (and in the generated SPARQL), an Instance is only denoted by a reference to a class URI, so we need an additional declaration to differentiate the Instance referencing `:homoSapiens` in the context of a patient and the one referencing it in the context of a doctor. We can use instances of `:TurboGraphContext` for this.

```
:homoSapiensPrescribedMedication a :InstanceToInstanceRecipe ;
  :subject :homoSapiens ;
  :predicate :was_prescribed ;
  :object :medication ;
  :cardinality :1-many ;
  :mustExecuteIf :objectExists ;
  :subjectUsesContext :patientContext ;
.
:patientContext a :TurboGraphContext .

:doctorPrescribesMedication a :InstanceToInstanceRecipe ;
    :subject :doctor ;
    :predicate :prescribed ;
    :object :medication ;
    :cardinality :1-many ;
    :mustExecuteIf :objectExists ;
    :subjectUsesContext :doctorContext ;
.
:doctorContext a :TurboGraphContext .
```
As an additional (and perhaps unnecessary...) safeguard, each referenced class must be annotated with a possible context:
```
:homoSapiens a owl:Class ;
    :hasPossibleContext :doctorContext ;
    :hasPossibleContext :patientContext ;
.
```
It's important to note that using Contexts would only be relevant if both Instances referencing the same class are expected or created by the same Update Specification. In this case, if Contexts were not used, `:homoSapiens` would be interpreted as a single Instance, rather than as two Instances. It would appear in the generated SPARQL like this:
```
?homoSapiens a :homoSapiens .
```
With the contexts applied, the two Instances will appear like this:
```
?homoSapiens_doctorContext a :homoSapiens .
?homoSapiens_patientContext a :homoSapiens .
```

## Referenced in Graph*

By default, the input pattern of an Update Specification will be matched against the provided `:inputNamedGraph`. This default can be altered for individual Connection Recipes by specifying that a Connection Recipe is `:referencedInGraph`. See the example below.
```
:ClassAtoClassB a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classB ;
  :cardinality :1-1 ;
  :referencedInGraph :secondaryInputGraph ;
.
```
A new `GRAPH` clause for graph `:secondaryInputGraph` will be created in the `WHERE` clause of the generated `SPARQL` query:
```
GRAPH <https://github.com/PennTURBO/Drivetrain/secondaryInputGraph>
{
    ?classA <https://github.com/PennTURBO/Drivetrain/relatesTo> ?classB .
    ?classA rdf:type <https://github.com/PennTURBO/Drivetrain/classA> .
    ?classB rdf:type <https://github.com/PennTURBO/Drivetrain/classB> .
}
```
Note that uses of `:referencedInGraph` for a Connection Recipe that is included in the output pattern will have no effect.

If the same Instance is referenced by multiple Connection Recipes that appear in different `GRAPH` clauses, the type declaration will go in the clause for the default input named graph if applicable. If not applicable, (in the case that each of the Connection Recipes referencing the same Instance references a graph that is not the default graph) the behavior is ill-defined and should be addressed in the object-based refactor.

## Predicate Suffix*

The Semantic Engine Language has the capacity to implement two SPARQl Property Path operators: * and +. * enables inclusive recursive predicate path searches, and + enables exclusive recursive predicate path searches. See the docs [here](https://www.w3.org/TR/sparql11-property-paths/) for more information.

To implement a Suffix Operator, use the reserved URIs `:star` or `:plus` to annotate a Connection Recipe.

```
:ClassAtoClassB a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classB ;
  :cardinality :1-1 ;
  :mustExecuteIf :subjectExists ;
  :predicateSuffix :star ;
.
```

The SPARQL representation of this Connection Recipe will then appear like this:
```
?classA <https://github.com/PennTURBO/Drivetrain/relatesTo>* ?classB .
```
The asterisk at the end of the predicate indicates that we will still match the pattern if `classB` is any number of `:relatesTo` hops away from `classA`. Without the asterisk, the pattern would only match if it were one hop away.

Note that these operators may cause a significant performance degradation.

## Input Data Validation

Besides validation of the Semantic Engine Language files, another validation step that uses the `:mustExecuteIf` predicate is validation of concise RDF input data. This option can be turned on in the properties file using key `dataValidationMode`.

If set to `dataValidationMode = stop`, the system will cancel any instantiation in which an error in the input data is found.

If set to `dataValidationMode = log`, the system will proceed with the instantiation, and log any errors in the `errorLogFile`.

If set to `dataValidationMode = none`, the system will not perform any validation of input data.

Input Data Validation is executed using the `InputDataValidator`, and takes place before each Update is executed. This means that if set to `stop`, the instantiation may be cancelled after some Updates have already completed. Note that the property `clearGraphsAtStart` in the properties file can be set to `true` to clear the graphs specified in the `expandedNamedGraph` and `processNamedGraph` keys in the properties file, which will effectively reset the repository in the case that an instantiation is terminated in an incomplete state. These graphs can also be cleared manually using the GraphDB web interface.

The `InputDataValidator` checks the execution requirement of each Connection Recipe that is input to the queued Update, and uses that to generate a SPARQL statement that is run against the named graph(s) that contain the concise RDF input data.

For example, consider the following Connection Recipe, and assume that it is referenced as an input by some Update:

```
:ClassAtoClassB a :InstanceToInstanceRecipe ;
  :subject :classA ;
  :predicate :relatesTo ;
  :object :classB ;
  :cardinality :1-1 ;
  :mustExecuteIf :subjectExists ;
.
```

If Input Data Validation were turned on, before running the Update that references this Recipe, a SPARQL statement would run to ensure that there are no instances of `classB` that do not have a `relatesTo` relationship with `classA` in the input dataset.

## Custom Bind Rules*

Custom Bind Rules can be used to assign new URIs in special cases when the template described in the **Cardinality and URI Construction** section is not sufficient. Note that by using this feature, a user will be writing raw SPARQL snippets that will be inserted directly into the generated query without safety checks. Make sure you know what you are doing! Having an understanding of SPARQL Bind Clause syntax would be a good prerequisite to have before diving into this. In the future, this system should probably move towards more structured methods for defining special rules; Custom Bind Rules were intended as a "quick and dirty" approach to get the tasks at hand done, but not a long term solution.

Custom Bind Rules can be used to create URIs for Instances using a special rule, or to create URIs for Terms that use a `ClassResourceList`. Using a Custom Bind Rule is currently the only way that a `ClassResourceList` can be assigned a URI in the Bind clause. To indicate that an Element should be assigned a Custom Bind Rule, the following semantics should be used:

```
:ClassA :usesCustomVariableManipulationRule :customRule1 .
:customRule1 a :TurboGraphVariableManipulationLogic .
:customRule1 :usesSparql {USER PUTS SPARQL TEMPLATE HERE}
```

The SPARQL template can embed the following parameters using a bracket syntax. These will be replaced using a String replace with the appropriate term.

`${dependent}`: Will be replaced with the Element that is declared as a dependent of the element to be created. See the **Dependents** section for how to declare this. If no dependent is declared but the custom rule includes this parameter, an error will be thrown.

`${cardinalityEnforcer}`: Will be replaced with the Element that is discovered as the cardinality enforcer for the element to be created. Cardinalty enforcers cannot be declared and must be discoverable by the `BindClauseBuilder` using the set of rules in place there. If no enforcer is discovered but the custom rule includes this parameter, an error will be thrown.

`${replacement}`: Will be replaced with the Element that is to be assigned the generated URI. In the example above, this would be `:ClassA`.

`${localUUID}`: Will be replaced with the UUID generated for the current instantiation. This can be useful to include to avoid collisions with pre-existing URIs.

`${defaultPrefix}`: Will be replaced with the `defaultPrefix` defined in the properties file `turbo_properties.properties`

Here is example of a SPARQL template for a custom rule that uses all of the parameters:

```BIND(uri(concat("${defaultPrefix}",SHA256(CONCAT("${replacement}",str(${cardinalityEnforcer}),"${localUUID}",str(${dependent}))))) AS ${replacement})```

This Custom Rule indicates that a new URI will be created starting with the `defaultPrefix` and ending with the Hash value from the SHA256 function that concatenates a string representing the Element to be created (`replacement`), the `cardinalityEnforcer` for that element, the `localUUID` for the instantiation, and the Element that the Element to be created is `dependent` on. This varies from the standard Bind template because the `dependent` is included as part of the Hash function, rather than the default behavior of creating an `IF BOUND` declaration within the Bind clause.
