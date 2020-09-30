package generator;

import java.util.HashSet;
import java.util.Set;

import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.model.IAMLConceptConfig;
import concept.tree.GenericTreeNode;

public class BindingRegister {
	
	private Set<Binding> bindings;
	
	public BindingRegister() {
		this.bindings = new HashSet<Binding>();
	}
	
	public void addBinding (Binding binding) {
		this.bindings.add(binding);
	} 
	
	/**
	 * Get the bound source node of the given target node
	 * @param targetNode
	 * @return
	 */
	public GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getBoundSourceNode (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode) {		
		for (Binding binding : bindings) {
			if(binding.getTarget().equals(targetNode)) {
				return binding.getSource();
			}
		}			
		
		return null;
	}
	
	/**
	 * Get the bound target node of the given source node
	 * @param sourceNode
	 * @return
	 */
	public Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> getBoundTargetNodes (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode) {
		Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> targetNodes = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		for (Binding binding : bindings) {
			if(binding.getSource().equals(sourceNode)) {
				targetNodes.add(binding.getTarget());
			}
		}
		
		return targetNodes;
	}		
	
	/**
	 * Get all bound source nodes
	 * @return
	 */
	public Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> getBoundSourceNodes () {
		Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> sourceNodes = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>>();
		for (Binding binding : bindings) {		
			sourceNodes.add(binding.getSource());			
		}
		
		return sourceNodes;
	}
	
	/**
	 * Get all bound target nodes
	 * @return
	 */
	public Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> getBoundTargetNodes () {
		Set<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>> targetNodes = new HashSet<GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>>>();
		for (Binding binding : bindings) {			
			targetNodes.add(binding.getTarget());			
		}
		
		return targetNodes;
	}		
	
	/**
	 * whether the give node is bound
	 * @param node
	 * @return
	 */
	public <T extends IAMLConceptConfig> boolean isNodeBound (GenericTreeNode<GenericAMLConceptModel<T>> node) {
		for(Binding binding : bindings) {
			if(node.data.getConfig() instanceof AMLTargetConfig) {
				if(binding.getTarget().equals(node)) {
					return true;
				}	
			}
			else if (node.data.getConfig() instanceof AMLTargetConfig) {
				if(binding.getSource().equals(node)) {
					return true;
				}
			}
			else
				return false;
		}
		return false;
	}	
	
	/**
	 * Whether the give node has a bound descendant or it is itself a bound node
	 * @param node
	 * @return
	 */
	public boolean isNodeOrDescendantBound (GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> node) {		
		for(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> descendantOrSelf : node.getDescendantOrSelf()) {
			if(isNodeBound(descendantOrSelf)) {
				return true;
			}
		}		
		return false;
	}
	
	public boolean isEmpty () {
		return bindings.isEmpty();
	}

	/**
	 * @return the bindings
	 */
	public Set<Binding> getBindings() {
		return bindings;
	}
}
