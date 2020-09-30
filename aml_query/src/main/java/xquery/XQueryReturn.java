/**
 * 
 */
package xquery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 *
 */
public class XQueryReturn implements IXQueryConstructor{

	private List<String> directRet = new ArrayList<String>();
	private XQueryFor xfor;
	private Set<XQueryCommand> nested = new HashSet<XQueryCommand>();
	private static AtomicInteger nextId = new AtomicInteger(0);
    private int id;
    private int nestDepth;
    private String xmlTag;
	
	public XQueryReturn(int nestDepth) {
		// TODO Auto-generated constructor stub
		id = nextId.getAndIncrement();
		this.nestDepth = nestDepth;
	}
		
	public static void resetIdx() {
		nextId.getAndSet(0); 
	}
	
	public void addFor (XQueryFor xfor) {
		this.xfor = xfor;
	}
	
	/**
	 * add a string as one direct return clause to the return object
	 * @param ret the string to be returned
	 */
	public void addDirectReturn (String ret) {
		
		String[] lines = ret.split("\n");						
		if(lines[0].contains("return")) {
			for(int i = 1; i < lines.length-1; i++) {				
				if(lines[i].contains("for")) {
					String elem = lines[i] + "\n";
					i++;
					for(int j = i; j < lines.length; j++) {
						elem += lines[j] + "\n";
						if(lines[j].endsWith(")")){
							i = j;
							elem = elem.substring(0, elem.length()-1);
							break;
						}						
					}
					this.directRet.add(elem);					
				}
				else if(lines[i].contains("return")) {
					
				}
				else {	
					String elem = lines[i];
					elem = elem.replaceAll("\t", "");
					elem = elem.replaceAll(",", "");
					this.directRet.add(elem);										
					for(int j = i+1; j < lines.length; j++) {						
						if(lines[j].endsWith(")")){
							i = j;
							break;
						}
						if(lines[j].contains("for")) {
							break;
						}
						elem = lines[j] + "\n";
						this.directRet.add(elem);
						i = j;
					}
					
				}
			}
		}
		else
			this.directRet.add(ret);
		
	}
	
	/**
	 * add a nested XQuery command as part of the return
	 * @param nested the nested XQuery command
	 */
	public void addNestedReturn (XQueryCommand nested) {
		this.nested.add(nested);
	}
	
	/**
	 * get nested formated expression with dynamically generated leading tabs
	 */
	@Override
	public String getExpression() {
		String tabs = "";
		for(int i = 0; i < nestDepth; i++) {
			tabs += "\t";
		}
		
		if(directRet.isEmpty() && nested == null)
			return null;
		
		String expr = "";
		
		// go through the direct returns and see if there is any simple direct returns as "$nx", which needs a XML tag for grouping
		// these correspond to the LCAs that generates direct returns
		for(String ret : directRet) {
			String[] lines = ret.split("\n");
			if(lines[0].contains("for")) {
				ret = ret.substring(ret.indexOf("for"));
			}
	
			expr += tabs + "\t" + ret + ",\n";
		}
		
		if(xfor != null)
			expr += xfor.getExpression() + "\n";

		for(XQueryCommand child : nested) {
			String childExpr = child.getExpression();
			childExpr = childExpr.substring(0, childExpr.length()-1);
			expr += childExpr + "," + "\n";
		}			
		
		if(!nested.isEmpty())
			expr = expr.substring(0, expr.length()-2);
					
		if(expr.endsWith(",\n"))
			expr = expr.substring(0, expr.length()-2);
		
		if(this.directRet.isEmpty())
			return expr;
		else {
			
			// add XML tags if necessary
			if(this.xmlTag != null) {
				expr = "<" + this.xmlTag + ">{\n" + expr;
				expr = expr + "\n" + tabs + "}</" + xmlTag + ">\n";
				return tabs + "return (" + expr + tabs + ")";
			}
			else
				return tabs + "return (\n" + expr + "\n" + tabs + ")";
		}
	}
	
	public String toString () {
		return getExpression();
	}
	
	public List<String> getDirectReturns () {
		return this.directRet;
	}

	public void setXMLTag (String xmlTag) {
		this.xmlTag = xmlTag;
	}
}
