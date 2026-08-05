package com.github.seecret1.cardservice.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.cardservice.config.kafka.properties.KafkaProperties;
import com.github.seecret1.cardservice.config.kafka.properties.RetryProperties;
import com.github.seecret1.cardservice.dto.order.message.OrderCardDto;
import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    private final RetryProperties retryProperties;

    private final KafkaSslConfig kafkaSslConfig;

    private final ObjectMapper objectMapper;

    @Bean
    public NewTopic cardsTopic() {
        return TopicBuilder.name(kafkaProperties.getCardsTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @Bean
    public NewTopic cardsDltTopic() {
        return TopicBuilder.name(kafkaProperties.getDltTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @Bean
    public ProducerFactory<String, BaseMessage> orderProducerFactory() {
        return new DefaultKafkaProducerFactory<>(
                getBaseProducerConfig(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public ProducerFactory<String, OrderCardDto> orderCreateProducerFactory() {
        return new DefaultKafkaProducerFactory<>(
                getBaseProducerConfig(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public RetryTemplate kafkaRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryProperties.getMaxAttempts());

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProperties.getDelay());
        backOffPolicy.setMultiplier(retryProperties.getMultiplier());
        backOffPolicy.setMaxInterval(retryProperties.getMaxInterval());

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    public KafkaTemplate<String, BaseMessage> orderCardKafkaTemplate(
            ProducerFactory<String, BaseMessage> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, OrderCardDto> orderCreateKafkaTemplate(
            ProducerFactory<String, OrderCardDto> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, BaseMessage> consumerFactory() {
        Map<String, Object> config = getBaseConsumerConfig(kafkaProperties.getGroupId());
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BaseMessage.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(objectMapper)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BaseMessage> responseCardKafkaListenerContainerFactory(
            ConsumerFactory<String, BaseMessage> consumerFactory,
            KafkaTemplate<String, BaseMessage> orderCreateKafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, BaseMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                orderCreateKafkaTemplate,
                (record, exception) -> new org.apache.kafka.common.TopicPartition(
                        kafkaProperties.getDltTopic(),
                        record.partition()
                )
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(retryProperties.getDelay(), retryProperties.getMaxAttempts())
        );
        errorHandler.setRetryListeners();

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    private Map<String, Object> getBaseProducerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        config.putAll(kafkaSslConfig.getSslConfigs());

        return config;
    }

    private Map<String, Object> getBaseConsumerConfig(String groupId) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getMaxPullRecords());

        config.putAll(kafkaSslConfig.getSslConfigs());

        return config;
    }
}