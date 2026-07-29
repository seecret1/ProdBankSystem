package com.github.seecret1.order_service.config.feign.rest;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OfficeServiceClientProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient userServiceRestClient(OfficeServiceClientProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(properties.getXInternalKey(), properties.getApiKey())
                .build();
    }
}
