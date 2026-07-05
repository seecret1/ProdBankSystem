package com.github.seecret1.order_service.listener;

import com.github.seecret1.order_service.dto.card.OrderCreateCardDto;
import com.github.seecret1.order_service.exception.OrderCardCreationException;
import com.github.seecret1.order_service.service.OrderCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCardListener {

    private final OrderCardService service;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "orderCardKafkaListenerContainerFactory"
    )
    public void listenOrderCard(
            @Payload OrderCreateCardDto order,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Order received: traceId={} orderId={}, userId={}, cardId={}, createdAt={}",
                order.getTraceId(), order.getOrderId(), order.getUserId(), order.getCardId(), order.getCreatedAt());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, Instant.ofEpochMilli(timestamp));

        log.debug("Order body: {}", order);

        try {
            order.validate();
            service.createOrder(order);
        } catch(Exception ex) {
            log.error("Order not validate: {}", order);
            throw new OrderCardCreationException("Order not valid");
        }
    }
}
