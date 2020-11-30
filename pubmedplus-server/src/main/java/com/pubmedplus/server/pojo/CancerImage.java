package com.pubmedplus.server.pojo;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/19
 */
public class CancerImage {
    private Integer id;
    private String cancerId;
    private String cancerNameCn;
    private String cancerNameEn;
    private String cancerNameEnQuan;
    private String url;
    private String cancerImg;


    public CancerImage() {
        super();
    }

    public CancerImage(Integer id, String cancerId, String cancerNameCn, String cancerNameEn, String cancerNameEnQuan, String url, String cancerImg) {
        this.id = id;
        this.cancerId = cancerId;
        this.cancerNameCn = cancerNameCn;
        this.cancerNameEn = cancerNameEn;
        this.cancerNameEnQuan = cancerNameEnQuan;
        this.url = url;
        this.cancerImg = cancerImg;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCancerId() {
        return cancerId;
    }

    public void setCancerId(String cancerId) {
        this.cancerId = cancerId;
    }

    public String getCancerImg() {
        return cancerImg;
    }

    public void setCancerImg(String cancerImg) {
        this.cancerImg = cancerImg;
    }

    public String getCancerNameCn() {
        return cancerNameCn;
    }

    public void setCancerNameCn(String cancerNameCn) {
        this.cancerNameCn = cancerNameCn;
    }

    public String getCancerNameEn() {
        return cancerNameEn;
    }

    public void setCancerNameEn(String cancerNameEn) {
        this.cancerNameEn = cancerNameEn;
    }

    public String getCancerNameEnQuan() {
        return cancerNameEnQuan;
    }

    public void setCancerNameEnQuan(String cancerNameEnQuan) {
        this.cancerNameEnQuan = cancerNameEnQuan;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "CancerImage{" +
                "id=" + id +
                ", cancerId='" + cancerId + '\'' +
                ", cancerNameCn='" + cancerNameCn + '\'' +
                ", cancerNameEn='" + cancerNameEn + '\'' +
                ", cancerNameEnQuan='" + cancerNameEnQuan + '\'' +
                ", url='" + url + '\'' +
                ", cancerImg='" + cancerImg + '\'' +
                '}';
    }
}
