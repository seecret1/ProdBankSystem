package com.github.seecret1.cardservice.kafka.consumer;

import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.service.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryConsumer {

    private final OrderProcessingService orderProcessingService;

    @KafkaListener(
            topics = "${app.kafka.retry-topic}",
            groupId = "${app.kafka.retry-group-id}",
            containerFactory = "retryCardKafkaListenerContainerFactory"
    )
    public void listenOrderCardResponses(
            ConsumerRecord<String, BaseMessage> order
    ) {
        BaseMessage message = order.value();
        log.info("Response order received: traceId={} orderId={}, userId={}, cardData={}, createdAt={}",
                message.getTraceId(), message.getOrderId(), message.getUserId(), message.getProductId(), message.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                order.key(), order.partition(), order.topic(), order.timestamp());

        try {
            orderProcessingService.orderProcessing(message);
            log.debug("Received retry response order body: {}", order);
        } catch (Exception e) {
            log.error("Error retry processing order: traceId={}, orderId={}, error={}",
                    message.getTraceId(), message.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
}
