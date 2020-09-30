//package clause;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import xquery.XQueryVariable;
//
//public class IdentityTransformationClause extends NestedClause {
//
//	private XQueryVariable variable, childVariable;	
//	
//	private ReturnClause xret;		
//	
//	private List<NewElementTransformationOLD> newElements;
//	
//	public IdentityTransformationClause (XQueryVariable variable, XQueryVariable childVariable) {
//		this.variable = variable;
//		this.childVariable = childVariable;
//		this.newElements = new ArrayList<NewElementTransformationOLD>();
//		this.xret = new ReturnClause();		
//	}
//	
//	public void addReturnCommand (String cmd) {
//		xret.addClause(new StringClause(cmd));
//	}
//	
//	public void addReturnSubClause (AbstractFormattedXQueryClause clause) {
//		xret.addClause(clause);
//	}
//	
//	public void addNewElement (NewElementTransformationOLD element) {
//		newElements.add(element);
//	}
//	
//	@Override
//	public String getXQuery () {	
//		
//		String s = "element{node-name(" + variable.getExpression() + ")}\n";
//		s += "{\n";
//		s += variable.getExpression() + "/@*,\n";
//		
//		//TODO: this is wrong.
//		// If the source of child and source of the node itself is not parent-child-relation, then it does not work
//		// for both projected and full-copied nodes, if the child does not come from a child of its source
//		// we could enforce that, for all bound nodes, its children shall come from the same source node
//		s += "for " + childVariable.getExpression() + " in " + variable.getExpression() + "/node()\n";
//		
//		// if the return clase is empty yet
//		// add the default return command
//		if(xret.getClauses().isEmpty()) {
//			xret.addClause(new StringClause("caex215:copy(" + childVariable.getExpression() + ")\n"));
//		}
//		
//		s += xret.getXQuery();
//		
//		if(!this.newElements.isEmpty()) {
//			s += ",\n";
//			for(NewElementTransformationOLD ele : newElements) {
//				s += ele.getXQuery() + ",\n";
//			}			
//		}
//		
//		if(s.endsWith(",\n")) {
//			s = s.substring(0, s.length()-2);
//			s += "\n";
//		}
//		
//		s += "}\n";
//		return s;
//	}
//	
//}
