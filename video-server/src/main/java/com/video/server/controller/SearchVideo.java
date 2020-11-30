package com.video.server.controller;

import com.video.server.pojo.VideoTotal;
import com.video.server.utils.JSONObjectUtil;
import com.video.server.utils.RedisConfig;
import com.video.server.utils.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class SearchVideo {

    @Autowired
    private RedisConfig redisConfig;
    /**
     * 查询视频
     *
     * @return
     */
    @PostMapping("/searchVideo")
    public String searchVideo(String money, @RequestParam("types") List<String> types, String sort) throws IOException {
        Object key = redisConfig.getKey(money + types.toString() + sort);
        if (key != null){
            System.out.println(key);
            return key.toString();
        }
        var responseJson = new JSONObject();
        if (money != null && money.split("-").length > 1 && Util.isNumeric(money.split("-")[0]) && Util.isNumeric(money.split("-")[1])) {
            money = "`money`>=" + money.split("-")[0] + " AND money<=" + money.split("-")[1];
        } else {
            money = null;
        }
        var type = types.size() == 0 ? null : "'" + String.join("','", types) + "'";
        if (sort != null && !sort.isBlank() && sort.split("_").length > 1) {
            var sortName = sort.split("_")[0];
            var sortOrder = Integer.valueOf(sort.split("_")[1]);
            if ("time-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
                sort = sortOrder == 1 ? " ORDER BY `video_total`.`create_time` DESC" : " ORDER BY `video_total`.`create_time` ASC";
            } else if ("money-sort".equals(sortName) && (sortOrder == -1 || sortOrder == 1)) {
                sort = sortOrder == 1 ? " ORDER BY `video_total`.`money` DESC" : " ORDER BY `video_total`.`money` ASC";
            }
        } else {
            sort = " ORDER BY `video_total`.`create_time` DESC";
        }
        var videoList = Util.util.videoMapper.searchVideo(money, type, sort);
        for (VideoTotal videoTotal : videoList){
            BigDecimal decimal = Util.util.videoMapper.searchDis(videoTotal.getId());
            videoTotal.setDiscount(decimal);
        }
        responseJson.put("videoList", JSONArray.fromObject(videoList, JSONObjectUtil.getJsonConfig()));
        redisConfig.setExpireKey(money+types.toString()+sort,responseJson,120L);
        return responseJson.toString();
    }

    /**
     * 获取视频分类
     *
     * @throws Exception
     */
    @GetMapping("/getVideoTypeInfo")
    public String getVideoTypeInfo() throws Exception {
        var responseJson = new JSONObject();
        responseJson.put("typeList", Util.util.videoMapper.listVideoType());
        return responseJson.toString();
    }

    /**
     * 获取视频信息
     *
     * @throws Exception
     */
    @GetMapping("/getVideoInfo")
    public String getVideoInfo(@NotBlank String id, HttpServletRequest request) throws Exception {
        var responseJson = new JSONObject();
        var videoTotalInfo = Util.util.videoMapper.getTotalInfo(id);
        BigDecimal decimal = Util.util.videoMapper.searchDis(id);
        responseJson.put("discount",decimal);
        responseJson.put("shoppingId", videoTotalInfo.getId());
        var video = Util.util.videoMapper.getVideo(id);
        responseJson.put("video", JSONObject.fromObject(video, JSONObjectUtil.getJsonConfig()));
        String path = Util.util.videoMapper.getPath(id);
        if (!StringUtils.isEmpty(path)){
            path = path.trim();
        }
        System.out.println(video.toString() + ">>>>>>>>>>>>" + path);
        if (!StringUtils.isEmpty(path)) {
            responseJson.put("hasZip","1");
        }else {
            responseJson.put("hasZip","0");
        }
        var videoCommentList = Util.util.videoMapper.listVideoComment(videoTotalInfo.getId());
        var userId = request.getHeader("userId");
        if (userId != null && !userId.isBlank()) {
            responseJson.put("memberType",Util.util.videoMapper.getMemberType(userId));
            videoCommentList = videoCommentList.stream().map(videoComment -> {
                videoComment.setIsReply((userId.equals(videoTotalInfo.getUserId()) && !userId.equals(videoComment.getUserInfo().getId())) ? 1 : 0);
                videoComment.setVideoReplyList(videoComment.getVideoReplyList().stream().map(videoReply -> {
                    videoReply.setIsReply((!userId.equals(videoReply.getFromUserInfo().getId()) && userId.equals(videoReply.getToUserInfo().getId())) ? 1 : 0);
                    return videoReply;
                }).collect(Collectors.toList()));
                return videoComment;
            }).collect(Collectors.toList());
        }
        responseJson.put("videoCommentList", Util.compress(JSONArray.fromObject(videoCommentList, JSONObjectUtil.getJsonConfig()).toString()));

        if (Util.util.videoMapper.countVideo(videoTotalInfo.getId()) > 1) {
            var totalVideoList = Util.util.videoMapper.listTotalVideo(videoTotalInfo.getId());
            responseJson.put("totalVideoList", Util.compress(JSONArray.fromObject(totalVideoList, JSONObjectUtil.getJsonConfig()).toString()));
        } else {
            var type = Util.util.videoMapper.getVideoType(videoTotalInfo.getId());
            var videoTypetList = Util.util.videoMapper.listTypeVideo(videoTotalInfo.getId(), type);
            responseJson.put("videoTypeList", Util.compress(JSONArray.fromObject(videoTypetList, JSONObjectUtil.getJsonConfig()).toString()));
        }
        return responseJson.toString();
    }

    /**
     * 判断用户是否有观看视频的权限
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/isVideoViewPayAuto")
    public String isVideoViewPayAuto(@NotBlank String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        var responseJson = new JSONObject();
        var userPhone = request.getHeader("userPhone");
        if (Util.isViewVideoAuto(id, userPhone)) {
            responseJson.put("isView", 1);
            return responseJson.toString();
        }
        throw new Exception("没有权限查看视频!");
    }

}
