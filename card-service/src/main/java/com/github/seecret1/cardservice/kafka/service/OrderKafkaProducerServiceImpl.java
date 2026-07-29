package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.dto.order.message.OrderCardDto;
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

    @Value("${app.kafka.topic}")
    private String ordersTopic;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderCardDto> kafkaTemplate;

    @Override
    public void sendNoWait(OrderCardDto message) {
        loggingMessage(message);
        kafkaTemplate.send(ordersTopic, message.getTraceId(), message);
    }

    @Override
    public void sendWithWait(OrderCardDto message) {

        loggingMessage(message);

        kafkaRetryTemplate.execute(context -> {
            try {
                kafkaTemplate.send(
                        ordersTopic,
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

    private static void loggingMessage(OrderCardDto message) {
        log.debug("Order message: traceId={}, userId={}, cardId={}, cardType={}, orderType={}, spendingLimit={}, comment={}, createdAt={}",
                message.getTraceId(), message.getUserId(), message.getCardId(), message.getCardType(), message.getOrderType(),
                message.getSpendingLimit(), message.getComment(), message.getCreatedAt());
    }
}
