package com.github.seecret1.order_service;

import com.github.seecret1.order_service.feign.OfficeServiceFeignClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {
		"com.github.seecret1.common",
		"com.github.seecret1.jwt_common.security",
		"com.github.seecret1.order_service"
})
@EnableFeignClients(basePackageClasses = {OfficeServiceFeignClient.class})
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
