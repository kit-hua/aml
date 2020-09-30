package translation.expression;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.util.SimpleRenderer;

import concept.model.AMLConceptConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import concept.util.GenericAMLConceptModelUtils;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class TranslationUtils {
		
	public static OWLObjectRenderer renderer = new SimpleRenderer();
	
	public static List<AMLConceptTree> toAMLConceptTrees (OWLClassExpression ce, IRI caexType, boolean verbose) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {				
		
		AMLConceptTree tree = new AMLConceptTree(ce);
		tree.getRoot().data.setCaexType(caexType);
		
		// the root node is the primary object
		tree.getRoot().data.setPrimary(true);
		
		// expand the initial tree to remove disjunctions
		Set<AMLConceptTree> expandedTrees = tree.expand();
//		int i = 1;
		// for each disjunction operand
		List<AMLConceptTree> trees = new ArrayList<AMLConceptTree>();
		for(AMLConceptTree expanded : expandedTrees) {		

			if(verbose) {
				System.out.println("\n - 1. The AND-tree: \n");
				System.out.println(expanded.toStringWithIndent(3));	
			}			

			AMLConceptTree noInverse = expanded.removeInverseRole();
			
			if(verbose) {
				System.out.println("\n - 2. removed inverse roles: \n");
				System.out.println(noInverse.toStringWithIndent(3));	
			}
			
			trees.add(noInverse);
		}

		return trees;
	}
	
	public static GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> toAMLConceptModel (AMLConceptTree tree, boolean verbose) {
		GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> acm = null;
		try {
			if(verbose) {
				System.out.println("\n - 3. The generated AML concept model:\n");
			}
			acm = AMLConceptTree.toAMLConceptModelTreeNode(tree.getRoot());
			
			if(verbose) {
				System.out.println(acm.toStringWithIndent(3));
				System.out.println("\n - 4. the cleaned (fused) AML concept model:\n");
			}
			
			GenericAMLConceptModelUtils.fuse(acm);
			
			if (verbose) {
				System.out.println(acm.toStringWithIndent(3));
			}
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return acm;
	}
	
	public static List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> toAMLConceptModel (OWLClassExpression ce, IRI caexType, boolean verbose) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
		AMLConceptTree tree = new AMLConceptTree(ce);
		tree.getRoot().data.setCaexType(caexType);
		
		// the root node is the primary object
		tree.getRoot().data.setPrimary(true);
		
		// expand the initial tree to remove disjunctions
		Set<AMLConceptTree> expandedTrees = tree.expand();
		int treeIdx = 1;
		// for each disjunction operand
		
		List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> acms = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>>();
		for(AMLConceptTree expanded : expandedTrees) {		

			if(verbose) {
				System.out.println("\n - 1. The AND-tree [" + treeIdx + "]: \n");
				System.out.println(expanded.toStringWithIndent(3));	
			}
			
			AMLConceptTree noInverse = expanded.removeInverseRole();
			
			if(verbose) {
				System.out.println("\n - 2. removed inverse roles [" + treeIdx + "]: \n");
				System.out.println(noInverse.toStringWithIndent(3));
			}
			
			GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> acm = null;
			try {
				
				if(verbose) {
					System.out.println("\n - 3. The generated AML concept model [" + treeIdx + "]: \n");
				}
				
				acm = AMLConceptTree.toAMLConceptModelTreeNode(noInverse.getRoot());
				
				if(verbose) {
					System.out.println(acm.toStringWithIndent(3));
					System.out.println("\n - 4. the cleaned (fused) AML concept model [" + treeIdx + "]: \n");
				}
				// the fusion is necessary since OWL class expressions and their AML concept trees split nested attributes and cardinalities into several expressions
				// - the attribute frame.(x, y) will become two data properties has_frame_x and has_frame_y, which need to be fused to one CAEX attribute
				// - the min and max cardinality of one object will become two cardinality restrictions 
				GenericAMLConceptModelUtils.fuse(acm);
				System.out.println(acm.toStringWithIndent(3));
				
				acms.add(acm);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			treeIdx++;
		}
				
		return acms;
	}
	
	public static Set<OWLClassExpression> toOWLClassExpression (List<GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>>> acms, boolean verbose) {
		Set<OWLClassExpression> union = new HashSet<OWLClassExpression>(); 
		if(acms.size() > 1 && verbose)
			System.out.println("   Since we have several AML concept trees generated, we compute an OWL class for each of them:\n");
		
		int i = 1;
		for(GenericTreeNode<GenericAMLConceptModel<AMLConceptConfig>> acm : acms) {				
			AML2OWLConverter aml2owl = new AML2OWLConverter();
			OWLClassExpression ce2 = aml2owl.toOWLClassExpression(acm);
			OWLClassExpression ce3 = aml2owl.cleanse(ce2);
			
			if(verbose) {
				System.out.println(" - generated owl class of ACM [" + i + "]: \n\n\t " + renderer.render(ce2) + "\n");
				System.out.println(" - simplified owl class of ACM [" + i + "]:\n\n\t " + renderer.render(ce3) + "\n");
			}
			union.add(ce3);											
		}
		
		return union;
	}

}
