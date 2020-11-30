package com.pubmedplus.server.pojo;

public class JourcacheUser {
	private String nlmId;
	private String title;
	private String course;
	private String createTime;

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getNlmId() {
		return nlmId;
	}

	public void setNlmId(String nlmId) {
		this.nlmId = nlmId;
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

}
