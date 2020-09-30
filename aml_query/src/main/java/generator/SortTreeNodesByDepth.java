package generator;

import java.util.Comparator;

import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.model.IAMLConceptConfig;
import concept.tree.GenericTreeNode;

public class SortTreeNodesByDepth<T extends IAMLConceptConfig> implements Comparator<GenericTreeNode<GenericAMLConceptModel<T>>>{

	/**
	 * compare two tree nodes in ascending order of depth
	 * The node with lower depth, i.e., in higher part of the tree is in front
	 */
	@Override	
	public int compare(GenericTreeNode<GenericAMLConceptModel<T>> o1, GenericTreeNode<GenericAMLConceptModel<T>> o2) {
		return o1.getDepth() - o2.getDepth();
	}
	
}
