package com.video.server.controller;

import com.video.server.pojo.VideoReply;
import com.video.server.pojo.VideoTotal;
import com.video.server.utils.Util;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@Validated
public class VideoComment {

    /**
     * 评论视频
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/addUserVideoComment")
    public String addUserVideoComment(@NotBlank String id, @NotBlank String content, HttpServletRequest request) throws Exception {
        var responseJson = new JSONObject();
        var userPhone = request.getHeader("userPhone");
        if (Util.isPhone(userPhone)) {
            var videoTotalInfo = Util.util.videoMapper.getTotalInfo(id);
            var userId = request.getHeader("userId");
            var count = Util.util.videoMapper.addVideoComment(videoTotalInfo.getId(), content, userPhone, userId);
            if (count == 0) {
                throw new Exception("评论失败!");
            }
            var videoInfo = Util.util.videoMapper.getTotalInfo(id);
            var fromUser = Util.util.videoMapper.getUserInfo(userId);
            var message = fromUser.getUserName() + "用户评论了您的" + videoInfo.getTitle() + "视频!";
            Util.sendRabbitmqRoutingMessage(String.valueOf(videoInfo.getUserPhone()), message);
            responseJson.put("content", count);
            return responseJson.toString();
        }
        throw new Exception("评论失败!");
    }

    /**
     * 回复评论
     *
     * @return
     * @throws Exception
     */
    @PostMapping("/addUserVideoReply")
    public String addUserVideoReply(@Validated VideoReply VideoReply, @NotBlank String videoId, HttpServletRequest request) throws Exception {
        var responseJson = new JSONObject();
        var userPhone = request.getHeader("userPhone");
        var userId = request.getHeader("userId");
        if (Util.isPhone(userPhone)) {
            if (VideoReply.getToUid().equals(userId)) {
                throw new Exception("不能自己回复自己!");
            }
            VideoReply.setFromUid(userId);
            VideoReply.setReplyType(VideoReply.getCommentId().equals(VideoReply.getReplyId()) ? "comment" : "reply");
            var count = Util.util.videoMapper.addVideoReply(VideoReply);
            if (count == 0) {
                throw new Exception("评论失败!");
            }
            responseJson.put("content", count);
            var videoInfo = Util.util.videoMapper.getTotalInfo(videoId);
            var fromUser = Util.util.videoMapper.getUserInfo(VideoReply.getFromUid());
            var toUser = Util.util.videoMapper.getUserInfo(VideoReply.getToUid());
            String message = null;
            if (fromUser.getMobile().equals(String.valueOf(videoInfo.getUserPhone()))) {
                message = fromUser.getUserName() + "用户回复了您在" + videoInfo.getTitle() + "视频的评论!";
            } else {
                message = fromUser.getUserName() + "用户评论了您的" + videoInfo.getTitle() + "视频!";
            }
            Util.sendRabbitmqRoutingMessage(toUser.getMobile(), message);
            return responseJson.toString();
        }
        throw new Exception("评论失败!");
    }

    @PostMapping("/addVideoDesc")
    public String addVideoDesc(@NotBlank String id, HttpServletRequest request,String desc){
        String userPhone = request.getHeader("userPhone");
        var responseJson = new JSONObject();
        if (Util.isPhone(userPhone)) {
            int i = Util.util.videoMapper.addVideoDes(desc, id);
        }
        responseJson.put("content","success");
        return responseJson.toString();
    }

    @GetMapping("/getVideoDesc")
    public String getVideoDesc(@NotBlank String id){
        String videoDes = Util.util.videoMapper.getVideoDes(id);
        var responseJson = new JSONObject();
        responseJson.put("desc",videoDes);
        return responseJson.toString();
    }
}
