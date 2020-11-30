package com.pubmedplus.server.dao.pubmed;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pubmedplus.server.pojo.JourcacheUserCollect;
import com.pubmedplus.server.pojo.JourcacheUserComment;

@Mapper
public interface IJournalMapper {
	
	/**
	 * 查询用户评论
	 * @param pmid
	 * @return
	 */
	@Select("SELECT * FROM `jourcache_user_comments` WHERE `nlmId`=#{nlmId}")
	List<JourcacheUserComment> list_JourcacheUserComments(@Param("nlmId") String nlmId);
	
	/**
	 * 查询用户收藏
	 * @param pmid
	 * @return
	 */
	@Select("SELECT * FROM `jourcache_user__collection` WHERE `nlmId`=#{nlmId}")
	List<JourcacheUserCollect> list_JourcacheUserCollection(@Param("nlmId") String nlmId);
	
}
