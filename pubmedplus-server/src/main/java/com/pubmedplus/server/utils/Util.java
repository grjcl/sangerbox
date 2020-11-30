package com.pubmedplus.server.utils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.dao.pubmed.IArticleMapper;
import com.pubmedplus.server.dao.pubmed.IGenesMapper;
import com.pubmedplus.server.dao.pubmed.IJournalMapper;
import com.pubmedplus.server.dao.pubmed.IPubmedPlusMapper;
import com.pubmedplus.server.dao.pubmed.IRabbitMapper;
import com.pubmedplus.server.dao.pubmed.ITcgaMapper;
import com.pubmedplus.server.dao.sangerbox.IGeoMapper;
import com.pubmedplus.server.dao.sangerbox.ISangerBoxMapper;
import com.pubmedplus.server.pojo.AmqpRabbitListener;
import com.pubmedplus.server.pojo.Jourcache;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

import net.sf.json.JSONObject;

@Component
@RestController
@EnableScheduling
public class Util {

	@Resource
	public RestHighLevelClient restHighLevelClient;
	@Resource
	public RabbitTemplate rabbitTemplate;
	@Resource
	public IPubmedPlusMapper pubmedPlusMapper;
	@Resource
	public IArticleMapper articleMapper;
	@Resource
	public IJournalMapper journalMapper;
	@Resource
	public IGenesMapper genesMapper;
	@Resource
	public IGeoMapper geoMapper;
	@Resource
	public IRabbitMapper rabbitMapper;
	@Resource
	public ITcgaMapper tcgaMapper;
	@Resource
	public ISangerBoxMapper sangerBoxMapper;

	public static final String SEARCH_PUBMED_INDEX = "pubmed";
	public static final String SEARCH_JOURNAL_INDEX = "journal";
	public static final String SEARCH_FUND_INDEX = "fund";
	public static final String SEARCH_GENES_INDEX = "genes";
	public static final String SEARCH_GENES_ID_TO_ID_INDEX = "genesidtoid";
	public static final String SEARCH_GEO_INDEX = "geo";
	
	public static final String GEO_FILE_PREFIX="/pub1/data/";
	public static final String R_SCRIPT="/pub1/data/mg_projects/users/zhurf/Sangerbox/Rscript/";
	public static final String R_DATA="/pub1/data/mg_projects/users/zhurf/Sangerbox/Rscript/TmpData/";
	
	public static final String RABBITMQ_TACK = "rabbitmqTack";
	public static final String RABBITMQ_TACK_WEB = "rabbitmqTackWeb";
	
	public static final String USER_DATA="/pub1/data/user_data/";
	
	public static final int EXECUTOR_SERVICE_COUNT = 40;
	
	public static Util util;

	@PostConstruct
	public void init() {
		util = this;
		util.restHighLevelClient = this.restHighLevelClient;
		util.rabbitTemplate = this.rabbitTemplate;
		util.pubmedPlusMapper = this.pubmedPlusMapper;
		util.articleMapper = this.articleMapper;
		util.journalMapper = this.journalMapper;
		util.sangerBoxMapper = this.sangerBoxMapper;
		util.genesMapper = this.genesMapper;
		util.geoMapper = this.geoMapper;
		util.rabbitMapper = this.rabbitMapper;
		util.tcgaMapper = this.tcgaMapper;
		
		var ids = Util.util.rabbitMapper.list_amqpRabbitListenerIncomplete();
		ids.forEach(id -> {
			Util.util.rabbitTemplate.convertAndSend(Util.RABBITMQ_TACK, String.valueOf(id));
		});
	}

	public static String countGeoAmount(){
		return Util.util.geoMapper.countGeoAmount();
	}

	public static List<List<String>> splitList(List<String> list, int groupSize) {
		int length = list.size();
		int num = (length + groupSize - 1) / groupSize;
		List<List<String>> newList = new ArrayList<>(num);
		for (int i = 0; i < num; i++) {
			int fromIndex = i * groupSize;
			int toIndex = (i + 1) * groupSize < length ? (i + 1) * groupSize : length;
			newList.add(list.subList(fromIndex, toIndex));
		}
		return newList;
	}
	
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
	
	public static String strToMd5(String text) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
	        md5.update(text.getBytes("UTF-8"));
	        return new BigInteger(1, md5.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return UUID.randomUUID().toString().replaceAll("-","");
	}
	
	public static String startDrawPng(StringBuilder cmd,String f){
		var md5 = strToMd5(cmd.toString());
		cmd.append(" -o " + md5 + "."+f+" -e " + md5 + "." + f + ".error.log");
		var filePath = new File(Util.R_DATA + md5 + "." + f);
		var cmdLog = runCmd(cmd.toString());
		if (cmdLog == null || cmdLog.isBlank() || !filePath.exists()) {
			return null;
		}
		if("txt".equals(f)) {
			return Util.readerFile(filePath.toString()).toString();
		}else if("png".equals(f)) {
			try {
				byte[] textByte = Files.readAllBytes(filePath.toPath());
				return new Base64().encodeToString(textByte);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filePath.toString();
	}

	public static String startDrawPngs(StringBuilder cmd,String f){
		var md5 = strToMd5(cmd.toString());
		String file = Util.R_DATA + md5 + "." + f;
		cmd.append(" -o ").append(file);
		var filePath = new File(file);
		if (!filePath.exists()){
			var cmdLog = runCmd(cmd.toString());
			if (cmdLog == null || cmdLog.isBlank() || !filePath.exists()) {
				return null;
			}
		}
		if("png".equals(f.trim())) {
			System.out.println(cmd.toString());
			try {
				return new Base64().encodeToString(Files.readAllBytes(filePath.toPath()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filePath.toString();
	}

	public static String runCmd(String cmd){
		System.out.println(cmd);
		try {
			var process = Runtime.getRuntime().exec(cmd);
			return printlnRdata(process);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String printlnRdata(Process process) throws Exception{
		var logSb = new StringBuffer();
		var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			logSb.append(line+"\n");
//			System.out.println("R运行结果:" + line);
		}
		reader.close();
		reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		line = null;
		while ((line = reader.readLine()) != null) {
			logSb.append(line+"\n");
//			System.out.println("R运行错误:" + line);
		}
		reader.close();
		process.waitFor();
		return logSb.toString();
	}

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
	   
	public static List<String> readerFile(String filePath) {
		List<String> list=new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			String tempString=null;
			while ((tempString = reader.readLine()) != null) {
				list.add(tempString);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static void responseByte(byte[] fileByte,String fileName,HttpServletResponse response) throws Exception {
		OutputStream os = response.getOutputStream();
		response.reset();
		response.setHeader("Content-Disposition","attachment; filename=" + fileName);
		os.write(fileByte);
		os.close();
	}
	
	public static int getRabbitListenerMessageCount(String rabbitListenerName) {
		DeclareOk declareOk = Util.util.rabbitTemplate.execute(channel -> channel.queueDeclarePassive(rabbitListenerName));
		return declareOk.getMessageCount();
	}
	
	public static Boolean rabbitmqAckSuccess(Message message, Channel channel) {
		try {
			if(channel!=null) {
				channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			}
			return true;
		} catch (Exception e) {
		}
		return false;
	}
	
	public static void rabbitmqAckError2(Message message, Channel channel) {
		try {
			if(channel!=null) {
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		} catch (IOException e) {
		}
	}
	
	public static synchronized void sendRabbitmqTask(AmqpRabbitListener amqpRabbitListener) {
		if (amqpRabbitListener == null) {
			return;
		}
		System.out.println(amqpRabbitListener.getCmdMd5());
		var message = Util.util.rabbitMapper.exist_amqpRabbitListener(amqpRabbitListener.getCmdMd5()) == 0 ? null : "请勿重复提交!";
		if (message == null && Util.util.rabbitMapper.add_amqpRabbitListener(amqpRabbitListener) > 0) {
			var amqpRunCount = Util.util.rabbitMapper.count_amqpRabbitListenerRunCount(1);
			if (amqpRunCount < Util.EXECUTOR_SERVICE_COUNT) {
				amqpRunCount = 0;
			} else {
				amqpRunCount = Util.util.rabbitMapper.count_amqpRabbitListenerRunCount(0);
			}
			message=amqpRunCount==0?"":"已将任务发布到后台排队中,你的前面有" + amqpRunCount + "个任务,请耐心等待!";
			Util.util.rabbitTemplate.convertAndSend(Util.RABBITMQ_TACK, String.valueOf(amqpRabbitListener.getId()));
		} else if (message == null) {
			message = "服务器错误,请稍后再试!";
		}
		if(message.isBlank()) {
			return;
		}
		sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, amqpRabbitListener.getUserPhone(), message);
	}
	
	public static void getRabbitmqTask(String id, Message message, Channel channel) {
		var amqpRabbitListener = Util.util.rabbitMapper.get_amqpRabbitListener(id);
		if (amqpRabbitListener == null || amqpRabbitListener.getStatus()==1) {
			rabbitmqAckSuccess(message, channel);
			return;
		}
		var responseMessage = Util.util.rabbitMapper.update_amqpRabbitListenerRun(id) == 0 ? amqpRabbitListener.getDesc()+"错误,请重新发布!" : null;
		if (responseMessage != null) {
			rabbitmqAckSuccess(message, channel);
			sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, amqpRabbitListener.getUserPhone(), responseMessage);
			return;
		}
		sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, amqpRabbitListener.getUserPhone(),"开始"+amqpRabbitListener.getDesc());
		var cmdLog = runCmd(amqpRabbitListener.getCmd());
		Util.util.rabbitMapper.update_amqpRabbitListenerLog(id, cmdLog);
		responseMessage = amqpRabbitListener.getDesc() + "错误,请重新发布!";
		var successFile = new File(amqpRabbitListener.getUserFile() + amqpRabbitListener.getSuccessFilePath());
		var errorFile = new File(amqpRabbitListener.getUserFile() + amqpRabbitListener.getErrorFilePath());
		var status = 200;
		if (successFile.exists()){
			responseMessage = amqpRabbitListener.getDesc() + "完成,位置:" + amqpRabbitListener.getSuccessFilePath();
		}else if(errorFile.exists()) {
			status = 500;
			responseMessage = amqpRabbitListener.getDesc() + "失败,失败日志:" + amqpRabbitListener.getErrorFilePath();
		}else {
			status = 404;
			responseMessage = amqpRabbitListener.getDesc() + "失败,请检查文件空间!";
		}
		if (Util.util.rabbitMapper.update_amqpRabbitListenerEnd(id, status) == 0) {
			responseMessage = "服务器错误,请重新发布" + amqpRabbitListener.getDesc() + "任务!";
		}
		responseMessage = status + ";" + responseMessage;
		rabbitmqAckSuccess(message, channel);
		sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, amqpRabbitListener.getUserPhone(), responseMessage);
	}
	
	public static void sendRabbitmqRoutingMessage(String exchange, String routingKey, String message) {
		if (exchange != null && routingKey != null && message != null) {
			Util.util.rabbitTemplate.convertAndSend(exchange, routingKey, message);
		}
	}
	
	public static Jourcache getJourcacheNowIfs(Jourcache jourcache) {
		if (jourcache == null) {
			return null;
		}
		var currentYear=Integer.valueOf(new SimpleDateFormat("yyyy").format(new Date()));
		var jcrIfs=JSONObject.fromObject(jourcache.getJcrIfs(),JSONObjectUtil.getJsonConfig());
		while (currentYear-- > 2013) {
			if (jcrIfs.containsKey("year" + currentYear)) {
				var nowIfs = Float.valueOf(jcrIfs.get("year" + currentYear).toString());
				if (nowIfs > 0.0) {
					jourcache.setIfs(jcrIfs.getDouble("year" + currentYear));
					break;
				}
			}
		}
		jourcache.setJcrIfs(null);
		return jourcache;
	}
	
	public static Boolean isNumeric(String str) {
		if (str != null && !str.isBlank() && StringUtils.isNumeric(str)) {
			return true;
		}
		return false;
	}
	
	public static boolean isPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			return false;
		}
		Pattern p = Pattern.compile("1\\d{10}");
		Matcher m = p.matcher(phone);
		return m.matches();
	}
	
	public static String unescapeStr(String str) {
		try {
			return (str == null || str.isBlank()) ? null: StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeJava(str)));
		} catch (Exception e) {
		}
		try {
			return (str == null || str.isBlank()) ? null
					: StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(str));
		} catch (Exception e) {
		}
		return str;
	}

	public static Date getYearFirst(int year) {
		var calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		return calendar.getTime();
	}

	public static Date getYearLast(int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.roll(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime();
	}

	@Scheduled(fixedDelay = 1800000)
	public void sendHeartbeat() throws IOException {
		Util.util.restHighLevelClient.exists(new GetRequest(Util.SEARCH_PUBMED_INDEX)
				.id("0").fetchSourceContext(new FetchSourceContext(false)).storedFields("_none_"), RequestOptions.DEFAULT);
	}

	public static String getImgStr(String imgFile) {
		// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String(Objects.requireNonNull(Base64.encodeBase64(data)));
	}
}
