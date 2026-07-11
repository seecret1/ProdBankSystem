package com.github.seecret1.cardservice.kafka.listener;

import com.github.seecret1.cardservice.order.OrderStatus;
import com.github.seecret1.cardservice.order.message.OrderCardResponse;
import com.github.seecret1.cardservice.order.message.OrderMessage;
import com.github.seecret1.cardservice.service.CardService;
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
public class OrderListener {

    private final CardService service;

    @KafkaListener(
            topics = "${app.kafka.response-topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "responseCardKafkaListenerContainerFactory"
    )
    public void listenOrderCardResponses(
            @Payload OrderMessage<OrderCardResponse> order,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Response order received: traceId={} orderId={}, userId={}, cardId={}, createdAt={}",
                order.getTraceId(), order.getOrderId(), order.getUserId(), order.getData().getCardId(), order.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, Instant.ofEpochMilli(timestamp));

        // TODO: временная обработка
        if (order.getStatus() == OrderStatus.SUCCESS) {
            String cardId = order.getData().getCardId();
            service.activateCard(cardId);
            log.info("Activated card: {}", cardId);
        }

        log.debug("Response order body: {}", order);
    }
}
