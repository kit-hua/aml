package clause;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class FullCopyTransformation extends AbstractTransformation{

	public FullCopyTransformation(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode,
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot, XQueryVariable baseVariable,
			XQueryVariableRegister varReg, BindingRegister bindReg) {
		super(targetNode, sourceRoot, baseVariable, varReg, bindReg);
		// TODO Auto-generated constructor stub
	}

	@Override
	public NestedClause getXQueryClause() {
		
		NestedClause clause = new NestedClause();
		
		// if the target node is a full-copy node with no child
		if(type.equals(TargetType.COPY_SIMPLE)) {
			StringClause copy = getCopyCommand(this.baseVariable);
			clause.addClause(copy);
			// no child node so directly return
			return clause;
		}
		
		// A complex full copy target node can have:
		// - own renaming children
		// - own new element children
		// - external children including:
		//		- renaming
		// 		- full copy
		// 		- projection
		else if (type.equals(TargetType.COPY)) {

			/* 1. build the default copy commands */
			
			StringClause copy1 = getElementNodeNameClause(this.baseVariable);
			StringClause copy2 = new StringClause("{");
			StringClause copy3 = new StringClause(this.baseVariable.getExpression() + "/@*,");
			clause.addClause(copy1);
			clause.addClause(copy2);
			clause.addClause(copy3);						
			
			/* 2. handle all direct renaming children*/
						
			// build FOR-RETURN for direct renaming children
//			XQueryVariable directChildVar = new XQueryVariable();
			XQueryVariable directChildVar = varReg.getChildVariable(boundNode);
			String directChildXPath = "";
			directChildXPath = this.baseVariable.getExpression() + "/node()";
			ForClause directChildrenFor = new ForClause(directChildVar, directChildXPath);
			ReturnClause directChildrenRet = new ReturnClause();
			
			// handle all direct renaming and new element children
			// TODO: what about direct bound children? A direct copy child which changes some of attributes?
			NestedClause renamingClause = getClauseForRenamingChildren(directChildVar);
			if(!renamingClause.getClauses().isEmpty()) {
				directChildrenRet.addClause(renamingClause);
			}
//			else {
//				// add new element children
//				NestedClause newElementClause = getClauseForOwnNewElementChildren(directChildVar);
//				if(newElementClause != null)
//					directChildrenRet.addClause(newElementClause);
//			}
			
			/* 3. add default copy */	
			if(!renamingClause.getClauses().isEmpty()) {
				ElseClause elseClause = getInnerMostElseFromIfThenElseClause((IfThenElseClause) renamingClause);
				elseClause.addClause(getCopyCommand(directChildVar));
			}			
			else {
				directChildrenRet.addClause(getCopyCommand(directChildVar));
			}
			ForReturnClause directChildrenForReturn = new ForReturnClause(directChildrenFor, directChildrenRet);
			clause.addClause(directChildrenForReturn);
												
			/* 4. handle all external children */			
			int nrExternalTargetChildren = externalTargetChildren.size();
			if(nrExternalTargetChildren != 0) {
				clause.addClause(new StringClause(","));
				NestedClause externalChildrenClause = getClausesForExternalChildren(this.externalTargetChildren);
				clause.addClause(externalChildrenClause);
			}
			
			/* 5. handle all simple new element children */			
			NestedClause newElementChildrenClause = getClauseForSimpleNewElementChildren();
			if(!newElementChildrenClause.getClauses().isEmpty()) {
				clause.addClause(new StringClause(","));
				clause.addClause(newElementChildrenClause);
			}

			
			clause.addClause(new StringClause("}"));
			return clause;
		}
		
		else {
			QueryWarning warning = new QueryWarning(new Throwable().getStackTrace(), this.getClass().toString() + ": not a full copy transformation!");
			warning.print();
			return null;
		}	
	}

}
