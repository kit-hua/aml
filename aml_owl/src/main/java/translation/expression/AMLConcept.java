/**
 * 
 */
package translation.expression;

import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
//import org.dllearner.core.StringRenderer;
//import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.util.SimpleRenderer;

/**
 * @author aris
 * A class contains the information about an AML class description
 * 	- the owl class
 *  - primary
 *  - descendant
 */
public class AMLConcept {
	
	private static OWLObjectRenderer render = new SimpleRenderer();
//	private OWLObjectRenderer render = new DLSyntaxObjectRenderer();
	
	// the owl class expression
	private OWLClassExpression expression;
	
	// whether this AML concept is primary: i.e. learned
	private boolean isPrimary = false;
	
	// whether this AML concept is a descendant object
	private boolean isDescendant = false;
	
	// cardinality is necessary if the AMLConcept may contain inversed descendant roles
	private int min = 1;
	private int max = -1;
	
	// the caex type of the AML concept
	private IRI caexType;	
	
	public AMLConcept() {}
	
	public AMLConcept(OWLClassExpression ce) {
		this.expression = ce;
	}
	
	public AMLConcept(OWLClassExpression ce, IRI caexType, boolean returned) {
		this(ce);
		this.caexType = caexType;
		this.isPrimary = returned;
	}
	
	public AMLConcept(AMLConcept other) {
//		this();
//		this.expression = OWLClassExpressionUtils.clone(other.expression);
		this.expression = other.expression;
		if(other.caexType != null)
			this.caexType = IRI.create(other.caexType.toString());
		this.isPrimary = new Boolean(other.isPrimary);
	}

	/**
	 * @return the expression
	 */
	public OWLClassExpression getExpression() {
		return expression;
	}

	/**
	 * @param expression the expression to set
	 */
	public void setExpression(OWLClassExpression expression) {
		this.expression = expression;
	}

	/**
	 * @return the returned
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * @param returned the returned to set
	 */
	public void setPrimary(boolean returned) {
		this.isPrimary = returned;
	}

	/**
	 * @return the isDescendant
	 */
	public boolean isDescendant() {
		return isDescendant;
	}

	/**
	 * @param isDescendant the isDescendant to set
	 */
	public void setDescendant(boolean isDescendant) {
		this.isDescendant = isDescendant;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @return the caexType
	 */
	public IRI getCaexType() {
		return caexType;
	}

	/**
	 * @param caexType the caexType to set
	 */
	public void setCaexType(IRI caexType) {
		this.caexType = caexType;
	}
	
	
	public String toString() {
		
		if(this.expression == null) {
			System.err.println("AMLConcept.toString: no owl class expression set!");
			return "";
		}
		
		String str = "";
		
		if(this.caexType != null)
			str = this.caexType.getShortForm().toString() + ", ";		
		
		if(this.isPrimary)
			str += "primary, ";
		
		if(this.isDescendant)
			str += "descendant, ";
		
		if(this.min != 1 || this.max != -1)
			str += "[" + min + ", " + max + "]"; 
		
		// DL rendering does not work in UI
		return render.render(this.expression) + " : " + str;
//		return this.expression + " : " + str;
				
//		if(this.caexType != null)
//			return render.render(this.expression) + " : " + this.caexType.getShortForm().toString() + "," + this.isPrimary;
//		else
//			return render.render(this.expression);
	}

	/**
	 * @param render the render to set
	 */
	public static void setRender(OWLObjectRenderer render) {
		AMLConcept.render = render;
	}
	
//	public String toString(OWLObjectRenderer render) {
//		
//		String str = toString();
//		
//		// DL rendering does not work in UI
//		return render.render(this.expression) + " : " + str;			
//	}
	
	
}
