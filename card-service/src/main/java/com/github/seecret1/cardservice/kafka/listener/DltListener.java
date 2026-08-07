package com.github.seecret1.cardservice.kafka.listener;

import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
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
            containerFactory = "dltCardsKafkaListenerContainerFactory"
    )
    public void listenDlt(
            @Payload BaseMessage message,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception
    ) {
        log.error("Message in DLT - Order: {}; Original Topic: {}; Offset: {}", message, topic, offset);
        log.error("Original exception: {}", exception);
    }
}
