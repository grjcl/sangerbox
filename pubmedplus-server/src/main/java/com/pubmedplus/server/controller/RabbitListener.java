package com.pubmedplus.server.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.stereotype.Component;

import com.pubmedplus.server.utils.Util;
import com.rabbitmq.client.Channel;

@Component
public class RabbitListener {
	
	@org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(value = @Queue(Util.RABBITMQ_TACK_WEB),exchange = @Exchange(Util.RABBITMQ_TACK_WEB)))
	public void createTackRabbitListener(String message){}
	
	private final ExecutorService executorIdConvert = Executors.newFixedThreadPool(Util.EXECUTOR_SERVICE_COUNT);
	
	@org.springframework.amqp.rabbit.annotation.RabbitListener(bindings = @QueueBinding(value = @Queue(Util.RABBITMQ_TACK),exchange = @Exchange(Util.RABBITMQ_TACK)))
	public void geoDataSampleRabbitListener(Message message, Channel channel){
		executorIdConvert.submit(() -> {
			Util.rabbitmqAckSuccess(message, channel);
			var id = new String(message.getBody());
			Util.getRabbitmqTask(id,message,null);
		});
	}
	
}
