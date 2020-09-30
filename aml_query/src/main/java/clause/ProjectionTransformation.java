package clause;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import CAEX215.AttributeType;
import clause.AbstractTransformation.TargetType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import generator.BindingRegister;
import generator.TPQRegister;
import generator.XQueryVariableRegister;
import xquery.XQueryVariable;

public class ProjectionTransformation extends AbstractTransformation{

	public ProjectionTransformation(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode,
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot, XQueryVariable baseVariable,
			XQueryVariableRegister varReg, BindingRegister bindReg) {
		super(targetNode, sourceRoot, baseVariable, varReg, bindReg);
		// TODO Auto-generated constructor stub
	}
	
	private IfClause getIfDefault (XQueryVariable variable) {
		return new IfClause("caex215:isProjectDefault(" + variable.getExpression() + ")");
	}
		
	/**
	 * get clause for direct bound children: full copied or projected
	 * It is a nested IF-THEN-ELSE clause, where each renaming clause is nested into its previous renmaing clauses's ELSE part
	 * @param childVar the variable for the renaming children
	 * @return
	 */
	private IfThenElseClause getClauseForDirectBoundChildren (XQueryVariable childVar) {

//		NestedClause renamingClause = new NestedClause();
		IfThenElseClause projectionClause = new IfThenElseClause(); 
		
		// find out all own renaming children first
//		List<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> ownProjectedChildren = getOwnProjectedChildren();
		Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> ownProjectedChildren = this.directBoundChildren;
		
//		int nrOwnRenamingChildren = ownRenamingChildren.size();
		ElseClause lastElse = null;
		for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> ownProjectedChild : ownProjectedChildren) {
			String childXpath = getXPath(bindReg.getBoundSourceNode(ownProjectedChild)).substring(1);
			childXpath = "[self::" + childXpath + "]";
			IfClause childIf = new IfClause(childVar.getExpression() + childXpath);
			ThenClause childThen = new ThenClause();
//			childThen.addClause(getCopyCommand(childVar));
			
			AbstractTransformation childTransformation = getTransformation(ownProjectedChild, childVar);
			childThen.addClause(childTransformation.getXQueryClause());
			ElseClause childElse = new ElseClause();	
			IfThenElseClause childClause = new IfThenElseClause(childIf, childThen, childElse);
			
			if(lastElse == null) {
				projectionClause = childClause;				
			}										
			else {
				lastElse.addClause(childClause);
			}
			
			lastElse = childElse;			
		}
				
		return projectionClause;
	}

	@Override
	public NestedClause getXQueryClause() {
		
		/* 1. build the default copy commands */
		
		NestedClause clause = new NestedClause();
		StringClause copy1 = getElementNodeNameClause(this.baseVariable);
		StringClause copy2 = new StringClause("{");
		StringClause copy3 = new StringClause(this.baseVariable.getExpression() + "/@*,");
		clause.addClause(copy1);
		clause.addClause(copy2);
		clause.addClause(copy3);
		
		// build the FOR-RETURN clause for default copy
//		XQueryVariable directChildVar = new XQueryVariable();
		XQueryVariable directChildVar = varReg.getChildVariable(this.boundNode);
		String directChildXPath = "";
		directChildXPath = this.baseVariable.getExpression() + "/node()";
		ForClause directChildrenFor = new ForClause(directChildVar, directChildXPath);
		ReturnClause directChildrenRet = new ReturnClause();
		
		// build the IF-THEN-ELSE for default copy
		IfClause ifClause = getIfDefault(directChildVar);
		ThenClause thenClause = new ThenClause();
		thenClause.addClause(getCopyCommand(directChildVar));
		
		/* 2. Add direct renmaing, projection, full-copied, and new element children to ELSE*/
		
		// the else clause will contain all clauses for all own children
		ElseClause elseClause = new ElseClause();		
		NestedClause renamingClause = getClauseForRenamingChildren(directChildVar);
		if(!renamingClause.getClauses().isEmpty()) {
			elseClause.addClause(renamingClause);
		}
		else {
			// add projected children
			NestedClause projectionClause = getClauseForDirectBoundChildren(directChildVar);		
			elseClause.addClause(projectionClause);
			// add new element children
//			NestedClause newElementClause = getClauseForOwnNewElementChildren(directChildVar);
//			if(newElementClause != null)
//				elseClause.addClause(newElementClause);
		}
		
		// finishing the direct children
		directChildrenRet.addClause(ifClause);
		directChildrenRet.addClause(thenClause);
		directChildrenRet.addClause(elseClause);
		ForReturnClause directChildrenForReturn = new ForReturnClause(directChildrenFor, directChildrenRet);
		clause.addClause(directChildrenForReturn);
				
		/* 3. handle all external children */
		int nrExternalTargetChildren = externalTargetChildren.size();
		if(nrExternalTargetChildren != 0) {
			clause.addClause(new StringClause(","));
			NestedClause externalChildrenClause = getClausesForExternalChildren(this.externalTargetChildren);
			clause.addClause(externalChildrenClause);
		}
		
		/* 4. handle all simple new element children */			
		NestedClause newElementChildrenClause = getClauseForSimpleNewElementChildren();
		if(!newElementChildrenClause.getClauses().isEmpty()) {
			clause.addClause(new StringClause(","));
			clause.addClause(newElementChildrenClause);
		}

		
		clause.addClause(new StringClause("}"));
		return clause;
	}	
}
