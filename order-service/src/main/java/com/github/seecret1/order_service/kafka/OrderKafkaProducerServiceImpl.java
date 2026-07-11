package com.github.seecret1.order_service.kafka;

import com.github.seecret1.order_service.dto.OrderMessage;
import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaProducerServiceImpl implements OrderKafkaProducerService {

    @Value("${app.kafka.response-topic}")
    private String responseTopic;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderMessage<OrderCardResponse>> kafkaTemplate;

    @Override
    public void sendNoWait(OrderCardDto event, OrderMessage<OrderCardResponse> message) {
        kafkaTemplate.send(responseTopic, event.getTraceId(), message);
    }

    @Override
    public void sendWithWait(OrderCardDto event, OrderMessage<OrderCardResponse> message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(
                        responseTopic,
                        event.getTraceId(),
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
