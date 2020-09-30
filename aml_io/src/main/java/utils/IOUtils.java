package utils;

import java.io.IOException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.ecore.EPackage;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import CAEX215.CAEXFileType;
import importer.AMLImporter;
import parser.AMLParser;

public class IOUtils {
	
	public static CAEXFileType doImport (String amlfilename){
		
		AMLParser parser;
		CAEXFileType caex = null;
		try {
			parser = new AMLParser(amlfilename);
			EPackage modelPackage = CAEX215.CAEX215Package.eINSTANCE;
			
			// import the aml file 
			AMLImporter importer = new AMLImporter(modelPackage);		
			caex = (CAEXFileType) importer.doImport(parser.getDoc(), false);
			
		} catch (ParserConfigurationException | SAXException | IOException | DOMException | DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return caex;
	}

}
