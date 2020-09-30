package AMLQuery;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.basex.query.value.item.Item;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import CAEX215.CAEX215Factory;
import CAEX215.CAEXFileType;
import CAEX215.CAEXObject;
import CAEX215.InstanceHierarchyType;
import concept.model.AMLQueryConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import concept.util.AMLLinkCollector;
import exporter.AMLExporter;
import generator.TransitiveClosure;
import generator.XQueryGenerator;
import parser.AMLParser;
import serializer.BaseX2AMLConverter;
import xquery.XQueryVariable;
import xquery.XQueryReturn;

public class AMLQueryDemo extends TestAMLQueryParser{
	
	private static final String rootPath = "CAEXFile/InstanceHierarchy";
	private static final String caexLibPath = "src/main/resources/caex.xqy";
	private AMLQueryDemoConfig config;

	public AMLQueryDemo(AMLQueryDemoConfig config) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, ClassNotFoundException, InstantiationException,
			NoSuchFieldException, DOMException, ParserConfigurationException, SAXException, IOException, DatatypeConfigurationException {
		super(config.getQueryfile());
		
		AMLParser dataParser = new AMLParser(config.getDatafile());
		Document dataCaex = dataParser.getDoc();		
		CAEXFileType dataAML = (CAEXFileType) importer.doImport(dataCaex, false);
		
		// use an AMLLinkCollector to collect all InternalLink information across the AML document
		AMLLinkCollector collector = new AMLLinkCollector(queryAML, dataAML);
		collector.init();
		
		this.config = config;
	}
	
	public String toXQuery (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> query, boolean semantically, boolean withLink, boolean simple) {
		
		XQueryGenerator queryGen = new XQueryGenerator(query, config.getDatafile(), rootPath, caexLibPath);
						
		String xquery = queryGen.translateToXQuery(semantically, withLink, simple);
		System.out.println(xquery);
		
		XQueryVariable.resetIdx();
		XQueryReturn.resetIdx();
		TransitiveClosure.resetIdx();
		
		return xquery;
	} 

	
	public String toXQuery(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> query, String queryName) {
		System.out.println("\n-------------------" + queryName + "--------------------");
		return toXQuery(query, config.isSemantically(), config.isWithLink(), config.isSimpleOutput());
	}
	
	public Value executeXQuery (String xquery) throws QueryException {
		Context context = new Context();
		
		QueryProcessor processor = new QueryProcessor(xquery, context);
		Value result = processor.value();						
		
		// Close the database context
		context.close();
		processor.close();
		
		return result;
	}

	public static void main(String[] args) throws QueryException, DOMException, DatatypeConfigurationException, ParserConfigurationException, SAXException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchFieldException, TransformerException{
		
		// the aml file contains all example queries
		String queryfile = "src/test/resources/query.aml";
		
		// the aml file contains the running example in paper
		String data_excerpt = "src/test/resources/data_query.aml";
		
		// the aml file contains the original robot cell
		String data_original = "src/test/resources/RobotCell.aml";
		
		// the output aml file
		String resultfile = "src/test/resources/output.aml";
		
		AMLExporter exporter = new AMLExporter();
		CAEXFileType amlOutput  = CAEX215Factory.eINSTANCE.createCAEXFileType();
		
		
		// this is the config of the query framework
		// - query file
		// - data file
		// - whether semantically, default: false
		// - whether with link constraints, default: false
		// - whether use simple outputs for better reading (basically to compare with the ground truth in the paper), default: false
		AMLQueryDemoConfig demoConfig = new AMLQueryDemoConfig();
		
		// set query file
		demoConfig.setQueryfile(queryfile);
		// set data file
		demoConfig.setDatafile(data_excerpt);
		// with link
		demoConfig.setWithLink(false);
		// semantically
		demoConfig.setSemantically(false);
		// simple output
		demoConfig.setSimpleOutput(true);

		AMLQueryDemo demo = new AMLQueryDemo(demoConfig);

		// In the query.aml file, we have four IHs that contain various query examples
		// tii: example queries in the paper
		// link: example queries with internal link constraints
		// further: further example queries
		// complex: complex query examples
//		String tii = "TII2019Examples";
//		String link = "LinkExamples";
//		String further = "FurtherExamples";
//		String complex = "ComplexExamples";
//		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queries = demo.readQuery(tii);
		
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queries = demo.readQuery2();
		long startTime, stopTime, elapsedTime;		
		int id = 1;
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> query : queries) {		
			
			String queryName = "q" + Integer.toString(id++);
//			String xqueryCommand = demo.toXQuery(query);
			String xqueryCommand = demo.toXQuery(query, queryName);
			
			startTime = System.currentTimeMillis();
			Value result = demo.executeXQuery(xqueryCommand);
			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println("time: " + elapsedTime);
			
			System.out.println("\nNr.RESULTS:" + result.size());	
			for(Item r : result) {
				System.out.println(r.serialize());
			}
			
			// Simple results are only some strings, can not be stored in AML files
			if(!demoConfig.isSimpleOutput()) {
				BaseX2AMLConverter serializer = new BaseX2AMLConverter();				
				InstanceHierarchyType ih = CAEX215Factory.eINSTANCE.createInstanceHierarchyType();
				ih.setName("result_" + queryName);
				
				if(serializer.toCaex(result, ih))
					amlOutput.getInstanceHierarchy().add(ih);
			}
			
			System.out.println("-------------------------------------------\n");									
		}
		
		// Simple results are only some strings, can not be stored in AML files
		if(!demoConfig.isSimpleOutput())
			exporter.write(amlOutput , resultfile);
   }
	
}
	

