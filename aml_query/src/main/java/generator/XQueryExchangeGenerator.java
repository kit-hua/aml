package generator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import CAEX215.CAEXObject;
import CAEX215.InstanceHierarchyType;
import CAEX215.util.AMLHelperFunctions;
import clause.AbstractTransformation;
import clause.LetClause;
import clause.NestedClause;
import clause.NewElementTransformation;
import clause.ReturnClause;
import clause.StringClause;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import xquery.XQueryVariable;

public class XQueryExchangeGenerator {
	
	private XPathGenerator xpathGenerator = new XPathGenerator();

	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source;	
	private GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target;
		
	public static final String BOUND = "bound";	
	public static final String VISITED = "visited";
	public static final String LCA = "lca";
	
	private InstanceHierarchyType sourceIh;			
	private BindingRegister bindReg;
	private TPQRegister tpqReg;
	private XQueryVariableRegister varReg;
	private boolean isInitialized;
	
	public XQueryExchangeGenerator(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source, GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target) {
		this.source = source;
		this.target = target;
		
		// Assuming the input is an IH
		this.sourceIh = (InstanceHierarchyType) source.data.getObj();
		
		bindReg = new BindingRegister();
		tpqReg = new TPQRegister();
		varReg = new XQueryVariableRegister();	
		
		isInitialized = false;
	}
	
	public void init(boolean verbose) {
		
		// 1. build bindings
		this.buildBindings(verbose);
		
		// 2. build source LCA
		try {
			this.buildLCATree(verbose);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 3. build source TPQs
//		buildTPQs(verbose);
		
		isInitialized = true;		
	}
	
	/**
	 * Build the object mappings between source and target
	 */
	private void buildBindings (boolean verbose) {
		// traverse the target model for all bindings		
		// the root node is always IH, so start with its children
		for (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode : target.getDescendant()) {
			GenericAMLConceptModel<AMLTargetConfig> data = targetNode.data;
			AMLTargetConfig config = (AMLTargetConfig) data.getConfig();
			AMLTargetConfig.ExchangeMode mode = config.getMode();
			
			// for all target nodes which are copy or projection, build a mapping
			if(mode.equals(AMLTargetConfig.ExchangeMode.COPY) || mode.equals(AMLTargetConfig.ExchangeMode.PROJECTION)) {
				String sourceId = config.getSourceID();
				CAEXObject objectWithId = AMLHelperFunctions.getObjectWithID(sourceIh, sourceId);
				
				// find the source node with the caex object
				for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode : source.getDescendant()) {
					// build the mapping
					if(sourceNode.data.getObj().equals(objectWithId)) {
						// add the source node to the correspondences of the target node
						bindReg.addBinding(new Binding(sourceNode, targetNode));
						
						// label the source node with "bound"
						sourceNode.addLabel(BOUND);
						// add the source node to the set of all bound source nodes							
						break;
					}
				}	
			}
		}
		
		if(verbose) {
			System.out.println("1. BUILD BINDINGS");
			Set<Binding> bindings = bindReg.getBindings();
			for(Binding binding : bindings) {
				String ts = binding.getTarget().toString();
				if(ts.contains("\n"))
					ts = ts.substring(0, ts.indexOf("\n"));
				System.out.println("target:\t" + ts);
				String ss = binding.getSource().toString();
				if(ss.contains("\n"))
					ss = ss.substring(0, ss.indexOf("\n"));
				System.out.println("source:\t" + ss);
			}
		}
		
	}
	
	/**
	 * Build the LCA Tree of the source model   
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private void buildLCATree (boolean verbose) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// compute mappings if not done yet
		if(bindReg.isEmpty())
			buildBindings(verbose);
		
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> boundSourceNodes = bindReg.getBoundSourceNodes();
		if(boundSourceNodes.size()==1) {
			boundSourceNodes.iterator().next().addLabel(LCA);
			return;
		}
		
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> visited = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		Iterator<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> bsnIterOuter = boundSourceNodes.iterator();
		while(bsnIterOuter.hasNext()) {
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> outerNode = bsnIterOuter.next();					
			Iterator<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> bsnIterInner = boundSourceNodes.iterator();
			while(bsnIterInner.hasNext()) {
				GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> innerNode = bsnIterInner.next();
				// !outerNode.equals(innerNode) avoids computing LCA of the same node
				// !visited.contains(innerNode) avoids computing LCA of (A,B) and (B,A)
				if(!outerNode.equals(innerNode) && !visited.contains(innerNode)) {
					// get LCA of outer and inner
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> lca = outerNode.getLowestCommonAncestor(innerNode);
					if(!lca.hasLabel(LCA))
						lca.addLabel(LCA);
				}
			}
			visited.add(outerNode);
		}		
		
		if(verbose) {
			System.out.println("\n2. LABELED TREE: \n");
			System.out.println(source.toString());					
			// LCA-correspondence indicates the context of the new element (besides IH), i.e., the skolem function of the new element
			// For all other target nodes, use the direct bound source node
			for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode : target.getDescendantOrSelf()) {
				if(targetNode.data.getObj() instanceof InstanceHierarchyType)
					continue;
				
				AMLTargetConfig config = (AMLTargetConfig) targetNode.data.getConfig();
				if(config.getMode().equals(AMLTargetConfig.ExchangeMode.NEW) && !targetNode.isLeaf()) {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode = getLCA(targetNode);
					String ts = targetNode.toString();
					if(ts.contains("\n"))
						ts = ts.substring(0, ts.indexOf("\n"));
					System.out.println("\ntarget:\t" + ts);
					String ss = sourceNode.toString();
					if(ss.contains("\n"))
						ss = ss.substring(0, ss.indexOf("\n"));
					System.out.println("\nsource:\t" + ss);
				}
			}
		}
	}
	
	/**
	 * get the closest LCA of the given source node	 
	 * @param sourceNode
	 * @return the LCA which is the closest predecessor of the given source node
	 */
	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getClosestLCA (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode) {
		
		if(sourceNode.isRoot()) {
			return null;
		}
		
		else if(sourceNode.getParent().hasLabel(LCA)) {
			return sourceNode.getParent();
		}
		
		else
			return getClosestLCA(sourceNode.getParent());					
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
	private String getXPath (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> start) {
		
		// if the start node is the IH
		// build xpath for each of its children
		// combine them into a predicate
		if(start.equals(source)) {
			
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
	 * build xpath from start to end while skipping the start node
	 * start can be IH or any other predecessor of end
	 * @param start
	 * @param end
	 * @return
	 */
	private String getXPathBetween (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> start, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> end) {
		String xpath = "";
		
		if(start.equals(end)) {
			return "";
		}
		
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> predecessor = getShallowestPredecessor(end, start);
		
		// if the only parent of end is IH
		// then we build .../[XPATH(end)]
		if(predecessor.equals(source) || predecessor.equals(start)) {
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
	 * Build the TPQs of each LCA and bound node in the source
	 * These TPQs are used as xpath commands during target traversal
	 */
	private void buildTPQs (boolean verbose) {
		
		// BFS traverse the source model
	
		LinkedList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> queue = new LinkedList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		queue.add(source);
		while(queue.size() != 0) {
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> currentNode = queue.poll();
			
			if(currentNode.equals(source)) {
				tpqReg.addTPQ(currentNode, getXPath(source));
			}
			
			// for each LCA or bound node, invent a variable and build a XPath
			if(currentNode.hasLabel(LCA) || currentNode.hasLabel(BOUND)) {
				String xpath = "";
				
				// if the node is IH
				if(currentNode.equals(source)) {
					xpath = getXPath(currentNode);
				}
				
				// for all other nodes
				else {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> closestLCA = getClosestLCA(currentNode);				
					// if the current node does not have any predecessor LCA: build xpath from root to it while ignoring the root
					if(closestLCA == null) {
						// This will miss the xpath of the source
						// xpath = getXPathBetween(source, currentNode);
//						xpath = getXPath(source) + getXPathBetween(source, currentNode);
						xpath = tpqReg.getTPQ(source) + getXPathBetween(source, currentNode);
					}				
					// if the current node has any predecessor LCA, it must have been processed already
					// build the xpath from the closest LCA while ignoring it
					else {
						xpath = getXPathBetween(closestLCA, currentNode);					
					}
				}
								
				tpqReg.addTPQ(currentNode, xpath);
			}		
			
			// add all children of the current node to queue
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : currentNode.getChildren()) {
				if(!child.hasLabel(VISITED)) {					
					child.addLabel(VISITED);					
					queue.add(child);
				}				
			}						
		}
		
		if(verbose) {
			System.out.println("\n3. TPQ:\n");
			HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> tpqs = getTpqs();
			Iterator<Entry<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String>> tpqIter = tpqs.entrySet().iterator();
			while(tpqIter.hasNext()) {				
				Map.Entry<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> pair = 
						(Map.Entry<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String>) tpqIter.next();
				GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode = pair.getKey();
				String xpath = pair.getValue();				
				String ss = sourceNode.toString();
				if(ss.contains("\n"))
					ss = ss.substring(0, ss.indexOf("\n"));
				System.out.println("source:\t" + ss);
				System.out.println("xpath: " + xpath);
			}
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
		if(!sourceNode.hasLabel(LCA)) {
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
	 * Get the LCA-correspondence of the target node starting from the given sourceNode as a root
	 * A source node is the LCA-correspondence of a target node, if the source node LCA-covers all bound descendants of the target node and also the target node if itself is bound 
	 * @param target
	 * @return
	 */
	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getLCA (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode) {
		
		// if the target node is not bound and is a leaf
		if(!bindReg.isNodeBound(targetNode) && targetNode.isLeaf())
			return null;
		
		// get all target descendants-or-self
		List<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> allDescendantsOrSelf = targetNode.getDescendantOrSelf();
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
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode : source.getDescendantOrSelf()) {
			
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
	 * Compose the final data exchange program
	 * @param datafile the source aml data file
	 * @param xqueryRootPath the xquery root path, supposed to be "CAEXFile"
	 * @param caexLibPath the caex data query xquery lib 
	 * @param caexExchangeLibPath the caex data exchange xquery lib
	 * @return
	 */
	public String getProgram (String datafile, String caexLibPath) {
		
		if(!isInitialized) {
			QueryWarning warning = new QueryWarning(new Throwable().getStackTrace(), this.getClass().toString() + " not initialized yet!");
			warning.print();
			return null;
		}
		
		String docRef = "doc(\"" + datafile + "\")/";
		String rootRef = docRef + "CAEXFile/InstanceHierarchy[@Name=\"RobotCell\"]";		
		String module1 = "declare namespace uuid = 'java.util.UUID';";
		String module2 = "import module namespace caex215 = \"http://ipr.kit.edu/caex\" at \"" + caexLibPath + "\";\n";

		XQueryVariable rootVar = new XQueryVariable("root");
		varReg.addVariable(source, rootVar);
		LetClause rootLet = new LetClause(rootVar, rootRef);
//		XQueryVariable idVar = new XQueryVariable("id");
//		LetClause idLet = new LetClause(idVar, "fn:random-number-generator()"); 				
		ReturnClause rootRet = new ReturnClause();

//		NestedClause query = getXQuery();
//		rootRet.addClause(query);
		
		AbstractTransformation rootTransformation = new NewElementTransformation(target, source, varReg.getVariable(source), varReg, bindReg);
		rootRet.addClause(rootTransformation.getXQueryClause());		
		
		NestedClause program = new NestedClause();
		program.addClause(new StringClause(module1));
		program.addClause(new StringClause(module2));
		program.addClause(rootLet);
//		program.addClause(idLet);
		program.addClause(rootRet);
		return program.toString();
	}


	/**
	 * @return the tpqs
	 */
	public HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> getTpqs() {
		return tpqReg.getTPQs();
	}

	/**
	 * @return the bReg
	 */
	public BindingRegister getBindingRegister() {
		return bindReg;
	}

}
