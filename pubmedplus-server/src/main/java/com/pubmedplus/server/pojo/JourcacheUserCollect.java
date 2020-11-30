package com.pubmedplus.server.pojo;

public class JourcacheUserCollect {

	private String uniquenNumber;
	private String nlmId;
	private long userPhone;
	private int isCollect;
	private String createTime;

	public String getUniquenNumber() {
		return uniquenNumber;
	}

	public void setUniquenNumber(String uniquenNumber) {
		this.uniquenNumber = uniquenNumber;
	}

	public String getNlmId() {
		return nlmId;
	}

	public void setNlmId(String nlmId) {
		this.nlmId = nlmId;
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
