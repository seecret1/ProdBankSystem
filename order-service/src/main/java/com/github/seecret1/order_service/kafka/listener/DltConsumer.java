package com.github.seecret1.order_service.kafka.listener;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
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
    public void listenDlt(
            ConsumerRecord<String, OrderCardDto> event,
            @Header(value = KafkaHeaders.DLT_EXCEPTION_MESSAGE, required = false) String exception
    ) {
        log.error("Message in DLT - Order: {}; Original Topic: {}; Offset: {}", event, event.topic(), event.offset());
        log.error("Original exception: {}", exception);
    }
}
