package clause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import CAEX215.AttributeType;
import CAEX215.CAEXObject;
import CAEX215.InstanceHierarchyType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.model.AMLTargetConfig.ExchangeMode;
import concept.tree.GenericTreeNode;
import generator.BindingRegister;
import generator.QueryWarning;
import generator.SortTreeNodesByDepth;
import generator.XPathGenerator;
import generator.XQueryExchangeGenerator;
import generator.XQueryVariableRegister;
import xquery.XQueryVariable;

public abstract class AbstractTransformation extends AbstractFormattedXQueryClause implements ITransformation {
	
	private XPathGenerator xpathGenerator;
	
	protected GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode;		
	
	// xquery variable register
	protected XQueryVariableRegister varReg;
	
	// binding of this target node
	protected BindingRegister bindReg;		

	// binding of this target node
//	protected TPQRegister tpqReg;		
	
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot;
	
	// the context source node of the target node: must exist
	// for the target root node, its context is the source root node
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> contextNode;
	
	// the bound source node of the target node: is null for all new elements
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> boundNode;
	
	// the source node that serves as the basis for building XPath of current target node
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> baseSourceNode;
	
	private Set<AbstractTransformation> childTransformations;
	private AbstractTransformation parentTransformation;
	
	protected enum TargetType {COPY_SIMPLE, COPY, RENAME, PROJECT, NEW, UNKNOWN}
	protected TargetType type;
	protected XQueryVariable baseVariable;
	
	private static final String ORDER = "O";
	
	/**
	 * all external children of the current target node
	 * for bound target nodes, external target children are those children, that, context(child) goes beyond bound(target)
	 * for new element target nodes, external target children are those children which are not simple new elements
	 */
	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> externalTargetChildren;
	
	/**
	 * all simple new children of the current target node
	 * they are new elements whose descendants are not bound
	 */
	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> simpleNewTargetChildren;
	
	/**
	 * all direct renaming children of the current target node
	 * They are attribute nodes whose source bound is a child of the current target node
	 */
	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> directRenamingChildren;
	
	/**
	 * all bound children of the current target node which are not attribute nodes, and
	 * whose source bound node is a child of the current source bound node
	 * For projection nodes, these are the 
	 *  - full copied children: children whose source correspondence are completely taken to target 
	 *  - projected children: children whose source correspondence are again projected to target
	 * For full copy nodes, these are the 
	 * 	- full copied children
	 */
	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> directBoundChildren;
	
//	/**
//	 * all direct complex new element children of the current target node
//	 * their source context nodes are children of the current source bound node 
//	 */
//	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> directNewElementChildren;

	public AbstractTransformation(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode, 
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot,
			XQueryVariable baseVariable,
			XQueryVariableRegister varReg, BindingRegister bindReg) {
		this.targetNode = targetNode;
		this.varReg = varReg;
		this.bindReg = bindReg;
//		this.tpqReg = tpqReg;
		this.sourceRoot = sourceRoot;
		this.baseVariable = baseVariable;
		
		this.contextNode = this.getContextNode(targetNode);		
		this.boundNode = this.bindReg.getBoundSourceNode(targetNode);		
		
		this.childTransformations = new HashSet<AbstractTransformation>();
		this.xpathGenerator = new XPathGenerator();
		
//		this.internalTargetChildren = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		this.externalTargetChildren = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		this.simpleNewTargetChildren = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		this.directRenamingChildren = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		this.directBoundChildren = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
//		this.directNewElementChildren = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		this.type = getType(targetNode);
		this.classifyTargetChildren();						
	}
	
	public GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> getTargetNode () {
		return this.targetNode;
	}
	
	protected StringClause getElementNodeNameClause (XQueryVariable variable) {
		return new StringClause("element{node-name(" + variable.getExpression() + ")}");
	}
	
	protected StringClause getCopyCommand (XQueryVariable variable) {
		return new StringClause("caex215:copy(" + variable.getExpression() + ")");
	}
	
	protected void addChildTransformation (AbstractTransformation child) {
		this.childTransformations.add(child);
		child.parentTransformation = this;
		child.baseSourceNode = child.getBaseSourceNode();
	}
	
	/**
	 * get the node type of the target node
	 * @param target
	 * @return
	 */
	protected TargetType getType (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target) {
		
		AMLTargetConfig config = (AMLTargetConfig) target.data.getConfig();
		ExchangeMode mode = config.getMode();

		// if the target node is a full-copy non-attribute node with no child
		// this is the simplest case, where we only return the variable name
		if(mode.equals(AMLTargetConfig.ExchangeMode.COPY) && target.isLeaf() && !(target.data.getObj() instanceof AttributeType)) 
			return TargetType.COPY_SIMPLE;
		
		// if the target node is a full-copy attribute node: rename
		// here we need renaming commands
		// for potential sub-attributes, further renaming required
		// However, we do not consider projection of sub-attributes
		if((mode.equals(AMLTargetConfig.ExchangeMode.COPY) || mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION)) && target.data.getObj() instanceof AttributeType)
			return TargetType.RENAME;
		
		// if the target node is a full-copy node with child
		// the child can be either renaming nodes or new elements
		if(mode.equals(AMLTargetConfig.ExchangeMode.COPY) && !target.isLeaf()) {
			return TargetType.COPY;
		}

		// if the target node is a projection node
		// we need to take care of all possible children
		if(mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION)) {
			return TargetType.PROJECT;
		}
		
		// TODO: remove redundancy checks
		if(mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION) || !bindReg.isNodeBound(target))
			return TargetType.NEW;
				
		// In other cases, e.g., renaming with projection, projection from own node: exception
		// We do not consider "new element type" here, since they belong to another class
		return TargetType.UNKNOWN;
	}
	
	public AbstractTransformation getTransformation (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target, XQueryVariable variable) {
		TargetType type = getType(target);
		
		if(type.equals(TargetType.RENAME))
			return new RenamingTransformation(target, sourceRoot, variable, varReg, bindReg);
		
		else if(type.equals(TargetType.PROJECT))
			return new ProjectionTransformation(target, sourceRoot, variable, varReg, bindReg);
		
		else if(type.equals(TargetType.COPY_SIMPLE) || type.equals(TargetType.COPY))
			return new FullCopyTransformation(target, sourceRoot, variable, varReg, bindReg);
		
		else if(type.equals(TargetType.NEW))
			return new NewElementTransformation(target, sourceRoot, variable, varReg, bindReg);
			
		
		else {
			QueryWarning warning = new QueryWarning(new Throwable().getStackTrace(), this.getClass().toString() + ": target type unknown!");
			warning.print();
			return null;
		}
			
		
	}
	
	/**
	 * Check if the source node LCA-covers the target node
	 * a source node LCA-covers a target node, if the source node is a LCA and a predecessor of the source correspondence of the target node
	 * @return
	 */
	private boolean isLCAcovers (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode, GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode) {
		
		// if the target node is not bound
		if(!bindReg.isNodeBound(targetNode))
			return false;
		
		// if the source node is not a LCA
		if(!sourceNode.hasLabel(XQueryExchangeGenerator.LCA)) {
			return false;
		}
		
		else {
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> boundSource = bindReg.getBoundSourceNode(targetNode);
			if(!sourceNode.equals(boundSource) && !sourceNode.hasDescendant(boundSource)) {
				return false;
			}

		}
		
		return true;
	}
	
	/**
	 * Get the LCA-correspondence of the target node 
	 * A source node is the LCA-correspondence of a target node, if 
	 * 	- the source node LCA-covers all bound descendants of the target node
	 *  - the source node LCA-covers the target node if itself is bound
	 *  - no source node deeper than it also satisfies the previous both conditions: we get the deepest one 
	 * @param target
	 * @return
	 */
	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getLCA (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> node) {
		
		// if the target node is not bound and is a leaf
		if(!bindReg.isNodeBound(node) && node.isLeaf())
			return null;
		
		// get all target descendants-or-self
		List<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> allDescendantsOrSelf = node.getDescendantOrSelf();
		List<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> boundDescendantsOrSelf = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		// filter out bound nodes
		for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> candidate : allDescendantsOrSelf) {
			if(bindReg.isNodeBound(candidate)) {
				boundDescendantsOrSelf.add(candidate);
			}
		}
		
		// if the target node is not bound and its sub-tree is not bound
		if(boundDescendantsOrSelf.isEmpty())
			return null;
		
		// find the deepest LCA that covers all the bound descendants
		int depth = -1;
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> lca = null;
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode : sourceRoot.getDescendantOrSelf()) {
			
			boolean covers = true;
			// the source node shall LCAcover all the bound descendants
			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> boundTarget : boundDescendantsOrSelf) {
				if (!isLCAcovers(sourceNode, boundTarget)) {
					covers = false;
					break;
				}
			}			

			if(covers) {
				int sourceNodeDepth = sourceNode.getDepth();
				if(sourceNodeDepth > depth) {
					lca = sourceNode;
					depth = sourceNodeDepth;
				}
			}
		}
		
		return lca;		
	}	
	
	/**
	 * For a given target node, return the context source node that serves as its base TPQ. 
	 * A context node is either a LCA or a bound node.
	 * A LCA context node reduces redundancy in the xquery program
	 *  - if the target node is not bound and has no bound child, then it is a simple new node and has no source correspondence, return null
	 *  - if the target node has a LCA correspondence, e.g., a new element or a bound node that shares common predecessor with other bound target nodes, then return the LCA
	 *  - in other cases, it is a single (not sharing predecessor with other target node) bound target node, so return the bound source
	 * @param targetNode
	 * @return
	 */
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getContextNode (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> node) {
		
		if(node.data.getObj() instanceof InstanceHierarchyType)
			return sourceRoot;
		
		// if target node is not bound and has no bound descendant, then it is a simple new node and has no source correspondence
		if(!bindReg.isNodeOrDescendantBound(node))
			return null;
		
		// if target node has a LCA, return the LCA
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> lca = getLCA(node);
		if(lca != null) {
			return lca;
		}
		else
			// if lca is null, then return the bound source
			return bindReg.getBoundSourceNode(node);				
	}
	
	/**
	 * Get the set of all source context nodes from the given set of target nodes
	 * @param nodes
	 * @return
	 */
	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getContextNodes (Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> nodes) {
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> contextNodes = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : nodes) {
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> contextNode = getContextNode(child);			
			// the context node could be null if the child is a new element
			if(contextNode != null)
				contextNodes.add(contextNode);
		}
		
		return contextNodes;	
	}
	
	/**
	 * Sort the given set of source context nodes by their depth: nodes near root are put in front
	 * @param nodes
	 * @return
	 */
	protected List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> sortContextNodes (Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> nodes) {
		// sort the context nodes by depth
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> sortedContextNodes = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		sortedContextNodes.addAll(nodes);
		Collections.sort(sortedContextNodes, new SortTreeNodesByDepth<AMLQueryConfig>());
		return sortedContextNodes;
	}
	
	/**
	 * whether a source node serves as a basis for building the XPath of a given target node
	 * That is, target.xpath = $var(source)/[XPATH BETWEEN SOURCE AND TARGET]
	 * This is the case, if the source node is a predecessor of the context node of the target
	 * @param contextNode
	 * @param node
	 * @return
	 */
	protected boolean isSourceNodeBasisForTargetNodeXPath (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source, GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target) {				
		return isSourceNodeBasisForSourceNodeXPath(source, getContextNode(target));
	}
	
	/**
	 * whether source1 serves as a basis for building the XPath of source2
	 * That is, source2.xpath = $var(source1)/[XPATH BETWEEN SOURCE1 AND SOURCE2]
	 * @param contextNode
	 * @param node
	 * @return
	 */
	protected boolean isSourceNodeBasisForSourceNodeXPath (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source1, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source2) {		
		if(source1.getDescendantOrSelf().contains(source2)) {
			return true;
		}
		return false;
	}
	
	/**
	 * get all context and bound nodes of predecessor AbstractXQueryCode
	 * they are candidates of the base source node of the current target node
	 * the final base source code might not be the direct super AbstractXQueryCode, but must exist in the predecessors
	 * In worst case, we trace back to the source root node
	 * @return
	 */
	protected Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getCandidatesOfBaseSourceNode () {
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> candidates = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		
		if(this.parentTransformation != null) {
			candidates.add(this.parentTransformation.contextNode);
			if(this.parentTransformation.boundNode != null)
				candidates.add(this.parentTransformation.boundNode);
		
			candidates.addAll(this.parentTransformation.getCandidatesOfBaseSourceNode());			
		}
		
		return candidates;
	}
	
	/**
	 * get the source node that can serve as base xpath of current target node
	 * traverse backwards to super AbstractXQueryCode to find it, i.e., the deepest one
	 * in this implementation, we collect all candidates from super AbstractQueryNodes
	 * and pick the deepest one
	 * @return
	 */
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getBaseSourceNode () {
	
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> candidates = this.getCandidatesOfBaseSourceNode();
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> baseSourceNode = sourceRoot;
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> candidate : candidates) {
			if(isSourceNodeBasisForTargetNodeXPath(candidate, this.targetNode)) {
				if(candidate.getDepth() > baseSourceNode.getDepth())
					baseSourceNode = candidate;
			}
		}		
		return baseSourceNode;						
	} 
	
	/**
	 * Get the base source node for a given source node from this transformation.
	 * This method is used for compute the XQuery variable that serves as the basis for child nodes of the current target node.
	 * The base source node is a source node that is the nearest predecessor of the given source node, 
	 * selected from transformations starting from the current one and recursively traverses upwards.
	 * 1. if the current bound node is a base for the given context node, then return current bound node, otherwise,
	 * 2. if the current context node is a base for the given context node, then return current context node, otherwise,
	 * 3. go upwards to previous transformation and try to find one: this is not really happening in our test cases!
	 * @param source
	 * @return
	 */
	protected GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getBaseSourceNodeForContextNode (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source) {
		// if the context node of the current target node is a base for the given context node
		// then check the bound node, since boundNode.depth >= contextNode.depth
		if(isSourceNodeBasisForSourceNodeXPath(this.contextNode, source)) {
			// if bound node exist, check if it satisfies
			if(this.boundNode!= null && isSourceNodeBasisForSourceNodeXPath(this.boundNode, source)) {
				return this.boundNode;
			}
			else
				return this.contextNode;
		}
		else {
			
			if(this.parentTransformation != null)
				return this.parentTransformation.getBaseSourceNodeForContextNode(source);
			else 
				// shall never happen!
				return null;
		}
		
	}
	
	/**
	 * the cardinality in raw xpath needs to be adapted for the data exchange
	 * @param xpath
	 * @return
	 */
	private String cleanCardinality (String xpath) {
		
		String zeroCardPrefix = "*[count(";
		String zeroCardPostfix = ")>=0]";
		
		String zeroCardPrefixSelf = zeroCardPrefix + "self::";
		String zeroCardPrefixDescendantOrSelf = zeroCardPrefix + "descendant-or-self::";

		// if the xpath expression was a zero-or-one cardniliaty using self:: axis, remove the self:: axis also
		if(xpath.startsWith(zeroCardPrefixSelf) && xpath.endsWith(zeroCardPostfix)) {
			xpath = "/" + xpath.substring(zeroCardPrefixSelf.length(), xpath.length()-zeroCardPostfix.length());
		}
		
		// if the xpath expression was a zero-or-one cardniliaty using descedant-or-self:: axis, only remove the cardinality prefix
		else if(xpath.startsWith(zeroCardPrefixDescendantOrSelf) && xpath.endsWith(zeroCardPostfix)) {
			xpath = "/" + xpath.substring(zeroCardPrefix.length(), xpath.length()-zeroCardPostfix.length());
		}
		
		// if the xpath expression was not a zero-or-one cardinality, add the "/" 
		else
			xpath = "/" + xpath;

		return xpath;
	}
	
	/**
	 * get the full xpath for the given "start" node
	 * it can be the root IH or any other nodes
	 * @param start
	 * @return
	 */
	protected String getXPath (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> start) {
		
		// if the start node is the IH
		// build xpath for each of its children
		// combine them into a predicate
		if(start.equals(sourceRoot)) {
			
			// make Predicate an object to get rid of "[" "]" operations
			String predicate = "[";
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : start.getChildren()) {
				predicate += xpathGenerator.getXPath(child, false, false);
				predicate += " and ";
			}
			predicate = predicate.substring(0, predicate.length()-5);
			predicate += "]";
			return "/InstanceHierarchy" + predicate;
		}
		
		else {
			String xpath = xpathGenerator.getXPath(start, false, false);
			return cleanCardinality(xpath);
		}
		
	}
	
	/**
	 * get the predecessor of the sourceNode bounded by the context Node
	 * So the result is the predecessor of the source node, which is the child of the contextNode 
	 * @param sourceNode
	 * @param contextNode
	 * @return
	 */
	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getShallowestPredecessor (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> contextNode) {
		
		if(sourceNode.equals(contextNode) || sourceNode.getParent().equals(contextNode)) {
			return sourceNode;
		}		
		
		else
			return getShallowestPredecessor(sourceNode.getParent(), contextNode);					
	} 
	
	/**
	 * build xpath from start to end while skipping the start node
	 * start can be IH or any other predecessor of end
	 * @param start
	 * @param end
	 * @return
	 */
	protected String getXPathBetween (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> start, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> end) {
		String xpath = "";
		
		if(start.equals(end)) {
			return "";
		}
		
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> predecessor = getShallowestPredecessor(end, start);
		
		// if the only parent of end is IH
		// then we build .../[XPATH(end)]
		if(predecessor.equals(sourceRoot) || predecessor.equals(start)) {
			xpath += getXPath(end);	
		}
		// otherwise
		else {
			xpath += xpathGenerator.getXPathBetween(predecessor, end, false, false);
			return cleanCardinality(xpath);
		}
		
		return xpath;
	}
	
	/**
	 * whether a target node is a simple new element
	 * yes, if the neither the target node nor any of its descendant is bound
	 * @param node
	 * @return
	 */
	protected boolean isSimpleNewElement (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> node) {
		return !bindReg.isNodeOrDescendantBound(node);
	}
	
	/**
	 * Classify all children of the current target node:
	 * - direct renaming children
	 * - direct bound children: can by copy or projection
	 * - direct new element children
	 * - external children
	 */
	protected void classifyTargetChildren () {
		
		/* 1. if the current target node is a new node */
		
		// then all children are external
		// Because new element nodes do not have theoretically dependent direct children
		// So to handle all children, we always need to consider the hierarchy of their context nodes
		// Therefore always use getForReturnClausesForExternalChildren() for all children
		if(this.type.equals(TargetType.NEW)) {
			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : targetNode.getChildren()) {
				
				/* 1.1. if the child node is simple */
				if(isSimpleNewElement(child))
					this.simpleNewTargetChildren.add(child);
				
				/* 1.2. if the child node is complex */
				else
					this.externalTargetChildren.add(child);
			}			
		}
		
		/* 2. if the current target node is a bound node */
		else {
			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : targetNode.getChildren()) {				
				
				/* 2.1. if the child node is a bound node */
				if(bindReg.isNodeBound(child)) {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childBound = bindReg.getBoundSourceNode(child);								

					/* 2.1.1. then the child is internal only if: bound(child).parent = bound(targetNode) */
					if(childBound.getParent().equals(this.boundNode)) {
						
						/* 2.1.1.1. if the child is an AML Attribute, then renaming */				
						if(child.data.getObj() instanceof AttributeType)
							this.directRenamingChildren.add(child);
						
						/* 2.1.1.2. Otherwise, direct bound child */
						else {
							this.directBoundChildren.add(child);
						}
					}
					
					/* 2.1.2. if bound(child).parent != bound(targetNode) but bound(child).predecessor = bound(targetNode) */ 					
					// then we need to handle it as an external node, because we need to take care the hiearchy of such bound child nodes
					else {
						this.externalTargetChildren.add(child);
					}
				}
				/* 2.2. if the child is a new element node */
				else {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childContext = getContextNode(child);
					
					/* 2.2.1. if child context exists, i.e., it is not a simple new child node */
					if(childContext != null) {
						
						/* 2.2.1.1. the child is internal only if: context(child).parent = bound(targetNode) */
						// TODO: due to extra complexity, we put direct new element children to external target children
						// because new element children's context node is unpredictable for now
						if(childContext.getParent().equals(this.boundNode)) {
//							this.directNewElementChildren.add(child);
							this.externalTargetChildren.add(child);
						}
						
						/* 2.2.1.2. the child is external*/
						else {
							this.externalTargetChildren.add(child);
						}
					}
					
					/* 2.2.2. the child context does not exist, it is a simple new element */
					else {
						this.simpleNewTargetChildren.add(child);
					}
				}
			}
		}
			
	}
	
//	protected NestedClause getClauseForOwnNewElementChildren (XQueryVariable childVar) {
//		NestedClause clause = new NestedClause(); 
//		int nrNewElementChildren = this.directNewElementChildren.size();
//		if(nrNewElementChildren > 0) {			
//			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : this.directNewElementChildren) {
//				NewElementTransformation childTransformation = new NewElementTransformation(child, sourceRoot, childVar, varReg, bindReg, tpqReg);
//				clause.addClause(childTransformation.getXQueryClause());
//				if(nrNewElementChildren > 1) {
//					clause.addClause(new StringClause(","));
//					nrNewElementChildren--;
//				}
//			}
//			return clause;
//		}
//		else
//			return null;		
//	} 
	
	protected NestedClause getClauseForSimpleNewElementChildren () {
		NestedClause clause = new NestedClause();
		if(!simpleNewTargetChildren.isEmpty()) {
			
			int nrNewElementChildren = simpleNewTargetChildren.size();
			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : simpleNewTargetChildren) {
				NewElementTransformation childTransformation = new NewElementTransformation(child, sourceRoot, null, varReg, bindReg);
				this.addChildTransformation(childTransformation);
				clause.addClause(childTransformation.getXQueryClause());
				if(nrNewElementChildren > 1) {
					clause.addClause(new StringClause(","));
					nrNewElementChildren--;
				}
			}						
		}
		return clause;
	}
	
	/**
	 * get clause for own renaming children
	 * It is a nested IF-THEN-ELSE clause, where each renaming clause is nested into its previous renmaing clauses's ELSE part
	 * @param childVar the variable for the renaming children
	 * @return
	 */
	protected IfThenElseClause getClauseForRenamingChildren (XQueryVariable childVar) {

//		NestedClause renamingClause = new NestedClause();
		IfThenElseClause renamingClause = new IfThenElseClause(); 
		
//		int nrOwnRenamingChildren = ownRenamingChildren.size();
		ElseClause lastElse = null;
		for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> ownRenamingChild : this.directRenamingChildren) {
			RenamingTransformation childRenaming = new RenamingTransformation(ownRenamingChild, sourceRoot, childVar, varReg, bindReg);
			this.addChildTransformation(childRenaming);
			IfThenElseClause childRenamingClause = (IfThenElseClause) childRenaming.getXQueryClause();
			
			if(lastElse == null) 
				renamingClause = childRenamingClause;						
			else
				lastElse.addClause(childRenamingClause);
			
			lastElse = childRenamingClause.getElseClause();
			
//			if(nrOwnRenamingChildren > 1) {
//				renamingClause.addClause(new StringClause(","));
//				nrOwnRenamingChildren--;
//			}
		}
				
		return renamingClause;
	}
	
	protected ElseClause getInnerMostElseFromIfThenElseClause (IfThenElseClause clause) {
		ElseClause currentElse = clause.getElseClause();
		
		if(currentElse.getClauses().isEmpty())
			return clause.getElseClause();
		else {
			if(currentElse.getClauses().size() > 1) {
				System.out.println("something wrong?!");
				return null;
			}			
			if(currentElse.getClauses().get(0) instanceof IfThenElseClause) {
				IfThenElseClause innerClause = (IfThenElseClause) currentElse.getClauses().get(0);
				return getInnerMostElseFromIfThenElseClause(innerClause);
			}
			else
				return clause.getElseClause();
		}		
			
	}
	
	private NestedClause getClauseForExternalChildrenWithSameContextNode (Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> externalChildren) {		
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> childContextNodes = this.getContextNodes(externalChildren);
		if(childContextNodes.size() != 1) {
			QueryWarning warning = new QueryWarning(new Throwable().getStackTrace(), this.getClass().toString() + ": external children with different context nodes!");
			warning.print();
			return null;
		}
		
		NestedClause clause = new NestedClause();				
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childContextNode = childContextNodes.iterator().next();
		
		// If the child context node is not the base source node of itself
		// build a for-return clause for the current context node, based on some base node in predecessors
		// baseSourceNodeForChildContext: could be the current context node or its predecessor, but shall not be its descendant
		// childContextXPath is used to build the FOR-RETURN of the child context node. It is computed as: the xpath between (baseSourceNodeForChildContext, childContextNode)
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> baseSourceNodeForChildContext = getBaseSourceNodeForContextNode(childContextNode);
		ForClause childContextFor = null;
		ReturnClause childContextRet = null;
//		XQueryVariable childContextVar = this.baseVariable;
		XQueryVariable childContextVar = varReg.getVariable(baseSourceNodeForChildContext);
		
//		if(!baseSourceNodeForChildContext.equals(this.baseSourceNode)) {
		if(!baseSourceNodeForChildContext.equals(childContextNode)) {
			XQueryVariable baseSourceNodeForChildContextVariable = varReg.getVariable(baseSourceNodeForChildContext);		
			String childContextXPath = getXPathBetween(baseSourceNodeForChildContext, childContextNode);
			childContextVar = varReg.getVariable(childContextNode);
			childContextFor = new ForClause(childContextVar, baseSourceNodeForChildContextVariable + childContextXPath);
			childContextRet = new ReturnClause();
		}			

		// loop through all the children of the current target node, which have the current context node
		// for full copy transformation, children can be any bound nodes
		// childOfContextNode: child of the current target node that has the current context node
		int nrChildOfContextNode = externalChildren.size();
		for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> childOfContextNode : externalChildren) {														
			
			// get the source bound node of the child target node
			// if the child node does not have a bound node, it will be handled below
			// basically, a child clause will be built based on its context node
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childBoundNode = bindReg.getBoundSourceNode(childOfContextNode);				
			
			// If:
			// - the child context node is the same as the child's bound node, or
			// - the child is a new element without bound node
			// Then:
			// we do not need to build FOR-RETURN for the child bound node
			// Either the FOR-RETURN of the current target node suffices
			// Or the FOR-RETURN of the child context node sufficices: this is also the case for new element children without bound node
			if(childContextNode.equals(childBoundNode) || childBoundNode == null) {
				AbstractTransformation childTransformation = getTransformation(childOfContextNode, childContextVar);
				this.addChildTransformation(childTransformation);
				NestedClause childClause = childTransformation.getXQueryClause();					
				// child context FOR-RETURN exists
				if(childContextRet != null) {
					childContextRet.addClause(childClause);
					if(nrChildOfContextNode > 1) {
						childContextRet.addClause(new StringClause(","));
						nrChildOfContextNode--;
					}
				}
				// child context FOR-RETURN does not exist, because the child context node is the current source base node
				else {
					clause.addClause(childClause);
					if(nrChildOfContextNode > 1) {
						clause.addClause(new StringClause(","));
						nrChildOfContextNode--;
					}
				}
				
			}
			
			// if the child context node is different to child bound node, and child bound node exists
			// then we need to build a FOR-RETURN for the child bound node
			//  - var: context variable
			//  - xpath: context to child's bound
			if(!childContextNode.equals(childBoundNode) && childBoundNode != null) {												
				XQueryVariable childBoundVar = varReg.getVariable(childBoundNode);
				String childBoundXpath = this.getXPathBetween(childContextNode, childBoundNode);
				ForClause childBoundFor = new ForClause(childBoundVar, childContextVar.getExpression() + childBoundXpath);
				ReturnClause childBoundRet = new ReturnClause();
				// add the code of child
				AbstractTransformation childTransformation = getTransformation(childOfContextNode, childBoundVar);
				this.addChildTransformation(childTransformation);
				childBoundRet.addClause(childTransformation.getXQueryClause());
				// build FOR-RETURN for the child bound node						
				ForReturnClause boundForRet = new ForReturnClause(childBoundFor, childBoundRet);
				// child context FOR-RETURN exists
				if(childContextRet != null) {
					childContextRet.addClause(boundForRet);
					if(nrChildOfContextNode > 1) {
						childContextRet.addClause(new StringClause(","));
						nrChildOfContextNode--;
					}	
				}
				// child context FOR-RETURN does not exist, because the child context node is the current source base node
				else {
					clause.addClause(boundForRet);
					if(nrChildOfContextNode > 1) {
						clause.addClause(new StringClause(","));
						nrChildOfContextNode--;
					}
				}
				
			}													
		} // end all children of current context node

		// If child context FOR-RETURN exists, then add it to the output clause			
		if(childContextRet != null) {				
			clause.addClause(new ForReturnClause(childContextFor, childContextRet));
		}
		
		return clause;
	}
	
	/**
	 * get the FOR-RETURN clauses for all extraneous children that have a context node different to the current context and bound node
	 * @param extraneousChildren
	 * @return
	 */
	protected NestedClause getClausesForExternalChildren (Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> externalChildren) {
	
		// the output clause
		NestedClause externalChildrenClause = new NestedClause();
		
		// get all context nodes of all children
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> childContextNodes = this.getContextNodes(externalChildren);
		
		// sort the context nodes
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> sortedChildContextNodes = this.sortContextNodes(childContextNodes);
//		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> sortedChildContextNodes = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
//		sortedChildContextNodes.retainAll(childContextNodes);
		
//		int lastDepth = -1;
//		int order = 0;
//		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childContextNode : sortedChildContextNodes) {
//			if(childContextNode.getDepth() > lastDepth) {							
//				childContextNode.addLabel(ORDER + order);
//				lastDepth = childContextNode.getDepth();
//				order ++;
//			}
//			else
//				childContextNode.addLabel(ORDER + order);
//		}
		
//		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> restChildContextNodes = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		
		// we loop through the children in the order of the sorted context nodes
		int nrChildContextNodes = sortedChildContextNodes.size();
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> childContextNode : sortedChildContextNodes) {
									
			// retain each child of the current target node, for which the current context node is its context node
			Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> childrenOfContextNode = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> child : externalTargetChildren) {
				if(getContextNode(child).equals(childContextNode)) {
					childrenOfContextNode.add(child);
				}
			}
			
			NestedClause contextNodeClause = getClauseForExternalChildrenWithSameContextNode(childrenOfContextNode);

			// If child context FOR-RETURN exists, then add it to the output clause			
			externalChildrenClause.addClause(contextNodeClause);
			if(nrChildContextNodes > 1) {
				externalChildrenClause.addClause(new StringClause(","));
				nrChildContextNodes--;
			}
		} // end all child context nodes
			
		return externalChildrenClause;
	}
	
	 
	@Override
	public String getXQuery () {
		NestedClause clause = this.getXQueryClause();
		return clause.getXQuery();
	}
}
