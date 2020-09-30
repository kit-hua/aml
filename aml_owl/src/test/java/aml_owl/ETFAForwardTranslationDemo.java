package aml_owl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.dllearner.core.StringRenderer;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.util.SimpleRenderer;
import org.semanticweb.owlapi.vocab.OWLFacet;

import concept.model.GenericAMLConceptModel;
import concept.model.AMLConceptConfig;
import concept.tree.GenericTreeNode;
import concept.util.GenericAMLConceptModelUtils;
import constants.AMLClassIRIs;
import constants.AMLObjectPropertyIRIs;
import translation.expression.AML2OWLConverter;
import translation.expression.AMLConcept;
import translation.expression.AMLConceptTree;
import translation.expression.TranslationUtils;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class ETFAForwardTranslationDemo {

	protected static OWLDataFactory dataFactory = new OWLDataFactoryImpl();
	private OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
//	private OWLObjectRenderer renderer = new SimpleRenderer();

	// classes
	static OWLClass A = dataFactory.getOWLClass(IRI.create("A"));
	static OWLClass B = dataFactory.getOWLClass(IRI.create("B"));
	static OWLClass C = dataFactory.getOWLClass(IRI.create("C"));
	static OWLClass D = dataFactory.getOWLClass(IRI.create("D"));
	static OWLClass E = dataFactory.getOWLClass(IRI.create("E"));
	static OWLClass Robot = dataFactory.getOWLClass(IRI.create("Robot"));
	static OWLClass Controller = dataFactory.getOWLClass(IRI.create("Controller"));
	static OWLClass Joint = dataFactory.getOWLClass(IRI.create("Joint"));
	static OWLClass Motor = dataFactory.getOWLClass(IRI.create("Motor"));
	static OWLClass STRUCTURE = dataFactory.getOWLClass(IRI.create("Structure"));

	// properties
	static OWLObjectProperty hasIE = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_IE);
	static OWLObjectProperty isIE = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_OF);
	static OWLDataProperty price = dataFactory.getOWLDataProperty(IRI.create("has_price"));
	static OWLDataProperty weight = dataFactory.getOWLDataProperty(IRI.create("has_weight"));
	static OWLDataProperty axis = dataFactory.getOWLDataProperty(IRI.create("has_axis"));
	static OWLDataProperty vel = dataFactory.getOWLDataProperty(IRI.create("has_vel"));

	// <=30
	static OWLDataRange doubleLe30 = dataFactory.getOWLDatatypeRestriction(dataFactory.getDoubleOWLDatatype(), OWLFacet.MAX_INCLUSIVE, dataFactory.getOWLLiteral(30));				
	// <=10
	static OWLDataRange doubleLe10 = dataFactory.getOWLDatatypeRestriction(dataFactory.getDoubleOWLDatatype(), OWLFacet.MAX_INCLUSIVE, dataFactory.getOWLLiteral(10));
	// >=10
	static OWLDataRange doubleGr10 = dataFactory.getOWLDatatypeRestriction(dataFactory.getDoubleOWLDatatype(), OWLFacet.MIN_INCLUSIVE, dataFactory.getOWLLiteral(10));

	static OWLClassExpression hasPrice = dataFactory.getOWLDataSomeValuesFrom(price, doubleLe30);
	static OWLClassExpression hasWeight = dataFactory.getOWLDataSomeValuesFrom(weight, doubleLe10);
	static OWLClassExpression hasVel = dataFactory.getOWLDataSomeValuesFrom(vel, doubleGr10);

	static OWLClassExpression hasController = dataFactory.getOWLObjectSomeValuesFrom(hasIE, dataFactory.getOWLObjectIntersectionOf(Controller, hasWeight));		
	static OWLClassExpression hasAxis = dataFactory.getOWLDataHasValue(axis, dataFactory.getOWLLiteral(6));
	static OWLClassExpression hasMotor = dataFactory.getOWLObjectSomeValuesFrom(hasIE, dataFactory.getOWLObjectIntersectionOf(Motor, hasVel));				
	static OWLClassExpression hasJoint = dataFactory.getOWLObjectSomeValuesFrom(hasIE, dataFactory.getOWLObjectIntersectionOf(Joint, hasMotor));		
	static OWLClassExpression hasRobot = dataFactory.getOWLObjectIntersectionOf(Robot, hasAxis, hasJoint);		
	static OWLClassExpression hasRobot2 = dataFactory.getOWLObjectUnionOf(hasPrice, hasController, hasRobot);

	// (A or B) and (C or D)
	static OWLClassExpression abcd = dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectUnionOf(A,B), dataFactory.getOWLObjectUnionOf(C,D));		
	// (A or B) and C
	static OWLClassExpression abc = dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectUnionOf(A,B), C);
	// (A and b) or (C and D)		
	static OWLClassExpression abcd2 = dataFactory.getOWLObjectUnionOf(dataFactory.getOWLObjectIntersectionOf(A,B), dataFactory.getOWLObjectIntersectionOf(C,D));
	// (A or B)
	static OWLClassExpression ab = dataFactory.getOWLObjectUnionOf(A,B);
	// (B and C)
	static OWLClassExpression bc = dataFactory.getOWLObjectIntersectionOf(B,C);
	// (A and D)		
	static OWLClassExpression ad = dataFactory.getOWLObjectIntersectionOf(A, D);
	static OWLClassExpression ieA = dataFactory.getOWLObjectSomeValuesFrom(hasIE, A);

	// isIEOf some (hasRobot2)
	static OWLClassExpression inv1 = dataFactory.getOWLObjectSomeValuesFrom(isIE, hasRobot2);
	// isIEOf some (inv1)
	static OWLClassExpression inv2 = dataFactory.getOWLObjectSomeValuesFrom(isIE, inv1);
	static OWLClassExpression inv3 = dataFactory.getOWLObjectIntersectionOf(inv1, A);

	static OWLClassExpression inv4 = dataFactory.getOWLObjectSomeValuesFrom(isIE, A);
	static OWLClassExpression inv41 = dataFactory.getOWLObjectSomeValuesFrom(isIE, B);
	static OWLClassExpression inv42 = dataFactory.getOWLObjectSomeValuesFrom(isIE, dataFactory.getOWLObjectUnionOf(inv4, inv41));
	static OWLClassExpression inv5 = dataFactory.getOWLObjectSomeValuesFrom(isIE, dataFactory.getOWLObjectIntersectionOf(inv4, B));
	static OWLClassExpression inv6 = dataFactory.getOWLObjectSomeValuesFrom(isIE, hasVel);
	static OWLClassExpression inv7 = dataFactory.getOWLObjectIntersectionOf(A, dataFactory.getOWLObjectSomeValuesFrom(isIE, dataFactory.getOWLObjectIntersectionOf(B, hasPrice)));
	static OWLClassExpression inv8 = dataFactory.getOWLObjectIntersectionOf(A, dataFactory.getOWLObjectSomeValuesFrom(isIE, dataFactory.getOWLObjectIntersectionOf(B, dataFactory.getOWLObjectIntersectionOf(hasPrice, hasVel))));
	static OWLClassExpression inv9 = dataFactory.getOWLObjectSomeValuesFrom(isIE, dataFactory.getOWLObjectIntersectionOf(A, hasVel));
	static OWLClassExpression inv10 = dataFactory.getOWLObjectSomeValuesFrom(isIE, inv6);
	static OWLClassExpression inv11 = dataFactory.getOWLObjectSomeValuesFrom(isIE, inv8);

	static OWLClassExpression AandPrice = dataFactory.getOWLObjectIntersectionOf(A, hasPrice);

	// This example is bad since it produces a negated attribute
	//			OWLClassExpression notRobotandhasPrice = dataFactory.getOWLObjectComplementOf(dataFactory.getOWLObjectIntersectionOf(Robot, hasPrice));

	// This example is bad since it produces a negated attribute
	//			OWLClassExpression notRobotandhasController = dataFactory.getOWLObjectComplementOf(dataFactory.getOWLObjectIntersectionOf(Robot, hasController));

	static OWLClassExpression notRobotandhasController = dataFactory.getOWLObjectComplementOf(dataFactory.getOWLObjectIntersectionOf(Robot, dataFactory.getOWLObjectSomeValuesFrom(hasIE, Controller)));

	// This example is bad since it produces a negated attribute
	static OWLClassExpression nothasPrice = dataFactory.getOWLObjectComplementOf(hasPrice);

	static OWLClassExpression allIEnotController = dataFactory.getOWLObjectAllValuesFrom(hasIE, dataFactory.getOWLObjectComplementOf(Controller));
	static OWLClassExpression allIEonlyController = dataFactory.getOWLObjectAllValuesFrom(hasIE, Controller);
	static OWLClassExpression someIEnotController = dataFactory.getOWLObjectSomeValuesFrom(hasIE, dataFactory.getOWLObjectComplementOf(Controller));
	static OWLClassExpression notController = dataFactory.getOWLObjectComplementOf(Controller);	


	static OWLObjectProperty HAS_IE = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_IE);
	static OWLObjectProperty HAS_EI = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_EI);
	static OWLObjectProperty IS_IE = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_OF);
	static OWLObjectProperty IS_IE_TC = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_OF_TC);
	static OWLObjectProperty IS_EI = dataFactory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_EI_OF);
	static OWLClassExpression IOCONTROLLER = dataFactory.getOWLClass(IRI.create("IOController"));
	static OWLClassExpression IOINTERFACE = dataFactory.getOWLClass(IRI.create("IOInterface"));
	static OWLClassExpression NOT_IOCONTROLLER = dataFactory.getOWLObjectComplementOf(IOCONTROLLER);
	static OWLDataProperty MANUFACTURER = dataFactory.getOWLDataProperty(IRI.create("has_manufacturer"));
	static OWLLiteral KUKA = dataFactory.getOWLLiteral("KUKA");	

	static OWLClassExpression ETFA_A = dataFactory.getOWLObjectIntersectionOf(
			Robot, 
			dataFactory.getOWLObjectComplementOf(
					dataFactory.getOWLObjectSomeValuesFrom(HAS_IE, NOT_IOCONTROLLER))); 
//	static OWLClassExpression ETFA_B = dataFactory.getOWLObjectSomeValuesFrom(IS_IE, dataFactory.getOWLObjectIntersectionOf(
//			Robot,
//			dataFactory.getOWLDataHasValue(MANUFACTURER, KUKA)));
	static OWLClassExpression ETFA_B = dataFactory.getOWLObjectIntersectionOf(Controller, dataFactory.getOWLObjectSomeValuesFrom(IS_IE, dataFactory.getOWLObjectIntersectionOf(
			Robot,
			dataFactory.getOWLDataHasValue(MANUFACTURER, KUKA))));
	static OWLClassExpression ETFA_C = dataFactory.getOWLObjectIntersectionOf(Robot, dataFactory.getOWLObjectSomeValuesFrom(HAS_IE, dataFactory.getOWLObjectIntersectionOf(
			IOCONTROLLER,
			dataFactory.getOWLObjectMinCardinality(3, HAS_EI, IOINTERFACE))));
	static OWLClassExpression ETFA_D = dataFactory.getOWLObjectIntersectionOf(IOINTERFACE, dataFactory.getOWLObjectSomeValuesFrom(IS_EI, dataFactory.getOWLObjectMinCardinality(3, HAS_EI, IOINTERFACE)));
	
	static OWLClassExpression ROBOT_AND_CONTROLLER = dataFactory.getOWLObjectIntersectionOf(Robot, Controller);
	static OWLClassExpression ROBOT_AND_CONTROLLER2 = dataFactory.getOWLObjectIntersectionOf(Robot, dataFactory.getOWLObjectUnionOf(Controller, IOCONTROLLER));
	
	static OWLClassExpression CARD = dataFactory.getOWLObjectMinCardinality(3, HAS_IE);
	static OWLClassExpression HAS_STRUCTURE = dataFactory.getOWLObjectSomeValuesFrom(HAS_IE, STRUCTURE);
	static OWLClassExpression TEST = dataFactory.getOWLObjectIntersectionOf(Robot, 
			dataFactory.getOWLObjectMinCardinality(2, IS_IE_TC));	

	private List<OWLClassExpression> etfa = new ArrayList<OWLClassExpression>(); 

	public ETFAForwardTranslationDemo() {
//		etfa.add(ETFA_A);
//		etfa.add(ETFA_B);
//		etfa.add(ETFA_C);
//		etfa.add(ETFA_D);
//		etfa.add(notRobotandhasController);
//		etfa.add(CARD);
		
		etfa.add(TEST);
	}

	public List<OWLClassExpression> getETFAClasses () {
		return this.etfa;
	}

	public void run (OWLClassExpression ce, IRI caexType) {				
		
		// ======================= STEP 1: OWL -> ACM ======================= //
		System.out.println("1. First, we show the result (AML Concept Model) of the forward translation:\n");
		System.out.println(" original owl class:\n\n\t " + renderer.render(ce) + "\n");
		System.out.println(" negation normal form (NNF):\n\n\t " + renderer.render(ce.getNNF()));
		List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> acms = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>>();
		try {
			acms = TranslationUtils.toAMLConceptModel(ce, caexType, true);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
				| IllegalArgumentException | InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		// ======================= STEP 2: ACM -> OWL ======================= //
		System.out.println("\n2. Then, we show the reproduced OWL class expression using backward translation\n");		
		Set<OWLClassExpression> union = TranslationUtils.toOWLClassExpression(acms, true);
		OWLClassExpression reproduced = null;
		
		if(union.size() == 1) {
			reproduced = union.iterator().next();
		}
		
		if(union.size() > 1) {									
			int k = 1;
			for(OWLClassExpression op : union) {
				System.out.println("    - OWL class of the AML concept tree [" + k + "]:\n\n\t " + renderer.render(op) + "\n");
				k++;
			}		
			
			reproduced = dataFactory.getOWLObjectUnionOf(union);
			System.out.println("   Then we combine each of the OWL classes as a union: \n\n\t" + renderer.render(reproduced) + "\n");						
		}
		
		System.out.println("   It shall be clear that the following OWL class expressiones are equivalent:\n");
		System.out.println("    - the original OWL class (C) in NNF:\n\n\t " + renderer.render(ce.getNNF()) + "\n");
		System.out.println("    - the reproduced OWL class as backward_translation(forward_translation(C)):\n\n\t " + renderer.render(reproduced) + "\n");
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {						

		ETFAForwardTranslationDemo tester = new ETFAForwardTranslationDemo();
		TranslationUtils.renderer = tester.renderer;
		AMLConcept.setRender(tester.renderer);
		
		String className = "";
		for(int i = 0; i < tester.getETFAClasses().size(); i++) {
			
			if(i==0)
				className = "ETFA_A";
			else if(i==1)
				className = "ETFA_B";
			else if(i==2)
				className = "ETFA_C";
			else if(i==3)
				className = "ETFA_D";
			else if(i==4)
				className = "ETFA_E";
			
			System.out.print("====================================== " + className + " ======================================\n");
			
			IRI caexType = AMLClassIRIs.INTERNAL_ELEMENT;
			if(i == 3)
				caexType = AMLClassIRIs.EXTERNAL_INTERFACE;
			
			
			tester.run(tester.getETFAClasses().get(i), caexType);
			
		}
	}

}
