package serializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import concept.tree.GenericTreeNode;

/**
 * parse a multi-returned query result into groups of objects
 * We use the structure of the query model to formulate results in AML  
 * @author 
 *
 */
public class BaseXResultParser {
	
	private final String PREFIX = "r";
	private Set<Integer> visited = new HashSet<Integer> ();	
	
	public BaseXResultParser() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * whether a returned XML node is a place holder, i.e. something as <n1>...</n1>
	 * @param node
	 * @return
	 */
	private boolean isPlaceholder (Node node) {
		String name = node.getNodeName();
		if(name.startsWith(this.PREFIX)) {			
			return true;
		}
		return false;
	}
	
	/**
	 * get the place holder idx of a place holder node
	 * @param node
	 * @return
	 */
	private int getIdx (Node node) {
		String name = node.getNodeName();
		if(name.startsWith(this.PREFIX)) {
			return Integer.valueOf(name.substring(1));
		}else
			return -1;
	}
	
	/**
	 * parse query results to tree structures
	 * each query result is a tree
	 * @param objects XML nodes
	 * @return a list of trees
	 */
	public List<GenericTreeNode<List<Node>>> toTrees (Object[] objects) {
		
		List<GenericTreeNode<List<Node>>> trees = new ArrayList<GenericTreeNode<List<Node>>>();
		List<List<Node>> grps = new ArrayList<List<Node>>();
		parse(objects, grps);
		
		for(List<Node> grp : grps) {
			GenericTreeNode<List<Node>> tree = toTreeRec(grp);
			trees.add(tree);
		}
		
		return trees;
	}
	
	/**
	 * parse a list of objects (XML nodes) into groups of XML nodes
	 * @param objects
	 * @param grps
	 */
	private void parse (Object[] objects, List<List<Node>> grps) {
		
		List<Node> grp = new ArrayList<Node>();
		boolean added = false;
		for(int i = 0; i < objects.length; i++) {
			Node node = (Node) objects[i];					
			if(isPlaceholder(node)) {
				int idx = getIdx(node);
				if(!visited.contains(idx)) {
					visited.add(idx);
					grp.add(node);
				}
				else {
					grps.add(grp);
					added = true;
					visited.clear();
					List<Object> rest = new ArrayList<Object>();
					for(int j = i; j < objects.length; j++) {
						rest.add(objects[j]);
					}
					parse(rest.toArray(), grps);
					i = i + rest.size();
				}
			}
			else {
				grp.add(node);
			}
		}
		
		if(!added)
			grps.add(grp);
	}
	
	/**
	 * recursive function to transform a group of XML nodes into a tree
	 * @param grp
	 * @return
	 */
	private GenericTreeNode<List<Node>> toTreeRec (List<Node> grp) {
		GenericTreeNode<List<Node>> grpTree = new GenericTreeNode<List<Node>>();
		grpTree.data = new ArrayList<Node>();				
		
		// a node is either a place holder or a caex obj
		for(Node node : grp) {			
			// if the node is a caex obj, add it as data of the grpTree
			if(!isPlaceholder(node)) {
				grpTree.data.add(node);
			}
			else {
				NodeList children = node.getChildNodes();	
				if(children != null && children.getLength() > 0) {
					List<Node> recChildren = new ArrayList<Node>();
					for(int i = 0; i < children.getLength(); i++) {
						if(!isPlaceholder(children.item(i)))
							grpTree.data.add(children.item(i));
						else
							recChildren.add(children.item(i));
					}
					
					if(!recChildren.isEmpty()) {
						List<List<Node>> subGrps = new ArrayList<List<Node>>();
						parse(recChildren.toArray(), subGrps);
						for(List<Node> subGrp : subGrps) {
							GenericTreeNode<List<Node>> subGrpTree = toTreeRec(subGrp);
							grpTree.addChild(subGrpTree);
						}	
					}					
				}
			}
		}
		
		return grpTree;
	}

	

}
