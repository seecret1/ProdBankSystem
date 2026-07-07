package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.order.message.OrderCreateCardDto;
import com.github.seecret1.cardservice.order.message.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaProducerServiceImpl implements OrderKafkaProducerService {

    @Value("${app.kafka.topic}")
    private String ordersTopic;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderCreateCardDto> kafkaTemplate;

    @Override
    public void sendNoWait(Card card, String comment, String userId) {
        OrderCreateCardDto message = createCardDto(card, comment, userId);
        kafkaTemplate.send(ordersTopic, message.getTraceId(), message);
    }

    @Override
    public void sendWithWait(Card card, String comment, String userId) {

        OrderCreateCardDto message = createCardDto(card, comment, userId);

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

    private OrderCreateCardDto createCardDto(Card card, String comment, String userId) {
        String traceId = UUID.randomUUID().toString();
        return OrderCreateCardDto.builder()
                .traceId(traceId)
                .orderType(OrderDto.OrderType.CARD)
                .userId(userId)
                .cardId(card.getId())
                .cardType(card.getType())
                .spendingLimit(card.getSpendingLimit())
                .comment(comment)
                .createdAt(Instant.now())
                .build();
    }
}
