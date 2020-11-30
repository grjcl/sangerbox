package com.pubmedplus.server.dao.pubmed;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.pubmedplus.server.pojo.AmqpRabbitListener;

@Mapper
public interface IRabbitMapper {

	/**
	 * 添加等待执行的cmd命令
	 * @param userFile 
	 * @return
	 */
	@Insert("INSERT INTO `amqp_rabbit_listener`(`cmd_md5`,`cmd`,`desc`,`user_phone`,`type`,`user_file`,`success_file_path`,`error_file_path`,`project_type`,`create_time`) VALUE(#{cmdMd5},#{cmd},#{desc},#{userPhone},#{type},#{userFile},#{successFilePath},#{errorFilePath},'pubmed',now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int add_amqpRabbitListener(AmqpRabbitListener amqpRabbitListener);

	/**
	 * 判断命令是否存在
	 * @param cmdMd5
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM `amqp_rabbit_listener` WHERE `cmd_md5`=#{cmdMd5} and `status`='0' LIMIT 1")
	int exist_amqpRabbitListener(@Param("cmdMd5") String cmdMd5);

	@Select("SELECT COUNT(*) FROM `amqp_rabbit_listener` WHERE `id`=#{id} LIMIT 1")
	int exist_amqpRabbitListeners(@Param("id") String id);
	/**
	 * 查询某个cmd命令
	 * @param cmdMd5
	 * @return
	 */
	@Select("SELECT * FROM `amqp_rabbit_listener` WHERE `id`=#{id}  LIMIT 1")
	AmqpRabbitListener get_amqpRabbitListener(@Param("id") String id);

	/**
	 * 修改cmd命令完成
	 * @param cmdMd5
	 * @param status
	 * @return
	 */
	@Update("UPDATE `amqp_rabbit_listener` SET `status`=#{status},`update_time`=now(),`total_time`=TIMESTAMPDIFF(second, `run_time`, `update_time`) WHERE `id`=#{id}")
	int update_amqpRabbitListenerEnd(@Param("id") String id, @Param("status") int status);

	/**
	 * 修改cmd命令运行状态
	 * @param cmdMd5
	 * @param status
	 * @return
	 */
	@Update("UPDATE `amqp_rabbit_listener` SET `status`=1,`run_time`=now() WHERE `id`=#{id}")
	int update_amqpRabbitListenerRun(@Param("id") String id);
	
	/**
	 * 增加cmd日志
	 * @param id
	 * @param cmdLog
	 */
	@Update("UPDATE `amqp_rabbit_listener` SET `log`=#{cmdLog} WHERE `id`=#{id}")
	void update_amqpRabbitListenerLog(@Param("id") String id,@Param("cmdLog") String cmdLog);
	
	/**
	 * 任务重新运行
	 * @param id
	 * @param userPhone
	 * @param i
	 * @return
	 */
	@Update("UPDATE `amqp_rabbit_listener` SET `status`=0,`run_time`=null,`update_time`=null,`total_time`=null WHERE `id`=#{id} AND `user_phone`=#{userPhone}")
	int update_amqpRabbitListenerRepeat(@Param("id")int id,@Param("userPhone") String userPhone);
	
	/**
	 * 获取消息列队对应状态的任务数量
	 * @param type
	 * @return
	 */
	@Select("SELECT COUNT(*) FROM `amqp_rabbit_listener` WHERE `status`=#{status} AND `project_type`='pubmed'")
	int count_amqpRabbitListenerRunCount(@Param("status") int status);

	/**
	 * 获取消息列队中等待执行和正在执行的任务
	 * @param type
	 * @return
	 */
	@Select("SELECT `id` FROM `amqp_rabbit_listener` WHERE (`status`=0 OR `status`=1) AND `project_type`='pubmed'")
	List<Integer> list_amqpRabbitListenerIncomplete();



	
	
}
