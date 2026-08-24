package com.github.seecret1.invoice_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.kafka.ssl")
public class SslProperties {

    private Boolean enabled = false;

    private String trustStoreLocation;

    private String trustStorePassword;

    private String keyStoreLocation;

    private String keyStorePassword;

    private String keyPassword;

    private String protocol = "SSL";

    private String enabledProtocols = "TLSv1.2,TLSv1.3";
}