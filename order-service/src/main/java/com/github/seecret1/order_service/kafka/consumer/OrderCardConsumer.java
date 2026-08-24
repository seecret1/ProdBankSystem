package com.github.seecret1.order_service.kafka.consumer;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.service.processed.OrderProcessing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCardConsumer {

    private final OrderProcessing orderProcessing;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "orderCardKafkaListenerContainerFactory"
    )
    public void listenOrderCard(
            ConsumerRecord<String, OrderCardDto> order
    ) {
        OrderCardDto dto = order.value();
        log.info("Order received: traceId={}, userId={}, cardId={}, createdAt={}",
                dto.getTraceId(), dto.getUserId(), dto.getCardId(), dto.getCreatedAt());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                order.key(), order.partition(), order.topic(), Instant.ofEpochMilli(order.timestamp()));

        try {
            orderProcessing.processOrder(dto);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", order.topic(), dto.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }

    @KafkaListener(
            topics = "${app.kafka.response-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "responseOrderCardKafkaListenerContainerFactory"
    )
    public void listenBaseMessage(
            ConsumerRecord<String, BaseMessage> record
    ) {
        BaseMessage message = record.value();
        log.info("Order received: traceId={}, userId={}, cardId={}, timestamp={}",
                message.getTraceId(), message.getUserId(), message.getProductId(), message.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            orderProcessing.processMessage(message);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", record.topic(), message.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}
