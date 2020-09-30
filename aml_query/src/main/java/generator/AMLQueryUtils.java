package generator;

import java.util.ArrayList;
import java.util.List;

import concept.model.AMLQueryConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;

public class AMLQueryUtils {
	
	/**
	 * get all distinguished nodes that are descendants-or-self of the give node
	 * @param node
	 * @return
	 */
	public static List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getReturnedNodes(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node){
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> returned = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> all = node.getDescendantOrSelf();
		
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> descendant : all) {
			if(((AMLQueryConfig) descendant.data.getConfig()).isDistinguished())
				returned.add((GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>) descendant);
		}
		
		return returned;
	}
	
	/**
	 * get all descendant nodes that are distinguished
	 * @param node
	 * @return
	 */
	public static List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getReturendDescendants(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node){
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> ret = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>(); 
		for (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> rn : getReturnedNodes(node)) {
			if(rn.getDepth() > node.getDepth())
				ret.add(rn);
		}
		return ret;
	}
	
	/**
	 * get the number of all descendant distinguished nodes
	 * @param node
	 * @return
	 */
	public static int getNumReturnedNodes(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		return getReturnedNodes(node).size();
	}
	
	
	/**
	 * Get the common parents of nodes in the given list.
	 * @param nodes
	 * @return
	 */
	public static List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getCommonParents(List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> nodes){
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> letter = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		int newNodes = 0;
		if(nodes.size() == 1)
			letter.addAll(nodes);
		else {
			for(int i = 0; i < nodes.size(); i++) {
				GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node1 = nodes.get(i);
				for(int j = 0; j < nodes.size(); j++) {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node2 = nodes.get(j);
					if(!nodes.get(i).equals(nodes.get(j))) {
						GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> commonParent = (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>) node1.getLowestCommonAncestor(node2);
						if(!letter.contains(commonParent)) {
							letter.add(commonParent);
							newNodes ++;
						}
					}
				}
			}
		}		
		
		if(newNodes != 0) {
			List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> recursive = getCommonParents(letter);
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node : recursive) {
				if(!letter.contains(node))
					letter.add(node);
			}
		}
					
		return letter;
	}
	
	
	/**
	 * get all critical descendants of the given node
	 * a critical descendant is either a distinguished node or an ancestor of an distinguished node
	 * @param node
	 * @return
	 */
	public static List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getCriticalDescendants(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node){
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> ret = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> returnedNodes = getReturnedNodes(node);
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> criticalNodes = getCommonParents(returnedNodes);
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> cn : criticalNodes) {
			if(cn.getDepth() > node.getDepth())
				ret.add(cn);			
		}
		return ret;
	}
	
	
	/**
	 * get number of all critical descendants of the given node
	 * a critical descendant is either a distinguished node or an ancestor of an distinguished node
	 * @param node
	 * @return
	 */
	public static int getNumCriticalDescendants(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		return getCriticalDescendants(node).size();
	}
	
	
	/**
	 * get the tree depth of the next critical descendant
	 * a critical descendant is either a distinguished node or an ancestor of an distinguished node
	 * @param node
	 * @return
	 */
	public static int getDepthOfNextCriticalNode (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		int dNode = node.getDepth();
		int next = Integer.MAX_VALUE;
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> cn : getCriticalDescendants(node)) {
			int dcn = cn.getDepth();
			if(dcn > dNode && dcn < next)
				next = dcn;
		}		
		
		if(next == Integer.MAX_VALUE)
			return -1;
		else
			return next;
	}
	
	
	/**
	 * get all transitive descendants
	 * @param node
	 * @return
	 */
	public static List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getTransNodes(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> ret = new ArrayList<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> allNodes = node.getDescendantOrSelf();
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> descendant : allNodes) {
			if(((AMLQueryConfig) node.data.getConfig()).isTransitive())
				ret.add((GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>) descendant);
		}		
		return ret;
	}
	
	
	/**
	 * rewrite an xpath expression for better reading
	 * TODO: not finished yet
	 * @param xpath
	 * @return
	 */
	public static String format (String xpath) {
		String[] tokens = xpath.split("\\[");
		
		for(int i = 0; i < tokens.length; i++) {
		}
		
		return xpath;
	}
}
