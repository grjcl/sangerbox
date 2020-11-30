package com.pubmedplus.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.pojo.ArticleUserComment;
import com.pubmedplus.server.pojo.JourcacheUserComment;
import com.pubmedplus.server.utils.JSONObjectUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class Comments {
	
	@PostMapping("/editArticleUserComment")
	public String editArticleUserComment(ArticleUserComment articleUserComment,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (!Util.isPhone(userPhone)) {
			return responseJson.toString();
		}
		articleUserComment.setIsReadAll(articleUserComment.getIsReadAll() == 0 ? 0 : 1);
		articleUserComment.setIsComment(articleUserComment.getIsComment() == 0 ? 0 : 1);
		articleUserComment.setUserPhone(Long.valueOf(userPhone));
		articleUserComment.setUniquenNumber(articleUserComment.getPmid() + ":" + articleUserComment.getUserPhone());
		responseJson.put("count", Util.util.pubmedPlusMapper.edit_articleUserComment(articleUserComment));
		return responseJson.toString();
	}
	
	@GetMapping("/getArticleComment")
	public String getArticleComment(int pmid,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (pmid > 0 && Util.isPhone(userPhone)) {
			var articleUserComment = Util.util.pubmedPlusMapper.get_ArticleComment(pmid + ":" + userPhone);
			responseJson.put("articleComment", JSONObject.fromObject(articleUserComment, JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}

	@GetMapping("/getArticleComments")
	public String getArticleComments(int pmid) throws Exception{
		var responseJson = new JSONObject();
		var articleCommentList = Util.util.pubmedPlusMapper.get_ArticleComments(pmid);
		responseJson.put("articleCommentList", JSONArray.fromObject(articleCommentList, JSONObjectUtil.getJsonConfig()));
		return responseJson.toString();
	}

	@GetMapping("/getArticleUserComments")
	public String getArticleUserComments(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			var articleUserCommentList = Util.util.pubmedPlusMapper.list_ArticleUserComment(userPhone);
			articleUserCommentList.forEach(articleUserComment->{
				articleUserComment.setJourcache(Util.getJourcacheNowIfs(articleUserComment.getJourcache()));
			});
			responseJson.put("articleUserCommentList", JSONArray.fromObject(articleUserCommentList, JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}
	
	
	

	@PostMapping("/editJourcacheUserComment")
	public String editJourcacheUserComment(JourcacheUserComment jourcacheUserComment,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (jourcacheUserComment.getNlmId() == null || !Util.isPhone(userPhone)) {
			return responseJson.toString();
		}
		jourcacheUserComment.setIsReadAll(jourcacheUserComment.getIsReadAll() == 0 ? 0 : 1);
		jourcacheUserComment.setIsComment(jourcacheUserComment.getIsComment() == 0 ? 0 : 1);
		jourcacheUserComment.setUserPhone(Long.valueOf(userPhone));
		jourcacheUserComment.setUniquenNumber(jourcacheUserComment.getNlmId() + ":" + jourcacheUserComment.getUserPhone());
		responseJson.put("count", Util.util.pubmedPlusMapper.edit_JourcacheUserComment(jourcacheUserComment));
		return responseJson.toString();
	}
	
	@GetMapping("/getJourcacheComment")
	public String getJourcacheComment(String nlmId,HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (nlmId != null && Util.isPhone(userPhone)) {
			var jourcacheUserComment = Util.util.pubmedPlusMapper.get_JourcacheComment(nlmId + ":" + userPhone);
			responseJson.put("articleComment", JSONObject.fromObject(jourcacheUserComment, JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getJourcacheComments")
	public String getJourcacheComments(String nlmId) throws Exception{
		var responseJson = new JSONObject();
		if (nlmId != null) {
			var JourcacheCommentList = Util.util.pubmedPlusMapper.get_JourcacheComments(nlmId);
			responseJson.put("JourcacheCommentList", JSONArray.fromObject(JourcacheCommentList, JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/getJourcacheUserComments")
	public String getJourcacheUserComments(HttpServletRequest request) throws Exception{
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			var jourcacheUserCommentList = Util.util.pubmedPlusMapper.list_JourcacheUserComment(userPhone);
			responseJson.put("jourcacheUserCommentList", JSONArray.fromObject(jourcacheUserCommentList, JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}
	
}
