package com.video.server.pojo;

import javax.validation.constraints.NotBlank;

public class VideoReply {
    private String id;
    @NotBlank
    private String commentId;
    @NotBlank
    private String replyId;
    private String replyType;
    @NotBlank
    private String content;
    private String fromUid;
    @NotBlank
    private String toUid;
    private UserInfo fromUserInfo;
    private UserInfo toUserInfo;
    private String createTime;
    private int isReply;

    public UserInfo getFromUserInfo() {
        return fromUserInfo;
    }

    public int getIsReply() {
        return isReply;
    }

    public void setIsReply(int isReply) {
        this.isReply = isReply;
    }

    public void setFromUserInfo(UserInfo fromUserInfo) {
        this.fromUserInfo = fromUserInfo;
    }

    public UserInfo getToUserInfo() {
        return toUserInfo;
    }

    public void setToUserInfo(UserInfo toUserInfo) {
        this.toUserInfo = toUserInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public String getReplyType() {
        return replyType;
    }

    public void setReplyType(String replyType) {
        this.replyType = replyType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "VideoReply{" +
                "id='" + id + '\'' +
                ", commentId='" + commentId + '\'' +
                ", replyId='" + replyId + '\'' +
                ", replyType='" + replyType + '\'' +
                ", content='" + content + '\'' +
                ", fromUid='" + fromUid + '\'' +
                ", toUid='" + toUid + '\'' +
                ", fromUserInfo=" + fromUserInfo +
                ", toUserInfo=" + toUserInfo +
                ", createTime='" + createTime + '\'' +
                ", isReply=" + isReply +
                '}';
    }
}
