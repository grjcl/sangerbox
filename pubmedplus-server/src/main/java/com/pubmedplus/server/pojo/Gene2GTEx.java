package com.pubmedplus.server.pojo;

public class Gene2GTEx {
	private String sampleCode;
	private String primarySite;
	private Double val;

	public String getSampleCode() {
		return sampleCode;
	}

	public void setSampleCode(String sampleCode) {
		this.sampleCode = sampleCode;
	}

	public String getPrimarySite() {
		return primarySite;
	}

	public void setPrimarySite(String primarySite) {
		this.primarySite = primarySite;
	}

	public Double getVal() {
		return val;
	}

	public void setVal(Double val) {
		this.val = val;
	}

	@Override
	public String toString() {
		return "Gene2GTEx{" +
				"sampleCode='" + sampleCode + '\'' +
				", primarySite='" + primarySite + '\'' +
				", val=" + val +
				'}';
	}
}
