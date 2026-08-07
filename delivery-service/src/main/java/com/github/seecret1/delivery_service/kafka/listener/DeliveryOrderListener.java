package com.github.seecret1.delivery_service.kafka.listener;

import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import com.github.seecret1.delivery_service.kafka.producer.DeliveryKafkaProducerService;
import com.github.seecret1.delivery_service.service.DeliveryService;
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
public class DeliveryOrderListener {

    private final DeliveryService deliveryService;

    private final DeliveryKafkaProducerService deliveryKafkaProducerService;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "deliveryKafkaListenerContainerFactory"
    )
    public void listenDeliveryOrder(
            @Payload OrderDeliveryDto event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Order delivery received: traceId={}, userId={}, originAddress={}, destinationAddress={}, createdAt={}",
                event.getTraceId(), event.getUserId(), event.getOriginAddress(), event.getDestinationAddress(), event.getCreatedAt());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, Instant.ofEpochMilli(timestamp));

        try {
            deliveryService.create(event);
            log.info("Order delivery processed successfully: traceId={}", event.getTraceId());
        } catch (Exception ex) {
            deliveryKafkaProducerService.sendToRetry(event, ex, 1);
            log.error("Error processing order delivery: traceId={}", event.getTraceId(), ex);
        }
    }
}
