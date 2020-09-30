package serializer;

import java.util.List;

import org.basex.api.dom.BXElem;
import org.basex.query.QueryException;
import org.basex.query.value.Value;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import CAEX215.AttributeType;
import CAEX215.AttributeValueRequirementType;
import CAEX215.CAEX215Factory;
import CAEX215.CAEXBasicObject;
import CAEX215.CAEXObject;
import CAEX215.ChangeMode;
import CAEX215.CopyrightType;
import CAEX215.DescriptionType;
import CAEX215.ExternalInterfaceType;
import CAEX215.InstanceHierarchyType;
import CAEX215.InterfaceClassType;
import CAEX215.InterfaceFamilyType;
import CAEX215.InternalElementType;
import CAEX215.InternalLinkType;
import CAEX215.MappingType;
import CAEX215.RefSemanticType;
import CAEX215.RevisionType;
import CAEX215.RoleClassType;
import CAEX215.RoleFamilyType;
import CAEX215.RoleRequirementsType;
import CAEX215.SupportedRoleClassType;
import CAEX215.SystemUnitClassType;
import CAEX215.SystemUnitFamilyType;
import CAEX215.VersionType;
import CAEX215.util.AMLHelperFunctions;
import concept.tree.GenericTreeNode;

public class BaseX2AMLConverter {
	
	private BaseXResultParser parser = new BaseXResultParser();
	
	public BaseX2AMLConverter () {
	}

	
	private boolean setXMLAttribute (CAEXBasicObject obj, Node domAttribute) {
		
		String name = domAttribute.getNodeName();
		String value = domAttribute.getNodeValue(); 
		
		if(name.equals("ChangeMode")) {
			if(value.equals("create"))
				obj.setChangeMode(ChangeMode.CREATE);
			else if(value.equals("state"))
				obj.setChangeMode(ChangeMode.STATE);
			else if(value.equals("change"))
				obj.setChangeMode(ChangeMode.CHANGE);
			else if(value.equals("delete"))
				obj.setChangeMode(ChangeMode.DELETE);
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": change mode [" + value + "] of caex basic object [" + obj.toString() + "] unknown!");
				return false;
			}
			
			return true;
		}
	
		else 			
			return false;
	}
	
	private boolean setXMLAttribute (CAEXObject obj, Node domAttribute) {
		
		if(setXMLAttribute((CAEXBasicObject) obj, domAttribute)) {
			return true;
		}
		
		else {
			String name = domAttribute.getNodeName();
			String value = domAttribute.getNodeValue();
			
			if(name.equals("ID")) {
				obj.setID(value);
				return true;
			}
			
			else if(name.equals("Name")) {
				obj.setName(value);
				return true;
			}
			
			return false;
		}		
	}
	
	private boolean setXMLAttribute (AttributeType attr, Node domAttribute) {
		
		if(setXMLAttribute((CAEXObject) attr, domAttribute)) {
			return true;
		}
		else {
			if(domAttribute.getNodeName().equals("Unit")) {
				attr.setUnit(domAttribute.getNodeValue());
				return true;
			}
			
			else if(domAttribute.getNodeName().equals("AttributeDataType")) {
				attr.setAttributeDataType(domAttribute.getNodeValue());
				return true;
			}
			
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML attribute [" + domAttribute.getNodeName() + ":" + domAttribute.getNodeValue() + "] for the CAEX attribute [" + attr.toString() + "]!");
			}
			
			return false;
		}
	}
		
	private boolean setXMLAttributes (AttributeType attr, NamedNodeMap domAttributes) {
		
		boolean success = true;
		if(domAttributes != null) {
			for(int i = 0; i < domAttributes.getLength(); i++) {
				Node domAttr = domAttributes.item(i);					
				if(domAttr != null) {
					if(!setXMLAttribute(attr, domAttr)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	private boolean setXMLAttribute (SupportedRoleClassType src, Node domAttribute) {
		
		if(setXMLAttribute((CAEXBasicObject) src, domAttribute)) {
			return true;
		}
		else {
			if(domAttribute.getNodeName().equals("RefRoleClassPath")) {
				src.setRefRoleClassPath(domAttribute.getNodeValue());
				return true;
			}			
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML attribute [" + domAttribute.getNodeName() + ":" + domAttribute.getNodeValue() + "] for the CAEX SRC [" + src.toString() + "]!");
			}
			
			return false;
		}
	}
	
	private boolean setXMLAttributes (SupportedRoleClassType src, NamedNodeMap domAttributes) {
		
		boolean success = true;
		if(domAttributes != null) {
			for(int i = 0; i < domAttributes.getLength(); i++) {
				Node domAttr = domAttributes.item(i);					
				if(domAttr != null) {
					if(!setXMLAttribute(src, domAttr)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	private boolean setXMLAttribute (RoleRequirementsType rr, Node domAttribute) {
		
		if(setXMLAttribute((CAEXBasicObject) rr, domAttribute)) {
			return true;
		}
		else {
			if(domAttribute.getNodeName().equals("RefBaseRoleClassPath")) {
				rr.setRefBaseRoleClassPath(domAttribute.getNodeValue());
				return true;
			}			
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML attribute [" + domAttribute.getNodeName() + ":" + domAttribute.getNodeValue() + "] for the CAEX RR [" + rr.toString() + "]!");
			}
			
			return false;
		}
	}
	
	private boolean setXMLAttributes (RoleRequirementsType rr, NamedNodeMap domAttributes) {
		
		boolean success = true;
		if(domAttributes != null) {
			for(int i = 0; i < domAttributes.getLength(); i++) {
				Node domAttr = domAttributes.item(i);					
				if(domAttr != null) {
					if(!setXMLAttribute(rr, domAttr)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	private boolean setXMLAttribute (ExternalInterfaceType ei, Node domAttribute) {
		
		if(setXMLAttribute((CAEXObject) ei, domAttribute)) {
			return true;
		}
		else {
			if(domAttribute.getNodeName().equals("RefBaseClassPath")) {
				ei.setRefBaseClassPath(domAttribute.getNodeValue());
				return true;
			}
			
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML attribute [" + domAttribute.getNodeName() + ":" + domAttribute.getNodeValue() + "] for the CAEX EI [" + ei.toString() + "]!");
			}
			
			return false;
		}
	}
	
	private boolean setXMLAttributes (ExternalInterfaceType ei, NamedNodeMap domAttributes) {
		
		boolean success = true;
		if(domAttributes != null) {
			for(int i = 0; i < domAttributes.getLength(); i++) {
				Node domAttr = domAttributes.item(i);					
				if(domAttr != null) {
					if(!setXMLAttribute(ei, domAttr)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	private boolean setXMLAttribute (InternalElementType ie, Node domAttribute) {
		
		if(setXMLAttribute((CAEXObject) ie, domAttribute)) {
			return true;
		}
		else {
			if(domAttribute.getNodeName().equals("RefBaseSystemUnitPath")) {
				ie.setRefBaseSystemUnitPath(domAttribute.getNodeValue());
				return true;
			}
			
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML attribute [" + domAttribute.getNodeName() + ":" + domAttribute.getNodeValue() + "] for the CAEX IE [" + ie.toString() + "]!");
			}
			
			return false;
		}
	}
	
	private boolean setXMLAttributes (InternalElementType ie, NamedNodeMap domAttributes) {
		
		boolean success = true;
		if(domAttributes != null) {
			for(int i = 0; i < domAttributes.getLength(); i++) {
				Node domAttr = domAttributes.item(i);					
				if(domAttr != null) {
					if(!setXMLAttribute(ie, domAttr)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	private boolean setXMLAttribute (InternalLinkType il, Node domAttribute) {
		
		if(setXMLAttribute((CAEXBasicObject) il, domAttribute)) {
			return true;
		}
		else {
			if(domAttribute.getNodeName().equals("RefPartnerSideA")) {
				il.setRefPartnerSideA(domAttribute.getNodeValue());
				return true;
			}
			
			else if(domAttribute.getNodeName().equals("RefPartnerSideB")) {
				il.setRefPartnerSideB(domAttribute.getNodeValue());
				return true;
			}
			
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML attribute [" + domAttribute.getNodeName() + ":" + domAttribute.getNodeValue() + "] for the CAEX IL [" + il.toString() + "]!");
			}
			
			return false;
		}
	}
	
	private boolean setXMLAttributes (InternalLinkType il, NamedNodeMap domAttributes) {
		
		boolean success = true;
		if(domAttributes != null) {
			for(int i = 0; i < domAttributes.getLength(); i++) {
				Node domAttr = domAttributes.item(i);					
				if(domAttr != null) {
					if(!setXMLAttribute(il, domAttr)) {
						success = false;
						break;
					}
				}
			}
		}
		
		return success;
	}
	
	/**
	 * set the fields of caex basic object for the given caex object from the given XML node
	 * @param obj
	 * @param node
	 * @return
	 */
	private void setCaexBasicObj (CAEXBasicObject obj, Node node) {
			
		NodeList children = node.getChildNodes();
		if(children != null && children.getLength() > 0) {
			for(int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if(DescriptionType.class.getSimpleName().contains(child.getNodeName())) {
					DescriptionType desc = CAEX215Factory.eINSTANCE.createDescriptionType();
					desc.setValue(child.getNodeValue());
				}
				
				else if(VersionType.class.getSimpleName().contains(child.getNodeName())) {
					VersionType version = CAEX215Factory.eINSTANCE.createVersionType();
					version.setValue(child.getNodeValue());
				}
				
				//TODO: revision
				else if(RevisionType.class.getSimpleName().contains(child.getNodeName())) {
					RevisionType revision = CAEX215Factory.eINSTANCE.createRevisionType();
				}
				
				else if(CopyrightType.class.getSimpleName().contains(child.getNodeName())) {
					CopyrightType copyright = CAEX215Factory.eINSTANCE.createCopyrightType();
					copyright.setValue(child.getNodeValue());
				}
				
				//TODO
				else if(child.getNodeName().equals("AdditionalInformation")) {
					
				}
				
//				else {
//					System.err.println(this.getClass().getEnclosingMethod().getName() + ": handling caex basic object failed, unknown node type [" + child.getNodeName() + "] !");
//				}								
			}
		}
	
	}
	
	private boolean setCaexAttribute (AttributeType attr, Node node) {
				
		setCaexBasicObj(attr, node);
		
		if(!setXMLAttributes(attr, node.getAttributes()))
			return false;
		
		// handle XML children of the attribute node
		NodeList children = node.getChildNodes();
		if(children != null && children.getLength() > 0) {
			for(int i = 0; i < children.getLength(); i++) {
				
				Node child = children.item(i);
				
				if(AttributeType.class.getSimpleName().contains(child.getNodeName())) {
					AttributeType subAttr = CAEX215Factory.eINSTANCE.createAttributeType();
					setCaexAttribute(subAttr, child);
					attr.getAttribute().add(subAttr);
				}
				
				else if(child.getNodeName().equals("Value")) {
					attr.setValue(AMLHelperFunctions.toAMLAnyType(child.getTextContent()));
				}
				
				else if(child.getNodeName().equals("DefaultValue")) {
					attr.setDefaultValue(AMLHelperFunctions.toAMLAnyType(child.getTextContent()));
				}
				
				//TODO
				else if(RefSemanticType.class.getSimpleName().contains(child.getNodeName())) {
//					attr.getRefSemantic().add((RefSemanticType) child);
				}
				
				//TODO
				else if(AttributeValueRequirementType.class.getSimpleName().contains(child.getNodeName())) {
//					attr.getConstraint().add((AttributeValueRequirementType) child);
				}
			}
		}
		
		return true;
	}
	
	private boolean setCaexRR (RoleRequirementsType rr, Node node) {
		
		setCaexBasicObj(rr, node);
		
		if(!setXMLAttributes(rr, node.getAttributes())) {
			return false;
		}
		
		// handle XML children of the SRC node
		NodeList children = node.getChildNodes();
		if(children != null && children.getLength() > 0) {
			for(int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);				
				if(AttributeType.class.getSimpleName().contains(child.getNodeName())) {
					AttributeType attr = CAEX215Factory.eINSTANCE.createAttributeType();
					setCaexAttribute(attr, child);
					rr.getAttribute().add(attr);
				}
				
				else if(ExternalInterfaceType.class.getSimpleName().contains(child.getNodeName())) {
					ExternalInterfaceType ei = CAEX215Factory.eINSTANCE.createExternalInterfaceType();
					setCaexEI(ei, child);
					rr.getExternalInterface().add(ei);
				}
				
				else{
					StackTraceElement[] stackTrace = new Throwable().getStackTrace();
					System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML node [" + child.getNodeName() + "] in RR [" + rr.toString() + "]");
				}
			}
		}
		
		return true;
	}
	
	private boolean setCaexSRC (SupportedRoleClassType src, Node node) {
		setCaexBasicObj(src, node);
		
		if(!setXMLAttributes(src, node.getAttributes())) {
			return false;
		}
		
		// handle XML children of the SRC node
		NodeList children = node.getChildNodes();
		if(children != null && children.getLength() > 0) {
			for(int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);				
				if(MappingType.class.getSimpleName().contains(child.getNodeName())) {
					MappingType mapping = CAEX215Factory.eINSTANCE.createMappingType();
					setCaexMapping(mapping, child);
					src.setMappingObject(mapping);
				}
				
				else{
					StackTraceElement[] stackTrace = new Throwable().getStackTrace();
					System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML node [" + child.getNodeName() + "] in SRC [" + src.toString() + "]");
				}
			}
		}
		
		return true;
	}
	
	private boolean setCaexLink (InternalLinkType il, Node node) {
		
		setCaexBasicObj(il, node);
		
		if(!setXMLAttributes(il, node.getAttributes()))
			return false;
		
		return true;
	}
	
	// TODO
	private boolean setCaexMapping (MappingType mapping, Node node) {
	
		return true;
	}
	
	private boolean setCaexEI (ExternalInterfaceType ei, Node node) {
		setCaexBasicObj(ei, node);
		
		if(!setXMLAttributes(ei, node.getAttributes()))
			return false;
		
		// handle XML children of the EI node
		NodeList children = node.getChildNodes();
		if(children != null && children.getLength() > 0) {						
			for(int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if(AttributeType.class.getSimpleName().contains(child.getNodeName())) {
					AttributeType attr = CAEX215Factory.eINSTANCE.createAttributeType();
					setCaexAttribute(attr, child);
					ei.getAttribute().add(attr);
				}
				
				else{
					StackTraceElement[] stackTrace = new Throwable().getStackTrace();
					System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML node [" + child.getNodeName() + "] in EI [" + ei.toString() + "]");
				}
			}
		}
		
		return true;
	}
	
	private boolean setCaexIE (InternalElementType ie, Node node) {
		setCaexBasicObj(ie, node);
		
		if(!setXMLAttributes(ie, node.getAttributes())) {
			return false;
		}
		
		// handle XML children of the IE node
		NodeList children = node.getChildNodes();
		if(children != null && children.getLength() > 0) {
			for(int i = 0; i < children.getLength(); i++) {
				
				Node child = children.item(i);
				
				if(AttributeType.class.getSimpleName().contains(child.getNodeName())) {
					AttributeType attr = CAEX215Factory.eINSTANCE.createAttributeType();
					setCaexAttribute(attr, child);
					ie.getAttribute().add(attr);
				}
				
				else if(ExternalInterfaceType.class.getSimpleName().contains(child.getNodeName())) {
					ExternalInterfaceType ei = CAEX215Factory.eINSTANCE.createExternalInterfaceType();
					setCaexEI(ei, child);
					ie.getExternalInterface().add(ei);
				}
				
				else if(InternalElementType.class.getSimpleName().contains(child.getNodeName())) {
					InternalElementType subIe = CAEX215Factory.eINSTANCE.createInternalElementType();
					setCaexIE(subIe, child);
					ie.getInternalElement().add(subIe);
				}
				
				else if(RoleRequirementsType.class.getSimpleName().contains(child.getNodeName())) {
					RoleRequirementsType rr = CAEX215Factory.eINSTANCE.createRoleRequirementsType();
					setCaexRR(rr, child);
					ie.setRoleRequirements(rr);
				}
				
				else if(SupportedRoleClassType.class.getSimpleName().contains(child.getNodeName())) {
					SupportedRoleClassType src = CAEX215Factory.eINSTANCE.createSupportedRoleClassType();
					setCaexSRC(src, child);
					ie.getSupportedRoleClass().add(src);
				}
				
				else if(InternalLinkType.class.getSimpleName().contains(child.getNodeName())) {
					InternalLinkType il = CAEX215Factory.eINSTANCE.createInternalLinkType();
					setCaexLink(il, child);
					ie.getInternalLink().add(il);
				}
				
				else if(MappingType.class.getSimpleName().contains(child.getNodeName())) {
					MappingType mapping = CAEX215Factory.eINSTANCE.createMappingType();
					setCaexMapping(mapping, child);
					ie.setMappingObject(mapping);
				}
				
				else{
					StackTraceElement[] stackTrace = new Throwable().getStackTrace();
					System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": unknown XML node [" + child.getNodeName() + "] in EI [" + ie.toString() + "]");
				}
			}
		}
		
		
		return true;
	}
	
	private CAEXObject toCaex (Node node) {
		
		if(node instanceof BXElem) {
			return toCaex ((BXElem) node);
		}
		
		else {//if (node instanceof BXText) {				
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": ignoring the node " + node.toString());
			return null;
		}		
	}
		
	private CAEXObject toCaex (Element dom) {
				
		// dom is caex attribute
		if(AttributeType.class.getSimpleName().contains(dom.getTagName())) {
			AttributeType attr = CAEX215Factory.eINSTANCE.createAttributeType();
			setCaexAttribute(attr, dom);
			return attr;
		}
		
		// dom is caex ei
		else if(ExternalInterfaceType.class.getSimpleName().contains(dom.getTagName())) {
			ExternalInterfaceType ei = CAEX215Factory.eINSTANCE.createExternalInterfaceType();
			setCaexEI(ei, dom);
			return ei;
		}
		
		// dom is caex ie
		else if(InternalElementType.class.getSimpleName().contains(dom.getTagName())){
			InternalElementType ie = CAEX215Factory.eINSTANCE.createInternalElementType();
			setCaexIE(ie, dom);
			return ie;
		}

		// TODO
		else if(InterfaceClassType.class.getSimpleName().contains(dom.getTagName())) {
			InterfaceFamilyType iff = CAEX215Factory.eINSTANCE.createInterfaceFamilyType();
			
			return iff;
		}
		
		// TODO
		else if(RoleClassType.class.getSimpleName().contains(dom.getTagName())) {
			RoleFamilyType rf = CAEX215Factory.eINSTANCE.createRoleFamilyType();
			
			return rf;
		}
		
		//TODO
		else if(SystemUnitClassType.class.getSimpleName().contains(dom.getTagName())) {
			SystemUnitFamilyType suf = CAEX215Factory.eINSTANCE.createSystemUnitFamilyType();
			
			return suf;
		}
		
		return null;		
	}
	
	/**
	 * convert a BaseX query result into a CAEX Instance hierarchy 
	 * @param result the BaseX query result
	 * @param library the target CAEX instance hierarchy
	 * @return whether the conversion succeeded
	 * @throws QueryException
	 */
	public boolean toCaex (Value result, CAEXObject library) throws QueryException {
		// the caex object to hold the query results as CAEX objects
		// shall be library objects: ih, rcl, icl, sucl
//		if(!(library instanceof RoleClassLibType || library instanceof InterfaceClassLibType || library instanceof SystemUnitClassLibType || library instanceof InstanceHierarchyType)) {
//			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
//			System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": query results can only be stored inside caex libraries!");
//			return false;
//		}

		if(!(library instanceof InstanceHierarchyType)) {
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": currently only supporting writing to CAEX instance hierarchy!");
			return false;
		}
		
		InstanceHierarchyType ih = (InstanceHierarchyType) library;
		Object obj = result.toJava();		
		
		if(!obj.getClass().isArray()) {
			Node node = (Node) obj;			
			InternalElementType ie = toCaexIE(node);
			if(ie == null) {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": cannot construct an IE from the given node!");
				return false;
			}else {
				ih.getInternalElement().add(ie);
			}
		
			return true;		
		}
		
		else{						
			Object[] nodes = (Object[]) obj;			
			List<GenericTreeNode<List<Node>>> roots = parser.toTrees(nodes);
			
			if(roots.size() == 1) {
				GenericTreeNode<List<Node>> root = roots.get(0);
				if(root.getChildren().isEmpty()) {
					for(Node node: root.data) {
						InternalElementType ie = toCaexIE(node);
						if(ie == null) {
							StackTraceElement[] stackTrace = new Throwable().getStackTrace();
							System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": cannot construct an IE from the given node!");
							return false;
						}else {
							ih.getInternalElement().add(ie);
						}		
					}	
				}				
			}
			else {
				int grpId = 0;
				for(GenericTreeNode<List<Node>> root : roots) {
					String grpName = "r" + String.valueOf(++grpId);
					InternalElementType rootIE = toCaexIE(root, grpName);
					ih.getInternalElement().add(rootIE);
				}	
			}			
			return true;
		}
	}
	
	/**
	 * get the caex IE from an XML node
	 * @param node
	 * @return
	 */
	private InternalElementType toCaexIE (Node node) {
		CAEXObject caexObj = toCaex(node);
		InternalElementType placeholder = CAEX215Factory.eINSTANCE.createInternalElementType();
		placeholder.setName("placeholder");
		if(caexObj instanceof InternalElementType)
			return (InternalElementType) caexObj;
		else {
			if(caexObj instanceof ExternalInterfaceType)
				placeholder.getExternalInterface().add((ExternalInterfaceType) caexObj);
			else if(caexObj instanceof AttributeType)
				placeholder.getAttribute().add((AttributeType) caexObj);
			else {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": currently only supporting writing IE, EI and Attr to CAEX instance hierarchy!");
				return null;
			}
			return placeholder;
		}
	}
	
	/**
	 * get the caex IE of a XML structure (a root node with its sub-tree)
	 * @param root the XML structure
	 * @param grpName the name of the generated IE
	 * @return
	 */
	private InternalElementType toCaexIE(GenericTreeNode<List<Node>> root, String grpName) {
		
		InternalElementType grpIE = CAEX215Factory.eINSTANCE.createInternalElementType();
		grpIE.setName(grpName);
		
		// the data of the root are CAEX objects
		for(Node node : root.data) {
			InternalElementType ie = toCaexIE(node);
			if(ie == null) {
				StackTraceElement[] stackTrace = new Throwable().getStackTrace();
				System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": cannot construct an IE from the given node!");
				return null;
			}else {
				grpIE.getInternalElement().add(ie);
			}
		}
		
		// the children of the root are placeholders
		int childIdx = 0;
		for(GenericTreeNode<List<Node>> child : root.getChildren()) {
			String subGrpName = grpName + "-" + String.valueOf(++childIdx);			
			InternalElementType childGrpIE = toCaexIE(child, subGrpName);
			grpIE.getInternalElement().add(childGrpIE);
		}
		
		return grpIE;
	}
	
}
