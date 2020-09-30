package clause;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import CAEX215.AttributeType;
import clause.AbstractTransformation.TargetType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import xquery.XQueryVariable;

public interface ITransformation {

	/**
	 * Get the full nested xquery clause for this transformation w.r.t. to current target node
	 * The clause contains the identity transformation clause and its nested FOR-RETURN loops for recursive handling of special children, including:
	 * - for a renaming node:
	 * 		- renaming child
	 * - for a full copy node:
	 * 		- all bound children including renaming
	 * 		- new element child with bound descendant
	 * - for a projection node:
	 * 		- renaming child
	 * 		- non-projected bound child (projected bound child are covered within IF-THEN-ELSE)
	 * 		- new element child with bound descendant   
	 * @return
	 */
	public NestedClause getXQueryClause();
	
}
