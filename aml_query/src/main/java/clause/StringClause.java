package clause;

public class StringClause extends AbstractFormattedXQueryClause {

	private String clause;
	
	public StringClause (String clause) {
		this.clause = clause;
	}
	
	@Override
	public String getXQuery() {
		if(this.clause.endsWith("\n"))
			return this.clause;
		else
			return this.clause + "\n";
	}

}
