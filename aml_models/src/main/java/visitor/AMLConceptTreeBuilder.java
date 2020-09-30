//package visitor;
//
//import CAEX215.AttributeType;
//import CAEX215.CAEXObject;
//import CAEX215.ExternalInterfaceType;
//import CAEX215.InstanceHierarchyType;
//import CAEX215.InterfaceClassLibType;
//import CAEX215.InterfaceClassType;
//import CAEX215.InterfaceFamilyType;
//import CAEX215.InternalElementType;
//import CAEX215.InternalLinkType;
//import CAEX215.RoleClassLibType;
//import CAEX215.RoleClassType;
//import CAEX215.RoleFamilyType;
//import CAEX215.SystemUnitClassLibType;
//import CAEX215.SystemUnitClassType;
//import CAEX215.SystemUnitFamilyType;
//import concept.model.AMLQueryConfig;
//import concept.model.GenericAMLConceptModel;
//import concept.model.IAMLConceptConfig;
//import concept.tree.GenericTreeNode;
//import concept.util.AMLLinkCollector;
//
//public class AMLConceptTreeBuilder<T> implements CAEXObjectVisitorEx<T>{
//
//	@Override
//	public T visit(CAEXObject caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(AttributeType caexObj) {
//		
//		GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>> root = new GenericTreeNode<GenericAMLConceptModel<AMLQueryConfig>>();
//		root.data = new GenericAMLConceptModel(AMLQueryConfig.class);				
//		
//		// Note: this will only be called if the root is not "queryConfig"!
//		if(caexObj instanceof AttributeType) {
//			AttributeType attr = (AttributeType) caexObj;
//			if(!attr.getName().equals(IAMLConceptConfig.CONFIG)) {
//				root.data.fromConfiguredCAEXObj(caexObj);
//				for(AttributeType sub : attr.getAttribute()) {					
//					if(!sub.getName().equals(IAMLConceptConfig.CONFIG)) {					
//						root.addChild(visit(sub));
//					}
//				}	
//				
//				// clear the children of the attribute
////				if(type == AMLConceptConfig.class) {					
////					attr.getAttribute().clear();
////				}
//			}
//			else {
//				System.err.println("AMLConceptModelTreeParser.parse: the given caex object for parsing is a top level AML concept attribute!");
//			}	
//		}
//
//		else if(obj instanceof InterfaceClassType && !(obj instanceof InterfaceFamilyType) && !(obj instanceof ExternalInterfaceType)) {			
//			InterfaceClassType ic = (InterfaceClassType) obj;
//			root.data.fromConfiguredCAEXObj(obj);
//			for(AttributeType attr : ic.getAttribute()) {
//				if(!attr.getName().equals(IAMLConceptConfig.CONFIG)) {
//					root.addChild(parse(attr, type));
//				}
//			}			
//			// clear the children of the ic
////			if(type == AMLConceptConfig.class) {
////				ic.getAttribute().clear();
////			}
//		}
//		
//		else if(obj instanceof ExternalInterfaceType) {
//			ExternalInterfaceType ei = (ExternalInterfaceType) obj;
//			root.data.fromConfiguredCAEXObj(obj);
//			for(AttributeType attr : ei.getAttribute()) {
//				if(!attr.getName().equals(IAMLConceptConfig.CONFIG)) {
//					root.addChild(parse(attr, type));
//				}
//			}
//			
//			// add the link constraints to the AMLQuery model
//			if(type == AMLQueryConfig.class) {
//				((AMLQueryConfig) root.data.getConfig()).setLinkConstraints(AMLLinkCollector.getPartnersOfEI(ei));
//			}
//				
//			// clear the children of ei
////			if(type == AMLConceptConfig.class) {
////				ei.getAttribute().clear();
////			}
//		}
//		
//		else if(obj instanceof RoleClassType && !(obj instanceof RoleFamilyType)) {
//			RoleClassType rc = (RoleClassType) obj;		
//			root.data.fromConfiguredCAEXObj(obj);
//			for(AttributeType attr : rc.getAttribute()) {
//				if(!attr.getName().equals(IAMLConceptConfig.CONFIG)) {
//					root.addChild(parse(attr, type));
//				}
//			}
//			
//			for(ExternalInterfaceType ei : rc.getExternalInterface()) {
//				root.addChild(parse(ei, type));
//			}
//			// clear the children of rc
////			if(type == AMLConceptConfig.class) {
////				rc.getAttribute().clear();
////				rc.getExternalInterface().clear();
////			}
//		}
//		
//		else if(obj instanceof InternalElementType) {
//			InternalElementType ie = (InternalElementType) obj;
//			root.data.fromConfiguredCAEXObj(obj);
//			for(AttributeType attr : ie.getAttribute()) {
//				if(!attr.getName().equals(IAMLConceptConfig.CONFIG)) {
//					root.addChild(parse(attr, type));
//				}
//			}
//			
//			for(ExternalInterfaceType ei : ie.getExternalInterface()) {
//				root.addChild(parse(ei, type));
//			}
//			
//			for(InternalElementType sub : ie.getInternalElement()) {
//				root.addChild(parse(sub, type));
//			}
//			// clear the children of ie: IL and SRC are not CAEX Object children
////			if(type == AMLConceptConfig.class) {
////				ie.getAttribute().clear();
////				ie.getExternalInterface().clear();
////				ie.getInternalElement().clear();
////			}
//		}		
//		
//		else if(obj instanceof SystemUnitClassType && !(obj instanceof SystemUnitFamilyType)) {
//			SystemUnitClassType suc = (SystemUnitClassType) obj;
//			root.data.fromConfiguredCAEXObj(obj);
//			for(AttributeType attr : suc.getAttribute()) {
//				if(!attr.getName().equals(IAMLConceptConfig.CONFIG)) {
//					root.addChild(parse(attr, type));
//				}
//			}
//			
//			for(ExternalInterfaceType ei : suc.getExternalInterface()) {
//				root.addChild(parse(ei, type));
//			}
//			
//			for(InternalElementType ie : suc.getInternalElement()) {
//				root.addChild(parse(ie, type));
//			}
//			
//			// clear the children of suc: IL and SRC are not CAEX Object children
////			if(type == AMLConceptConfig.class) {
////				suc.getAttribute().clear();
////				suc.getExternalInterface().clear();
////				suc.getInternalElement().clear();
////			}
//		}
//	
//		else {
//			System.err.println("AMLConceptModelParser.parse: the given caex object is of type [" + obj.getClass() + "], which is not supported by AML concept models!");
//			return null;
//		}
//		
//		return root;
//		
//		return null;
//	}
//
//	@Override
//	public T visit(InstanceHierarchyType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(InterfaceClassLibType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(InterfaceClassType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(InterfaceFamilyType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(InternalLinkType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(RoleClassLibType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(RoleClassType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(RoleFamilyType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(SystemUnitClassLibType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(SystemUnitClassType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(SystemUnitFamilyType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(InternalElementType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public T visit(ExternalInterfaceType caexObj) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
