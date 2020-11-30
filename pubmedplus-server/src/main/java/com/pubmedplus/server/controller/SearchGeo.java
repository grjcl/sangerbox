package com.pubmedplus.server.controller;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import com.pubmedplus.server.pojo.GeoObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.AmqpRabbitListener;
import com.pubmedplus.server.pojo.PubmedGeo;
import com.pubmedplus.server.utils.ElasticSearchUtil;
import com.pubmedplus.server.utils.Util;
import com.pubmedplus.server.utils.ZipUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@Validated
public class SearchGeo {

	@GetMapping("/countGeoAmount")
	public String countGeoAmount(){
		var responseJson = new JSONObject();
		Date date=new Date();
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		responseJson.put("time",dateFormat.format(date));
		responseJson.put("countAmount",Util.countGeoAmount());
		return responseJson.toString();
	}
	@GetMapping("/getDataPlatform")
	public String dataPlatform(@NotBlank String accession){
		var responseJson = new JSONObject();
		String str = Util.util.geoMapper.findPlatformIDsByAccession(accession);
		if(org.springframework.util.StringUtils.isEmpty(str)){
			System.out.println("数据为空");
			responseJson.put("data",null);
			return responseJson.toString();
		}
		String[] splits = str.split(";");
		List list = new ArrayList();
		Map<String,String> map = new HashMap<>();
		for (String platform:splits
		) {
			map.put("platform",platform);
			String describe = Util.util.geoMapper.findTitleByAccession(platform);
			map.put("describe",describe);
			String sample = Util.util.geoMapper.countSample(accession,platform);
			map.put("sample",sample);
			String originalDataExport;
			var geoFileList = Util.util.geoMapper.get_geoFileListIsLocal(accession.trim());
			if (geoFileList.size() == 0) {
				originalDataExport=null;
			}else{
				originalDataExport=platform;
			}
			String dataExport;
			if("{}".equals(Util.util.geoMapper.get_PlatformInfomation(accession.trim(), platform.trim()))&& "{}".equals(Util.util.geoMapper.get_GsmInfomation(accession.trim(), platform.trim()))){
				dataExport=null;
				originalDataExport=null;
			}else{
				dataExport=platform;
			}
			list.add(new GeoObject(platform, describe,sample,dataExport,originalDataExport));
		}

		responseJson.put("data",list);
		return responseJson.toString();
	}

	@PostMapping("/searchGeoInfo")
	public String searchGeoInfo(PubmedGeo pubmedGeo) throws Exception {
		var responseJson = new JSONObject();
		var multiSearchRequest = new MultiSearchRequest();
//		BoolQueryBuilder进行复合查询
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
//		封装查询条件 返回的文档可能满足should子句的条件.在一个bool查询中,如果没有must或者filter,有一个或者多个should子句,那么只要满足一个就可以返回.minimum_should_match参数定义了至少满足几个子句.  or
		boolQueryBuilder.should(QueryBuilders.termQuery("accession", pubmedGeo.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("pubmedIDs", pubmedGeo.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("platformIDs", pubmedGeo.queryStringQuery));

		multiSearchRequest.add(pubmedGeo.getSearchRequest(boolQueryBuilder,10));
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedGeo.queryStringQuery.length()>0) {
			boolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.should(QueryBuilders.termQuery("title.uppercase", pubmedGeo.queryStringQuery).boost(100));
			boolQueryBuilder.should(QueryBuilders.queryStringQuery(pubmedGeo.queryStringQuery)
					.field("title",3).field("summary",5).field("gsmInfoTitle.english").field("sampleTypesJson.english").lenient(true));
			boolQueryBuilder.should(QueryBuilders.termQuery("sampleTypesJson", pubmedGeo.queryStringQuery));
			var queryStringQuery = QueryParser.escape(pubmedGeo.queryStringQuery);
			boolQueryBuilder.should(QueryBuilders.regexpQuery("gsmInfoTitle", ".*?" + queryStringQuery + ".*?"));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("title", queryStringQuery).boost(5));
			boolQueryBuilder.should(QueryBuilders.matchPhraseQuery("summary", queryStringQuery).boost(10));
		}

		multiSearchRequest.add(pubmedGeo.getSearchRequest(boolQueryBuilder,10));
//		System.out.println(multiSearchRequest.toString());
//		执行查询
		var searchResponse = Util.util.restHighLevelClient.msearch(multiSearchRequest, RequestOptions.DEFAULT);
		org.elasticsearch.action.search.MultiSearchResponse.Item[] responsesItems = searchResponse.getResponses();
		for (org.elasticsearch.action.search.MultiSearchResponse.Item item : responsesItems) {
			if (item.getResponse()!=null&&item.getResponse().getHits().getTotalHits().value > 0) {
				responseJson.put("GeoList", ElasticSearchUtil.getSearchElasticsearchData(item.getResponse(), "title"));
				break;
			}
		}
		return responseJson.toString();
	}
	
	@PostMapping("/searchGeoSampleTypesInfo")
	public String searchGeoSampleTypesInfo(PubmedGeo pubmedGeo) throws Exception {
		var responseJson = new JSONObject();
		var sampleTypesSet = new HashSet<String>();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.should(QueryBuilders.termQuery("accession", pubmedGeo.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("pubmedIDs", pubmedGeo.queryStringQuery));
		boolQueryBuilder.should(QueryBuilders.termQuery("platformIDs", pubmedGeo.queryStringQuery));
		var searchRequest = pubmedGeo.getSearchRequest(boolQueryBuilder,3000);
		var searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		if (searchResponse.getHits().getTotalHits().value > 0) {
			searchGeoScroll(searchResponse,searchResponse.getScrollId(),sampleTypesSet);
			responseJson.put("sampleTypesList", sampleTypesSet);
			return responseJson.toString();
		}
		
		boolQueryBuilder = QueryBuilders.boolQuery();
		if(pubmedGeo.queryStringQuery.length()>0) {
			boolQueryBuilder.minimumShouldMatch(1);
			boolQueryBuilder.should(QueryBuilders.termQuery("title.uppercase", pubmedGeo.queryStringQuery).boost(100));
			boolQueryBuilder.should(QueryBuilders.queryStringQuery(QueryParser.escape(pubmedGeo.queryStringQuery))
					.field("title",3).field("summary",5).field("gsmInfoTitle.english").field("sampleTypesJson.english").lenient(true));
			boolQueryBuilder.should(QueryBuilders.termQuery("sampleTypesJson", pubmedGeo.queryStringQuery));
			boolQueryBuilder.should(QueryBuilders.regexpQuery("gsmInfoTitle", ".*?" + QueryParser.escape(pubmedGeo.queryStringQuery) + ".*?"));
		}
		searchRequest = pubmedGeo.getSearchRequest(boolQueryBuilder,3000);
		searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		if (searchResponse.getHits().getTotalHits().value > 0) {
			searchGeoScroll(searchResponse,searchResponse.getScrollId(),sampleTypesSet);
			responseJson.put("sampleTypesList", sampleTypesSet);
		}
		return responseJson.toString();
	}
	
	public HashSet<String> searchGeoScroll(SearchResponse searchResponse, String scrollId, HashSet<String> sampleTypesSet) throws Exception {
		var searchHits = searchResponse.getHits().getHits();
		List<String> sampleTypesList = null;
		SearchScrollRequest scrollRequest = null;
		while (searchHits != null && searchHits.length > 0) {
			for (SearchHit hit : searchHits) {
				sampleTypesList = Arrays.asList(StringUtils.strip(hit.getSourceAsMap().get("sampleTypesJson").toString(),"[]").split(","));
				sampleTypesSet.addAll(sampleTypesList.stream()
						.filter(value->!value.isBlank())
						.map(value->value.trim())
						.collect(Collectors.toSet()));
			}
			scrollRequest = new SearchScrollRequest(scrollId);
			scrollRequest.scroll("3m");
			searchResponse = Util.util.restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
		    scrollId = searchResponse.getScrollId();
		    searchHits = searchResponse.getHits().getHits();
		}
		return sampleTypesSet;
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/getGeoInfo")
	public String getGeoInfo(@NotBlank String accession) throws Exception {
		var responseJson = new JSONObject();
		var getRequest = new GetRequest(Util.SEARCH_GEO_INDEX, accession.trim());
		var includes = new String[] { "summary", "lastUpdateDate", "accession", "title", "sampleCount", "pubmedIDs",
				"Summary","sampleTypesJson", "overallDesign", "platformIDs", "platformTypes", "gseRelationList"
				,"geoFileList.fileName","geoFileList.fileSize","geoFileList.fullName","geoFileList.location" };
		getRequest.fetchSourceContext(new FetchSourceContext(true, includes, null));
		var searchResponse = Util.util.restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
		var sourceAsMap = searchResponse.getSourceAsMap();
		if (sourceAsMap == null) {
			return responseJson.toString();
		}
		if(sourceAsMap.containsKey("pubmedIDs")) {
			var pubmedIDs = JSONArray.fromObject(sourceAsMap.get("pubmedIDs"));
			if (pubmedIDs.size() > 0) {
				var articleList = Util.util.articleMapper.list_ArticleTitlePubmedIds("'" + String.join(",", pubmedIDs) + "'");
				articleList.forEach(article->{
					article.setTitle(Util.unescapeStr(article.getTitle()));
				});
				sourceAsMap.put("articleList", articleList);
			}
		}
		if(sourceAsMap.containsKey("gseRelationList")) {
			var gseRelationList2 = new JSONArray();
			var gseRelationList = JSONArray.fromObject(sourceAsMap.get("gseRelationList"));
			for (Object gseRelation : gseRelationList) {
				var gseRelationJson=JSONObject.fromObject(gseRelation);
				if(gseRelationJson.containsKey("relationsTarget")&&("SuperSeries of".equals(gseRelationJson.get("relationsType"))||"SubSeries of".equals(gseRelationJson.get("relationsType")))) {
					gseRelationJson.put("title", Util.util.geoMapper.get_gseInfomationTitle(gseRelationJson.getString("relationsTarget")));
				}
				gseRelationList2.add(gseRelationJson);
			}
			sourceAsMap.put("gseRelationList", gseRelationList2);
		}
		responseJson.put("geoDetails", sourceAsMap);
		return Util.compress(responseJson.toString());
	}

	/**
	 * 相似数据集列表
	 * @param accession
	 * @return
	 * @throws Exception
	 */
	@GetMapping({ "/moreLikeThisPubmedGeo" })
	public String moreLikeThisPubmedGeo(@NotBlank String accession) throws Exception {
		JSONObject responseJson = new JSONObject();
		SearchRequest searchRequest = new SearchRequest(new String[] { "geo" });
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		String[] includes = { "accession", "title", "sampleCount" };
		searchSourceBuilder.fetchSource(includes, null);
		searchSourceBuilder.trackTotalHitsUpTo(10);
		MoreLikeThisQueryBuilder.Item[] items = { new MoreLikeThisQueryBuilder.Item("geo", accession) };
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().minimumShouldMatch(1);
		boolQueryBuilder.should((QueryBuilder) (new MoreLikeThisQueryBuilder(new String[] { "titile", "summary" }, null, items))
						.maxQueryTerms(50).minTermFreq(1));
		boolQueryBuilder.should((QueryBuilder) (new MoreLikeThisQueryBuilder(new String[] { "titile", "summary" }, null, items))
						.maxQueryTerms(50).minTermFreq(2));
		searchSourceBuilder.query((QueryBuilder) boolQueryBuilder);
		searchRequest.source(searchSourceBuilder);
		SearchResponse searchResponse = Util.util.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
		responseJson.put("geoList", ElasticSearchUtil.getSearchElasticsearchData(searchResponse, new String[0]));
		return responseJson.toString();
	}
	
	@GetMapping("/getGeoDataSampleAuthInfo")
	public void getGeoDataSampleAuthInfo(@NotBlank String accession,HttpServletRequest request) throws Exception {
		var userPhone = request.getHeader("userPhone");
		if (!Util.isPhone(userPhone)) {
			return;
		}
		accession = accession.trim();
		var userFile = new File(Util.USER_DATA + userPhone + "/" + accession);
		if (!userFile.exists()) {
			userFile.mkdirs();
		}
		var fileName = accession + "_sample";
		var successFilePath = "/" + accession + "/" + fileName + ".txt";
		var errorFilePath = successFilePath + ".log";
		var cmd = "Rscript " + Util.R_SCRIPT + "getGEOData.R -i " + accession + " -f sample -o " + (userFile + "/" + fileName + ".txt") + " -e "+ fileName + "_error.log";
		var cmdMd5 = Util.strToMd5(userPhone + ":" + cmd);
		var amqpRabbitListener=new AmqpRabbitListener(cmdMd5,cmd,"下载"+accession+"样本数据",userPhone,"rabbitmqGeoDataSample",userFile.getParent(),successFilePath,errorFilePath);
		System.out.println(amqpRabbitListener.toString());
		Util.sendRabbitmqTask(amqpRabbitListener);
	}
	
	@GetMapping("/getGeoDataExpInfo")
	public String getGeoDataExpInfo(@NotBlank String accession,@NotBlank String gpl) throws Exception {
		var responseJson = new JSONObject();
		responseJson.put("platformInfomation", Util.util.geoMapper.get_PlatformInfomation(accession.trim(), gpl.trim()));
		responseJson.put("gsmInfomation", Util.util.geoMapper.get_GsmInfomation(accession.trim(), gpl.trim()));
		return responseJson.toString();
	}
	
	@GetMapping("/getGeoDataExpAuthInfo")
	public void getGeoDataExpAuthInfo(@NotBlank String accession,@NotBlank String gpl,@NotBlank String a,@NotBlank String n,@NotBlank String m,HttpServletRequest request) throws Exception {
		var userPhone = request.getHeader("userPhone");
		if (!Util.isPhone(userPhone)) {
			return;
		}
		accession = accession.trim();
		var userFile = new File(Util.USER_DATA + userPhone + "/" + accession);
		if (!userFile.exists()) {
			userFile.mkdirs();
		}
		var fileName = accession + "_" + gpl.trim() + "_sample_exp";
		var successFilePath = "/" + accession + "/" + fileName + ".txt";
		var errorFilePath = successFilePath + ".log";
		var cmd = "Rscript " + Util.R_SCRIPT + "getGEOData.R -i " + accession + " -f exp -p "+gpl.trim()+" -a "+a.trim()+" -n "+n.trim()+" -m "+m.trim()+" -o " + (userFile + "/" + fileName + ".txt") + " -e "+ fileName + "_error.log";
		var cmdMd5 = Util.strToMd5(userPhone + ":" + cmd);
		var amqpRabbitListener=new AmqpRabbitListener(cmdMd5,cmd,"下载"+accession+"表达谱数据",userPhone,"rabbitmqGeoDataExp",userFile.getParent(),successFilePath,errorFilePath);
		Util.sendRabbitmqTask(amqpRabbitListener);
	}
	
	@GetMapping("/getGeoDataRawexpInfo")
	public String getGeoDataRawexpInfo(@NotBlank String accession,@NotBlank String gpl) throws Exception {
		var responseJson = new JSONObject();
		var geoFileList = Util.util.geoMapper.get_geoFileListIsLocal(accession.trim());
		if (geoFileList.size() == 0) {
			return "no file";
		}
		var fullNameSet=geoFileList.stream().filter(geoFile->Pattern.compile(".*?RAW.*?").matcher(geoFile.getFileName()).find()).map(geoFile->geoFile.getFullName()).collect(Collectors.toSet());
		if (fullNameSet.size() == 0) {
			return "no file";
		}
		var isCel = false;
		for (String fullName : fullNameSet) {
			if (ZipUtil.zipFileIsCEL(Util.GEO_FILE_PREFIX+fullName.substring(1))) {
				isCel = true;
				break;
			}
		}
		if (isCel) {
			responseJson.put("platformInfomation", Util.util.geoMapper.get_PlatformInfomation(accession.trim(), gpl.trim()));
			responseJson.put("gsmInfomation", Util.util.geoMapper.get_GsmInfomation(accession.trim(), gpl.trim()));
		} else {
			return "no file";
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getGeoDataRawexpAuthInfo")
	public void getGeoDataRawexpAuthInfo(@NotBlank String accession,@NotBlank String gpl,@NotBlank String m,HttpServletRequest request) throws Exception {
		var userPhone = request.getHeader("userPhone");
		if (!Util.isPhone(userPhone)) {
			return;
		}
		accession = accession.trim();
		var userFile = new File(Util.USER_DATA + userPhone + "/" + accession);
		if (!userFile.exists()) {
			userFile.mkdirs();
		}
		var fileName = accession + "_" + gpl.trim() + "_sample_rawexp";
		var successFilePath = "/" + accession + "/" + fileName + ".txt";
		var errorFilePath = successFilePath + ".log";
		var cmd = "Rscript " + Util.R_SCRIPT + "getGEOData.R -i " + accession + " -f rawexp -p "+gpl.trim()+" -m "+m.trim()+" -o " + (userFile + "/" + fileName + ".txt") + " -e "+ fileName + "_error.log";
		var cmdMd5 = Util.strToMd5(userPhone + ":" + cmd);
		var amqpRabbitListener=new AmqpRabbitListener(cmdMd5,cmd,"下载"+accession+"表达谱原始数据",userPhone,"rabbitmqGeoDataRawexp",userFile.getParent(),successFilePath,errorFilePath);
		Util.sendRabbitmqTask(amqpRabbitListener);
	}
	
	@GetMapping("/getGeoSampleCompressInfo")
	public String getGeoSampleCompressInfo(@NotBlank String accession) throws Exception {
		return Util.compress(getGeoSampleInfo(accession));
	}
	
	@GetMapping("/getGeoSampleInfo")
	public String getGeoSampleInfo(@NotBlank String accession) throws Exception {
		System.out.println("enter getGeoSampleInfo accession="+accession);
		var responseJson = new JSONObject();
		var gseInfomationList = Util.util.geoMapper.list_GetSampleInfos(accession.trim());
		responseJson.put("gsmInfomationList", gseInfomationList);
		return responseJson.toString();
	}

	@GetMapping("/getGeoSampleInfo2")
	public String getGeoSampleInfo2(@NotBlank String accession) throws Exception {
		System.out.println("enter getGeoSampleInfo accession="+accession);
		var responseJson = new JSONObject();
		var gseInfomationList = Util.util.geoMapper.list_GetSampleInfos(accession.trim());
		responseJson.put("gsmInfomationList", gseInfomationList);
		return responseJson.toString();
	}

	@GetMapping("/downloadFile")
	public void downloadFile(@NotBlank String filePath, HttpServletResponse response) {
		try {
			var file = new File(Util.GEO_FILE_PREFIX + filePath);
			if (file.exists()) {
				byte[] fileByte = Files.readAllBytes(file.toPath());
				Util.responseByte(fileByte, file.getName(), response);
			}
		} catch (Exception e) {
		}
	}
	
}
