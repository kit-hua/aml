package clause;

public class ReturnClause extends NestedClause{

	public ReturnClause() {
	}

	@Override
	public String getXQuery() {
			
		String s = "return\n(\n";

//		if(clauses.size() == 1)
//			s += clauses.get(0).getXQuery();
//		
//		
//		else {
//			for (int i = 0; i < clauses.size(); i++) {
//				AbstractFormattedXQueryClause clause = clauses.get(i);
//				if(i == (clauses.size()-1) || clause.getXQuery().endsWith(",") || clause.getXQuery().endsWith(",\n"))
//					s += clause.getXQuery();
//				else
//					s += clause.getXQuery() + ",\n";
//			}	
//		}		
		
		for (AbstractFormattedXQueryClause clause : clauses)
			s += clause.getXQuery();
					
		
		s += ")\n";		
		return s;
	}
}
