package com.github.seecret1.cardservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(UserServiceClientProperties.class)
public class RestClientConfig {

    @Value("${user-service.client.x-internal-key}")
    private String internalApiKey;

    @Bean
    public RestClient userServiceRestClient(UserServiceClientProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(internalApiKey, properties.getApiKey())
                .build();
    }
}
