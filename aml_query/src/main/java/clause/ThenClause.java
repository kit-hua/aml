package clause;

public class ThenClause extends NestedClause {
	
	public ThenClause () {
	}
		
	public String getXQuery () {
		
		String s = "then\n(\n";
		for (AbstractFormattedXQueryClause clause : clauses)
			s += clause.getXQuery();
		
		s += ")\n";
		
		return s;
	}

}
