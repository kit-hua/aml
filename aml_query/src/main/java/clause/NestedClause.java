package clause;

import java.util.ArrayList;
import java.util.List;

/**
 * General nested xquery clause 
 * @author Yingbing Hua, IAR-IPR, KIT
 * @email yingbing.hua@kit.edu, yingbing.hua@gmail.com
 */
public class NestedClause extends AbstractFormattedXQueryClause{

	protected List<AbstractFormattedXQueryClause> clauses;
	
	public NestedClause() {
		this.clauses = new ArrayList<AbstractFormattedXQueryClause>();
	}
	
	public void addClause (AbstractFormattedXQueryClause cmd) {
		this.clauses.add(cmd);
	}
	
	public List<AbstractFormattedXQueryClause> getClauses (){
		return this.clauses;
	}

	@Override
	public String getXQuery() {
		String s = "";
		for(AbstractFormattedXQueryClause clause : clauses) {
			s += clause.getXQuery();
		}
		return s;
	}
}
