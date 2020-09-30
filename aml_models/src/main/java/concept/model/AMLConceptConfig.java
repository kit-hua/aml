package concept.model;

import CAEX215.AttributeType;
import CAEX215.util.AMLHelperFunctions;

/**
 * The class for holding OWL configuration of AML Concept Models 
 * @author aris
 *
 */
public class AMLConceptConfig extends BasicAMLConceptConfig{
	// whether the class reference or the value range of an AML concept model shall be negated
	// this is the negation in logic sense
	// this is useful to express negated concepts or data ranges
	// it is NOT considered as "not existing" - which is modeled as min=max=0
	private boolean negated = false;
	
	
	// whether this object is a descendant of its parent
	// by default, it is not a descendant but the direct child of its parent
	private boolean descendant = false;
	
	public static final String CONFIG_NEGATED = "negated";
	public static final String CONFIG_DESCENDANT = "descendant";

	
	public AMLConceptConfig() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the negated
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * @param negated the negated to set
	 */
	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	/**
	 * @return the descendant
	 */
	public boolean isDescendant() {
		return descendant;
	}
	
	/**
	 * @param descendant the descendant to set
	 */
	public void setDescendant(boolean descendant) {
		this.descendant = descendant;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		return super.equals(obj) 
				&& this.descendant == ((AMLConceptConfig) obj).descendant
				&& this.negated == ((AMLConceptConfig) obj).negated;
	}
	
	@Override
	public boolean canBeFusedWith(IAMLConceptConfig other) {
		
		return super.canBeFusedWith(other)
				&& this.descendant == ((AMLConceptConfig) other).descendant
				&& this.negated == ((AMLConceptConfig) other).negated;
	}
	
	@Override
	public AttributeType toCAEXAttribute () {
		AttributeType amlConf = super.toCAEXAttribute();		
		amlConf.getAttribute().add(toCAEXAttribute("negated", negated));
		amlConf.getAttribute().add(toCAEXAttribute("descendant", descendant));
		return amlConf;
	}
	
	@Override
	public void fromCAEXAttribute(AttributeType attr) {
		// TODO Auto-generated method stub
		super.fromCAEXAttribute(attr);
		
		for(AttributeType child : attr.getAttribute()) {
			if(child.getName().equalsIgnoreCase(AMLConceptConfig.CONFIG_NEGATED)) {
				Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(child);
				if(b!=null)
					negated = b;				
			}
			
			if(child.getName().equalsIgnoreCase(AMLConceptConfig.CONFIG_DESCENDANT)) {
				Boolean b = AMLHelperFunctions.getAMLAttributeValueBoolean(child);
				if(b!=null)
					descendant = b;				
			}
		}
	}
	
	@Override
	public String toString () {
		String s = super.toString();
		
		if(negated)
			s += "negated, ";
		
		if(descendant)
			s += "descendant, ";
		
		return s;
	}

}
