package concept.util;

public class ConceptWarning {

	private StackTraceElement[] elements;
	private String msg;
	
	public ConceptWarning (StackTraceElement[] elements, String msg) {
		this.elements = elements;
		this.msg = msg;
	} 
	
	public void print() {
		System.err.println(elements[0].getClassName() + "." + elements[0].getMethodName() + ": " + msg);
	}
}
