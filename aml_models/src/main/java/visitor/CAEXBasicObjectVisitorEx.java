package visitor;

import CAEX215.AttributeNameMappingType;
import CAEX215.AttributeValueRequirementType;
import CAEX215.ExternalReferenceType;
import CAEX215.InterfaceNameMappingType;
import CAEX215.MappingType;
import CAEX215.RefSemanticType;
import CAEX215.RevisionType;
import CAEX215.RoleRequirementsType;
import CAEX215.SupportedRoleClassType;

public interface CAEXBasicObjectVisitorEx<T> {

	T visit(RevisionType caexObj);

	T visit(SupportedRoleClassType caexObj);

	T visit(RoleRequirementsType caexObj);
	
	T visit(RefSemanticType caexObj);

	T visit(MappingType caexObj);

	T visit(InterfaceNameMappingType caexObj);

	T visit(ExternalReferenceType caexObj);

	T visit(AttributeValueRequirementType caexObj);

	T visit(AttributeNameMappingType caexObj);
}
