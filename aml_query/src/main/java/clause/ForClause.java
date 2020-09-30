package clause;

import xquery.XQueryVariable;

public class ForClause extends AbstractFormattedXQueryClause {

	private XQueryVariable variable;
	private String xpath;
	
	public ForClause(XQueryVariable variable, String xpath) {
		this.variable = variable;
		this.xpath = xpath;		
	}
	
	public XQueryVariable getVariable () {
		return this.variable;
	}
	
	public String getXpath () {
		return this.xpath;
	}
	
	@Override
	public String getXQuery () {		
		return "for " + this.variable.getExpression() + " in " + this.xpath + "\n";
	}
}
