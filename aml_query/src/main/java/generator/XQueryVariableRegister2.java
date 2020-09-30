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
public class XQueryVariableRegister2 {
		
	private HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, XQueryVariable> variables;
//	private boolean initailized;
//	private BindingRegister bindReg;
	
	public XQueryVariableRegister2(BindingRegister bindReg) {
		variables = new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, XQueryVariable>();
//		this.bindReg = bindReg;
//		initailized = false;
	}
	
//	public boolean init (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot) {
//		if(!sourceRoot.isRoot()) {
//			QueryWarning warning = new QueryWarning(new Throwable().getStackTrace(), this.getClass().toString() + ": sourceRoot is not a root node! Initialization failed.");
//			warning.print();
//			return false;
//		}
//		
//		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : sourceRoot.getChildren()) {
//			if(bindReg.) {
//				
//			}
//		}
//	}
	
	/**
	 * add an xquery variable to the register
	 * @param node
	 * @param var
	 */
	public void addVariable (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, XQueryVariable var) {
		this.variables.put(node, var);		
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
	
	
}
