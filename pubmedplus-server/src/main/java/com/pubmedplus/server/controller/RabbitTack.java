package com.pubmedplus.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pubmedplus.server.utils.JSONObjectUtil;
import com.pubmedplus.server.utils.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class RabbitTack {

	@GetMapping("/getUserRabbitTackInfo")
	public String getUserRabbitTackInfo(HttpServletRequest request) throws Exception {
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			var tackLIst = Util.util.pubmedPlusMapper.list_UserRabbitTack(userPhone);
			responseJson.put("tackList", JSONArray.fromObject(tackLIst,JSONObjectUtil.getJsonConfig()));
		}
		return responseJson.toString();
	}
	
	@GetMapping("/repeatUserRabbitTack")
	public void repeatUserRabbitTackInfo(int id,HttpServletRequest request) throws Exception {
		var userPhone = request.getHeader("userPhone");
		if (!Util.isPhone(userPhone)) {
			return;
		}
		System.out.println(id);
		if(Util.util.rabbitMapper.exist_amqpRabbitListeners(String.valueOf(id)) == 0) {
			Util.sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, userPhone, "服务器错误!");
			return;
		}
		if (Util.util.rabbitMapper.update_amqpRabbitListenerRepeat(id, userPhone) > 0) {
			var amqpRunCount = Util.util.rabbitMapper.count_amqpRabbitListenerRunCount(1);
			if (amqpRunCount < Util.EXECUTOR_SERVICE_COUNT) {
				amqpRunCount = 0;
			} else {
				amqpRunCount = Util.util.rabbitMapper.count_amqpRabbitListenerRunCount(0);
			}
			if(amqpRunCount>0) {
				Util.sendRabbitmqRoutingMessage(Util.RABBITMQ_TACK_WEB, userPhone, "已将任务发布到后台排队中,你的前面有" + amqpRunCount + "个任务,请耐心等待!");
			}
			Util.util.rabbitTemplate.convertAndSend(Util.RABBITMQ_TACK, String.valueOf(id));
		}
	}
	
	@GetMapping("/delUserRabbitTackInfo")
	public String delUserRabbitTackInfo(int id,HttpServletRequest request) throws Exception {
		var responseJson = new JSONObject();
		var userPhone = request.getHeader("userPhone");
		if (Util.isPhone(userPhone)) {
			responseJson.put("count", Util.util.pubmedPlusMapper.del_UserRabbitTack(id,userPhone));
		}
		return responseJson.toString();
	}
	
}
