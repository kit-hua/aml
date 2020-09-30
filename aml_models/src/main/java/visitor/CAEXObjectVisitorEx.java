package visitor;

import CAEX215.AttributeType;
import CAEX215.CAEXObject;
import CAEX215.ExternalInterfaceType;
import CAEX215.InstanceHierarchyType;
import CAEX215.InterfaceClassLibType;
import CAEX215.InterfaceClassType;
import CAEX215.InterfaceFamilyType;
import CAEX215.InternalElementType;
import CAEX215.InternalLinkType;
import CAEX215.RoleClassLibType;
import CAEX215.RoleClassType;
import CAEX215.RoleFamilyType;
import CAEX215.SystemUnitClassLibType;
import CAEX215.SystemUnitClassType;
import CAEX215.SystemUnitFamilyType;

public interface CAEXObjectVisitorEx<T> {
	

    T visit(CAEXObject caexObj);
    
    
    T visit(AttributeType caexObj);
    
    
    T visit(InstanceHierarchyType caexObj);
    
    
    T visit(InterfaceClassLibType caexObj);
    
    
    T visit(InterfaceClassType caexObj);
    
    
    T visit(InterfaceFamilyType caexObj);
    
    
    T visit(InternalLinkType caexObj);
    
    
    T visit(RoleClassLibType caexObj);
    
    
    T visit(RoleClassType caexObj);
    
    
    T visit(RoleFamilyType caexObj);
    
    
    T visit(SystemUnitClassLibType caexObj);
    
    
    T visit(SystemUnitClassType caexObj);
    
    
    T visit(SystemUnitFamilyType caexObj);
    
    
    T visit(InternalElementType caexObj);
    
    
    T visit(ExternalInterfaceType caexObj);
   

}
