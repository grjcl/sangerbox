package com.video.server.pojo;

public class Video {
    private String totalId;
    private String id;
    private String title;
    private String createTime;

    public Video() {
        super();
    }

    public Video(String totalId, String id, String title) {
        super();
        this.totalId = totalId;
        this.id = id;
        this.title = title;
    }

    public String getTotalId() {
        return totalId;
    }

    public void setTotalId(String totalId) {
        this.totalId = totalId;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

}
