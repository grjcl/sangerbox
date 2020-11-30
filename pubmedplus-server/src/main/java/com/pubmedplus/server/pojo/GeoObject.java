package com.pubmedplus.server.pojo;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/11/18
 */
public class GeoObject {
    private String platform;
    private String describe;
    private String sample;
    private String dataExport;
    private String originalDataExport;

    public GeoObject(String platform, String describe, String sample, String dataExport, String originalDataExport) {
        this.platform = platform;
        this.describe = describe;
        this.sample = sample;
        this.dataExport = dataExport;
        this.originalDataExport = originalDataExport;
    }

    public GeoObject() {
        super();
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public String getDataExport() {
        return dataExport;
    }

    public void setDataExport(String dataExport) {
        this.dataExport = dataExport;
    }

    public String getOriginalDataExport() {
        return originalDataExport;
    }

    public void setOriginalDataExport(String originalDataExport) {
        this.originalDataExport = originalDataExport;
    }

    @Override
    public String toString() {
        return "GeoObject{" +
                "platform='" + platform + '\'' +
                ", describe='" + describe + '\'' +
                ", sample='" + sample + '\'' +
                ", dataExport='" + dataExport + '\'' +
                ", originalDataExport='" + originalDataExport + '\'' +
                '}';
    }
}
