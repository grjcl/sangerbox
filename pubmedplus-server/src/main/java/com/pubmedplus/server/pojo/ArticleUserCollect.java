package com.pubmedplus.server.pojo;

public class ArticleUserCollect {
	private String uniquenNumber;
	private int pmid;
	private long userPhone;
	private int isCollect;
	private String createTime;

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

	public int getIsCollect() {
		return isCollect;
	}

	public void setIsCollect(int isCollect) {
		this.isCollect = isCollect;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}


}
