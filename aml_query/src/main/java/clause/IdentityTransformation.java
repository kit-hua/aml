//package clause;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;
//
//import CAEX215.AttributeType;
//import CAEX215.CAEXBasicObject;
//import concept.model.AMLQueryConfig;
//import concept.model.AMLTargetConfig;
//import concept.model.GenericAMLConceptModel;
//import concept.model.AMLTargetConfig.ExchangeMode;
//import concept.tree.GenericTreeNode;
//import exporter.AMLExporter;
//import generator.BindingRegister;
//import generator.TPQRegister;
//import generator.XQueryVariableRegister;
//import xquery.XQueryVariable;
//
///**
// * Identity transformation is the XQuery code pattern for copying and adapting source XML data into target XML schema
// * It is used as the basic structure for nAQL in data exchange scenarios
// * For all bound nodes in target model, we construct IdentityTransformation for data exchange
// * Inputs for the identity transformations are:
// * - source node name
// * - renaming attribute name (source, target)
// * - simple new nodes name
// * - if it is a projection: the projected children 
// * 
// * The XQueryExchangeGenerator will traverse the target model
// * - if a full copied node: identity transformation on simple copied node
// * - if a projection node: identity transformation with default copy.
// * 		- for all children, add their own identity transformation to the current node IF-THEN(Default)-ELSE(Child)
// * - if a renaming node: identity transformation of the renaming, add to the current node FOR(Children)-RETURN clause
// * 
// * @author Yingbing Hua, IAR-IPR, KIT
// * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
// */
//public class IdentityTransformation extends AbstractTransformation{
//	
////	private enum TargetType {COPY_SIMPLE, COPY, RENAME, PROJECT, UNKNOWN} 	
////	
////	private TargetType type;
//	
//	GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode;
//	private XQueryVariable xqueryVar, childXqueryVar;
//	
//	public IdentityTransformation(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode,
//			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot,
//			XQueryVariable baseVariable,
//			XQueryVariableRegister varReg, BindingRegister bindReg, TPQRegister tpqReg) {
//		
//		super(targetNode, sourceRoot, baseVariable, varReg, bindReg, tpqReg);		
//		this.sourceNode = bindReg.getBoundSourceNode(targetNode);
//		this.xqueryVar = varReg.getVariable(sourceNode);
//		this.childXqueryVar = varReg.getChildVariable(sourceNode);
//		
//		this.type = getType(this.targetNode);
////		this.subNewElements = new HashSet<NewElementConstructor>();
////		this.subTrans = new HashSet<IdentityTransformation>();
//	}
//
////	private String getElementNodeNameClause (XQueryVariable variable) {
////		return "element{node-name(" + variable.getExpression() + ")}\n";
////	}
//
//	private IfClause getRenameCondition (String sourceXpath) {
//		
//		// remove heading "/"
//		if(sourceXpath.startsWith("/"))
//			sourceXpath = sourceXpath.substring(1);
//		
//		// adding "self::"
//		sourceXpath = "self::" + sourceXpath;
//		
//		// build XQuery If clause
//		IfClause attributeTestIf = new IfClause(xqueryVar.getExpression() + "[" + sourceXpath + "]");
//		return attributeTestIf;
//	}
//	
////	private TargetType getType () {
////		
////		AMLTargetConfig config = (AMLTargetConfig) targetNode.data.getConfig();
////		ExchangeMode mode = config.getMode();
////
////		// if the target node is a full-copy non-attribute node with no child
////		// this is the simplest case, where we only return the variable name
////		if(mode.equals(AMLTargetConfig.ExchangeMode.COPY) && targetNode.isLeaf() && !(targetNode.data.getObj() instanceof AttributeType)) 
////			return TargetType.COPY_SIMPLE;
////		
////		// if the target node is a full-copy attribute node: rename
////		// here we need renaming commands
////		// for potential sub-attributes, further renaming required
////		// However, we do not consider projection of sub-attributes
////		if((mode.equals(AMLTargetConfig.ExchangeMode.COPY) || mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION)) && targetNode.data.getObj() instanceof AttributeType)
////			return TargetType.RENAME;
////		
////		// if the target node is a full-copy node with child
////		// the child can be either renaming nodes or new elements
////		if(mode.equals(AMLTargetConfig.ExchangeMode.COPY) && !targetNode.isLeaf()) {
////			return TargetType.COPY;
////		}
////
////		// if the target node is a projection node
////		// we need to take care of all possible children
////		if(mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION)) {
////			return TargetType.PROJECT;
////		}
////				
////		// In other cases, e.g., renaming with projection, projection from own node: exception
////		// We do not consider "new element type" here, since they belong to another class
////		return TargetType.UNKNOWN;
////	} 
//	
////	public void addNewElementChild (NewElementConstructor child) {
////		this.subNewElements.add(child);
////	}
////	
////	public void addIdentityTransformationChild (IdentityTransformation child) {
////		this.subTrans.add(child);
////	}				
//	
////	private StringClause getCopyCommand (XQueryVariable xvar) {
////		return new StringClause("caex215:copy(" + xqueryVar.getExpression() + ")");
////	}
//	
//	/**
//	 * Get the full nested xquery clause for this target node
//	 * The clause contains the identity transformation clause and its nested FOR-RETURN loops for recursive handling of special children, including:
//	 * - for a renaming node:
//	 * 		- renaming child
//	 * - for a full copy node:
//	 * 		- all bound children including renaming
//	 * 		- new element child with bound descendant
//	 * - for a projection node:
//	 * 		- renaming child
//	 * 		- non-projected bound child (projected bound child are covered within IF-THEN-ELSE)
//	 * 		- new element child with bound descendant   
//	 * @param baseVar the base xquery variable for this transformation. Generated and given by the super transformation in its FOR-RETURN loop for this target node
//	 * @return
//	 */
//	public NestedClause getXQueryClause(XQueryVariable baseVar) {
//		NestedClause clause = new NestedClause();
//		
//		// if the target node is a full-copy node with no child
//		if(type.equals(TargetType.COPY_SIMPLE)) {
//			StringClause copy = getCopyCommand(baseVar);
//			clause.addClause(copy);
//			// no child node so directly return
//			return clause;
//		}
//		
//		// if the target node is a renaming node
//		// build a IF-THEN-ELSE clause as:
//		// - IF: the attribute testing
//		// - THEN: the transformation with renaming, and possible FOR-RETURN clause with
//		//		- FOR: loop over children
//		//		- RETURN: for each child, build an IF-THEN-ELSE clause for the renaming child
//		// - ELSE: empty clause
//		else if(type.equals(TargetType.RENAME)) {
//			
//			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceAttr = bindReg.getBoundSourceNode(targetNode);			
//			String sourceAttrXpath = getXPath(sourceAttr);
//			
//			/* Generating the if-clause */
//			IfClause ifClause = getRenameCondition(sourceAttrXpath);			
//
//			/* Generating the then-clause */
//			ThenClause thenClause = new ThenClause();						
//			/* Generating the attribute renaming commands */
//			AttributeType targetAttr = (AttributeType) targetNode.data.getObj();
//			StringClause rename1 = getElementNodeNameClause(xqueryVar);
//			thenClause.addClause(rename1);
//			StringClause rename2 = new StringClause("{\n");
//			thenClause.addClause(rename2);
//			StringClause rename3 = new StringClause(xqueryVar + "/@*[not(name(.)=\"Name\")],\n") ;
//			thenClause.addClause(rename3);
//			StringClause rename4 = new StringClause("attribute{\"Name\"}{\"" + targetAttr.getName() + "\"},\n");
//			thenClause.addClause(rename4);
//						
//			// handle child nodes
//			// renaming nodes can only have renaming children, but no projection and new nodes			
//			Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> children = targetNode.getChildren();
//			int nrChildren = children.size();
//			if(nrChildren != 0) {
//				List<ForReturnClause> childrenContextClauses = new ArrayList<ForReturnClause>(); 
//				
//				// get all context nodes of all children
//				Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> childContextNodes = this.getContextNodes(children);
//				
//				// sort the context nodes
//				List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> sortedChildContextNodes = this.sortContextNodes(childContextNodes);
//				
//				// we loop through the children in the order of the sorted context nodes
//				for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childContextNode : sortedChildContextNodes) {
//											
//					// retain each child of the current target node, for which the current context node is its context node
//					Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> childrenOfContextNode = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
//					for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : children) {
//						if(getContextNode(child).equals(childContextNode)) {
//							childrenOfContextNode.add(child);
//						}
//					}
//					
//					// build a for-return clause for the current context node, based on some base node in predecessors
//					// baseNodeForContext could be the current context node or its predecessor
//					// TODO: getXPathBetween shall cover both cases
//					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childBaseNodeForContext = getBaseSourceNodeForContextNode(childContextNode);
//					XQueryVariable childBaseVar = varReg.getVariable(childBaseNodeForContext);		
//					String childContextXpath = getXPathBetween(childBaseNodeForContext, childContextNode);
//					
//					// Note here we will not use the previous "for $n3 in $n2/node()" for the child nodes
//					// but use more general "for $n3 in $n2/Attribute[...]" if the child node's source is a child of the current node's source
//					// - if all children have the same context node, which will be the current renaming node, then the rest will be similar to the previous code
//					// otherwise, it will be "for $n3 in $n1/.../Attribute[...]" to allow merging of different attributes 
//					ForClause childContextFor = new ForClause(childBaseVar, childBaseVar + childContextXpath);
//					ReturnClause childContextRet = new ReturnClause();
//
//					// loop through all the children of the current target node, which have the current context node				
//					for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> childOfContextNode : childrenOfContextNode) {														
//						// if the current context node is the same as the child's bound node, we need only the context for-return clause
//						// if the context node is different to child's bound node, and child's bound node exists
//						// then we need two fors
//						// the first one for the context node, as we will reuse it for other children
//						// the second one for the current child (bound) node
//						//  - var: context variable
//						//  - xpath: context to child's bound
//						if(!childContextNode.equals(this.boundNode) && this.boundNode!= null) {
//							XQueryVariable childContextVar = varReg.getVariable(childContextNode);
//							GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childBound = bindReg.getBoundSourceNode(childOfContextNode);
//							XQueryVariable childBoundVar = varReg.getVariable(childBound);
//							String childBoundXpath = this.getXPathBetween(childContextNode, childBound);
//							ForClause childBoundFor = new ForClause(childBoundVar, childContextVar.getExpression() + childBoundXpath);
//							ReturnClause childBoundRet = new ReturnClause();
//							// add the code of child
//							childBoundRet.addClause(getXQueryClause(childBoundVar));
//							// build FOR-RETURN for the child bound node						
//							ForReturnClause boundForRet = new ForReturnClause(childBoundFor, childBoundRet);
//							childContextRet.addClause(boundForRet);
//						}													
//					} // end all children of current context node
//					childrenContextClauses.add(new ForReturnClause(childContextFor, childContextRet));
//				} // end all context nodes
//				
//				// add all child context clauses to the previous then clause
//				for (ForReturnClause childContextClause : childrenContextClauses) {
//					thenClause.addClause(childContextClause);
//					thenClause.addClause(new StringClause(","));
//				}
//			} // end nrChildren != 0
//			
//			
////			int nrChildren = children.size();
////			if(!children.isEmpty()) {
////				// this would introduce new xquery variable, but it is necessary as it is not a variable for a specific child but all children			
////				ForClause xfor = new ForClause(childXqueryVar, xqueryVar + "/node()");
////				ReturnClause xret = new ReturnClause();
////				for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : children) {
////					IdentityTransformation childTran = new IdentityTransformation(child, sourceRoot, varReg, bindReg, tpqReg);
////					xret.addClause(childTran);
////					StringClause comma = new StringClause(",\n");
////					if(nrChildren>1) {
////						xret.addClause(comma);
////						nrChildren--;
////					}					
////				}												
////				thenClause.addClause(xfor);
////				thenClause.addClause(xret);
////			} // end child
//			else {
//				// if the attribute node has no child
//				XQueryVariable dummyChild = new XQueryVariable();				
//				ForClause xfor = new ForClause(dummyChild, xqueryVar.getExpression() + "/node()");
//				ReturnClause xret = new ReturnClause();
//				xret.addClause(getCopyCommand(dummyChild));
//				thenClause.addClause(xfor);
//				thenClause.addClause(xret);
//			} // end rename children
//						
//			StringClause rename5 = new StringClause("}\n");
//			thenClause.addClause(rename5);
//			ElseClause elseClause = new ElseClause();
//			clause.addClause(ifClause);
//			clause.addClause(thenClause);
//			clause.addClause(elseClause);
//		}
//		
//		return clause;
//	}
//	
//	@Override
//	public String getXQuery () {
//		
//		String command = "";
//		
//		// build for-return clause
//		
//		// if the target node is a full-copy node with no child
//		if(type.equals(TargetType.COPY_SIMPLE)) {
//			command = "caex215:copy(" + xqueryVar.getExpression() + ")";
//		}
//		
//		// if the target node is a renaming node
//		// build a IF-THEN-ELSE clause as:
//		// - IF: the attribute testing
//		// - THEN: the transformation with renaming, and possible FOR-RETURN clause with
//		//		- FOR: loop over children
//		//		- RETURN: for each child, build an IF-THEN-ELSE clause for the renaming child
//		// - ELSE: empty clause
//		else if(type.equals(TargetType.RENAME)) {
////			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceAttr = sourceNode; 
//			String sourceAttrXpath = tpqReg.getTPQ(sourceNode);
//			
//			/* Generating the if-clause */
//			IfClause ifClause = getRenameCondition(sourceAttrXpath);			
//
//			/* Generating the then-clause */
//			ThenClause thenClause = new ThenClause();						
//			/* Generating the attribute renaming commands */
//			AttributeType targetAttr = (AttributeType) targetNode.data.getObj();
//			StringClause rename1 = getElementNodeNameClause(xqueryVar);
//			thenClause.addClause(rename1);
//			StringClause rename2 = new StringClause("{\n");
//			thenClause.addClause(rename2);
//			StringClause rename3 = new StringClause(xqueryVar + "/@*[not(name(.)=\"Name\")],\n") ;
//			thenClause.addClause(rename3);
//			StringClause rename4 = new StringClause("attribute{\"Name\"}{\"" + targetAttr.getName() + "\"},\n");
//			thenClause.addClause(rename4);
//						
//			// handle child nodes
//			// renaming nodes can only have renaming children, but no projection and new nodes			
//			Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> children = targetNode.getChildren();
//			int nrChildren = children.size();
//			if(!children.isEmpty()) {
//				// this would introduce new xquery variable, but it is necessary as it is not a variable for a specific child but all children			
//				ForClause xfor = new ForClause(childXqueryVar, xqueryVar + "/node()");
//				ReturnClause xret = new ReturnClause();
//				for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : children) {
//					IdentityTransformation childTran = new IdentityTransformation(child, sourceRoot, childXqueryVar, varReg, bindReg, tpqReg);
//					xret.addClause(childTran);
//					StringClause comma = new StringClause(",\n");
//					if(nrChildren>1) {
//						xret.addClause(comma);
//						nrChildren--;
//					}					
//				}												
//				thenClause.addClause(xfor);
//				thenClause.addClause(xret);
//			} // end child
//			else {
//				// if the attribute node has no child
//				XQueryVariable dummyChild = new XQueryVariable();				
//				ForClause xfor = new ForClause(dummyChild, xqueryVar.getExpression() + "/node()");
//				ReturnClause xret = new ReturnClause();
//				xret.addClause(new StringClause("caex215:copy(" + dummyChild.getExpression() + ")"));
//				thenClause.addClause(xfor);
//				thenClause.addClause(xret);
//			}
//			
//			// TODO: remove the last ","
//			StringClause rename5 = new StringClause("}\n");
//			thenClause.addClause(rename5);
//			ElseClause elseClause = new ElseClause();
//			if(!(targetNode.getParent().data.getObj() instanceof AttributeType)) {
//				elseClause.addClause(new StringClause("caex215:copy(" + xqueryVar.getExpression() + ")"));
//			}
//			
//			command += ifClause.getXQuery();
//			command += thenClause.getXQuery();
//			command += elseClause.getXQuery();			
//		}
//		
//		// if the target is a full-copy node with potential renaming and new element children
//		// build a identity transformation 
//		// for each renaming child, add its identity transformation into the FOR(Children)-RETURN clause
//		else if(type.equals(TargetType.COPY)) {
//			// compose command
//			IdentityTransformationClause itClause = new IdentityTransformationClause(xqueryVar, childXqueryVar);
//			
//			// handle child nodes
//			// full-copied nodes can have either renaming children or new element children						
//			// handle renaming children
//			for(IdentityTransformation subTran : getSubIdentityTransformations()) {
//				itClause.addReturnSubClause(subTran);
//			}
//			
//			for(NewElementTransformationOLD subNew : getSubNewElementConstructors()) {
//				itClause.addNewElement(subNew);				
//			}
//			
//			command = itClause.getXQuery();					
//		} // END COPY
//		
//		// if the target is a projection node
//		// build a identity transformation
//		// fill the return clause with IF-THEN-ELSE, where
//		// - IF: default
//		// - THEN: default
//		// - ELSE: projected children, rename children, new elements?
//		else if(type.equals(TargetType.PROJECT)) {
//			IdentityTransformationClause itClause = new IdentityTransformationClause(xqueryVar, childXqueryVar);
//			
//			// default copies
//			IfClause ifClause = new IfClause("caex215:isProjectDefault(" + childXqueryVar.getExpression() + ")");
//			ThenClause thenClause = new ThenClause();
//			thenClause.addClause(new StringClause("caex215:copy(" + childXqueryVar.getExpression() + ")"));
//			ElseClause elseClause = new ElseClause();			
//			
//			// handle child nodes
//			// projection nodes can have either full-copied children, renaming children, or new element children
//			int nrChildren = getSubIdentityTransformations().size();
//			for(IdentityTransformation subTran : getSubIdentityTransformations()) {
//				// for each simple full-copied or projected child: IF-THEN-ELSE
//				// - THEN: result from child
//				// - ELSE: ()
//				if(subTran.type.equals(TargetType.COPY_SIMPLE) || subTran.type.equals(TargetType.COPY) || subTran.type.equals(TargetType.PROJECT)) {
//					// find the xpath of child
//					// TODO: this is wrong if the source of the target child is not a child of the source of the target
//					// 			because tpqReg is based on hierarchy of LCA nodes
//					String childXpath = tpqReg.getTPQ(bindReg.getBoundSourceNode(subTran.targetNode));
//					if(childXpath.startsWith("/"))
//						childXpath = childXpath.substring(1);
//					childXpath = "self::" +  childXpath;
//					IfClause childIf = new IfClause(childXqueryVar.getExpression() + "[" + childXpath + "]");
//					ThenClause childThen = new ThenClause();
//					childThen.addClause(new StringClause(subTran.getXQuery()));
//					ElseClause childElse = new ElseClause();
//					String childClause = childIf.getXQuery() + childThen.getXQuery() + childElse.getXQuery();										
//					elseClause.addClause(new StringClause(childClause));
//				}
//				
//				// for each rename child: directly IF-THEN-ELSE from child
//				if(subTran.type.equals(TargetType.RENAME)) {
//					String childClause = subTran.getXQuery();
//					elseClause.addClause(new StringClause(childClause));
//				}
//				
//				if(nrChildren > 1) {
//					StringClause comma = new StringClause(",\n");
//					elseClause.addClause(comma);
//					nrChildren--;
//				}
//			}
//			
//			// compose command
//			itClause.addReturnSubClause(ifClause);
//			itClause.addReturnSubClause(thenClause);
//			itClause.addReturnSubClause(elseClause);
//			
//			for(NewElementTransformationOLD subNew : getSubNewElementConstructors()) {
//				itClause.addNewElement(subNew);				
//			}
//								
//			command = itClause.getXQuery();
//		}  // END PROJECT		
//		
//		return command;
//	}
//
//	@Override
//	public NestedClause getXQueryClause() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}
