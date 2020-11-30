package com.video.server.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.video.server.dao.IVideoMapper;

@Component
public class Util {

	public static final String VIDEO_SAVE_PATH = "/pub1/data/user_video/";
	public static final String VIDEO_KEY_NAME = "enc.key";
	public static final String PROJECT_SITE = "http://cloud.sangerbox.com/video/";
//	public static final String ZIP_PATH = "/pub1/data/user_video/";

	@Resource
	public IVideoMapper videoMapper;
	@Resource
	public RabbitTemplate rabbitTemplate;
	
	// 维护一个本类的静态变量
	public static Util util;

	@PostConstruct
	public void init() {
		util = this;
		util.videoMapper = this.videoMapper;
		util.rabbitTemplate = this.rabbitTemplate;
	}
	
	/**
	 * 发送rabbitmq路由模式消息
	 * @param exchange
	 * @param routingKey
	 * @param message
	 */
	public static void sendRabbitmqRoutingMessage(String routingKey, String message) {
		if (routingKey != null && message != null) {
			Util.util.rabbitTemplate.convertAndSend("rabbitmqTackWeb", routingKey, message);
		}
	}
	
	/**
	 * 判断用户是否可以查看视频
	 * @param str
	 * @return
	 */
	public static Boolean isViewVideoAuto(String id,String userPhone) {
		var video = Util.util.videoMapper.getVideo(id);
		if (video != null) {
			if (new BigDecimal(video.getMoney()).compareTo(new BigDecimal("0"))<1) {
				return true;
			}
			if (Util.isPhone(userPhone)) {
				var videoTotalInfo = Util.util.videoMapper.getTotalInfo(id);
//				判断用户是否有观看收费视频的权限
				if (Util.util.videoMapper.isVideoViewPayAuto(videoTotalInfo.getId(), userPhone) > 0) {
					return true;
				}
//				TODO  工具  高级会员免费
				if (videoTotalInfo.getType()!=null ){
					Integer memberInfo = Util.util.videoMapper.getMemberInfo(userPhone);
					if (memberInfo!=null&&memberInfo==5){
						return true;
					}
				}
//				if (videoTotalInfo.getType()!=null && "工具".equals(videoTotalInfo.getType().trim())){
//					Integer memberInfo = Util.util.videoMapper.getMemberInfo(userPhone);
//					if (memberInfo!=null&&memberInfo==4){
//						return true;
//					}
//				}

			}
		}
		return false;
	}
	
	/**
	 * 判断字符串是不是纯数字
	 * @param str
	 * @return
	 */
	public static Boolean isNumeric(String str) {
		var reg = "^[0-9]+(.[0-9]+)?$";
		if (str != null && !str.isBlank() && str.matches(reg)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 判断字符串是不是手机号 
	 * @param mobiles
	 * @return
	 */
	public static boolean isPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return false;
		}
		Pattern p = Pattern.compile("1\\d{10}");
		Matcher m = p.matcher(phone);
		return m.matches();
	}
	
	/**
	 * 生成文本
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static Boolean writerText(String filePath, String text) {
		try {
			var writer = new PrintWriter(filePath, "UTF-8");
			writer.println(text);
			writer.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 读取文本
	 * @param filePath
	 * @return
	 */
	public static String readText(String filePath) {
		var aryList=new StringBuffer();
		try {
			var isr = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
			var reader = new BufferedReader(isr);
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				aryList.append(tempString+"\n");
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aryList.toString();
	}
	
	/**
	 * 生成UUID
	 * @return
	 */
	public static String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-","");
	}
	
	/**
	 * 运行cmd
	 * @param cmds
	 * @return
	 * @throws IOException
	 */
	public static int runCmd(String cmd){
		System.out.println(cmd);
		try {
			var process = Runtime.getRuntime().exec(cmd);
			return printlnRdata(process);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	
	/**
	 * 打印R运行结果
	 * @param process
	 * @param logSb 
	 * @return 
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static int printlnRdata(Process process) throws Exception{
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				BufferedReader reader=null;
				try {
					reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				BufferedReader reader=null;
				try {
					reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line = null;
					while ((line = reader.readLine()) != null) {
						System.out.println(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		return process.waitFor();
	}
	
	/**
	 * 压缩字符串
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString("ISO-8859-1");
    }
	
}
