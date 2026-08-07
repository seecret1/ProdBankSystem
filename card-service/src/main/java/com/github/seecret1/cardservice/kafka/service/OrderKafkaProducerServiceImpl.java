package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.config.kafka.properties.KafkaProperties;
import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.dto.order.message.OrderCardDto;
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
public class OrderKafkaProducerServiceImpl implements OrderKafkaProducerService {

    @Value("${app.retry.timout}")
    private int retryTimeout;

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendNoWait(OrderCardDto message) {
        loggingMessage(message);
        kafkaTemplate.send(kafkaProperties.getOrdersTopic(), message.getTraceId(), message);
    }

    @Override
    public void sendWithWait(OrderCardDto message) {

        loggingMessage(message);

        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(
                        kafkaProperties.getOrdersTopic(),
                        message.getTraceId(),
                        message
                ).get();

                return null;

            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to send message to Kafka", e);
                throw new RuntimeException("Kafka send failed", e);
            }
        });
    }

    @Override
    public void sendToRetry(BaseMessage message, Throwable error, int attempt) {
        String retryTopic = kafkaProperties.getRetryTopic();
        ProducerRecord<String, Object> record = getRecord(retryTopic, message);
        record.headers()
                .add("error-message", getErrorMessage(error))
                .add("error-type", error.getClass().getSimpleName().getBytes(StandardCharsets.UTF_8))
                .add("retry-count", String.valueOf(attempt).getBytes(StandardCharsets.UTF_8));

        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(record).get(retryTimeout, TimeUnit.SECONDS);
                log.debug("[traceId={}][attempt={}]Message sent to retry", message.getTraceId(), attempt);
                return null;

            } catch (Exception e) {
                log.error("Failed to send message to Kafka", e);
                throw new RuntimeException("Failed to send to retry", e);
            }
        });
    }

    private ProducerRecord<String, Object> getRecord(String topic, BaseMessage message) {
        return new ProducerRecord<>(topic, message.getTraceId(), message);
    }

    private byte[] getErrorMessage(Throwable error) {
        String message = error.getMessage() != null ? error.getMessage() : "unknown";
        return message.getBytes(StandardCharsets.UTF_8);
    }

    private static void loggingMessage(OrderCardDto message) {
        log.debug("Order message: traceId={}, userId={}, cardId={}, cardType={}, orderType={}, spendingLimit={}, comment={}, createdAt={}",
                message.getTraceId(), message.getUserId(), message.getCardId(), message.getCardType(), message.getOrderType(),
                message.getSpendingLimit(), message.getComment(), message.getCreatedAt());
    }
}
