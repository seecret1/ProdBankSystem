package com.github.seecret1.payment_service.kafka.producer.impl;

import com.github.seecret1.payment_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.kafka.producer.PaymentMessageKafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMessageKafkaProducerServiceImpl implements PaymentMessageKafkaProducerService {

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, TransactionMessage> kafkaTemplate;

    @Override
    public void sendNoWait(TransactionMessage message) {
        logging(message);
        ProducerRecord<String, TransactionMessage> record = getRecord(kafkaProperties.getTransactionTopic(), message);
        kafkaTemplate.send(record);
    }

    @Override
    public void sendWithWait(TransactionMessage message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                logging(message);

                ProducerRecord<String, TransactionMessage> record =
                        getRecord(kafkaProperties.getTransactionTopic(), message);
                kafkaTemplate.send(record).get(kafkaProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
                return null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to send message to Kafka", e);
                throw new IllegalStateException("Interrupted while waiting for Kafka send", e);
            } catch (ExecutionException | TimeoutException ex) {
                log.error("Failed to send request to Kafka", ex);
                throw new RuntimeException("Failed to send request to Kafka", ex);
            }
        });
    }

    private ProducerRecord<String, TransactionMessage> getRecord(String topic, TransactionMessage message) {
        var record = new ProducerRecord<>(topic, message.getTraceId(), message);
        Map<String, String> headers = Map.of(
                "outgoing_topic", kafkaProperties.getTopic(),
                "source-service", "order-service",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
        headers.forEach((key, value) ->
                record.headers().add(key, value.getBytes(StandardCharsets.UTF_8))
        );
        return record;
    }

    private static void logging(TransactionMessage message) {
        log.info("Response sent to Kafka: traceId={}, data={}, timestamp={}",
                message.getTraceId(), message.getData(), message.getTimestamp());
    }
}
