package com.pubmedplus.server.pojo;

public class Gene2Pancancer {
	private String sampleCode;
	private String TCGACode;
	private String primarySite;
	private String dbType;
	private String siteType;
	private Double val;

	public String getSampleCode() {
		return sampleCode;
	}

	public void setSampleCode(String sampleCode) {
		this.sampleCode = sampleCode;
	}

	public String getTCGACode() {
		return TCGACode;
	}

	public void setTCGACode(String tCGACode) {
		TCGACode = tCGACode;
	}

	public String getPrimarySite() {
		return primarySite;
	}

	public void setPrimarySite(String primarySite) {
		this.primarySite = primarySite;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public String getSiteType() {
		return siteType;
	}

	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}

	public Double getVal() {
		return val;
	}

	public void setVal(Double val) {
		this.val = val;
	}

}
