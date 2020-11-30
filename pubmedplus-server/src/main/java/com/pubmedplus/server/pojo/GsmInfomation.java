package com.pubmedplus.server.pojo;

public class GsmInfomation {
	private String accession;
	private String title;
	private String sampleType;
	private String channelCount;
	private String dataProcessing;
	private String platformRef;
	private String description;
	private Object characteristicsJson;
	private Object organismJson;
	private Object sourceJson;
	private Object dataSummaryJson;
	private Object organismIDJson;

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSampleType() {
		return sampleType;
	}

	public void setSampleType(String sampleType) {
		this.sampleType = sampleType;
	}

	public String getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(String channelCount) {
		this.channelCount = channelCount;
	}

	public String getDataProcessing() {
		return dataProcessing;
	}

	public void setDataProcessing(String dataProcessing) {
		this.dataProcessing = dataProcessing;
	}

	public String getPlatformRef() {
		return platformRef;
	}

	public void setPlatformRef(String platformRef) {
		this.platformRef = platformRef;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object getCharacteristicsJson() {
		return characteristicsJson;
	}

	public void setCharacteristicsJson(Object characteristicsJson) {
		this.characteristicsJson = characteristicsJson;
	}

	public Object getOrganismJson() {
		return organismJson;
	}

	public void setOrganismJson(Object organismJson) {
		this.organismJson = organismJson;
	}

	public Object getSourceJson() {
		return sourceJson;
	}

	public void setSourceJson(Object sourceJson) {
		this.sourceJson = sourceJson;
	}

	public Object getDataSummaryJson() {
		return dataSummaryJson;
	}

	public void setDataSummaryJson(Object dataSummaryJson) {
		this.dataSummaryJson = dataSummaryJson;
	}

	public Object getOrganismIDJson() {
		return organismIDJson;
	}

	public void setOrganismIDJson(Object organismIDJson) {
		this.organismIDJson = organismIDJson;
	}

}
