package com.github.seecret1.invoice_service.config.kafka;

import com.github.seecret1.invoice_service.config.kafka.properties.KafkaSecurityProperties;
import com.github.seecret1.invoice_service.config.kafka.properties.SslProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaSslConfig {

    private final ResourceLoader resourceLoader;

    private final SslProperties sslProperties;

    private final KafkaSecurityProperties securityProperties;

    public Map<String, Object> getSslConfigs() {
        Map<String, Object> config = new HashMap<>();

        if (sslProperties.getEnabled()) {
            log.info("Configuring Kafka SSL with protocol: {}", securityProperties.getSecurityProtocol());

            config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProperties.getSecurityProtocol());
            config.put(SslConfigs.SSL_PROTOCOL_CONFIG, sslProperties.getProtocol());

            // Handle truststore
            if (sslProperties.getTrustStoreLocation() != null && !sslProperties.getTrustStoreLocation().isEmpty()) {
                String resolvedPath = resolveResourcePath(sslProperties.getTrustStoreLocation());
                config.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, resolvedPath);
                config.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, sslProperties.getTrustStorePassword());
                log.debug("Truststore configured: {}", resolvedPath);
            }

            // Handle keystore
            if (sslProperties.getKeyStoreLocation() != null && !sslProperties.getKeyStoreLocation().isEmpty()) {
                String resolvedPath = resolveResourcePath(sslProperties.getKeyStoreLocation());
                config.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, resolvedPath);
                config.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, sslProperties.getKeyStorePassword());
                config.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, sslProperties.getKeyPassword());
                log.debug("Keystore configured: {}", resolvedPath);
            }

            if (securityProperties.getEndpointIdentificationAlgorithm() != null &&
                    !securityProperties.getEndpointIdentificationAlgorithm().isEmpty()) {
                config.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG,
                        securityProperties.getEndpointIdentificationAlgorithm());
            }

            // Additional SSL properties for enhanced security
            if (sslProperties.getEnabledProtocols() != null) {
                config.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, sslProperties.getEnabledProtocols());
            }

            log.info("SSL configuration completed successfully");
        } else {
            log.info("SSL is disabled for Kafka");
        }

        return config;
    }

    private String resolveResourcePath(String location) {
        if (location == null || location.isEmpty()) {
            return null;
        }

        try {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                // For classpath resources, copy to temp file for Kafka to read
                if (location.startsWith("classpath:")) {
                    Path tempFile = Files.createTempFile("kafka-ssl-", ".jks");
                    tempFile.toFile().deleteOnExit();
                    try (InputStream is = resource.getInputStream()) {
                        Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    log.debug("Copied classpath resource to temp file: {}", tempFile);
                    return tempFile.toAbsolutePath().toString();
                }
                return resource.getFile().getAbsolutePath();
            }
        } catch (IOException e) {
            log.error("Failed to resolve SSL resource: {}", location, e);
            throw new RuntimeException("Failed to resolve SSL resource: " + location, e);
        }

        // If not a resource, return as is (assuming it's a file path)
        return location;
    }
}