# Semantic Engine Language Feature Guide

This document covers all features included in the Semantic Engine Language, including how-to-use examples and derived SPARQL clause snippets. The Semantic Engine Language is used to create both Transformation Instruction Sets (TIS) and Graph Specifications (GS). Although it is conventional to use the TIS to define everything related to an incoming dataset and the GS to define everything related to the output model, this is a matter of convention only. All triples from both of these files will be combined in the Model Repository and read from there by the Semantic Engine, so there is some flexibility allowed. To encourage best practices, the features that are intended to be applied only in the TIS are denoted with an asterisk in this document.

"*" = Transformation Instruction Set-specific feature

## Namespace

For the remainder of this document, the prefix `:` will be used to denote `https://github.com/PennTURBO/Drivetrain/` which is the namespace for the Semantic Engine Language. Prefixes can be defined in the file `prefixes.txt`.

## Semantic Engine Language Graph Pattern Elements

In the Semantic Engine Language, relationships are declared between Graph Pattern Elements by Connection Recipes. All Graph Pattern Elements must be an Instance, a Term, or a Literal.

For the rest of this document, Semantic Engine Language Instances will be referred to as Instances (uppercase). Actual instances of classes will be referred to as instances (lowercase). The basis of the distinction is that an Instance is a component of a graph pattern implemented in SPARQL, and an instance is an actual data entity that is a member of a specific class. Likewise, a Literal represents part of a graph pattern, whereas a literal represents a data value.

**Instances**

Instances are generally expressed as explicit references to some class that is itself defined as an instance of `owl:Class`in the application ontology. As a workaround the `owl:Class` declaration could be placed in a TIS or GS file. If a Connection Recipe explicitly references a class as an Instance, the Semantic Engine will understand to expect or create instances of this class. If creating, URI generation and assignment of type will be handled automatically.

Instances can also be defined without a type. This can come in handy when the type is not known until execution time, perhaps because it is dependent on some element in the incoming dataset. Currently, a ClassResourceList can be supplied instead of a class URI. However, this is a bit confusing because these are also used as Term placeholders. In the future, developers on this project could create a new Semantic Engine Language feature called `UntypedInstance`.

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

Note that Cardinality enforcement may have some bugs in implementation. I will try to document and correct over the next few weeks.

The following are valid Semantic Engine Cardinality settings: `:1-1`, `:1-many`, `:many-1`, `:many-singleton`, `:singleton-many`, `:singleton-singleton`, `:many-superSingleton`, `:superSingleton-many`, `:singleton-superSingleton`, `:superSingleton-singleton`. `:superSingleton-superSingleton` is not currently valid but should be added.

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

In the Semantic Engine Language, optional groups can be created like this:

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

## Dependents*

## Execution Requirements

## Contexts

## Referenced in Graph*

## Predicate Suffix*

- "*" and "+" supported

## Custom Bind Rules*

- Should be used as a "last resort"
