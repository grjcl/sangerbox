package com.pubmedplus.server.pojo;

public class ExpByGenes {
	private String sampleCode;
	private Double val;

	public ExpByGenes() {
		super();
	}

	public ExpByGenes(String sampleCode, Double val) {
		super();
		this.sampleCode = sampleCode;
		this.val = val;
	}

	public String getSampleCode() {
		return sampleCode;
	}

	public void setSampleCode(String sampleCode) {
		this.sampleCode = sampleCode;
	}

	public Double getVal() {
		return val;
	}

	public void setVal(Double val) {
		this.val = val;
	}

	@Override
	public String toString() {
		return "ExpByGenes [sampleCode=" + sampleCode + ", val=" + val + "]";
	}

}
