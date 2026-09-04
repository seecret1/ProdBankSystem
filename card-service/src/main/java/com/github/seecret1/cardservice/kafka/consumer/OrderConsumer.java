package com.github.seecret1.cardservice.kafka.consumer;

import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.service.processing.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderProcessingService orderProcessingServiceImpl;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "cardKafkaListenerContainerFactory"
    )
    public void listenOrderCardResponses(
            ConsumerRecord<String, BaseMessage> order
    ) {
        BaseMessage message = order.value();
        log.info("Response order received: traceId={} orderId={}, userId={}, cardId={}, createdAt={}, data={}",
                message.getTraceId(), message.getOrderId(), message.getUserId(),
                message.getProductId(), message.getTimestamp(), message.getData());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                order.key(), order.partition(), order.topic(), Instant.ofEpochMilli(order.timestamp()));

        try {
            orderProcessingServiceImpl.orderProcessing(message);
            log.debug("Received response order body: {}", message);
        } catch (Exception e) {
            log.error("Error processing order: traceId={}, orderId={}, error={}",
                    message.getTraceId(), message.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}
