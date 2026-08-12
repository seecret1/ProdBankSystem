package com.github.seecret1.delivery_service.kafka.producer;

import com.github.seecret1.delivery_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.delivery_service.dto.BaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryKafkaProducerServiceImpl implements DeliveryKafkaProducerService {

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    @Override
    public void sendNoWait(BaseMessage message) {
        log.info("Message: {}, sent to topic: {} user no wait method",
                message.getTraceId(), kafkaProperties.getOrdersTopic());
        ProducerRecord<String, BaseMessage> record = getRecord(kafkaProperties.getOrdersTopic(), message);
        kafkaTemplate.send(record);
    }

    @Override
    public void sendWithWait(BaseMessage message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                ProducerRecord<String, BaseMessage> record = getRecord(kafkaProperties.getOrdersTopic(), message);
                kafkaTemplate.send(record).get();
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

    private ProducerRecord<String, BaseMessage> getRecord(String topic, BaseMessage message) {
        return new ProducerRecord<>(topic, message.getTraceId(), message);
    }
}
