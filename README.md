# Introduction

This repository contains four Java projects which are part of my Ph.D. thesis:

1. `aml_models`: the Java representation of the CAEX XML schema using [Eclipse Modeling Framework (EMF)](http://www.eclipse.org/modeling/emf/)
2. `aml_io`: an AML importer using the CAEX modeling in aml_models
3. `aml_query`: implementation of AQT (AML Query Template) and AQP (AML Query Processor)
4. `aml_owl`: implementation of the bidirectional translation between AML and OWL based on the AML Concept Model (ACM)

## Installation

You first need to download the latest version of the Eclipse Modeling Tools from [eclipse.org](http://www.eclipse.org/downloads/eclipse-packages/) to your Eclipse IDE.

Then use Maven to import the projects and compile: `aml_models` -> `aml_io` -> `aml_query` -> `aml_owl`

## Running AQT Demos

The experiments are implemented in the test folder of each Java project.

For testing AQT and AQP, there are several resource files stored at `aml_query/src/test/resource/`: 

* `RobotCell.aml`: original AML data provided by the [AutomationML Consortium](https://www.automationml.org/o.red.c/home.html) and can be downloaded at https://www.automationml.org/o.red.c/dateien.html?cat=1.

* `data_query.aml`: excerpt of the original AML data that is used for the experiments in the data access part.

* `data_exchange.aml`: excerpt of the original AML data that is used for the experiments in the data exchange part. 

* `query.aml`: AQT models for the data access experiments. The result of the data access tests will be stored as `output.aml` if the user needs them to be stored externally. Otherwise, the results are printed in the console.

* `data_TF5-object.aml` and `exchange.aml`: AQT models for the data exchange experiements. The result of the data exchange tests will be stored in the folder `benchmark`, in which the baseline is stored.

Use the Java class `aml_query/src/test/java/AMLQueryDemo.java` and `aml_query/src/test/java/AMLExchangeDemo.java` to load and execute your query models.

Since AQT models are native AML models, you can use any standard AML editor to generate one. Use the configuration parameters mentioned in the thesis to configure your query models.

# Running ACM Demos

To simply generate OWL ontologies from AML data, use the Java class `aml_owl/src/test/java/TestAML2OWLOntology.java`.

To try the forward and backward translations between AML and OWL, use the Java classes `aml_owl/src/test/java/ETFAForwardTranslationDemo.java` and `aml_owl/src/test/java/ETFAForwardTranslationDemo.java`.


