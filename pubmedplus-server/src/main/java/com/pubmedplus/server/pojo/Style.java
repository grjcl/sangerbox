package com.pubmedplus.server.pojo;

import java.io.Serializable;

/**
 * @Author : zp
 * @Description :
 * @Date : 2020/7/3
 */
public class Style implements Serializable {

    private String color;

    public Style(String color) {
        this.color = color;
    }

    public Style() {
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
