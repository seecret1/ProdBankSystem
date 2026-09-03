package com.github.seecret1.invoice_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {

    private String bootstrapServers;

    private String topic;

    private String invoiceTransactionTopic;

    private String requestTopic;

    private String retryTopic;

    private String translateTopic;

    private String transactionResponseTopic;

    private String responseOrdersTopic;

    private String ordersTopic;

    private String groupId;

    private String retryGroupId;

    private String deliveryTopic;

    private String dltTopic;

    private String dltGroupId;

    private Integer maxPullRecords;

    private Integer partitions;

    private Integer replicas;

    private Integer timeoutSeconds;
}