package com.github.seecret1.cardservice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor userServiceRequestInterceptor(
            UserServiceClientProperties properties
    ) {
        return requestTemplate -> {
            String apiKey = properties.getApiKey();
            String headerKey = properties.getHeaderKey();
            log.info("Adding header: {} = {}", headerKey, apiKey);
            requestTemplate.header(headerKey, apiKey);
        };
    }
}
