package com.video.server.controller;

import com.video.server.pojo.VideoTotal;
import com.video.server.utils.Util;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @Author : zp
 * @Description :
 * @Date : 2020/5/21
 */
@Controller
public class Down {
    /**
     * 下载视频附件
     * @param id
     * @param response
     */
    @GetMapping("/downVideoZip")
    public void downVideoZip(String id, HttpServletResponse response){
        String path = Util.util.videoMapper.getPath(id);
        File file = new File(path);
        String name = file.getName().trim().replaceAll(" ", "");
        try {
            response.setContentType("application/force-download");
            response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(name, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.copyFile(file, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
