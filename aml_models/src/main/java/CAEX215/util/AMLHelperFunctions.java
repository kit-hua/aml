/**
 * 
 */
package CAEX215.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.eclipse.emf.ecore.xml.type.XMLTypeFactory;

import CAEX215.AttributeType;
import CAEX215.CAEXFileType;
import CAEX215.CAEXObject;
import CAEX215.ExternalInterfaceType;
import CAEX215.InstanceHierarchyType;
import CAEX215.InterfaceClassLibType;
import CAEX215.InterfaceFamilyType;
import CAEX215.InternalElementType;
import CAEX215.InternalLinkType;
import CAEX215.RoleClassLibType;
import CAEX215.RoleFamilyType;
import CAEX215.SystemUnitClassLibType;
import CAEX215.SystemUnitClassType;
import CAEX215.SystemUnitFamilyType;
import CAEX215.impl.RoleFamilyTypeImpl;

/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 *
 */
public class AMLHelperFunctions {
	
	// Somehow works but no idea why...
	public static AnyType toAMLAnyType (String value) {	
			
		ResourceSet resourceSet = new ResourceSetImpl();
		final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(resourceSet.getPackageRegistry());
		AnyType anytype = XMLTypeFactory.eINSTANCE.createAnyType();
//		EStructuralFeature mixedFeature = extendedMetaData.getMixedFeature(anytype.eClass());
		EStructuralFeature valueAttribute = extendedMetaData.demandFeature(null, "stringValue", false);
//		EList<String> valueList = new BasicEList<String>(); 
//		valueList.add(value);
//		anytype.eSet(mixedFeature, valueList);
		anytype.eSet(valueAttribute, value);

		return anytype;
	}
	
	public static AnyType toAMLAnyType (Object value) {
		return toAMLAnyType(String.valueOf(value));
	}
	
	public static AttributeType overwriteAttribute (AttributeType attr, Object value) {
		attr.setValue(toAMLAnyType(value));
		return attr;
	}
	
	public static String fromAMLAnyType(AnyType anyType) {
		if(anyType != null && anyType.getAnyAttribute().size() > 0) {
			if(anyType.getAnyAttribute().getValue(0) != null) {
				return (String) anyType.getAnyAttribute().getValue(0);					
			}
		}
		if(anyType != null && anyType.getMixed().size() > 0) {
			if(anyType.getMixed().getValue(0) != null) {
				return (String) anyType.getMixed().getValue(0);					
			}
		}
		return null;
	}
	
//	public static String getAMLAnyTypeValue(AnyType anyType) {
//		if(anyType != null && anyType.getMixed().size() > 0) {
//			if(anyType.getMixed().getValue(0) != null) {
//				return (String) anyType.getMixed().getValue(0);					
//			}
//		}
//		return null;
//	}
	
	public static String getAMLAttributeValue(AttributeType attribute) {
//		if(attribute.getValue() != null) {
//			if(attribute.getValue().getMixed().size() > 0) {
//				if(attribute.getValue().getMixed().getValue(0) != null) {
//					return (String) attribute.getValue().getMixed().getValue(0);					
//				}
//			}
//		}
//		
//		return null;
		return fromAMLAnyType(attribute.getValue());
	}
	
	public static Double getAMLAttributeValueDouble(AttributeType attribute) {		
		
		String sValue = getAMLAttributeValue(attribute);
		if(sValue != null)
			return Double.parseDouble(sValue);
		else
			return Double.NaN;
	}
	
	public static Integer getAMLAttributeValueInteger(AttributeType attribute) {
		
		String sValue = getAMLAttributeValue(attribute);
		if(sValue != null)
			return Integer.parseInt(sValue);
		else
			return null;
	}
	
	public static Boolean getAMLAttributeValueBoolean(AttributeType attribute) {
		
		String sValue = getAMLAttributeValue(attribute);
		if(sValue != null)
			return Boolean.parseBoolean(sValue);
		else
			return null;
	}
	
	/**
	 * Whether the given IH has an object with the given ID
	 * @param ih The given IH
	 * @param id The given ID, for IE/EI, it is the UUID, for Attributes, it is the UUID/[ATTRIBUTE-NAME-PATH]
	 * @return
	 */
	public static CAEXObject getObjectWithID (InstanceHierarchyType ih, String id) {
		
		boolean isLookingForAttribute = false;
		String objId = id; 
		if(objId.contains("/")) {
			objId = objId.substring(0, objId.indexOf("/"));
			isLookingForAttribute = true;
		}
		
		// if we are looking for IE/EI
		if(!isLookingForAttribute) {
			for(InternalElementType ie : ih.getInternalElement()) {
				CAEXObject objWithId = getObjectWithID(ie, objId);
				if (objWithId != null)
					return objWithId;
			}	
		}
		
		// if we are looking for attribute
		else {
			
			for(InternalElementType ie : ih.getInternalElement()) {
				CAEXObject objWithId = getObjectWithID(ie, objId);
			
				// we found the object holding the attribute
				if (objWithId != null) {
					
					// decompose the attribute name path
					String attributeNamePath = id.substring(id.indexOf("/")+1, id.length());
					String[] attributeNames = attributeNamePath.split("/");
					
					if(objWithId instanceof InternalElementType) {
						InternalElementType ieWithId = (InternalElementType) objWithId;
						AttributeType attr = getAttributeWithNamePath(ieWithId.getAttribute(), attributeNames);
						if(attr != null)
							return attr;						
					}
					
					else if (objWithId instanceof ExternalInterfaceType) {
						InternalElementType eiWithId = (InternalElementType) objWithId;
						AttributeType attr = getAttributeWithNamePath(eiWithId.getAttribute(), attributeNames);
						if(attr != null)
							return attr;
					}
				}					
			}						
		}
		
		// if failed, return null
		return null;
	}
	
	/**
	 * From a set of attributes (from an IE or EI), find a (nested) attribute which corresponds to the namePath
	 * @param attributes
	 * @param namePath
	 * @return
	 */
	private static AttributeType getAttributeWithNamePath (EList<AttributeType> attributes, String[] namePath) {
		
		for(AttributeType attr : attributes) {			
			if(attr.getName().equals(namePath[0])) {
				
				if(namePath.length == 1) {
					return attr;
				}
				
				else {
					String[] restNamePath = new String [namePath.length-1];
					// get the rest of the name path
					for(int i = 1; i < namePath.length; i++) {
						restNamePath[i-1] = namePath[i];
					}
					return getAttributeWithNamePath(attr.getAttribute(), restNamePath);
				}								
			}
		}
		
		return null;
	}
	
	private static CAEXObject getObjectWithID (InternalElementType ie, String id) {							
		
		if(ie.getID().equals(id)) {
			return ie;
		}
		
		else {
			
			for (ExternalInterfaceType childEI : ie.getExternalInterface()) {
				if(childEI.getID().equals(id)) 
					return childEI;
			}
			
			for (InternalElementType childIE : ie.getInternalElement()) {
				CAEXObject obj = getObjectWithID(childIE, id);
				if(obj != null)
					return obj;
			}			
		}
		return null;
	}
	
	public static ExternalInterfaceType getEIById(String id, CAEXFileType aml) {
		for(SystemUnitClassLibType sucl : aml.getSystemUnitClassLib()) {
			for(SystemUnitFamilyType suf : sucl.getSystemUnitClass()) {
				ExternalInterfaceType ei = getEIFromSUC(id, suf);
				if(ei != null) {
					return ei;
				}
			}
		}
		
		for(InstanceHierarchyType ih : aml.getInstanceHierarchy()) {
			for(InternalElementType ie : ih.getInternalElement()) {
				ExternalInterfaceType ei = getEIFromIE(id, ie);
				if(ei != null) {					
					return ei;
				}
			}
		}
		return null;
	}
	
	
	private static ExternalInterfaceType getEIFromIE(String id, InternalElementType ie) {
		boolean found = false;
		for(ExternalInterfaceType candidate : ie.getExternalInterface()) {
			if(candidate.getID().equals(id)) {
				found = true;
				return candidate;
			} 
		}
		
		if(!found) {
			for(InternalElementType child : ie.getInternalElement()) {
				ExternalInterfaceType candidate = getEIFromIE(id, child);
				if(candidate != null)
					return candidate;
			}
		}
		
		return null;
	}
	
	
	private static ExternalInterfaceType getEIFromSUC(String id, SystemUnitFamilyType suf) {
		
		boolean found = false;
		for(ExternalInterfaceType candidate : suf.getExternalInterface()) {
			if(candidate.getID().equals(id)) {
				found = true;
				return candidate;
			} 
		}
		
		if(!found) {
			for(SystemUnitFamilyType child : suf.getSystemUnitClass()) {
				ExternalInterfaceType candidate = getEIFromSUC(id, child);
				if(candidate != null)
					return candidate;
			}		
		}		
		return null;
	}
	
	public static boolean sucHasEI (SystemUnitClassType suc, ExternalInterfaceType ei) {
		
		if(suc.getExternalInterface().contains(ei)) {
			return true;
		}
		
		for(InternalElementType ie : suc.getInternalElement()) {
			if(sucHasEI(ie, ei))
				return true;
		}
		
		return false;
	}
	
	public static CAEXObject getRootOfEI (ExternalInterfaceType ei, CAEXFileType aml) {
		
		for(SystemUnitClassLibType sucl : aml.getSystemUnitClassLib()) {
			for(SystemUnitFamilyType suf : sucl.getSystemUnitClass()) {
				if(sucHasEI(suf, ei))
					return suf;
			}
		}
		
		for(InstanceHierarchyType ih : aml.getInstanceHierarchy()) {
			for(InternalElementType ie : ih.getInternalElement()) {
				if(sucHasEI(ie, ei))
					return ie;
			}
		}
		
		return null;
	}
	
	public static SystemUnitClassType getParentOfEI(ExternalInterfaceType ei, CAEXFileType aml) {
		for(SystemUnitClassLibType sucl : aml.getSystemUnitClassLib()) {
			for(SystemUnitFamilyType suf : sucl.getSystemUnitClass()) {
				SystemUnitClassType parent = getParentOfEIFromSUC(ei, suf);
				if(parent != null) {
					return parent;
				}
			}
		}
		
		for(InstanceHierarchyType ih : aml.getInstanceHierarchy()) {
			for(InternalElementType ie : ih.getInternalElement()) {
				SystemUnitClassType parent = getParentOfEIFromIE(ei, ie);
				if(parent != null) {
					return parent;
				}
			}
		}
		return null;
	}
	
	private static SystemUnitClassType getParentOfEIFromIE(ExternalInterfaceType ei, InternalElementType ie) {
		boolean found = false;
		
		for(ExternalInterfaceType candidate : ie.getExternalInterface()) {
			if(ei.getID().equals(candidate.getID())) {
				found = true;
				return ie;
			}
		}
		
		if(!found) {
			for(InternalElementType child : ie.getInternalElement()) {
				SystemUnitClassType parent = getParentOfEIFromIE(ei, child);
				if(parent != null)
					return parent;
			}
		}
		
		return null;
	}
	
	private static SystemUnitClassType getParentOfEIFromSUC(ExternalInterfaceType ei, SystemUnitFamilyType suf) {
		
		boolean found = false;
		for(ExternalInterfaceType candidate : suf.getExternalInterface()) {
			if(ei.getID().equals(candidate.getID())) {
				found = true;
				return suf;
			}				
		}
		
		if(!found) {
			for(SystemUnitFamilyType child : suf.getSystemUnitClass()) {
				SystemUnitClassType parent = getParentOfEIFromSUC(ei, child);
				if(parent != null)
					return parent;
			}			
		}		
		
		return null;
	}
	
	
	public static List<InternalLinkType> getAllLinks(CAEXFileType aml) {
		List<InternalLinkType> links = new ArrayList<InternalLinkType>();
		for(SystemUnitClassLibType sucl : aml.getSystemUnitClassLib()) {
			for(SystemUnitFamilyType suf : sucl.getSystemUnitClass()) {
				links.addAll(getAllLinksFromSUC(suf));
			}
		}
		
		for(InstanceHierarchyType ih : aml.getInstanceHierarchy()) {
			for(InternalElementType ie : ih.getInternalElement())
				links.addAll(getAllLinksFromIE(ie));
		}		
		return links;
	}
	
	private static List<InternalLinkType> getAllLinksFromIE(InternalElementType ie) {
		List<InternalLinkType> links = new ArrayList<InternalLinkType>();
		for(InternalLinkType il : ie.getInternalLink())
			links.add(il);
		for(InternalElementType child : ie.getInternalElement())
			links.addAll(getAllLinksFromIE(child));
		return links;
	}
	
	
	private static List<InternalLinkType> getAllLinksFromSUC(SystemUnitFamilyType suf) {
		List<InternalLinkType> links = new ArrayList<InternalLinkType>();
		for(InternalLinkType il : suf.getInternalLink())
			links.add(il);
		for(InternalElementType ie : suf.getInternalElement())
			links.addAll(ie.getInternalLink());
		for(SystemUnitFamilyType child : suf.getSystemUnitClass())
			links.addAll(getAllLinksFromSUC(child));		
		return links;
	}		
	
	public static InternalElementType getIEByID(String id, CAEXFileType aml) {
		for(SystemUnitClassLibType sucl : aml.getSystemUnitClassLib()) {
			for(SystemUnitFamilyType suf : sucl.getSystemUnitClass()) {
				InternalElementType ie = getIEFromSUCByID(id, suf);
				if(ie != null)
					return ie;
			}
		}
		
		for(InstanceHierarchyType ih : aml.getInstanceHierarchy()) {
			for(InternalElementType ie : ih.getInternalElement()) {
				InternalElementType candidate = getIEFromIEByID(id, ie);
				if(candidate != null)
					return candidate;
			}
		}
		
		return null;
	}
	
	private static InternalElementType getIEFromIEByID(String id, InternalElementType ie) {
		if(ie.getID().equals(id)){
			return ie;
		}
		
		for(InternalElementType child : ie.getInternalElement()) {
			InternalElementType candidate = getIEFromIEByID(id, child);
			if(candidate != null)
				return candidate;
		}
		
		return null;
	}
	
	private static InternalElementType getIEFromSUCByID(String id, SystemUnitFamilyType suf) {
		for(InternalElementType ie : suf.getInternalElement()) {
			if(ie.getID().equals(id)){
				return ie;
			}
		}
		
		for(SystemUnitFamilyType child : suf.getSystemUnitClass()) {
			InternalElementType ie = getIEFromSUCByID(id, child);
			if(ie != null)
				return ie;
		}
			
		
		return null;
	}
	
	/**
	 * get the parent role class of the given one, by traversing the subsumption hierarchy of the refBaseClassPath
	 * @param ift
	 * @param aml
	 * @return
	 */
	public static RoleFamilyType getParent (RoleFamilyType rf, CAEXFileType aml) {
		String path = rf.getRefBaseClassPath();
		
		// if path is null: no parent
		if(path == null)
			return null;
		
		String baseName = "";
		RoleClassLibType rcl = null;
		
		// if the path is an absolute path
		if(path.contains("/")) {
			String libName = getLibNameFromPath(path);
			for(RoleClassLibType lib : aml.getRoleClassLib()) {
				if(lib.getName().equals(libName))
					rcl = lib; 
			}
			baseName = getClassNameFromPath(path);
		}
		
		// the path is relative: the super class is the parent 
		else {
			rcl = getLib(rf, aml);			 
			baseName = path;	
		}
					
		// if rcl is null: the lib of the parent is outside of the aml file
		if(rcl == null) {
			System.err.println("Cannot find parent of role class " + rf.getName() + " in the given AML file");
		}			
		
		RoleFamilyType parent = find(rcl, baseName);
		
		if(parent == null) {
			System.err.println("Cannot find parent of role class " + rf.getName() + " in the given AML file");
			return null;
		}
		
		return parent;
	}
	
	/**
	 * get the parent interface class of the given one, by traversing the subsumption hierarchy of the refBaseClassPath
	 * @param ift
	 * @param aml
	 * @return
	 */
	public static InterfaceFamilyType getParent (InterfaceFamilyType ift, CAEXFileType aml) {
		String path = ift.getRefBaseClassPath();
		
		// if path is null: no parent
		if(path == null)
			return null;
		
		String baseName = "";
		InterfaceClassLibType icl = null;
		
		// if the path is an absolute path
		if(path.contains("/")) {
			String libName = getLibNameFromPath(path);
			for(InterfaceClassLibType lib : aml.getInterfaceClassLib()) {
				if(lib.getName().equals(libName))
					icl = lib; 
			}
			baseName = getClassNameFromPath(path);
		}
		
		// the path is relative: the super class is the parent 
		else {
			icl = getLib(ift, aml);			 
			baseName = path;	
		}
					
		// if icl is null: the lib of the parent is outside of the aml file
		if(icl == null) {
			System.err.println("Cannot find parent of interface class " + ift.getName() + " in the given AML file");
		}			
		
		InterfaceFamilyType parent = find(icl, baseName);
		
		if(parent == null) {
			System.err.println("Cannot find parent of interface class " + ift.getName() + " in the given AML file");
			return null;
		}
		
		return parent;
	}
	
	/**
	 * get the interface class lib of the given interface class: assuming it is within the given AML file
	 * @param ift
	 * @param aml
	 * @return
	 */
	public static InterfaceClassLibType getLib (InterfaceFamilyType ift, CAEXFileType aml) {
		for (InterfaceClassLibType icl : aml.getInterfaceClassLib()) {
			if(contains(icl, ift)) {
				return icl;
			}
		}
		
		return null;
	}
	
	/**
	 * get the role class lib of the given role class: assuming it is within the given AML file
	 * @param rf
	 * @param aml
	 * @return
	 */
	public static RoleClassLibType getLib (RoleFamilyType rf, CAEXFileType aml) {
		for (RoleClassLibType rcl : aml.getRoleClassLib()) {
			if(contains(rcl, rf)) {
				return rcl;
			}
		}
		
		return null;
	}
	
	/**
	 * get all interfaces in the given lib
	 * @param icl
	 * @return
	 */
	public static Set<InterfaceFamilyType> getAllInterfaces (InterfaceClassLibType icl) {
			
		Set<InterfaceFamilyType> all = new HashSet<InterfaceFamilyType>();
		for(InterfaceFamilyType ic : icl.getInterfaceClass()) {
			all.add(ic);
			all.addAll(getAllSubInterfaces(ic));			
		}				
		
		return all;
	}
	
	/**
	 * get all sub/descendant interfaces of the given interface class by traversing its subsumption hierarchy
	 * @param ift
	 * @return
	 */
	public static Set<InterfaceFamilyType> getAllSubInterfaces (InterfaceFamilyType ift){
		Set<InterfaceFamilyType> all = new HashSet<InterfaceFamilyType>();
		for(InterfaceFamilyType ic : ift.getInterfaceClass()) {
			all.add(ic);
			all.addAll(getAllSubInterfaces(ic));
		}		
		
		return all;
	}
	
	/**
	 * try to find a role class in a library by name: assuming unique name within one lib 
	 * @param rcl
	 * @param name
	 * @return
	 */
	public static RoleFamilyType find (RoleClassLibType rcl, String name) {
		for(RoleFamilyType rc : getAllRoles(rcl)) {
			if(rc.getName().equals(name))
				return rc;			
		}
		
		return null;
	}
	
	
	/**
	 * try to find an interface class in a library by name: assuming unique name within one lib 
	 * @param icl
	 * @param name
	 * @return
	 */
	public static InterfaceFamilyType find (InterfaceClassLibType icl, String name) {
		for(InterfaceFamilyType ic : getAllInterfaces(icl)) {
			if(ic.getName().equals(name))
				return ic;			
		}
		
		return null;
	}
	
	/**
	 * check if the given interface class lib contains the given interface class
	 * assuming that name is unique within one library (AML conform)
	 * @param icl
	 * @param ift
	 * @return
	 */
	public static boolean contains (InterfaceClassLibType icl, InterfaceFamilyType ift) {
		for(InterfaceFamilyType ic : getAllInterfaces(icl)) {
			if(ic.getName().equals(ift.getName()))
				return true;			
		}
		
		return false;
	}
	
	/**
	 * check if the given interface class lib contains the given interface class by name
	 * assuming that name is unique within one library (AML conform)
	 * @param icl
	 * @param ift
	 * @return
	 */
	public static boolean contains (InterfaceClassLibType icl, String interfaceName) {
		for(InterfaceFamilyType ic : getAllInterfaces(icl)) {
			if(ic.getName().equals(interfaceName))
				return true;			
		}
		
		return false;
	}
	
	/**
	 * get all sub/descendant roles of a role class by traversing its subsumption hierarchy
	 * @param rf
	 * @return
	 */
	public static Set<RoleFamilyType> getAllSubRoles (RoleFamilyType rf) {
		Set<RoleFamilyType> all = new HashSet<RoleFamilyType>();
		for(RoleFamilyType rc : rf.getRoleClass()) {
			all.add(rc);
			all.addAll(getAllSubRoles(rc));
		}		
		
		return all;
	}
	
	/**
	 * get all role classes in the given lib
	 * @param rcl
	 * @return
	 */
	public static Set<RoleFamilyType> getAllRoles (RoleClassLibType rcl) {		
		Set<RoleFamilyType> all = new HashSet<RoleFamilyType>();
		for(RoleFamilyType rc : rcl.getRoleClass()) {
			all.add(rc);
			all.addAll(getAllSubRoles(rc));			
		}				
		
		return all;
	}
	
	/**
	 * check if the given role class lib contains the given role class: assuming unique name within the lib
	 * @param rcl
	 * @param rf
	 * @return
	 */
	public static boolean contains (RoleClassLibType rcl, RoleFamilyType rf) {
		for(RoleFamilyType rc : getAllRoles(rcl)) {
			if(rc.getName().equals(rf.getName()))
				return true;			
		}
		
		return false;
	}
	
	/**
	 * check if the given role class lib contains the given role class by name: assuming unique name within the lib
	 * @param rcl
	 * @param rf
	 * @return
	 */
	public static boolean contains (RoleClassLibType rcl, String roleName) {
		for(RoleFamilyType rc : getAllRoles(rcl)) {
			if(rc.getName().equals(roleName))
				return true;			
		}
		
		return false;
	}
	
	
	public static InterfaceFamilyType findInterfaceByName (String className, String libName, CAEXFileType aml) {
		for(InterfaceClassLibType icl : aml.getInterfaceClassLib()) {
			if(icl.getName().equals(libName)) {
				return find(icl, className);
			}
		}
		
		System.err.println("Cannot find the interface class " + libName + ":" + className + " in the aml file!");
		return null;
	}
	
	public static InterfaceFamilyType findInterface (String refBaseInterfaceClassPath, CAEXFileType aml) {
		String libName = getLibNameFromPath(refBaseInterfaceClassPath);
		String className = getClassNameFromPath(refBaseInterfaceClassPath);
		return findInterfaceByName(className, libName, aml);
	}
	
	
	public static RoleFamilyType findRoleByName (String className, String libName, CAEXFileType aml) {
		for(RoleClassLibType rcl : aml.getRoleClassLib()){
			if(rcl.getName().equals(libName)) {
				return find(rcl, className);
			}
		}
		System.err.println("Cannot find the role class " + libName + ":" + className + " in the aml file!");
		return null;
	}
	
	public static RoleFamilyType findRole (String refBaseRoleClassPath, CAEXFileType aml) {
		String libName = getLibNameFromPath(refBaseRoleClassPath);
		String className = getClassNameFromPath(refBaseRoleClassPath);
		return findRoleByName(className, libName, aml);
	}
	
	
	
	
	
	
	private static String getClassNameFromPath (String path) {
		String[] tokens = path.split("/");
		int num_tokens = tokens.length;
		return tokens[num_tokens-1];		
	}
	
	private static String getLibNameFromPath (String path) {
		String[] tokens = path.split("/");
		return tokens[0];		
	}
}
