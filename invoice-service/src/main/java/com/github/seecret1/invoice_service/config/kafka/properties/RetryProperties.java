package com.github.seecret1.invoice_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.retry")
public class RetryProperties {

    private Integer maxAttempts;

    private Long delay;

    private Integer multiplier;

    private Long maxInterval;
}
