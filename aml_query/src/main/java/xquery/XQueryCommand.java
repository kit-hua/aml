/**
 * 
 */
package xquery;


/**
 * @author Yingbing Hua, yingbing.hua@kit.edu
 * This class represents an XQuery command using the For and Return clauses
 */
public class XQueryCommand implements IXQueryConstructor{
	
	private XQueryFor xfor;	
	private XQueryReturn xreturn;
	
	public XQueryCommand() {
		// TODO Auto-generated constructor stub
	}
	
	public void setFor(XQueryFor xfor) {
		this.xfor = xfor;
	}
		
	public void setReturn(XQueryReturn xreturn) {
		this.xreturn = xreturn;
	}
	
	public XQueryReturn getReturn() {
		return this.xreturn;
	}
	
	public XQueryFor getFor() {
		return this.xfor;
	}
	
	@Override
	public String getExpression() {
		String query = "";
		
		if(xfor != null) {
			query += xfor.getExpression() + "\n";
		}
				
		if(xreturn != null)
			query += xreturn.getExpression() + "\n";
			
		return query;
	}
	
	public String toString () {
		return getExpression();
	}
}
