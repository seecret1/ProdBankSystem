package com.github.seecret1.order_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka.headers")
public class KafkaHeaders {

    private String receivedTopic;
}
