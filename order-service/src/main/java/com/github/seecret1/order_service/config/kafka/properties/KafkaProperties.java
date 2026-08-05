package com.github.seecret1.order_service.config.kafka.properties;

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

    private String retryTopic;

    private String cardsTopic;

    private String groupId;

    private String dltTopic;

    private String dltGroupId;

    private Integer maxPullRecords;

    private Integer partitions;

    private Integer replicas;
}