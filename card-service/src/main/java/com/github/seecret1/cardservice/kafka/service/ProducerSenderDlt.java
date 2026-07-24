package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.order.message.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerSenderDlt {

    @Value("${app.kafka.dlt-topic}")
    private String dtlTopic;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderMessage> kafkaTemplate;

    public void sendMessageInDlt(OrderMessage message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(
                        dtlTopic,
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
}
