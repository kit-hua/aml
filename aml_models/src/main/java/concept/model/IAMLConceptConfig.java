package concept.model;

import CAEX215.AttributeType;

public interface IAMLConceptConfig {

	public AttributeType toCAEXAttribute ();
	
	public void fromCAEXAttribute (AttributeType attr);
	
	/**
	 * whether or not one AMLConcept can be fused with another: they differ only in cardinalities or distinguished
	 * @param other
	 * @return
	 */
	public boolean canBeFusedWith (IAMLConceptConfig other);
	
	// top level AML attribute reserved for concept objects
	// if this attribute is not found - default
	public static final String CONFIG = "queryConfig";
	public static final String CONFIG_DISTINGUISHED = "distinguished"; 	 
	public static final String CONFIG_ID = "identifiedById";	
	public static final String CONFIG_NAME = "identifiedByName"; 	
	public static final String CONFIG_MIN = "minCardinality";
	public static final String CONFIG_MAX = "maxCardinality";	
	
}
