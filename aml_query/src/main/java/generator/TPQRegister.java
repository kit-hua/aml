package generator;

import java.util.HashMap;

import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.model.IAMLConceptConfig;
import concept.tree.GenericTreeNode;

/**
 * Register for all Tree Pattern Queries (TPQs) in the source model
 * Each bound node or LCA has a TPQ, represented by its xpath string
 * @author Yingbing Hua, IAR-IPR, KIT
 * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
 */
public class TPQRegister {

	private HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> tpqs;
	
	public TPQRegister () {
		this.tpqs = new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String>();
	}
	
	public void addTPQ (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode, String xpath) {
		tpqs.put(sourceNode, xpath);
	}
	
	public String getTPQ (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceNode) {		
		return tpqs.get(sourceNode);
	}
	
	public HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> getTPQs (){
		return tpqs;
	}
	
}
