package AMLQuery;

public class AMLQueryDemoConfig {

	private boolean semantically = false;
	private boolean withLink = false;
	private boolean simpleOutput = false;
	
	private String datafile;
	private String queryfile;
	
	public AMLQueryDemoConfig() {}
	
	public AMLQueryDemoConfig(String datafile, String queryfile) {
		this.setDatafile(datafile);
		this.setQueryfile(queryfile);
	}
	

	public boolean isSemantically() {
		return semantically;
	}

	public void setSemantically(boolean semantically) {
		this.semantically = semantically;
	}

	public boolean isWithLink() {
		return withLink;
	}

	public void setWithLink(boolean withLink) {
		this.withLink = withLink;
	}

	public boolean isSimpleOutput() {
		return simpleOutput;
	}

	public void setSimpleOutput(boolean simpleOutput) {
		this.simpleOutput = simpleOutput;
	}

	public String getDatafile() {
		return datafile;
	}

	public void setDatafile(String datafile) {
		this.datafile = datafile;
	}

	public String getQueryfile() {
		return queryfile;
	}

	public void setQueryfile(String queryfile) {
		this.queryfile = queryfile;
	}
	
}
