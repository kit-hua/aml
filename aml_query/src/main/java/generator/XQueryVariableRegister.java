package generator;

import java.util.HashMap;

import concept.model.AMLConceptConfig;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import xquery.XQueryVariable;

/**
 * Register for xquery variables used in the query
 * Provides a service for looking up variables during target traversal
 * @author Yingbing Hua, IAR-IPR, KIT
 * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
 */
public class XQueryVariableRegister {

	private HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, XQueryVariable> variables;
	private HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, XQueryVariable> childVariables;
	
	public XQueryVariableRegister() {
		variables = new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, XQueryVariable>();
		childVariables = new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, XQueryVariable>();
	}
	
	/**
	 * add an xquery variable to the register, and sync between all siblings
	 * @param node
	 * @param var
	 */
	// if adding a new variable, then sync all siblings
	public void addVariable (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, XQueryVariable var) {
		this.variables.put(node, var);
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sibling : node.getSibling()) {
			this.variables.put(sibling, var);
		}
	}
	
	/**
	 * get the xquery variable of the given node
	 * if the variable does not exist yet, construct one
	 * @param node
	 * @return
	 */
	public XQueryVariable getVariable (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		
		if(variables.containsKey(node))
			return variables.get(node);		
		else {
			XQueryVariable var = new XQueryVariable();
			this.addVariable(node, var);
			return var;
		}
	}
	
	/**
	 * get the xquery variable for the children of the give node
	 * @param node
	 * @return
	 */
	public XQueryVariable getChildVariable (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		
		if(childVariables.containsKey(node))
			return childVariables.get(node);
		
		else {			
			
			// if node has no child, need to invent a new variable for projection nodes with no child
			if(node.getChildren().isEmpty()) {
				XQueryVariable childVar = new XQueryVariable();
				childVariables.put(node, childVar);
				return childVar;
			}			
			else {
				// if node has children, but none of them has triggered the construction of a variable
				// then make one, and store the child variable				
				GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child = node.getChildren().iterator().next();
				XQueryVariable childVar = getVariable(child);
				childVariables.put(node, childVar);
				return childVar;
			}
				
		}		
	}
}
