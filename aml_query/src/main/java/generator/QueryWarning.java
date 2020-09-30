package generator;

public class QueryWarning {
	
	private StackTraceElement[] elements;
	private String msg;
	
	/**
	 * Constructor of a warning
	 * @param elements shall be set to "new Throwable().getStackTrace()"
	 * @param msg shall be set to "this.getClass().toString() + MESSAGE"
	 */
	public QueryWarning (StackTraceElement[] elements, String msg) {
		this.elements = elements;
		this.msg = msg;
	} 
	
	/**
	 * print the warning
	 */
	public void print() {
		System.err.println(elements[0].getClassName() + "." + elements[0].getMethodName() + ": " + msg);
	}
}
