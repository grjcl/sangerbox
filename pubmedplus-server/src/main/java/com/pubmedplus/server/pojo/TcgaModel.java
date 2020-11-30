package com.pubmedplus.server.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * @Author : zp
 * @Description :
 * @Date : 2020/6/29
 */
public class TcgaModel implements Serializable {
    private String name;
    private Integer value;
    private List<TcgaModel> children;
    private Style itemStyle;

    public Style getItemStyle() {
        return itemStyle;
    }

    public void setItemStyle(Style itemStyle) {
        this.itemStyle = itemStyle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public List<TcgaModel> getChildren() {
        return children;
    }

    public void setChildren(List<TcgaModel> children) {
        this.children = children;
    }

    public TcgaModel() {
    }

    public TcgaModel(String name, Integer value, List<TcgaModel> children) {
        this.name = name;
        this.value = value;
        this.children = children;
    }

    public boolean add(TcgaModel t){
       List<TcgaModel> tcgaFiles = this.getChildren();
       tcgaFiles.add(t);
       return true;
   }
}
