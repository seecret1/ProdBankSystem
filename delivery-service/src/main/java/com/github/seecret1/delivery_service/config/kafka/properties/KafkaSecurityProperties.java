package com.github.seecret1.delivery_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class KafkaSecurityProperties {

    private String securityProtocol = "PLAINTEXT";

    private String endpointIdentificationAlgorithm = "https";
}