package AMLQuery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import CAEX215.CAEX215Factory;
import CAEX215.CAEXFileType;
import CAEX215.InstanceHierarchyType;
import CAEX215.InternalElementType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import concept.util.GenericAMLConceptModelUtils;
import exporter.AMLExporter;
import generator.XQueryExchangeGenerator;
import importer.AMLImporter;
import parser.AMLParser;
import xquery.XQueryVariable;

public class AMLExchangeDemo {
	
	protected static AMLImporter importer;
	
	protected static CAEXFileType queryAML;
	
	public AMLExchangeDemo(String queryfile) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, 
		InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchFieldException, ParserConfigurationException, 
		SAXException, IOException, DOMException, DatatypeConfigurationException{
		importer = new AMLImporter("CAEX215");
		
		AMLParser queryParser = new AMLParser(queryfile);
		Document queryCaex = queryParser.getDoc();		
		queryAML = (CAEXFileType) importer.doImport(queryCaex, false);
	}

	
	public GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> readSource (String caseName) throws InstantiationException, IllegalAccessException{		
		
		for(InstanceHierarchyType ih : queryAML.getInstanceHierarchy()) {
			
			// find the test case
			if(ih.getName().contains(caseName)) {
				
				// the source model
				if(ih.getName().contains("-Source")) {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source = GenericAMLConceptModelUtils.parse(ih, AMLQueryConfig.class);
					return source;
				}									
			}
		}
				
		return null;
	}
	
	public GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> readTarget (String caseName) throws InstantiationException, IllegalAccessException{	
		
		for(InstanceHierarchyType ih : queryAML.getInstanceHierarchy()) {
			
			// find the test case
			if(ih.getName().contains(caseName)) {
				
				// the target model
				if(ih.getName().contains("-Target")) {
					GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target = GenericAMLConceptModelUtils.parseTarget(ih);					
					return target;
				}									
			}
		}
				
		return null;
	}
	
	public void write(String output, List<InternalElementType> roots) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		CAEXFileType amlTestWrite = CAEX215Factory.eINSTANCE.createCAEXFileType();
		InstanceHierarchyType test = CAEX215Factory.eINSTANCE.createInstanceHierarchyType();
		test.setName("testWrite");
		for(InternalElementType ie : roots) {
			test.getInternalElement().add(ie);
		}
		amlTestWrite.getInstanceHierarchy().add(test);
		AMLExporter exporter = new AMLExporter(amlTestWrite);
		exporter.write("src/test/resources/queryTestWrite.aml");
	}
	
	public boolean isTargetModelValid (String caseName, GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target) {
		
		for(InstanceHierarchyType ih : queryAML.getInstanceHierarchy()) {					
			// find the test case
			if(ih.getName().contains(caseName)) {				
				// the source model
				if(ih.getName().contains("-Source")) {
					List<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> targetNodes = target.getDescendantOrSelf(); 
					for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode : targetNodes) {
						AMLTargetConfig targetConfig = (AMLTargetConfig) targetNode.data.getConfig();
						if(!targetConfig.isValid(ih)) {
							return false;
						}
					}
					return true;
				}									
			}
		}
		return false;
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchFieldException, ParserConfigurationException, SAXException, IOException, DOMException, DatatypeConfigurationException, TransformerFactoryConfigurationError, TransformerException, QueryException {
		
		String queryFile = "src/test/resources/exchange.aml";				
		AMLExchangeDemo demo = new AMLExchangeDemo(queryFile);	
		
		List<String> cases = new ArrayList<String>();
		cases.add("TF1-Equality");
		cases.add("TF1-Subclass1");
		cases.add("TF1-Subclass2");
		cases.add("TF1-Superclass1");
		cases.add("TF1-Superclass2");
		cases.add("TF1-Superclass3");
		cases.add("TF2-Renaming");
		cases.add("TF4-Projection");
		cases.add("TF4-NestedProjection1");
		cases.add("TF4-NestedProjection2");
		cases.add("TF4-NewRole");
		cases.add("TF5-Attribute");
		cases.add("TF5-Object");
		cases.add("TF6-Aggregation1");
		cases.add("TF6-Aggregation2");
		cases.add("TF8-Flattening1");
		cases.add("TF8-Flattening2");
		cases.add("TF4-NestedProjection3");
		cases.add("TF10-OffsetAttribute");
		cases.add("TF10-OffsetObject");
		cases.add("TF10-InverseProjection");
		
		boolean succeed = true;
		for(String testcase : cases) {
			System.out.println("--------------test case: " + testcase + "--------------");
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source = demo.readSource(testcase);
//			System.out.println("source: \n" + source.toString() + "\n");
			
			GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target = demo.readTarget(testcase);			
//			System.out.println("target: \n" + target.toString());	
			if(!demo.isTargetModelValid(testcase, target)) {
				System.err.println("INVALID: " + testcase);
			}
			
//			System.out.println("\nBUILDING XQUERY CODE\n");
			XQueryExchangeGenerator generator = new XQueryExchangeGenerator(source, target);
			generator.init(false);
			
//			System.out.println("\nGENERATING QUERY PROGRAM:\n");
			String caexLibPath = "src/main/resources/caex.xqy";
			String dataFile = "";
			if(testcase.equals("TF5-Object"))
				dataFile = "src/test/resources/data_TF5-object.aml";
			else
				dataFile = "src/test/resources/data_exchange.aml";
			
			String folder = "src/test/resources/benchmark/";
			String program = generator.getProgram(dataFile, caexLibPath);			
			
			XQueryVariable.resetIdx();
			BufferedWriter writer = new BufferedWriter(new FileWriter(folder+"result/"+testcase));
			writer.write(program);
			writer.close();
//			System.out.println(program);
			
			// Run the query
//			System.out.println("\nEXECUTING QUERY PROGRAM:\n");
			Context context = new Context();			
			QueryProcessor processor = new QueryProcessor(program, context);
			Value result = processor.value();
			String xmlResult = new String(result.itemAt(0).serialize().buffer());
			// Need this to clear symbols after result
			xmlResult = xmlResult.substring(0, xmlResult.lastIndexOf(">")+1);
//			System.out.println(xmlResult);
			// Close the database context
			context.close();
			processor.close();
			
			// Write result to file			
			writer = new BufferedWriter(new FileWriter(folder+"result/"+testcase+".xml"));
			writer.write(xmlResult);
			writer.close();
			
			// compare results
//			System.out.println("\nTESTING:\n");			
			String referenceName = folder + "reference/" + testcase + ".xml";			
			File referenceFile = new File(referenceName);
			if(referenceFile.exists()) {
		        String xmlReference = new String(Files.readAllBytes(Paths.get(referenceName)));
//				System.out.println("comparing result with reference: ");
				AMLComparator comparator = new AMLComparator(xmlResult, xmlReference);
				boolean different = comparator.compare(false);
				
				// if they are not different, then succeed
				if(!different) {
					System.out.println("succeed!\n");
				}
				else {
					succeed = false;
					System.out.println("test case [" + testcase + "] failed!\n");
				}
			}
		}
		
		if(succeed)
			System.out.println("All test cases succeed!");
	}
}
