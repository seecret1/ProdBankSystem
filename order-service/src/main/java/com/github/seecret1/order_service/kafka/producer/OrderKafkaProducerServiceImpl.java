package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.order_service.dto.BaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaProducerServiceImpl implements OrderKafkaProducerService {

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    @Override
    public void sendNoWait(BaseMessage message) {
        kafkaTemplate.send(kafkaProperties.getCardsTopic(), message.getTraceId(), message);
    }

    @Override
    public void sendWithWait(BaseMessage message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(
                        kafkaProperties.getCardsTopic(),
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
