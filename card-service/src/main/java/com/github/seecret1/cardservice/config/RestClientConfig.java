package com.github.seecret1.cardservice.config;

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
                .defaultHeader("X-Internal-Api-Key", properties.getApiKey())
                .build();
    }
}
