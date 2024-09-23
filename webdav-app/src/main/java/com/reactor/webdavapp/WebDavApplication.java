package com.reactor.webdavapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties
@PropertySource(value = { "classpath:application.properties", "classpath:application.yaml" }, ignoreResourceNotFound = true)
public class WebDavApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebDavApplication.class, args);
	}

}
