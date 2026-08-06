package com.github.seecret1.order_service.kafka.listener;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import lombok.extern.slf4j.Slf4j;
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
            @Payload OrderCardDto event,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception
    ) {
        logging(event, topic, offset, exception);
    }

    @KafkaListener(
            topics = "${app.kafka.dlt-topic}",
            groupId = "${app.kafka.dlt-group-id}",
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void dltListen(
            @Payload BaseMessage event,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception
    ) {
        logging(event, topic, offset, exception);
    }

    private static void logging(Object event, String topic, long offset, String ex) {
        log.error("Message in DLT - Order: {}; Original Topic: {}; Offset: {}", event, topic, offset);
        log.error("Original exception: {}", ex);
    }
}
