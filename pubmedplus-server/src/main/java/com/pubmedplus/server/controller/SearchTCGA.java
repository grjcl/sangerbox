package com.pubmedplus.server.controller;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import com.pubmedplus.server.pojo.Style;
import com.pubmedplus.server.pojo.TcgaFile;
import com.pubmedplus.server.pojo.TcgaModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.utils.JSONObjectUtil;
import com.pubmedplus.server.utils.MergeFileUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@Validated
public class SearchTCGA {
	
	/**
	 * 获取查询下拉
	 */
	@GetMapping("/getTcgaCancerSelectList")
	public String getTcgaCancerSelectList() throws Exception {
		var responseJson = new JSONObject();
		responseJson.put("tcgaCancerSelectList", JSONArray.fromObject(Util.util.tcgaMapper.getTcgaCancerSelectList(), JSONObjectUtil.getJsonConfig()));
		return responseJson.toString();
	}
	
	/**
	 * Files列内容
	 */
	@GetMapping("/getTcgaCancerCategoryFilesCount")
	public String getTcgaCancerCategoryFilesCount(@NotBlank String id) throws Exception {
		var responseJson = new JSONObject();
		responseJson.put("tcgaCancerFilesCount", JSONArray.fromObject(Util.util.tcgaMapper.getTcgaCancerCategoryFilesCount(id), JSONObjectUtil.getJsonConfig()));
		return responseJson.toString();
	}
	
	/**
	 * Case列内容
	 */
	@GetMapping("/getTcgaCancerCategoryCaseCount")
	public String getTcgaCancerCategoryCaseCount(@NotBlank String id) throws Exception {
		var responseJson = new JSONObject();
		responseJson.put("tcgaCancerCaseCount", JSONArray.fromObject(Util.util.tcgaMapper.getTcgaCancerCategoryCaseCount(id), JSONObjectUtil.getJsonConfig()));
		return responseJson.toString();
	}
	
	/**
	 * Files列内容
	 */
	@GetMapping("/getTcgaCancerStrategyFilesCount")
	public String getTcgaCancerStrategyFilesCount(@NotBlank String id) throws Exception {
		var responseJson = new JSONObject();
		responseJson.put("tcgaCancerStrategyFilesCount",  JSONArray.fromObject(Util.util.tcgaMapper.getTcgaCancerStrategyFilesCount(id), JSONObjectUtil.getJsonConfig()));
		return responseJson.toString();
	}

	/**
	 * Case列内容
	 */
	@GetMapping("/getTcgaCancerStrategyCaseCount")
	public String getTcgaCancerStrategyCaseCount(@NotBlank String id) throws Exception {
		var responseJson = new JSONObject();
		responseJson.put("tcgaCancerStrategyCaseCount", JSONArray.fromObject(Util.util.tcgaMapper.getTcgaCancerStrategyCaseCount(id), JSONObjectUtil.getJsonConfig()));
		return responseJson.toString();
	}
	
	/**
	 * FPKM
	 * @throws Exception
	 */
	@PostMapping("/getTcgaTypeInfo")
	public String getTcgaTypeInfo(@NotBlank String id,String workflowType,String dataType,String platform,String dataCategory,String dataFormat,
								  String dataFormatNoT,String location,String locationNot,String experimental,String experimentalNoT,
								  String transferNoT, String outsideNoT, String platformNoT) throws Exception {
		var responseJson = new JSONObject();
		if (workflowType == null && dataType == null && platform == null && dataCategory == null && dataFormat == null && dataFormatNoT == null && location==null && locationNot==null && experimental==null && experimentalNoT==null) {
			return responseJson.toString();
		}
		List<TcgaFile> tcgaFiles = Util.util.tcgaMapper.listTcgaTypeInfo(id, workflowType, dataType, platform, dataCategory, dataFormat, dataFormatNoT, location, locationNot, experimental, experimentalNoT, transferNoT, outsideNoT, platformNoT);
		responseJson.put("tcgaTypeInfoList", JSONArray.fromObject(tcgaFiles, JSONObjectUtil.getJsonConfig()));

		return responseJson.toString();
	}

	@GetMapping("/downloadTcgaData")
	public void downloadTcgaData(@NotBlank String id,@NotBlank String fileId, HttpServletResponse response) throws Exception {
		try {
			var tcgaFile = Util.util.tcgaMapper.getTcgaFile(fileId);
			if(tcgaFile==null) {
				return;
			}
			var file = new File("/pub1/data/TCGA/Backup_Files/"+id+"/"+fileId+"."+tcgaFile.getDataFormat());
			if (file.exists()) {
				byte[] fileByte = Files.readAllBytes(file.toPath());
				Util.responseByte(fileByte, file.getName(), response);
			}
		} catch (Exception e) {

		}
	}

	@PostMapping("/drawTcgaPng")
	public String drawTcgaPng(@NotBlank String id){
		List<TcgaFile> tcgaFiles = Util.util.tcgaMapper.listTcgaTypeInfo(id, null, null,
				null, null, null, null, null, null, null, null, null, null, null);
		var responseJson = new JSONObject();
		Set<String> set1 = new HashSet<>();
		Set<String> set2 = new HashSet<>();
		Set<String> set3 = new HashSet<>();
		Set<String> set4 = new HashSet<>();
		Set<String> set5 = new HashSet<>();
//		Map<String, Long> collect = tcgaFiles.stream().collect(Collectors.groupingBy(TcgaFile::getDataCategory, Collectors.counting()));
//		Map<String, Long> exp = tcgaFiles.stream().collect(Collectors.groupingBy(TcgaFile::getExperimentalStrategy, Collectors.counting()));
//		Map<String, Long> plat = tcgaFiles.stream().collect(Collectors.groupingBy(TcgaFile::getPlatform, Collectors.counting()));
//		Map<String, Long> work = tcgaFiles.stream().collect(Collectors.groupingBy(TcgaFile::getWorkflowType, Collectors.counting()));
//		Map<String, Long> data = tcgaFiles.stream().collect(Collectors.groupingBy(TcgaFile::getDataType, Collectors.counting()));
		for (TcgaFile tcgaFile : tcgaFiles){
			set1.add(tcgaFile.getDataCategory());
		}
		for (TcgaFile tcgaFile : tcgaFiles){
			set2.add(tcgaFile.getExperimentalStrategy());
		}
		for (TcgaFile tcgaFile : tcgaFiles){
			set3.add(tcgaFile.getPlatform());
		}
		for (TcgaFile tcgaFile : tcgaFiles){
			set4.add(tcgaFile.getWorkflowType());
		}
		for (TcgaFile tcgaFile : tcgaFiles){
			set5.add(tcgaFile.getDataType());
		}
		JSONObject js = new JSONObject();
		String[] style = new String[]{"#003f5c","#2e5572","#4d6b88","#6b839f","#899bb7","#a7b5ce","#c5cfe6","#e3eaff"};
		int i = 0;
		for (String s1:set1){
			List<TcgaFile> t = tcgaFiles;
			t = t.stream().filter(tcgaFile -> tcgaFile.getDataCategory().equals(s1)).collect(Collectors.toList());
			TcgaModel t1 = new TcgaModel(s1,t.size(),new ArrayList<>());
			Style s = new Style(style[i]);
			i++;
			t1.setItemStyle(s);
			for (String s2:set2) {
				List<TcgaFile> tt1 = t;
				tt1 = tt1.stream().filter(tcgaFile -> tcgaFile.getExperimentalStrategy().equals(s2)).collect(Collectors.toList());
				TcgaModel t2 = new TcgaModel(s2,tt1.size(),new ArrayList<>());
				t2.setItemStyle(s);
				for (String s3:set3) {
					List<TcgaFile> tt2 = tt1;
					tt2 = tt2.stream().filter(tcgaFile -> tcgaFile.getPlatform().equals(s3)).collect(Collectors.toList());
					TcgaModel t3 = new TcgaModel(s3,tt2.size(),new ArrayList<>());
					t3.setItemStyle(s);
					for (String s4:set4) {
						List<TcgaFile> tt3 = tt2;
						tt3 = tt3.stream().filter(tcgaFile -> tcgaFile.getWorkflowType().equals(s4)).collect(Collectors.toList());
						TcgaModel t4 = new TcgaModel(s4,tt3.size(),new ArrayList<>());
						t4.setItemStyle(s);
						for (String s5:set5) {
							List<TcgaFile> tt4 = tt3;
							tt4 = tt4.stream().filter(tcgaFile -> tcgaFile.getDataType().equals(s5)).collect(Collectors.toList());
							TcgaModel t5 = new TcgaModel(s5,tt4.size(),new ArrayList<>());
							t5.setItemStyle(s);
							t4.add(t5);
						}
						t3.add(t4);
					}
					t2.add(t3);
				}
				t1.add(t2);
			}
			js.put(s1,t1);
		}
		responseJson.put("resp",js);
		return responseJson.toString();
	}


	@PostMapping("/downloadTcgaDataAll")
	public String downloadTcgaDataAll(@NotBlank String id,@NotBlank String type,String workflowType,String dataType,
									  String platform,String dataCategory,String dataFormat,String dataFormatNoT,String location,
									  String experimental,String locationNot,HttpServletRequest request,String experimentalNoT,
									  String transferNoT, String outsideNoT, String platformNoT) {
		if (workflowType == null && dataType == null && platform == null && dataCategory == null && dataFormat == null && dataFormatNoT == null&&location==null&&locationNot==null&&transferNoT!=null
				&&outsideNoT!=null&&platformNoT!=null) {
			return null;
		}
		var userPhone = request.getHeader("userPhone");
		if (!Util.isPhone(userPhone)) {
			return null;
		}
		var userFile = new File(Util.USER_DATA + userPhone + "/" + id + "_" + type);
		if (!userFile.exists()) {
			userFile.mkdirs();
		}
		Util.sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, userPhone, "开始下载"+id + "_" + type+"数据");
		var tcgaFileList=Util.util.tcgaMapper.listTcgaTypeInfo(id,workflowType, dataType, platform, dataCategory, dataFormat, dataFormatNoT,location,locationNot,
				experimental,experimentalNoT,transferNoT,outsideNoT,platformNoT);
		if (tcgaFileList.size() == 0) {
			return null;
		}
		var txtSb=new StringBuilder("id\tfilename\tmd5\tsize\tstate\tsample_id\tsample_type\n");
		for (var tcgaFile : tcgaFileList) {
//			Util.runCmd("ln -s '/pub1/data/TCGA/Backup_Files/"+id+"/"+tcgaFile.getFileId()+"."+tcgaFile.getDataFormat()+"' '"+userFile+"/"+tcgaFile.getFileName()+"'");
			File file1 = new File("/pub1/data/TCGA/Backup_Files/"+id+"/"+tcgaFile.getFileId()+"."+tcgaFile.getDataFormat());
			File file2 = new File(userFile+"/"+tcgaFile.getFileName());
//			创建软链接
			try {
				Files.createSymbolicLink(file2.toPath(),file1.toPath());
			} catch (Exception e) {

			}
			txtSb.append(tcgaFile.getFileId()+"\t"+tcgaFile.getFileName()+"\t"+Util.strToMd5(tcgaFile.getFileId())+"\t"+tcgaFile.getFileSize()+"\tvalidated\t"+tcgaFile.getSubmitterId()+"\t"+tcgaFile.getSampleType()+"\n");
		}
		if(Util.writerText(userFile + "/MANIFEST.txt", txtSb.toString())) {
			if(MergeFileUtil.startMergeFile(userFile.toString(), id + "_Merge.txt", type)) {
				Util.sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, userPhone, "200;"+id + "_" + type+"下载成功,目录:/"+id + "_" + type);
				return null;
			}
		}
		Util.sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, userPhone, "500;"+id + "_" + type+"下载失败!");
		return null;
	}

	@PostMapping("/getTcgaTypeInfos")
	public String getTcgaTypeInfos(String id) throws Exception {
		JSONObject js = new JSONObject();
		String name = id.replaceAll("-", "_");
		String o = "/pub1/data/mg_projects/users/zhurf/Sangerbox/Rscript/TmpData/"+name;
		String cmd = "Rscript /pub1/data/mg_projects/users/zhurf/Sangerbox/Rscript/getTCGASampleStatistics.R -i "
				+id+" -o "+o;
		File f= new File(o);
		if (!f.exists()){
			f.mkdir();
			String s = Util.runCmd(cmd);
		}
		File[] files = f.listFiles();
		StringBuilder sb = new StringBuilder();
		for (File file: files) {
			if (file.getName().endsWith(".json")){
				BufferedReader bf = new BufferedReader(new FileReader(file));
				String str;
				while ((str=bf.readLine())!=null){
					sb.append(str);
				}
				bf.close();
				js.put("json",sb.toString());
			}else if (file.getName().endsWith(".png")){
				String imgStr = Util.getImgStr(file.getPath());
				js.put("img",imgStr);
			}
		}
		return js.toString();
	}
	
}
