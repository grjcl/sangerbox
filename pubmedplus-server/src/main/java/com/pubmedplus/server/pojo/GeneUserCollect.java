package com.pubmedplus.server.pojo;

public class GeneUserCollect {
	private String uniquenNumber;
	private String geneId;
	private long userPhone;
	private int isCollect;
	private String createTime;

	public String getUniquenNumber() {
		return uniquenNumber;
	}

	public void setUniquenNumber(String uniquenNumber) {
		this.uniquenNumber = uniquenNumber;
	}

	public String getGeneId() {
		return geneId;
	}

	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
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

}
