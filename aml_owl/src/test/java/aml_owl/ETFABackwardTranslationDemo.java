package aml_owl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import CAEX215.CAEXFileType;
import CAEX215.CAEXObject;
import CAEX215.ExternalInterfaceType;
import CAEX215.InternalElementType;
import concept.model.GenericAMLConceptModel;
import concept.model.AMLConceptConfig;
import concept.tree.GenericTreeNode;
import concept.util.GenericAMLConceptModelUtils;
import constants.AMLClassIRIs;
import importer.AMLImporter;
import parser.AMLParser;
import translation.expression.AML2OWLConverter;
import translation.expression.AMLConcept;
import translation.expression.AMLConceptTree;
import translation.expression.TranslationUtils;

public class ETFABackwardTranslationDemo{
	
	private static AMLImporter importer;
	private AML2OWLConverter converter = new AML2OWLConverter();
	private OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

	public ETFABackwardTranslationDemo() throws NoSuchMethodException, SecurityException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, MalformedURLException, ClassNotFoundException,
	InstantiationException, NoSuchFieldException {
		importer = new AMLImporter("CAEX215");
		// TODO Auto-generated constructor stub
	}
	
	public List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> readModels (String conceptModelFile) throws NoSuchMethodException, SecurityException, IllegalAccessException, 
	IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchFieldException,
	ParserConfigurationException, SAXException, IOException, DOMException, DatatypeConfigurationException {

		AMLParser parser = new AMLParser(conceptModelFile);
		Document caex = parser.getDoc();		
		CAEXFileType aml = (CAEXFileType) importer.doImport(caex, false);	

		List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> trees = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>>();
		GenericAMLConceptModelUtils interpreter = new GenericAMLConceptModelUtils();
		for(InternalElementType obj : aml.getInstanceHierarchy().get(0).getInternalElement()) {

			if(obj.getName().contains("Q")) {
				GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> root = interpreter.parse(obj, AMLConceptConfig.class);
				trees.add(root);
			}					
		}

		return trees;
	}
	
	public void run(GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> acm) {
		// ======================= STEP 1: AML -> ACM ======================= //
		System.out.println("\n============================================================" + ((CAEXObject) acm.getRoot().data.getObj()).getName() + "============================================================");				
		System.out.println("\n1. First, we show the result (OWL Class) of the backward translation from AML to OWL");				
		System.out.println("\n - 1. The AML concept model read from the file:");

		System.out.println(acm.toStringWithIndent(3));

		Set<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> primaries = GenericAMLConceptModelUtils.getPrimaryObjs(acm);
		if(primaries.size() != 1) {
			System.out.println("\ncannot transform this AMLQuery model to DL: need exactly one primary (returned) object!");
			return;
		}

		GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> primary = primaries.iterator().next();
		IRI caexType = AMLClassIRIs.INTERNAL_ELEMENT;
		if(primary.data.getObj() instanceof ExternalInterfaceType)
			caexType = AMLClassIRIs.EXTERNAL_INTERFACE;

		// ======================= STEP 2: ACM -> OWL ======================= //
		System.out.println("\n - 2. The generated OWL Class Expression\n");

		OWLClassExpression ce = converter.toOWLClassExpression(acm.getRoot());

		System.out.println("   " + renderer.render(ce));				
		
		// ======================= STEP 3: OWL -> ACM ======================= //
		System.out.println("\n2. Then, we show the reproduced AML concept models using forward translation");
		try {					
			List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> acmsReproduced = TranslationUtils.toAMLConceptModel(ce, caexType, true);
			// since we read the ACM from file, there is no union in the concept, so we simply take the only element in the list
			GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> acmReproduced = acmsReproduced.get(0);

			System.out.println("\n It shall be clear that the following AML concept models are equivalent:\n");
			System.out.println(" - the original AML concept model (M) read from the file: \n\n" + acm.toStringWithIndent(3) + "\n");
			System.out.println(" - the reproduced AML concept model as forward_translation(backward_translation(M)):\n\n" + acmReproduced.toStringWithIndent(3) + "\n");					 							

		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
				| IllegalArgumentException | InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}


	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, 
	IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException, 
	NoSuchFieldException, ParserConfigurationException, SAXException, IOException, DOMException, DatatypeConfigurationException {

		// TODO Auto-generated method stub
		String acmfile = "src/test/resources/concepts.aml";
		
		ETFABackwardTranslationDemo tester = new ETFABackwardTranslationDemo();
		TranslationUtils.renderer = tester.renderer;
		AMLConcept.setRender(tester.renderer);
		List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> acms = tester.readModels(acmfile);

		for(GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> acm : acms) {
			if(((CAEXObject) acm.getRoot().data.getObj()).getName().contains("Q")) {
				tester.run(acm);
			}			
		}

	}

}
