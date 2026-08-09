package com.github.seecret1.order_service.kafka.listener;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.kafka.producer.OrderKafkaProducerService;
import com.github.seecret1.order_service.service.OrderCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCardConsumer {

    private final OrderKafkaProducerService orderKafkaProducerService;

    private final OrderCardService orderCardService;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "orderCardKafkaListenerContainerFactory"
    )
    public void listenOrderCard(
            ConsumerRecord<String, OrderCardDto> order
    ) {
        OrderCardDto dto = order.value();
        log.info("Order received: traceId={}, userId={}, cardId={}, createdAt={}",
                dto.getTraceId(), dto.getUserId(), dto.getCardId(), dto.getCreatedAt());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                order.key(), order.partition(), order.topic(), order.timestamp());

        try {
            orderCardService.createOrder(dto);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", order.topic(), dto.getTraceId());
        } catch (Exception ex) {
            orderKafkaProducerService.sendToRetry(dto, ex,1);
            log.error("Error while creating order", ex);
        }
    }
}
