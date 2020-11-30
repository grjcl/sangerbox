package com.pubmedplus.server.pojo;

public class AmqpRabbitListener {
	private int id;
	private String cmdMd5;
	private String cmd;
	private String desc;
	private String userPhone;
	private int status;
	private String type;
	private String userFile;
	private String successFilePath;
	private String errorFilePath;
	private String createTime;
	private String runTime;
	private String updateTime;
	private String totalTime;
	private String projectType;
	private String log;

	public AmqpRabbitListener() {
		super();
	}

	public AmqpRabbitListener(String cmdMd5, String cmd, String desc, String userPhone, String type, String userFile,
			String successFilePath, String errorFilePath) {
		super();
		this.cmdMd5 = cmdMd5;
		this.cmd = cmd;
		this.desc = desc;
		this.userPhone = userPhone;
		this.type = type;
		this.userFile = userFile;
		this.successFilePath = successFilePath;
		this.errorFilePath = errorFilePath;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getCmdMd5() {
		return cmdMd5;
	}

	public void setCmdMd5(String cmdMd5) {
		this.cmdMd5 = cmdMd5;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserFile() {
		return userFile;
	}

	public void setUserFile(String userFile) {
		this.userFile = userFile;
	}

	public String getSuccessFilePath() {
		return successFilePath;
	}

	public void setSuccessFilePath(String successFilePath) {
		this.successFilePath = successFilePath;
	}

	public String getErrorFilePath() {
		return errorFilePath;
	}

	public void setErrorFilePath(String errorFilePath) {
		this.errorFilePath = errorFilePath;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public String getProjectType() {
		return projectType;
	}

	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
	public String toString() {
		return "AmqpRabbitListener{" +
				"id=" + id +
				", cmdMd5='" + cmdMd5 + '\'' +
				", cmd='" + cmd + '\'' +
				", desc='" + desc + '\'' +
				", userPhone='" + userPhone + '\'' +
				", status=" + status +
				", type='" + type + '\'' +
				", userFile='" + userFile + '\'' +
				", successFilePath='" + successFilePath + '\'' +
				", errorFilePath='" + errorFilePath + '\'' +
				", createTime='" + createTime + '\'' +
				", runTime='" + runTime + '\'' +
				", updateTime='" + updateTime + '\'' +
				", totalTime='" + totalTime + '\'' +
				", projectType='" + projectType + '\'' +
				", log='" + log + '\'' +
				'}';
	}
}
