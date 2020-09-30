/**
 * 
 */
package xquery;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 *
 */
public class XQueryVariable {

	private static AtomicInteger nextId = new AtomicInteger(0);
	
	private int id;
	
	private String name;
	
	public XQueryVariable() {
		// TODO Auto-generated constructor stub
		id = nextId.getAndIncrement();
	}
	
	public XQueryVariable(String name) {		
		this();
		this.name = name;
	}
	
	public String getName() {
		if(name == null)
			return "n" + id;
		else
			return name;
	}
	
	public static void resetIdx() {
		nextId.getAndSet(0); 
	}
	
	public String getExpression () {
		return "$" + getName();
	} 
	
	public String toString () {
		return getExpression();
	}
	
}
