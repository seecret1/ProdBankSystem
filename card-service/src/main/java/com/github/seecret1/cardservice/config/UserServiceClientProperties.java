package com.github.seecret1.cardservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "user-service.client")
public class UserServiceClientProperties {

    private String baseUrl = "http://localhost:8091";

    private String apiKey = "change-me-in-production";
}
