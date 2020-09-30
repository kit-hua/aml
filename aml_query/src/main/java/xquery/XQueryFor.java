/**
 * 
 */
package xquery;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 *
 */
public class XQueryFor implements IXQueryConstructor{

	private Map<String, String> data = new HashMap<String, String>();
	
	private int nestDepth;
	
	public XQueryFor(int nestDepth) {
		this.nestDepth = nestDepth;
	}
	
	public XQueryFor(String node, String path) {
		// TODO Auto-generated constructor stub
	}
	
	public boolean isEmpty () {
		return data.isEmpty();
	}
	
	public boolean addData (String node, String path) {
		String previous = data.put(node, path);
		if(previous == null)
			return true;
		else {
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			System.err.println(stackTrace[0].getClassName() + "." + stackTrace[0].getMethodName() + ": overwriting the previous value of node [" + node + "]!");
			return false;
		}		
	}
	
	public void addAll (Map<String, String> forData) {
		this.data.putAll(forData);
	}
	
	public Map<String, String> getData () {
		return this.data;
	}

	@Override
	public String getExpression() {		
		if(data.isEmpty())
			return null;
		
		String tabs = "";
		for(int i = 0; i < nestDepth; i++) {
			tabs += "\t";
		}
		
		String s = tabs + "for ";
		
		for (Map.Entry<String, String> entry : data.entrySet()) {
		    String node = entry.getKey();
		    Object path = entry.getValue();
		    
		    s += "$" + node + " in " + path + ", ";
		}
		
		s = s.substring(0, s.length()-2);
		return s;
	}
	
	public String toString () {
		return getExpression();
	}
}
