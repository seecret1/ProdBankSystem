package com.github.seecret1.cardservice.kafka.listener;

import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.service.OrderProcessingService;
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
public class RetryListener {

    private final OrderProcessingService orderProcessingService;

    @KafkaListener(
            topics = "${app.kafka.retry-topic}",
            groupId = "${app.kafka.retry-group-id}",
            containerFactory = "retryCardKafkaListenerContainerFactory"
    )
    public void listenOrderCardResponses(
            @Payload BaseMessage order,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Response order received: traceId={} orderId={}, userId={}, cardData={}, createdAt={}",
                order.getTraceId(), order.getOrderId(), order.getUserId(), order.getProductId(), order.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, Instant.ofEpochMilli(timestamp));

        try {
            orderProcessingService.orderProcessing(order);
            log.debug("Received response order body: {}", order);
        } catch (Exception e) {
            log.error("Error processing order: traceId={}, orderId={}, error={}",
                    order.getTraceId(), order.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}
