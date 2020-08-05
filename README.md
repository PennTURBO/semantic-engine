# Semantic Engine
The Semantic Engine transforms concise RDF data into a source-independent, semantically rich RDF model. It is designed to allow ontologists and programmers to work collaboratively on the creation and implementation of a target model that standardizes data from disparate sources. See the [QuickStart Guide](docs/quickStart.md) to get up and running. For more extensive description of the various components and a toy use case tutorial, see [this Word document](docs/Semantic%20Engine%20Configuration%20Tutorial.docx).

The system uses a syntax called the Semantic Engine Language to define RDF graph transformations. There are 2 types of configuration files that use this syntax: a Graph Specification file (.gs) defines the RDF target model, and a Transformation Instruction Set (.tis) maps another RDF schema to the target model. The intention is that this schema will have similar semantics to the original data source, whereas the target model will include rich semantics defined by an application ontology. Pre-existing public .gs and .tis files are available in [drivetrain/config](drivetrain/config). For documentation about designing your own Semantic Engine Language configurations, see the [Semantic Engine Language Feature Guide](docs/Semantic_Engine_Language_Guide.md).

![workers](docs/images/workers.JPG)

## Installation

### Prerequisites

You must have SBT and Java installed on your system. Unless running from a precompiled .jar file, interaction with the software will take place through the SBT console.

### Setup

Clone the repository locally, and copy/remove the .template suffix from the following 4 files:
- `turbo_properties.properties.template`
- `drivetrain/build.sbt.template`
- `drivetrain/project/plugins.sbt.template`
- `drivetrain/project/build.properties.template`

Configure `turbo_properties.properties` to use the desired Semantic Engine Language files: Transformation Instruction Set and Graph Specification. Pre-existing options exist in the `config` folder.

## SBT Commands
The following commands can be run via the SBT console. SBT should be started from the directory `drivetrain/`.

**Run commands**

`run all` Runs all update specifications from selected transformation instruction set in specified order

`run <some_process_URI>` Runs only a specific update specification from the selected transformation instruction set

`run printQuery <some_process_URI>` Prints the generated update query to the console for a specific update specification. No changes will be made to the graph.

`run buildTest` Builds automated snapshot tests for all update specifications (even unqueued ones) in the selected transformation instruction set.

`run buildTest <some_process_URI>` Builds an automated snapshot test for a specific update specification

`run debug [--min]` Generates a synthetic set of input triples for all queued update specifications and runs them in order, leaving the output for examination in the testing repository. If min flag is present, only the minimum required set of triples will be generated for each update specification, otherwise all possible triples will be generated.  Note that if updates reference inputs that were the output of a previous update, instances of these input classes will still be created by the synthetic triple generation service, even if other instances of those classes already exist in the graph.

`run debug [--min] <some_process_URI>` Generates a synthetic set of input triples for a specific update specification and then runs just that update specification, leaving the output for examination in the testing repository. If min flag is present, only the minimum required set of triples will be generated for each update specification, otherwise all possible triples will be generated.

Click [here](docs/snapshotTestDocs.md) for more detailed information on snapshot test generation.

**Test commands**

`test` Runs all tests

`testOnly edu.upenn.turbo.<test_class_name>` Runs the tests inside a specific test class

`testOnly edu.upenn.turbo.SnapshotTest` Runs all Snapshot tests

`testOnly edu.upenn.turbo.SnapshotTest -- -DfindTest={searchString}` Runs Snapshot tests whose name includes the searchString
