package clause;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import CAEX215.AttributeType;
import clause.AbstractTransformation.TargetType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import generator.BindingRegister;
import generator.QueryWarning;
import generator.TPQRegister;
import generator.XQueryVariableRegister;
import xquery.XQueryVariable;

public class RenamingTransformation extends AbstractTransformation{

	public RenamingTransformation(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode,
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot, XQueryVariable baseVariable,
			XQueryVariableRegister varReg, BindingRegister bindReg) {
		super(targetNode, sourceRoot, baseVariable, varReg, bindReg);
		// TODO Auto-generated constructor stub
	}
	
	private IfClause getRenameCondition (String sourceXpath) {
		
		// remove heading "/"
		if(sourceXpath.startsWith("/"))
			sourceXpath = sourceXpath.substring(1);
		
		// adding "self::"
		sourceXpath = "self::" + sourceXpath;
		
		// build XQuery If clause
		IfClause attributeTestIf = new IfClause(baseVariable.getExpression() + "[" + sourceXpath + "]");
		return attributeTestIf;
	}

	/**
	 * Get the full nested xquery clause for this target node
	 * The clause contains the renaming transformation clause and its nested FOR-RETURN loops for recursive handling of special children, including:
	 * - for a renaming node:
	 * 		- renaming child
	 * @param baseVar the base xquery variable for this transformation. Generated and given by the super transformation in its FOR-RETURN loop for this target node
	 * @return
	 */
	@Override	
	public NestedClause getXQueryClause() {
		
		if(!type.equals(TargetType.RENAME)) {
			QueryWarning warning = new QueryWarning(new Throwable().getStackTrace(), this.getClass().toString() + ": not a renaming transformation!");
			warning.print();
			return null;			
		}
		
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceAttr = bindReg.getBoundSourceNode(targetNode);			
		String sourceAttrXpath = getXPath(sourceAttr);
		
		/* 1. Build the default renaming commands */
		
		// Generating the if-clause
		IfClause ifClause = getRenameCondition(sourceAttrXpath);			

		/* Generating the then-clause */
		ThenClause thenClause = new ThenClause();						
		/* Generating the attribute renaming commands */
		AttributeType targetAttr = (AttributeType) targetNode.data.getObj();
		StringClause rename1 = getElementNodeNameClause(this.baseVariable);
		thenClause.addClause(rename1);
		StringClause rename2 = new StringClause("{\n");
		thenClause.addClause(rename2);
		StringClause rename3 = new StringClause(this.baseVariable + "/@*[not(name(.)=\"Name\")],\n") ;
		thenClause.addClause(rename3);
		StringClause rename4 = new StringClause("attribute{\"Name\"}{\"" + targetAttr.getName() + "\"},\n");
		thenClause.addClause(rename4);		
		
		/* 2. Handle direct renaming child nodes */
		
		// build FOR-RETURN for all direct renaming children
//		XQueryVariable directChildVar = new XQueryVariable();
		XQueryVariable directChildVar = varReg.getChildVariable(this.boundNode);
		String directChildXPath = "";
		directChildXPath = this.baseVariable.getExpression() + "/node()";
		ForClause directChildrenFor = new ForClause(directChildVar, directChildXPath);
		ReturnClause directChildrenRet = new ReturnClause();
		Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> children = targetNode.getChildren();
		// get the clause of renaming children if there is any
		int nrChildren = children.size();
		if(nrChildren != 0) {
			NestedClause renamingClause = getClauseForRenamingChildren(directChildVar);
			if(!renamingClause.getClauses().isEmpty()) {
				directChildrenRet.addClause(renamingClause);
			}
			ForReturnClause directChildrenForReturn = new ForReturnClause(directChildrenFor, directChildrenRet);
			thenClause.addClause(directChildrenForReturn);
		}
		// otherwise, get default copy of all children
		else {
			// if the attribute node has no child
			XQueryVariable dummyChild = new XQueryVariable();			
			ForClause xfor = new ForClause(dummyChild, baseVariable.getExpression() + "/node()");
			ReturnClause xret = new ReturnClause();
			xret.addClause(getCopyCommand(dummyChild));
			thenClause.addClause(xfor);
			thenClause.addClause(xret);
		} // end rename children
				
		/* 3. Close the IF-THEN-ELSE clause */
		
		StringClause rename5 = new StringClause("}\n");
		thenClause.addClause(rename5);
		ElseClause elseClause = new ElseClause();
		IfThenElseClause clause = new IfThenElseClause(ifClause, thenClause, elseClause);
		
		// TODO
		/* 4. Handle external children */
		
		return clause;
	}

}
