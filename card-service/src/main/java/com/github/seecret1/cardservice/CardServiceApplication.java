package com.github.seecret1.cardservice;

import com.github.seecret1.cardservice.client.UserServiceClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ComponentScan(basePackages = {
		"com.github.seecret1.cardservice",
		"com.github.seecret1.common",
		"com.github.seecret1.jwt_common.security",
})
@SpringBootApplication
@EnableConfigurationProperties
@EnableFeignClients(basePackageClasses = {UserServiceClient.class})
public class CardServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardServiceApplication.class, args);
	}

}
