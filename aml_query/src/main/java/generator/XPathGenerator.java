package generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import CAEX215.AttributeType;
import CAEX215.AttributeValueRequirementType;
import CAEX215.ExternalInterfaceType;
import CAEX215.InterfaceClassType;
import CAEX215.InterfaceFamilyType;
import CAEX215.InternalElementType;
import CAEX215.InternalLinkType;
import CAEX215.NominalScaledTypeType;
import CAEX215.OrdinalScaledTypeType;
import CAEX215.RefSemanticType;
import CAEX215.RoleClassType;
import CAEX215.RoleFamilyType;
import CAEX215.RoleRequirementsType;
import CAEX215.SupportedRoleClassType;
import CAEX215.SystemUnitClassType;
import CAEX215.SystemUnitFamilyType;
import CAEX215.UnknownTypeType;
import CAEX215.util.AMLHelperFunctions;
import concept.model.AMLQueryConfig;
import concept.model.GenericAMLConceptModel;
import concept.tree.GenericTreeNode;
import concept.util.AMLLinkCollector;
import concept.util.EILinkRefSide;
import concept.util.GenericAMLConceptModelUtils;

public class XPathGenerator {
	
	private Map<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, List<String>> constraints = 
			new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, List<String>>();
	
	private Map<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, TransitiveClosure> closures =
			new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, TransitiveClosure>();
	
	private Map<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> xpathExpr = 
			new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String>();
	
	private Map<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> xpathNodeExpr = 
			new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String>();
	
	private Map<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String> xpathNodeExprSimple = 
			new HashMap<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>, String>();
	
	private static final String CAEX_XQUERY_LIB = "caex215";
	
	private static final String SUPPORTS_RC = CAEX_XQUERY_LIB + ":supportsRC";
	private static final String SUPPORTS_RC_NEGATED = CAEX_XQUERY_LIB + ":supportsRCNegated";
	
	private static final String REFS_IC = CAEX_XQUERY_LIB + ":refsIC";
	private static final String REFS_IC_NEGATED = CAEX_XQUERY_LIB + ":refsICNegated";
	
	private static final String REFS_RC = CAEX_XQUERY_LIB + ":refsRC";
	private static final String REFS_RC_NEGATED = CAEX_XQUERY_LIB + ":refsRCNegated";
	
	private static final String REQUIRES_RR = CAEX_XQUERY_LIB + ":requiresRR";
	private static final String REQUIRES_RR_NEGATED = CAEX_XQUERY_LIB + ":requiresRCNegated";
	
	//TODO: need to add negated attributes - negated data range as DL
	
	public static final String DESCENDANT = "descendant-or-self::";	
	
	//TODO: somehow not good
	// this is for computing link constraints for the EIs
//	private CAEXFileType queryAML;
		
	public XPathGenerator () {	
	}
	
//	public XPathGenerator (CAEXFileType queryAML) {
//		this.queryAML = queryAML;
//	}
	
	private boolean isNodeConstraintHandled (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		return this.constraints.containsKey(node);
	} 
	
	public String getClosureFunction (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node) {
		if(closures.containsKey(node))
			return closures.get(node).getFunction();
		else
			return null;
	}
	
	private void handleConstraints(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		
		if(isNodeConstraintHandled(node))
			return;
		
		List<String> constraints = new ArrayList<String>();
		if(node.data.getObj() instanceof AttributeType) {
			constraints.add(handleAttribute((AttributeType) node.data.getObj(), (AMLQueryConfig) node.data.getConfig(), false));
		}
		else if(node.data.getObj() instanceof ExternalInterfaceType) {
			constraints.add(handleExternalInterface((ExternalInterfaceType) node.data.getObj(), (AMLQueryConfig) node.data.getConfig(), false, false, semantically, withLink));
		}
		else if(node.data.getObj() instanceof InternalElementType) {			
			constraints.add(handleInternalElement((InternalElementType) node.data.getObj(), (AMLQueryConfig) node.data.getConfig(), false, semantically));
		}
		else if(node.data.getObj() instanceof SystemUnitFamilyType) {
			constraints.add(handleSystemUnitClass((SystemUnitFamilyType) node.data.getObj(), (AMLQueryConfig) node.data.getConfig(), false, semantically));			
		}
		else if(node.data.getObj() instanceof RoleFamilyType) {
			constraints.add(handleRoleClass((RoleFamilyType) node.data.getObj(), (AMLQueryConfig) node.data.getConfig(), false, semantically));
		}
		else if(node.data.getObj() instanceof InterfaceFamilyType) {
			constraints.add(handleInterfaceClass((InterfaceFamilyType) node.data.getObj(), (AMLQueryConfig) node.data.getConfig(), false, semantically));
		}
		
		this.constraints.put(node, constraints);
	}	
	
	/**
	 * handle internal link as part of the suc syntactically
	 * @deprecated replaced by AMLLinkCollector and related Link constraint handling  
	 */
	private String handleInternalLink(SystemUnitClassType suc) {
		String expr = "";
		for(InternalLinkType il : suc.getInternalLink()) {
			/**
			 * we ignore the name of the link here, only take the endpoints as the constraint
			 */			
			expr += "InternalLink[@RefPartnerSideA=" + toXQueryString(il.getRefPartnerSideA()) + " and @RefPartnerSideB=" + toXQueryString(il.getRefPartnerSideB()) + "]";
		}
		return expr;
	}
	
	// TODO: for CAEXFile
	private String handleExternalReference() {
		return "";
	}
	
	
	// TODO: for SRC and IE
	private String handleMappingObject() {
		return "";
		//return handleAttributeNameMapping() + " and " + handleInterfaceNameMapping();
	}
	
	// TODO: 
	private String handleAttributeNameMapping() {
		return "";
	}
	
	// TODO
	private String handleInterfaceNameMapping() {
		return "";
	}
	
	private String toXQueryString(String s) {
		return "\"" + s + "\"";
	}
	
	private String handleRRRefBase(String basePath, boolean negated, boolean semantically) {
		if(semantically) {
			String[] tokens = basePath.split("/");
			String lib = tokens[0];
			String role = tokens[tokens.length-1];
	
			String args = genArgumentString(role, lib);
			if(negated)
				return REQUIRES_RR_NEGATED + "(" + args + ", .)";
			else
				return REQUIRES_RR + "(" + args + ", .)";
		}else
			return "@RefBaseRoleClassPath=" + toXQueryString(basePath);
	}
	
	private String handleRCRefBase(String basePath, boolean negated, boolean semantically) {
		if(semantically) {
			String[] tokens = basePath.split("/");
			String lib = tokens[0];
			String role = tokens[tokens.length-1];
	
			String args = genArgumentString(role, lib);
			
			if(negated)				
				return REFS_RC + "(" + args + ", .)";
			else
				return REFS_RC_NEGATED + "(" + args + ", .)";
		}else
			return "@RefBaseClassPath=" + toXQueryString(basePath);
	}
	
	private String handleICRefBase(String basePath, boolean negated, boolean semantically) {
		
		if(basePath == null || basePath == "") {
			return "";
		}
		
		if(semantically) {
			String[] tokens = basePath.split("/");
			String lib = tokens[0];
			String ic = tokens[tokens.length-1];
	
			String args = genArgumentString(ic, lib);
			
			if(negated)
				return REFS_IC_NEGATED + "(" + args + ", .)";
			else
				return REFS_IC + "(" + args + ", .)";
		}else
			return "@RefBaseClassPath=" + toXQueryString(basePath);		
	}
	
	private String genArgumentString(String... arguments) {
		String expr = "";
		for(int i = 0; i < arguments.length; i++) {		
			expr += toXQueryString(arguments[i]) + ",";
		}
		expr = expr.substring(0, expr.length()-1);
		return expr;
	}

	/**
	 * Handle the attribute of this node
	 * @param attr: the attribute
	 * @param recursive: generally, sub attributes of the current attribute is automatically handled by recursive traverse of the model.
	 * however, for role requirements (which is not a query object), we need to explicitly handle all sub attributes since they are ignored in the tree building phase  
	 * @return
	 */
	private String handleAttribute(AttributeType attr, AMLQueryConfig config, boolean recursive) {
		String expr = "";
		
		if(attr.getName().equalsIgnoreCase("queryConfig")) 
			return expr;
		
		if(config.isIdentifiedByName()) {
//		if(true) { //now we set default to true for attribute names
			String name = attr.getName();
			if (name != null && !name.isEmpty())
				expr += "@Name=" + toXQueryString(name) + " and ";	
		}
		
		//TODO: now we ignore datatypes since AML editor does not allow empty value of double attributes
//		String datatype = attr.getAttributeDataType();
//		if (datatype != null && !datatype.isEmpty())
//			expr += "@AttributeDataType=" + toXQueryString(datatype) + " and ";
		
		String unit = attr.getUnit();
		if (unit != null && !unit.isEmpty())
			expr += "@Unit=" + toXQueryString(unit) + " and ";
		
		String value = AMLHelperFunctions.getAMLAttributeValue(attr);
		if (value != null && !value.isEmpty())
			expr += "Value=" + toXQueryString(value) + " and ";

		// handle value requirements as predicates instead of axis: since it is only CAEXBasicObject
		List<AttributeValueRequirementType> requirements = attr.getConstraint();
		for(int i = 0; i < requirements.size(); i++) {
			AttributeValueRequirementType req = requirements.get(i);
						
			NominalScaledTypeType nst = req.getNominalScaledType();
			if (nst != null && !nst.getRequiredValue().isEmpty()) {
//				String nstExpr = " and ";
				String nstExpr = "";
				for(int j = 0; j < nst.getRequiredValue().size(); j++) {
					String nstvalue = AMLHelperFunctions.fromAMLAnyType(nst.getRequiredValue().get(j));
					// if this contradicts the specified value in the attribute: error
					if(value != null && !value.isEmpty() && value != nstvalue)
						System.out.println("Error: NominalScaledType " + req.getName() + 
								" inconsistent with the specified value of its attribute" + attr.getName() + "!");
					else
						nstExpr += "Value=" + toXQueryString(nstvalue) + " or ";													
				}	
				nstExpr = nstExpr.substring(0, nstExpr.length()-4) + " and ";
				expr += nstExpr;	
			}
						
			OrdinalScaledTypeType ost = req.getOrdinalScaledType();
			if (ost != null) {
				String ostExpr = "";
				String ostvalue = AMLHelperFunctions.fromAMLAnyType(ost.getRequiredValue());
				String min = AMLHelperFunctions.fromAMLAnyType(ost.getRequiredMinValue());
				String max = AMLHelperFunctions.fromAMLAnyType(ost.getRequiredMaxValue());
				// if this contradicts the specified value in the attribute: error
				if(value != null && !value.isEmpty() && !ostvalue.isEmpty() && value != ostvalue)
					System.out.println("Error: OrdinlaScaledType " + req.getName() + 
							" inconsistent with the specified value of its attribute" + attr.getName() + "!");
				else if(ostvalue != null && !ostvalue.isEmpty())
					ostExpr += "Value=" + toXQueryString(ostvalue) + " and ";
				else {
					// we handle the value comparison rather dirty here - using string comparison for numerical values
					// but shall work also
					if(min != null && !min.isEmpty())					
						ostExpr += "Value>=" + min + " and ";
					if(max != null && !max.isEmpty())
						ostExpr += "Value<=" + max + " and "; 
				}
				expr += ostExpr;				
			}
			
			// TODO: we do not hanle this yet
			UnknownTypeType utt = req.getUnknownType();		
		}
		
		String defaultValue = AMLHelperFunctions.fromAMLAnyType(attr.getDefaultValue());
		if (defaultValue != null && !defaultValue.isEmpty())
			expr += " and DefaultValue=" + toXQueryString(defaultValue);
		
		List<RefSemanticType> semantics = attr.getRefSemantic();
		for (int i = 0; i < semantics.size(); i++)
		{
			String spath = semantics.get(i).getCorrespondingAttributePath();
			expr += "RefSemantic[@CorrespondingAttributePath=" + toXQueryString(spath) + "] and ";
		}
		
		if(recursive){
			for(AttributeType child : attr.getAttribute()) {
				if(!child.getName().equalsIgnoreCase("queryConfig"))
					expr += "Attribute[" + handleAttribute(child, recursive) + "] and ";
			}
		}
		
		if(expr != null && expr.length()>0)
			expr = expr.substring(0, expr.length()-5);
		
		return expr;
	}
	
	private String handleAttribute(AttributeType attr, boolean recursive) {
		String expr = "";
		
		if(attr.getName().equalsIgnoreCase("queryConfig")) 
			return expr;
		
		AMLQueryConfig config = new AMLQueryConfig();
		for(int i = 0; i < attr.getAttribute().size(); i++) {
			AttributeType child = attr.getAttribute().get(i);
			if(child.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG)) {
				for(AttributeType gc : child.getAttribute()) {
					if(gc.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_DISTINGUISHED)) {
						Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(gc);
						if(b!=null)
							config.setDistinguished(b.booleanValue());				
					}
//					else if(gc.getName().equalsIgnoreCase(AMLQueryModelParser2.QUERY_CONFIG_NEGATED)) {
//						Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(gc);
//						if(b!=null)
//							config.setNegated(b.booleanValue());				
//					}
					else if(gc.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_TRANSITIVE)) {
						Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(gc);
						if(b!=null)
							config.setTransitive(b.booleanValue());				
					}
					else if(gc.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_ID)) {
						Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(gc);
						if(b!=null)
							config.setIdentifiedById(b.booleanValue());				
					}
					else if(gc.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_NAME)) {
						Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(gc);
						if(b!=null)
							config.setIdentifiedByName(b.booleanValue());				
					}
					else if(child.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_MAX)) {
						Integer b = AMLHelperFunctions.getAMLAttributeValueInteger(child);
						if(b != null)
							config.setMaxCardinality(b.intValue());
					}
					else if(child.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_MIN)) {
						Integer b = AMLHelperFunctions.getAMLAttributeValueInteger(child);
						if(b != null)
							config.setMinCardinality(b.intValue());
					}
					else if(gc.getName().equalsIgnoreCase(AMLQueryConfig.CONFIG_DESCENDANT)) {
						Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(gc);
						if(b!=null)
							config.setDescendant(b.booleanValue());
					}
				}
			}
		}
		
		if(config.getMaxCardinality() < config.getMinCardinality() && config.getMaxCardinality() != -1) {
			System.err.println("maximum cardinliaty (" + config.getMaxCardinality() + ") smaller than the minimum cardinality (" + config.getMinCardinality() + ")");
			System.err.println(" - changing cardinilities to -1 (unbounded)");
			config.setMaxCardinality(-1);
			config.setMinCardinality(1);	
		}
		
		if(config.getMaxCardinality() < -1) {
			System.err.println("maximum cardinliaty (" + config.getMaxCardinality() + ") <-1");
			System.err.println(" - same as -1, set it to -1");
			config.setMaxCardinality(-1);
//			cardinalityError = true;
		}
		
		if(config.getMinCardinality() < -1) {
			System.err.println("minimum cardinliaty (" + config.getMinCardinality() + ") <0");
			System.err.println("same as 0, set it to 0");
			config.setMinCardinality(0);
		}
		
		expr += handleAttribute(attr, config, false) + " and ";
		if(recursive){
			for(AttributeType child : attr.getAttribute()) {
				if(!child.getName().equalsIgnoreCase("queryConfig"))
					expr += "Attribute[" + handleAttribute(child, recursive) + "] and ";
			}
		}
		
		if(expr != null && expr.length()>0)
			expr = expr.substring(0, expr.length()-5);
		
		return expr;
		
	}
	
	/**
	 *  Usually, attributes of EI do not have to be handled here, since they are query children objects of the EI
	 *  But for role requirements, we need to explicitly handle its attributes since these are ignored in the tree building phase
	 * ---------------- Meaning of symbols ----------------
	 * A, B: end points
	 * q: query object
	 * d: data object
	 * ----------------------------------------------------
	 * 
	 * 
	 * ---------------- Supported queries ----------------
	 * (null, Bq):	the EI in query (Bq) shall connects to something - caex215:isInterfaceConnectedById(ID_IN_DATA)
	 * (Aq, null):	the EI in query (Aq) shall connects to something - caex215:isInterfaceConnectedById(ID_IN_DATA)
	 * 
	 * (Aq, Bd): 	the EI in query (Aq) shall connects to the EI(Bd) in data - caex215:isInterfaceConnectedToPartner(ID_IN_DATA, Bd)
	 * (Ad, Bq):		the EI in query (Bq) shall connects to the EI(Ad) in data - caex215:isInterfaceConnectedToPartner(ID_IN_DATA, Ad)
	 * 
	 * (Aq, Bq): 	the EI in query (Aq) shall connects to the EI in pattern(Bq) - find EIs in data that match both patterns and make sure that they are connected
	 * 
	 * ---------------- Ignored (invalid) queries ----------------
	 * (null, null):	 no end points are found in the query model
	 * (null, Bd):	 no end points are found in the query model
	 * (Ad, null):	 no end points are found in the query model
	 * (Ad, Bd):		 the EI(Ad) and EI(Bd) in data shall be connected - check if it is so - will be ignored for now
	 * 
	 * 
	 * Case 1: if one end point is empty: (Aq, null) or (null, Bq) 
	 * 		- caex215:isInterfaceConnectedById(ID_IN_DATA)
	 *
	 * Case 2: if one end point in query, the other one not in query: (Aq, Bd) or (Ad, Bq) - the EI in pattern shall connect to an specific EI in data 
	 * 		- caex215:isInterfaceConnectedToPartner(ID_IN_DATA, Bd)
	 * 		- caex215:isInterfaceConnectedToPartner(Ad, ID_IN_DATA)
	 *
	 * Case 3: if one both end points in query: (Aq, Bq) - the EI in pattern(Aq) shall connects to the EI in pattern(Bq) 
	 * 		- find EIs in data that match both patterns and make sure that they are connected
	 */
	private String handleInternalLink (EILinkRefSide partner, boolean semantically) {
		String constraint = "";

		// Case 1: if one end point is empty: (Aq, null) or (null, Bq), i.e. existence of any connection to the EIs match this pattern
		if(partner.getType().equals(EILinkRefSide.EndpointType.EMPTY)) {
//			constraint += "caex215:isInterfaceConnectedById(@ID, .) and ";
			constraint += "caex215:isConnected(.) and ";
		}
		
		// Case 2: one end point in query, the other one not in query - (Aq, Bd) or (Ad, Bq), i.e. existence of a connection to the specific EI
		else if(partner.getType().equals(EILinkRefSide.EndpointType.DATA)) {
//			constraint += "caex215:connectsToRefSide(.,\""+ partner.getRefSide() +"\") and ";
			String pid = partner.getEIId();
			if(pid != null)
				constraint += "caex215:connectsTo(., $root//ExternalInterface[@ID=\""+ partner.getEIId() +"\"]) and ";
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": partner ID not found in query and data, ignored...");
				return null;
			}
				
		}
		
		// if partner side can be found in the query file -> referential dependency in model
		// this will be a cycle, since A->B->A->B...
		// right now, we only consider one step link: so no chains
		// is chained links really usefu?? 
		else if(partner.getType().equals(EILinkRefSide.EndpointType.QUERY)) {
			ExternalInterfaceType partnerEI = AMLLinkCollector.getEI(partner); 					
			if(GenericAMLConceptModelUtils.isAMLConceptModel(partnerEI)) {
				try {
					GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> partnerNode = GenericAMLConceptModelUtils.parse(partnerEI, AMLQueryConfig.class);
					// set withLink to false: one step
					String partnerXpath = this.getXPath(partnerNode, semantically, false);
					constraint += "caex215:connectsToAny(., $root//" + partnerXpath + ") and ";
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		else {
			// invalid endpoint
		}
		
		return constraint;
	}
	
	
	private String handleExternalInterface(ExternalInterfaceType ei, AMLQueryConfig config, boolean recursive, boolean negated, boolean semantically, boolean withLink) {
				
		String expr = "";		
		
		if(config.isIdentifiedByName()) {
			String name = ei.getName();
			if (name != null && !name.isEmpty())
				expr += "@Name=" + toXQueryString(name) + " and ";
		}
		
		if(config.isIdentifiedById()) {
			String id = ei.getID();
			if (id != null && !id.isEmpty())
				expr += "@ID=" + toXQueryString(id) + " and ";
		}
		
		Set<EILinkRefSide> linkConstraints = config.getLinkConstraints();
		if(withLink && linkConstraints != null) {
			for(EILinkRefSide partner : linkConstraints) {
				String constraint = handleInternalLink(partner, semantically);
				if(constraint != null && constraint.length()>0)
					expr += constraint;
			}
		}		
			
		
		if(ei.getRefBaseClassPath() != null) {
			String basePath = ei.getRefBaseClassPath();
			expr += handleICRefBase(basePath, negated, semantically) + " and ";
		}
		
		
		if(recursive) {
			for(AttributeType attr : ei.getAttribute()) {
				if(!attr.getName().equalsIgnoreCase("queryConfig"))
					expr += " and Attribute[" + handleAttribute(attr, recursive) + "]";
			}
		}
		
		if(expr.length()>0)
			expr = expr.substring(0, expr.length()-5);
	
		return expr;
	}
	
	private String handleInterfaceClass(InterfaceFamilyType icf, AMLQueryConfig config, boolean negated, boolean semantically) {
		String expr = "";
		
		if(config.isIdentifiedByName()) {
			String name = icf.getName();
			if (name != null && !name.isEmpty())
				expr += "@Name=" + toXQueryString(name) + " and ";
		}
		
		if(icf.getRefBaseClassPath() != null) {
			String basePath = icf.getRefBaseClassPath();
			expr += handleICRefBase(basePath, negated, semantically) + " and ";
		}
			
		
		if(expr.length()>0)
			expr = expr.substring(0, expr.length()-5);
	
		return expr;		
	}
	
	private String handleRoleClass(RoleFamilyType rf, AMLQueryConfig config, boolean negated, boolean semantically) {
		String expr = "";
		
		if(config.isIdentifiedByName()) {
			String name = rf.getName();
			if (name != null && !name.isEmpty())
				expr += "@Name=" + toXQueryString(name) + " and ";
		}
		
		if(rf.getRefBaseClassPath() != null) {
			String basePath = rf.getRefBaseClassPath();
			expr += handleRCRefBase(basePath, negated, semantically);
		}
		
		if(expr.length()>0)
			expr = expr.substring(0, expr.length()-5);
	
		return expr;		
	}
	
	private String handleInternalElement(InternalElementType ie, AMLQueryConfig config, boolean negated, boolean semantically) {
		String expr = "";
		
		if(config.isIdentifiedByName()) {
			String name = ie.getName();
			if (name != null && !name.isEmpty())
				expr += "@Name=" + toXQueryString(name) + " and ";
		}
		
		if(config.isIdentifiedById()) {
			String id = ie.getID();
			if (id != null && !id.isEmpty())
				expr += "@ID=" + toXQueryString(id) + " and ";
		}
		
		if(ie.getRefBaseSystemUnitPath() != null && ie.getRefBaseSystemUnitPath() != "")
			expr += "@RefBaseSystemUnitPath=" + toXQueryString(ie.getRefBaseSystemUnitPath()) + " and ";
		
		String src = handleSupportedRoleClass(ie, negated, semantically);
		String rr = handleRoleRequirement(ie, config, negated, semantically);
		String mapping = handleMappingObject();
		
		if(src != null && !src.isEmpty())
			expr += src + " and ";
		
		if(rr != null && !rr.isEmpty())
			expr += rr + " and ";
		
		if(mapping != null && !mapping.isEmpty())
			expr += mapping + " and ";
		
		if(expr.length()>0)
			expr = expr.substring(0, expr.length()-5);
				
		return expr;
	}
	
	
	/** handle role requirement as predicate instead of axis: since it is only CAEXBasicObject
	 * now, we do not expect(allow) the data retrieval of role requirements but only handle it as constraints of the IE 
	 * CAEX standard says:
	 * 1. The RoleRequirements definition at an InternalElement is valid for the individual InternalElement. 
	 * 	  It may be extended by further CAEX attributes or CAEX ExternalInterfaces, even when they are not defined in the referenced RoleClass. 
	 * 	  This supports extending the requirements for the related InternalElement.
	 * 2. Above the definitions in the RoleRequirements, the related InternalElement may have additional specifications (Attributes, Interfaces), 
	 * 	  which are not defined at the related RoleClass. This allows defining implementation specific details of the individual Internal Element.
	 * 3. The specification of the InternalElement may violate the specification of the Role Requirements or SupportedRole Class. 
	 *    AML explicitly supports the storage of in consistent or incomplete engineering data. 
	 *    The validity of the data is a matter of the tools, CAEX only depicts their current data. It does not provide consistency.
	 */
	private String handleRoleRequirement(InternalElementType ie,  AMLQueryConfig config, boolean negated, boolean semantically) {
		RoleRequirementsType rr = ie.getRoleRequirements();		
		if(rr == null)
			return null;
		String path = rr.getRefBaseRoleClassPath();
		String expr = "RoleRequirements[" + handleRRRefBase(path, negated, semantically);
		
		/**
		 *  We need to handle attributes and eis here, since role requirements is not query object
		 *  TODO: we need to do it recursively
		 */				
		for(AttributeType attr : rr.getAttribute()) {
			if(!attr.getName().equalsIgnoreCase("queryConfig")) {
				String s = handleAttribute(attr, true);
				expr += " and Attribute[" + s + "]";
			}						
		}
		for(ExternalInterfaceType ei : rr.getExternalInterface()) {
			// since RR is not query object, its sub objects can not be query objects, therefore we use std. setting for handleExternalInterface
			// recursive: true
			// negated: false
			expr += " and ExternalInterface[" + handleExternalInterface(ei, config, true, false, semantically, false) + "]";
		}
		expr += "]";
				
		return expr;
	}
	
	private String handleSystemUnitClass(SystemUnitFamilyType suf, AMLQueryConfig config, boolean negated, boolean semantically) {
		String expr = "";
		
		if(config.isIdentifiedByName()) {
			String name = suf.getName();
			if (name != null && !name.isEmpty())
				expr += "@Name=" + toXQueryString(name) + " and ";
		}
		
		if(config.isIdentifiedById()) {
			String id = suf.getID();
			if (id != null && !id.isEmpty())
				expr += "@ID=" + toXQueryString(id) + " and ";
		}
		
		String src = handleSupportedRoleClass(suf, negated, semantically);
		if(src != null && !src.isEmpty())
			expr += src;
		return expr;
	}
	
	// for SUC
	private String handleSupportedRoleClass(SystemUnitClassType suc, boolean negated, boolean semantically) {
		if(!semantically){
			String expr = "";
			for (int i = 0; i < suc.getSupportedRoleClass().size(); i++) {
				SupportedRoleClassType src = suc.getSupportedRoleClass().get(i);
				String path = src.getRefRoleClassPath();
				String part = "SupportedRoleClass[@RefRoleClassPath=" + toXQueryString(path);
				expr += part + " and ";
			}
			if(expr != "")
				expr = expr.substring(0, expr.length()-5) + "]";
			return expr;
		}else {
			String expr = "";
			for (int i = 0; i < suc.getSupportedRoleClass().size(); i++) {
				SupportedRoleClassType src = suc.getSupportedRoleClass().get(i);
				String path = src.getRefRoleClassPath();
				String[] tokens = path.split("/");
				String lib_name = tokens[0];
				String role_name = tokens[tokens.length-1];
				String args = genArgumentString(role_name, lib_name);
				
				String part = "";
				if(negated)
					part = SUPPORTS_RC_NEGATED + "(" + args + ", .)";
				else
					part = SUPPORTS_RC + "(" + args + ", .)";
				
				expr += part + " and ";
			}		
			
			if(!expr.isEmpty())
				expr = expr.substring(0, expr.length()-5);
			return expr;
		}
		
//		return handleMappingObject();
	}
	
	
	/**
	 * get the xpath predicate of the input node itself (without primary children) considering: 
	 * 	- caex model constraints (conditional children): class reference, attribute configs etc.
	 *  - parameter: identifiedById
	 *  - parameter: identifiedByName
	 * @param node
	 * @param semantically
	 * @return
	 */
	public String getXPathPredicate(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		handleConstraints(node, semantically, withLink);			
		
		String predicate = "";
		List<String> constraints = this.constraints.get(node);
		
		for(int i = 0; i < constraints.size(); i++) {
			if(constraints.get(i) != "") {
				predicate += constraints.get(i) + " and ";
			}
		}
		if(!predicate.isEmpty())
			predicate = predicate.substring(0, predicate.length() - 5);
		
//		System.out.println("predicate(" + this.getCAEXObject().getName() + "): " + predicate);
		return predicate;		
	}
	

	/**
	 * get the xpath expression of the input node itself (without primary children): without cardinalities and transitive closure
	 * 	- caex model constraints (conditional children): class reference, attribute configs etc.
	 *  	- parameter: identifiedById
	 *  	- parameter: identifiedByName
	 *  	- parameter: descendant
	 * @param node
	 * @param semantically
	 * @return
	 */
	public String getXPathBase(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		
		if(node.data.getObj() instanceof AttributeType) {
			AttributeType attr = (AttributeType) node.data.getObj();
			if(attr.getName().equalsIgnoreCase("queryConfig")) 
			return null;
		}
		
		if(!xpathNodeExprSimple.containsKey(node)) {
			String expr = ""; 
			String predicate = getXPathPredicate(node, semantically, withLink);
			String predicateExpr = "";
			if(predicate.isEmpty())
				predicate = predicateExpr;
			else
				predicateExpr = "[" + predicate + "]";
			if(node.data.getObj() instanceof AttributeType)
				expr = "Attribute" + predicateExpr;
			else if(node.data.getObj() instanceof InternalElementType)
				expr = "InternalElement" + predicateExpr;
			else if(node.data.getObj() instanceof SystemUnitClassType || node.data.getObj() instanceof SystemUnitFamilyType)
				expr = "SystemUnitClass" + predicateExpr;
			else if(node.data.getObj() instanceof RoleClassType || node.data.getObj() instanceof RoleFamilyType)
				expr = "RoleClass" + predicateExpr;
			else if(node.data.getObj() instanceof ExternalInterfaceType)
				expr = "ExternalInterface" + predicateExpr;
			else if(node.data.getObj() instanceof InterfaceClassType || node.data.getObj() instanceof InterfaceFamilyType)
				expr = "InterfaceClass" + predicateExpr;
			else 
				return null;
			
			if(((AMLQueryConfig) node.data.getConfig()).isDescendant())
				expr = DESCENDANT + expr;
			
			xpathNodeExprSimple.put(node, expr);
		}
	
		return xpathNodeExprSimple.get(node);
	}
	
	/**
	 * get the simple xpath expression of the input node (without primary children):
	 * 	- caex model constraints (conditional children): class reference, attribute configs etc.
	 *  	- parameter: identifiedById
	 *  	- parameter: identifiedByName
	 *  	- parameter: descendant
	 *  - parameter: transitive
	 *  - parameter: cardinalities: use "count"
	 *  - TODO: if the node (class reference and attribute data range) is negated
	 * @param node 
	 * @param semantically
	 * @return
	 */ 
	public String getXPathSimple(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		
		if(!xpathNodeExpr.containsKey(node)) {
			String expr = getXPathBase(node, semantically, withLink);
			
			// handle transitive closure
			if(((AMLQueryConfig) node.data.getConfig()).isTransitive()) {
				TransitiveClosure closure = new TransitiveClosure(expr);
				closures.put(node, closure);
				expr = "*[" + closure.getExpression() + "]";
			}
			
//			if(data.getConfig().isNegated()) {
//				expr = "*[not(self::" + expr + ")]";	
//			}
			
			String tmp = expr;
			int min = ((AMLQueryConfig) node.data.getConfig()).getMinCardinality();
			int max = ((AMLQueryConfig) node.data.getConfig()).getMaxCardinality();
			
			if(max == 0) {
				expr = "count(" + tmp + ")=0";
			}							
			
			// min = 0 and max > 0 means it can not exist to max times
			else if(min == 0) {
				if(max > 0 )
					expr = "count(" + tmp + ")<=" + ((AMLQueryConfig) node.data.getConfig()).getMaxCardinality();
				// min = 0, max = -1: it does not matter what it is
				// TODO: make sure this is correct
				else
					expr = "*";
			}			
			
			else if(min == 1 && max > 0)
				expr = "count(" + tmp + ")>=" + ((AMLQueryConfig) node.data.getConfig()).getMinCardinality() + "and count(" + tmp + ")<=" + ((AMLQueryConfig) node.data.getConfig()).getMaxCardinality();
			
			else if(min > 1 && max > 0)
				expr = "count(" + tmp + ")>=" + ((AMLQueryConfig) node.data.getConfig()).getMinCardinality() + "and count(" + tmp + ")<=" + ((AMLQueryConfig) node.data.getConfig()).getMaxCardinality();
			
			xpathNodeExpr.put(node, expr);
		}
		
		return xpathNodeExpr.get(node);				
	}	

	/**
	 * get the full xpath expression of the input node with primary children, starting from the node itself
	 * 	- caex model constraints (conditional children): class reference, attribute configs etc.
	 *  	- parameter: identifiedById
	 *  	- parameter: identifiedByName
	 *  	- parameter: descendant
	 *  - parameter: transitive
	 *  - parameter: cardinalities: use "count"
	 *  - primary children: RC, IC, SUC, IE, EI, ATTR 
	 * @param node 
	 * @param semantically
	 * @return
	 */ 
	public String getXPath(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		
		if(!xpathExpr.containsKey(node))
			xpathExpr.put(node, getXPathRecursive(node, semantically, withLink));
		
		return xpathExpr.get(node);
	}
	
	/**
	 * recursive function for getting the complete xpath expression of the input node with primary children
	 * 	- caex model constraints (conditional children): class reference, attribute configs etc.
	 *  	- parameter: identifiedById
	 *  	- parameter: identifiedByName
	 *  	- parameter: descendant
	 *  - parameter: transitive
	 *  - parameter: cardinalities: use "count"
	 *  - primary children: RC, IC, SUC, IE, EI, ATTR 
	 * @param node 
	 * @param semantically
	 * @return
	 */
	private String getXPathRecursive(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		
		String simpleExpr = getXPathBase(node, semantically, withLink);
		
		// handle transitive closure
		if(((AMLQueryConfig) node.data.getConfig()).isTransitive()) {
			if(!closures.containsKey(node)) {				
				closures.put(node, new TransitiveClosure(simpleExpr));
			}
			simpleExpr = "*[" + closures.get(node).getExpression() + "]";
		}
		
		String rootExpr = simpleExpr;
		
		//TODO: negated
//		if(node.data.getConfig().isNegated()) {
//			rootExpr = "*[not(self::" + rootExpr + ")]";	
//		}				

		
		String childExpr = "";
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : node.getChildren()) {
			if(getXPathSimple(child, semantically, withLink) != "") {
				childExpr += getXPathRecursive(child, semantically, withLink) + " and ";
			}
		}
				
		if(rootExpr.endsWith("]")) {			
			if(!childExpr.isEmpty()) {
				rootExpr = rootExpr.substring(0, rootExpr.length()-1);
				rootExpr += " and " + childExpr.substring(0, childExpr.length()-5) + "]";
			}
		}
		else if(rootExpr.endsWith(" and ")) {
			if(!childExpr.isEmpty()) {
				rootExpr += childExpr.substring(0, childExpr.length()-5);
			}
			else
				rootExpr = rootExpr.substring(0, rootExpr.length()-5);
		}
		else {
			if(!childExpr.isEmpty()) {
				rootExpr += "[" + childExpr.substring(0, childExpr.length()-5) + "]";
			}
		}
		
		
		String tmp = rootExpr;
		int max = ((AMLQueryConfig) node.data.getConfig()).getMaxCardinality();
		int min = ((AMLQueryConfig) node.data.getConfig()).getMinCardinality();
		if(max == 0) {
//			rootExpr = "count(" + tmp + ")=0 and ";
			rootExpr = "count(" + tmp + ")=0";
		}						
		// min = 0 and max > 0 means it can exist zero to max times
		else if(min == 0) {
			if(max > 0 )
//				rootExpr = "count(" + tmp + ")<=" + max + " and ";
				rootExpr = "count(" + tmp + ")<=" + max;
			// min = 0, max = -1: it does not matter what it is
			// TODO: make sure this is correct
			else {
//				rootExpr = "*";
				if(tmp.startsWith("descendant-or-self"))
					rootExpr = "*[count(" + tmp + ")>=0]";
				else
					rootExpr = "*[count(self::" + tmp + ")>=0]";
			}
		}		
		else if(min == 1 && max > 0) {
//			rootExpr = "count(" + tmp + ")>=" + min + " and " + "count(" + tmp + ")<=" + max + " and ";
			rootExpr = "count(" + tmp + ")>=" + min + " and " + "count(" + tmp + ")<=" + max;
		}
		else if(min > 1 && max > 0) {
//			rootExpr = "count(" + tmp + ")>=" + min + " and " + "count(" + tmp + ")<=" + max + " and ";
			rootExpr = "count(" + tmp + ")>=" + min + " and " + "count(" + tmp + ")<=" + max;
		}
		else if(min > 1 && max == -1) {
//			rootExpr = "count(" + tmp + ")>=" + min + " and ";
			rootExpr = "count(" + tmp + ")>=" + min; 
		}
				
		return rootExpr;
	}
	
	
	
	/**
	 * get the full XPath expression from start node to the end node, e.g. IE/IE/.../ATTR[...]
	 * all nodes which are not on the direct path to end are constraints somewhere
	 * @param start 
	 * @param end 
	 * @param semantically
	 * @return
	 */ 
	public String getXPathBetween(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> start, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> end, boolean semantically, boolean withLink) {

		String query = "";
		if (!start.equals(end)) {				
			if(((AMLQueryConfig) start.data.getConfig()).isTransitive()) {
				if(!closures.containsKey(start)) {
					TransitiveClosure closure = new TransitiveClosure(getXPathBase(start, semantically, withLink));
					closures.put(start, closure);
				}
				query = "*/" + closures.get(start).getExpression() + "[";
			}else {				
				String simple = getXPathSimple(start, semantically, withLink);
				// if xpath simple contains a "]" at the end, we need to remove it to include new constraints
				if(simple.endsWith("]")) {
					query = simple.substring(0, simple.length()-1);
					// we also want to prepare the "and" for possible new constraints
					query += " and ";
				}
				// otherwise, we need to add a new "[" to include new constraints
				else {
					query = getXPathSimple(start, semantically, withLink) + "[";
				}
			}
			Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> children = start.getChildren();
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> criticalChild = null;
			int nonCriticalChildren = 0;
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : children) {		
				List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> descendantOrSelf = child.getDescendantOrSelf();
				if(!descendantOrSelf.contains(end)) {
					query += getXPath(child, semantically, withLink) + " and ";
					nonCriticalChildren++;
				}
				else {
					criticalChild = child;
				}
			}
			
			// if there were any children constraints, remove the last " and " and close the bracket
			if(nonCriticalChildren>0)
				query = query.substring(0, query.length()-5) + "]";
			// if there were no children constraints, remove the last "["
			else {				
				// this would have happend if the simple xpath of this node contains a "]" at the end, e.g. has RR
				if(query.endsWith(" and ")) {
					query = query.substring(0, query.length()-5);
					query += "]";
				}
				else {
					query = query.substring(0, query.length()-1);
				}
			}
			
			if(criticalChild != null) {
				String rec = getXPathBetween(criticalChild, end, semantically, withLink);
				query += "/" + rec;
			}else {
//				System.out.println("node " + start.data.getObj().getName() + " has no end child " + end.data.getObj().getName());
				System.out.println("node " + start.data.getObj() + " has no end child " + end.data.getObj());
				return null;
			}					
		}else {
			query = getXPath(start, semantically, withLink);
		}
		return query;
	}
	
	/**
	 * get the full XPath expression from start node to the end node without primary children of the end point
	 * all nodes which are not on the direct path to end are constraints somewhere
	 * use getXPathSimple to exclude primary children of the end point
	 * @param start 
	 * @param end 
	 * @param semantically
	 * @return
	 */ 
	public String getXPathBetweenOpenEnded(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> start, GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> end, boolean semantically, boolean withLink) {

		String query = "";
		if (!start.equals(end)) {				
			if(((AMLQueryConfig) start.data.getConfig()).isTransitive()) {
				if(!closures.containsKey(start)) {
					TransitiveClosure closure = new TransitiveClosure(getXPathBase(start, semantically, withLink));
					closures.put(start, closure);
				}
				query = "*/" + closures.get(start).getExpression() + "[";
			}else {				
				String simple = getXPathSimple(start, semantically, withLink);
				// if xpath simple contains a "]" at the end, we need to remove it to include new constraints
				if(simple.endsWith("]")) {
					query = simple.substring(0, simple.length()-1);
					// we also want to prepare the "and" for possible new constraints
					query += " and ";
				}
				// otherwise, we need to add a new "[" to include new constraints
				else {
					query = getXPathSimple(start, semantically, withLink) + "[";
				}
			}
			Set<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> children = start.getChildren();
			GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> criticalChild = null;
			int nonCriticalChildren = 0;
			for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : children) {		
				List<GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>> descendantOrSelf = child.getDescendantOrSelf();
				if(!descendantOrSelf.contains(end)) {
					query += getXPath(child, semantically, withLink) + " and ";
					nonCriticalChildren++;
				}
				else {
					criticalChild = child;
				}
			}
			
			// if there were any children constraints, remove the last " and " and close the bracket
			if(nonCriticalChildren>0)
				query = query.substring(0, query.length()-5) + "]";
			// if there were no children constraints, remove the last "["
			else {				
				// this would have happend if the simple xpath of this node contains a "]" at the end, e.g. has RR
				if(query.endsWith(" and ")) {
					query = query.substring(0, query.length()-5);
					query += "]";
				}
				else {
					query = query.substring(0, query.length()-1);
				}
			}
			
			if(criticalChild != null) {
				String rec = getXPathBetweenOpenEnded(criticalChild, end, semantically, withLink);
				query += "/" + rec;
			}else {
//				System.out.println("node " + start.data.getObj().getName() + " has no end child " + end.data.getObj().getName());
				System.out.println("node " + start.data.getObj() + " has no end child " + end.data.getObj());
				return null;
			}					
		}else {
			// use getXPathSimple to exclude primary children of the end point
			query = getXPathExceptCriticalDescendants(start, semantically, withLink);
		}
		return query;
	}
	
	/**
	 * get XPath expression of the given node except from the critical descendants of the node, i.e. only taking data from
	 *  - conditional children
	 *  - primary children that are not distinguished (returned)  
	 * @param node
	 * @param semantically
	 * @param withLink
	 * @return
	 */
	public String getXPathExceptCriticalDescendants (GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> node, boolean semantically, boolean withLink) {
		
		String simpleExpr = getXPathBase(node, semantically, withLink);
		
		// handle transitive closure
		if(((AMLQueryConfig) node.data.getConfig()).isTransitive()) {
			if(!closures.containsKey(node)) {				
				closures.put(node, new TransitiveClosure(simpleExpr));
			}
			simpleExpr = "*[" + closures.get(node).getExpression() + "]";
		}
		
		String rootExpr = simpleExpr;
		
		//TODO: negated
//		if(node.data.getConfig().isNegated()) {
//			rootExpr = "*[not(self::" + rootExpr + ")]";	
//		}
		
		String tmp = rootExpr;
		int max = ((AMLQueryConfig) node.data.getConfig()).getMaxCardinality();
		int min = ((AMLQueryConfig) node.data.getConfig()).getMinCardinality();
		if(max == 0) {
			rootExpr = "count(" + tmp + ")=0 and ";
		}						
		// min = 0 and max > 0 means it can not exist to max times
		else if(min == 0) {
			if(max > 0 )
				rootExpr = "count(" + tmp + ")<=" + max + " and ";
			// min = 0, max = -1: it does not matter what it is
			// TODO: make sure this is correct
			else
				rootExpr = "*";
		}		
		else if(min == 1 && max > 0) {
			rootExpr = "count(" + tmp + ")>=" + min + " and " + "count(" + tmp + ")<=" + max + " and "; 
		}
		else if(min > 1 && max > 0) {
			rootExpr = "count(" + tmp + ")>=" + min + " and " + "count(" + tmp + ")<=" + max + " and "; 
		}
		
		String childExpr = "";
		for(GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> child : node.getChildren()) {
			if(AMLQueryUtils.getReturnedNodes(child).isEmpty()) {
				if(getXPathSimple(child, semantically, withLink) != "") {
					childExpr += getXPathRecursive(child, semantically, withLink) + " and ";
				}	
			}			
		}
				
		if(rootExpr.endsWith("]")) {			
			if(!childExpr.isEmpty()) {
				rootExpr = rootExpr.substring(0, rootExpr.length()-1);
				rootExpr += " and " + childExpr.substring(0, childExpr.length()-5) + "]";
			}
		}
		else if(rootExpr.endsWith(" and ")) {
			if(!childExpr.isEmpty()) {
				rootExpr += childExpr.substring(0, childExpr.length()-5);
			}
			else
				rootExpr = rootExpr.substring(0, rootExpr.length()-5);
		}
		else {
			if(!childExpr.isEmpty()) {
				rootExpr += "[" + childExpr.substring(0, childExpr.length()-5) + "]";
			}
		}
				
		return rootExpr;
	}
	
	
}
