package com.video.server.dao;

import com.video.server.pojo.*;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Mapper
public interface IVideoMapper {

    /**
     * 添加视频合集表
     *
     * @param videoTotal
     * @return
     */
    @Insert("INSERT IGNORE INTO `video_total`(`id`,`title`,`desc`,`money`,`type`,`image`,`user_phone`,`user_id`,`create_time`,`path`) VALUE(#{id},#{title},#{desc},#{money},#{type},#{image},#{userPhone},#{userId},now(),#{path})")
    int addVideoTotal(VideoTotal videoTotal);

    /**
     * 添加商品
     *
     * @return
     */
    @Insert("INSERT IGNORE INTO `tb_member_rank`(`amount`,`member_name`,`uuid`,`create_date`) VALUE(#{money},#{title},#{videoTotalUUID},now())")
    int addMemberRank(@Param("money") String money, @Param("title") String title, @Param("videoTotalUUID") String videoTotalUUID);

    /**
     * 添加视频
     *
     * @param ids
     * @param video
     * @return
     */
    @Insert("<script>INSERT IGNORE INTO `video`(`total_id`,`id`,`title`,`create_time`) VALUES"
            + "<foreach collection='list' item='item' index='index'  separator=','>"
            + "(#{item.totalId},#{item.id},#{item.title},now())"
            + "</foreach></script>")
    int addVideoList(@Param("list") ArrayList<Video> videoList);

    /**
     * 添加视频评论
     *
     * @param userId
     * @param userPhone2
     * @param userPhone2
     * @return
     */
    @Insert("INSERT IGNORE INTO `video_comment`(`id`,`total_id`,`content`,`user_phone`,`user_id`,`create_time`) VALUE(REPLACE(UUID(),'-',''),#{videoTotalId},#{content},#{userPhone},#{userId},now())")
    int addVideoComment(@Param("videoTotalId") String videoTotalId, @Param("content") String content, @Param("userPhone") String userPhone, @Param("userId") String userId);

    /**
     * 添加评论回复
     *
     * @param videoReply
     * @return
     */
    @Insert("INSERT IGNORE INTO `video_reply`(`id`,`comment_id`,`reply_id`,`reply_type`,`content`,`from_uid`,`to_uid`,`create_time`) VALUE(REPLACE(UUID(),'-',''),#{commentId},#{replyId},#{replyType},#{content},#{fromUid},#{toUid},now())")
    int addVideoReply(VideoReply videoReply);

    /**
     * 新增视频分类
     *
     * @param type
     */
    @Insert("INSERT IGNORE INTO `video_type`(`id`,`type`,`create_time`) VALUE(REPLACE(UUID(),'-',''),#{type},now())")
    void addVideoType(@Param("type") String type);

    /**
     * 视频id转合集表id
     *
     * @param id
     * @return
     */
    @Select("SELECT `video_total`.`id`,`video_total`.`type`,`video_total`.`title`,`video_total`.`user_id`,`video_total`.`user_phone`,`video_total`.`path` FROM `video_total`,`video` WHERE `video`.`total_id`=`video_total`.`id` AND `video`.`id`=#{id}")
    VideoTotal getTotalInfo(@Param("id") String id);


    @Select("SELECT `member_type` FROM `tb_user_member` WHERE `user_id`=(SELECT `id` FROM `tb_user_info` WHERE mobile=#{phone}) AND end_time > NOW()")
    Integer getMemberInfo(@Param("phone") String phone);


    @Select("SELECT `member_type` FROM `tb_user_member` WHERE `user_id`=#{id} AND end_time > NOW()")
    Integer getMemberType(@Param("id") String id);

    @Select("SELECT `video_total`.`path` FROM `video_total`,`video` WHERE `video`.`total_id`=`video_total`.`id` AND `video`.`id`=#{id}")
    String getPath(@Param("id") String id);

    @Select("SELECT `video_des` FROM `video` WHERE `id`=#{id}")
    String getVideoDes(@Param("id") String id);
    /**
     * 搜索视频
     *
     * @return
     */
    @Select("<script>SELECT `video`.`id`,`video_total`.`type`,`video_total`.`desc`,`video_total`.`title`,`video_total`.`money`,`video_total`.`image` FROM `video_total`,`video` WHERE `video_total`.`id`=`video`.`total_id`"
            + "<if test='money!=null'> AND ${money}</if>"
            + "<if test='type!=null'> AND `type` IN (${type})</if>"
            + "GROUP BY `video_total`.`id` ${sort}</script>")
    List<VideoTotal> searchVideo(@Param("money") String money, @Param("type") String type, @Param("sort") String sort);

    @Select("SELECT `tb_member_rank`.`discount` from tb_member_rank where uuid=(SELECT `video`.`total_id`  FROM video where id=#{id})")
    BigDecimal searchDis(@Param("id")String id);

    /**
     * 查询单个视频
     *
     * @param id
     * @return
     */
    @Select("SELECT `video`.`id`,`video_total`.`title`,`video_total`.`desc`,`video_total`.`money`,`video_total`.`type`,`video_total`.`image`,`video_total`.`create_time`,`tb_user_info`.`username` AS `userName` FROM `video_total`,`video`,`tb_user_info` WHERE `video`.`id`=#{id} AND `video`.`total_id`=`video_total`.`id` AND `video_total`.user_phone=`tb_user_info`.`mobile`")
    VideoTotal getVideo(@Param("id") String id);

    /**
     * 判断用户是否有观看收费视频的权限
     *
     * @return
     */
    @Select("SELECT COUNT(*) FROM `tb_order_pay` WHERE `goods_id`=#{videoTotalId} AND `mobile`=#{userPhone} AND `is_pay`=1")
    int isVideoViewPayAuto(@Param("videoTotalId") String videoTotalId, @Param("userPhone") String userPhone);

    /**
     * 获取视频分类
     *
     * @param videoTotalId
     * @return
     */
    @Select("SELECT `type` FROM `video_total` WHERE `id`=#{videoTotalId}")
    String getVideoType(@Param("videoTotalId") String videoTotalId);

    /**
     * 查询视频评论
     *
     * @param videoId
     * @return
     */
    @Select("SELECT * FROM `video_comment` WHERE `total_id`=#{videoTotalId} ORDER BY `create_time` DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userInfo", column = "user_id", many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
            @Result(property = "videoReplyList", column = "id", many = @Many(select = "com.video.server.dao.IVideoMapper.listVideoChildReply"))
    })
    List<VideoComment> listVideoComment(@Param("videoTotalId") String videoTotalId);

    /**
     * 获取评论的子回复
     *
     * @param commentId
     * @return
     */
    @Select("SELECT * FROM `video_reply` WHERE `comment_id`=#{id} ORDER BY `create_time` ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "fromUserInfo", column = "from_uid", many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
            @Result(property = "toUserInfo", column = "to_uid", many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
    })
    List<VideoReply> listVideoChildReply(@Param("id") String id);

//	/**
//	 * 获取评论的父回复
//	 * @param commentId
//	 * @return
//	 */
//	@Select("SELECT * FROM `video_reply` WHERE `comment_id`=#{commentId} AND `reply_type`='comment' ORDER BY `create_time` ASC")
//	@Results({
//		@Result(property = "id",column = "id"),
//		@Result(property = "fromUserInfo", column = "from_uid" ,many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
//		@Result(property = "toUserInfo", column = "to_uid" ,many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
//		@Result(property = "videoReplyList", column = "id" ,many = @Many(select = "com.video.server.dao.IVideoMapper.listVideoChildReply"))
//	})
//	List<VideoReply> listVideoParentReply(@Param("commentId")String commentId);

//	/**
//	 * 获取评论的子回复
//	 * @param commentId
//	 * @return
//	 */
//	@Select("SELECT * FROM `video_reply` WHERE `reply_id`=#{id} AND `reply_type`='reply' ORDER BY `create_time` ASC")
//	@Results({
//		@Result(property = "id",column = "id"),
//		@Result(property = "fromUserInfo", column = "from_uid" ,many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
//		@Result(property = "toUserInfo", column = "to_uid" ,many = @Many(select = "com.video.server.dao.IVideoMapper.getUserInfo")),
//		@Result(property = "videoReplyList", column = "id" ,many = @Many(select = "com.video.server.dao.IVideoMapper.listVideoChildReply"))
//	})
//	List<VideoReply> listVideoChildReply(@Param("id")String id);

    /**
     * 获取视频所有分类
     *
     * @return
     */
    @Select("SELECT `id`,`username`,`mobile` FROM `tb_user_info` WHERE `id`=#{id}")
    UserInfo getUserInfo(@Param("id") String id);

    /**
     * 获取相关视频
     *
     * @param id
     * @param type
     * @return
     */
    @Select("SELECT `video`.`id`,`video`.`title`,`video_total`.`image` FROM `video_total`,`video` WHERE `video_total`.`id`!=#{id} AND `video_total`.`type`=#{type} AND `video_total`.`id`=`video`.`total_id` ORDER BY `video`.`auto_id` ASC LIMIT 5")
    List<VideoTotal> listTypeVideo(@Param("id") String id, @Param("type") String type);

    /**
     * 获取视频合集有多少个视频
     *
     * @param id
     * @return
     */
    @Select("SELECT COUNT(*) FROM `video` WHERE `total_id`=#{videoTotalId}")
    int countVideo(@Param("videoTotalId") String videoTotalId);

    /**
     * 获取合集视频
     *
     * @param videoTotalId
     * @return
     */
    @Select("SELECT `video`.`id`,`video`.`title` FROM `video_total`,`video` WHERE `video_total`.id=`video`.total_id AND `video`.total_id=#{videoTotalId}")
    List<Video> listTotalVideo(@Param("videoTotalId") String videoTotalId);

    /**
     * 获取视频所有分类
     *
     * @return
     */
    @Select("SELECT `type` FROM `video_type`")
    List<String> listVideoType();


    @Update("update video set `video_des` =#{val} where `id`=#{id}")
    int addVideoDes(@Param("val")String val,@Param("id")String id);
}
