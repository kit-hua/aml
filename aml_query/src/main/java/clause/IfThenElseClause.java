package clause;

public class IfThenElseClause extends NestedClause{
	
	private IfClause ifClause;
	private ThenClause thenClause;
	private ElseClause elseClause;
	
	public IfThenElseClause(IfClause ifClause, ThenClause thenClause, ElseClause elseClause) {
		this.ifClause = ifClause;
		this.thenClause = thenClause;
		this.elseClause = elseClause;
		this.clauses.add(ifClause);
		this.clauses.add(thenClause);
		this.clauses.add(elseClause);
	}
	
	public IfThenElseClause () {
		
	}

	/**
	 * @return the ifClause
	 */
	public IfClause getIfClause() {
		return ifClause;
	}

	/**
	 * @param ifClause the ifClause to set
	 */
	public void setIfClause(IfClause ifClause) {
		this.clauses.clear();
		this.ifClause = ifClause;
		this.clauses.add(this.ifClause);
		this.clauses.add(this.thenClause);
		this.clauses.add(this.elseClause);
	}

	/**
	 * @return the thenClause
	 */
	public ThenClause getThenClause() {
		return thenClause;
	}

	/**
	 * @param thenClause the thenClause to set
	 */
	public void setThenClause(ThenClause thenClause) {		
		this.clauses.clear();
		this.thenClause = thenClause;
		this.clauses.add(this.ifClause);
		this.clauses.add(this.thenClause);
		this.clauses.add(this.elseClause);
	}

	/**
	 * @return the elseClause
	 */
	public ElseClause getElseClause() {
		return elseClause;
	}

	/**
	 * @param elseClause the elseClause to set
	 */
	public void setElseClause(ElseClause elseClause) {
		this.clauses.clear();
		this.elseClause = elseClause;
		this.clauses.add(this.ifClause);
		this.clauses.add(this.thenClause);
		this.clauses.add(this.elseClause);
	}
	

}
