package com.video.server.controller;

import com.video.server.pojo.Video;
import com.video.server.pojo.VideoTotal;
import com.video.server.utils.Util;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@Validated
public class Upload {


    public String upUserVideo(MultipartFile file, HttpServletRequest request) throws Exception {
        if (file == null) {
            throw new Exception("没有文件!");
        } else if (!"video/mp4".equals(file.getContentType())) {
            throw new Exception("文件不是MP4!");
        }
        var uuid = Util.getUUID();
        var videoFile = new File(Util.VIDEO_SAVE_PATH + uuid);
        if (!videoFile.exists()) {
            videoFile.mkdirs();
        }
        file.transferTo(new File(videoFile + "/" + uuid + ".mp4"));
        var status = Util.writerText(videoFile + "/" + Util.VIDEO_KEY_NAME, Util.getUUID());
        if (!status) {
            throw new Exception("上传失败!");
        }
        StringBuffer keyinfo = new StringBuffer(Util.PROJECT_SITE + "getVideoKey?id=" + uuid + "\n");
        keyinfo.append(Util.VIDEO_SAVE_PATH + uuid + "/" + Util.VIDEO_KEY_NAME + "\n");
        keyinfo.append(Util.getUUID());
        status = Util.writerText(videoFile + "/" + Util.VIDEO_KEY_NAME + "info", keyinfo.toString());
        if (!status) {
            throw new Exception("上传失败!");
        }
        StringBuilder ffmpegCmd = new StringBuilder("ffmpeg -y -i");
        ffmpegCmd.append(" " + videoFile + "/" + uuid + ".mp4");
        ffmpegCmd.append(" -hls_time 10");
        ffmpegCmd.append(" -hls_key_info_file");
        ffmpegCmd.append(" " + Util.VIDEO_SAVE_PATH + uuid + "/" + Util.VIDEO_KEY_NAME + "info");
        ffmpegCmd.append(" -hls_playlist_type vod");
        ffmpegCmd.append(" -hls_base_url " + Util.PROJECT_SITE + "getVideoFragment?id=");
        ffmpegCmd.append(" -hls_segment_filename " + Util.VIDEO_SAVE_PATH + uuid + "/" + uuid + "%5d");
        ffmpegCmd.append(" " + Util.VIDEO_SAVE_PATH + uuid + "/" + uuid + ".m3u8");
        return ffmpegCmd.toString();
    }
    /**
     * 上传文件
     *
     * @return 返回一个路径名
     * @throws Exception
     */
    @PostMapping("/uploadUserVideo")
    public String uploadUserVideo(MultipartFile file, HttpServletRequest request) throws Exception {
        if (file == null) {
            throw new Exception("没有文件!");
        } else if (!"video/mp4".equals(file.getContentType())) {
            throw new Exception("文件不是MP4!");
        }
        var userPhone = request.getHeader("userPhone");
        if (Util.isPhone(userPhone)) {
            var uuid = Util.getUUID();
            var videoFile = new File(Util.VIDEO_SAVE_PATH + uuid);
            System.out.println(uuid);
            if (!videoFile.exists()) {
                videoFile.mkdirs();
            }
            file.transferTo(new File(videoFile + "/" + uuid + ".mp4"));
            var status = Util.writerText(videoFile + "/" + Util.VIDEO_KEY_NAME, Util.getUUID());
            if (!status) {
                throw new Exception("上传失败!");
            }
            var keyinfo = new StringBuffer(Util.PROJECT_SITE + "getVideoKey?id=" + uuid + "\n");
            keyinfo.append(Util.VIDEO_SAVE_PATH + uuid + "/" + Util.VIDEO_KEY_NAME + "\n");
            keyinfo.append(Util.getUUID());
            status = Util.writerText(videoFile + "/" + Util.VIDEO_KEY_NAME + "info", keyinfo.toString());
            if (!status) {
                throw new Exception("上传失败!");
            }
            var ffmpegCmd = new StringBuilder("ffmpeg -y -i");
            ffmpegCmd.append(" " + videoFile + "/" + uuid + ".mp4");
            ffmpegCmd.append(" -hls_time 10");
            ffmpegCmd.append(" -hls_key_info_file");
            ffmpegCmd.append(" " + Util.VIDEO_SAVE_PATH + uuid + "/" + Util.VIDEO_KEY_NAME + "info");
            ffmpegCmd.append(" -hls_playlist_type vod");
            ffmpegCmd.append(" -hls_base_url " + Util.PROJECT_SITE + "getVideoFragment?id=");
            ffmpegCmd.append(" -hls_segment_filename " + Util.VIDEO_SAVE_PATH + uuid + "/" + uuid + "%5d");
            ffmpegCmd.append(" " + Util.VIDEO_SAVE_PATH + uuid + "/" + uuid + ".m3u8");
            var cmdStatus = Util.runCmd(ffmpegCmd.toString());
            System.out.println(ffmpegCmd.toString());
            if (cmdStatus == 0) {
                return uuid;
            } else {
                deleteFiles(videoFile);
                throw new Exception("请检查视频完整性!");
            }
        }
        throw new Exception("上传失败!");
    }
    @PostMapping("/submitUserVideo")
    @Transactional(rollbackFor = Exception.class)
    public String submitUserVideos(String id, MultipartFile imageFile,MultipartFile zipFile, @Validated VideoTotal videoTotal, HttpServletRequest request) throws Exception {
        var userPhone = request.getHeader("userPhone");
        String[] split = id.split(",");
        if (!Util.isPhone(userPhone) || split.length == 0) {
            throw new Exception("提交失败!");
        }
        if (imageFile == null || imageFile.getOriginalFilename() == null || !imageFile.getContentType().contains("image")) {
            throw new Exception("请上传图片!");
        }
        var responseJson = new JSONObject();
        var videoTotalUUID = Util.getUUID();
        videoTotal.setId(videoTotalUUID);
        videoTotal.setUserId(request.getHeader("userId"));
        videoTotal.setImage(new Base64().encodeToString(imageFile.getBytes()));
        videoTotal.setMoney(new BigDecimal(videoTotal.getMoney()).compareTo(new BigDecimal("0")) == -1 ? "0" : videoTotal.getMoney());
        videoTotal.setUserPhone(Long.valueOf(userPhone));
        if (new BigDecimal(videoTotal.getMoney()).compareTo(new BigDecimal("0")) == 1) {
            if (Util.util.videoMapper.addMemberRank(videoTotal.getMoney(), videoTotal.getTitle(), videoTotalUUID) == 0) {
                throw new Exception("提交失败!");
            }
        }
        Util.util.videoMapper.addVideoType(videoTotal.getType());
        var videoList = new ArrayList<Video>();
        for (int i = 0; i < split.length; i++) {
            videoList.add(new Video(videoTotalUUID, split[i] ,videoTotal.getTitle()));
        }
        if (zipFile!=null) {
            File file = new File(Util.VIDEO_SAVE_PATH +split[0] +"/"+ split[0]+".zip");
            zipFile.transferTo(file);
            videoTotal.setPath(file.getPath());
        }
        if (Util.util.videoMapper.addVideoTotal(videoTotal) == 0) {
            throw new Exception("提交失败!");
        }
        responseJson.put("count", Util.util.videoMapper.addVideoList(videoList));
        return responseJson.toString();
    }
        /**
         * 上传视频数据
         *
         * @param request
         * @return
         * @throws Exception
         */
//    @PostMapping("/submitUserVideo")
//    @Transactional(rollbackFor = Exception.class)
    public String submitUserVideo(MultipartFile[] files, MultipartFile imageFile,MultipartFile zipFile, @Validated VideoTotal videoTotal, HttpServletRequest request) throws Exception {
        var userPhone = request.getHeader("userPhone");
        System.out.println(userPhone);
        if (!Util.isPhone(userPhone) || files.length == 0) {
            throw new Exception("提交失败!");
        }
        if (imageFile == null || imageFile.getOriginalFilename() == null || !imageFile.getContentType().contains("image")) {
            throw new Exception("请上传图片!");
        }
        var responseJson = new JSONObject();
        var videoTotalUUID = Util.getUUID();
        videoTotal.setId(videoTotalUUID);
        videoTotal.setUserId(request.getHeader("userId"));
        videoTotal.setImage(new Base64().encodeToString(imageFile.getBytes()));
        videoTotal.setMoney(new BigDecimal(videoTotal.getMoney()).compareTo(new BigDecimal("0")) == -1 ? "0" : videoTotal.getMoney());
        videoTotal.setUserPhone(Long.valueOf(userPhone));
        if (new BigDecimal(videoTotal.getMoney()).compareTo(new BigDecimal("0")) == 1) {
            if (Util.util.videoMapper.addMemberRank(videoTotal.getMoney(), videoTotal.getTitle(), videoTotalUUID) == 0) {
                throw new Exception("提交失败!");
            }
        }
        Util.util.videoMapper.addVideoType(videoTotal.getType());
        var videoList = new ArrayList<Video>();
        String uuid = "";
        for (int i = 0; i < files.length; i++) {
            uuid = uploadUserVideo(files[i], request);
            videoList.add(new Video(videoTotalUUID, uuid , files.length == 1 ? videoTotal.getTitle() : getFileNameNoEx(files[i].getOriginalFilename())));
        }
        if (zipFile!=null) {
            File file = new File(Util.VIDEO_SAVE_PATH +uuid +"/"+ uuid+".zip");
            zipFile.transferTo(file);
            videoTotal.setPath(file.getPath());
        }
        if (Util.util.videoMapper.addVideoTotal(videoTotal) == 0) {
            throw new Exception("提交失败!");
        }
        responseJson.put("count", Util.util.videoMapper.addVideoList(videoList));
        return responseJson.toString();
    }

    public String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public void deleteFiles(File filePath) {
        File[] files = filePath.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteFiles(f);
            }
        }
        filePath.delete();
    }

}
