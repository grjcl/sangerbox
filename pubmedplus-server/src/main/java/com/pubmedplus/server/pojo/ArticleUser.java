package com.pubmedplus.server.pojo;

public class ArticleUser {
	private int PMID;
	private String title;
	private String createTime;
	private Jourcache jourcache;

	public int getPMID() {
		return PMID;
	}

	public void setPMID(int pMID) {
		PMID = pMID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Jourcache getJourcache() {
		return jourcache;
	}

	public void setJourcache(Jourcache jourcache) {
		this.jourcache = jourcache;
	}

}
