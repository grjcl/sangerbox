package com.video.server.pojo;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class VideoTotal {
    private String id;
    @NotBlank
    private String title;
    @NotBlank
    private String desc;
    @NotBlank
    private String money;
    @NotBlank
    private String type;
    private String image;
    private Long userPhone;
    private String userId;
    private String createTime;
    private String userName;
    private String path;
    private BigDecimal discount;
    public String getPath() {
        return path;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(Long userPhone) {
        this.userPhone = userPhone;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "VideoTotal{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", money='" + money + '\'' +
                ", type='" + type + '\'' +
                ", userPhone=" + userPhone +
                ", userId='" + userId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", userName='" + userName + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
