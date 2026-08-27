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
public class OrderResponseMessageConsumer {

    private final OrderCardProcessing orderProcessing;

    @KafkaListener(
            topics = "${app.kafka.response-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "responseOrderCardKafkaListenerContainerFactory"
    )
    public void listenBaseMessageInResponseTopic(
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
