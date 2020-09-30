/**
 * 
 */
package generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CAEX215.AttributeType;
import concept.model.AMLQueryConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import xquery.XQueryVariable;
import xquery.XQueryCommand;
import xquery.XQueryFor;
import xquery.XQueryReturn;

/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 * 
 */
public class XQueryGenerator {

	private String docRef;
	private String rootRef;
	private String caexLibPath;
	
	private String postfixObj = ""; 
	private String postfixAttr = ""; 
	
	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> root;
	private XPathGenerator xpathGenerator = new XPathGenerator();
	
	
	private int xmlTagIdx = 0;
	
	/**
	 * @param root the root object of the AMLQuery model
	 * @param filename the name of the data file
	 * @param rootPath the path of the target AML library, e.g. CAEXFile/InstanceHirarchy
	 */
	public XQueryGenerator(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> root, String datafile, String xqueryRootPath, String caexLibPath) {
		this.docRef = "doc(\"" + datafile + "\")/";
		this.rootRef = docRef + xqueryRootPath;
		this.root = root;
		this.caexLibPath = caexLibPath;
	}
	
	/**
	 * @param semantically whether the generation of XQuery shall use semantic class matches
	 * @param simple whether the generated XQuery shall use @ID and @Value for simplifying the query results
	 */
	public String translateToXQuery(boolean semantically, boolean withLink, boolean simple) {
		if(simple) {
			postfixObj = "/@Name";
			postfixAttr = "/Value";
		}
		
		return translateToXQuery(semantically, withLink);			
	}
	
	
	private String translateToXQuery(boolean semantically, boolean withLink) {
		
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> returnedNodes = AMLQueryUtils.getReturnedNodes(this.root);
		String query = "";		
		
		// import modules
		String modules = "";
//		File caexLib = new File("src/main/resources/caex.xqy");
//		String caexLibPath = "\"" + caexLib.getAbsolutePath() + "\"";
	
		modules += "import module namespace caex215 = \"http://ipr.kit.edu/caex\" at \"" + caexLibPath + "\";\n\n";
				
		// if no node shall be returned, then there can be no critical nodes, and it is about checking the existence of the tree in data
		if(returnedNodes.size() == 0) {
			query = "count(" + docRef + AMLQueryUtils.format(xpathGenerator.getXPath(this.root, semantically, true)) + ")";
		}
		
		// if only one node shall be returned, then there can be no critical nodes
		else if(returnedNodes.size() == 1) {
			String xpath = AMLQueryUtils.format(xpathGenerator.getXPathBetween(this.root, returnedNodes.get(0), semantically, withLink));
			query = rewriteAndAppendPostFix("$root", xpath, returnedNodes.get(0));
		}
		
		// if more than one nodes shall be returned
		else {									
			XQueryCommand xquery = translateToXQueryRecursive(this.root, null, 0, semantically, withLink);
			query = xquery.getExpression();
		}	
				
		
		String closures = "";
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node : this.root.getDescendantOrSelf()) {
			if(xpathGenerator.getClosureFunction(node) != null)
				closures += xpathGenerator.getClosureFunction(node) + "\n";
		}
		
		String rootLet = "let $root := " + rootRef + "\n";
		String rootRet = "return (\n\n" + query + "\n\n)";
		
		String program = modules + closures + rootLet + rootRet;				
		
//		return program;
//		return program.replaceAll("descendant-or-self::", "/");
		return program.replaceAll("descendant-or-self::", "descendant::");
	}
	
	/**
	 * append a post fix to the given xpath based on the CAEX type of the node
	 * @param xpath
	 * @param node
	 * @return
	 */
	private String appendPostfix (String xpath, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		if(node.data.getObj() instanceof AttributeType)
			return xpath + postfixAttr;
		else
			return xpath + postfixObj;
	}
	
	/**
	 * rewrite an xpath expression from the node by:
	 * 	- handling transitive closure first using: transitiveClosureToXQuery(xpath)
	 *  - add the prefix
	 * @param prefix
	 * @param xpath
	 * @param node
	 * @return
	 */
	private String rewrite (String prefix, String xpath, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		String xpath2 = xpath;
		
		if(((AMLQueryConfig) node.data.getConfig()).isTransitive())
			xpath2 = transitiveClosureToXQuery(xpath);
		
		if(prefix!="" && !prefix.endsWith("/"))
			xpath2 = prefix + "/" + xpath2;
		else
			xpath2 = prefix + xpath2;
		
		return xpath2;
	}
	
	/**
	 * rewrite an XPath expression and add the post fix 
	 * @param prefix
	 * @param xpath
	 * @param node
	 * @return
	 */
	private String rewriteAndAppendPostFix (String prefix, String xpath, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		String xpath2 = rewrite(prefix, xpath, node);
		return appendPostfix(xpath2, node);
	}
	
	@Deprecated
	private XQueryCommand translateToXQueryRecursive2(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, XQueryVariable aux, int nestDepth, boolean semantically, boolean withLink) {
		
		/*
		 * 1. the node is a distinguished node
		 * 		* construct a return clause: getXPath(node)
		 * 2. the node has descendant distinguished nodes
		 * 	 - 2.1 the node has exactly one distinguished node
		 * 		* construct a return clause with the XPath from this node to the distinguished one: getXPathBetween(node, distinguished)
		 * 			* consider all conditional nodes as predicate 
		 *   - 2.2 the node has more than one distinguished nodes	
		 *   		2.2.1. for each child that is a distinguished node: generate a return clause which is described first with a for, so that sibling constraints are taken into account
		 *   			* construct a for clause with a new auxiliary node and the XPath of the child node: for $newAux in $aux/getXPath(child)
		 *   			* construct a return clause for the new auxiliary node: return $newAux
		 *   			* add the for to the child for
		 *   			* add the return to the child return
		 *   		2.2.2. for each child that contains one distinguished node
		 *   			* construct a for clause with a new auxiliary node and the XPath of the distinguished node starting from the child: for $newAux in $aux/getXPathBetween(child, distinguished)
		 *   			* construct a return clause for the new auxiliary node: return $newAux
		 *   			* add the for to the child for
		 *   			* add the return to the child return 
		 *   		2.2.3. for each child that contains several distinguished nodes
		 *   			* get the critical nodes under child (incl. child)
		 *   			* for each critical nodes at the highest level: cn1, cn2, ... with depth(cn1) = depth(cn2) = ... < depth(cnx), for all x
		 *   				* construct a for clause with a new auxiliary node and the open-ended XPath of the critical node starting from the child: for $newAux in $aux/getXPathOpenEnded(child, cn)
		 *   				* recursive call on the critical node as: translateToXQueryRecursive(cn, newAux, nestDept+1, semantically, withLink)
		 *   				* add the for to the child for
		 *   				* add the result of the recursive call as a nested query to the child return 
		 * 	construct a child query using child for and child return
		 *  add the child query as a ...
		 */
		
		XQueryCommand query = new XQueryCommand();
		XQueryFor nodeFor = new XQueryFor(nestDepth);
		XQueryReturn nodeRet = new XQueryReturn(nestDepth);
		
		// distinguished and critical children
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> distinguishedNodes = AMLQueryUtils.getReturnedNodes(node);
		
		// 1. the current node need to be returned
		if(distinguishedNodes.contains(node)) {
			if(node.getParent() == null) {
				XQueryVariable newAux = new XQueryVariable();
				String xpath = xpathGenerator.getXPath(node, semantically, withLink);
//				String xpath2 = rewrite(rootRef, xpath, node);
//				nodeFor.addData(newAux.getName(), rewrite(rootRef, xpath, node));
				nodeFor.addData(newAux.getName(), rewrite("$root", xpath, node));
				nodeRet.addDirectReturn(appendPostfix(newAux.getExpression(), node));
			}
			else {
				String xpath = xpathGenerator.getXPath(node, semantically, withLink);
//				String xpath2 = rewrite("", xpath, node);
				nodeRet.addDirectReturn(aux.getName() + "/" + rewriteAndAppendPostFix("", xpath, node));
				query.setReturn(nodeRet);
			}						
		}		
		
		// 2. if the node has no descendant distinguished nodes: something wrong
		if (distinguishedNodes.isEmpty()) {
			return null;
		}
			
		// 2.1. the node has one descendant distinguished node
		if(distinguishedNodes.size() == 1) {
			String xpath = xpathGenerator.getXPathBetween(node, distinguishedNodes.get(0), semantically, withLink);			
			nodeRet.addDirectReturn(aux.getName() + "/" + rewriteAndAppendPostFix("", xpath, node));
		}
		
		// 2.2. the node has more than one descendant distinguished node
		if(distinguishedNodes.size() > 1) {
			if(node.getParent() == null) {
				aux = new XQueryVariable();
				String xpath = xpathGenerator.getXPathExceptCriticalDescendants(node, semantically, withLink);
//				nodeFor.addData(aux.getName(), rewrite(rootRef, xpath, node));
				nodeFor.addData(aux.getName(), rewrite("$root", xpath, node));
			}
			
			XQueryCommand childQuery = new XQueryCommand();
			XQueryReturn childRet = new XQueryReturn(nestDepth+1);
			XQueryFor childFor = new XQueryFor(nestDepth+1);
			
			// for each child
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : node.getChildren()) {
				
				List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> childDistinguishedNodes = AMLQueryUtils.getReturnedNodes(child);
				
				// 2.2.1 the child is a distinguished node: add to return
				if(distinguishedNodes.contains(child)) {
					
					// make a for clause in case multiple distinguished children present
					XQueryVariable childAux = new XQueryVariable();
					String childExpr = xpathGenerator.getXPath(child, semantically, withLink);
					String childPath = aux.getExpression() + "/" + rewrite("", childExpr, child);  
					childFor.addData(childAux.getName(), childPath);
					
					// add a return clause for the child
					childRet.addDirectReturn(appendPostfix(childAux.getExpression(), child));
				}
				
				// 2.2.2 the child contains one distinguished node
				else if(childDistinguishedNodes.size() == 1) {
					// make a for clause in case multiple distinguished children present
					XQueryVariable childAux = new XQueryVariable();
					String childExpr = xpathGenerator.getXPathBetween(child, childDistinguishedNodes.get(0), semantically, withLink);
					String childPath = aux.getExpression() + "/" + rewrite("", childExpr, child);  
					childFor.addData(childAux.getName(), childPath);
					
					// add a return clause for the distinguished node
					childRet.addDirectReturn(appendPostfix(childAux.getExpression(), childDistinguishedNodes.get(0)));
				}
				
				// 2.2.3 the child contains several distinguished nodes
				else if(childDistinguishedNodes.size() > 1) {
					
					List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> criticalNodes = AMLQueryUtils.getCommonParents(childDistinguishedNodes);
					criticalNodes.sort(Comparator.comparing(GenericTreeNode::getDepth));
					
					// find the first critical node under the current node
					Iterator<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> iterator = criticalNodes.iterator();
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> nextCritical = iterator.next();
					Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> criticalsAtSameDepth = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
					criticalsAtSameDepth.add(nextCritical);
					int depth = nextCritical.getDepth();					
					while(iterator.hasNext()) {
						GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> overNext = iterator.next();
						if(overNext.getDepth() == depth) {
							criticalsAtSameDepth.add(overNext);
						}else
							break;
					}
//					
					for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> criticalNode : criticalsAtSameDepth) {
						// make a for clause in case multiple distinguished children present
						XQueryVariable criticalAux = new XQueryVariable();
						String criticalExpr = xpathGenerator.getXPathBetweenOpenEnded(child, criticalNode, semantically, withLink);
						String criticalPath = aux.getExpression() + "/" + rewrite("", criticalExpr, criticalNode);
						childFor.addData(criticalAux.getName(), criticalPath);
						
						// add nested return for the child
						XQueryCommand nestedQuery = translateToXQueryRecursive2(criticalNode, criticalAux, nestDepth+1, semantically, withLink);
						childRet.addNestedReturn(nestedQuery);
					}
				}								
			}//end:child						
			
			if(!childFor.isEmpty() && node.getParent() == null) {
				childQuery.setFor(childFor);
				childQuery.setReturn(childRet);
				nodeRet.addNestedReturn(childQuery);
				if(!nodeFor.isEmpty()) {
					query.setFor(nodeFor);
				}
				
				if(nodeRet.getExpression() != null)
					query.setReturn(nodeRet);
			}
			
			if(!childFor.isEmpty() && node.getParent() != null) {
				query.setFor(childFor);
				query.setReturn(childRet);
			}
			
			return query;
		}
		
		// TODO: should not happen
		return null;
	}
	
	/**
	 * translate an nAQL query into an XQuery program
	 * @param node
	 * @param aux
	 * @param nestDepth
	 * @param semantically
	 * @param withLink
	 * @return
	 */
	private XQueryCommand translateToXQueryRecursive(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, XQueryVariable aux, int nestDepth, boolean semantically, boolean withLink) {
		
		/*
		 * 1. the node is a distinguished node
		 * 		* construct a return clause: getXPath(node)
		 * 2. the node has descendant distinguished nodes
		 * 	 - 2.1 the node has exactly one distinguished node
		 * 		* construct a return clause with the XPath from this node to the distinguished one: getXPathBetween(node, distinguished)
		 * 			* consider all conditional nodes as predicate 
		 *   - 2.2 the node has more than one distinguished nodes	
		 *   		* get the lowest common ancestor (LCA) of all distinguished nodes
		 *   		* make an auxiliary node (XQuery variable) for the LCA: $LCA
		 *   		* construct a for clause as: for $LCA in $aux/getXPathOpenEnded(node, LCA). This will consider all simple nodes under the LCA   		
		 *   		* for each LCA.child with distinguished descendants 
		 *   			* recursive call on child as: translateToXQueryRecursive(child, $LCA, nestDept+1, semantically, withLink)
		 *   			* add the for and return of the result to nestedQuery
		 *   		* add nestedQuery to return
		 *  construct XQuery with existing for and return
		 */
		
		String prefix = "";
		if(node.getParent() == null)
//			prefix = rootRef;
			prefix = "$root";
		
		XQueryCommand query = new XQueryCommand();
		XQueryFor nodeFor = new XQueryFor(nestDepth);
		XQueryReturn nodeRet = new XQueryReturn(nestDepth);
		
		// distinguished and LSP
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> distinguishedNodes = AMLQueryUtils.getReturnedNodes(node);
		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> lowestCommonAncestor = null;
		if(!distinguishedNodes.isEmpty()) {
			lowestCommonAncestor = GenericTreeNode.getLowestCommonAncestor(distinguishedNodes);
		}
		
		XQueryVariable criticalAux = null;
		// 1. the current node need to be returned
		if(distinguishedNodes.contains(node)) {
			String xpath = xpathGenerator.getXPath(node, semantically, withLink);
			String xpath2 = rewrite(prefix, xpath, node);
			// aux = null means root node
			if(aux==null) {
				aux = new XQueryVariable();				
				nodeFor.addData(aux.getName(), xpath2);
				nodeRet.addDirectReturn(appendPostfix(aux.getExpression(), node));
			}
			else {
				criticalAux = new XQueryVariable();
				nodeFor.addData(criticalAux.getName(), aux.getExpression() + "/" + xpath2);
				nodeRet.addDirectReturn(appendPostfix(criticalAux.getExpression(), node));				
			}		
			distinguishedNodes.remove(node);
		}		
		
		// 2. if the node has no descendant distinguished nodes: something wrong
			
		// 2.1. the node has one descendant distinguished node
		if(distinguishedNodes.size() == 1 && !distinguishedNodes.contains(node)) {
			String xpath = xpathGenerator.getXPathBetween(node, distinguishedNodes.get(0), semantically, withLink);
			
			// won't happen, since all recursive calls have already aux and the root contains more than one distinguished nodes
//			if(aux == null) {
//				aux = new AuxiliaryXQueryNode();				
//			}
						
//			AuxiliaryXQueryNode newAux = new AuxiliaryXQueryNode();		
			if(criticalAux == null)
				criticalAux = new XQueryVariable();
			
			nodeFor.addData(criticalAux.getName(), aux.getExpression() + "/" + rewrite(prefix, xpath, node));
			nodeRet.addDirectReturn(appendPostfix(criticalAux.getExpression(), node));
		}
		
		// 2.2. the node has more than one descendant distinguished node
		if(distinguishedNodes.size() > 1) {
			
			// now, the current node has some distinguished nodes, so the LCA must be under or equals it
			// in both cases, we want to make an auxiliary node for the LCA
			// if aux is null, then the node must be root. So we make an auxiliary node using getXPathBetweenOpenEnded(node, LCA)
			// other wise, we make a new one which refers to the aux using $aux/getXPathBetweenOpenEnded(node, LCA)		
			String criticalPath = xpathGenerator.getXPathBetweenOpenEnded(node, lowestCommonAncestor, semantically, withLink);
			String criticalPathExt = rewrite(prefix, criticalPath, node);
			
			// build a for clause for the LCA: for $LCA in $aux/criticalPath
			if(nodeFor.isEmpty()) {
				if(criticalAux == null)
					criticalAux = new XQueryVariable();
				
				if(aux == null) {
					nodeFor.addData(criticalAux.getName(), criticalPathExt);
				}else
					nodeFor.addData(criticalAux.getName(), aux.getExpression()+"/"+criticalPathExt);
			}
						
			// now we have the auxiliary node for the LCA, we want to analyze the children of the LCA via the auxiliary node
			XQueryCommand childQuery = new XQueryCommand();
			XQueryFor childFor = new XQueryFor(nestDepth+1);
			XQueryReturn childReturn = new XQueryReturn(nestDepth+1);
			XQueryCommand partChildQuery = null;
			// traverse children of the LCA
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : lowestCommonAncestor.getChildren()) {
				
				// check if the child is critical: it is distinguished or the ancestor of any distinguished nodes
				boolean critical = false;
				for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> descendant : child.getDescendantOrSelf()) {
					if(((AMLQueryConfig) descendant.data.getConfig()).isDistinguished()) {
						critical = true;
						break;
					}
				}
				
				// if child is not critical, ignore it
				if(!critical)
					continue;
				
				partChildQuery = translateToXQueryRecursive(child, criticalAux, nestDepth+1, semantically, withLink);	
				if(partChildQuery.getFor() != null)
					childFor.addAll(partChildQuery.getFor().getData());
			
				String partRet = partChildQuery.getReturn().getExpression();
				childReturn.addDirectReturn(partRet);	
			}
			
			boolean xmlTag = false;
			Pattern pattern = Pattern.compile("\\$n\\d+", Pattern.DOTALL);
			for(String dr : childReturn.getDirectReturns()) {
				Matcher matcher = pattern.matcher(dr);
				if(matcher.matches()) {
					xmlTag = true;
					break;
				}
			}
			
			if(xmlTag) {
				childReturn.setXMLTag("r" + String.valueOf(++xmlTagIdx)); 
			}
			
			
			childQuery.setFor(childFor);
			childQuery.setReturn(childReturn);
			nodeRet.addNestedReturn(childQuery);
		}
			
		if(!nodeFor.isEmpty()) {
			query.setFor(nodeFor);
		}			
		if(nodeRet.getExpression() != null)
			query.setReturn(nodeRet);
		
		return query;
	}

	/**
	 * rewrite the transitive closure in an XPath
	 */
	private String transitiveClosureToXQuery(String tcPath) {
		String tcExpr = "";
		String head = "";
		int lastSlash = tcPath.lastIndexOf("/");
		if(lastSlash != -1)
			head = tcPath.substring(0, lastSlash);
		
		int lastBracket = tcPath.lastIndexOf("[");
		int end = tcPath.length()-1;
		
		if(lastBracket != -1) {
			tcExpr = tcPath.substring(lastBracket+1, end);
		}
		else
			tcExpr = tcPath;
		
		// remove *, [, ] from tcExpr
//		tcExpr = tcExpr.substring(2, end);
		if(head.isEmpty())
			return head + "*/" + tcExpr;
		else
			return head + "/*/" + tcExpr;
	}

}
