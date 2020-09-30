package clause;

public class IfClause extends AbstractFormattedXQueryClause{
	
	private String condition;		
	
	public IfClause(String condition) {
		this.condition = condition;
	}
	
	@Override
	public String getXQuery () {		
		return "if (" + condition + ")\n";
	}

}
