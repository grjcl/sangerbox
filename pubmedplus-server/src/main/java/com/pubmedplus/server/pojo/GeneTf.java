package com.pubmedplus.server.pojo;

public class GeneTf {
	private String geneIdName;
	private String datasets;
	private String tf;
	private String geneIdExp;
	private String tissue;
	private String peaks;
	private String peaksInGene;
	private String peaksAround;
	private String peakCloseTss;
	private String peakWithStrong;

	public String getGeneIdName() {
		return geneIdName;
	}

	public void setGeneIdName(String geneIdName) {
		this.geneIdName = geneIdName;
	}

	public String getDatasets() {
		return datasets;
	}

	public void setDatasets(String datasets) {
		this.datasets = datasets;
	}

	public String getTf() {
		return tf;
	}

	public void setTf(String tf) {
		this.tf = tf;
	}

	public String getGeneIdExp() {
		return geneIdExp;
	}

	public void setGeneIdExp(String geneIdExp) {
		this.geneIdExp = geneIdExp;
	}

	public String getTissue() {
		return tissue;
	}

	public void setTissue(String tissue) {
		this.tissue = tissue;
	}

	public String getPeaks() {
		return peaks;
	}

	public void setPeaks(String peaks) {
		this.peaks = peaks;
	}

	public String getPeaksInGene() {
		return peaksInGene;
	}

	public void setPeaksInGene(String peaksInGene) {
		this.peaksInGene = peaksInGene;
	}

	public String getPeaksAround() {
		return peaksAround;
	}

	public void setPeaksAround(String peaksAround) {
		this.peaksAround = peaksAround;
	}

	public String getPeakCloseTss() {
		return peakCloseTss;
	}

	public void setPeakCloseTss(String peakCloseTss) {
		this.peakCloseTss = peakCloseTss;
	}

	public String getPeakWithStrong() {
		return peakWithStrong;
	}

	public void setPeakWithStrong(String peakWithStrong) {
		this.peakWithStrong = peakWithStrong;
	}

}
