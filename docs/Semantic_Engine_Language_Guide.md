# Semantic Engine Language Style Guide

This document covers all features included in the Semantic Engine Language, including how-to-use examples and derived SPARQL clause snippets. The Semantic Engine Language is used to create both Transformation Instruction Sets (TIS) and Graph Specifications (GS). Although it is conventional to use the TIS to define everything related to an incoming dataset and the GS to define everything related to the output model, this is a matter of convention only. All triples from both of these files will be combined in the Model Repository and read from there by the Semantic Engine, so there is some flexibility allowed.

## Semantic Engine Language Graph Elements

In the Semantic Engine Language, relationships are declared between Graph Elements. All Elements must be an Instance, a Term, or a Literal.

**Instances**

- Reference to some owl:Class
- Class Resource List (new non-typed instance element should be made instead)

**Terms**

- Hardcoded term
- Class Resource List

**Literals**

- Hardcoded literal
- Literal Resource List

## Connection Recipes

Relationships between Graph Elements are defined via Connection Recipes. There are 6 types of Connection Recipes, described below.

**Instance To Instance Recipe**

**Instance To Term Recipe**

**Term To Instance Recipe**

**Instance To Literal Recipe**

**Term To Term Recipe**

**Term To Literal Recipe**

## Cardinality

- Ignored for Term recipes (term is implied to be singleton)

**1-1**

**1-many**

**many-1**

**many-singleton**

**singleton-many**

**singleton-singleton**

**many-superSingleton**

**superSingleton-many**

**singleton-superSingleton**

**superSingleton-singleton**

## Update Specifications

**Inputs and Outputs**

- Required Inputs
- Optional Inputs
- Outputs

**Named Graphs**

- Named Graph Wildcard
- Named Graph From Properties

## Resource Lists

**Literal Resource Lists**

- Currently there is no type enforcement for literals

| Literal Type                       | Related XSD Type  |
|------------------------------------|-------------------|
| LiteralResourceList (generic type) |    Any            |
| StringLiteralResourceList          |    xsd:String     |
| IntegerLiteralResourceList         |    xsd:Integer    |
| DoubleLiteralResourceList          |    xsd:Double     |
| BooleanLiteralResourceList         |    xsd:Boolean    |
| DateLiteralResourceList            |    xsd:Date       |

**Class Resource Lists**

- Currently includes non-typed instances, should probably just be terms
- Range can be specified

## SPARQL Groups

**Optional Groups**

**Minus Groups**

## Dependents

## Execution Requirements

## Contexts

## Referenced in Graph

## Predicate Suffix

- "*" and "+" supported

## Custom Bind Rules

- Should be used as a "last resort"
