package AMLQuery;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.basex.query.QueryException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import CAEX215.CAEX215Factory;
import CAEX215.CAEXFileType;
import CAEX215.InstanceHierarchyType;
import CAEX215.InternalElementType;
import concept.model.AMLQueryConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import concept.util.GenericAMLConceptModelUtils;
import exporter.AMLExporter;
import importer.AMLImporter;
import parser.AMLParser;

public class TestAMLQueryParser {
	
	protected static AMLImporter importer;
	
	protected static CAEXFileType queryAML;
	
	public TestAMLQueryParser(String queryfile) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, 
		InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchFieldException, ParserConfigurationException, 
		SAXException, IOException, DOMException, DatatypeConfigurationException{
		importer = new AMLImporter("CAEX215");
		
		AMLParser queryParser = new AMLParser(queryfile);
		Document queryCaex = queryParser.getDoc();		
		queryAML = (CAEXFileType) importer.doImport(queryCaex, false);
	}
	
	public List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> readQuery (String ihName) throws InstantiationException, IllegalAccessException{
		
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queries = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		
		for(InstanceHierarchyType ih : queryAML.getInstanceHierarchy()) {
			if(ih.getName().equals(ihName)) {
				for(InternalElementType obj : ih.getInternalElement()) {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> root = GenericAMLConceptModelUtils.parse(obj, AMLQueryConfig.class);
					queries.add(root);				
				}
			}
		}
				
		return queries;
	}
	
	public List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> readQuery2 () throws InstantiationException, IllegalAccessException{
		
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queries = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		
		for(InstanceHierarchyType ih : queryAML.getInstanceHierarchy()) {
			if(ih.getName().startsWith("q")) {
				InternalElementType queryObj = ih.getInternalElement().get(0);
				GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> root = GenericAMLConceptModelUtils.parse(queryObj, AMLQueryConfig.class);
				queries.add(root);				
			}
		}
				
		return queries;
	}
	
	public List<InternalElementType> readIE (String sourceFile) throws ParserConfigurationException, SAXException, IOException, DOMException, DatatypeConfigurationException {
		
		AMLParser parser = new AMLParser(sourceFile);
		Document caex = parser.getDoc();	
		CAEXFileType aml = (CAEXFileType) importer.doImport(caex, false);			
		
		List<InternalElementType> roots = new ArrayList<InternalElementType>();
		for(InternalElementType obj : aml.getInstanceHierarchy().get(0).getInternalElement()) {						
			roots.add(obj);
		}
		
		return roots;
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

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchFieldException, ParserConfigurationException, SAXException, IOException, DOMException, DatatypeConfigurationException, TransformerFactoryConfigurationError, TransformerException, QueryException {
		
		String queryFile = "src/test/resources/query.aml";
		
		TestAMLQueryParser tester = new TestAMLQueryParser(queryFile);
		
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queries = tester.readQuery("TII2019Examples");
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> query : queries) {
			System.out.println(query.toString());
		}
		
//		String output = "src/test/resources/queryTestWrite.aml";
//		List<InternalElementType> ies = tester.readIE(queryFile);
//		tester.write(output, ies);
//		
//		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queries2 = tester.readQuery(output);
//		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> query : queries) {
//			System.out.println(query.toString());
//		}
	}
}
