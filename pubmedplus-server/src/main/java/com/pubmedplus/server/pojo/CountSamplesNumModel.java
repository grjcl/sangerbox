package com.pubmedplus.server.pojo;

/**
 * @Author : 小潘
 * @Description :
 * @Date : 2020/8/18
 */
public class CountSamplesNumModel {

    private String TCGACode;
    private Integer count;


    public CountSamplesNumModel() {
        super();
    }

    public CountSamplesNumModel(String TCGACode, Integer count) {
        this.TCGACode = TCGACode;
        this.count = count;

    }

    public String getTCGACode() {
        return TCGACode;
    }

    public void setTCGACode(String TCGACode) {
        this.TCGACode = TCGACode;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }




    @Override
    public String toString() {
        return "CountSamplesNumModel{" +
                "TCGACode='" + TCGACode + '\'' +
                ", count=" + count +

                '}';
    }
}
