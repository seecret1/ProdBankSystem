package com.github.seecret1.delivery_service.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DltConsumer {

    @KafkaListener(
            topics = "${app.kafka.dlt-topic}",
            groupId = "${app.kafka.dlt-group-id}",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void dltListen(
            ConsumerRecord<String, Object> message,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_TOPIC, required = false) String originalTopic,
            @Header(value = KafkaHeaders.DLT_ORIGINAL_TIMESTAMP, required = false) Long originalTimestamp
    ) {
        var dto = message.value();
        log.error("Message in Dead Letter Topic (DLT) - Failed after all retry attempts");
        log.error("Delivery Order: {}", dto);
        log.error("DLT Message Info: topic={}, partition={}, offset={}, timestamp={}",
                message.topic(), message.partition(), message.offset(), message.timestamp());
        log.error("Original Message Info: topic={}, timestamp={}", originalTopic, originalTimestamp);
        log.error("Exception that caused DLT: {}", exception);
    }
}
