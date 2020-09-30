/**
 * 
 */
package generator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 *
 */
public class TransitiveClosure {
//	private String function;
	private String pattern;
	private static AtomicInteger nextId = new AtomicInteger(0);	
	private int id;
	
	
	public TransitiveClosure(String pattern) {
		this.pattern = pattern;
		this.id = nextId.getAndIncrement();
	}
	
	public static void resetIdx() {
		nextId.set(0); 
	}
	
	/**
	 * for constraint, i.e. an object which has a sub object of the closure, use 
	 * 					"ObjectType[*[caex215:getClosure(., local:patternX(?))]]"
	 * for return and axis, i.e. an object of the closure, use 
	 * 					"*\/caex215:getClosure(., local:pattern1(?))"
	 */
	public String getExpression() {
		return "caex215:getClosure(., local:pattern" + id + "(?))";
	}
	
	public String getReturnExpression() {
		return "*/caex215:getClosure(., local:pattern" + id + "(?))";
	}
	
	public String getFunction() {		
		String prefix = "self::";
		
		if(this.pattern.startsWith(XPathGenerator.DESCENDANT)) {
			prefix = "";
		}

		String fun = "declare function local:pattern" + id + "($node as node()) as xs:boolean {\n";
		fun += "\t if($node[" + prefix + this.pattern + "])\n";
		fun += "\t then fn:true()\n";
		fun += "\t else fn:false()\n";
		fun += "};\n";

		return fun;
	}
}
