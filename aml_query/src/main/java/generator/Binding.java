package generator;

import CAEX215.AttributeType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;

/**
 * Class for a binding between one source model node and one target model node
 * A binding can exists between:
 *  - (IE, IE)
 *  - (EI, EI)
 *  - (Attr, Attr)
 * @author Yingbing Hua, IAR-IPR, KIT
 * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
 */
public class Binding {
	
	private GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source;
	private GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target;
	
	public Binding (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source, GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target) {
		this.source = source;
		this.target = target;
	}
	
	public boolean isAttributeRenaming () {
		if(source.data.getObj() instanceof AttributeType && target.data.getObj() instanceof AttributeType) {
			return true;
		}
		
		return false;
	}

	/**
	 * @return the source
	 */
	public GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> target) {
		this.target = target;
	}

}
