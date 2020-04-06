# Drivetrain
The Semantic Expander

## SBT Commands
The following commands can be run via the SBT console

**Run commands**

`run all` Runs all update specifications from selected transformation instruction set in specified order

`run <some_process_URI>` Runs only a specific update specification from the selected transformation instruction set

`run printQuery <some_process_URI>` Prints the generated update query to the console for a specific update specification

`run buildTest` Builds automated tests for all update specifications (even unqueued ones) in the selected transformation instruction set

`run buildTest <some_process_URI>` Builds an automated test for a specific update specification

`run debug [--min]` Generates a synthetic set of input triples for all queued update specifications and runs them in order, leaving the output for examination in the testing repository. If min flag is present, only the minimum required set of triples will be generated for each update specification, otherwise all possible triples will be generated.

`run debug [--min] <some_process_URI>` Generates a synthetic set of input triples for a specific update specification and then runs just that update specification, leaving the output for examination in the testing repository. If min flag is present, only the minimum required set of triples will be generated for each update specification, otherwise all possible triples will be generated.

**Test commands**

`test` Run all tests

`testOnly edu.upenn.turbo.<test_class_name>` Runs the tests inside a specific test class
