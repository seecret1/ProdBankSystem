package com.github.seecret1.order_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.order_service.dto.OrderMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
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
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topic}")
    private String topicName;

    @Value("${app.kafka.groupId}")
    private String groupId;

    @Value("${app.kafka.dlt-group-id}")
    private String dltGroupId;

    @Value("${app.kafka.dlt-topic}")
    private String dltTopicName;

    @Value("${app.kafka.partitions}")
    private Integer partitions;

    @Value("${app.kafka.replicas}")
    private Integer replicas;

    @Value("${app.retry.max-attempts}")
    private Integer maxAttempts;

    @Value("${app.kafka.max-pull-records}")
    private Integer maxPullRecords;

    @Value("${app.retry.delay}")
    private Long delayMs;

    @Value("${app.retry.multiplier}")
    private Integer multiplier;

    @Value("${app.retry.max-interval}")
    private Long maxInterval;

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(topicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public NewTopic orderDltTopic() {
        return TopicBuilder.name(dltTopicName)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }

    @Bean
    public ProducerFactory<String, OrderMessage> orderProducerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public ProducerFactory<String, OrderCardDto> orderCreateProducerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public ProducerFactory<String, Object> orderDtlProducerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
    }

    @Bean
    public RetryTemplate kafkaRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(delayMs);
        backOffPolicy.setMultiplier(multiplier);
        backOffPolicy.setMaxInterval(maxInterval);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    public KafkaTemplate<String, OrderMessage> orderCardKafkaTemplate(
            ProducerFactory<String, OrderMessage> producerFactory
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
    public KafkaTemplate<String, Object> orderDltKafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, OrderCardDto> consumerFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderCardDto.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPullRecords);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(objectMapper));
    }

    @Bean
    public ConsumerFactory<String, Object> consumerDtlFactory(
            ObjectMapper objectMapper
    ) {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, dltGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class.getName());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPullRecords);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(objectMapper));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCardDto> orderCardKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCardDto> consumerFactory,
            KafkaTemplate<String, OrderCardDto> orderCreateKafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCardDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                orderCreateKafkaTemplate,
                (record, exception) -> {
                    return new org.apache.kafka.common.TopicPartition(
                            dltTopicName,
                            record.partition()
                    );
                }
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(delayMs, maxAttempts)
        );
        errorHandler.setRetryListeners();

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> dltKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerDtlFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerDtlFactory);
        return factory;
    }
}
