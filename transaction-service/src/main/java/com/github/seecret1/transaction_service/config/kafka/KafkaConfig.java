package com.github.seecret1.transaction_service.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.transaction_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.transaction_service.config.kafka.properties.RetryProperties;
import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
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
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
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
    public NewTopic responseTopic() {
        return TopicBuilder.name(kafkaProperties.getResponseTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @Bean
    public NewTopic requestTopic() {
        return TopicBuilder.name(kafkaProperties.getRequestTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @Bean
    public NewTopic retryTopic() {
        return TopicBuilder.name(kafkaProperties.getRetryTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @Bean
    public NewTopic orderDltTopic() {
        return TopicBuilder.name(kafkaProperties.getDltTopic())
                .partitions(kafkaProperties.getPartitions())
                .replicas(kafkaProperties.getReplicas())
                .build();
    }

    @Bean
    public ProducerFactory<String, TransactionMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(
                getBaseProducerConfig(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public ProducerFactory<String, Object> orderDltProducerFactory() {
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
    public KafkaTemplate<String, TransactionMessage> orderKafkaTemplate(
            ProducerFactory<String, TransactionMessage> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, Object> orderDltKafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, TransactionMessage> consumerBaseMessageFactory() {
        Map<String, Object> config = getBaseConsumerConfig(kafkaProperties.getGroupId());
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionMessage.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return getDefaultKafkaConsumerFactory(config, TransactionMessage.class);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerDltFactory() {
        Map<String, Object> config = getBaseConsumerConfig(kafkaProperties.getDltGroupId());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class.getName());

        return getDefaultKafkaConsumerFactory(config, Object.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TransactionMessage> kafkaListenerContainerFactory(
            ConsumerFactory<String, TransactionMessage> consumerFactory,
            KafkaTemplate<String, TransactionMessage> orderCreateKafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TransactionMessage> factory =
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

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dltKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerDltFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerDltFactory);
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
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getMaxPullRecords());

        config.putAll(kafkaSslConfig.getSslConfigs());

        return config;
    }

    private <T>DefaultKafkaConsumerFactory<String, T> getDefaultKafkaConsumerFactory(Map<String, Object> config, Class<T> clazz) {
        Map<String, Object> consumerConfig = new HashMap<>(config);
        consumerConfig.remove(JsonDeserializer.VALUE_DEFAULT_TYPE);
        consumerConfig.remove(JsonDeserializer.USE_TYPE_INFO_HEADERS);
        consumerConfig.remove(JsonDeserializer.TRUSTED_PACKAGES);

        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(clazz, objectMapper, false);
        jsonDeserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfig,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }
}