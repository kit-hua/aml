/**
 */
package CAEX215;

import visitor.CAEXObjectVisitorEx;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>External Interface Type</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see CAEX215.CAEX215Package#getExternalInterfaceType()
 * @model extendedMetaData="name='ExternalInterface_._type' kind='elementOnly'"
 * @generated
 */
public interface ExternalInterfaceType extends InterfaceClassType {
	
    /**
     * @param visitor
     *        visitor
     * @param <T>
     *        visitor return type
     * @return visitor return value
     */
    <T> T accept(CAEXObjectVisitorEx<T> visitor);
} // ExternalInterfaceType
