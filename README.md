# Semantic Engine
The Semantic Engine ("Drivetrain") transforms concise RDF data into a source-independent, semantically rich RDF model. 

## Installation

### Prerequisites

You must have SBT and Java installed on your system. Unless running from a precompiled .jar file, interaction with the software will take place through the SBT console.

### Setup

Clone the repository locally, and copy/remove the .template suffix from the following 4 files:
- `turbo_properties.properties.template`
- `drivetrain/build.sbt.template`
- `drivetrain/project/plugins.sbt.template`
- `drivetrain/project/build.properties.template`

Configure `turbo_properties.properties` to use the desired Transformation Instruction Set and Graph Specification files. Pre-existing options exist in the `ontologies` folder. For instructions on designing your own Semantic Engine configurations, see the documentation [here](Semantic%20Engine%20Configuration%20Tutorial.docx).

## SBT Commands
The following commands can be run via the SBT console

**Run commands**

`run all` Runs all update specifications from selected transformation instruction set in specified order

`run <some_process_URI>` Runs only a specific update specification from the selected transformation instruction set

`run printQuery <some_process_URI>` Prints the generated update query to the console for a specific update specification. No changes will be made to the graph.

`run buildTest` Builds automated snapshot tests for all update specifications (even unqueued ones) in the selected transformation instruction set.

`run buildTest <some_process_URI>` Builds an automated snapshot test for a specific update specification

`run debug [--min]` Generates a synthetic set of input triples for all queued update specifications and runs them in order, leaving the output for examination in the testing repository. If min flag is present, only the minimum required set of triples will be generated for each update specification, otherwise all possible triples will be generated.  Note that if updates reference inputs that were the output of a previous update, instances of these input classes will still be created by the synthetic triple generation service, even if other instances of those classes already exist in the graph.

`run debug [--min] <some_process_URI>` Generates a synthetic set of input triples for a specific update specification and then runs just that update specification, leaving the output for examination in the testing repository. If min flag is present, only the minimum required set of triples will be generated for each update specification, otherwise all possible triples will be generated.

Click [here](snapshotTests.md) more detailed information on the debug mode and snapshot test generation.

**Test commands**

`test` Runs all tests

`testOnly edu.upenn.turbo.<test_class_name>` Runs the tests inside a specific test class

`testOnly edu.upenn.turbo.SnapshotTest` Runs all Snapshot tests

`testOnly edu.upenn.turbo.SnapshotTest -- -DfindTest={searchString}` Runs Snapshot tests whose name includes the searchString
