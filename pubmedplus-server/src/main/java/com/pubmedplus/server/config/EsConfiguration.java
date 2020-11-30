package com.pubmedplus.server.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfiguration {

	@Value("${elasticsearch.host}")
	private String host; 
	
	@Value("${elasticsearch.port}")
	private int port;
	
	@Value("${elasticsearch.username}")
	private String username;
	
	@Value("${elasticsearch.password}")
	private String password;
	
	@Value("${elasticsearch.schema}")
	private String schema; 
	
	@Value("${elasticsearch.connectTimeOut}")
	private int connectTimeOut;
	
	@Value("${elasticsearch.socketTimeOut}")
	private int socketTimeOut;
	
	@Value("${elasticsearch.connectionRequestTimeOut}")
	private int connectionRequestTimeOut;
	
	@Value("${elasticsearch.maxConnectNum}")
	private int maxConnectNum; 
	
	@Value("${elasticsearch.maxConnectPerRoute}")
	private int maxConnectPerRoute; 
	
	@Bean
	public RestHighLevelClient client() {
		
		RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, schema));
		
		// 异步httpclient连接延时配置
		builder.setRequestConfigCallback(new RequestConfigCallback() {
			@Override
			public Builder customizeRequestConfig(Builder requestConfigBuilder) {
				requestConfigBuilder.setConnectTimeout(connectTimeOut);
				requestConfigBuilder.setSocketTimeout(socketTimeOut);
				requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
				return requestConfigBuilder;
			}
		});
		
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(username, password));
		
		// 异步httpclient连接数配置
		builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
			@Override
			public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
				httpClientBuilder.setMaxConnTotal(maxConnectNum);
				httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
				httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				return httpClientBuilder;
			}
		});
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}
	
	
}
