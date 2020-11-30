package com.video.server.view;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.video.server.utils.AesUtil;
import com.video.server.utils.Util;

@RestController
public class ViewVideo {
	
	/**
	 * 获取视频
	 * @return
	 */
	@GetMapping("getVideo")
	public String getVideo(String id, HttpServletRequest request) {
		var userPhone = request.getHeader("userPhone");
		if (id != null && Util.isViewVideoAuto(id, userPhone)) {
			return AesUtil.aesEncrypt(Util.readText(Util.VIDEO_SAVE_PATH + id + "/" + id + ".m3u8"));
		}
		return "";
	}

	/**
	 * 获取视频key
	 * @return
	 */
	@GetMapping("getVideoKey")
	public String getVideoKey(String id, HttpServletRequest request) {
		var userPhone = request.getHeader("userPhone");

		if (id != null && Util.isViewVideoAuto(id, userPhone)) {
			return Util.readText(Util.VIDEO_SAVE_PATH + id + "/" + Util.VIDEO_KEY_NAME);
		}
		return "";
	}
	
	/**
	 * 获取视频切片
	 * @throws IOException
	 */
	@GetMapping("getVideoFragment")
	public void getVideoFragment(String id,HttpServletRequest request, HttpServletResponse response) {
		if (id == null || id.trim().length() < 5) {
			return;
		}
		var userPhone = request.getHeader("userPhone");
		var uuid = id.substring(0, id.length() - 5);
		var file = new File(Util.VIDEO_SAVE_PATH + uuid + "/" + id);
		if (file.exists() && Util.isViewVideoAuto(uuid, userPhone)) {
			try {
				byte[] fileByte = Files.readAllBytes(file.toPath());
				OutputStream os = response.getOutputStream();
				response.reset();
				response.setHeader("Content-Disposition","attachment;");
				os.write(fileByte);
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
