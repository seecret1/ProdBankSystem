package com.github.seecret1.office_service;

import com.github.seecret1.office_service.feign.UserServiceFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
		"com.github.seecret1.office_service",
		"com.github.seecret1.common",
		"com.github.seecret1.jwt_common.security"
})
@SpringBootApplication
@EnableConfigurationProperties
@EnableFeignClients(basePackageClasses = {UserServiceFeignClient.class})
public class OfficeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfficeServiceApplication.class, args);
	}

}
