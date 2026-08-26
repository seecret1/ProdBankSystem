package com.github.seecret1.order_service.config.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {

    private String bootstrapServers;

    private String topic;

    private String innerTopic;

    private String retryTopic;

    private String cardsTopic;

    private String requestInvoiceTopic;

    private String invoiceTopic;

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