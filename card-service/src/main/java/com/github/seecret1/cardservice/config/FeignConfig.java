package com.github.seecret1.cardservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${user-service.client.x-internal-key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor userServiceRequestInterceptor(
            UserServiceClientProperties properties
    ) {
        return requestTemplate -> {
            requestTemplate.header(internalApiKey, properties.getApiKey());
        };
    }
}
