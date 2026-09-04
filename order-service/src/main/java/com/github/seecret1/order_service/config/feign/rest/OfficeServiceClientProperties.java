package com.github.seecret1.order_service.config.feign.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "office-service.client")
public class OfficeServiceClientProperties {

    private String baseUrl;

    private String apiKey;

    private String xInternalKey;
}
