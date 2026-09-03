package com.github.seecret1.transaction_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
		"com.github.seecret1.transaction_service",
		"com.github.seecret1.common",
		"com.github.seecret1.jwt_common.security",
})
@SpringBootApplication
@EnableConfigurationProperties
public class TransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}

}
