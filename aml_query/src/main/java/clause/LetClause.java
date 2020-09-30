package clause;

import xquery.XQueryVariable;

public class LetClause extends AbstractFormattedXQueryClause{

	private XQueryVariable var;
	private String xpath;

	public LetClause(XQueryVariable var, String xpath) {
		this.var = var;
		this.xpath = xpath;
	}
	
	@Override
	public String getXQuery() {
		return "let " + var.getExpression() + " := " + xpath + "\n";
	}
}
