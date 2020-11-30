package com.pubmedplus.server.pojo;

public class TcgaFile {

	private String id;
	private String fileId;
	private String access;
	private String dataFormat;
	private String platform;
	private String dataCategory;
	private String experimentalStrategy;
	private String fileName;
	private String fileSize;
	private String workflowType;
	private String location;
	private String projectId;
	private String dataType;
	private Long count;
	private String sampleType;
	private String submitterId;

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getDataCategory() {
		return dataCategory;
	}

	public void setDataCategory(String dataCategory) {
		this.dataCategory = dataCategory;
	}

	public String getExperimentalStrategy() {
		return experimentalStrategy;
	}

	public void setExperimentalStrategy(String experimentalStrategy) {
		this.experimentalStrategy = experimentalStrategy;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getSampleType() {
		return sampleType;
	}

	public void setSampleType(String sampleType) {
		this.sampleType = sampleType;
	}

	public String getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(String submitterId) {
		this.submitterId = submitterId;
	}

}
