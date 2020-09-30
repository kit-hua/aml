package clause;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import CAEX215.AttributeType;
import CAEX215.InstanceHierarchyType;
import concept.model.AMLQueryConfig;
import concept.model.AMLTargetConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import exporter.AMLExporter;
import generator.BindingRegister;
import generator.TPQRegister;
import generator.XQueryVariableRegister;
import xquery.XQueryVariable;

/**
 * Class for target nodes which are not bound, i.e., new elements
 * @author Yingbing Hua, IAR-IPR, KIT
 * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
 */
public class NewElementTransformation extends AbstractTransformation{
	
	private AMLExporter exporter;
	
	public NewElementTransformation(GenericTreeNode<GenericAMLConceptModel<AMLTargetConfig>> targetNode,
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> sourceRoot,
			XQueryVariable baseVariable,
			XQueryVariableRegister varReg, BindingRegister bindReg) {		
		super(targetNode, sourceRoot, baseVariable, varReg, bindReg);

		try {
			exporter = new AMLExporter();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * invent a ID string
	 * TODO: make sure of uniqueness across the target document
	 * TODO: this is wrong, ID invention shall happen within XQuery, the new ID here will be used for each matched object!
	 * @return
	 */
	private String inventID () {
//		UUID uuid = UUID.randomUUID();
//		return uuid.toString();
//		return "caex215:createID ()";
		return "uuid:randomUUID()";
//		return "caex215:generate-uuid-v4($id?next())";
//		return "caex215:createID2 (fn:random-number-generator(), 1)";
//		return "caex215:generate-uuid-v4-multiple(fn:random-number-generator(), 1)";
	}
	
	@Override
	public NestedClause getXQueryClause() {
		NestedClause clause = new NestedClause();
		
		// If this is a simple new element, export its XML code
		if(isSimpleNewElement(targetNode)) {
			try {
				String exported = exporter.elementToString(targetNode.data.getObj());
				exported = exported.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
				clause.addClause(new StringClause(exported));
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// If this is a complex new element with bound descendants
		else {
			
			/* 1. Build default XML tags */
			String start = "", end = "";
			if(targetNode.data.getObj() instanceof InstanceHierarchyType) {
				start = "<InstanceHierarchy Name=\"RobotCell\">\n{";
				end = "}\n</InstanceHierarchy>";
			}
			else {
				String name = "IE";
				start = "<InternalElement Name=\"" + name + "\" ID=\"{" + inventID() + "}\">\n{";
				end = "}\n</InternalElement>";
			}			
			clause.addClause(new StringClause(start));
						
			/* 3. handle external children */
			// New element transformation does not need to handle own direct children besides simple new elements
			// Because new element nodes do not have theoretically dependent direct children
			// So to handle all children, we always need to consider the hierarchy of their context nodes
			// Therefore always use getForReturnClausesForExternalChildren() for all children

			int nrExternalTargetChildren = externalTargetChildren.size();
			if(nrExternalTargetChildren != 0) {
				NestedClause externalChildrenClause = getClausesForExternalChildren(this.externalTargetChildren);
				clause.addClause(externalChildrenClause);
			}
			
			/* 4. handle all simple new element children */
			
			NestedClause newElementChildrenClause = getClauseForSimpleNewElementChildren();
			if(!newElementChildrenClause.getClauses().isEmpty()) {				
				clause.addClause(new StringClause(","));
				clause.addClause(newElementChildrenClause);
			}
			
			clause.addClause(new StringClause(end));
		}
					
		return clause;
	}
	
	
}
