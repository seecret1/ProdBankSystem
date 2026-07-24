package com.github.seecret1.order_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class KafkaSecurityProperties {

    private String securityProtocol = "SSL";

    private String endpointIdentificationAlgorithm = "https";
}