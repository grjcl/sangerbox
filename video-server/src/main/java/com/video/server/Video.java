package com.video.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;



@EnableDiscoveryClient
@SpringBootApplication
@EnableTransactionManagement
public class Video{
	
	public static void main(String[] args) {
		SpringApplication.run(Video.class, args);
	}

}
