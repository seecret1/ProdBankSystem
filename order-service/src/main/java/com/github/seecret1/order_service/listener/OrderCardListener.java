package com.github.seecret1.order_service.listener;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.entity.OrderType;
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

    private final OrderCardService orderCardService;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "orderCardKafkaListenerContainerFactory"
    )
    public void listenOrderCard(
            @Payload OrderCardDto order,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Order received: traceId={}, userId={}, cardId={}, createdAt={}",
                order.getTraceId(), order.getUserId(), order.getCardId(), order.getCreatedAt());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, Instant.ofEpochMilli(timestamp));

        orderCardService.createOrder(order);
    }
}
