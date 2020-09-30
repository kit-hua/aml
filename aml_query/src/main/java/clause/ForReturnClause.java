package clause;

public class ForReturnClause extends NestedClause{

	private ForClause xfor;
	private ReturnClause xret;
	
	public ForReturnClause (ForClause xfor, ReturnClause xret) {
		this.xfor = xfor;
		this.xret = xret;		
		this.clauses.add(xfor);
		this.clauses.add(xret);
	}		
	
	public ForReturnClause () {}

	/**
	 * @return the xfor
	 */
	public ForClause getXfor() {
		return xfor;
	}

	/**
	 * When updating the xfor, clear the clauses list and re-add
	 * @param xfor the xfor to set
	 */
	public void setXfor(ForClause xfor) {		
		this.clauses.clear();
		this.clauses.add(xfor);
		this.clauses.add(xret);
		this.xfor = xfor;
	}

	/**
	 * @return the xret
	 */
	public ReturnClause getXret() {
		return xret;
	}

	/**
	 * When updating the xret, clear the clauses list and re-add
	 * @param xret the xret to set
	 */
	public void setXret(ReturnClause xret) {
		this.clauses.clear();
		this.clauses.add(xfor);
		this.clauses.add(xret);
		this.xret = xret;
	}
}
