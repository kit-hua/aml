package translation.ontology;
/**
 * 
 */


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import CAEX215.AttributeType;
import CAEX215.CAEXFileType;
import CAEX215.CAEXObject;
import CAEX215.ExternalInterfaceType;
import CAEX215.InstanceHierarchyType;
import CAEX215.InterfaceClassLibType;
import CAEX215.InterfaceClassType;
import CAEX215.InterfaceFamilyType;
import CAEX215.InternalElementType;
import CAEX215.RoleClassLibType;
import CAEX215.RoleFamilyType;
import CAEX215.RoleRequirementsType;
import CAEX215.SupportedRoleClassType;
import CAEX215.SystemUnitClassLibType;
import CAEX215.SystemUnitFamilyType;
import CAEX215.impl.RoleClassLibTypeImpl;
import CAEX215.util.AMLHelperFunctions;
import constants.AMLObjectPropertyIRIs;
import constants.Consts;
import parser.AMLParser;

/**
 * @author hua
 *
 */
public class AML2OWLOntology implements IAML2OWL{


	public OWLOntologyManager manager;
	public OWLDataFactory factory;
//	public OWLReasonerFactory rfactory;
	public OWLReasoner reasoner;
	public OWLOntology aml_ont, output_ont;
//	public PrefixManager pm;	
//	private IFHandler if_handler;
//	private RFHandler rf_handler;
	private SUCHandler suc_handler;	
//	private IEHandler ie_handler;
	private AttributeHandler attr_handler;
	private CAEXFileType aml;
	
	// a map holding the ontology class name for each AML interface class
	private Map<InterfaceFamilyType, String> interfaceNames = new HashMap<InterfaceFamilyType, String>();
	// a map holding the ontology class name for each AML role class
	private Map<RoleFamilyType, String> roleNames = new HashMap<RoleFamilyType, String>();
	
	public AML2OWLOntology(CAEXFileType aml, String aml_ont_filename) throws OWLOntologyCreationException {
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = this.manager.getOWLDataFactory();
//		this.rfactory = new StructuralReasonerFactory();
//		this.pm = new DefaultPrefixManager(Consts.importer_pref);
		
		this.aml = aml;
		
        if(aml_ont_filename != null) {
        		this.aml_ont = this.load(aml_ont_filename);
//        		this.reasoner = rfactory.createReasoner(aml_ont);
        }
		
//		new AttributeHandler(manager, Consts.importer_pref);
//		this.if_handler = new IFHandler(manager, Consts.aml_pref);
//		this.rf_handler = new RFHandler(manager, Consts.aml_pref);
		this.suc_handler = new SUCHandler(manager, Consts.aml_pref);
//		this.ie_handler = new IEHandler(manager, Consts.aml_pref);
		this.attr_handler = new AttributeHandler(manager, Consts.aml_pref);
	}
	
	/**
	 * Create an ontology with given IRI
	 * @param iri
	 * @throws OWLOntologyCreationException
	 */
	public void createOnt(IRI iri) throws OWLOntologyCreationException {
		this.output_ont = manager.createOntology(iri);
//		OWLImportsDeclaration decl = factory.getOWLImportsDeclaration(this.manager.getOntologyDocumentIRI(this.aml_ont));			
//		manager.applyChange(new AddImport(new_ont, decl));		
	}
	
	/**
	 * Load an ontology from file
	 * @param filename
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public OWLOntology load(String filename) throws OWLOntologyCreationException {
        
//		System.out.println("\n\n=============== Loading OWL ontology from file system ================\n");
		File file = new File(filename);
        // Now load the local copy
        OWLOntology ont = this.manager.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded ontology: " + ont);
        // We can always obtain the location where an ontology was loaded from
        IRI documentIRI = this.manager.getOntologyDocumentIRI(ont);
        System.out.println("from: " + documentIRI);      
        return ont;
	}
	
	/**
	 * Save the current ontology using the given format 
	 * @param ont
	 * @param format
	 * @param filename
	 * @throws IOException
	 * @throws OWLOntologyStorageException
	 */
	public void save(OWLOntology ont, OWLDocumentFormat format, String filename) throws IOException, OWLOntologyStorageException {
		
//		System.out.println("\n\n=============== Saving OWL ontology to file system ================\n");
		
		File file = new File(filename);
        
//        OWLDocumentFormat f = this.manager.getOntologyFormat(ont);  
//        System.out.println("current format: " + f.toString());           
        
//      this.manager.saveOntology(ont, new SystemOutDocumentTarget());        
        this.manager.saveOntology(ont, format, IRI.create(file.toURI()));      
//        System.out.println("successfully saved ontology to file: " + filename);
	}
	

	public boolean verifiy() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Create an OWL class using a name as its ID
	 * @param name
	 * @return
	 */
	private OWLClass createOWLClassByName(String name, String ns) {
		return factory.getOWLClass(IRI.create(ns + name));		 
	}	
	
	
	private OWLObjectProperty createOWLObjectPropertyByName (String name, String ns) {
		return factory.getOWLObjectProperty(IRI.create(ns + name));
	}
	
	private String getClassNameFromPath (String path) {
		String[] tokens = path.split("/");
		int num_tokens = tokens.length;
		return tokens[num_tokens-1];		
	}
	
	private String getLibNameFromPath (String path) {
		String[] tokens = path.split("/");
		return tokens[0];		
	}
	

	/**
	 * Generate the class iri for the given interface family type (interface class)
	 * 1. if the path is a single class name: then it is a relative path
	 * 		- need to go to the super class
	 * 		- get the name of the super class (recursively or cached)
	 * 2. if the path is a complete path: it is a absolute path
	 *		- 2.1. if the path ends with a class in the same library: [path]/[classname]
	 *		- 2.2. if the path ends with a class in a different library: [libname]/[classname] 
	 * 3. if the path is empty, then it must be a top level class: [libname]/[classname]
	 * @param ift
	 * @param icl
	 * @return
	 */
	private IRI getClassIri (InterfaceFamilyType ift, InterfaceClassLibType icl) {
		
		if(ift.getName().contains("COLLADA"))
			System.out.println();
		String classname = "";
		
		if(interfaceNames.containsKey(ift))
			return IRI.create(Consts.aml_pref + interfaceNames.get(ift));
		
		if(ift.getRefBaseClassPath() != null) {						

			// 1. relative path
			if(!ift.getRefBaseClassPath().contains("/")) {
				InterfaceFamilyType base = AMLHelperFunctions.find(icl, ift.getRefBaseClassPath());
				if(base != null) {
					// if the super class name exists, build the current one
					if(interfaceNames.containsKey(base)) {
						classname = interfaceNames.get(base) + "/" + ift.getName();
					}
					// otherwise, recursively generate the super class name: shall not happen since we traverse top-down in the subsumption hierarchy
					else {
						classname = getClassIri(base, icl) + "/" + ift.getName();
					}
				}				
			} 
			
			// 2. absolute path			
			else{
				
				String libName = getLibNameFromPath(ift.getRefBaseClassPath());
				
				// 2.1. same lib: [path]/[name]
				if(libName.equals(icl.getName())) {
					classname = ift.getRefBaseClassPath() + "/" + ift.getName();
				}
				
				// 2.2. different lib: [lib]/[name]
				else {
					classname = icl.getName() + "/" + ift.getName();
				}														
			}
		}		
		
		// 3. empty path
		else {
			classname = icl.getName() + "/" + ift.getName();
		}
		
		interfaceNames.put(ift, classname);
		
		return IRI.create(Consts.aml_pref + classname);
	}
	
	
	/**
	 * Generate the class iri for the given role family type (role class)
	 * 1. if the path is a single class name: then it is a relative path
	 * 		- need to go to the super class
	 * 		- get the name of the super class (recursively or cached)
	 * 2. if the path is a complete path: it is a absolute path
	 *		- 2.1. if the path ends with a class in the same library: [path]/[classname]
	 *		- 2.2. if the path ends with a class in a different library: [libname]/[classname] 
	 * 3. if the path is empty, then it must be a top level class: [libname]/[classname]
	 * @param rf
	 * @param rcl
	 * @return
	 */
	private IRI getClassIri (RoleFamilyType rf, RoleClassLibType rcl) {
		
		String classname = "";
		
		if(roleNames.containsKey(rf))
			return IRI.create(Consts.aml_pref + roleNames.get(rf));
		
		if(rf.getRefBaseClassPath() != null) {						

			// 1. relative path
			if(!rf.getRefBaseClassPath().contains("/")) {
				RoleFamilyType base = AMLHelperFunctions.find(rcl, rf.getRefBaseClassPath());
				if(base != null) {
					// if the super class name exists, build the current one
					if(roleNames.containsKey(base)) {
						classname = roleNames.get(base) + "/" + rf.getName();
					}
					// otherwise, recursively generate the super class name: shall not happen since we traverse top-down in the subsumption hierarchy
					else {
						classname = getClassIri(base, rcl) + "/" + rf.getName();
					}
				}		
			} 
			
			// 2. absolute path			
			else{
				
				String libName = getLibNameFromPath(rf.getRefBaseClassPath());
				
				// 2.1. same lib: [path]/[name]
				if(libName.equals(rf.getName())) {
					classname = rf.getRefBaseClassPath() + "/" + rf.getName();
				}
				
				// 2.2. different lib: [lib]/[name]
				else {
					classname = rcl.getName() + "/" + rf.getName();
				}														
			}
		}		
		
		// 3. empty path
		else {
			classname = rcl.getName() + "/" + rf.getName();
		}
		
		roleNames.put(rf, classname);
		
		return IRI.create(Consts.aml_pref + classname);
	}
	
	
	private boolean transformIE(InternalElementType ie, OWLNamedIndividual owner) {
		
		IRI ieIri = IRI.create(Consts.aml_pref + "ie_" + ie.getName() + "_" + ie.getID());		
		OWLNamedIndividual ind_ie = factory.getOWLNamedIndividual(ieIri);
		
		// declare IE in ontology
		OWLDeclarationAxiom ax_ie = factory.getOWLDeclarationAxiom(ind_ie);		
		manager.applyChange(new AddAxiom(output_ont, ax_ie));	
		
		// handle all attributes of the IE
		for(AttributeType attr : ie.getAttribute()) {
			attr_handler.add2Owl(attr, ind_ie, output_ont);
		}
		
		// handle all interfaces of the IE
		for(ExternalInterfaceType ei : ie.getExternalInterface()) {
			transformEI(ei, ind_ie);
		}
		
		// handle RoleRequirement of the IE: class assertion axioms
		RoleRequirementsType rr = ie.getRoleRequirements();
		OWLClass role = null;
		if(rr != null) {
			RoleFamilyType rf = AMLHelperFunctions.findRole(ie.getRoleRequirements().getRefBaseRoleClassPath(), aml);
			IRI rcIri = IRI.create(Consts.aml_pref + roleNames.get(rf));
			for(OWLClass owlClass : output_ont.getClassesInSignature()) {
				if(owlClass.getIRI().equals(rcIri)) {
					role = owlClass;
				}
			}
		}
		
		// For all attributes in the AML role requirement:
		// a data property is created to constraint the Class membership of this IE to the Role Class 
		Set<OWLClassExpression> constraints = new HashSet<OWLClassExpression>();		
		if(rr != null) {
			for(AttributeType attr : rr.getAttribute()) {
				constraints.addAll(this.attr_handler.getConstraintExpressions(attr, output_ont, attr.getName()));
			}	
		}
		
		OWLClassExpression intersectedConstraints = null;		
		if(constraints.size() != 0)
			intersectedConstraints = factory.getOWLObjectIntersectionOf(constraints);
		OWLClassAssertionAxiom ax_assertion = null;
		if(role != null && intersectedConstraints != null)
		{
			OWLClassExpression constraintedRole = factory.getOWLObjectIntersectionOf(role, intersectedConstraints);
			ax_assertion = factory.getOWLClassAssertionAxiom(constraintedRole, ind_ie);			
		}else if(role != null)
		{
			ax_assertion = factory.getOWLClassAssertionAxiom(role, ind_ie);
		}else if(intersectedConstraints != null)
			ax_assertion = factory.getOWLClassAssertionAxiom(intersectedConstraints, ind_ie);
			
		if(ax_assertion != null)
			manager.applyChange(new AddAxiom(output_ont, ax_assertion));
		
		// handle the SupportedRoleClass of the iE: class assertion axiom
		Set<OWLClass> roles = new HashSet<OWLClass>();
		for(SupportedRoleClassType src : ie.getSupportedRoleClass()) {
			String role_path = src.getRefRoleClassPath();
//			String[] tokens = role_path.split("/");
//			String role_name = tokens[tokens.length-1];
			
			for(OWLClass owlClass : output_ont.getClassesInSignature()) {
				String a = owlClass.getIRI().toString();
				String b = Consts.aml_pref + role_path;
				if(owlClass.getIRI().toString().equals(Consts.aml_pref + role_path)) {
					roles.add(owlClass);
				}
			}
		}
		if(!roles.isEmpty())
			manager.applyChange(new AddAxiom(output_ont, factory.getOWLClassAssertionAxiom(factory.getOWLObjectIntersectionOf(roles), ind_ie)));
			
		// add this IE to its parent object
		OWLObjectPropertyExpression hasIE = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_IE);
		if(owner != null) {
			OWLObjectPropertyAssertionAxiom ax_hasIE = factory.getOWLObjectPropertyAssertionAxiom(hasIE, owner, ind_ie);
			manager.applyChange(new AddAxiom(output_ont, ax_hasIE));	
		}		
		
		//TODO: change to AMLClassIRI
		OWLAnnotation hasSemantic = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral("InternalElement")); 
		OWLAnnotationAssertionAxiom ax_hasSemantic = factory.getOWLAnnotationAssertionAxiom(ind_ie.getIRI(), hasSemantic);
		manager.applyChange(new AddAxiom(output_ont, ax_hasSemantic));
				
		// handle sub elements
		for(InternalElementType subIE : ie.getInternalElement()) {
			transformIE(subIE, ind_ie);
		}		
		
		return true;

	}
	
	
	private boolean transformEI(ExternalInterfaceType ei, OWLEntity owner) {
		
		IRI eiIri = null;
		if(ei.getID() != null) {
			eiIri = IRI.create(Consts.aml_pref + "ei_" + ei.getName() + "_" + ei.getID());
		}
		else {
			eiIri = IRI.create(Consts.aml_pref + "ei_" + ei.getName());
		}
		
		OWLNamedIndividual ind_ei = factory.getOWLNamedIndividual(eiIri);
		
						
		for(AttributeType attr : ei.getAttribute()) {				
				attr_handler.add2Owl(attr, ind_ei, output_ont);
		}								
		
		// declare the EI
		OWLDeclarationAxiom ax_ei = factory.getOWLDeclarationAxiom(ind_ei);
		manager.applyChange(new AddAxiom(output_ont, ax_ei));
				
		// CAEX type comment
		OWLAnnotation hasSemantic = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral("ExternalInterface")); 
		OWLAnnotationAssertionAxiom ax_hasSemantic = factory.getOWLAnnotationAssertionAxiom(ind_ei.getIRI(), hasSemantic);
		manager.applyChange(new AddAxiom(output_ont, ax_hasSemantic));
		
		// handle the EI in case it belongs to an AML class
		if(owner.getEntityType().equals(EntityType.CLASS)) {			
			OWLClass cl = (OWLClass) owner;	
			OWLSubClassOfAxiom ax_sub;
			// class of all individuals which has the given interface
			OWLClassExpression somethingHasInterface = factory.getOWLObjectHasValue(factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_EI),
	                ind_ei);				
			// make the aml class subclass of this class
			ax_sub = factory.getOWLSubClassOfAxiom(cl, somethingHasInterface);
			this.manager.applyChange(new AddAxiom(output_ont, ax_sub));
		}
		
		// handle the EI in case it belongs to an AML instance
		if(owner.getEntityType().equals(EntityType.NAMED_INDIVIDUAL)) {			
			OWLObjectPropertyExpression hasInterface = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_EI);
			OWLObjectPropertyAssertionAxiom ax_hasInterface = factory.getOWLObjectPropertyAssertionAxiom(hasInterface, (OWLNamedIndividual) owner, ind_ei);
			manager.applyChange(new AddAxiom(output_ont, ax_hasInterface));
		}		
		
		// Add class membership of this EI to its Interface Class
		if(ei.getRefBaseClassPath() != null) {
			OWLClass ic = null;
			
			InterfaceFamilyType ift = AMLHelperFunctions.findInterface(ei.getRefBaseClassPath(), aml);
			IRI iftIri = IRI.create(Consts.aml_pref + interfaceNames.get(ift));
			for(OWLClass owlClass : output_ont.getClassesInSignature()) {
				if(owlClass.getIRI().equals(iftIri)) {
					ic = owlClass;
				}
			}		
			
			OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(ic, ind_ei);
			manager.applyChange(new AddAxiom(output_ont, assertion));
		}
		
		return true;
	}
	
	private boolean transformIF(InterfaceFamilyType ift, InterfaceClassLibType icl) {
		
		OWLClass cl = this.manager.getOWLDataFactory().getOWLClass(getClassIri(ift, icl));
		
		// RefBaseClassPath will be transformed to sub class in OWL
		// The path will also constitute the IRI of the class
		if(ift.getRefBaseClassPath() != null) {	
			
			InterfaceFamilyType parent = AMLHelperFunctions.getParent(ift, aml);
			InterfaceClassLibType parentLib = AMLHelperFunctions.getLib(parent, aml);
			
			IRI parentIri = getClassIri(parent, parentLib);
			
			for(OWLClass owlClass : output_ont.getClassesInSignature()) {
				if(owlClass.getIRI().equals(parentIri)) {					
					OWLSubClassOfAxiom sc_ax = factory.getOWLSubClassOfAxiom(cl, owlClass);
					manager.applyChange(new AddAxiom(output_ont, sc_ax));
				}
			}
		}				
		
		
		// interface class as subclass of things that have these attributes
//		for(AttributeType attr : ift.getAttribute()) {
//			attribute_handler.add2Owl(attr, owl_ift, ont);
//		}			

		OWLDeclarationAxiom owl_ic = factory.getOWLDeclarationAxiom(cl);
		manager.applyChange(new AddAxiom(output_ont, owl_ic));
		

		// TODO: change to AMLClassIRI
		OWLAnnotation hasSemantic = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral("InterfaceClass")); 
		OWLAnnotationAssertionAxiom ax_hasSemantic = factory.getOWLAnnotationAssertionAxiom(cl.getIRI(), hasSemantic);
		manager.applyChange(new AddAxiom(output_ont, ax_hasSemantic));
		
		
		// Sub interfaces: the hierarchy of sub interfaces in AML has no specific semantics, will be ignored.
		// All sub interfaces will be transformed to flat structures, unless they have subclass relationship to the parent		
		for(InterfaceFamilyType sub_ift : ift.getInterfaceClass()) {
			transformIF(sub_ift, icl);
		}
		
		return true;
	}
	
	private boolean transformRF(RoleFamilyType rf, RoleClassLibType rcl) {
		
		OWLClass cl = this.manager.getOWLDataFactory().getOWLClass(getClassIri(rf, rcl));
		
		// RefBaseClassPath will be transformed to sub class in OWL
		// The path will also constitute the IRI of the class
		if(rf.getRefBaseClassPath() != null) {	
			
			RoleFamilyType parent = AMLHelperFunctions.getParent(rf, aml);
			RoleClassLibType parentLib = AMLHelperFunctions.getLib(parent, aml);
			
			IRI parentIri = getClassIri(parent, parentLib);
			
			for(OWLClass owlClass : output_ont.getClassesInSignature()) {
				if(owlClass.getIRI().equals(parentIri)) {					
					OWLSubClassOfAxiom sc_ax = factory.getOWLSubClassOfAxiom(cl, owlClass);
					manager.applyChange(new AddAxiom(output_ont, sc_ax));
				}
			}
		}	
		
		// role class as subclass of things that have these attributes
//		for(AttributeType attr : rf.getAttribute()) {
//			attribute_handler.add2Owl(attr, cl, ont);
//		}
//		
		// role class as subclass of things that have these interfaces
//		for(ExternalInterfaceType ei : rf.getExternalInterface()) {
//			ic_handler.add2Owl(ei, cl, ont);
//		}		
		
		OWLDeclarationAxiom owl_role = factory.getOWLDeclarationAxiom(cl);
		manager.applyChange(new AddAxiom(output_ont, owl_role));
		
		// TODO: change to AMLClassIRI
		OWLAnnotation hasSemantic = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral("RoleClass")); 
		OWLAnnotationAssertionAxiom ax_hasSemantic = factory.getOWLAnnotationAssertionAxiom(cl.getIRI(), hasSemantic);
		manager.applyChange(new AddAxiom(output_ont, ax_hasSemantic));
				
		for(RoleFamilyType sub_rf : rf.getRoleClass()) {
			transformRF(sub_rf, rcl);
		}	
		
		return true;
	}
	
	/**
	 * Transforms the AML InterfaceClasses inside a InterfaceClassLib to OWL Classes
	 * @param aml: the AML file
	 * @return
	 */
	private boolean transformICLibs() {		
		
		EList<InterfaceClassLibType> icls = aml.getInterfaceClassLib();		
		
		for (InterfaceClassLibType icl : icls) {
			// note the icl has only the top-level classes as direct ICs
			for(InterfaceFamilyType ift : icl.getInterfaceClass()) {
//				if_handler.add2Owl(ift, this.factory.getOWLThing(), output_ont);
				//System.out.println(ift.getName());
				transformIF(ift, icl);
			}									
		}
		return true;
	}
	
	/**
	 * Transforms the AML RoleClasses inside a RoleClassLib to OWL Classes
	 * @param aml
	 * @return
	 */
	private boolean transformRCLibs() {
		
		EList<RoleClassLibType> rcls = aml.getRoleClassLib();	
		
		for(RoleClassLibType rcl : rcls) {
//			if(rcl.getName().contains("AutomationMLBaseRoleClassLib"))
//				break;
//			System.out.println("\n" + rcl.getName() + ": \n");
			for(RoleFamilyType rf : rcl.getRoleClass()) {
//				rf_handler.add2Owl(rf, factory.getOWLThing(), output_ont);
//				System.out.println(rf.getName());
				transformRF(rf, rcl);
			}
		}
		return true;
	} 	
	
	// traverse the SUC class libs in the aml file
	private boolean transformSUCLibs() {
				
		// TODO: we assume all source sucs are in the SourceSystemUnitClassLib
		for(SystemUnitClassLibType sucl : aml.getSystemUnitClassLib()) {
//			if(sucl.getName().equalsIgnoreCase("Source") || sucl.getName().equalsIgnoreCase("Dummy")) {
				for(SystemUnitFamilyType suf : sucl.getSystemUnitClass()) {
					this.suc_handler.add2Owl(suf, factory.getOWLThing(), output_ont);
				}	
//			}				
		}		
		return true;
	}
	
	// traverse the instance hierarchies in the aml file
	private boolean transformIH() {
		
		for(InstanceHierarchyType ih : aml.getInstanceHierarchy()) {
			for(InternalElementType ie : ih.getInternalElement()) {
//				this.ie_handler.add2Owl(ie, null, output_ont);
				transformIE(ie, null);
			}
		}
		
		return true;
	}
	

	public OWLOntology transform() {	
		
		// create some abstract parent classes for ordering the aml models:
		
//		// caex role class
//		OWLClass base_role = createOWLClassByName("AutomationMLBaseRole", Consts.importer_pref);
//		OWLDeclarationAxiom owl_role = factory.getOWLDeclarationAxiom(base_role);
//		manager.addAxiom(this.output_ont, owl_role);
//		
//		// caex interface class
//		OWLClass base_ic = createOWLClassByName("AutomationMLBaseInterface", Consts.importer_pref);
//		OWLDeclarationAxiom owl_ic = factory.getOWLDeclarationAxiom(base_ic);
//		manager.addAxiom(this.output_ont, owl_ic);
		
		// atomic roles
		OWLObjectProperty hasIE = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_IE);
		OWLObjectProperty hasEI = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_EI);

		/**
		 * The following part for learning transitive closure and inverse roles
		 * Which is considered to be an extension of the current work
		 */
		
		/** DO NOT REMOVE THE COMMENT FOR NOW

		// inversed atomic roles
		OWLObjectProperty isIEOf = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_OF);
		manager.addAxiom(this.output_ont, factory.getOWLInverseObjectPropertiesAxiom(hasIE, isIEOf));
		OWLObjectProperty isEIOf = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_EI_OF);
		manager.addAxiom(this.output_ont, factory.getOWLInverseObjectPropertiesAxiom(hasEI, isEIOf));
		
		// declare transtive closures
		OWLObjectProperty hasIETC = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_IE_TC);		
		OWLObjectProperty hasEITC = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_EI_TC);
		
//		OWLObjectProperty isIEOfTC = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_OF_TC);
//		OWLObjectProperty isEIOfTC = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_EI_OF_TC);
		
		// inverse transitive closures
//		manager.addAxiom(this.output_ont, factory.getOWLInverseObjectPropertiesAxiom(hasIETC, isIEOfTC));
//		manager.addAxiom(this.output_ont, factory.getOWLInverseObjectPropertiesAxiom(hasEITC, isEIOfTC));
				
		// transitivity
		manager.addAxiom(this.output_ont, factory.getOWLTransitiveObjectPropertyAxiom(hasIETC));
		manager.addAxiom(this.output_ont, factory.getOWLTransitiveObjectPropertyAxiom(hasEITC));
//		manager.addAxiom(this.output_ont, factory.getOWLTransitiveObjectPropertyAxiom(isIEOfTC));
//		manager.addAxiom(this.output_ont, factory.getOWLTransitiveObjectPropertyAxiom(isEIOfTC));
		
		// subproperties
		manager.addAxiom(this.output_ont, factory.getOWLSubObjectPropertyOfAxiom(hasIE, hasIETC));
		manager.addAxiom(this.output_ont, factory.getOWLSubObjectPropertyOfAxiom(hasEI, hasEITC));
//		manager.addAxiom(this.output_ont, factory.getOWLSubObjectPropertyOfAxiom(isIEOf, isIEOfTC));
//		manager.addAxiom(this.output_ont, factory.getOWLSubObjectPropertyOfAxiom(isEIOf, isEIOfTC));
		
		// hasIE+ o hasEI -> hasEI+
		List<OWLObjectProperty> chain = new ArrayList<OWLObjectProperty>();
		chain.add(hasIETC);
		chain.add(hasEI);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chain, hasEITC));
								
		// hasIESibling
		OWLObjectProperty hasIESibling = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_IE_SIBLING);
//		manager.addAxiom(this.output_ont, factory.getOWLSymmetricObjectPropertyAxiom(hasIESibling));
		List<OWLObjectProperty> chainIEhasIESibling = new ArrayList<OWLObjectProperty>();
		chainIEhasIESibling.add(isIEOf);
		chainIEhasIESibling.add(hasIE);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainIEhasIESibling, hasIESibling));
		List<OWLObjectProperty> chainEIhasIESibling = new ArrayList<OWLObjectProperty>();
		chainEIhasIESibling.add(isEIOf);
		chainEIhasIESibling.add(hasIE);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainEIhasIESibling, hasIESibling));
		
		// hasEISibling
		OWLObjectProperty hasEISibling = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.HAS_EI_SIBLING);
//		manager.addAxiom(this.output_ont, factory.getOWLSymmetricObjectPropertyAxiom(hasEISibling));
		List<OWLObjectProperty> chainIEhasEISibling = new ArrayList<OWLObjectProperty>();
		chainIEhasEISibling.add(isIEOf);
		chainIEhasEISibling.add(hasEI);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainIEhasIESibling, hasEISibling));
		List<OWLObjectProperty> chainEIhasEISibling = new ArrayList<OWLObjectProperty>();
		chainEIhasEISibling.add(isEIOf);
		chainEIhasEISibling.add(hasEI);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainEIhasIESibling, hasEISibling));
		
		// isIEOfSiblingIE
		OWLObjectProperty isIESiblingOfIE = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_SIBLING_OF_IE);
//		manager.addAxiom(this.output_ont, factory.getOWLSymmetricObjectPropertyAxiom(isIEOfSiblingIE));
		List<OWLObjectProperty> chainisIESiblingOfIE = new ArrayList<OWLObjectProperty>();
		chainisIESiblingOfIE.add(isIEOf);
		chainisIESiblingOfIE.add(hasIE);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainisIESiblingOfIE, isIESiblingOfIE));
		
		// isIEOfSiblingIE
		OWLObjectProperty isIESiblingOfEI = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_IE_SIBLING_OF_EI);
//		manager.addAxiom(this.output_ont, factory.getOWLSymmetricObjectPropertyAxiom(isIEOfSiblingEI));
		List<OWLObjectProperty> chainisIESiblingOfEI = new ArrayList<OWLObjectProperty>();
		chainisIESiblingOfEI.add(isIEOf);
		chainisIESiblingOfEI.add(hasEI);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainisIESiblingOfEI, isIESiblingOfEI));
		
		// isEIOfSiblingIE
		OWLObjectProperty isEISiblingOfIE = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_EI_SIBLING_OF_IE);
//		manager.addAxiom(this.output_ont, factory.getOWLSymmetricObjectPropertyAxiom(isEIOfSiblingIE));
		List<OWLObjectProperty> chainisEISiblingOfIE = new ArrayList<OWLObjectProperty>();
		chainisEISiblingOfIE.add(isEIOf);
		chainisEISiblingOfIE.add(hasIE);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainisEISiblingOfIE, isEISiblingOfIE));
		
		// isEIOfSiblingEI
		OWLObjectProperty isEISiblingOfEI = factory.getOWLObjectProperty(AMLObjectPropertyIRIs.IS_EI_SIBLING_OF_EI);
//		manager.addAxiom(this.output_ont, factory.getOWLSymmetricObjectPropertyAxiom(isEIOfSiblingEI));
		List<OWLObjectProperty> chainisEISiblingOfEI = new ArrayList<OWLObjectProperty>();
		chainisEISiblingOfEI.add(isEIOf);
		chainisEISiblingOfEI.add(hasEI);
		manager.addAxiom(this.output_ont, factory.getOWLSubPropertyChainOfAxiom(chainisEISiblingOfEI, isEISiblingOfEI));
		
		*/
		
		if (!transformICLibs())
			return null;
		
		if (!transformRCLibs())
			return null;
		
		if (!transformSUCLibs())
			return null;
		
		if(!transformIH()) 
			return null;
		
		// state all individuals to be distinct
//		ArrayList<OWLIndividual> individuals = this.output_ont.individualsInSignature().collect(Collectors.toCollection(ArrayList::new));
//		manager.addAxiom(this.output_ont, factory.getOWLDifferentIndividualsAxiom(individuals));
		
		Set<OWLNamedIndividual> individuals = this.output_ont.getIndividualsInSignature();
		manager.addAxiom(this.output_ont, factory.getOWLDifferentIndividualsAxiom(individuals));
		
		return this.output_ont;
				
	}
	
}
