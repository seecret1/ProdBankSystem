package com.github.seecret1.delivery_service.kafka.listener;

import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
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
            @Payload OrderDeliveryDto message,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception
    ) {
        log.error("Message in DLT - OrderDeliveryDto: {}; Original Topic: {}; Offset: {}", message, topic, offset);
        log.error("Original exception in DeliveryDto: {}", exception);
    }
}
