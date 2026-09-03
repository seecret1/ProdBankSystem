package com.github.seecret1.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
		"com.github.seecret1.payment_service",
		"com.github.seecret1.common",
		"com.github.seecret1.jwt_common.security",
})
@SpringBootApplication
@EnableConfigurationProperties
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}
