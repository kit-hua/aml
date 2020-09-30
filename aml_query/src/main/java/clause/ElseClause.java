package clause;

public class ElseClause extends NestedClause {
	
	public ElseClause() {
	}
		
	@Override
	public String getXQuery () {
		
		String s = "else\n(\n";
		 
		for (AbstractFormattedXQueryClause clause : clauses)
			s += clause.getXQuery();
		
		s += ")\n";
		
		return s;
	}

}
