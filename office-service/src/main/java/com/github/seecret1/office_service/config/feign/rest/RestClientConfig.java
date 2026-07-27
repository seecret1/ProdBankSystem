package com.github.seecret1.office_service.config.feign.rest;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(UserServiceClientProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient userServiceRestClient(UserServiceClientProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(properties.getXInternalKey(), properties.getApiKey())
                .build();
    }
}
