package com.pubmedplus.server.pojo;

public class Jourcache {

	private String isoAbbr;

	private Double ifs;

	private JcrIfs jcrIfs;

	public Double getIfs() {
		return ifs;
	}

	public void setIfs(Double ifs) {
		this.ifs = ifs;
	}

	public String getIsoAbbr() {
		return isoAbbr;
	}

	public void setIsoAbbr(String isoAbbr) {
		this.isoAbbr = isoAbbr;
	}

	public JcrIfs getJcrIfs() {
		return jcrIfs;
	}

	public void setJcrIfs(JcrIfs jcrIfs) {
		this.jcrIfs = jcrIfs;
	}

}
