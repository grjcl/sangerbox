package com.pubmedplus.server.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan(basePackages = "com.pubmedplus.server.dao.sangerbox", sqlSessionTemplateRef = "sangerboxSqlSessionTemplate")
public class SangerboxDataSource {
	
	@Bean(name = "sangerboxData")
	@ConfigurationProperties(prefix = "spring.datasource.sangerbox")
	public DataSource sangerboxData() {
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "sangerboxSqlSessionFactory")
	public SqlSessionFactory sangerboxSqlSessionFactory(@Qualifier("sangerboxData") DataSource dataSource)throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		bean.setConfiguration(configuration);
		return bean.getObject();
	}

	@Bean(name = "sangerboxTransactionManager")
	public DataSourceTransactionManager sangerboxTransactionManager(@Qualifier("sangerboxData") DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean(name = "sangerboxSqlSessionTemplate")
	public SqlSessionTemplate sangerboxSqlSessionTemplate(@Qualifier("sangerboxSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
		return new SqlSessionTemplate(sqlSessionFactory);
	}
	
}
