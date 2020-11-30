package com.pubmedplus.server.pojo;

public class JourcacheUserComment {

	private String uniquenNumber;
	private String nlmId;
	private long userPhone;
	private String label;
	private String JourcacheTitle;
	private String title;
	private String content;
	private String course;
	private int isReadAll;
	private int isComment;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getJourcacheTitle() {
		return JourcacheTitle;
	}

	public void setJourcacheTitle(String jourcacheTitle) {
		JourcacheTitle = jourcacheTitle;
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

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

}
