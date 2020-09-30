# aml_io

Reader and writer of AML data

## exporter, importer, parser

These packages contain code for reading and writing AML data, based on the Eclipse EMF models in aml_models
* `edu.kit.aml.importer.AbstractXMLImporter` contains the common components (almost all of the actual import logic) used by both importers.
* `edu.kit.aml.importer.GenericXMLImporter` is an importer that generates the Java instance hierarchy using ["dynamic EMF"](http://www.heod.ru/0131425420_ch13lev1sec6.shtml) from an Ecore model (could be of anything, not just AML) given in the constructor. Its `doImport` method takes the name of an XML file conforming to the model.
* `edu.kit.aml.importer.AMLImporter` uses the generated Java classes to import the contents of an AML file and create Java instances. Its constructor takes the name of the Java package corresponding to the Ecore model (must be on the classpath). Its `doImport` method takes the name of an AML file.

## tree

This package contains the implementation of parsers for `AML Concept Model`
