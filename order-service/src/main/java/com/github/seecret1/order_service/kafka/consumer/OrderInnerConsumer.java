package com.github.seecret1.order_service.kafka.consumer;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.service.OrderCardProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInnerConsumer {

    private final OrderCardProcessingService service;

    @KafkaListener(
            topics = "${app.kafka.inner-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "orderCardKafkaListenerContainerFactory"
    )
    public void createCard(ConsumerRecord<String, OrderCardDto> record) {
        OrderCardDto message = record.value();
        log.info("Order received: traceId={}, userId={}, cardId={}, createdAt={}",
                message.getTraceId(), message.getUserId(), message.getCardId(), message.getCreatedAt());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            service.createOrder(message);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", record.topic(), message.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}
