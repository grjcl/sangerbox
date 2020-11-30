package com.pubmedplus.server.dao.pubmed;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.pubmedplus.server.pojo.AmqpRabbitListener;
import com.pubmedplus.server.pojo.ArticleUser;
import com.pubmedplus.server.pojo.ArticleUserCollect;
import com.pubmedplus.server.pojo.ArticleUserComment;
import com.pubmedplus.server.pojo.GeneUser;
import com.pubmedplus.server.pojo.GeneUserCollect;
import com.pubmedplus.server.pojo.GeoUser;
import com.pubmedplus.server.pojo.GeoUserCollect;
import com.pubmedplus.server.pojo.JcrIfs;
import com.pubmedplus.server.pojo.Jourcache;
import com.pubmedplus.server.pojo.JourcacheUser;
import com.pubmedplus.server.pojo.JourcacheUserCollect;
import com.pubmedplus.server.pojo.JourcacheUserComment;

@Mapper
public interface IPubmedPlusMapper {

	/**
	 * 添加,删除文章评论
	 * @param articleUserComments
	 * @return
	 */
	@Insert("REPLACE INTO `article_user_comments`(`uniquen_number`,`pmid`,`user_phone`,`label`,`title`,`content`,`is_read_all`,`is_comment`,`create_time`) VALUE(#{uniquenNumber},#{pmid},#{userPhone},#{label},#{title},#{content},#{isReadAll},#{isComment},now())")
	int edit_articleUserComment(ArticleUserComment articleUserComments);

	/**
	 * 查询文章评论
	 * @param uniquenNumber
	 * @return
	 */
	@Select("SELECT `label`,`title`,`content`,`is_read_all` FROM `article_user_comments` WHERE `uniquen_number`=#{uniquenNumber} AND `is_comment`=1 LIMIT 1")
	ArticleUserComment get_ArticleComment(@Param("uniquenNumber") String uniquenNumber);
	
	/**
	 * 查询文章所有评论
	 * @param pmid
	 * @return
	 */
	@Select("SELECT `label`,`title`,`content`,`is_read_all`,`create_time` FROM `article_user_comments` WHERE `pmid`=#{pmid} AND `is_comment`=1 AND `is_read_all`=0")
	List<ArticleUserComment> get_ArticleComments(@Param("pmid")int pmid);
	
	/**
	 * 查询用户所有文章评论
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `article`.`title` AS `articTitle`,`article`.`NlmId`,`article_user_comments`.`pmid`,`article_user_comments`.`label`,`article_user_comments`.`title`,`article_user_comments`.`content`,`article_user_comments`.`is_read_all`,`article_user_comments`.`create_time` FROM `article_user_comments`,`article` WHERE `article_user_comments`.`user_phone`=#{userPhone} AND `article_user_comments`.`is_comment`=1 AND `article_user_comments`.`pmid`=`article`.`PMID`")
	@Results({
		@Result(property = "jourcache", column = "NlmId" ,many = @Many(select = "com.pubmedplus.server.dao.pubmed.IPubmedPlusMapper.get_Journal")),
	})
	List<ArticleUserComment> list_ArticleUserComment(@Param("userPhone") String userPhone);
	
	/**
	 * 添加,删除杂志评论
	 * @param jourcacheUserComments
	 * @return
	 */
	@Insert("REPLACE INTO `jourcache_user_comments`(`uniquen_number`,`nlmId`,`user_phone`,`label`,`title`,`content`,`is_read_all`,`is_comment`,`create_time`) VALUE(#{uniquenNumber},#{nlmId},#{userPhone},#{label},#{title},#{content},#{isReadAll},#{isComment},now())")
	int edit_JourcacheUserComment(JourcacheUserComment jourcacheUserComments);
	
	/**
	 * 查询杂志评论
	 * @param uniquenNumber
	 * @return
	 */
	@Select("SELECT `label`,`title`,`content`,`is_read_all` FROM `jourcache_user_comments` WHERE `uniquen_number`=#{uniquenNumber} AND `is_comment`=1 LIMIT 1")
	JourcacheUserComment get_JourcacheComment(@Param("uniquenNumber") String uniquenNumber);
	
	/**
	 * 查询杂志所有评论
	 * @param pmid
	 * @return
	 */
	@Select("SELECT `label`,`title`,`content`,`is_read_all`,`create_time` FROM `jourcache_user_comments` WHERE `nlmId`=#{nlmId} AND `is_comment`=1 AND `is_read_all`=0")
	List<JourcacheUserComment> get_JourcacheComments(@Param("nlmId") String nlmId);
	
	/**
	 * 查询用户所有杂志评论
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `jourcache`.`jcr_id`,`jourcache`.`title` AS `JourcacheTitle`,`jourcache_user_comments`.`nlmId`,`jourcache_user_comments`.`label`,`jourcache_user_comments`.`title`,`jourcache_user_comments`.`content`,`jourcache_user_comments`.`is_read_all`,`jourcache_user_comments`.`create_time` FROM `jourcache_user_comments`,`jourcache` WHERE `jourcache_user_comments`.`user_phone`=#{userPhone} AND `jourcache_user_comments`.`is_comment`=1 AND `jourcache_user_comments`.`nlmId`=`jourcache`.`nlmId`")
	@Results({
		@Result(property = "course", column = "jcr_id" ,many = @Many(select = "com.pubmedplus.server.dao.pubmed.IPubmedPlusMapper.get_JourcacheCourse")),
	})
	List<JourcacheUserComment> list_JourcacheUserComment(@Param("userPhone") String userPhone);
	
	
	//=====================评论====================
	
	/**
	 * 收藏文章
	 * @param articleUserCollection
	 * @return
	 */
	@Insert("REPLACE INTO `article_user_collect`(`uniquen_number`,`pmid`,`user_phone`,`is_collect`,`create_time`) VALUE(#{uniquenNumber},#{pmid},#{userPhone},#{isCollect},now())")
	int edit_articleCollect(ArticleUserCollect articleUserCollection);
	
	/**
	 * 查询文章收藏
	 * @param uniquenNumber
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM `article_user_collect` WHERE `uniquen_number`=#{uniquenNumber} AND `is_collect`=1 LIMIT 1")
	int exists_ArticleCollect(@Param("uniquenNumber") String uniquenNumber);
	
	/**
	 * 批量查询文章收藏
	 * @param articleUserCollect
	 * @return
	 */
	@Select("SELECT `pmid` FROM `article_user_collect` WHERE `user_phone`=#{userPhone} AND `is_collect`=1")
	Set<Integer> list_ArticleCollect(@Param("userPhone") String userPhone);
	
	/**
	 * 查询用户所有文章收藏
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `article`.`PMID`,`article`.`title`,`article`.`NlmId`,`article_user_collect`.`create_time` FROM `article_user_collect`,`article` WHERE `article_user_collect`.`user_phone`=#{userPhone} AND `article_user_collect`.`is_collect`=1 AND `article_user_collect`.`pmid`=`article`.`PMID` ORDER BY `article_user_collect`.`create_time` DESC")
	@Results({
		@Result(property = "jourcache", column = "NlmId" ,many = @Many(select = "com.pubmedplus.server.dao.pubmed.IPubmedPlusMapper.get_Journal")),
	})
	List<ArticleUser> list_ArticleUserCollect(@Param("userPhone") String userPhone);
	
	
	/**
	 * 添加杂志收藏
	 * @param jourcacheUserCollection
	 * @return
	 */
	@Insert("REPLACE INTO `jourcache_user_collect`(`uniquen_number`,`nlmId`,`user_phone`,`is_collect`,`create_time`) VALUE(#{uniquenNumber},#{nlmId},#{userPhone},#{isCollect},now())")
	int edit_JourcacheCollect(JourcacheUserCollect jourcacheUserCollection);
	
	/**
	 * 查询杂志收藏
	 * @param uniquenNumber
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM `jourcache_user_collect` WHERE `uniquen_number`=#{uniquenNumber} AND `is_collect`=1 LIMIT 1")
	int exists_JourcacheCollect(@Param("uniquenNumber") String uniquenNumber);
	
	/**
	 * 批量查询杂志收藏
	 * @param jourcacheUserCollect
	 * @return
	 */
	@Select("SELECT `nlmId` FROM `jourcache_user_collect` WHERE `user_phone`=#{userPhone} AND `is_collect`=1")
	Set<String> list_JourcacheCollect(@Param("userPhone") String userPhone);
	
	/**
	 * 查询用户所有杂志收藏
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `jourcache`.`jcr_id`,`jourcache`.`NlmId`,`jourcache`.`title`,`jourcache_user_collect`.`create_time` FROM `jourcache_user_collect`,`jourcache` WHERE `jourcache_user_collect`.`user_phone`=#{userPhone} AND `jourcache_user_collect`.`is_collect`=1 AND `jourcache_user_collect`.`nlmId`=`jourcache`.`NlmId` ORDER BY `jourcache_user_collect`.`create_time` DESC")
	@Results({
		@Result(property = "course", column = "jcr_id" ,many = @Many(select = "com.pubmedplus.server.dao.pubmed.IPubmedPlusMapper.get_JourcacheCourse")),
	})
	List<JourcacheUser> list_JourcacheUserCollect(@Param("userPhone") String userPhone);
	
	/**
	 * 查询杂志大类
	 * @param issn
	 * @return
	 */
	@Select("SELECT `Journal`.`course` FROM `LetpubJournal`,`Journal` WHERE `LetpubJournal`.`jcr_journalId`=#{id} AND `LetpubJournal`.`journalId`=`Journal`.`id`")
	String get_JourcacheCourse(@Param("id")int id);
	
	
	/**
	 * 添加，删除基因收藏
	 * @param geneUserCollection
	 * @return
	 */
	@Insert("REPLACE INTO `genes_user_collect`(`uniquen_number`,`gene_id`,`user_phone`,`is_collect`,`create_time`) VALUE(#{uniquenNumber},#{geneId},#{userPhone},#{isCollect},now())")
	int edit_GenesCollect(GeneUserCollect geneUserCollection);
	
	/**
	 * 查询基因收藏
	 * @param uniquenNumber
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM `genes_user_collect` WHERE `uniquen_number`=#{uniquenNumber} AND `is_collect`=1 LIMIT 1")
	int exists_GenesCollect(@Param("uniquenNumber") String uniquenNumber);
	
	/**
	 * 查询用户所有基因收藏
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `gene_info`.`GeneID`,`gene_info`.`Name`,`genes_user_collect`.`create_time` FROM `genes_user_collect`,`gene_info` WHERE `genes_user_collect`.`user_phone`=#{userPhone} AND `genes_user_collect`.`is_collect`=1 AND `genes_user_collect`.`gene_id`=`gene_info`.`GeneID` ORDER BY `genes_user_collect`.`create_time` DESC")
	List<GeneUser> list_GenesUserCollect(@Param("userPhone") String userPhone);
	
	
	
	/**
	 * 添加，删除GEO收藏
	 * @param geoUserCollect
	 * @return
	 */
	@Insert("REPLACE INTO `geo_user_collect`(`uniquen_number`,`accession`,`user_phone`,`is_collect`,`create_time`) VALUE(#{uniquenNumber},#{accession},#{userPhone},#{isCollect},now())")
	int edit_GeoCollect(GeoUserCollect geoUserCollect);
	
	/**
	 * 查询Geo收藏
	 * @param uniquenNumber
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM `geo_user_collect` WHERE `uniquen_number`=#{uniquenNumber} AND `is_collect`=1 LIMIT 1")
	int exists_GeoCollect(@Param("uniquenNumber") String uniquenNumber);
	
	/**
	 * 批量查询GEO收藏
	 * @param geoUserCollect
	 * @return
	 */
	@Select("SELECT `accession` FROM `geo_user_collect` WHERE `user_phone`=#{userPhone} AND `is_collect`=1")
	Set<String> list_GeoCollect(@Param("userPhone") String userPhone);

	/**
	 * 查询用户所有GEO收藏
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `accession`,`create_time` FROM `geo_user_collect` WHERE `user_phone`=#{userPhone} AND `is_collect`=1 ORDER BY `create_time` DESC")
	List<GeoUser> list_GeoUserCollect(@Param("userPhone") String userPhone);

	/**
	 * 查询用户所有任务
	 * @param userPhone
	 * @return
	 */
	@Select("SELECT `id`,`desc`,`status`,`success_file_path`,`error_file_path`,`create_time`,`run_time`,`update_time`,`total_time`,`project_type`,`log` FROM `amqp_rabbit_listener` WHERE `user_phone`=#{userPhone} AND `is_del`=0 ORDER BY `create_time` DESC")
	List<AmqpRabbitListener> list_UserRabbitTack(@Param("userPhone") String userPhone);

	/**
	 * 删除任务
	 * @param id
	 * @param userPhone
	 * @return
	 */
	@Update("UPDATE `amqp_rabbit_listener` SET `is_del`=1 WHERE `id`=#{id} AND `user_phone`=#{userPhone}")
	int del_UserRabbitTack(@Param("id")int id,@Param("userPhone") String userPhone);


	/**
	 * 查询单个杂志
	 * @param PMID
	 * @return
	 */
	@Select("SELECT `jcr_id`,`isoAbbr` FROM `jourcache` WHERE `NlmId`=#{nlmId}")
	@Results(id="journalResults",value={
		@Result(property = "jcrIfs", column = "jcr_id" ,many = @Many(select = "com.pubmedplus.server.dao.pubmed.IPubmedPlusMapper.get_Ifs")),
	})
	Jourcache get_Journal(@Param("nlmId") String nlmId); 

	/**
	 * 根据杂志id查询ifs
	 * @param journalId
	 * @return
	 */
	@Select("SELECT * FROM `jcr_ifs` WHERE `jcrJournalId`=#{id}")
	JcrIfs get_Ifs(@Param("id")int id);





	
}
