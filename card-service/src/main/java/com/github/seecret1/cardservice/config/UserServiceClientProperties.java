package com.github.seecret1.cardservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "user-service.client")
public class UserServiceClientProperties {

    @Value("${user-service.client.base-url}")
    private String baseUrl;

    @Value("${user-service.client.api-key}")
    private String apiKey;

    @Value("${user-service.client.x-internal-key:X-Internal-Api-Key}")
    private String headerKey;
}
