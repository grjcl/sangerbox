package com.pubmedplus.server.controller;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.ArticleUserCollect;
import com.pubmedplus.server.pojo.GeneUserCollect;
import com.pubmedplus.server.pojo.GeoUserCollect;
import com.pubmedplus.server.pojo.JourcacheUserCollect;
import com.pubmedplus.server.utils.JSONObjectUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class Collect {

	@PostMapping("/editArticleCollect")
	public String editArticleCollect(ArticleUserCollect articleUserCollect,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			articleUserCollect.setIsCollect(articleUserCollect.getIsCollect() == 0 ? 0 : 1);
			articleUserCollect.setUserPhone(Long.valueOf(userPhone));
			articleUserCollect.setUniquenNumber(articleUserCollect.getPmid() + ":" + articleUserCollect.getUserPhone());
			responseJson.put("count", Util.util.pubmedPlusMapper.edit_articleCollect(articleUserCollect));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/existsArticleCollect")
	public String existsArticleCollect(int pmid,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (pmid > 0 && Util.isPhone(userPhone)) {
			responseJson.put("count", Util.util.pubmedPlusMapper.exists_ArticleCollect(pmid + ":" + userPhone));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getArticleCollects")
	public String getArticleCollects(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			System.out.println(userPhone);
			responseJson.put("pmidList", Util.util.pubmedPlusMapper.list_ArticleCollect(userPhone));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getArticleUserCollects")
	public String getArticleUserCollects(HttpServletRequest request) throws Exception {
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			var articleList = Util.util.pubmedPlusMapper.list_ArticleUserCollect(userPhone);
			articleList.forEach(article->{
				article.setTitle(Util.unescapeStr(article.getTitle()));
				article.setJourcache(Util.getJourcacheNowIfs(article.getJourcache()));
			});
			responseJson.put("articleList", JSONArray.fromObject(articleList, JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}
	

	@PostMapping("/editJourcacheCollect")
	public String editJourcacheCollect(JourcacheUserCollect jourcacheUserCollect,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (jourcacheUserCollect.getNlmId() == null || !Util.isPhone(userPhone)) {
			return responseJson.toString();
		}
		jourcacheUserCollect.setIsCollect(jourcacheUserCollect.getIsCollect() == 0 ? 0 : 1);
		jourcacheUserCollect.setUserPhone(Long.valueOf(userPhone));
		jourcacheUserCollect.setUniquenNumber(jourcacheUserCollect.getNlmId() + ":" + jourcacheUserCollect.getUserPhone());
		responseJson.put("count", Util.util.pubmedPlusMapper.edit_JourcacheCollect(jourcacheUserCollect));
		return responseJson.toString();
	}
	
	@GetMapping("/existsJourcacheCollect")
	public String existsJourcacheCollect(String nlmId,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (nlmId != null && Util.isPhone(userPhone)) {
			responseJson.put("count", Util.util.pubmedPlusMapper.exists_JourcacheCollect(nlmId + ":" + userPhone));
		}
		return responseJson.toString();
	}

	@GetMapping("/getJourcacheCollects")
	public String getJourcacherCollects(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			responseJson.put("nlmIdList", Util.util.pubmedPlusMapper.list_JourcacheCollect(userPhone));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getJourcacheUserCollects")
	public String getJourcacheUserCollects(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			responseJson.put("jourcacheList", Util.util.pubmedPlusMapper.list_JourcacheUserCollect(userPhone));
		}
		return responseJson.toString();
	}
	
	@PostMapping("/editGeneCollect")
	public String editGeneCollect(GeneUserCollect geneUserCollect,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (geneUserCollect.getGeneId() == null || !Util.isPhone(userPhone)) {
			return responseJson.toString();
		}
		geneUserCollect.setIsCollect(geneUserCollect.getIsCollect() == 0 ? 0 : 1);
		geneUserCollect.setUserPhone(Long.valueOf(userPhone));
		geneUserCollect.setUniquenNumber(geneUserCollect.getGeneId() + ":" + geneUserCollect.getUserPhone());
		responseJson.put("count", Util.util.pubmedPlusMapper.edit_GenesCollect(geneUserCollect));
		return responseJson.toString();
	}

	@GetMapping("/existsGenesCollect")
	public String existsGenesCollect(String geneId,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (geneId != null && Util.isPhone(userPhone)) {
			responseJson.put("count", Util.util.pubmedPlusMapper.exists_GenesCollect(geneId + ":" + userPhone));
		}
		return responseJson.toString();
	}

	@GetMapping("/getGenesUserCollects")
	public String getGenesUserCollects(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			responseJson.put("geneList", Util.util.pubmedPlusMapper.list_GenesUserCollect(userPhone));
		}
		return responseJson.toString();
	}

	@PostMapping("/editGeoCollect")
	public String editGeoCollect(GeoUserCollect geoUserCollect,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (geoUserCollect.getAccession() == null || !Util.isPhone(userPhone)) {
			return responseJson.toString();
		}
		geoUserCollect.setIsCollect(geoUserCollect.getIsCollect() == 0 ? 0 : 1);
		geoUserCollect.setUserPhone(Long.valueOf(userPhone));
		geoUserCollect.setUniquenNumber(geoUserCollect.getAccession() + ":" + geoUserCollect.getUserPhone());
		responseJson.put("count", Util.util.pubmedPlusMapper.edit_GeoCollect(geoUserCollect));
		return responseJson.toString();
	}
	
	@GetMapping("/existsGeoCollect")
	public String existsGeoCollect(String accession,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (accession != null && Util.isPhone(userPhone)) {
			responseJson.put("count", Util.util.pubmedPlusMapper.exists_GeoCollect(accession + ":" + userPhone));
		}
		return responseJson.toString();
	}

	@GetMapping("/getGeoCollects")
	public String getGeoCollects(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			responseJson.put("geoList", Util.util.pubmedPlusMapper.list_GeoCollect(userPhone));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getGeoUserCollects")
	public String getGeoUserCollects(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			var geoList = Util.util.pubmedPlusMapper.list_GeoUserCollect(userPhone);
			var accessions = geoList.stream().map(geo -> geo.getAccession()).collect(Collectors.toSet());
			var geoMap = Util.util.sangerBoxMapper.get_gseInfomationTitles("'"+String.join("','", accessions)+"'");
			geoList.forEach(value->{
				var geo = geoMap.get(value.getAccession());
				value.setTitle(geo!=null?geo.getTitle():null);
			});
			responseJson.put("geoList", geoList);
		}
		return responseJson.toString();
	}

	
}
