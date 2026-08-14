package com.github.seecret1.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.encryption")
public class EncryptionProperties {

    private String algorithm;
    private String transformation;

    /**
     * Ключ должен быть 16, 24 или 32 байта для AES-128/192/256
    */
    private String secretKey;
}
