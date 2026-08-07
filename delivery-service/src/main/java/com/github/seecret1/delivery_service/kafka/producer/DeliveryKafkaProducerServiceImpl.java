package com.github.seecret1.delivery_service.kafka.producer;

import com.github.seecret1.delivery_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryKafkaProducerServiceImpl implements DeliveryKafkaProducerService {

    @Value("${app.retry.timeout}")
    private int retryTimeout;

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendNoWait(BaseMessage message) {
        log.info("Message: {}, sent to topic: {} user no wait method",
                message.getTraceId(), kafkaProperties.getOrdersTopic());
        kafkaTemplate.send(kafkaProperties.getOrdersTopic(), message.getTraceId(), message);
    }

    @Override
    public void sendWithWait(BaseMessage message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(
                        kafkaProperties.getOrdersTopic(),
                        message.getTraceId(),
                        message
                ).get();

                return null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to send message to Kafka", e);
                throw new IllegalStateException("Interrupted while waiting for Kafka send", e);
            } catch (ExecutionException e) {
                log.error("Failed to send message to Kafka", e);
                throw new IllegalStateException("Failed to send message to Kafka", e);
            }
        });
    }

    @Override
    public void sendToDlt(OrderDeliveryDto event, Throwable error) {
        ProducerRecord<String, Object> record = getRecord(kafkaProperties.getDltTopic(), event);
        record.headers()
                .add("error-message", getErrorMessage(error))
                .add("error-type", error.getClass().getSimpleName().getBytes(StandardCharsets.UTF_8));

        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(record).get(retryTimeout, TimeUnit.SECONDS);
                log.debug("[traceId={}][topic={}]Message sent to DLT", event.getTraceId(), record.topic());
                return null;

            } catch (Exception ex) {
                log.error("[traceId={}][topic={}]Failed send to DLT: {}", event.getTraceId(), record.topic(), ex.getMessage());
                throw new RuntimeException("Failed to send to retry", ex);
            }
        });
    }

    @Override
    public void sendToRetry(OrderDeliveryDto event, Throwable error, int attempt) {
        String retryTopic = kafkaProperties.getRetryTopic();
        ProducerRecord<String, Object> record = getRecord(retryTopic, event);
        record.headers()
                .add("error-message", getErrorMessage(error))
                .add("error-type", error.getClass().getSimpleName().getBytes(StandardCharsets.UTF_8))
                .add("retry-count", String.valueOf(attempt).getBytes(StandardCharsets.UTF_8));

        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(record).get(retryTimeout, TimeUnit.SECONDS);
                log.debug("[traceId={}][attempt={}]Message sent to retry", event.getTraceId(), attempt);
                return null;

            } catch (Exception ex) {
                log.warn("[traceId={}][attempt={}]Failed to send to retry: {}", event.getTraceId(), attempt, ex.getMessage());
                throw new RuntimeException("Failed to send to retry", ex);
            }
        });
    }

    private ProducerRecord<String, Object> getRecord(String topic, OrderDeliveryDto event) {
        return new ProducerRecord<>(topic, event.getTraceId(), event);
    }

    private byte[] getErrorMessage(Throwable error) {
        String message = error.getMessage() != null ? error.getMessage() : "unknown";
        return message.getBytes(StandardCharsets.UTF_8);
    }
}
