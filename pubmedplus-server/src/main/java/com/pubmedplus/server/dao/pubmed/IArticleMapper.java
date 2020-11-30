package com.pubmedplus.server.dao.pubmed;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pubmedplus.server.pojo.ArticleUser;
import com.pubmedplus.server.pojo.ArticleUserCollect;
import com.pubmedplus.server.pojo.ArticleUserComment;

@Mapper
public interface IArticleMapper {

	/**
	 * 批量查询文章标题
	 * @param pmids
	 * @return
	 */
	@Select("SELECT `PMID`,`title` FROM `article` WHERE `PMID` IN (${pmid})")
	List<ArticleUser> list_ArticleTitlePubmedIds(@Param("pmid") String pmids);
	
	/**
	 * 查询用户评论
	 * @param pmid
	 * @return
	 */
	@Select("SELECT * FROM `article_user_comments` WHERE `pmid`=#{pmid}")
	List<ArticleUserComment> list_ArticleUserComments(@Param("pmid") int pmid);
	
	/**
	 * 查询用户收藏
	 * @param pmid
	 * @return
	 */
	@Select("SELECT * FROM `article_user_collection` WHERE `pmid`=#{pmid}")
	List<ArticleUserCollect> list_ArticleUserCollection(@Param("pmid") int pmid);

	
}
