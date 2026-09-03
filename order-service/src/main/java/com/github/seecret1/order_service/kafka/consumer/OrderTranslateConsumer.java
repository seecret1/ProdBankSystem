package com.github.seecret1.order_service.kafka.consumer;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.service.processed.OrderCardProcessing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTranslateConsumer {

    private final OrderCardProcessing orderProcessing;

    @KafkaListener(
            topics = "${app.kafka.translate-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "responseOrderCardKafkaListenerContainerFactory"
    )
    public void listenBaseMessage(
            ConsumerRecord<String, BaseMessage> record
    ) {
        BaseMessage message = record.value();
        log.info("Order received: traceId={}, userId={}, productId={}, orderStatus={} timestamp={}",
                message.getTraceId(), message.getUserId(), message.getProductId(),
                message.getStatus(), message.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            orderProcessing.processMessageOnInvoiceService(message);
            log.debug("[topic: {}][traceId: {}]Send response in cards topic", record.topic(), message.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}
