package AMLQuery;

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelectors;

import CAEX215.CAEXBasicObject;

public class AMLComparator {

	private String result;
	private String reference;
	
	public AMLComparator (String result, String reference) {
		this.result = result;
		this.reference = reference;
	}
	
	public Document loadXMLFromString(String xml) throws Exception {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    return builder.parse(is);
	}
	
	public boolean compareWithXMLUnit () {
//		Diff diff = DiffBuilder.compare(Input.fromString(xmlResult))
//		   .withTest(Input.fromString(xmlReference))
//		   .ignoreWhitespace()
//		   .withNodeMatcher(new DefaultNodeMatcher(
//                 ElementSelectors.conditionalBuilder()
//                 		.whenElementIsNamed("InternalElement")
//                 		.thenUse(ElementSelectors.byXPath("./Attribute", ElementSelectors.byNameAndText))
//                 		.elseUse(ElementSelectors.byNameAndText).build())).build();

		Diff diff = DiffBuilder.compare(Input.fromString(result))
				   .withTest(Input.fromString(reference))
				   .ignoreWhitespace()
				   .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)).build();

		if(!diff.hasDifferences()) {
			return true;
		}
		else {
			Iterator<Difference> diffIter = diff.getDifferences().iterator();
			while(diffIter.hasNext()){
				Difference difference = diffIter.next();
				System.out.println(difference.getComparison());
			}	
			return false;
		}
	}
	
	/**
	 * compares the result string with the reference string line by line 
	 * assuming that the XML element sequence is the same
	 * @return whether they are different
	 */
	public boolean compare (boolean verbose) {
		String[] linesOfResult = result.split("\n");
		String[] linesOfReference = reference.split("\n");
		
		if(linesOfResult.length != linesOfReference.length) {
			System.out.println("[ERROR] result has [" + linesOfResult.length + "] lines, reference has [" + linesOfReference.length + "] lines!");
			// they are of different size
			return true;
		}
		
		boolean different = false;
		for (int i = 0; i < linesOfReference.length; i++) {
			String lineOfResult = linesOfResult[i];
			String lineOfReference = linesOfReference[i];
			lineOfResult.trim();
			lineOfReference.trim();

			// if this line is an IE we ignore the ID compare 
			if(lineOfReference.contains("<InternalElement") && lineOfResult.contains("<InternalElement") && !lineOfReference.contains("ID")) {
				if(verbose) {
					System.out.println("\nskipping line: ");
					System.out.println("\t - " + lineOfResult);
					System.out.println("\t - " + lineOfReference);	
				}				
				continue;
			}
			if(!lineOfResult.equals(lineOfReference)) {		
				if(verbose) {
					System.out.println("\n[EEROR] line [" + i + "] is different!");
					System.out.println("\t - result: " + lineOfResult);
					System.out.println("\t - reference: " + lineOfReference);	
				}				
				different = true;
			}
			else {			
//				System.out.println("same line: ");
//				System.out.println("\t - " + lineOfResult);
//				System.out.println("\t - " + lineOfReference);
			}
		}
		
		return different;
	}
}
