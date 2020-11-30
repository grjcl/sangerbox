package com.video.server.pojo;

import java.util.List;

public class VideoComment {
    private String id;
    private String userId;
    private String content;
    private UserInfo userInfo;
    private String createTime;
    private int isReply;
    private List<VideoReply> videoReplyList;

    public int getIsReply() {
        return isReply;
    }

    public void setIsReply(int isReply) {
        this.isReply = isReply;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<VideoReply> getVideoReplyList() {
        return videoReplyList;
    }

    public void setVideoReplyList(List<VideoReply> videoReplyList) {
        this.videoReplyList = videoReplyList;
    }

    @Override
    public String toString() {
        return "VideoComment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                ", userInfo=" + userInfo +
                ", createTime='" + createTime + '\'' +
                ", isReply=" + isReply +
                ", videoReplyList=" + videoReplyList +
                '}';
    }
}
