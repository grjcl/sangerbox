package com.pubmedplus.server.pojo;

public class ArticleUserComment {

	private String uniquenNumber;
	private int pmid;
	private long userPhone;
	private String label;
	private String articTitle;
	private String title;
	private String content;
	private int isReadAll;
	private int isComment;
	private String createTime;
	private Jourcache jourcache;

	public String getUniquenNumber() {
		return uniquenNumber;
	}

	public void setUniquenNumber(String uniquenNumber) {
		this.uniquenNumber = uniquenNumber;
	}

	public int getPmid() {
		return pmid;
	}

	public void setPmid(int pmid) {
		this.pmid = pmid;
	}

	public long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(long userPhone) {
		this.userPhone = userPhone;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getArticTitle() {
		return articTitle;
	}

	public void setArticTitle(String articTitle) {
		this.articTitle = articTitle;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getIsReadAll() {
		return isReadAll;
	}

	public void setIsReadAll(int isReadAll) {
		this.isReadAll = isReadAll;
	}

	public int getIsComment() {
		return isComment;
	}

	public void setIsComment(int isComment) {
		this.isComment = isComment;
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
