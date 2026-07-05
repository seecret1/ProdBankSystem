package com.github.seecret1.order_service.listener;

import com.github.seecret1.order_service.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DltListener {

    @KafkaListener(
            topics = "${app.kafka.dlt-topic}",
            groupId = "${app.kafka.dlt-group-id}",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void dltListen(
            @Payload OrderDto event,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception
    ) {
        log.error("Message in DLT - Order: {}; Original Topic: {}; Offset: {}", event, topic, offset);
        log.error("Original exception: {}", exception);
    }

    @DltHandler
    public void dltListener(
            @Payload OrderDto event,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exception
    ) {
        log.error("Message moved to DLT - Order: {}; Original Topic: {}; Offset: {}; Exception: {}",
                event, topic, offset, exception);
        log.debug("Handle order: {}", event);
    }
}
